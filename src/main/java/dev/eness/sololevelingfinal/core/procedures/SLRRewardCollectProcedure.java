package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.RewardManager;

public class SLRRewardCollectProcedure {
   public static void execute(CommandContext<CommandSourceStack> arguments) {
      try {
         for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
            while (RewardManager.hasRewards(entityiterator)) {
               RewardManager.claimReward(entityiterator, 1);
            }
         }
      } catch (CommandSyntaxException e) {
         e.printStackTrace();
      }
   }
}
