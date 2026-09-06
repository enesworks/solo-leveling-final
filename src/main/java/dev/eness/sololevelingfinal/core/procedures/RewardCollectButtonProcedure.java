package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.RewardManager;

public class RewardCollectButtonProcedure {
   public static void execute(Entity entity, int slot) {
      if (entity != null) {
         RewardManager.claimReward(entity, slot);
      }
   }
}
