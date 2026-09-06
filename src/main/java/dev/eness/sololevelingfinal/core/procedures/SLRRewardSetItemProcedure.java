package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.RewardManager;

public class SLRRewardSetItemProcedure {
   public static void execute(CommandContext<CommandSourceStack> arguments) {
      double reward_slot = 0.0;
      boolean collect_prev_reward = false;
      String reward_to_recieve = "";
      String reward_to_assign = "";
      String item = "";
      ItemStack itemname = ItemStack.EMPTY;
      reward_slot = DoubleArgumentType.getDouble(arguments, "slot");
      collect_prev_reward = BoolArgumentType.getBool(arguments, "AutoCollect");
      itemname = ItemArgument.getItem(arguments, "item").getItem().getDefaultInstance();
      ItemStack itemstack = ItemArgument.getItem(arguments, "item").getItem().getDefaultInstance();
      String itemResourceLocation = ForgeRegistries.ITEMS.getKey(itemstack.getItem()).toString();
      reward_to_assign = "ITEM:" + itemResourceLocation;

      try {
         for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
            if (reward_slot == 1.0) {
               reward_to_recieve = entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .reward_1;
               if (collect_prev_reward) {
                  RewardManager.appendReward(entityiterator, reward_to_recieve);
               }

               String _setval = reward_to_assign;
               entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.reward_1 = _setval;
                  capability.syncPlayerVariables(entityiterator);
               });
            }

            if (reward_slot == 2.0) {
               reward_to_recieve = entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .reward_2;
               if (collect_prev_reward) {
                  RewardManager.appendReward(entityiterator, reward_to_recieve);
               }

               String _setval = reward_to_assign;
               entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.reward_2 = _setval;
                  capability.syncPlayerVariables(entityiterator);
               });
            }

            if (reward_slot == 3.0) {
               reward_to_recieve = entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .reward_3;
               if (collect_prev_reward) {
                  RewardManager.appendReward(entityiterator, reward_to_recieve);
               }

               String _setval = reward_to_assign;
               entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.reward_3 = _setval;
                  capability.syncPlayerVariables(entityiterator);
               });
            }
         }
      } catch (CommandSyntaxException e) {
         e.printStackTrace();
      }
   }
}
