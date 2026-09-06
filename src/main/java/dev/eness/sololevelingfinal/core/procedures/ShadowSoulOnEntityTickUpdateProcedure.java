package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.ShadowSoulEntity;
import dev.eness.sololevelingfinal.core.util.AriseExtractionRules;

public class ShadowSoulOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 3, 0.01, 0.01, 0.01, 0.0);
         }

         int failures = entity.getPersistentData().contains("slr_arise_failures")
            ? entity.getPersistentData().getInt("slr_arise_failures")
            : (int)Math.floor(entity.getPersistentData().getDouble("ariset"));
         if (AriseExtractionRules.failuresExhausted(failures)) {
            if (!entity.level().isClientSide()) {
               entity.discard();
            }
         } else if (!AriseExtractionRules.isBossSoul(entity.getPersistentData().getString("soultype"))) {
            if (world.getLevelData().getGameTime() % 20L == 0L) {
               if (entity instanceof ShadowSoulEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(
                        ShadowSoulEntity.DATA_life,
                        (entity instanceof ShadowSoulEntity _datEntI ? _datEntI.getEntityData().get(ShadowSoulEntity.DATA_life) : 0) + 1
                     );
               }

               if ((entity instanceof ShadowSoulEntity _datEntI ? _datEntI.getEntityData().get(ShadowSoulEntity.DATA_life) : 0) >= 20
                  && !entity.level().isClientSide()) {
                  entity.discard();
               }
            }
         }
      }
   }
}
