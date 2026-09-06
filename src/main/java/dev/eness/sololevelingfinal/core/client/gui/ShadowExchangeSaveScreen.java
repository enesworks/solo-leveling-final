package dev.eness.sololevelingfinal.core.client.gui;

import java.util.HashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.ShadowExchangeSaveButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.DoesHaveGoblinArcherProcedure;
import dev.eness.sololevelingfinal.core.procedures.DoesHaveGoblinMageProcedure;
import dev.eness.sololevelingfinal.core.procedures.DoesHaveGoblinProcedure;
import dev.eness.sololevelingfinal.core.procedures.DoesHaveKnightsProcedure;
import dev.eness.sololevelingfinal.core.procedures.DoesHavePolarBearProcedure;
import dev.eness.sololevelingfinal.core.procedures.DoesHaveWolfProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeSaveMenu;

public class ShadowExchangeSaveScreen extends ShadowStyledScreen<ShadowExchangeSaveMenu> {
   private static final HashMap<String, Object> guistate = ShadowExchangeSaveMenu.guistate;
   private static final ShadowExchangeSaveScreen.SaveEntry[] ENTRIES = new ShadowExchangeSaveScreen.SaveEntry[]{
      new ShadowExchangeSaveScreen.SaveEntry(0, "Knight"),
      new ShadowExchangeSaveScreen.SaveEntry(1, "Goblin Fighter"),
      new ShadowExchangeSaveScreen.SaveEntry(2, "Goblin Archer"),
      new ShadowExchangeSaveScreen.SaveEntry(3, "Goblin Mage"),
      new ShadowExchangeSaveScreen.SaveEntry(4, "Lycan"),
      new ShadowExchangeSaveScreen.SaveEntry(5, "Polar Bear")
   };
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private final ShadowStyledScreen.ShadowButton[] buttons = new ShadowStyledScreen.ShadowButton[ENTRIES.length];

   public ShadowExchangeSaveScreen(ShadowExchangeSaveMenu container, Inventory inventory, Component text) {
      super(container, inventory, text, 360, 212);
      this.world = container.world;
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
   }

   @Override
   protected String shadowTitle() {
      return "SET EXCHANGE SHADOW";
   }

   @Override
   protected void initShadowWidgets() {
      for (int i = 0; i < ENTRIES.length; i++) {
         ShadowExchangeSaveScreen.SaveEntry entry = ENTRIES[i];
         ShadowStyledScreen.ShadowButton button = new ShadowStyledScreen.ShadowButton(
            0, 0, 150, 27, Component.literal(entry.label), entry.id % 2 == 1, b -> this.sendButton(entry.id)
         );
         this.buttons[i] = button;
         guistate.put("button:shadow_exchange_save_" + entry.id, button);
         this.addRenderableWidget(button);
      }

      this.layoutButtons();
   }

   @Override
   public void containerTick() {
      super.containerTick();
      this.layoutButtons();
   }

   @Override
   protected void renderShadowSections(GuiGraphics guiGraphics) {
      int x = this.leftPos;
      int y = this.topPos;
      outline(guiGraphics, x + 12, y + 32, 336, 160, 2000931071);
      guiGraphics.fill(x + 13, y + 33, x + 347, y + 47, 856836982);
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, "AVAILABLE SHADOWS", 18, 37, -12334849, false);
      if (this.visibleCount() == 0) {
         guiGraphics.drawCenteredString(this.font, Component.literal("No summonable shadows available."), this.imageWidth / 2, 105, -6510635);
      }
   }

   private void layoutButtons() {
      int visible = 0;

      for (int i = 0; i < ENTRIES.length; i++) {
         ShadowStyledScreen.ShadowButton button = this.buttons[i];
         if (button != null) {
            boolean show = this.hasShadow(ENTRIES[i].id);
            button.visible = show;
            button.active = show;
            if (show) {
               int col = visible % 2;
               int row = visible / 2;
               button.setPosition(this.leftPos + 24 + col * 162, this.topPos + 58 + row * 38);
               visible++;
            }
         }
      }
   }

   private int visibleCount() {
      int count = 0;

      for (ShadowExchangeSaveScreen.SaveEntry entry : ENTRIES) {
         if (this.hasShadow(entry.id)) {
            count++;
         }
      }

      return count;
   }

   private boolean hasShadow(int id) {
      return switch (id) {
         case 0 -> DoesHaveKnightsProcedure.execute(this.entity);
         case 1 -> DoesHaveGoblinProcedure.execute(this.entity);
         case 2 -> DoesHaveGoblinArcherProcedure.execute(this.entity);
         case 3 -> DoesHaveGoblinMageProcedure.execute(this.entity);
         case 4 -> DoesHaveWolfProcedure.execute(this.entity);
         case 5 -> DoesHavePolarBearProcedure.execute(this.entity);
         default -> false;
      };
   }

   private void sendButton(int buttonId) {
      if (this.hasShadow(buttonId)) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowExchangeSaveButtonMessage(buttonId, this.x, this.y, this.z));
         ShadowExchangeSaveButtonMessage.handleButtonAction(this.entity, buttonId, this.x, this.y, this.z);
      }
   }

   private record SaveEntry(int id, String label) {
   }
}
