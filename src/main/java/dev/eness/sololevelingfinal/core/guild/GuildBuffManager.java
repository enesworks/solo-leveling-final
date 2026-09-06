package dev.eness.sololevelingfinal.core.guild;

import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.ManaArrowEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public final class GuildBuffManager {
   private static final UUID FOOTWORK_SPEED_ID = UUID.fromString("c71353c3-7299-4829-88af-1f8a0c715063");
   private static final String FOOTWORK_SPEED_NAME = "Guild assassin footwork";
   private static final String COMBAT_UNTIL = "sl_guild_combat_until";
   private static final String DEFENSE_UNTIL = "sl_guild_defense_until";
   private static final String BATTLE_UNTIL = "sl_guild_battle_until";
   private static final String LAST_MP = "sl_guild_last_mp";

   private GuildBuffManager() {
   }

   public static boolean hasActive(Entity entity, int buffId) {
      GuildData guild = guildFor(entity);
      return guild != null && (guild.activeBuffSlot1 == buffId || guild.activeBuffSlot2 == buffId);
   }

   public static double xpMultiplier(Entity entity) {
      return hasActive(entity, 9) ? 1.15 : 1.0;
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void onLivingHurt(LivingHurtEvent event) {
      if (!(event.getAmount() <= 0.0F) && !event.getEntity().level().isClientSide()) {
         ServerPlayer attacker = owningPlayer(event.getSource().getEntity());
         if (attacker != null) {
            markCombat(attacker, 160);
            boolean magic = isMagicDamage(event.getSource());
            boolean skill = isSkillDamage(event.getSource());
            float multiplier = 1.0F;
            if (!magic && hasActive(attacker, 1)) {
               multiplier *= 1.15F;
            }

            if (magic && hasActive(attacker, 2)) {
               multiplier *= 1.1F;
            }

            if (hasActive(attacker, 5) && isPrecisionHit(attacker)) {
               multiplier *= 1.15F;
            }

            if (hasActive(attacker, 8)) {
               long now = attacker.level().getGameTime();
               if (skill || attacker.getPersistentData().getLong("sl_guild_battle_until") > now) {
                  multiplier *= 1.12F;
                  attacker.getPersistentData().putLong("sl_guild_battle_until", now + 80L);
               }
            }

            event.setAmount(event.getAmount() * multiplier);
         }

         if (event.getEntity() instanceof ServerPlayer victim) {
            if (hasActive(victim, 4) && victim.getRandom().nextFloat() < 0.1F) {
               event.setCanceled(true);
               markCombat(victim, 160);
               return;
            }

            float multiplier = 1.0F;
            if (hasActive(victim, 3)) {
               if (victim.isBlocking()) {
                  multiplier *= 0.85F;
               }

               if (victim.getPersistentData().getLong("sl_guild_defense_until") > victim.level().getGameTime()) {
                  multiplier *= 0.92F;
               }

               victim.getPersistentData().putLong("sl_guild_defense_until", victim.level().getGameTime() + 40L);
            }

            markCombat(victim, 160);
            event.setAmount(event.getAmount() * multiplier);
         }
      }
   }

   @SubscribeEvent
   public static void onLivingHeal(LivingHealEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && hasActive(player, 6)) {
         event.setAmount(event.getAmount() * 1.2F);
      }
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      ServerPlayer player = owningPlayer(event.getSource().getEntity());
      if (player != null && hasActive(player, 6)) {
         player.heal(Math.max(1.0F, player.getMaxHealth() * 0.03F));
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            vars.MP = Math.min(vars.Mana, vars.MP + Math.max(5.0, vars.Mana * 0.05));
            vars.syncPlayerVariables(player);
         });
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         applyFootworkSpeed(player);
         tickManaBuffs(player);
      }
   }

   private static void tickManaBuffs(ServerPlayer player) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
         double beforeMp = vars.MP;
         double lastMp = player.getPersistentData().contains("sl_guild_last_mp") ? player.getPersistentData().getDouble("sl_guild_last_mp") : vars.MP;
         if (hasActive(player, 7) && vars.MP < lastMp) {
            double spent = lastMp - vars.MP;
            vars.MP = Math.min(vars.Mana, vars.MP + spent * 0.25);
         }

         if (hasActive(player, 2) && vars.MP < vars.Mana && player.tickCount % 20 == 0) {
            double base = Math.max(1.0, TemporaryStatBonusManager.effectiveIntelligence(player) / 20.0 * 2.0 + vars.manaregen);
            vars.MP = Math.min(vars.Mana, vars.MP + base * 0.1);
         }

         if (Double.compare(vars.MP, beforeMp) != 0) {
            vars.syncPlayerVariables(player);
         }

         player.getPersistentData().putDouble("sl_guild_last_mp", vars.MP);
      });
   }

   private static void applyFootworkSpeed(ServerPlayer player) {
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         AttributeModifier old = speed.getModifier(FOOTWORK_SPEED_ID);
         boolean desired = hasActive(player, 4) && player.getPersistentData().getLong("sl_guild_combat_until") > player.level().getGameTime();
         boolean matches = old != null && Double.compare(old.getAmount(), 0.12) == 0 && old.getOperation() == Operation.MULTIPLY_TOTAL;
         if (!desired) {
            if (old != null) {
               speed.removeModifier(FOOTWORK_SPEED_ID);
            }
         } else if (!matches) {
            if (old != null) {
               speed.removeModifier(FOOTWORK_SPEED_ID);
            }

            speed.addTransientModifier(new AttributeModifier(FOOTWORK_SPEED_ID, "Guild assassin footwork", 0.12, Operation.MULTIPLY_TOTAL));
         }
      }
   }

   private static void markCombat(ServerPlayer player, int ticks) {
      player.getPersistentData().putLong("sl_guild_combat_until", player.level().getGameTime() + ticks);
   }

   private static boolean isPrecisionHit(ServerPlayer player) {
      boolean vanillaCrit = player.fallDistance > 0.0F && !player.onGround() && !player.isInWater();
      return vanillaCrit || player.getRandom().nextFloat() < 0.05F;
   }

   private static boolean isMagicDamage(DamageSource source) {
      return source.is(DamageTypes.MAGIC)
         || source.is(DamageTypes.LIGHTNING_BOLT)
         || source.is(DamageTypes.IN_FIRE)
         || source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:mage")));
   }

   private static boolean isSkillDamage(DamageSource source) {
      return isMagicDamage(source)
         ? true
         : source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin")))
            || source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:fighter")))
            || source.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:ranger")))
            || source.getDirectEntity() instanceof ManaArrowEntity;
   }

   private static GuildData guildFor(Entity entity) {
      return entity instanceof ServerPlayer player ? GuildSavedData.get(player.serverLevel()).getGuildForPlayer(player.getUUID()) : null;
   }

   private static ServerPlayer owningPlayer(Entity source) {
      if (source instanceof ServerPlayer player) {
         return player;
      } else if (source instanceof TamableAnimal tame && tame.getOwner() instanceof ServerPlayer owner) {
         return owner;
      } else if (source instanceof Projectile projectile && projectile.getOwner() != null) {
         return owningPlayer(projectile.getOwner());
      } else {
         if (source != null) {
            UUID ownerId = ShadowMonarchManager.getShadowOwnerUUID(source);
            if (ownerId != null && source.getServer() != null) {
               return source.getServer().getPlayerList().getPlayer(ownerId);
            }
         }

         return null;
      }
   }
}
