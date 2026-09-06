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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.LiuSwordBeamEntity;
import dev.eness.sololevelingfinal.core.entity.LiuSwordVfxEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.LiuExecutionImpactMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public final class LiuZhigangCombatManager {
   public static final String HEAVENLY_COUNTER = "Heavenly Counter";
   public static final String GOLDEN_DRAGON_DANCE = "Golden Dragon Dance";
   public static final String SOVEREIGN_SWORD_DOMAIN = "Sovereign Sword Domain";
   public static final String DRAGON_SWORD_MANIFESTATION = "Dragon Sword Manifestation";
   public static final int GOLD = 16765774;
   public static final int PALE_GOLD = 16774064;
   public static final int DEMON_BLUE = 11069951;
   public static final int KAMISH_RED = 16724040;
   public static final long BEAM_TIER_ONE_TICKS = 17L;
   public static final long BEAM_TIER_TWO_TICKS = 34L;
   public static final long BEAM_TIER_THREE_TICKS = 234L;
   private static final long MAX_BEAM_CHARGE_TICKS = 600L;
   private static final String IDENTITY = "liu_zhigang";
   private static final String BEAM_COOLDOWN = "liu_charged_sword_beam";
   private static final String FLASH_COOLDOWN = "liu_dragon_flash";
   private static final String NEXT_STRIKE = "liu_next_enhanced_strike";
   private static final String STRIKE_PRESSURE_STACKS = "liu_enhanced_strike_pressure";
   private static final String STRIKE_PRESSURE_LAST = "liu_enhanced_strike_pressure_last";
   private static final String FALL_SAFE_UNTIL = "liu_fall_safe_until";
   private static final String COUNTER_UNTIL = "liu_counter_until";
   private static final String COUNTER_CHARGES = "liu_counter_charges";
   private static final String COUNTER_SURGE_UNTIL = "liu_counter_surge_until";
   private static final String DOMAIN_MARK_OWNER = "liu_domain_mark_owner";
   private static final String DOMAIN_MARK_UNTIL = "liu_domain_mark_until";
   private static final String DOMAIN_MARK_STACKS = "liu_domain_mark_stacks";
   private static final String EXECUTION_SLOW_OWNER = "liu_execution_slow_owner";
   private static final String EXECUTION_SLOW_UNTIL = "liu_execution_slow_until";
   private static final String OWNED_BEAM_OWNER = "liu_owned_beam_owner";
   private static final String DOMAIN_PROJECTILE_OWNER = "liu_domain_projectile_owner";
   private static final int EXECUTION_DELAY_TICKS = 50;
   private static final int EXECUTION_IMPACT_TICKS = 16;
   private static final int EXECUTION_FIRE_RED = 14825496;
   private static final int EXECUTION_FIRE_YELLOW = 16767050;
   private static final int EXECUTION_PARTICLE_TARGET_LIMIT = 10;
   private static final double EXECUTION_PARTICLE_RANGE_SQR = 147456.0;
   private static final int EXECUTION_MAX_LINK_NODES = 48;
   private static final int EXECUTION_MAX_LINKS = 24;
   private static final double EXECUTION_LINK_RANGE_SQR = 400.0;
   private static final int ENHANCED_STRIKE_RECOVERY_TICKS = 6;
   private static final int ENHANCED_STRIKE_FINISHER_RECOVERY_TICKS = 8;
   private static final int ENHANCED_STRIKE_PRESSURE_RESET_TICKS = 45;
   private static final int ENHANCED_STRIKE_PRESSURE_CAP = 6;
   private static final double ENHANCED_STRIKE_PRESSURE_COST_STEP = 0.22;
   private static final float CHARGED_BEAM_DAMAGE_MULTIPLIER = 0.72F;
   private static final float ENHANCED_STRIKE_DAMAGE_MULTIPLIER = 0.72F;
   private static final float DANCE_WAVE_BEAM_DAMAGE_MULTIPLIER = 0.82F;
   private static final TagKey<Item> NORMAL_SWORDS = TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "nsword"));
   private static final TagKey<Item> DAGGERS = TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "dagger"));
   private static final Map<UUID, LiuZhigangCombatManager.BeamChargeState> BEAM_CHARGES = new HashMap<>();
   private static final Map<UUID, LiuZhigangCombatManager.FlashChargeState> FLASH_CHARGES = new HashMap<>();
   private static final Map<UUID, LiuZhigangCombatManager.FlashDashState> FLASH_DASHES = new HashMap<>();
   private static final Map<UUID, LiuZhigangCombatManager.DanceState> DANCES = new HashMap<>();
   private static final Map<UUID, LiuZhigangCombatManager.DomainState> DOMAINS = new HashMap<>();
   private static final Map<UUID, List<LiuZhigangCombatManager.ExecutionState>> EXECUTIONS = new HashMap<>();

   private LiuZhigangCombatManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null && player.getServer() != null) {
         UUID playerId = player.getUUID();
         clearState(player);
         ArrayList<Entity> ownedProjectiles = new ArrayList<>();

         for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
               if (entity instanceof LivingEntity living) {
                  CompoundTag data = living.getPersistentData();
                  if (data.hasUUID("liu_domain_mark_owner") && playerId.equals(data.getUUID("liu_domain_mark_owner"))) {
                     clearDomainMark(living);
                  }

                  if (data.hasUUID("liu_execution_slow_owner") && playerId.equals(data.getUUID("liu_execution_slow_owner"))) {
                     releaseExecutionRestraint(player, living, true);
                  }
               }

               CompoundTag data = entity.getPersistentData();
               boolean ownedBeam = entity instanceof LiuSwordBeamEntity
                  && data.hasUUID("liu_owned_beam_owner")
                  && playerId.equals(data.getUUID("liu_owned_beam_owner"));
               boolean ownedDeflection = entity instanceof Projectile
                  && data.hasUUID("liu_domain_projectile_owner")
                  && playerId.equals(data.getUUID("liu_domain_projectile_owner"));
               if (ownedBeam || ownedDeflection) {
                  ownedProjectiles.add(entity);
               }
            }
         }

         ownedProjectiles.forEach(Entity::discard);
      }
   }

   public static boolean isLiuVessel(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = variables(entity);
      return (int)vars.JOB == 6 && ("liu_zhigang".equals(vars.vesselIdentity) || vars.vesselIdentity.isBlank());
   }

   public static boolean isCombatStance(Entity entity) {
      return isLiuVessel(entity) && variables(entity).combatmode;
   }

   public static boolean isManifested(Entity entity) {
      return LiuManifestationManager.isActive(entity);
   }

   public static boolean isMeleeWeapon(ItemStack stack) {
      return !stack.isEmpty()
         && (
            stack.getItem() instanceof SwordItem
               || stack.getItem() instanceof AxeItem
               || stack.getItem() instanceof TridentItem
               || stack.is(NORMAL_SWORDS)
               || stack.is(DAGGERS)
         );
   }

   public static int colorFor(ItemStack stack) {
      if (stack.is(SololevelingModItems.DEMON_KINGS_LONG_SWORD.get())) {
         return 11069951;
      } else {
         return !stack.is(SololevelingModItems.KAMISH_WRATH.get()) && !stack.is(SololevelingModItems.KAMISH_WRATH_2.get()) ? 16765774 : 16724040;
      }
   }

   public static int beamChargeTier(long heldTicks) {
      return heldTicks >= 234L ? 3 : (heldTicks >= 34L ? 2 : (heldTicks >= 17L ? 1 : 0));
   }

   public static void beginBeamCharge(ServerPlayer player) {
      if (isCombatStance(player) && player.isAlive() && !BEAM_CHARGES.containsKey(player.getUUID())) {
         ItemStack main = player.getMainHandItem();
         ItemStack off = player.getOffhandItem();
         if (isMeleeWeapon(main) || isMeleeWeapon(off)) {
            boolean dual = isMeleeWeapon(main) && isMeleeWeapon(off);
            if (!isMeleeWeapon(main)) {
               main = off;
            }

            int mainColor = colorFor(main);
            int offColor = dual ? colorFor(off) : mainColor;
            LiuSwordVfxEntity charge = LiuSwordVfxEntity.spawnAttached(
               player.serverLevel(), player, 1, mainColor, offColor, dual ? 1.5F : 1.15F, dual ? 2.8F : 2.2F, 0.0F, 620, dual
            );
            BEAM_CHARGES.put(
               player.getUUID(),
               new LiuZhigangCombatManager.BeamChargeState(
                  player.level().getGameTime(),
                  itemSignature(main),
                  itemSignature(off),
                  mainColor,
                  offColor,
                  dual,
                  (float)((weaponPower(main) + (dual ? weaponPower(off) : 0.0)) / (dual ? 2.0 : 1.0)),
                  charge.getUUID()
               )
            );
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.52F, 1.72F);
         }
      }
   }

   public static void releaseBeamCharge(ServerPlayer player) {
      LiuZhigangCombatManager.BeamChargeState state = BEAM_CHARGES.remove(player.getUUID());
      if (state != null) {
         removeVfx(player.serverLevel(), state.effectId);
         if (isCombatStance(player) && player.isAlive()) {
            if (sameHeldWeapons(player, state)) {
               long heldTicks = Math.max(1L, Math.min(600L, player.level().getGameTime() - state.startedAt));
               if (heldTicks < 17L && CooldownManager.isOnCooldown(player, "liu_charged_sword_beam")) {
                  player.displayClientMessage(Component.literal("Sword beam is still recovering").withStyle(ChatFormatting.GOLD), true);
               } else {
                  int tier = beamChargeTier(heldTicks);
                  if (player.getPersistentData().getLong("liu_counter_surge_until") >= player.level().getGameTime()) {
                     if (tier < 2) {
                        tier++;
                     }

                     player.getPersistentData().remove("liu_counter_surge_until");
                  }
                  int mana = switch (tier) {
                     case 1 -> 190;
                     case 2 -> 450;
                     case 3 -> 1100;
                     default -> 55 + (int)Math.min(85L, heldTicks * 5L);
                  };

                  double manaScaling = switch (tier) {
                     case 1 -> 0.75;
                     case 2 -> 1.0;
                     case 3 -> 1.35;
                     default -> 0.55;
                  };
                  mana = VesselManaScaling.strengthScaledCost(player, mana, manaScaling);
                  if (state.dual) {
                     mana = (int)Math.ceil(mana * 1.18);
                  }

                  if (consumeMana(player, mana)) {
                     float quality = Mth.clamp((state.weaponPower - 5.0F) / 27.0F, 0.0F, 0.85F);
                     float partial = Mth.clamp((float)heldTicks / 17.0F, 0.3F, 1.0F);

                     float width = switch (tier) {
                        case 1 -> 5.8F;
                        case 2 -> 8.5F;
                        case 3 -> 27.0F;
                        default -> 2.6F + 2.1F * partial;
                     };

                     float range = switch (tier) {
                        case 1 -> 29.0F;
                        case 2 -> 43.0F;
                        case 3 -> 180.0F;
                        default -> 12.0F + 10.0F * partial;
                     };
                     width *= 1.0F + quality * 0.24F + (state.dual ? 0.18F : 0.0F);
                     range *= 1.0F + quality * 0.18F;

                     float multiplier = switch (tier) {
                        case 1 -> 1.0F;
                        case 2 -> 1.75F;
                        case 3 -> 3.5F;
                        default -> 0.42F + partial * 0.3F;
                     };
                     float damage = swordSkillDamage(player, state.weaponPower, multiplier * (state.dual ? 1.1 : 1.0)) * 0.72F;
                     Vec3 look = player.getLookAngle().normalize();
                     Vec3 origin = player.getEyePosition().add(look.scale(1.9)).add(0.0, -0.22, 0.0);
                     LiuSwordBeamEntity beam = LiuSwordBeamEntity.spawn(
                        player.serverLevel(),
                        player,
                        origin,
                        look,
                        tier,
                        state.dual,
                        state.primaryColor,
                        state.secondaryColor,
                        width,
                        range,
                        tier == 3 ? 5.2F : 3.35F,
                        damage
                     );
                     beam.getPersistentData().putUUID("liu_owned_beam_owner", player.getUUID());

                     CooldownManager.set(player, "liu_charged_sword_beam", switch (tier) {
                        case 2 -> 30;
                        case 3 -> 55;
                        default -> 13;
                     });
                     player.level()
                        .playSound(
                           (Player)null,
                           player.blockPosition(),
                           tier == 3 ? SoundEvents.WARDEN_SONIC_BOOM : SoundEvents.TRIDENT_THROW,
                           SoundSource.PLAYERS,
                           tier == 3 ? 1.2F : 0.82F,
                           tier == 3 ? 0.72F : 1.15F + tier * 0.14F
                        );
                  }
               }
            }
         }
      }
   }

   public static void cancelBeamCharge(ServerPlayer player) {
      LiuZhigangCombatManager.BeamChargeState state = BEAM_CHARGES.remove(player.getUUID());
      if (state != null) {
         removeVfx(player.getServer(), state.effectId);
      }
   }

   public static void enhancedAttack(ServerPlayer player, boolean offhand, int comboIndex) {
      if (isCombatStance(player) && player.isAlive()) {
         long now = player.level().getGameTime();
         int pressure = refreshedEnhancedStrikePressure(player, now);
         if (player.getPersistentData().getLong("liu_next_enhanced_strike") > now) {
            addEnhancedStrikePressure(player, now);
         } else {
            ItemStack stack = offhand ? player.getOffhandItem() : player.getMainHandItem();
            if (isMeleeWeapon(stack)) {
               boolean finisher = Math.floorMod(comboIndex, 4) == 3;
               int mana = enhancedStrikeManaCost(player, finisher, pressure);
               if (consumeManaSilently(player, mana)) {
                  player.getPersistentData().putLong("liu_next_enhanced_strike", now + (finisher ? 8 : 6));
                  addEnhancedStrikePressure(player, now);
                  int color = colorFor(stack);
                  int otherColor = colorFor(offhand ? player.getMainHandItem() : player.getOffhandItem());
                  boolean dual = isMeleeWeapon(player.getMainHandItem()) && isMeleeWeapon(player.getOffhandItem());

                  float roll = switch (Math.floorMod(comboIndex, 4)) {
                     case 0 -> -26.0F;
                     case 1 -> 24.0F;
                     case 2 -> -8.0F;
                     default -> 18.0F;
                  };
                  if (offhand) {
                     roll = -roll;
                  }

                  float strikeRoll = roll;
                  Vec3 look = player.getLookAngle().normalize();
                  Vec3 origin = player.getEyePosition().add(look.scale(3.0)).add(0.0, -0.28, 0.0);
                  LiuSwordVfxEntity.spawnMoving(
                     player.serverLevel(),
                     origin,
                     look,
                     0,
                     color,
                     dual ? otherColor : color,
                     finisher ? 4.9F : 3.55F,
                     finisher ? 8.2F : 6.2F,
                     strikeRoll,
                     finisher ? 8 : 6,
                     finisher && dual,
                     finisher ? 1.38F : 1.12F
                  );
                  float damage = swordSkillDamage(player, stack, finisher ? 0.78 : 0.5) * 0.72F;
                  SololevelingMod.queueServerWork(
                     1,
                     () -> {
                        if (player.isAlive() && isCombatStance(player)) {
                           for (LivingEntity target : targetsInSlash(
                              player, player.getEyePosition(), look, finisher ? 8.5 : 6.3, finisher ? 3.2 : 2.0, finisher ? 8 : 4
                           )) {
                              if (dealSwordDamage(player, target, damage)) {
                                 markForDomain(player, target, 1);
                                 push(target, look, finisher ? 0.75 : 0.35, 0.08);
                                 if (isDomainActive(player)) {
                                    SololevelingMod.queueServerWork(
                                       4,
                                       () -> {
                                          if (player.isAlive() && isCombatStance(player) && isDomainActive(player) && isValidTarget(player, target)) {
                                             LiuSwordVfxEntity.spawnAttached(
                                                player.serverLevel(), target, 0, color, color, 2.1F, 3.4F, -strikeRoll * 0.7F, 8, false
                                             );
                                             dealSwordDamage(player, target, damage * 0.42F);
                                          }
                                       }
                                    );
                                 }
                              }
                           }
                        }
                     }
                  );
               }
            }
         }
      }
   }

   public static void beginDragonFlash(Entity entity) {
      if (entity instanceof ServerPlayer player
         && isCombatStance(player)
         && !FLASH_CHARGES.containsKey(player.getUUID())
         && !FLASH_DASHES.containsKey(player.getUUID())
         && !CooldownManager.isOnCooldown(player, "liu_dragon_flash")) {
         LivingEntity target = crosshairTarget(player, 52.0);
         UUID marker = null;
         if (target != null) {
            LiuSwordVfxEntity effect = LiuSwordVfxEntity.spawnAttached(
               player.serverLevel(),
               target,
               2,
               primaryHandColor(player),
               secondaryHandColor(player),
               Math.max(1.4F, target.getBbWidth() * 1.35F),
               Math.max(2.1F, target.getBbHeight()),
               0.0F,
               80,
               isDualWielding(player)
            );
            marker = effect.getUUID();
         }

         FLASH_CHARGES.put(
            player.getUUID(), new LiuZhigangCombatManager.FlashChargeState(target == null ? null : target.getUUID(), player.level().getGameTime(), marker)
         );
         player.level().playSound((Player)null, player.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.55F, target == null ? 1.1F : 1.55F);
      }
   }

   public static void releaseDragonFlash(Entity entity, int pressedMs) {
      if (entity instanceof ServerPlayer player && isCombatStance(player)) {
         LiuZhigangCombatManager.FlashChargeState charge = FLASH_CHARGES.remove(player.getUUID());
         if (charge != null && !CooldownManager.isOnCooldown(player, "liu_dragon_flash")) {
            if (charge.markerId != null) {
               removeVfx(player.serverLevel(), charge.markerId);
            }

            LivingEntity target = charge.targetId == null ? null : living(player.serverLevel(), charge.targetId);
            if (target != null && (!isValidTarget(player, target) || player.distanceToSqr(target) > 3600.0)) {
               target = null;
            }

            boolean manifested = isManifested(player);
            int mana = target == null ? (manifested ? 210 : 150) : (manifested ? 360 : 265);
            mana = VesselManaScaling.strengthScaledCost(player, mana, 0.65);
            if (consumeMana(player, mana)) {
               Vec3 direction = target == null
                  ? player.getLookAngle().normalize()
                  : target.getBoundingBox().getCenter().subtract(player.getBoundingBox().getCenter()).normalize();
               double power = Mth.clamp(0.78 + pressedMs / 1500.0, 0.8, 1.32);
               FLASH_DASHES.put(
                  player.getUUID(),
                  new LiuZhigangCombatManager.FlashDashState(
                     target == null ? null : target.getUUID(),
                     direction,
                     player.level().getGameTime(),
                     target == null ? 18 : 24,
                     power,
                     primaryHandColor(player),
                     secondaryHandColor(player),
                     isDualWielding(player),
                     new HashSet<>()
                  )
               );
               CooldownManager.set(player, "liu_dragon_flash", manifested ? 58 : 72);
               player.getPersistentData().putLong("liu_fall_safe_until", player.level().getGameTime() + 80L);
               player.setDeltaMovement(direction.scale((manifested ? 2.85 : 2.45) * power));
               player.hurtMarked = true;
            }
         }
      }
   }

   public static void castHeavenlyCounter(Entity entity) {
      if (entity instanceof ServerPlayer player && isLiuVessel(player) && ready(player, "Heavenly Counter")) {
         boolean manifested = isManifested(player);
         int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 360 : 250, 0.5);
         if (consumeMana(player, mana)) {
            player.getPersistentData().putLong("liu_counter_until", player.level().getGameTime() + (manifested ? 34L : 24L));
            player.getPersistentData().putInt("liu_counter_charges", manifested ? 2 : 1);
            CooldownManager.set(player, "Heavenly Counter", manifested ? 105 : 125);
            LiuSwordVfxEntity.spawnAttached(
               player.serverLevel(),
               player,
               4,
               primaryHandColor(player),
               secondaryHandColor(player),
               manifested ? 2.5F : 2.0F,
               manifested ? 4.1F : 3.3F,
               0.0F,
               manifested ? 34 : 24,
               isDualWielding(player)
            );
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.42F);
         }
      }
   }

   public static void castGoldenDragonDance(Entity entity) {
      if (entity instanceof ServerPlayer player && isLiuVessel(player) && ready(player, "Golden Dragon Dance") && !DANCES.containsKey(player.getUUID())) {
         boolean manifested = isManifested(player);
         int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 760 : 540, 0.8);
         if (consumeMana(player, mana)) {
            double radius = manifested ? 27.0 : 20.0;
            List<LivingEntity> found = targets(player, player.getBoundingBox().inflate(radius, radius * 0.5, radius));
            found.removeIf(target -> !player.hasLineOfSight(target));
            found.sort(Comparator.comparingDouble(player::distanceToSqr));
            int cap = manifested ? 10 : 6;
            List<UUID> ids = found.stream().limit(cap).map(Entity::getUUID).toList();
            CooldownManager.set(player, "Golden Dragon Dance", manifested ? 155 : 185);
            if (ids.isEmpty()) {
               spawnDanceWaves(player, manifested);
            } else {
               DANCES.put(
                  player.getUUID(),
                  new LiuZhigangCombatManager.DanceState(
                     new ArrayList<>(ids), 0, 0, player.level().getGameTime(), manifested, primaryHandColor(player), secondaryHandColor(player)
                  )
               );
               player.getPersistentData().putLong("liu_fall_safe_until", player.level().getGameTime() + 100L);
               player.level().playSound((Player)null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.55F);
            }
         }
      }
   }

   public static void castSovereignSwordDomain(Entity entity) {
      if (entity instanceof ServerPlayer player && isLiuVessel(player) && ready(player, "Sovereign Sword Domain")) {
         boolean manifested = isManifested(player);
         int mana = VesselManaScaling.strengthScaledCost(player, manifested ? 1420 : 1050, 1.0);
         if (consumeMana(player, mana)) {
            LiuZhigangCombatManager.DomainState old = DOMAINS.remove(player.getUUID());
            if (old != null) {
               finishDomain(player, old);
            }

            long now = player.level().getGameTime();
            int duration = manifested ? 280 : 220;
            DOMAINS.put(player.getUUID(), new LiuZhigangCombatManager.DomainState(now + duration, now, now, manifested));
            CooldownManager.set(player, "Sovereign Sword Domain", manifested ? 520 : 600);
            LiuSwordVfxEntity.spawnAttached(
               player.serverLevel(), player, 5, 16765774, 16774064, manifested ? 29.0F : 22.0F, manifested ? 11.0F : 8.5F, 0.0F, duration, true
            );
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.78F);
         }
      }
   }

   public static void toggleDragonSwordManifestation(Entity entity) {
      if (entity instanceof ServerPlayer player && isLiuVessel(player)) {
         if (isManifested(player)) {
            LiuManifestationManager.restore(player);
            LiuSwordVfxEntity.spawnAttached(player.serverLevel(), player, 7, 16765774, 16774064, 3.1F, 5.2F, 18.0F, 15, true);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.72F, 1.45F);
         } else if (ready(player, "Dragon Sword Manifestation") && consumeMana(player, 780)) {
            LiuManifestationManager.toggle(player);
            CooldownManager.set(player, "Dragon Sword Manifestation", 45);
            LiuSwordVfxEntity.spawnAttached(player.serverLevel(), player, 1, 16765774, 16774064, 3.8F, 6.2F, 0.0F, 32, true);
            LiuSwordVfxEntity.spawnAttached(player.serverLevel(), player, 7, 16765774, 16774064, 4.5F, 7.0F, 18.0F, 20, true);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.72F);
         }
      }
   }

   public static boolean hitBySwordBeam(ServerPlayer owner, LivingEntity target, float damage, int tier) {
      if (isLiuVessel(owner) && dealSwordDamage(owner, target, damage)) {
         markForDomain(owner, target, Math.max(1, tier));
         Vec3 direction = target.position().subtract(owner.position());
         if (direction.lengthSqr() > 0.001) {
            push(target, direction.normalize(), 0.25 + tier * 0.18, 0.08 + tier * 0.03);
         }

         return true;
      } else {
         return false;
      }
   }

   public static void detonateExecutionMarks(ServerPlayer owner, Set<UUID> ids, float damage, int primaryColor, int secondaryColor, boolean dual) {
      List<LivingEntity> victims = new ArrayList<>();

      for (UUID id : ids) {
         LivingEntity target = living(owner.serverLevel(), id);
         if (isValidTarget(owner, target)) {
            victims.add(target);
         }
      }

      victims.sort(Comparator.comparingDouble(owner::distanceToSqr));
      spawnExecutionDetonationEffects(owner, victims);

      for (LivingEntity target : victims) {
         dealSwordDamage(owner, target, damage);
         markForDomain(owner, target, 3);
      }

      for (UUID id : ids) {
         LivingEntity target = living(owner.serverLevel(), id);
         if (target != null) {
            releaseExecutionRestraint(owner, target);
         }
      }

      if (!victims.isEmpty()) {
         owner.level().playSound((Player)null, BlockPos.containing(victims.get(0).position()), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 0.68F);
      }
   }

   private static void spawnExecutionDetonationEffects(ServerPlayer owner, List<LivingEntity> victims) {
      ServerLevel level = owner.serverLevel();
      List<Vec3> centers = new ArrayList<>(victims.size());

      for (int index = 0; index < victims.size(); index++) {
         LivingEntity target = victims.get(index);
         Vec3 center = target.getBoundingBox().getCenter();
         centers.add(center);
         float blastWidth = Mth.clamp(Math.max(4.5F, target.getBbWidth() * 2.15F), 4.5F, 10.5F);
         float blastHeight = Mth.clamp(Math.max(4.8F, target.getBbHeight() * 1.65F), 4.8F, 12.0F);
         LiuSwordVfxEntity.spawn(
            level, center.x, center.y, center.z, 9, 14825496, 16767050, blastWidth, blastHeight, owner.getRandom().nextFloat() * 360.0F, 22, 0.0F, 0.0F, false
         );
         if (index < 10) {
            double spread = Math.min(1.2, blastWidth * 0.14);
            sendExecutionParticles(level, owner, center, ParticleTypes.EXPLOSION, 2, spread * 0.45, spread * 0.45, spread * 0.45, 0.02);
            sendExecutionParticles(level, owner, center, ParticleTypes.FLAME, 8, spread, spread * 0.8, spread, 0.12);
            sendExecutionParticles(level, owner, center, ParticleTypes.LAVA, 3, spread * 0.55, spread * 0.45, spread * 0.55, 0.05);
         }
      }

      spawnExecutionLinks(level, centers);
   }

   private static void sendExecutionParticles(
      ServerLevel level, ServerPlayer owner, Vec3 center, ParticleOptions particle, int count, double xSpread, double ySpread, double zSpread, double speed
   ) {
      for (ServerPlayer viewer : level.players()) {
         if (viewer == owner || !(viewer.position().distanceToSqr(center) > 147456.0)) {
            level.sendParticles(viewer, particle, true, center.x, center.y, center.z, count, xSpread, ySpread, zSpread, speed);
         }
      }
   }

   private static void spawnExecutionLinks(ServerLevel level, List<Vec3> centers) {
      int nodeCount = Math.min(48, centers.size());
      if (nodeCount >= 2) {
         List<LiuZhigangCombatManager.ExecutionLink> candidates = new ArrayList<>();

         for (int first = 0; first < nodeCount; first++) {
            for (int second = first + 1; second < nodeCount; second++) {
               double distanceSqr = centers.get(first).distanceToSqr(centers.get(second));
               if (distanceSqr >= 0.25 && distanceSqr <= 400.0) {
                  candidates.add(new LiuZhigangCombatManager.ExecutionLink(first, second, distanceSqr));
               }
            }
         }

         candidates.sort(Comparator.comparingDouble(LiuZhigangCombatManager.ExecutionLink::distanceSqr));
         int[] parent = new int[nodeCount];
         int index = 0;

         while (index < nodeCount) {
            parent[index] = index++;
         }

         index = 0;

         for (LiuZhigangCombatManager.ExecutionLink candidate : candidates) {
            int firstRoot = executionRoot(parent, candidate.first());
            int secondRoot = executionRoot(parent, candidate.second());
            if (firstRoot != secondRoot) {
               parent[secondRoot] = firstRoot;
               Vec3 start = centers.get(candidate.first());
               Vec3 end = centers.get(candidate.second());
               float distance = (float)Math.sqrt(candidate.distanceSqr());
               float width = Mth.clamp(0.22F + distance * 0.018F, 0.28F, 0.62F);
               LiuSwordVfxEntity.spawnExecutionLink(level, start, end, 14825496, 16767050, width, 16);
               if (++index >= 24) {
                  break;
               }
            }
         }
      }
   }

   private static int executionRoot(int[] parent, int node) {
      int root = node;

      while (parent[root] != root) {
         root = parent[root];
      }

      while (parent[node] != node) {
         int next = parent[node];
         parent[node] = root;
         node = next;
      }

      return root;
   }

   public static boolean registerExecutionTarget(
      ServerPlayer owner,
      UUID beamId,
      LivingEntity target,
      UUID markerEffect,
      float damage,
      int primaryColor,
      int secondaryColor,
      boolean dual,
      boolean allowCreate
   ) {
      if (owner != null && isLiuVessel(owner) && beamId != null && markerEffect != null && isValidTarget(owner, target)) {
         List<LiuZhigangCombatManager.ExecutionState> pending = EXECUTIONS.computeIfAbsent(owner.getUUID(), ignored -> new ArrayList<>());
         LiuZhigangCombatManager.ExecutionState state = null;

         for (LiuZhigangCombatManager.ExecutionState candidate : pending) {
            if (beamId.equals(candidate.beamId)) {
               state = candidate;
               break;
            }
         }

         if (state == null) {
            if (!allowCreate) {
               if (pending.isEmpty()) {
                  EXECUTIONS.remove(owner.getUUID());
               }

               return false;
            }

            state = new LiuZhigangCombatManager.ExecutionState(beamId, owner.level().getGameTime() + 50L, damage, primaryColor, secondaryColor, dual);
            pending.add(state);
         }

         UUID targetId = target.getUUID();
         state.targets.add(targetId);
         UUID previousMarker = state.markerEffects.put(targetId, markerEffect);
         if (previousMarker != null && !previousMarker.equals(markerEffect)) {
            removeVfx(owner.serverLevel(), previousMarker);
         }

         long until = state.detonateAt + 3L;
         int remainingTicks = (int)Math.max(2L, until - target.level().getGameTime());
         CompoundTag data = target.getPersistentData();
         data.putUUID("liu_execution_slow_owner", owner.getUUID());
         data.putLong("liu_execution_slow_until", Math.max(data.getLong("liu_execution_slow_until"), until));
         target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, remainingTicks, 2, false, false, true));
         return true;
      } else {
         return false;
      }
   }

   public static boolean isValidTarget(Player player, LivingEntity target) {
      if (player == null
         || target == null
         || target == player
         || !target.isAlive()
         || !target.isAttackable()
         || target.isInvulnerable()
         || target instanceof ArmorStand) {
         return false;
      }

      if (!player.isAlliedTo(target) && !target.isAlliedTo(player) && !ShadowMonarchManager.isOwnedShadow(target, player)) {
         if (target instanceof TamableAnimal tame && player.getUUID().equals(tame.getOwnerUUID())) {
            return false;
         } else {
            return !(target instanceof Player other) ? true : !other.isCreative() && !other.isSpectator() && player.canHarmPlayer(other);
         }
      } else {
         return false;
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide() && event.player instanceof ServerPlayer player) {
         if (player.isAlive() && isLiuVessel(player)) {
            tickCharge(player);
            tickFlash(player);
            tickDance(player);
            tickDomain(player);
            tickExecutions(player);
         } else {
            clearState(player);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onOwnedProjectileAttack(LivingAttackEvent event) {
      if (event.getSource().getDirectEntity() instanceof Projectile projectile && projectile.level() instanceof ServerLevel level) {
         CompoundTag data = projectile.getPersistentData();
         if (data.hasUUID("liu_domain_projectile_owner")) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(data.getUUID("liu_domain_projectile_owner"));
            if (owner == null || !isLiuVessel(owner)) {
               event.setCanceled(true);
               projectile.discard();
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onAttacked(LivingAttackEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && isLiuVessel(player) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         long now = player.level().getGameTime();
         int charges = player.getPersistentData().getInt("liu_counter_charges");
         if (charges > 0 && player.getPersistentData().getLong("liu_counter_until") >= now) {
            event.setCanceled(true);
            player.getPersistentData().putInt("liu_counter_charges", charges - 1);
            player.getPersistentData().putLong("liu_counter_surge_until", now + 100L);
            Entity direct = event.getSource().getDirectEntity();
            Entity attackerEntity = event.getSource().getEntity();
            if (direct instanceof Projectile projectile) {
               Vec3 reflected = projectile.getDeltaMovement().scale(-1.35).add(0.0, 0.08, 0.0);
               projectile.setOwner(player);
               projectile.getPersistentData().putUUID("liu_domain_projectile_owner", player.getUUID());
               projectile.setDeltaMovement(reflected);
               projectile.hurtMarked = true;
            }

            if (attackerEntity instanceof LivingEntity attacker && isValidTarget(player, attacker)) {
               float damage = swordSkillDamage(player, player.getMainHandItem(), 1.55 * (isManifested(player) ? 1.15 : 1.0));
               dealSwordDamage(player, attacker, damage);
               Vec3 away = attacker.position().subtract(player.position());
               push(attacker, away.lengthSqr() < 0.001 ? player.getLookAngle() : away.normalize(), 1.15, 0.24);
               LiuSwordVfxEntity.spawnAttached(
                  player.serverLevel(), attacker, 4, primaryHandColor(player), secondaryHandColor(player), 3.2F, 5.0F, 24.0F, 12, isDualWielding(player)
               );
            }

            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.75F, 1.62F);
         }
      }
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      if (event.getEntity() instanceof Player player && player.getPersistentData().getLong("liu_fall_safe_until") >= player.level().getGameTime()) {
         event.setCanceled(true);
         player.fallDistance = 0.0F;
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      clearState(event.getEntity());
   }

   private static void tickCharge(ServerPlayer player) {
      LiuZhigangCombatManager.BeamChargeState state = BEAM_CHARGES.get(player.getUUID());
      if (state != null) {
         long held = player.level().getGameTime() - state.startedAt;
         if (held <= 600L && isCombatStance(player) && sameHeldWeapons(player, state)) {
            if (held == 17L || held == 34L || held == 234L) {
               boolean executionReady = held == 234L;
               float pitch = held == 17L ? 1.25F : (held == 34L ? 1.55F : 1.9F);
               player.level()
                  .playSound(
                     (Player)null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, executionReady ? 1.0F : 0.62F, pitch
                  );
               LiuSwordVfxEntity.spawnAttached(
                  player.serverLevel(),
                  player,
                  1,
                  state.primaryColor,
                  state.secondaryColor,
                  executionReady ? 3.1F : 1.7F + (float)held / 38.0F,
                  executionReady ? 5.5F : 3.4F,
                  executionReady ? 18.0F : 0.0F,
                  8,
                  state.dual
               );
            }
         } else {
            cancelBeamCharge(player);
         }
      }
   }

   private static void tickFlash(ServerPlayer player) {
      LiuZhigangCombatManager.FlashChargeState charge = FLASH_CHARGES.get(player.getUUID());
      if (charge != null && player.level().getGameTime() - charge.startedAt > 85L) {
         FLASH_CHARGES.remove(player.getUUID());
         if (charge.markerId != null) {
            removeVfx(player.serverLevel(), charge.markerId);
         }
      }

      LiuZhigangCombatManager.FlashDashState state = FLASH_DASHES.get(player.getUUID());
      if (state != null) {
         long elapsed = player.level().getGameTime() - state.startedAt;
         LivingEntity target = state.targetId == null ? null : living(player.serverLevel(), state.targetId);
         if (state.targetId != null && !isValidTarget(player, target)) {
            finishFlash(player, state, null, false);
         } else {
            Vec3 motion;
            if (target != null) {
               if (CombatRangeHelper.withinSurfaceRange(player, target, 1.65)) {
                  finishFlash(player, state, target, true);
                  return;
               }

               Vec3 toward = target.getBoundingBox().getCenter().subtract(player.getBoundingBox().getCenter());
               motion = toward.normalize().scale(2.65 * state.power);
               dampenTargetForFlash(target);
            } else {
               motion = state.direction.scale(2.35 * state.power).add(0.0, -0.035 * Math.max(0L, elapsed - 5L), 0.0);
            }

            if (!player.serverLevel().hasChunkAt(BlockPos.containing(player.position().add(motion)))) {
               finishFlash(player, state, target, false);
            } else {
               boolean blocked = !player.serverLevel().noCollision(player, player.getBoundingBox().move(motion));
               if (!blocked && elapsed < state.maxTicks) {
                  player.setDeltaMovement(motion);
                  player.hurtMarked = true;
                  player.fallDistance = 0.0F;
                  if ((elapsed & 1L) == 0L) {
                     LiuSwordVfxEntity.spawnAttached(
                        player.serverLevel(), player, 3, state.primaryColor, state.secondaryColor, 1.65F, 4.2F, state.dual ? 18.0F : 0.0F, 4, state.dual
                     );
                  }

                  for (LivingEntity crossed : player.serverLevel()
                     .getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(1.4), candidate -> isValidTarget(player, candidate))) {
                     if (state.crossed.add(crossed.getUUID())) {
                        dealSwordDamage(player, crossed, swordSkillDamage(player, player.getMainHandItem(), 0.45));
                     }
                  }
               } else {
                  finishFlash(player, state, target, true);
               }
            }
         }
      }
   }

   private static void finishFlash(ServerPlayer player, LiuZhigangCombatManager.FlashDashState state, LivingEntity target, boolean impact) {
      FLASH_DASHES.remove(player.getUUID());
      player.setDeltaMovement(Vec3.ZERO);
      player.hurtMarked = true;
      player.fallDistance = 0.0F;
      if (impact) {
         Vec3 center = target == null ? player.position().add(player.getLookAngle().scale(1.4)) : target.getBoundingBox().getCenter();
         float damage = swordSkillDamage(player, player.getMainHandItem(), 2.2 * (isManifested(player) ? 1.18 : 1.0));

         for (LivingEntity victim : targets(player, new AABB(center, center).inflate(state.dual ? 6.8 : 5.2, 3.4, state.dual ? 6.8 : 5.2))) {
            if (dealSwordDamage(player, victim, victim == target ? damage * 1.2F : damage)) {
               markForDomain(player, victim, 2);
            }
         }

         LiuSwordVfxEntity.spawn(
            player.serverLevel(),
            center.x,
            center.y,
            center.z,
            7,
            state.primaryColor,
            state.secondaryColor,
            state.dual ? 8.5F : 6.4F,
            state.dual ? 10.0F : 7.2F,
            18.0F,
            17,
            player.getYRot(),
            0.0F,
            state.dual
         );
         player.level().playSound((Player)null, BlockPos.containing(center), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.25F, 0.72F);
      }
   }

   private static void tickDance(ServerPlayer player) {
      LiuZhigangCombatManager.DanceState state = DANCES.get(player.getUUID());
      if (state != null) {
         if (state.index < state.targets.size() && player.level().getGameTime() - state.startedAt <= 95L) {
            LivingEntity target = living(player.serverLevel(), state.targets.get(state.index));
            if (!isValidTarget(player, target)) {
               state.index++;
               state.approachTicks = 0;
            } else {
               Vec3 toward = target.getBoundingBox().getCenter().subtract(player.getBoundingBox().getCenter());
               boolean close = CombatRangeHelper.withinSurfaceRange(player, target, 1.7);
               if (!close && state.approachTicks < 4) {
                  Vec3 motion = toward.normalize().scale(state.manifested ? 2.45 : 2.05);
                  if (player.serverLevel().noCollision(player, player.getBoundingBox().move(motion))) {
                     player.setDeltaMovement(motion);
                     player.hurtMarked = true;
                     state.approachTicks++;
                     if ((state.approachTicks & 1) == 0) {
                        LiuSwordVfxEntity.spawnAttached(player.serverLevel(), player, 3, state.primaryColor, state.secondaryColor, 1.35F, 3.2F, 22.0F, 4, true);
                     }

                     return;
                  }
               }

               float damage = swordSkillDamage(player, player.getMainHandItem(), 1.2 * (state.manifested ? 1.16 : 1.0));
               dealSwordDamage(player, target, damage);
               markForDomain(player, target, 2);
               LiuSwordVfxEntity.spawnAttached(
                  player.serverLevel(),
                  target,
                  8,
                  state.index % 2 == 0 ? state.primaryColor : state.secondaryColor,
                  state.index % 2 == 0 ? state.secondaryColor : state.primaryColor,
                  3.0F + state.index * 0.18F,
                  5.2F,
                  state.index % 2 == 0 ? -28.0F : 28.0F,
                  10,
                  true
               );
               state.index++;
               state.approachTicks = 0;
               player.setDeltaMovement(Vec3.ZERO);
               player.hurtMarked = true;
            }
         } else {
            finishDance(player);
         }
      }
   }

   private static void finishDance(ServerPlayer player) {
      DANCES.remove(player.getUUID());
      player.setDeltaMovement(Vec3.ZERO);
      player.hurtMarked = true;
      LiuSwordVfxEntity.spawnAttached(player.serverLevel(), player, 7, primaryHandColor(player), secondaryHandColor(player), 5.4F, 7.5F, 18.0F, 13, true);
   }

   private static void spawnDanceWaves(ServerPlayer player, boolean manifested) {
      Vec3 origin = player.getEyePosition().add(0.0, -0.25, 0.0);

      for (int i = -1; i <= 1; i++) {
         int delay = (i + 1) * 3;
         int index = i;
         SololevelingMod.queueServerWork(
            delay,
            () -> {
               if (player.isAlive() && isLiuVessel(player)) {
                  Vec3 direction = rotateYaw(player.getLookAngle().normalize(), index * 11.0);
                  LiuSwordBeamEntity beam = LiuSwordBeamEntity.spawn(
                     player.serverLevel(),
                     player,
                     origin,
                     direction,
                     0,
                     false,
                     primaryHandColor(player),
                     secondaryHandColor(player),
                     manifested ? 4.8F : 3.6F,
                     manifested ? 25.0F : 19.0F,
                     3.5F,
                     swordSkillDamage(player, player.getMainHandItem(), 0.9 * (manifested ? 1.15 : 1.0)) * 0.82F
                  );
                  beam.getPersistentData().putUUID("liu_owned_beam_owner", player.getUUID());
               }
            }
         );
      }
   }

   private static void tickDomain(ServerPlayer player) {
      LiuZhigangCombatManager.DomainState state = DOMAINS.get(player.getUUID());
      if (state != null) {
         long now = player.level().getGameTime();
         if (now >= state.endTick) {
            DOMAINS.remove(player.getUUID());
            finishDomain(player, state);
         } else {
            if (now >= state.nextScan) {
               state.nextScan = now + 10L;
               double radius = state.manifested ? 28.0 : 22.0;
               List<LivingEntity> nearby = targets(player, player.getBoundingBox().inflate(radius, 10.0, radius));
               nearby.sort(Comparator.comparingDouble(player::distanceToSqr));

               for (LivingEntity target : nearby.stream().limit(state.manifested ? 18L : 12L).toList()) {
                  markForDomain(player, target, 1);
                  if (player.tickCount % 20 == 0) {
                     LiuSwordVfxEntity.spawnAttached(
                        player.serverLevel(),
                        target,
                        6,
                        16765774,
                        16774064,
                        Math.max(1.1F, target.getBbWidth()),
                        Math.max(1.8F, target.getBbHeight()),
                        0.0F,
                        14,
                        false
                     );
                  }
               }
            }

            if (now >= state.nextDeflect) {
               state.nextDeflect = now + 4L;
               int count = 0;

               for (Projectile projectile : player.serverLevel()
                  .getEntitiesOfClass(
                     Projectile.class, player.getBoundingBox().inflate(state.manifested ? 10.0 : 7.5), projectilex -> projectilex.getOwner() != player
                  )) {
                  if (count++ >= (state.manifested ? 10 : 6)) {
                     break;
                  }

                  Vec3 away = projectile.position().subtract(player.getEyePosition());
                  if (away.lengthSqr() < 0.001) {
                     away = player.getLookAngle();
                  }

                  projectile.setOwner(player);
                  projectile.getPersistentData().putUUID("liu_domain_projectile_owner", player.getUUID());
                  projectile.setDeltaMovement(away.normalize().scale(Math.max(1.0, projectile.getDeltaMovement().length() * 1.15)));
                  projectile.hurtMarked = true;
               }
            }
         }
      }
   }

   private static void finishDomain(ServerPlayer player, LiuZhigangCombatManager.DomainState state) {
      double radius = state.manifested ? 32.0 : 25.0;
      List<LivingEntity> marked = targets(player, player.getBoundingBox().inflate(radius, 14.0, radius));
      marked.removeIf(targetx -> !hasDomainMark(player, targetx));

      for (LivingEntity target : marked) {
         int stacks = target.getPersistentData().getInt("liu_domain_mark_stacks");
         LiuSwordVfxEntity.spawnAttached(
            player.serverLevel(),
            target,
            7,
            16765774,
            16774064,
            Math.max(3.0F, target.getBbWidth() * 1.8F),
            Math.max(3.8F, target.getBbHeight() * 1.35F),
            stacks % 2 == 0 ? 20.0F : -20.0F,
            15,
            true
         );
      }

      for (LivingEntity target : marked) {
         int stacks = Mth.clamp(target.getPersistentData().getInt("liu_domain_mark_stacks"), 1, 8);
         float damage = swordSkillDamage(player, player.getMainHandItem(), 0.75 * (1.0 + stacks * 0.16));
         dealSwordDamage(player, target, damage);
         clearDomainMark(target);
      }

      if (!marked.isEmpty()) {
         player.level().playSound((Player)null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.92F, 1.62F);
      }
   }

   private static boolean isDomainActive(ServerPlayer player) {
      LiuZhigangCombatManager.DomainState state = DOMAINS.get(player.getUUID());
      return state != null && state.endTick > player.level().getGameTime();
   }

   private static void markForDomain(ServerPlayer player, LivingEntity target, int stacks) {
      if (isDomainActive(player) && isValidTarget(player, target)) {
         long now = target.level().getGameTime();
         if (!target.getPersistentData().hasUUID("liu_domain_mark_owner")
            || !player.getUUID().equals(target.getPersistentData().getUUID("liu_domain_mark_owner"))
            || target.getPersistentData().getLong("liu_domain_mark_until") < now) {
            target.getPersistentData().putInt("liu_domain_mark_stacks", 0);
         }

         target.getPersistentData().putUUID("liu_domain_mark_owner", player.getUUID());
         target.getPersistentData().putLong("liu_domain_mark_until", now + 340L);
         target.getPersistentData()
            .putInt("liu_domain_mark_stacks", Math.min(8, target.getPersistentData().getInt("liu_domain_mark_stacks") + Math.max(1, stacks)));
      }
   }

   private static boolean hasDomainMark(ServerPlayer player, LivingEntity target) {
      return target.getPersistentData().hasUUID("liu_domain_mark_owner")
         && player.getUUID().equals(target.getPersistentData().getUUID("liu_domain_mark_owner"))
         && target.getPersistentData().getLong("liu_domain_mark_until") >= target.level().getGameTime();
   }

   private static void clearDomainMark(LivingEntity target) {
      target.getPersistentData().remove("liu_domain_mark_owner");
      target.getPersistentData().remove("liu_domain_mark_until");
      target.getPersistentData().remove("liu_domain_mark_stacks");
   }

   private static void tickExecutions(ServerPlayer player) {
      List<LiuZhigangCombatManager.ExecutionState> pending = EXECUTIONS.get(player.getUUID());
      if (pending != null) {
         long now = player.level().getGameTime();

         for (int i = pending.size() - 1; i >= 0; i--) {
            LiuZhigangCombatManager.ExecutionState state = pending.get(i);
            if (!state.impactSent && now >= state.detonateAt - 16L) {
               state.impactSent = true;
               sendExecutionImpactFrame(player, state);
            }

            if (now >= state.detonateAt) {
               detonateExecutionMarks(player, state.targets, state.damage, state.primaryColor, state.secondaryColor, state.dual);

               for (UUID markerId : state.markerEffects.values()) {
                  removeVfx(player.serverLevel(), markerId);
               }

               pending.remove(i);
            }
         }

         if (pending.isEmpty()) {
            EXECUTIONS.remove(player.getUUID());
         }
      }
   }

   private static void sendExecutionImpactFrame(ServerPlayer owner, LiuZhigangCombatManager.ExecutionState state) {
      for (UUID targetId : state.targets) {
         LivingEntity target = living(owner.serverLevel(), targetId);
         if (isValidTarget(owner, target)) {
            LiuExecutionImpactMessage message = new LiuExecutionImpactMessage(16, state.primaryColor, state.secondaryColor);
            SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> owner), message);
            return;
         }
      }
   }

   private static void releaseExecutionRestraint(ServerPlayer owner, LivingEntity target) {
      releaseExecutionRestraint(owner, target, false);
   }

   private static void releaseExecutionRestraint(ServerPlayer owner, LivingEntity target, boolean force) {
      CompoundTag data = target.getPersistentData();
      if (data.hasUUID("liu_execution_slow_owner") && owner.getUUID().equals(data.getUUID("liu_execution_slow_owner"))) {
         if (force || data.getLong("liu_execution_slow_until") <= target.level().getGameTime() + 5L) {
            MobEffectInstance effect = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (effect != null && effect.getAmplifier() == 2) {
               target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }

            data.remove("liu_execution_slow_owner");
            data.remove("liu_execution_slow_until");
         }
      }
   }

   private static void cancelPendingExecutions(ServerPlayer player) {
      List<LiuZhigangCombatManager.ExecutionState> pending = EXECUTIONS.remove(player.getUUID());
      if (pending != null) {
         for (LiuZhigangCombatManager.ExecutionState state : pending) {
            for (UUID targetId : state.targets) {
               LivingEntity target = living(player.getServer(), targetId);
               if (target != null) {
                  releaseExecutionRestraint(player, target, true);
               }
            }

            for (UUID markerId : state.markerEffects.values()) {
               removeVfx(player.getServer(), markerId);
            }
         }
      }
   }

   private static void clearState(Entity entity) {
      if (entity != null) {
         UUID id = entity.getUUID();
         if (entity instanceof ServerPlayer player) {
            cancelBeamCharge(player);
            cancelPendingExecutions(player);
            player.getPersistentData().remove("liu_next_enhanced_strike");
            player.getPersistentData().remove("liu_enhanced_strike_pressure");
            player.getPersistentData().remove("liu_enhanced_strike_pressure_last");
            player.getPersistentData().remove("liu_fall_safe_until");
            player.getPersistentData().remove("liu_counter_until");
            player.getPersistentData().remove("liu_counter_charges");
            player.getPersistentData().remove("liu_counter_surge_until");
         }

         LiuZhigangCombatManager.FlashChargeState charge = FLASH_CHARGES.remove(id);
         if (charge != null && charge.markerId != null && entity instanceof ServerPlayer player) {
            removeVfx(player.getServer(), charge.markerId);
         }

         boolean wasMoving = FLASH_DASHES.remove(id) != null;
         wasMoving |= DANCES.remove(id) != null;
         DOMAINS.remove(id);
         if (wasMoving && entity instanceof Player player) {
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
            player.fallDistance = 0.0F;
         }
      }
   }

   private static int refreshedEnhancedStrikePressure(ServerPlayer player, long now) {
      CompoundTag data = player.getPersistentData();
      long last = data.getLong("liu_enhanced_strike_pressure_last");
      if (last > 0L && now >= last && now - last <= 45L) {
         return Mth.clamp(data.getInt("liu_enhanced_strike_pressure"), 0, 6);
      }

      data.remove("liu_enhanced_strike_pressure");
      data.remove("liu_enhanced_strike_pressure_last");
      return 0;
   }

   private static void addEnhancedStrikePressure(ServerPlayer player, long now) {
      CompoundTag data = player.getPersistentData();
      int pressure = refreshedEnhancedStrikePressure(player, now);
      data.putInt("liu_enhanced_strike_pressure", Math.min(6, pressure + 1));
      data.putLong("liu_enhanced_strike_pressure_last", now);
   }

   private static int enhancedStrikeManaCost(ServerPlayer player, boolean finisher, int pressure) {
      int base = VesselManaScaling.strengthScaledCost(player, finisher ? 42 : 26, 0.45);
      return pressure <= 0 ? base : (int)Math.ceil(base * (1.0 + 0.22 * Math.min(pressure, 6)));
   }

   private static boolean sameHeldWeapons(ServerPlayer player, LiuZhigangCombatManager.BeamChargeState state) {
      ItemStack main = player.getMainHandItem();
      ItemStack off = player.getOffhandItem();
      if (!isMeleeWeapon(main)) {
         main = off;
      }

      return state.mainSignature.equals(itemSignature(main)) && state.offSignature.equals(itemSignature(off));
   }

   private static String itemSignature(ItemStack stack) {
      if (stack.isEmpty()) {
         return "empty";
      }

      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return id == null ? stack.getItem().getDescriptionId() : id.toString();
   }

   private static double weaponPower(ItemStack stack) {
      if (stack.isEmpty()) {
         return 2.0;
      }

      double value = 1.0;

      for (AttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
         value += modifier.getAmount();
      }

      return Mth.clamp(value, 2.0, 42.0);
   }

   private static float swordSkillDamage(ServerPlayer player, ItemStack weapon, double abilityScale) {
      return swordSkillDamage(player, weaponPower(weapon), abilityScale);
   }

   private static float swordSkillDamage(ServerPlayer player, double weaponPower, double abilityScale) {
      double strength = Math.max(0.0, TemporaryStatBonusManager.effectiveStrength(player));
      double mastery = 1.0 + strength * 0.12 + Math.pow(strength, 1.25) * 0.025;
      double weaponEfficiency = 0.82 + Mth.clamp(weaponPower, 2.0, 30.0) / 70.0;
      return (float)Math.max(0.5, mastery * weaponEfficiency * abilityScale);
   }

   private static int primaryHandColor(ServerPlayer player) {
      ItemStack main = isMeleeWeapon(player.getMainHandItem()) ? player.getMainHandItem() : player.getOffhandItem();
      return colorFor(main);
   }

   private static int secondaryHandColor(ServerPlayer player) {
      return isMeleeWeapon(player.getOffhandItem()) ? colorFor(player.getOffhandItem()) : primaryHandColor(player);
   }

   private static boolean isDualWielding(ServerPlayer player) {
      return isMeleeWeapon(player.getMainHandItem()) && isMeleeWeapon(player.getOffhandItem());
   }

   private static LivingEntity crosshairTarget(ServerPlayer player, double range) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle().normalize();
      AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.8);
      LivingEntity best = null;
      double bestAlong = Double.MAX_VALUE;

      for (LivingEntity target : targets(player, search)) {
         Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
         double along = to.dot(look);
         if (!(along <= 0.0) && !(along > range)) {
            double perpendicular = to.subtract(look.scale(along)).length();
            if (perpendicular <= 1.1 + target.getBbWidth() * 0.55 && along < bestAlong && player.hasLineOfSight(target)) {
               best = target;
               bestAlong = along;
            }
         }
      }

      return best;
   }

   private static List<LivingEntity> targets(ServerPlayer player, AABB area) {
      return player.serverLevel().getEntitiesOfClass(LivingEntity.class, area, target -> isValidTarget(player, target));
   }

   private static List<LivingEntity> targetsInSlash(ServerPlayer player, Vec3 start, Vec3 direction, double range, double width, int cap) {
      Vec3 end = start.add(direction.scale(range));
      AABB area = new AABB(start, end).inflate(width, width * 0.75, width);
      List<LivingEntity> found = targets(player, area);
      found.removeIf(
         target -> {
            Vec3 center = target.getBoundingBox().getCenter();
            Vec3 relative = center.subtract(start);
            double along = relative.dot(direction);
            return !(along < -0.5) && !(along > range + target.getBbWidth())
               ? relative.subtract(direction.scale(Mth.clamp(along, 0.0, range))).length() > width + target.getBbWidth() * 0.65
               : true;
         }
      );
      found.sort(Comparator.comparingDouble(player::distanceToSqr));
      return found.stream().limit(cap).toList();
   }

   private static boolean dealSwordDamage(ServerPlayer player, LivingEntity target, float damage) {
      if (!isValidTarget(player, target)) {
         return false;
      }

      target.invulnerableTime = 0;
      boolean hurt = target.hurt(player.damageSources().playerAttack(player), Math.max(0.5F, damage));
      if (hurt) {
         target.setLastHurtByPlayer(player);
      }

      return hurt;
   }

   private static void dampenTargetForFlash(LivingEntity target) {
      double factor = controlFactor(target);
      Vec3 movement = target.getDeltaMovement();
      target.setDeltaMovement(movement.x * (1.0 - factor), Math.max(0.015, movement.y * 0.18), movement.z * (1.0 - factor));
      target.fallDistance = 0.0F;
      target.hurtMarked = true;
   }

   private static double controlFactor(LivingEntity target) {
      double resistance = Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
      double sizePenalty = !(target.getBbWidth() >= 4.0F) && !(target.getMaxHealth() >= 500.0F)
         ? (!(target.getBbWidth() >= 2.4F) && !(target.getMaxHealth() >= 220.0F) ? 1.0 : 0.5)
         : 0.25;
      return Mth.clamp((1.0 - resistance * 0.8) * sizePenalty, 0.1, 1.0);
   }

   private static void push(LivingEntity target, Vec3 direction, double horizontal, double vertical) {
      Vec3 flat = new Vec3(direction.x, 0.0, direction.z);
      if (flat.lengthSqr() < 0.001) {
         flat = new Vec3(0.0, 0.0, 1.0);
      }

      double factor = controlFactor(target);
      flat = flat.normalize().scale(horizontal * factor);
      target.setDeltaMovement(flat.x, Math.max(target.getDeltaMovement().y, vertical * factor), flat.z);
      target.hurtMarked = true;
   }

   private static Vec3 rotateYaw(Vec3 vector, double degrees) {
      double radians = Math.toRadians(degrees);
      double sin = Math.sin(radians);
      double cos = Math.cos(radians);
      return new Vec3(vector.x * cos - vector.z * sin, vector.y, vector.x * sin + vector.z * cos).normalize();
   }

   private static LivingEntity living(ServerLevel level, UUID id) {
      return level.getEntity(id) instanceof LivingEntity living ? living : null;
   }

   private static LivingEntity living(MinecraftServer server, UUID id) {
      if (server != null && id != null) {
         for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof LivingEntity living) {
               return living;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static void removeVfx(ServerLevel level, UUID id) {
      Entity effect = id == null ? null : level.getEntity(id);
      if (effect != null) {
         effect.discard();
      }
   }

   private static void removeVfx(MinecraftServer server, UUID id) {
      if (server != null && id != null) {
         for (ServerLevel level : server.getAllLevels()) {
            Entity effect = level.getEntity(id);
            if (effect != null) {
               effect.discard();
               return;
            }
         }
      }
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
         capability.MP = Math.max(0.0, capability.MP - amount);
         capability.syncPlayerVariables(player);
      });
      CooldownManager.set(player, "mana_refresh", 35);
      return true;
   }

   private static boolean ready(ServerPlayer player, String key) {
      if (!CooldownManager.isOnCooldown(player, key)) {
         return true;
      }

      player.displayClientMessage(Component.literal(key + " is on cooldown").withStyle(ChatFormatting.RED), true);
      return false;
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private record BeamChargeState(
      long startedAt, String mainSignature, String offSignature, int primaryColor, int secondaryColor, boolean dual, float weaponPower, UUID effectId
   ) {
   }

   private static final class DanceState {
      private final List<UUID> targets;
      private int index;
      private int approachTicks;
      private final long startedAt;
      private final boolean manifested;
      private final int primaryColor;
      private final int secondaryColor;

      private DanceState(List<UUID> targets, int index, int approachTicks, long startedAt, boolean manifested, int primaryColor, int secondaryColor) {
         this.targets = targets;
         this.index = index;
         this.approachTicks = approachTicks;
         this.startedAt = startedAt;
         this.manifested = manifested;
         this.primaryColor = primaryColor;
         this.secondaryColor = secondaryColor;
      }
   }

   private static final class DomainState {
      private final long endTick;
      private long nextScan;
      private long nextDeflect;
      private final boolean manifested;

      private DomainState(long endTick, long nextScan, long nextDeflect, boolean manifested) {
         this.endTick = endTick;
         this.nextScan = nextScan;
         this.nextDeflect = nextDeflect;
         this.manifested = manifested;
      }
   }

   private record ExecutionLink(int first, int second, double distanceSqr) {
   }

   private static final class ExecutionState {
      private final UUID beamId;
      private final long detonateAt;
      private final Set<UUID> targets;
      private final Map<UUID, UUID> markerEffects;
      private final float damage;
      private final int primaryColor;
      private final int secondaryColor;
      private final boolean dual;
      private boolean impactSent;

      private ExecutionState(UUID beamId, long detonateAt, float damage, int primaryColor, int secondaryColor, boolean dual) {
         this.beamId = beamId;
         this.detonateAt = detonateAt;
         this.targets = new HashSet<>();
         this.markerEffects = new HashMap<>();
         this.damage = damage;
         this.primaryColor = primaryColor;
         this.secondaryColor = secondaryColor;
         this.dual = dual;
      }
   }

   private record FlashChargeState(UUID targetId, long startedAt, UUID markerId) {
   }

   private record FlashDashState(
      UUID targetId, Vec3 direction, long startedAt, int maxTicks, double power, int primaryColor, int secondaryColor, boolean dual, Set<UUID> crossed
   ) {
   }
}
