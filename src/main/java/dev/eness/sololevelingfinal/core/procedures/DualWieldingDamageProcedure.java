package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
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
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class DualWieldingDamageProcedure {
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
         if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(SololevelingModMobEffects.DUAL_WIELDING.get())) {
            Vec3 _center = new Vec3(entity.getX(), entity.getY(), entity.getZ());

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.5), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entityiterator != entity
                  && entityiterator instanceof LivingEntity
                  && (
                     !(entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                              ? _teamEnt.level()
                                 .getScoreboard()
                                 .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                 .getName()
                              : "")
                           .equals(
                              entityiterator instanceof LivingEntity _teamEnt
                                    && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
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
                  SololevelingMod.queueServerWork(
                     10,
                     () -> {
                        if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                           _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 13, 5, false, false));
                        }

                        entityiterator.hurt(
                           new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), entity),
                           (float)(5.0 + Math.floor(TemporaryStatBonusManager.effectiveStrength(entity) / 40.0))
                        );
                        if (world instanceof ServerLevel _level) {
                           _level.sendParticles(
                              SololevelingModParticleTypes.CLEAVE.get(),
                              entityiterator.getX(),
                              entityiterator.getY() + entityiterator.getBbHeight() / 2.0F,
                              entityiterator.getZ(),
                              1,
                              0.1,
                              0.3,
                              0.1,
                              0.1
                           );
                        }

                        if (world instanceof ServerLevel _level) {
                           _level.sendParticles(
                              SololevelingModParticleTypes.GOODSLASH_1.get(),
                              entityiterator.getX(),
                              entityiterator.getY() + entityiterator.getBbHeight() / 2.0F,
                              entityiterator.getZ(),
                              2,
                              0.1,
                              0.3,
                              0.1,
                              0.1
                           );
                        }
                     }
                  );
               }
            }
         }
      }
   }
}
