package dev.eness.sololevelingfinal.core.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.procedures.SoloDungeonStuckProcedureProcedure;

@EventBusSubscriber
public class SoloDungeonStuckCommand {
   @SubscribeEvent
   public static void registerCommand(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)Commands.literal("solodungeonstuck")
               .executes(
                  arguments -> {
                     boolean escaped = SoloDungeonStuckProcedureProcedure.execute(((CommandSourceStack)arguments.getSource()).getPlayerOrException());
                     if (!escaped) {
                        ((CommandSourceStack)arguments.getSource()).sendFailure(Component.literal("No active dungeon recovery point was found."));
                        return 0;
                     } else {
                        ((CommandSourceStack)arguments.getSource())
                           .sendSuccess(() -> Component.literal("Returned safely from the dungeon.").withStyle(ChatFormatting.YELLOW), false);
                        return 1;
                     }
                  }
               )
         );
   }
}
