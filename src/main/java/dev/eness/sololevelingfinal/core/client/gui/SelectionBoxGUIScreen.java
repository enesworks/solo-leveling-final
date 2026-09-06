package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SelectionBoxGUIButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.SelectionBoxGUIMenu;

public class SelectionBoxGUIScreen extends AbstractContainerScreen<SelectionBoxGUIMenu> {
   private static final HashMap<String, Object> guistate = SelectionBoxGUIMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   Button button_empty;
   Button button_empty1;
   Button button_empty2;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/selection_box_gui.png");

   public SelectionBoxGUIScreen(SelectionBoxGUIMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.world = container.world;
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 96;
      this.imageHeight = 36;
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
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.selection_box_gui.label_get_whole_set"), 11, -13, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.button_empty = new PlainTextButton(
         this.leftPos + 5, this.topPos + 8, 25, 20, Component.translatable("gui.sololeveling.selection_box_gui.button_empty"), e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new SelectionBoxGUIButtonMessage(0, this.x, this.y, this.z));
            SelectionBoxGUIButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }, this.font
      );
      guistate.put("button:button_empty", this.button_empty);
      this.addRenderableWidget(this.button_empty);
      this.button_empty1 = new PlainTextButton(
         this.leftPos + 35, this.topPos + 8, 25, 20, Component.translatable("gui.sololeveling.selection_box_gui.button_empty1"), e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new SelectionBoxGUIButtonMessage(1, this.x, this.y, this.z));
            SelectionBoxGUIButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }, this.font
      );
      guistate.put("button:button_empty1", this.button_empty1);
      this.addRenderableWidget(this.button_empty1);
      this.button_empty2 = new PlainTextButton(
         this.leftPos + 66, this.topPos + 8, 25, 20, Component.translatable("gui.sololeveling.selection_box_gui.button_empty2"), e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new SelectionBoxGUIButtonMessage(2, this.x, this.y, this.z));
            SelectionBoxGUIButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }, this.font
      );
      guistate.put("button:button_empty2", this.button_empty2);
      this.addRenderableWidget(this.button_empty2);
   }
}
