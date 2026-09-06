package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class AttackshardDeathTimeIsReachedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
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
         _level.sendParticles(SololevelingModParticleTypes.SHARD_PARTICLE.get(), x, y, z, 6, 0.0, 0.0, 0.0, 1.0);
      }
   }
}
