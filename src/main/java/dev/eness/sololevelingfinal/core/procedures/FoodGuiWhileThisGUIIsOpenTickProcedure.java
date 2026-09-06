package dev.eness.sololevelingfinal.core.procedures;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FoodGuiWhileThisGUIIsOpenTickProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof Player _player && _player.containerMenu instanceof Supplier _current && _current.get() instanceof Map _slots) {
            ItemStack _setstack = new ItemStack(Items.BREAD);
            _setstack.setCount(1);
            ((Slot)_slots.get(1)).set(_setstack);
            _player.containerMenu.broadcastChanges();
         }

         if (entity instanceof Player _player && _player.containerMenu instanceof Supplier _current && _current.get() instanceof Map _slots) {
            ItemStack _setstack = new ItemStack(Items.COOKED_BEEF);
            _setstack.setCount(1);
            ((Slot)_slots.get(0)).set(_setstack);
            _player.containerMenu.broadcastChanges();
         }

         if (entity instanceof Player _player && _player.containerMenu instanceof Supplier _current && _current.get() instanceof Map _slots) {
            ItemStack _setstack = new ItemStack(Items.GOLDEN_APPLE);
            _setstack.setCount(1);
            ((Slot)_slots.get(2)).set(_setstack);
            _player.containerMenu.broadcastChanges();
         }
      }
   }
}
