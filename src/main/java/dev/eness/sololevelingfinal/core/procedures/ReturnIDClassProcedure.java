package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public class ReturnIDClassProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         return entity instanceof LivingEntity _entity && _entity.isHolding(SololevelingModItems.HUNTER_ID.get())
            ? "Hunter Type: §l" + (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getOrCreateTag().getString("Class")
            : "";
      }
   }
}
