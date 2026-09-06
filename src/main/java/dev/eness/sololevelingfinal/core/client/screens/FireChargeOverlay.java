package dev.eness.sololevelingfinal.core.client.screens;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent.Pre;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.procedures.FireCharg2Procedure;
import dev.eness.sololevelingfinal.core.procedures.FireCharge1Procedure;
import dev.eness.sololevelingfinal.core.procedures.FireCharge3Procedure;

@EventBusSubscriber(Dist.CLIENT)
public class FireChargeOverlay {
   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void eventHandler(Pre event) {
      int w = event.getWindow().getGuiScaledWidth();
      int h = event.getWindow().getGuiScaledHeight();
      Level world = null;
      double x = 0.0;
      double y = 0.0;
      double z = 0.0;
      Player entity = Minecraft.getInstance().player;
      if (entity != null) {
         world = entity.level();
         x = entity.getX();
         y = entity.getY();
         z = entity.getZ();
      }

      boolean firstCharge = FireCharge1Procedure.execute(entity);
      boolean secondCharge = FireCharg2Procedure.execute(entity);
      boolean thirdCharge = FireCharge3Procedure.execute(entity);
      if (firstCharge || secondCharge || thirdCharge) {
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         if (firstCharge) {
            event.getGuiGraphics()
               .blit(new ResourceLocation("sololeveling:textures/screens/firecharge1.png"), w / 2 + -16, h / 2 + -10, 0.0F, 0.0F, 32, 32, 32, 32);
         }

         if (firstCharge) {
            event.getGuiGraphics()
               .blit(new ResourceLocation("sololeveling:textures/screens/firecharge1.png"), w / 2 + -15, h / 2 + -10, 0.0F, 0.0F, 32, 32, 32, 32);
         }

         if (secondCharge) {
            event.getGuiGraphics()
               .blit(new ResourceLocation("sololeveling:textures/screens/firecharge2.png"), w / 2 + -16, h / 2 + -10, 0.0F, 0.0F, 32, 32, 32, 32);
         }

         if (secondCharge) {
            event.getGuiGraphics()
               .blit(new ResourceLocation("sololeveling:textures/screens/firecharge2.png"), w / 2 + -15, h / 2 + -10, 0.0F, 0.0F, 32, 32, 32, 32);
         }

         if (thirdCharge) {
            event.getGuiGraphics()
               .blit(new ResourceLocation("sololeveling:textures/screens/firecharge3.png"), w / 2 + -16, h / 2 + -10, 0.0F, 0.0F, 32, 32, 32, 32);
         }

         if (thirdCharge) {
            event.getGuiGraphics()
               .blit(new ResourceLocation("sololeveling:textures/screens/firecharge3.png"), w / 2 + -15, h / 2 + -10, 0.0F, 0.0F, 32, 32, 32, 32);
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
