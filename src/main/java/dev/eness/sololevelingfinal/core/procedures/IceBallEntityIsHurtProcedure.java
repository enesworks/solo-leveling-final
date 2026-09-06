package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class IceBallEntityIsHurtProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (entity.getPersistentData().getString("caster").equals(sourceentity.getDisplayName().getString())) {
            CooldownManager.set(sourceentity, "job_1", 120);
            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12.5), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entity != entityiterator && !entity.getPersistentData().getString("caster").equals(entityiterator.getDisplayName().getString())) {
                  if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.FREEZE.get(), 40, 1));
                  }

                  entityiterator.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), sourceentity),
                     (float)(4.0 + TemporaryStatBonusManager.effectiveIntelligence(sourceentity) / 15.0)
                  );
               }
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                     SoundSource.NEUTRAL,
                     5.0F,
                     0.75F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 5.0F, 0.75F, false
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
                     3.0F,
                     0.75F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 3.0F, 0.75F, false
                  );
               }
            }

            int horizontalRadiusSphere = 7;
            int verticalRadiusSphere = 5;
            int yIterationsSphere = verticalRadiusSphere;

            for (int i = -yIterationsSphere; i <= yIterationsSphere; i++) {
               for (int xi = -horizontalRadiusSphere; xi <= horizontalRadiusSphere; xi++) {
                  for (int zi = -horizontalRadiusSphere; zi <= horizontalRadiusSphere; zi++) {
                     double distanceSq = (double)(xi * xi) / (horizontalRadiusSphere * horizontalRadiusSphere)
                        + (double)(i * i) / (verticalRadiusSphere * verticalRadiusSphere)
                        + (double)(zi * zi) / (horizontalRadiusSphere * horizontalRadiusSphere);
                     if (distanceSq <= 1.0) {
                        if (world instanceof ServerLevel _level) {
                           _level.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                       CommandSource.NULL,
                                       new Vec3(x + xi, y + i, z + zi),
                                       Vec2.ZERO,
                                       _level,
                                       4,
                                       "",
                                       Component.literal(""),
                                       _level.getServer(),
                                       null
                                    )
                                    .withSuppressedOutput(),
                                 "/particle snowflake ~ ~ ~ 1 0 2 0 2 force"
                              );
                        }

                        world.levelEvent(2001, BlockPos.containing(x + xi, y + i, z + zi), Block.getId(Blocks.ICE.defaultBlockState()));
                     }
                  }
               }
            }

            if (!entity.level().isClientSide()) {
               entity.discard();
            }

            sourceentity.getPersistentData().putBoolean("UsingIceBall", false);
         }
      }
   }
}
