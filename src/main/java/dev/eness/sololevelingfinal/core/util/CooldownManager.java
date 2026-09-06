package dev.eness.sololevelingfinal.core.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public final class CooldownManager {
   private static final String PREFIX = "cd_";
   private static final String FULL_DURATION_PREFIX = "slr_cd_full_";
   private static final String SNAPSHOT_V2 = "v2@";
   private static final Map<Entity, CooldownManager.ClientSnapshotClock> CLIENT_SNAPSHOT_CLOCKS = Collections.synchronizedMap(new WeakHashMap<>());

   private CooldownManager() {
   }

   public static void set(Entity entity, String key, int durationTicks) {
      setInternal(entity, key, durationTicks, false);
   }

   public static void setFullDuration(Entity entity, String key, int durationTicks) {
      setInternal(entity, key, durationTicks, true);
   }

   private static void setInternal(Entity entity, String key, int durationTicks, boolean fullDuration) {
      if (entity != null && !entity.level().isClientSide()) {
         if (isCreativePlayer(entity)) {
            clearStoredCooldown(entity, key);
         } else {
            long expiry = entity.level().getGameTime() + Math.max(0, durationTicks);
            entity.getPersistentData().putLong("cd_" + key, expiry);
            if (fullDuration) {
               entity.getPersistentData().putBoolean("slr_cd_full_" + key, true);
            } else {
               entity.getPersistentData().remove("slr_cd_full_" + key);
            }

            pushSnapshot(entity);
         }
      }
   }

   public static void clear(Entity entity, String key) {
      if (entity != null && !entity.level().isClientSide()) {
         entity.getPersistentData().remove("cd_" + key);
         entity.getPersistentData().remove("slr_cd_full_" + key);
         pushSnapshot(entity);
      }
   }

   public static void discardIfRemainingExceeds(Entity entity, String key, int maximumTicks) {
      if (entity != null && !entity.level().isClientSide()) {
         if (isCreativePlayer(entity)) {
            clearStoredCooldown(entity, key);
         } else {
            long now = entity.level().getGameTime();
            long expiry = entity.getPersistentData().getLong("cd_" + key);
            if (expiry - now > maximumTicks) {
               clear(entity, key);
            }
         }
      }
   }

   public static void clearAll(Entity entity) {
      if (entity != null && !entity.level().isClientSide()) {
         entity.getPersistentData()
            .getAllKeys()
            .stream()
            .filter(key -> key.startsWith("cd_") || key.startsWith("slr_cd_full_"))
            .toList()
            .forEach(entity.getPersistentData()::remove);
         pushSnapshot(entity);
      }
   }

   public static boolean isOnCooldown(Entity entity, String key) {
      if (entity == null) {
         return false;
      }

      trimCreativeCooldown(entity, key);
      return getRemainingTicks(entity, key) > 0;
   }

   public static int getRemainingTicks(Entity entity, String key) {
      if (entity == null) {
         return 0;
      }

      if (isCreativePlayer(entity)) {
         trimCreativeCooldown(entity, key);
         return 0;
      }

      if (entity.level().isClientSide()) {
         return getClientRemainingTicks(entity, key);
      }

      long expiry = entity.getPersistentData().getLong("cd_" + key);
      int remaining = (int)Math.max(0L, expiry - entity.level().getGameTime());
      if (remaining == 0) {
         entity.getPersistentData().remove("slr_cd_full_" + key);
      }

      return remaining;
   }

   public static int getRemainingSeconds(Entity entity, String key) {
      int ticks = getRemainingTicks(entity, key);
      return ticks == 0 ? 0 : (int)Math.ceil(ticks / 20.0);
   }

   private static void trimCreativeCooldown(Entity entity, String key) {
      if (entity != null && !entity.level().isClientSide() && isCreativePlayer(entity)) {
         clearStoredCooldown(entity, key);
      }
   }

   private static void clearStoredCooldown(Entity entity, String key) {
      boolean changed = entity.getPersistentData().contains("cd_" + key) || entity.getPersistentData().contains("slr_cd_full_" + key);
      entity.getPersistentData().remove("cd_" + key);
      entity.getPersistentData().remove("slr_cd_full_" + key);
      if (changed) {
         pushSnapshot(entity);
      }
   }

   private static boolean isCreativePlayer(Entity entity) {
      return entity instanceof Player player && player.isCreative();
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      pushSnapshot(event.getEntity());
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      pushSnapshot(event.getEntity());
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      pushSnapshot(event.getEntity());
   }

   @SubscribeEvent
   public static void onCreativePlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player.isCreative()) {
         event.player.getCooldowns().removeCooldown(SololevelingModItems.DEMON_KINGS_LONG_SWORD.get());
         event.player.getCooldowns().removeCooldown(SololevelingModItems.KATANA_STIER.get());
         event.player.getCooldowns().removeCooldown(SololevelingModItems.MANA_GUN.get());
         if (!event.player.level().isClientSide() && hasStoredCooldowns(event.player)) {
            clearAll(event.player);
         }
      }
   }

   private static boolean hasStoredCooldowns(Entity entity) {
      return entity.getPersistentData().getAllKeys().stream().anyMatch(key -> key.startsWith("cd_") || key.startsWith("slr_cd_full_"));
   }

   private static void pushSnapshot(Entity entity) {
      if (entity != null && !entity.level().isClientSide() && entity instanceof Player player) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.cooldownData = buildSnapshot(entity);
            capability.syncPlayerVariables(player);
         });
      }
   }

   private static String buildSnapshot(Entity entity) {
      long now = entity.level().getGameTime();
      StringBuilder snapshot = new StringBuilder("v2@").append(now);
      if (isCreativePlayer(entity)) {
         return snapshot.toString();
      }

      for (String nbtKey : entity.getPersistentData().getAllKeys()) {
         if (nbtKey.startsWith("cd_")) {
            long expiry = entity.getPersistentData().getLong(nbtKey);
            if (expiry > now) {
               snapshot.append(';').append(nbtKey, "cd_".length(), nbtKey.length()).append(':').append(expiry - now);
            }
         }
      }

      return snapshot.toString();
   }

   private static int getClientRemainingTicks(Entity entity, String key) {
      String snapshot = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.cooldownData).orElse("");
      if (snapshot == null || snapshot.isEmpty()) {
         CLIENT_SNAPSHOT_CLOCKS.remove(entity);
         return 0;
      }

      if (!snapshot.startsWith("v2@")) {
         long expiry = parseSnapshotValue(snapshot, key);
         return (int)Math.max(0L, expiry - entity.level().getGameTime());
      }

      CooldownManager.ClientSnapshotClock clock = CLIENT_SNAPSHOT_CLOCKS.get(entity);
      if (clock == null || !clock.snapshot.equals(snapshot) || entity.tickCount < clock.receivedAtTick) {
         clock = new CooldownManager.ClientSnapshotClock(snapshot, entity.tickCount);
         CLIENT_SNAPSHOT_CLOCKS.put(entity, clock);
      }

      long initialRemaining = clock.remainingByKey.getOrDefault(key, 0L);
      long elapsed = Math.max(0, entity.tickCount - clock.receivedAtTick);
      return (int)Math.max(0L, initialRemaining - elapsed);
   }

   private static Map<String, Long> parseV2Snapshot(String snapshot) {
      Map<String, Long> values = new HashMap<>();

      for (String entry : snapshot.split(";")) {
         int colon = entry.indexOf(58);
         if (colon >= 0) {
            try {
               values.put(entry.substring(0, colon), Long.parseLong(entry.substring(colon + 1)));
            } catch (NumberFormatException var8) {
            }
         }
      }

      return values;
   }

   private static long parseSnapshotValue(String snapshot, String key) {
      if (snapshot != null && !snapshot.isEmpty()) {
         for (String entry : snapshot.split(";")) {
            int colon = entry.indexOf(58);
            if (colon >= 0 && entry.regionMatches(0, key, 0, colon) && colon == key.length()) {
               try {
                  return Long.parseLong(entry.substring(colon + 1));
               } catch (NumberFormatException ignored) {
                  return 0L;
               }
            }
         }

         return 0L;
      } else {
         return 0L;
      }
   }

   private static final class ClientSnapshotClock {
      private final String snapshot;
      private final int receivedAtTick;
      private final Map<String, Long> remainingByKey;

      private ClientSnapshotClock(String snapshot, int receivedAtTick) {
         this.snapshot = snapshot;
         this.receivedAtTick = receivedAtTick;
         this.remainingByKey = CooldownManager.parseV2Snapshot(snapshot);
      }
   }
}
