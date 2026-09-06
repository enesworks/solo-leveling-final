package dev.eness.sololevelingfinal.core.party;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.EntityHighlightSystem;

@EventBusSubscriber
public final class PartyHighlightManager {
   private static final int MAX_TARGETS = 7;
   private static final Map<UUID, PartyHighlightManager.ViewerState> VIEWERS = new HashMap<>();

   private PartyHighlightManager() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer viewer && viewer.tickCount % 20 == 0) {
         sync(viewer);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer viewer) {
         VIEWERS.remove(viewer.getUUID());
         sync(viewer);
      }
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         removeTargetFromOtherViewers(player);
         clearViewer(player, true);
         sync(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         removeTargetFromOtherViewers(player);
         VIEWERS.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      clearServer(event.getServer());
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      VIEWERS.clear();
   }

   public static void syncNow(ServerPlayer viewer) {
      if (viewer != null && !viewer.hasDisconnected()) {
         sync(viewer);
      }
   }

   public static void clearNow(ServerPlayer viewer) {
      if (viewer != null) {
         clearViewer(viewer, true);
      }
   }

   private static void sync(ServerPlayer viewer) {
      if (viewer != null && !viewer.hasDisconnected()) {
         PartyService.reconcilePlayer(viewer);
         if (!PartyService.glowEnabled(viewer)) {
            clearViewer(viewer, false);
         } else {
            int color = PartyService.glowColor(viewer) & 16777215;
            Map<UUID, ResourceKey<Level>> desired = desiredTargets(viewer);
            PartyHighlightManager.ViewerState state = VIEWERS.computeIfAbsent(viewer.getUUID(), ignored -> new PartyHighlightManager.ViewerState());
            boolean colorChanged = state.color != color;

            for (Entry<UUID, ResourceKey<Level>> previous : List.copyOf(state.targets.entrySet())) {
               ResourceKey<Level> currentDimension = desired.get(previous.getKey());
               if (currentDimension == null || !currentDimension.equals(previous.getValue())) {
                  EntityHighlightSystem.hide(viewer, previous.getKey(), previous.getValue(), "party:members");
               }
            }

            for (Entry<UUID, ResourceKey<Level>> target : desired.entrySet()) {
               ResourceKey<Level> previousDimension = state.targets.get(target.getKey());
               if (colorChanged || previousDimension == null || !previousDimension.equals(target.getValue())) {
                  EntityHighlightSystem.show(viewer, target.getKey(), target.getValue(), "party:members", color, 0, 80);
               }
            }

            state.targets.clear();
            state.targets.putAll(desired);
            state.color = color;
            if (state.targets.isEmpty()) {
               VIEWERS.remove(viewer.getUUID());
            }
         }
      }
   }

   private static Map<UUID, ResourceKey<Level>> desiredTargets(ServerPlayer viewer) {
      ResourceKey<Level> viewerDimension = viewer.level().dimension();
      Map<UUID, ResourceKey<Level>> desired = new LinkedHashMap<>();
      PartyService.onlineMembers(viewer)
         .stream()
         .filter(member -> validTarget(viewer, member, viewerDimension))
         .sorted(Comparator.comparing(member -> member.getUUID().toString()))
         .limit(7L)
         .forEach(member -> desired.put(member.getUUID(), member.level().dimension()));
      return desired;
   }

   private static boolean validTarget(ServerPlayer viewer, ServerPlayer member, ResourceKey<Level> viewerDimension) {
      return member != null
         && member != viewer
         && !member.getUUID().equals(viewer.getUUID())
         && !member.hasDisconnected()
         && member.isAlive()
         && !member.isRemoved()
         && !member.isSpectator()
         && member.level().dimension().equals(viewerDimension);
   }

   private static void removeTargetFromOtherViewers(ServerPlayer target) {
      MinecraftServer server = target.getServer();
      if (server != null) {
         for (Entry<UUID, PartyHighlightManager.ViewerState> entry : List.copyOf(VIEWERS.entrySet())) {
            if (!entry.getKey().equals(target.getUUID())) {
               ResourceKey<Level> dimension = entry.getValue().targets.remove(target.getUUID());
               if (dimension != null) {
                  ServerPlayer viewer = server.getPlayerList().getPlayer(entry.getKey());
                  if (viewer != null && !viewer.hasDisconnected()) {
                     EntityHighlightSystem.hide(viewer, target.getUUID(), dimension, "party:members");
                  }

                  if (entry.getValue().targets.isEmpty()) {
                     VIEWERS.remove(entry.getKey());
                  }
               }
            }
         }
      }
   }

   private static void clearViewer(ServerPlayer viewer, boolean forcePacket) {
      PartyHighlightManager.ViewerState removed = VIEWERS.remove(viewer.getUUID());
      if (forcePacket || removed != null) {
         EntityHighlightSystem.clearSource(viewer, "party:members");
      }
   }

   private static void clearServer(MinecraftServer server) {
      for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
         EntityHighlightSystem.clearSource(viewer, "party:members");
      }

      VIEWERS.clear();
   }

   private static final class ViewerState {
      private final Map<UUID, ResourceKey<Level>> targets = new HashMap<>();
      private int color = -1;
   }
}
