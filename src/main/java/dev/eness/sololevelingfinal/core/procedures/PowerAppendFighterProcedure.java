package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class PowerAppendFighterProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double rank = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).HunterRank;
         if (rank > 1.0) {
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
            }

            if (rank > 2.0) {
               if (Math.random() < 0.6666667F) {
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
                  }
               } else if (Math.random() < 0.6F
                  && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
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
               }

               if (rank > 3.0) {
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
                  }

                  if (rank > 4.0) {
                     if (Math.random() < 0.6666667F
                        && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
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
                     }

                     if (rank > 5.0
                        && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
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
                     }
                  }
               }
            }
         }
      }
   }
}
