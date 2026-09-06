package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public class CeismicslashbarDisplayOverlayIngameProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.KAMISH_WRATH.get()
            && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.KAMISH_WRATH_2.get();
   }
}
