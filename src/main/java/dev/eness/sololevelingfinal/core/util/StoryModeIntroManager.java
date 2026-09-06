package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.AncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.CartenonGateEntity;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;
import dev.eness.sololevelingfinal.core.entity.LiuSwordVfxEntity;
import dev.eness.sololevelingfinal.core.entity.StatueOfGodEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class StoryModeIntroManager {
   public static final int ASSASSIN_CLASS_ID = 1;
   public static final int FIGHTER_CLASS_ID = 3;
   public static final int E_RANK_ID = 1;
   public static final int TEAM_SIZE = 6;
   public static final ResourceKey<Level> ANCIENT_GOLEM_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling", "dungeon_dimension_c")
   );
   public static final BlockPos ANCIENT_GOLEM_ORIGIN = new BlockPos(0, 64, 0);
   public static final Vec3 ANCIENT_GOLEM_ENTRY = new Vec3(10.5, 2.0, 25.5);
   private static final ResourceLocation ANCIENT_GOLEM_STRUCTURE = new ResourceLocation("sololeveling", "dungeon_ancientgolem");
   private static final ResourceLocation AWAKENED_ADVANCEMENT = new ResourceLocation("sololeveling", "awakened");
   private static final String OWNER_MARKER = "slr_story_intro_owner";
   private static final String OWNER_UUID = "slr_story_intro_owner_uuid";
   private static final String BOSS_MARKER = "slr_story_intro_boss";
   private static final String HUNTER_MARKER = "slr_story_intro_hunter";
   private static final String GATE_MARKER = "slr_story_intro_gate";
   private static final String GOD_MARKER = "slr_story_intro_god";
   private static final String STORY_STATUE_MARKER = "slr_story_intro_statue";
   private static final String STORY_LASER_DONE = "slr_story_intro_laser_done";
   private static final String STORY_ACTIVATION_AT = "slr_story_intro_activation_at";
   private static final String TEMPLE_FOLLOW_MARKER = "slr_story_intro_follow_owner";
   private static final String PROFILE_INDEX = "slr_story_intro_profile";
   private static final String INSTANCE_ID = "slr_story_intro_instance";
   private static final String DUNGEON_TAG_KEY = "dungeon_tag";
   private static final String STORY_DUNGEON_TAG = "story_intro_ancient_golem";
   private static final int TEMPLE_INSTANCE_SPACING = 512;
   private static final int TEMPLE_INSTANCE_COLUMNS = 32;
   private static final int TEMPLE_FLOOR_Y = 64;
   private static final int LASER_STAGGER_TICKS = 14;
   private static final int LASER_KILL_DELAY_TICKS = 10;
   private static final int LASER_TARGET_RECOVERY_TICKS = 40;
   private static final int STATUE_ACTIVATION_DELAY_TICKS = 5;
   private static final int BOSS_ENTITY_LOAD_GRACE_TICKS = 60;
   private static final int STATUE_SEQUENCE_MANAGER_INTERVAL_TICKS = 10;
   private static final int STORY_GOD_DUPLICATE_AUDIT_INTERVAL_TICKS = 100;
   private static final Vec3[] TEAM_ENTRY_OFFSETS = new Vec3[]{
      new Vec3(-4.0, 0.0, -3.0),
      new Vec3(-2.0, 0.0, -5.0),
      new Vec3(0.0, 0.0, -4.0),
      new Vec3(2.0, 0.0, -5.0),
      new Vec3(4.0, 0.0, -3.0),
      new Vec3(0.0, 0.0, 3.0)
   };
   private static final Vec3[] GATE_FORMATION = new Vec3[]{
      new Vec3(-4.5, 0.0, -2.5),
      new Vec3(-1.5, 0.0, -4.0),
      new Vec3(1.5, 0.0, -4.0),
      new Vec3(4.5, 0.0, -2.5),
      new Vec3(-2.5, 0.0, 3.0),
      new Vec3(2.5, 0.0, 3.0)
   };
   private static final Vec3[] TEMPLE_FORMATION = new Vec3[]{
      new Vec3(-5.0, 1.0, 10.0),
      new Vec3(-3.0, 1.0, 12.0),
      new Vec3(-1.0, 1.0, 10.0),
      new Vec3(1.0, 1.0, 12.0),
      new Vec3(3.0, 1.0, 10.0),
      new Vec3(5.0, 1.0, 12.0)
   };
   private static final List<StoryModeIntroManager.HunterProfile> HUNTER_PROFILES = List.of(
      new StoryModeIntroManager.HunterProfile("C", "Fighter", 6.0, 44.0, 12.0, 0.38, "sololeveling:c_tier_sword", "minecraft:shield", 2, 5, 2, 1, 3, 1, 2, 1),
      new StoryModeIntroManager.HunterProfile(
         "D", "Assassin", 4.5, 34.0, 7.0, 0.43, "sololeveling:dagger_knight_d", "sololeveling:dagger_karambit_e", 4, 8, 3, 2, 5, 2, 4, 2
      ),
      new StoryModeIntroManager.HunterProfile("C", "Tanker", 4.5, 58.0, 18.0, 0.33, "sololeveling:war_axe", "minecraft:shield", 1, 11, 4, 3, 2, 1, 6, 1),
      new StoryModeIntroManager.HunterProfile("D", "Fighter", 4.0, 38.0, 9.0, 0.35, "sololeveling:d_tier_sword", "minecraft:shield", 3, 3, 1, 2, 7, 2, 1, 2),
      new StoryModeIntroManager.HunterProfile(
         "C", "Assassin", 6.0, 42.0, 10.0, 0.45, "sololeveling:dagger_chain_c", "sololeveling:dagger_knight_d", 6, 13, 5, 4, 4, 1, 7, 1
      ),
      new StoryModeIntroManager.HunterProfile("B", "Healer", 2.0, 56.0, 15.0, 0.33, "minecraft:air", "sololeveling:storm_griamore", 5, 9, 2, 3, 6, 2, 3, 2)
   );

   private StoryModeIntroManager() {
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(player.server);
         if (data.stage() == StoryModeIntroSavedData.Stage.NOT_STARTED
            && player.server.overworld().getGameRules().getBoolean(SololevelingModGameRules.SOLO_LEVELING_STORY_MODE)) {
            int classId = chooseStartingClass(player);
            if (data.claimOwner(player.getUUID(), classId, player.server.overworld().getGameTime())) {
               initializePlayer(player, classId);
               player.sendSystemMessage(Component.literal("Story Mode: you have entered the Ancient Golem dungeon as an E-rank hunter."));
            }
         }

         if (data.isOwner(player.getUUID()) && data.isActive()) {
            if (data.cartenonInstanceId() > 0 && CartenonProgressSavedData.get(player.serverLevel()).isResolved(player.getUUID())) {
               onAwakeningResolved(player, CartenonProgressSavedData.get(player.serverLevel()).accepted(player.getUUID()));
            } else {
               if (data.stage() == StoryModeIntroSavedData.Stage.PREPARING) {
                  initializePlayer(player, data.playerClassId());
               } else {
                  markOwner(player);
               }

               if (data.stage() == StoryModeIntroSavedData.Stage.PREPARING) {
                  prepareAncientGolemIntro(player, data);
               } else {
                  resumeOwner(player, data);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(player.server);
         if (data.isOwner(player.getUUID()) && data.isActive()) {
            markOwner(player);
            player.server.execute(() -> resumeOwner(player, data));
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(player.server);
         if (data.isOwner(player.getUUID()) && data.isActive()) {
            if (data.cartenonInstanceId() > 0
               && data.stage().ordinal() >= StoryModeIntroSavedData.Stage.TEMPLE.ordinal()
               && data.stage().ordinal() <= StoryModeIntroSavedData.Stage.PLAYER_HUNT.ordinal()
               && player.tickCount % 20 == 0) {
               releaseFrozenTempleHunters(player.server, data);
            }

            switch (data.stage()) {
               case PREPARING:
                  if (player.tickCount % 20 == 0) {
                     prepareAncientGolemIntro(player, data);
                  }
                  break;
               case ANCIENT_GOLEM:
                  if (player.tickCount % 20 == 0) {
                     recoverAncientGolemStage(player, data);
                  }
                  break;
               case GATE_WAIT:
                  if (player.tickCount % 20 == 0) {
                     recoverGateWaitStage(player, data);
                  }
                  break;
               case TEMPLE:
                  if (player.tickCount % 20 == 0) {
                     ensureTempleParty(player, data);
                  }

                  StatueOfGodEntity god = resolveStoryGod(player, data);
                  if (god != null && isAtStoryTempleCenter(player)) {
                     beginLaserExecution(player, god, data);
                  }
                  break;
               case LASER_EXECUTION:
                  tickLaserExecution(player, data);
                  break;
               case WAITING_FOR_SNEAK:
                  tickWaitingForSneak(player, data);
                  break;
               case STATUE_WAKING:
               case STATUE_HUNT:
               case PLAYER_HUNT:
                  if (player.tickCount % 10 == 0) {
                     tickStatueSequence(player, data);
                  }
            }
         }
      }
   }

   public static boolean isStoryBoss(ServerPlayer owner, Entity boss) {
      if (owner != null && boss != null && boss.level() instanceof ServerLevel) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(owner.server);
         if (data.isActive() && data.isOwner(owner.getUUID())) {
            if (data.stage() != StoryModeIntroSavedData.Stage.ANCIENT_GOLEM) {
               return false;
            }

            boolean uuidMatches = data.bossId() != null && data.bossId().equals(boss.getUUID());
            return uuidMatches || boss.getTags().contains("slr_story_intro_boss") && hasOwner(boss, owner.getUUID());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isHandledStoryBoss(ServerPlayer owner, Entity boss) {
      if (owner != null && boss != null && boss.level() instanceof ServerLevel) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(owner.server);
         if (data.isActive() && data.isOwner(owner.getUUID()) && data.stage().ordinal() > StoryModeIntroSavedData.Stage.ANCIENT_GOLEM.ordinal()) {
            boolean uuidMatches = data.bossId() != null && data.bossId().equals(boss.getUUID());
            return uuidMatches || boss.getTags().contains("slr_story_intro_boss") && hasOwner(boss, owner.getUUID());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Nullable
   public static ServerPlayer storyOwnerForBoss(Entity boss) {
      if (boss != null && boss.level() instanceof ServerLevel level) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(level);
         if (data.isActive() && data.ownerId() != null) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(data.ownerId());
            return owner != null && isStoryBoss(owner, boss) ? owner : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void onCartenonGateCreated(ServerPlayer owner, CartenonGateEntity gate, int instanceId) {
      if (owner != null && gate != null) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(owner.server);
         if (data.isActive() && data.isOwner(owner.getUUID())) {
            boolean firstGate = data.stage() == StoryModeIntroSavedData.Stage.ANCIENT_GOLEM;
            boolean sameInstanceRecovery = data.stage() == StoryModeIntroSavedData.Stage.GATE_WAIT && data.cartenonInstanceId() == Math.max(1, instanceId);
            if (firstGate || sameInstanceRecovery) {
               tagOwned(gate, "slr_story_intro_gate", owner.getUUID());
               gate.getPersistentData().putInt("slr_story_intro_instance", Math.max(1, instanceId));
               data.setGate(gate.getUUID(), Math.max(1, instanceId), owner.server.overworld().getGameTime());
               List<HunterEntity> hunters = findStoryHunters(owner.server, data);

               for (int index = 0; index < hunters.size(); index++) {
                  freezeAt(hunters.get(index), gate.position().add(GATE_FORMATION[index % GATE_FORMATION.length]));
               }
            }
         }
      }
   }

   public static void onPlayerEnteredTemple(ServerPlayer owner, ServerLevel templeLevel, int instanceId) {
      if (owner != null && templeLevel != null) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(owner.server);
         if (data.isActive() && data.isOwner(owner.getUUID())) {
            if (data.stage() == StoryModeIntroSavedData.Stage.GATE_WAIT && data.cartenonInstanceId() == Math.max(1, instanceId)) {
               BlockPos origin = cartenonInstanceOrigin(instanceId);
               forceChunk(templeLevel, origin.relative(Direction.SOUTH, 145));
               ServerLevel sourceDungeon = owner.server.getLevel(ANCIENT_GOLEM_DIMENSION);
               if (sourceDungeon != null) {
                  loadDungeonChunks(sourceDungeon);
               }

               List<UUID> movedIds = new ArrayList<>(data.hunterIds());
               List<HunterEntity> hunters = findStoryHunters(owner.server, data);

               for (int index = 0; index < hunters.size(); index++) {
                  UUID originalId = hunters.get(index).getUUID();
                  Vec3 destination = Vec3.atLowerCornerOf(origin).add(TEMPLE_FORMATION[index % TEMPLE_FORMATION.length]);
                  HunterEntity moved = moveHunter(hunters.get(index), templeLevel, destination);
                  if (moved != null) {
                     moved.getPersistentData().putInt("slr_story_intro_instance", Math.max(1, instanceId));
                     enableTempleFollowing(moved);
                     int storedIndex = movedIds.indexOf(originalId);
                     if (storedIndex >= 0) {
                        movedIds.set(storedIndex, moved.getUUID());
                     } else {
                        movedIds.add(moved.getUUID());
                     }
                  }
               }

               data.replaceHunterIds(movedIds);
               StatueOfGodEntity god = findAndMarkGodStatue(owner, templeLevel, instanceId);
               data.setTemple(Math.max(1, instanceId), god == null ? null : god.getUUID(), owner.server.overworld().getGameTime());
               if (god != null) {
                  data.trackGodStatue(god.getUUID(), god.blockPosition());
               }

               owner.getPersistentData().putInt("slr_story_intro_instance", Math.max(1, instanceId));
               ensureTempleParty(owner, data);
            }
         }
      }
   }

   public static void onAwakeningResolved(ServerPlayer player, boolean accepted) {
      if (player != null) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(player.server);
         if (data.isActive() && data.isOwner(player.getUUID())) {
            if (data.stage() == StoryModeIntroSavedData.Stage.PLAYER_HUNT && !hasLivingStoryHunter(player.server, data)) {
               if (accepted) {
                  grantAwakenedAdvancement(player);
               }

               cleanActiveMarkers(player.server, data);
               player.getPersistentData().remove("slr_story_intro_owner");
               player.getPersistentData().remove("slr_story_intro_owner_uuid");
               player.getPersistentData().remove("slr_story_intro_instance");
               if ("story_intro_ancient_golem".equals(player.getPersistentData().getString("dungeon_tag"))) {
                  player.getPersistentData().remove("dungeon_tag");
               }

               data.setStage(StoryModeIntroSavedData.Stage.COMPLETE, player.server.overworld().getGameTime());
            }
         }
      }
   }

   private static void grantAwakenedAdvancement(ServerPlayer player) {
      Advancement advancement = player.server.getAdvancements().getAdvancement(AWAKENED_ADVANCEMENT);
      if (advancement != null) {
         AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);

         for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
         }
      }
   }

   public static boolean isIntroActive(Entity entity) {
      if (entity != null && entity.level() instanceof ServerLevel level) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(level);
         if (!data.isActive() || data.ownerId() == null) {
            return false;
         } else {
            return entity instanceof ServerPlayer player
               ? data.isOwner(player.getUUID())
               : hasOwner(entity, data.ownerId())
                  && (
                     entity.getTags().contains("slr_story_intro_boss")
                        || entity.getTags().contains("slr_story_intro_hunter")
                        || entity.getTags().contains("slr_story_intro_gate")
                        || entity.getTags().contains("slr_story_intro_god")
                  );
         }
      } else {
         return false;
      }
   }

   public static boolean isStoryOwner(ServerPlayer player) {
      if (player == null) {
         return false;
      }

      StoryModeIntroSavedData data = StoryModeIntroSavedData.get(player.server);
      return data.isActive() && data.isOwner(player.getUUID());
   }

   public static boolean canTriggerAwakening(ServerPlayer player, DamageSource source) {
      if (player == null) {
         return true;
      } else {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(player.server);
         if (!data.isActive() || !data.isOwner(player.getUUID())) {
            return true;
         } else {
            return !canResolveAwakening(player)
               ? false
               : (source == null ? null : source.getEntity()) instanceof StatueOfGodEntity god
                  && god.getPersistentData().getBoolean("slr_story_intro_statue")
                  && god.getPersistentData().getInt("slr_story_intro_instance") == data.cartenonInstanceId()
                  && hasOwner(god, player.getUUID());
         }
      }
   }

   public static boolean canResolveAwakening(ServerPlayer player) {
      if (player == null) {
         return true;
      }

      StoryModeIntroSavedData data = StoryModeIntroSavedData.get(player.server);
      return !data.isActive()
         || !data.isOwner(player.getUUID())
         || data.stage() == StoryModeIntroSavedData.Stage.PLAYER_HUNT && !hasLivingStoryHunter(player.server, data);
   }

   public static boolean shouldSuppressClassSelection(ServerPlayer player) {
      if (player == null) {
         return false;
      }

      StoryModeIntroSavedData data = StoryModeIntroSavedData.get(player.server);
      return data.isActive() && data.isOwner(player.getUUID())
         ? true
         : data.stage() == StoryModeIntroSavedData.Stage.NOT_STARTED
            && player.server.overworld().getGameRules().getBoolean(SololevelingModGameRules.SOLO_LEVELING_STORY_MODE);
   }

   public static BlockPos cartenonInstanceOrigin(int instanceId) {
      int zeroBased = Math.max(0, instanceId - 1);
      int column = zeroBased % 32;
      int row = zeroBased / 32;
      return new BlockPos(column * 512, 64, row * 512);
   }

   public static BlockPos cartenonTempleCenter(int instanceId) {
      return cartenonInstanceOrigin(instanceId).relative(Direction.SOUTH, 77).above();
   }

   public static boolean isAtStoryTempleCenter(Entity entity) {
      if (entity != null && entity.level() instanceof ServerLevel level && level.dimension() == CartenonTempleManager.CARTENON_DIMENSION) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(level);
         if (data.isActive() && data.cartenonInstanceId() > 0) {
            Vec3 center = Vec3.atCenterOf(cartenonTempleCenter(data.cartenonInstanceId()));
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;
            return dx * dx + dz * dz <= 196.0;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void protectStoryHunters(LivingAttackEvent event) {
      if (event.getEntity().level() instanceof ServerLevel level) {
         StoryModeIntroSavedData data = StoryModeIntroSavedData.get(level);
         if (data.isActive() && data.ownerId() != null) {
            Entity attacker = event.getSource().getEntity();
            if (event.getEntity() instanceof HunterEntity hunter && hunter.getTags().contains("slr_story_intro_hunter")) {
               boolean scriptedStatueAttack = attacker instanceof StatueOfGodEntity god
                  && god.getPersistentData().getBoolean("slr_story_intro_statue")
                  && hasOwner(god, data.ownerId())
                  && data.stage().ordinal() >= StoryModeIntroSavedData.Stage.LASER_EXECUTION.ordinal();
               if (!scriptedStatueAttack) {
                  event.setCanceled(true);
               }
            } else {
               if (event.getEntity() instanceof ServerPlayer owner
                  && data.isOwner(owner.getUUID())
                  && attacker instanceof StatueOfGodEntity god
                  && god.getPersistentData().getBoolean("slr_story_intro_statue")
                  && hasLivingStoryHunter(owner.server, data)) {
                  event.setCanceled(true);
               }
            }
         }
      }
   }

   private static void beginLaserExecution(ServerPlayer owner, StatueOfGodEntity god, StoryModeIntroSavedData data) {
      ensureTempleParty(owner, data);
      List<HunterEntity> candidates = livingStoryHunters(owner.server, data).stream().filter(hunter -> hunter.level() == god.level()).toList();
      if (!candidates.isEmpty()) {
         int targetCount = Math.min(candidates.size(), 1 + Math.floorMod(owner.getUUID().hashCode(), 2));
         int startIndex = Math.floorMod(owner.getUUID().hashCode() ^ data.cartenonInstanceId(), candidates.size());
         List<UUID> targetIds = new ArrayList<>(targetCount);

         for (int offset = 0; offset < targetCount; offset++) {
            targetIds.add(candidates.get((startIndex + offset) % candidates.size()).getUUID());
         }

         god.getPersistentData().putBoolean("slr_story_intro_laser_done", false);
         god.getPersistentData().remove("slr_story_intro_activation_at");
         god.getEntityData().set(StatueOfGodEntity.SHOOT, true);
         data.beginLasers(targetIds, owner.server.overworld().getGameTime());
         tickLaserExecution(owner, data);
      }
   }

   private static void tickLaserExecution(ServerPlayer owner, StoryModeIntroSavedData data) {
      StatueOfGodEntity god = resolveStoryGod(owner, data);
      if (god != null && god.level() instanceof ServerLevel temple) {
         loadTempleActorChunks(temple, data.cartenonInstanceId());
         List var14 = data.laserTargetIds();
         long gameTick = owner.server.overworld().getGameTime();
         long elapsed = Math.max(0L, gameTick - data.stageStartedTick());
         int pending = data.laserKilledCount();
         if (pending < var14.size()) {
            HunterEntity pendingTarget = findEntity(owner.server, (UUID)var14.get(pending), HunterEntity.class);
            if (!isMatchingLaserTarget(pendingTarget, owner, data, temple)) {
               boolean missingOrDead = pendingTarget == null || pendingTarget.isRemoved() || !pendingTarget.isAlive();
               boolean beamPersisted = data.laserFiredCount() > pending;
               if (missingOrDead && beamPersisted) {
                  data.setLaserKilledCount(pending + 1);
               } else if (beamPersisted ? gameTick - data.laserLastFiredTick() >= 40L : elapsed >= pending * 14L + 40L) {
                  replaceMissingLaserTarget(owner, data, temple, pending);
                  return;
               }
            }
         }

         int fired = data.laserFiredCount();
         if (fired < var14.size() && elapsed >= fired * 14L) {
            HunterEntity target = findEntity(owner.server, (UUID)var14.get(fired), HunterEntity.class);
            if (isMatchingLaserTarget(target, owner, data, temple)) {
               Vec3 eye = god.position().add(0.0, 14.0, 0.0);
               Vec3 impact = target.position().add(0.0, target.getBbHeight() * 0.58, 0.0);
               LiuSwordVfxEntity.spawnExecutionLink(temple, eye, impact, 16715792, 7208960, 0.48F, 13);
               data.markLaserFired(fired + 1, gameTick);
            }
         }

         int killed = data.laserKilledCount();
         if (killed < var14.size() && data.laserFiredCount() > killed && gameTick - data.laserLastFiredTick() >= 10L) {
            HunterEntity target = findEntity(owner.server, (UUID)var14.get(killed), HunterEntity.class);
            if (isMatchingLaserTarget(target, owner, data, temple)) {
               killByStatue(target, god);
               if (!target.isAlive()) {
                  data.setLaserKilledCount(killed + 1);
               }
            }
         }

         long finishedAt = var14.isEmpty() ? 0L : (var14.size() - 1) * 14L + 10L + 1L;
         if (data.laserKilledCount() >= var14.size() && elapsed >= finishedAt) {
            god.getEntityData().set(StatueOfGodEntity.SHOOT, false);
            god.getPersistentData().putBoolean("slr_story_intro_laser_done", true);
            god.getPersistentData().putLong("slr_story_intro_activation_at", temple.getGameTime() + 5L);
            data.setStage(StoryModeIntroSavedData.Stage.WAITING_FOR_SNEAK, owner.server.overworld().getGameTime());
         }
      }
   }

   private static boolean replaceMissingLaserTarget(ServerPlayer owner, StoryModeIntroSavedData data, ServerLevel temple, int targetIndex) {
      List<UUID> reservedTargets = data.laserTargetIds();

      for (HunterEntity candidate : livingStoryHunters(owner.server, data)) {
         if (candidate.level() == temple && !reservedTargets.contains(candidate.getUUID()) && isMatchingStoryHunter(candidate, owner, data)) {
            return data.replacePendingLaserTarget(targetIndex, candidate.getUUID());
         }
      }

      return false;
   }

   private static boolean isMatchingStoryHunter(HunterEntity hunter, ServerPlayer owner, StoryModeIntroSavedData data) {
      return hunter != null
         && hunter.isAlive()
         && !hunter.isRemoved()
         && hunter.getPersistentData().getBoolean("slr_story_intro_hunter")
         && hunter.getPersistentData().getInt("slr_story_intro_instance") == data.cartenonInstanceId()
         && hasOwner(hunter, owner.getUUID())
         && data.hunterIds().contains(hunter.getUUID());
   }

   private static void tickWaitingForSneak(ServerPlayer owner, StoryModeIntroSavedData data) {
      StatueOfGodEntity god = resolveStoryGod(owner, data);
      if (god != null) {
         String state = god.getPersistentData().getString("state");
         if ("waking".equals(state)) {
            data.setStage(StoryModeIntroSavedData.Stage.STATUE_WAKING, owner.server.overworld().getGameTime());
         } else if ("aggresive".equals(state)) {
            data.setStage(StoryModeIntroSavedData.Stage.STATUE_HUNT, owner.server.overworld().getGameTime());
         }
      }
   }

   private static void tickStatueSequence(ServerPlayer owner, StoryModeIntroSavedData data) {
      StatueOfGodEntity god = resolveStoryGod(owner, data);
      if (god != null) {
         String state = god.getPersistentData().getString("state");
         if (data.stage() == StoryModeIntroSavedData.Stage.STATUE_WAKING && "aggresive".equals(state)) {
            data.setStage(StoryModeIntroSavedData.Stage.STATUE_HUNT, owner.server.overworld().getGameTime());
         }

         if (hasLivingStoryHunter(owner.server, data)) {
            if (data.stage() == StoryModeIntroSavedData.Stage.PLAYER_HUNT) {
               data.setStage(StoryModeIntroSavedData.Stage.STATUE_HUNT, owner.server.overworld().getGameTime());
            }
         } else {
            if (data.stage() != StoryModeIntroSavedData.Stage.PLAYER_HUNT) {
               data.setStage(StoryModeIntroSavedData.Stage.PLAYER_HUNT, owner.server.overworld().getGameTime());
            }
         }
      }
   }

   @Nullable
   private static StatueOfGodEntity resolveStoryGod(ServerPlayer owner, StoryModeIntroSavedData data) {
      ServerLevel temple = owner.server.getLevel(CartenonTempleManager.CARTENON_DIMENSION);
      if (temple != null && data.cartenonInstanceId() > 0) {
         StatueOfGodEntity god = data.godStatueId() != null && temple.getEntity(data.godStatueId()) instanceof StatueOfGodEntity savedGod ? savedGod : null;
         if (god != null && (god.level() != temple || god.isRemoved() || !god.isAlive())) {
            god = null;
         }

         boolean recoveringGod = god == null;
         if (god == null && data.godStatuePosition() != null) {
            loadChunksAround(temple, data.godStatuePosition(), 1);
            god = data.godStatueId() != null && temple.getEntity(data.godStatueId()) instanceof StatueOfGodEntity loadedGod ? loadedGod : null;
         }

         List<StatueOfGodEntity> ownedGods = List.of();
         if (god == null) {
            loadStoryGodRecoveryChunks(temple, owner, data);
            ownedGods = findLoadedStoryGods(temple, owner, data);
            god = chooseStoryGod(ownedGods, data);
         }

         if (god == null) {
            BlockPos expected = cartenonInstanceOrigin(data.cartenonInstanceId()).relative(Direction.SOUTH, 145);
            forceChunk(temple, expected);
            god = findAndMarkGodStatue(owner, temple, data.cartenonInstanceId());
         }

         if (god == null) {
            return null;
         }

         boolean auditDuplicates = recoveringGod || owner.tickCount % 100 == 0;
         if (auditDuplicates && ownedGods.isEmpty()) {
            ownedGods = findLoadedStoryGods(temple, owner, data);
         }

         if (auditDuplicates) {
            retireDuplicateStoryGods(ownedGods, god);
         }

         reconcileGodForStage(owner, god, data);
         if (recoveringGod || owner.tickCount % 20 == 0) {
            data.trackGodStatue(god.getUUID(), god.blockPosition());
         }

         return god;
      } else {
         return null;
      }
   }

   private static void loadStoryGodRecoveryChunks(ServerLevel temple, ServerPlayer owner, StoryModeIntroSavedData data) {
      loadStoryGodCorridorChunks(temple, data.cartenonInstanceId());
      if (data.godStatuePosition() != null) {
         loadChunksAround(temple, data.godStatuePosition(), 1);
      }

      if (owner.serverLevel() == temple) {
         loadChunksAround(temple, owner.blockPosition(), 1);
      }
   }

   private static void loadStoryGodCorridorChunks(ServerLevel temple, int instanceId) {
      BlockPos origin = cartenonInstanceOrigin(instanceId);
      int minChunkX = origin.getX() - 24 >> 4;
      int maxChunkX = origin.getX() + 24 >> 4;
      int minChunkZ = origin.getZ() - 8 >> 4;
      int maxChunkZ = origin.getZ() + 170 >> 4;

      for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
         for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            temple.getChunk(chunkX, chunkZ);
         }
      }
   }

   private static void loadChunksAround(ServerLevel level, BlockPos center, int chunkRadius) {
      int centerChunkX = center.getX() >> 4;
      int centerChunkZ = center.getZ() >> 4;

      for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
         for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
            level.getChunk(centerChunkX + dx, centerChunkZ + dz);
         }
      }
   }

   private static List<StatueOfGodEntity> findLoadedStoryGods(ServerLevel temple, ServerPlayer owner, StoryModeIntroSavedData data) {
      Map<UUID, StatueOfGodEntity> found = new LinkedHashMap<>();
      BlockPos origin = cartenonInstanceOrigin(data.cartenonInstanceId());
      collectStoryGods(temple, owner, data, new AABB(origin.offset(-24, -8, -8), origin.offset(24, 48, 170)), found);
      if (data.godStatuePosition() != null) {
         collectStoryGods(temple, owner, data, new AABB(data.godStatuePosition()).inflate(48.0), found);
      }

      if (owner.serverLevel() == temple) {
         collectStoryGods(temple, owner, data, owner.getBoundingBox().inflate(48.0), found);
      }

      return new ArrayList<>(found.values());
   }

   private static void collectStoryGods(ServerLevel temple, ServerPlayer owner, StoryModeIntroSavedData data, AABB bounds, Map<UUID, StatueOfGodEntity> found) {
      for (StatueOfGodEntity candidate : temple.getEntitiesOfClass(StatueOfGodEntity.class, bounds, Entity::isAlive)) {
         boolean savedId = data.godStatueId() != null && data.godStatueId().equals(candidate.getUUID());
         boolean matchingOwner = hasOwner(candidate, owner.getUUID())
            || candidate.getPersistentData().hasUUID("slr_story_intro_owner")
               && owner.getUUID().equals(candidate.getPersistentData().getUUID("slr_story_intro_owner"));
         boolean matchingInstance = candidate.getPersistentData().getInt("slr_story_intro_instance") == data.cartenonInstanceId();
         if (savedId
            || matchingOwner
               && matchingInstance
               && (candidate.getTags().contains("slr_story_intro_god") || candidate.getPersistentData().getBoolean("slr_story_intro_statue"))) {
            found.putIfAbsent(candidate.getUUID(), candidate);
         }
      }
   }

   @Nullable
   private static StatueOfGodEntity chooseStoryGod(List<StatueOfGodEntity> candidates, StoryModeIntroSavedData data) {
      if (candidates.isEmpty()) {
         return null;
      }

      if (data.godStatueId() != null) {
         for (StatueOfGodEntity candidate : candidates) {
            if (data.godStatueId().equals(candidate.getUUID())) {
               return candidate;
            }
         }
      }

      Vec3 anchor = data.godStatuePosition() == null
         ? Vec3.atCenterOf(cartenonInstanceOrigin(data.cartenonInstanceId()).relative(Direction.SOUTH, 145).above(6))
         : Vec3.atCenterOf(data.godStatuePosition());
      return candidates.stream().min(Comparator.comparingDouble(candidatex -> candidatex.distanceToSqr(anchor))).orElse(null);
   }

   private static void retireDuplicateStoryGods(List<StatueOfGodEntity> candidates, StatueOfGodEntity retained) {
      for (StatueOfGodEntity candidate : candidates) {
         if (candidate != retained) {
            candidate.getPersistentData().remove("slr_story_intro_statue");
            candidate.getPersistentData().remove("slr_story_intro_owner");
            candidate.getPersistentData().remove("slr_story_intro_laser_done");
            candidate.getPersistentData().remove("slr_story_intro_activation_at");
            removeOwnedTags(candidate, "slr_story_intro_god");
            candidate.discard();
         }
      }
   }

   private static void reconcileGodForStage(ServerPlayer owner, StatueOfGodEntity god, StoryModeIntroSavedData data) {
      tagOwned(god, "slr_story_intro_god", owner.getUUID());
      god.getPersistentData().putBoolean("slr_story_intro_statue", true);
      god.getPersistentData().putUUID("slr_story_intro_owner", owner.getUUID());
      god.getPersistentData().putInt("slr_story_intro_instance", data.cartenonInstanceId());
      god.setPersistenceRequired();
      StoryModeIntroSavedData.Stage stage = data.stage();
      if (stage != StoryModeIntroSavedData.Stage.LASER_EXECUTION) {
         god.getEntityData().set(StatueOfGodEntity.SHOOT, false);
      }

      if (stage.ordinal() >= StoryModeIntroSavedData.Stage.WAITING_FOR_SNEAK.ordinal()) {
         god.getPersistentData().putBoolean("slr_story_intro_laser_done", true);
         if (!god.getPersistentData().contains("slr_story_intro_activation_at")) {
            god.getPersistentData().putLong("slr_story_intro_activation_at", god.level().getGameTime());
         }
      } else {
         god.getPersistentData().putBoolean("slr_story_intro_laser_done", false);
         god.getPersistentData().remove("slr_story_intro_activation_at");
      }

      if (stage != StoryModeIntroSavedData.Stage.TEMPLE && stage != StoryModeIntroSavedData.Stage.LASER_EXECUTION) {
         if (stage == StoryModeIntroSavedData.Stage.STATUE_WAKING
            && !"waking".equals(god.getPersistentData().getString("state"))
            && !"aggresive".equals(god.getPersistentData().getString("state"))) {
            god.getPersistentData().putString("state", "waking");
            god.getPersistentData().putInt("IA", 0);
            god.getEntityData().set(StatueOfGodEntity.DATA_state, "waking");
            god.getEntityData().set(StatueOfGodEntity.DATA_story_upright, false);
            god.setNoAi(true);
            god.setTarget(null);
            god.getNavigation().stop();
         } else if (stage == StoryModeIntroSavedData.Stage.STATUE_HUNT || stage == StoryModeIntroSavedData.Stage.PLAYER_HUNT) {
            if (!"aggresive".equals(god.getPersistentData().getString("state"))) {
               god.getPersistentData().putString("state", "aggresive");
               god.getPersistentData().putInt("IA", 0);
               god.getEntityData().set(StatueOfGodEntity.DATA_state, "aggresive");
            }

            god.getEntityData().set(StatueOfGodEntity.DATA_story_upright, true);
            god.setNoAi(false);
         }
      } else {
         god.getPersistentData().putString("state", "throne");
         god.getPersistentData().putInt("IA", 0);
         god.getEntityData().set(StatueOfGodEntity.DATA_state, "throne");
         god.getEntityData().set(StatueOfGodEntity.DATA_story_upright, false);
         god.setNoAi(true);
         god.setTarget(null);
         god.getNavigation().stop();
      }
   }

   private static List<HunterEntity> livingStoryHunters(MinecraftServer server, StoryModeIntroSavedData data) {
      return findStoryHunters(server, data)
         .stream()
         .filter(Entity::isAlive)
         .filter(entity -> entity.getPersistentData().getBoolean("slr_story_intro_hunter"))
         .filter(entity -> data.ownerId() != null && hasOwner(entity, data.ownerId()))
         .filter(
            entity -> data.cartenonInstanceId() <= 0
               || entity.level().dimension() == CartenonTempleManager.CARTENON_DIMENSION
                  && entity.getPersistentData().getInt("slr_story_intro_instance") == data.cartenonInstanceId()
         )
         .toList();
   }

   private static boolean hasLivingStoryHunter(MinecraftServer server, StoryModeIntroSavedData data) {
      if (data.cartenonInstanceId() > 0) {
         ServerLevel temple = server.getLevel(CartenonTempleManager.CARTENON_DIMENSION);
         if (temple == null) {
            return false;
         }

         for (UUID hunterId : data.hunterIds()) {
            if (temple.getEntity(hunterId) instanceof HunterEntity hunter
               && hunter.isAlive()
               && hunter.getPersistentData().getBoolean("slr_story_intro_hunter")
               && hunter.getPersistentData().getInt("slr_story_intro_instance") == data.cartenonInstanceId()
               && data.ownerId() != null
               && hasOwner(hunter, data.ownerId())) {
               return true;
            }
         }

         return false;
      } else {
         return !livingStoryHunters(server, data).isEmpty();
      }
   }

   private static boolean isMatchingLaserTarget(@Nullable HunterEntity target, ServerPlayer owner, StoryModeIntroSavedData data, ServerLevel temple) {
      return target != null
         && target.isAlive()
         && target.level() == temple
         && target.getPersistentData().getBoolean("slr_story_intro_hunter")
         && target.getPersistentData().getInt("slr_story_intro_instance") == data.cartenonInstanceId()
         && hasOwner(target, owner.getUUID())
         && data.hunterIds().contains(target.getUUID());
   }

   private static void killByStatue(HunterEntity target, StatueOfGodEntity god) {
      DamageSource source = target.level().damageSources().mobAttack(god);
      target.invulnerableTime = 0;
      target.setInvulnerable(false);
      target.hurt(source, Math.max(10000.0F, target.getMaxHealth() * 20.0F));
      if (target.isAlive()) {
         target.setHealth(0.0F);
         target.die(source);
         if (target.isAlive()) {
            target.remove(RemovalReason.KILLED);
         }
      }
   }

   private static void recoverAncientGolemStage(ServerPlayer owner, StoryModeIntroSavedData data) {
      ServerLevel dungeon = owner.server.getLevel(ANCIENT_GOLEM_DIMENSION);
      if (dungeon != null) {
         loadDungeonChunks(dungeon);
         tagStoryReturnPortals(dungeon);
         List<HunterEntity> hunters = ensureInitialTeam(owner, dungeon, data);
         CartenonGateEntity existingGate = findRecoverableStoryGate(owner, dungeon, data);
         if (existingGate != null && existingGate.getInstanceId() > 0) {
            AncientGolemEntity staleBoss = findAndMarkBoss(owner, dungeon);
            if (staleBoss != null) {
               staleBoss.discard();
            }

            onCartenonGateCreated(owner, existingGate, existingGate.getInstanceId());

            for (int index = 0; index < hunters.size(); index++) {
               freezeAt(hunters.get(index), existingGate.position().add(GATE_FORMATION[index % GATE_FORMATION.length]));
            }
         } else {
            AncientGolemEntity boss = findAndMarkBoss(owner, dungeon);
            if (boss == null && bossRecoveryGraceElapsed(owner, data)) {
               boss = spawnReplacementBoss(owner, dungeon);
            }

            if (boss != null) {
               data.setBossId(boss.getUUID());
            }
         }
      }
   }

   private static void recoverGateWaitStage(ServerPlayer owner, StoryModeIntroSavedData data) {
      if (data.cartenonInstanceId() > 0 && owner.serverLevel().dimension() == CartenonTempleManager.CARTENON_DIMENSION) {
         onPlayerEnteredTemple(owner, owner.serverLevel(), data.cartenonInstanceId());
      } else {
         ServerLevel dungeon = owner.server.getLevel(ANCIENT_GOLEM_DIMENSION);
         if (dungeon != null) {
            loadDungeonChunks(dungeon);
            tagStoryReturnPortals(dungeon);
            List<HunterEntity> hunters = ensureInitialTeam(owner, dungeon, data);
            CartenonGateEntity gate = findRecoverableStoryGate(owner, dungeon, data);
            if (gate == null) {
               gate = spawnReplacementGate(owner, dungeon, data.cartenonInstanceId());
            }

            if (gate != null) {
               if (gate.getTags().contains("slr_story_intro_gate")
                  && hasOwner(gate, owner.getUUID())
                  && data.gateId() != null
                  && data.gateId().equals(gate.getUUID())) {
                  for (int index = 0; index < hunters.size(); index++) {
                     freezeAt(hunters.get(index), gate.position().add(GATE_FORMATION[index % GATE_FORMATION.length]));
                  }
               } else {
                  onCartenonGateCreated(owner, gate, data.cartenonInstanceId());
               }
            }
         }
      }
   }

   @Nullable
   private static CartenonGateEntity findRecoverableStoryGate(ServerPlayer owner, ServerLevel dungeon, StoryModeIntroSavedData data) {
      return data.gateId() != null && dungeon.getEntity(data.gateId()) instanceof CartenonGateEntity savedGate
         ? savedGate
         : dungeon.getEntitiesOfClass(
               CartenonGateEntity.class,
               dungeonBounds().inflate(24.0),
               entity -> entity.getInstanceId() > 0
                  && (entity.getTags().contains("slr_story_intro_gate") && hasOwner(entity, owner.getUUID()) || entity.isAllowed(owner.getUUID()))
            )
            .stream()
            .findFirst()
            .orElse(null);
   }

   private static void ensureTempleParty(ServerPlayer owner, StoryModeIntroSavedData data) {
      if (data.cartenonInstanceId() > 0) {
         ServerLevel temple = owner.server.getLevel(CartenonTempleManager.CARTENON_DIMENSION);
         if (temple != null) {
            loadTempleActorChunks(temple, data.cartenonInstanceId());
            ServerLevel dungeon = owner.server.getLevel(ANCIENT_GOLEM_DIMENSION);
            if (dungeon != null) {
               loadDungeonChunks(dungeon);
            }

            Map<Integer, HunterEntity> byProfile = new LinkedHashMap<>();

            for (HunterEntity hunter : findStoryHunters(owner.server, data)) {
               addHunterByProfile(byProfile, hunter, owner.getUUID());
            }

            if (dungeon != null) {
               for (HunterEntity hunter : dungeon.getEntitiesOfClass(
                  HunterEntity.class,
                  dungeonBounds().inflate(16.0),
                  entity -> entity.isAlive() && entity.getTags().contains("slr_story_intro_hunter") && hasOwner(entity, owner.getUUID())
               )) {
                  addHunterByProfile(byProfile, hunter, owner.getUUID());
               }
            }

            BlockPos origin = cartenonInstanceOrigin(data.cartenonInstanceId());
            List<UUID> movedIds = new ArrayList<>(6);
            List<HunterEntity> movedHunters = new ArrayList<>(6);

            for (int profileIndex = 0; profileIndex < 6; profileIndex++) {
               Vec3 destination = Vec3.atLowerCornerOf(origin).add(TEMPLE_FORMATION[profileIndex]);
               HunterEntity hunter = byProfile.get(profileIndex);
               boolean placedAtEntry = hunter == null || hunter.level() != temple;
               if (hunter == null) {
                  hunter = spawnHunterAt(owner, temple, profileIndex, destination);
               } else if (hunter.level() != temple) {
                  hunter = moveHunter(hunter, temple, destination);
               }

               if (hunter != null) {
                  tagOwned(hunter, "slr_story_intro_hunter", owner.getUUID());
                  hunter.getPersistentData().putInt("slr_story_intro_profile", profileIndex);
                  hunter.getPersistentData().putInt("slr_story_intro_instance", data.cartenonInstanceId());
                  hunter.getPersistentData().putString("dungeon_tag", "story_intro_ancient_golem");
                  hunter.setPersistenceRequired();
                  if (placedAtEntry || hunter.isNoAi() || !hunter.getPersistentData().getBoolean("slr_story_intro_follow_owner")) {
                     enableTempleFollowing(hunter);
                  }

                  movedIds.add(hunter.getUUID());
                  movedHunters.add(hunter);
               }
            }

            String allies = buildAllies(owner.getUUID(), movedHunters);

            for (HunterEntity hunter : movedHunters) {
               hunter.getEntityData().set(HunterEntity.DATA_Allies, allies);
            }

            data.replaceHunterIds(movedIds);
         }
      }
   }

   private static void addHunterByProfile(Map<Integer, HunterEntity> byProfile, HunterEntity hunter, UUID ownerId) {
      if (hunter != null && hunter.isAlive() && hunter.getTags().contains("slr_story_intro_hunter") && hasOwner(hunter, ownerId)) {
         int profile = hunter.getPersistentData().getInt("slr_story_intro_profile");
         if (profile >= 0 && profile < 6) {
            HunterEntity existing = byProfile.putIfAbsent(profile, hunter);
            if (existing != null && existing != hunter) {
               retireDuplicateStoryHunter(hunter);
            }
         }
      }
   }

   private static void retireDuplicateStoryHunter(HunterEntity hunter) {
      hunter.setNoAi(false);
      hunter.setTarget(null);
      removeOwnedTags(hunter, "slr_story_intro_hunter");
      if ("story_intro_ancient_golem".equals(hunter.getPersistentData().getString("dungeon_tag"))) {
         hunter.getPersistentData().remove("dungeon_tag");
      }

      hunter.discard();
   }

   @Nullable
   private static AncientGolemEntity spawnReplacementBoss(ServerPlayer owner, ServerLevel dungeon) {
      AncientGolemEntity existing = findAndMarkBoss(owner, dungeon);
      if (existing != null) {
         return existing;
      }

      AncientGolemEntity boss = SololevelingModEntities.ANCIENT_GOLEM.get().create(dungeon);
      if (boss == null) {
         return null;
      }

      Vec3 spawn = Vec3.atLowerCornerOf(ANCIENT_GOLEM_ORIGIN).add(98.86, 2.0, 24.69);
      boss.moveTo(spawn.x, spawn.y, spawn.z, 90.0F, 0.0F);
      boss.finalizeSpawn(dungeon, dungeon.getCurrentDifficultyAt(BlockPos.containing(spawn)), MobSpawnType.EVENT, null, null);
      tagOwned(boss, "slr_story_intro_boss", owner.getUUID());
      boss.getPersistentData().putString("dungeon_tag", "story_intro_ancient_golem");
      boss.setPersistenceRequired();
      if (!dungeon.addFreshEntity(boss)) {
         return null;
      }

      SololevelingMod.LOGGER.warn("Story Mode restored its missing Ancient Golem boss.");
      return boss;
   }

   @Nullable
   private static CartenonGateEntity spawnReplacementGate(ServerPlayer owner, ServerLevel dungeon, int instanceId) {
      if (instanceId <= 0) {
         return null;
      }

      CartenonGateEntity gate = SololevelingModEntities.CARTENON_GATE.get().create(dungeon);
      if (gate == null) {
         return null;
      }

      gate.configure(owner.getUUID(), Set.of(owner.getUUID()), instanceId);
      BlockPos bossCenter = ANCIENT_GOLEM_ORIGIN.offset(99, 2, 25);
      BlockPos gatePos = findStoryGatePosition(dungeon, bossCenter);
      gate.moveTo(gatePos.getX() + 0.5, gatePos.getY(), gatePos.getZ() + 0.5, owner.getYRot() + 180.0F, 0.0F);
      if (!dungeon.addFreshEntity(gate)) {
         return null;
      }

      onCartenonGateCreated(owner, gate, instanceId);
      SololevelingMod.LOGGER.warn("Story Mode restored its missing Cartenon gate.");
      return gate;
   }

   private static BlockPos findStoryGatePosition(ServerLevel level, BlockPos center) {
      int[][] offsets = new int[][]{{3, 0}, {-3, 0}, {0, 3}, {0, -3}, {4, 4}, {-4, 4}, {4, -4}, {-4, -4}, {0, 0}};

      for (int[] offset : offsets) {
         for (int dy = 3; dy >= -3; dy--) {
            BlockPos candidate = center.offset(offset[0], dy, offset[1]);
            if (level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)
               && level.getBlockState(candidate).getCollisionShape(level, candidate).isEmpty()
               && level.getBlockState(candidate.above()).getCollisionShape(level, candidate.above()).isEmpty()
               && level.getBlockState(candidate.above(2)).getCollisionShape(level, candidate.above(2)).isEmpty()) {
               return candidate;
            }
         }
      }

      return center.above();
   }

   private static void prepareAncientGolemIntro(ServerPlayer player, StoryModeIntroSavedData data) {
      ServerLevel dungeon = player.server.getLevel(ANCIENT_GOLEM_DIMENSION);
      if (dungeon == null) {
         SololevelingMod.LOGGER.error("Story Mode cannot start: dimension {} is unavailable.", ANCIENT_GOLEM_DIMENSION.location());
      } else {
         loadDungeonChunks(dungeon);
         player.getPersistentData().putString("dungeon_tag", "story_intro_ancient_golem");
         if (!data.dungeonPlaced() && appearsDungeonAlreadyPlaced(dungeon)) {
            data.markDungeonPlaced();
         }

         if (!data.dungeonPlaced()) {
            StructureTemplate template = dungeon.getStructureManager().get(ANCIENT_GOLEM_STRUCTURE).orElse(null);
            if (template == null || template.getSize().getX() <= 0) {
               SololevelingMod.LOGGER.error("Story Mode cannot start: structure {} is unavailable.", ANCIENT_GOLEM_STRUCTURE);
               return;
            }

            boolean placed = template.placeInWorld(
               dungeon,
               ANCIENT_GOLEM_ORIGIN,
               ANCIENT_GOLEM_ORIGIN,
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               dungeon.random,
               2
            );
            if (!placed) {
               SololevelingMod.LOGGER.error("Story Mode could not place {} at {}.", ANCIENT_GOLEM_STRUCTURE, ANCIENT_GOLEM_ORIGIN);
               return;
            }

            data.markDungeonPlaced();
         }

         tagStoryReturnPortals(dungeon);
         AncientGolemEntity boss = findAndMarkBoss(player, dungeon);
         if (boss == null) {
            if (!bossRecoveryGraceElapsed(player, data)) {
               return;
            }

            repairStoryDungeonBlocks(dungeon);
            boss = findAndMarkBoss(player, dungeon);
            if (boss == null) {
               boss = spawnReplacementBoss(player, dungeon);
            }

            if (boss == null) {
               SololevelingMod.LOGGER.warn("Story Mode is waiting for its Ancient Golem boss at {}.", ANCIENT_GOLEM_ORIGIN);
               return;
            }
         }

         data.setBossId(boss.getUUID());
         List<HunterEntity> hunters = ensureInitialTeam(player, dungeon, data);
         if (hunters.size() >= 6) {
            data.setStage(StoryModeIntroSavedData.Stage.ANCIENT_GOLEM, player.server.overworld().getGameTime());
            Vec3 entry = dungeonEntry();
            player.stopRiding();
            player.teleportTo(dungeon, entry.x, entry.y, entry.z, -90.0F, 0.0F);
         }
      }
   }

   private static void repairStoryDungeonBlocks(ServerLevel dungeon) {
      StructureTemplate template = dungeon.getStructureManager().get(ANCIENT_GOLEM_STRUCTURE).orElse(null);
      if (template != null && template.getSize().getX() > 0) {
         template.placeInWorld(
            dungeon,
            ANCIENT_GOLEM_ORIGIN,
            ANCIENT_GOLEM_ORIGIN,
            new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(true),
            dungeon.random,
            2
         );
      }
   }

   private static void tagStoryReturnPortals(ServerLevel dungeon) {
      for (Entity entity : dungeon.getEntitiesOfClass(
         Entity.class, dungeonBounds().inflate(3.0), candidate -> candidate.getType() == SololevelingModEntities.PORTAL_12.get()
      )) {
         entity.getPersistentData().putString("dungeon_tag", "story_intro_ancient_golem");
      }
   }

   private static AncientGolemEntity findAndMarkBoss(ServerPlayer owner, ServerLevel dungeon) {
      AABB bounds = dungeonBounds().inflate(3.0);
      StoryModeIntroSavedData data = StoryModeIntroSavedData.get(owner.server);
      Vec3 expected = Vec3.atLowerCornerOf(ANCIENT_GOLEM_ORIGIN).add(98.86, 2.0, 24.69);
      List<AncientGolemEntity> bosses = new ArrayList<>(dungeon.getEntitiesOfClass(AncientGolemEntity.class, bounds, entity -> !entity.isRemoved()));
      if (data.bossId() != null) {
         AncientGolemEntity tracked = findEntity(owner.server, data.bossId(), AncientGolemEntity.class);
         if (tracked != null && tracked.level() == dungeon && !tracked.isRemoved() && !bosses.contains(tracked)) {
            bosses.add(tracked);
         }
      }

      bosses.sort(Comparator.<AncientGolemEntity>comparingInt(entity -> {
         if (data.bossId() != null && data.bossId().equals(entity.getUUID())) {
            return 0;
         } else {
            return entity.getTags().contains("slr_story_intro_boss") && hasOwner(entity, owner.getUUID()) ? 1 : 2;
         }
      }).thenComparingDouble(entity -> entity.distanceToSqr(expected)));
      AncientGolemEntity boss = bosses.isEmpty() ? null : bosses.get(0);

      for (int index = 1; index < bosses.size(); index++) {
         bosses.get(index).discard();
      }

      if (bosses.size() > 1) {
         SololevelingMod.LOGGER.warn("Story Mode removed {} duplicate Ancient Golem boss(es).", bosses.size() - 1);
      }

      if (boss != null) {
         data.noteBossObserved();
         tagOwned(boss, "slr_story_intro_boss", owner.getUUID());
         boss.getPersistentData().putString("dungeon_tag", "story_intro_ancient_golem");
         boss.setPersistenceRequired();
      }

      return boss;
   }

   private static boolean bossRecoveryGraceElapsed(ServerPlayer owner, StoryModeIntroSavedData data) {
      return data.bossRecoveryGraceElapsed(owner.server.overworld().getGameTime(), 60L);
   }

   private static List<HunterEntity> ensureInitialTeam(ServerPlayer owner, ServerLevel dungeon, StoryModeIntroSavedData data) {
      Map<Integer, HunterEntity> byProfile = new LinkedHashMap<>();

      for (UUID hunterId : data.hunterIds()) {
         HunterEntity hunter = findEntity(owner.server, hunterId, HunterEntity.class);
         if (hunter != null && hunter.level() != dungeon) {
            int profile = hunter.getPersistentData().getInt("slr_story_intro_profile");
            if (profile >= 0 && profile < 6) {
               hunter = moveHunter(hunter, dungeon, dungeonEntry().add(TEAM_ENTRY_OFFSETS[profile]));
            }
         }

         addHunterByProfile(byProfile, hunter, owner.getUUID());
      }

      for (HunterEntity hunter : dungeon.getEntitiesOfClass(
         HunterEntity.class, dungeonBounds().inflate(16.0), entity -> entity.getTags().contains("slr_story_intro_hunter") && hasOwner(entity, owner.getUUID())
      )) {
         addHunterByProfile(byProfile, hunter, owner.getUUID());
      }

      for (int index = 0; index < 6; index++) {
         if (!byProfile.containsKey(index)) {
            HunterEntity hunter = spawnHunter(owner, dungeon, index);
            if (hunter != null) {
               byProfile.put(index, hunter);
            }
         }
      }

      List<HunterEntity> result = byProfile.entrySet().stream().sorted(Entry.comparingByKey()).map(Entry::getValue).toList();
      data.replaceHunterIds(result.stream().map(Entity::getUUID).toList());
      String allies = buildAllies(owner.getUUID(), result);

      for (HunterEntity hunter : result) {
         hunter.getEntityData().set(HunterEntity.DATA_Allies, allies);
         hunter.getPersistentData().putString("dungeon_tag", "story_intro_ancient_golem");
      }

      return result;
   }

   @Nullable
   private static HunterEntity spawnHunter(ServerPlayer owner, ServerLevel dungeon, int profileIndex) {
      return spawnHunterAt(owner, dungeon, profileIndex, dungeonEntry().add(TEAM_ENTRY_OFFSETS[profileIndex]));
   }

   @Nullable
   private static HunterEntity spawnHunterAt(ServerPlayer owner, ServerLevel level, int profileIndex, Vec3 spawn) {
      HunterEntity hunter = SololevelingModEntities.HUNTER.get().create(level);
      if (hunter == null) {
         return null;
      }

      hunter.moveTo(spawn.x, spawn.y, spawn.z, -90.0F, 0.0F);
      hunter.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(spawn)), MobSpawnType.EVENT, null, null);
      applyProfile(hunter, HUNTER_PROFILES.get(profileIndex));
      hunter.tame(owner);
      hunter.setPersistenceRequired();
      hunter.setNoAi(false);
      hunter.setInvulnerable(false);
      tagOwned(hunter, "slr_story_intro_hunter", owner.getUUID());
      hunter.getPersistentData().putString("dungeon_tag", "story_intro_ancient_golem");
      hunter.getPersistentData().putInt("slr_story_intro_profile", profileIndex);
      return level.addFreshEntity(hunter) ? hunter : null;
   }

   private static void applyProfile(HunterEntity hunter, StoryModeIntroManager.HunterProfile profile) {
      hunter.getEntityData().set(HunterEntity.DATA_Rank, profile.rank());
      hunter.getEntityData().set(HunterEntity.DATA_HunterClass, profile.hunterClass());
      hunter.getEntityData().set(HunterEntity.DATA_Eyes, profile.eyes());
      hunter.getEntityData().set(HunterEntity.DATA_TopIn, profile.topIn());
      hunter.getEntityData().set(HunterEntity.DATA_TopOut, profile.topOut());
      hunter.getEntityData().set(HunterEntity.DATA_Bottom, profile.bottom());
      hunter.getEntityData().set(HunterEntity.DATA_Foot, profile.foot());
      hunter.getEntityData().set(HunterEntity.DATA_EyeBs, profile.eyeBase());
      hunter.getEntityData().set(HunterEntity.DATA_Hair, profile.hair());
      hunter.getEntityData().set(HunterEntity.DATA_Mouth, profile.mouth());
      hunter.setItemInHand(InteractionHand.MAIN_HAND, stack(profile.mainHand()));
      hunter.setItemInHand(InteractionHand.OFF_HAND, stack(profile.offHand()));
      setBaseAttribute(hunter, Attributes.ATTACK_DAMAGE, profile.attack());
      setBaseAttribute(hunter, Attributes.MAX_HEALTH, profile.health());
      setBaseAttribute(hunter, Attributes.ARMOR, profile.armor());
      setBaseAttribute(hunter, Attributes.MOVEMENT_SPEED, profile.speed());
      setBaseAttribute(hunter, Attributes.FOLLOW_RANGE, 64.0);
      hunter.setHealth(hunter.getMaxHealth());
      hunter.getPersistentData().putDouble("int", rankPower(profile.rank()) * 12.0);
   }

   private static void initializePlayer(ServerPlayer player, int classId) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.HunterRank = 1.0;
         capability.prevRank = 1.0;
         capability.Level = 1.0;
         capability.prevLevel = 1.0;
         capability.Classes = classId;
         capability.ranking = "E";
         capability.Player = false;
         capability.syncPlayerVariables(player);
      });
      markOwner(player);
   }

   private static int chooseStartingClass(ServerPlayer player) {
      long choice = player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits() ^ player.server.overworld().getSeed();
      return (Long.bitCount(choice) & 1) == 0 ? 3 : 1;
   }

   private static void resumeOwner(ServerPlayer player, StoryModeIntroSavedData data) {
      StoryModeIntroSavedData.Stage stage = data.stage();
      if (stage == StoryModeIntroSavedData.Stage.PREPARING) {
         prepareAncientGolemIntro(player, data);
      } else if (stage != StoryModeIntroSavedData.Stage.ANCIENT_GOLEM && stage != StoryModeIntroSavedData.Stage.GATE_WAIT) {
         if (data.cartenonInstanceId() > 0 && player.serverLevel().dimension() != CartenonTempleManager.CARTENON_DIMENSION) {
            ServerLevel temple = player.server.getLevel(CartenonTempleManager.CARTENON_DIMENSION);
            if (temple != null) {
               BlockPos entry = cartenonInstanceOrigin(data.cartenonInstanceId()).relative(Direction.SOUTH, 8).above();
               player.teleportTo(temple, entry.getX() + 0.5, entry.getY(), entry.getZ() + 0.5, 0.0F, 0.0F);
            }
         }
      } else {
         player.getPersistentData().putString("dungeon_tag", "story_intro_ancient_golem");
         if (player.serverLevel().dimension() != ANCIENT_GOLEM_DIMENSION) {
            ServerLevel dungeon = player.server.getLevel(ANCIENT_GOLEM_DIMENSION);
            if (dungeon != null) {
               loadDungeonChunks(dungeon);
               Vec3 returnPosition = dungeonEntry();
               if (stage == StoryModeIntroSavedData.Stage.GATE_WAIT && data.gateId() != null) {
                  Entity gate = dungeon.getEntity(data.gateId());
                  if (gate != null) {
                     returnPosition = gate.position().add(0.0, 0.0, 3.5);
                  }
               }

               player.teleportTo(dungeon, returnPosition.x, returnPosition.y, returnPosition.z, -90.0F, 0.0F);
            }
         }
      }
   }

   private static List<HunterEntity> findStoryHunters(MinecraftServer server, StoryModeIntroSavedData data) {
      Map<UUID, HunterEntity> found = new LinkedHashMap<>();
      if (data.cartenonInstanceId() > 0) {
         ServerLevel temple = server.getLevel(CartenonTempleManager.CARTENON_DIMENSION);
         if (temple != null) {
            loadTempleActorChunks(temple, data.cartenonInstanceId());
         }
      } else {
         ServerLevel dungeon = server.getLevel(ANCIENT_GOLEM_DIMENSION);
         if (dungeon != null) {
            loadDungeonChunks(dungeon);
         }
      }

      for (UUID hunterId : data.hunterIds()) {
         HunterEntity hunter = findEntity(server, hunterId, HunterEntity.class);
         if (hunter != null) {
            found.putIfAbsent(hunter.getUUID(), hunter);
         }
      }

      if (data.cartenonInstanceId() > 0) {
         ServerLevel temple = server.getLevel(CartenonTempleManager.CARTENON_DIMENSION);
         if (temple != null) {
            BlockPos origin = cartenonInstanceOrigin(data.cartenonInstanceId());
            AABB partyBounds = new AABB(origin.offset(-24, -8, -8), origin.offset(24, 24, 32));

            for (HunterEntity hunter : temple.getEntitiesOfClass(
               HunterEntity.class,
               partyBounds,
               entity -> entity.getTags().contains("slr_story_intro_hunter")
                  && data.ownerId() != null
                  && hasOwner(entity, data.ownerId())
                  && entity.getPersistentData().getInt("slr_story_intro_instance") == data.cartenonInstanceId()
            )) {
               found.putIfAbsent(hunter.getUUID(), hunter);
            }
         }
      } else {
         ServerLevel dungeon = server.getLevel(ANCIENT_GOLEM_DIMENSION);
         if (dungeon != null) {
            for (HunterEntity hunter : dungeon.getEntitiesOfClass(
               HunterEntity.class,
               dungeonBounds().inflate(16.0),
               entity -> entity.getTags().contains("slr_story_intro_hunter") && data.ownerId() != null && hasOwner(entity, data.ownerId())
            )) {
               found.putIfAbsent(hunter.getUUID(), hunter);
            }
         }
      }

      List<HunterEntity> result = new ArrayList<>(found.values());
      result.sort(Comparator.comparingInt(entity -> entity.getPersistentData().getInt("slr_story_intro_profile")));
      return result;
   }

   @Nullable
   private static StatueOfGodEntity findAndMarkGodStatue(ServerPlayer owner, ServerLevel temple, int instanceId) {
      BlockPos origin = cartenonInstanceOrigin(instanceId);
      BlockPos expected = origin.relative(Direction.SOUTH, 145).above(6);
      AABB bounds = new AABB(origin.offset(-18, -3, 128), origin.offset(18, 40, 155));
      StatueOfGodEntity god = temple.getEntitiesOfClass(StatueOfGodEntity.class, bounds, Entity::isAlive)
         .stream()
         .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(Vec3.atCenterOf(expected))))
         .orElse(null);
      if (god == null) {
         god = spawnReplacementGodStatue(temple, expected);
      }

      if (god != null) {
         tagOwned(god, "slr_story_intro_god", owner.getUUID());
         god.getPersistentData().putBoolean("slr_story_intro_statue", true);
         god.getPersistentData().putUUID("slr_story_intro_owner", owner.getUUID());
         god.getPersistentData().putInt("slr_story_intro_instance", Math.max(1, instanceId));
         god.getPersistentData().putBoolean("slr_story_intro_laser_done", false);
         god.getPersistentData().remove("slr_story_intro_activation_at");
         god.setPersistenceRequired();
      }

      return god;
   }

   @Nullable
   private static StatueOfGodEntity spawnReplacementGodStatue(ServerLevel temple, BlockPos expected) {
      StatueOfGodEntity god = SololevelingModEntities.STATUE_OF_GOD.get().create(temple);
      if (god == null) {
         return null;
      }

      float yaw = 180.0F;
      god.moveTo(expected.getX() + 0.5, expected.getY(), expected.getZ() + 0.5, yaw, 0.0F);
      god.finalizeSpawn(temple, temple.getCurrentDifficultyAt(expected), MobSpawnType.STRUCTURE, null, null);
      god.setYRot(yaw);
      god.setYBodyRot(yaw);
      god.setYHeadRot(yaw);
      god.getPersistentData().putString("state", "throne");
      god.getPersistentData().putInt("IA", 0);
      god.getPersistentData().putFloat("CartenonHomeYaw", yaw);
      god.getPersistentData().putBoolean("CartenonTempleStatue", true);
      god.getEntityData().set(StatueOfGodEntity.DATA_state, "throne");
      god.getEntityData().set(StatueOfGodEntity.DATA_default_x, expected.getX());
      god.getEntityData().set(StatueOfGodEntity.DATA_default_y, expected.getY());
      god.getEntityData().set(StatueOfGodEntity.DATA_default_z, expected.getZ());
      god.setNoAi(true);
      god.setPersistenceRequired();
      if (!temple.addFreshEntity(god)) {
         return null;
      }

      SololevelingMod.LOGGER.warn("Story Mode restored its missing Statue of God.");
      return god;
   }

   @Nullable
   private static HunterEntity moveHunter(HunterEntity hunter, ServerLevel destinationLevel, final Vec3 destination) {
      if (hunter.level() == destinationLevel) {
         hunter.moveTo(destination.x, destination.y, destination.z, hunter.getYRot(), hunter.getXRot());
         return hunter;
      } else {
         ITeleporter teleporter = new ITeleporter() {
            @Override
            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
               return new PortalInfo(destination, Vec3.ZERO, entity.getYRot(), entity.getXRot());
            }

            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
               Entity moved = repositionEntity.apply(false);
               moved.moveTo(destination.x, destination.y, destination.z, yaw, entity.getXRot());
               return moved;
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
               return false;
            }
         };
         return hunter.changeDimension(destinationLevel, teleporter) instanceof HunterEntity movedHunter ? movedHunter : null;
      }
   }

   private static void freezeAt(HunterEntity hunter, Vec3 position) {
      hunter.getPersistentData().remove("slr_story_intro_follow_owner");
      hunter.moveTo(position.x, position.y, position.z, hunter.getYRot(), hunter.getXRot());
      hunter.setNoAi(true);
      hunter.setTarget(null);
      hunter.getNavigation().stop();
      hunter.setDeltaMovement(Vec3.ZERO);
      hunter.fallDistance = 0.0F;
   }

   private static void enableTempleFollowing(HunterEntity hunter) {
      hunter.getPersistentData().putBoolean("slr_story_intro_follow_owner", true);
      hunter.setOrderedToSit(false);
      hunter.setNoAi(false);
      hunter.setTarget(null);
      hunter.getNavigation().stop();
      hunter.setDeltaMovement(Vec3.ZERO);
      hunter.fallDistance = 0.0F;
   }

   private static void releaseFrozenTempleHunters(MinecraftServer server, StoryModeIntroSavedData data) {
      for (HunterEntity hunter : findStoryHunters(server, data)) {
         if (hunter.level().dimension() == CartenonTempleManager.CARTENON_DIMENSION) {
            boolean needsRelease = hunter.isNoAi() || hunter.isOrderedToSit() || !hunter.getPersistentData().getBoolean("slr_story_intro_follow_owner");
            if (needsRelease) {
               enableTempleFollowing(hunter);
            }
         }
      }
   }

   private static void cleanActiveMarkers(MinecraftServer server, StoryModeIntroSavedData data) {
      ServerLevel dungeon = server.getLevel(ANCIENT_GOLEM_DIMENSION);
      if (dungeon != null) {
         loadDungeonChunks(dungeon);
      }

      ServerLevel temple = null;
      if (data.cartenonInstanceId() > 0) {
         temple = server.getLevel(CartenonTempleManager.CARTENON_DIMENSION);
         if (temple != null) {
            loadTempleActorChunks(temple, data.cartenonInstanceId());
            loadStoryGodCorridorChunks(temple, data.cartenonInstanceId());
            if (data.godStatuePosition() != null) {
               loadChunksAround(temple, data.godStatuePosition(), 1);
            }
         }
      }

      for (HunterEntity hunter : findStoryHunters(server, data)) {
         hunter.setNoAi(false);
         hunter.getPersistentData().remove("slr_story_intro_follow_owner");
         removeOwnedTags(hunter, "slr_story_intro_hunter");
      }

      Entity boss = data.bossId() == null ? null : findEntity(server, data.bossId(), Entity.class);
      if (boss != null) {
         removeOwnedTags(boss, "slr_story_intro_boss");
      }

      Entity gate = data.gateId() == null ? null : findEntity(server, data.gateId(), Entity.class);
      if (gate != null) {
         removeOwnedTags(gate, "slr_story_intro_gate");
      }

      Map<UUID, StatueOfGodEntity> gods = new LinkedHashMap<>();
      StatueOfGodEntity savedGod = data.godStatueId() == null ? null : findEntity(server, data.godStatueId(), StatueOfGodEntity.class);
      if (savedGod != null) {
         gods.put(savedGod.getUUID(), savedGod);
      }

      if (temple != null && data.ownerId() != null && data.cartenonInstanceId() > 0) {
         BlockPos origin = cartenonInstanceOrigin(data.cartenonInstanceId());

         for (StatueOfGodEntity candidate : temple.getEntitiesOfClass(
            StatueOfGodEntity.class, new AABB(origin.offset(-24, -8, -8), origin.offset(24, 48, 170)), Entity::isAlive
         )) {
            if (candidate.getPersistentData().getInt("slr_story_intro_instance") == data.cartenonInstanceId()
               && (hasOwner(candidate, data.ownerId()) || candidate.getPersistentData().getBoolean("slr_story_intro_statue"))) {
               gods.putIfAbsent(candidate.getUUID(), candidate);
            }
         }
      }

      for (StatueOfGodEntity statue : gods.values()) {
         resetStoryGod(statue);
      }
   }

   private static void resetStoryGod(StatueOfGodEntity statue) {
      statue.getEntityData().set(StatueOfGodEntity.SHOOT, false);
      statue.getPersistentData().remove("slr_story_intro_statue");
      statue.getPersistentData().remove("slr_story_intro_owner");
      statue.getPersistentData().remove("slr_story_intro_laser_done");
      statue.getPersistentData().remove("slr_story_intro_activation_at");
      statue.getPersistentData().putString("state", "throne");
      statue.getPersistentData().putInt("IA", 0);
      statue.getEntityData().set(StatueOfGodEntity.DATA_state, "throne");
      statue.getEntityData().set(StatueOfGodEntity.DATA_story_upright, false);
      statue.setTarget(null);
      statue.getNavigation().stop();
      statue.setDeltaMovement(Vec3.ZERO);
      statue.setNoAi(true);
      removeOwnedTags(statue, "slr_story_intro_god");
   }

   private static <T extends Entity> T findEntity(MinecraftServer server, UUID id, Class<T> expectedType) {
      if (server != null && id != null) {
         for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (expectedType.isInstance(entity)) {
               return expectedType.cast(entity);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static void tagOwned(Entity entity, String marker, UUID ownerId) {
      entity.addTag(marker);
      entity.getPersistentData().putBoolean(marker, true);
      entity.getPersistentData().putUUID("slr_story_intro_owner_uuid", ownerId);
   }

   private static boolean hasOwner(Entity entity, UUID ownerId) {
      return entity != null
         && ownerId != null
         && entity.getPersistentData().hasUUID("slr_story_intro_owner_uuid")
         && ownerId.equals(entity.getPersistentData().getUUID("slr_story_intro_owner_uuid"));
   }

   private static void removeOwnedTags(Entity entity, String marker) {
      entity.removeTag(marker);
      entity.getPersistentData().remove(marker);
      entity.getPersistentData().remove("slr_story_intro_owner_uuid");
      entity.getPersistentData().remove("slr_story_intro_instance");
   }

   private static void markOwner(ServerPlayer player) {
      player.getPersistentData().putBoolean("slr_story_intro_owner", true);
      player.getPersistentData().putUUID("slr_story_intro_owner_uuid", player.getUUID());
   }

   private static Vec3 dungeonEntry() {
      return Vec3.atLowerCornerOf(ANCIENT_GOLEM_ORIGIN).add(ANCIENT_GOLEM_ENTRY);
   }

   private static AABB dungeonBounds() {
      return new AABB(ANCIENT_GOLEM_ORIGIN, ANCIENT_GOLEM_ORIGIN.offset(126, 22, 51));
   }

   private static void loadDungeonChunks(ServerLevel level) {
      int minChunkX = ANCIENT_GOLEM_ORIGIN.getX() >> 4;
      int minChunkZ = ANCIENT_GOLEM_ORIGIN.getZ() >> 4;
      int maxChunkX = ANCIENT_GOLEM_ORIGIN.getX() + 125 >> 4;
      int maxChunkZ = ANCIENT_GOLEM_ORIGIN.getZ() + 50 >> 4;

      for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
         for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            level.getChunk(chunkX, chunkZ);
         }
      }
   }

   private static void loadTempleActorChunks(ServerLevel level, int instanceId) {
      BlockPos origin = cartenonInstanceOrigin(instanceId);
      forceChunk(level, origin);

      for (Vec3 offset : TEMPLE_FORMATION) {
         forceChunk(level, BlockPos.containing(Vec3.atLowerCornerOf(origin).add(offset)));
      }

      forceChunk(level, cartenonTempleCenter(instanceId));
      forceChunk(level, origin.relative(Direction.SOUTH, 145).above(6));
   }

   private static boolean appearsDungeonAlreadyPlaced(ServerLevel level) {
      int solidSamples = 0;
      int[][] samples = new int[][]{{10, 0, 25}, {38, 0, 25}, {68, 0, 25}, {98, 0, 25}, {108, 0, 47}};

      for (int[] sample : samples) {
         if (!level.getBlockState(ANCIENT_GOLEM_ORIGIN.offset(sample[0], sample[1], sample[2])).isAir()) {
            solidSamples++;
         }
      }

      return solidSamples >= 3 ? true : !level.getEntitiesOfClass(AncientGolemEntity.class, dungeonBounds().inflate(3.0), entity -> true).isEmpty();
   }

   private static void forceChunk(ServerLevel level, BlockPos pos) {
      level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }

   private static ItemStack stack(String id) {
      Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
      return new ItemStack(item == null ? Items.AIR : item);
   }

   private static void setBaseAttribute(LivingEntity entity, Attribute attribute, double value) {
      AttributeInstance instance = entity.getAttribute(attribute);
      if (instance != null) {
         instance.setBaseValue(value);
      }
   }

   private static int rankPower(String rank) {
      return switch (rank) {
         case "B" -> 3;
         case "C" -> 2;
         default -> 1;
      };
   }

   private static String buildAllies(UUID owner, List<HunterEntity> hunters) {
      StringBuilder builder = new StringBuilder(owner.toString());

      for (HunterEntity hunter : hunters) {
         builder.append(',').append(hunter.getUUID());
      }

      return builder.toString();
   }

   private record HunterProfile(
      String rank,
      String hunterClass,
      double attack,
      double health,
      double armor,
      double speed,
      String mainHand,
      String offHand,
      int eyes,
      int topOut,
      int bottom,
      int foot,
      int hair,
      int eyeBase,
      int topIn,
      int mouth
   ) {
   }
}
