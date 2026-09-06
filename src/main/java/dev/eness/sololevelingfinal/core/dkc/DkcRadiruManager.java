package dev.eness.sololevelingfinal.core.dkc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dkc.event.EsilPermitClaimEvent;
import dev.eness.sololevelingfinal.core.entity.DemonEntity;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import dev.eness.sololevelingfinal.core.entity.EsilRadiruEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.RadiruMercyChoiceStateMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.DKCDemonSpawnerProcedure;
import dev.eness.sololevelingfinal.core.procedures.XPGainProcedure;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

@EventBusSubscriber(modid = "sololeveling")
public final class DkcRadiruManager {
   public static final int FLOOR = 15;
   public static final String RESIDENT_TAG = "radiru_resident";
   public static final String TRAINING_DUMMY_TAG = "radiru_training_dummy";
   public static final String PERMIT_FLOOR_TAG = "DkcPermitFloor";
   public static final String PERMIT_OWNER_TAG = "DkcPermitOwner";
   private static final String OWNER_TAG = "radiru_owner";
   private static final String SANCTUARY_TAG = "radiru_sanctuary";
   private static final String STATION_TAG = "radiru_training_station";
   private static final String SURRENDERED_TAG = "radiru_floor_15_surrendered";
   private static final String GATE_OPEN_TAG = "radiru_floor_15_gate_open";
   private static final String RESIDENTS_SPAWNED_TAG = "radiru_floor_15_residents_spawned";
   private static final String TOWER_SEALED_TAG = "radiru_floor_15_tower_sealed";
   private static final String STATE_SCHEMA_TAG = "radiru_state_schema";
   private static final String NOTICE_COOLDOWN = "radiru_pact_notice_cooldown";
   private static final String DAMAGE_WINDOW_START = "radiru_dummy_damage_window_start";
   private static final String DAMAGE_WINDOW_TOTAL = "radiru_dummy_damage_window_total";
   private static final String DAMAGE_WINDOW_HITS = "radiru_dummy_damage_window_hits";
   private static final String DAMAGE_DISPLAY_AFTER = "radiru_dummy_display_after";
   private static final String PERMIT_REISSUE_AFTER = "radiru_permit_reissue_after";
   private static final int STATE_SCHEMA = 1;
   private static final int FLOOR_XP = 1500;
   private static final int EXECUTION_BONUS_XP = 2500;
   private static final long ENSURE_INTERVAL = 100L;
   private static final int MERCY_CHOICE_TICKS = 600;
   private static final Map<UUID, DkcRadiruManager.PendingMercyChoice> PENDING_MERCY_CHOICES = new HashMap<>();
   private static final double[] DUMMY_ARMOR = new double[]{0.0, 8.0, 15.0, 22.0, 28.0, 30.0};
   private static final double[] DUMMY_TOUGHNESS = new double[]{0.0, 0.0, 4.0, 8.0, 12.0, 20.0};
   private static final String[] DUMMY_NAMES = new String[]{
      "Unarmored Demon", "Demon Soldier", "Armored Demon", "Demon Knight", "Elite Demon Knight", "Royal Guard"
   };

   private DkcRadiruManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         PENDING_MERCY_CHOICES.remove(player.getUUID());
         sendMercyChoiceState(player, false);
      }
   }

   public static void onDefendersOverpowered(ServerLevel level, ServerPlayer player) {
      if (validOwnerOnFloor(level, player) && !(variables(player).dkc_cleared >= 15.0)) {
         CompoundTag data = player.getPersistentData();
         data.putBoolean("radiru_floor_15_surrendered", true);
         if (DkcFloorBuilder.openRadiruGate(level, player)) {
            data.putBoolean("radiru_floor_15_gate_open", true);
         }

         ensureEsil(level, player);
         spawnInitialResidents(level, player);
         notify(player, -2601729, "HOUSE RADIRU SURRENDERS", "The castle gate is open. Esil waits inside with an Entry Permit.", 150);
      }
   }

   public static void tick(ServerPlayer player) {
      if (player != null && player.level() instanceof ServerLevel level && validOwnerOnFloor(level, player)) {
         SololevelingModVariables.PlayerVariables vars = migrateLegacyState(player);
         CompoundTag data = player.getPersistentData();
         if (data.getBoolean("radiru_floor_15_surrendered") || vars.radiru_pact || vars.radiru_slaughtered) {
            if (level.getGameTime() % 100L == Math.floorMod(player.getId(), 100L)) {
               reconcileLoadedCastle(level, player, vars, false);
            }
         }
      }
   }

   public static void onFloorEntered(ServerPlayer player) {
      if (player != null
         && player.level() instanceof ServerLevel level
         && validOwnerOnFloor(level, player)
         && player.server != null
         && DkcRunSavedData.get(player.server).isGenerated(player, 15)) {
         SololevelingModVariables.PlayerVariables vars = migrateLegacyState(player);
         if (!player.getPersistentData().getBoolean("radiru_floor_15_surrendered") && !vars.radiru_pact && !vars.radiru_slaughtered) {
            purgeLoadedRadiruActors(level, player);
         } else {
            reconcileLoadedCastle(level, player, vars, true);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && player.server != null) {
         SololevelingMod.queueServerWork(player.server, 1, () -> {
            if (!player.hasDisconnected()) {
               onFloorEntered(player);
            }
         });
      }
   }

   @SubscribeEvent
   public static void onPlayerLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         PENDING_MERCY_CHOICES.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      PENDING_MERCY_CHOICES.entrySet().removeIf(entry -> entry.getValue().server() == event.getServer());
   }

   private static SololevelingModVariables.PlayerVariables migrateLegacyState(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables vars = variables(player);
      CompoundTag data = player.getPersistentData();
      boolean firstMigration = data.getInt("radiru_state_schema") < 1;
      boolean legacyPact = firstMigration && vars.dkc_cleared >= 15.0 && !vars.radiru_pact && !vars.radiru_slaughtered;
      boolean unlockSideQuest = (vars.radiru_pact || legacyPact) && vars.dkc_cleared >= 20.0 && !vars.radiru_side_quest_unlocked;
      if (legacyPact || unlockSideQuest) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (legacyPact) {
               capability.radiru_pact = true;
            }

            if (unlockSideQuest) {
               capability.radiru_side_quest_unlocked = true;
            }

            capability.syncPlayerVariables(player);
         });
         vars = variables(player);
      }

      if (vars.radiru_pact) {
         data.putBoolean("radiru_floor_15_surrendered", true);
      }

      if (firstMigration) {
         data.putInt("radiru_state_schema", 1);
      }

      return vars;
   }

   private static void reconcileLoadedCastle(ServerLevel level, ServerPlayer player, SololevelingModVariables.PlayerVariables vars, boolean forcePhysicalState) {
      CompoundTag data = player.getPersistentData();
      boolean gateShouldOpen = data.getBoolean("radiru_floor_15_surrendered") || vars.dkc_cleared >= 15.0;
      if (gateShouldOpen && (forcePhysicalState || !data.getBoolean("radiru_floor_15_gate_open")) && DkcFloorBuilder.openRadiruGate(level, player)) {
         data.putBoolean("radiru_floor_15_gate_open", true);
      }

      boolean towerShouldSeal = vars.dkc_cleared >= 20.0 && !vars.radiru_slaughtered;
      if (towerShouldSeal && (forcePhysicalState || !data.getBoolean("radiru_floor_15_tower_sealed")) && DkcFloorBuilder.sealRadiruTower(level, player)) {
         data.putBoolean("radiru_floor_15_tower_sealed", true);
      }

      if (!vars.radiru_slaughtered && (data.getBoolean("radiru_floor_15_surrendered") || vars.radiru_pact)) {
         ensureEsil(level, player);
         ensureResidents(level, player);
      }

      if (vars.radiru_pact) {
         ensureTrainingDummies(level, player);
      }
   }

   public static void onCastleConquered(ServerPlayer player) {
      if (player != null) {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         if (vars.radiru_pact && !vars.radiru_slaughtered && !vars.radiru_side_quest_unlocked) {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.radiru_side_quest_unlocked = true;
               capability.syncPlayerVariables(player);
            });
            notify(player, -2601729, "SECRET QUEST UNLOCKED", "A House Beyond the Gate - Radiru Castle is now bound to your System.", 180);
         }
      }
   }

   public static void normalizeDebugProgress(ServerPlayer player, int cleared) {
      if (player != null) {
         closeMercyChoice(player);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (cleared < 15) {
               capability.radiru_pact = false;
               capability.radiru_slaughtered = false;
               capability.radiru_side_quest_unlocked = false;
            } else {
               if (!capability.radiru_pact && !capability.radiru_slaughtered) {
                  capability.radiru_pact = true;
               }

               capability.radiru_side_quest_unlocked = cleared >= 20 && capability.radiru_pact && !capability.radiru_slaughtered;
            }

            capability.syncPlayerVariables(player);
         });
         CompoundTag data = player.getPersistentData();
         data.putInt("radiru_state_schema", 1);
         if (cleared < 15) {
            data.remove("radiru_floor_15_surrendered");
            data.remove("radiru_floor_15_gate_open");
            data.remove("radiru_floor_15_residents_spawned");
            data.remove("radiru_floor_15_tower_sealed");
            String prefix = "dkc_floor_15";
            data.remove(prefix + "_spawned");
            data.remove(prefix + "_initial_spawned");
            data.remove(prefix + "_spawning");
            data.remove(prefix + "_complete");
            data.remove(prefix + "_killed");
            data.remove(prefix + "_required");
            data.remove(prefix + "_demon_count");
            data.remove(prefix + "_knight_count");
            data.remove(prefix + "_miniboss_spawned");
            data.remove(prefix + "_spawn_retry_after");
            DKCDemonSpawnerProcedure.invalidateAttempt(player, 15);
            if (player.server != null) {
               ServerLevel dkc = player.server.getLevel(DkcFloorRegistry.SHARED_DIMENSION);
               if (dkc != null) {
                  DKCDemonSpawnerProcedure.discardOwnedWave(dkc, player, 15);
               }
            }
         }

         if (cleared < 20) {
            data.remove("radiru_floor_15_tower_sealed");
         }
      }
   }

   public static void reconcileDebugProgress(ServerPlayer player, int cleared) {
      if (player != null && player.level() instanceof ServerLevel level && validOwnerOnFloor(level, player)) {
         if (cleared < 15) {
            purgeLoadedRadiruActors(level, player);
         }

         DkcFloorBuilder.prepareFloor(player, 15);
         if (cleared >= 15) {
            onFloorEntered(player);
         }
      }
   }

   public static void resetFailedEncounter(ServerLevel level, ServerPlayer player) {
      if (validOwnerOnFloor(level, player) && !(variables(player).dkc_cleared >= 15.0)) {
         closeMercyChoice(player);
         CompoundTag data = player.getPersistentData();
         data.remove("radiru_floor_15_surrendered");
         data.remove("radiru_floor_15_gate_open");
         data.remove("radiru_floor_15_residents_spawned");
         DkcFloorBuilder.closeRadiruGate(level, player);
         purgeLoadedRadiruActors(level, player);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onPermitClaim(EsilPermitClaimEvent event) {
      EsilRadiruEntity esil = event.esil();
      ServerPlayer player = event.player();
      if (esil.level() instanceof ServerLevel level && validMercyInteraction(level, player, esil)) {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         if (vars.radiru_pact && !vars.radiru_slaughtered && vars.dkc_cleared >= 15.0) {
            if (!vars.radiru_side_quest_unlocked && !(vars.dkc_cleared >= 20.0)) {
               DkcRunSavedData runs = DkcRunSavedData.get(player.server);
               long now = level.getGameTime();
               if (runs.isTransitionArmed(player, 15)) {
                  event.deny();
               } else if (!hasEntryPermit(player) && !hasNearbyFloorPermit(level, player)) {
                  if (now < player.getPersistentData().getLong("radiru_permit_reissue_after")) {
                     player.displayClientMessage(Component.literal("§5Your Floor 15 permit is still accounted for."), true);
                     event.deny();
                  } else {
                     give(player, createFloorPermit(player));
                     player.getPersistentData().putLong("radiru_permit_reissue_after", now + 200L);
                     event.grantPermit();
                     notify(player, -4633345, "RADIRU PERMIT RESTORED", "Esil replaced the lost Floor 15 permit. It cannot open any other floor.", 120);
                  }
               }
            }
         } else if (vars.radiru_slaughtered || vars.radiru_pact || vars.dkc_cleared >= 15.0) {
            event.deny();
         } else if (!isExecutionReady(player)) {
            event.deny();
         } else {
            openMercyChoice(level, player, esil);
            event.deny();
            event.setCanceled(true);
         }
      } else {
         event.deny();
      }
   }

   public static void resolveMercyChoice(ServerPlayer player, boolean spare) {
      if (player != null) {
         DkcRadiruManager.PendingMercyChoice pending = PENDING_MERCY_CHOICES.remove(player.getUUID());
         sendMercyChoiceState(player, false);
         if (spare
            && pending != null
            && player.server == pending.server()
            && player.level() instanceof ServerLevel level
            && level.getGameTime() <= pending.expiresAt()) {
            if (level.getEntity(pending.esilId()) instanceof EsilRadiruEntity esil && validMercyInteraction(level, player, esil)) {
               SololevelingModVariables.PlayerVariables vars = variables(player);
               if (!vars.radiru_pact && !vars.radiru_slaughtered && !(vars.dkc_cleared >= 15.0) && isExecutionReady(player)) {
                  finalizePact(level, player, esil);
               }
            }
         }
      }
   }

   private static void openMercyChoice(ServerLevel level, ServerPlayer player, EsilRadiruEntity esil) {
      long expiresAt = level.getGameTime() + 600L;
      PENDING_MERCY_CHOICES.put(player.getUUID(), new DkcRadiruManager.PendingMercyChoice(player.server, esil.getUUID(), expiresAt));
      esil.getLookControl().setLookAt(player, 30.0F, 30.0F);
      player.displayClientMessage(
         Component.translatable("dialogue.sololeveling.esil.speech", esil.getDisplayName(), Component.translatable("dialogue.sololeveling.esil.mercy.plea")),
         false
      );
      sendMercyChoiceState(player, true);
      MinecraftServer server = player.server;
      UUID playerId = player.getUUID();
      SololevelingMod.queueServerWork(server, 600, () -> expireMercyChoice(server, playerId, expiresAt));
   }

   private static void expireMercyChoice(MinecraftServer server, UUID playerId, long expiresAt) {
      DkcRadiruManager.PendingMercyChoice pending = PENDING_MERCY_CHOICES.get(playerId);
      if (pending != null && pending.server() == server && pending.expiresAt() == expiresAt) {
         PENDING_MERCY_CHOICES.remove(playerId);
         ServerPlayer player = server.getPlayerList().getPlayer(playerId);
         if (player != null) {
            sendMercyChoiceState(player, false);
         }
      }
   }

   private static void finalizePact(ServerLevel level, ServerPlayer player, EsilRadiruEntity esil) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.radiru_pact = true;
         capability.radiru_slaughtered = false;
         capability.dkc_cleared = Math.max(capability.dkc_cleared, 15.0);
         capability.syncPlayerVariables(player);
      });
      VesselProgressionManager.reconcileEntitlements(player);
      player.getPersistentData().putInt("radiru_state_schema", 1);
      player.getPersistentData().putLong("radiru_permit_reissue_after", level.getGameTime() + 200L);
      esil.getPersistentData().putBoolean("radiru_sanctuary", true);
      esil.markPermitClaimed();
      give(player, createFloorPermit(player));
      XPGainProcedure.awardBaseXp(level, player, 1500);
      ensureResidents(level, player);
      ensureTrainingDummies(level, player);
      notify(player, -4633345, "PACT OF HOUSE RADIRU", "Entry Permit received. Radiru blood may no longer be spilled here.", 150);
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void protectSanctuary(LivingAttackEvent event) {
      Entity target = event.getEntity();
      CompoundTag tag = target.getPersistentData();
      if (tag.getBoolean("radiru_training_dummy")) {
         if (creditedPlayer(event.getSource()) == null) {
            event.setCanceled(true);
         }
      } else if (tag.getBoolean("radiru_sanctuary") || isPactProtected(target)) {
         event.setCanceled(true);
         ServerPlayer attacker = creditedPlayer(event.getSource());
         if (attacker != null) {
            long now = attacker.level().getGameTime();
            if (now >= attacker.getPersistentData().getLong("radiru_pact_notice_cooldown")) {
               attacker.getPersistentData().putLong("radiru_pact_notice_cooldown", now + 40L);
               attacker.displayClientMessage(Component.literal("§5The pact forbids bloodshed inside Radiru Castle."), true);
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void meterTrainingDamage(LivingDamageEvent event) {
      if (event.getEntity().getPersistentData().getBoolean("radiru_training_dummy")) {
         ServerPlayer player = creditedPlayer(event.getSource());
         if (player == null) {
            event.setCanceled(true);
         } else {
            float damage = Math.max(0.0F, event.getAmount());
            event.getEntity().setHealth(event.getEntity().getMaxHealth());
            CompoundTag data = player.getPersistentData();
            long now = player.level().getGameTime();
            long start = data.getLong("radiru_dummy_damage_window_start");
            if (start <= 0L || now - start > 100L) {
               start = now;
               data.putLong("radiru_dummy_damage_window_start", start);
               data.putDouble("radiru_dummy_damage_window_total", 0.0);
               data.putInt("radiru_dummy_damage_window_hits", 0);
            }

            double total = data.getDouble("radiru_dummy_damage_window_total") + damage;
            int hits = data.getInt("radiru_dummy_damage_window_hits") + 1;
            data.putDouble("radiru_dummy_damage_window_total", total);
            data.putInt("radiru_dummy_damage_window_hits", hits);
            if (now >= data.getLong("radiru_dummy_display_after")) {
               data.putLong("radiru_dummy_display_after", now + 4L);
               double seconds = Math.max(0.05, (now - start + 1L) / 20.0);
               double dps = total / seconds;
               double armor = attributeValue(event.getEntity().getAttribute(Attributes.ARMOR));
               player.displayClientMessage(
                  Component.literal(String.format(Locale.ROOT, "§dTraining Hit: §f%.1f  §5Window DPS: §f%.1f  §8Armor: %.0f", damage, dps, armor)), true
               );
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void preserveTrainingTargetsAndResolveEsil(LivingDeathEvent event) {
      Entity target = event.getEntity();
      if (target.getPersistentData().getBoolean("radiru_training_dummy")) {
         restoreFromDeath(event);
      } else if (isPactProtected(target) || target instanceof EsilRadiruEntity protectedEsil && protectedEsil.isPermitClaimed()) {
         restoreFromDeath(event);
      } else if (target.getPersistentData().getBoolean("radiru_resident")) {
         ServerPlayer owner = ownerPlayer(target);
         ServerPlayer killer = creditedPlayer(event.getSource());
         if (owner != null
            && killer != null
            && owner.getUUID().equals(killer.getUUID())
            && target.level() instanceof ServerLevel level
            && validOwnerOnFloor(level, owner)) {
            SololevelingModVariables.PlayerVariables vars = variables(owner);
            if (!vars.radiru_slaughtered) {
               if (!isExecutionReady(owner)) {
                  event.setCanceled(true);
                  target.discard();
               } else {
                  if (!completeExecutionRoute(level, owner)) {
                     restoreFromDeath(event);
                  }
               }
            }
         } else {
            restoreFromDeath(event);
         }
      } else if (target instanceof EsilRadiruEntity esil) {
         UUID owner = esil.getEncounterOwner().orElse(null);
         ServerPlayer killer = creditedPlayer(event.getSource());
         if (owner == null
            || killer == null
            || !owner.equals(killer.getUUID())
            || !(esil.level() instanceof ServerLevel level && validOwnerOnFloor(level, killer))) {
            restoreFromDeath(event);
         } else if (!variables(killer).radiru_slaughtered) {
            if (!isExecutionReady(killer)) {
               event.setCanceled(true);
               esil.discard();
            } else {
               if (!completeExecutionRoute(level, killer)) {
                  restoreFromDeath(event);
               }
            }
         }
      }
   }

   private static boolean completeExecutionRoute(ServerLevel level, ServerPlayer player) {
      SololevelingModVariables.PlayerVariables vars = variables(player);
      if (!vars.radiru_pact && !vars.radiru_slaughtered && !(vars.dkc_cleared >= 15.0)) {
         closeMercyChoice(player);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.radiru_slaughtered = true;
            capability.radiru_pact = false;
            capability.dkc_cleared = Math.max(capability.dkc_cleared, 15.0);
            capability.syncPlayerVariables(player);
         });
         VesselProgressionManager.reconcileEntitlements(player);
         player.getPersistentData().putInt("radiru_state_schema", 1);
         give(player, createFloorPermit(player));
         give(player, new ItemStack(SololevelingModItems.RUNESTONE_COLD_BLOOD.get()));
         XPGainProcedure.awardBaseXp(level, player, 4000);
         notify(player, -52172, "HOUSE RADIRU HAS FALLEN", "Entry Permit, Cold Blood, and execution XP acquired.", 160);
         return true;
      } else {
         return false;
      }
   }

   private static EsilRadiruEntity ensureEsil(ServerLevel level, ServerPlayer player) {
      AABB castle = DkcFloorBuilder.radiruCastleBounds(player);
      AABB search = castle.inflate(96.0);
      List<EsilRadiruEntity> found = level.getEntitiesOfClass(EsilRadiruEntity.class, search, entity -> entity.isOwnedBy(player));
      found.sort(Comparator.comparingInt(Entity::getId));
      EsilRadiruEntity esil = found.isEmpty() ? null : found.get(0);

      for (int i = 1; i < found.size(); i++) {
         found.get(i).discard();
      }

      BlockPos pos = DkcFloorBuilder.radiruEsilPosition(player);
      if (esil == null) {
         if (!level.isPositionEntityTicking(pos)) {
            return null;
         }

         esil = SololevelingModEntities.ESIL_RADIRU.get().create(level);
         if (esil == null) {
            return null;
         }

         esil.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180.0F, 0.0F);
         esil.setEncounterOwner(player.getUUID());
         esil.setCustomName(Component.literal("Esil Radiru").withStyle(ChatFormatting.LIGHT_PURPLE));
         esil.setCustomNameVisible(true);
         if (!level.addFreshEntity(esil)) {
            esil.discard();
            return null;
         }
      }

      if (level.isPositionEntityTicking(pos)
         && (
            !castle.intersects(esil.getBoundingBox())
               || esil.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) > 36.0
               || !isOpenActorAnchor(level, esil.blockPosition())
         )) {
         esil.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
         esil.setDeltaMovement(0.0, 0.0, 0.0);
      }

      boolean sanctuary = variables(player).radiru_pact;
      esil.setEncounterOwner(player.getUUID());
      esil.setPermitClaimed(sanctuary);
      esil.setEncounterState(sanctuary ? EsilRadiruEntity.EncounterState.SANCTUARY : EsilRadiruEntity.EncounterState.SURRENDERED);
      esil.getPersistentData().putBoolean("radiru_sanctuary", sanctuary);
      return esil;
   }

   private static void spawnInitialResidents(ServerLevel level, ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      data.putBoolean("radiru_floor_15_residents_spawned", true);
      ensureResidents(level, player);
   }

   private static void ensureResidents(ServerLevel level, ServerPlayer player) {
      List<BlockPos> anchors = DkcFloorBuilder.radiruResidentPositions(player);
      AABB bounds = DkcFloorBuilder.radiruCastleBounds(player).inflate(64.0);
      List<Mob> existing = level.getEntitiesOfClass(
         Mob.class,
         bounds,
         mob -> mob.getPersistentData().getBoolean("radiru_resident") && player.getStringUUID().equals(mob.getPersistentData().getString("radiru_owner"))
      );

      for (int station = 0; station < anchors.size(); station++) {
         int index = station;
         List<Mob> stationResidents = existing.stream()
            .filter(mob -> mob.getPersistentData().getInt("radiru_training_station") == index)
            .sorted(Comparator.comparingInt(Entity::getId))
            .toList();
         Mob resident = stationResidents.isEmpty() ? null : stationResidents.get(0);

         for (int duplicate = 1; duplicate < stationResidents.size(); duplicate++) {
            stationResidents.get(duplicate).discard();
         }

         BlockPos anchor = anchors.get(station);
         if (resident == null) {
            if (!level.isPositionEntityTicking(anchor)) {
               continue;
            }

            resident = station % 2 == 0 ? SololevelingModEntities.DEMON_KNIGHT.get().create(level) : SololevelingModEntities.DEMON.get().create(level);
            if (resident == null) {
               continue;
            }

            resident.moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 180.0F, 0.0F);
            if (resident instanceof DemonEntity demon) {
               demon.randomizeAppearance();
            } else if (resident instanceof DemonKnightEntity knight) {
               knight.randomizeAppearance();
            }

            resident.setNoAi(true);
            resident.setPersistenceRequired();
            resident.setCustomName(Component.literal(station < 2 ? "Radiru Royal Guard" : "Radiru Resident").withStyle(ChatFormatting.DARK_PURPLE));
            resident.setCustomNameVisible(false);
            CompoundTag tag = resident.getPersistentData();
            tag.putBoolean("radiru_resident", true);
            tag.putBoolean("radiru_sanctuary", variables(player).radiru_pact);
            tag.putString("radiru_owner", player.getStringUUID());
            tag.putInt("radiru_training_station", station);
            if (!level.addFreshEntity(resident)) {
               resident.discard();
            }
         } else {
            boolean sanctuary = variables(player).radiru_pact;
            resident.getPersistentData().putBoolean("radiru_sanctuary", sanctuary);
            if (sanctuary) {
               resident.setHealth(resident.getMaxHealth());
            }

            if (level.isPositionEntityTicking(anchor) && resident.distanceToSqr(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5) > 2.25) {
               resident.teleportTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5);
            }
         }

         resident.setDeltaMovement(0.0, 0.0, 0.0);
      }
   }

   private static void ensureTrainingDummies(ServerLevel level, ServerPlayer player) {
      List<BlockPos> anchors = DkcFloorBuilder.radiruTrainingDummyPositions(player);
      AABB bounds = DkcFloorBuilder.radiruCastleBounds(player).inflate(64.0);
      List<Mob> existing = level.getEntitiesOfClass(
         Mob.class,
         bounds,
         mob -> mob.getPersistentData().getBoolean("radiru_training_dummy") && player.getStringUUID().equals(mob.getPersistentData().getString("radiru_owner"))
      );

      for (int station = 0; station < anchors.size(); station++) {
         int index = station;
         List<Mob> stationDummies = existing.stream()
            .filter(mob -> mob.getPersistentData().getInt("radiru_training_station") == index)
            .sorted(Comparator.comparingInt(Entity::getId))
            .toList();
         Mob dummy = stationDummies.isEmpty() ? null : stationDummies.get(0);

         for (int duplicate = 1; duplicate < stationDummies.size(); duplicate++) {
            stationDummies.get(duplicate).discard();
         }

         BlockPos anchor = anchors.get(station);
         if (dummy == null) {
            if (!level.isPositionEntityTicking(anchor)) {
               continue;
            }

            dummy = station % 2 == 0 ? SololevelingModEntities.DEMON.get().create(level) : SololevelingModEntities.DEMON_KNIGHT.get().create(level);
            if (dummy == null) {
               continue;
            }

            dummy.moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 180.0F, 0.0F);

            try {
               dummy.finalizeSpawn(level, level.getCurrentDifficultyAt(anchor), MobSpawnType.EVENT, null, null);
            } catch (RuntimeException var11) {
            }

            dummy.setNoAi(true);
            dummy.setPersistenceRequired();
            CompoundTag tag = dummy.getPersistentData();
            tag.putBoolean("radiru_training_dummy", true);
            tag.putString("radiru_owner", player.getStringUUID());
            tag.putInt("radiru_training_station", station);
            configureDummy(dummy, station);
            if (!level.addFreshEntity(dummy)) {
               dummy.discard();
               continue;
            }
         } else {
            configureDummy(dummy, station);
            if (level.isPositionEntityTicking(anchor) && dummy.distanceToSqr(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5) > 2.25) {
               dummy.teleportTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5);
            }
         }

         dummy.setDeltaMovement(0.0, 0.0, 0.0);
      }
   }

   private static boolean isOpenActorAnchor(ServerLevel level, BlockPos feet) {
      return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
         && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
   }

   private static void purgeLoadedRadiruActors(ServerLevel level, ServerPlayer player) {
      AABB bounds = DkcFloorBuilder.radiruCastleBounds(player).inflate(96.0);
      level.getEntitiesOfClass(EsilRadiruEntity.class, bounds, entity -> entity.isOwnedBy(player)).forEach(Entity::discard);
      level.getEntitiesOfClass(
            Mob.class,
            bounds,
            mob -> player.getStringUUID().equals(mob.getPersistentData().getString("radiru_owner"))
               && (mob.getPersistentData().getBoolean("radiru_resident") || mob.getPersistentData().getBoolean("radiru_training_dummy"))
         )
         .forEach(Entity::discard);
   }

   private static void configureDummy(Mob dummy, int station) {
      int index = Math.max(0, Math.min(DUMMY_ARMOR.length - 1, station));
      setBase(dummy.getAttribute(Attributes.MAX_HEALTH), 1024.0);
      setBase(dummy.getAttribute(Attributes.ARMOR), DUMMY_ARMOR[index]);
      setBase(dummy.getAttribute(Attributes.ARMOR_TOUGHNESS), DUMMY_TOUGHNESS[index]);
      setBase(dummy.getAttribute(Attributes.KNOCKBACK_RESISTANCE), 1.0);
      dummy.setHealth(dummy.getMaxHealth());
      dummy.setNoAi(true);
      dummy.setCustomName(
         Component.literal(DUMMY_NAMES[index] + "  [" + (int)DUMMY_ARMOR[index] + " Armor / " + (int)DUMMY_TOUGHNESS[index] + " Toughness]")
            .withStyle(index >= 4 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GOLD)
      );
      dummy.setCustomNameVisible(true);
   }

   private static void setBase(AttributeInstance attribute, double value) {
      if (attribute != null) {
         attribute.setBaseValue(value);
      }
   }

   private static double attributeValue(AttributeInstance attribute) {
      return attribute == null ? 0.0 : attribute.getValue();
   }

   public static ItemStack createFloorPermit(ServerPlayer player) {
      ItemStack permit = new ItemStack(SololevelingModItems.ENTRY_PERMIT.get());
      CompoundTag tag = permit.getOrCreateTag();
      tag.putInt("DkcPermitFloor", 15);
      if (player != null) {
         tag.putUUID("DkcPermitOwner", player.getUUID());
      }

      return permit;
   }

   public static boolean isPermitValidForFloor(ItemStack stack, ServerPlayer player, int floor) {
      if (stack != null && stack.is(SololevelingModItems.ENTRY_PERMIT.get())) {
         CompoundTag tag = stack.getTag();
         if (tag != null && tag.contains("DkcPermitFloor")) {
            return tag.getInt("DkcPermitFloor") != floor
               ? false
               : player == null || !tag.hasUUID("DkcPermitOwner") || player.getUUID().equals(tag.getUUID("DkcPermitOwner"));
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean canRecoverTransitionWithoutPermit(ServerPlayer player) {
      if (player == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = variables(player);
      return vars.dkc_cleared >= 15.0 && (vars.radiru_pact || vars.radiru_slaughtered) && !hasEntryPermit(player);
   }

   private static boolean hasEntryPermit(ServerPlayer player) {
      for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
         if (isPermitValidForFloor(player.getInventory().getItem(slot), player, 15)) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasNearbyFloorPermit(ServerLevel level, ServerPlayer player) {
      return !level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(12.0), item -> isPermitValidForFloor(item.getItem(), player, 15))
         .isEmpty();
   }

   private static boolean isExecutionReady(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      return data.getBoolean("radiru_floor_15_surrendered") && data.getBoolean("dkc_floor_15_complete") && variables(player).dkc_cleared < 15.0;
   }

   private static boolean isPactProtected(Entity target) {
      if (target == null || target.getPersistentData().getBoolean("radiru_training_dummy")) {
         return false;
      }

      if (target.getPersistentData().getBoolean("radiru_sanctuary")) {
         return true;
      }

      if (!(target instanceof EsilRadiruEntity) && !target.getPersistentData().getBoolean("radiru_resident")) {
         return false;
      }

      ServerPlayer owner = ownerPlayer(target);
      if (owner == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = variables(owner);
      return vars.radiru_pact && !vars.radiru_slaughtered;
   }

   private static ServerPlayer ownerPlayer(Entity target) {
      if (target != null && target.getServer() != null) {
         UUID owner = null;
         if (target instanceof EsilRadiruEntity esil) {
            owner = esil.getEncounterOwner().orElse(null);
         } else {
            String ownerText = target.getPersistentData().getString("radiru_owner");
            if (!ownerText.isBlank()) {
               try {
                  owner = UUID.fromString(ownerText);
               } catch (IllegalArgumentException ignored) {
                  return null;
               }
            }
         }

         return owner == null ? null : target.getServer().getPlayerList().getPlayer(owner);
      } else {
         return null;
      }
   }

   private static void restoreFromDeath(LivingDeathEvent event) {
      event.setCanceled(true);
      event.getEntity().setHealth(event.getEntity().getMaxHealth());
      event.getEntity().clearFire();
   }

   private static void give(ServerPlayer player, ItemStack stack) {
      if (!player.getInventory().add(stack)) {
         ItemEntity dropped = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), stack);
         dropped.setNoPickUpDelay();
         player.level().addFreshEntity(dropped);
      }
   }

   private static boolean validOwnerOnFloor(ServerLevel level, ServerPlayer player) {
      return level != null && player != null && DkcFloorRegistry.isSharedDkc(level) && DkcSpatialLayout.isPlayerInFloor(player, 15);
   }

   private static boolean validMercyInteraction(ServerLevel level, ServerPlayer player, EsilRadiruEntity esil) {
      if (level != null
         && player != null
         && esil != null
         && esil.level() == level
         && validOwnerOnFloor(level, player)
         && esil.isOwnedBy(player)
         && DkcSpatialLayout.isEntityInOwnedFloor(esil, player.getUUID(), 15)
         && !(player.distanceToSqr(esil) > 64.0)) {
         AABB castle = DkcFloorBuilder.radiruCastleBounds(player);
         return castle.intersects(player.getBoundingBox()) && castle.intersects(esil.getBoundingBox());
      } else {
         return false;
      }
   }

   private static void closeMercyChoice(ServerPlayer player) {
      if (player != null && PENDING_MERCY_CHOICES.remove(player.getUUID()) != null) {
         sendMercyChoiceState(player, false);
      }
   }

   private static void sendMercyChoiceState(ServerPlayer player, boolean open) {
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new RadiruMercyChoiceStateMessage(open));
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static ServerPlayer creditedPlayer(DamageSource source) {
      return source == null ? null : creditedPlayer(source.getEntity());
   }

   private static ServerPlayer creditedPlayer(Entity source) {
      if (source instanceof ServerPlayer player) {
         return player;
      } else if (source instanceof Projectile projectile) {
         return creditedPlayer(projectile.getOwner());
      } else if (source instanceof TamableAnimal tame) {
         return creditedPlayer(tame.getOwner());
      } else {
         if (source != null && source.getServer() != null) {
            UUID owner = ShadowMonarchManager.getShadowOwnerUUID(source);
            if (owner != null) {
               return source.getServer().getPlayerList().getPlayer(owner);
            }
         }

         return null;
      }
   }

   private static void notify(ServerPlayer player, int accent, String title, String under, int duration) {
      SystemNotifications.showTitleUnder(
         player,
         accent,
         duration,
         Component.literal(title).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
         Component.literal(under).withStyle(ChatFormatting.GRAY)
      );
   }

   private record PendingMercyChoice(MinecraftServer server, UUID esilId, long expiresAt) {
   }
}
