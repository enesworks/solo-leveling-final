package dev.eness.sololevelingfinal.core.util;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.StealthBossDetectionHelper;

@EventBusSubscriber(modid = "sololeveling")
public final class ColdBloodSkillManager {
   public static final String SKILL = "Cold Blood";
   private static final String ACTIVE_UNTIL = "sl_cold_blood_until";
   private static final String STACKS = "sl_cold_blood_stacks";
   private static final String NEXT_FX = "sl_cold_blood_next_fx";
   private static final TagKey<EntityType<?>> SOLO_BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss"));
   private static final int MP_COST = 800;
   private static final int COOLDOWN_TICKS = 700;
   private static final int BASE_DURATION_TICKS = 240;
   private static final int MAX_DURATION_TICKS = 440;
   private static final int KILL_EXTENSION_TICKS = 80;
   private static final int COOLDOWN_REFUND_TICKS = 80;
   private static final int MIN_REMAINING_COOLDOWN_TICKS = 160;
   private static final int MAX_STACKS = 5;

   private ColdBloodSkillManager() {
   }

   public static void cast(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         if (vars.MP < 800.0) {
            player.displayClientMessage(Component.literal("You dont have enough MP"), true);
         } else if (CooldownManager.isOnCooldown(player, "Cold Blood")) {
            player.displayClientMessage(Component.literal("Ability on cooldown! " + CooldownManager.getRemainingSeconds(player, "Cold Blood") + "s"), true);
         } else {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = Math.max(0.0, capability.MP - 800.0);
               capability.syncPlayerVariables(player);
            });
            long now = player.level().getGameTime();
            CompoundTag data = player.getPersistentData();
            data.putLong("sl_cold_blood_until", now + 240L);
            data.putInt("sl_cold_blood_stacks", 0);
            data.putLong("sl_cold_blood_next_fx", 0L);
            CooldownManager.set(player, "Cold Blood", 700);
            CooldownManager.set(player, "mana_refresh", 60);
            applyBuffs(player);
            emitCasterParticles(player, 36);
            player.displayClientMessage(Component.literal("Using Cold Blood").withStyle(ChatFormatting.DARK_RED), true);
         }
      }
   }

   public static boolean isActive(Entity entity) {
      return entity != null && entity.getPersistentData().getLong("sl_cold_blood_until") > entity.level().getGameTime();
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         CompoundTag data = player.getPersistentData();
         if (data.getLong("sl_cold_blood_until") > 0L) {
            long now = player.level().getGameTime();
            if (player.isAlive() && data.getLong("sl_cold_blood_until") > now) {
               if (player.tickCount % 10 == 0) {
                  applyBuffs(player);
               }

               if (data.getLong("sl_cold_blood_next_fx") <= now) {
                  data.putLong("sl_cold_blood_next_fx", now + 8L);
                  emitCasterParticles(player, 8 + stacks(player) * 2);
               }
            } else {
               clear(player);
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onLivingHurt(LivingHurtEvent event) {
      if (!(event.getAmount() <= 0.0F) && !event.getEntity().level().isClientSide()) {
         ServerPlayer attacker = owningPlayer(event.getSource().getEntity());
         if (attacker == null) {
            attacker = owningPlayer(event.getSource().getDirectEntity());
         }

         if (attacker != null && isActive(attacker)) {
            LivingEntity target = event.getEntity();
            if (target != attacker && MageCombatHelper.isValidTarget(attacker, target)) {
               int stacks = stacks(attacker);
               boolean playerTarget = target instanceof Player;
               boolean boss = isBoss(target);
               float multiplier = damageMultiplier(stacks, boss, playerTarget);
               float boosted = event.getAmount() * multiplier;
               if (boss && target.getHealth() <= target.getMaxHealth() * 0.15F) {
                  float finisher = Math.min(32.0F, Math.max(4.0F, target.getMaxHealth() * 0.04F));
                  boosted += finisher;
               } else if (!boss && !playerTarget && target.getHealth() - boosted <= target.getMaxHealth() * 0.25F) {
                  target.invulnerableTime = 0;
                  boosted = Math.max(boosted, target.getHealth() + Math.max(1.0F, target.getMaxHealth() * 0.05F));
               }

               event.setAmount(boosted);
               emitTargetParticles(attacker.serverLevel(), target, boss ? 10 : 14);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      if (!event.getEntity().level().isClientSide() && !(event.getEntity() instanceof Player)) {
         ServerPlayer player = owningPlayer(event.getSource().getEntity());
         if (player == null) {
            player = owningPlayer(event.getSource().getDirectEntity());
         }

         if (player != null && isActive(player) && !MageCombatHelper.areAllied(player, event.getEntity())) {
            CompoundTag data = player.getPersistentData();
            long now = player.level().getGameTime();
            data.putInt("sl_cold_blood_stacks", Math.min(5, data.getInt("sl_cold_blood_stacks") + 1));
            long currentUntil = Math.max(data.getLong("sl_cold_blood_until"), now);
            data.putLong("sl_cold_blood_until", Math.min(now + 440L, currentUntil + 80L));
            restoreResources(player);
            reduceCooldown(player);
            emitCasterParticles(player, 24);
            player.displayClientMessage(
               Component.literal("Cold Blood x" + data.getInt("sl_cold_blood_stacks")).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true
            );
         }
      }
   }

   private static void applyBuffs(ServerPlayer player) {
      player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, false, true));
      player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, false, true));
   }

   private static void restoreResources(ServerPlayer player) {
      player.heal(Math.min(4.0F, Math.max(2.0F, player.getMaxHealth() * 0.06F)));
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.MP = Math.min(capability.Mana, capability.MP + Math.max(120.0, capability.Mana * 0.04));
         capability.syncPlayerVariables(player);
      });
   }

   private static void reduceCooldown(ServerPlayer player) {
      int remaining = CooldownManager.getRemainingTicks(player, "Cold Blood");
      if (remaining > 160) {
         CooldownManager.set(player, "Cold Blood", Math.max(160, remaining - 80));
      }
   }

   private static float damageMultiplier(int stacks, boolean boss, boolean playerTarget) {
      if (playerTarget) {
         return 1.08F + stacks * 0.02F;
      } else {
         return boss ? 1.15F + stacks * 0.03F : 1.25F + stacks * 0.05F;
      }
   }

   private static int stacks(ServerPlayer player) {
      return Mth.clamp(player.getPersistentData().getInt("sl_cold_blood_stacks"), 0, 5);
   }

   private static boolean isBoss(LivingEntity target) {
      return target.getType().is(SOLO_BOSS_TAG) || StealthBossDetectionHelper.seesThroughStealth(target);
   }

   private static void clear(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      data.remove("sl_cold_blood_until");
      data.remove("sl_cold_blood_stacks");
      data.remove("sl_cold_blood_next_fx");
   }

   private static void emitCasterParticles(ServerPlayer player, int count) {
      ServerLevel level = player.serverLevel();
      level.sendParticles(SololevelingModParticleTypes.BLOOD_PARTICLE.get(), player.getX(), player.getY() + 1.0, player.getZ(), count, 0.55, 0.7, 0.55, 0.08);
      level.sendParticles(
         SololevelingModParticleTypes.RED_DUST_PARTICLE.get(), player.getX(), player.getY() + 1.0, player.getZ(), Math.max(4, count / 3), 0.45, 0.6, 0.45, 0.02
      );
   }

   private static void emitTargetParticles(ServerLevel level, LivingEntity target, int count) {
      level.sendParticles(
         SololevelingModParticleTypes.BLOOD_PARTICLE.get(),
         target.getX(),
         target.getY() + target.getBbHeight() * 0.6,
         target.getZ(),
         count,
         target.getBbWidth() * 0.35,
         target.getBbHeight() * 0.25,
         target.getBbWidth() * 0.35,
         0.12
      );
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static ServerPlayer owningPlayer(Entity source) {
      if (source instanceof ServerPlayer player) {
         return player;
      } else if (source instanceof Projectile projectile) {
         return owningPlayer(projectile.getOwner());
      } else if (source instanceof TamableAnimal tame && tame.getOwner() instanceof ServerPlayer owner) {
         return owner;
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
}
