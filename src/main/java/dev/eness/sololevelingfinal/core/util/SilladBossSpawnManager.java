package dev.eness.sololevelingfinal.core.util;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.SilladBossEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

@EventBusSubscriber(modid = "sololeveling")
public final class SilladBossSpawnManager {
   private static final String AUTHORIZED_BY_TAG = "slr_sillad_preview_authorizer";
   private static final double SPAWN_DISTANCE = 7.0;
   private static final int[] VERTICAL_OFFSETS = new int[]{0, 1, -1, 2, -2, 3, -3};
   private static final int[][] HORIZONTAL_OFFSETS = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
   private static final Set<UUID> PENDING_AUTHORIZATIONS = ConcurrentHashMap.newKeySet();

   private SilladBossSpawnManager() {
   }

   @Nullable
   public static SilladBossEntity spawnForDeveloper(ServerPlayer player) {
      if (player != null && DeveloperModeManager.isEnabled(player)) {
         ServerLevel level = player.serverLevel();
         SilladBossEntity sillad = SololevelingModEntities.SILLAD_BOSS.get().create(level);
         if (sillad == null) {
            return null;
         }

         BlockPos spawnPos = findSafeSpawn(level, player, sillad);
         if (spawnPos == null) {
            return null;
         }

         float facing = player.getYRot() + 180.0F;
         sillad.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, facing, 0.0F);
         sillad.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
         sillad.setPersistenceRequired();
         sillad.getPersistentData().putUUID("slr_sillad_preview_authorizer", player.getUUID());
         UUID entityId = sillad.getUUID();
         PENDING_AUTHORIZATIONS.add(entityId);

         try {
            return level.addFreshEntity(sillad) ? sillad : null;
         } finally {
            PENDING_AUTHORIZATIONS.remove(entityId);
         }
      } else {
         return null;
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide() && event.getEntity() instanceof SilladBossEntity sillad) {
         boolean var4 = sillad.getPersistentData().hasUUID("slr_sillad_preview_authorizer");
         boolean authorized = event.loadedFromDisk() ? var4 : var4 && PENDING_AUTHORIZATIONS.remove(sillad.getUUID());
         if (!authorized) {
            event.setCanceled(true);
         }
      }
   }

   @Nullable
   private static BlockPos findSafeSpawn(ServerLevel level, ServerPlayer player, SilladBossEntity sillad) {
      Vec3 look = player.getLookAngle();
      Vec3 horizontal = new Vec3(look.x, 0.0, look.z);
      if (horizontal.lengthSqr() < 1.0E-6) {
         horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
      }

      horizontal = new Vec3(horizontal.x, 0.0, horizontal.z).normalize();
      Vec3 target = player.position().add(horizontal.scale(7.0));
      BlockPos base = BlockPos.containing(target.x, player.getY(), target.z);

      for (int[] horizontalOffset : HORIZONTAL_OFFSETS) {
         for (int verticalOffset : VERTICAL_OFFSETS) {
            BlockPos candidate = base.offset(horizontalOffset[0], verticalOffset, horizontalOffset[1]);
            if (level.hasChunkAt(candidate)) {
               BlockPos support = candidate.below();
               if (level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
                  sillad.moveTo(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5, player.getYRot() + 180.0F, 0.0F);
                  if (level.noCollision(sillad, sillad.getBoundingBox())) {
                     return candidate.immutable();
                  }
               }
            }
         }
      }

      return null;
   }
}
