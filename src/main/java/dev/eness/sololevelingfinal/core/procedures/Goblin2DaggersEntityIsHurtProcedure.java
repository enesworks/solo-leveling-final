package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;

public class Goblin2DaggersEntityIsHurtProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      if (world instanceof Level _level) {
         if (!_level.isClientSide()) {
            _level.playSound(
               (Player)null,
               BlockPos.containing(x, y, z),
               ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.villager.hurt")),
               SoundSource.NEUTRAL,
               0.5F,
               2.0F
            );
         } else {
            _level.playLocalSound(
               x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.villager.hurt")), SoundSource.NEUTRAL, 0.5F, 2.0F, false
            );
         }
      }
   }
}
