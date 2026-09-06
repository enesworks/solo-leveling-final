package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.QuickSlashesEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class QuickSlashesProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double delay = 0.0;
         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
         }

         CooldownManager.set(entity, "mana_refresh", 150);
         CooldownManager.set(entity, "Quickslashes", 300);
         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(6.0), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entity != entityiterator
               && !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt))
               && entityiterator instanceof LivingEntity
               && (
                  !(entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                           ? _teamEnt.level()
                              .getScoreboard()
                              .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                              .getName()
                           : "")
                        .equals(
                           entityiterator instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
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
               if (entity instanceof LivingEntity livingEntity) {
                  QuickSlashesEntity.spawn(world, livingEntity, entityiterator);
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SWEEP_ATTACK, entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 6, 0.5, 1.0, 0.5, 0.0);
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(
                     SololevelingModParticleTypes.CLEAVE.get(), entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 6, 0.5, 1.0, 0.5, 0.0
                  );
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(
                     SololevelingModParticleTypes.DISMANTLE.get(), entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 6, 0.5, 1.0, 0.5, 0.0
                  );
               }

               entityiterator.hurt(
                  new DamageSource(
                     world.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
                     entity
                  ),
                  (float)(6.0 + TemporaryStatBonusManager.effectiveStrength(entity) / 20.0)
               );
               if (entity instanceof LivingEntity _entity) {
                  _entity.swing(InteractionHand.MAIN_HAND, true);
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:slash")),
                        SoundSource.NEUTRAL,
                        0.3F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:slash")), SoundSource.NEUTRAL, 0.3F, 1.0F, false
                     );
                  }
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.attack.sweep")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.attack.sweep")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F,
                        false
                     );
                  }
               }
            }
         }
      }
   }
}
