package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.OrcEntity;

public class OrcEntityIsHurtProcedure {
   public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         double rand = 0.0;
         rand = Math.random();
         if (!(sourceentity instanceof Player _plr && _plr.getAbilities().instabuild) && !(sourceentity instanceof OrcEntity) && rand >= 0.85) {
            if (entity instanceof OrcEntity) {
               ((OrcEntity)entity).setAnimation("smash");
            }

            SololevelingMod.queueServerWork(
               25,
               () -> {
                  Vec3 _center = new Vec3(entity.getX(), entity.getY(), entity.getZ());

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entity != entityiterator && !(entityiterator instanceof OrcEntity)) {
                        entityiterator.hurt(
                           new DamageSource(
                              world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK_NO_AGGRO), entity
                           ),
                           12.0F
                        );
                        entityiterator.setDeltaMovement(new Vec3(0.0, 1.0, 0.0));
                     }
                  }
               }
            );
         }
      }
   }
}
