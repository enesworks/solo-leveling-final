package dev.eness.sololevelingfinal.core.procedures;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.LevelRewardRules;

public final class SLRSetLevelProcedure {
   private SLRSetLevelProcedure() {
   }

   public static void execute(CommandContext<CommandSourceStack> arguments) {
      int targetLevel = IntegerArgumentType.getInteger(arguments, "amount");

      try {
         for (Entity target : EntityArgument.getEntities(arguments, "name")) {
            setLevel(target, targetLevel);
         }
      } catch (CommandSyntaxException exception) {
         exception.printStackTrace();
      }
   }

   private static void setLevel(Entity target, int targetLevel) {
      target.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
         int currentLevel = Math.max(0, (int)Math.floor(variables.Level));
         double difference = targetLevel - variables.Level;
         if (difference > 0.0) {
            variables.Level = targetLevel;
            variables.Vitality += difference;
            variables.Strength += difference;
            variables.Intelligence += difference;
            variables.Speed += difference;
            variables.perception += difference;
            variables.SkillPoints = variables.SkillPoints + LevelRewardRules.skillPointsForLevels(Math.max(0, targetLevel - currentLevel));
         } else {
            if (!(difference < 0.0)) {
               return;
            }

            variables.Level = targetLevel;
            variables.Vitality = targetLevel;
            variables.Strength = targetLevel;
            variables.Intelligence = targetLevel;
            variables.Speed = targetLevel;
            variables.perception = targetLevel;
         }

         variables.syncPlayerVariables(target);
      });
   }
}
