package dev.eness.sololevelingfinal.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.LiuAttackMessage;
import dev.eness.sololevelingfinal.core.network.LiuChargeMessage;
import dev.eness.sololevelingfinal.core.util.LiuZhigangCombatManager;

@EventBusSubscriber(Dist.CLIENT)
public final class LiuCombatClientEvents {
   private static boolean charging;
   private static long chargeStartedAt;
   private static int vanillaCombo;

   private LiuCombatClientEvents() {
   }

   @SubscribeEvent
   public static void onInteractionInput(InteractionKeyMappingTriggered event) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      if (player != null && minecraft.screen == null && LiuZhigangCombatManager.isCombatStance(player)) {
         if (event.isAttack()) {
            if (!ModList.get().isLoaded("bettercombat")) {
               sendVanillaEnhancedAttack(player);
            }
         } else {
            if (event.isUseItem()
               && (LiuZhigangCombatManager.isMeleeWeapon(player.getMainHandItem()) || LiuZhigangCombatManager.isMeleeWeapon(player.getOffhandItem()))) {
               event.setCanceled(true);
               event.setSwingHand(false);
               if (!charging) {
                  charging = true;
                  chargeStartedAt = player.level().getGameTime();
                  SololevelingMod.PACKET_HANDLER.sendToServer(new LiuChargeMessage(0));
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END && charging) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         boolean valid = player != null
            && minecraft.screen == null
            && LiuZhigangCombatManager.isCombatStance(player)
            && (LiuZhigangCombatManager.isMeleeWeapon(player.getMainHandItem()) || LiuZhigangCombatManager.isMeleeWeapon(player.getOffhandItem()));
         if (!valid || !minecraft.options.keyUse.isDown()) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new LiuChargeMessage(valid ? 1 : 2));
            charging = false;
         }
      }
   }

   public static void sendEnhancedAttack(boolean offhand, int comboIndex) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.screen == null && LiuZhigangCombatManager.isCombatStance(minecraft.player)) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new LiuAttackMessage(offhand, comboIndex));
      }
   }

   private static void sendVanillaEnhancedAttack(LocalPlayer player) {
      boolean mainhand = LiuZhigangCombatManager.isMeleeWeapon(player.getMainHandItem());
      boolean offhand = LiuZhigangCombatManager.isMeleeWeapon(player.getOffhandItem());
      if (mainhand || offhand) {
         if (mainhand && offhand) {
            int step = Math.floorMod(vanillaCombo++, 3);
            sendEnhancedAttack(step == 1, step == 2 ? 3 : step);
         } else {
            sendEnhancedAttack(offhand, vanillaCombo++);
         }
      }
   }

   public static boolean isCharging() {
      return charging;
   }

   public static long getChargeTicks(float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.level == null ? 0L : Math.max(0L, minecraft.level.getGameTime() - chargeStartedAt);
   }
}
