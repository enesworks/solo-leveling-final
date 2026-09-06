package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class WhiteFlameProjectileHitsLivingEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      if (Math.random() < 0.025F && world instanceof ServerLevel _level) {
         LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
         entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
         entityToSpawn.setVisualOnly(true);
         _level.addFreshEntity(entityToSpawn);
      }
   }
}
