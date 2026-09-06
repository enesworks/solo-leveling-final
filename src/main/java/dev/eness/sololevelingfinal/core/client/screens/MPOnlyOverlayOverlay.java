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
import dev.eness.sololevelingfinal.core.procedures.MPOnlyOverlayDisplayOverlayIngameProcedure;
import dev.eness.sololevelingfinal.core.procedures.Mana0Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana100Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana10Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana20Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana30Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana40Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana50Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana60Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana70Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana80Procedure;
import dev.eness.sololevelingfinal.core.procedures.Mana90Procedure;
import dev.eness.sololevelingfinal.core.procedures.ManaTextProcedure;

@EventBusSubscriber(Dist.CLIENT)
public class MPOnlyOverlayOverlay {
   @SubscribeEvent(priority = EventPriority.NORMAL)
   public static void eventHandler(Pre event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!minecraft.options.renderDebug) {
         int w = event.getWindow().getGuiScaledWidth();
         int h = event.getWindow().getGuiScaledHeight();
         Level world = null;
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         Player entity = minecraft.player;
         if (entity != null) {
            world = entity.level();
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
         }

         boolean visible = MPOnlyOverlayDisplayOverlayIngameProcedure.execute(entity);
         if (visible) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (visible) {
               if (Mana0Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/bar1.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana10Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana10.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana20Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana20.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana30Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana30.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana40Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana40.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana50Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana50.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana60Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana60.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana70Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana70.png"), 7, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana80Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana80.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana90Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana90.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               if (Mana100Procedure.execute(entity)) {
                  event.getGuiGraphics().blit(new ResourceLocation("sololeveling:textures/screens/barmana100.png"), 6, 6, 0.0F, 0.0F, 90, 10, 90, 10);
               }

               event.getGuiGraphics().drawString(Minecraft.getInstance().font, ManaTextProcedure.execute(entity), 16, 7, -1, false);
            }

            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         }
      }
   }
}
