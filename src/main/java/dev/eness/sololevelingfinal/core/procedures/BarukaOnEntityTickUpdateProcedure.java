package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class BarukaOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double Chain = 0.0;
         double ChainWait = 0.0;
         if (!world.isClientSide()) {
            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
               entity.setSprinting(true);
               entity.getPersistentData().putDouble("IA", entity.getPersistentData().getDouble("IA") + 1.0);
               entity.lookAt(
                  Anchor.EYES,
                  new Vec3(
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 1.5,
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
                  )
               );
               if (entity instanceof Mob _entity) {
                  _entity.getNavigation()
                     .moveTo(
                        (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                        (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY(),
                        (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ(),
                        1.0
                     );
               }
            } else {
               entity.getPersistentData().putDouble("IA", 0.0);
               entity.setSprinting(false);
            }

            if (entity.getPersistentData().getDouble("IA") == 60.0) {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 120, 0.5, 1.8, 0.5, 0.0);
               }

               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 1, false, false));
               }

               CooldownManager.set(entity, "Stealth", 60);
            }

            if (entity.getPersistentData().getDouble("IA") == 130.0) {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 120, 0.5, 1.8, 0.5, 0.0);
               }

               BarukaTeleportProcedure.execute(world, entity);
            }

            if (entity.getPersistentData().getDouble("IA") == 140.0) {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 120, 0.5, 1.8, 0.5, 0.0);
               }

               BarukaTeleportProcedure.execute(world, entity);
            }

            if (entity.getPersistentData().getDouble("IA") == 150.0) {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 120, 0.5, 1.8, 0.5, 0.0);
               }

               BarukaTeleportProcedure.execute(world, entity);
            }

            if (entity.getPersistentData().getDouble("IA") == 160.0) {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 120, 0.5, 1.8, 0.5, 0.0);
               }

               BarukaTeleportProcedure.execute(world, entity);
            }

            if (entity.getPersistentData().getDouble("IA") == 170.0) {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 120, 0.5, 1.8, 0.5, 0.0);
               }

               BarukaTeleportProcedure.execute(world, entity);
            }

            if (entity.getPersistentData().getDouble("IA") == 180.0) {
               entity.getPersistentData().putDouble("IA", 0.0);
            }
         }
      }
   }
}
