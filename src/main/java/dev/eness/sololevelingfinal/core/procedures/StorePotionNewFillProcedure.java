package dev.eness.sololevelingfinal.core.procedures;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public class StorePotionNewFillProcedure {
   public static void execute(Entity entity) {
      if (entity != null && entity instanceof Player player) {
         if (player.containerMenu instanceof Supplier<?> current && current.get() instanceof Map<?, ?> slots) {
            Item[] var10 = new Item[]{
               SololevelingModItems.SMALL_HEALTH_POTION.get(),
               SololevelingModItems.MEDIUM_HEALTH_POTION.get(),
               SololevelingModItems.LARGE_HEALTH_POTION.get(),
               SololevelingModItems.SMALL_MANA_POTION.get(),
               SololevelingModItems.MEDIUM_MANA_POTION.get(),
               SololevelingModItems.LARGE_MANA_POTION.get(),
               SololevelingModItems.SMALL_FATIGUE_POTION.get(),
               SololevelingModItems.MEDIUM_FATIGUE_POTION.get(),
               SololevelingModItems.LARGE_FATIGUE_POTION.get()
            };

            for (int i = 0; i < var10.length; i++) {
               if (slots.get(i) instanceof Slot s) {
                  ItemStack stack = new ItemStack(var10[i]);
                  stack.setCount(1);
                  s.set(stack);
               }
            }

            player.containerMenu.broadcastChanges();
         }
      }
   }
}
