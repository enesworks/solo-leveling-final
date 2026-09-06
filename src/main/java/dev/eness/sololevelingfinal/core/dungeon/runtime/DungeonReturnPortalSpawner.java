package dev.eness.sololevelingfinal.core.dungeon.runtime;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public final class DungeonReturnPortalSpawner {
   private DungeonReturnPortalSpawner() {
   }

   @Nullable
   public static Entity spawn(ServerLevel level, BlockPos position, @Nullable Direction facing, UUID instanceId, String dungeonTag) {
      return level != null && position != null && instanceId != null ? spawnConfigured(level, position, facing, instanceId, dungeonTag) : null;
   }

   @Nullable
   public static Entity spawnUnscoped(ServerLevel level, BlockPos position, String dungeonTag) {
      return level != null && position != null ? spawnConfigured(level, position, null, null, dungeonTag) : null;
   }

   @Nullable
   private static Entity spawnConfigured(ServerLevel level, BlockPos position, @Nullable Direction facing, @Nullable UUID instanceId, String dungeonTag) {
      Entity portal = SololevelingModEntities.PORTAL_12.get().create(level);
      if (portal == null) {
         return null;
      }

      portal.moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 0.5, 0.0F, 0.0F);
      if (portal instanceof Mob mob) {
         mob.finalizeSpawn(level, level.getCurrentDifficultyAt(position), MobSpawnType.MOB_SUMMONED, null, null);
      }

      if (facing != null && facing.getAxis().isHorizontal()) {
         float yaw = facing.toYRot();
         portal.setYRot(yaw);
         portal.setXRot(0.0F);
         portal.yRotO = yaw;
         portal.xRotO = 0.0F;
         if (portal instanceof Mob mob) {
            mob.yBodyRot = yaw;
            mob.yBodyRotO = yaw;
            mob.setYHeadRot(yaw);
            mob.yHeadRotO = yaw;
         }
      }

      if (instanceId != null) {
         portal.getPersistentData().putString("slr_dungeon_instance", instanceId.toString());
      }

      String cleanDungeonTag = dungeonTag == null ? "" : dungeonTag.trim();
      if (!cleanDungeonTag.isEmpty()) {
         portal.getPersistentData().putString("dungeon_tag", cleanDungeonTag);
      } else if (instanceId != null) {
         portal.getPersistentData().putString("dungeon_tag", instanceId.toString());
      }

      if (!level.addFreshEntity(portal)) {
         portal.discard();
         return null;
      } else {
         return portal;
      }
   }
}
