package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.daily.DailyQuestLifecycleManager;

public class SLRFinishDailyProcedure {
   public static void execute(CommandContext<CommandSourceStack> arguments) {
      try {
         for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
            if (entityiterator instanceof ServerPlayer player) {
               DailyQuestLifecycleManager.finishQuestNow(player);
            }
         }
      } catch (CommandSyntaxException e) {
         e.printStackTrace();
      }
   }
}
