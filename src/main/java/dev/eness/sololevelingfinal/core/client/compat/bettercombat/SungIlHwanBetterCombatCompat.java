package dev.eness.sololevelingfinal.core.client.compat.bettercombat;

import net.bettercombat.api.client.BetterCombatClientEvents;
import net.bettercombat.api.client.BetterCombatClientEvents.PlayerAttackHit;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SungIlHwanAttackMessage;
import dev.eness.sololevelingfinal.core.util.SungIlHwanCombatManager;

public final class SungIlHwanBetterCombatCompat {
   private static boolean registered;

   private SungIlHwanBetterCombatCompat() {
   }

   public static void register() {
      if (!registered) {
         registered = true;
         BetterCombatClientEvents.ATTACK_HIT.register((PlayerAttackHit)(player, hand, targets, cursorTarget) -> {
            if (SungIlHwanCombatManager.shouldReplaceBasicAttack(player)) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new SungIlHwanAttackMessage());
            }
         });
      }
   }
}
