package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.PartyScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemPanelScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemSettingsScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemTooltip;
import dev.eness.sololevelingfinal.core.network.EquippedAbilitiesButtonMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.ReturnAbilitySlotColorProcedure;
import dev.eness.sololevelingfinal.core.procedures.ReturnAbilitySlotProcedure;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;
import dev.eness.sololevelingfinal.core.util.JobSkillManager;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;
import dev.eness.sololevelingfinal.core.world.inventory.EquippedAbilitiesMenu;

public class EquippedAbilitiesScreen extends SystemContainerScreen<EquippedAbilitiesMenu> {
   private static final int ROWS = 8;
   private static final int ROW_H = 23;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private int abilityPage = 1;
   private SystemScreen.SystemButton pageButton;
   private final SystemScreen.SystemButton[] changeButtons = new SystemScreen.SystemButton[8];
   private final SystemScreen.SystemButton[] clearButtons = new SystemScreen.SystemButton[8];

   public EquippedAbilitiesScreen(EquippedAbilitiesMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.world = container.world;
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.abilityPage = container.initialPage;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -118;
      this.pRelY = -126;
      this.pW = 236;
      this.pH = 252;
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int gx, int gy) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShopStyle.panel(g, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, this.pH);
      int panelLeft = this.leftPos + this.pRelX;
      int panelTop = this.topPos + this.pRelY;
      if (SystemPlayerAccess.hasSystem(this.entity)) {
         ShopStyle.titleBar(g, this.font, panelLeft, panelTop, this.pW, "SKILL SLOTS");
      } else {
         g.fill(panelLeft, panelTop, panelLeft + this.pW, panelTop + 16, 1712335422);
         g.fill(panelLeft, panelTop + 16, panelLeft + this.pW, panelTop + 17, -12597505);
         int titleLeft = panelLeft + 44;
         int titleRight = panelLeft + this.pW - 109;
         String title = "SKILL SLOTS";
         g.drawString(this.font, title, titleLeft + (titleRight - titleLeft - this.font.width(title)) / 2, panelTop + 4, -12597505, false);
      }

      int startY = this.topPos + this.pRelY + 34;

      for (int i = 0; i < 8; i++) {
         int y0 = startY + i * 23;
         int fill = i % 2 == 0 ? 856695608 : 571482936;
         g.fill(this.leftPos + this.pRelX + 10, y0, this.leftPos + this.pRelX + this.pW - 10, y0 + 20, fill);
         g.fill(this.leftPos + this.pRelX + 10, y0 + 20, this.leftPos + this.pRelX + this.pW - 10, y0 + 21, 1430243071);
      }

      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
      int startY = this.pRelY + 39;
      String page = this.abilityPage == 1 ? "SLOTS 1-8" : "SLOTS 9-16";
      g.drawString(this.font, page, this.pRelX + 12, this.pRelY + 22, -12597505, false);
      SololevelingModVariables.PlayerVariables vars = this.entity
         .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());

      for (int i = 0; i < 8; i++) {
         int slot = this.displaySlot(i + 1);
         int rowY = startY + i * 23;
         int color = ReturnAbilitySlotColorProcedure.execute(this.entity, slot);
         String rawSkill = SkillSlotHelper.getSlot(vars, slot);
         boolean whiteFlame = JobSkillManager.isWhiteFlameSkill(rawSkill);
         String skill = ReturnAbilitySlotProcedure.execute(this.entity, slot);
         if (skill.length() > 22) {
            skill = skill.substring(0, 21) + "...";
         }

         g.drawString(this.font, slot < 10 ? "0" + slot : String.valueOf(slot), this.pRelX + 16, rowY, -7358248, false);
         Component skillText = Component.literal(skill);
         if (whiteFlame) {
            skillText = skillText.copy().withStyle(ChatFormatting.BOLD);
         }

         g.drawString(this.font, skillText, this.pRelX + 44, rowY, whiteFlame ? -1 : color, false);
      }
   }

   @Override
   protected void renderExtras(GuiGraphics g, int mouseX, int mouseY) {
      for (int i = 0; i < 8; i++) {
         int slot = this.displaySlot(i + 1);
         int y0 = this.topPos + this.pRelY + 34 + i * 23;
         if (mouseX >= this.leftPos + this.pRelX + 10 && mouseX < this.leftPos + this.pRelX + this.pW - 10 && mouseY >= y0 && mouseY < y0 + 20) {
            SystemTooltip.render(
               g,
               this.font,
               List.of(Component.literal("Slot " + slot), Component.literal(ReturnAbilitySlotProcedure.execute(this.entity, slot))),
               mouseX,
               mouseY,
               this.width,
               this.height
            );
            return;
         }
      }
   }

   @Override
   public void init() {
      super.init();
      boolean systemPlayer = SystemPlayerAccess.hasSystem(this.entity);
      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.leftPos + this.pRelX + 3,
            this.topPos + this.pRelY + 2,
            systemPlayer ? 42 : 38,
            12,
            Component.literal(systemPlayer ? "< Back" : "Close"),
            b -> {
               if (systemPlayer && this.minecraft != null && this.minecraft.player != null) {
                  this.minecraft.player.closeContainer();
                  this.openSystemScreen(new SystemPanelScreen());
               } else {
                  this.beginClose();
               }
            }
         )
      );
      if (!systemPlayer) {
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               this.leftPos + this.pRelX + this.pW - 106, this.topPos + this.pRelY + 2, 45, 12, Component.literal("Party"), b -> this.openNonSystemParty()
            )
         );
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               this.leftPos + this.pRelX + this.pW - 58, this.topPos + this.pRelY + 2, 55, 12, Component.literal("Settings"), b -> this.openNonSystemSettings()
            )
         );
      }

      this.pageButton = new SystemScreen.SystemButton(
         this.leftPos + this.pRelX + this.pW - 72, this.topPos + this.pRelY + 20, 60, 16, Component.literal("Page 2"), b -> this.togglePage()
      );
      this.addRenderableWidget(this.pageButton);
      int startY = this.topPos + this.pRelY + 35;

      for (int i = 0; i < 8; i++) {
         int row = i + 1;
         int by = startY + i * 23;
         this.changeButtons[i] = new SystemScreen.SystemButton(
            this.leftPos + this.pRelX + this.pW - 91, by, 48, 18, Component.literal("Change"), b -> this.sendButton(this.equipButtonId(row))
         );
         this.clearButtons[i] = new SystemScreen.SystemButton(
            this.leftPos + this.pRelX + this.pW - 39, by, 28, 18, Component.literal("X"), b -> this.sendButton(this.removeButtonId(row))
         );
         this.addRenderableWidget(this.changeButtons[i]);
         this.addRenderableWidget(this.clearButtons[i]);
      }

      this.refreshPageButton();
   }

   @Override
   protected boolean allowsNonSystemAccess() {
      return true;
   }

   private void openNonSystemSettings() {
      if (this.minecraft != null && this.minecraft.player != null) {
         this.minecraft.player.closeContainer();
         this.minecraft.setScreen(new SystemSettingsScreen(true));
      }
   }

   private void openNonSystemParty() {
      if (this.minecraft != null && this.minecraft.player != null) {
         this.minecraft.player.closeContainer();
         this.minecraft.setScreen(new PartyScreen(true));
      }
   }

   private int displaySlot(int row) {
      return this.abilityPage == 2 ? row + 8 : row;
   }

   private int equipButtonId(int row) {
      return this.abilityPage == 2 ? row + 15 : row - 1;
   }

   private int removeButtonId(int row) {
      return this.abilityPage == 2 ? row + 23 : row + 7;
   }

   private void togglePage() {
      this.abilityPage = this.abilityPage == 1 ? 2 : 1;
      this.refreshPageButton();
   }

   private void refreshPageButton() {
      if (this.pageButton != null) {
         this.pageButton.setMessage(Component.literal(this.abilityPage == 1 ? "Page 2" : "Page 1"));
      }
   }

   private void sendButton(int buttonId) {
      SololevelingMod.PACKET_HANDLER.sendToServer(new EquippedAbilitiesButtonMessage(buttonId, this.x, this.y, this.z));
   }
}
