package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.ThomasAndreEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class ThomasPunchProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         if (entity.getPersistentData().getDouble("IA") == 20.0) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 16, 99, false, false));
            }

            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 16, 99, false, false));
            }
         }

         if (entity.getPersistentData().getDouble("IA") == 30.0) {
            if (entity instanceof ThomasAndreEntity) {
               ((ThomasAndreEntity)entity).setAnimation("punches");
            }

            Entity _ent = entity;
            _ent.teleportTo(
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX()
                  + -1.0 * (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getLookAngle().x,
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 0.2,
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
                  + -1.0 * (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getLookAngle().z
            );
            if (_ent instanceof ServerPlayer _serverPlayer) {
               _serverPlayer.connection
                  .teleport(
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX()
                        + -1.0 * (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getLookAngle().x,
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 0.2,
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
                        + -1.0 * (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getLookAngle().z,
                     _ent.getYRot(),
                     _ent.getXRot()
                  );
            }

            entity.lookAt(
               Anchor.EYES,
               new Vec3(
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 1.6,
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
               )
            );
         }

         if (entity.getPersistentData().getDouble("IA") == 22.0
            && CombatRangeHelper.withinSurfaceRange(entity, entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null, 2.0)) {
            (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
               .hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity), 20.0F);
         }

         if (entity.getPersistentData().getDouble("IA") == 34.0
            && CombatRangeHelper.withinSurfaceRange(entity, entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null, 2.0)) {
            (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
               .hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity), 20.0F);
            (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
               .setDeltaMovement(new Vec3(entity.getLookAngle().x * 2.0, entity.getLookAngle().y * 1.5, entity.getLookAngle().z * 2.0));
         }

         if (entity.getPersistentData().getDouble("IA") == 50.0) {
            AndreStateChangerProcedure.execute(entity);
         }
      }
   }
}
