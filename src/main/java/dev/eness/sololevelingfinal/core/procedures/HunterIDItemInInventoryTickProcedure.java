package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.item.HunterIDItem;

public class HunterIDItemInInventoryTickProcedure {
   public static void execute(Entity entity, ItemStack itemstack) {
      HunterIDItem.refreshStack(entity, itemstack);
   }
}
