package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public class FireReleaseAOEProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player);
      }
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null) {
         double modifier = 0.0;
         double xRadius = 0.0;
         double loop = 0.0;
         double zRadius = 0.0;
         double particleAmount = 0.0;
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FireRingTimer
            > 0.0) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .FireRingTimer
               - 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.FireRingTimer = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firestr
               == 1.0) {
               loop = 0.0;
               particleAmount = 50.0;
               xRadius = 3.0;
               zRadius = 3.0;

               while (loop < particleAmount) {
                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(
                        ParticleTypes.FLAME,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FX
                           + Math.cos((Math.PI * 2) / particleAmount * loop) * xRadius,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FY
                           + 4.0,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FZ
                           + Math.sin((Math.PI * 2) / particleAmount * loop) * zRadius,
                        2,
                        0.05,
                        0.05,
                        0.05,
                        1.0
                     );
                  }

                  loop++;
               }

               Vec3 _center = new Vec3(
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FX,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FY,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FZ
               );

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3.5), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator != entity
                     && entityiterator instanceof LivingEntity
                     && (
                        !(entityiterator instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                 ? _teamEnt.level()
                                    .getScoreboard()
                                    .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                    .getName()
                                 : "")
                              .equals(
                                 entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                    ? _teamEnt.level()
                                       .getScoreboard()
                                       .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                       .getName()
                                    : ""
                              )
                           || (entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                 ? _teamEnt.level()
                                    .getScoreboard()
                                    .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                    .getName()
                                 : "")
                              .equals("")
                     )) {
                     entityiterator.setSecondsOnFire(2);
                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
                     }

                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.IN_FIRE), entity),
                        (float)(2.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 200.0 * 0.0)
                     );
                  }
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firestr
               == 2.0) {
               loop = 0.0;
               particleAmount = 50.0;
               xRadius = 6.0;
               zRadius = 6.0;

               while (loop < particleAmount) {
                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(
                        ParticleTypes.FLAME,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FX
                           + Math.cos((Math.PI * 2) / particleAmount * loop) * xRadius,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FY
                           + 4.0,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FZ
                           + Math.sin((Math.PI * 2) / particleAmount * loop) * zRadius,
                        2,
                        0.05,
                        0.05,
                        0.05,
                        1.0
                     );
                  }

                  loop++;
               }

               Vec3 _center = new Vec3(
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FX,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FY,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FZ
               );

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(7.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator != entity
                     && entityiterator instanceof LivingEntity
                     && (
                        !(entityiterator instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                 ? _teamEnt.level()
                                    .getScoreboard()
                                    .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                    .getName()
                                 : "")
                              .equals(
                                 entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                    ? _teamEnt.level()
                                       .getScoreboard()
                                       .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                       .getName()
                                    : ""
                              )
                           || (entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                 ? _teamEnt.level()
                                    .getScoreboard()
                                    .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                    .getName()
                                 : "")
                              .equals("")
                     )) {
                     entityiterator.setSecondsOnFire(10);
                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
                     }

                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.IN_FIRE), entity),
                        (float)(3.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 175.0 * 0.0)
                     );
                  }
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firestr
               == 3.0) {
               loop = 0.0;
               particleAmount = 50.0;
               xRadius = 9.0;
               zRadius = 9.0;

               while (loop < particleAmount) {
                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(
                        ParticleTypes.FLAME,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FX
                           + Math.cos((Math.PI * 2) / particleAmount * loop) * xRadius,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FY
                           + 4.0,
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FZ
                           + Math.sin((Math.PI * 2) / particleAmount * loop) * zRadius,
                        2,
                        0.05,
                        0.05,
                        0.05,
                        1.0
                     );
                  }

                  loop++;
               }

               Vec3 _center = new Vec3(
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FX,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FY,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FZ
               );

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10.5), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator != entity
                     && entityiterator instanceof LivingEntity
                     && (
                        !(entityiterator instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                 ? _teamEnt.level()
                                    .getScoreboard()
                                    .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                    .getName()
                                 : "")
                              .equals(
                                 entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                    ? _teamEnt.level()
                                       .getScoreboard()
                                       .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                       .getName()
                                    : ""
                              )
                           || (entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                 ? _teamEnt.level()
                                    .getScoreboard()
                                    .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                    .getName()
                                 : "")
                              .equals("")
                     )) {
                     entityiterator.setSecondsOnFire(15);
                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
                     }

                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.IN_FIRE), entity),
                        (float)(4.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 150.0 * 1.0)
                     );
                  }
               }
            }
         }
      }
   }
}
