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
import dev.eness.sololevelingfinal.core.network.TrainingGUIButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.TrainingGUIMenu;

public class TrainingGUIScreen extends AbstractContainerScreen<TrainingGUIMenu> {
   private static final HashMap<String, Object> guistate = TrainingGUIMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_back;
   ImageButton imagebutton_panel_rework_rewardbutton;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/training_gui.png");

   public TrainingGUIScreen(TrainingGUIMenu container, Inventory inventory, Component text) {
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
      if (mouseX > this.leftPos + -128 && mouseX < this.leftPos + -104 && mouseY > this.topPos + -15 && mouseY < this.topPos + 9) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.training_gui.tooltip_back_to_main_panel"), mouseX, mouseY);
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/panel_rework_vertical2.png"),
         this.leftPos + -104,
         this.topPos + -107,
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
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.training_gui.label_spawn_a_training_bot"), -59, -66, -11962198, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.training_gui.label_to_test_your_skills_on"), -57, -55, -11962198, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.training_gui.label_and_also_level_up"), -54, -44, -11962198, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.training_gui.label_bots_strength_will_be"), -55, 5, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.training_gui.label_be_proportional_to"), -52, 17, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.training_gui.label_level_of_the_player"), -55, 30, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.training_gui.label_sslaccept"), -29, 55, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_back = new ImageButton(
         this.leftPos + -126,
         this.topPos + -13,
         20,
         20,
         0,
         0,
         20,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_back.png"),
         20,
         40,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new TrainingGUIButtonMessage(0, this.x, this.y, this.z));
            TrainingGUIButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_back", this.imagebutton_back);
      this.addRenderableWidget(this.imagebutton_back);
      this.imagebutton_panel_rework_rewardbutton = new ImageButton(
         this.leftPos + -41,
         this.topPos + 48,
         64,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_panel_rework_rewardbutton.png"),
         64,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new TrainingGUIButtonMessage(1, this.x, this.y, this.z));
            TrainingGUIButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_panel_rework_rewardbutton", this.imagebutton_panel_rework_rewardbutton);
      this.addRenderableWidget(this.imagebutton_panel_rework_rewardbutton);
   }
}
