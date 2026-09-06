package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SLRPlayerProcedure {
   public static void execute(CommandContext<CommandSourceStack> arguments) {
      try {
         for (Entity entityiterator : EntityArgument.getEntities(arguments, "name")) {
            boolean _setval = BoolArgumentType.getBool(arguments, "player");
            entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Player = _setval;
               capability.syncPlayerVariables(entityiterator);
            });
         }
      } catch (CommandSyntaxException e) {
         e.printStackTrace();
      }
   }
}
