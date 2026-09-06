package dev.eness.sololevelingfinal.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.GoliathCombatMessage;
import dev.eness.sololevelingfinal.core.util.GoliathCombatManager;

@EventBusSubscriber(Dist.CLIENT)
public final class GoliathCombatClientEvents {
   private GoliathCombatClientEvents() {
   }

   @SubscribeEvent
   public static void onInteractionInput(InteractionKeyMappingTriggered event) {
      if (event.isAttack()) {
         Minecraft minecraft = Minecraft.getInstance();
         Player player = minecraft.player;
         if (player != null && minecraft.screen == null && GoliathCombatManager.isCombatStance(player)) {
            event.setCanceled(true);
            event.setSwingHand(false);
            SololevelingMod.PACKET_HANDLER.sendToServer(new GoliathCombatMessage());
         }
      }
   }
}
