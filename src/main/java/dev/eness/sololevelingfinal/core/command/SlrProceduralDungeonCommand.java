package dev.eness.sololevelingfinal.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.stream.IntStream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.DungeonTheme;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonGenerator;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonResult;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonSettings;

@EventBusSubscriber
public class SlrProceduralDungeonCommand {
   private static final SuggestionProvider<CommandSourceStack> RANK_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
      Arrays.stream(ProceduralDungeonRank.values()).map(Enum::name), builder
   );
   private static final SuggestionProvider<CommandSourceStack> THEME_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
      Arrays.stream(DungeonTheme.values()).map(theme -> theme.name().toLowerCase()), builder
   );
   private static final SuggestionProvider<CommandSourceStack> COMPLEXITY_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
      IntStream.rangeClosed(1, 10).mapToObj(Integer::toString), builder
   );

   @SubscribeEvent
   public static void registerCommand(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("slr").requires(source -> source.hasPermission(3)))
               .then(
                  Commands.argument("name", EntityArgument.player())
                     .then(
                        Commands.literal("proceduraldungeon")
                           .then(
                              Commands.argument("rank", StringArgumentType.word())
                                 .suggests(RANK_SUGGESTIONS)
                                 .then(
                                    Commands.argument("theme", StringArgumentType.word())
                                       .suggests(THEME_SUGGESTIONS)
                                       .then(
                                          Commands.argument("complexity", IntegerArgumentType.integer(1, 10))
                                             .suggests(COMPLEXITY_SUGGESTIONS)
                                             .executes(
                                                arguments -> {
                                                   ServerPlayer player = EntityArgument.getPlayer(arguments, "name");
                                                   if (player.level() instanceof ServerLevel level) {
                                                      ProceduralDungeonRank var7 = ProceduralDungeonRank.fromString(
                                                         StringArgumentType.getString(arguments, "rank")
                                                      );
                                                      DungeonTheme theme = DungeonTheme.fromString(StringArgumentType.getString(arguments, "theme"));
                                                      int complexity = IntegerArgumentType.getInteger(arguments, "complexity");
                                                      ProceduralDungeonResult result = ProceduralDungeonGenerator.generate(
                                                         level, player.blockPosition(), new ProceduralDungeonSettings(var7, theme, complexity), player
                                                      );
                                                      player.teleportTo(
                                                         level,
                                                         result.startPos.getX() + 0.5,
                                                         result.startPos.getY(),
                                                         result.startPos.getZ() + 0.5,
                                                         player.getYRot(),
                                                         player.getXRot()
                                                      );
                                                      ((CommandSourceStack)arguments.getSource())
                                                         .sendSuccess(
                                                            () -> Component.literal(
                                                               "Generated "
                                                                  + var7.name()
                                                                  + " "
                                                                  + theme.name().toLowerCase()
                                                                  + " procedural dungeon: "
                                                                  + result.rooms
                                                                  + " rooms, "
                                                                  + result.monsters
                                                                  + " mobs. Start "
                                                                  + result.startPos.getX()
                                                                  + " "
                                                                  + result.startPos.getY()
                                                                  + " "
                                                                  + result.startPos.getZ()
                                                            ),
                                                            true
                                                         );
                                                      return 1;
                                                   } else {
                                                      return 0;
                                                   }
                                                }
                                             )
                                       )
                                 )
                           )
                     )
               )
         );
   }
}
