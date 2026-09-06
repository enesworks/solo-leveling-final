package dev.eness.sololevelingfinal.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

@EventBusSubscriber
public class SlNotifyCommand {
   private static final int DURATION = 80;

   @SubscribeEvent
   public static void registerCommand(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("slnotify")
                        .requires(s -> s.hasPermission(2)))
                     .then(Commands.literal("title").then(Commands.argument("text", StringArgumentType.greedyString()).executes(ctx -> {
                        ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                        SystemNotifications.showTitle(player, -12597505, 80, comp(StringArgumentType.getString(ctx, "text")));
                        return 1;
                     }))))
                  .then(Commands.literal("under").then(Commands.argument("text", StringArgumentType.greedyString()).executes(ctx -> {
                     ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                     SystemNotifications.showUnder(player, -12597505, 80, comp(StringArgumentType.getString(ctx, "text")));
                     return 1;
                  }))))
               .then(Commands.literal("both").then(Commands.argument("text", StringArgumentType.greedyString()).executes(ctx -> {
                  ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
                  String[] parts = StringArgumentType.getString(ctx, "text").split("\\|", 2);
                  Component title = comp(parts[0].trim());
                  Component under = parts.length > 1 && !parts[1].trim().isEmpty() ? comp(parts[1].trim()) : null;
                  SystemNotifications.showTitleUnder(player, -12597505, 80, title, under);
                  return 1;
               })))
         );
   }

   private static Component comp(String s) {
      return Component.literal(s.replace("\\n", "\n").replace('&', '§'));
   }
}
