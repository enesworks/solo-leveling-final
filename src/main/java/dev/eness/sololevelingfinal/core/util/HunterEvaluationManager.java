package dev.eness.sololevelingfinal.core.util;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.block.entity.HunterRankEvaluatorTileEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;
import dev.eness.sololevelingfinal.core.item.HunterIDItem;
import dev.eness.sololevelingfinal.core.network.HunterEvaluationStateMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import org.joml.Vector3f;

@EventBusSubscriber(modid = "sololeveling")
public final class HunterEvaluationManager {
   private static final int SCHEMA = 2;
   private static final String ROOT_KEY = "slr_evaluation";
   private static final String SESSION_KEY = "Session";
   private static final String INITIAL_COMPLETE = "InitialComplete";
   private static final String LEGACY_MIGRATED = "LegacyMigrated";
   private static final double MAX_DISTANCE_SQUARED = 64.0;

   private HunterEvaluationManager() {
   }

   public static void openEvaluator(ServerPlayer player, BlockPos evaluatorPos) {
      if (player != null && evaluatorPos != null && player.level() instanceof ServerLevel level && isEvaluator(level.getBlockState(evaluatorPos).getBlock())) {
         CompoundTag data = evaluationData(player);
         migrateLegacy(player, data);
         HunterEvaluationManager.Session session = readSession(data);
         long now = level.getGameTime();
         if (session == null) {
            session = createSession(player, data, now);
            writeSession(data, session);
         }

         session.stationDimension = level.dimension().location().toString();
         session.stationPos = evaluatorPos.asLong();
         session.hasStation = true;
         if (session.deadline == 0L && session.pausedTicks > 0) {
            session.deadline = now + session.pausedTicks;
            session.pausedTicks = 0;
         }

         writeSession(data, session);
         activatePublicPulse(level, evaluatorPos, session, now, false);
         level.playSound((Player)null, evaluatorPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.75F, 1.05F);
         sendState(player, session, true);
      }
   }

   public static void handleAction(ServerPlayer player, UUID sessionId, HunterEvaluationRules.Action action) {
      if (player != null && sessionId != null && action != null && player.level() instanceof ServerLevel level) {
         CompoundTag data = evaluationData(player);
         HunterEvaluationManager.Session session = readSession(data);
         if (session != null && session.id.equals(sessionId) && isNearStation(player, session)) {
            long now = level.getGameTime();

            boolean changed = switch (action) {
               case BEGIN_CONTACT -> beginContact(session, now);
               case CANCEL_CONTACT -> cancelContact(session);
               case REROLL_CLASS -> reroll(player, session, now);
               case ACCEPT_RESULT -> accept(player, data, session, now);
               case ACKNOWLEDGE -> acknowledge(player, data, session);
            };
            if (changed) {
               if (action != HunterEvaluationRules.Action.ACKNOWLEDGE) {
                  writeSession(data, session);
                  BlockPos pos = session.station();
                  activatePublicPulse(level, pos, session, now, true);
                  sendState(player, session, false);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         CompoundTag data = evaluationDataIfPresent(player);
         if (data != null) {
            HunterEvaluationManager.Session session = readSession(data);
            if (session != null) {
               ServerLevel level = player.serverLevel();
               long now = level.getGameTime();
               if (!isNearStation(player, session)) {
                  if (session.hasStation) {
                     pauseAwayFromStation(session, now);
                     writeSession(data, session);
                     sendClosed(player);
                  }
               } else {
                  boolean advanced = false;

                  for (int guard = 0; guard < 8 && session.deadline > 0L && now >= session.deadline; guard++) {
                     advance(player, session, now);
                     advanced = true;
                  }

                  if (advanced) {
                     writeSession(data, session);
                     activatePublicPulse(level, session.station(), session, now, true);
                     sendState(player, session, false);
                  }

                  emitPublicParticles(level, session, now);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CompoundTag data = evaluationDataIfPresent(player);
         if (data != null) {
            HunterEvaluationManager.Session session = readSession(data);
            if (session != null && session.hasStation) {
               pauseAwayFromStation(session, player.serverLevel().getGameTime());
               writeSession(data, session);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onClone(Clone event) {
      if (event.getEntity() instanceof ServerPlayer replacement) {
         CompoundTag originalRoot = event.getOriginal().getPersistentData();
         if (originalRoot.contains("PlayerPersisted", 10)) {
            CompoundTag originalPersisted = originalRoot.getCompound("PlayerPersisted");
            if (originalPersisted.contains("slr_evaluation", 10)) {
               persisted(replacement).put("slr_evaluation", originalPersisted.getCompound("slr_evaluation").copy());
            }
         }
      }
   }

   private static HunterEvaluationManager.Session createSession(ServerPlayer player, CompoundTag data, long now) {
      SololevelingModVariables.PlayerVariables variables = variables(player);
      boolean initialComplete = data.getBoolean("InitialComplete");
      int currentClass = boundedClass(variables.Classes);
      int earnedRank = boundedRank(variables.HunterRank);
      int certifiedRank = boundedRank(variables.prevRank);
      if (initialComplete && currentClass > 0 && earnedRank > 0) {
         int result = resolvedEvaluationRank(player, variables, earnedRank);
         return HunterEvaluationManager.Session.create(
            HunterEvaluationRules.Mode.REEVALUATION, HunterEvaluationRules.Phase.CONTACT, currentClass, result, certifiedRank, 63, true, now, 0
         );
      }

      boolean fixedClass = currentClass > 0;
      int candidateRank = earnedRank > 0 ? earnedRank : HunterRankOdds.roll(player.serverLevel(), player.getRandom().nextInt(100) + 1);
      int resultRank = resolvedEvaluationRank(player, variables, candidateRank);
      int remainingMask = 63;
      if (currentClass == 0) {
         HunterEvaluationRules.ClassDraw draw = HunterEvaluationRules.drawClass(remainingMask, 0, player.getRandom().nextInt());
         currentClass = draw.classId();
         remainingMask = draw.remainingMask();
      } else {
         remainingMask &= ~(1 << currentClass - 1);
      }

      return HunterEvaluationManager.Session.create(
         HunterEvaluationRules.Mode.INITIAL, HunterEvaluationRules.Phase.BOOT, currentClass, resultRank, certifiedRank, remainingMask, fixedClass, now, 40
      );
   }

   private static boolean beginContact(HunterEvaluationManager.Session session, long now) {
      if (session.phase == HunterEvaluationRules.Phase.CONTACT && session.deadline <= 0L) {
         session.phaseStarted = now;
         session.phaseDuration = 30;
         session.deadline = now + 30L;
         session.pausedTicks = 0;
         return true;
      } else {
         return false;
      }
   }

   private static boolean cancelContact(HunterEvaluationManager.Session session) {
      if (session.phase == HunterEvaluationRules.Phase.CONTACT && session.deadline > 0L) {
         session.phaseStarted = 0L;
         session.phaseDuration = 0;
         session.deadline = 0L;
         session.pausedTicks = 0;
         return true;
      } else {
         return false;
      }
   }

   private static boolean reroll(ServerPlayer player, HunterEvaluationManager.Session session, long now) {
      if (session.mode == HunterEvaluationRules.Mode.INITIAL && session.phase == HunterEvaluationRules.Phase.DECISION && !session.fixedClass) {
         HunterEvaluationRules.ClassDraw draw = HunterEvaluationRules.drawClass(session.remainingClassMask, session.classId, player.getRandom().nextInt());
         session.classId = draw.classId();
         session.remainingClassMask = draw.remainingMask();
         startPhase(session, HunterEvaluationRules.Phase.REROLL, now);
         return true;
      } else {
         return false;
      }
   }

   private static boolean accept(ServerPlayer player, CompoundTag data, HunterEvaluationManager.Session session, long now) {
      if (session.phase != HunterEvaluationRules.Phase.DECISION) {
         return false;
      }

      if (session.mode == HunterEvaluationRules.Mode.INITIAL) {
         commitInitial(player, data, session);
      } else {
         commitReevaluation(player, session);
      }

      startPhase(session, HunterEvaluationRules.Phase.COMPLETE, now);
      return true;
   }

   private static boolean acknowledge(ServerPlayer player, CompoundTag data, HunterEvaluationManager.Session session) {
      if (session.phase != HunterEvaluationRules.Phase.COMPLETE) {
         return false;
      }

      data.remove("Session");
      sendClosed(player);
      return true;
   }

   private static void commitInitial(ServerPlayer player, CompoundTag data, HunterEvaluationManager.Session session) {
      int selectedClass = boundedClass(session.classId);
      int resultRank = resolvedEvaluationRank(player, variables(player), session.rank);
      session.rank = resultRank;
      if (selectedClass != 0 && resultRank != 0) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Classes = selectedClass;
            capability.HunterRank = resultRank;
            capability.prevRank = resultRank;
            capability.prevLevel = capability.Level;
            capability.ranking = HunterEvaluationRules.rankName(resultRank);
            capability.statshown = 0.0;
            capability.sl_EVA = 0.0;
            capability.syncPlayerVariables(player);
         });
         data.putBoolean("InitialComplete", true);
         HunterEvaluationRewardService.applyInitialRewards(player, selectedClass, resultRank, data);
         HunterIDItem.refreshAll(player);
         player.displayClientMessage(
            Component.literal(
               "Hunter Association registration complete: "
                  + HunterEvaluationRules.rankName(resultRank)
                  + "-Rank "
                  + HunterEvaluationRules.className(selectedClass)
                  + "."
            ),
            false
         );
      }
   }

   private static void commitReevaluation(ServerPlayer player, HunterEvaluationManager.Session session) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         int earned = boundedRank(capability.HunterRank);
         int result = resolvedEvaluationRank(player, capability, earned);
         capability.HunterRank = result;
         capability.prevRank = result;
         capability.prevLevel = capability.Level;
         capability.ranking = HunterEvaluationRules.rankName(result);
         capability.syncPlayerVariables(player);
         session.rank = result;
      });
      HunterIDItem.refreshAll(player);
      String message = session.rank > session.previousRank
         ? "Reevaluation complete. Your Hunter ID is now certified " + HunterEvaluationRules.rankName(session.rank) + "-Rank."
         : "Reevaluation complete. Your certified rank is unchanged.";
      player.displayClientMessage(Component.literal(message), false);
   }

   private static void advance(ServerPlayer player, HunterEvaluationManager.Session session, long now) {
      switch (session.phase) {
         case BOOT:
            startPhase(session, HunterEvaluationRules.Phase.CONTACT, now);
            break;
         case CONTACT:
            startPhase(session, HunterEvaluationRules.Phase.SCAN, now);
            break;
         case SCAN:
            if (session.mode == HunterEvaluationRules.Mode.INITIAL) {
               startPhase(session, HunterEvaluationRules.Phase.CLASS_REVEAL, now);
            } else {
               refreshReevaluationResult(player, session);
               startPhase(session, HunterEvaluationRules.Phase.RANK_REVEAL, now);
            }
            break;
         case CLASS_REVEAL:
            startPhase(session, HunterEvaluationRules.Phase.RANK_REVEAL, now);
            break;
         case RANK_REVEAL:
            startPhase(
               session, session.mode == HunterEvaluationRules.Mode.INITIAL ? HunterEvaluationRules.Phase.SETTLE : HunterEvaluationRules.Phase.DECISION, now
            );
            break;
         case SETTLE:
         case REROLL:
            startPhase(session, HunterEvaluationRules.Phase.DECISION, now);
            break;
         default:
            session.deadline = 0L;
            session.phaseDuration = 0;
      }
   }

   private static void refreshReevaluationResult(ServerPlayer player, HunterEvaluationManager.Session session) {
      SololevelingModVariables.PlayerVariables capability = variables(player);
      session.rank = resolvedEvaluationRank(player, capability, boundedRank(capability.HunterRank));
   }

   private static int resolvedEvaluationRank(ServerPlayer player, SololevelingModVariables.PlayerVariables capability, int candidateRank) {
      return HunterEvaluationRules.resolvedEvaluationRank(
         candidateRank, boundedRank(capability.prevRank), Math.max(0, (int)Math.floor(capability.Level)), VesselManager.currentDefinition(player) != null
      );
   }

   private static void startPhase(HunterEvaluationManager.Session session, HunterEvaluationRules.Phase phase, long now) {
      session.phase = phase;
      session.phaseStarted = now;
      session.phaseDuration = phase == HunterEvaluationRules.Phase.CONTACT
         ? 0
         : (
            phase == HunterEvaluationRules.Phase.RANK_REVEAL
               ? HunterEvaluationRules.rankRevealDuration(session.rank)
               : HunterEvaluationRules.phaseDuration(session.mode, phase)
         );
      session.deadline = session.phaseDuration > 0 ? now + session.phaseDuration : 0L;
      session.pausedTicks = 0;
   }

   private static void pauseAwayFromStation(HunterEvaluationManager.Session session, long now) {
      if (session.phase == HunterEvaluationRules.Phase.CONTACT) {
         session.deadline = 0L;
         session.phaseDuration = 0;
         session.pausedTicks = 0;
      } else if (session.deadline > now) {
         session.pausedTicks = (int)Math.min(2147483647L, session.deadline - now);
         session.deadline = 0L;
      }

      session.hasStation = false;
      session.stationDimension = "";
      session.stationPos = 0L;
   }

   private static void activatePublicPulse(ServerLevel level, BlockPos pos, HunterEvaluationManager.Session session, long now, boolean playTransitionSound) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      long until = session.deadline > now ? session.deadline : now + 40L;
      if (blockEntity instanceof HunterRankEvaluatorTileEntity evaluator) {
         evaluator.setPublicPulse(session.id, pulseColor(session), pulseIntensity(session), session.phase.ordinal(), until);
      }

      if (playTransitionSound) {
         SoundEvent sound = switch (session.phase) {
            case CONTACT -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case SCAN -> SoundEvents.BEACON_ACTIVATE;
            case CLASS_REVEAL, REROLL -> SoundEvents.ENCHANTMENT_TABLE_USE;
            case RANK_REVEAL -> session.rank == 6 ? SoundEvents.WITHER_SPAWN : SoundEvents.PLAYER_LEVELUP;
            default -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case COMPLETE -> SoundEvents.TOTEM_USE;
         };
         level.playSound(
            (Player)null,
            pos,
            sound,
            SoundSource.BLOCKS,
            session.rank == 6 && session.phase == HunterEvaluationRules.Phase.RANK_REVEAL ? 0.8F : 0.65F,
            phasePitch(session)
         );
         spawnPulseBurst(level, pos, session);
      }
   }

   private static void emitPublicParticles(ServerLevel level, HunterEvaluationManager.Session session, long now) {
      if (now % 3L == 0L) {
         if (level.getBlockEntity(session.station()) instanceof HunterRankEvaluatorTileEntity evaluator && evaluator.isPublicPulseOwner(session.id, now)) {
            DustParticleOptions dust = dust(session, 1.0F);
            BlockPos pos = session.station();
            level.sendParticles(dust, pos.getX() + 0.5, pos.getY() + 1.55, pos.getZ() + 0.5, 2, 0.3, 0.34, 0.3, 0.008);
            if (session.rank == 6 && HunterEvaluationRules.revealsRank(session.phase) && now % 6L == 0L) {
               level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.55, pos.getZ() + 0.5, 1, 0.16, 0.22, 0.16, 0.004);
            }
         }
      }
   }

   private static void spawnPulseBurst(ServerLevel level, BlockPos pos, HunterEvaluationManager.Session session) {
      level.sendParticles(dust(session, 1.35F), pos.getX() + 0.5, pos.getY() + 1.55, pos.getZ() + 0.5, 28, 0.46, 0.5, 0.46, 0.025);
      if (session.rank == 6 && HunterEvaluationRules.revealsRank(session.phase)) {
         level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.55, pos.getZ() + 0.5, 16, 0.34, 0.42, 0.34, 0.02);
      }
   }

   private static DustParticleOptions dust(HunterEvaluationManager.Session session, float scale) {
      int rgb = pulseColor(session);
      float intensity = pulseIntensity(session);
      float red = (rgb >> 16 & 0xFF) / 255.0F;
      float green = (rgb >> 8 & 0xFF) / 255.0F;
      float blue = (rgb & 0xFF) / 255.0F;
      if (session.rank == 6 && HunterEvaluationRules.revealsRank(session.phase)) {
         red = Math.min(1.0F, red * 0.7F + 0.55F);
         green = Math.min(1.0F, green * 0.7F + 0.55F);
         blue = Math.min(1.0F, blue * 0.7F + 0.55F);
      } else {
         red = Math.max(0.05F, red * intensity);
         green = Math.max(0.05F, green * intensity);
         blue = Math.max(0.05F, blue * intensity);
      }

      return new DustParticleOptions(new Vector3f(red, green, blue), scale);
   }

   private static int pulseColor(HunterEvaluationManager.Session session) {
      return HunterEvaluationRules.revealsClass(session.mode, session.phase)
         ? HunterEvaluationRules.classColor(session.classId)
         : HunterEvaluationRules.classColor(0);
   }

   private static float pulseIntensity(HunterEvaluationManager.Session session) {
      return HunterEvaluationRules.revealsRank(session.phase) ? HunterEvaluationRules.rankIntensity(session.rank) : 0.48F;
   }

   private static float phasePitch(HunterEvaluationManager.Session session) {
      if (session.phase == HunterEvaluationRules.Phase.RANK_REVEAL) {
         return 0.72F + session.rank * 0.08F;
      } else {
         return session.phase == HunterEvaluationRules.Phase.COMPLETE ? 1.18F : 1.0F;
      }
   }

   private static void sendState(ServerPlayer player, HunterEvaluationManager.Session session, boolean forceOpen) {
      long now = player.serverLevel().getGameTime();
      int remaining = session.deadline > now ? (int)Math.min(2147483647L, session.deadline - now) : session.pausedTicks;
      boolean revealClass = HunterEvaluationRules.revealsClass(session.mode, session.phase);
      boolean revealRank = HunterEvaluationRules.revealsRank(session.phase);
      boolean canReroll = session.mode == HunterEvaluationRules.Mode.INITIAL && session.phase == HunterEvaluationRules.Phase.DECISION && !session.fixedClass;
      SololevelingMod.PACKET_HANDLER
         .send(
            PacketDistributor.PLAYER.with(() -> player),
            new HunterEvaluationStateMessage(
               true,
               forceOpen,
               session.id,
               session.mode.ordinal(),
               session.phase.ordinal(),
               revealClass ? session.classId : 0,
               revealRank ? session.rank : 0,
               session.previousRank,
               session.phaseDuration,
               remaining,
               canReroll,
               session.fixedClass
            )
         );
   }

   private static void sendClosed(ServerPlayer player) {
      SololevelingMod.PACKET_HANDLER
         .send(PacketDistributor.PLAYER.with(() -> player), new HunterEvaluationStateMessage(false, false, new UUID(0L, 0L), 0, 0, 0, 0, 0, 0, 0, false, false));
   }

   private static boolean isNearStation(ServerPlayer player, HunterEvaluationManager.Session session) {
      if (session.hasStation && player.level().dimension().location().toString().equals(session.stationDimension)) {
         BlockPos pos = session.station();
         if (!isEvaluator(player.level().getBlockState(pos).getBlock())) {
            return false;
         }

         double dx = player.getX() - (pos.getX() + 0.5);
         double dy = player.getY() - (pos.getY() + 0.5);
         double dz = player.getZ() - (pos.getZ() + 0.5);
         return dx * dx + dy * dy + dz * dz <= 64.0;
      } else {
         return false;
      }
   }

   private static boolean isEvaluator(Block block) {
      return block == SololevelingModBlocks.HUNTER_RANK_EVALUATOR.get() || block == SololevelingModBlocks.EVALUATOR_TEST.get();
   }

   private static void migrateLegacy(ServerPlayer player, CompoundTag data) {
      if (data.getInt("Schema") < 2 || !data.getBoolean("LegacyMigrated")) {
         SololevelingModVariables.PlayerVariables capability = variables(player);
         int classId = boundedClass(capability.Classes);
         int rank = boundedRank(capability.HunterRank);
         boolean hasClass = classId > 0;
         boolean hasRank = rank > 0;
         boolean legacyMidCeremony = capability.sl_EVA >= 10.0 && capability.sl_EVA < 130.0 && hasClass && hasRank;
         if (legacyMidCeremony) {
            int mask = 63 & ~(1 << classId - 1);
            HunterEvaluationManager.Session session = HunterEvaluationManager.Session.create(
               HunterEvaluationRules.Mode.INITIAL,
               HunterEvaluationRules.Phase.DECISION,
               classId,
               rank,
               boundedRank(capability.prevRank),
               mask,
               false,
               player.serverLevel().getGameTime(),
               0
            );
            writeSession(data, session);
            data.putBoolean("InitialComplete", false);
         } else if (hasClass && hasRank) {
            data.putBoolean("InitialComplete", true);
            HunterEvaluationRewardService.markLegacyRewardsApplied(data);
            if (capability.prevRank <= 0.0) {
               capability.prevRank = rank;
            }
         }

         capability.sl_EVA = 0.0;
         capability.statshown = 0.0;
         capability.syncPlayerVariables(player);
         data.putInt("Schema", 2);
         data.putBoolean("LegacyMigrated", true);
      }
   }

   private static CompoundTag evaluationData(ServerPlayer player) {
      CompoundTag persisted = persisted(player);
      if (!persisted.contains("slr_evaluation", 10)) {
         persisted.put("slr_evaluation", new CompoundTag());
      }

      return persisted.getCompound("slr_evaluation");
   }

   private static CompoundTag evaluationDataIfPresent(ServerPlayer player) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         return null;
      }

      CompoundTag persisted = root.getCompound("PlayerPersisted");
      return persisted.contains("slr_evaluation", 10) ? persisted.getCompound("slr_evaluation") : null;
   }

   private static CompoundTag persisted(ServerPlayer player) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         root.put("PlayerPersisted", new CompoundTag());
      }

      return root.getCompound("PlayerPersisted");
   }

   private static HunterEvaluationManager.Session readSession(CompoundTag data) {
      return data.contains("Session", 10) ? HunterEvaluationManager.Session.load(data.getCompound("Session")) : null;
   }

   private static void writeSession(CompoundTag data, HunterEvaluationManager.Session session) {
      data.put("Session", session.save());
      data.putInt("Schema", 2);
      data.putBoolean("LegacyMigrated", true);
   }

   private static SololevelingModVariables.PlayerVariables variables(ServerPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static int boundedClass(double value) {
      int classId = (int)Math.round(value);
      return classId >= 1 && classId <= 6 ? classId : 0;
   }

   private static int boundedRank(double value) {
      int rank = (int)Math.round(value);
      return rank >= 1 && rank <= 6 ? rank : 0;
   }

   private static final class Session {
      private UUID id;
      private HunterEvaluationRules.Mode mode;
      private HunterEvaluationRules.Phase phase;
      private int classId;
      private int rank;
      private int previousRank;
      private int remainingClassMask;
      private boolean fixedClass;
      private long phaseStarted;
      private long deadline;
      private int phaseDuration;
      private int pausedTicks;
      private boolean hasStation;
      private String stationDimension = "";
      private long stationPos;

      private static HunterEvaluationManager.Session create(
         HunterEvaluationRules.Mode mode,
         HunterEvaluationRules.Phase phase,
         int classId,
         int rank,
         int previousRank,
         int remainingClassMask,
         boolean fixedClass,
         long now,
         int duration
      ) {
         HunterEvaluationManager.Session session = new HunterEvaluationManager.Session();
         session.id = UUID.randomUUID();
         session.mode = mode;
         session.phase = phase;
         session.classId = classId;
         session.rank = rank;
         session.previousRank = previousRank;
         session.remainingClassMask = remainingClassMask;
         session.fixedClass = fixedClass;
         session.phaseStarted = now;
         session.phaseDuration = Math.max(0, duration);
         session.deadline = duration > 0 ? now + duration : 0L;
         return session;
      }

      private static HunterEvaluationManager.Session load(CompoundTag tag) {
         if (!tag.hasUUID("Id")) {
            return null;
         }

         HunterEvaluationManager.Session session = new HunterEvaluationManager.Session();
         session.id = tag.getUUID("Id");
         session.mode = HunterEvaluationRules.Mode.fromId(tag.getInt("Mode"));
         session.phase = HunterEvaluationRules.Phase.fromId(tag.getInt("Phase"));
         session.classId = HunterEvaluationManager.boundedClass(tag.getInt("Class"));
         session.rank = HunterEvaluationManager.boundedRank(tag.getInt("Rank"));
         session.previousRank = HunterEvaluationManager.boundedRank(tag.getInt("PreviousRank"));
         session.remainingClassMask = tag.getInt("RemainingClassMask") & 63;
         session.fixedClass = tag.getBoolean("FixedClass");
         session.phaseStarted = tag.getLong("PhaseStarted");
         session.deadline = tag.getLong("Deadline");
         session.phaseDuration = Math.max(0, tag.getInt("PhaseDuration"));
         session.pausedTicks = Math.max(0, tag.getInt("PausedTicks"));
         session.hasStation = tag.getBoolean("HasStation");
         session.stationDimension = tag.getString("StationDimension");
         session.stationPos = tag.getLong("StationPos");
         return session.classId != 0 && session.rank != 0 ? session : null;
      }

      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putUUID("Id", this.id);
         tag.putInt("Mode", this.mode.ordinal());
         tag.putInt("Phase", this.phase.ordinal());
         tag.putInt("Class", this.classId);
         tag.putInt("Rank", this.rank);
         tag.putInt("PreviousRank", this.previousRank);
         tag.putInt("RemainingClassMask", this.remainingClassMask);
         tag.putBoolean("FixedClass", this.fixedClass);
         tag.putLong("PhaseStarted", this.phaseStarted);
         tag.putLong("Deadline", this.deadline);
         tag.putInt("PhaseDuration", this.phaseDuration);
         tag.putInt("PausedTicks", this.pausedTicks);
         tag.putBoolean("HasStation", this.hasStation);
         tag.putString("StationDimension", this.stationDimension == null ? "" : this.stationDimension);
         tag.putLong("StationPos", this.stationPos);
         return tag;
      }

      private BlockPos station() {
         return BlockPos.of(this.stationPos);
      }
   }
}
