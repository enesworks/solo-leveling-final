package dev.eness.sololevelingfinal.core.procedures;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.entity.Portal1Entity;
import dev.eness.sololevelingfinal.core.entity.PortalAncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.PortalBeruEntity;
import dev.eness.sololevelingfinal.core.entity.PortalCemeteryEntity;
import dev.eness.sololevelingfinal.core.entity.PortalKargalgansThroneRoomEntity;
import dev.eness.sololevelingfinal.core.entity.PortalLabEntity;
import dev.eness.sololevelingfinal.core.entity.PortalLushEntity;
import dev.eness.sololevelingfinal.core.entity.PortalSewersEntity;
import dev.eness.sololevelingfinal.core.entity.RandomCaveLargeEntity;
import dev.eness.sololevelingfinal.core.entity.RedGateEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.StoryModeIntroSavedData;

public class GateBreakChanceProcedure {
   private static final int MAX_MEMORY_STACKS = 4;
   private static final double MEMORY_PENALTY_PER_STACK = 0.15;

   private GateBreakChanceProcedure() {
   }

   public static boolean shouldBreak(LevelAccessor world, Entity gate) {
      if (!(world instanceof ServerLevel serverLevel && gate != null)) {
         return true;
      } else {
         if (serverLevel.getServer().overworld().getGameRules().getBoolean(SololevelingModGameRules.SOLO_LEVELING_STORY_MODE)
            && StoryModeIntroSavedData.get(serverLevel).hasDungeonBreakGrace()) {
            return false;
         }

         int gateRank = gateRank(gate);
         int maxPlayerRank = maxPlayerRank(serverLevel);
         if (gateRank <= maxPlayerRank) {
            return true;
         }

         String key = gateKey(gate);
         SololevelingModVariables.MapVariables mapVars = SololevelingModVariables.MapVariables.get(world);
         Map<String, Integer> memory = readMemory(mapVars.GateBreakMemory);
         int stacks = memory.getOrDefault(key, 0);
         double chance = Math.max(0.05, baseBreakChance(gateRank - maxPlayerRank) - stacks * 0.15);
         boolean breaks = serverLevel.random.nextDouble() < chance;
         if (breaks) {
            memory.put(key, Math.min(4, stacks + 1));
         } else if (stacks > 0) {
            memory.put(key, stacks - 1);
         }

         mapVars.GateBreakMemory = writeMemory(memory);
         mapVars.syncData(world);
         return breaks;
      }
   }

   private static int maxPlayerRank(ServerLevel level) {
      int maxRank = 0;

      for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         maxRank = Math.max(maxRank, (int)vars.HunterRank);
      }

      return maxRank;
   }

   private static double baseBreakChance(int rankGap) {
      if (rankGap <= 1) {
         return 0.65;
      } else if (rankGap == 2) {
         return 0.45;
      } else {
         return rankGap == 3 ? 0.3 : 0.18;
      }
   }

   private static int gateRank(Entity gate) {
      if (gate instanceof Portal1Entity) {
         return proceduralRank(gate).numericRank;
      } else if (gate instanceof PortalSewersEntity) {
         return ProceduralDungeonRank.D.numericRank;
      } else if (gate instanceof RandomCaveLargeEntity || gate instanceof PortalAncientGolemEntity) {
         return ProceduralDungeonRank.C.numericRank;
      } else if (gate instanceof PortalLushEntity || gate instanceof PortalCemeteryEntity || gate instanceof RedGateEntity) {
         return ProceduralDungeonRank.B.numericRank;
      } else if (gate instanceof PortalLabEntity || gate instanceof PortalKargalgansThroneRoomEntity) {
         return ProceduralDungeonRank.A.numericRank;
      } else {
         return gate instanceof PortalBeruEntity ? ProceduralDungeonRank.S.numericRank : ProceduralDungeonRank.E.numericRank;
      }
   }

   private static String gateKey(Entity gate) {
      if (gate instanceof Portal1Entity) {
         return "procedural_" + proceduralRank(gate).name().toLowerCase();
      } else if (gate instanceof PortalSewersEntity) {
         return "sewers";
      } else if (gate instanceof RandomCaveLargeEntity) {
         return "random_cave_large";
      } else if (gate instanceof PortalAncientGolemEntity) {
         return "ancient_golem";
      } else if (gate instanceof PortalLushEntity) {
         return "lush";
      } else if (gate instanceof PortalCemeteryEntity) {
         return "cemetery";
      } else if (gate instanceof RedGateEntity) {
         return "red_gate";
      } else if (gate instanceof PortalLabEntity) {
         return "lab";
      } else if (gate instanceof PortalKargalgansThroneRoomEntity) {
         return "kargalgan";
      } else {
         return gate instanceof PortalBeruEntity ? "beru" : gate.getType().toString().replace(';', '_').replace('=', '_');
      }
   }

   private static ProceduralDungeonRank proceduralRank(Entity gate) {
      return ProceduralDungeonRank.fromString(gate.getPersistentData().getString("slr_procedural_rank"));
   }

   private static Map<String, Integer> readMemory(String saved) {
      Map<String, Integer> memory = new LinkedHashMap<>();
      if (saved != null && !saved.isBlank()) {
         for (String entry : saved.split(";")) {
            int separator = entry.indexOf(61);
            if (separator > 0 && separator < entry.length() - 1) {
               try {
                  int stacks = Integer.parseInt(entry.substring(separator + 1));
                  if (stacks > 0) {
                     memory.put(entry.substring(0, separator), Math.min(4, stacks));
                  }
               } catch (NumberFormatException var8) {
               }
            }
         }

         return memory;
      } else {
         return memory;
      }
   }

   private static String writeMemory(Map<String, Integer> memory) {
      StringBuilder saved = new StringBuilder();

      for (Entry<String, Integer> entry : memory.entrySet()) {
         int stacks = Math.min(4, Math.max(0, entry.getValue()));
         if (stacks > 0) {
            if (saved.length() > 0) {
               saved.append(';');
            }

            saved.append(entry.getKey()).append('=').append(stacks);
         }
      }

      return saved.toString();
   }
}
