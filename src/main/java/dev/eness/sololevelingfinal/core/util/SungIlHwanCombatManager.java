package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.network.SungIlHwanAttackMessage;
import dev.eness.sololevelingfinal.core.network.SungIlHwanVfxEventMessage;

@EventBusSubscriber(modid = "sololeveling")
public final class SungIlHwanCombatManager {
   public static final String SKILL_PREDATORS_PRESENCE = "Predator's Presence";
   public static final String SKILL_ASSASSIN_STANCE = "Assassin Stance";
   public static final String SKILL_SPATIAL_EXECUTION = "Spatial Execution";
   public static final String SKILL_SPIRITUALIZATION = "Spiritualization";
   public static final int STAGE_NONE = 0;
   public static final int STAGE_ONE = 1;
   public static final int STAGE_TWO = 2;
   public static final int MAX_RISK = 100;
   private static final String IDENTITY = "sung_il_hwan";
   private static final String STATE_ROOT = "slr_sung_il_hwan";
   private static final String STATE_STAGE = "stage";
   private static final String STATE_STAGE_REMAINING = "stage_remaining";
   private static final String STATE_EXHAUSTION_REMAINING = "exhaustion_remaining";
   private static final String STATE_RISK = "rulers_fracture";
   private static final String STATE_RECOVERY_AT = "risk_recovery_at";
   private static final String STATE_PRESENCE = "presence";
   private static final String STATE_STANCE = "assassin_stance";
   private static final String STATE_LAST_DRAIN = "last_mana_drain";
   private static final String STATE_COMBO = "dagger_combo";
   private static final String STATE_COMBO_AT = "dagger_combo_at";
   private static final String SPIRITUALIZATION_AURA = "sung_il_hwan_spiritualization";
   private static final String PLAYER_AURA_KEY = "sololeveling_player_aura";
   private static final String FEAR_PREFIX = "slr_sih_fear_";
   private static final String FEAR_EXPIRY_PREFIX = "slr_sih_fear_expiry_";
   private static final String SEVER_COOLDOWN = "sih_spatial_sever";
   private static final int STAGE_TWO_DURATION = 360;
   private static final int EXHAUSTION_DURATION = 480;
   private static final int FEAR_LIFETIME = 160;
   private static final int MAX_AURA_TARGETS = 20;
   private static final int MAX_EXECUTION_TARGETS = 96;
   private static final int MAX_PENDING_FRACTURES = 96;
   private static final int MAX_ASSASSIN_LINE_TARGETS = 24;
   private static final int MAX_CHARGE_TICKS = 100;
   private static final int EXECUTION_FRACTURE_DELAY = 20;
   private static final double EXECUTION_START_RADIUS = 3.0;
   private static final double EXECUTION_MAX_RADIUS = 15.0;
   private static final double EXECUTION_STAGE_TWO_MAX_RADIUS = 19.0;
   private static final TagKey<Item> DAGGERS = TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "dagger"));
   private static final Map<UUID, SungIlHwanCombatManager.ChargeState> EXECUTION_CHARGES = new HashMap<>();
   private static final Map<UUID, SungIlHwanCombatManager.ExecutionTraversal> EXECUTION_TRAVERSALS = new HashMap<>();
   private static final Map<UUID, List<SungIlHwanCombatManager.FractureState>> PENDING_FRACTURES = new HashMap<>();
   private static final Map<UUID, Long> LAST_STANCE_ATTACK = new HashMap<>();
   private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

   private SungIlHwanCombatManager() {
   }

   public static boolean isSkill(String skillName) {
      return "Predator's Presence".equals(skillName)
         || "Assassin Stance".equals(skillName)
         || "Spatial Execution".equals(skillName)
         || "Spiritualization".equals(skillName);
   }

   public static int skillColor(String skillName) {
      return switch (skillName) {
         case "Predator's Presence", "Assassin Stance", "Spatial Execution", "Spiritualization" -> 16765774;
         default -> 16777215;
      };
   }

   public static Component tooltip(Entity entity, String skillName) {
      return switch (skillName) {
         case "Predator's Presence" -> Component.literal(
            "Toggle a silent pressure aura that builds Fear on visible enemies. Fear weakens prey and empowers Spatial Execution."
         );
         case "Assassin Stance" -> Component.literal("Adopt a dagger stance for rapid spatial combinations. Use a dagger on prey to perform Spatial Sever.");
         case "Spatial Execution" -> Component.literal(
            "Hold to form an execution field, then release to cut Fear-marked enemies before delayed spatial fractures erupt."
         );
         case "Spiritualization" -> Component.literal(
            "Toggle Stage I for sustained Ruler enhancement. While active, sneak and use again to commit to a fixed, non-cancellable Stage II followed by exhaustion; dying during Stage II or exhaustion causes recoverable Ruler's Fracture."
         );
         default -> Component.empty();
      };
   }

   public static boolean isSungIlHwanVessel(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = variables(entity);
      return DeveloperModeManager.isEnabled(entity) && (int)vars.JOB == 7 && ("sung_il_hwan".equals(vars.vesselIdentity) || vars.vesselIdentity.isBlank());
   }

   public static boolean shouldReplaceBasicAttack(Entity entity) {
      if (!(entity instanceof Player player && isSungIlHwanVessel(player) && isDagger(player.getMainHandItem()))) {
         return false;
      } else {
         return player.level().isClientSide() ? SungIlHwanAttackMessage.isClientStanceActive() : state(player).getBoolean("assassin_stance");
      }
   }

   public static boolean performAssassinLineCut(ServerPlayer player) {
      if (player != null && player.isAlive() && shouldReplaceBasicAttack(player) && player.level().hasChunkAt(player.blockPosition())) {
         long now = player.level().getGameTime();
         Long lastAttack = LAST_STANCE_ATTACK.get(player.getUUID());
         int minimumInterval = stanceAttackIntervalTicks(player);
         if (lastAttack != null && now - lastAttack < minimumInterval) {
            return false;
         }

         LAST_STANCE_ATTACK.put(player.getUUID(), now);
         float cooledStrength = Math.max(0.75F, player.getAttackStrengthScale(0.5F));
         CompoundTag stanceState = state(player);
         int currentStage = stage(player);
         boolean overloaded = currentStage == 2;
         int maxCombo = overloaded ? 4 : 3;
         int combo = now - stanceState.getLong("dagger_combo_at") <= 16L ? stanceState.getInt("dagger_combo") % maxCombo + 1 : 1;
         stanceState.putInt("dagger_combo", combo);
         stanceState.putLong("dagger_combo_at", now);
         Vec3 direction = player.getLookAngle().normalize();
         if (direction.lengthSqr() < 0.001) {
            direction = new Vec3(0.0, 0.0, 1.0);
         }

         double range = overloaded ? 12.0 : 9.5;
         double width = overloaded ? 2.8 : 2.1;
         Vec3 origin = player.getEyePosition().add(direction.scale(0.3));
         Vec3 endpoint = origin.add(direction.scale(range));
         double strength = TemporaryStatBonusManager.effectiveStrength(player);
         double agility = TemporaryStatBonusManager.effectiveAgility(player);
         double stageScale = overloaded ? 1.55 : (currentStage == 1 ? 1.22 : 1.0);
         double riskScale = 1.0 - clampRisk(stanceState.getInt("rulers_fracture")) * 0.0025;
         double swingScale = 0.2 + cooledStrength * cooledStrength * 0.8;
         double weaponDamage = Math.max(1.0, player.getAttributeValue(Attributes.ATTACK_DAMAGE));
         float damage = (float)((weaponDamage + 1.5 + strength / 34.0 + agility / 28.0) * (1.0 + (combo - 1) * 0.12) * stageScale * riskScale * swingScale);
         boolean confirmedHit = false;

         for (LivingEntity target : targetsAlongLine(player, origin, direction, range, width, overloaded ? 24 : 10)) {
            boolean hit = hurtInternally(player, target, damage);
            confirmedHit |= hit;
            if (hit) {
               addFear(player, target, 5 + combo * 2 + (overloaded ? 4 : 0));
            }
         }

         int seed = visualSeed(player) ^ combo * 73244475;
         SungIlHwanVfxEventMessage.sendSpatialSlash(player, null, origin, endpoint, overloaded ? 3 + combo % 2 : combo - 1, confirmedHit, seed);
         if (overloaded) {
            Vec3 horizontalRight = new Vec3(-direction.z, 0.0, direction.x);
            if (horizontalRight.lengthSqr() > 0.001) {
               horizontalRight = horizontalRight.normalize();
            }

            SungIlHwanVfxEventMessage.sendSpatialSlash(
               player,
               null,
               origin.add(horizontalRight.scale(1.25)).add(0.0, -0.65, 0.0),
               endpoint.add(horizontalRight.scale(-1.25)).add(0.0, 0.8, 0.0),
               5,
               confirmedHit,
               seed ^ 20903
            );
            SungIlHwanVfxEventMessage.sendSpatialSlash(
               player,
               null,
               origin.add(horizontalRight.scale(-1.0)).add(0.0, 0.7, 0.0),
               endpoint.add(horizontalRight.scale(1.0)).add(0.0, -0.55, 0.0),
               6,
               confirmedHit,
               seed ^ 12057
            );
         }

         player.resetAttackStrengthTicker();
         player.level()
            .playSound(
               (Player)null,
               player.blockPosition(),
               SoundEvents.PLAYER_ATTACK_SWEEP,
               SoundSource.PLAYERS,
               overloaded ? 1.15F : 0.9F,
               overloaded ? 1.42F : 1.62F
            );
         return true;
      } else {
         return false;
      }
   }

   public static int fearTier(int fear) {
      int bounded = Math.max(0, fear);
      return bounded >= 75 ? 3 : (bounded >= 50 ? 2 : (bounded >= 25 ? 1 : 0));
   }

   public static int fearCap(LivingEntity target) {
      if (target instanceof Player) {
         return 50;
      } else {
         return isBossLike(target) ? 75 : 100;
      }
   }

   public static double fearPowerScale(double casterPower, double targetPower) {
      double safeCaster = Math.max(1.0, casterPower);
      double safeTarget = Math.max(1.0, targetPower);
      return Mth.clamp(Math.sqrt(safeCaster / safeTarget), 0.35, 1.65);
   }

   public static double fearPowerScale(ServerPlayer caster, LivingEntity target) {
      return fearPowerScale(rulerPower(caster), targetPower(target));
   }

   public static int scaledFearGain(double baseGain, double powerScale) {
      return Mth.clamp((int)Math.round(Math.max(0.0, baseGain) * Mth.clamp(powerScale, 0.25, 2.0)), 1, 24);
   }

   public static int scaledFearGain(ServerPlayer caster, LivingEntity target) {
      int base = stage(caster) == 2 ? 10 : (stage(caster) == 1 ? 7 : 5);
      return scaledFearGain(base, fearPowerScale(caster, target));
   }

   public static boolean canCommitStageTwo(Entity entity) {
      if (entity instanceof ServerPlayer player && isSungIlHwanVessel(player)) {
         CompoundTag state = state(player);
         return state.getInt("stage") == 1 && state.getInt("exhaustion_remaining") <= 0 && player.isAlive() && player.isShiftKeyDown();
      } else {
         return false;
      }
   }

   public static int clampRisk(int risk) {
      return Mth.clamp(risk, 0, 100);
   }

   public static int executionChargeTier(int pressedMs) {
      int heldTicks = Math.max(0, pressedMs / 50);
      return heldTicks >= 50 ? 3 : (heldTicks >= 28 ? 2 : (heldTicks >= 12 ? 1 : 0));
   }

   public static void press(Entity entity, String skillName) {
      if (entity instanceof ServerPlayer player && isSungIlHwanVessel(player) && player.isAlive()) {
         switch (skillName) {
            case "Predator's Presence":
               togglePresence(player);
               break;
            case "Assassin Stance":
               toggleAssassinStance(player);
               break;
            case "Spatial Execution":
               beginSpatialExecution(player);
               break;
            case "Spiritualization":
               useSpiritualization(player);
         }
      }
   }

   public static void release(Entity entity, String skillName, int pressedMs) {
      if (entity instanceof ServerPlayer player && "Spatial Execution".equals(skillName)) {
         releaseSpatialExecution(player, pressedMs);
      }
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         EXECUTION_CHARGES.remove(player.getUUID());
         abortExecutionTraversal(player, true);
         PENDING_FRACTURES.remove(player.getUUID());
         LAST_STANCE_ATTACK.remove(player.getUUID());
         CompoundTag outer = player.getPersistentData();
         if (outer.contains("PlayerPersisted", 10)) {
            CompoundTag persisted = outer.getCompound("PlayerPersisted");
            persisted.remove("slr_sung_il_hwan");
            outer.put("PlayerPersisted", persisted);
         }

         clearSpiritualizationAura(player);
         SungIlHwanAttackMessage.syncStance(player, false);
         SungIlHwanVfxEventMessage.sendExecutionCancel(player, visualSeed(player));
         SungIlHwanVfxEventMessage.sendStageEnd(player, visualSeed(player));
      }
   }

   public static void tick(ServerPlayer player) {
      if (player != null) {
         if (!isSungIlHwanVessel(player)) {
            teardownAfterIdentityLoss(player);
         } else {
            CompoundTag state = state(player);
            long now = player.level().getGameTime();
            int currentStage = Mth.clamp(state.getInt("stage"), 0, 2);
            if (currentStage == 2) {
               int stageRemaining = Math.max(0, state.getInt("stage_remaining"));
               if (stageRemaining <= 0) {
                  finishStageTwo(player, state);
                  currentStage = 0;
               } else {
                  state.putInt("stage_remaining", stageRemaining - 1);
               }
            }

            if (currentStage == 1) {
               applyStageEffects(player, false);
               if (now % 900L == 0L) {
                  SungIlHwanVfxEventMessage.sendStage(player, 1, 1200, visualSeed(player));
               }

               if (now - state.getLong("last_mana_drain") >= 20L) {
                  state.putLong("last_mana_drain", now);
                  if (!consumeManaSilently(player, 16 + state.getInt("rulers_fracture") / 20)) {
                     deactivateStageOne(player, state, true);
                  }
               }
            } else if (currentStage == 2) {
               applyStageEffects(player, true);
            }

            int exhaustionRemaining = Math.max(0, state.getInt("exhaustion_remaining"));
            if (exhaustionRemaining > 0) {
               state.putInt("exhaustion_remaining", exhaustionRemaining - 1);
               int riskTier = Math.max(0, state.getInt("rulers_fracture") / 25);
               player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 12, Math.min(3, 1 + riskTier), false, false, true));
               player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, Math.min(2, riskTier), false, false, true));
               if (now % 5L == 0L) {
                  CooldownManager.setFullDuration(player, "mana_refresh", 8);
               }
            } else {
               recoverRisk(player, state, now);
            }

            if (state.getBoolean("presence")) {
               tickPresence(player, state, now);
            }

            if (state.getBoolean("assassin_stance") && !hasDagger(player)) {
               state.putBoolean("assassin_stance", false);
               SungIlHwanAttackMessage.syncStance(player, false);
               player.displayClientMessage(Component.literal("Assassin Stance ended: equip a dagger").withStyle(ChatFormatting.GRAY), true);
            }

            tickExecutionCharge(player, now);
            tickExecutionTraversal(player, now);
            tickFractures(player, now);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         tick(player);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onAttackEntity(AttackEntityEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && !INTERNAL_DAMAGE.contains(player.getUUID())) {
         if (shouldReplaceBasicAttack(player) && replaceOrAlreadyReplacedAssassinAttack(player)) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onAssassinLivingAttack(LivingAttackEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer player
         && event.getSource().getDirectEntity() == player
         && !INTERNAL_DAMAGE.contains(player.getUUID())
         && shouldReplaceBasicAttack(player)) {
         if (replaceOrAlreadyReplacedAssassinAttack(player)) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public static void onDaggerDamage(LivingHurtEvent event) {
      if (event.getEntity() instanceof ServerPlayer victim && isSungIlHwanVessel(victim) && state(victim).getInt("exhaustion_remaining") > 0) {
         int risk = state(victim).getInt("rulers_fracture");
         event.setAmount(event.getAmount() * (1.16F + Mth.clamp(risk, 0, 100) * 0.0018F));
      }

      if (event.getSource().getEntity() instanceof ServerPlayer player
         && !INTERNAL_DAMAGE.contains(player.getUUID())
         && isSungIlHwanVessel(player)
         && state(player).getBoolean("assassin_stance")
         && isDagger(player.getMainHandItem())
         && MageCombatHelper.isValidTarget(player, event.getEntity())) {
         CompoundTag state = state(player);
         long now = player.level().getGameTime();
         int maxCombo = stage(player) == 2 ? 4 : 3;
         int combo = now - state.getLong("dagger_combo_at") <= 16L ? state.getInt("dagger_combo") % maxCombo + 1 : 1;
         state.putInt("dagger_combo", combo);
         state.putLong("dagger_combo_at", now);
         double strength = TemporaryStatBonusManager.effectiveStrength(player);
         double agility = TemporaryStatBonusManager.effectiveAgility(player);
         double stageScale = stage(player) == 2 ? 1.45 : (stage(player) == 1 ? 1.2 : 1.0);
         double riskScale = 1.0 - state.getInt("rulers_fracture") * 0.0025;
         float addition = (float)((2.0 + strength / 34.0 + agility / 28.0) * (1.0 + (combo - 1) * 0.13) * stageScale * riskScale);
         event.setAmount(event.getAmount() + addition);
         addFear(player, event.getEntity(), 5 + combo * 2);
         Vec3 from = player.getEyePosition().add(player.getLookAngle().scale(0.35));
         Vec3 to = event.getEntity().getBoundingBox().getCenter();
         SungIlHwanVfxEventMessage.sendSpatialSlash(player, event.getEntity(), from, to, combo - 1, true, visualSeed(player) ^ event.getEntity().getId());
         if (stage(player) == 2) {
            Vec3 crossFrom = to.add(combo % 2 == 0 ? -1.0 : 1.0, 0.55, combo % 2 == 0 ? 0.7 : -0.7);
            SungIlHwanVfxEventMessage.sendSpatialSlash(
               player, event.getEntity(), crossFrom, to, combo + 2, true, visualSeed(player) ^ event.getEntity().getId() ^ 20903
            );
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void applyExhaustionAndFractureDamage(LivingHurtEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer player && isSungIlHwanVessel(player) && event.getEntity() != player) {
         CompoundTag state = state(player);
         float multiplier = 1.0F - clampRisk(state.getInt("rulers_fracture")) * 0.0015F;
         if (state.getInt("exhaustion_remaining") > 0) {
            multiplier *= 0.72F;
         }

         event.setAmount(Math.max(0.0F, event.getAmount() * multiplier));
      }
   }

   @SubscribeEvent
   public static void onEntityInteract(EntityInteract event) {
      if (event.getHand() == InteractionHand.MAIN_HAND && event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity target) {
         if (trySpatialSever(player, target)) {
            event.setCanceled(true);
            player.swing(InteractionHand.MAIN_HAND, true);
         }
      }
   }

   @SubscribeEvent
   public static void onRightClickItem(RightClickItem event) {
      if (event.getHand() == InteractionHand.MAIN_HAND
         && event.getEntity() instanceof ServerPlayer player
         && isSungIlHwanVessel(player)
         && state(player).getBoolean("assassin_stance")
         && isDagger(player.getMainHandItem())) {
         LivingEntity target = findLookTarget(player, stage(player) == 2 ? 12.0 : 8.0);
         if (target != null && trySpatialSever(player, target)) {
            event.setCanceled(true);
            player.swing(InteractionHand.MAIN_HAND, true);
         }
      }
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && isSungIlHwanVessel(player)) {
         CompoundTag state = state(player);
         boolean stageTwo = state.getInt("stage") == 2;
         boolean exhausted = state.getInt("exhaustion_remaining") > 0;
         if (stageTwo || exhausted) {
            int increase = stageTwo ? 22 : 12;
            state.putInt("rulers_fracture", clampRisk(state.getInt("rulers_fracture") + increase));
            state.putInt("stage", 0);
            state.putInt("stage_remaining", 0);
            state.putInt("exhaustion_remaining", Math.max(state.getInt("exhaustion_remaining"), 480));
            CooldownManager.setFullDuration(player, "mana_refresh", 12);
         }

         EXECUTION_CHARGES.remove(player.getUUID());
         state.putBoolean("assassin_stance", false);
         SungIlHwanAttackMessage.syncStance(player, false);
         clearSpiritualizationAura(player);
         abortExecutionTraversal(player, false);
         PENDING_FRACTURES.remove(player.getUUID());
         LAST_STANCE_ATTACK.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onClone(Clone event) {
      if (event.getOriginal() instanceof ServerPlayer original && event.getEntity() instanceof ServerPlayer replacement) {
         CompoundTag oldOuter = original.getPersistentData();
         if (oldOuter.contains("PlayerPersisted", 10)) {
            CompoundTag oldPersisted = oldOuter.getCompound("PlayerPersisted");
            if (oldPersisted.contains("slr_sung_il_hwan", 10)) {
               CompoundTag newOuter = replacement.getPersistentData();
               CompoundTag newPersisted = newOuter.contains("PlayerPersisted", 10) ? newOuter.getCompound("PlayerPersisted") : new CompoundTag();
               newPersisted.put("slr_sung_il_hwan", oldPersisted.getCompound("slr_sung_il_hwan").copy());
               newOuter.put("PlayerPersisted", newPersisted);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && isSungIlHwanVessel(player)) {
         CompoundTag state = state(player);
         state.putInt("rulers_fracture", clampRisk(state.getInt("rulers_fracture")));
         int currentStage = Mth.clamp(state.getInt("stage"), 0, 2);
         state.putInt("stage", currentStage);
         syncPersistentPresentation(player, state);
         SungIlHwanAttackMessage.syncStance(player, state.getBoolean("assassin_stance") && hasDagger(player));
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && isSungIlHwanVessel(player)) {
         CompoundTag state = state(player);
         syncPersistentPresentation(player, state);
         SungIlHwanAttackMessage.syncStance(player, state.getBoolean("assassin_stance") && hasDagger(player));
         int risk = clampRisk(state.getInt("rulers_fracture"));
         if (risk > 0) {
            SungIlHwanVfxEventMessage.sendRiskFeedback(player, riskSeverity(risk), 40, visualSeed(player));
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         EXECUTION_CHARGES.remove(player.getUUID());
         abortExecutionTraversal(player, true);
         PENDING_FRACTURES.remove(player.getUUID());
         LAST_STANCE_ATTACK.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onDimensionChanged(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         SungIlHwanCombatManager.ChargeState charge = EXECUTION_CHARGES.remove(player.getUUID());
         if (charge != null) {
            SungIlHwanVfxEventMessage.sendExecutionCancel(player, charge.seed);
         }

         abortExecutionTraversal(player, false);
         PENDING_FRACTURES.remove(player.getUUID());
         LAST_STANCE_ATTACK.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      EXECUTION_CHARGES.clear();
      EXECUTION_TRAVERSALS.clear();
      PENDING_FRACTURES.clear();
      LAST_STANCE_ATTACK.clear();
      INTERNAL_DAMAGE.clear();
   }

   private static void togglePresence(ServerPlayer player) {
      CompoundTag state = state(player);
      boolean activating = !state.getBoolean("presence");
      if (!activating || consumeMana(player, 90)) {
         state.putBoolean("presence", activating);
         player.displayClientMessage(
            Component.literal(activating ? "Predator's Presence spreads across the battlefield" : "Predator's Presence withdrawn")
               .withStyle(activating ? ChatFormatting.DARK_PURPLE : ChatFormatting.GRAY),
            true
         );
         if (activating) {
            SungIlHwanVfxEventMessage.sendFearPulse(player, player.position(), stage(player) == 2 ? 18.0 : 13.0, visualSeed(player));
         }
      }
   }

   private static void toggleAssassinStance(ServerPlayer player) {
      CompoundTag state = state(player);
      boolean activating = !state.getBoolean("assassin_stance");
      if (activating && !hasDagger(player)) {
         player.displayClientMessage(Component.literal("Assassin Stance requires a dagger").withStyle(ChatFormatting.RED), true);
      } else if (!activating || consumeMana(player, 70)) {
         state.putBoolean("assassin_stance", activating);
         state.putInt("dagger_combo", 0);
         SungIlHwanAttackMessage.syncStance(player, activating);
         player.displayClientMessage(
            Component.literal(activating ? "Assassin Stance ready" : "Assassin Stance released")
               .withStyle(activating ? ChatFormatting.AQUA : ChatFormatting.GRAY),
            true
         );
      }
   }

   private static void useSpiritualization(ServerPlayer player) {
      CompoundTag state = state(player);
      int currentStage = state.getInt("stage");
      if (currentStage == 2) {
         player.displayClientMessage(Component.literal("Stage II cannot be cancelled").withStyle(ChatFormatting.LIGHT_PURPLE), true);
      } else if (currentStage == 1) {
         if (player.isShiftKeyDown()) {
            commitStageTwo(player, state);
         } else {
            deactivateStageOne(player, state, false);
         }
      } else if (state.getInt("exhaustion_remaining") > 0) {
         player.displayClientMessage(Component.literal("Ruler channels are exhausted").withStyle(ChatFormatting.RED), true);
      } else if (CooldownManager.isOnCooldown(player, "Spiritualization")) {
         player.displayClientMessage(Component.literal("Spiritualization is recovering").withStyle(ChatFormatting.RED), true);
      } else if (consumeMana(player, 180)) {
         state.putInt("stage", 1);
         state.putInt("stage_remaining", 0);
         long now = player.level().getGameTime();
         state.putLong("last_mana_drain", now);
         setSpiritualizationAura(player, 1);
         SungIlHwanVfxEventMessage.sendStage(player, 1, 1200, visualSeed(player));
         player.displayClientMessage(
            Component.literal("Stage I active. Sneak-use Spiritualization to commit to Stage II; death during Stage II or exhaustion deepens Ruler's Fracture.")
               .withStyle(ChatFormatting.LIGHT_PURPLE),
            false
         );
         player.level().playSound((Player)null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.75F, 1.45F);
      }
   }

   private static void commitStageTwo(ServerPlayer player, CompoundTag state) {
      if (canCommitStageTwo(player)) {
         int cost = 620 + state.getInt("rulers_fracture") * 3;
         if (consumeMana(player, cost)) {
            state.putInt("stage", 2);
            state.putInt("stage_remaining", 360);
            setSpiritualizationAura(player, 2);
            SungIlHwanVfxEventMessage.sendStage(player, 2, 360, visualSeed(player));
            player.displayClientMessage(Component.literal("Spiritualization Stage II committed").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 1.25F);
         }
      }
   }

   private static void deactivateStageOne(ServerPlayer player, CompoundTag state, boolean depleted) {
      state.putInt("stage", 0);
      state.putInt("stage_remaining", 0);
      CooldownManager.set(player, "Spiritualization", depleted ? 100 : 50);
      clearSpiritualizationAura(player);
      SungIlHwanVfxEventMessage.sendStageEnd(player, visualSeed(player));
      if (depleted) {
         player.displayClientMessage(Component.literal("Stage I ended: insufficient MP").withStyle(ChatFormatting.RED), true);
      }
   }

   private static void finishStageTwo(ServerPlayer player, CompoundTag state) {
      state.putInt("stage", 0);
      state.putInt("stage_remaining", 0);
      state.putInt("exhaustion_remaining", 480);
      CooldownManager.setFullDuration(player, "Spiritualization", 640);
      clearSpiritualizationAura(player);
      SungIlHwanVfxEventMessage.sendStageEnd(player, visualSeed(player));
      SungIlHwanVfxEventMessage.sendExhaustion(player, riskSeverity(state.getInt("rulers_fracture")), 480, visualSeed(player));
   }

   private static void applyStageEffects(ServerPlayer player, boolean secondStage) {
      int riskPenalty = state(player).getInt("rulers_fracture") >= 75 ? 1 : 0;
      player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 12, Math.max(0, (secondStage ? 3 : 1) - riskPenalty), false, false, true));
      player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 12, Math.max(0, (secondStage ? 2 : 0) - riskPenalty), false, false, true));
      player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 12, Math.max(0, (secondStage ? 3 : 0) - riskPenalty), false, false, true));
      if (secondStage) {
         player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12, riskPenalty == 0 ? 1 : 0, false, false, true));
      }
   }

   private static void recoverRisk(ServerPlayer player, CompoundTag state, long now) {
      int risk = clampRisk(state.getInt("rulers_fracture"));
      if (risk > 0) {
         long recoverAt = state.getLong("risk_recovery_at");
         if (recoverAt == 0L) {
            state.putLong("risk_recovery_at", now + 900L);
         } else {
            if (now >= recoverAt && state.getInt("stage") == 0) {
               state.putInt("rulers_fracture", risk - 1);
               state.putLong("risk_recovery_at", now + 900L);
               if ((risk - 1) % 10 == 0) {
                  SungIlHwanVfxEventMessage.sendRiskFeedback(player, riskSeverity(risk - 1), 24, visualSeed(player));
               }
            }
         }
      }
   }

   private static void teardownAfterIdentityLoss(ServerPlayer player) {
      clearSpiritualizationAura(player);
      SungIlHwanAttackMessage.syncStance(player, false);
      abortExecutionTraversal(player, true);
      SungIlHwanCombatManager.ChargeState charge = EXECUTION_CHARGES.remove(player.getUUID());
      PENDING_FRACTURES.remove(player.getUUID());
      LAST_STANCE_ATTACK.remove(player.getUUID());
      if (charge != null) {
         SungIlHwanVfxEventMessage.sendExecutionCancel(player, charge.seed);
      }

      CompoundTag outer = player.getPersistentData();
      if (outer.contains("PlayerPersisted", 10)) {
         CompoundTag persisted = outer.getCompound("PlayerPersisted");
         if (persisted.contains("slr_sung_il_hwan", 10)) {
            CompoundTag sungState = persisted.getCompound("slr_sung_il_hwan");
            int previousStage = sungState.getInt("stage");
            boolean visuallyActive = previousStage != 0;
            if (previousStage == 2) {
               sungState.putInt("exhaustion_remaining", Math.max(sungState.getInt("exhaustion_remaining"), 480));
            }

            sungState.putInt("stage", 0);
            sungState.putInt("stage_remaining", 0);
            sungState.putBoolean("presence", false);
            sungState.putBoolean("assassin_stance", false);
            persisted.put("slr_sung_il_hwan", sungState);
            outer.put("PlayerPersisted", persisted);
            if (visuallyActive) {
               SungIlHwanVfxEventMessage.sendStageEnd(player, visualSeed(player));
            }
         }
      }
   }

   private static void tickPresence(ServerPlayer player, CompoundTag state, long now) {
      if (now % 10L == 0L) {
         if (now % 40L == 0L && !consumeManaSilently(player, stage(player) == 2 ? 10 : 7)) {
            state.putBoolean("presence", false);
            player.displayClientMessage(Component.literal("Predator's Presence faded: insufficient MP").withStyle(ChatFormatting.RED), true);
         } else {
            double radius = stage(player) == 2 ? 18.0 : (stage(player) == 1 ? 15.0 : 12.0);

            for (LivingEntity target : nearbyTargets(player, player.getBoundingBox().inflate(radius, Math.min(8.0, radius), radius), 20)) {
               if (isFearExposed(player, target)) {
                  addFear(player, target, scaledFearGain(player, target));
               }
            }

            if (now % 20L == 0L) {
               SungIlHwanVfxEventMessage.sendFearPulse(player, player.position(), radius, visualSeed(player));
            }
         }
      }
   }

   private static boolean trySpatialSever(ServerPlayer player, LivingEntity target) {
      boolean overloaded = stage(player) == 2;
      double maximumRange = overloaded ? 12.0 : 10.0;
      if (isSungIlHwanVessel(player)
         && state(player).getBoolean("assassin_stance")
         && isDagger(player.getMainHandItem())
         && MageCombatHelper.isValidTarget(player, target)
         && !(player.distanceToSqr(target) > maximumRange * maximumRange)
         && player.hasLineOfSight(target)
         && !CooldownManager.isOnCooldown(player, "sih_spatial_sever")) {
         int stage = stage(player);
         int mana = stage == 2 ? 80 : 110;
         if (!consumeMana(player, mana)) {
            return false;
         }

         int fear = fear(player, target);
         int consumed = Math.min(fear, 35);
         setFear(player, target, fear - consumed);
         double strength = TemporaryStatBonusManager.effectiveStrength(player);
         double agility = TemporaryStatBonusManager.effectiveAgility(player);
         float damage = (float)((10.0 + strength / 16.0 + agility / 13.0) * (1.0 + consumed * 0.012) * (stage == 2 ? 1.35 : (stage == 1 ? 1.14 : 1.0)));
         boolean hit = hurtInternally(player, target, overloaded ? damage * 0.72F : damage);
         if (overloaded) {
            hit |= hurtInternally(player, target, damage * 0.28F);
         }

         if (hit) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 2, false, false, true));
            addFear(player, target, 14);
         }

         CooldownManager.set(player, "sih_spatial_sever", stage == 2 ? 28 : 42);
         if (stage == 1) {
            CooldownManager.set(player, "sih_spatial_sever", 35);
         }

         SungIlHwanVfxEventMessage.sendSpatialSlash(
            player, target, player.getEyePosition(), target.getBoundingBox().getCenter(), 5, hit, visualSeed(player) ^ target.getId()
         );
         if (overloaded) {
            SungIlHwanVfxEventMessage.sendSpatialSlash(
               player,
               target,
               target.getBoundingBox().getCenter().add(-1.2, 0.8, 0.8),
               target.getBoundingBox().getCenter(),
               6,
               hit,
               visualSeed(player) ^ target.getId() ^ 12057
            );
         }

         player.level().playSound((Player)null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 1.55F);
         return true;
      } else {
         return false;
      }
   }

   private static void beginSpatialExecution(ServerPlayer player) {
      if (!EXECUTION_CHARGES.containsKey(player.getUUID())
         && !EXECUTION_TRAVERSALS.containsKey(player.getUUID())
         && !CooldownManager.isOnCooldown(player, "Spatial Execution")) {
         Vec3 focus = executionCenter(player);
         int seed = visualSeed(player);
         EXECUTION_CHARGES.put(player.getUUID(), new SungIlHwanCombatManager.ChargeState(player.level().getGameTime(), seed));
         double maximumRadius = stage(player) == 2 ? 19.0 : 15.0;
         SungIlHwanVfxEventMessage.sendExecutionCharge(player, focus, maximumRadius, 100, seed);
      }
   }

   private static void tickExecutionCharge(ServerPlayer player, long now) {
      SungIlHwanCombatManager.ChargeState charge = EXECUTION_CHARGES.get(player.getUUID());
      if (charge != null) {
         long heldTicks = Math.max(0L, now - charge.startedAt);
         if (player.isAlive() && isSungIlHwanVessel(player)) {
            if (heldTicks % 5L == 0L) {
               Vec3 focus = executionCenter(player);
               double radius = executionRadius(player, heldTicks);
               int remaining = (int)Math.max(8L, 100L - heldTicks + 8L);
               SungIlHwanVfxEventMessage.sendExecutionTarget(player, null, focus, radius, remaining, charge.seed);
               int markIndex = 0;

               for (LivingEntity target : executionTargets(player, focus, radius)) {
                  SungIlHwanVfxEventMessage.sendExecutionTarget(player, target, focus, radius, remaining, charge.seed + 31 * ++markIndex);
               }
            }
         } else {
            EXECUTION_CHARGES.remove(player.getUUID());
            SungIlHwanVfxEventMessage.sendExecutionCancel(player, charge.seed);
         }
      }
   }

   private static void releaseSpatialExecution(ServerPlayer player, int pressedMs) {
      SungIlHwanCombatManager.ChargeState charge = EXECUTION_CHARGES.remove(player.getUUID());
      if (charge != null) {
         if (player.isAlive() && isSungIlHwanVessel(player)) {
            int elapsedMs = (int)Math.min(2147483647L, Math.max(0L, player.level().getGameTime() - charge.startedAt) * 50L);
            int tier = executionChargeTier(elapsedMs);
            if (tier == 0) {
               SungIlHwanVfxEventMessage.sendExecutionCancel(player, charge.seed);
            } else {
               Vec3 originalPosition = player.position();
               Vec3 safeReturnPosition = findSafeExecutionReturnPosition(player, player.serverLevel(), originalPosition, 6, true);
               if (safeReturnPosition == null) {
                  player.displayClientMessage(Component.literal("Spatial Execution needs stable ground to return to").withStyle(ChatFormatting.RED), true);
                  SungIlHwanVfxEventMessage.sendExecutionCancel(player, charge.seed);
               } else {
                  int mana = switch (tier) {
                     case 2 -> 510;
                     case 3 -> 760;
                     default -> 310;
                  };
                  if (!consumeMana(player, mana)) {
                     SungIlHwanVfxEventMessage.sendExecutionCancel(player, charge.seed);
                  } else {
                     int currentStage = stage(player);
                     boolean overloaded = currentStage == 2;
                     long heldTicks = Math.max(0L, player.level().getGameTime() - charge.startedAt);
                     Vec3 focus = executionCenter(player);
                     double radius = executionRadius(player, heldTicks);
                     List<LivingEntity> targets = executionTargets(player, focus, radius);
                     if (targets.isEmpty()) {
                        CooldownManager.set(player, "Spatial Execution", 35);
                        SungIlHwanVfxEventMessage.sendExecutionCancel(player, charge.seed);
                     } else {
                        double strength = TemporaryStatBonusManager.effectiveStrength(player);
                        double agility = TemporaryStatBonusManager.effectiveAgility(player);
                        double intelligence = TemporaryStatBonusManager.effectiveIntelligence(player);
                        double base = (18.0 + strength / 13.0 + agility / 11.0 + intelligence / 24.0)
                           * (0.75 + tier * 0.28)
                           * (overloaded ? 1.35 : (currentStage == 1 ? 1.12 : 1.0));
                        ArrayList<SungIlHwanCombatManager.ExecutionTarget> traversalTargets = new ArrayList<>();

                        for (LivingEntity target : targets) {
                           int storedFear = fear(player, target);
                           int consumedFear = Math.min(storedFear, 75);
                           setFear(player, target, storedFear - consumedFear);
                           float totalDamage = (float)(base * (1.0 + consumedFear * 0.012));
                           traversalTargets.add(new SungIlHwanCombatManager.ExecutionTarget(target.getUUID(), totalDamage, target.getBoundingBox().getCenter()));
                        }

                        SungIlHwanCombatManager.ExecutionTraversal traversal = new SungIlHwanCombatManager.ExecutionTraversal(
                           player.level().dimension(),
                           originalPosition,
                           safeReturnPosition,
                           player.getYRot(),
                           player.getXRot(),
                           focus,
                           radius,
                           tier,
                           charge.seed,
                           overloaded,
                           traversalTargets,
                           player.level().getGameTime()
                        );
                        EXECUTION_TRAVERSALS.put(player.getUUID(), traversal);
                        CooldownManager.set(
                           player, "Spatial Execution", currentStage == 2 ? 220 + tier * 25 : (currentStage == 1 ? 260 + tier * 30 : 300 + tier * 35)
                        );
                        SungIlHwanVfxEventMessage.sendExecutionRelease(player, null, focus, radius, tier, charge.seed);
                     }
                  }
               }
            }
         } else {
            SungIlHwanVfxEventMessage.sendExecutionCancel(player, charge.seed);
         }
      }
   }

   private static void tickExecutionTraversal(ServerPlayer player, long now) {
      SungIlHwanCombatManager.ExecutionTraversal traversal = EXECUTION_TRAVERSALS.get(player.getUUID());
      if (traversal != null && now >= traversal.nextStepAt) {
         if (player.isAlive() && isSungIlHwanVessel(player) && player.level().dimension().equals(traversal.dimension)) {
            if (traversal.nextTarget >= traversal.targets.size()) {
               completeExecutionTraversal(player, traversal, now);
            } else {
               SungIlHwanCombatManager.ExecutionTarget marked = traversal.targets.get(traversal.nextTarget);
               ServerLevel level = player.server.getLevel(traversal.dimension);
               Entity entity = level == null ? null : level.getEntity(marked.targetId);
               Vec3 from = player.getEyePosition();
               if (entity instanceof LivingEntity target && MageCombatHelper.isValidTarget(player, target)) {
                  IgrisCombatTeleportHelper.tryMoveBehindTarget(player, target);
                  Vec3 point = target.getBoundingBox().getCenter();
                  int index = traversal.nextTarget;
                  SungIlHwanVfxEventMessage.sendSpatialSlash(player, target, from, point, index % 3, false, traversal.seed + index * 31);
                  if (traversal.overloaded) {
                     Vec3 crossStart = point.add(index % 2 == 0 ? -1.4 : 1.4, 0.8, index % 2 == 0 ? 0.9 : -0.9);
                     SungIlHwanVfxEventMessage.sendSpatialSlash(player, target, crossStart, point, 3 + index % 4, false, traversal.seed + index * 31 + 13);
                  }
               }

               traversal.nextTarget++;
               traversal.nextStepAt = now + 1L;
            }
         } else {
            abortExecutionTraversal(player, player.isAlive() && player.level().dimension().equals(traversal.dimension));
         }
      }
   }

   private static void completeExecutionTraversal(ServerPlayer player, SungIlHwanCombatManager.ExecutionTraversal traversal, long now) {
      if (EXECUTION_TRAVERSALS.remove(player.getUUID()) == traversal) {
         if (!returnToExecutionOrigin(player, traversal)) {
            SungIlHwanVfxEventMessage.sendExecutionCancel(player, traversal.seed);
            player.displayClientMessage(Component.literal("Spatial Execution canceled: no safe return point").withStyle(ChatFormatting.RED), true);
         } else {
            long sharedExecuteAt = now + 20L;
            ArrayList<SungIlHwanCombatManager.FractureState> pending = new ArrayList<>();

            for (SungIlHwanCombatManager.ExecutionTarget target : traversal.targets) {
               if (pending.size() >= 96) {
                  break;
               }

               pending.add(new SungIlHwanCombatManager.FractureState(target.targetId, sharedExecuteAt, target.damage, target.focus, traversal.dimension));
            }

            if (!pending.isEmpty()) {
               PENDING_FRACTURES.put(player.getUUID(), pending);
            }

            SungIlHwanVfxEventMessage.sendExecutionFracture(player, traversal.sphereCenter, traversal.radius, 20, traversal.seed);
         }
      }
   }

   private static void abortExecutionTraversal(ServerPlayer player, boolean restoreOrigin) {
      SungIlHwanCombatManager.ExecutionTraversal traversal = EXECUTION_TRAVERSALS.remove(player.getUUID());
      if (traversal != null) {
         if (restoreOrigin && player.isAlive()) {
            returnToExecutionOrigin(player, traversal);
         }

         SungIlHwanVfxEventMessage.sendExecutionCancel(player, traversal.seed);
      }
   }

   private static void tickFractures(ServerPlayer player, long now) {
      List<SungIlHwanCombatManager.FractureState> pending = PENDING_FRACTURES.get(player.getUUID());
      if (pending != null && !pending.isEmpty()) {
         for (SungIlHwanCombatManager.FractureState fracture : new ArrayList<>(pending)) {
            if (fracture.executeAt <= now) {
               pending.remove(fracture);
               ServerLevel level = player.server.getLevel(fracture.dimension);
               if ((level == null ? null : level.getEntity(fracture.targetId)) instanceof LivingEntity target && MageCombatHelper.isValidTarget(player, target)
                  )
                {
                  hurtInternally(player, target, fracture.damage);
                  target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 18, 1, false, false, true));
               }
            }
         }

         if (pending.isEmpty()) {
            PENDING_FRACTURES.remove(player.getUUID());
         }
      }
   }

   private static List<LivingEntity> executionTargets(ServerPlayer player, Vec3 focus, double radius) {
      List<LivingEntity> targets = player.serverLevel()
         .getEntitiesOfClass(
            LivingEntity.class,
            new AABB(focus, focus).inflate(radius),
            target -> !(target instanceof ArmorStand) && MageCombatHelper.isValidTarget(player, target)
         );
      targets.removeIf(target -> distanceToAabbSqr(focus, target.getBoundingBox()) > radius * radius);
      targets.sort(Comparator.comparingDouble(target -> distanceToAabbSqr(focus, target.getBoundingBox())));
      return targets.size() > 96 ? new ArrayList<>(targets.subList(0, 96)) : targets;
   }

   private static double executionRadius(ServerPlayer player, long heldTicks) {
      double maximum = stage(player) == 2 ? 19.0 : 15.0;
      double progress = Mth.clamp(heldTicks / 100.0, 0.0, 1.0);
      return Mth.lerp(progress, 3.0, maximum);
   }

   private static Vec3 executionCenter(ServerPlayer player) {
      return player.position().add(0.0, player.getBbHeight() * 0.52, 0.0);
   }

   private static boolean returnToExecutionOrigin(ServerPlayer player, SungIlHwanCombatManager.ExecutionTraversal traversal) {
      ServerLevel destinationLevel = player.server.getLevel(traversal.dimension);
      if (destinationLevel == null) {
         return false;
      }

      if (tryExecutionReturn(player, destinationLevel, traversal.originalPosition, traversal.originalYaw, traversal.originalPitch)) {
         return true;
      }

      if (!traversal.safeReturnPosition.equals(traversal.originalPosition)
         && tryExecutionReturn(player, destinationLevel, traversal.safeReturnPosition, traversal.originalYaw, traversal.originalPitch)) {
         return true;
      }

      Vec3 nearby = findSafeExecutionReturnPosition(player, destinationLevel, traversal.originalPosition, 8, false);
      if (nearby != null && tryExecutionReturn(player, destinationLevel, nearby, traversal.originalYaw, traversal.originalPitch)) {
         return true;
      }

      Vec3 sharedSpawn = Vec3.atBottomCenterOf(destinationLevel.getSharedSpawnPos());
      Vec3 emergency = findSafeExecutionReturnPosition(player, destinationLevel, sharedSpawn, 12, true);
      return emergency != null && tryExecutionReturn(player, destinationLevel, emergency, traversal.originalYaw, traversal.originalPitch);
   }

   private static boolean tryExecutionReturn(ServerPlayer player, ServerLevel level, Vec3 destination, float yaw, float pitch) {
      if (!isSafeExecutionReturn(level, player, destination)) {
         return false;
      } else {
         player.teleportTo(level, destination.x, destination.y, destination.z, yaw, pitch);
         if (player.serverLevel() == level && !(player.position().distanceToSqr(destination) > 0.25)) {
            player.setDeltaMovement(Vec3.ZERO);
            player.fallDistance = 0.0F;
            player.setOnGround(true);
            return true;
         } else {
            return false;
         }
      }
   }

   private static boolean isSafeExecutionReturn(ServerLevel level, ServerPlayer player, Vec3 destination) {
      return IgrisCombatTeleportHelper.isSafeDestination(level, player, destination);
   }

   private static Vec3 findSafeExecutionReturnPosition(ServerPlayer player, ServerLevel level, Vec3 center, int maximumRadius, boolean includeCenter) {
      if (includeCenter && isSafeExecutionReturn(level, player, center)) {
         return center;
      }

      int[] verticalOffsets = new int[]{0, 1, -1, 2, -2, 3, -3, 4, -4};

      for (int radius = 1; radius <= maximumRadius; radius++) {
         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               if (Math.abs(x) == radius || Math.abs(z) == radius) {
                  for (int y : verticalOffsets) {
                     Vec3 candidate = center.add(x, y, z);
                     if (isSafeExecutionReturn(level, player, candidate)) {
                        return candidate;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private static List<LivingEntity> nearbyTargets(ServerPlayer player, AABB bounds, int maximum) {
      List<LivingEntity> result = player.serverLevel()
         .getEntitiesOfClass(LivingEntity.class, bounds, target -> !(target instanceof ArmorStand) && MageCombatHelper.isValidTarget(player, target));
      result.sort(Comparator.comparingDouble(player::distanceToSqr));
      return result.size() > maximum ? new ArrayList<>(result.subList(0, maximum)) : result;
   }

   private static List<LivingEntity> targetsAlongLine(ServerPlayer player, Vec3 origin, Vec3 direction, double range, double width, int maximum) {
      Vec3 unitDirection = direction.lengthSqr() < 0.001 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
      Vec3 endpoint = origin.add(unitDirection.scale(range));
      AABB query = new AABB(origin, endpoint).inflate(width);
      List<LivingEntity> targets = player.serverLevel()
         .getEntitiesOfClass(LivingEntity.class, query, target -> !(target instanceof ArmorStand) && MageCombatHelper.isValidTarget(player, target));
      targets.removeIf(target -> !player.hasLineOfSight(target) || !segmentIntersectsExpandedAabb(origin, endpoint, target.getBoundingBox(), width));
      targets.sort(Comparator.comparingDouble(target -> lineEntryDistanceSqr(origin, endpoint, target.getBoundingBox(), width)));
      return targets.size() > maximum ? new ArrayList<>(targets.subList(0, maximum)) : targets;
   }

   private static boolean segmentIntersectsExpandedAabb(Vec3 origin, Vec3 endpoint, AABB bounds, double width) {
      AABB expanded = bounds.inflate(width);
      return expanded.contains(origin) || expanded.clip(origin, endpoint).isPresent();
   }

   private static double lineEntryDistanceSqr(Vec3 origin, Vec3 endpoint, AABB bounds, double width) {
      AABB expanded = bounds.inflate(width);
      return expanded.contains(origin) ? 0.0 : expanded.clip(origin, endpoint).map(point -> point.distanceToSqr(origin)).orElse(Double.MAX_VALUE);
   }

   private static double distanceToAabbSqr(Vec3 point, AABB bounds) {
      double dx = Math.max(Math.max(bounds.minX - point.x, 0.0), point.x - bounds.maxX);
      double dy = Math.max(Math.max(bounds.minY - point.y, 0.0), point.y - bounds.maxY);
      double dz = Math.max(Math.max(bounds.minZ - point.z, 0.0), point.z - bounds.maxZ);
      return dx * dx + dy * dy + dz * dz;
   }

   private static LivingEntity findLookTarget(ServerPlayer player, double range) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      Vec3 end = eye.add(look.scale(range));
      LivingEntity best = null;
      double bestDistance = Double.MAX_VALUE;

      for (LivingEntity target : nearbyTargets(player, player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5), 12)) {
         Vec3 center = target.getBoundingBox().getCenter();
         double along = Mth.clamp(center.subtract(eye).dot(look), 0.0, range);
         double distance = center.distanceToSqr(eye.add(look.scale(along)));
         double allowance = Math.max(0.8, target.getBbWidth() * 0.75);
         if (distance <= allowance * allowance && along < bestDistance && player.hasLineOfSight(target)) {
            best = target;
            bestDistance = along;
         }
      }

      return best;
   }

   private static int fear(ServerPlayer owner, LivingEntity target) {
      String suffix = owner.getUUID().toString().replace("-", "");
      CompoundTag data = target.getPersistentData();
      if (data.getLong("slr_sih_fear_expiry_" + suffix) < target.level().getGameTime()) {
         data.remove("slr_sih_fear_" + suffix);
         data.remove("slr_sih_fear_expiry_" + suffix);
         return 0;
      } else {
         return Mth.clamp(data.getInt("slr_sih_fear_" + suffix), 0, fearCap(target));
      }
   }

   private static void setFear(ServerPlayer owner, LivingEntity target, int amount) {
      String suffix = owner.getUUID().toString().replace("-", "");
      CompoundTag data = target.getPersistentData();
      int bounded = Mth.clamp(amount, 0, fearCap(target));
      if (bounded <= 0) {
         data.remove("slr_sih_fear_" + suffix);
         data.remove("slr_sih_fear_expiry_" + suffix);
      } else {
         data.putInt("slr_sih_fear_" + suffix, bounded);
         data.putLong("slr_sih_fear_expiry_" + suffix, target.level().getGameTime() + 160L);
      }
   }

   private static void addFear(ServerPlayer owner, LivingEntity target, int amount) {
      if (amount > 0 && MageCombatHelper.isValidTarget(owner, target)) {
         int oldFear = fear(owner, target);
         int updated = Mth.clamp(oldFear + amount, 0, fearCap(target));
         setFear(owner, target, updated);
         int oldTier = fearTier(oldFear);
         int newTier = fearTier(updated);
         applyFearDebuff(target, newTier);
         if (newTier > oldTier) {
            SungIlHwanVfxEventMessage.sendFearMark(owner, target, 160, visualSeed(owner) ^ target.getId() ^ newTier * 101);
         }
      }
   }

   private static void applyFearDebuff(LivingEntity target, int tier) {
      if (tier > 0) {
         target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, Math.min(2, tier - 1), false, false, true));
         if (tier >= 2) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, Math.min(1, tier - 2), false, false, true));
         }
      }
   }

   private static boolean isFearExposed(ServerPlayer caster, LivingEntity target) {
      if (!caster.hasLineOfSight(target)) {
         return false;
      }

      if (target instanceof Player opponent) {
         if (!opponent.hasLineOfSight(caster)) {
            return false;
         }

         Vec3 towardCaster = caster.getEyePosition().subtract(opponent.getEyePosition());
         return towardCaster.lengthSqr() < 0.001 ? true : opponent.getLookAngle().normalize().dot(towardCaster.normalize()) >= 0.08;
      } else {
         return true;
      }
   }

   private static void syncPersistentPresentation(ServerPlayer player, CompoundTag state) {
      int currentStage = Mth.clamp(state.getInt("stage"), 0, 2);
      if (currentStage == 2 && state.getInt("stage_remaining") > 0) {
         setSpiritualizationAura(player, 2);
         SungIlHwanVfxEventMessage.sendStage(player, 2, state.getInt("stage_remaining"), visualSeed(player));
      } else if (currentStage == 1) {
         setSpiritualizationAura(player, 1);
         SungIlHwanVfxEventMessage.sendStage(player, 1, 1200, visualSeed(player));
      } else {
         clearSpiritualizationAura(player);
         SungIlHwanVfxEventMessage.sendStageEnd(player, visualSeed(player));
      }

      if (state.getInt("exhaustion_remaining") > 0) {
         int remaining = state.getInt("exhaustion_remaining");
         CooldownManager.setFullDuration(player, "mana_refresh", 12);
         SungIlHwanVfxEventMessage.sendExhaustion(player, riskSeverity(state.getInt("rulers_fracture")), remaining, visualSeed(player));
      }
   }

   private static void setSpiritualizationAura(ServerPlayer player, int stage) {
      PlayerAuraSystem.setContinuous(player, "sung_il_hwan_spiritualization", stage == 2 ? 1.65F : 1.25F);
   }

   private static void clearSpiritualizationAura(ServerPlayer player) {
      if (player != null && "sung_il_hwan_spiritualization".equals(player.getPersistentData().getString("sololeveling_player_aura"))) {
         PlayerAuraSystem.clearContinuous(player);
      }
   }

   private static boolean hurtInternally(ServerPlayer player, LivingEntity target, float damage) {
      INTERNAL_DAMAGE.add(player.getUUID());

      try {
         return MageCombatHelper.hurt(player.serverLevel(), player, target, Math.max(0.0F, damage));
      } finally {
         INTERNAL_DAMAGE.remove(player.getUUID());
      }
   }

   private static int stage(ServerPlayer player) {
      return Mth.clamp(state(player).getInt("stage"), 0, 2);
   }

   private static int stanceAttackIntervalTicks(ServerPlayer player) {
      double attackSpeed = Math.max(0.1, player.getAttributeValue(Attributes.ATTACK_SPEED));
      double vanillaCooldownTicks = 20.0 / attackSpeed;
      return Mth.clamp((int)Math.ceil(vanillaCooldownTicks * 0.8), 2, 10);
   }

   private static boolean replaceOrAlreadyReplacedAssassinAttack(ServerPlayer player) {
      if (performAssassinLineCut(player)) {
         return true;
      }

      Long lastAttack = LAST_STANCE_ATTACK.get(player.getUUID());
      return lastAttack != null && player.level().getGameTime() - lastAttack <= 1L;
   }

   private static boolean hasDagger(ServerPlayer player) {
      return isDagger(player.getMainHandItem());
   }

   private static boolean isDagger(ItemStack stack) {
      return stack != null && !stack.isEmpty() && stack.is(DAGGERS);
   }

   private static double rulerPower(ServerPlayer player) {
      return 20.0
         + TemporaryStatBonusManager.effectiveStrength(player) * 1.25
         + TemporaryStatBonusManager.effectiveAgility(player) * 1.1
         + TemporaryStatBonusManager.effectiveIntelligence(player) * 0.35;
   }

   private static double attributeValueOrZero(LivingEntity entity, Attribute attribute) {
      AttributeInstance instance = entity.getAttribute(attribute);
      return instance == null ? 0.0 : instance.getValue();
   }

   private static double targetPower(LivingEntity target) {
      return 10.0
         + target.getMaxHealth() * 1.25
         + Math.max(0.0, attributeValueOrZero(target, Attributes.ARMOR)) * 3.0
         + Math.max(0.0, attributeValueOrZero(target, Attributes.ATTACK_DAMAGE)) * 6.0;
   }

   private static boolean isBossLike(LivingEntity target) {
      return target.getMaxHealth() >= 120.0F || target.getPersistentData().getBoolean("Boss") || target.getType().toString().toLowerCase().contains("boss");
   }

   private static int riskSeverity(int risk) {
      return Mth.clamp((int)Math.round(clampRisk(risk) * 2.55), 0, 255);
   }

   private static boolean consumeMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      } else if (variables(player).MP < amount) {
         player.displayClientMessage(Component.literal("Not enough MP (" + amount + " required)").withStyle(ChatFormatting.RED), true);
         return false;
      } else {
         return drainMana(player, amount);
      }
   }

   private static boolean consumeManaSilently(ServerPlayer player, int amount) {
      return player.isCreative() || variables(player).MP >= amount && drainMana(player, amount);
   }

   private static boolean drainMana(ServerPlayer player, int amount) {
      if (player.isCreative()) {
         return true;
      }

      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.MP = Math.max(0.0, capability.MP - Math.max(0, amount));
         capability.syncPlayerVariables(player);
      });
      CooldownManager.set(player, "mana_refresh", 35);
      return true;
   }

   private static CompoundTag state(Player player) {
      CompoundTag outer = player.getPersistentData();
      CompoundTag persisted = outer.contains("PlayerPersisted", 10) ? outer.getCompound("PlayerPersisted") : new CompoundTag();
      CompoundTag state = persisted.contains("slr_sung_il_hwan", 10) ? persisted.getCompound("slr_sung_il_hwan") : new CompoundTag();
      persisted.put("slr_sung_il_hwan", state);
      outer.put("PlayerPersisted", persisted);
      return state;
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static int visualSeed(ServerPlayer player) {
      long mixed = player.getUUID().getMostSignificantBits()
         ^ player.getUUID().getLeastSignificantBits()
         ^ player.level().getGameTime() * -7046029254386353131L;
      return (int)(mixed ^ mixed >>> 32);
   }

   private record ChargeState(long startedAt, int seed) {
   }

   private record ExecutionTarget(UUID targetId, float damage, Vec3 focus) {
   }

   private static final class ExecutionTraversal {
      private final ResourceKey<Level> dimension;
      private final Vec3 originalPosition;
      private final Vec3 safeReturnPosition;
      private final float originalYaw;
      private final float originalPitch;
      private final Vec3 sphereCenter;
      private final double radius;
      private final int tier;
      private final int seed;
      private final boolean overloaded;
      private final List<SungIlHwanCombatManager.ExecutionTarget> targets;
      private int nextTarget;
      private long nextStepAt;

      private ExecutionTraversal(
         ResourceKey<Level> dimension,
         Vec3 originalPosition,
         Vec3 safeReturnPosition,
         float originalYaw,
         float originalPitch,
         Vec3 sphereCenter,
         double radius,
         int tier,
         int seed,
         boolean overloaded,
         List<SungIlHwanCombatManager.ExecutionTarget> targets,
         long nextStepAt
      ) {
         this.dimension = dimension;
         this.originalPosition = originalPosition;
         this.safeReturnPosition = safeReturnPosition;
         this.originalYaw = originalYaw;
         this.originalPitch = originalPitch;
         this.sphereCenter = sphereCenter;
         this.radius = radius;
         this.tier = tier;
         this.seed = seed;
         this.overloaded = overloaded;
         this.targets = List.copyOf(targets);
         this.nextStepAt = nextStepAt;
      }
   }

   private record FractureState(UUID targetId, long executeAt, float damage, Vec3 focus, ResourceKey<Level> dimension) {
   }
}
