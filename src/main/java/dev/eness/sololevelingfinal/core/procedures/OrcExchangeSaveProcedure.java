package dev.eness.sololevelingfinal.core.procedures;

import java.text.DecimalFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class OrcExchangeSaveProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (DoesHaveOrcProcedure.execute(entity)) {
            if (!ExchangeCon7Procedure.execute(entity)) {
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .shadowstorageusage
                  - 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.shadowstorageusage = _setval;
                  capability.syncPlayerVariables(entity);
               });
               _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .orcmax
                  - 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.orcmax = _setval;
                  capability.syncPlayerVariables(entity);
               });
               String _setvalx = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .ExchangeCords
                  + new DecimalFormat("##.#").format(entity.getX())
                  + " "
                  + new DecimalFormat("##.#").format(entity.getY())
                  + " "
                  + new DecimalFormat("##.#").format(entity.getZ())
                  + ",";
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.ExchangeCords = _setval;
                  capability.syncPlayerVariables(entity);
               });
               String _setvalxx = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .ExchangeDimensions
                  + "execute in "
                  + entity.level().dimension().location().toString()
                  + " run tp @p "
                  + new DecimalFormat("##.#").format(entity.getX())
                  + " "
                  + new DecimalFormat("##.#").format(entity.getY())
                  + " "
                  + new DecimalFormat("##.#").format(entity.getZ())
                  + ",";
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.ExchangeDimensions = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof Player _player) {
                  _player.closeContainer();
               }

               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("Shadow has been placed at the coordinate!"), true);
               }

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

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.hurt")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.hurt")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }
            } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("You cant place any more shadows around!"), true);
            }
         }
      }
   }
}
