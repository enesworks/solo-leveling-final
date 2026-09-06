package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.ManaArrowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.RangerStateMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.BackStepProcedure;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;

@EventBusSubscriber
public final class RangerCombatManager {
   public static final String MANA_QUIVER = "Mana Quiver";
   public static final String BACK_STEP = "Back Step";
   public static final String HAWKEYE = "Hawkeye";
   public static final String RAPID_FIRE = "Rapid Fire";
   public static final String HIGH_VALUE_TARGET = "High Value Target";
   public static final String SHARPSHOOTER = "Sharpshooter";
   public static final String ARROW_SHOWER = "Arrow Shower";
   public static final String LEGACY_PROXIMITY_TRAP = "Proximity Trap";
   public static final String HYPER_FOCUS = "Hyper Focus";
   private static final ResourceKey<DamageType> RANGER_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling", "ranger"));
   private static final List<String> CORE_SKILLS = List.of(
      "Mana Quiver", "Back Step", "Hawkeye", "Rapid Fire", "High Value Target", "Sharpshooter", "Arrow Shower", "Hyper Focus"
   );
   private static final String QUIVER_ACTIVE = "slr_ranger_quiver_active";
   private static final String CORE_RECONCILED = "slr_ranger_core_reconciled_v5";
   private static final String CORE_RECONCILED_RANK = "slr_ranger_core_rank_v5";
   private static final String LOCK_TARGET = "slr_ranger_lock_target";
   private static final String HAWKEYE_UNTIL = "slr_ranger_hawkeye_until";
   private static final String HYPER_UNTIL = "slr_ranger_hyper_until";
   private static final String QUICK_DRAW_UNTIL = "slr_ranger_quick_draw_until";
   private static final String MARK_TARGET = "slr_ranger_mark_target";
   private static final String MARK_DIMENSION = "slr_ranger_mark_dimension";
   private static final String MARK_UNTIL = "slr_ranger_mark_until";
   private static final String SUNDER_OWNER = "slr_ranger_sunder_owner";
   private static final String SUNDER_UNTIL = "slr_ranger_sunder_until";
   private static final String FIVEFOLD_CHARGES = "slr_ranger_fivefold_charges";
   private static final String FIVEFOLD_UNTIL = "slr_ranger_fivefold_until";
   private static final String CLIENT_STATE_ACTIVE = "slr_ranger_client_state_active";
   private static final String HAWKEYE_SOURCE = "skill:ranger_hawkeye";
   private static final String MARK_SOURCE = "skill:ranger_mark";
   private static final int FULL_DRAW_TICKS = 20;
   private static final int STAGE_ONE_TICKS = 27;
   private static final int STAGE_TWO_TICKS = 39;
   private static final int STAGE_THREE_TICKS = 55;
   private static final int MAX_ORDINARY_MANA_ARROWS = 5;
   private static final double LOCK_RANGE = 96.0;
   private static final double LOCK_CONE_DOT = 0.996;
   private static final List<RangerCombatManager.ArrowShowerState> ARROW_SHOWERS = new ArrayList<>();
   private static final Map<UUID, RangerCombatManager.RapidFireState> RAPID_STATES = new HashMap<>();
   private static final Map<UUID, ArrayDeque<ManaArrowEntity>> ORDINARY_ARROWS = new HashMap<>();

   private RangerCombatManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         clearScheduledAttacks(player.getUUID());
         clearLock(player);
         clearMark(player);
         EntityHighlightSystem.clearSource(player, "skill:ranger_hawkeye");
         EntityHighlightSystem.clearSource(player, "skill:ranger_mark");
         player.getPersistentData().remove("slr_ranger_hawkeye_until");
         syncEmptyClient(player);
      }
   }

   public static boolean isRangerSkill(String skill) {
      return CORE_SKILLS.contains(skill) || "Proximity Trap".equals(skill);
   }

   public static boolean isManaQuiverActive(Entity entity) {
      return hasSkill(entity, "Mana Quiver") && entity.getPersistentData().getBoolean("slr_ranger_quiver_active");
   }

   public static boolean isHyperFocusActive(Entity entity) {
      return entity != null && entity.getPersistentData().getLong("slr_ranger_hyper_until") > entity.level().getGameTime();
   }

   public static boolean hasSkill(Entity entity, String skill) {
      if (entity != null && skill != null && !skill.isBlank()) {
         String list = variables(entity).Plist;
         if (list != null && !list.isBlank()) {
            for (String token : list.split(",")) {
               String clean = cleanSkillToken(token);
               if (skill.equals(clean)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void reconcileRanger(ServerPlayer player) {
      if (isRanger(player)) {
         CompoundTag data = player.getPersistentData();
         int currentRank = rangerRankTier(variables(player));
         if (!data.getBoolean("slr_ranger_core_reconciled_v5") || data.getInt("slr_ranger_core_rank_v5") != currentRank) {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
               boolean changed = false;
               if (hasExactToken(vars.Plist, "Proximity Trap")) {
                  vars.Plist = replaceSkillToken(vars.Plist, "Proximity Trap", "Arrow Shower");

                  for (int slot = 1; slot <= 16; slot++) {
                     if ("Proximity Trap".equals(SkillSlotHelper.getSlot(vars, slot))) {
                        SkillSlotHelper.setSlot(vars, slot, "Arrow Shower");
                     }
                  }

                  if ("Proximity Trap".equals(vars.PselectedPower)) {
                     vars.PselectedPower = "Arrow Shower";
                  }

                  changed = true;
               }

               int rank = rangerRankTier(vars);
               changed |= ensureSkill(vars, "Back Step");
               if (rank >= 2) {
                  changed |= ensureSkill(vars, "Hawkeye");
               }

               if (rank >= 3) {
                  changed |= ensureSkill(vars, "Rapid Fire");
               }

               if (rank >= 4) {
                  changed |= ensureSkill(vars, "High Value Target");
               }

               if (rank >= 5) {
                  changed |= ensureSkill(vars, "Sharpshooter");
               }

               if (rank >= 6) {
                  changed |= ensureSkill(vars, "Arrow Shower");
               }

               if (changed) {
                  vars.syncPlayerVariables(player);
               }
            });
            data.putBoolean("slr_ranger_core_reconciled_v5", true);
            data.putInt("slr_ranger_core_rank_v5", currentRank);
            ClassPassiveManager.syncRangerFocus(player);
            syncClient(player);
         }
      }
   }

   public static boolean grantSkill(ServerPlayer player, String skill) {
      if (player != null && skill != null && !skill.isBlank()) {
         boolean[] granted = new boolean[]{false};
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            granted[0] = ensureSkill(vars, skill);
            if (granted[0]) {
               vars.syncPlayerVariables(player);
            }
         });
         return granted[0];
      } else {
         return false;
      }
   }

   public static void learnFromRunestone(Entity entity, ItemStack stack, String skill) {
      if (entity instanceof ServerPlayer player && stack != null && !stack.isEmpty()) {
         if (!isRanger(player)) {
            player.displayClientMessage(Component.translatable("message.sololeveling.ranger.ranger_only"), false);
         } else if (hasSkill(player, skill)) {
            player.displayClientMessage(Component.translatable("message.sololeveling.ranger.skill_known"), false);
         } else if (grantSkill(player, skill)) {
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            player.displayClientMessage(Component.translatable("message.sololeveling.ranger.skill_gained", skill), false);
         }
      }
   }

   public static boolean activateSkill(ServerPlayer player, String skill) {
      if (player != null && isRangerSkill(skill)) {
         String canonicalSkill = "Proximity Trap".equals(skill) ? "Arrow Shower" : skill;
         boolean unlocked = hasSkill(player, canonicalSkill) || "Proximity Trap".equals(skill) && hasSkill(player, "Proximity Trap");
         if (!unlocked) {
            player.displayClientMessage(Component.translatable("message.sololeveling.ranger.skill_locked"), true);
            return false;
         }

         return switch (canonicalSkill) {
            case "Mana Quiver" -> toggleManaQuiver(player);
            case "Back Step" -> castEvasiveShot(player);
            case "Hawkeye" -> castHawkeye(player);
            case "Rapid Fire" -> castRapidFire(player);
            case "High Value Target" -> castHighValueTarget(player);
            case "Sharpshooter" -> castSunderShot(player);
            case "Arrow Shower" -> castArrowShower(player);
            case "Hyper Focus" -> castHyperFocus(player);
            default -> false;
         };
      } else {
         return false;
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (isRanger(player)) {
            reconcileRanger(player);
         } else if (hasRangerAccess(player)) {
            syncClient(player);
         } else {
            syncEmptyClient(player);
         }
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         clearScheduledAttacks(player.getUUID());
         player.getPersistentData().remove("slr_ranger_core_reconciled_v5");
         if (isRanger(player)) {
            reconcileRanger(player);
         } else if (hasRangerAccess(player)) {
            syncClient(player);
         } else {
            syncEmptyClient(player);
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      clearScheduledAttacks(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      RAPID_STATES.clear();
      ARROW_SHOWERS.clear();
      ORDINARY_ARROWS.clear();
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         if (!hasRangerAccess(player)) {
            player.getPersistentData().remove("slr_ranger_core_reconciled_v5");
            player.getPersistentData().remove("slr_ranger_core_rank_v5");
            if (player.getPersistentData().getBoolean("slr_ranger_client_state_active")) {
               clearLock(player);
               clearMark(player);
               EntityHighlightSystem.clearSource(player, "skill:ranger_hawkeye");
               clearScheduledAttacks(player.getUUID());
               syncEmptyClient(player);
            }
         } else {
            player.getPersistentData().putBoolean("slr_ranger_client_state_active", true);
            if (isRanger(player)
               && (
                  !player.getPersistentData().getBoolean("slr_ranger_core_reconciled_v5")
                     || player.getPersistentData().getInt("slr_ranger_core_rank_v5") != rangerRankTier(variables(player))
               )) {
               reconcileRanger(player);
            }

            tickRapidFire(player);
            tickHawkeye(player);
            tickManaArrowLock(player);
            expireCombatStates(player);
            if (player.tickCount % 2 == 0
               && (isManaQuiverActive(player) && isUsingSupportedBow(player) || player.tickCount % 20 == 0 || fivefoldCharges(player) > 0)) {
               syncClient(player);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         tickArrowShowers();
      }
   }

   @SubscribeEvent
   public static void onArrowNock(ArrowNockEvent event) {
      Player player = event.getEntity();
      if (hasSkill(player, "Mana Quiver") && isSupportedBow(event.getBow()) && shouldInterceptBow(player)) {
         player.startUsingItem(event.getHand());
         event.setAction(InteractionResultHolder.consume(event.getBow()));
      }
   }

   @SubscribeEvent
   public static void onArrowLoose(ArrowLooseEvent event) {
      Player player = event.getEntity();
      ItemStack bow = event.getBow();
      if (hasSkill(player, "Mana Quiver") && isSupportedBow(bow) && shouldInterceptBow(player)) {
         event.setCanceled(true);
         if (player instanceof ServerPlayer serverPlayer) {
            if (isFivefoldActive(serverPlayer)) {
               fireFivefoldArrow(serverPlayer, bow, event.getCharge());
            } else {
               int previewStage = chargeStage(serverPlayer, event.getCharge());
               if (previewStage <= 0) {
                  serverPlayer.displayClientMessage(Component.translatable("message.sololeveling.ranger.mana_arrow_forming"), true);
                  clearLock(serverPlayer);
                  syncClient(serverPlayer);
               } else {
                  LivingEntity locked = lockedTarget(serverPlayer);
                  int stage = previewStage >= 3 && locked != null ? 3 : Math.min(previewStage, 2);
                  double damage = manaArrowDamage(serverPlayer, bow, stage);
                  double cost = manaArrowCost(serverPlayer, damage, stage);
                  if (spendMana(serverPlayer, cost, 25 + stage * 10)) {
                     spawnManaArrow(serverPlayer, bow, stage, damage, locked, true, false, false);
                     damageBow(serverPlayer, bow);
                     serverPlayer.awardStat(Stats.ITEM_USED.get(bow.getItem()));
                     clearLock(serverPlayer);
                     syncClient(serverPlayer);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onRangerProjectileDamage(LivingHurtEvent event) {
      if (!event.isCanceled()) {
         if (event.getSource().getEntity() instanceof ServerPlayer ranger
            && isRanger(ranger)
            && event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            LivingEntity var17 = event.getEntity();
            if (MageCombatHelper.isValidTarget(ranger, var17)) {
               double distance = ranger.distanceTo(var17);
               boolean pvp = var17 instanceof Player;
               double rangeBonus = distance <= 10.0 ? 0.0 : Mth.clamp((distance - 10.0) / 22.0, 0.0, 1.0) * (pvp ? 0.08 : 0.18);
               boolean marked = isMarkedTarget(ranger, var17);
               double multiplier = 1.0 + rangeBonus + (marked ? (pvp ? 0.05 : 0.12) : 0.0);
               CompoundTag targetData = var17.getPersistentData();
               if (targetData.hasUUID("slr_ranger_sunder_owner")
                  && ranger.getUUID().equals(targetData.getUUID("slr_ranger_sunder_owner"))
                  && targetData.getLong("slr_ranger_sunder_until") > var17.level().getGameTime()) {
                  multiplier += pvp ? 0.04 : 0.08;
               }

               boolean focusEligible = !arrow.getPersistentData().getBoolean("ranger_no_focus")
                  && !var17.getPersistentData().getBoolean("radiru_training_dummy");
               if (focusEligible && ClassPassiveManager.consumeRangerFocus(ranger)) {
                  multiplier += pvp ? 0.1 : 0.25;
               }

               event.setAmount((float)Math.max(0.0, event.getAmount() * multiplier));
               if (arrow.getPersistentData().getBoolean("ranger_sunder")) {
                  targetData.putUUID("slr_ranger_sunder_owner", ranger.getUUID());
                  targetData.putLong("slr_ranger_sunder_until", var17.level().getGameTime() + (pvp ? 60 : 100));
               }

               if (focusEligible) {
                  double gain = 16.0 + (distance >= 18.0 ? 8.0 : 0.0) + (marked ? 6.0 : 0.0);
                  if (isHyperFocusActive(ranger)) {
                     gain *= 2.0;
                  }

                  ClassPassiveManager.addRangerFocus(ranger, gain);
               }

               if (arrow.getPersistentData().getBoolean("ranger_typhoon") && !arrow.getPersistentData().getBoolean("ranger_typhoon_triggered")) {
                  arrow.getPersistentData().putBoolean("ranger_typhoon_triggered", true);
                  triggerTyphoonImpact(ranger, var17, event.getAmount());
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onRangerDamaged(LivingHurtEvent event) {
      if (!event.isCanceled()) {
         if (event.getEntity() instanceof ServerPlayer ranger && isRanger(ranger)) {
            Entity attacker = event.getSource().getEntity();
            if (attacker != null && attacker.distanceToSqr(ranger) <= 36.0) {
               ClassPassiveManager.addRangerFocus(ranger, -20.0);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onMarkedTargetDeath(LivingDeathEvent event) {
      if (!event.getEntity().level().isClientSide()) {
         MinecraftServer server = event.getEntity().getServer();
         if (server != null) {
            for (ServerPlayer ranger : server.getPlayerList().getPlayers()) {
               if (isMarkedTarget(ranger, event.getEntity())) {
                  if (event.getEntity() instanceof Player) {
                     clearMark(ranger);
                  } else {
                     refundMana(ranger, 200.0);
                     ranger.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
                        if (vars.rangerleapnum < 3.0) {
                           double reducedTimer = vars.rangerleaptimer - 60.0;
                           if (reducedTimer <= 0.0) {
                              vars.rangerleapnum = Math.min(3.0, vars.rangerleapnum + 1.0);
                              vars.rangerleaptimer = vars.rangerleapnum >= 3.0 ? 0.0 : 180.0;
                           } else {
                              vars.rangerleaptimer = reducedTimer;
                           }
                        }

                        vars.syncPlayerVariables(ranger);
                     });
                     clearMark(ranger);
                  }
               }
            }
         }
      }
   }

   private static boolean toggleManaQuiver(ServerPlayer player) {
      boolean enabled = !player.getPersistentData().getBoolean("slr_ranger_quiver_active");
      player.getPersistentData().putBoolean("slr_ranger_quiver_active", enabled);
      clearLock(player);
      player.displayClientMessage(
         Component.translatable(enabled ? "message.sololeveling.ranger.mana_quiver_on" : "message.sololeveling.ranger.mana_quiver_off"), true
      );
      player.level().playSound((Player)null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.55F, enabled ? 1.45F : 0.75F);
      syncClient(player);
      return true;
   }

   private static boolean castEvasiveShot(ServerPlayer player) {
      if (variables(player).rangerleapnum <= 0.0) {
         player.displayClientMessage(Component.translatable("message.sololeveling.ranger.no_evasive_charges"), true);
         return false;
      }

      if (!canAffordMana(player, 200.0)) {
         return false;
      }

      if (!BackStepProcedure.execute(player)) {
         return false;
      }

      spendMana(player, 200.0, 25);
      player.getPersistentData().putLong("slr_ranger_quick_draw_until", player.level().getGameTime() + 40L);
      ItemStack bow = heldBow(player);
      if (!bow.isEmpty()) {
         spawnManaArrow(player, bow, 1, manaArrowDamage(player, bow, 1) * 0.55, null, false, true, false);
      }

      return true;
   }

   private static boolean castHawkeye(ServerPlayer player) {
      if (CooldownManager.isOnCooldown(player, "Hawkeye")) {
         return cooldownMessage(player, "Hawkeye");
      }

      if (!spendMana(player, 350.0, 40)) {
         return false;
      }

      player.getPersistentData().putLong("slr_ranger_hawkeye_until", player.level().getGameTime() + 360L);
      CooldownManager.set(player, "Hawkeye", 520);
      player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 380, 0, false, false));
      player.displayClientMessage(Component.translatable("message.sololeveling.ranger.hawkeye"), true);
      return true;
   }

   private static boolean castRapidFire(ServerPlayer player) {
      if (CooldownManager.isOnCooldown(player, "Rapid Fire")) {
         return cooldownMessage(player, "Rapid Fire");
      }

      ItemStack bow = heldBow(player);
      if (bow.isEmpty()) {
         player.displayClientMessage(Component.translatable("message.sololeveling.ranger.bow_required"), true);
         return false;
      }

      if (!spendMana(player, 550.0, 45)) {
         return false;
      }

      CooldownManager.set(player, "Rapid Fire", 180);
      RAPID_STATES.put(player.getUUID(), new RangerCombatManager.RapidFireState(player.level().dimension(), bow.copy(), player.level().getGameTime(), 0));
      damageBow(player, bow);
      player.displayClientMessage(Component.translatable("message.sololeveling.ranger.rapid_fire"), true);
      return true;
   }

   private static boolean castHighValueTarget(ServerPlayer player) {
      if (CooldownManager.isOnCooldown(player, "High Value Target")) {
         return cooldownMessage(player, "High Value Target");
      }

      LivingEntity target = raycastTarget(player, 64.0);
      if (target == null) {
         player.displayClientMessage(Component.translatable("message.sololeveling.ranger.no_target"), true);
         return false;
      }

      if (!spendMana(player, 300.0, 35)) {
         return false;
      }

      long duration = target instanceof Player ? 120L : 300L;
      CompoundTag data = player.getPersistentData();
      if (data.hasUUID("slr_ranger_mark_target")) {
         clearMark(player);
      }

      data.putUUID("slr_ranger_mark_target", target.getUUID());
      data.putString("slr_ranger_mark_dimension", target.level().dimension().location().toString());
      data.putLong("slr_ranger_mark_until", player.level().getGameTime() + duration);
      CooldownManager.set(player, "High Value Target", 240);
      EntityHighlightSystem.show(player, target, "skill:ranger_mark", 16762941, (int)duration, 160);
      player.displayClientMessage(Component.translatable("message.sololeveling.ranger.target_marked", target.getDisplayName()), true);
      return true;
   }

   private static boolean castSunderShot(ServerPlayer player) {
      if (CooldownManager.isOnCooldown(player, "Sharpshooter")) {
         return cooldownMessage(player, "Sharpshooter");
      }

      ItemStack bow = heldBow(player);
      if (bow.isEmpty()) {
         player.displayClientMessage(Component.translatable("message.sololeveling.ranger.bow_required"), true);
         return false;
      }

      boolean evolved = hasAllCoreSkills(player);
      double manaCost = evolved ? 1050.0 : 800.0;
      if (!spendMana(player, manaCost, evolved ? 70 : 55)) {
         return false;
      }

      CooldownManager.set(player, "Sharpshooter", evolved ? 300 : 240);
      ManaArrowEntity arrow = spawnManaArrow(player, bow, 2, manaArrowDamage(player, bow, 2) * (evolved ? 2.65 : 2.2), null, false, true, true);
      if (evolved && arrow != null) {
         arrow.getPersistentData().putBoolean("ranger_typhoon", true);
      }

      damageBow(player, bow);
      player.displayClientMessage(
         Component.translatable(evolved ? "message.sololeveling.ranger.typhoon_shot" : "message.sololeveling.ranger.sunder_shot"), true
      );
      return true;
   }

   private static boolean castArrowShower(ServerPlayer player) {
      if (CooldownManager.isOnCooldown(player, "Arrow Shower")) {
         return cooldownMessage(player, "Arrow Shower");
      }

      HitResult hit = player.pick(56.0, 1.0F, false);
      Vec3 center = hit.getType() == Type.MISS ? player.getEyePosition().add(player.getLookAngle().scale(36.0)) : hit.getLocation().add(0.0, 0.12, 0.0);
      if (!spendMana(player, 1100.0, 65)) {
         return false;
      }

      CooldownManager.set(player, "Arrow Shower", 400);
      CooldownManager.set(player, "Proximity Trap", 400);
      ARROW_SHOWERS.removeIf(state -> state.ownerId.equals(player.getUUID()));
      double pulseDamage = manaBaseDamage(player, heldBow(player)) * 0.45;
      ARROW_SHOWERS.add(new RangerCombatManager.ArrowShowerState(player.getUUID(), player.level().dimension(), center, pulseDamage, 0));
      if (player.level() instanceof ServerLevel level) {
         level.sendParticles(ParticleTypes.ENCHANT, center.x, center.y + 0.2, center.z, 32, 5.0, 0.05, 5.0, 0.1);
         level.playSound((Player)null, BlockPos.containing(center), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.9F, 1.45F);
      }

      player.displayClientMessage(Component.translatable("message.sololeveling.ranger.arrow_shower"), true);
      return true;
   }

   private static boolean castHyperFocus(ServerPlayer player) {
      if (CooldownManager.isOnCooldown(player, "Hyper Focus")) {
         return cooldownMessage(player, "Hyper Focus");
      }

      boolean evolved = hasAllCoreSkills(player);
      if (evolved) {
         if (ClassPassiveManager.getRangerFocus(player) < 100.0) {
            player.displayClientMessage(Component.translatable("message.sololeveling.ranger.deadeye_required"), true);
            return false;
         }

         double cost = Math.max(900.0, variables(player).Mana * 0.08);
         if (!spendMana(player, cost, 80)) {
            return false;
         }

         ClassPassiveManager.consumeRangerFocus(player);
         CompoundTag data = player.getPersistentData();
         data.putInt("slr_ranger_fivefold_charges", 5);
         data.putLong("slr_ranger_fivefold_until", player.level().getGameTime() + 200L);
         CooldownManager.set(player, "Hyper Focus", 960);
         player.displayClientMessage(Component.translatable("message.sololeveling.ranger.fivefold"), true);
         syncClient(player);
         return true;
      } else {
         if (!spendMana(player, 900.0, 60)) {
            return false;
         }

         player.getPersistentData().putLong("slr_ranger_hyper_until", player.level().getGameTime() + 160L);
         CooldownManager.set(player, "Hyper Focus", 600);
         player.displayClientMessage(Component.translatable("message.sololeveling.ranger.hyper_focus"), true);
         return true;
      }
   }

   private static void tickRapidFire(ServerPlayer player) {
      RangerCombatManager.RapidFireState state = RAPID_STATES.get(player.getUUID());
      if (state != null) {
         if (player.isAlive() && state.dimension.equals(player.level().dimension())) {
            long now = player.level().getGameTime();
            if (now >= state.nextShotTick) {
               ItemStack bow = state.bow;
               double damage = manaArrowDamage(player, bow, 1) * 0.48;
               ManaArrowEntity arrow = spawnManaArrow(player, bow, 2, damage, null, false, true, false);
               if (arrow != null) {
                  arrow.getPersistentData().putBoolean("ranger_no_focus", state.shotsFired > 0);
               }

               int fired = state.shotsFired + 1;
               if (fired >= 5) {
                  RAPID_STATES.remove(player.getUUID());
               } else {
                  RAPID_STATES.put(player.getUUID(), new RangerCombatManager.RapidFireState(state.dimension, state.bow, now + 5L, fired));
               }
            }
         } else {
            RAPID_STATES.remove(player.getUUID());
         }
      }
   }

   private static void tickHawkeye(ServerPlayer player) {
      long now = player.level().getGameTime();
      if (player.getPersistentData().getLong("slr_ranger_hawkeye_until") > now && player.tickCount % 10 == 0) {
         Vec3 eye = player.getEyePosition();
         Vec3 look = player.getLookAngle();
         List<LivingEntity> candidates = player.level()
            .getEntitiesOfClass(
               LivingEntity.class,
               player.getBoundingBox().inflate(48.0),
               targetx -> MageCombatHelper.isValidTarget(player, targetx) && targetx.getBoundingBox().getCenter().subtract(eye).normalize().dot(look) >= 0.93
            )
            .stream()
            .sorted(Comparator.comparingDouble(player::distanceToSqr))
            .limit(16L)
            .toList();
         int shown = 0;

         for (LivingEntity target : candidates) {
            if (player.hasLineOfSight(target)) {
               EntityHighlightSystem.show(player, target, "skill:ranger_hawkeye", EntityHighlightSystem.perceptionColor(target), 16, 120);
               if (++shown >= 5) {
                  break;
               }
            }
         }
      }
   }

   private static void tickManaArrowLock(ServerPlayer player) {
      if (isManaQuiverActive(player) && isUsingSupportedBow(player) && maxManaArrowStage(player) >= 3) {
         CompoundTag data = player.getPersistentData();
         if (data.hasUUID("slr_ranger_lock_target")) {
            if (entityByUuid(player.serverLevel(), data.getUUID("slr_ranger_lock_target")) instanceof LivingEntity living && validLockTarget(player, living)) {
               return;
            }

            clearLock(player);
         }

         if (player.isCrouching()) {
            LivingEntity candidate = findInstantLockTarget(player);
            if (candidate != null) {
               data.putUUID("slr_ranger_lock_target", candidate.getUUID());
               player.level().playSound((Player)null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.45F, 1.9F);
            }
         }
      } else {
         clearLock(player);
      }
   }

   private static void tickArrowShowers() {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null && !ARROW_SHOWERS.isEmpty()) {
         Iterator<RangerCombatManager.ArrowShowerState> iterator = ARROW_SHOWERS.iterator();

         while (iterator.hasNext()) {
            RangerCombatManager.ArrowShowerState state = iterator.next();
            ServerLevel level = server.getLevel(state.dimension);
            ServerPlayer owner = server.getPlayerList().getPlayer(state.ownerId);
            if (level != null && owner != null && owner.level() == level && owner.isAlive()) {
               int age = state.age + 1;
               if (age >= 16 && age <= 56 && (age - 16) % 10 == 0) {
                  pulseArrowShower(level, owner, state.center, state.damage, (age - 16) / 10);
               }

               if (age > 60) {
                  iterator.remove();
               } else {
                  state.age = age;
               }
            } else {
               iterator.remove();
            }
         }
      }
   }

   private static void pulseArrowShower(ServerLevel level, ServerPlayer owner, Vec3 center, double snapshottedDamage, int pulse) {
      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 6.0, center.z, 24, 4.2, 2.0, 4.2, 0.03);
      level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 2.5, center.z, 12, 4.5, 2.5, 4.5, 0.04);
      List<LivingEntity> targets = level.getEntitiesOfClass(
            LivingEntity.class, new AABB(center, center).inflate(7.0, 4.0, 7.0), targetx -> MageCombatHelper.isValidTarget(owner, targetx)
         )
         .stream()
         .sorted(Comparator.comparingDouble(targetx -> targetx.distanceToSqr(center)))
         .limit(32L)
         .toList();
      float damage = (float)Math.max(0.25, snapshottedDamage);

      for (LivingEntity target : targets) {
         boolean hurt = target.hurt(
            new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RANGER_DAMAGE), owner), damage
         );
         if (hurt) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 16, 0, false, false));
         }
      }

      if (pulse == 0 || pulse == 4) {
         level.playSound((Player)null, BlockPos.containing(center), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 0.85F, 1.2F + pulse * 0.08F);
      }
   }

   private static void triggerTyphoonImpact(ServerPlayer ranger, LivingEntity primary, float primaryDamage) {
      if (ranger.level() instanceof ServerLevel level) {
         Vec3 var11 = primary.getBoundingBox().getCenter();
         level.sendParticles(ParticleTypes.CLOUD, var11.x, var11.y, var11.z, 28, 2.2, 1.2, 2.2, 0.16);
         level.sendParticles(ParticleTypes.END_ROD, var11.x, var11.y, var11.z, 16, 1.8, 1.0, 1.8, 0.08);
         level.playSound((Player)null, primary.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.75F, 1.65F);
         float splashDamage = Math.max(1.0F, primaryDamage * 0.42F);

         for (LivingEntity target : level.getEntitiesOfClass(
               LivingEntity.class, primary.getBoundingBox().inflate(4.5), targetx -> targetx != primary && MageCombatHelper.isValidTarget(ranger, targetx)
            )
            .stream()
            .sorted(Comparator.comparingDouble(primary::distanceToSqr))
            .limit(12L)
            .toList()) {
            boolean hurt = target.hurt(
               new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RANGER_DAMAGE), ranger), splashDamage
            );
            if (hurt) {
               Vec3 push = target.position().subtract(var11);
               if (push.lengthSqr() > 0.001) {
                  push = push.normalize().scale(target instanceof Player ? 0.35 : 0.7);
                  target.push(push.x, 0.16, push.z);
                  target.hurtMarked = true;
               }
            }
         }
      }
   }

   private static void fireFivefoldArrow(ServerPlayer player, ItemStack bow, int charge) {
      if (charge < 6) {
         player.displayClientMessage(Component.translatable("message.sololeveling.ranger.fivefold_forming"), true);
      } else {
         int charges = fivefoldCharges(player);
         if (charges > 0) {
            ManaArrowEntity arrow = spawnManaArrow(player, bow, 2, manaBaseDamage(player, bow) * 1.2, null, false, true, false);
            if (arrow != null) {
               arrow.getPersistentData().putBoolean("ranger_no_focus", true);
            }

            player.getPersistentData().putInt("slr_ranger_fivefold_charges", charges - 1);
            damageBow(player, bow);
            if (charges - 1 <= 0) {
               player.getPersistentData().remove("slr_ranger_fivefold_until");
            }

            syncClient(player);
         }
      }
   }

   private static ManaArrowEntity spawnManaArrow(
      ServerPlayer player, ItemStack bow, int stage, double damage, LivingEntity lockedTarget, boolean ordinary, boolean noFocus, boolean sunder
   ) {
      ManaArrowEntity arrow = new ManaArrowEntity(SololevelingModEntities.MANA_ARROW.get(), player, player.level());

      double speed = switch (stage) {
         case 2 -> 3.65;
         case 3 -> 3.55;
         default -> 3.2;
      };
      if (player.getPersistentData().getLong("slr_ranger_hawkeye_until") > player.level().getGameTime()) {
         speed *= 1.1;
      }

      if (isHyperFocusActive(player)) {
         speed *= 1.15;
      }

      arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float)speed, stage >= 2 ? 0.0F : 0.35F);
      arrow.setBaseDamage(Math.max(0.25, damage));
      int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
      if (punch > 0) {
         arrow.setKnockback(punch);
      }

      if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
         arrow.setSecondsOnFire(100);
      }

      arrow.setCritArrow(stage >= 2);
      if (sunder) {
         arrow.setPierceLevel((byte)3);
      }

      arrow.configureRangerShot(
         stage, lockedTarget == null ? null : lockedTarget.getUUID(), lockedTarget == null ? 0.0 : player.distanceTo(lockedTarget), ordinary
      );
      if (noFocus) {
         arrow.getPersistentData().putBoolean("ranger_no_focus", true);
      }

      if (sunder) {
         arrow.getPersistentData().putBoolean("ranger_sunder", true);
      }

      player.level().addFreshEntity(arrow);
      if (ordinary) {
         trackOrdinaryArrow(player, arrow);
      }

      player.level().playSound((Player)null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.9F, 1.15F + stage * 0.13F);
      return arrow;
   }

   private static double manaArrowDamage(ServerPlayer player, ItemStack bow, int stage) {
      double multiplier = switch (stage) {
         case 2 -> 1.23;
         case 3 -> 1.38;
         default -> 1.0;
      };
      return manaBaseDamage(player, bow) * multiplier;
   }

   private static double manaBaseDamage(ServerPlayer player, ItemStack bow) {
      SololevelingModVariables.PlayerVariables vars = variables(player);
      double statBonus = Math.min(8.0, vars.perception / 55.0 + TemporaryStatBonusManager.effectiveIntelligence(player) / 120.0);
      int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
      double enchantment = power > 0 ? 0.5 + power * 0.5 : 0.0;
      return 3.0 + statBonus + enchantment;
   }

   private static double manaArrowCost(ServerPlayer player, double predictedDamage, int stage) {
      SololevelingModVariables.PlayerVariables vars = variables(player);
      double base = 90.0 + Math.max(1000.0, vars.Mana) * 0.01 + predictedDamage * 4.0;

      double multiplier = switch (stage) {
         case 2 -> 1.75;
         case 3 -> 2.75;
         default -> 1.0;
      };
      if (isHyperFocusActive(player)) {
         multiplier *= 0.8;
      }

      return Math.ceil(base * multiplier);
   }

   private static int chargeStage(ServerPlayer player, int charge) {
      int max = maxManaArrowStage(player);
      if (charge >= adjustedThreshold(player, 55) && max >= 3) {
         return 3;
      } else if (charge >= adjustedThreshold(player, 39) && max >= 2) {
         return 2;
      } else {
         return charge >= adjustedThreshold(player, 27) ? 1 : 0;
      }
   }

   private static int adjustedThreshold(ServerPlayer player, int threshold) {
      int extra = Math.max(0, threshold - 20);
      if (isHyperFocusActive(player)) {
         extra = (int)Math.ceil(extra * 0.8);
      }

      int result = 20 + extra;
      if (player.getPersistentData().getLong("slr_ranger_quick_draw_until") > player.level().getGameTime()) {
         result = Math.max(12, result - 5);
      }

      return result;
   }

   private static int maxManaArrowStage(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables vars = variables(player);
      int rank = rangerRankTier(vars);
      int systemLevel = Math.max(0, (int)Math.floor(vars.Level));
      if (rank >= 5 || systemLevel >= 80) {
         return 3;
      } else {
         return rank < 4 && systemLevel < 60 ? 1 : 2;
      }
   }

   private static int rangerRankTier(SololevelingModVariables.PlayerVariables vars) {
      return Mth.clamp((int)Math.round(vars.HunterRank), 1, 6);
   }

   private static void expireCombatStates(ServerPlayer player) {
      long now = player.level().getGameTime();
      CompoundTag data = player.getPersistentData();
      if (data.getLong("slr_ranger_mark_until") <= now && data.hasUUID("slr_ranger_mark_target")) {
         clearMark(player);
      }

      if (data.getLong("slr_ranger_fivefold_until") <= now && data.getInt("slr_ranger_fivefold_charges") > 0) {
         data.putInt("slr_ranger_fivefold_charges", 0);
         data.remove("slr_ranger_fivefold_until");
      }
   }

   private static void trackOrdinaryArrow(ServerPlayer player, ManaArrowEntity arrow) {
      ArrayDeque<ManaArrowEntity> arrows = ORDINARY_ARROWS.computeIfAbsent(player.getUUID(), ignored -> new ArrayDeque<>());
      arrows.removeIf(existing -> existing == null || existing.isRemoved());

      while (arrows.size() >= 5) {
         ManaArrowEntity oldest = arrows.pollFirst();
         if (oldest != null && !oldest.isRemoved()) {
            oldest.discard();
         }
      }

      arrows.addLast(arrow);
   }

   private static LivingEntity raycastTarget(ServerPlayer player, double range) {
      Vec3 eye = player.getEyePosition();
      Vec3 end = eye.add(player.getLookAngle().scale(range));
      BlockHitResult blockHit = player.level().clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, player));
      double maxDistanceSq = blockHit.getType() == Type.MISS ? range * range : eye.distanceToSqr(blockHit.getLocation());
      AABB search = player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0);
      EntityHitResult hit = ProjectileUtil.getEntityHitResult(
         player, eye, end, search, candidate -> candidate instanceof LivingEntity livingx && MageCombatHelper.isValidTarget(player, livingx), maxDistanceSq
      );
      return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
   }

   private static LivingEntity findInstantLockTarget(ServerPlayer player) {
      LivingEntity direct = raycastTarget(player, 96.0);
      if (direct != null) {
         return direct;
      }

      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      Vec3 end = eye.add(look.scale(96.0));
      AABB searchBox = new AABB(eye, end).inflate(9.0);
      return player.level()
         .getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            target -> MageCombatHelper.isValidTarget(player, target) && player.distanceToSqr(target) <= 9216.0 && lockAlignment(eye, look, target) >= 0.996
         )
         .stream()
         .filter(target -> canSeeLockTarget(player, target))
         .sorted(Comparator.<LivingEntity>comparingDouble(target -> -lockAlignment(eye, look, target)).thenComparingDouble(player::distanceToSqr))
         .findFirst()
         .orElse(null);
   }

   private static double lockAlignment(Vec3 eye, Vec3 look, LivingEntity target) {
      Vec3 towardTarget = target.getBoundingBox().getCenter().subtract(eye);
      return towardTarget.lengthSqr() < 1.0E-4 ? 1.0 : towardTarget.normalize().dot(look);
   }

   private static LivingEntity lockedTarget(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      if (!data.hasUUID("slr_ranger_lock_target")) {
         return null;
      } else {
         return entityByUuid(player.serverLevel(), data.getUUID("slr_ranger_lock_target")) instanceof LivingEntity living && validLockTarget(player, living)
            ? living
            : null;
      }
   }

   private static boolean validLockTarget(ServerPlayer player, LivingEntity target) {
      return MageCombatHelper.isValidTarget(player, target)
         && target.level() == player.level()
         && player.distanceToSqr(target) <= 9216.0
         && canSeeLockTarget(player, target);
   }

   private static boolean canSeeLockTarget(ServerPlayer player, LivingEntity target) {
      Vec3 eye = player.getEyePosition();
      return hasClearLockPath(player, eye, target.getBoundingBox().getCenter()) || hasClearLockPath(player, eye, target.getEyePosition());
   }

   private static boolean hasClearLockPath(ServerPlayer player, Vec3 from, Vec3 to) {
      BlockHitResult obstruction = player.level().clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, player));
      return obstruction.getType() != Type.BLOCK || obstruction.getLocation().distanceToSqr(from) + 0.25 >= to.distanceToSqr(from);
   }

   private static Entity entityByUuid(ServerLevel level, UUID uuid) {
      return level != null && uuid != null ? level.getEntity(uuid) : null;
   }

   private static boolean isMarkedTarget(ServerPlayer ranger, Entity target) {
      CompoundTag data = ranger.getPersistentData();
      return target != null
         && data.hasUUID("slr_ranger_mark_target")
         && target.getUUID().equals(data.getUUID("slr_ranger_mark_target"))
         && data.getLong("slr_ranger_mark_until") > ranger.level().getGameTime()
         && target.level().dimension().location().toString().equals(data.getString("slr_ranger_mark_dimension"));
   }

   private static void clearMark(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      if (data.hasUUID("slr_ranger_mark_target")) {
         ResourceLocation storedDimension = ResourceLocation.tryParse(data.getString("slr_ranger_mark_dimension"));
         ResourceKey<Level> dimension = storedDimension == null ? player.level().dimension() : ResourceKey.create(Registries.DIMENSION, storedDimension);
         EntityHighlightSystem.hide(player, data.getUUID("slr_ranger_mark_target"), dimension, "skill:ranger_mark");
      }

      data.remove("slr_ranger_mark_target");
      data.remove("slr_ranger_mark_dimension");
      data.remove("slr_ranger_mark_until");
   }

   private static void clearLock(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      data.remove("slr_ranger_lock_target");
   }

   private static boolean spendMana(ServerPlayer player, double amount, int regenLockTicks) {
      if (player.isCreative()) {
         return true;
      } else {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         if (vars.MP + 1.0E-4 < amount) {
            player.displayClientMessage(Component.translatable("message.sololeveling.ranger.not_enough_mana", (int)Math.ceil(amount)), true);
            return false;
         } else {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(cap -> {
               cap.MP = Math.max(0.0, cap.MP - amount);
               cap.syncPlayerVariables(player);
            });
            CooldownManager.set(player, "mana_refresh", regenLockTicks);
            return true;
         }
      }
   }

   private static boolean canAffordMana(ServerPlayer player, double amount) {
      if (player.isCreative()) {
         return true;
      }

      if (variables(player).MP + 1.0E-4 >= amount) {
         return true;
      }

      player.displayClientMessage(Component.translatable("message.sololeveling.ranger.not_enough_mana", (int)Math.ceil(amount)), true);
      return false;
   }

   private static void refundMana(ServerPlayer player, double amount) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(cap -> {
         cap.MP = Math.min(cap.Mana, cap.MP + Math.max(0.0, amount));
         cap.syncPlayerVariables(player);
      });
   }

   private static boolean cooldownMessage(ServerPlayer player, String skill) {
      player.displayClientMessage(
         Component.translatable("message.sololeveling.ranger.cooldown", skill, CooldownManager.getRemainingSeconds(player, skill)), true
      );
      return false;
   }

   private static void damageBow(ServerPlayer player, ItemStack bow) {
      if (!player.isCreative() && !bow.isEmpty()) {
         InteractionHand hand = player.getOffhandItem() == bow ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         bow.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
      }
   }

   private static ItemStack heldBow(Player player) {
      if (isSupportedBow(player.getMainHandItem())) {
         return player.getMainHandItem();
      } else {
         return isSupportedBow(player.getOffhandItem()) ? player.getOffhandItem() : ItemStack.EMPTY;
      }
   }

   private static boolean isUsingSupportedBow(Player player) {
      return player != null && player.isUsingItem() && isSupportedBow(player.getUseItem());
   }

   private static boolean isSupportedBow(ItemStack stack) {
      return stack != null && !stack.isEmpty() && stack.getItem() instanceof BowItem;
   }

   private static boolean isRanger(Entity entity) {
      return entity != null && Math.round(variables(entity).Classes) == 6L;
   }

   private static boolean hasRangerAccess(Entity entity) {
      if (isRanger(entity)) {
         return true;
      }

      for (String skill : CORE_SKILLS) {
         if (hasSkill(entity, skill)) {
            return true;
         }
      }

      return hasSkill(entity, "Proximity Trap");
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static boolean ensureSkill(SololevelingModVariables.PlayerVariables vars, String skill) {
      if (hasExactToken(vars.Plist, skill)) {
         return false;
      }

      String list = vars.Plist == null ? "" : vars.Plist.trim();
      if (list.isEmpty()) {
         list = ".";
      }

      if (!list.endsWith(",")) {
         list = list + ",";
      }

      vars.Plist = list + skill + ",";
      return true;
   }

   private static boolean hasExactToken(String list, String skill) {
      if (list != null && !list.isBlank()) {
         for (String token : list.split(",")) {
            if (skill.equals(cleanSkillToken(token))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static String replaceSkillToken(String list, String oldSkill, String newSkill) {
      if (list != null && !list.isBlank()) {
         List<String> skills = new ArrayList<>();

         for (String token : list.split(",")) {
            String clean = cleanSkillToken(token);
            if (!clean.isEmpty()) {
               String mapped = oldSkill.equals(clean) ? newSkill : clean;
               if (!skills.contains(mapped)) {
                  skills.add(mapped);
               }
            }
         }

         return "." + String.join(",", skills) + ",";
      } else {
         return "." + newSkill + ",";
      }
   }

   private static String cleanSkillToken(String token) {
      if (token == null) {
         return "";
      }

      String clean = token.trim();

      while (clean.startsWith(".")) {
         clean = clean.substring(1);
      }

      return clean;
   }

   private static boolean hasAllCoreSkills(Entity player) {
      if (player == null) {
         return false;
      }

      for (String skill : CORE_SKILLS) {
         if (!hasSkill(player, skill)) {
            return false;
         }
      }

      return true;
   }

   private static boolean isFivefoldActive(Player player) {
      return player != null && fivefoldCharges(player) > 0 && player.getPersistentData().getLong("slr_ranger_fivefold_until") > player.level().getGameTime();
   }

   private static boolean shouldInterceptBow(Player player) {
      if (player == null) {
         return false;
      } else {
         return player.level().isClientSide()
            ? RangerClientState.quiverActive || RangerClientState.fivefoldCharges > 0
            : isManaQuiverActive(player) || isFivefoldActive(player);
      }
   }

   private static int fivefoldCharges(Player player) {
      return player == null ? 0 : Math.max(0, player.getPersistentData().getInt("slr_ranger_fivefold_charges"));
   }

   private static void syncClient(ServerPlayer player) {
      if (player != null) {
         int stage = isUsingSupportedBow(player) ? chargeStage(player, player.getTicksUsingItem()) : 0;
         CompoundTag data = player.getPersistentData();
         float lockProgress = data.hasUUID("slr_ranger_lock_target") ? 1.0F : 0.0F;
         RangerStateMessage message = new RangerStateMessage(
            isManaQuiverActive(player),
            stage,
            maxManaArrowStage(player),
            lockProgress,
            data.hasUUID("slr_ranger_lock_target"),
            fivefoldCharges(player),
            data.getLong("slr_ranger_hawkeye_until") > player.level().getGameTime(),
            isHyperFocusActive(player)
         );
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message);
      }
   }

   private static void syncEmptyClient(ServerPlayer player) {
      if (player != null) {
         player.getPersistentData().remove("slr_ranger_client_state_active");
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new RangerStateMessage(false, 0, 1, 0.0F, false, 0, false, false));
      }
   }

   private static void clearScheduledAttacks(UUID playerId) {
      if (playerId != null) {
         RAPID_STATES.remove(playerId);
         ARROW_SHOWERS.removeIf(state -> state.ownerId.equals(playerId));
         ArrayDeque<ManaArrowEntity> arrows = ORDINARY_ARROWS.remove(playerId);
         if (arrows != null) {
            for (ManaArrowEntity arrow : arrows) {
               if (arrow != null && !arrow.isRemoved()) {
                  arrow.discard();
               }
            }
         }
      }
   }

   public static List<Component> tooltip(Entity entity, String skill) {
      List<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));

      String description = switch (skill) {
         case "Mana Quiver" -> "Toggle mana ammunition. Linear unlocks at B rank or System Lv60; Seeking at A rank or System Lv80.";
         case "Back Step" -> "Retreat safely, fire a covering shot, preserve Focus, and accelerate the next draw.";
         case "Hawkeye" -> "Reveal visible hostiles in your aiming cone and sharpen long-range shots.";
         case "Rapid Fire" -> "Fire five controlled spectral arrows while freely adjusting your aim.";
         case "High Value Target" -> "Mark one visible target to increase Ranger damage and Focus generation.";
         case "Sharpshooter" -> hasAllCoreSkills(entity)
            ? "Typhoon Shot: pierce the formation and detonate a controlled windburst on first impact."
            : "Fire a piercing Sunder Shot that exposes a target to subsequent arrows.";
         case "Arrow Shower", "Proximity Trap" -> "Rain five bounded volleys onto a distant target area.";
         case "Hyper Focus" -> hasAllCoreSkills(entity)
            ? "Fivefold Execution: manifest five rapid precision arrows."
            : "Accelerate Mana Arrow charging and double Focus generation.";
         default -> "";
      };
      lines.add(Component.literal(description).withStyle(ChatFormatting.GRAY));
      return lines;
   }

   private static final class ArrowShowerState {
      private final UUID ownerId;
      private final ResourceKey<Level> dimension;
      private final Vec3 center;
      private final double damage;
      private int age;

      private ArrowShowerState(UUID ownerId, ResourceKey<Level> dimension, Vec3 center, double damage, int age) {
         this.ownerId = ownerId;
         this.dimension = dimension;
         this.center = center;
         this.damage = damage;
         this.age = age;
      }
   }

   private record RapidFireState(ResourceKey<Level> dimension, ItemStack bow, long nextShotTick, int shotsFired) {
   }
}
