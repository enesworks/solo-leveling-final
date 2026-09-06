package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.network.VesselSelectionStateMessage;
import dev.eness.sololevelingfinal.core.procedures.DungeonDimensionPlayerLeavesDimensionProcedure;
import dev.eness.sololevelingfinal.core.procedures.JobChangeCleanupProcedure;

@EventBusSubscriber
public final class JobChangeQuestManager {
   public static final String QUEST_ID = "job_change";
   public static final int STATE_IDLE = 0;
   public static final int STATE_DUNGEON_ACTIVE = -4;
   public static final int STATE_SHADOW_PRESENTATION = -3;
   public static final int STATE_ADVANCEMENT = -2;
   public static final int STATE_SELECTION = -1;
   private static final String TOKEN = "job_change,";
   private static final String SELECTION_AUTHORIZED = "slr_job_change_selection_authorized";
   private static final String COMMAND_SELECTION_AUTHORIZED = "slr_command_vessel_selection_authorized";
   private static final String SELECTION_OPEN_AFTER_TAG = "slr_job_change_selection_open_after";
   public static final String ATTEMPT_ID_TAG = "slr_job_change_attempt_id";
   public static final String ATTEMPT_OWNER_TAG = "slr_job_change_attempt_owner";
   private static final String ATTEMPT_BOSS_PROCESSED_TAG = "slr_job_change_attempt_boss_processed";
   private static final String ADVANCEMENT_POINT_CREDITED_TAG = "slr_job_change_advancement_point_credited";
   private static final String RETRY_AFTER_TAG = "slr_job_change_retry_after";
   private static final String FAILURE_NOTICE_TAG = "slr_job_change_failure_notice";
   private static final long RETRY_DELAY_TICKS = 200L;
   private static final long SELECTION_OPEN_DELAY_TICKS = 30L;
   private static final int ACCENT = -8758017;
   private static final double PARTY_RANGE_SQR = 65536.0;
   private static final ResourceKey<Level> IGRIS_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling", "dungeon_dimension_igris")
   );

   private JobChangeQuestManager() {
   }

   public static boolean isUnlocked(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = vars(entity);
      return vars.JOB > 0.0 || contains(vars.unlocked_quests, "job_change");
   }

   public static boolean isFinished(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = vars(entity);
      return contains(vars.finished_quests, "job_change") || vars.JOB > 0.0 && state(vars) == 0;
   }

   public static boolean isVisible(Entity entity) {
      return isUnlocked(entity) && !isFinished(entity);
   }

   public static boolean isDungeonActive(Entity entity) {
      return entity != null && state(vars(entity)) == -4;
   }

   public static boolean isAdvancementActive(Entity entity) {
      return entity != null && state(vars(entity)) == -2;
   }

   public static boolean isSelectionPending(Entity entity) {
      return entity != null && state(vars(entity)) == -1;
   }

   public static boolean isShadowPresentation(Entity entity) {
      return entity != null && state(vars(entity)) == -3;
   }

   public static boolean canResumeDungeon(Entity entity) {
      if (entity instanceof ServerPlayer player && (isDungeonActive(player) || isAdvancementActive(player))) {
         UUID attemptId = activeAttemptId(player);
         return attemptId != null && JobChangeAttemptSavedData.get(player.server).isActive(attemptId);
      } else {
         return false;
      }
   }

   public static int advancementPoints(Entity entity) {
      return entity == null ? 0 : Math.max(0, (int)Math.round(vars(entity).jobadvpoint));
   }

   public static int requiredPoints(Entity entity) {
      return entity == null ? 50 : Math.max(1, entity.level().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_JOB_CHANGE_POINTS));
   }

   public static boolean isOverworld(Entity entity) {
      return entity != null && Level.OVERWORLD.equals(entity.level().dimension());
   }

   public static boolean unlock(Entity entity) {
      if (entity instanceof ServerPlayer player && isOverworld(player) && !isUnlocked(player) && !isFinished(player)) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (!contains(capability.unlocked_quests, "job_change")) {
               capability.unlocked_quests = append(capability.unlocked_quests);
            }

            capability.jobkey = true;
            capability.syncPlayerVariables(player);
         });
         SystemNotifications.showTitleUnder(
            player,
            -8758017,
            100,
            Component.literal("QUEST UNLOCKED").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
            Component.literal("Job Change Quest").withStyle(ChatFormatting.LIGHT_PURPLE)
         );
         return true;
      } else {
         return false;
      }
   }

   public static boolean startDungeonRun(ServerPlayer player) {
      if (player != null && isOverworld(player) && retryDelayTicks(player) <= 0) {
         UUID previousAttempt = activeAttemptId(player);
         if (previousAttempt != null) {
            JobChangeAttemptSavedData.get(player.server).removeParticipant(previousAttempt, player.getUUID());
         }

         UUID attemptId = UUID.randomUUID();
         setActiveAttemptId(player, attemptId);
         clearRetryState(player);
         JobChangeAttemptSavedData.get(player.server).start(attemptId, player.getUUID(), currentGameTime(player.server));
         player.getPersistentData().remove("slr_job_change_selection_authorized");
         player.getPersistentData().remove("slr_command_vessel_selection_authorized");
         persistentPlayerData(player).remove("slr_job_change_selection_open_after");
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.jobtimer = -4.0;
            capability.jobadvpoint = 0.0;
            capability.JobChange_timer = 0.0;
            capability.instancecomplete = false;
            capability.syncPlayerVariables(player);
         });
         return true;
      } else {
         return false;
      }
   }

   public static List<ServerPlayer> beginAdvancementPhase(ServerPlayer killer, Entity defeatedBoss) {
      UUID attemptId = attemptId(defeatedBoss);
      if (attemptId != null && matchesActiveAttempt(killer, defeatedBoss) && markBossProcessed(defeatedBoss)) {
         List<ServerPlayer> participants = questParticipants(killer, false);
         participants.removeIf(playerx -> !isDungeonActive(playerx) || playerx.level() != defeatedBoss.level());

         for (ServerPlayer player : participants) {
            if (!isAdvancementActive(player) && !isSelectionPending(player) && !isShadowPresentation(player)) {
               UUID previousAttempt = activeAttemptId(player);
               if (previousAttempt != null && !previousAttempt.equals(attemptId)) {
                  JobChangeCleanupProcedure.executeAttempt(player.server, previousAttempt);
                  JobChangeAttemptSavedData.get(player.server).removeParticipant(previousAttempt, player.getUUID());
               }

               setActiveAttemptId(player, attemptId);
               JobChangeAttemptSavedData.get(player.server).addParticipant(attemptId, player.getUUID(), currentGameTime(player.server));
               player.getPersistentData().remove("slr_job_change_selection_authorized");
               persistentPlayerData(player).remove("slr_job_change_selection_open_after");
               player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.giftstatus = true;
                  capability.jobtimer = -2.0;
                  capability.jobadvpoint = 0.0;
                  capability.JobChange_timer = 0.0;
                  capability.instancecomplete = false;
                  capability.syncPlayerVariables(player);
               });
               SystemNotifications.showTitleUnder(
                  player,
                  -2124281,
                  100,
                  Component.literal("BOSS SLAIN").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                  Component.literal("Defeat the summoned knights [0/" + requiredPoints(player) + "]").withStyle(ChatFormatting.LIGHT_PURPLE)
               );
            }
         }

         return participants;
      } else {
         return List.of();
      }
   }

   public static void grantAdvancementPoint(ServerPlayer killer, Entity defeated) {
      if (killer != null
         && defeated != null
         && isAdvancementActive(killer)
         && defeated.getPersistentData().getBoolean("slr_job_change_advancement_knight")
         && matchesActiveAttempt(killer, defeated)
         && !defeated.getPersistentData().getBoolean("slr_job_change_advancement_point_credited")) {
         UUID attemptId = attemptId(defeated);
         List<ServerPlayer> recipients = questParticipants(killer, true);
         recipients.removeIf(playerx -> !hasAttemptId(playerx, attemptId));
         if (!recipients.isEmpty()) {
            if (JobChangeAttemptSavedData.get(defeated.level().getServer())
               .creditAdvancementKill(attemptId, defeated.getUUID(), currentGameTime(defeated.level().getServer()))) {
               defeated.getPersistentData().putBoolean("slr_job_change_advancement_point_credited", true);

               for (ServerPlayer player : recipients) {
                  int required = requiredPoints(player);
                  player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.jobadvpoint = Math.min(required, Math.max(0.0, capability.jobadvpoint) + 1.0);
                     capability.syncPlayerVariables(player);
                  });
                  int progress = advancementPoints(player);
                  player.displayClientMessage(
                     Component.literal("Advancement Points [" + progress + "/" + required + "]").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true
                  );
               }

               boolean allComplete = recipients.stream().allMatch(playerx -> advancementPoints(playerx) >= requiredPoints(playerx));
               if (allComplete) {
                  for (ServerPlayer player : recipients) {
                     enterSelection(player);
                  }

                  JobChangeCleanupProcedure.completeAttempt(defeated.level().getServer(), attemptId);
                  JobChangeAttemptSavedData.get(defeated.level().getServer()).complete(attemptId);
               }
            }
         }
      }
   }

   public static void selectVessel(ServerPlayer player, String type, String identity) {
      if (player != null) {
         if (isSelectionPending(player) && isSelectionAuthorized(player)) {
            VesselManager.VesselDefinition definition = VesselManager.definition(type, identity);
            if (definition == null) {
               selectionError(player, "That vessel does not exist.");
            } else if (!VesselManager.isSelectableFor(player, definition)) {
               selectionError(player, "WIP (Work in progress)");
            } else if (!selectionOpenReady(player)) {
               closeSelection(player);
            } else {
               VesselManager.AssignmentResult result = VesselManager.isAntares(definition)
                  ? VesselManager.assignAntaresVessel(player, true)
                  : VesselManager.assignPlayer(player, definition, true);
               if (result == VesselManager.AssignmentResult.LOCKED) {
                  selectionError(player, definition.name() + " has already reached the server limit.");
               } else if (result != VesselManager.AssignmentResult.SUCCESS) {
                  selectionError(player, "The System could not assign that vessel.");
               } else {
                  player.getPersistentData().remove("slr_command_vessel_selection_authorized");
                  closeSelection(player);
                  if ("ashborn".equals(definition.identity())) {
                     player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.jobtimer = -3.0;
                        capability.JobChange_timer = 1.0;
                        capability.instancecomplete = false;
                        capability.syncPlayerVariables(player);
                     });
                  } else {
                     finish(player);
                     int color = "ruler".equals(definition.type()) ? -12597505 : -4626945;
                     SystemNotifications.showTitleUnder(
                        player,
                        color,
                        120,
                        Component.literal("VESSEL SELECTED").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD),
                        Component.literal(definition.commandDisplay())
                           .withStyle("ruler".equals(definition.type()) ? ChatFormatting.AQUA : ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                     );
                  }
               }
            }
         } else {
            if (!isFinished(player) && !(vars(player).JOB > 0.0)) {
               selectionError(player, "The Job Change trial is not complete.");
            } else {
               closeSelection(player);
            }
         }
      }
   }

   public static void requestSelectionScreen(ServerPlayer player) {
      if (player != null) {
         if (isSelectionPending(player) && isSelectionAuthorized(player) && selectionOpenReady(player)) {
            sendSelectionState(player);
         }
      }
   }

   public static void openSelectionFromCommand(ServerPlayer player) {
      if (player != null) {
         player.getPersistentData().putBoolean("slr_job_change_selection_authorized", true);
         player.getPersistentData().putBoolean("slr_command_vessel_selection_authorized", true);
         persistentPlayerData(player).remove("slr_job_change_selection_open_after");
         int required = requiredPoints(player);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.jobtimer = -1.0;
            capability.jobadvpoint = required;
            capability.JobChange_timer = 0.0;
            capability.instancecomplete = false;
            capability.syncPlayerVariables(player);
         });
         sendSelectionState(player);
      }
   }

   public static void finish(Entity entity) {
      if (entity != null) {
         if (entity instanceof ServerPlayer player) {
            UUID attemptId = activeAttemptId(player);
            if (attemptId != null) {
               JobChangeCleanupProcedure.completeAttempt(player.server, attemptId);
               JobChangeAttemptSavedData.get(player.server).complete(attemptId);
            }

            clearActiveAttempt(player);
            clearRetryState(player);
         }

         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (!contains(capability.unlocked_quests, "job_change")) {
               capability.unlocked_quests = append(capability.unlocked_quests);
            }

            if (!contains(capability.finished_quests, "job_change")) {
               capability.finished_quests = append(capability.finished_quests);
            }

            capability.jobtimer = 0.0;
            capability.JobChange_timer = 0.0;
            capability.instancecomplete = true;
            capability.jobkey = false;
            capability.syncPlayerVariables(entity);
         });
         entity.setNoGravity(false);
         entity.getPersistentData().putBoolean("slr_job_change_dungeon", false);
         entity.getPersistentData().remove("slr_job_change_selection_authorized");
         entity.getPersistentData().remove("slr_command_vessel_selection_authorized");
         if (entity instanceof Player) {
            persistentPlayerData(entity).remove("slr_job_change_selection_open_after");
         }
      }
   }

   public static void resetForPlayerReset(ServerPlayer player) {
      if (player != null) {
         UUID attemptId = activeAttemptId(player);
         if (attemptId != null) {
            JobChangeCleanupProcedure.executeAttempt(player.server, attemptId);
            JobChangeAttemptSavedData.get(player.server).removeParticipant(attemptId, player.getUUID());
         }

         clearActiveAttempt(player);
         clearRetryState(player);
         player.getPersistentData().remove("slr_job_change_selection_authorized");
         player.getPersistentData().remove("slr_command_vessel_selection_authorized");
         persistentPlayerData(player).remove("slr_job_change_selection_open_after");
         player.getPersistentData().remove("slr_job_change_dungeon");
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.unlocked_quests = removeToken(capability.unlocked_quests, "job_change");
            capability.finished_quests = removeToken(capability.finished_quests, "job_change");
            capability.jobtimer = 0.0;
            capability.jobadvpoint = 0.0;
            capability.JobChange_timer = 0.0;
            capability.instancecomplete = false;
            capability.giftstatus = false;
            capability.jobkey = false;
            capability.JOB = 0.0;
            capability.syncPlayerVariables(player);
         });
         player.setNoGravity(false);
         closeSelection(player);
      }
   }

   @Nullable
   public static UUID attemptId(Entity entity) {
      if (entity == null) {
         return null;
      }

      CompoundTag data;
      if (entity instanceof Player) {
         CompoundTag root = entity.getPersistentData();
         if (!root.contains("PlayerPersisted", 10)) {
            return null;
         }

         data = root.getCompound("PlayerPersisted");
      } else {
         data = entity.getPersistentData();
      }

      return data.hasUUID("slr_job_change_attempt_id") ? data.getUUID("slr_job_change_attempt_id") : null;
   }

   @Nullable
   public static UUID activeAttemptId(Entity entity) {
      return attemptId(entity);
   }

   public static boolean hasAttemptId(Entity entity, @Nullable UUID attemptId) {
      UUID actual = attemptId(entity);
      return attemptId != null && attemptId.equals(actual);
   }

   public static boolean isAttemptEntity(Entity entity) {
      return entity != null && !(entity instanceof Player) && entity.getPersistentData().hasUUID("slr_job_change_attempt_id");
   }

   public static void tagAttemptEntity(Entity entity, UUID attemptId, UUID ownerId) {
      if (entity != null && !(entity instanceof Player) && attemptId != null) {
         entity.getPersistentData().putUUID("slr_job_change_attempt_id", attemptId);
         if (ownerId != null) {
            entity.getPersistentData().putUUID("slr_job_change_attempt_owner", ownerId);
         }
      }
   }

   public static void copyAttempt(Entity source, Entity target) {
      if (source != null && target != null) {
         UUID attemptId = attemptId(source);
         if (attemptId != null) {
            UUID ownerId = source.getPersistentData().hasUUID("slr_job_change_attempt_owner")
               ? source.getPersistentData().getUUID("slr_job_change_attempt_owner")
               : null;
            tagAttemptEntity(target, attemptId, ownerId);
         }
      }
   }

   public static boolean matchesActiveAttempt(ServerPlayer player, Entity encounterEntity) {
      if (player != null && encounterEntity != null && player.level() == encounterEntity.level()) {
         UUID playerAttempt = activeAttemptId(player);
         return playerAttempt != null && hasAttemptId(encounterEntity, playerAttempt)
            ? JobChangeAttemptSavedData.get(player.server).isActive(playerAttempt)
            : false;
      } else {
         return false;
      }
   }

   public static boolean isAttemptActive(MinecraftServer server, UUID attemptId) {
      if (server != null && attemptId != null && JobChangeAttemptSavedData.get(server).isActive(attemptId)) {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (hasAttemptId(player, attemptId) && (isDungeonActive(player) || isAdvancementActive(player))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static int retryDelayTicks(ServerPlayer player) {
      if (player != null && player.server != null) {
         long remaining = persistentPlayerData(player).getLong("slr_job_change_retry_after") - currentGameTime(player.server);
         return (int)Math.min(2147483647L, Math.max(0L, remaining));
      } else {
         return 0;
      }
   }

   public static boolean failActiveAttempt(ServerPlayer failedPlayer) {
      if (failedPlayer != null && (isDungeonActive(failedPlayer) || isAdvancementActive(failedPlayer))) {
         UUID attemptId = activeAttemptId(failedPlayer);
         long now = currentGameTime(failedPlayer.server);
         long retryAfter = now + 200L;
         Set<UUID> participantIds = new LinkedHashSet<>();
         if (attemptId != null) {
            participantIds.addAll(JobChangeAttemptSavedData.get(failedPlayer.server).invalidate(attemptId, retryAfter, now));
            JobChangeCleanupProcedure.executeAttempt(failedPlayer.server, attemptId);
         } else {
            participantIds.add(failedPlayer.getUUID());
            if (failedPlayer.level() instanceof ServerLevel) {
               JobChangeCleanupProcedure.execute(failedPlayer.level(), failedPlayer.getX(), failedPlayer.getY(), failedPlayer.getZ());
            }
         }

         List<ServerPlayer> affected = new ArrayList<>();

         for (ServerPlayer player : failedPlayer.server.getPlayerList().getPlayers()) {
            if (participantIds.contains(player.getUUID()) || attemptId != null && hasAttemptId(player, attemptId)) {
               affected.add(player);
            }
         }

         if (affected.stream().noneMatch(playerx -> playerx.getUUID().equals(failedPlayer.getUUID()))) {
            affected.add(failedPlayer);
         }

         for (ServerPlayer player : affected) {
            resetFailedAttemptState(player, attemptId, retryAfter);
            if (player != failedPlayer && player.isAlive()) {
               showFailureNotice(player);
               persistentPlayerData(player).remove("slr_job_change_failure_notice");
               if (player.level().dimension().equals(IGRIS_DIMENSION)) {
                  DungeonDimensionPlayerLeavesDimensionProcedure.emergencyExit(player);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static void unlockIfEligible(LevelAccessor world, Entity entity, int requiredLevel) {
      if (entity instanceof ServerPlayer player && world != null) {
         SololevelingModVariables.PlayerVariables vars = vars(player);
         if (vars.JOB > 0.0 && state(vars) == 0) {
            finish(player);
         } else if (isOverworld(player)) {
            if (vars.Player
               && vars.Level >= requiredLevel
               && vars.JOB == 0.0
               && !contains(vars.finished_quests, "job_change")
               && !contains(vars.unlocked_quests, "job_change")) {
               unlock(player);
            }
         }
      }
   }

   public static boolean hasAdvancementPlayerNear(Entity portal, double range) {
      if (portal != null && !portal.level().isClientSide()) {
         UUID attemptId = attemptId(portal);
         if (attemptId == null) {
            return false;
         }

         double rangeSqr = range * range;

         for (ServerPlayer player : portal.level().getServer().getPlayerList().getPlayers()) {
            if (player.level() == portal.level() && isAdvancementActive(player) && hasAttemptId(player, attemptId) && player.distanceToSqr(portal) <= rangeSqr) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onPlayerDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && player.level().dimension().equals(IGRIS_DIMENSION)) {
         failActiveAttempt(player);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onAttemptMobDrops(LivingDropsEvent event) {
      if (isAttemptEntity(event.getEntity())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onAttemptMobExperience(LivingExperienceDropEvent event) {
      if (isAttemptEntity(event.getEntity())) {
         event.setDroppedExperience(0);
      }
   }

   @SubscribeEvent
   public static void onPlayerClone(Clone event) {
      if (event.getEntity() instanceof ServerPlayer clone) {
         CompoundTag originalRoot = event.getOriginal().getPersistentData();
         if (originalRoot.contains("PlayerPersisted", 10)) {
            CompoundTag original = originalRoot.getCompound("PlayerPersisted");
            CompoundTag copied = persistentPlayerData(clone);
            if (original.hasUUID("slr_job_change_attempt_id")) {
               copied.putUUID("slr_job_change_attempt_id", original.getUUID("slr_job_change_attempt_id"));
            }

            if (original.contains("slr_job_change_retry_after", 99)) {
               copied.putLong("slr_job_change_retry_after", original.getLong("slr_job_change_retry_after"));
            }

            if (original.getBoolean("slr_job_change_failure_notice")) {
               copied.putBoolean("slr_job_change_failure_notice", true);
            }

            if (original.contains("slr_job_change_selection_open_after", 99)) {
               copied.putLong("slr_job_change_selection_open_after", original.getLong("slr_job_change_selection_open_after"));
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         recover(player, true);
         showPendingFailureNotice(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         recover(player, true);
         showPendingFailureNotice(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player && player.tickCount % 20 == 0) {
         recover(player, player.tickCount % 40 == 0);
      }
   }

   private static void enterSelection(ServerPlayer player) {
      if (player != null && isAdvancementActive(player) && advancementPoints(player) >= requiredPoints(player)) {
         clearActiveAttempt(player);
         clearRetryState(player);
         player.getPersistentData().putBoolean("slr_job_change_selection_authorized", true);
         persistentPlayerData(player).putLong("slr_job_change_selection_open_after", currentGameTime(player.server) + 30L);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.jobtimer = -1.0;
            capability.JobChange_timer = 0.0;
            capability.instancecomplete = false;
            capability.syncPlayerVariables(player);
         });
         SystemNotifications.showTitleUnder(
            player,
            -8758017,
            90,
            Component.literal("ADVANCEMENT COMPLETE").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
            Component.literal("Choose the power that will become your vessel.").withStyle(ChatFormatting.LIGHT_PURPLE)
         );
      }
   }

   private static void recover(ServerPlayer player, boolean refreshScreen) {
      SololevelingModVariables.PlayerVariables data = vars(player);
      int currentState = state(data);
      if (currentState == -4 || currentState == -2) {
         UUID attemptId = activeAttemptId(player);
         JobChangeAttemptSavedData.AttemptView attempt = attemptId == null ? null : JobChangeAttemptSavedData.get(player.server).view(attemptId);
         if (attempt == null || !attempt.active()) {
            long retryAfter = attempt == null ? currentGameTime(player.server) : attempt.retryAfterGameTime();
            resetFailedAttemptState(player, attemptId, retryAfter);
            persistentPlayerData(player).remove("slr_job_change_failure_notice");
            showFailureNotice(player);
            if (player.level().dimension().equals(IGRIS_DIMENSION)) {
               DungeonDimensionPlayerLeavesDimensionProcedure.emergencyExit(player);
            }

            return;
         }
      }

      if (data.JOB == 0.0 && data.jobtimer > 0.0) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.jobtimer = -2.0;
            capability.jobadvpoint = Math.max(0.0, capability.jobadvpoint - 1.0);
            capability.syncPlayerVariables(player);
         });
         currentState = -2;
      }

      if (data.JOB == 0.0 && data.JobChange_timer > 0.0) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.jobtimer = -1.0;
            capability.JobChange_timer = 0.0;
            capability.instancecomplete = false;
            capability.syncPlayerVariables(player);
         });
         currentState = -1;
      }

      if (data.JOB == 0.0 && currentState == -1 && !isSelectionAuthorized(player)) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.jobtimer = 0.0;
            capability.jobadvpoint = 0.0;
            capability.JobChange_timer = 0.0;
            capability.syncPlayerVariables(player);
         });
         player.getPersistentData().remove("slr_job_change_selection_authorized");
         closeSelection(player);
      } else if (currentState == -1 && data.JOB > 0.0 && !isCommandSelectionAuthorized(player)) {
         VesselManager.VesselDefinition definition = VesselManager.currentDefinition(player);
         if (definition != null && "ashborn".equals(definition.identity())) {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.jobtimer = -3.0;
               capability.JobChange_timer = Math.max(1.0, capability.JobChange_timer);
               capability.syncPlayerVariables(player);
            });
         } else {
            finish(player);
         }

         closeSelection(player);
      } else {
         if (currentState == -1 && isSelectionAuthorized(player)) {
            CompoundTag persisted = persistentPlayerData(player);
            boolean delayed = persisted.contains("slr_job_change_selection_open_after", 99);
            if (selectionOpenReady(player) && (delayed || refreshScreen)) {
               sendSelectionState(player);
            }
         } else if (data.JOB > 0.0 && currentState == 0 && !contains(data.finished_quests, "job_change")) {
            finish(player);
         }
      }
   }

   private static List<ServerPlayer> questParticipants(ServerPlayer anchor, boolean requireAdvancement) {
      List<ServerPlayer> result = new ArrayList<>();
      if (anchor == null) {
         return result;
      }

      String party = vars(anchor).party;

      for (ServerPlayer candidate : anchor.serverLevel().players()) {
         boolean samePlayer = candidate.getUUID().equals(anchor.getUUID());
         boolean sameParty = party != null && !party.isBlank() && party.equals(vars(candidate).party);
         if ((samePlayer || sameParty)
            && !(candidate.distanceToSqr(anchor) > 65536.0)
            && (!requireAdvancement || isAdvancementActive(candidate))
            && (requireAdvancement || !(vars(candidate).JOB > 0.0) && !isFinished(candidate))) {
            result.add(candidate);
         }
      }

      if (result.isEmpty() && (!requireAdvancement || isAdvancementActive(anchor))) {
         result.add(anchor);
      }

      return result;
   }

   private static void selectionError(ServerPlayer player, String message) {
      SystemNotifications.showNegativeTitleUnder(
         player,
         -49830,
         80,
         Component.literal("VESSEL UNAVAILABLE").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
         Component.literal(message).withStyle(ChatFormatting.GRAY)
      );
      if (isSelectionPending(player) && isSelectionAuthorized(player) && selectionOpenReady(player)) {
         sendSelectionState(player);
      } else {
         closeSelection(player);
      }
   }

   private static void sendSelectionState(ServerPlayer player) {
      SololevelingMod.PACKET_HANDLER
         .send(
            PacketDistributor.PLAYER.with(() -> player),
            new VesselSelectionStateMessage(
               true,
               advancementPoints(player),
               requiredPoints(player),
               VesselManager.vesselLimit(player),
               VesselManager.claimCounts(player),
               DeveloperModeManager.isEnabled(player)
            )
         );
   }

   private static void closeSelection(ServerPlayer player) {
      SololevelingMod.PACKET_HANDLER
         .send(
            PacketDistributor.PLAYER.with(() -> player),
            new VesselSelectionStateMessage(
               false, 0, requiredPoints(player), VesselManager.vesselLimit(player), new int[0], DeveloperModeManager.isEnabled(player)
            )
         );
   }

   private static boolean markBossProcessed(Entity boss) {
      if (boss != null && !boss.getPersistentData().getBoolean("slr_job_change_attempt_boss_processed")) {
         boss.getPersistentData().putBoolean("slr_job_change_attempt_boss_processed", true);
         return true;
      } else {
         return false;
      }
   }

   private static void setActiveAttemptId(ServerPlayer player, UUID attemptId) {
      if (player != null && attemptId != null) {
         persistentPlayerData(player).putUUID("slr_job_change_attempt_id", attemptId);
      }
   }

   private static void clearActiveAttempt(ServerPlayer player) {
      if (player != null) {
         persistentPlayerData(player).remove("slr_job_change_attempt_id");
      }
   }

   private static void clearRetryState(ServerPlayer player) {
      if (player != null) {
         CompoundTag data = persistentPlayerData(player);
         data.remove("slr_job_change_retry_after");
         data.remove("slr_job_change_failure_notice");
      }
   }

   private static void resetFailedAttemptState(ServerPlayer player, @Nullable UUID attemptId, long retryAfter) {
      if (player != null) {
         player.getPersistentData().remove("slr_job_change_selection_authorized");
         player.getPersistentData().remove("slr_command_vessel_selection_authorized");
         persistentPlayerData(player).remove("slr_job_change_selection_open_after");
         player.getPersistentData().putBoolean("slr_job_change_dungeon", false);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (!contains(capability.unlocked_quests, "job_change")) {
               capability.unlocked_quests = append(capability.unlocked_quests);
            }

            capability.jobtimer = 0.0;
            capability.jobadvpoint = 0.0;
            capability.JobChange_timer = 0.0;
            capability.instancecomplete = false;
            capability.giftstatus = false;
            capability.jobkey = true;
            capability.BossKilled = false;
            capability.tpd = false;
            capability.syncPlayerVariables(player);
         });
         clearActiveAttempt(player);
         CompoundTag persisted = persistentPlayerData(player);
         if (retryAfter > currentGameTime(player.server)) {
            persisted.putLong("slr_job_change_retry_after", retryAfter);
         } else {
            persisted.remove("slr_job_change_retry_after");
         }

         persisted.putBoolean("slr_job_change_failure_notice", true);
         if (attemptId != null) {
            JobChangeAttemptSavedData.get(player.server).acknowledgeFailure(attemptId, player.getUUID());
         }

         player.setNoGravity(false);
         player.fallDistance = 0.0F;
         closeSelection(player);
      }
   }

   private static void showFailureNotice(ServerPlayer player) {
      int seconds = (retryDelayTicks(player) + 19) / 20;
      MutableComponent detail = seconds > 0
         ? Component.literal("The attempt was reset. Retry in " + seconds + " seconds.")
         : Component.literal("The attempt was reset. You can try again.");
      SystemNotifications.showNegativeTitleUnder(
         player,
         -49830,
         100,
         Component.literal("JOB CHANGE FAILED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
         detail.copy().withStyle(ChatFormatting.GRAY)
      );
   }

   private static void showPendingFailureNotice(ServerPlayer player) {
      CompoundTag persisted = persistentPlayerData(player);
      if (persisted.getBoolean("slr_job_change_failure_notice")) {
         if (player.level().dimension().equals(IGRIS_DIMENSION) && !DungeonDimensionPlayerLeavesDimensionProcedure.emergencyExit(player)) {
            showFailureNotice(player);
         } else {
            persisted.remove("slr_job_change_failure_notice");
            showFailureNotice(player);
         }
      }
   }

   private static CompoundTag persistentPlayerData(Entity entity) {
      CompoundTag root = entity.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         root.put("PlayerPersisted", new CompoundTag());
      }

      return root.getCompound("PlayerPersisted");
   }

   private static long currentGameTime(MinecraftServer server) {
      return server == null ? 0L : server.overworld().getGameTime();
   }

   private static boolean isSelectionAuthorized(Entity entity) {
      return entity != null
         && (entity.getPersistentData().getBoolean("slr_job_change_selection_authorized") || advancementPoints(entity) >= requiredPoints(entity));
   }

   private static boolean isCommandSelectionAuthorized(Entity entity) {
      return entity != null && entity.getPersistentData().getBoolean("slr_command_vessel_selection_authorized");
   }

   private static int state(SololevelingModVariables.PlayerVariables vars) {
      return (int)Math.round(vars.jobtimer);
   }

   private static SololevelingModVariables.PlayerVariables vars(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static boolean contains(String list, String id) {
      return list != null && list.contains(id + ",");
   }

   private static String append(String list) {
      return list != null && !list.equals("\"\"") && !list.isBlank() ? list + "job_change," : "job_change,";
   }

   private static boolean selectionOpenReady(ServerPlayer player) {
      CompoundTag persisted = persistentPlayerData(player);
      if (!persisted.contains("slr_job_change_selection_open_after", 99)) {
         return true;
      }

      if (currentGameTime(player.server) < persisted.getLong("slr_job_change_selection_open_after")) {
         return false;
      }

      persisted.remove("slr_job_change_selection_open_after");
      return true;
   }

   private static String removeToken(String list, String id) {
      if (list != null && !list.isBlank()) {
         StringBuilder result = new StringBuilder();

         for (String entry : list.split(",")) {
            if (!entry.isBlank() && !entry.equals(id)) {
               result.append(entry).append(',');
            }
         }

         return result.toString();
      } else {
         return "";
      }
   }
}
