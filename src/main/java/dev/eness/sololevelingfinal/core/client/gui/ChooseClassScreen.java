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
import dev.eness.sololevelingfinal.core.network.ChooseClassButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.ChooseClassMenu;

public class ChooseClassScreen extends AbstractContainerScreen<ChooseClassMenu> {
   private static final HashMap<String, Object> guistate = ChooseClassMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_classchooseassasin;
   ImageButton imagebutton_classchoosemage;
   ImageButton imagebutton_classchooseknight;
   ImageButton imagebutton_classchoosetank;
   ImageButton imagebutton_classchoosehealer;
   ImageButton imagebutton_classchoosearcher;

   public ChooseClassScreen(ChooseClassMenu container, Inventory inventory, Component text) {
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
      if (mouseX > this.leftPos + -67 && mouseX < this.leftPos + -43 && mouseY > this.topPos + -65 && mouseY < this.topPos + -41) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_speed_and_strenght_fro"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -24 && mouseX < this.leftPos + 0 && mouseY > this.topPos + -65 && mouseY < this.topPos + -41) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_speed_and_strenght_fro1"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -45 && mouseX < this.leftPos + -21 && mouseY > this.topPos + -65 && mouseY < this.topPos + -41) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_speed_and_strenght_fro2"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 0 && mouseX < this.leftPos + 24 && mouseY > this.topPos + -65 && mouseY < this.topPos + -41) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_passive_mana_regenerat"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 24 && mouseX < this.leftPos + 48 && mouseY > this.topPos + -65 && mouseY < this.topPos + -41) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_passive_mana_regenerat1"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 43 && mouseX < this.leftPos + 67 && mouseY > this.topPos + -65 && mouseY < this.topPos + -41) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_passive_mana_regenerat2"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -67 && mouseX < this.leftPos + -43 && mouseY > this.topPos + -28 && mouseY < this.topPos + -4) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_damage_from_using_swor"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -45 && mouseX < this.leftPos + -21 && mouseY > this.topPos + -28 && mouseY < this.topPos + -4) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_damage_from_using_swor1"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -24 && mouseX < this.leftPos + 0 && mouseY > this.topPos + -28 && mouseY < this.topPos + -4) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_damage_from_using_swor2"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 0 && mouseX < this.leftPos + 24 && mouseY > this.topPos + -28 && mouseY < this.topPos + -4) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_strenght_and_defence_w"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 21 && mouseX < this.leftPos + 45 && mouseY > this.topPos + -28 && mouseY < this.topPos + -4) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_strenght_and_defence_w1"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 43 && mouseX < this.leftPos + 67 && mouseY > this.topPos + -28 && mouseY < this.topPos + -4) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_increased_strenght_and_defence_w2"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -45 && mouseX < this.leftPos + -21 && mouseY > this.topPos + 11 && mouseY < this.topPos + 35) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_passive_regeneration_effects_on"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -67 && mouseX < this.leftPos + -43 && mouseY > this.topPos + 10 && mouseY < this.topPos + 34) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_passive_regeneration_effects_on1"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + -24 && mouseX < this.leftPos + 0 && mouseY > this.topPos + 11 && mouseY < this.topPos + 35) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_passive_regeneration_effects_on2"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 0 && mouseX < this.leftPos + 24 && mouseY > this.topPos + 10 && mouseY < this.topPos + 34) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_utilize_special_bows_and_use_man"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 21 && mouseX < this.leftPos + 45 && mouseY > this.topPos + 10 && mouseY < this.topPos + 34) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_utilize_special_bows_and_use_man1"), mouseX, mouseY);
      }

      if (mouseX > this.leftPos + 42 && mouseX < this.leftPos + 66 && mouseY > this.topPos + 10 && mouseY < this.topPos + 34) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.sololeveling.choose_class.tooltip_utilize_special_bows_and_use_man2"), mouseX, mouseY);
      }
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/techbase.png"), this.leftPos + -75, this.topPos + -73, 0.0F, 0.0F, 150, 150, 150, 150
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
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_classchooseassasin = new ImageButton(
         this.leftPos + -65,
         this.topPos + -63,
         64,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_classchooseassasin.png"),
         64,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new ChooseClassButtonMessage(0, this.x, this.y, this.z));
            ChooseClassButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_classchooseassasin", this.imagebutton_classchooseassasin);
      this.addRenderableWidget(this.imagebutton_classchooseassasin);
      this.imagebutton_classchoosemage = new ImageButton(
         this.leftPos + 1,
         this.topPos + -63,
         64,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_classchoosemage.png"),
         64,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new ChooseClassButtonMessage(1, this.x, this.y, this.z));
            ChooseClassButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_classchoosemage", this.imagebutton_classchoosemage);
      this.addRenderableWidget(this.imagebutton_classchoosemage);
      this.imagebutton_classchooseknight = new ImageButton(
         this.leftPos + -65,
         this.topPos + -26,
         64,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_classchooseknight.png"),
         64,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new ChooseClassButtonMessage(2, this.x, this.y, this.z));
            ChooseClassButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_classchooseknight", this.imagebutton_classchooseknight);
      this.addRenderableWidget(this.imagebutton_classchooseknight);
      this.imagebutton_classchoosetank = new ImageButton(
         this.leftPos + 1,
         this.topPos + -26,
         64,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_classchoosetank.png"),
         64,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new ChooseClassButtonMessage(3, this.x, this.y, this.z));
            ChooseClassButtonMessage.handleButtonAction(this.entity, 3, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_classchoosetank", this.imagebutton_classchoosetank);
      this.addRenderableWidget(this.imagebutton_classchoosetank);
      this.imagebutton_classchoosehealer = new ImageButton(
         this.leftPos + -65,
         this.topPos + 11,
         64,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_classchoosehealer.png"),
         64,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new ChooseClassButtonMessage(4, this.x, this.y, this.z));
            ChooseClassButtonMessage.handleButtonAction(this.entity, 4, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_classchoosehealer", this.imagebutton_classchoosehealer);
      this.addRenderableWidget(this.imagebutton_classchoosehealer);
      this.imagebutton_classchoosearcher = new ImageButton(
         this.leftPos + 1,
         this.topPos + 11,
         64,
         21,
         0,
         0,
         21,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_classchoosearcher.png"),
         64,
         42,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new ChooseClassButtonMessage(5, this.x, this.y, this.z));
            ChooseClassButtonMessage.handleButtonAction(this.entity, 5, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_classchoosearcher", this.imagebutton_classchoosearcher);
      this.addRenderableWidget(this.imagebutton_classchoosearcher);
   }
}
