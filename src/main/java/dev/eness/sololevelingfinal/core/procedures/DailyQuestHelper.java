package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;
import dev.eness.sololevelingfinal.core.util.daily.DailyQuestObjectiveManager;

@EventBusSubscriber
public class DailyQuestHelper {
   public static final double NORMAL_MINING_TARGET = 32.0;
   public static final double SECRET_MINING_TARGET = 64.0;
   public static final double NORMAL_THREAT_TARGET = 8.0;
   public static final double SECRET_THREAT_TARGET = 16.0;
   public static final double NORMAL_RUN_TARGET = 500.0;
   public static final double SECRET_RUN_TARGET = 1000.0;

   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         clearSecretQuestIfDkcUnlocked(player);
      }
   }

   public static boolean isSecretQuest(Entity entity) {
      return entity == null ? false : !DkcQuestManager.isUnlocked(entity) && vars(entity).dailysecrettrans >= 2.0;
   }

   public static boolean isSecretQuestRevealed(Entity entity) {
      return entity == null ? false : !DkcQuestManager.isUnlocked(entity) && vars(entity).dailysecrettrans >= 3.0;
   }

   public static boolean canActivateSecretQuest(Entity entity) {
      if (entity != null && !DkcQuestManager.isUnlocked(entity)) {
         SololevelingModVariables.PlayerVariables variables = vars(entity);
         return variables.Level >= 30.0 && variables.dailysecrettrans != 0.0;
      } else {
         return false;
      }
   }

   public static boolean clearSecretQuestIfDkcUnlocked(Entity entity) {
      if (entity != null && DkcQuestManager.isUnlocked(entity) && vars(entity).dailysecrettrans != 0.0) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.dailysecrettrans = 0.0;
            capability.syncPlayerVariables(entity);
         });
         return true;
      } else {
         return false;
      }
   }

   public static void keepSecretQuestPending(Entity entity) {
      if (entity != null) {
         if (DkcQuestManager.isUnlocked(entity)) {
            clearSecretQuestIfDkcUnlocked(entity);
         } else {
            if (canActivateSecretQuest(entity)) {
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.dailysecrettrans = 2.0;
                  capability.syncPlayerVariables(entity);
               });
            }
         }
      }
   }

   public static void completeSecretQuest(Entity entity) {
      if (entity != null) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.dailysecrettrans = 0.0;
            capability.syncPlayerVariables(entity);
         });
      }
   }

   public static void checkSecretTransition(Entity entity, double previousValue, double newValue, double normalTarget) {
      if (entity != null) {
         SololevelingModVariables.PlayerVariables vars = vars(entity);
         if (!DkcQuestManager.isUnlocked(entity) && vars.dailysecrettrans == 2.0 && previousValue <= normalTarget && newValue > normalTarget) {
            sendSecretTransitionMessage(entity);
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.dailysecrettrans = 3.0;
               capability.syncPlayerVariables(entity);
            });
         }
      }
   }

   public static void resetDailyProgress(Entity entity) {
      if (entity != null) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.ActiveDaily = false;
            capability.dailytimer = 0.0;
            capability.dailyMinedBlocks = 0.0;
            capability.dailyThreatPoints = 0.0;
            capability.dailyCombatWaived = false;
            capability.situp = 0.0;
            capability.squat = 0.0;
            capability.pushup = 0.0;
            capability.RUN = 0.0;
            capability.traintype = "";
            capability.isdailytraining = false;
            capability.syncPlayerVariables(entity);
         });
         if (entity instanceof ServerPlayer serverPlayer) {
            DailyQuestObjectiveManager.resetQuestRuntime(serverPlayer);
         }
      }
   }

   public static void sendQuestFailedChat(Entity entity) {
      if (entity instanceof Player player && !player.level().isClientSide()) {
         player.displayClientMessage(Component.literal("Daily Quest failed. You can try again tomorrow.").withStyle(ChatFormatting.RED), false);
         if (player instanceof ServerPlayer serverPlayer) {
            SystemNotifications.showNegativeTitleUnder(
               serverPlayer,
               -49859,
               80,
               Component.literal("DAILY QUEST FAILED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
               Component.literal("You can try again tomorrow.").withStyle(ChatFormatting.RED)
            );
         }
      }
   }

   private static void sendSecretTransitionMessage(Entity entity) {
      if (entity instanceof Player player && !player.level().isClientSide()) {
         player.displayClientMessage(
            Component.literal("")
               .append(Component.literal("Daily Quest").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
               .append(Component.literal(" turned into ").withStyle(ChatFormatting.RED))
               .append(Component.literal("Secret Quest").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
               .append(Component.literal(".").withStyle(ChatFormatting.RED)),
            false
         );
         if (player instanceof ServerPlayer serverPlayer) {
            SystemNotifications.showTitleUnder(
               serverPlayer,
               -49859,
               100,
               Component.literal("SECRET QUEST").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
               Component.literal("Daily Quest has changed.\nComplete the hidden requirements.").withStyle(ChatFormatting.RED)
            );
         }
      }
   }

   private static SololevelingModVariables.PlayerVariables vars(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }
}
