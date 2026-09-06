package dev.eness.sololevelingfinal.core.client.gui.system;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.AbilitiesGUIButtonMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.network.SystemSettingsButtonMessage;
import dev.eness.sololevelingfinal.core.util.SystemClientConfig;

public class SystemSettingsScreen extends SystemScreen {
   private static final int CONTENT_TOP = 67;
   private static final int ROW_STEP = 46;
   private static final int TAB_GAP = 3;
   private static final int TAB_WIDTH = 59;
   private final boolean returnToSkills;
   private SystemSettingsScreen.Category category = SystemSettingsScreen.Category.GENERAL;
   private SystemScreen.SystemSlider popupPositionSlider;
   private SystemScreen.SystemSlider popupLifetimeSlider;

   public SystemSettingsScreen() {
      this(false);
   }

   public SystemSettingsScreen(boolean returnToSkills) {
      super(Component.literal("SETTINGS"));
      this.returnToSkills = returnToSkills;
      this.panelW = 270;
      this.panelH = 322;
   }

   @Override
   protected void init() {
      super.init();
      this.positionPanel();
      this.buildWidgets();
   }

   @Override
   protected void rebuildWidgets() {
      this.clearWidgets();
      this.positionPanel();
      this.buildWidgets();
   }

   private void positionPanel() {
      this.panelX = (this.width - this.panelW) / 2;
      this.panelY = (this.height - this.panelH) / 2;
      if (this.category == SystemSettingsScreen.Category.POPUPS) {
         int available = Math.max(0, (this.width - this.panelW) / 2 - 8);
         this.panelX = this.panelX + Math.min(82, available);
      }
   }

   private void buildWidgets() {
      this.addRenderableWidget(new SystemScreen.SystemButton(this.panelX + 3, this.panelY + 3, 40, 12, Component.literal("< Back"), button -> {
         if (this.returnToSkills) {
            this.openSkills();
         } else {
            this.openChild(new SystemPanelScreen());
         }
      }));
      int tabsX = this.panelX + 10;

      for (SystemSettingsScreen.Category value : SystemSettingsScreen.Category.values()) {
         SystemSettingsScreen.Category selected = value;
         String label = value == this.category ? "[" + value.label + "]" : value.label;
         this.addRenderableWidget(
            new SystemScreen.SystemButton(
               tabsX + value.ordinal() * 62, this.panelY + 25, 59, 18, Component.literal(label), button -> this.selectCategory(selected)
            )
         );
      }

      switch (this.category) {
         case GENERAL:
            this.buildGeneralWidgets();
            break;
         case POPUPS:
            this.buildPopupWidgets();
            break;
         case HIGHLIGHTS:
            this.buildHighlightWidgets();
            break;
         case OVERLAY:
            this.buildOverlayWidgets();
      }
   }

   private void selectCategory(SystemSettingsScreen.Category selected) {
      if (selected != this.category) {
         this.category = selected;
         SystemGuiSounds.switchInsideSystem();
         this.rebuildWidgets();
      }
   }

   private void buildGeneralWidgets() {
      int buttonX = this.panelX + this.panelW - 76;
      int y = this.contentY(0);
      this.addRenderableWidget(new SystemScreen.SystemButton(buttonX, y, 62, 18, Component.literal("Toggle"), button -> this.abilityAction(3)));
      y = this.contentY(1);
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.panelX + this.panelW - 78, y, 30, 18, Component.literal("-"), button -> this.abilityAction(1))
      );
      this.addRenderableWidget(
         new SystemScreen.SystemButton(this.panelX + this.panelW - 44, y, 30, 18, Component.literal("+"), button -> this.abilityAction(2))
      );
      y = this.contentY(2);
      this.addRenderableWidget(
         new SystemScreen.SystemButton(buttonX, y, 62, 18, Component.literal("Toggle"), button -> SystemClientConfig.toggleDamageNumbers())
      );
      if (hasSystemPlayer()) {
         y = this.contentY(3);
         this.addRenderableWidget(new SystemScreen.SystemButton(buttonX, y, 62, 18, Component.literal("Toggle"), button -> this.settingToggle(2)));
      }
   }

   private void buildPopupWidgets() {
      int buttonX = this.panelX + this.panelW - 76;
      this.addRenderableWidget(new SystemScreen.SystemButton(buttonX, this.contentY(0), 62, 18, Component.literal("Toggle"), button -> {
         SystemClientConfig.toggleDynamicNotifications();
         this.previewNotification();
         this.rebuildWidgets();
      }));
      float minScale = 0.5F;
      float maxScale = 2.0F;
      double scaleValue = normalize(SystemClientConfig.getNotificationScale(), minScale, maxScale);
      this.addRenderableWidget(
         new SystemScreen.SystemSlider(
            this.panelX + 16,
            this.panelY + 119,
            this.panelW - 32,
            16,
            scaleValue,
            value -> Component.literal("§bSize: " + Math.round((minScale + value * (maxScale - minScale)) * 100.0) + "%"),
            value -> SystemClientConfig.setNotificationScale((float)(minScale + value * (maxScale - minScale))),
            this::previewNotification
         )
      );
      float minPosition = -1.4F;
      float maxPosition = 1.4F;
      double positionValue = normalize(SystemClientConfig.getNotificationHorizontalOffset(), minPosition, maxPosition);
      this.popupPositionSlider = this.addRenderableWidget(
         new SystemScreen.SystemSlider(
            this.panelX + 16,
            this.panelY + 169,
            this.panelW - 32,
            16,
            positionValue,
            value -> Component.literal("§b" + positionLabel((float)(minPosition + value * (maxPosition - minPosition)))),
            value -> SystemClientConfig.setNotificationHorizontalOffset((float)(minPosition + value * (maxPosition - minPosition))),
            this::previewNotification
         )
      );
      float minLifetime = 1.0F;
      float maxLifetime = 10.0F;
      double lifetimeValue = normalize(SystemClientConfig.getNotificationLifetimeSeconds(), minLifetime, maxLifetime);
      this.popupLifetimeSlider = this.addRenderableWidget(
         new SystemScreen.SystemSlider(
            this.panelX + 16,
            this.panelY + 219,
            this.panelW - 32,
            16,
            lifetimeValue,
            value -> Component.literal("§bLifetime: " + String.format(Locale.ROOT, "%.1fs", minLifetime + value * (maxLifetime - minLifetime))),
            value -> SystemClientConfig.setNotificationLifetimeSeconds((float)(minLifetime + value * (maxLifetime - minLifetime))),
            this::previewNotification
         )
      );
      boolean manual = !SystemClientConfig.isDynamicNotificationsEnabled();
      this.popupPositionSlider.active = manual;
      this.popupLifetimeSlider.active = manual;
   }

   private void buildHighlightWidgets() {
      int buttonX = this.panelX + this.panelW - 76;
      this.addRenderableWidget(
         new SystemScreen.SystemButton(buttonX, this.contentY(0), 62, 18, Component.literal("Toggle"), button -> SystemClientConfig.toggleEntityOutlines())
      );
      this.addRenderableWidget(
         new SystemScreen.SystemButton(buttonX, this.contentY(1), 62, 18, Component.literal("Change"), button -> SystemClientConfig.cycleOutlineDensity())
      );
      this.addRenderableWidget(
         new SystemScreen.SystemButton(buttonX, this.contentY(2), 62, 18, Component.literal("Toggle"), button -> SystemClientConfig.togglePerceptionOutlines())
      );
      this.addRenderableWidget(
         new SystemScreen.SystemButton(buttonX, this.contentY(3), 62, 18, Component.literal("Toggle"), button -> SystemClientConfig.toggleEncounterOutlines())
      );
   }

   private void buildOverlayWidgets() {
      int buttonX = this.panelX + this.panelW - 76;
      this.addRenderableWidget(new SystemScreen.SystemButton(buttonX, this.contentY(0), 62, 18, Component.literal("Toggle"), button -> this.settingToggle(1)));
      this.addRenderableWidget(
         new SystemScreen.SystemButton(buttonX, this.contentY(1), 62, 18, Component.literal("Toggle"), button -> SystemClientConfig.toggleLegacyOverlay())
      );
   }

   @Override
   protected boolean allowsNonSystemAccess() {
      return true;
   }

   private void openSkills() {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         BlockPos position = player.blockPosition();
         if (this.minecraft != null) {
            this.minecraft.setScreen(null);
         }

         SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(5, position.getX(), position.getY(), position.getZ()));
      }
   }

   private void settingToggle(int id) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         BlockPos position = player.blockPosition();
         SololevelingMod.PACKET_HANDLER.sendToServer(new SystemSettingsButtonMessage(id, position.getX(), position.getY(), position.getZ()));
      }
   }

   private void abilityAction(int id) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         BlockPos position = player.blockPosition();
         SololevelingMod.PACKET_HANDLER.sendToServer(new AbilitiesGUIButtonMessage(id, position.getX(), position.getY(), position.getZ()));
      }
   }

   private void previewNotification() {
      SystemNotificationManager.INSTANCE.push(-12597505, 80, Component.literal("§eSYSTEM PREVIEW"), Component.literal("§7Position · size · lifetime"));
   }

   @Override
   protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      Player entity = Minecraft.getInstance().player;
      if (entity != null) {
         SololevelingModVariables.PlayerVariables variables = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         switch (this.category) {
            case GENERAL:
               this.renderGeneral(graphics, variables);
               break;
            case POPUPS:
               this.renderPopups(graphics);
               break;
            case HIGHLIGHTS:
               this.renderHighlights(graphics);
               break;
            case OVERLAY:
               this.renderOverlay(graphics, variables);
         }
      }
   }

   private void renderGeneral(GuiGraphics graphics, SololevelingModVariables.PlayerVariables variables) {
      this.drawRow(graphics, 0, "Triple Jump", onOff(variables.tjonoff));
      this.drawRow(graphics, 1, "Move Speed", "§b" + (int)Math.round(variables.speedpercent) + "%");
      this.drawRow(graphics, 2, "Damage Numbers", onOff(SystemClientConfig.isDamageNumbersEnabled()));
      if (variables.Player) {
         this.drawRow(graphics, 3, "PvP Urgent Quests", onOff(variables.pvpUrgentQuests));
      } else {
         this.drawLockedNote(graphics, "Awaken the System for Player-only settings.");
      }
   }

   private void renderPopups(GuiGraphics graphics) {
      boolean dynamic = SystemClientConfig.isDynamicNotificationsEnabled();
      this.drawRow(graphics, 0, "Dynamic Layout", onOff(dynamic));
      graphics.drawString(this.font, "Popup Size", this.panelX + 16, this.panelY + 106, -1509633, false);
      graphics.drawString(
         this.font,
         dynamic ? "Horizontal Position (automatic)" : "Horizontal Position",
         this.panelX + 16,
         this.panelY + 156,
         dynamic ? -10454902 : -1509633,
         false
      );
      graphics.drawString(this.font, dynamic ? "Lifetime (automatic)" : "Lifetime", this.panelX + 16, this.panelY + 206, dynamic ? -10454902 : -1509633, false);
      String note = dynamic
         ? "Dynamic mode keeps wide popups clear of the crosshair and times them by text length."
         : "Drag a slider, then release it to spawn a live preview on the left.";
      this.drawWrapped(graphics, note, this.panelX + 16, this.panelY + 248, this.panelW - 32, dynamic ? -8874582 : -7358248);
   }

   private void renderHighlights(GuiGraphics graphics) {
      this.drawRow(graphics, 0, "Target Highlights", onOff(SystemClientConfig.isEntityOutlinesEnabled()));
      this.drawRow(graphics, 1, "Highlight Density", "§b[" + SystemClientConfig.getOutlineDensityLabel() + "]");
      this.drawRow(graphics, 2, "Sense & Skills", onOff(SystemClientConfig.isPerceptionOutlinesEnabled()));
      this.drawRow(graphics, 3, "Dungeon Finder", onOff(SystemClientConfig.isEncounterOutlinesEnabled()));
   }

   private void renderOverlay(GuiGraphics graphics, SololevelingModVariables.PlayerVariables variables) {
      this.drawRow(graphics, 0, "Custom System Overlay", onOff(variables.CustomHUD));
      this.drawRow(graphics, 1, "Legacy Overlay", onOff(SystemClientConfig.isLegacyOverlayEnabled()));
      this.drawWrapped(
         graphics,
         "Choose the modern custom HUD or retain the original resource-based overlay.",
         this.panelX + 16,
         this.panelY + 174,
         this.panelW - 32,
         -7358248
      );
   }

   @Override
   protected List<Component> getHoverTooltip(int mouseX, int mouseY) {
      if (mouseX >= this.panelX + 12 && mouseX <= this.panelX + this.panelW - 12) {
         int row = (mouseY - (this.panelY + 67)) / 46;
         if (mouseY < this.panelY + 67) {
            return super.getHoverTooltip(mouseX, mouseY);
         } else if (this.category == SystemSettingsScreen.Category.POPUPS && row == 0) {
            return List.of(Component.literal("Automatically offsets each popup by its rendered width and chooses a readable lifetime."));
         } else if (this.category == SystemSettingsScreen.Category.HIGHLIGHTS && row == 0) {
            return List.of(Component.literal("Master switch for all private target highlights."));
         } else if (this.category == SystemSettingsScreen.Category.HIGHLIGHTS && row == 1) {
            return List.of(Component.literal("Minimal favors essentials; Balanced prioritizes hidden and distant targets; High shows more."));
         } else if (this.category == SystemSettingsScreen.Category.HIGHLIGHTS && row == 2) {
            return List.of(Component.literal("Perception, Detection Eye, Hyper Focus, and other skill highlights."));
         } else if (this.category == SystemSettingsScreen.Category.HIGHLIGHTS && row == 3) {
            return List.of(Component.literal("Dungeon, Red Gate, and Demon King's Castle encounter highlights."));
         } else if (this.category == SystemSettingsScreen.Category.OVERLAY && row == 0) {
            return List.of(Component.literal("Toggle the modern custom System HUD."));
         } else {
            return this.category == SystemSettingsScreen.Category.OVERLAY && row == 1
               ? List.of(Component.literal("Toggle the original legacy HUD presentation."))
               : super.getHoverTooltip(mouseX, mouseY);
         }
      } else {
         return super.getHoverTooltip(mouseX, mouseY);
      }
   }

   private int contentY(int row) {
      return this.panelY + 67 + row * 46;
   }

   private void drawRow(GuiGraphics graphics, int row, String label, String state) {
      int y = this.contentY(row);
      graphics.drawString(this.font, label, this.panelX + 16, y, -1509633, false);
      graphics.drawString(this.font, state, this.panelX + 16, y + 12, -7358248, false);
      graphics.fill(this.panelX + 12, y + 29, this.panelX + this.panelW - 12, y + 30, 1430243071);
   }

   private void drawLockedNote(GuiGraphics graphics, String text) {
      this.drawWrapped(graphics, text, this.panelX + 16, this.panelY + 220, this.panelW - 32, -10454902);
   }

   private void drawWrapped(GuiGraphics graphics, String text, int x, int y, int width, int color) {
      int line = 0;

      for (FormattedCharSequence sequence : this.font.split(Component.literal(text), width)) {
         graphics.drawString(this.font, sequence, x, y + line * 11, color, false);
         line++;
      }
   }

   private static boolean hasSystemPlayer() {
      Player player = Minecraft.getInstance().player;
      return player != null
         && player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(variables -> variables.Player).orElse(false);
   }

   private static String onOff(boolean on) {
      return on ? "§a[ON]" : "§c[OFF]";
   }

   private static double normalize(float value, float minimum, float maximum) {
      return Math.max(0.0, Math.min(1.0, (double)(value - minimum) / (maximum - minimum)));
   }

   private static String positionLabel(float offset) {
      if (Math.abs(offset) < 0.04F) {
         return "Position: Original";
      }

      int percent = Math.round(Math.abs(offset) / 1.4F * 100.0F);
      return "Position: " + (offset > 0.0F ? "Left " : "Right ") + percent + "%";
   }

   private enum Category {
      GENERAL("General"),
      POPUPS("Popups"),
      HIGHLIGHTS("Highlights"),
      OVERLAY("Overlay");

      private final String label;

      Category(String label) {
         this.label = label;
      }
   }
}
