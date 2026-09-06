package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.network.FoodGuiButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.FoodGuiMenu;

public class FoodGuiScreen extends SystemContainerScreen<FoodGuiMenu> {
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private static final int SLOT_X = -53;
   private static final int[] SLOT_Y = new int[]{-84, -57, -30};

   public FoodGuiScreen(FoodGuiMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -92;
      this.pRelY = -100;
      this.pW = 184;
      this.pH = 196;
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int gx, int gy) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShopStyle.panel(g, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, this.pH);
      ShopStyle.titleBar(g, this.font, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, "FOOD SHOP");

      for (int sy : SLOT_Y) {
         ShopStyle.slot(g, this.leftPos + -53, this.topPos + sy);
      }

      for (int si = 0; si < 3; si++) {
         for (int sj = 0; sj < 9; sj++) {
            ShopStyle.slot(g, this.leftPos + -79 + sj * 18, this.topPos + 1 + si * 18);
         }
      }

      for (int si = 0; si < 9; si++) {
         ShopStyle.slot(g, this.leftPos + -79 + si * 18, this.topPos + 59);
      }

      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      g.drawString(this.font, Component.translatable("gui.sololeveling.food_gui.label_25g_x16"), -30, SLOT_Y[0] + 1, -9882, false);
      g.drawString(this.font, Component.translatable("gui.sololeveling.food_gui.label_x16"), -30, SLOT_Y[0] + 10, -7358248, false);
      g.drawString(this.font, Component.translatable("gui.sololeveling.food_gui.label_40g"), -30, SLOT_Y[1] + 1, -9882, false);
      g.drawString(this.font, Component.translatable("gui.sololeveling.food_gui.label_x8"), -30, SLOT_Y[1] + 10, -7358248, false);
      g.drawString(this.font, Component.translatable("gui.sololeveling.food_gui.label_80g"), -30, SLOT_Y[2] + 1, -9882, false);
      g.drawString(this.font, Component.translatable("gui.sololeveling.food_gui.label_x1"), -30, SLOT_Y[2] + 10, -7358248, false);
      ShopStyle.gold(g, this.font, this.entity, this.pRelX + 6, this.pRelY + this.pH - 12);
   }

   @Override
   public void init() {
      super.init();

      for (int i = 0; i < 3; i++) {
         int id = i;
         PlainTextButton buy = new PlainTextButton(
            this.leftPos + -53 - 1,
            this.topPos + SLOT_Y[i] - 1,
            18,
            18,
            Component.literal(""),
            e -> SololevelingMod.PACKET_HANDLER.sendToServer(new FoodGuiButtonMessage(id, this.x, this.y, this.z)),
            this.font
         );
         this.addRenderableWidget(buy);
      }

      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.leftPos + this.pRelX + 3,
            this.topPos + this.pRelY + 2,
            40,
            12,
            Component.literal("< Back"),
            b -> SololevelingMod.PACKET_HANDLER.sendToServer(new FoodGuiButtonMessage(3, this.x, this.y, this.z))
         )
      );
   }
}
