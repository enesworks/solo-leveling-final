package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class PowerAppendAssassinProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double rank = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).HunterRank;
         if (rank > 1.0) {
            if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .Plist
               .contains("Ghost Step")) {
               String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                  + "Ghost Step,";
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.Plist = _setval;
                  capability.syncPlayerVariables(entity);
               });
            }

            if (rank > 2.0) {
               if (Math.random() < 0.75
                  && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                     .contains("Night Rend")) {
                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Plist
                     + "Night Rend,";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Plist = _setval;
                     capability.syncPlayerVariables(entity);
                  });
               }

               if (rank > 3.0) {
                  if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                     .contains("Stealth")) {
                     String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .Plist
                        + "Stealth,";
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
                           .contains("Flash Cut")) {
                        String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .Plist
                           + "Flash Cut,";
                        entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                           capability.Plist = _setval;
                           capability.syncPlayerVariables(entity);
                        });
                     }

                     if (rank > 5.0
                        && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .Plist
                           .contains("Dualwield")) {
                        String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .Plist
                           + "Dualwield,";
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
