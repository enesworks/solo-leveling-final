package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.procedures.ReturnIDClassProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnIDPersonProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnIDRankProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.HunterIDGuiMenu;

public class HunterIDGuiScreen extends AbstractContainerScreen<HunterIDGuiMenu> {
   private static final HashMap<String, Object> guistate = HunterIDGuiMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/hunter_id_gui.png");

   public HunterIDGuiScreen(HunterIDGuiMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.world = container.world;
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(guiGraphics);
      super.render(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
      if (mouseX > this.leftPos + 41 && mouseX < this.leftPos + 65 && mouseY > this.topPos + 25 && mouseY < this.topPos + 42) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.hunter_id_gui.tooltip_certified_hunter"), mouseX, mouseY);
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(new ResourceLocation("sololeveling:textures/screens/idcard1.png"), this.leftPos + -88, this.topPos + -55, 0.0F, 0.0F, 176, 110, 176, 110);
      RenderSystem.disableBlend();
   }

   @Override
   public boolean keyPressed(int key, int b, int c) {
      if (key == 256) {
         this.minecraft.player.closeContainer();
         return true;
      } else {
         return super.keyPressed(key, b, c);
      }
   }

   @Override
   public void containerTick() {
      super.containerTick();
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.hunter_id_gui.label_hunters_association"), -55, -40, -13421569, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.hunter_id_gui.label_hunter_id_card"), -46, -31, -13421569, false);
      guiGraphics.drawString(this.font, ReturnIDPersonProcedure.execute(this.entity), -76, 0, -12829636, false);
      guiGraphics.drawString(this.font, ReturnIDRankProcedure.execute(this.entity), -76, 16, -12829636, false);
      guiGraphics.drawString(this.font, ReturnIDClassProcedure.execute(this.entity), -76, 33, -12829636, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
   }
}
