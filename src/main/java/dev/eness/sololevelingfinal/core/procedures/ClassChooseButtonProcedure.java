package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ClassChooseButtonProcedure {
   public static void execute(Entity entity, int classNum) {
      if (entity != null) {
         double _setval = classNum;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Classes = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (entity instanceof Player _player) {
            _player.closeContainer();
         }

         if (entity instanceof Player _player) {
            ItemStack _stktoremove = new ItemStack(SololevelingModItems.CLASS_CHOOSER.get());
            _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
         }
      }
   }
}
