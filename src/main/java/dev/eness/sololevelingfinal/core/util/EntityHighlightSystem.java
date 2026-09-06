package dev.eness.sololevelingfinal.core.util;

import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonMobLevelAdapter;
import dev.eness.sololevelingfinal.core.network.EntityHighlightMessage;

public final class EntityHighlightSystem {
   public static final String SOURCE_PARTY_MEMBERS = "party:members";
   public static final String SOURCE_SHADOW_GLOW = "shadow:glow";
   public static final int COLOR_WAVE_NORMAL = 5822719;
   public static final int COLOR_WAVE_ELITE = 16757575;
   public static final int COLOR_WAVE_BOSS = 16724047;
   public static final int COLOR_PERCEPTION_HOSTILE = 16740419;
   public static final int COLOR_PERCEPTION_PLAYER = 5617919;
   public static final int COLOR_PERCEPTION_NEUTRAL = 15913822;
   public static final int PRIORITY_PARTY = 80;
   public static final int PRIORITY_SHADOW_GLOW = 90;
   public static final int PRIORITY_PERCEPTION = 100;
   public static final int PRIORITY_DUNGEON_NORMAL = 200;
   public static final int PRIORITY_DUNGEON_ELITE = 250;
   public static final int PRIORITY_DUNGEON_BOSS = 300;
   private static final int MAX_DURATION_TICKS = 1200000;
   private static final int MAX_PRIORITY = 10000;
   private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "soloboss"));
   private static final TagKey<EntityType<?>> PORTAL_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "portals"));
   private static final TagKey<EntityType<?>> SIDE_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "side"));

   private EntityHighlightSystem() {
   }

   public static void show(ServerPlayer viewer, Entity target, String source, int rgb, int durationTicks, int priority) {
      if (target != null && !target.level().isClientSide()) {
         show(viewer, target.getUUID(), target.level().dimension(), source, rgb, durationTicks, priority);
      }
   }

   public static void show(ServerPlayer viewer, UUID targetId, ResourceKey<Level> dimension, String source, int rgb, int durationTicks, int priority) {
      if (valid(viewer, targetId, dimension)) {
         send(
            viewer,
            new EntityHighlightMessage(
               (byte)0,
               targetId,
               dimension.location(),
               cleanSource(source),
               rgb & 16777215,
               Mth.clamp(durationTicks, 0, 1200000),
               Mth.clamp(priority, 0, 10000)
            )
         );
      }
   }

   public static void hide(ServerPlayer viewer, Entity target, String source) {
      if (target != null) {
         hide(viewer, target.getUUID(), target.level().dimension(), source);
      }
   }

   public static void hide(ServerPlayer viewer, UUID targetId, ResourceKey<Level> dimension, String source) {
      if (valid(viewer, targetId, dimension)) {
         send(viewer, new EntityHighlightMessage((byte)1, targetId, dimension.location(), cleanSource(source), 0, 0, 0));
      }
   }

   public static void clearSource(ServerPlayer viewer, String source) {
      if (viewer != null) {
         send(
            viewer, new EntityHighlightMessage((byte)2, EntityHighlightMessage.NO_TARGET, viewer.level().dimension().location(), cleanSource(source), 0, 0, 0)
         );
      }
   }

   public static void clearAll(ServerPlayer viewer) {
      if (viewer != null) {
         send(viewer, new EntityHighlightMessage((byte)3, EntityHighlightMessage.NO_TARGET, viewer.level().dimension().location(), "default", 0, 0, 0));
      }
   }

   public static String dungeonSource(UUID instanceId) {
      return cleanSource("dungeon:" + instanceId);
   }

   public static int dungeonColor(DungeonMobLevelAdapter.MobRole role) {
      return switch (role == null ? DungeonMobLevelAdapter.MobRole.NORMAL : role) {
         case BOSS -> 16724047;
         case ELITE -> 16757575;
         case NORMAL -> 5822719;
      };
   }

   public static int dungeonPriority(DungeonMobLevelAdapter.MobRole role) {
      return switch (role == null ? DungeonMobLevelAdapter.MobRole.NORMAL : role) {
         case BOSS -> 300;
         case ELITE -> 250;
         case NORMAL -> 200;
      };
   }

   public static int perceptionColor(LivingEntity target) {
      DungeonMobLevelAdapter.MobRole role = DungeonMobLevelAdapter.MobRole.fromString(target.getPersistentData().getString("slr_dungeon_role"));
      if (role == DungeonMobLevelAdapter.MobRole.BOSS || target.getType().is(BOSS_TAG)) {
         return 16724047;
      } else if (role == DungeonMobLevelAdapter.MobRole.ELITE) {
         return 16757575;
      } else if (target instanceof Enemy) {
         return 16740419;
      } else {
         return target instanceof Player ? 5617919 : 15913822;
      }
   }

   public static int perceptionPriority(LivingEntity target) {
      DungeonMobLevelAdapter.MobRole role = DungeonMobLevelAdapter.MobRole.fromString(target.getPersistentData().getString("slr_dungeon_role"));
      if (role == DungeonMobLevelAdapter.MobRole.BOSS || target.getType().is(BOSS_TAG)) {
         return 130;
      } else {
         return role != DungeonMobLevelAdapter.MobRole.ELITE && !(target instanceof Enemy) ? 100 : 110;
      }
   }

   public static boolean isPerceptionCandidate(LivingEntity target) {
      return target != null
         && target.isAlive()
         && !target.isRemoved()
         && !target.isSpectator()
         && !target.getType().is(PORTAL_TAG)
         && !target.getType().is(SIDE_TAG);
   }

   private static boolean valid(ServerPlayer viewer, UUID targetId, ResourceKey<Level> dimension) {
      return viewer != null && !viewer.hasDisconnected() && targetId != null && !EntityHighlightMessage.NO_TARGET.equals(targetId) && dimension != null;
   }

   private static void send(ServerPlayer viewer, EntityHighlightMessage message) {
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> viewer), message);
   }

   private static String cleanSource(String source) {
      if (source != null && !source.isBlank()) {
         String trimmed = source.trim();
         return trimmed.length() <= 64 ? trimmed : trimmed.substring(0, 64);
      } else {
         return "default";
      }
   }
}
