package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.PanelEarlyButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.FatigueTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.HasSkillPointsProcedure;
import dev.eness.sololevelingfinal.core.procedures.HealthTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.IntelligenceTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.LevelTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.ManaTextGUIProcedure;
import dev.eness.sololevelingfinal.core.procedures.MaxXpTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnClassNameProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnNameAndGuildProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnPlayerProcedure;
import dev.eness.sololevelingfinal.core.procedures.SenseTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.SkillPointsTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.StrengthTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.TimerTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.TitleTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.VitalityTextProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.PanelEarlyMenu;

public class PanelEarlyScreen extends AbstractContainerScreen<PanelEarlyMenu> {
   private static final HashMap<String, Object> guistate = PanelEarlyMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_invest;
   ImageButton imagebutton_oie_transparent;
   ImageButton imagebutton_oie_transparent1;
   ImageButton imagebutton_oie_transparent2;
   ImageButton imagebutton_oie_transparent3;
   ImageButton imagebutton_oie_transparent5;
   ImageButton imagebutton_oie_transparent6;
   ImageButton imagebutton_store;
   ImageButton imagebutton_quests;
   ImageButton imagebutton_abilities;
   ImageButton imagebutton_craftingmenu;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/panel_early.png");

   public PanelEarlyScreen(PanelEarlyMenu container, Inventory inventory, Component text) {
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
      if (ReturnPlayerProcedure.execute(this.entity) instanceof LivingEntity livingEntity) {
         InventoryScreen.renderEntityInInventoryFollowsAngle(
            guiGraphics,
            this.leftPos + 109,
            this.topPos + -54,
            30,
            0.0F + (float)Math.atan((this.leftPos + 109 - mouseX) / 40.0),
            (float)Math.atan((this.topPos + -103 - mouseY) / 40.0),
            livingEntity
         );
      }

      this.renderTooltip(guiGraphics, mouseX, mouseY);
      if (mouseX > this.leftPos + -110 && mouseX < this.leftPos + -86 && mouseY > this.topPos + 16 && mouseY < this.topPos + 40) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_early.tooltip_active_quests"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 91 && mouseX < this.leftPos + 115 && mouseY > this.topPos + -49 && mouseY < this.topPos + -25) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_early.tooltip_training"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -109 && mouseX < this.leftPos + -85 && mouseY > this.topPos + -16 && mouseY < this.topPos + 8) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_early.tooltip_daily_rewards"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -109 && mouseX < this.leftPos + -85 && mouseY > this.topPos + -49 && mouseY < this.topPos + -25) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_early.tooltip_shop"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 91 && mouseX < this.leftPos + 115 && mouseY > this.topPos + 17 && mouseY < this.topPos + 41) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_early.tooltip_adjust_abilities"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 91 && mouseX < this.leftPos + 115 && mouseY > this.topPos + -16 && mouseY < this.topPos + 8) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.panel_early.tooltip_craft_special_items"), mouseX, mouseY);
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(new ResourceLocation("sololeveling:textures/screens/panel3.png"), this.leftPos + -86, this.topPos + -117, 0.0F, 0.0F, 181, 242, 181, 242);
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
      guiGraphics.drawString(this.font, VitalityTextProcedure.execute(this.entity), 59, 41, -1, false);
      guiGraphics.drawString(this.font, StrengthTextProcedure.execute(this.entity), -7, 42, -1, false);
      guiGraphics.drawString(this.font, IntelligenceTextProcedure.execute(this.entity), 4, 74, -1, false);
      guiGraphics.drawString(this.font, SpeedTextProcedure.execute(this.entity), -19, 57, -1, false);
      guiGraphics.drawString(this.font, LevelTextProcedure.execute(this.entity), 51, -82, -1, false);
      guiGraphics.drawString(this.font, SkillPointsTextProcedure.execute(this.entity), 32, 98, -1, false);
      guiGraphics.drawString(this.font, MaxXpTextProcedure.execute(this.entity), 40, -60, -1, false);
      guiGraphics.drawString(this.font, FatigueTextProcedure.execute(this.entity), 59, -36, -1, false);
      guiGraphics.drawString(this.font, HealthTextProcedure.execute(this.entity), -47, -16, -1, false);
      guiGraphics.drawString(this.font, ManaTextGUIProcedure.execute(this.entity), -48, 6, -1, false);
      guiGraphics.drawString(this.font, TitleTextProcedure.execute(this.entity), -44, -38, -13312, false);
      guiGraphics.drawString(this.font, ReturnNameAndGuildProcedure.execute(this.entity), -40, -83, -3355444, false);
      guiGraphics.drawString(this.font, ReturnClassNameProcedure.execute(this.entity), -46, -61, -3355444, false);
      guiGraphics.drawString(this.font, SenseTextProcedure.execute(this.entity), 49, 57, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.panel_early.label_welcome_player"), -33, -106, -6684673, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.panel_early.label_daily"), -65, 20, -1, false);
      guiGraphics.drawString(this.font, TimerTextProcedure.execute(this.entity), -33, 21, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_invest = new ImageButton(
         this.leftPos + -64, this.topPos + 42, 7, 7, 0, 0, 7, new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_invest.png"), 7, 14, e -> {
            if (HasSkillPointsProcedure.execute(this.entity)) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(0, this.x, this.y, this.z));
               PanelEarlyButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
            }
         }
      ) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (HasSkillPointsProcedure.execute(PanelEarlyScreen.this.entity)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      };
      guistate.put("button:imagebutton_invest", this.imagebutton_invest);
      this.addRenderableWidget(this.imagebutton_invest);
      this.imagebutton_oie_transparent = new ImageButton(
         this.leftPos + -64,
         this.topPos + 57,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_oie_transparent.png"),
         7,
         14,
         e -> {
            if (HasSkillPointsProcedure.execute(this.entity)) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(1, this.x, this.y, this.z));
               PanelEarlyButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
            }
         }
      ) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (HasSkillPointsProcedure.execute(PanelEarlyScreen.this.entity)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      };
      guistate.put("button:imagebutton_oie_transparent", this.imagebutton_oie_transparent);
      this.addRenderableWidget(this.imagebutton_oie_transparent);
      this.imagebutton_oie_transparent1 = new ImageButton(
         this.leftPos + 11,
         this.topPos + 57,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_oie_transparent1.png"),
         7,
         14,
         e -> {
            if (HasSkillPointsProcedure.execute(this.entity)) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(2, this.x, this.y, this.z));
               PanelEarlyButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
            }
         }
      ) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (HasSkillPointsProcedure.execute(PanelEarlyScreen.this.entity)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      };
      guistate.put("button:imagebutton_oie_transparent1", this.imagebutton_oie_transparent1);
      this.addRenderableWidget(this.imagebutton_oie_transparent1);
      this.imagebutton_oie_transparent2 = new ImageButton(
         this.leftPos + 11,
         this.topPos + 42,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_oie_transparent2.png"),
         7,
         14,
         e -> {
            if (HasSkillPointsProcedure.execute(this.entity)) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(3, this.x, this.y, this.z));
               PanelEarlyButtonMessage.handleButtonAction(this.entity, 3, this.x, this.y, this.z);
            }
         }
      ) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (HasSkillPointsProcedure.execute(PanelEarlyScreen.this.entity)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      };
      guistate.put("button:imagebutton_oie_transparent2", this.imagebutton_oie_transparent2);
      this.addRenderableWidget(this.imagebutton_oie_transparent2);
      this.imagebutton_oie_transparent3 = new ImageButton(
         this.leftPos + -64,
         this.topPos + 74,
         7,
         7,
         0,
         0,
         7,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_oie_transparent3.png"),
         7,
         14,
         e -> {
            if (HasSkillPointsProcedure.execute(this.entity)) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(4, this.x, this.y, this.z));
               PanelEarlyButtonMessage.handleButtonAction(this.entity, 4, this.x, this.y, this.z);
            }
         }
      ) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (HasSkillPointsProcedure.execute(PanelEarlyScreen.this.entity)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      };
      guistate.put("button:imagebutton_oie_transparent3", this.imagebutton_oie_transparent3);
      this.addRenderableWidget(this.imagebutton_oie_transparent3);
      this.imagebutton_oie_transparent5 = new ImageButton(
         this.leftPos + -106,
         this.topPos + -11,
         17,
         15,
         0,
         0,
         15,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_oie_transparent5.png"),
         17,
         30,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(5, this.x, this.y, this.z));
            PanelEarlyButtonMessage.handleButtonAction(this.entity, 5, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_oie_transparent5", this.imagebutton_oie_transparent5);
      this.addRenderableWidget(this.imagebutton_oie_transparent5);
      this.imagebutton_oie_transparent6 = new ImageButton(
         this.leftPos + 94,
         this.topPos + -44,
         17,
         15,
         0,
         0,
         15,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_oie_transparent6.png"),
         17,
         30,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(6, this.x, this.y, this.z));
            PanelEarlyButtonMessage.handleButtonAction(this.entity, 6, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_oie_transparent6", this.imagebutton_oie_transparent6);
      this.addRenderableWidget(this.imagebutton_oie_transparent6);
      this.imagebutton_store = new ImageButton(
         this.leftPos + -106,
         this.topPos + -44,
         17,
         15,
         0,
         0,
         15,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_store.png"),
         17,
         30,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(7, this.x, this.y, this.z));
            PanelEarlyButtonMessage.handleButtonAction(this.entity, 7, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_store", this.imagebutton_store);
      this.addRenderableWidget(this.imagebutton_store);
      this.imagebutton_quests = new ImageButton(
         this.leftPos + -106,
         this.topPos + 22,
         10,
         10,
         0,
         0,
         10,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_quests.png"),
         10,
         20,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(8, this.x, this.y, this.z));
            PanelEarlyButtonMessage.handleButtonAction(this.entity, 8, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_quests", this.imagebutton_quests);
      this.addRenderableWidget(this.imagebutton_quests);
      this.imagebutton_abilities = new ImageButton(
         this.leftPos + 94,
         this.topPos + 22,
         17,
         15,
         0,
         0,
         15,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_abilities.png"),
         17,
         30,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(9, this.x, this.y, this.z));
            PanelEarlyButtonMessage.handleButtonAction(this.entity, 9, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_abilities", this.imagebutton_abilities);
      this.addRenderableWidget(this.imagebutton_abilities);
      this.imagebutton_craftingmenu = new ImageButton(
         this.leftPos + 94,
         this.topPos + -11,
         17,
         15,
         0,
         0,
         15,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_craftingmenu.png"),
         17,
         30,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new PanelEarlyButtonMessage(10, this.x, this.y, this.z));
            PanelEarlyButtonMessage.handleButtonAction(this.entity, 10, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_craftingmenu", this.imagebutton_craftingmenu);
      this.addRenderableWidget(this.imagebutton_craftingmenu);
   }
}
