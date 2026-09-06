package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SLRSRankProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         String _setval = "S";
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.ranking = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (world instanceof ServerLevel _level) {
            LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
            entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
            entityToSpawn.setVisualOnly(true);
            _level.addFreshEntity(entityToSpawn);
         }
      }
   }
}
