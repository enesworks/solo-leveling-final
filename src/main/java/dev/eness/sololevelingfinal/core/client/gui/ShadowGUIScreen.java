package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.ShadowGUIButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.BeruSpawnedProcedure;
import dev.eness.sololevelingfinal.core.procedures.IgrisNotSpawnedProcedure;
import dev.eness.sololevelingfinal.core.procedures.IgrisSpawnedProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnGoblinArcherProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShadowBeruProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShadowGobMageProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShadowGobProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShadowIgrisProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShadowSoldProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShadowStorageLevelProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShadowWolfProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowGUIMenu;

public class ShadowGUIScreen extends AbstractContainerScreen<ShadowGUIMenu> {
   private static final HashMap<String, Object> guistate = ShadowGUIMenu.guistate;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   Button button_berserk;
   Button button_dismiss_soldiers;
   Button button_dismiss_goblin_club;
   Button button_dismiss_goblin_mage;
   Button button_dismisswolf;
   Button button_dismiss_goblin_club1;
   private static final ResourceLocation texture = new ResourceLocation("sololeveling:textures/screens/shadow_gui.png");

   public ShadowGUIScreen(ShadowGUIMenu container, Inventory inventory, Component text) {
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
      if (ReturnShadowIgrisProcedure.execute(this.world, this.x, this.y, this.z, this.entity) instanceof LivingEntity livingEntity
         && IgrisSpawnedProcedure.execute(this.entity)) {
         InventoryScreen.renderEntityInInventoryFollowsAngle(
            guiGraphics,
            this.leftPos + -103,
            this.topPos + 3,
            30,
            0.0F + (float)Math.atan((this.leftPos + -103 - mouseX) / 40.0),
            (float)Math.atan((this.topPos + -46 - mouseY) / 40.0),
            livingEntity
         );
      }

      if (ReturnShadowBeruProcedure.execute(this.world, this.x, this.y, this.z, this.entity) instanceof LivingEntity livingEntity
         && BeruSpawnedProcedure.execute(this.entity)) {
         InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + 101, this.topPos + 1, 30, 0.0F, 0.0F, livingEntity);
      }

      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/basewide2.png"), this.leftPos + -151, this.topPos + -119, 0.0F, 0.0F, 300, 225, 300, 225
      );
      if (IgrisNotSpawnedProcedure.execute(this.entity)) {
         guiGraphics.blit(
            new ResourceLocation("sololeveling:textures/screens/shadowunlocked.png"), this.leftPos + -135, this.topPos + -98, 0.0F, 0.0F, 67, 105, 67, 105
         );
      }

      guiGraphics.blit(
         new ResourceLocation("sololeveling:textures/screens/shadowunlocked.png"), this.leftPos + 67, this.topPos + -100, 0.0F, 0.0F, 67, 105, 67, 105
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
      if (IgrisNotSpawnedProcedure.execute(this.entity)) {
         guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.shadow_gui.label_not_spawned"), -115, -53, -10092544, false);
      }

      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.shadow_gui.label_available_orders"), -46, -94, -16777216, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.shadow_gui.label_beru"), 90, -55, -10092544, false);
      guiGraphics.drawString(this.font, ReturnShadowSoldProcedure.execute(this.entity), -37, 7, -16777216, false);
      guiGraphics.drawString(this.font, ReturnShadowGobProcedure.execute(this.entity), -21, 26, -16777216, false);
      guiGraphics.drawString(this.font, ReturnShadowWolfProcedure.execute(this.entity), -63, 82, -16777216, false);
      guiGraphics.drawString(this.font, Component.translatable("gui.sololeveling.shadow_gui.label_shadow_storage"), 73, 18, -16777216, false);
      guiGraphics.drawString(this.font, ReturnShadowStorageLevelProcedure.execute(this.entity), 71, 32, -16777216, false);
      guiGraphics.drawString(this.font, ReturnGoblinArcherProcedure.execute(this.entity), -21, 44, -16777216, false);
      guiGraphics.drawString(this.font, ReturnShadowGobMageProcedure.execute(this.entity), -21, 64, -16777216, false);
   }

   @Override
   public void onClose() {
      super.onClose();
   }

   @Override
   public void init() {
      super.init();
      this.button_berserk = Button.builder(Component.translatable("gui.sololeveling.shadow_gui.button_berserk"), e -> {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowGUIButtonMessage(0, this.x, this.y, this.z));
         ShadowGUIButtonMessage.handleButtonAction(this.entity, 0, this.x, this.y, this.z);
      }).bounds(this.leftPos + -37, this.topPos + -74, 61, 20).build();
      guistate.put("button:button_berserk", this.button_berserk);
      this.addRenderableWidget(this.button_berserk);
      this.button_dismiss_soldiers = Button.builder(Component.translatable("gui.sololeveling.shadow_gui.button_dismiss_soldiers"), e -> {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowGUIButtonMessage(1, this.x, this.y, this.z));
         ShadowGUIButtonMessage.handleButtonAction(this.entity, 1, this.x, this.y, this.z);
      }).bounds(this.leftPos + -146, this.topPos + 2, 108, 20).build();
      guistate.put("button:button_dismiss_soldiers", this.button_dismiss_soldiers);
      this.addRenderableWidget(this.button_dismiss_soldiers);
      this.button_dismiss_goblin_club = Button.builder(Component.translatable("gui.sololeveling.shadow_gui.button_dismiss_goblin_club"), e -> {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowGUIButtonMessage(2, this.x, this.y, this.z));
         ShadowGUIButtonMessage.handleButtonAction(this.entity, 2, this.x, this.y, this.z);
      }).bounds(this.leftPos + -146, this.topPos + 21, 124, 20).build();
      guistate.put("button:button_dismiss_goblin_club", this.button_dismiss_goblin_club);
      this.addRenderableWidget(this.button_dismiss_goblin_club);
      this.button_dismiss_goblin_mage = Button.builder(Component.translatable("gui.sololeveling.shadow_gui.button_dismiss_goblin_mage"), e -> {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowGUIButtonMessage(3, this.x, this.y, this.z));
         ShadowGUIButtonMessage.handleButtonAction(this.entity, 3, this.x, this.y, this.z);
      }).bounds(this.leftPos + -146, this.topPos + 59, 124, 20).build();
      guistate.put("button:button_dismiss_goblin_mage", this.button_dismiss_goblin_mage);
      this.addRenderableWidget(this.button_dismiss_goblin_mage);
      this.button_dismisswolf = Button.builder(Component.translatable("gui.sololeveling.shadow_gui.button_dismisswolf"), e -> {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowGUIButtonMessage(4, this.x, this.y, this.z));
         ShadowGUIButtonMessage.handleButtonAction(this.entity, 4, this.x, this.y, this.z);
      }).bounds(this.leftPos + -146, this.topPos + 77, 82, 20).build();
      guistate.put("button:button_dismisswolf", this.button_dismisswolf);
      this.addRenderableWidget(this.button_dismisswolf);
      this.button_dismiss_goblin_club1 = Button.builder(Component.translatable("gui.sololeveling.shadow_gui.button_dismiss_goblin_club1"), e -> {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowGUIButtonMessage(5, this.x, this.y, this.z));
         ShadowGUIButtonMessage.handleButtonAction(this.entity, 5, this.x, this.y, this.z);
      }).bounds(this.leftPos + -146, this.topPos + 40, 124, 20).build();
      guistate.put("button:button_dismiss_goblin_club1", this.button_dismiss_goblin_club1);
      this.addRenderableWidget(this.button_dismiss_goblin_club1);
   }
}
