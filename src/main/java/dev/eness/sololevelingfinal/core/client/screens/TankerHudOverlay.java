package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public final class TankerHudOverlay {
   private static final int PANEL_X = 8;
   private static final int NORMAL_PANEL_Y = 76;
   private static final int CREATIVE_PANEL_Y = 8;

   private TankerHudOverlay() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onRenderGui(Pre event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && !minecraft.options.hideGui && !minecraft.options.renderDebug && minecraft.screen == null) {
         SololevelingModVariables.PlayerVariables vars = minecraft.player
            .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (vars.CustomHUD && (int)Math.round(vars.Classes) == 4) {
            GameType gameType = gameType(minecraft);
            if (gameType != GameType.SPECTATOR) {
               int panelY = gameType == GameType.CREATIVE ? 8 : 76;
               GuiGraphics graphics = event.getGuiGraphics();
               graphics.pose().pushPose();
               graphics.pose().translate(0.0F, 0.0F, 220.0F);
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableDepthTest();
               graphics.fill(13, panelY + 3, 83, panelY + 14, -922219244);
               graphics.drawString(minecraft.font, Component.translatable("gui.sololeveling.tanker.iron_wall"), 15, panelY + 4, -5908225, false);
               RenderSystem.enableDepthTest();
               RenderSystem.disableBlend();
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               graphics.pose().popPose();
            }
         }
      }
   }

   private static GameType gameType(Minecraft minecraft) {
      if (minecraft.player != null && minecraft.getConnection() != null) {
         PlayerInfo info = minecraft.getConnection().getPlayerInfo(minecraft.player.getGameProfile().getId());
         return info == null ? GameType.SURVIVAL : info.getGameMode();
      } else {
         return GameType.SURVIVAL;
      }
   }
}
