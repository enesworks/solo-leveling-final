package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;

public class GoblinBossattack2Procedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if ((entity instanceof GoblinKingEntity _datEntI ? _datEntI.getEntityData().get(GoblinKingEntity.DATA_MF) : 0) == 1
            && entity instanceof GoblinKingEntity) {
            ((GoblinKingEntity)entity).setAnimation("attack_2");
         }

         if ((entity instanceof GoblinKingEntity _datEntI ? _datEntI.getEntityData().get(GoblinKingEntity.DATA_MF) : 0) == 7) {
            Vec3 _center = new Vec3(entity.getX() + 2.0 * entity.getLookAngle().x, entity.getY() + 1.0, entity.getZ() + 2.0 * entity.getLookAngle().z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entity != entityiterator) {
                  if (world instanceof Level _level && !_level.isClientSide()) {
                     _level.explode(entity, entityiterator.getX(), entityiterator.getY() + 1.0, entityiterator.getZ(), 1.0F, ExplosionInteraction.NONE);
                  }

                  entityiterator.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity), 5.0F
                  );
               }
            }
         }

         if ((entity instanceof GoblinKingEntity _datEntI ? _datEntI.getEntityData().get(GoblinKingEntity.DATA_MF) : 0) == 41) {
            GoblinBossstatechangerProcedure.execute(entity);
         }
      }
   }
}
