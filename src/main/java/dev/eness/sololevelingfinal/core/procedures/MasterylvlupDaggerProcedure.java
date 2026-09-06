package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.RulersAuthorityManager;

public class MasterylvlupDaggerProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         String list = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Plist;
         String unlocked = !list.contains("Dagger Throw")
            ? "Dagger Throw"
            : (
               RulersAuthorityManager.hasAuthority(entity) && !list.contains("Dagger Rush")
                  ? "Dagger Rush"
                  : (!list.contains("Critical Attack") ? "Critical Attack" : "")
            );
         if (!unlocked.isEmpty()) {
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Plist = capability.Plist + unlocked + ",";
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player player && !player.level().isClientSide()) {
               player.displayClientMessage(Component.literal("Gained skill: " + unlocked), false);
            }
         }
      }
   }
}
