package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemPanelScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.network.StoreGUIButtonMessage;
import dev.eness.sololevelingfinal.core.world.inventory.StoreGUIMenu;

public class StoreGUIScreen extends SystemContainerScreen<StoreGUIMenu> {
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;

   public StoreGUIScreen(StoreGUIMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -74;
      this.pRelY = -78;
      this.pW = 148;
      this.pH = 156;
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int gx, int gy) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShopStyle.panel(g, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, this.pH);
      ShopStyle.titleBar(g, this.font, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, "SHOP");
      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      ShopStyle.gold(g, this.font, this.entity, this.pRelX + 6, this.pRelY + this.pH - 12);
   }

   private void category(int id) {
      SololevelingMod.PACKET_HANDLER.sendToServer(new StoreGUIButtonMessage(id, this.x, this.y, this.z));
   }

   @Override
   public void init() {
      super.init();
      int bw = 116;
      int bh = 26;
      int bx = this.leftPos + this.pRelX + (this.pW - bw) / 2;
      this.addRenderableWidget(new SystemScreen.SystemButton(bx, this.topPos + this.pRelY + 30, bw, bh, Component.literal("Weapons"), b -> this.category(0)));
      this.addRenderableWidget(new SystemScreen.SystemButton(bx, this.topPos + this.pRelY + 64, bw, bh, Component.literal("Foods"), b -> this.category(1)));
      this.addRenderableWidget(new SystemScreen.SystemButton(bx, this.topPos + this.pRelY + 98, bw, bh, Component.literal("Potions"), b -> this.category(2)));
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.leftPos + this.pRelX + 3, this.topPos + this.pRelY + 2, 40, 12, Component.literal("< Back"), b -> {
            if (this.minecraft != null && this.minecraft.player != null) {
               this.minecraft.player.closeContainer();
               this.openSystemScreen(new SystemPanelScreen());
            }
         })
      );
   }
}
