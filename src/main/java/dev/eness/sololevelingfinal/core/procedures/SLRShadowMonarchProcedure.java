package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SLRShadowMonarchProcedure {
   public static void execute(CommandContext<CommandSourceStack> arguments) {
      try {
         for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
            double _setval = 1.0;
            entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.JOB = _setval;
               capability.syncPlayerVariables(entityiterator);
            });
         }
      } catch (CommandSyntaxException e) {
         e.printStackTrace();
      }
   }
}
