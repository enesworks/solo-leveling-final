package dev.eness.sololevelingfinal.core.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import dev.eness.sololevelingfinal.core.dungeon.DungeonGenerator;
import dev.eness.sololevelingfinal.core.dungeon.DungeonTheme;
import dev.eness.sololevelingfinal.core.entity.SilladBossEntity;
import dev.eness.sololevelingfinal.core.guild.GuildData;
import dev.eness.sololevelingfinal.core.guild.GuildSavedData;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.procedures.DKCLevelItemRightclickedProcedure;
import dev.eness.sololevelingfinal.core.procedures.DKCPathTeleportProcedure;
import dev.eness.sololevelingfinal.core.procedures.DungeonDimensionPlayerLeavesDimensionProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRARankProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRBRankProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRCLassRandomProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRCRankProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRClassProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRDRankProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRDungeonBreakProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRERankProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRFinishDailyProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRPenaltyTriggerProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRPlayerProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRResetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRRewardCollectProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRRewardSetFullRecoveryProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRRewardSetItemProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRRewardSetItemboxProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRRewardSetSkillPointsProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRRewardsSetGoldsProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRSRankProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRSetLevelProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRcompletedDungeonPlayerProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRcompletedDungeonsProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRdimensionProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRgoldgetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRgoldresetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRgoldsetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRshopgetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRshopsetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsagilityaddProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsagilitysetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsintelligenceaddProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsintelligencesetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatssensesetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsspaddProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsspsetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsstrengthaddProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsstrengthsetProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsvitalityaddProcedure;
import dev.eness.sololevelingfinal.core.procedures.SLRstatsvitalitysetProcedure;
import dev.eness.sololevelingfinal.core.util.CartenonTempleGenerator;
import dev.eness.sololevelingfinal.core.util.DeveloperModeManager;
import dev.eness.sololevelingfinal.core.util.DkcStructurePreviewBuilder;
import dev.eness.sololevelingfinal.core.util.FrostMonarchCastleGenerator;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.SilladBossSpawnManager;
import dev.eness.sololevelingfinal.core.util.VesselManager;

@EventBusSubscriber
public class SlrCommand {
   private static final String RECOVERY_COOLDOWN_TAG = "slr_recovery_command_cooldown";
   private static final long RECOVERY_COOLDOWN_TICKS = 200L;
   private static final Map<String, List<String>> STRUCTURE_SETS = Map.ofEntries(
      Map.entry(
         "instance",
         List.of(
            "instance_first_room",
            "instancestart",
            "dunduninstance1",
            "dunduninstance2",
            "instanceboss",
            "instancegoblin",
            "instancegoblinlycan",
            "instancegoblinnlycan",
            "instancelycan"
         )
      ),
      Map.entry(
         "instancedungeon",
         List.of(
            "instance_first_room",
            "instancestart",
            "dunduninstance1",
            "dunduninstance2",
            "instanceboss",
            "instancegoblin",
            "instancegoblinlycan",
            "instancegoblinnlycan",
            "instancelycan"
         )
      ),
      Map.entry("kasaka", List.of("updatedkasakadungeon", "kasakadungeon")),
      Map.entry(
         "erank",
         List.of(
            "erankstart",
            "erankroom1",
            "erankroom2",
            "erankroom3",
            "erankroom4",
            "erankleftrightand",
            "erankrightleftand",
            "erankbig1",
            "erankbig2",
            "erankboss"
         )
      ),
      Map.entry(
         "e",
         List.of(
            "erankstart",
            "erankroom1",
            "erankroom2",
            "erankroom3",
            "erankroom4",
            "erankleftrightand",
            "erankrightleftand",
            "erankbig1",
            "erankbig2",
            "erankboss"
         )
      ),
      Map.entry(
         "cemetery",
         List.of(
            "b_rank_cemetery_enterance",
            "b_rank_cemetery_corridor1",
            "b_rank_cemetery_corridor2",
            "b_rank_cemetery_corridor3",
            "b_rank_cemetery_corridor4",
            "b_rank_cemetery_turn_left",
            "b_rank_cemetery_turn_right",
            "b_rank_cemetery_mid1",
            "b_rank_cemetery_mid2",
            "b_rank_cemetery_boss"
         )
      ),
      Map.entry(
         "brank",
         List.of(
            "b_rank_cemetery_enterance",
            "b_rank_cemetery_corridor1",
            "b_rank_cemetery_corridor2",
            "b_rank_cemetery_corridor3",
            "b_rank_cemetery_corridor4",
            "b_rank_cemetery_turn_left",
            "b_rank_cemetery_turn_right",
            "b_rank_cemetery_mid1",
            "b_rank_cemetery_mid2",
            "b_rank_cemetery_boss"
         )
      ),
      Map.entry("lab", List.of("labdunstart", "labduncor1", "labduncor2", "labduncor3", "labdunrturn", "labdunlturn", "labdunboss")),
      Map.entry("large", List.of("updatedlargerandstart", "bigroom1", "bigroom2", "bigroom3", "bigroom4", "bigroom5", "bigroomboss")),
      Map.entry("largecave", List.of("updatedlargerandstart", "bigroom1", "bigroom2", "bigroom3", "bigroom4", "bigroom5", "bigroomboss")),
      Map.entry("kamish", List.of("kamishupdatedstart", "kamishroom1", "kamishroom2", "kamishroom3", "kamishblock", "kamishboss")),
      Map.entry("dkc", List.of("dkc_left", "dkc_middle", "dkc_middle_boss", "dkc_middle_boss_baran", "dkc_middle_boss_cerberus", "dkc_right")),
      Map.entry("demoncastle", List.of("dkc_left", "dkc_middle", "dkc_middle_boss", "dkc_middle_boss_baran", "dkc_middle_boss_cerberus", "dkc_right")),
      Map.entry("kargalgan", List.of("dun_kargalgan_enterance", "dun_kargalgan", "dun_kargalgan_bossroom")),
      Map.entry("igris", List.of("jobchange_dungeon1", "updateddungeonigris", "dungeonigris", "igrisdungeon", "igrisdungeon1", "igrisdungeon2")),
      Map.entry("beru", List.of("updateddungeonberu", "berudungeon", "dungeonberu", "dungeonberu2")),
      Map.entry("ancientgolem", List.of("dungeon_ancientgolem")),
      Map.entry("lush", List.of("lushcave")),
      Map.entry(
         "random",
         List.of(
            "drankdunnew", "updatedrandomdungeon", "randomdun1", "dungeon1", "testroom1", "testroom2", "testroom3", "testroom11", "testroom21", "testroom31"
         )
      )
   );

   @SubscribeEvent
   public static void registerCommand(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                    "slr"
                                 )
                                 .then(Commands.literal("help").executes(SlrCommand::showRecoveryHelp)))
                              .then(Commands.literal("recovery").executes(SlrCommand::showRecoveryHelp)))
                           .then(Commands.literal("escape").executes(SlrCommand::escapeDungeon)))
                        .then(Commands.literal("stuck").executes(SlrCommand::recoverFromStuck)))
                     .then(
                        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("dkc")
                                 .then(Commands.literal("status").executes(SlrCommand::showDkcStatus)))
                              .then(Commands.literal("unstuck").executes(SlrCommand::unstuckDkc)))
                           .then(Commands.literal("leave").executes(SlrCommand::escapeDungeon))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)Commands.literal("boss").requires(SlrCommand::isDeveloperSource))
                        .then(Commands.literal("spawn").then(Commands.literal("sillad").executes(SlrCommand::spawnSillad)))
                  ))
               .then(
                  ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                                           "name", EntityArgument.players()
                                                                        )
                                                                        .requires(s -> s.hasPermission(3)))
                                                                     .then(
                                                                        Commands.literal("player")
                                                                           .then(Commands.argument("player", BoolArgumentType.bool()).executes(arguments -> {
                                                                              Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                              double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                              double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                              double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                              Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                              if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                 entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                              }

                                                                              Direction direction = Direction.DOWN;
                                                                              if (entity != null) {
                                                                                 direction = entity.getDirection();
                                                                              }

                                                                              SLRPlayerProcedure.execute(arguments);
                                                                              return 0;
                                                                           }))
                                                                     ))
                                                                  .then(
                                                                     Commands.literal("level")
                                                                        .then(
                                                                           Commands.argument("amount", IntegerArgumentType.integer(0, 500))
                                                                              .executes(arguments -> {
                                                                                 Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                                 double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                                 double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                                 double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                                 Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                                 if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                                 }

                                                                                 Direction direction = Direction.DOWN;
                                                                                 if (entity != null) {
                                                                                    direction = entity.getDirection();
                                                                                 }

                                                                                 SLRSetLevelProcedure.execute(arguments);
                                                                                 return 0;
                                                                              })
                                                                        )
                                                                  ))
                                                               .then(
                                                                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                                                          "class"
                                                                                       )
                                                                                       .then(Commands.literal("assassin").executes(arguments -> {
                                                                                          Level world = ((CommandSourceStack)arguments.getSource())
                                                                                             .getUnsidedLevel();
                                                                                          double x = ((CommandSourceStack)arguments.getSource())
                                                                                             .getPosition()
                                                                                             .x();
                                                                                          double y = ((CommandSourceStack)arguments.getSource())
                                                                                             .getPosition()
                                                                                             .y();
                                                                                          double z = ((CommandSourceStack)arguments.getSource())
                                                                                             .getPosition()
                                                                                             .z();
                                                                                          Entity entity = ((CommandSourceStack)arguments.getSource())
                                                                                             .getEntity();
                                                                                          if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                             entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                                          }

                                                                                          Direction direction = Direction.DOWN;
                                                                                          if (entity != null) {
                                                                                             direction = entity.getDirection();
                                                                                          }

                                                                                          SLRClassProcedure.execute(arguments, 1);
                                                                                          return 0;
                                                                                       })))
                                                                                    .then(Commands.literal("mage").executes(arguments -> {
                                                                                       Level world = ((CommandSourceStack)arguments.getSource())
                                                                                          .getUnsidedLevel();
                                                                                       double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                                       double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                                       double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                                       Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                                       if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                          entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                                       }

                                                                                       Direction direction = Direction.DOWN;
                                                                                       if (entity != null) {
                                                                                          direction = entity.getDirection();
                                                                                       }

                                                                                       SLRClassProcedure.execute(arguments, 2);
                                                                                       return 0;
                                                                                    })))
                                                                                 .then(Commands.literal("fighter").executes(arguments -> {
                                                                                    Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                                    double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                                    double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                                    double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                                    Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                                    if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                       entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                                    }

                                                                                    Direction direction = Direction.DOWN;
                                                                                    if (entity != null) {
                                                                                       direction = entity.getDirection();
                                                                                    }

                                                                                    SLRClassProcedure.execute(arguments, 3);
                                                                                    return 0;
                                                                                 })))
                                                                              .then(Commands.literal("tanker").executes(arguments -> {
                                                                                 Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                                 double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                                 double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                                 double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                                 Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                                 if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                                 }

                                                                                 Direction direction = Direction.DOWN;
                                                                                 if (entity != null) {
                                                                                    direction = entity.getDirection();
                                                                                 }

                                                                                 SLRClassProcedure.execute(arguments, 4);
                                                                                 return 0;
                                                                              })))
                                                                           .then(Commands.literal("healer").executes(arguments -> {
                                                                              Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                              double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                              double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                              double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                              Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                              if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                 entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                              }

                                                                              Direction direction = Direction.DOWN;
                                                                              if (entity != null) {
                                                                                 direction = entity.getDirection();
                                                                              }

                                                                              SLRClassProcedure.execute(arguments, 5);
                                                                              return 0;
                                                                           })))
                                                                        .then(Commands.literal("ranger").executes(arguments -> {
                                                                           Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                           double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                           double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                           double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                           Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                           if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                              entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                           }

                                                                           Direction direction = Direction.DOWN;
                                                                           if (entity != null) {
                                                                              direction = entity.getDirection();
                                                                           }

                                                                           SLRClassProcedure.execute(arguments, 6);
                                                                           return 0;
                                                                        })))
                                                                     .then(Commands.literal("random").executes(arguments -> {
                                                                        Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                        double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                        double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                        double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                        Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                        if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                           entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                        }

                                                                        Direction direction = Direction.DOWN;
                                                                        if (entity != null) {
                                                                           direction = entity.getDirection();
                                                                        }

                                                                        SLRCLassRandomProcedure.execute(arguments);
                                                                        return 0;
                                                                     }))
                                                               ))
                                                            .then(Commands.literal("reset").executes(arguments -> {
                                                               Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                               double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                               double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                               double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                               Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                               if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                  entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                               }

                                                               Direction direction = Direction.DOWN;
                                                               if (entity != null) {
                                                                  direction = entity.getDirection();
                                                               }

                                                               SLRResetProcedure.execute(world, arguments);
                                                               return 0;
                                                            })))
                                                         .then(
                                                            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                                           "vessel"
                                                                        )
                                                                        .then(Commands.literal("select").executes(VesselManager::openSelection)))
                                                                     .then(Commands.literal("reset").executes(VesselManager::reset)))
                                                                  .then(
                                                                     ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                                                          "ruler"
                                                                                       )
                                                                                       .then(
                                                                                          Commands.literal("ashborn")
                                                                                             .executes(
                                                                                                arguments -> VesselManager.assign(arguments, "ruler", "ashborn")
                                                                                             )
                                                                                       ))
                                                                                    .then(
                                                                                       Commands.literal("thomas_andre")
                                                                                          .executes(
                                                                                             arguments -> VesselManager.assign(
                                                                                                arguments, "ruler", "thomas_andre"
                                                                                             )
                                                                                          )
                                                                                    ))
                                                                                 .then(
                                                                                    Commands.literal("liu_zhigang")
                                                                                       .executes(
                                                                                          arguments -> VesselManager.assign(arguments, "ruler", "liu_zhigang")
                                                                                       )
                                                                                 ))
                                                                              .then(
                                                                                 Commands.literal("christopher_reed")
                                                                                    .executes(
                                                                                       arguments -> VesselManager.assign(arguments, "ruler", "christopher_reed")
                                                                                    )
                                                                              ))
                                                                           .then(
                                                                              Commands.literal("sung_il_hwan")
                                                                                 .executes(
                                                                                    arguments -> VesselManager.assign(arguments, "ruler", "sung_il_hwan")
                                                                                 )
                                                                           ))
                                                                        .then(
                                                                           Commands.literal("go_gunhee")
                                                                              .executes(arguments -> VesselManager.assign(arguments, "ruler", "go_gunhee"))
                                                                        )
                                                                  ))
                                                               .then(
                                                                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                                                 "monarch"
                                                                              )
                                                                              .then(
                                                                                 Commands.literal("sillad")
                                                                                    .executes(arguments -> VesselManager.assign(arguments, "monarch", "sillad"))
                                                                              ))
                                                                           .then(
                                                                              Commands.literal("baran")
                                                                                 .executes(arguments -> VesselManager.assign(arguments, "monarch", "baran"))
                                                                           ))
                                                                        .then(
                                                                           Commands.literal("rakan")
                                                                              .executes(arguments -> VesselManager.assign(arguments, "monarch", "rakan"))
                                                                        ))
                                                                     .then(
                                                                        Commands.literal("antares")
                                                                           .executes(arguments -> VesselManager.assign(arguments, "monarch", "antares"))
                                                                     )
                                                               )
                                                         ))
                                                      .then(
                                                         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("dkc")
                                                                  .then(
                                                                     Commands.argument("floor", IntegerArgumentType.integer(1, 20))
                                                                        .executes(SlrCommand::setDkcFloor)
                                                                  ))
                                                               .then(
                                                                  Commands.literal("floor")
                                                                     .then(
                                                                        Commands.argument("floor", IntegerArgumentType.integer(1, 20))
                                                                           .executes(SlrCommand::setDkcFloor)
                                                                     )
                                                               ))
                                                            .then(
                                                               Commands.literal("level")
                                                                  .then(
                                                                     Commands.argument("floor", IntegerArgumentType.integer(1, 20))
                                                                        .executes(SlrCommand::setDkcFloor)
                                                                  )
                                                            )
                                                      ))
                                                   .then(
                                                      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                                           "rank"
                                                                        )
                                                                        .then(
                                                                           Commands.literal("E")
                                                                              .executes(arguments -> forEachTarget(arguments, SLRERankProcedure::execute))
                                                                        ))
                                                                     .then(
                                                                        Commands.literal("D")
                                                                           .executes(arguments -> forEachTarget(arguments, SLRDRankProcedure::execute))
                                                                     ))
                                                                  .then(
                                                                     Commands.literal("C")
                                                                        .executes(arguments -> forEachTarget(arguments, SLRCRankProcedure::execute))
                                                                  ))
                                                               .then(
                                                                  Commands.literal("B")
                                                                     .executes(arguments -> forEachTarget(arguments, SLRBRankProcedure::execute))
                                                               ))
                                                            .then(
                                                               Commands.literal("A")
                                                                  .executes(arguments -> forEachTarget(arguments, SLRARankProcedure::execute))
                                                            ))
                                                         .then(
                                                            Commands.literal("S")
                                                               .executes(
                                                                  arguments -> forEachTarget(
                                                                     arguments,
                                                                     target -> SLRSRankProcedure.execute(
                                                                        target.level(), target.getX(), target.getY(), target.getZ(), target
                                                                     )
                                                                  )
                                                               )
                                                         )
                                                   ))
                                                .then(
                                                   ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("OP")
                                                            .then(
                                                               Commands.literal("DungeonBreak")
                                                                  .executes(
                                                                     arguments -> forEachTarget(
                                                                        arguments,
                                                                        target -> SLRDungeonBreakProcedure.execute(
                                                                           target.level(), target.getX(), target.getY(), target.getZ(), target
                                                                        )
                                                                     )
                                                                  )
                                                            ))
                                                         .then(Commands.literal("TriggerPenaltyZone").executes(arguments -> {
                                                            Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                            double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                            double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                            double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                            Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                            if (entity == null && world instanceof ServerLevel _servLevel) {
                                                               entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                            }

                                                            Direction direction = Direction.DOWN;
                                                            if (entity != null) {
                                                               direction = entity.getDirection();
                                                            }

                                                            SLRPenaltyTriggerProcedure.execute(world, arguments);
                                                            return 0;
                                                         })))
                                                      .then(Commands.literal("FinishDaily").executes(arguments -> {
                                                         Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                         double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                         double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                         double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                         Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                         if (entity == null && world instanceof ServerLevel _servLevel) {
                                                            entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                         }

                                                         Direction direction = Direction.DOWN;
                                                         if (entity != null) {
                                                            direction = entity.getDirection();
                                                         }

                                                         SLRFinishDailyProcedure.execute(arguments);
                                                         return 0;
                                                      }))
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)Commands.literal("shop")
                                                      .then(
                                                         Commands.literal("set")
                                                            .then(
                                                               Commands.argument("amount", DoubleArgumentType.doubleArg(1.0, 6.0))
                                                                  .then(
                                                                     Commands.argument("item", ItemArgument.item(event.getBuildContext()))
                                                                        .executes(arguments -> {
                                                                           Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                           double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                           double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                           double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                           Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                           if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                              entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                           }

                                                                           Direction direction = Direction.DOWN;
                                                                           if (entity != null) {
                                                                              direction = entity.getDirection();
                                                                           }

                                                                           SLRshopsetProcedure.execute(arguments);
                                                                           return 0;
                                                                        })
                                                                  )
                                                            )
                                                      ))
                                                   .then(
                                                      Commands.literal("get")
                                                         .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1.0, 6.0)).executes(arguments -> {
                                                            Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                            double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                            double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                            double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                            Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                            if (entity == null && world instanceof ServerLevel _servLevel) {
                                                               entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                            }

                                                            Direction direction = Direction.DOWN;
                                                            if (entity != null) {
                                                               direction = entity.getDirection();
                                                            }

                                                            SLRshopgetProcedure.execute(arguments);
                                                            return 0;
                                                         }))
                                                   )
                                             ))
                                          .then(
                                             ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("gold")
                                                      .then(
                                                         Commands.literal("Set")
                                                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0)).executes(arguments -> {
                                                               Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                               double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                               double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                               double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                               Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                               if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                  entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                               }

                                                               Direction direction = Direction.DOWN;
                                                               if (entity != null) {
                                                                  direction = entity.getDirection();
                                                               }

                                                               SLRgoldsetProcedure.execute(arguments);
                                                               return 0;
                                                            }))
                                                      ))
                                                   .then(
                                                      Commands.literal("Add")
                                                         .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1.0)).executes(arguments -> {
                                                            Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                            double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                            double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                            double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                            Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                            if (entity == null && world instanceof ServerLevel _servLevel) {
                                                               entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                            }

                                                            Direction direction = Direction.DOWN;
                                                            if (entity != null) {
                                                               direction = entity.getDirection();
                                                            }

                                                            SLRgoldgetProcedure.execute(arguments);
                                                            return 0;
                                                         }))
                                                   ))
                                                .then(Commands.literal("reset").executes(arguments -> {
                                                   Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                   double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                   double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                   double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                   Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                   if (entity == null && world instanceof ServerLevel _servLevel) {
                                                      entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                   }

                                                   Direction direction = Direction.DOWN;
                                                   if (entity != null) {
                                                      direction = entity.getDirection();
                                                   }

                                                   SLRgoldresetProcedure.execute(arguments);
                                                   return 0;
                                                }))
                                          ))
                                       .then(
                                          ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                               "stats"
                                                            )
                                                            .then(
                                                               ((LiteralArgumentBuilder)Commands.literal("skillpoints")
                                                                     .then(
                                                                        Commands.literal("Set")
                                                                           .then(
                                                                              Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                                                                                 .executes(arguments -> {
                                                                                    Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                                    double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                                    double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                                    double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                                    Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                                    if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                       entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                                    }

                                                                                    Direction direction = Direction.DOWN;
                                                                                    if (entity != null) {
                                                                                       direction = entity.getDirection();
                                                                                    }

                                                                                    SLRstatsspsetProcedure.execute(arguments);
                                                                                    return 0;
                                                                                 })
                                                                           )
                                                                     ))
                                                                  .then(
                                                                     Commands.literal("Add")
                                                                        .then(
                                                                           Commands.argument("amount", DoubleArgumentType.doubleArg(1.0))
                                                                              .executes(arguments -> {
                                                                                 Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                                 double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                                 double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                                 double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                                 Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                                 if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                                 }

                                                                                 Direction direction = Direction.DOWN;
                                                                                 if (entity != null) {
                                                                                    direction = entity.getDirection();
                                                                                 }

                                                                                 SLRstatsspaddProcedure.execute(arguments);
                                                                                 return 0;
                                                                              })
                                                                        )
                                                                  )
                                                            ))
                                                         .then(
                                                            ((LiteralArgumentBuilder)Commands.literal("strength")
                                                                  .then(
                                                                     Commands.literal("Set")
                                                                        .then(
                                                                           Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                                                                              .executes(arguments -> {
                                                                                 Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                                 double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                                 double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                                 double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                                 Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                                 if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                                 }

                                                                                 Direction direction = Direction.DOWN;
                                                                                 if (entity != null) {
                                                                                    direction = entity.getDirection();
                                                                                 }

                                                                                 SLRstatsstrengthsetProcedure.execute(arguments);
                                                                                 return 0;
                                                                              })
                                                                        )
                                                                  ))
                                                               .then(
                                                                  Commands.literal("Add")
                                                                     .then(
                                                                        Commands.argument("amount", DoubleArgumentType.doubleArg(1.0)).executes(arguments -> {
                                                                           Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                           double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                           double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                           double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                           Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                           if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                              entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                           }

                                                                           Direction direction = Direction.DOWN;
                                                                           if (entity != null) {
                                                                              direction = entity.getDirection();
                                                                           }

                                                                           SLRstatsstrengthaddProcedure.execute(arguments);
                                                                           return 0;
                                                                        })
                                                                     )
                                                               )
                                                         ))
                                                      .then(
                                                         ((LiteralArgumentBuilder)Commands.literal("vitality")
                                                               .then(
                                                                  Commands.literal("Set")
                                                                     .then(
                                                                        Commands.argument("amount", DoubleArgumentType.doubleArg(0.0)).executes(arguments -> {
                                                                           Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                           double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                           double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                           double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                           Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                           if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                              entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                           }

                                                                           Direction direction = Direction.DOWN;
                                                                           if (entity != null) {
                                                                              direction = entity.getDirection();
                                                                           }

                                                                           SLRstatsvitalitysetProcedure.execute(arguments);
                                                                           return 0;
                                                                        })
                                                                     )
                                                               ))
                                                            .then(
                                                               Commands.literal("Add")
                                                                  .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1.0)).executes(arguments -> {
                                                                     Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                     double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                     double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                     double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                     Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                     if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                        entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                     }

                                                                     Direction direction = Direction.DOWN;
                                                                     if (entity != null) {
                                                                        direction = entity.getDirection();
                                                                     }

                                                                     SLRstatsvitalityaddProcedure.execute(arguments);
                                                                     return 0;
                                                                  }))
                                                            )
                                                      ))
                                                   .then(
                                                      ((LiteralArgumentBuilder)Commands.literal("agility")
                                                            .then(
                                                               Commands.literal("Set")
                                                                  .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0)).executes(arguments -> {
                                                                     Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                     double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                     double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                     double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                     Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                     if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                        entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                     }

                                                                     Direction direction = Direction.DOWN;
                                                                     if (entity != null) {
                                                                        direction = entity.getDirection();
                                                                     }

                                                                     SLRstatsagilitysetProcedure.execute(arguments);
                                                                     return 0;
                                                                  }))
                                                            ))
                                                         .then(
                                                            Commands.literal("Add")
                                                               .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1.0)).executes(arguments -> {
                                                                  Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                  double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                  double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                  double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                  Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                  if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                     entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                  }

                                                                  Direction direction = Direction.DOWN;
                                                                  if (entity != null) {
                                                                     direction = entity.getDirection();
                                                                  }

                                                                  SLRstatsagilityaddProcedure.execute(arguments);
                                                                  return 0;
                                                               }))
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal("sense")
                                                      .then(
                                                         Commands.literal("Set")
                                                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0, 100.0)).executes(arguments -> {
                                                               Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                               double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                               double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                               double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                               Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                               if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                  entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                               }

                                                               Direction direction = Direction.DOWN;
                                                               if (entity != null) {
                                                                  direction = entity.getDirection();
                                                               }

                                                               SLRstatssensesetProcedure.execute(arguments);
                                                               return 0;
                                                            }))
                                                      )
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)Commands.literal("intelligence")
                                                      .then(
                                                         Commands.literal("Set")
                                                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0)).executes(arguments -> {
                                                               Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                               double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                               double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                               double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                               Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                               if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                  entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                               }

                                                               Direction direction = Direction.DOWN;
                                                               if (entity != null) {
                                                                  direction = entity.getDirection();
                                                               }

                                                               SLRstatsintelligencesetProcedure.execute(arguments);
                                                               return 0;
                                                            }))
                                                      ))
                                                   .then(
                                                      Commands.literal("Add")
                                                         .then(Commands.argument("amount", DoubleArgumentType.doubleArg(1.0)).executes(arguments -> {
                                                            Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                            double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                            double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                            double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                            Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                            if (entity == null && world instanceof ServerLevel _servLevel) {
                                                               entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                            }

                                                            Direction direction = Direction.DOWN;
                                                            if (entity != null) {
                                                               direction = entity.getDirection();
                                                            }

                                                            SLRstatsintelligenceaddProcedure.execute(arguments);
                                                            return 0;
                                                         }))
                                                   )
                                             )
                                       ))
                                    .then(
                                       ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debug")
                                                .then(
                                                   Commands.literal("Dimension")
                                                      .executes(arguments -> forEachTarget(arguments, SLRdimensionProcedure::execute))
                                                ))
                                             .then(
                                                Commands.literal("ClearedGates")
                                                   .executes(
                                                      arguments -> forEachTarget(
                                                         arguments, target -> SLRcompletedDungeonsProcedure.execute(target.level(), target)
                                                      )
                                                   )
                                             ))
                                          .then(
                                             Commands.literal("CurrentGatesStatus")
                                                .executes(
                                                   arguments -> forEachTarget(
                                                      arguments, target -> SLRcompletedDungeonPlayerProcedure.execute(target.level(), target)
                                                   )
                                                )
                                          )
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)Commands.literal("rewards").then(Commands.literal("collect").executes(arguments -> {
                                          Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                          double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                          double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                          double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                          Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                          if (entity == null && world instanceof ServerLevel _servLevel) {
                                             entity = FakePlayerFactory.getMinecraft(_servLevel);
                                          }

                                          Direction direction = Direction.DOWN;
                                          if (entity != null) {
                                             direction = entity.getDirection();
                                          }

                                          SLRRewardCollectProcedure.execute(arguments);
                                          return 0;
                                       })))
                                       .then(
                                          Commands.literal("set")
                                             .then(
                                                ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                                  "slot", DoubleArgumentType.doubleArg(1.0, 3.0)
                                                               )
                                                               .then(
                                                                  Commands.literal("RandomItem")
                                                                     .then(Commands.argument("AutoCollect", BoolArgumentType.bool()).executes(arguments -> {
                                                                        Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                        double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                        double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                        double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                        Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                        if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                           entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                        }

                                                                        Direction direction = Direction.DOWN;
                                                                        if (entity != null) {
                                                                           direction = entity.getDirection();
                                                                        }

                                                                        SLRRewardSetItemboxProcedure.execute(arguments);
                                                                        return 0;
                                                                     }))
                                                               ))
                                                            .then(
                                                               Commands.literal("FullRecovery")
                                                                  .then(Commands.argument("AutoCollect", BoolArgumentType.bool()).executes(arguments -> {
                                                                     Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                     double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                     double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                     double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                     Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                     if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                        entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                     }

                                                                     Direction direction = Direction.DOWN;
                                                                     if (entity != null) {
                                                                        direction = entity.getDirection();
                                                                     }

                                                                     SLRRewardSetFullRecoveryProcedure.execute(arguments);
                                                                     return 0;
                                                                  }))
                                                            ))
                                                         .then(
                                                            Commands.literal("SkillPoints")
                                                               .then(
                                                                  Commands.argument("amount", DoubleArgumentType.doubleArg(1.0))
                                                                     .then(Commands.argument("AutoCollect", BoolArgumentType.bool()).executes(arguments -> {
                                                                        Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                        double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                        double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                        double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                        Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                        if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                           entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                        }

                                                                        Direction direction = Direction.DOWN;
                                                                        if (entity != null) {
                                                                           direction = entity.getDirection();
                                                                        }

                                                                        SLRRewardSetSkillPointsProcedure.execute(arguments);
                                                                        return 0;
                                                                     }))
                                                               )
                                                         ))
                                                      .then(
                                                         Commands.literal("Golds")
                                                            .then(
                                                               Commands.argument("amount", DoubleArgumentType.doubleArg(1.0))
                                                                  .then(Commands.argument("AutoCollect", BoolArgumentType.bool()).executes(arguments -> {
                                                                     Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                     double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                     double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                     double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                     Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                     if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                        entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                     }

                                                                     Direction direction = Direction.DOWN;
                                                                     if (entity != null) {
                                                                        direction = entity.getDirection();
                                                                     }

                                                                     SLRRewardsSetGoldsProcedure.execute(arguments);
                                                                     return 0;
                                                                  }))
                                                            )
                                                      ))
                                                   .then(
                                                      Commands.literal("Item")
                                                         .then(
                                                            Commands.argument("item", ItemArgument.item(event.getBuildContext()))
                                                               .then(Commands.argument("AutoCollect", BoolArgumentType.bool()).executes(arguments -> {
                                                                  Level world = ((CommandSourceStack)arguments.getSource()).getUnsidedLevel();
                                                                  double x = ((CommandSourceStack)arguments.getSource()).getPosition().x();
                                                                  double y = ((CommandSourceStack)arguments.getSource()).getPosition().y();
                                                                  double z = ((CommandSourceStack)arguments.getSource()).getPosition().z();
                                                                  Entity entity = ((CommandSourceStack)arguments.getSource()).getEntity();
                                                                  if (entity == null && world instanceof ServerLevel _servLevel) {
                                                                     entity = FakePlayerFactory.getMinecraft(_servLevel);
                                                                  }

                                                                  Direction direction = Direction.DOWN;
                                                                  if (entity != null) {
                                                                     direction = entity.getDirection();
                                                                  }

                                                                  SLRRewardSetItemProcedure.execute(arguments);
                                                                  return 0;
                                                               }))
                                                         )
                                                   )
                                             )
                                       )
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("shadows")
                                          .then(
                                             Commands.literal("add")
                                                .then(
                                                   Commands.argument("shadow", StringArgumentType.word())
                                                      .suggests(
                                                         (context, builder) -> SharedSuggestionProvider.suggest(
                                                            ShadowMonarchManager.shadowCommandTargets().stream().filter(value -> !"all".equals(value)).toList(),
                                                            builder
                                                         )
                                                      )
                                                      .executes(arguments -> modifyShadowSoldiers(arguments, 1))
                                                )
                                          ))
                                       .then(
                                          Commands.literal("remove")
                                             .then(
                                                Commands.argument("shadow", StringArgumentType.word())
                                                   .suggests(
                                                      (context, builder) -> SharedSuggestionProvider.suggest(
                                                         ShadowMonarchManager.shadowCommandTargets().stream().filter(value -> !"all".equals(value)).toList(),
                                                         builder
                                                      )
                                                   )
                                                   .executes(arguments -> modifyShadowSoldiers(arguments, -1))
                                             )
                                       ))
                                    .then(
                                       Commands.literal("level")
                                          .then(
                                             ((RequiredArgumentBuilder)Commands.argument("shadow", StringArgumentType.word())
                                                   .suggests(
                                                      (context, builder) -> SharedSuggestionProvider.suggest(
                                                         ShadowMonarchManager.shadowCommandTargets(), builder
                                                      )
                                                   )
                                                   .then(
                                                      Commands.literal("add")
                                                         .then(
                                                            Commands.argument("levels", IntegerArgumentType.integer(1, 1000000))
                                                               .executes(arguments -> modifyShadowLevels(arguments, true))
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal("set")
                                                      .then(
                                                         Commands.argument("level", IntegerArgumentType.integer(1, 1000000))
                                                            .executes(arguments -> modifyShadowLevels(arguments, false))
                                                      )
                                                )
                                          )
                                    )
                              ))
                           .then(
                              ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                   "guild"
                                                )
                                                .then(
                                                   Commands.literal("list")
                                                      .executes(
                                                         arguments -> {
                                                            CommandSourceStack source = (CommandSourceStack)arguments.getSource();
                                                            ServerLevel sl = source.getLevel();
                                                            Collection<GuildData> guilds = GuildSavedData.get(sl).allGuilds();
                                                            if (guilds.isEmpty()) {
                                                               source.sendSuccess(() -> Component.literal("§7No guilds exist yet."), false);
                                                            } else {
                                                               source.sendSuccess(() -> Component.literal("§e══ Guild List ══"), false);

                                                               for (GuildData g : guilds) {
                                                                  GuildData gf = g;
                                                                  source.sendSuccess(
                                                                     () -> Component.literal(
                                                                        "§f"
                                                                           + gf.name
                                                                           + " §7(Lv."
                                                                           + gf.level
                                                                           + ")  §8Owner: §f"
                                                                           + gf.ownerName
                                                                           + "  §8Clears: §e"
                                                                           + gf.totalClears
                                                                     ),
                                                                     false
                                                                  );
                                                               }
                                                            }

                                                            return 0;
                                                         }
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("level")
                                                   .then(
                                                      Commands.argument("amount", DoubleArgumentType.doubleArg(1.0, 10.0))
                                                         .executes(
                                                            arguments -> {
                                                               ServerLevel sl = ((CommandSourceStack)arguments.getSource()).getLevel();

                                                               for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
                                                                  GuildData guild = GuildSavedData.get(sl).getGuildForPlayer(target.getUUID());
                                                                  if (guild != null) {
                                                                     guild.level = (int)DoubleArgumentType.getDouble(arguments, "amount");
                                                                     GuildSavedData.get(sl).markDirty();
                                                                     GuildData gf = guild;
                                                                     ((CommandSourceStack)arguments.getSource())
                                                                        .sendSuccess(
                                                                           () -> Component.literal("§aSet guild §e" + gf.name + " §alevel to §e" + gf.level),
                                                                           false
                                                                        );
                                                                  } else {
                                                                     ServerPlayer tf = target;
                                                                     ((CommandSourceStack)arguments.getSource())
                                                                        .sendFailure(
                                                                           Component.literal("§c" + tf.getName().getString() + " is not in any guild.")
                                                                        );
                                                                  }
                                                               }

                                                               return 0;
                                                            }
                                                         )
                                                   )
                                             ))
                                          .then(
                                             Commands.literal("xp")
                                                .then(
                                                   Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                                                      .executes(
                                                         arguments -> {
                                                            ServerLevel sl = ((CommandSourceStack)arguments.getSource()).getLevel();

                                                            for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
                                                               GuildData guild = GuildSavedData.get(sl).getGuildForPlayer(target.getUUID());
                                                               if (guild != null) {
                                                                  guild.xp = (long)DoubleArgumentType.getDouble(arguments, "amount");
                                                                  GuildSavedData.get(sl).markDirty();
                                                                  GuildData gf = guild;
                                                                  ((CommandSourceStack)arguments.getSource())
                                                                     .sendSuccess(
                                                                        () -> Component.literal("§aSet guild §e" + gf.name + " §aXP to §e" + gf.xp), false
                                                                     );
                                                               } else {
                                                                  ServerPlayer tf = target;
                                                                  ((CommandSourceStack)arguments.getSource())
                                                                     .sendFailure(Component.literal("§c" + tf.getName().getString() + " is not in any guild."));
                                                               }
                                                            }

                                                            return 0;
                                                         }
                                                      )
                                                )
                                          ))
                                       .then(
                                          Commands.literal("give")
                                             .executes(
                                                arguments -> {
                                                   for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
                                                      target.getInventory().add(new ItemStack(SololevelingModItems.GUILD_COMPUTER.get()));
                                                      ServerPlayer tf = target;
                                                      ((CommandSourceStack)arguments.getSource())
                                                         .sendSuccess(
                                                            () -> Component.literal("§aGave §eGuild Computer §ato §f" + tf.getName().getString()), false
                                                         );
                                                   }

                                                   return 0;
                                                }
                                             )
                                       ))
                                    .then(Commands.literal("leave").executes(arguments -> {
                                       CommandSourceStack source = (CommandSourceStack)arguments.getSource();
                                       ServerLevel sl = source.getLevel();
                                       GuildSavedData data = GuildSavedData.get(sl);
                                       int changed = 0;

                                       for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
                                          GuildData guild = data.getGuildForPlayer(target.getUUID());
                                          if (guild == null) {
                                             source.sendFailure(Component.literal("§c" + target.getName().getString() + " is not in any guild."));
                                          } else {
                                             String guildName = guild.name;
                                             String targetName = target.getName().getString();
                                             if (guild.ownerUUID.equals(target.getUUID())) {
                                                data.deleteGuild(guild.id);
                                                source.sendSuccess(
                                                   () -> Component.literal("§c§lGuild §e" + guildName + " §c§lwas disbanded for §f" + targetName + "§c§l."),
                                                   false
                                                );
                                             } else {
                                                guild.memberPermissions.removeIf(permission -> permission.playerUUID.equals(target.getUUID()));
                                                data.markDirty();
                                                source.sendSuccess(
                                                   () -> Component.literal("§aRemoved §f" + targetName + " §afrom §e" + guildName + "§a."), false
                                                );
                                             }

                                             changed++;
                                          }
                                       }

                                       return changed;
                                    })))
                                 .then(
                                    Commands.literal("remove")
                                       .then(
                                          Commands.argument("target", EntityArgument.player())
                                             .executes(
                                                arguments -> {
                                                   ServerLevel sl = ((CommandSourceStack)arguments.getSource()).getLevel();
                                                   ServerPlayer targetPlayer = EntityArgument.getPlayer(arguments, "target");

                                                   for (ServerPlayer owner : EntityArgument.getPlayers(arguments, "name")) {
                                                      GuildData guild = GuildSavedData.get(sl).getGuildForPlayer(owner.getUUID());
                                                      if (guild == null) {
                                                         ServerPlayer of = owner;
                                                         ((CommandSourceStack)arguments.getSource())
                                                            .sendFailure(Component.literal("§c" + of.getName().getString() + " is not in any guild."));
                                                      } else if (targetPlayer.getUUID().equals(guild.ownerUUID)) {
                                                         ((CommandSourceStack)arguments.getSource())
                                                            .sendFailure(Component.literal("§cCannot remove the guild owner."));
                                                      } else {
                                                         ServerPlayer tf = targetPlayer;
                                                         boolean removed = guild.memberPermissions.removeIf(p -> p.playerUUID.equals(tf.getUUID()));
                                                         if (removed) {
                                                            GuildSavedData.get(sl).markDirty();
                                                            GuildData gf = guild;
                                                            ((CommandSourceStack)arguments.getSource())
                                                               .sendSuccess(
                                                                  () -> Component.literal("§aRemoved §f" + tf.getName().getString() + " §afrom §e" + gf.name),
                                                                  false
                                                               );
                                                         } else {
                                                            ((CommandSourceStack)arguments.getSource())
                                                               .sendFailure(
                                                                  Component.literal("§c" + tf.getName().getString() + " is not a member of that guild.")
                                                               );
                                                         }
                                                      }
                                                   }

                                                   return 0;
                                                }
                                             )
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("structure")
                              .then(
                                 Commands.argument("dungeon", StringArgumentType.word())
                                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(structureSuggestions(), builder))
                                    .executes(
                                       arguments -> {
                                          String dungeonName = StringArgumentType.getString(arguments, "dungeon");
                                          String normalizedDungeon = normalizeDungeonName(dungeonName);
                                          if (FrostMonarchCastleGenerator.handles(normalizedDungeon)) {
                                             int started = 0;

                                             for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
                                                if (FrostMonarchCastleGenerator.start(target)) {
                                                   started++;
                                                }
                                             }

                                             int startedBuilds = started;
                                             ((CommandSourceStack)arguments.getSource())
                                                .sendSuccess(() -> Component.literal("Queued " + startedBuilds + " Boreal Crown site survey(s)."), false);
                                             return started > 0 ? 1 : 0;
                                          } else if (DkcStructurePreviewBuilder.handles(normalizedDungeon)) {
                                             int started = 0;

                                             for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
                                                if (DkcStructurePreviewBuilder.start(target, normalizedDungeon)) {
                                                   started++;
                                                }
                                             }

                                             int startedBuilds = started;
                                             ((CommandSourceStack)arguments.getSource())
                                                .sendSuccess(
                                                   () -> Component.literal("Started " + startedBuilds + " connected DKC structure preview(s)."), false
                                                );
                                             return started > 0 ? 1 : 0;
                                          } else if (isCartenonTempleName(normalizedDungeon)) {
                                             int started = 0;

                                             for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
                                                if (CartenonTempleGenerator.start(target)) {
                                                   started++;
                                                }
                                             }

                                             int startedBuilds = started;
                                             ((CommandSourceStack)arguments.getSource())
                                                .sendSuccess(() -> Component.literal("Started " + startedBuilds + " Cartenon Temple build(s)."), false);
                                             return started > 0 ? 1 : 0;
                                          } else {
                                             List<String> structures = structuresForDungeon(dungeonName);
                                             if (structures != null && !structures.isEmpty()) {
                                                int totalPlaced = 0;

                                                for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
                                                   totalPlaced += placeStructureGallery(
                                                      target.serverLevel(), target.blockPosition().offset(8, 0, 8), structures
                                                   );
                                                }

                                                int placed = totalPlaced;
                                                String normalizedName = normalizeDungeonName(dungeonName);
                                                ((CommandSourceStack)arguments.getSource())
                                                   .sendSuccess(
                                                      () -> Component.literal("§aPlaced §e" + placed + " §a" + normalizedName + " structure option(s)."), false
                                                   );
                                                return placed > 0 ? 1 : 0;
                                             } else {
                                                ((CommandSourceStack)arguments.getSource())
                                                   .sendFailure(
                                                      Component.literal(
                                                         "§cUnknown dungeon structure set: §e"
                                                            + dungeonName
                                                            + "§c. Try: §7"
                                                            + String.join(", ", structureSuggestions())
                                                      )
                                                   );
                                                return 0;
                                             }
                                          }
                                       }
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("generate")
                           .then(
                              Commands.literal("dungeon")
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("complexity", IntegerArgumentType.integer(1, 10))
                                          .executes(
                                             arguments -> forEachTarget(
                                                arguments,
                                                target -> {
                                                   DungeonTheme theme = DungeonTheme.random();
                                                   String result = DungeonGenerator.generate(
                                                      target.serverLevel(),
                                                      target.blockPosition(),
                                                      IntegerArgumentType.getInteger(arguments, "complexity"),
                                                      theme
                                                   );
                                                   ((CommandSourceStack)arguments.getSource())
                                                      .sendSuccess(() -> Component.literal(target.getName().getString() + ": " + result), false);
                                                }
                                             )
                                          ))
                                       .then(
                                          Commands.argument("theme", StringArgumentType.word())
                                             .executes(
                                                arguments -> {
                                                   DungeonTheme theme = DungeonTheme.fromString(StringArgumentType.getString(arguments, "theme"));
                                                   return forEachTarget(
                                                      arguments,
                                                      target -> {
                                                         String result = DungeonGenerator.generate(
                                                            target.serverLevel(),
                                                            target.blockPosition(),
                                                            IntegerArgumentType.getInteger(arguments, "complexity"),
                                                            theme
                                                         );
                                                         ((CommandSourceStack)arguments.getSource())
                                                            .sendSuccess(() -> Component.literal(target.getName().getString() + ": " + result), false);
                                                      }
                                                   );
                                                }
                                             )
                                       )
                                 )
                           )
                     )
               )
         );
   }

   private static boolean isDeveloperSource(CommandSourceStack source) {
      return source.getEntity() instanceof ServerPlayer player && DeveloperModeManager.isEnabled(player);
   }

   private static int spawnSillad(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)arguments.getSource()).getPlayerOrException();
      SilladBossEntity sillad = SilladBossSpawnManager.spawnForDeveloper(player);
      if (sillad == null) {
         ((CommandSourceStack)arguments.getSource()).sendFailure(Component.literal("Unable to spawn Sillad at a safe nearby position."));
         return 0;
      } else {
         ((CommandSourceStack)arguments.getSource())
            .sendSuccess(() -> Component.literal("Spawned Sillad for developer testing.").withStyle(ChatFormatting.AQUA), false);
         return 1;
      }
   }

   private static int showRecoveryHelp(CommandContext<CommandSourceStack> arguments) {
      ((CommandSourceStack)arguments.getSource()).sendSuccess(() -> Component.translatable("commands.slr.recovery.help").withStyle(ChatFormatting.AQUA), false);
      return 1;
   }

   private static int escapeDungeon(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)arguments.getSource()).getPlayerOrException();
      if (recoveryOnCooldown(player, (CommandSourceStack)arguments.getSource())) {
         return 0;
      } else if (DkcFloorRegistry.isDkc(player.level())) {
         markRecoveryUsed(player);
         DKCPathTeleportProcedure.returnToSavedOverworld(player);
         ((CommandSourceStack)arguments.getSource())
            .sendSuccess(() -> Component.translatable("commands.slr.recovery.dkc_left").withStyle(ChatFormatting.LIGHT_PURPLE), false);
         return 1;
      } else if (!DungeonDimensionPlayerLeavesDimensionProcedure.emergencyExit(player)) {
         ((CommandSourceStack)arguments.getSource()).sendFailure(Component.translatable("commands.slr.recovery.not_in_dungeon"));
         return 0;
      } else {
         markRecoveryUsed(player);
         ((CommandSourceStack)arguments.getSource())
            .sendSuccess(() -> Component.translatable("commands.slr.recovery.escaped").withStyle(ChatFormatting.YELLOW), false);
         return 1;
      }
   }

   private static int recoverFromStuck(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)arguments.getSource()).getPlayerOrException();
      return DkcFloorRegistry.isDkc(player.level()) ? unstuckDkc(arguments) : escapeDungeon(arguments);
   }

   private static int unstuckDkc(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)arguments.getSource()).getPlayerOrException();
      if (!DkcFloorRegistry.isDkc(player.level())) {
         ((CommandSourceStack)arguments.getSource()).sendFailure(Component.translatable("commands.slr.recovery.not_in_dkc"));
         return 0;
      }

      if (recoveryOnCooldown(player, (CommandSourceStack)arguments.getSource())) {
         return 0;
      }

      int floor = DkcSpatialLayout.floor(player);
      if (floor <= 0) {
         floor = (int)player.getPersistentData().getDouble("dkc_current_floor");
      }

      if (floor >= 1 && floor <= 20) {
         markRecoveryUsed(player);
         DkcFloorBuilder.teleportToFloor(player, floor);
         int recoveredFloor = floor;
         ((CommandSourceStack)arguments.getSource())
            .sendSuccess(() -> Component.translatable("commands.slr.recovery.dkc_unstuck", recoveredFloor).withStyle(ChatFormatting.LIGHT_PURPLE), false);
         return 1;
      } else {
         ((CommandSourceStack)arguments.getSource()).sendFailure(Component.translatable("commands.slr.recovery.dkc_floor_unknown"));
         return 0;
      }
   }

   private static int showDkcStatus(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)arguments.getSource()).getPlayerOrException();
      if (!DkcFloorRegistry.isDkc(player.level())) {
         ((CommandSourceStack)arguments.getSource()).sendFailure(Component.translatable("commands.slr.recovery.not_in_dkc"));
         return 0;
      } else {
         int spatialFloor = DkcSpatialLayout.floor(player);
         int storedFloor = (int)player.getPersistentData().getDouble("dkc_current_floor");
         ((CommandSourceStack)arguments.getSource())
            .sendSuccess(
               () -> Component.translatable(
                     "commands.slr.recovery.dkc_status", spatialFloor > 0 ? spatialFloor : storedFloor, spatialFloor > 0 ? "inside" : "outside"
                  )
                  .withStyle(ChatFormatting.AQUA),
               false
            );
         return 1;
      }
   }

   private static boolean recoveryOnCooldown(ServerPlayer player, CommandSourceStack source) {
      long remaining = player.getPersistentData().getLong("slr_recovery_command_cooldown") - player.serverLevel().getGameTime();
      if (remaining <= 0L) {
         return false;
      }

      long seconds = Math.max(1L, (remaining + 19L) / 20L);
      source.sendFailure(Component.translatable("commands.slr.recovery.cooldown", seconds));
      return true;
   }

   private static void markRecoveryUsed(ServerPlayer player) {
      player.getPersistentData().putLong("slr_recovery_command_cooldown", player.serverLevel().getGameTime() + 200L);
   }

   private static int forEachTarget(CommandContext<CommandSourceStack> arguments, Consumer<ServerPlayer> action) {
      try {
         Collection<ServerPlayer> targets = EntityArgument.getPlayers(arguments, "name");

         for (ServerPlayer target : targets) {
            action.accept(target);
         }

         return targets.size();
      } catch (CommandSyntaxException exception) {
         ((CommandSourceStack)arguments.getSource()).sendFailure(Component.literal("Unable to resolve target players"));
         return 0;
      }
   }

   private static int setDkcFloor(CommandContext<CommandSourceStack> arguments) throws CommandSyntaxException {
      int floor = IntegerArgumentType.getInteger(arguments, "floor");
      int changed = 0;

      for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
         if (DKCLevelItemRightclickedProcedure.setCurrentFloor(target, floor)) {
            changed++;
         }
      }

      int changedPlayers = changed;
      ((CommandSourceStack)arguments.getSource())
         .sendSuccess(() -> Component.literal("Queued fresh DKC Floor " + floor + " state for " + changedPlayers + " player(s)."), true);
      return changed;
   }

   private static int modifyShadowSoldiers(CommandContext<CommandSourceStack> arguments, int amount) throws CommandSyntaxException {
      String shadow = StringArgumentType.getString(arguments, "shadow");
      int changed = 0;

      for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
         if (amount > 0 && !ShadowMonarchManager.isShadowAvailableFor(target, shadow)) {
            ((CommandSourceStack)arguments.getSource())
               .sendFailure(Component.literal("Iron is available only while the target player's developer preview is enabled.").withStyle(ChatFormatting.RED));
         } else if (ShadowMonarchManager.modifyShadowAmount(target, shadow, amount)) {
            changed++;
            ServerPlayer tf = target;
            String action = amount > 0 ? "added to" : "removed from";
            ((CommandSourceStack)arguments.getSource())
               .sendSuccess(() -> Component.literal("§aShadow §e" + shadow + " §a" + action + " §f" + tf.getName().getString() + "§a."), false);
         } else {
            ServerPlayer tf = target;
            ((CommandSourceStack)arguments.getSource())
               .sendFailure(Component.literal("§cUnknown shadow type §e" + shadow + " §cfor §f" + tf.getName().getString() + "§c."));
         }
      }

      return changed;
   }

   private static int modifyShadowLevels(CommandContext<CommandSourceStack> arguments, boolean additive) throws CommandSyntaxException {
      String shadow = StringArgumentType.getString(arguments, "shadow");
      int value = IntegerArgumentType.getInteger(arguments, additive ? "levels" : "level");
      int changedPlayers = 0;

      for (ServerPlayer target : EntityArgument.getPlayers(arguments, "name")) {
         ShadowMonarchManager.ShadowLevelCommandResult result = ShadowMonarchManager.modifyShadowLevels(target, shadow, value, additive);
         if (!result.knownTarget()) {
            ((CommandSourceStack)arguments.getSource()).sendFailure(Component.literal("Unknown shadow type: " + shadow).withStyle(ChatFormatting.RED));
         } else if (result.changed() <= 0) {
            ((CommandSourceStack)arguments.getSource())
               .sendFailure(Component.literal(target.getName().getString() + " owns no matching shadows.").withStyle(ChatFormatting.RED));
         } else {
            changedPlayers++;
            String levelText = result.lowestLevel() == result.highestLevel()
               ? "Lv." + result.highestLevel()
               : "Lv." + result.lowestLevel() + "-" + result.highestLevel();
            ((CommandSourceStack)arguments.getSource())
               .sendSuccess(
                  () -> Component.literal(
                        (additive ? "Added " + value + " level(s) to " : "Set the level of ")
                           + result.changed()
                           + " "
                           + shadow
                           + " shadow(s) for "
                           + target.getName().getString()
                           + " ("
                           + levelText
                           + ")."
                     )
                     .withStyle(ChatFormatting.GREEN),
                  true
               );
         }
      }

      return changedPlayers;
   }

   private static List<String> structuresForDungeon(String dungeonName) {
      return STRUCTURE_SETS.get(normalizeDungeonName(dungeonName));
   }

   private static List<String> structureSuggestions() {
      List<String> suggestions = new ArrayList<>(STRUCTURE_SETS.keySet());
      suggestions.add("cartenon");
      suggestions.addAll(DkcStructurePreviewBuilder.suggestions());
      suggestions.addAll(FrostMonarchCastleGenerator.suggestions());
      suggestions.sort(String::compareTo);
      return suggestions;
   }

   private static boolean isCartenonTempleName(String normalizedName) {
      return normalizedName.equals("cartenon")
         || normalizedName.equals("cartenontemple")
         || normalizedName.equals("doubletemple")
         || normalizedName.equals("doubleungeon");
   }

   private static String normalizeDungeonName(String dungeonName) {
      return dungeonName.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
   }

   private static int placeStructureGallery(ServerLevel level, BlockPos origin, List<String> structureNames) {
      int placed = 0;
      int xOffset = 0;

      for (String structureName : structureNames) {
         StructureTemplate template = level.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", structureName));
         if (template != null && template.getSize().getX() > 0 && template.getSize().getZ() > 0) {
            BlockPos placeAt = origin.offset(xOffset, 0, 0);
            template.placeInWorld(
               level,
               placeAt,
               placeAt,
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               level.random,
               2
            );
            xOffset += Math.max(8, template.getSize().getX()) + 6;
            placed++;
         }
      }

      return placed;
   }
}
