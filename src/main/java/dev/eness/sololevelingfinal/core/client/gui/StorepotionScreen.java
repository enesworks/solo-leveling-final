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
import dev.eness.sololevelingfinal.core.network.StorepotionButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.CoinTierPot1Procedure;
import dev.eness.sololevelingfinal.core.procedures.CoinTierPot2Procedure;
import dev.eness.sololevelingfinal.core.procedures.CoinTierPot3Procedure;
import dev.eness.sololevelingfinal.core.procedures.GoldTextProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.StorepotionMenu;

public class StorepotionScreen extends AbstractContainerScreen<StorepotionMenu> {
   private static final HashMap<String, Object> guistate = StorepotionMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   ImageButton imagebutton_potionmana;
   ImageButton imagebutton_potionhealth;
   ImageButton imagebutton_potionfatigue;
   ImageButton imagebutton_potionmana1;
   ImageButton imagebutton_potionmana2;
   ImageButton imagebutton_potionhealth1;
   ImageButton imagebutton_potionfatigue1;
   ImageButton imagebutton_potionhealth2;
   ImageButton imagebutton_potionfatigue2;

   public StorepotionScreen(StorepotionMenu container, Inventory inventory, Component text) {
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
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/basewide.png"), this.leftPos + -149, this.topPos + -115, 0.0F, 0.0F, 300, 225, 300, 225
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
      guiGraphics.drawString(this.font, GoldTextProcedure.execute(this.entity), -134, -102, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_1000_mana"), -88, -79, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_5000"), -1, -79, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_1000"), 89, -79, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_6_hp"), -87, -10, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_15_hp"), -1, -10, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_full_recovery"), 88, -10, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_20_fatigue"), -88, 56, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_50_ftg"), -1, 56, -1, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.storepotion.label_100_ftg"), 88, 56, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot1Procedure.execute(this.entity), -88, -69, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot1Procedure.execute(this.entity), -88, 0, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot1Procedure.execute(this.entity), -88, 67, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot2Procedure.execute(this.entity), -2, -69, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot2Procedure.execute(this.entity), -3, 1, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot2Procedure.execute(this.entity), -2, 67, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot3Procedure.execute(this.entity), 89, -68, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot3Procedure.execute(this.entity), 88, 2, -1, false);
      guiGraphics.drawString(this.font, CoinTierPot3Procedure.execute(this.entity), 88, 67, -1, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.imagebutton_potionmana = new ImageButton(
         this.leftPos + -125,
         this.topPos + -83,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionmana.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(0, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionmana", this.imagebutton_potionmana);
      this.addRenderableWidget(this.imagebutton_potionmana);
      this.imagebutton_potionhealth = new ImageButton(
         this.leftPos + -125,
         this.topPos + -16,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionhealth.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(1, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionhealth", this.imagebutton_potionhealth);
      this.addRenderableWidget(this.imagebutton_potionhealth);
      this.imagebutton_potionfatigue = new ImageButton(
         this.leftPos + -125,
         this.topPos + 51,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionfatigue.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(2, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionfatigue", this.imagebutton_potionfatigue);
      this.addRenderableWidget(this.imagebutton_potionfatigue);
      this.imagebutton_potionmana1 = new ImageButton(
         this.leftPos + -41,
         this.topPos + -83,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionmana1.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(3, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 3, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionmana1", this.imagebutton_potionmana1);
      this.addRenderableWidget(this.imagebutton_potionmana1);
      this.imagebutton_potionmana2 = new ImageButton(
         this.leftPos + 50,
         this.topPos + -83,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionmana2.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(4, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 4, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionmana2", this.imagebutton_potionmana2);
      this.addRenderableWidget(this.imagebutton_potionmana2);
      this.imagebutton_potionhealth1 = new ImageButton(
         this.leftPos + -41,
         this.topPos + -16,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionhealth1.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(5, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 5, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionhealth1", this.imagebutton_potionhealth1);
      this.addRenderableWidget(this.imagebutton_potionhealth1);
      this.imagebutton_potionfatigue1 = new ImageButton(
         this.leftPos + -41,
         this.topPos + 51,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionfatigue1.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(6, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 6, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionfatigue1", this.imagebutton_potionfatigue1);
      this.addRenderableWidget(this.imagebutton_potionfatigue1);
      this.imagebutton_potionhealth2 = new ImageButton(
         this.leftPos + 50,
         this.topPos + -16,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionhealth2.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(7, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 7, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionhealth2", this.imagebutton_potionhealth2);
      this.addRenderableWidget(this.imagebutton_potionhealth2);
      this.imagebutton_potionfatigue2 = new ImageButton(
         this.leftPos + 50,
         this.topPos + 51,
         33,
         33,
         0,
         0,
         33,
         new ResourceLocation("sololeveling:textures/screens/atlas/imagebutton_potionfatigue2.png"),
         33,
         66,
         e -> {
            SololevelingMod.PACKET_HANDLER.sendToServer(new StorepotionButtonMessage(8, this.x, this.y, this.z));
            StorepotionButtonMessage.handleButtonAction(this.entity, 8, this.x, this.y, this.z);
         }
      );
      guistate.put("button:imagebutton_potionfatigue2", this.imagebutton_potionfatigue2);
      this.addRenderableWidget(this.imagebutton_potionfatigue2);
   }
}
