package dev.eness.sololevelingfinal.core.util.daily;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.DailyQuestHelper;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;
import dev.eness.sololevelingfinal.core.util.RewardManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;

@EventBusSubscriber(modid = "sololeveling")
public final class DailyQuestLifecycleManager {
   public static final int SAVE_SCHEMA = 2;
   public static final double QUEST_DURATION_TICKS = 24000.0;
   private static final double FIVE_MINUTES_TICKS = 6000.0;
   private static final double ONE_MINUTE_TICKS = 1200.0;
   private static final String DAILY_FULL_RECOVERY_REWARD = "FR";
   private static final String DAILY_SKILL_POINTS_REWARD = "SP10";
   private static final String DAILY_ITEM_REWARD = "ITEMBOX";
   private static final String SECRET_SKILL_POINTS_REWARD = "SP20";
   private static final String SECRET_DKC_KEY_REWARD = "ITEM:sololeveling:redkey";
   private static final ResourceKey<Level> SURVIVAL_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling", "survival_dimension")
   );

   private DailyQuestLifecycleManager() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null) {
            DailyQuestHelper.clearSecretQuestIfDkcUnlocked(player);
            long minecraftDay = minecraftDay(player);
            boolean migratedActiveQuest = migrateIfNeeded(player, variables, minecraftDay);
            if (SystemPlayerAccess.hasSystem(player)) {
               if (!migratedActiveQuest) {
                  if (variables.ActiveDaily) {
                     tickActiveQuest(player, variables);
                  } else {
                     if (dailyQuestsEnabled(player) && variables.lastDailyQuestDay != minecraftDay) {
                        startQuest(player, variables, minecraftDay);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onObjectivesCompleted(DailyQuestObjectivesCompletedEvent event) {
      completeQuest(event.getPlayer());
   }

   public static void startQuestNow(ServerPlayer player) {
      if (player != null) {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null) {
            startQuest(player, variables, minecraftDay(player));
         }
      }
   }

   public static boolean finishQuestNow(ServerPlayer player) {
      if (player == null) {
         return false;
      } else {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null && variables.ActiveDaily) {
            variables.dailyMinedBlocks = DailyQuestObjectiveManager.miningTarget(player);
            variables.dailyThreatPoints = DailyQuestObjectiveManager.threatTarget(player);
            variables.RUN = DailyQuestObjectiveManager.distanceTarget(player);
            variables.syncPlayerVariables(player);
            return DailyQuestObjectiveManager.evaluateCompletion(player);
         } else {
            return false;
         }
      }
   }

   public static void resetQuestState(ServerPlayer player, boolean allowAssignmentToday) {
      if (player != null) {
         SololevelingModVariables.PlayerVariables variables = variables(player);
         if (variables != null) {
            variables.dailyQuestSchema = 2;
            variables.ActiveDaily = false;
            variables.dailytimer = 0.0;
            variables.dailyMinedBlocks = 0.0;
            variables.dailyThreatPoints = 0.0;
            variables.RUN = 0.0;
            variables.dailyCombatWaived = false;
            variables.lastDailyQuestDay = allowAssignmentToday ? Long.MIN_VALUE : minecraftDay(player);
            clearLegacyWorkoutState(variables);
            variables.syncPlayerVariables(player);
            DailyQuestObjectiveManager.resetQuestRuntime(player);
         }
      }
   }

   private static boolean migrateIfNeeded(ServerPlayer player, SololevelingModVariables.PlayerVariables variables, long minecraftDay) {
      if (variables.dailyQuestSchema >= 2) {
         return false;
      } else {
         boolean hadActiveLegacyQuest = variables.ActiveDaily;
         variables.dailyQuestSchema = 2;
         variables.dailyMinedBlocks = 0.0;
         variables.dailyThreatPoints = 0.0;
         variables.RUN = 0.0;
         clearLegacyWorkoutState(variables);
         variables.dailyCombatWaived = player.level().getDifficulty() == Difficulty.PEACEFUL;
         if (hadActiveLegacyQuest) {
            variables.ActiveDaily = true;
            variables.dailytimer = 24000.0;
            variables.lastDailyQuestDay = minecraftDay;
            activateSecretQuestIfEligible(player, variables);
            variables.syncPlayerVariables(player);
            DailyQuestObjectiveManager.resetQuestRuntime(player);
            announceAssignment(player, true, variables.dailyCombatWaived);
            return true;
         } else {
            variables.ActiveDaily = false;
            variables.dailytimer = 0.0;
            variables.lastDailyQuestDay = Long.MIN_VALUE;
            variables.syncPlayerVariables(player);
            DailyQuestObjectiveManager.resetQuestRuntime(player);
            return false;
         }
      }
   }

   private static void startQuest(ServerPlayer player, SololevelingModVariables.PlayerVariables variables, long minecraftDay) {
      variables.dailyQuestSchema = 2;
      variables.ActiveDaily = true;
      variables.dailytimer = 24000.0;
      variables.lastDailyQuestDay = minecraftDay;
      variables.dailyMinedBlocks = 0.0;
      variables.dailyThreatPoints = 0.0;
      variables.RUN = 0.0;
      variables.dailyCombatWaived = player.level().getDifficulty() == Difficulty.PEACEFUL;
      variables.overlay_alpha_dailyquestwarning = 0.0;
      clearLegacyWorkoutState(variables);
      activateSecretQuestIfEligible(player, variables);
      variables.syncPlayerVariables(player);
      DailyQuestObjectiveManager.resetQuestRuntime(player);
      announceAssignment(player, false, variables.dailyCombatWaived);
   }

   private static void tickActiveQuest(ServerPlayer player, SololevelingModVariables.PlayerVariables variables) {
      boolean mustSync = false;
      if (!variables.dailyCombatWaived && player.level().getDifficulty() == Difficulty.PEACEFUL) {
         variables.dailyCombatWaived = true;
         mustSync = true;
      }

      if (variables.dailytimer > 0.0) {
         double previous = variables.dailytimer;
         variables.dailytimer = Math.max(0.0, previous - 1.0);
         if (crossed(previous, variables.dailytimer, 6000.0)) {
            SystemNotifications.showTitleUnder(
               player,
               -18371,
               80,
               Component.literal("5 MINUTES LEFT").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
               Component.literal("Daily quest timer is running out.").withStyle(ChatFormatting.GOLD)
            );
            mustSync = true;
         }

         if (crossed(previous, variables.dailytimer, 1200.0)) {
            SystemNotifications.showTitleUnder(
               player,
               -49859,
               80,
               Component.literal("1 MINUTE LEFT").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
               Component.literal("Finish the daily quest now.").withStyle(ChatFormatting.RED)
            );
            mustSync = true;
         }
      }

      if (variables.dailytimer <= 0.0) {
         variables.dailytimer = 0.0;
         if (player.level().dimension() != SURVIVAL_DIMENSION) {
            failQuest(player);
            return;
         }
      }

      if (mustSync || player.level().getGameTime() % 20L == 0L) {
         variables.syncPlayerVariables(player);
      }
   }

   private static void failQuest(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = variables(player);
      if (variables != null && variables.ActiveDaily) {
         boolean applyPunishment = shouldPunishFailure(player);
         boolean keepSecret = DailyQuestHelper.isSecretQuest(player) || DailyQuestHelper.canActivateSecretQuest(player);
         variables.lastDailyQuestDay = minecraftDay(player);
         DailyQuestHelper.sendQuestFailedChat(player);
         DailyQuestHelper.resetDailyProgress(player);
         if (keepSecret) {
            DailyQuestHelper.keepSecretQuestPending(player);
         }

         if (applyPunishment) {
            enterPunishmentZone(player);
         }
      }
   }

   private static boolean shouldPunishFailure(ServerPlayer player) {
      return player.level().getGameRules().getBoolean(SololevelingModGameRules.SOLO_PUNISHMENT) && !JobChangeQuestManager.isFinished(player);
   }

   private static boolean enterPunishmentZone(ServerPlayer player) {
      return DailyPunishmentManager.enter(player);
   }

   private static void completeQuest(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = variables(player);
      if (variables != null && variables.ActiveDaily) {
         boolean secretQuest = DailyQuestHelper.isSecretQuest(player);
         variables.ActiveDaily = false;
         variables.dailytimer = 0.0;
         variables.lastDailyQuestDay = minecraftDay(player);
         clearLegacyWorkoutState(variables);
         variables.syncPlayerVariables(player);
         DailyQuestObjectiveManager.resetQuestRuntime(player);
         RewardManager.appendReward(player, "FR");
         RewardManager.appendReward(player, "SP10");
         RewardManager.appendReward(player, "ITEMBOX");
         if (secretQuest) {
            DkcQuestManager.unlock(player);
            DailyQuestHelper.completeSecretQuest(player);
            RewardManager.appendReward(player, "FR");
            RewardManager.appendReward(player, "SP20");
            RewardManager.appendReward(player, "ITEM:sololeveling:redkey");
         }

         variables.syncPlayerVariables(player);
         if (secretQuest) {
            SystemNotifications.showTitleUnder(
               player,
               -49779,
               120,
               Component.literal("SECRET QUEST COMPLETE").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
               Component.literal("Rewards added to System Rewards.").withStyle(ChatFormatting.LIGHT_PURPLE)
            );
         } else {
            SystemNotifications.showTitleUnder(
               player,
               -26051,
               100,
               Component.literal("DAILY QUEST COMPLETE").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
               Component.literal("Rewards added to System Rewards.").withStyle(ChatFormatting.GRAY)
            );
         }
      }
   }

   private static void activateSecretQuestIfEligible(ServerPlayer player, SololevelingModVariables.PlayerVariables variables) {
      if (DkcQuestManager.isUnlocked(player)) {
         variables.dailysecrettrans = 0.0;
      } else {
         if (DailyQuestHelper.canActivateSecretQuest(player)) {
            variables.dailysecrettrans = 2.0;
         }
      }
   }

   private static void clearLegacyWorkoutState(SololevelingModVariables.PlayerVariables variables) {
      variables.pushup = 0.0;
      variables.situp = 0.0;
      variables.squat = 0.0;
      variables.traintype = "";
      variables.isdailytraining = false;
   }

   private static void announceAssignment(ServerPlayer player, boolean migrated, boolean combatWaived) {
      String detail = combatWaived
         ? "Mine eligible blocks and travel 10 KM.\nThreat Suppression is waived on Peaceful."
         : "Mine, suppress threats, and travel 10 KM.\nComplete all objectives within 20 minutes.";
      SystemNotifications.showTitleUnder(
         player,
         -12597505,
         120,
         Component.literal(migrated ? "DAILY QUEST UPDATED" : "DAILY QUEST").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
         Component.literal(detail).withStyle(ChatFormatting.GRAY)
      );
   }

   private static boolean crossed(double previous, double current, double threshold) {
      return previous > threshold && current <= threshold;
   }

   private static boolean dailyQuestsEnabled(ServerPlayer player) {
      return player.level().getGameRules().getBoolean(SololevelingModGameRules.SOLO_DAILY_QUEST);
   }

   private static long minecraftDay(ServerPlayer player) {
      return Math.floorDiv(player.server.overworld().getDayTime(), 24000L);
   }

   @Nullable
   private static SololevelingModVariables.PlayerVariables variables(@Nullable ServerPlayer player) {
      return player == null ? null : player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
   }
}
