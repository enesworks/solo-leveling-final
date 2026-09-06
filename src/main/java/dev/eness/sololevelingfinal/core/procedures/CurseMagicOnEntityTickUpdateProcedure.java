package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.CurseMagicEntity;

public class CurseMagicOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if ((entity instanceof CurseMagicEntity _datEntI ? _datEntI.getEntityData().get(CurseMagicEntity.DATA_life) : 0) < 20) {
            if (entity instanceof CurseMagicEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     CurseMagicEntity.DATA_life,
                     (entity instanceof CurseMagicEntity _datEntI ? _datEntI.getEntityData().get(CurseMagicEntity.DATA_life) : 0) + 1
                  );
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.PORTAL, x, y, z, 5, 0.6, 0.6, 0.6, 1.0);
            }
         } else {
            if (!entity.level().isClientSide()) {
               entity.discard();
            }

            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (!(entity instanceof CurseMagicEntity _datEntS ? _datEntS.getEntityData().get(CurseMagicEntity.DATA_owner) : "")
                  .equals(entityiterator.getStringUUID())) {
                  if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 2, false, false));
                  }

                  if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 3, false, false));
                  }

                  if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
                  }
               }
            }
         }
      }
   }
}
