package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class BloodRedComIgrisOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) > 0.0F) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
               if (entity instanceof Mob _mob && _mob.getTarget() != null) {
                  LivingEntity target = _mob.getTarget();
                  double deltaX = target.getX() - entity.getX();
                  double deltaZ = target.getZ() - entity.getZ();
                  float targetYaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
                  entity.setYRot(targetYaw);
                  entity.yRotO = targetYaw;
                  if (entity instanceof LivingEntity _livingEntity) {
                     _livingEntity.yBodyRot = targetYaw;
                     _livingEntity.yHeadRot = targetYaw;
                  }
               }

               entity.getPersistentData().putDouble("MF", entity.getPersistentData().getDouble("MF") + 1.0);
               if (!entity.getPersistentData().getBoolean("enraged")) {
                  double maxHp = entity instanceof LivingEntity _le ? _le.getAttributeValue(Attributes.MAX_HEALTH) : 110.0;
                  double curHp = entity instanceof LivingEntity _le2 ? _le2.getHealth() : maxHp;
                  if (curHp <= maxHp * 0.5) {
                     entity.getPersistentData().putBoolean("enraged", true);
                     if (entity instanceof LivingEntity _le && !_le.level().isClientSide()) {
                        AttributeInstance attr = _le.getAttribute(Attributes.MOVEMENT_SPEED);
                        if (attr != null) {
                           attr.setBaseValue(0.8);
                        }
                     }

                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 1.0, z, 60, 0.8, 1.0, 0.8, 0.2);
                        _level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.0, z, 30, 0.6, 0.8, 0.6, 0.05);
                     }

                     entity.getPersistentData().putString("state", "scream");
                     entity.getPersistentData().putDouble("MF", 0.0);
                  }
               }
            } else {
               entity.getPersistentData().putDouble("MF", 0.0);
               entity.getPersistentData().putString("state", "idle");
               entity.getPersistentData().putDouble("nextAttackMF", 10.0);
            }

            String state = entity.getPersistentData().getString("state");
            if (state.equals("idle")) {
               double nextAttackMF = entity.getPersistentData().contains("nextAttackMF") ? entity.getPersistentData().getDouble("nextAttackMF") : 10.0;
               if (entity.getPersistentData().getDouble("MF") >= nextAttackMF) {
                  IgrisStateChangerProcedure.execute(entity);
               }
            } else if (state.equals("spin")) {
               IgrisSpinProcedure.execute(world, x, y, z, entity);
            } else if (state.equals("stab")) {
               IgrisStabProcedure.execute(world, x, y, z, entity);
            } else if (state.equals("slam")) {
               IgrisSlamProcedure.execute(world, x, y, z, entity);
            } else if (state.equals("scream")) {
               IgrisScreamProcedure.execute(world, x, y, z, entity);
            }
         } else {
            entity.getPersistentData().putDouble("MF", 0.0);
         }

         if (entity instanceof TamableAnimal _tamEnt
            && _tamEnt.isTame()
            && entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
            && (entity instanceof TamableAnimal _tamEnt2 ? _tamEnt2.getOwner() : null) != null
            && (entity instanceof TamableAnimal _tamEnt3 ? _tamEnt3.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .Call4Death) {
            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(), entity.getZ(), 30, 0.05, 0.05, 0.05, 1.0);
            }

            if (!entity.level().isClientSide()) {
               entity.discard();
            }
         }
      }
   }
}
