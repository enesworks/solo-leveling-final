package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class MasterylvlupFighterProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         while (
            !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Slash Dash")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Slash Fury")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                     .contains("Cross Strike")
                  && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                     .contains("Critical Strike")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Ground Slam")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Sword Dance")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Sword of Light")
         ) {
            if (Math.random() < 0.16666667F) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Slash Dash")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Slash Dash,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Slash Dash"), false);
                  }
                  break;
               }
            } else if (Math.random() < 0.2F) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Slash Fury")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Slash Fury,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Slash Fury"), false);
                  }
                  break;
               }
            } else if (Math.random() < 0.25) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                     .contains("Cross Strike")
                  && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                     .contains("Critical Strike")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Cross Strike,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Cross Strike"), false);
                  }
                  break;
               }
            } else if (Math.random() < 0.33333334F) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Ground Slam")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Ground Slam,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Ground Slam"), false);
                  }
                  break;
               }
            } else if (Math.random() < 0.5) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Sword Dance")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Sword Dance,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Sword Dance"), false);
                  }
                  break;
               }
            } else if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .Plist
               .contains("Sword of Light")) {
               String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                  + "Sword of Light,";
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.Plist = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("Gained skill: Sword of Light"), false);
               }
               break;
            }
         }
      }
   }
}
