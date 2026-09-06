package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.KangTaeshikEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public final class KangTaeshikAmbushManager {
   public static final String AMBUSH_TAG = "SLRKangTaeshikAmbush";
   public static final String OWNER_TAG = "SLRKangTaeshikTarget";
   private static final String PENDING_UNTIL_TAG = "SLRKangTaeshikAmbushPendingUntil";
   private static final String COOLDOWN_TAG = "SLRKangTaeshikAmbushCooldown";
   private static final String COMPLETED_TAG = "SLRKangTaeshikAmbushCompleted";
   private static final double AMBUSH_CHANCE = 0.1;
   private static final long AMBUSH_COOLDOWN = 12000L;

   private KangTaeshikAmbushManager() {
   }

   public static void trySchedule(ServerPlayer player, Entity defeatedBoss) {
      if (player != null && defeatedBoss != null && player.isAlive() && SystemPlayerAccess.hasSystem(player)) {
         if (!hasCompletedAmbush(player) && isEligibleDungeonBoss(defeatedBoss)) {
            long now = player.level().getGameTime();
            if (player.getPersistentData().getLong("SLRKangTaeshikAmbushPendingUntil") <= now) {
               player.getPersistentData().remove("SLRKangTaeshikAmbushPendingUntil");
               if (now >= player.getPersistentData().getLong("SLRKangTaeshikAmbushCooldown")
                  && !(player.getRandom().nextDouble() >= 0.1)
                  && !hasLiveOwnedAmbush(player)) {
                  MinecraftServer server = player.getServer();
                  if (server != null) {
                     UUID playerId = player.getUUID();
                     player.getPersistentData().putLong("SLRKangTaeshikAmbushPendingUntil", now + 60L);
                     SololevelingMod.queueServerWork(server, 25, () -> spawnAmbush(server, playerId));
                  }
               }
            }
         }
      }
   }

   public static boolean isAmbush(KangTaeshikEntity kang) {
      return kang != null && kang.getPersistentData().getBoolean("SLRKangTaeshikAmbush") && kang.getPersistentData().hasUUID("SLRKangTaeshikTarget");
   }

   public static UUID ownerId(KangTaeshikEntity kang) {
      return isAmbush(kang) ? kang.getPersistentData().getUUID("SLRKangTaeshikTarget") : null;
   }

   public static boolean markAmbushCompleted(ServerPlayer player) {
      if (player == null) {
         return false;
      }

      CompoundTag persisted = persistentPlayerData(player);
      if (persisted.getBoolean("SLRKangTaeshikAmbushCompleted")) {
         return false;
      }

      persisted.putBoolean("SLRKangTaeshikAmbushCompleted", true);
      return true;
   }

   public static void resetPlayerProgress(ServerPlayer player) {
      if (player != null && player.getServer() != null) {
         player.getPersistentData().remove("SLRKangTaeshikAmbushPendingUntil");
         player.getPersistentData().remove("SLRKangTaeshikAmbushCooldown");
         UUID playerId = player.getUUID();
         ArrayList<KangTaeshikEntity> loadedAmbushes = new ArrayList<>();

         for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
               if (entity instanceof KangTaeshikEntity kang && playerId.equals(ownerId(kang))) {
                  loadedAmbushes.add(kang);
               }
            }
         }

         loadedAmbushes.forEach(Entity::discard);
      }
   }

   private static boolean hasCompletedAmbush(ServerPlayer player) {
      return player != null && persistentPlayerData(player).getBoolean("SLRKangTaeshikAmbushCompleted");
   }

   private static boolean isEligibleDungeonBoss(Entity boss) {
      if (boss instanceof KangTaeshikEntity) {
         return false;
      } else {
         ResourceLocation dimension = boss.level().dimension().location();
         if (!"sololeveling".equals(dimension.getNamespace()) || !dimension.getPath().startsWith("dungeon_dimension")) {
            return false;
         } else {
            return "dungeon_dimension_igris".equals(dimension.getPath()) ? false : CombatRankHelper.rankOf(boss) <= 4;
         }
      }
   }

   private static void spawnAmbush(MinecraftServer server, UUID playerId) {
      ServerPlayer player = server.getPlayerList().getPlayer(playerId);
      if (player != null) {
         if (player.getPersistentData().getLong("SLRKangTaeshikAmbushPendingUntil") > player.level().getGameTime()) {
            player.getPersistentData().remove("SLRKangTaeshikAmbushPendingUntil");
            if (player.isAlive() && !player.isRemoved() && player.level() instanceof ServerLevel level && SystemPlayerAccess.hasSystem(player)) {
               if (!hasCompletedAmbush(player) && !UrgentQuestManager.hasActiveQuest(player) && !hasLiveOwnedAmbush(player)) {
                  BlockPos spawnPos = findSafeSpawn(level, player);
                  if (spawnPos != null) {
                     KangTaeshikEntity kang = SololevelingModEntities.KANG_TAESHIK.get().create(level);
                     if (kang != null) {
                        kang.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), 0.0F);
                        kang.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
                        kang.getPersistentData().putBoolean("SLRKangTaeshikAmbush", true);
                        kang.getPersistentData().putUUID("SLRKangTaeshikTarget", player.getUUID());
                        kang.setPersistenceRequired();
                        if (level.addFreshEntity(kang)) {
                           if (!UrgentQuestManager.startKangAmbushQuest(player, kang)) {
                              kang.discard();
                           } else {
                              kang.setTarget(player);
                              kang.getNavigation().moveTo(player, 1.35);
                              Vec3 leap = player.position().subtract(kang.position());
                              if (leap.lengthSqr() > 0.001) {
                                 leap = leap.normalize();
                                 kang.setDeltaMovement(leap.x * 0.85, 0.32, leap.z * 0.85);
                              }

                              player.getPersistentData().putLong("SLRKangTaeshikAmbushCooldown", level.getGameTime() + 12000L);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean hasLiveOwnedAmbush(ServerPlayer player) {
      if (player.getServer() == null) {
         return false;
      }

      UUID playerId = player.getUUID();

      for (ServerLevel level : player.getServer().getAllLevels()) {
         for (Entity entity : level.getAllEntities()) {
            if (entity instanceof KangTaeshikEntity kang && kang.isAlive() && !kang.isRemoved() && isAmbush(kang) && playerId.equals(ownerId(kang))) {
               return true;
            }
         }
      }

      return false;
   }

   private static CompoundTag persistentPlayerData(Player player) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         root.put("PlayerPersisted", new CompoundTag());
      }

      return root.getCompound("PlayerPersisted");
   }

   private static BlockPos findSafeSpawn(ServerLevel level, ServerPlayer player) {
      Vec3 forward = new Vec3(player.getLookAngle().x, 0.0, player.getLookAngle().z);
      if (forward.lengthSqr() < 0.001) {
         forward = new Vec3(0.0, 0.0, 1.0);
      }

      forward = forward.normalize();
      Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
      Vec3[] offsets = new Vec3[]{
         forward.scale(-6.0),
         forward.scale(-8.0),
         right.scale(6.0),
         right.scale(-6.0),
         forward.scale(-5.0).add(right.scale(3.0)),
         forward.scale(-5.0).add(right.scale(-3.0))
      };

      for (Vec3 offset : offsets) {
         int baseX = (int)Math.floor(player.getX() + offset.x);
         int baseZ = (int)Math.floor(player.getZ() + offset.z);

         for (int dy = 2; dy >= -4; dy--) {
            BlockPos feet = new BlockPos(baseX, player.getBlockY() + dy, baseZ);
            if (isSafeStandingPos(level, feet)) {
               return feet;
            }
         }
      }

      return null;
   }

   private static boolean isSafeStandingPos(ServerLevel level, BlockPos feet) {
      BlockPos floor = feet.below();
      return level.hasChunkAt(feet)
         && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
         && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
         && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
         && level.getFluidState(feet).isEmpty()
         && level.getFluidState(feet.above()).isEmpty();
   }
}
