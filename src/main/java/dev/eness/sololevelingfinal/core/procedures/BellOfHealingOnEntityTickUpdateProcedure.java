package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class BellOfHealingOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double xRadius = 0.0;
         double loop = 0.0;
         double zRadius = 0.0;
         double particleAmount = 0.0;
         loop = 0.0;
         particleAmount = 20.0;
         xRadius = 10.0;
         zRadius = 10.0;

         while (loop < particleAmount) {
            world.addParticle(
               ParticleTypes.GLOW_SQUID_INK,
               x + 0.5 + Math.cos((Math.PI * 2) / particleAmount * loop) * xRadius,
               y - 2.0,
               z + 0.5 + Math.sin((Math.PI * 2) / particleAmount * loop) * zRadius,
               0.0,
               0.05,
               0.0
            );
            loop++;
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, x, y, z, 3, 9.0, 9.0, 9.0, 1.0);
         }

         if (entity instanceof TamableAnimal _tamEnt && _tamEnt.isTame() && (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != null) {
            Entity _owner = entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null;
            if (CooldownManager.isOnCooldown(_owner, "Blessing Mark")) {
               if (CooldownManager.getRemainingTicks(_owner, "Blessing Mark") % 20 == 0 && world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:bellirng")),
                        SoundSource.NEUTRAL,
                        0.1F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:bellirng")), SoundSource.NEUTRAL, 0.1F, 1.0F, false
                     );
                  }
               }
            } else if (!entity.level().isClientSide()) {
               entity.discard();
            }

            Vec3 _center = new Vec3(x, y - 2.0, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(15.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if ((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .party
                  .equals("")) {
                  if ((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) instanceof LivingEntity _entity
                     && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 2));
                  }

                  if ((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) instanceof LivingEntity _entity
                     && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20, 0));
                  }
               } else if ((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .party
                  .equals(
                     entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .party
                  )) {
                  if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 2));
                  }

                  if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20, 0));
                  }
               }
            }
         }
      }
   }
}
