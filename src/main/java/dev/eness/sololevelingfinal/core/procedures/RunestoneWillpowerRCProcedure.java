package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public final class RunestoneWillpowerRCProcedure {
   private RunestoneWillpowerRCProcedure() {
   }

   public static void execute(Entity entity, ItemStack itemstack) {
      TankerProgressionHelper.learnFromRunestone(entity, itemstack, "Willpower");
   }
}
