package dev.eness.sololevelingfinal.core;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.init.SololevelingModKeyMappings;
import dev.eness.sololevelingfinal.core.network.Ability2Message;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.RulersAuthorityManager;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public class ScrollDisable {
   @SubscribeEvent
   public static void Scroll(MouseScrollingEvent event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.screen == null && SololevelingModKeyMappings.ABILITY_2.isDown()) {
         SololevelingModVariables.PlayerVariables variables = minecraft.player
            .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (variables.combatmode && RulersAuthorityManager.hasAbility(minecraft.player)) {
            int direction = event.getScrollDelta() > 0.0 ? 1 : -1;
            SololevelingMod.PACKET_HANDLER.sendToServer(new Ability2Message(2, direction));
            event.setCanceled(true);
         }
      }
   }
}
