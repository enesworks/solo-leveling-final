package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class GoblinArcherOnEntityTickUpdateProcedure {
   private static final int DRAW_TICKS = 12;
   private static final int RELEASE_TICKS = 22;
   private static final int ATTACK_CYCLE_TICKS = 80;

   public static void execute(Entity entity) {
      if (entity != null) {
         if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) > 0.0F) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
               entity.getPersistentData().putDouble("AL", entity.getPersistentData().getDouble("AL") + 1.0);
               if (entity.getPersistentData().getBoolean("CanShoot")) {
                  entity.getPersistentData().putDouble("MF", entity.getPersistentData().getDouble("MF") + 1.0);
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 999, 90, false, false));
                  }
               } else {
                  entity.getPersistentData().putDouble("MF", 0.0);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                  }
               }

               Entity target = entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null;
               entity.lookAt(Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.55, target.getZ()));
               CombatRangeHelper.maintainRangedBand(entity, target, 5.5, 21.0, 1.0);
               if (CombatRangeHelper.withinSurfaceRange(entity, target, 24.0)
                  && entity instanceof Mob mob
                  && target instanceof LivingEntity livingTarget
                  && mob.getSensing().hasLineOfSight(livingTarget)) {
                  entity.getPersistentData().putBoolean("CanShoot", true);
               } else {
                  entity.getPersistentData().putBoolean("CanShoot", false);
               }
            } else {
               entity.getPersistentData().putDouble("MF", 0.0);
               entity.getPersistentData().putBoolean("CanShoot", false);
               if (entity instanceof LivingEntity _entity) {
                  _entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
               }
            }

            if (entity.getPersistentData().getDouble("MF") == 12.0) {
               if (entity instanceof GoblinArcherEntity) {
                  ((GoblinArcherEntity)entity).setAnimation("empty");
               }

               if (entity instanceof GoblinArcherEntity) {
                  ((GoblinArcherEntity)entity).setAnimation("shoot");
               }
            }

            if (entity.getPersistentData().getDouble("MF") == 22.0 && entity instanceof Mob mob) {
               fireArrow(mob);
            }

            if (entity.getPersistentData().getDouble("MF") >= 80.0) {
               entity.getPersistentData().putDouble("MF", 0.0);
            }
         }
      }
   }

   private static void fireArrow(Mob shooter) {
      if (shooter.isAlive() && !shooter.level().isClientSide()) {
         LivingEntity target = shooter.getTarget();
         if (target != null
            && target.isAlive()
            && target.level() == shooter.level()
            && shooter.getSensing().hasLineOfSight(target)
            && CombatRangeHelper.withinSurfaceRange(shooter, target, 24.0)) {
            Level projectileLevel = shooter.level();
            AbstractArrow arrow = new Arrow(EntityType.ARROW, projectileLevel);
            arrow.setOwner(shooter);
            arrow.setBaseDamage(2.0);
            arrow.setKnockback(0);
            arrow.setCritArrow(true);
            arrow.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
            double dx = target.getX() - shooter.getX();
            double dz = target.getZ() - shooter.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            double dy = target.getY() + target.getBbHeight() * 0.5 - arrow.getY();
            arrow.shoot(dx, dy + horizontal * 0.12, dz, 2.5F, 2.0F);
            projectileLevel.addFreshEntity(arrow);
         }
      }
   }
}
