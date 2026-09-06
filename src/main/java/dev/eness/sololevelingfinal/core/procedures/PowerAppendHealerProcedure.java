package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class PowerAppendHealerProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double rank = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).HunterRank;
         if (rank > 1.0) {
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
            }

            if (rank > 2.0) {
               if (Math.random() < 0.6666667F) {
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
                  }
               } else if (Math.random() < 0.6666667F
                  && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
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
               }

               if (rank > 3.0) {
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
                  }

                  if (rank > 4.0) {
                     if (Math.random() < 0.6666667F
                        && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
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
                     }

                     if (rank > 5.0
                        && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .Plist
                           .contains("Blessing Mark")) {
                        String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .Plist
                           + "Sword of Light,";
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                           capability.Plist = _setval;
                           capability.syncPlayerVariables(entity);
                        });
                     }
                  }
               }
            }
         }
      }
   }
}
