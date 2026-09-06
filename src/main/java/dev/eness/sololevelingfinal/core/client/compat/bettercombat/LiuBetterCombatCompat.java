package dev.eness.sololevelingfinal.core.client.compat.bettercombat;

import net.bettercombat.api.client.BetterCombatClientEvents;
import net.bettercombat.api.client.BetterCombatClientEvents.PlayerAttackStart;
import dev.eness.sololevelingfinal.core.client.LiuCombatClientEvents;

public final class LiuBetterCombatCompat {
   private static boolean registered;

   private LiuBetterCombatCompat() {
   }

   public static void register() {
      if (!registered) {
         registered = true;
         BetterCombatClientEvents.ATTACK_START
            .register((PlayerAttackStart)(player, hand) -> LiuCombatClientEvents.sendEnhancedAttack(hand.isOffHand(), hand.combo().current()));
      }
   }
}
