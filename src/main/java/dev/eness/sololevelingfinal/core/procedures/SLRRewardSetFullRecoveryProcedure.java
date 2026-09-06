package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.RewardManager;

public class SLRRewardSetFullRecoveryProcedure {
   public static void execute(CommandContext<CommandSourceStack> arguments) {
      double reward_slot = 0.0;
      boolean collect_prev_reward = false;
      reward_slot = DoubleArgumentType.getDouble(arguments, "slot");
      collect_prev_reward = BoolArgumentType.getBool(arguments, "AutoCollect");

      try {
         for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
            RewardManager.setFullRecoveryReward(entityiterator, (int)reward_slot, collect_prev_reward);
         }
      } catch (CommandSyntaxException e) {
         e.printStackTrace();
      }
   }
}
