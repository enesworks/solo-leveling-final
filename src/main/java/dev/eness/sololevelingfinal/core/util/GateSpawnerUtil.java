package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import dev.eness.sololevelingfinal.core.dungeon.DatapackDungeonGateHandler;
import dev.eness.sololevelingfinal.core.dungeon.DungeonTheme;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataManager;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataSnapshot;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDefinition;
import dev.eness.sololevelingfinal.core.entity.DatapackGateEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class GateSpawnerUtil {
   private GateSpawnerUtil() {
   }

   public static void spawnNearRandomOverworldPlayer(LevelAccessor world) {
      if (!DungeonBuilderMode.isActive(world) && world instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
         List<Entity> players = new ArrayList<>();

         for (Entity player : new ArrayList<>(world.players())) {
            if (player.level().dimension() == Level.OVERWORLD) {
               players.add(player);
            }
         }

         if (!players.isEmpty()) {
            RandomSource random = RandomSource.create();
            Entity target = players.get(Mth.nextInt(random, 0, players.size() - 1));
            double baseX = truncate(target.getX());
            double baseZ = truncate(target.getZ());
            double randX = Mth.nextInt(random, -200, 200);
            double randZ = Mth.nextInt(random, -200, 200);
            int spawnX = (int)(baseX + randX);
            int spawnZ = (int)(baseZ + randZ);
            int spawnY = world.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);
            if (spawnY < -60) {
               delayNextGate(world);
            } else {
               BlockPos pos = BlockPos.containing(baseX + randX, spawnY, baseZ + randZ);
               SololevelingModVariables.MapVariables mapVars = SololevelingModVariables.MapVariables.get(world);
               if (!mapVars.firstNaturalGateSpawned) {
                  Entity firstGate = spawn(serverLevel, SololevelingModEntities.PORTAL_SEWERS.get(), pos);
                  if (firstGate != null) {
                     mapVars.firstNaturalGateSpawned = true;
                     mapVars.syncData(world);
                  } else {
                     delayNextGate(world);
                  }
               } else {
                  spawnEligibleNaturalGate(serverLevel, target, pos, random);
               }
            }
         }
      }
   }

   private static double truncate(double value) {
      return (int)(Math.pow(10.0, 2.0) * value) / (float)Math.pow(10.0, 2.0);
   }

   private static Entity spawn(ServerLevel level, EntityType<?> type, BlockPos pos) {
      return type.spawn(level, pos, MobSpawnType.MOB_SUMMONED);
   }

   private static void spawnEligibleNaturalGate(ServerLevel level, Entity target, BlockPos pos, RandomSource random) {
      boolean ranked = level.getGameRules().getBoolean(SololevelingModGameRules.SOLO_LEVELING_RANKED_GATES);
      ProceduralDungeonRank unlockedRank = rankFor(target);
      List<ResourceLocation> datapackDungeons = eligibleDatapackDungeons(ranked, unlockedRank);
      List<GateSpawnerUtil.NaturalGateChoice> choices = new ArrayList<>();
      addChoice(choices, GateSpawnerUtil.NaturalGateChoice.procedural(270, ProceduralDungeonRank.E), ranked, unlockedRank);
      if (!datapackDungeons.isEmpty()) {
         choices.add(GateSpawnerUtil.NaturalGateChoice.datapack(100));
      }

      addChoice(
         choices, GateSpawnerUtil.NaturalGateChoice.entity(137, ProceduralDungeonRank.C, SololevelingModEntities.RANDOM_CAVE_LARGE.get()), ranked, unlockedRank
      );
      addChoice(
         choices, GateSpawnerUtil.NaturalGateChoice.entity(114, ProceduralDungeonRank.B, SololevelingModEntities.PORTAL_LUSH.get()), ranked, unlockedRank
      );
      addChoice(
         choices,
         GateSpawnerUtil.NaturalGateChoice.entity(95, ProceduralDungeonRank.C, SololevelingModEntities.PORTAL_ANCIENT_GOLEM.get()),
         ranked,
         unlockedRank
      );
      addChoice(choices, GateSpawnerUtil.NaturalGateChoice.entity(79, ProceduralDungeonRank.A, SololevelingModEntities.PORTAL_LAB.get()), ranked, unlockedRank);
      addChoice(
         choices, GateSpawnerUtil.NaturalGateChoice.entity(66, ProceduralDungeonRank.B, SololevelingModEntities.PORTAL_CEMETERY.get()), ranked, unlockedRank
      );
      addChoice(choices, GateSpawnerUtil.NaturalGateChoice.red(33, ProceduralDungeonRank.B), ranked, unlockedRank);
      addChoice(
         choices,
         GateSpawnerUtil.NaturalGateChoice.entity(59, ProceduralDungeonRank.A, SololevelingModEntities.PORTAL_KARGALGANS_THRONE_ROOM.get()),
         ranked,
         unlockedRank
      );
      addChoice(choices, GateSpawnerUtil.NaturalGateChoice.entity(47, ProceduralDungeonRank.S, SololevelingModEntities.PORTAL_BERU.get()), ranked, unlockedRank);
      addChoice(
         choices, GateSpawnerUtil.NaturalGateChoice.entity(75, ProceduralDungeonRank.D, SololevelingModEntities.PORTAL_SEWERS.get()), ranked, unlockedRank
      );
      int totalWeight = choices.stream().mapToInt(GateSpawnerUtil.NaturalGateChoice::weight).sum();
      if (totalWeight <= 0) {
         delayNextGate(level);
      } else {
         int roll = random.nextInt(totalWeight);
         GateSpawnerUtil.NaturalGateChoice selected = choices.get(choices.size() - 1);

         for (GateSpawnerUtil.NaturalGateChoice choice : choices) {
            roll -= choice.weight();
            if (roll < 0) {
               selected = choice;
               break;
            }
         }

         if (selected.datapack()) {
            spawnDatapackGate(level, pos, random, ranked, unlockedRank, datapackDungeons);
         } else if (selected.procedural()) {
            spawnProceduralGate(level, target, pos, random, ranked);
         } else if (selected.red()) {
            if (!SololevelingModVariables.MapVariables.get(level).RedGate) {
               spawn(level, SololevelingModEntities.RED_GATE.get(), pos);
            } else {
               delayNextGate(level);
            }
         } else {
            spawn(level, selected.type(), pos);
         }
      }
   }

   private static void addChoice(
      List<GateSpawnerUtil.NaturalGateChoice> choices, GateSpawnerUtil.NaturalGateChoice choice, boolean ranked, ProceduralDungeonRank unlockedRank
   ) {
      if (!ranked || unlockedRank.numericRank >= choice.minimumRank().numericRank) {
         choices.add(choice);
      }
   }

   private static List<ResourceLocation> eligibleDatapackDungeons(boolean ranked, ProceduralDungeonRank unlockedRank) {
      DungeonDataSnapshot snapshot = DungeonDataManager.snapshot();
      return snapshot.dungeonIds()
         .stream()
         .filter(id -> snapshot.dungeon(id).map(definition -> !ranked || definition.supportsRank(unlockedRank)).orElse(false))
         .toList();
   }

   private static void spawnDatapackGate(
      ServerLevel level, BlockPos pos, RandomSource random, boolean ranked, ProceduralDungeonRank unlockedRank, List<ResourceLocation> eligibleDungeons
   ) {
      if (eligibleDungeons.isEmpty()) {
         delayNextGate(level);
      } else {
         ResourceLocation dungeonId = eligibleDungeons.get(Mth.nextInt(random, 0, eligibleDungeons.size() - 1));
         DungeonDefinition definition = DungeonDataManager.dungeon(dungeonId).orElse(null);
         if (definition == null) {
            delayNextGate(level);
         } else {
            ProceduralDungeonRank rank;
            if (ranked) {
               if (!definition.supportsRank(unlockedRank)) {
                  delayNextGate(level);
                  return;
               }

               rank = unlockedRank;
            } else {
               List<ProceduralDungeonRank> allowedRanks = Arrays.stream(ProceduralDungeonRank.values()).filter(definition.allowedRanks()::contains).toList();
               if (allowedRanks.isEmpty()) {
                  delayNextGate(level);
                  return;
               }

               rank = randomOpenRank(random, allowedRanks);
            }

            DatapackGateEntity gate = SololevelingModEntities.DATAPACK_GATE.get().spawn(level, pos, MobSpawnType.MOB_SUMMONED);
            if (gate == null) {
               delayNextGate(level);
            } else {
               if (!DatapackDungeonGateHandler.bind(gate, dungeonId, rank)) {
                  gate.discard();
                  delayNextGate(level);
               }
            }
         }
      }
   }

   private static void spawnProceduralGate(ServerLevel level, Entity target, BlockPos pos, RandomSource random, boolean ranked) {
      Entity gate = spawn(level, SololevelingModEntities.PORTAL_1.get(), pos);
      if (gate != null) {
         ProceduralDungeonRank rank = ranked ? randomRankAtOrBelow(random, rankFor(target)) : randomOpenRank(random, List.of(ProceduralDungeonRank.values()));
         DungeonTheme theme = randomTheme(random);
         gate.getPersistentData().putBoolean("slr_procedural_gate", true);
         gate.getPersistentData().putBoolean("slr_procedural_red_gate", false);
         gate.getPersistentData().putString("slr_procedural_rank", rank.name());
         gate.getPersistentData().putString("slr_procedural_theme", theme.name());
         gate.getPersistentData().putInt("slr_procedural_complexity", complexityFor(rank, random));
      }
   }

   private static ProceduralDungeonRank randomRankAtOrBelow(RandomSource random, ProceduralDungeonRank maximum) {
      List<ProceduralDungeonRank> eligible = Arrays.stream(ProceduralDungeonRank.values()).filter(rankx -> rankx.numericRank <= maximum.numericRank).toList();
      int totalWeight = eligible.stream().mapToInt(rankx -> {
         return switch (maximum.numericRank - rankx.numericRank) {
            case 0 -> 40;
            case 1 -> 28;
            case 2 -> 17;
            default -> 9;
         };
      }).sum();
      int roll = random.nextInt(totalWeight);

      for (ProceduralDungeonRank rank : eligible) {
         roll -= switch (maximum.numericRank - rank.numericRank) {
            case 0 -> 40;
            case 1 -> 28;
            case 2 -> 17;
            default -> 9;
         };
         if (roll < 0) {
            return rank;
         }
      }

      return maximum;
   }

   private static ProceduralDungeonRank randomOpenRank(RandomSource random, List<ProceduralDungeonRank> allowed) {
      int totalWeight = allowed.stream().mapToInt(GateSpawnerUtil::openRankWeight).sum();
      int roll = random.nextInt(Math.max(1, totalWeight));

      for (ProceduralDungeonRank rank : allowed) {
         roll -= openRankWeight(rank);
         if (roll < 0) {
            return rank;
         }
      }

      return allowed.get(allowed.size() - 1);
   }

   private static int openRankWeight(ProceduralDungeonRank rank) {
      return switch (rank) {
         case E -> 4;
         case D -> 7;
         case C -> 13;
         case B -> 22;
         case A -> 28;
         case S -> 26;
      };
   }

   private static ProceduralDungeonRank rankFor(Entity target) {
      SololevelingModVariables.PlayerVariables vars = target.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      int certifiedRank = Mth.clamp((int)Math.floor(vars.HunterRank), 1, 6);
      int levelFloor = HunterEvaluationRules.rankFloorForLevel(Math.max(0, (int)Math.floor(vars.Level)));
      int progressionRank = VesselManager.currentDefinition(target) != null ? 6 : Math.max(certifiedRank, levelFloor);
      return ProceduralDungeonRank.values()[progressionRank - 1];
   }

   private static DungeonTheme randomTheme(RandomSource random) {
      DungeonTheme[] themes = DungeonTheme.values();
      return themes[Mth.nextInt(random, 0, themes.length - 1)];
   }

   private static int complexityFor(ProceduralDungeonRank rank, RandomSource random) {
      return switch (rank) {
         case E -> Mth.nextInt(random, 2, 6);
         case D -> Mth.nextInt(random, 3, 7);
         case C -> Mth.nextInt(random, 3, 6);
         case B -> Mth.nextInt(random, 5, 7);
         case A -> Mth.nextInt(random, 7, 9);
         case S -> Mth.nextInt(random, 8, 10);
      };
   }

   private static void delayNextGate(LevelAccessor world) {
      SololevelingModVariables.MapVariables.get(world).gatetimer = world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_GATE_DELAY) - 1;
      SololevelingModVariables.MapVariables.get(world).syncData(world);
   }

   private record NaturalGateChoice(int weight, ProceduralDungeonRank minimumRank, EntityType<?> type, boolean procedural, boolean red, boolean datapack) {
      private static GateSpawnerUtil.NaturalGateChoice procedural(int weight, ProceduralDungeonRank minimumRank) {
         return new GateSpawnerUtil.NaturalGateChoice(weight, minimumRank, null, true, false, false);
      }

      private static GateSpawnerUtil.NaturalGateChoice datapack(int weight) {
         return new GateSpawnerUtil.NaturalGateChoice(weight, ProceduralDungeonRank.E, null, false, false, true);
      }

      private static GateSpawnerUtil.NaturalGateChoice red(int weight, ProceduralDungeonRank minimumRank) {
         return new GateSpawnerUtil.NaturalGateChoice(weight, minimumRank, null, false, true, false);
      }

      private static GateSpawnerUtil.NaturalGateChoice entity(int weight, ProceduralDungeonRank minimumRank, EntityType<?> type) {
         return new GateSpawnerUtil.NaturalGateChoice(weight, minimumRank, type, false, false, false);
      }
   }
}
