package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.renderer.shader.ShadowSummonBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.init.SololevelingModSounds;
import dev.eness.sololevelingfinal.core.network.ShadowSummonGUIButtonMessage;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowSummonGUIMenu;
import org.joml.Matrix4f;

public class ShadowSummonGUIScreen extends AbstractContainerScreen<ShadowSummonGUIMenu> {
   private static final HashMap<String, Object> guistate = ShadowSummonGUIMenu.guistate;
   private static final int PANEL_W = 500;
   private static final int PANEL_H = 292;
   private static final int ACCENT = -4760321;
   private static final int ACCENT_BLUE = -12334849;
   private static final int ACCENT_DIM = -9818728;
   private static final int TEXT_MAIN = -1250305;
   private static final int TEXT_SUB = -6510635;
   private static final int NORMAL_X = 18;
   private static final int NORMAL_Y = 51;
   private static final int NORMAL_W = 248;
   private static final int NORMAL_H = 142;
   private static final int BOSS_X = 284;
   private static final int BOSS_Y = 51;
   private static final int BOSS_W = 198;
   private static final int BOSS_H = 142;
   private static final int BUTTON_H = 27;
   private static final int BUTTON_GAP = 8;
   private static final int BOSS_SUMMON_H = 40;
   private static final int BOSS_CUSTOMIZE_H = 15;
   private static final int BOSS_CONTROL_GAP = 2;
   private static final int BOSS_ROW_H = 57;
   private static final int BOSS_ROW_GAP = 6;
   private static final long ANIM_MS = 190L;
   private static final float OPEN_SOUND_PITCH = 0.78F;
   private static final float CLOSE_SOUND_PITCH = 0.72F;
   private static final float SOUND_VOLUME = 0.46F;
   private final Level world;
   private final int x;
   private final int y;
   private final int z;
   private final Player entity;
   private final List<ShadowSummonGUIScreen.SummonEntry> normalEntries = new ArrayList<>();
   private final List<ShadowSummonGUIScreen.SummonEntry> bossEntries = new ArrayList<>();
   private final List<ShadowSummonGUIScreen.SummonButton> summonButtons = new ArrayList<>();
   private final List<ShadowSummonGUIScreen.CustomizeButton> customizeButtons = new ArrayList<>();
   private final List<ShadowSummonGUIScreen.GrandMarshalButton> grandMarshalButtons = new ArrayList<>();
   private ShadowSummonGUIScreen.ControlButton formationModeButton;
   private ShadowSummonGUIScreen.ControlButton saveFormationButton;
   private ShadowSummonGUIScreen.ControlButton healBossShadowsButton;
   private ShadowSummonGUIScreen.ControlButton healAllShadowsButton;
   private ShadowSummonGUIScreen.ControlButton dismissButton;
   private EditBox formationNameBox;
   private int normalScroll;
   private int bossScroll;
   private boolean formationMode = false;
   private ShadowSummonGUIScreen.State state = ShadowSummonGUIScreen.State.OPENING;
   private long animStart;
   private boolean closed;
   private float reveal;
   private int shownBossHealingCost = Integer.MIN_VALUE;
   private int shownAllHealingCost = Integer.MIN_VALUE;

   public ShadowSummonGUIScreen(ShadowSummonGUIMenu container, Inventory inventory, Component text) {
      super(container, inventory, text);
      this.world = container.world;
      this.x = container.x;
      this.y = container.y;
      this.z = container.z;
      this.entity = container.entity;
      this.imageWidth = 500;
      this.imageHeight = 292;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.updateAnimation();
      if (!this.closed) {
         ResponsiveGuiScale.Transform transform = this.responsiveTransform();
         int logicalMouseX = transform.logicalMouseX(mouseX);
         int logicalMouseY = transform.logicalMouseY(mouseY);
         this.renderBackground(guiGraphics);
         this.layoutSummonButtons();
         guiGraphics.flush();
         ResponsiveGuiScale.push(guiGraphics, transform);
         int centerY = this.topPos + this.imageHeight / 2;
         int halfH = Math.round((this.imageHeight / 2.0F + 4.0F) * this.reveal);
         int top = centerY - halfH;
         int bottom = centerY + halfH;
         int sx0 = this.leftPos - 3;
         int sx1 = this.leftPos + this.imageWidth + 3;
         ResponsiveGuiScale.enableScissor(guiGraphics, transform, sx0, top, sx1, bottom);
         super.render(guiGraphics, logicalMouseX, logicalMouseY, partialTicks);
         guiGraphics.disableScissor();
         if (this.reveal < 1.0F) {
            guiGraphics.fill(sx0, top, sx1, top + 1, -4760321);
            guiGraphics.fill(sx0, bottom - 1, sx1, bottom, -4760321);
            guiGraphics.fill(sx0, top + 1, sx1, top + 2, 2008505599);
            guiGraphics.fill(sx0, bottom - 2, sx1, bottom - 1, 2000931071);
         }

         ResponsiveGuiScale.pop(guiGraphics);
         if (this.state == ShadowSummonGUIScreen.State.OPEN) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
         }
      }
   }

   private ResponsiveGuiScale.Transform responsiveTransform() {
      return ResponsiveGuiScale.fit(this.width, this.height, 508, 300);
   }

   private double logicalMouseX(double mouseX) {
      return this.responsiveTransform().logicalX(mouseX);
   }

   private double logicalMouseY(double mouseY) {
      return this.responsiveTransform().logicalY(mouseY);
   }

   @Override
   public void containerTick() {
      super.containerTick();
      this.layoutSummonButtons();
      this.updateHealingButtons();
   }

   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      this.renderAnimatedBackground(guiGraphics, mouseX, mouseY);
      this.renderFrame(guiGraphics);
      this.renderSections(guiGraphics);
      RenderSystem.disableBlend();
   }

   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.drawString(this.font, "NORMAL UNITS", 18, 37, -12334849, false);
      guiGraphics.drawString(this.font, "BOSS SHADOWS", 286, 37, -4760321, false);
      guiGraphics.drawString(this.font, "FORMATION", 18, 207, -12334849, false);
      guiGraphics.drawString(this.font, this.formationMode ? "Name current layout." : "Toggle to save current layout.", 108, 207, -6510635, false);
      guiGraphics.drawString(this.font, "MANAGE", 18, 260, -12334849, false);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (this.state != ShadowSummonGUIScreen.State.OPEN) {
         return true;
      } else {
         double logicalMouseX = this.logicalMouseX(mouseX);
         double logicalMouseY = this.logicalMouseY(mouseY);
         if (isOverBox(logicalMouseX, logicalMouseY, this.leftPos + 18, this.topPos + 51, 248, 142)) {
            int max = this.maxScroll(this.visibleEntries(this.normalEntries).size(), this.visibleNormalRows(), true);
            this.normalScroll = clamp(this.normalScroll - (int)Math.signum(delta), 0, max);
            this.layoutSummonButtons();
            return true;
         } else if (isOverBox(logicalMouseX, logicalMouseY, this.leftPos + 284, this.topPos + 51, 198, 142)) {
            int max = this.maxScroll(this.visibleEntries(this.bossEntries).size(), this.visibleBossRows(), false);
            this.bossScroll = clamp(this.bossScroll - (int)Math.signum(delta), 0, max);
            this.layoutSummonButtons();
            return true;
         } else {
            return super.mouseScrolled(logicalMouseX, logicalMouseY, delta);
         }
      }
   }

   @Override
   public boolean keyPressed(int key, int scanCode, int modifiers) {
      if (this.formationNameBox != null && this.formationNameBox.visible && this.formationNameBox.isFocused()) {
         if (key == 256) {
            this.beginClose();
            return true;
         } else {
            return this.formationNameBox.keyPressed(key, scanCode, modifiers);
         }
      } else {
         if (key != 256 && (this.minecraft == null || !this.minecraft.options.keyInventory.matches(key, scanCode))) {
            return super.keyPressed(key, scanCode, modifiers);
         }

         this.beginClose();
         return true;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.state != ShadowSummonGUIScreen.State.OPEN ? true : super.mouseClicked(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), button);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      float scale = this.responsiveTransform().scale();
      return super.mouseDragged(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY), button, dragX / scale, dragY / scale);
   }

   @Override
   public void mouseMoved(double mouseX, double mouseY) {
      super.mouseMoved(this.logicalMouseX(mouseX), this.logicalMouseY(mouseY));
   }

   @Override
   public boolean charTyped(char codePoint, int modifiers) {
      if (this.state != ShadowSummonGUIScreen.State.OPEN) {
         return true;
      } else {
         return this.formationNameBox != null && this.formationNameBox.visible && this.formationNameBox.isFocused()
            ? this.formationNameBox.charTyped(codePoint, modifiers)
            : super.charTyped(codePoint, modifiers);
      }
   }

   @Override
   public void init() {
      super.init();
      this.state = ShadowSummonGUIScreen.State.OPENING;
      this.animStart = Util.getMillis();
      this.closed = false;
      this.reveal = 0.0F;
      this.playPanelSound(SololevelingModSounds.PANELOPEN.get(), 0.78F);
      this.normalEntries.clear();
      this.bossEntries.clear();
      this.summonButtons.clear();
      this.customizeButtons.clear();
      this.grandMarshalButtons.clear();
      this.shownBossHealingCost = Integer.MIN_VALUE;
      this.shownAllHealingCost = Integer.MIN_VALUE;
      this.addEntries();

      for (ShadowSummonGUIScreen.SummonEntry entry : this.normalEntries) {
         this.addSummonButton(entry, false);
      }

      for (ShadowSummonGUIScreen.SummonEntry entry : this.bossEntries) {
         this.addSummonButton(entry, true);
         this.addCustomizeButton(entry);
         if (ShadowMonarchManager.isGrandMarshalType(entry.type)) {
            this.addGrandMarshalButton(entry);
         }
      }

      this.formationModeButton = new ShadowSummonGUIScreen.ControlButton(
         this.leftPos + 18, this.topPos + 226, 104, 20, Component.literal("Formation: OFF"), b -> this.toggleFormationMode()
      );
      guistate.put("button:button_formation_mode", this.formationModeButton);
      this.addRenderableWidget(this.formationModeButton);
      this.formationNameBox = new EditBox(this.font, this.leftPos + 132, this.topPos + 227, 246, 18, Component.literal("Formation Name"));
      this.formationNameBox.setMaxLength(24);
      this.formationNameBox.setValue("Formation");
      this.formationNameBox.setTextColor(-1250305);
      this.formationNameBox.setBordered(false);
      this.formationNameBox.visible = false;
      this.addRenderableWidget(this.formationNameBox);
      this.saveFormationButton = new ShadowSummonGUIScreen.ControlButton(
         this.leftPos + 386, this.topPos + 226, 96, 20, Component.literal("Save Formation"), b -> this.saveFormation()
      );
      this.saveFormationButton.visible = false;
      guistate.put("button:button_save_formation", this.saveFormationButton);
      this.addRenderableWidget(this.saveFormationButton);
      this.healBossShadowsButton = new ShadowSummonGUIScreen.ControlButton(
         this.leftPos + 72, this.topPos + 258, 150, 20, Component.literal("Heal Bosses"), b -> this.healShadows(true)
      );
      guistate.put("button:button_heal_boss_shadows", this.healBossShadowsButton);
      this.addRenderableWidget(this.healBossShadowsButton);
      this.healAllShadowsButton = new ShadowSummonGUIScreen.ControlButton(
         this.leftPos + 228, this.topPos + 258, 150, 20, Component.literal("Heal All"), b -> this.healShadows(false)
      );
      guistate.put("button:button_heal_all_shadows", this.healAllShadowsButton);
      this.addRenderableWidget(this.healAllShadowsButton);
      this.dismissButton = new ShadowSummonGUIScreen.ControlButton(
         this.leftPos + 386, this.topPos + 258, 96, 20, Component.literal("Dismiss"), b -> this.openDismiss()
      );
      guistate.put("button:button_shadow_dismiss", this.dismissButton);
      this.addRenderableWidget(this.dismissButton);
      this.updateHealingButtons();
      this.layoutSummonButtons();
   }

   @Override
   public void onClose() {
      this.beginClose();
   }

   private void beginClose() {
      if (this.state != ShadowSummonGUIScreen.State.CLOSING && !this.closed) {
         this.state = ShadowSummonGUIScreen.State.CLOSING;
         this.animStart = Util.getMillis();
         this.playPanelSound(SololevelingModSounds.PANELCLOSE.get(), 0.72F);
      }
   }

   private void updateAnimation() {
      float raw = Math.min(1.0F, (float)(Util.getMillis() - this.animStart) / 190.0F);
      float eased = raw * raw * (3.0F - 2.0F * raw);
      switch (this.state) {
         case OPENING:
            this.reveal = eased;
            if (raw >= 1.0F) {
               this.state = ShadowSummonGUIScreen.State.OPEN;
               this.reveal = 1.0F;
            }
            break;
         case OPEN:
            this.reveal = 1.0F;
            break;
         case CLOSING:
            this.reveal = 1.0F - eased;
            if (raw >= 1.0F && !this.closed) {
               this.closed = true;
               if (this.minecraft != null && this.minecraft.player != null) {
                  this.minecraft.player.closeContainer();
               }
            }
      }
   }

   private void playPanelSound(SoundEvent sound, float pitch) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getSoundManager() != null) {
         mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, 0.46F));
      }
   }

   private void addEntries() {
      this.normalEntries.add(new ShadowSummonGUIScreen.SummonEntry(0, "Goblin Fighter", "goblin_club"));
      this.normalEntries.add(new ShadowSummonGUIScreen.SummonEntry(1, "Goblin Archer", "goblin_archer"));
      this.normalEntries.add(new ShadowSummonGUIScreen.SummonEntry(2, "Goblin Mage", "goblin_mage"));
      this.normalEntries.add(new ShadowSummonGUIScreen.SummonEntry(3, "Lycan", "wolf"));
      this.normalEntries.add(new ShadowSummonGUIScreen.SummonEntry(4, "Knight", "knight"));
      this.normalEntries.add(new ShadowSummonGUIScreen.SummonEntry(5, "Polar Bear", "polar_bear"));
      this.normalEntries.add(new ShadowSummonGUIScreen.SummonEntry(6, "Orc", "orc"));
      this.normalEntries.add(new ShadowSummonGUIScreen.SummonEntry(10, "High Orc", "high_orc"));
      this.bossEntries.add(new ShadowSummonGUIScreen.SummonEntry(7, "Igris", "igris"));
      this.bossEntries.add(new ShadowSummonGUIScreen.SummonEntry(8, "Beru", "beru"));
      this.bossEntries.add(new ShadowSummonGUIScreen.SummonEntry(9, "Kamish", "kamish"));
      this.bossEntries.add(new ShadowSummonGUIScreen.SummonEntry(11, "Tusk", "tusk"));
      this.bossEntries.add(new ShadowSummonGUIScreen.SummonEntry(12, "Kaisel", "kaisel"));
      this.bossEntries.add(new ShadowSummonGUIScreen.SummonEntry(13, "Iron", "iron"));
   }

   private void addSummonButton(ShadowSummonGUIScreen.SummonEntry entry, boolean boss) {
      ShadowSummonGUIScreen.SummonButton button = new ShadowSummonGUIScreen.SummonButton(
         0, 0, boss ? 186 : 118, boss ? 40 : 27, entry, boss, b -> this.summon(entry)
      );
      button.visible = false;
      this.summonButtons.add(button);
      guistate.put("button:shadow_summon_" + entry.id, button);
      this.addRenderableWidget(button);
   }

   private void addCustomizeButton(ShadowSummonGUIScreen.SummonEntry entry) {
      ShadowSummonGUIScreen.CustomizeButton button = new ShadowSummonGUIScreen.CustomizeButton(0, 0, 186, 15, entry, b -> this.customize(entry));
      button.visible = false;
      this.customizeButtons.add(button);
      guistate.put("button:shadow_customize_" + entry.id, button);
      this.addRenderableWidget(button);
   }

   private void addGrandMarshalButton(ShadowSummonGUIScreen.SummonEntry entry) {
      ShadowSummonGUIScreen.GrandMarshalButton button = new ShadowSummonGUIScreen.GrandMarshalButton(0, 0, 186, 15, entry, b -> this.appointGrandMarshal(entry));
      button.visible = false;
      this.grandMarshalButtons.add(button);
      guistate.put("button:shadow_grand_marshal_" + entry.id, button);
      this.addRenderableWidget(button);
   }

   private void layoutSummonButtons() {
      List<ShadowSummonGUIScreen.SummonEntry> visibleNormals = this.visibleEntries(this.normalEntries);
      List<ShadowSummonGUIScreen.SummonEntry> visibleBosses = this.visibleEntries(this.bossEntries);
      this.normalScroll = clamp(this.normalScroll, 0, this.maxScroll(visibleNormals.size(), this.visibleNormalRows(), true));
      this.bossScroll = clamp(this.bossScroll, 0, this.maxScroll(visibleBosses.size(), this.visibleBossRows(), false));

      for (ShadowSummonGUIScreen.SummonButton button : this.summonButtons) {
         button.visible = false;
         button.active = false;
      }

      for (ShadowSummonGUIScreen.CustomizeButton button : this.customizeButtons) {
         button.visible = false;
         button.active = false;
      }

      for (ShadowSummonGUIScreen.GrandMarshalButton button : this.grandMarshalButtons) {
         button.visible = false;
         button.active = false;
      }

      this.layoutNormalButtons(visibleNormals);
      this.layoutBossButtons(visibleBosses);
   }

   private void layoutNormalButtons(List<ShadowSummonGUIScreen.SummonEntry> entries) {
      int start = this.normalScroll * 2;
      int end = Math.min(entries.size(), start + this.visibleNormalRows() * 2);

      for (int i = start; i < end; i++) {
         ShadowSummonGUIScreen.SummonButton button = this.buttonFor(entries.get(i));
         if (button != null) {
            int local = i - start;
            int col = local % 2;
            int row = local / 2;
            int bx = this.leftPos + 18 + 6 + col * 124;
            int by = this.topPos + 51 + row * 35;
            button.setPosition(bx, by);
            button.setClip(this.leftPos + 18, this.topPos + 51, 248, 142);
            button.visible = true;
            button.active = true;
         }
      }
   }

   private void layoutBossButtons(List<ShadowSummonGUIScreen.SummonEntry> entries) {
      int start = this.bossScroll;
      int end = Math.min(entries.size(), start + this.visibleBossRows());

      for (int i = start; i < end; i++) {
         ShadowSummonGUIScreen.SummonButton button = this.buttonFor(entries.get(i));
         if (button != null) {
            int row = i - start;
            int rowY = this.topPos + 51 + row * 63;
            button.setPosition(this.leftPos + 284 + 6, rowY);
            button.setClip(this.leftPos + 284, this.topPos + 51, 198, 142);
            button.visible = true;
            button.active = true;
            ShadowSummonGUIScreen.CustomizeButton customize = this.customizeButtonFor(entries.get(i));
            ShadowSummonGUIScreen.GrandMarshalButton grandMarshal = this.grandMarshalButtonFor(entries.get(i));
            int controlY = rowY + 40 + 2;
            int controlWidth = 186;
            boolean customizable = this.menu.isCustomizable(entries.get(i).id);
            if (customize != null && customizable) {
               int splitWidth = grandMarshal == null ? controlWidth : (controlWidth - 3) / 2;
               customize.setWidth(splitWidth);
               customize.setPosition(this.leftPos + 284 + 6, controlY);
               customize.setClip(this.leftPos + 284, this.topPos + 51, 198, 142);
               customize.visible = true;
               customize.active = true;
               if (grandMarshal != null) {
                  grandMarshal.setWidth(controlWidth - splitWidth - 3);
                  grandMarshal.setPosition(this.leftPos + 284 + 6 + splitWidth + 3, controlY);
               }
            } else if (grandMarshal != null) {
               grandMarshal.setWidth(controlWidth);
               grandMarshal.setPosition(this.leftPos + 284 + 6, controlY);
            }

            if (grandMarshal != null) {
               grandMarshal.setClip(this.leftPos + 284, this.topPos + 51, 198, 142);
               grandMarshal.visible = true;
               grandMarshal.active = this.menu.isGrandMarshalEligible(entries.get(i).id) && !this.menu.isGrandMarshalActive(entries.get(i).id);
            }
         }
      }
   }

   private ShadowSummonGUIScreen.SummonButton buttonFor(ShadowSummonGUIScreen.SummonEntry entry) {
      for (ShadowSummonGUIScreen.SummonButton button : this.summonButtons) {
         if (button.entry == entry) {
            return button;
         }
      }

      return null;
   }

   private ShadowSummonGUIScreen.CustomizeButton customizeButtonFor(ShadowSummonGUIScreen.SummonEntry entry) {
      for (ShadowSummonGUIScreen.CustomizeButton button : this.customizeButtons) {
         if (button.entry == entry) {
            return button;
         }
      }

      return null;
   }

   private ShadowSummonGUIScreen.GrandMarshalButton grandMarshalButtonFor(ShadowSummonGUIScreen.SummonEntry entry) {
      for (ShadowSummonGUIScreen.GrandMarshalButton button : this.grandMarshalButtons) {
         if (button.entry == entry) {
            return button;
         }
      }

      return null;
   }

   private List<ShadowSummonGUIScreen.SummonEntry> visibleEntries(List<ShadowSummonGUIScreen.SummonEntry> entries) {
      List<ShadowSummonGUIScreen.SummonEntry> visible = new ArrayList<>();

      for (ShadowSummonGUIScreen.SummonEntry entry : entries) {
         if (this.menu.hasShadow(entry.id)) {
            visible.add(entry);
         }
      }

      visible.sort((first, second) -> {
         int byRank = Integer.compare(this.menu.shadowRank(second.id), this.menu.shadowRank(first.id));
         if (byRank != 0) {
            return byRank;
         }

         int byLevel = Integer.compare(this.menu.shadowLevel(second.id), this.menu.shadowLevel(first.id));
         return byLevel != 0 ? byLevel : first.name.compareToIgnoreCase(second.name);
      });
      return visible;
   }

   private int visibleNormalRows() {
      return Math.max(1, 4);
   }

   private int visibleBossRows() {
      return Math.max(1, 2);
   }

   private int maxScroll(int entryCount, int visibleRows, boolean twoColumns) {
      int rows = twoColumns ? (int)Math.ceil(entryCount / 2.0) : entryCount;
      return Math.max(0, rows - visibleRows);
   }

   private void summon(ShadowSummonGUIScreen.SummonEntry entry) {
      if (this.menu.hasShadow(entry.id)) {
         String payload = hasShiftDown() ? "all" : "";
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowSummonGUIButtonMessage(entry.id, this.x, this.y, this.z, payload));
      }
   }

   private void customize(ShadowSummonGUIScreen.SummonEntry entry) {
      if (this.menu.isCustomizable(entry.id)) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowSummonGUIButtonMessage(200 + entry.id, this.x, this.y, this.z));
      }
   }

   private void appointGrandMarshal(ShadowSummonGUIScreen.SummonEntry entry) {
      if (this.menu.isGrandMarshalEligible(entry.id) && !this.menu.isGrandMarshalActive(entry.id)) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowSummonGUIButtonMessage(300 + entry.id, this.x, this.y, this.z));
      }
   }

   private void toggleFormationMode() {
      this.formationMode = !this.formationMode;
      this.formationModeButton.setMessage(Component.literal(this.formationMode ? "Formation: ON" : "Formation: OFF"));
      this.formationNameBox.visible = this.formationMode;
      this.formationNameBox.setFocused(this.formationMode);
      this.saveFormationButton.visible = this.formationMode;
      if (this.formationMode) {
         this.setInitialFocus(this.formationNameBox);
      }
   }

   private void saveFormation() {
      if (this.formationMode) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowSummonGUIButtonMessage(100, this.x, this.y, this.z, this.formationNameBox.getValue()));
         ShadowSummonGUIButtonMessage.handleButtonAction(this.entity, 100, this.x, this.y, this.z, this.formationNameBox.getValue());
      }
   }

   private void openDismiss() {
      SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowSummonGUIButtonMessage(101, this.x, this.y, this.z));
      ShadowSummonGUIButtonMessage.handleButtonAction(this.entity, 101, this.x, this.y, this.z);
   }

   private void healShadows(boolean bossesOnly) {
      int cost = bossesOnly ? this.menu.bossHealingManaCost() : this.menu.allHealingManaCost();
      if (cost > 0) {
         int buttonId = bossesOnly ? 102 : 103;
         SololevelingMod.PACKET_HANDLER.sendToServer(new ShadowSummonGUIButtonMessage(buttonId, this.x, this.y, this.z));
      }
   }

   private void updateHealingButtons() {
      if (this.healBossShadowsButton != null && this.healAllShadowsButton != null) {
         int bossCost = this.menu.bossHealingManaCost();
         int allCost = this.menu.allHealingManaCost();
         this.healBossShadowsButton.active = bossCost > 0;
         this.healAllShadowsButton.active = allCost > 0;
         if (bossCost != this.shownBossHealingCost) {
            this.shownBossHealingCost = bossCost;
            this.healBossShadowsButton
               .setTooltip(
                  Tooltip.create(
                     Component.literal(
                        bossCost > 0
                           ? "Fully restore all summoned boss shadows. Mana cost: " + bossCost + " MP (1 MP per 4 health)."
                           : "No summoned boss shadows need healing. Mana cost: 0 MP."
                     )
                  )
               );
         }

         if (allCost != this.shownAllHealingCost) {
            this.shownAllHealingCost = allCost;
            this.healAllShadowsButton
               .setTooltip(
                  Tooltip.create(
                     Component.literal(
                        allCost > 0
                           ? "Fully restore every summoned shadow. Mana cost: " + allCost + " MP (1 MP per 4 health)."
                           : "No summoned shadows need healing. Mana cost: 0 MP."
                     )
                  )
               );
         }
      }
   }

   private void renderFrame(GuiGraphics g) {
      int x = this.leftPos;
      int y = this.topPos;
      int w = this.imageWidth;
      int h = this.imageHeight;
      g.fill(x - 1, y - 1, x + w + 1, y, -2008823553);
      g.fill(x - 1, y + h, x + w + 1, y + h + 1, -2008823553);
      g.fill(x - 1, y, x, y + h, -2008823553);
      g.fill(x + w, y, x + w + 1, y + h, -2008823553);
      outline(g, x, y, w, h, -9818728);
      g.fill(x, y, x + w, y + 22, 2048133409);
      g.fill(x, y + 22, x + w, y + 23, -4760321);
      this.drawCornerBrackets(g, x, y, w, h);
      String title = "[ SHADOW SUMMON ]";
      g.drawString(this.font, title, x + (w - this.font.width(title)) / 2, y + 7, -4760321, false);
   }

   private void renderSections(GuiGraphics g) {
      int x = this.leftPos;
      int y = this.topPos;
      outline(g, x + 12, y + 32, 254, 170, 2000931071);
      outline(g, x + 284 - 6, y + 32, 210, 170, -2001249025);
      outline(g, x + 12, y + 218, this.imageWidth - 24, 32, 2000931071);
      outline(g, x + 12, y + 254, this.imageWidth - 24, 26, 2000931071);
      g.fill(x + 13, y + 33, x + 265, y + 47, 856836982);
      g.fill(x + 284 - 5, y + 33, x + 284 + 198 + 5, y + 47, 857541425);
      g.fill(x + 13, y + 219, x + this.imageWidth - 13, y + 225, 856836982);
      g.fill(x + 13, y + 255, x + this.imageWidth - 13, y + 261, 856836982);
      this.drawScrollHint(g, x + 256, y + 52, y + 191, this.visibleEntries(this.normalEntries).size(), this.visibleNormalRows(), this.normalScroll, true);
      this.drawScrollHint(g, x + 284 + 198 - 5, y + 52, y + 191, this.visibleEntries(this.bossEntries).size(), this.visibleBossRows(), this.bossScroll, false);
      if (this.formationMode) {
         outline(g, x + 129, y + 224, 252, 24, -9818728);
         g.fill(x + 130, y + 225, x + 380, y + 247, 1427120952);
      }
   }

   private void drawScrollHint(GuiGraphics g, int x, int y0, int y1, int count, int rows, int scroll, boolean twoColumns) {
      int rowCount = twoColumns ? (int)Math.ceil(count / 2.0) : count;
      if (rowCount > rows) {
         g.fill(x, y0, x + 2, y1, 1145030399);
         int trackH = y1 - y0;
         int knobH = Math.max(16, trackH * rows / rowCount);
         int knobY = y0 + (trackH - knobH) * scroll / Math.max(1, rowCount - rows);
         g.fill(x, knobY, x + 2, knobY + knobH, twoColumns ? -12334849 : -4760321);
      }
   }

   private void renderAnimatedBackground(GuiGraphics g, int mouseX, int mouseY) {
      float localX = clamp01((float)(mouseX - this.leftPos) / this.imageWidth);
      float localY = clamp01((float)(mouseY - this.topPos) / this.imageHeight);
      ShaderInstance shader = ShadowSummonBackgroundRenderTypes.get();
      if (shader == null) {
         this.renderFallbackBackground(g, localX, localY);
      } else {
         RenderSystem.setShader(ShadowSummonBackgroundRenderTypes::get);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableCull();
         AbstractUniform mouse = shader.safeGetUniform("MousePos");
         mouse.set(localX, localY);
         Matrix4f matrix = g.pose().last().pose();
         BufferBuilder buffer = Tesselator.getInstance().getBuilder();
         buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         buffer.vertex(matrix, this.leftPos, this.topPos + this.imageHeight, 0.0F).uv(0.0F, 1.0F).endVertex();
         buffer.vertex(matrix, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0.0F).uv(1.0F, 1.0F).endVertex();
         buffer.vertex(matrix, this.leftPos + this.imageWidth, this.topPos, 0.0F).uv(1.0F, 0.0F).endVertex();
         buffer.vertex(matrix, this.leftPos, this.topPos, 0.0F).uv(0.0F, 0.0F).endVertex();
         Tesselator.getInstance().end();
         RenderSystem.enableCull();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void renderFallbackBackground(GuiGraphics g, float localX, float localY) {
      int x = this.leftPos;
      int y = this.topPos;
      g.fillGradient(x, y, x + this.imageWidth, y + this.imageHeight, -268040430, -267122915);
      float t = (float)(Util.getMillis() % 100000L) / 1000.0F;
      int mx = x + (int)(localX * this.imageWidth);
      int my = y + (int)(localY * this.imageHeight);

      for (int i = 0; i < 14; i++) {
         int lineY = my - 38 + i * 6;
         int shift = (int)(Math.sin(t * 12.0F + i * 1.7F) * 10.0);
         int alpha = 35 + (int)(25.0 * (0.5 + 0.5 * Math.sin(t * 8.0F + i)));
         g.fill(mx - 54 + shift, lineY, mx + 54 + shift, lineY + 1, alpha << 24 | 12016895);
         if (i % 3 == 0) {
            g.fill(mx - 44 - shift, lineY + 2, mx + 42 - shift, lineY + 3, alpha << 24 | 4442367);
         }
      }

      g.fillGradient(x, y, x + this.imageWidth, y + this.imageHeight, 0, 1711276032);
   }

   private static boolean isOverBox(double mouseX, double mouseY, int x, int y, int w, int h) {
      return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
   }

   private static void outline(GuiGraphics g, int x, int y, int w, int h, int color) {
      g.fill(x, y, x + w, y + 1, color);
      g.fill(x, y + h - 1, x + w, y + h, color);
      g.fill(x, y, x + 1, y + h, color);
      g.fill(x + w - 1, y, x + w, y + h, color);
   }

   private void drawCornerBrackets(GuiGraphics g, int x, int y, int w, int h) {
      int len = 15;
      g.fill(x - 1, y - 1, x + len, y + 1, -12334849);
      g.fill(x - 1, y - 1, x + 1, y + len, -12334849);
      g.fill(x + w - len, y - 1, x + w + 1, y + 1, -4760321);
      g.fill(x + w - 1, y - 1, x + w + 1, y + len, -4760321);
      g.fill(x - 1, y + h - 1, x + len, y + h + 1, -12334849);
      g.fill(x - 1, y + h - len, x + 1, y + h + 1, -12334849);
      g.fill(x + w - len, y + h - 1, x + w + 1, y + h + 1, -4760321);
      g.fill(x + w - 1, y + h - len, x + w + 1, y + h + 1, -4760321);
   }

   private static class ControlButton extends Button {
      ControlButton(int x, int y, int w, int h, Component label, OnPress onPress) {
         super(x, y, w, h, label, onPress, DEFAULT_NARRATION);
      }

      @Override
      protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
         boolean hovered = this.active && this.isHoveredOrFocused();
         int border = !this.active ? -11051406 : (hovered ? -1 : -12334849);
         g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, !this.active ? 1142956072 : (hovered ? -2008823553 : 1427120952));
         ShadowSummonGUIScreen.outline(g, this.getX(), this.getY(), this.width, this.height, border);
         Font font = Minecraft.getInstance().font;
         g.drawCenteredString(
            font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, !this.active ? -9209462 : (hovered ? -1 : -1250305)
         );
      }
   }

   private class CustomizeButton extends Button {
      private final ShadowSummonGUIScreen.SummonEntry entry;
      private int clipX;
      private int clipY;
      private int clipW;
      private int clipH;

      CustomizeButton(int x, int y, int w, int h, ShadowSummonGUIScreen.SummonEntry entry, OnPress onPress) {
         super(x, y, w, h, Component.literal("CUSTOMIZE"), onPress, DEFAULT_NARRATION);
         this.entry = entry;
      }

      void setClip(int x, int y, int w, int h) {
         this.clipX = x;
         this.clipY = y;
         this.clipW = w;
         this.clipH = h;
      }

      @Override
      protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            ResponsiveGuiScale.Transform transform = ResponsiveGuiScale.fit(
               Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 508, 300
            );
            ResponsiveGuiScale.enableScissor(g, transform, this.clipX, this.clipY, this.clipX + this.clipW, this.clipY + this.clipH);
            boolean equipped = ShadowSummonGUIScreen.this.menu.isEquipped(this.entry.id);
            boolean hovered = this.isHoveredOrFocused();
            int border = hovered ? -1 : (equipped ? -14262 : -4760321);
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, hovered ? -1723196056 : (equipped ? 1429874960 : 1427120952));
            ShadowSummonGUIScreen.outline(g, this.getX(), this.getY(), this.width, this.height, border);
            Font font = Minecraft.getInstance().font;
            String label = equipped ? (this.width < 130 ? "GEAR | EQUIPPED" : "CUSTOMIZE  |  EQUIPPED") : "CUSTOMIZE";
            g.drawCenteredString(font, label, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, hovered ? -1 : (equipped ? -10374 : -1250305));
            g.disableScissor();
         }
      }
   }

   private class GrandMarshalButton extends Button {
      private final ShadowSummonGUIScreen.SummonEntry entry;
      private int clipX;
      private int clipY;
      private int clipW;
      private int clipH;

      GrandMarshalButton(int x, int y, int w, int h, ShadowSummonGUIScreen.SummonEntry entry, OnPress onPress) {
         super(x, y, w, h, Component.literal("GRAND MARSHAL"), onPress, DEFAULT_NARRATION);
         this.entry = entry;
      }

      void setClip(int x, int y, int w, int h) {
         this.clipX = x;
         this.clipY = y;
         this.clipW = w;
         this.clipH = h;
      }

      @Override
      protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            ResponsiveGuiScale.Transform transform = ResponsiveGuiScale.fit(
               Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 508, 300
            );
            ResponsiveGuiScale.enableScissor(g, transform, this.clipX, this.clipY, this.clipX + this.clipW, this.clipY + this.clipH);
            boolean assigned = ShadowSummonGUIScreen.this.menu.isGrandMarshalActive(this.entry.id);
            boolean eligible = ShadowSummonGUIScreen.this.menu.isGrandMarshalEligible(this.entry.id);
            boolean hovered = this.isHoveredOrFocused() && this.active;
            int border = assigned ? -14262 : (eligible ? -4760321 : -10792339);
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, assigned ? 1717322506 : (hovered ? -1723196056 : 1427120952));
            ShadowSummonGUIScreen.outline(g, this.getX(), this.getY(), this.width, this.height, hovered ? -1 : border);
            String label;
            if (assigned) {
               label = this.width < 130 ? "GM | ASSIGNED" : "GRAND MARSHAL | ASSIGNED";
            } else if (eligible) {
               label = this.width < 130 ? "PROMOTE GM" : "PROMOTE TO GRAND MARSHAL";
            } else {
               int required = ShadowMonarchManager.grandMarshalRequiredLevel(this.entry.type);
               label = this.width < 130 ? "GM AT LV." + required : "MARSHAL + LV." + required + " REQUIRED";
            }

            Font font = Minecraft.getInstance().font;
            g.drawCenteredString(
               font, label, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, assigned ? -10374 : (eligible ? -1250305 : -8226159)
            );
            g.disableScissor();
         }
      }
   }

   private enum State {
      OPENING,
      OPEN,
      CLOSING;
   }

   private class SummonButton extends Button {
      private final ShadowSummonGUIScreen.SummonEntry entry;
      private final boolean boss;
      private int clipX;
      private int clipY;
      private int clipW;
      private int clipH;

      SummonButton(int x, int y, int w, int h, ShadowSummonGUIScreen.SummonEntry entry, boolean boss, OnPress onPress) {
         super(x, y, w, h, Component.literal(entry.name), onPress, DEFAULT_NARRATION);
         this.entry = entry;
         this.boss = boss;
      }

      void setClip(int x, int y, int w, int h) {
         this.clipX = x;
         this.clipY = y;
         this.clipW = w;
         this.clipH = h;
      }

      @Override
      protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            ResponsiveGuiScale.Transform transform = ResponsiveGuiScale.fit(
               Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 508, 300
            );
            ResponsiveGuiScale.enableScissor(g, transform, this.clipX, this.clipY, this.clipX + this.clipW, this.clipY + this.clipH);
            boolean hovered = this.isHoveredOrFocused();
            int border = hovered ? -1 : (this.boss ? -4760321 : -12334849);
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, hovered ? -2008408728 : 1427120952);
            ShadowSummonGUIScreen.outline(g, this.getX(), this.getY(), this.width, this.height, border);
            Font font = Minecraft.getInstance().font;
            Player player = Minecraft.getInstance().player;
            String count = player == null ? "0/0" : ShadowSummonGUIScreen.this.menu.shadowCountText(this.entry.id);
            int countWidth = font.width(count);
            String fittedName = this.trimToWidth(font, this.entry.name, Math.max(12, this.width - countWidth - 20));
            g.drawString(font, fittedName, this.getX() + 7, this.getY() + 4, -1250305, false);
            g.drawString(font, count, this.getX() + this.width - countWidth - 7, this.getY() + 4, this.boss ? -2382081 : -6365185, false);
            int rank = ShadowSummonGUIScreen.this.menu.shadowRank(this.entry.id);
            String rankName = ShadowMonarchManager.rankDisplayName(rank);
            if (this.boss) {
               this.renderBossProgress(g, font, rank, rankName);
            } else {
               g.drawCenteredString(font, rankName, this.getX() + this.width / 2, this.getY() + 15, ShadowMonarchManager.rankColor(rank));
            }

            g.disableScissor();
         }
      }

      private void renderBossProgress(GuiGraphics g, Font font, int rank, String rankName) {
         int level = ShadowSummonGUIScreen.this.menu.shadowLevel(this.entry.id);
         String levelText = "Lv." + level;
         String rankContext;
         if (ShadowSummonGUIScreen.this.menu.isGrandMarshalActive(this.entry.id)) {
            rankContext = rankName + "  |  ASSIGNED";
         } else if (ShadowSummonGUIScreen.this.menu.isGrandMarshalEligible(this.entry.id)) {
            rankContext = rankName + "  |  READY";
         } else if (ShadowSummonGUIScreen.this.menu.isMaxRank(this.entry.id)) {
            rankContext = rankName + "  |  MAX";
         } else {
            String next = ShadowMonarchManager.rankDisplayName(ShadowSummonGUIScreen.this.menu.nextRank(this.entry.id));
            rankContext = rankName + " -> " + next;
         }

         int contextWidth = Math.max(18, this.width - font.width(levelText) - 22);
         rankContext = this.trimToWidth(font, rankContext, contextWidth);
         g.drawString(font, levelText, this.getX() + 7, this.getY() + 14, ShadowMonarchManager.rankColor(rank), false);
         g.drawString(font, rankContext, this.getX() + this.width - font.width(rankContext) - 7, this.getY() + 14, ShadowMonarchManager.rankColor(rank), false);
         int barX = this.getX() + 7;
         int barY = this.getY() + 25;
         int barW = this.width - 14;
         int xp = ShadowSummonGUIScreen.this.menu.rankXp(this.entry.id);
         int needed = Math.max(1, ShadowSummonGUIScreen.this.menu.rankXpNeeded(this.entry.id));
         boolean maxRank = ShadowSummonGUIScreen.this.menu.isMaxRank(this.entry.id);
         boolean promotionReady = ShadowSummonGUIScreen.this.menu.isGrandMarshalEligible(this.entry.id);
         int fill = !maxRank && !promotionReady ? (int)Math.round(barW * Math.min(1.0, (double)xp / needed)) : barW;
         g.fill(barX, barY, barX + barW, barY + 4, -871889648);
         if (fill > 0) {
            g.fill(barX, barY, barX + fill, barY + 4, !maxRank && !promotionReady ? -4760321 : -14262);
         }

         ShadowSummonGUIScreen.outline(g, barX, barY, barW, 4, !maxRank && !promotionReady ? -9818728 : -14262);
         String xpText;
         if (ShadowSummonGUIScreen.this.menu.isGrandMarshalActive(this.entry.id)) {
            xpText = "SIGNATURE: " + ShadowMonarchManager.grandMarshalSignatureName(this.entry.type);
         } else if (promotionReady) {
            xpText = "GRAND MARSHAL PROMOTION READY";
         } else if (maxRank) {
            xpText = "MAX RANK";
         } else if (ShadowSummonGUIScreen.this.menu.isAtLevelCap(this.entry.id)) {
            xpText = "LEVEL CAP  |  " + xp + " / " + needed + " XP";
         } else {
            xpText = xp + " / " + needed + " XP";
         }

         xpText = this.trimToWidth(font, xpText, this.width - 14);
         g.drawCenteredString(
            font,
            xpText,
            this.getX() + this.width / 2,
            this.getY() + 30,
            ShadowSummonGUIScreen.this.menu.isAtLevelCap(this.entry.id) && !maxRank ? -14244 : -6510635
         );
      }

      private String trimToWidth(Font font, String text, int maximumWidth) {
         if (font.width(text) <= maximumWidth) {
            return text;
         }

         String suffix = "...";
         int allowed = Math.max(0, maximumWidth - font.width(suffix));
         return font.plainSubstrByWidth(text, allowed) + suffix;
      }
   }

   private record SummonEntry(int id, String name, String type) {
   }
}
