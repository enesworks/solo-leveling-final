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
import dev.eness.sololevelingfinal.core.network.PanelReworkButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.FatigueTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.HealthTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.IntelligenceTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.LevelTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.ManaTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnNameAndGuildProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnRemainingXPProcedure;
import dev.eness.sololevelingfinal.core.procedures.SenseTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.SkillPointsTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.StrengthTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.TitleTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.VitalityTextProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.PanelReworkMenu;

public class PanelReworkScreen extends AbstractContainerScreen<PanelReworkMenu> {
   private static final HashMap<String, Object> guistate = PanelReworkMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_normal_quest;
   ImageButton imagebutton_normal_reward;
   ImageButton imagebutton_normal_store;
   ImageButton imagebutton_normal_abilities;
   ImageButton imagebutton_normal_training;
   ImageButton imagebutton_normal_craft;
   ImageButton imagebutton_invest2;
   ImageButton imagebutton_invest21;
   ImageButton imagebutton_invest22;
   ImageButton imagebutton_invest23;
   ImageButton imagebutton_invest24;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/panel_rework.png");

   public PanelReworkScreen(PanelReworkMenu container, Inventory inventory, Component text) {
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
      if (mouseX > this.leftPos + 27 && mouseX < this.leftPos + 51 && mouseY > this.topPos + -52 && mouseY < this.topPos + -28) {
         guiGraphics.renderTooltip(this.font, Component.literal(ReturnRemainingXPProcedure.execute(this.entity)), mouseX, mouseY);
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/panel_rework_6.png"), this.leftPos + -113, this.topPos + -105, 0.0F, 0.0F, 225, 190, 225, 190
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
      guiGraphics.drawString(this.font, ReturnNameAndGuildProcedure.execute(this.entity), -56, -54, -1, false);
      guiGraphics.drawString(this.font, TitleTextProcedure.execute(this.entity), -56, -35, -1, false);
      guiGraphics.drawString(this.font, SkillPointsTextProcedure.execute(this.entity), 26, 33, -1, false);
      guiGraphics.drawString(this.font, IntelligenceTextProcedure.execute(this.entity), 43, 18, -1, false);
      guiGraphics.drawString(this.font, StrengthTextProcedure.execute(this.entity), -22, 2, -1, false);
      guiGraphics.drawString(this.font, VitalityTextProcedure.execute(this.entity), 43, 2, -1, false);
      guiGraphics.drawString(this.font, SpeedTextProcedure.execute(this.entity), -22, 19, -1, false);
      guiGraphics.drawString(this.font, SenseTextProcedure.execute(this.entity), -22, 34, -1, false);
      guiGraphics.drawString(this.font, LevelTextProcedure.execute(this.entity), 52, -44, -1, false);
      guiGraphics.drawString(this.font, FatigueTextProcedure.execute(this.entity), -5, -18, -1, false);
      guiGraphics.drawString(this.font, HealthTextProcedure.execute(this.entity), -61, -18, -39322, false);
      guiGraphics.drawString(this.font, ManaTextProcedure.execute(this.entity), 32, -18, -10040065, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_normal_quest = new ImageButton(
         this.leftPos + -144,
         this.topPos + 15,
         32,
         34,
         0,
         0,
         34,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_normal_quest.png"),
         32,
         68,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(0, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_normal_quest", this.imagebutton_normal_quest);
      this.addRenderableWidget(this.imagebutton_normal_quest);
      this.imagebutton_normal_reward = new ImageButton(
         this.leftPos + -144,
         this.topPos + -25,
         32,
         34,
         0,
         0,
         34,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_normal_reward.png"),
         32,
         68,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(1, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_normal_reward", this.imagebutton_normal_reward);
      this.addRenderableWidget(this.imagebutton_normal_reward);
      this.imagebutton_normal_store = new ImageButton(
         this.leftPos + -144,
         this.topPos + -66,
         32,
         34,
         0,
         0,
         34,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_normal_store.png"),
         32,
         68,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(2, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_normal_store", this.imagebutton_normal_store);
      this.addRenderableWidget(this.imagebutton_normal_store);
      this.imagebutton_normal_abilities = new ImageButton(
         this.leftPos + 111,
         this.topPos + -66,
         32,
         34,
         0,
         0,
         34,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_normal_abilities.png"),
         32,
         68,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(3, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 3, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_normal_abilities", this.imagebutton_normal_abilities);
      this.addRenderableWidget(this.imagebutton_normal_abilities);
      this.imagebutton_normal_training = new ImageButton(
         this.leftPos + 112,
         this.topPos + -26,
         32,
         34,
         0,
         0,
         34,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_normal_training.png"),
         32,
         68,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(4, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 4, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_normal_training", this.imagebutton_normal_training);
      this.addRenderableWidget(this.imagebutton_normal_training);
      this.imagebutton_normal_craft = new ImageButton(
         this.leftPos + 111,
         this.topPos + 14,
         32,
         34,
         0,
         0,
         34,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_normal_craft.png"),
         32,
         68,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(5, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 5, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_normal_craft", this.imagebutton_normal_craft);
      this.addRenderableWidget(this.imagebutton_normal_craft);
      this.imagebutton_invest2 = new ImageButton(
         this.leftPos + -87, this.topPos + 2, 7, 7, 0, 0, 7, new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest2.png"), 7, 14, e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(6, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 6, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest2", this.imagebutton_invest2);
      this.addRenderableWidget(this.imagebutton_invest2);
      this.imagebutton_invest21 = new ImageButton(
         this.leftPos + -87,
         this.topPos + 18,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest21.png"),
         7,
         14,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(7, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 7, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest21", this.imagebutton_invest21);
      this.addRenderableWidget(this.imagebutton_invest21);
      this.imagebutton_invest22 = new ImageButton(
         this.leftPos + -87,
         this.topPos + 34,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest22.png"),
         7,
         14,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(8, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 8, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest22", this.imagebutton_invest22);
      this.addRenderableWidget(this.imagebutton_invest22);
      this.imagebutton_invest23 = new ImageButton(
         this.leftPos + 79, this.topPos + 2, 7, 7, 0, 0, 7, new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest23.png"), 7, 14, e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(9, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 9, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest23", this.imagebutton_invest23);
      this.addRenderableWidget(this.imagebutton_invest23);
      this.imagebutton_invest24 = new ImageButton(
         this.leftPos + 79,
         this.topPos + 18,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest24.png"),
         7,
         14,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelReworkButtonMessage(10, this.x, this.y, this.z));
            PanelReworkButtonMessage.handleButtonAction(this.entity, 10, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest24", this.imagebutton_invest24);
      this.addRenderableWidget(this.imagebutton_invest24);
   }
}
