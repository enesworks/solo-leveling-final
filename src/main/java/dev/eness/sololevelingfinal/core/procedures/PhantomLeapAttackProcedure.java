package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class PhantomLeapAttackProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         double X = 0.0;
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
               >= 800.0
            && !CooldownManager.isOnCooldown(entity, "Backstab")) {
            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entityiterator.getPersistentData().getString("target").equals(entity.getDisplayName().getString()) && entity != entityiterator) {
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 1, false, false));
                  }

                  CooldownManager.set(entity, "mana_refresh", 100);
                  CooldownManager.set(entity, "Backstab", 240);
                  double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .MP
                     - 800.0;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.MP = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.enderman.teleport")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x,
                           y,
                           z,
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.enderman.teleport")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F,
                           false
                        );
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 5, 0.4, 0.4, 0.4, 0.0);
                  }

                  entityiterator.setNoGravity(true);
                  entity.setNoGravity(true);
                  Entity _ent = entity;
                  _ent.teleportTo(
                     entityiterator.getX() + -2.0 * entityiterator.getLookAngle().x,
                     entityiterator.getY() + 0.8 + -1.3 * entityiterator.getLookAngle().y,
                     entityiterator.getZ() + -2.0 * entityiterator.getLookAngle().z
                  );
                  if (_ent instanceof ServerPlayer _serverPlayer) {
                     _serverPlayer.connection
                        .teleport(
                           entityiterator.getX() + -2.0 * entityiterator.getLookAngle().x,
                           entityiterator.getY() + 0.8 + -1.3 * entityiterator.getLookAngle().y,
                           entityiterator.getZ() + -2.0 * entityiterator.getLookAngle().z,
                           _ent.getYRot(),
                           _ent.getXRot()
                        );
                  }

                  entity.lookAt(Anchor.EYES, new Vec3(entityiterator.getX(), entityiterator.getY() + 1.6, entityiterator.getZ()));
                  entityiterator.hurt(
                     new DamageSource(
                        world.registryAccess()
                           .registryOrThrow(Registries.DAMAGE_TYPE)
                           .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
                        entity
                     ),
                     4.0F
                  );
                  entityiterator.getPersistentData().putString("target", "");
                  SololevelingMod.queueServerWork(4, () -> {
                     entityiterator.setNoGravity(false);
                     entity.setNoGravity(false);
                  });
               }
            }
         }
      }
   }
}
