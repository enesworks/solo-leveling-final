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
import dev.eness.sololevelingfinal.core.network.MiscItemsButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.GoldTextProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.MiscItemsMenu;

public class MiscItemsScreen extends AbstractContainerScreen<MiscItemsMenu> {
   private static final HashMap<String, Object> guistate = MiscItemsMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_device;
   ImageButton imagebutton_speciallootbox;
   ImageButton imagebutton_chooseboxicon;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/misc_items.png");

   public MiscItemsScreen(MiscItemsMenu container, Inventory inventory, Component text) {
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
      if (mouseX > this.leftPos + -59 && mouseX < this.leftPos + -35 && mouseY > this.topPos + -78 && mouseY < this.topPos + -54) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.misc_items.tooltip_class_changer"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -60 && mouseX < this.leftPos + -36 && mouseY > this.topPos + -29 && mouseY < this.topPos + -5) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.misc_items.tooltip_special_random_box"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -60 && mouseX < this.leftPos + -36 && mouseY > this.topPos + 19 && mouseY < this.topPos + 43) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.misc_items.tooltip_special_selection_box"), mouseX, mouseY);
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(new ResourceLocation("sololeveling:textures/screens/base.png"), this.leftPos + -77, this.topPos + -116, 0.0F, 0.0F, 150, 225, 150, 225);
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
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.misc_items.label_1000_golds"), -18, -73, -26317, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.misc_items.label_5000_golds"), -18, -24, -26317, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.misc_items.label_5000_golds1"), -18, 25, -26317, false);
      guiGraphics.drawString(this.font, GoldTextProcedure.execute(this.entity), -67, -106, -26317, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_device = new ImageButton(
         this.leftPos + -63,
         this.topPos + -82,
         32,
         32,
         0,
         0,
         32,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_device.png"),
         32,
         64,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new MiscItemsButtonMessage(0, this.x, this.y, this.z));
            MiscItemsButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_device", this.imagebutton_device);
      this.addRenderableWidget(this.imagebutton_device);
      this.imagebutton_speciallootbox = new ImageButton(
         this.leftPos + -60,
         this.topPos + -29,
         24,
         24,
         0,
         0,
         24,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_speciallootbox.png"),
         24,
         48,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new MiscItemsButtonMessage(1, this.x, this.y, this.z));
            MiscItemsButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_speciallootbox", this.imagebutton_speciallootbox);
      this.addRenderableWidget(this.imagebutton_speciallootbox);
      this.imagebutton_chooseboxicon = new ImageButton(
         this.leftPos + -60,
         this.topPos + 19,
         24,
         24,
         0,
         0,
         24,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_chooseboxicon.png"),
         24,
         48,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new MiscItemsButtonMessage(2, this.x, this.y, this.z));
            MiscItemsButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_chooseboxicon", this.imagebutton_chooseboxicon);
      this.addRenderableWidget(this.imagebutton_chooseboxicon);
   }
}
