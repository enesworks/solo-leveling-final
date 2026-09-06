package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public class SnowScreenOnTickProcedure {
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
         if (!world.isClientSide() && entity.getPersistentData().getBoolean("snowscreen") && world.getLevelData().getGameTime() % 10L == 0L) {
            if (world instanceof ServerLevel _level) {
               _level.sendParticles(
                  ParticleTypes.SNOWFLAKE,
                  entity.getPersistentData().getDouble("SnowX"),
                  entity.getPersistentData().getDouble("SnowY"),
                  entity.getPersistentData().getDouble("SnowZ"),
                  15,
                  5.0,
                  3.0,
                  5.0,
                  0.0
               );
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(
                  ParticleTypes.CAMPFIRE_COSY_SMOKE,
                  entity.getPersistentData().getDouble("SnowX"),
                  entity.getPersistentData().getDouble("SnowY"),
                  entity.getPersistentData().getDouble("SnowZ"),
                  6,
                  5.0,
                  3.0,
                  5.0,
                  0.0
               );
            }

            Vec3 _center = new Vec3(
               entity.getPersistentData().getDouble("SnowX"), entity.getPersistentData().getDouble("SnowY"), entity.getPersistentData().getDouble("SnowZ")
            );

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(6.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (!(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt))
                  && entityiterator != entity
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
                  entityiterator.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK), entity),
                     (float)(3.0 + (TemporaryStatBonusManager.effectiveStrength(entity) + TemporaryStatBonusManager.effectiveIntelligence(entity)) / 25.0)
                  );
                  if (!(entity instanceof LivingEntity _livEnt22 && _livEnt22.hasEffect(SololevelingModMobEffects.FREEZE.get()))
                     && entityiterator instanceof LivingEntity _entity
                     && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.FREEZE.get(), 40, 1, false, false));
                  }
               }
            }
         }
      }
   }
}
