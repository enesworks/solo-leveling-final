package dev.eness.sololevelingfinal.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.procedures.CreatingPartyProcedure;
import dev.eness.sololevelingfinal.core.procedures.JoiningPartyProcedure;
import dev.eness.sololevelingfinal.core.procedures.PartyLeaveProcedure;
import dev.eness.sololevelingfinal.core.procedures.PartyMembersProcedure;
import dev.eness.sololevelingfinal.core.procedures.PartyShowProcedure;

@EventBusSubscriber
public final class CreatePartyCommand {
   private CreatePartyCommand() {
   }

   @SubscribeEvent
   public static void registerCommand(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "Party"
                              )
                              .executes(context -> {
                                 PartyShowProcedure.execute(((CommandSourceStack)context.getSource()).getPlayerOrException());
                                 return 1;
                              }))
                           .then(
                              Commands.literal("Create")
                                 .then(((RequiredArgumentBuilder)Commands.argument("name", StringArgumentType.word()).executes(context -> {
                                    CreatingPartyProcedure.execute(context, ((CommandSourceStack)context.getSource()).getPlayerOrException());
                                    return 1;
                                 })).then(Commands.argument("pass", StringArgumentType.word()).executes(context -> {
                                    CreatingPartyProcedure.execute(context, ((CommandSourceStack)context.getSource()).getPlayerOrException());
                                    return 1;
                                 })))
                           ))
                        .then(
                           Commands.literal("Join")
                              .then(
                                 ((RequiredArgumentBuilder)Commands.argument("name", StringArgumentType.word())
                                       .executes(
                                          context -> {
                                             JoiningPartyProcedure.execute(
                                                ((CommandSourceStack)context.getSource()).getUnsidedLevel(),
                                                context,
                                                ((CommandSourceStack)context.getSource()).getPlayerOrException()
                                             );
                                             return 1;
                                          }
                                       ))
                                    .then(
                                       Commands.argument("pass", StringArgumentType.word())
                                          .executes(
                                             context -> {
                                                JoiningPartyProcedure.execute(
                                                   ((CommandSourceStack)context.getSource()).getUnsidedLevel(),
                                                   context,
                                                   ((CommandSourceStack)context.getSource()).getPlayerOrException()
                                                );
                                                return 1;
                                             }
                                          )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("Leave")
                           .executes(
                              context -> {
                                 PartyLeaveProcedure.execute(
                                    ((CommandSourceStack)context.getSource()).getUnsidedLevel(),
                                    ((CommandSourceStack)context.getSource()).getPlayerOrException()
                                 );
                                 return 1;
                              }
                           )
                     ))
                  .then(
                     Commands.literal("Members")
                        .executes(
                           context -> {
                              PartyMembersProcedure.execute(
                                 ((CommandSourceStack)context.getSource()).getUnsidedLevel(), ((CommandSourceStack)context.getSource()).getPlayerOrException()
                              );
                              return 1;
                           }
                        )
                  ))
               .then(Commands.literal("Show").executes(context -> {
                  PartyShowProcedure.execute(((CommandSourceStack)context.getSource()).getPlayerOrException());
                  return 1;
               }))
         );
   }
}
