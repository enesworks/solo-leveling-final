package dev.eness.sololevelingfinal.core.dkc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.DkcQuestProgressMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;

@EventBusSubscriber(modid = "sololeveling")
public final class DkcQuestProgressTracker {
   private static final int SYNC_INTERVAL_TICKS = 10;
   private static final int MIN_PRESS_INTERVAL_TICKS = 1;
   private static final Map<MinecraftServer, Map<UUID, DkcQuestProgressTracker.Snapshot>> LAST_SENT = new WeakHashMap<>();
   private static final Map<MinecraftServer, Set<UUID>> HELD = new WeakHashMap<>();
   private static final Map<MinecraftServer, Map<UUID, Long>> PRESS_ALLOWED_AT = new WeakHashMap<>();

   private DkcQuestProgressTracker() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null && player.server != null) {
         unmarkHeld(player.server, player.getUUID());
         forget(player.server, player.getUUID());
         forgetPressLimit(player.server, player.getUUID());
         send(player, DkcQuestProgressTracker.Snapshot.INACTIVE);
      }
   }

   public static void tick(ServerPlayer player) {
      if (player != null && player.server != null && player.tickCount % 10 == Math.floorMod(player.getId(), 10) && isHeld(player)) {
         syncIfChanged(player);
      }
   }

   public static synchronized boolean acceptPress(ServerPlayer player) {
      if (player != null && player.server != null && !isHeld(player)) {
         long now = player.serverLevel().getGameTime();
         Map<UUID, Long> serverLimits = PRESS_ALLOWED_AT.computeIfAbsent(player.server, ignored -> new HashMap<>());
         if (now < serverLimits.getOrDefault(player.getUUID(), 0L)) {
            return false;
         }

         serverLimits.put(player.getUUID(), now + 1L);
         return true;
      } else {
         return false;
      }
   }

   public static void beginTracking(ServerPlayer player) {
      if (player != null && player.server != null) {
         if (markHeld(player)) {
            syncNow(player);
         } else {
            syncIfChanged(player);
         }
      }
   }

   private static void syncNow(ServerPlayer player) {
      DkcQuestProgressTracker.Snapshot snapshot = snapshot(player);
      remember(player, snapshot);
      send(player, snapshot);
   }

   public static void stopTracking(ServerPlayer player) {
      if (player != null && player.server != null) {
         unmarkHeld(player.server, player.getUUID());
         forget(player.server, player.getUUID());
      }
   }

   private static void syncIfChanged(ServerPlayer player) {
      DkcQuestProgressTracker.Snapshot snapshot = snapshot(player);
      if (rememberIfChanged(player, snapshot)) {
         send(player, snapshot);
      }
   }

   private static DkcQuestProgressTracker.Snapshot snapshot(ServerPlayer player) {
      int floor = DkcSpatialLayout.floor(player);
      if (floor > 0 && player.getPersistentData().getBoolean("dkc_inside_castle")) {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         int cleared = Mth.clamp((int)Math.floor(vars.dkc_cleared), 0, 20);
         String floorName = DkcFloorRegistry.name(floor);
         CompoundTag data = player.getPersistentData();
         if (!DkcRunSavedData.get(player.server).isGenerated(player, floor)) {
            return new DkcQuestProgressTracker.Snapshot(
               true,
               floor,
               cleared,
               floorName,
               "ascent",
               "This floor is taking shape.",
               "Your progress is secured. The encounter will begin when construction finishes.",
               0,
               0
            );
         } else if (floor == 15 && cleared >= 20 && !vars.radiru_slaughtered && DkcQuestManager.hasRadiruCastleAccess(player)) {
            return new DkcQuestProgressTracker.Snapshot(
               true,
               floor,
               cleared,
               floorName,
               "sanctuary",
               "House Radiru remains under your protection.",
               "The training grounds are open. The ascent tower is permanently sealed.",
               0,
               0
            );
         } else if (cleared >= floor) {
            return clearedFloorSnapshot(player, vars, floor, cleared, floorName);
         } else if (floor == 1) {
            return new DkcQuestProgressTracker.Snapshot(
               true,
               floor,
               cleared,
               floorName,
               "boss",
               "Defeat Cerberus.",
               "The gatekeeper blocks the first ascension.",
               data.getBoolean("dkc_floor_1_boss_defeated") ? 1 : 0,
               1
            );
         } else {
            return floor == 20 ? throneSnapshot(data, floor, cleared, floorName) : waveSnapshot(data, floor, cleared, floorName);
         }
      } else {
         return DkcQuestProgressTracker.Snapshot.INACTIVE;
      }
   }

   private static DkcQuestProgressTracker.Snapshot clearedFloorSnapshot(
      ServerPlayer player, SololevelingModVariables.PlayerVariables vars, int floor, int cleared, String floorName
   ) {
      if (floor == 20) {
         String detail = !vars.radiru_slaughtered && DkcQuestManager.hasRadiruCastleAccess(player)
            ? "Use the return shrine. Radiru Castle is now available from System Quests."
            : "Use the throne return shrine to leave the conquered castle.";
         return new DkcQuestProgressTracker.Snapshot(true, floor, cleared, floorName, "conquered", "The Demon King's Castle has been conquered.", detail, 0, 0);
      }

      DkcRunSavedData runs = DkcRunSavedData.get(player.server);
      boolean armed = runs.isTransitionArmed(player, floor);
      boolean generated = runs.isGenerated(player, floor + 1);
      if (armed && generated) {
         return new DkcQuestProgressTracker.Snapshot(
            true, floor, cleared, floorName, "ascent", "The tower path is open.", "Enter the tower to ascend to Floor " + (floor + 1) + ".", 0, 0
         );
      }

      if (armed) {
         return new DkcQuestProgressTracker.Snapshot(
            true,
            floor,
            cleared,
            floorName,
            "ascent",
            "Floor " + (floor + 1) + " is taking shape.",
            "Your permit claim is secured. The tower will open when construction finishes.",
            0,
            0
         );
      }

      String objective = "Floor objective complete.";
      if (floor == 15 && vars.radiru_pact && !vars.radiru_slaughtered) {
         objective = "Esil's Entry Permit has been granted.";
      } else if (floor == 15 && vars.radiru_slaughtered) {
         objective = "House Radiru has fallen.";
      }

      return new DkcQuestProgressTracker.Snapshot(
         true, floor, cleared, floorName, "permit", objective, "Claim or present an Entry Permit at the crimson tower pedestal.", 0, 0
      );
   }

   private static DkcQuestProgressTracker.Snapshot waveSnapshot(CompoundTag data, int floor, int cleared, String floorName) {
      String prefix = "dkc_floor_" + floor;
      int required = DkcFloorRegistry.requiredKills(floor);
      int kills = Mth.clamp((int)Math.floor(data.getDouble(prefix + "_killed")), 0, required);
      boolean complete = data.getBoolean(prefix + "_complete");
      if (floor == 10 && complete) {
         return new DkcQuestProgressTracker.Snapshot(
            true,
            floor,
            cleared,
            floorName,
            "boss",
            "Defeat Vulcan.",
            "The Crucible guardian has entered the battlefield.",
            data.getBoolean("dkc_floor_10_boss_defeated") ? 1 : 0,
            1
         );
      }

      if (floor == 15 && complete) {
         return new DkcQuestProgressTracker.Snapshot(
            true,
            floor,
            cleared,
            floorName,
            "radiru",
            "House Radiru has surrendered.",
            "Enter the castle. Right-click Esil to accept the permit - or betray House Radiru.",
            required,
            required
         );
      }

      if (complete) {
         return new DkcQuestProgressTracker.Snapshot(
            true, floor, cleared, floorName, "permit", "Floor objective complete.", "Your Entry Permit is being issued.", required, required
         );
      }

      String objective = floor == 15 ? "Overpower House Radiru's defenders." : (floor == 10 ? "Defeat the Crucible defenders." : "Defeat the floor defenders.");
      String detail = !data.getBoolean(prefix + "_spawned")
         ? "The System is initializing this floor's encounter."
         : (!data.getBoolean(prefix + "_initial_spawned") ? "The defenders are gathering..." : "");
      return new DkcQuestProgressTracker.Snapshot(true, floor, cleared, floorName, floor == 15 ? "radiru" : "wave", objective, detail, kills, required);
   }

   private static DkcQuestProgressTracker.Snapshot throneSnapshot(CompoundTag data, int floor, int cleared, String floorName) {
      boolean baranDown = data.getBoolean("dkc_floor_20_baran_defeated");
      boolean kaiselinDown = data.getBoolean("dkc_floor_20_kaiselin_defeated");
      int progress = (baranDown ? 1 : 0) + (kaiselinDown ? 1 : 0);
      String detail;
      if (progress == 0) {
         detail = "Baran and Kaiselin still guard the throne.";
      } else if (baranDown) {
         detail = "Baran defeated - Kaiselin remains.";
      } else {
         detail = "Kaiselin defeated - Baran remains.";
      }

      return new DkcQuestProgressTracker.Snapshot(true, floor, cleared, floorName, "boss", "Defeat both throne guardians.", detail, progress, 2);
   }

   private static SololevelingModVariables.PlayerVariables variables(ServerPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static void send(ServerPlayer player, DkcQuestProgressTracker.Snapshot snapshot) {
      SololevelingMod.PACKET_HANDLER
         .send(
            PacketDistributor.PLAYER.with(() -> player),
            new DkcQuestProgressMessage(
               snapshot.active,
               snapshot.floor,
               snapshot.cleared,
               snapshot.floorName,
               snapshot.phase,
               snapshot.objective,
               snapshot.detail,
               snapshot.progress,
               snapshot.target
            )
         );
   }

   private static synchronized void remember(ServerPlayer player, DkcQuestProgressTracker.Snapshot snapshot) {
      LAST_SENT.computeIfAbsent(player.server, ignored -> new HashMap<>()).put(player.getUUID(), snapshot);
   }

   private static synchronized boolean rememberIfChanged(ServerPlayer player, DkcQuestProgressTracker.Snapshot snapshot) {
      Map<UUID, DkcQuestProgressTracker.Snapshot> serverCache = LAST_SENT.computeIfAbsent(player.server, ignored -> new HashMap<>());
      DkcQuestProgressTracker.Snapshot previous = serverCache.put(player.getUUID(), snapshot);
      return !snapshot.equals(previous);
   }

   private static synchronized void forget(MinecraftServer server, UUID playerId) {
      Map<UUID, DkcQuestProgressTracker.Snapshot> serverCache = LAST_SENT.get(server);
      if (serverCache != null) {
         serverCache.remove(playerId);
         if (serverCache.isEmpty()) {
            LAST_SENT.remove(server);
         }
      }
   }

   private static synchronized boolean markHeld(ServerPlayer player) {
      return HELD.computeIfAbsent(player.server, ignored -> new HashSet<>()).add(player.getUUID());
   }

   private static synchronized boolean isHeld(ServerPlayer player) {
      Set<UUID> heldPlayers = HELD.get(player.server);
      return heldPlayers != null && heldPlayers.contains(player.getUUID());
   }

   private static synchronized void unmarkHeld(MinecraftServer server, UUID playerId) {
      Set<UUID> heldPlayers = HELD.get(server);
      if (heldPlayers != null) {
         heldPlayers.remove(playerId);
         if (heldPlayers.isEmpty()) {
            HELD.remove(server);
         }
      }
   }

   private static synchronized void forgetPressLimit(MinecraftServer server, UUID playerId) {
      Map<UUID, Long> serverLimits = PRESS_ALLOWED_AT.get(server);
      if (serverLimits != null) {
         serverLimits.remove(playerId);
         if (serverLimits.isEmpty()) {
            PRESS_ALLOWED_AT.remove(server);
         }
      }
   }

   @SubscribeEvent
   public static void onChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (isHeld(player)) {
            syncNow(player);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> capability.questinfo = false);
         stopTracking(player);
         forgetPressLimit(player.server, player.getUUID());
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> capability.questinfo = false);
         stopTracking(player);
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> capability.questinfo = false);
         stopTracking(player);
         forgetPressLimit(player.server, player.getUUID());
      }
   }

   @SubscribeEvent
   public static synchronized void onServerStopped(ServerStoppedEvent event) {
      LAST_SENT.remove(event.getServer());
      HELD.remove(event.getServer());
      PRESS_ALLOWED_AT.remove(event.getServer());
   }

   private record Snapshot(boolean active, int floor, int cleared, String floorName, String phase, String objective, String detail, int progress, int target) {
      private static final DkcQuestProgressTracker.Snapshot INACTIVE = new DkcQuestProgressTracker.Snapshot(false, 0, 0, "", "", "", "", 0, 0);
   }
}
