package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
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
import dev.eness.sololevelingfinal.core.util.AbilityDestructionManager;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class UpforceSlashProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double delay = 0.0;
         double num = 0.0;
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
            >= 1000.0) {
            if (!CooldownManager.isOnCooldown(entity, "Ground Slam")) {
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .MP
                  - 1000.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.MP = _setval;
                  capability.syncPlayerVariables(entity);
               });
               CooldownManager.set(entity, "Ground Slam", 400);
               CooldownManager.set(entity, "mana_refresh", 50);
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.anvil.place")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.anvil.place")), SoundSource.NEUTRAL, 0.5F, 1.0F, false
                     );
                  }
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 0.5F, 1.0F, false
                     );
                  }
               }

               world.addParticle(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), entity.getY(), entity.getZ(), 0.0, 1.0, 0.0);
               if (entity instanceof ServerPlayer player) {
                  AbilityDestructionManager.impact(
                     player, AbilityDestructionManager.Profile.FIGHTER_SLAM, new Vec3(x, y, z), TemporaryStatBonusManager.effectiveStrength(player), false
                  );
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(16.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator instanceof LivingEntity
                     && entityiterator != entity
                     && !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt))) {
                     entityiterator.hurt(
                        new DamageSource(
                           world.registryAccess()
                              .registryOrThrow(Registries.DAMAGE_TYPE)
                              .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:fighter"))),
                           entity
                        ),
                        (float)(
                           (1.0 + TemporaryStatBonusManager.effectiveStrength(entity) / 15.0)
                              * (
                                 5.0
                                    / Math.sqrt(
                                       Math.pow(entity.getX() - entityiterator.getX(), 2.0)
                                          + Math.pow(entity.getY() - entityiterator.getY(), 2.0)
                                          + Math.pow(entity.getZ() - entityiterator.getZ(), 2.0)
                                    )
                              )
                        )
                     );
                  }
               }
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Not enough MP!"), true);
         }
      }
   }
}
