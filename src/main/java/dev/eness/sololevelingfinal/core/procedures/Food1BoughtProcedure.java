package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemHandlerHelper;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class Food1BoughtProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
            >= 25.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(Items.BREAD);
               _setstack.setCount(16);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 25.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }
      }
   }
}
