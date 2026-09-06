package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.network.QuestsButtonMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;
import dev.eness.sololevelingfinal.core.world.inventory.QuestsMenu;

public class QuestsScreen extends AbstractContainerScreen<QuestsMenu> {
   private static final HashMap<String, Object> guistate = QuestsMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_panel_rework_quests_daily;
   ImageButton imagebutton_panel_rework_quests_path;
   Button jobChangeButton;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/quests.png");

   public QuestsScreen(QuestsMenu container, Inventory inventory, Component text) {
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
      if (mouseX > this.leftPos + -19 && mouseX < this.leftPos + 5 && mouseY > this.topPos + -43 && mouseY < this.topPos + -19) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.quests.tooltip_daily_quests"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -7 && mouseX < this.leftPos + 17 && mouseY > this.topPos + -43 && mouseY < this.topPos + -19) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.quests.tooltip_daily_quests1"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -19 && mouseX < this.leftPos + 5 && mouseY > this.topPos + -31 && mouseY < this.topPos + -7) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.quests.tooltip_daily_quests2"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -7 && mouseX < this.leftPos + 17 && mouseY > this.topPos + -31 && mouseY < this.topPos + -7) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.quests.tooltip_daily_quests3"), mouseX, mouseY);
      }

      if (this.imagebutton_panel_rework_quests_path != null
         && this.imagebutton_panel_rework_quests_path.visible
         && mouseX > this.leftPos + -19
         && mouseX < this.leftPos + 17
         && mouseY > this.topPos + 2
         && mouseY < this.topPos + 38) {
         guiGraphics.renderTooltip(this.font, this.dkcButtonTooltip(), mouseX, mouseY);
      }

      if (this.jobChangeButton != null && this.jobChangeButton.visible && this.jobChangeButton.isHovered()) {
         guiGraphics.renderTooltip(this.font, Component.literal("Enter the Job Change Quest"), mouseX, mouseY);
      }
   }

   private Component dkcButtonTooltip() {
      if (this.entity != null && DkcFloorRegistry.isDkc(this.entity.level())) {
         return Component.literal("Open the Castle Tower");
      }

      if (DkcQuestManager.hasRadiruCastleAccess(this.entity)) {
         return Component.literal("Travel to Radiru Castle");
      }

      double cleared = this.entity == null
         ? 0.0
         : this.entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).dkc_cleared;
      return cleared >= 20.0 ? Component.literal("Demon King's Castle conquered") : Component.literal("Demon King's Castle Path");
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/panel_rework_empty_small.png"),
         this.leftPos + -101,
         this.topPos + -85,
         0.0F,
         0.0F,
         200,
         160,
         200,
         160
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
      this.updateJobChangeButton();
      this.updateDkcButton();
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.quests.label_sslquests_tab"), -29, -59, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_panel_rework_quests_daily = new ImageButton(
         this.leftPos + -19,
         this.topPos + -43,
         36,
         36,
         0,
         0,
         36,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_panel_rework_quests_daily.png"),
         36,
         72,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new QuestsButtonMessage(0, this.x, this.y, this.z));
            QuestsButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_panel_rework_quests_daily", this.imagebutton_panel_rework_quests_daily);
      this.addRenderableWidget(this.imagebutton_panel_rework_quests_daily);
      this.imagebutton_panel_rework_quests_path = new ImageButton(
         this.leftPos + -19,
         this.topPos + 2,
         36,
         36,
         0,
         0,
         36,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_panel_rework_quests_path.png"),
         36,
         72,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new QuestsButtonMessage(1, this.x, this.y, this.z));
            QuestsButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_panel_rework_quests_path", this.imagebutton_panel_rework_quests_path);
      this.addRenderableWidget(this.imagebutton_panel_rework_quests_path);
      this.updateDkcButton();
      this.jobChangeButton = Button.builder(Component.literal("Job Change"), e -> {
         SololevelingMod.PACKET_HANDLER.sendToServer(new QuestsButtonMessage(2, this.x, this.y, this.z));
         QuestsButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
      }).bounds(this.leftPos + -47, this.topPos + 47, 94, 20).build();
      guistate.put("button:job_change_quest", this.jobChangeButton);
      this.addRenderableWidget(this.jobChangeButton);
      this.updateJobChangeButton();
   }

   private void updateJobChangeButton() {
      if (this.jobChangeButton != null) {
         boolean show = JobChangeQuestManager.isVisible(this.entity);
         this.jobChangeButton.visible = show;
         this.jobChangeButton.active = show;
      }
   }

   private void updateDkcButton() {
      if (this.imagebutton_panel_rework_quests_path != null) {
         boolean show = DkcQuestManager.isVisible(this.entity);
         this.imagebutton_panel_rework_quests_path.visible = show;
         this.imagebutton_panel_rework_quests_path.active = show;
      }
   }
}
