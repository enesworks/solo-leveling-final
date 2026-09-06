package dev.eness.sololevelingfinal.core.dungeon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.runtime.SnowRedGateArenaManager;
import dev.eness.sololevelingfinal.core.entity.Portal1Entity;
import dev.eness.sololevelingfinal.core.guild.GuildGateHelper;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.MagicReadingHelper;
import dev.eness.sololevelingfinal.core.util.PlayerEntryGenerationGuard;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.UrgentQuestManager;

public class ProceduralDungeonGateHandler {
   private static final String PROCEDURAL_GATE = "slr_procedural_gate";
   private static final String PROCEDURAL_RED = "slr_procedural_red_gate";
   private static final String RANK = "slr_procedural_rank";
   private static final String THEME = "slr_procedural_theme";
   private static final String COMPLEXITY = "slr_procedural_complexity";
   private static final String GENERATED = "slr_procedural_generated";
   private static final String START_X = "slr_procedural_start_x";
   private static final String START_Y = "slr_procedural_start_y";
   private static final String START_Z = "slr_procedural_start_z";
   private static final String PROCEDURAL_DUNGEON = "slr_procedural_dungeon";
   private static final String PREPARING = "slr_procedural_preparing";
   private static final String PREPARING_SINCE = "slr_procedural_preparing_since";
   private static final long STALE_PREPARATION_TICKS = 200L;
   private static final float RED_GATE_CHANCE = 0.02F;

   private ProceduralDungeonGateHandler() {
   }

   public static boolean isProceduralGate(Entity gate) {
      return gate instanceof Portal1Entity || gate != null && gate.getPersistentData().getBoolean("slr_procedural_gate");
   }

   public static boolean isProceduralRedGate(Entity gate) {
      return gate != null && gate.getPersistentData().getBoolean("slr_procedural_red_gate");
   }

   public static boolean isGenerated(Entity gate) {
      return isProceduralGate(gate) && gate.getPersistentData().getBoolean("slr_procedural_generated");
   }

   public static void enter(LevelAccessor world, double x, double y, double z, Entity gate, Entity sourceentity) {
      if (gate != null && sourceentity != null) {
         if (MagicReadingHelper.isHoldingMagicReader(sourceentity)) {
            showMagicReading(gate, sourceentity);
         } else if (sourceentity instanceof ServerPlayer player) {
            ensureProceduralMetadata(gate);
            recoverInterruptedBinding(player);
            if (isCompletedGate(world, gate, player.server)) {
               player.displayClientMessage(Component.literal("This gate has already been cleared."), true);
            } else if (!isProceduralRedGate(gate) || !isLocked(gate)) {
               if (isDungeonBound(player)) {
                  player.displayClientMessage(Component.literal("You are already bound to a dungeon."), true);
               } else if (!GuildGateHelper.prepareGateEntry(world, gate, sourceentity)) {
                  if (isProceduralRedGate(gate)) {
                     SnowRedGateArenaManager.enterProcedural(world, gate, player, nearbyPartyMembers(world, gate, player));
                  } else {
                     boolean turnsRed = !gate.getPersistentData().getBoolean("slr_procedural_generated") && shouldTurnRed(world, gate);
                     if (turnsRed) {
                        turnRed(world, gate);
                     }

                     List<ServerPlayer> entrants = turnsRed ? nearbyPartyMembers(world, gate, player) : List.of(player);
                     if (isProceduralRedGate(gate)) {
                        SnowRedGateArenaManager.enterProcedural(world, gate, player, entrants);
                     } else if (isPreparationActive(gate)) {
                        player.displayClientMessage(Component.literal("This gate is already stabilizing."), true);
                     } else {
                        markPreparing(gate);
                        Map<UUID, Long> entryGenerations = new LinkedHashMap<>();

                        try {
                           for (ServerPlayer entrant : entrants) {
                              prepareEntrant(world, x, y, z, gate, entrant);
                              entryGenerations.put(entrant.getUUID(), PlayerEntryGenerationGuard.begin(entrant));
                           }

                           teleportEntrants(gate, entrants, entryGenerations);
                        } catch (RuntimeException exception) {
                           clearPreparing(gate);
                           failEntrants(
                              gate.getStringUUID(),
                              entrants,
                              "The dungeon entry failed before it could stabilize.",
                              gate.level() instanceof ServerLevel sourceLevel ? sourceLevel : player.serverLevel()
                           );
                           SololevelingMod.LOGGER.error("Failed to begin procedural gate entry {}", gate.getStringUUID(), exception);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean shouldTurnRed(LevelAccessor world, Entity gate) {
      return rankFor(gate).numericRank >= ProceduralDungeonRank.B.numericRank
         && !SololevelingModVariables.MapVariables.get(world).RedGate
         && RandomSource.create().nextFloat() < 0.02F;
   }

   private static void ensureProceduralMetadata(Entity gate) {
      if (gate != null && !gate.level().isClientSide()) {
         CompoundTag data = gate.getPersistentData();
         data.putBoolean("slr_procedural_gate", true);
         if (data.getString("slr_procedural_rank").isBlank()) {
            data.putString("slr_procedural_rank", ProceduralDungeonRank.D.name());
         }

         if (data.getString("slr_procedural_theme").isBlank()) {
            DungeonTheme[] themes = DungeonTheme.values();
            int themeIndex = Math.floorMod(gate.getUUID().hashCode(), themes.length);
            data.putString("slr_procedural_theme", themes[themeIndex].name());
         }

         if (data.getInt("slr_procedural_complexity") <= 0) {
            data.putInt("slr_procedural_complexity", 3);
         }

         if (!data.contains("tpx") || !data.contains("tpy") || !data.contains("tpz")) {
            long seed = gate.getUUID().getMostSignificantBits() ^ gate.getUUID().getLeastSignificantBits();
            RandomSource random = RandomSource.create(seed);
            data.putDouble("tpx", random.nextInt(599999) - 299999);
            data.putDouble("tpy", 60 + random.nextInt(61));
            data.putDouble("tpz", random.nextInt(599999) - 299999);
         }
      }
   }

   private static boolean isLocked(Entity gate) {
      return gate instanceof Portal1Entity portal && portal.getEntityData().get(Portal1Entity.DATA_usedbefore);
   }

   private static boolean isCompletedGate(LevelAccessor world, Entity gate, MinecraftServer server) {
      String gateTag = gate.getStringUUID();
      return SololevelingModVariables.MapVariables.get(world).GatesCleared.contains(gateTag + ",")
         || ProceduralDungeonCompletionHandler.isUnscopedRunDecided(server, gateTag);
   }

   private static boolean isDungeonBound(ServerPlayer player) {
      return !player.getPersistentData().getString("slr_dungeon_instance").isBlank()
         ? true
         : player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.dungeoning).orElse(false);
   }

   private static void recoverInterruptedBinding(ServerPlayer player) {
      if (player != null
         && player.level().dimension() == Level.OVERWORLD
         && player.getPersistentData().getString("slr_dungeon_instance").isBlank()
         && player.getPersistentData().getBoolean("slr_procedural_dungeon")) {
         String oldGateId = player.getPersistentData().getString("dungeon_tag");
         Entity oldGate = findLoadedGate(player.server, oldGateId);
         if (oldGate == null || !isPreparationActive(oldGate)) {
            if (!oldGateId.isBlank()) {
               ProceduralDungeonCompletionHandler.removeUnscopedEntrant(player, oldGateId);
            }

            clearEntrantBinding(player);
         }
      }
   }

   private static Entity findLoadedGate(MinecraftServer server, String gateId) {
      if (server != null && gateId != null && !gateId.isBlank()) {
         try {
            UUID uuid = UUID.fromString(gateId);

            for (ServerLevel level : server.getAllLevels()) {
               Entity gate = level.getEntity(uuid);
               if (gate != null) {
                  return gate;
               }
            }
         } catch (IllegalArgumentException var6) {
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean isPreparationActive(Entity gate) {
      if (gate != null && gate.getPersistentData().getBoolean("slr_procedural_preparing")) {
         long since = gate.getPersistentData().getLong("slr_procedural_preparing_since");
         long age = Math.max(0L, gate.level().getGameTime() - since);
         if (age <= 200L) {
            return true;
         }

         clearPreparing(gate);
         return false;
      } else {
         return false;
      }
   }

   private static void markPreparing(Entity gate) {
      gate.getPersistentData().putBoolean("slr_procedural_preparing", true);
      gate.getPersistentData().putLong("slr_procedural_preparing_since", gate.level().getGameTime());
   }

   private static void clearPreparing(Entity gate) {
      if (gate != null) {
         gate.getPersistentData().remove("slr_procedural_preparing");
         gate.getPersistentData().remove("slr_procedural_preparing_since");
      }
   }

   private static void turnRed(LevelAccessor world, Entity gate) {
      SnowRedGateArenaManager.assignTerritoryIfMissing(gate);
      gate.getPersistentData().putBoolean("slr_procedural_red_gate", true);
      gate.getPersistentData().putBoolean("slr_is_red_gate", true);
      if (gate instanceof Portal1Entity portal) {
         portal.getEntityData().set(Portal1Entity.DATA_usedbefore, true);
         portal.setTexture("21");
      }

      SololevelingModVariables.MapVariables.get(world).RedGate = true;
      SololevelingModVariables.MapVariables.get(world).syncData(world);
   }

   private static List<ServerPlayer> nearbyPartyMembers(LevelAccessor world, Entity gate, ServerPlayer player) {
      String party = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).party;
      if (party.equals("")) {
         return List.of(player);
      }

      List<ServerPlayer> entrants = new ArrayList<>();
      entrants.add(player);

      for (Entity candidate : new ArrayList<>(world.players())) {
         if (candidate instanceof ServerPlayer partyMember && !partyMember.getUUID().equals(player.getUUID())) {
            String candidateParty = partyMember.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .party;
            if (party.equals(candidateParty) && partyMember.distanceTo(gate) <= 10.0F) {
               entrants.add(partyMember);
            }
         }
      }

      return entrants;
   }

   private static void prepareEntrant(LevelAccessor world, double x, double y, double z, Entity gate, ServerPlayer entrant) {
      dismissOwnedShadows(world, x, y, z, entrant);
      saveReturnPosition(entrant);
      entrant.getPersistentData().putString("dungeon_tag", gate.getStringUUID());
      entrant.getPersistentData().putBoolean("slr_procedural_dungeon", true);
      entrant.getPersistentData().putBoolean("slr_procedural_red_gate", isProceduralRedGate(gate));
      entrant.getPersistentData().remove("slr_dungeon_instance");
   }

   private static void teleportEntrants(Entity gate, List<ServerPlayer> entrants, Map<UUID, Long> entryGenerations) {
      ResourceKey<Level> destination = destinationFor(gate);
      ServerPlayer firstPlayer = entrants.stream().filter(entrantx -> entrantx != null && !entrantx.level().isClientSide()).findFirst().orElse(null);
      if (firstPlayer == null) {
         clearPreparing(gate);
      } else {
         ServerLevel sourceLevel = gate.level() instanceof ServerLevel level ? level : firstPlayer.serverLevel();
         ServerLevel nextLevel = firstPlayer.server.getLevel(destination);
         if (nextLevel == null) {
            failEntrants(gate.getStringUUID(), entrants, "The dungeon destination dimension is unavailable.", sourceLevel);
            clearPreparing(gate);
         } else {
            BlockPos targetPos = storedTarget(gate);
            if (gate.getPersistentData().getBoolean("slr_procedural_generated")) {
               ProceduralDungeonCompletionHandler.preserveLegacyUnscopedRoster(firstPlayer, gate.getStringUUID(), nextLevel.dimension());
            }

            for (ServerPlayer entrant : entrants) {
               ProceduralDungeonCompletionHandler.recordUnscopedEntrant(entrant, gate.getStringUUID(), nextLevel.dimension());
            }

            SololevelingMod.queueServerWork(5, () -> {
               String gateId = gate.getStringUUID();
               List<ServerPlayer> currentEntrants = List.of();

               try {
                  rollbackInvalidEntrants(gateId, entrants, entryGenerations);
                  currentEntrants = currentPreparedEntrants(gateId, entrants, entryGenerations);
                  if (currentEntrants.isEmpty()) {
                     return;
                  }

                  ServerPlayer currentFirst = currentEntrants.get(0);
                  if (gate.isRemoved()) {
                     failEntrants(gateId, currentEntrants, "The dungeon gate disappeared before entry finished.", sourceLevel);
                     return;
                  }

                  if (!isCompletedGate(gate.level(), gate, currentFirst.server)) {
                     nextLevel.getChunk(targetPos.getX() >> 4, targetPos.getZ() >> 4);
                     if (!gate.getPersistentData().getBoolean("slr_procedural_generated") || !hasStoredStart(gate)) {
                        ProceduralDungeonResult result = ProceduralDungeonGenerator.generate(nextLevel, targetPos, settingsFor(gate), currentFirst, false);
                        gate.getPersistentData().putBoolean("slr_procedural_generated", true);
                        gate.getPersistentData().putDouble("slr_procedural_start_x", result.startPos.getX() + 0.5);
                        gate.getPersistentData().putDouble("slr_procedural_start_y", result.startPos.getY());
                        gate.getPersistentData().putDouble("slr_procedural_start_z", result.startPos.getZ() + 0.5);
                        ProceduralDungeonCompletionHandler.recordUnscopedReturnAnchor(nextLevel, gateId, result.returnPortalPos);
                     }

                     double startX = gate.getPersistentData().getDouble("slr_procedural_start_x");
                     double startY = gate.getPersistentData().getDouble("slr_procedural_start_y");
                     double startZ = gate.getPersistentData().getDouble("slr_procedural_start_z");
                     BlockPos startPos = BlockPos.containing(startX, startY, startZ);
                     nextLevel.getChunk(startPos.getX() >> 4, startPos.getZ() >> 4);
                     ProceduralDungeonCompletionHandler.recordUnscopedReturnAnchor(nextLevel, gateId, BlockPos.containing(startX - 2.0, startY, startZ));
                     ProceduralDungeonCompletionHandler.discardMatchingReturnPortals(nextLevel, null, gateId);

                     for (ServerPlayer entrantxx : currentEntrants) {
                        entrantxx.setNoGravity(false);
                        entrantxx.setDeltaMovement(Vec3.ZERO);
                        entrantxx.fallDistance = 0.0F;
                        entrantxx.teleportTo(nextLevel, startX, startY, startZ, entrantxx.getYRot(), entrantxx.getXRot());
                        entrantxx.setNoGravity(false);
                        entrantxx.setDeltaMovement(Vec3.ZERO);
                        entrantxx.fallDistance = 0.0F;
                     }

                     for (ServerPlayer entrantx : currentEntrants) {
                        UrgentQuestManager.markDungeonId(entrantx, "procedural");
                     }

                     return;
                  }

                  failEntrants(gateId, currentEntrants, "This gate was cleared before entry finished.", sourceLevel);
               } catch (RuntimeException exception) {
                  if (currentEntrants.isEmpty()) {
                     currentEntrants = currentPreparedEntrants(gateId, entrants, entryGenerations);
                  }

                  if (!currentEntrants.isEmpty()) {
                     failEntrants(gateId, currentEntrants, "The dungeon could not finish generating. You were returned safely.", sourceLevel);
                  }

                  SololevelingMod.LOGGER.error("Procedural gate {} ({}) failed at target {}", gateId, rankFor(gate), targetPos, exception);
                  return;
               } finally {
                  clearPreparing(gate);
               }
            });
         }
      }
   }

   private static List<ServerPlayer> currentPreparedEntrants(String gateId, List<ServerPlayer> entrants, Map<UUID, Long> entryGenerations) {
      return entrants.stream().filter(entrant -> {
         if (entrant != null && !entrant.hasDisconnected()) {
            Long generation = entryGenerations.get(entrant.getUUID());
            return generation != null && PlayerEntryGenerationGuard.isCurrent(entrant, generation) && hasPreparedBinding(entrant, gateId);
         } else {
            return false;
         }
      }).toList();
   }

   private static void rollbackInvalidEntrants(String gateId, List<ServerPlayer> entrants, Map<UUID, Long> entryGenerations) {
      for (ServerPlayer entrant : entrants) {
         Long generation = entryGenerations.get(entrant.getUUID());
         boolean current = generation != null && PlayerEntryGenerationGuard.isCurrent(entrant, generation) && hasPreparedBinding(entrant, gateId);
         if (!current && !hasPreparedBinding(entrant, gateId)) {
            ProceduralDungeonCompletionHandler.removeUnscopedEntrant(entrant, gateId);
         }
      }
   }

   private static boolean hasPreparedBinding(ServerPlayer entrant, String gateId) {
      return entrant != null
         && gateId.equals(entrant.getPersistentData().getString("dungeon_tag"))
         && entrant.getPersistentData().getBoolean("slr_procedural_dungeon")
         && entrant.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(capability -> capability.dungeoning).orElse(false);
   }

   private static boolean hasStoredStart(Entity gate) {
      return gate != null
         && gate.getPersistentData().contains("slr_procedural_start_x")
         && gate.getPersistentData().contains("slr_procedural_start_y")
         && gate.getPersistentData().contains("slr_procedural_start_z")
         && Double.isFinite(gate.getPersistentData().getDouble("slr_procedural_start_x"))
         && Double.isFinite(gate.getPersistentData().getDouble("slr_procedural_start_y"))
         && Double.isFinite(gate.getPersistentData().getDouble("slr_procedural_start_z"));
   }

   private static void failEntrants(String gateId, List<ServerPlayer> entrants, String message, ServerLevel sourceLevel) {
      for (ServerPlayer entrant : entrants) {
         if (entrant != null) {
            ProceduralDungeonCompletionHandler.removeUnscopedEntrant(entrant, gateId);
            entrant.setNoGravity(false);
            entrant.setDeltaMovement(Vec3.ZERO);
            entrant.fallDistance = 0.0F;
            SololevelingModVariables.PlayerVariables variables = entrant.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
            if (sourceLevel != null
               && entrant.serverLevel() != sourceLevel
               && variables != null
               && Double.isFinite(variables.DunX)
               && Double.isFinite(variables.DunY)
               && Double.isFinite(variables.DunZ)) {
               try {
                  entrant.teleportTo(sourceLevel, variables.DunX, variables.DunY, variables.DunZ, entrant.getYRot(), entrant.getXRot());
               } catch (RuntimeException exception) {
                  SololevelingMod.LOGGER.error("Could not return {} after procedural gate {} failed", entrant.getGameProfile().getName(), gateId, exception);
               }
            }

            clearEntrantBinding(entrant);
            if (!entrant.hasDisconnected()) {
               entrant.sendSystemMessage(Component.literal(message));
            }
         }
      }
   }

   private static void clearEntrantBinding(ServerPlayer entrant) {
      entrant.setNoGravity(false);
      entrant.setDeltaMovement(Vec3.ZERO);
      entrant.fallDistance = 0.0F;
      entrant.getPersistentData().putBoolean("slr_procedural_dungeon", false);
      entrant.getPersistentData().putBoolean("slr_procedural_red_gate", false);
      entrant.getPersistentData().remove("dungeon_tag");
      entrant.getPersistentData().remove("slr_dungeon_instance");
      entrant.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.dungeoning = false;
         capability.BossKilled = false;
         capability.syncPlayerVariables(entrant);
      });
   }

   private static void showMagicReading(Entity gate, Entity sourceentity) {
      if (sourceentity instanceof Player player && !player.level().isClientSide()) {
         if (isProceduralRedGate(gate)) {
            MagicReadingHelper.showUnreadableReading(player);
         } else {
            MagicReadingHelper.showRankReading(player, rankFor(gate));
         }
      }
   }

   private static void dismissOwnedShadows(LevelAccessor world, double x, double y, double z, Entity sourceentity) {
      if (sourceentity instanceof ServerPlayer owner) {
         ShadowMonarchManager.dismissLoadedOwnedShadows(owner, owner.level().dimension());
      }
   }

   private static void saveReturnPosition(Entity entity) {
      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.DunX = entity.getX();
         capability.DunY = entity.getY();
         capability.DunZ = entity.getZ();
         capability.dungeoning = true;
         capability.BossKilled = false;
         capability.syncPlayerVariables(entity);
      });
   }

   private static BlockPos storedTarget(Entity gate) {
      return BlockPos.containing(
         gate.getPersistentData().getDouble("tpx"), gate.getPersistentData().getDouble("tpy"), gate.getPersistentData().getDouble("tpz")
      );
   }

   private static ProceduralDungeonSettings settingsFor(Entity gate) {
      return new ProceduralDungeonSettings(
         rankFor(gate),
         DungeonTheme.fromString(gate.getPersistentData().getString("slr_procedural_theme")),
         gate.getPersistentData().getInt("slr_procedural_complexity")
      );
   }

   public static ProceduralDungeonRank rankFor(Entity gate) {
      return ProceduralDungeonRank.fromString(gate.getPersistentData().getString("slr_procedural_rank"));
   }

   private static ResourceKey<Level> destinationFor(Entity gate) {
      return switch (rankFor(gate)) {
         case E, D -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_d"));
         case C -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_c"));
         case B -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_b"));
         case A -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_a"));
         case S -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_s"));
      };
   }
}
