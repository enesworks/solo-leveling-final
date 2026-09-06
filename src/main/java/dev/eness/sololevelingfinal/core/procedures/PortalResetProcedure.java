package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PortalResetProcedure {
   @SubscribeEvent
   public static void onCommand(CommandEvent event) {
      Entity entity = ((CommandSourceStack)event.getParseResults().getContext().getSource()).getEntity();
      if (entity != null) {
         execute(event);
      }
   }

   public static void execute() {
      execute(null);
   }

   private static void execute(@Nullable Event event) {
   }
}
