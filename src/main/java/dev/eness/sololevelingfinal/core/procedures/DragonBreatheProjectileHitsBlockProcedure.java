package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DragonBreatheProjectileHitsBlockProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity immediatesourceentity) {
      if (entity != null && immediatesourceentity != null) {
         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5.0), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entityiterator == entity) {
               entityiterator.hurt(
                  new DamageSource(
                     world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), immediatesourceentity, entity
                  ),
                  9.0F
               );
            }
         }

         if (!immediatesourceentity.level().isClientSide()) {
            immediatesourceentity.discard();
         }
      }
   }
}
