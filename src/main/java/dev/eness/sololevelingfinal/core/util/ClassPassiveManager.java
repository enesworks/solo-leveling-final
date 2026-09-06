package dev.eness.sololevelingfinal.core.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.ClassPassiveMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public final class ClassPassiveManager {
   private static final String F_POWER = "sl_f_power";
   private static final String F_BURST_UNTIL = "sl_f_burst_until";
   private static final String H_STACKS = "sl_h_stacks";
   private static final String R_FOCUS = "sl_r_focus";
   private static final String R_LAST_DECAY = "sl_r_last_decay";
   private static final int HEALER_RES_MAX = 5;

   private ClassPassiveManager() {
   }

   public static void resetPlayerState(ServerPlayer player) {
      if (player != null) {
         CompoundTag data = player.getPersistentData();
         long now = player.level().getGameTime();
         long burstUntil = data.getLong("sl_f_burst_until");
         if (burstUntil >= now) {
            int expectedRemaining = (int)Math.max(0L, burstUntil - now);
            removeOwnedFighterBuff(player, MobEffects.MOVEMENT_SPEED, expectedRemaining);
            removeOwnedFighterBuff(player, MobEffects.DAMAGE_BOOST, expectedRemaining);
         }

         data.remove("sl_f_power");
         data.remove("sl_f_burst_until");
         data.remove("sl_h_stacks");
         data.remove("sl_r_focus");
         data.remove("sl_r_last_decay");
         sync(player, 1, 0.0);
         sync(player, 3, 0.0);
         sync(player, 4, 0.0);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && playerClass(player) == 6) {
         sync(player, 4, getRangerFocus(player));
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && playerClass(player) == 6) {
         sync(player, 4, getRangerFocus(player));
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onLivingHurt(LivingHurtEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
         int cls = playerClass(attacker);
         if (cls == 3) {
            processFighterHit(attacker, event.getAmount());
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.player instanceof ServerPlayer sp) {
            tickOwnedFighterBurst(sp);
            if (playerClass(sp) == 6) {
               tickRanger(sp);
            }
         }
      }
   }

   private static void tickOwnedFighterBurst(ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      long burstUntil = data.getLong("sl_f_burst_until");
      if (burstUntil > 0L) {
         long now = player.level().getGameTime();
         if (burstUntil < now) {
            data.remove("sl_f_burst_until");
         } else if (playerClass(player) != 3) {
            int expectedRemaining = (int)Math.max(0L, burstUntil - now);
            removeOwnedFighterBuff(player, MobEffects.MOVEMENT_SPEED, expectedRemaining);
            removeOwnedFighterBuff(player, MobEffects.DAMAGE_BOOST, expectedRemaining);
            data.remove("sl_f_burst_until");
         }
      }
   }

   private static void processFighterHit(ServerPlayer p, float dmg) {
      CompoundTag d = p.getPersistentData();
      double power = Math.min(d.getDouble("sl_f_power") + dmg * 4.0, 100.0);
      d.putDouble("sl_f_power", power);
      sync(p, 1, power);
      if (power >= 100.0) {
         d.putDouble("sl_f_power", 0.0);
         d.putLong("sl_f_burst_until", p.level().getGameTime() + 120L);
         sync(p, 1, 0.0);
         p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 1, false, true));
         p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1, false, true));
      }
   }

   public static void onHealerCast(ServerPlayer p) {
      if (p != null && playerClass(p) == 5) {
         CompoundTag d = p.getPersistentData();
         int stacks = Math.min(d.getInt("sl_h_stacks") + 1, 5);
         d.putInt("sl_h_stacks", stacks);
         sync(p, 3, stacks);
         if (stacks >= 5) {
            SololevelingMod.queueServerWork(1, () -> triggerResonanceBurst(p));
            d.putInt("sl_h_stacks", 0);
            sync(p, 3, 0.0);
         }
      }
   }

   private static void triggerResonanceBurst(ServerPlayer healer) {
      if (healer.isAlive() && playerClass(healer) == 5) {
         healer.level()
            .getEntitiesOfClass(Player.class, healer.getBoundingBox().inflate(8.0), ally -> ally != healer && !ally.isDeadOrDying())
            .forEach(ally -> ally.heal(6.0F));
         healer.heal(4.0F);
      }
   }

   private static void tickRanger(ServerPlayer p) {
      CompoundTag data = p.getPersistentData();
      double focus = data.getDouble("sl_r_focus");
      if (!(focus <= 0.0) && !(focus >= 100.0)) {
         long now = p.level().getGameTime();
         if (now - data.getLong("sl_r_last_decay") >= 10L) {
            data.putLong("sl_r_last_decay", now);
            double decay = p.isSprinting() ? 2.0 : (p.isUsingItem() ? 0.0 : 0.5);
            if (decay > 0.0) {
               addRangerFocus(p, -decay);
            }
         }
      }
   }

   public static boolean consumeRangerFocus(ServerPlayer p) {
      CompoundTag d = p.getPersistentData();
      if (d.getDouble("sl_r_focus") < 100.0) {
         return false;
      }

      d.putDouble("sl_r_focus", 0.0);
      sync(p, 4, 0.0);
      return true;
   }

   public static double getRangerFocus(ServerPlayer p) {
      return p == null ? 0.0 : Mth.clamp(p.getPersistentData().getDouble("sl_r_focus"), 0.0, 100.0);
   }

   public static void syncRangerFocus(ServerPlayer p) {
      if (p != null && playerClass(p) == 6) {
         sync(p, 4, getRangerFocus(p));
      }
   }

   public static void addRangerFocus(ServerPlayer p, double amount) {
      if (p != null && playerClass(p) == 6 && !(Math.abs(amount) < 1.0E-4)) {
         CompoundTag data = p.getPersistentData();
         double oldFocus = Mth.clamp(data.getDouble("sl_r_focus"), 0.0, 100.0);
         double newFocus = Mth.clamp(oldFocus + amount, 0.0, 100.0);
         if (!(Math.abs(newFocus - oldFocus) < 1.0E-4)) {
            data.putDouble("sl_r_focus", newFocus);
            if ((int)(newFocus / 2.0) != (int)(oldFocus / 2.0) || newFocus == 0.0 || newFocus == 100.0) {
               sync(p, 4, newFocus);
            }
         }
      }
   }

   private static int playerClass(ServerPlayer p) {
      return (int)p.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes;
   }

   private static void removeOwnedFighterBuff(ServerPlayer player, MobEffect effect, int expectedRemaining) {
      MobEffectInstance active = player.getEffect(effect);
      if (active != null && active.getAmplifier() == 1 && active.getDuration() <= expectedRemaining + 2) {
         player.removeEffect(effect);
      }
   }

   private static void sync(ServerPlayer p, int type, double value) {
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> p), new ClassPassiveMessage(type, value));
   }
}
