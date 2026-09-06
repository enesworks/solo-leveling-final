package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

public class RangerProjectileWhileProjectileFlyingTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity immediatesourceentity) {
      if (entity != null && immediatesourceentity != null) {
         immediatesourceentity.setNoGravity(true);
         if ((
               (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").contains("B")
                  || (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").contains("A")
                  || (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").contains("S")
            )
            && world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 2, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }
}
