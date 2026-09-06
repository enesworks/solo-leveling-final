package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class MasterylvlupHealerProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         while (
            !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Heal Beam")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Purification")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Blessing Mark")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Physical Buff")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Haste Buff")
               || !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Overheal")
         ) {
            if (Math.random() < 0.16666667F) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Heal Beam")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Heal Beam,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Heal Beam"), false);
                  }
                  break;
               }
            } else if (Math.random() < 0.2F) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Purification")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Purification,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Purification"), false);
                  }
                  break;
               }
            } else if (Math.random() < 0.25) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Blessing Mark")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Blessing Mark,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Blessing Mark"), false);
                  }
                  break;
               }
            } else if (Math.random() < 0.33333334F) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Physical Buff")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Physical Buff,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Physical Buff"), false);
                  }
                  break;
               }
            } else if (Math.random() < 0.5) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Haste Buff")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Haste Buff,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Gained skill: Haste Buff"), false);
                  }
                  break;
               }
            } else if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .Plist
               .contains("Overheal")) {
               String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                  + "Overheal,";
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.Plist = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("Gained skill: Overheal"), false);
               }
               break;
            }
         }
      }
   }
}
