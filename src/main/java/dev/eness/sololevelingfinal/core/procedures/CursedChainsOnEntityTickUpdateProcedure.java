package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.CursedChainsEntity;

public class CursedChainsOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(7.5), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entityiterator.getDisplayName()
               .getString()
               .equals(entity instanceof CursedChainsEntity _datEntS ? _datEntS.getEntityData().get(CursedChainsEntity.DATA_target) : "")) {
               entityiterator.setDeltaMovement(new Vec3(0.0, 0.0, 0.0));
               if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 255, false, false));
               }

               entityiterator.setDeltaMovement(
                  new Vec3((x - entityiterator.getX()) * 0.6, (y - entityiterator.getY()) * 0.6, (z - entityiterator.getZ()) * 0.6)
               );
            }
         }

         if (entity.getPersistentData().getDouble("Timer") < 100.0) {
            entity.getPersistentData().putDouble("Timer", entity.getPersistentData().getDouble("Timer") + 1.0);
         } else if (!entity.level().isClientSide()) {
            entity.discard();
         }
      }
   }
}
