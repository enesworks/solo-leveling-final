package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;

public class PanelEarlyThisGUIIsClosedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      if (world instanceof Level _level && _level.isClientSide()) {
         _level.playLocalSound(
            x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:panelclose")), SoundSource.NEUTRAL, 0.5F, 1.0F, false
         );
      }
   }
}
