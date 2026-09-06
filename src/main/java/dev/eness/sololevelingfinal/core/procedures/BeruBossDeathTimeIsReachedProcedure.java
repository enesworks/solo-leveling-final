package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class BeruBossDeathTimeIsReachedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity defeated) {
      if (world instanceof ServerLevel _level) {
         Entity entityToSpawn = SololevelingModEntities.BERU_DEAD_BODY.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
         if (entityToSpawn != null) {
            double targetLevel = DungeonLevelHelper.levelOf(defeated);
            if (targetLevel > 0.0) {
               entityToSpawn.getPersistentData().putDouble("slr_arise_target_level", targetLevel);
            }
         }
      }
   }
}
