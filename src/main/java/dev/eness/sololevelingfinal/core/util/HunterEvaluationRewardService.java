package dev.eness.sololevelingfinal.core.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.item.HunterIDItem;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.PowerAppendAssassinProcedure;
import dev.eness.sololevelingfinal.core.procedures.PowerAppendFighterProcedure;
import dev.eness.sololevelingfinal.core.procedures.PowerAppendHealerProcedure;
import dev.eness.sololevelingfinal.core.procedures.PowerAppendMageProcedure;
import dev.eness.sololevelingfinal.core.procedures.PowerAppendRangerProcedure;
import dev.eness.sololevelingfinal.core.procedures.PowerAppendTankerProcedure;

public final class HunterEvaluationRewardService {
   public static final String REWARDS_APPLIED = "RewardsApplied";

   private HunterEvaluationRewardService() {
   }

   public static boolean applyInitialRewards(ServerPlayer player, int classId, int rank, CompoundTag evaluationData) {
      if (player != null && !evaluationData.getBoolean("RewardsApplied")) {
         evaluationData.putBoolean("RewardsApplied", true);
         applyStats(player, classId, rank);
         giveStarterPack(player, classId);
         grantClassPowers(player, classId);
         ItemHandlerHelper.giveItemToPlayer(player, HunterIDItem.createBoundCard(player));
         return true;
      } else {
         return false;
      }
   }

   public static void markLegacyRewardsApplied(CompoundTag evaluationData) {
      evaluationData.putBoolean("RewardsApplied", true);
   }

   private static void applyStats(ServerPlayer player, int classId, int rank) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         double lore = capability.LoreAccurateRankStart;
         double curve = Math.pow(rank, lore);
         switch (classId) {
            case 1:
               capability.Speed += curve * 5.0;
               capability.Strength += curve * 2.0;
               capability.Intelligence += curve * 3.0;
               capability.Vitality += curve;
               capability.perception += curve * 4.0;
               break;
            case 2:
               capability.Speed += curve;
               capability.Vitality += curve;
               capability.Intelligence = capability.Intelligence + AwakeningStatCurves.intelligenceBonus(2, rank);
               capability.perception += curve * 2.0;
               capability.Strength += rank * lore * 2.0;
               break;
            case 3:
               capability.Vitality += curve * 2.0;
               capability.Strength += curve * 4.0;
               capability.perception += curve;
               capability.Intelligence += curve;
               capability.Speed += curve;
               break;
            case 4:
               capability.Vitality += curve * 4.0;
               capability.Strength += curve * 2.0;
               capability.Intelligence += rank * lore;
               capability.Speed += curve;
               capability.perception += rank * lore * 2.0;
               break;
            case 5:
               capability.Intelligence = capability.Intelligence + AwakeningStatCurves.intelligenceBonus(5, rank);
               capability.Vitality += curve * 5.0;
               capability.perception += curve * 3.0;
               capability.Speed += curve * 2.0;
               capability.Strength += curve * 2.0;
               break;
            case 6:
               capability.Intelligence = capability.Intelligence + AwakeningStatCurves.intelligenceBonus(6, rank);
               capability.Vitality += curve * 4.0;
               capability.perception += curve * 4.0;
               capability.Speed += curve * 3.0;
               capability.Strength += curve;
               break;
            default:
               return;
         }

         if (rank >= 1 && rank <= 4) {
            capability.Strength = Math.ceil(capability.Strength * 1.15);
            capability.Speed = Math.ceil(capability.Speed * 1.15);
            capability.Intelligence = Math.ceil(capability.Intelligence * 1.15);
            capability.Vitality = Math.ceil(capability.Vitality * 1.15);
            capability.perception = Math.ceil(capability.perception * 1.15);
         }

         capability.syncPlayerVariables(player);
      });
   }

   private static void giveStarterPack(ServerPlayer player, int classId) {
      Item item = switch (classId) {
         case 2 -> (Item)SololevelingModItems.MAGE_STARTERPACK.get();
         case 3 -> (Item)SololevelingModItems.FIGHTER_STARTERPACK.get();
         case 4 -> (Item)SololevelingModItems.TANKER_STARTERPACK.get();
         case 5 -> (Item)SololevelingModItems.HEALER_STARTERPACK.get();
         case 6 -> (Item)SololevelingModItems.RANGER_STARTERPACK.get();
         default -> null;
      };
      if (item != null) {
         ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(item));
      }
   }

   private static void grantClassPowers(ServerPlayer player, int classId) {
      switch (classId) {
         case 1:
            PowerAppendAssassinProcedure.execute(player);
            break;
         case 2:
            PowerAppendMageProcedure.execute(player);
            break;
         case 3:
            PowerAppendFighterProcedure.execute(player);
            break;
         case 4:
            PowerAppendTankerProcedure.execute(player);
            break;
         case 5:
            PowerAppendHealerProcedure.execute(player);
            break;
         case 6:
            PowerAppendRangerProcedure.execute(player);
      }
   }
}
