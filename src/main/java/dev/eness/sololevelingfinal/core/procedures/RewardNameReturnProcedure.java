package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.RewardManager;

public class RewardNameReturnProcedure {
   public static String execute(Entity entity) {
      return execute(entity, 1);
   }

   public static String execute(Entity entity, int slot) {
      return entity == null ? "" : RewardManager.displayName(entity, slot);
   }
}
