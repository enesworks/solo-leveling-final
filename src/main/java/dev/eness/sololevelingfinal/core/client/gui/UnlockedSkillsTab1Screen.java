package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemContainerScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemScreen;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemTooltip;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.network.UnlockedSkillsTab1ButtonMessage;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;
import dev.eness.sololevelingfinal.core.util.JobSkillManager;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.SkillCategoryRegistry;
import dev.eness.sololevelingfinal.core.util.SkillListHelper;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab1Menu;

public class UnlockedSkillsTab1Screen extends SystemContainerScreen<UnlockedSkillsTab1Menu> {
   private static final int ROWS = 8;
   private static final int ROW_H = 23;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private final SystemScreen.SystemButton[] equipButtons = new SystemScreen.SystemButton[8];
   private final SystemScreen.SystemButton[] removeButtons = new SystemScreen.SystemButton[8];
   private SystemScreen.SystemButton previousButton;
   private SystemScreen.SystemButton nextButton;
   private int page = 0;

   public UnlockedSkillsTab1Screen(UnlockedSkillsTab1Menu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.world = container.world;
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 0;
      this.imageHeight = 0;
      this.pRelX = -124;
      this.pRelY = -126;
      this.pW = 248;
      this.pH = 252;
   }

   @Override
   protected void renderBg(GuiGraphics g, float partialTicks, int gx, int gy) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ShopStyle.panel(g, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, this.pH);
      ShopStyle.titleBar(g, this.font, this.leftPos + this.pRelX, this.topPos + this.pRelY, this.pW, "SKILL LIST");
      int startY = this.topPos + this.pRelY + 42;

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
      this.clampPage();
      SololevelingModVariables.PlayerVariables vars = this.entity
         .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      int selectedSlot = (int)vars.PslotSelecting;
      String current = selectedSlot > 0 ? SkillSlotHelper.getSlot(vars, selectedSlot) : "";
      String target = selectedSlot > 0 ? "TARGET SLOT " + selectedSlot : "TARGET SLOT";
      g.drawString(this.font, target, this.pRelX + 12, this.pRelY + 22, -12597505, false);
      String currentLabel = current != null && !current.isBlank() ? ShadowMonarchManager.displaySkillName(this.entity, current) : "Empty";
      Component currentText = current != null && !current.isBlank() ? SkillCategoryRegistry.decorate(current, currentLabel) : Component.literal(currentLabel);
      if (JobSkillManager.isWhiteFlameSkill(current)) {
         currentText = currentText.copy().withStyle(ChatFormatting.BOLD);
      }

      g.drawString(this.font, currentText, this.pRelX + 12, this.pRelY + 33, JobSkillManager.isWhiteFlameSkill(current) ? -1 : -7358248, false);
      int pageCount = SkillListHelper.pageCount(this.entity, 8);

      for (int i = 0; i < 8; i++) {
         int index = this.page * 8 + i + 1;
         String raw = SkillListHelper.rawSkillAt(this.entity, index);
         String label = SkillListHelper.displaySkillAt(this.entity, index);
         int color = SkillListHelper.colorAt(this.entity, index);
         int rowY = this.pRelY + 47 + i * 23;
         g.drawString(this.font, index < 10 ? "0" + index : String.valueOf(index), this.pRelX + 16, rowY, -7358248, false);
         int availableWidth = ShadowMonarchManager.isFormationSkill(raw) ? this.pW - 136 : this.pW - 104;
         Component skillText = "empty".equals(raw) ? Component.literal("-") : this.fitSkillText(raw, label, availableWidth);
         if (JobSkillManager.isWhiteFlameSkill(raw)) {
            skillText = skillText.copy().withStyle(ChatFormatting.BOLD);
         }

         g.drawString(
            this.font, skillText, this.pRelX + 44, rowY, "empty".equals(raw) ? -11113862 : (JobSkillManager.isWhiteFlameSkill(raw) ? -1 : color), false
         );
      }

      String pageText = "Page " + (this.page + 1) + "/" + pageCount;
      g.drawString(this.font, pageText, this.pRelX + (this.pW - this.font.width(pageText)) / 2, this.pRelY + this.pH - 19, -7358248, false);
   }

   @Override
   protected void renderExtras(GuiGraphics g, int mouseX, int mouseY) {
      for (int i = 0; i < 8; i++) {
         int index = this.page * 8 + i + 1;
         String raw = SkillListHelper.rawSkillAt(this.entity, index);
         if (!"empty".equals(raw)) {
            int y0 = this.topPos + this.pRelY + 42 + i * 23;
            if (mouseX >= this.leftPos + this.pRelX + 10 && mouseX < this.leftPos + this.pRelX + this.pW - 10 && mouseY >= y0 && mouseY < y0 + 20) {
               SystemTooltip.render(g, this.font, JobSkillManager.tooltip(this.entity, raw), mouseX, mouseY, this.width, this.height);
               return;
            }
         }
      }
   }

   @Override
   public void containerTick() {
      super.containerTick();
      this.refreshButtons();
   }

   @Override
   public void init() {
      super.init();
      this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.leftPos + this.pRelX + 3, this.topPos + this.pRelY + 2, 40, 12, Component.literal("< Back"), b -> this.openSlotScreen()
         )
      );
      int startY = this.topPos + this.pRelY + 43;

      for (int i = 0; i < 8; i++) {
         int row = i;
         int by = startY + i * 23;
         this.removeButtons[i] = new SystemScreen.SystemButton(
            this.leftPos + this.pRelX + this.pW - 88, by, 28, 18, Component.literal("X"), b -> this.removeFormation(row)
         );
         this.equipButtons[i] = new SystemScreen.SystemButton(
            this.leftPos + this.pRelX + this.pW - 56, by, 44, 18, Component.literal("Equip"), b -> this.equip(row)
         );
         this.addRenderableWidget(this.removeButtons[i]);
         this.addRenderableWidget(this.equipButtons[i]);
      }

      this.previousButton = new SystemScreen.SystemButton(
         this.leftPos + this.pRelX + 12, this.topPos + this.pRelY + this.pH - 27, 42, 18, Component.literal("<"), b -> {
            if (this.page > 0) {
               this.page--;
            }

            this.refreshButtons();
         }
      );
      this.nextButton = new SystemScreen.SystemButton(
         this.leftPos + this.pRelX + this.pW - 54, this.topPos + this.pRelY + this.pH - 27, 42, 18, Component.literal(">"), b -> {
            int maxPage = SkillListHelper.pageCount(this.entity, 8) - 1;
            if (this.page < maxPage) {
               this.page++;
            }

            this.refreshButtons();
         }
      );
      this.addRenderableWidget(this.previousButton);
      this.addRenderableWidget(this.nextButton);
      this.refreshButtons();
   }

   private Component fitSkillText(String rawSkill, String label, int maxWidth) {
      Component result = SkillCategoryRegistry.decorate(rawSkill, label);
      if (this.font.width(result) <= maxWidth) {
         return result;
      }

      String shortened = label;

      while (!shortened.isEmpty()) {
         shortened = shortened.substring(0, shortened.length() - 1);
         result = SkillCategoryRegistry.decorate(rawSkill, shortened + "...");
         if (this.font.width(result) <= maxWidth) {
            return result;
         }
      }

      return SkillCategoryRegistry.decorate(rawSkill, "...");
   }

   @Override
   protected boolean allowsNonSystemAccess() {
      return true;
   }

   private void openSlotScreen() {
      SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab1ButtonMessage(101, this.x, this.y, this.z, 0));
   }

   private void equip(int row) {
      int index = this.page * 8 + row + 1;
      if (!"empty".equals(SkillListHelper.rawSkillAt(this.entity, index))) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab1ButtonMessage(row, this.x, this.y, this.z, index));
      }
   }

   private void removeFormation(int row) {
      int index = this.page * 8 + row + 1;
      if (ShadowMonarchManager.isFormationSkill(SkillListHelper.rawSkillAt(this.entity, index))) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new UnlockedSkillsTab1ButtonMessage(100, this.x, this.y, this.z, index));
      }
   }

   private void refreshButtons() {
      this.clampPage();
      int pageCount = SkillListHelper.pageCount(this.entity, 8);
      if (this.previousButton != null) {
         this.previousButton.visible = this.isOpen() && this.page > 0;
      }

      if (this.nextButton != null) {
         this.nextButton.visible = this.isOpen() && this.page + 1 < pageCount;
      }

      for (int i = 0; i < 8; i++) {
         int index = this.page * 8 + i + 1;
         String raw = SkillListHelper.rawSkillAt(this.entity, index);
         boolean hasSkill = !"empty".equals(raw);
         boolean formation = ShadowMonarchManager.isFormationSkill(raw);
         if (this.equipButtons[i] != null) {
            this.equipButtons[i].visible = this.isOpen() && hasSkill;
            this.equipButtons[i].active = hasSkill;
         }

         if (this.removeButtons[i] != null) {
            this.removeButtons[i].visible = this.isOpen() && formation;
            this.removeButtons[i].active = formation;
         }
      }
   }

   private void clampPage() {
      int pageCount = SkillListHelper.pageCount(this.entity, 8);
      if (this.page >= pageCount) {
         this.page = pageCount - 1;
      }

      if (this.page < 0) {
         this.page = 0;
      }
   }
}
