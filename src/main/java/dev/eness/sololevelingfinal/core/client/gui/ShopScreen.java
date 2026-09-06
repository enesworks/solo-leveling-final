package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.network.ShopButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.ReturnRefreshButtonProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShopSwords1Procedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShopSwords2Procedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShopSwords3Procedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShopSwords4Procedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShopSwords5Procedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnShopSwords6Procedure;
import dev.eness.sololevelingfinal.core.world.inventory.ShopMenu;

public class ShopScreen extends SystemContainerScreen<ShopMenu> {
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private static final int SLOT_X = -30;
   private static final int[] SLOT_Y = new int[]{-60, -36, -12, 12, 36, 60};
   private AbstractWidget refreshButton;

   public ShopScreen(ShopMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -54;
      this.pRelY = -96;
      this.pW = 172;
      this.pH = 192;
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int gx, int gy) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShopStyle.panel(g, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, this.pH);
      ShopStyle.titleBar(g, this.font, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, "WEAPON SHOP");

      for (int sy : SLOT_Y) {
         ShopStyle.slot(g, this.leftPos + -30, this.topPos + sy);
      }

      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      g.drawString(this.font, ReturnShopSwords1Procedure.execute(this.entity), -6, SLOT_Y[0] + 4, -1509633, false);
      g.drawString(this.font, ReturnShopSwords2Procedure.execute(this.entity), -6, SLOT_Y[1] + 4, -1509633, false);
      g.drawString(this.font, ReturnShopSwords3Procedure.execute(this.entity), -6, SLOT_Y[2] + 4, -1509633, false);
      g.drawString(this.font, ReturnShopSwords4Procedure.execute(this.entity), -6, SLOT_Y[3] + 4, -1509633, false);
      g.drawString(this.font, ReturnShopSwords5Procedure.execute(this.entity), -6, SLOT_Y[4] + 4, -1509633, false);
      g.drawString(this.font, ReturnShopSwords6Procedure.execute(this.entity), -6, SLOT_Y[5] + 4, -1509633, false);
      ShopStyle.gold(g, this.font, this.entity, this.pRelX + 6, this.pRelY + this.pH - 12);
   }

   @Override
   protected void renderExtras(GuiGraphics g, int mouseX, int mouseY) {
      if (this.refreshButton != null && this.refreshButton.isMouseOver(mouseX, mouseY)) {
         g.renderTooltip(this.font, Component.literal(ReturnRefreshButtonProcedure.execute(this.entity)), mouseX, mouseY);
      }
   }

   @Override
   public void init() {
      super.init();

      for (int i = 0; i < 6; i++) {
         int id = i;
         PlainTextButton buy = new PlainTextButton(
            this.leftPos + -30 - 1,
            this.topPos + SLOT_Y[i] - 1,
            18,
            18,
            Component.literal(""),
            e -> SololevelingMod.PACKET_HANDLER.sendToServer(new ShopButtonMessage(id, this.x, this.y, this.z)),
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
            b -> SololevelingMod.PACKET_HANDLER.sendToServer(new ShopButtonMessage(6, this.x, this.y, this.z))
         )
      );
      this.refreshButton = new SystemScreen.SystemButton(
         this.leftPos + this.pRelX + this.pW - 51,
         this.topPos + this.pRelY + 2,
         48,
         12,
         Component.literal("Refresh"),
         b -> SololevelingMod.PACKET_HANDLER.sendToServer(new ShopButtonMessage(7, this.x, this.y, this.z))
      );
      this.addRenderableWidget(this.refreshButton);
   }
}
