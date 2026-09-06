package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class HealingBeamProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         String found_entity_name = "";
         boolean entity_found = false;
         double Grow = 0.0;
         double TrackZ = 0.0;
         double TrackY = 0.0;
         double TrackX = 0.0;
         double raytrace_distance = 0.0;
         double delay = 0.0;
         if (!entity.isShiftKeyDown()) {
            CooldownManager.set(entity, "Heal Beam", 280);
            if (entity instanceof LivingEntity _entity) {
               _entity.swing(InteractionHand.MAIN_HAND, true);
            }

            entity.getPersistentData().putDouble("range", 60.0);
            entity.getPersistentData().putDouble("sx", entity.getX());
            entity.getPersistentData().putDouble("sy", entity.getY() + 1.2);
            entity.getPersistentData().putDouble("sz", entity.getZ());
            entity.getPersistentData()
               .putDouble(
                  "tx",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX()
               );
            entity.getPersistentData()
               .putDouble(
                  "ty",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getY()
               );
            entity.getPersistentData()
               .putDouble(
                  "tz",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );
            entity.getPersistentData()
               .putDouble(
                  "range",
                  Math.sqrt(
                     Math.pow(entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx"), 2.0)
                        + Math.pow(entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty"), 2.0)
                        + Math.pow(entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz"), 2.0)
                  )
               );
            entity.getPersistentData()
               .putDouble(
                  "x+",
                  (entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData()
               .putDouble(
                  "y+",
                  (entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData()
               .putDouble(
                  "z+",
                  (entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData().putDouble("size", 0.0);

            for (int index0 = 0; index0 < (int)(entity.getPersistentData().getDouble("range") * 5.0); index0++) {
               delay += 0.05;
               SololevelingMod.queueServerWork(
                  (int)delay,
                  () -> {
                     entity.getPersistentData().putDouble("size", entity.getPersistentData().getDouble("size") + 1.0);
                     entity.getPersistentData().putDouble("sx", entity.getPersistentData().getDouble("sx") + entity.getPersistentData().getDouble("x+") * -0.2);
                     entity.getPersistentData().putDouble("sy", entity.getPersistentData().getDouble("sy") + entity.getPersistentData().getDouble("y+") * -0.2);
                     entity.getPersistentData().putDouble("sz", entity.getPersistentData().getDouble("sz") + entity.getPersistentData().getDouble("z+") * -0.2);
                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(
                           ParticleTypes.GLOW_SQUID_INK,
                           entity.getPersistentData().getDouble("sx"),
                           entity.getPersistentData().getDouble("sy"),
                           entity.getPersistentData().getDouble("sz"),
                           7,
                           0.1,
                           0.1,
                           0.1,
                           0.1
                        );
                     }

                     Vec3 _center = new Vec3(
                        entity.getPersistentData().getDouble("sx"), entity.getPersistentData().getDouble("sy"), entity.getPersistentData().getDouble("sz")
                     );

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if (entity != entityiterator && !(entityiterator instanceof ExperienceOrb) && !(entityiterator instanceof ItemEntity)) {
                           double _setvalx = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .progression_healer
                              + 2.0;
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                              capability.progression_healer = _setvalx;
                              capability.syncPlayerVariables(entity);
                           });
                           if (entityiterator instanceof LivingEntity _livEnt58 && _livEnt58.getMobType() == MobType.UNDEAD) {
                              entityiterator.hurt(
                                 new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC)), 8.0F
                              );
                           } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .party
                              .equals(
                                 entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                       .orElse(new SololevelingModVariables.PlayerVariables())
                                    .party
                              )) {
                              if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                                 _entity.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 1, false, false));
                              }

                              if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                                 _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1, false, false));
                              }
                           }
                        }
                     }
                  }
               );
            }
         } else {
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 2, false, false));
            }

            CooldownManager.set(entity, "Heal Beam", 400);
            if (world instanceof ServerLevel _level) {
               _level.sendParticles(
                  ParticleTypes.GLOW_SQUID_INK,
                  entity.getPersistentData().getDouble("sx"),
                  entity.getPersistentData().getDouble("sy"),
                  entity.getPersistentData().getDouble("sz"),
                  40,
                  4.0,
                  4.0,
                  4.0,
                  0.5
               );
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .progression_healer
               + 2.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.progression_healer = _setval;
               capability.syncPlayerVariables(entity);
            });
         }
      }
   }
}
