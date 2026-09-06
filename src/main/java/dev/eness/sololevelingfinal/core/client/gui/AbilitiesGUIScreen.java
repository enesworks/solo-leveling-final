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
import dev.eness.sololevelingfinal.core.network.AbilitiesGUIButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.IsPlayerProcedure;
import dev.eness.sololevelingfinal.core.procedures.SMonTextProcedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent0Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent100Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent10Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent20Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent30Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent40Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent50Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent60Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent70Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent80Procedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedPercent90Procedure;
import dev.eness.sololevelingfinal.core.procedures.TripleJumpButtonDisplayConProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.AbilitiesGUIMenu;

public class AbilitiesGUIScreen extends AbstractContainerScreen<AbilitiesGUIMenu> {
   private static final HashMap<String, Object> guistate = AbilitiesGUIMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_back;
   ImageButton imagebutton_guiback;
   ImageButton imagebutton_guiforward;
   ImageButton imagebutton_guiabilitytj;
   ImageButton imagebutton_job1;
   ImageButton imagebutton_panel_rework_rewardbutton;

   public AbilitiesGUIScreen(AbilitiesGUIMenu container, Inventory inventory, Component text) {
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
      if (IsPlayerProcedure.execute(this.entity)
         && mouseX > this.leftPos + -126
         && mouseX < this.leftPos + -102
         && mouseY > this.topPos + -23
         && mouseY < this.topPos + 1) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.abilities_gui.tooltip_back_to_main_panel"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -44 && mouseX < this.leftPos + -20 && mouseY > this.topPos + -11 && mouseY < this.topPos + 13) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.abilities_gui.tooltip_toggle_triple_jump"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 96 && mouseX < this.leftPos + 120 && mouseY > this.topPos + -116 && mouseY < this.topPos + -92) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.abilities_gui.tooltip_adjust_your_job_abilities"), mouseX, mouseY);
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/panel_rework_vertical2.png"),
         this.leftPos + -103,
         this.topPos + -114,
         0.0F,
         0.0F,
         200,
         225,
         200,
         225
      );
      guiGraphics.blit(new ResourceLocation("sololeveling:textures/screens/guipercent.png"), this.leftPos + -17, this.topPos + -61, 0.0F, 0.0F, 16, 16, 16, 16);
      if (SpeedPercent10Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui10percent.png"), this.leftPos + -3, this.topPos + -61, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent20Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui20percent.png"), this.leftPos + -3, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent30Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui30percent.png"), this.leftPos + -3, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent40Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui40percent.png"), this.leftPos + -2, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent50Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui50percent.png"), this.leftPos + -3, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent60Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui60percent.png"), this.leftPos + -3, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent70Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui70percent.png"), this.leftPos + -3, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent80Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui80percent.png"), this.leftPos + -3, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent90Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui90percent.png"), this.leftPos + -3, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (SpeedPercent100Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui100percent.png"), this.leftPos + -4, this.topPos + -62, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

      if (TripleJumpButtonDisplayConProcedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/guiabilitytjon.png"), this.leftPos + -48, this.topPos + -15, 0.0F, 0.0F, 32, 32, 32, 32
         );
      }

      if (SpeedPercent0Procedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/gui0percent.png"), this.leftPos + -8, this.topPos + -61, 0.0F, 0.0F, 16, 16, 16, 16
         );
      }

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
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.abilities_gui.label_speed_adjustment"), -42, -76, -6697729, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.abilities_gui.label_abilities"), -23, -35, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_back = new ImageButton(
         this.leftPos + -124,
         this.topPos + -21,
         20,
         20,
         0,
         0,
         20,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_back.png"),
         20,
         40,
         e -> {
            if (IsPlayerProcedure.execute(this.entity)) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(0, this.x, this.y, this.z));
               AbilitiesGUIButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
            }
         }
      ) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (IsPlayerProcedure.execute(AbilitiesGUIScreen.this.entity)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      };
      guistate.put("button:imagebutton_back", this.imagebutton_back);
      this.addRenderableWidget(this.imagebutton_back);
      this.imagebutton_guiback = new ImageButton(
         this.leftPos + -36,
         this.topPos + -61,
         16,
         16,
         0,
         0,
         16,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_guiback.png"),
         16,
         32,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(1, this.x, this.y, this.z));
            AbilitiesGUIButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_guiback", this.imagebutton_guiback);
      this.addRenderableWidget(this.imagebutton_guiback);
      this.imagebutton_guiforward = new ImageButton(
         this.leftPos + 18,
         this.topPos + -61,
         16,
         16,
         0,
         0,
         16,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_guiforward.png"),
         16,
         32,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(2, this.x, this.y, this.z));
            AbilitiesGUIButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_guiforward", this.imagebutton_guiforward);
      this.addRenderableWidget(this.imagebutton_guiforward);
      this.imagebutton_guiabilitytj = new ImageButton(
         this.leftPos + -48,
         this.topPos + -15,
         32,
         32,
         0,
         0,
         32,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_guiabilitytj.png"),
         32,
         64,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(3, this.x, this.y, this.z));
            AbilitiesGUIButtonMessage.handleButtonAction(this.entity, 3, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_guiabilitytj", this.imagebutton_guiabilitytj);
      this.addRenderableWidget(this.imagebutton_guiabilitytj);
      this.imagebutton_job1 = new ImageButton(
         this.leftPos + 96,
         this.topPos + -117,
         25,
         25,
         0,
         0,
         25,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_job1.png"),
         25,
         50,
         e -> {
            if (SMonTextProcedure.execute(this.entity)) {
               SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(4, this.x, this.y, this.z));
               AbilitiesGUIButtonMessage.handleButtonAction(this.entity, 4, this.x, this.y, this.z);
            }
         }
      ) {
         @Override
         public void render(GuiGraphics guiGraphics, int gx, int gy, float ticks) {
            if (SMonTextProcedure.execute(AbilitiesGUIScreen.this.entity)) {
               super.render(guiGraphics, gx, gy, ticks);
            }
         }
      };
      guistate.put("button:imagebutton_job1", this.imagebutton_job1);
      this.addRenderableWidget(this.imagebutton_job1);
      this.imagebutton_panel_rework_rewardbutton = new ImageButton(
         this.leftPos + -34,
         this.topPos + -41,
         64,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_panel_rework_rewardbutton.png"),
         64,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(5, this.x, this.y, this.z));
            AbilitiesGUIButtonMessage.handleButtonAction(this.entity, 5, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_panel_rework_rewardbutton", this.imagebutton_panel_rework_rewardbutton);
      this.addRenderableWidget(this.imagebutton_panel_rework_rewardbutton);
   }
}
