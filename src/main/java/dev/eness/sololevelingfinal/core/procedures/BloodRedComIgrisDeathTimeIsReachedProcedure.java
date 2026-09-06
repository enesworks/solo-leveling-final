package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

public class BloodRedComIgrisDeathTimeIsReachedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (world instanceof ServerLevel _level) {
            Entity entityToSpawn = SololevelingModEntities.IGRIS_DEAD_BODY.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
            if (entityToSpawn != null) {
               JobChangeQuestManager.copyAttempt(entity, entityToSpawn);
               double targetLevel = DungeonLevelHelper.levelOf(entity);
               if (targetLevel > 0.0) {
                  entityToSpawn.getPersistentData().putDouble("slr_arise_target_level", targetLevel);
               }

               entityToSpawn.setYRot(entity.getYRot());
               entityToSpawn.setYBodyRot(entity.getYRot());
               entityToSpawn.setYHeadRot(entity.getYRot());
               entityToSpawn.setXRot(entity.getXRot());
            }
         }
      }
   }
}
