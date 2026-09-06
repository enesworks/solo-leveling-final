package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ShadowIgrisRightClickedOnEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if ((entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null) == sourceentity) {
            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 20, 1.0, 1.0, 1.0, 1.0);
            }

            if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).berserk
               )
             {
               boolean _setval = false;
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.berserk = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               if (sourceentity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal(entity.getDisplayName().getString() + " is now \"Passive\""), true);
               }
            } else if (!sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .berserk) {
               boolean _setval = true;
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.berserk = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               if (sourceentity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal(entity.getDisplayName().getString() + " is now \"Berserk\""), true);
               }
            }
         }
      }
   }
}
