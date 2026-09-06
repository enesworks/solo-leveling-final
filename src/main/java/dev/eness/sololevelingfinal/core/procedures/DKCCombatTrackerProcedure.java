package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DKCCombatTrackerProcedure {
   private static final String LAST_COMBAT_TICK = "solocraft_dkc_last_combat_tick";
   private static final long OUT_OF_COMBAT_TICKS = 400L;

   @SubscribeEvent
   public static void onEntityHurt(LivingHurtEvent event) {
      if (event != null && event.getEntity() != null) {
         if (!event.getEntity().getPersistentData().getBoolean("radiru_training_dummy")) {
            if (event.getEntity() instanceof ServerPlayer player) {
               markInCombat(player);
            }

            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof ServerPlayer player) {
               markInCombat(player);
            } else if (attacker instanceof TamableAnimal tamable && tamable.getOwner() instanceof ServerPlayer owner) {
               markInCombat(owner);
            }
         }
      }
   }

   public static void markInCombat(ServerPlayer player) {
      player.getPersistentData().putLong("solocraft_dkc_last_combat_tick", player.serverLevel().getGameTime());
   }

   public static boolean canEnterCastle(ServerPlayer player) {
      long lastCombatTick = player.getPersistentData().getLong("solocraft_dkc_last_combat_tick");
      return lastCombatTick <= 0L || player.serverLevel().getGameTime() - lastCombatTick >= 400L;
   }

   public static void sendCombatBlockedMessage(ServerPlayer player) {
      long lastCombatTick = player.getPersistentData().getLong("solocraft_dkc_last_combat_tick");
      long remainingTicks = Math.max(0L, 400L - (player.serverLevel().getGameTime() - lastCombatTick));
      long remainingSeconds = Math.max(1L, (remainingTicks + 19L) / 20L);
      player.displayClientMessage(Component.literal("§4The castle rejects cowards in flight. §cWait " + remainingSeconds + "s out of combat."), true);
   }
}
