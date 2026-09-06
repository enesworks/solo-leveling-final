package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.util.daily.DailyPunishmentManager;

public class SLRPenaltyTriggerProcedure {
   public static void execute(LevelAccessor world, CommandContext<CommandSourceStack> arguments) {
      if (!world.isClientSide()) {
         try {
            for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
               if (entityiterator instanceof ServerPlayer player) {
                  boolean keepSecret = DailyQuestHelper.isSecretQuest(entityiterator) || DailyQuestHelper.canActivateSecretQuest(entityiterator);
                  DailyQuestHelper.sendQuestFailedChat(entityiterator);
                  DailyQuestHelper.resetDailyProgress(entityiterator);
                  if (keepSecret) {
                     DailyQuestHelper.keepSecretQuestPending(entityiterator);
                  }

                  DailyPunishmentManager.enter(player);
               }
            }
         } catch (CommandSyntaxException e) {
            e.printStackTrace();
         }
      }
   }
}
