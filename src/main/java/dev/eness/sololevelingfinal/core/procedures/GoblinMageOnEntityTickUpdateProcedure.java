package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.ShamanMagicEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CombatRangeHelper;

public class GoblinMageOnEntityTickUpdateProcedure {
   private static final int VOLLEY_SIZE = 3;
   private static final int VOLLEY_SHOT_INTERVAL_TICKS = 7;
   private static final int ATTACK_CYCLE_TICKS = 80;
   private static final float MAGIC_DAMAGE = 4.0F;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) > 0.05) {
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

               entity.lookAt(
                  Anchor.EYES,
                  new Vec3(
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 1.2,
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
                  )
               );
               Entity target = entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null;
               CombatRangeHelper.maintainRangedBand(entity, target, 4.5, 14.0, 0.95);
               if (CombatRangeHelper.withinSurfaceRange(entity, target, 17.0)
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

            if (entity.getPersistentData().getDouble("MF") == 5.0 && entity instanceof GoblinMageEntity) {
               ((GoblinMageEntity)entity).setAnimation("attack");
            }

            if (entity.getPersistentData().getDouble("MF") == 13.0 && entity.level() instanceof ServerLevel serverLevel) {
               for (int shot = 1; shot <= 3; shot++) {
                  SololevelingMod.queueServerWork(serverLevel.getServer(), shot * 7, () -> fireBolt(entity));
               }
            }

            if (entity.getPersistentData().getDouble("MF") >= 80.0) {
               entity.getPersistentData().putDouble("MF", 0.0);
            }
         }
      }
   }

   private static void fireBolt(Entity shooter) {
      if (shooter instanceof Mob mob && shooter.isAlive() && !shooter.level().isClientSide()) {
         LivingEntity target = mob.getTarget();
         if (target != null
            && target.isAlive()
            && target.level() == shooter.level()
            && mob.getSensing().hasLineOfSight(target)
            && CombatRangeHelper.withinSurfaceRange(shooter, target, 17.0)) {
            Level projectileLevel = shooter.level();
            AbstractArrow bolt = new ShamanMagicEntity(SololevelingModEntities.SHAMAN_MAGIC.get(), projectileLevel);
            bolt.setOwner(shooter);
            bolt.setBaseDamage(4.0);
            bolt.setKnockback(0);
            bolt.setSilent(true);
            bolt.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
            Vec3 aim = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.65, target.getZ()).subtract(bolt.position());
            bolt.shoot(aim.x, aim.y, aim.z, 0.25F, 0.0F);
            projectileLevel.addFreshEntity(bolt);
            projectileLevel.playSound(
               (Player)null,
               shooter.blockPosition(),
               ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
               SoundSource.NEUTRAL,
               1.0F,
               0.5F
            );
         }
      }
   }
}
