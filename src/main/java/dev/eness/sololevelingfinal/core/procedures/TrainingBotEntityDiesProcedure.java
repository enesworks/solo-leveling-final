package dev.eness.sololevelingfinal.core.procedures;

import java.text.DecimalFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class TrainingBotEntityDiesProcedure {
   public static void execute(LevelAccessor world, Entity sourceentity) {
      if (sourceentity != null) {
         if (sourceentity instanceof Player) {
            boolean _setval = false;
            sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.istraining = _setval;
               capability.syncPlayerVariables(sourceentity);
            });
            double _setvalx = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Xp
               + world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER)
                  / 10.0
                  * (
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .Level
                        + 5.0
                  );
            sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Xp = _setval;
               capability.syncPlayerVariables(sourceentity);
            });
            if (sourceentity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(
                  Component.literal(
                     "Gained"
                        + new DecimalFormat("##.#")
                           .format(
                              world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER)
                                 / 10.0
                                 * (
                                    sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                             .orElse(new SololevelingModVariables.PlayerVariables())
                                          .Level
                                       + 5.0
                                 )
                           )
                        + "xp"
                  ),
                  true
               );
            }
         }
      }
   }
}
