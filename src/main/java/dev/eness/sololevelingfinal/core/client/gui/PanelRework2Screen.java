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
import dev.eness.sololevelingfinal.core.network.PanelRework2ButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.ReturnAgilityProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnFatigueProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnHPProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnIntelligenceProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnJobProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnLevelProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnMPProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnNameProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnPerceptionProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnRemainingXPProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnSPProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnStrengthProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnTitleProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnVitalityProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.PanelRework2Menu;

public class PanelRework2Screen extends AbstractContainerScreen<PanelRework2Menu> {
   private static final HashMap<String, Object> guistate = PanelRework2Menu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_invest2;
   ImageButton imagebutton_invest21;
   ImageButton imagebutton_invest22;
   ImageButton imagebutton_invest23;
   ImageButton imagebutton_invest24;
   ImageButton imagebutton_buttonshop;
   ImageButton imagebutton_buttonquests;
   ImageButton imagebutton_buttonrewards;
   ImageButton imagebutton_buttoncrafting;
   ImageButton imagebutton_buttontrain;
   ImageButton imagebutton_buttonabilities;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/panel_rework_2.png");

   public PanelRework2Screen(PanelRework2Menu container, Inventory inventory, Component text) {
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
      if (mouseX > this.leftPos + -50 && mouseX < this.leftPos + -26 && mouseY > this.topPos + -93 && mouseY < this.topPos + -69) {
         guiGraphics.renderTooltip(this.font, Component.literal(ReturnRemainingXPProcedure.execute(this.entity)), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -75 && mouseX < this.leftPos + -51 && mouseY > this.topPos + -93 && mouseY < this.topPos + -69) {
         guiGraphics.renderTooltip(this.font, Component.literal(ReturnRemainingXPProcedure.execute(this.entity)), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -155 && mouseX < this.leftPos + -131 && mouseY > this.topPos + -86 && mouseY < this.topPos + -62) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_rework_2.tooltip_open_system_shop"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -157 && mouseX < this.leftPos + -133 && mouseY > this.topPos + -19 && mouseY < this.topPos + 5) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_rework_2.tooltip_collectible_rewards"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -155 && mouseX < this.leftPos + -131 && mouseY > this.topPos + 48 && mouseY < this.topPos + 72) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_rework_2.tooltip_daily_quest"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 138 && mouseX < this.leftPos + 162 && mouseY > this.topPos + 60 && mouseY < this.topPos + 84) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_rework_2.tooltip_settings"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 140 && mouseX < this.leftPos + 164 && mouseY > this.topPos + -8 && mouseY < this.topPos + 16) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_rework_2.tooltip_crafting"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 138 && mouseX < this.leftPos + 162 && mouseY > this.topPos + -74 && mouseY < this.topPos + -50) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_rework_2.tooltip_training"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -75 && mouseX < this.leftPos + -51 && mouseY > this.topPos + -63 && mouseY < this.topPos + -39) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_rework_2.tooltip_titles_wip"), mouseX, mouseY);
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/panel_rework_8.png"), this.leftPos + -149, this.topPos + -120, 0.0F, 0.0F, 300, 240, 300, 240
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
      guiGraphics.drawString(this.font, ReturnStrengthProcedure.execute(this.entity), -40, 30, -1, false);
      guiGraphics.drawString(this.font, ReturnVitalityProcedure.execute(this.entity), 27, 30, -1, false);
      guiGraphics.drawString(this.font, ReturnAgilityProcedure.execute(this.entity), -40, 45, -1, false);
      guiGraphics.drawString(this.font, ReturnIntelligenceProcedure.execute(this.entity), 28, 45, -1, false);
      guiGraphics.drawString(this.font, ReturnPerceptionProcedure.execute(this.entity), -40, 61, -1, false);
      guiGraphics.drawString(this.font, ReturnSPProcedure.execute(this.entity), 28, 61, -1, false);
      guiGraphics.drawString(this.font, ReturnNameProcedure.execute(this.entity), -74, -72, -1, false);
      guiGraphics.drawString(this.font, ReturnTitleProcedure.execute(this.entity), -74, -57, -1, false);
      guiGraphics.drawString(this.font, ReturnJobProcedure.execute(this.entity), -74, -41, -1, false);
      guiGraphics.drawString(this.font, ReturnFatigueProcedure.execute(this.entity), -74, -27, -1, false);
      guiGraphics.drawString(this.font, ReturnLevelProcedure.execute(this.entity), -74, -85, -1, false);
      guiGraphics.drawString(this.font, ReturnHPProcedure.execute(this.entity), -79, -9, -1, false);
      guiGraphics.drawString(this.font, ReturnMPProcedure.execute(this.entity), 3, -9, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_invest2 = new ImageButton(
         this.leftPos + -87,
         this.topPos + 29,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest2.png"),
         7,
         14,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(0, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest2", this.imagebutton_invest2);
      this.addRenderableWidget(this.imagebutton_invest2);
      this.imagebutton_invest21 = new ImageButton(
         this.leftPos + -87,
         this.topPos + 45,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest21.png"),
         7,
         14,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(1, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest21", this.imagebutton_invest21);
      this.addRenderableWidget(this.imagebutton_invest21);
      this.imagebutton_invest22 = new ImageButton(
         this.leftPos + -87,
         this.topPos + 61,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest22.png"),
         7,
         14,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(2, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest22", this.imagebutton_invest22);
      this.addRenderableWidget(this.imagebutton_invest22);
      this.imagebutton_invest23 = new ImageButton(
         this.leftPos + 85,
         this.topPos + 29,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest23.png"),
         7,
         14,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(3, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 3, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest23", this.imagebutton_invest23);
      this.addRenderableWidget(this.imagebutton_invest23);
      this.imagebutton_invest24 = new ImageButton(
         this.leftPos + 85,
         this.topPos + 45,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest24.png"),
         7,
         14,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(4, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 4, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_invest24", this.imagebutton_invest24);
      this.addRenderableWidget(this.imagebutton_invest24);
      this.imagebutton_buttonshop = new ImageButton(
         this.leftPos + -159,
         this.topPos + -90,
         32,
         32,
         0,
         0,
         32,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_buttonshop.png"),
         32,
         64,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(5, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 5, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_buttonshop", this.imagebutton_buttonshop);
      this.addRenderableWidget(this.imagebutton_buttonshop);
      this.imagebutton_buttonquests = new ImageButton(
         this.leftPos + -159,
         this.topPos + 44,
         32,
         32,
         0,
         0,
         32,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_buttonquests.png"),
         32,
         64,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(6, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 6, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_buttonquests", this.imagebutton_buttonquests);
      this.addRenderableWidget(this.imagebutton_buttonquests);
      this.imagebutton_buttonrewards = new ImageButton(
         this.leftPos + -161,
         this.topPos + -23,
         32,
         32,
         0,
         0,
         32,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_buttonrewards.png"),
         32,
         64,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(7, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 7, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_buttonrewards", this.imagebutton_buttonrewards);
      this.addRenderableWidget(this.imagebutton_buttonrewards);
      this.imagebutton_buttoncrafting = new ImageButton(
         this.leftPos + 136,
         this.topPos + -12,
         32,
         32,
         0,
         0,
         32,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_buttoncrafting.png"),
         32,
         64,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(8, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 8, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_buttoncrafting", this.imagebutton_buttoncrafting);
      this.addRenderableWidget(this.imagebutton_buttoncrafting);
      this.imagebutton_buttontrain = new ImageButton(
         this.leftPos + 134,
         this.topPos + -78,
         32,
         32,
         0,
         0,
         32,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_buttontrain.png"),
         32,
         64,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(9, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 9, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_buttontrain", this.imagebutton_buttontrain);
      this.addRenderableWidget(this.imagebutton_buttontrain);
      this.imagebutton_buttonabilities = new ImageButton(
         this.leftPos + 134,
         this.topPos + 56,
         32,
         32,
         0,
         0,
         32,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_buttonabilities.png"),
         32,
         64,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelRework2ButtonMessage(10, this.x, this.y, this.z));
            PanelRework2ButtonMessage.handleButtonAction(this.entity, 10, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_buttonabilities", this.imagebutton_buttonabilities);
      this.addRenderableWidget(this.imagebutton_buttonabilities);
   }
}
