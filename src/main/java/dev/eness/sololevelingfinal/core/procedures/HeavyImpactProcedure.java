package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class HeavyImpactProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         CooldownManager.set(entity, "Slash Dash", 200);
         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                  SoundSource.NEUTRAL,
                  2.0F,
                  1.0F
               );
            } else {
               _level.playLocalSound(
                  entity.getX(),
                  entity.getY(),
                  entity.getZ(),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                  SoundSource.NEUTRAL,
                  2.0F,
                  1.0F,
                  false
               );
            }
         }

         boolean _setval = true;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.rushattack = _setval;
            capability.syncPlayerVariables(entity);
         });
         SololevelingMod.queueServerWork(20, () -> {
            boolean _setvalx = false;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.rushattack = _setvalx;
               capability.syncPlayerVariables(entity);
            });
         });
      }
   }
}
