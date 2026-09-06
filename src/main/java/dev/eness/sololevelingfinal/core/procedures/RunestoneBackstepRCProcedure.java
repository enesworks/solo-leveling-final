package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.util.RangerCombatManager;

public class RunestoneBackstepRCProcedure {
   public static void execute(Entity entity, ItemStack itemstack) {
      RangerCombatManager.learnFromRunestone(entity, itemstack, "Back Step");
   }
}
