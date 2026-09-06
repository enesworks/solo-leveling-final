package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.RewardPanelButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.RewardNameReturnProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.RewardPanelMenu;

public class RewardPanelScreen extends AbstractContainerScreen<RewardPanelMenu> {
   private static final HashMap<String, Object> guistate = RewardPanelMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_fullrecovery;
   ImageButton imagebutton_lootbox;
   ImageButton imagebutton_skillpoints10;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/reward_panel.png");

   public RewardPanelScreen(RewardPanelMenu container, Inventory inventory, Component text) {
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
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(guiGraphics);
      super.render(guiGraphics, mouseX, mouseY, partialTicks);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/panel_rework_vertical2.png"),
         this.leftPos + -99,
         this.topPos + -113,
         0.0F,
         0.0F,
         200,
         225,
         200,
         225
      );
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
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.reward_panel.label_ssfrewards"), -23, -68, -1, false);
      guiGraphics.drawString(this.font, RewardNameReturnProcedure.execute(this.entity), -43, 4, -1, false);
      guiGraphics.drawString(this.font, RewardNameReturnProcedure.execute(this.entity, 2), -43, 30, -1, false);
      guiGraphics.drawString(this.font, RewardNameReturnProcedure.execute(this.entity, 3), -43, 56, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_fullrecovery = new ImageButton(
         this.leftPos + -48,
         this.topPos + 24,
         96,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_fullrecovery.png"),
         96,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new RewardPanelButtonMessage(0, this.x, this.y, this.z));
            RewardPanelButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_fullrecovery", this.imagebutton_fullrecovery);
      this.addRenderableWidget(this.imagebutton_fullrecovery);
      this.imagebutton_lootbox = new ImageButton(
         this.leftPos + -48,
         this.topPos + -2,
         96,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_lootbox.png"),
         96,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new RewardPanelButtonMessage(1, this.x, this.y, this.z));
            RewardPanelButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_lootbox", this.imagebutton_lootbox);
      this.addRenderableWidget(this.imagebutton_lootbox);
      this.imagebutton_skillpoints10 = new ImageButton(
         this.leftPos + -48,
         this.topPos + 50,
         96,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_skillpoints10.png"),
         96,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new RewardPanelButtonMessage(2, this.x, this.y, this.z));
            RewardPanelButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_skillpoints10", this.imagebutton_skillpoints10);
      this.addRenderableWidget(this.imagebutton_skillpoints10);
   }
}
