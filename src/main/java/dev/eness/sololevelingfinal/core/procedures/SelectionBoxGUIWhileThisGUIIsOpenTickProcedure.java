package dev.eness.sololevelingfinal.core.procedures;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public class SelectionBoxGUIWhileThisGUIIsOpenTickProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get(0)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               != SololevelingModItems.KAMISH_WRATH.get()
            && entity instanceof Player _player
            && _player.containerMenu instanceof Supplier _current
            && _current.get() instanceof Map _slots) {
            ItemStack _setstack = new ItemStack(SololevelingModItems.KAMISH_WRATH.get());
            _setstack.setCount(1);
            ((Slot)_slots.get(0)).set(_setstack);
            _player.containerMenu.broadcastChanges();
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get(1)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               != SololevelingModItems.DEMON_KINGS_LONG_SWORD.get()
            && entity instanceof Player _player
            && _player.containerMenu instanceof Supplier _current
            && _current.get() instanceof Map _slots) {
            ItemStack _setstack = new ItemStack(SololevelingModItems.DEMON_KINGS_LONG_SWORD.get());
            _setstack.setCount(1);
            ((Slot)_slots.get(1)).set(_setstack);
            _player.containerMenu.broadcastChanges();
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get(2)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               != SololevelingModItems.DEMON_KINGS_DAGGER.get()
            && entity instanceof Player _player
            && _player.containerMenu instanceof Supplier _current
            && _current.get() instanceof Map _slots) {
            ItemStack _setstack = new ItemStack(SololevelingModItems.DEMON_KINGS_DAGGER.get());
            _setstack.setCount(1);
            ((Slot)_slots.get(2)).set(_setstack);
            _player.containerMenu.broadcastChanges();
         }
      }
   }
}
