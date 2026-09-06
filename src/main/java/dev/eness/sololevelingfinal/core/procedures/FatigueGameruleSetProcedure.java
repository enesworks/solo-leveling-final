package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class FatigueGameruleSetProcedure {
   @SubscribeEvent
   public static void onCommand(CommandEvent event) {
      Entity entity = ((CommandSourceStack)event.getParseResults().getContext().getSource()).getEntity();
      if (entity != null) {
         execute(event, entity.level(), event.getParseResults().getReader().getString());
      }
   }

   public static void execute(LevelAccessor world, String command) {
      execute(null, world, command);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, String command) {
      if (command != null) {
         if (command.contains("/gamerule soloFatigue") && !world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.SOLO_FATIGUE)) {
            for (Entity entityiterator : new ArrayList<>(world.players())) {
               double _setval = 0.0;
               entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.Fatigue = _setval;
                  capability.syncPlayerVariables(entityiterator);
               });
            }
         }
      }
   }
}
