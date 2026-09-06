package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.CartenonGateEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.CartenonAwakeningStateMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.DungeonDimensionPlayerLeavesDimensionProcedure;

@EventBusSubscriber(modid = "sololeveling")
public final class CartenonTempleManager {
   public static final ResourceKey<Level> CARTENON_DIMENSION = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling", "cartenon_temple"));
   private static final Set<String> ELIGIBLE_DUNGEONS = Set.of(
      "dungeon_dimension_d", "dungeon_dimension_c", "dungeon_dimension_b", "dungeon_dimension_a", "dungeon_dimension_s", "dungeon_dimension_snow"
   );
   private static final int INSTANCE_SPACING = 512;
   private static final int INSTANCE_COLUMNS = 32;
   private static final int TEMPLE_FLOOR_Y = 64;
   private static final int ENTRY_PROTECTION_TICKS = 40;
   private static final int DECLINE_DELAY_TICKS = 4;
   private static final Map<MinecraftServer, Map<Integer, LinkedHashSet<UUID>>> WAITING_PLAYERS = new WeakHashMap<>();
   private static final String INSTANCE_TAG = "slr_cartenon_instance";
   private static final String ENTRY_PROTECTION_TAG = "slr_cartenon_entry_protection";
   private static final String AWAKENING_PENDING_TAG = "slr_cartenon_awakening_pending";
   private static final String DECLINE_TICKS_TAG = "slr_cartenon_decline_ticks";
   private static final String DEATH_BYPASS_TAG = "slr_cartenon_death_bypass";
   private static final String PREVIOUS_INVULNERABLE_TAG = "slr_cartenon_previous_invulnerable";
   private static final String PREVIOUS_NO_GRAVITY_TAG = "slr_cartenon_previous_no_gravity";
   private static final String PROTECTION_STATE_SAVED_TAG = "slr_cartenon_protection_state_saved";

   private CartenonTempleManager() {
   }

   public static boolean onDungeonBossDefeated(LevelAccessor world, Entity boss, Entity creditedSource, String dungeonTag) {
      return world instanceof ServerLevel level && creditedSource instanceof ServerPlayer killer && boss != null && dungeonTag != null && !dungeonTag.isBlank()
         ? onDungeonBossDefeated(level, boss, killer, dungeonTag, dungeonParticipants(killer, level, dungeonTag))
         : false;
   }

   public static boolean onDungeonBossDefeated(
      LevelAccessor world, Entity boss, Entity creditedSource, String dungeonTag, Collection<ServerPlayer> suppliedParticipants
   ) {
      if (!(
         world instanceof ServerLevel level
            && creditedSource instanceof ServerPlayer killer
            && boss != null
            && dungeonTag != null
            && !dungeonTag.isBlank()
            && suppliedParticipants != null
      )) {
         return false;
      } else {
         if (!ELIGIBLE_DUNGEONS.contains(level.dimension().location().getPath())) {
            return false;
         }

         if (killer.server.getLevel(CARTENON_DIMENSION) == null) {
            return false;
         }

         if (StoryModeIntroManager.isHandledStoryBoss(killer, boss)) {
            return true;
         }

         boolean storyIntro = StoryModeIntroManager.isStoryBoss(killer, boss);
         LinkedHashSet<ServerPlayer> uniqueParticipants = new LinkedHashSet<>();
         if (storyIntro) {
            uniqueParticipants.add(killer);
         } else {
            for (ServerPlayer participant : suppliedParticipants) {
               if (participant == null || participant.serverLevel() != level) {
                  return false;
               }

               uniqueParticipants.add(participant);
            }
         }

         List<ServerPlayer> participants = List.copyOf(uniqueParticipants);
         CartenonProgressSavedData progressData = CartenonProgressSavedData.get(level);
         if (!participants.isEmpty() && participants.contains(killer)) {
            List<ServerPlayer> invitationOwners = new ArrayList<>();
            if (storyIntro) {
               if (!progressData.isResolved(killer.getUUID())) {
                  invitationOwners.add(killer);
               }
            } else {
               for (ServerPlayer participant : participants) {
                  if (!SystemPlayerAccess.hasSystem(participant)
                     && !progressData.isResolved(participant.getUUID())
                     && progressData.recordDungeonClear(participant.getUUID(), dungeonTag)) {
                     invitationOwners.add(participant);
                  }
               }
            }

            if (!storyIntro
               && participants.stream().anyMatch(participantx -> SystemPlayerAccess.hasSystem(participantx) || progressData.isResolved(participantx.getUUID()))
               )
             {
               return false;
            }

            if (invitationOwners.isEmpty()) {
               return false;
            }

            int instanceId = progressData.allocateInstance();

            for (ServerPlayer owner : invitationOwners) {
               progressData.markGateOffered(owner.getUUID(), instanceId);
            }

            CartenonGateEntity gate = SololevelingModEntities.CARTENON_GATE.get().create(level);
            if (gate == null) {
               for (ServerPlayer owner : invitationOwners) {
                  progressData.cancelGateOffer(owner.getUUID());
               }

               return false;
            } else {
               LinkedHashSet<UUID> allowedPlayers = new LinkedHashSet<>();

               for (ServerPlayer participant : participants) {
                  allowedPlayers.add(participant.getUUID());
               }

               gate.configure(invitationOwners.get(0).getUUID(), allowedPlayers, instanceId);
               BlockPos gatePos = findGatePosition(level, boss.blockPosition());
               gate.moveTo(gatePos.getX() + 0.5, gatePos.getY(), gatePos.getZ() + 0.5, killer.getYRot() + 180.0F, 0.0F);
               if (!level.addFreshEntity(gate)) {
                  for (ServerPlayer owner : invitationOwners) {
                     progressData.cancelGateOffer(owner.getUUID());
                  }

                  return false;
               } else {
                  if (storyIntro) {
                     StoryModeIntroManager.onCartenonGateCreated(killer, gate, instanceId);
                  }

                  level.playSound((Player)null, gatePos, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.1F, 1.32F);
                  level.sendParticles(ParticleTypes.REVERSE_PORTAL, gate.getX(), gate.getY() + 1.2, gate.getZ(), 90, 0.9, 1.2, 0.35, 0.08);

                  for (ServerPlayer participant : participants) {
                     SystemNotifications.showTitleUnder(
                        participant,
                        -11893505,
                        110,
                        Component.literal("HIDDEN DUNGEON").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                        Component.literal("A strange gate has appeared.").withStyle(ChatFormatting.DARK_PURPLE)
                     );
                  }

                  return true;
               }
            }
         } else {
            return false;
         }
      }
   }

   public static void enterGate(ServerPlayer player, CartenonGateEntity gate) {
      if (player != null && gate != null && !gate.isRemoved() && !SystemPlayerAccess.hasSystem(player)) {
         if (!gate.isAllowed(player.getUUID())) {
            player.sendSystemMessage(Component.literal("This hidden gate does not respond to you.").withStyle(ChatFormatting.RED));
         } else {
            CartenonProgressSavedData progressData = CartenonProgressSavedData.get(player.serverLevel());
            if (progressData.isResolved(player.getUUID())) {
               player.sendSystemMessage(Component.literal("The System has already recorded your decision.").withStyle(ChatFormatting.DARK_GRAY));
            } else {
               int instanceId = Math.max(1, gate.getInstanceId());
               progressData.associateInstance(player.getUUID(), instanceId);
               queuePlayer(player.server, instanceId, player.getUUID());
               ServerLevel templeLevel = player.server.getLevel(CARTENON_DIMENSION);
               if (templeLevel == null) {
                  removeWaitingPlayer(player.server, instanceId, player.getUUID());
                  player.sendSystemMessage(Component.literal("The Cartenon Temple dimension is unavailable.").withStyle(ChatFormatting.RED));
               } else {
                  BlockPos origin = instanceOrigin(instanceId);
                  boolean completionMarker = templeLevel.getBlockState(origin.below(2)).is(Blocks.LODESTONE);
                  if (progressData.isInstanceBuilt(instanceId) && completionMarker) {
                     teleportWaitingPlayers(player.server, instanceId);
                  } else if (completionMarker) {
                     progressData.markInstanceBuilt(instanceId);
                     teleportWaitingPlayers(player.server, instanceId);
                  } else {
                     SystemNotifications.showTitleUnder(
                        player,
                        -12597505,
                        100,
                        Component.literal("CARTENON TEMPLE").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                        Component.literal("The hidden dungeon is stabilizing...").withStyle(ChatFormatting.GRAY)
                     );
                     if (!CartenonTempleGenerator.isBuildingAt(templeLevel, origin)) {
                        MinecraftServer server = player.server;
                        boolean started = CartenonTempleGenerator.startAt(
                           templeLevel, origin, Direction.SOUTH, player.getUUID(), player.getGameProfile().getName(), true, () -> {
                              CartenonProgressSavedData.get(templeLevel).markInstanceBuilt(instanceId);
                              teleportWaitingPlayers(server, instanceId);
                           }
                        );
                        if (!started && !CartenonTempleGenerator.isBuildingAt(templeLevel, origin)) {
                           removeWaitingPlayer(player.server, instanceId, player.getUUID());
                           player.sendSystemMessage(Component.literal("The hidden dungeon failed to stabilize.").withStyle(ChatFormatting.RED));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void resolveAwakeningChoice(ServerPlayer player, boolean accept) {
      if (player != null && player.getPersistentData().getBoolean("slr_cartenon_awakening_pending")) {
         if (player.serverLevel().dimension() == CARTENON_DIMENSION) {
            if (!StoryModeIntroManager.canResolveAwakening(player)) {
               player.getPersistentData().remove("slr_cartenon_awakening_pending");
               player.getPersistentData().remove("slr_cartenon_decline_ticks");
               restoreProtectionState(player);
               player.setHealth(Math.max(1.0F, player.getHealth()));
               sendAwakeningState(player, false);
            } else {
               player.getPersistentData().remove("slr_cartenon_awakening_pending");
               CartenonProgressSavedData.get(player.serverLevel()).resolve(player.getUUID(), accept);
               StoryModeIntroManager.onAwakeningResolved(player, accept);
               sendAwakeningState(player, false);
               if (!accept) {
                  player.getPersistentData().putInt("slr_cartenon_decline_ticks", 4);
                  freezePlayer(player);
               } else {
                  player.getPersistentData().remove("slr_cartenon_decline_ticks");
                  player.getPersistentData().remove("slr_cartenon_entry_protection");
                  restoreProtectionState(player);
                  player.setHealth(player.getMaxHealth());
                  player.fallDistance = 0.0F;
                  player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Player = true;
                     capability.syncPlayerVariables(player);
                  });
                  VesselProgressionManager.sync(player);
                  ServerLevel overworld = player.server.overworld();
                  BlockPos spawn = findSafeOverworldSpawn(overworld);
                  player.stopRiding();
                  player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, overworld.getSharedSpawnAngle(), 0.0F);
                  SystemNotifications.showTitleUnder(
                     player,
                     -12597505,
                     140,
                     Component.literal("WELCOME").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                     Component.literal("You have become a Player.").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)
                  );
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingDamage(LivingDamageEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && player.serverLevel().dimension() == CARTENON_DIMENSION) {
         if (!player.getPersistentData().getBoolean("slr_cartenon_death_bypass")
            && !CartenonProgressSavedData.get(player.serverLevel()).isResolved(player.getUUID())) {
            if (player.getPersistentData().getBoolean("slr_cartenon_awakening_pending") || player.getPersistentData().getInt("slr_cartenon_decline_ticks") > 0) {
               event.setCanceled(true);
            } else if (!(event.getAmount() + 0.001F < player.getHealth())) {
               event.setCanceled(true);
               if (StoryModeIntroManager.canTriggerAwakening(player, event.getSource())) {
                  beginAwakeningChoice(player);
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLivingDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && player.serverLevel().dimension() == CARTENON_DIMENSION) {
         if (!player.getPersistentData().getBoolean("slr_cartenon_death_bypass")
            && !CartenonProgressSavedData.get(player.serverLevel()).isResolved(player.getUUID())) {
            event.setCanceled(true);
            if (!StoryModeIntroManager.canTriggerAwakening(player, event.getSource())) {
               player.setHealth(Math.max(1.0F, player.getHealth()));
            } else {
               beginAwakeningChoice(player);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         int declineTicks = player.getPersistentData().getInt("slr_cartenon_decline_ticks");
         if (declineTicks > 0) {
            freezePlayer(player);
            if (--declineTicks > 0) {
               player.getPersistentData().putInt("slr_cartenon_decline_ticks", declineTicks);
            } else {
               player.getPersistentData().remove("slr_cartenon_decline_ticks");
               restoreProtectionState(player);
               player.getPersistentData().putBoolean("slr_cartenon_death_bypass", true);
               player.kill();
               SololevelingMod.queueServerWork(1, () -> player.getPersistentData().remove("slr_cartenon_death_bypass"));
            }
         } else if (player.getPersistentData().getBoolean("slr_cartenon_awakening_pending")) {
            freezePlayer(player);
            if (player.tickCount % 20 == 0) {
               sendAwakeningState(player, true);
            }
         } else {
            int protectionTicks = player.getPersistentData().getInt("slr_cartenon_entry_protection");
            if (protectionTicks > 0) {
               freezePlayer(player);
               if (protectionTicks > 1) {
                  player.getPersistentData().putInt("slr_cartenon_entry_protection", protectionTicks - 1);
               } else {
                  player.getPersistentData().remove("slr_cartenon_entry_protection");
                  restoreProtectionState(player);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (player.getPersistentData().getBoolean("slr_cartenon_awakening_pending")) {
            captureProtectionState(player);
            freezePlayer(player);
            sendAwakeningState(player, true);
         }
      }
   }

   private static void beginAwakeningChoice(ServerPlayer player) {
      if (!player.getPersistentData().getBoolean("slr_cartenon_awakening_pending")) {
         captureProtectionState(player);
         player.getPersistentData().putBoolean("slr_cartenon_awakening_pending", true);
         player.getPersistentData().remove("slr_cartenon_entry_protection");
         player.setHealth(Math.max(1.0F, player.getHealth()));
         freezePlayer(player);
         sendAwakeningState(player, true);
      }
   }

   private static void sendAwakeningState(ServerPlayer player, boolean open) {
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new CartenonAwakeningStateMessage(open));
   }

   private static void teleportWaitingPlayers(MinecraftServer server, int instanceId) {
      Map<Integer, LinkedHashSet<UUID>> serverQueue = WAITING_PLAYERS.get(server);
      if (serverQueue != null) {
         LinkedHashSet<UUID> waiting = serverQueue.remove(instanceId);
         if (waiting != null && !waiting.isEmpty()) {
            if (serverQueue.isEmpty()) {
               WAITING_PLAYERS.remove(server);
            }

            ServerLevel templeLevel = server.getLevel(CARTENON_DIMENSION);
            if (templeLevel != null) {
               int index = 0;

               for (UUID playerId : waiting) {
                  ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                  if (player != null) {
                     teleportToTemple(player, templeLevel, instanceId, index++);
                  }
               }
            }
         }
      }
   }

   private static void teleportToTemple(ServerPlayer player, ServerLevel templeLevel, int instanceId, int partyIndex) {
      BlockPos origin = instanceOrigin(instanceId);
      int lateralOffset = (partyIndex % 5 - 2) * 2;
      BlockPos entry = origin.relative(Direction.SOUTH, 8).relative(Direction.SOUTH.getClockWise(), lateralOffset).above();
      templeLevel.getChunk(entry);
      DungeonDimensionPlayerLeavesDimensionProcedure.completeAlternateExit(player);
      CartenonProgressSavedData.get(templeLevel).associateInstance(player.getUUID(), instanceId);
      player.getPersistentData().putInt("slr_cartenon_instance", instanceId);
      player.getPersistentData().remove("slr_cartenon_awakening_pending");
      player.getPersistentData().remove("slr_cartenon_decline_ticks");
      captureProtectionState(player);
      player.getPersistentData().putInt("slr_cartenon_entry_protection", 40);
      freezePlayer(player);
      player.stopRiding();
      player.teleportTo(templeLevel, entry.getX() + 0.5, entry.getY(), entry.getZ() + 0.5, 0.0F, 0.0F);
      StoryModeIntroManager.onPlayerEnteredTemple(player, templeLevel, instanceId);
      SystemNotifications.showTitleUnder(
         player,
         -10912001,
         120,
         Component.literal("HIDDEN DUNGEON").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD),
         Component.literal("Cartenon Temple").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
      );
   }

   private static List<ServerPlayer> dungeonParticipants(ServerPlayer killer, ServerLevel level, String dungeonTag) {
      SololevelingModVariables.PlayerVariables killerVars = killer.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      String party = killerVars.party == null ? "" : killerVars.party.trim();
      if (party.isEmpty()) {
         return List.of(killer);
      }

      List<ServerPlayer> participants = new ArrayList<>();

      for (ServerPlayer candidate : level.players()) {
         SololevelingModVariables.PlayerVariables candidateVars = candidate.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (party.equals(candidateVars.party) && dungeonTag.equals(candidate.getPersistentData().getString("dungeon_tag"))) {
            participants.add(candidate);
         }
      }

      if (!participants.contains(killer)) {
         participants.add(killer);
      }

      return participants;
   }

   private static BlockPos instanceOrigin(int instanceId) {
      int zeroBased = Math.max(0, instanceId - 1);
      int column = zeroBased % 32;
      int row = zeroBased / 32;
      return new BlockPos(column * 512, 64, row * 512);
   }

   private static BlockPos findGatePosition(ServerLevel level, BlockPos center) {
      int[][] offsets = new int[][]{{3, 0}, {-3, 0}, {0, 3}, {0, -3}, {4, 4}, {-4, 4}, {4, -4}, {-4, -4}, {0, 0}};

      for (int[] offset : offsets) {
         for (int dy = 3; dy >= -3; dy--) {
            BlockPos candidate = center.offset(offset[0], dy, offset[1]);
            if (isGateSpace(level, candidate)) {
               return candidate;
            }
         }
      }

      return center.above();
   }

   private static boolean isGateSpace(ServerLevel level, BlockPos pos) {
      return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
         && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
         && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
         && level.getBlockState(pos.above(2)).getCollisionShape(level, pos.above(2)).isEmpty();
   }

   private static BlockPos findSafeOverworldSpawn(ServerLevel overworld) {
      BlockPos shared = overworld.getSharedSpawnPos();

      for (int radius = 0; radius <= 8; radius++) {
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               if (radius <= 0 || Math.abs(dx) == radius || Math.abs(dz) == radius) {
                  for (int dy = 6; dy >= -4; dy--) {
                     BlockPos candidate = shared.offset(dx, dy, dz);
                     if (isSafePlayerSpace(overworld, candidate)) {
                        return candidate;
                     }
                  }
               }
            }
         }
      }

      return shared.above();
   }

   private static boolean isSafePlayerSpace(ServerLevel level, BlockPos pos) {
      BlockState below = level.getBlockState(pos.below());
      return below.isFaceSturdy(level, pos.below(), Direction.UP)
         && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
         && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
   }

   private static void queuePlayer(MinecraftServer server, int instanceId, UUID playerId) {
      WAITING_PLAYERS.computeIfAbsent(server, ignored -> new LinkedHashMap<>()).computeIfAbsent(instanceId, ignored -> new LinkedHashSet<>()).add(playerId);
   }

   private static void removeWaitingPlayer(MinecraftServer server, int instanceId, UUID playerId) {
      Map<Integer, LinkedHashSet<UUID>> serverQueue = WAITING_PLAYERS.get(server);
      if (serverQueue != null) {
         Collection<UUID> waiting = serverQueue.get(instanceId);
         if (waiting != null) {
            waiting.remove(playerId);
            if (waiting.isEmpty()) {
               serverQueue.remove(instanceId);
            }
         }

         if (serverQueue.isEmpty()) {
            WAITING_PLAYERS.remove(server);
         }
      }
   }

   private static void captureProtectionState(ServerPlayer player) {
      if (!player.getPersistentData().getBoolean("slr_cartenon_protection_state_saved")) {
         player.getPersistentData().putBoolean("slr_cartenon_protection_state_saved", true);
         player.getPersistentData().putBoolean("slr_cartenon_previous_invulnerable", player.isInvulnerable());
         player.getPersistentData().putBoolean("slr_cartenon_previous_no_gravity", player.isNoGravity());
      }
   }

   private static void restoreProtectionState(ServerPlayer player) {
      if (player.getPersistentData().getBoolean("slr_cartenon_protection_state_saved")) {
         player.setInvulnerable(player.getPersistentData().getBoolean("slr_cartenon_previous_invulnerable"));
         player.setNoGravity(player.getPersistentData().getBoolean("slr_cartenon_previous_no_gravity"));
      }

      player.getPersistentData().remove("slr_cartenon_protection_state_saved");
      player.getPersistentData().remove("slr_cartenon_previous_invulnerable");
      player.getPersistentData().remove("slr_cartenon_previous_no_gravity");
      player.setDeltaMovement(Vec3.ZERO);
      player.fallDistance = 0.0F;
   }

   private static void freezePlayer(ServerPlayer player) {
      captureProtectionState(player);
      player.setInvulnerable(true);
      player.setNoGravity(true);
      player.setDeltaMovement(Vec3.ZERO);
      player.fallDistance = 0.0F;
   }
}
