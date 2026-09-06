package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;

public class AfterImageOnInitialEntitySpawnProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         entity.setNoGravity(true);
         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")),
                  SoundSource.NEUTRAL,
                  1.0F,
                  1.0F
               );
            } else {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
               );
            }
         }

         SololevelingMod.queueServerWork(10, () -> {
            if (world instanceof ServerLevel _levelx) {
               _levelx.sendParticles(ParticleTypes.GLOW, x, y, z, 5, 0.3, 0.3, 0.3, 0.1);
            }

            if (!entity.level().isClientSide()) {
               entity.discard();
            }
         });
      }
   }
}
