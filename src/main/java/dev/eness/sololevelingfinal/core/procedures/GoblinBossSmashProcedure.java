package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;

public class GoblinBossSmashProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (null != (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)) {
            if ((entity instanceof GoblinKingEntity _datEntI ? _datEntI.getEntityData().get(GoblinKingEntity.DATA_MF) : 0) == 1
               && entity instanceof GoblinKingEntity) {
               ((GoblinKingEntity)entity).setAnimation("slam");
            }

            if ((entity instanceof GoblinKingEntity _datEntI ? _datEntI.getEntityData().get(GoblinKingEntity.DATA_MF) : 0) == 18) {
               Vec3 _center = new Vec3(entity.getX(), entity.getY(), entity.getZ());

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(15.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator && entityiterator.onGround()) {
                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC), entity), 10.0F
                     );
                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                     }

                     if (entityiterator == (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)) {
                        if (entity instanceof GoblinKingEntity _datEntSetS) {
                           _datEntSetS.getEntityData().set(GoblinKingEntity.DATA_state, "Dash");
                        }

                        if (entity instanceof GoblinKingEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(GoblinKingEntity.DATA_MF, 0);
                        }
                     }
                  }
               }
            }

            if ((entity instanceof GoblinKingEntity _datEntI ? _datEntI.getEntityData().get(GoblinKingEntity.DATA_MF) : 0) == 51) {
               GoblinBossstatechangerProcedure.execute(entity);
            }
         }
      }
   }
}
