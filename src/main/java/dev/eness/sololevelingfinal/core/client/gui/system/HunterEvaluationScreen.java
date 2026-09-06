package dev.eness.sololevelingfinal.core.client.gui.system;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.renderer.shader.HunterEvaluationBackgroundRenderTypes;
import dev.eness.sololevelingfinal.core.network.HunterEvaluationActionMessage;
import dev.eness.sololevelingfinal.core.util.HunterEvaluationRules;
import org.joml.Matrix4f;

public final class HunterEvaluationScreen extends SystemScreen {
   private static final int PANEL_W = 340;
   private static final int PANEL_H = 292;
   private static final int HEADER_H = 22;
   private static final int STEP_Y = 28;
   private static final int STEP_H = 13;
   private static final int CHAMBER_Y = 46;
   private static final int CHAMBER_H = 152;
   private static final int CHAMBER_HEADER_H = 16;
   private static final int GEM_CY = 130;
   private static final int RING_RADIUS = 52;
   private static final int GEM_HALF_W = 21;
   private static final int GEM_HALF_H = 35;
   private static final float GEM_TABLE = 0.46F;
   private static final float GEM_CROWN = 0.26F;
   private static final float GEM_BELT = 0.52F;
   private static final int READOUT_Y = 204;
   private static final int PROGRESS_Y = 236;
   private static final int DETAIL_Y = 245;
   private static final int BUTTON_Y = 259;
   private static final int INK = -16579058;
   private static final int STEEL = -14469302;
   private static final int TEXT_DIM = -9600363;
   private static final int TEXT_BODY = -6441529;
   private static final String[] STAGE_NAMES = new String[]{"CONTACT", "SCAN", "CLASS", "RANK", "RECORD"};
   private static final String[] RANK_LETTERS = new String[]{"E", "D", "C", "B", "A", "S"};
   private UUID sessionId;
   private HunterEvaluationRules.Mode mode;
   private HunterEvaluationRules.Phase phase;
   private int classId;
   private int rank;
   private int previousRank;
   private int phaseDurationTicks;
   private int remainingTicks;
   private boolean canReroll;
   private boolean fixedClass;
   private long stateReceivedAt;
   private long clickWaveAt;
   private long contactLostAt;
   private boolean holdingContact;
   private boolean actionPending;
   private float holdCharge;
   private boolean gemHovered;
   private SystemScreen.SystemButton rerollButton;
   private SystemScreen.SystemButton acceptButton;
   private SystemScreen.SystemButton doneButton;

   private HunterEvaluationScreen(
      UUID sessionId,
      int mode,
      int phase,
      int classId,
      int rank,
      int previousRank,
      int phaseDurationTicks,
      int remainingTicks,
      boolean canReroll,
      boolean fixedClass
   ) {
      super(Component.literal("HUNTER EVALUATION"));
      this.panelW = 340;
      this.panelH = 292;
      this.updateState(sessionId, mode, phase, classId, rank, previousRank, phaseDurationTicks, remainingTicks, canReroll, fixedClass);
   }

   @Override
   protected void init() {
      super.init();
      int buttonY = this.panelY + 259;
      this.rerollButton = this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + 34, buttonY, 132, 21, Component.literal("REROLL CLASS"), button -> this.send(HunterEvaluationRules.Action.REROLL_CLASS)
         )
      );
      this.acceptButton = this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + 174, buttonY, 132, 21, Component.literal("ACCEPT RESULT"), button -> this.send(HunterEvaluationRules.Action.ACCEPT_RESULT)
         )
      );
      this.doneButton = this.addRenderableWidget(
         new SystemScreen.SystemButton(
            this.panelX + 95, buttonY, 150, 21, Component.literal("CLOSE RECORD"), button -> this.send(HunterEvaluationRules.Action.ACKNOWLEDGE)
         )
      );
   }

   @Override
   protected boolean allowsNonSystemAccess() {
      return true;
   }

   @Override
   protected boolean shouldPlaySystemSounds() {
      return false;
   }

   @Override
   protected void renderAnimatedBackground(GuiGraphics graphics, int mouseX, int mouseY) {
      float localX = clamp01((float)(mouseX - this.panelX) / this.panelW);
      float localY = clamp01((float)(mouseY - this.panelY) / this.panelH);
      ShaderInstance shader = HunterEvaluationBackgroundRenderTypes.get();
      if (shader == null) {
         this.renderFallback(graphics);
      } else {
         int color = this.activeColor();
         float red = (color >> 16 & 0xFF) / 255.0F;
         float green = (color >> 8 & 0xFF) / 255.0F;
         float blue = (color & 0xFF) / 255.0F;
         float intensity = this.rank > 0 ? HunterEvaluationRules.rankIntensity(this.rank) : 0.34F;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShader(HunterEvaluationBackgroundRenderTypes::get);
         AbstractUniform mouse = shader.safeGetUniform("MousePos");
         mouse.set(localX, localY);
         shader.safeGetUniform("ClassColor").set(red, green, blue);
         shader.safeGetUniform("RankIntensity").set(intensity);
         shader.safeGetUniform("WaveStrength").set(this.waveStrength());
         shader.safeGetUniform("Reveal").set(this.revealAmount());
         shader.safeGetUniform("HoldCharge").set(this.holdCharge);
         shader.safeGetUniform("ScanSweep").set(this.scanSweep());
         int x0 = this.panelX;
         int y0 = this.panelY;
         int x1 = this.panelX + this.panelW;
         int y1 = this.panelY + this.panelH;
         Matrix4f matrix = graphics.pose().last().pose();
         BufferBuilder buffer = Tesselator.getInstance().getBuilder();
         buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         buffer.vertex(matrix, x0, y1, 0.0F).uv(0.0F, 1.0F).endVertex();
         buffer.vertex(matrix, x1, y1, 0.0F).uv(1.0F, 1.0F).endVertex();
         buffer.vertex(matrix, x1, y0, 0.0F).uv(1.0F, 0.0F).endVertex();
         buffer.vertex(matrix, x0, y0, 0.0F).uv(0.0F, 0.0F).endVertex();
         Tesselator.getInstance().end();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void renderFallback(GuiGraphics graphics) {
      int color = this.activeColor();
      graphics.fillGradient(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, mix(-16645109, color, 0.14F), -16711418);
      float time = (float)(Util.getMillis() % 600000L) / 1000.0F;

      for (int i = 0; i < 42; i++) {
         float seed = i * 17.913F;
         float x = frac((float)Math.sin(seed) * 43758.547F);
         float y = frac((float)Math.sin(seed * 1.71F) * 24634.635F);
         int px = this.panelX + (int)(x * this.panelW);
         int py = this.panelY + (int)((y * this.panelH + time * (4.0F + x * 11.0F)) % this.panelH);
         graphics.fill(px, py, px + 1, py + 1, withAlpha(color, 100));
      }
   }

   @Override
   protected void renderFrame(GuiGraphics graphics) {
      graphics.drawManaged(() -> this.renderFrameBody(graphics));
   }

   private void renderFrameBody(GuiGraphics graphics) {
      int color = this.activeColor();
      int soft = withAlpha(color, 85);
      int dim = mix(-15718856, color, 0.52F);
      graphics.fill(this.panelX - 2, this.panelY - 2, this.panelX + this.panelW + 2, this.panelY - 1, withAlpha(color, 34));
      graphics.fill(this.panelX - 2, this.panelY + this.panelH + 1, this.panelX + this.panelW + 2, this.panelY + this.panelH + 2, withAlpha(color, 34));
      graphics.fill(this.panelX - 1, this.panelY - 1, this.panelX + this.panelW + 1, this.panelY, soft);
      graphics.fill(this.panelX - 1, this.panelY + this.panelH, this.panelX + this.panelW + 1, this.panelY + this.panelH + 1, soft);
      graphics.fill(this.panelX - 1, this.panelY, this.panelX, this.panelY + this.panelH, soft);
      graphics.fill(this.panelX + this.panelW, this.panelY, this.panelX + this.panelW + 1, this.panelY + this.panelH, soft);
      this.outline(graphics, this.panelX, this.panelY, this.panelW, this.panelH, dim);
      graphics.fillGradient(
         this.panelX,
         this.panelY,
         this.panelX + this.panelW,
         this.panelY + 22,
         withAlpha(mix(-16644852, color, 0.22F), 230),
         withAlpha(mix(-16644852, color, 0.08F), 196)
      );
      graphics.fill(this.panelX, this.panelY + 7, this.panelX + this.panelW, this.panelY + 8, withAlpha(-1, 14));
      graphics.fill(this.panelX, this.panelY + 22 - 1, this.panelX + this.panelW, this.panelY + 22, color);
      this.drawCorners(graphics, color);
      String titleText = this.title.getString();
      int titleColor = this.rank == 6 ? -1 : color;
      int titleWidth = this.trackedWidth(titleText, 1);
      int titleX = this.panelX + (this.panelW - titleWidth) / 2;
      this.drawTracked(graphics, titleText, titleX + 1, this.panelY + 8, -872414456, 1);
      this.drawTracked(graphics, titleText, titleX, this.panelY + 7, titleColor, 1);
      drawDiamond(graphics, titleX - 9, this.panelY + 11, 3, withAlpha(color, 176));
      drawDiamond(graphics, titleX + titleWidth + 8, this.panelY + 11, 3, withAlpha(color, 176));
      String modeTag = this.mode == HunterEvaluationRules.Mode.REEVALUATION ? "RE-EVAL" : "INITIAL";
      graphics.drawString(this.font, modeTag, this.panelX + this.panelW - 8 - this.font.width(modeTag), this.panelY + 7, withAlpha(color, 153), false);
   }

   @Override
   protected void renderContent(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.updateButtons();
      this.advanceCharge();
      this.gemHovered = this.phase == HunterEvaluationRules.Phase.CONTACT && this.isInsideChamber(mouseX, mouseY);
      graphics.drawManaged(() -> this.renderContentBody(graphics));
   }

   private void renderContentBody(GuiGraphics graphics) {
      int color = this.activeColor();
      int centerX = this.panelX + this.panelW / 2;
      this.renderStepper(graphics, this.panelX + 18, this.panelY + 28, this.panelW - 36, color);
      this.renderChamber(graphics, centerX, color);
      this.renderReadout(graphics, color);
      this.renderProgress(graphics, centerX, color);
   }

   private void renderStepper(GuiGraphics graphics, int x, int y, int width, int color) {
      int current = stageOf(this.phase);
      int segments = STAGE_NAMES.length;

      for (int index = 0; index < segments; index++) {
         int x0 = x + width * index / segments;
         int x1 = x + width * (index + 1) / segments - 2;
         boolean done = index < current;
         boolean active = index == current;
         int fill = done ? withAlpha(color, 89) : (active ? withAlpha(color, 140) : 1711871009);
         graphics.fill(x0, y, x1, y + 13, fill);
         graphics.fill(x0, y, x1, y + 1, !done && !active ? -14469302 : color);
         if (active) {
            int alpha = 120 + (int)(110.0F * pulse01());
            graphics.fill(x0, y + 13 - 2, x1, y + 13, withAlpha(color, alpha));
         }

         String label = STAGE_NAMES[index];
         int labelColor = active ? -1 : (done ? -6441529 : -9600363);
         graphics.drawString(this.font, label, x0 + (x1 - x0 - this.font.width(label)) / 2, y + 3, labelColor, false);
      }
   }

   private void renderChamber(GuiGraphics graphics, int centerX, int color) {
      int x = this.panelX + 18;
      int y = this.panelY + 46;
      int width = this.panelW - 36;
      int gemCenterY = this.panelY + 130;
      graphics.fillGradient(x, y, x + width, y + 152, 1711408656, -1946090486);
      this.outline(graphics, x, y, width, 152, withAlpha(color, 122));
      graphics.fill(x + 1, y + 1, x + width - 1, y + 16 - 1, withAlpha(mix(-16579058, color, 0.1F), 196));
      graphics.fill(x + 1, y + 16 - 1, x + width - 1, y + 16, withAlpha(color, 107));
      String title = this.phaseTitle();
      if (this.isSRankError()) {
         this.drawGlitchText(graphics, title, centerX, y + 4, 17);
      } else {
         int titleColor = this.rank == 6 && this.phase == HunterEvaluationRules.Phase.RANK_REVEAL ? -1 : color;
         this.drawTracked(graphics, title, centerX - this.trackedWidth(title, 1) / 2, y + 4, titleColor, 1);
      }

      this.renderChargeRing(graphics, centerX, gemCenterY, color);
      this.renderGem(graphics, centerX, gemCenterY, color);
      this.drawClickWave(graphics, centerX, gemCenterY, color);
      this.renderPrompt(graphics, centerX, y + 152 - 13, color);
      if (this.isSRankError()) {
         this.drawGlitchTears(graphics, x + 1, y + 16, width - 2, 135);
      }

      float flash = this.sRankResolveFlash();
      if (flash > 0.0F) {
         graphics.fill(x + 1, y + 16, x + width - 1, y + 152 - 1, withAlpha(-1, (int)(216.0F * flash)));
      }
   }

   private void renderChargeRing(GuiGraphics graphics, int centerX, int centerY, int color) {
      strokeCircle(graphics, centerX, centerY, 52, 1, withAlpha(color, 61));

      for (int tick = 0; tick < 12; tick++) {
         double angle = (-Math.PI / 2) + tick * Math.PI / 6.0;
         boolean major = tick % 3 == 0;
         int inner = 52 + (major ? 3 : 4);
         int outer = 52 + (major ? 9 : 7);
         int tx0 = centerX + (int)Math.round(Math.cos(angle) * inner);
         int ty0 = centerY + (int)Math.round(Math.sin(angle) * inner);
         int tx1 = centerX + (int)Math.round(Math.cos(angle) * outer);
         int ty1 = centerY + (int)Math.round(Math.sin(angle) * outer);
         boolean reached = this.holdCharge >= tick / 12.0F;
         drawLine(graphics, tx0, ty0, tx1, ty1, withAlpha(reached ? color : -14469302, reached ? 216 : 140));
      }

      if (this.holdCharge > 0.004F) {
         strokeArc(graphics, centerX, centerY, 52, 3, -90.0F, 360.0F * this.holdCharge, withAlpha(color, 242));
         strokeArc(graphics, centerX, centerY, 55, 1, -90.0F, 360.0F * this.holdCharge, withAlpha(color, 102));
         double head = Math.toRadians(-90.0F + 360.0F * this.holdCharge);
         int hx = centerX + (int)Math.round(Math.cos(head) * 52.0);
         int hy = centerY + (int)Math.round(Math.sin(head) * 52.0);
         graphics.fill(hx - 2, hy - 2, hx + 3, hy + 3, mix(color, -1, 0.55F));
      }

      float lost = this.contactLostAt == 0L ? 0.0F : Math.max(0.0F, 1.0F - (float)(Util.getMillis() - this.contactLostAt) / 520.0F);
      if (lost > 0.0F) {
         strokeCircle(graphics, centerX, centerY, 57, 1, withAlpha(-42130, (int)(192.0F * lost)));
      }
   }

   private void renderGem(GuiGraphics graphics, int centerX, int centerY, int color) {
      float reveal = this.revealAmount();
      float pulse = pulse01();
      if (this.isSRankError()) {
         int slot = (int)(Util.getMillis() / 55L);
         centerX += Math.round(hash01(slot, 3) * 5.0F) - 2;
         centerY += Math.round(hash01(7, slot) * 3.0F) - 1;
         color = hash01(slot, slot) > 0.5F ? mix(color, -54709, 0.75F) : mix(color, -1, 0.35F);
      }

      int auraAlpha = 4 + (int)(4.0F * pulse) + (int)(7.0F * this.holdCharge);

      for (int layer = 4; layer >= 0; layer--) {
         fillCircle(graphics, centerX, centerY, 37 + layer * 5, withAlpha(color, auraAlpha));
      }

      if (this.gemHovered) {
         strokeCircle(graphics, centerX, centerY, 47, 1, withAlpha(color, 122));
      }

      if (this.holdCharge > 0.02F) {
         for (int mote = 0; mote < 18; mote++) {
            float travel = ((float)Util.getMillis() / 880.0F + mote * 0.137F) % 1.0F;
            float distance = (1.0F - travel) * 68.0F;
            double angle = mote * 2.3999 + Util.getMillis() / 1500.0;
            int mx = centerX + (int)Math.round(Math.cos(angle) * distance);
            int my = centerY + (int)Math.round(Math.sin(angle) * distance);
            int alpha = (int)(210.0F * travel * this.holdCharge);
            int size = travel > 0.72F ? 2 : 1;
            graphics.fill(mx, my, mx + size, my + size, withAlpha(mix(color, -1, 0.45F), alpha));
         }
      }

      int fillLine = centerY + 35 - Math.round(this.holdCharge * 35.0F * 2.0F);

      for (int dy = -35; dy <= 35; dy++) {
         int halfWidth = gemHalfWidthAt(dy);
         if (halfWidth > 0) {
            int y = centerY + dy;
            boolean charged = y >= fillLine;
            int body = charged ? mix(color, -1, 0.34F) : mix(-16380906, color, 0.2F + reveal * 0.3F);
            graphics.fill(centerX - halfWidth, y, centerX + halfWidth, y + 1, withAlpha(body, charged ? 230 : 196));
            int shade = Math.max(1, halfWidth / 3);
            graphics.fill(centerX + halfWidth - shade, y, centerX + halfWidth, y + 1, 973078528);
            graphics.fill(centerX - halfWidth, y, centerX - halfWidth + 1, y + 1, withAlpha(mix(body, -1, 0.55F), 196));
         }
      }

      if (this.holdCharge > 0.01F && this.holdCharge < 0.995F) {
         int halfWidth = gemHalfWidthAt(fillLine - centerY);
         if (halfWidth > 0) {
            graphics.fill(centerX - halfWidth, fillLine, centerX + halfWidth, fillLine + 1, mix(color, -1, 0.8F));
            graphics.fill(centerX - halfWidth, fillLine + 1, centerX + halfWidth, fillLine + 2, withAlpha(-1, 107));
         }
      }

      int tableHalf = Math.round(9.66F);
      int tableY = centerY - 35;
      int beltTop = centerY - 35 + Math.round(18.199999F);
      int beltBottom = centerY - 35 + Math.round(36.399998F);
      int seam = withAlpha(mix(color, -1, 0.65F), 107);
      drawLine(graphics, centerX - tableHalf, tableY, centerX - 21, beltTop, seam);
      drawLine(graphics, centerX + tableHalf, tableY, centerX + 21, beltTop, seam);
      drawLine(graphics, centerX - 21, beltBottom, centerX, centerY + 35, seam);
      drawLine(graphics, centerX + 21, beltBottom, centerX, centerY + 35, seam);
      graphics.fill(centerX - tableHalf, tableY, centerX + tableHalf, tableY + 1, withAlpha(mix(color, -1, 0.85F), 216));
      graphics.fill(centerX - tableHalf, beltTop, centerX + tableHalf, beltTop + 1, seam);
      graphics.fill(centerX - 21, beltTop, centerX + 21, beltTop + 1, seam);
      graphics.fill(centerX - 21, beltBottom, centerX + 21, beltBottom + 1, seam);
      graphics.fill(centerX, beltBottom, centerX + 1, centerY + 35, withAlpha(-1, 38));
      int coreRadius = 4 + Math.round((this.holdCharge * 0.6F + reveal * 0.4F) * 6.0F);
      int core = this.rank == 6 && reveal > 0.5F ? -1 : mix(color, -1, 0.35F + this.holdCharge * 0.45F);
      fillCircle(graphics, centerX, centerY, coreRadius, withAlpha(core, 180 + (int)(64.0F * pulse)));
      fillCircle(graphics, centerX, centerY, Math.max(1, coreRadius - 3), withAlpha(-1, 140 + (int)(80.0F * this.holdCharge)));
   }

   private static int gemHalfWidthAt(int dy) {
      float t = (dy + 35) / 70.0F;
      if (!(t < 0.0F) && !(t > 1.0F)) {
         float profile = t < 0.26F ? 0.46F + t / 0.26F * 0.53999996F : (t < 0.52F ? 1.0F : (1.0F - t) / 0.48000002F);
         return Math.round(21.0F * Math.max(0.0F, profile));
      } else {
         return 0;
      }
   }

   private void renderPrompt(GuiGraphics graphics, int centerX, int y, int color) {
      if (this.isSRankError()) {
         this.drawGlitchText(graphics, "VALUE OUT OF RANGE", centerX, y, 73);
      } else {
         float lost = this.contactLostAt == 0L ? 0.0F : Math.max(0.0F, 1.0F - (float)(Util.getMillis() - this.contactLostAt) / 900.0F);
         if (lost > 0.0F && this.phase == HunterEvaluationRules.Phase.CONTACT) {
            String message = "CONTACT LOST - HOLD AGAIN";
            graphics.drawCenteredString(this.font, message, centerX, y, withAlpha(-38020, (int)(255.0F * lost)));
         } else if (this.phase == HunterEvaluationRules.Phase.CONTACT) {
            if (this.holdingContact) {
               String message = "HOLD STEADY  " + Math.round(this.holdCharge * 100.0F) + "%";
               graphics.drawCenteredString(this.font, message, centerX, y, mix(color, -1, 0.5F));
            } else {
               int alpha = 158 + (int)(97.0F * pulse01());
               graphics.drawCenteredString(this.font, "PRESS AND HOLD THE GEM", centerX, y, withAlpha(-1509633, alpha));
            }
         } else {
            if (this.phase == HunterEvaluationRules.Phase.DECISION && this.mode == HunterEvaluationRules.Mode.INITIAL && this.fixedClass) {
               graphics.drawCenteredString(this.font, "Legacy class preserved; rank certification only.", centerX, y, -6441529);
            } else if (this.phase == HunterEvaluationRules.Phase.DECISION && this.mode == HunterEvaluationRules.Mode.REEVALUATION) {
               String comparison = this.previousRank > 0
                  ? HunterEvaluationRules.rankName(this.previousRank) + "  >  " + HunterEvaluationRules.rankName(this.rank)
                  : "CERTIFY " + HunterEvaluationRules.rankName(this.rank);
               graphics.drawCenteredString(this.font, comparison, centerX, y, -2298113);
            }
         }
      }
   }

   private void renderReadout(GuiGraphics graphics, int color) {
      int y = this.panelY + 204;
      boolean hasClass = this.classId > 0;
      int crestX = this.panelX + 20;
      graphics.fill(crestX, y, crestX + 26, y + 26, withAlpha(mix(-16579058, color, 0.16F), 180));
      this.outline(graphics, crestX, y, 26, 26, withAlpha(hasClass ? color : -14469302, 180));
      if (hasClass) {
         drawClassGlyph(graphics, crestX + 13, y + 13, this.classId, this.rank == 6 ? -1 : color);
      } else {
         drawDiamond(graphics, crestX + 13, y + 13, 6, withAlpha(-14469302, 196));
      }

      this.drawTracked(graphics, "CLASS", crestX + 34, y + 2, -9600363, 1);
      String className = hasClass ? HunterEvaluationRules.className(this.classId).toUpperCase() : "UNRESOLVED";
      graphics.drawString(this.font, className, crestX + 34, y + 14, hasClass ? (this.rank == 6 ? -1 : color) : -9600363, false);
      int ladderWidth = RANK_LETTERS.length * 21 - 3;
      int ladderX = this.panelX + this.panelW - 20 - ladderWidth;
      String rankLabel = "RANK";
      this.drawTracked(graphics, rankLabel, this.panelX + this.panelW - 20 - this.trackedWidth(rankLabel, 1), y + 2, -9600363, 1);
      this.renderRankLadder(graphics, ladderX, y + 13, color);
   }

   private void renderRankLadder(GuiGraphics graphics, int x, int y, int color) {
      int width = RANK_LETTERS.length * 21 - 3;
      if (this.isSRankError()) {
         graphics.fill(x, y, x + width, y + 13, 1494747152);
         graphics.fill(x, y + 12, x + width, y + 13, -54709);
         this.drawGlitchText(graphics, "ERROR", x + width / 2, y + 3, 41);
      } else {
         boolean revealing = this.phase == HunterEvaluationRules.Phase.RANK_REVEAL;
         float progress = revealing ? this.rankRevealProgress() : 1.0F;
         int lit = this.rank <= 0 ? 0 : Math.max(1, Math.round(this.rank * progress));

         for (int index = 0; index < RANK_LETTERS.length; index++) {
            int cellX = x + index * 21;
            boolean on = index < lit;
            boolean peak = index == lit - 1 && lit > 0;
            int fill = on ? withAlpha(color, peak ? 180 : 102) : 1493701409;
            graphics.fill(cellX, y, cellX + 18, y + 13, fill);
            graphics.fill(cellX, y + 12, cellX + 18, y + 13, on ? color : -14469302);
            if (peak) {
               this.outline(graphics, cellX - 1, y - 1, 20, 15, index == 5 ? -1 : mix(color, -1, 0.5F));
               int alpha = 60 + (int)(90.0F * pulse01());
               graphics.fill(cellX, y, cellX + 18, y + 13, withAlpha(-1, alpha / 3));
            }

            String letter = RANK_LETTERS[index];
            graphics.drawString(this.font, letter, cellX + (18 - this.font.width(letter)) / 2, y + 3, on ? (peak ? -1 : -1509633) : -9600363, false);
         }
      }
   }

   private void renderProgress(GuiGraphics graphics, int centerX, int color) {
      int x = this.panelX + 20;
      int width = this.panelW - 40;
      int y = this.panelY + 236;
      graphics.fill(x, y, x + width, y + 4, 2047613478);
      int filled = Math.round(width * this.phaseProgress());
      graphics.fill(x, y, x + filled, y + 4, this.rank == 6 ? -1 : color);
      if (filled > 2) {
         graphics.fill(x + filled - 2, y, x + filled, y + 4, mix(color, -1, 0.7F));
      }

      for (int tick = 1; tick < 4; tick++) {
         graphics.fill(x + width * tick / 4, y, x + width * tick / 4 + 1, y + 4, 1493172224);
      }

      graphics.drawCenteredString(this.font, this.phaseDetail(), centerX, this.panelY + 245, -6441529);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      double logicalX = this.logicalMouseX(mouseX);
      double logicalY = this.logicalMouseY(mouseY);
      if (button == 0 && this.phase == HunterEvaluationRules.Phase.CONTACT && this.isInsideChamber(logicalX, logicalY) && !this.holdingContact) {
         this.holdingContact = true;
         this.clickWaveAt = Util.getMillis();
         this.contactLostAt = 0L;
         this.sendImmediate(HunterEvaluationRules.Action.BEGIN_CONTACT);
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && this.holdingContact) {
         this.holdingContact = false;
         if (this.phase == HunterEvaluationRules.Phase.CONTACT) {
            this.contactLostAt = Util.getMillis();
            this.sendImmediate(HunterEvaluationRules.Action.CANCEL_CONTACT);
         }

         return true;
      } else {
         return super.mouseReleased(mouseX, mouseY, button);
      }
   }

   @Override
   protected void beginClose() {
      if (this.holdingContact) {
         this.holdingContact = false;
         this.sendImmediate(HunterEvaluationRules.Action.CANCEL_CONTACT);
      }

      super.beginClose();
   }

   @Override
   public void onClose() {
      if (this.holdingContact) {
         this.holdingContact = false;
         this.sendImmediate(HunterEvaluationRules.Action.CANCEL_CONTACT);
      }

      super.onClose();
   }

   private boolean isInsideChamber(double mouseX, double mouseY) {
      int x = this.panelX + 18;
      int y = this.panelY + 46 + 16;
      return mouseX >= x && mouseX < x + this.panelW - 36 && mouseY >= y && mouseY < this.panelY + 46 + 152;
   }

   private void updateButtons() {
      if (this.rerollButton != null && this.acceptButton != null && this.doneButton != null) {
         boolean decision = this.phase == HunterEvaluationRules.Phase.DECISION;
         this.rerollButton.visible = decision && this.mode == HunterEvaluationRules.Mode.INITIAL && this.canReroll;
         this.acceptButton.visible = decision;
         this.doneButton.visible = this.phase == HunterEvaluationRules.Phase.COMPLETE;
         if (this.mode == HunterEvaluationRules.Mode.REEVALUATION) {
            this.acceptButton.setMessage(Component.literal(this.rank > this.previousRank ? "CERTIFY NEW RANK" : "CONFIRM RECORD"));
         } else {
            this.acceptButton.setMessage(Component.literal("ACCEPT RESULT"));
         }

         boolean paired = decision && this.mode == HunterEvaluationRules.Mode.INITIAL && this.canReroll;
         if (paired) {
            this.acceptButton.setX(this.panelX + 174);
            this.acceptButton.setWidth(132);
         } else {
            this.acceptButton.setX(this.panelX + 95);
            this.acceptButton.setWidth(150);
         }
      }
   }

   private void advanceCharge() {
      float target;
      if (this.phase == HunterEvaluationRules.Phase.CONTACT) {
         target = this.holdingContact ? this.phaseProgress() : 0.0F;
      } else if (this.phase == HunterEvaluationRules.Phase.BOOT) {
         target = 0.0F;
      } else {
         target = 1.0F;
      }

      float rate = target > this.holdCharge ? 0.2F : 0.28F;
      this.holdCharge = this.holdCharge + (target - this.holdCharge) * rate;
      if (Math.abs(target - this.holdCharge) < 0.004F) {
         this.holdCharge = target;
      }
   }

   private static int stageOf(HunterEvaluationRules.Phase phase) {
      return switch (phase) {
         case BOOT, CONTACT -> 0;
         case SCAN -> 1;
         case CLASS_REVEAL, REROLL -> 2;
         case RANK_REVEAL, SETTLE -> 3;
         case DECISION, COMPLETE -> 4;
      };
   }

   private String phaseTitle() {
      return switch (this.phase) {
         case BOOT -> "INITIALIZING EVALUATOR";
         case CONTACT -> this.holdingContact ? "MANA CONTACT STABILIZING" : "PLACE YOUR HAND";
         case SCAN -> "READING MANA SIGNATURE";
         case CLASS_REVEAL -> "CLASS RESONANCE DETECTED";
         case REROLL -> "RECALIBRATING CLASS";
         case RANK_REVEAL -> this.isSRankError() ? "ERROR" : "MEASURING MANA OUTPUT";
         case SETTLE -> "FINALIZING RESULT";
         case DECISION -> this.mode == HunterEvaluationRules.Mode.INITIAL ? "EVALUATION RESULT" : "REEVALUATION RESULT";
         case COMPLETE -> "HUNTER RECORD CERTIFIED";
      };
   }

   private String phaseDetail() {
      return switch (this.phase) {
         case BOOT -> "Association terminal handshake";
         case CONTACT -> "Maintain contact for 1.5 seconds";
         case SCAN -> this.mode == HunterEvaluationRules.Mode.INITIAL ? "Rank cannot be influenced or rerolled" : "Comparing earned power with certified rank";
         case CLASS_REVEAL -> "Hue identifies class resonance";
         case REROLL -> "Drawing from the remaining class shuffle";
         case RANK_REVEAL -> this.isSRankError() ? "Measured output exceeds the certified scale" : "Brightness identifies Hunter rank";
         case SETTLE -> "Locking the measured rank";
         case DECISION -> this.mode == HunterEvaluationRules.Mode.INITIAL && this.canReroll
            ? "Rerolls never repeat the current class"
            : "Confirm the official Association record";
         case COMPLETE -> "Hunter ID synchronized";
      };
   }

   private float phaseProgress() {
      if (this.phase == HunterEvaluationRules.Phase.DECISION || this.phase == HunterEvaluationRules.Phase.COMPLETE) {
         return 1.0F;
      } else {
         return this.phaseDurationTicks <= 0 ? 0.0F : clamp01(1.0F - (float)this.remainingNow() / this.phaseDurationTicks);
      }
   }

   private int remainingNow() {
      long elapsed = Math.max(0L, Util.getMillis() - this.stateReceivedAt);
      return Math.max(0, this.remainingTicks - (int)(elapsed / 50L));
   }

   private float waveStrength() {
      return switch (this.phase) {
         case BOOT -> 0.3F;
         case CONTACT -> 0.55F + this.holdCharge * 0.55F;
         case SCAN -> 1.0F;
         case CLASS_REVEAL, REROLL, RANK_REVEAL -> 1.25F;
         case SETTLE -> 0.86F;
         case DECISION, COMPLETE -> 0.62F;
      };
   }

   private float scanSweep() {
      return this.phase == HunterEvaluationRules.Phase.SCAN ? this.phaseProgress() : 0.0F;
   }

   private boolean isSRankError() {
      return this.phase == HunterEvaluationRules.Phase.RANK_REVEAL && this.rank == 6 && this.phaseProgress() < HunterEvaluationRules.sRankErrorFraction();
   }

   private float rankRevealProgress() {
      float progress = this.phaseProgress();
      if (this.rank != 6) {
         return progress;
      }

      float errorShare = HunterEvaluationRules.sRankErrorFraction();
      return progress <= errorShare ? 0.0F : (progress - errorShare) / (1.0F - errorShare);
   }

   private float sRankResolveFlash() {
      return this.rank == 6 && this.phase == HunterEvaluationRules.Phase.RANK_REVEAL && !this.isSRankError()
         ? clamp01(1.0F - this.rankRevealProgress() * 5.0F)
         : 0.0F;
   }

   private float revealAmount() {
      if (this.phase != HunterEvaluationRules.Phase.CLASS_REVEAL
         && this.phase != HunterEvaluationRules.Phase.RANK_REVEAL
         && this.phase != HunterEvaluationRules.Phase.REROLL) {
         return this.classId > 0 ? 1.0F : 0.0F;
      } else {
         return this.phaseProgress();
      }
   }

   private int activeColor() {
      return HunterEvaluationRules.classColor(this.classId);
   }

   private void send(HunterEvaluationRules.Action action) {
      if (!this.actionPending) {
         this.actionPending = true;
         this.sendImmediate(action);
      }
   }

   private void sendImmediate(HunterEvaluationRules.Action action) {
      if (this.sessionId != null) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new HunterEvaluationActionMessage(this.sessionId, action));
      }
   }

   private void updateState(
      UUID sessionId,
      int mode,
      int phase,
      int classId,
      int rank,
      int previousRank,
      int phaseDurationTicks,
      int remainingTicks,
      boolean canReroll,
      boolean fixedClass
   ) {
      this.sessionId = sessionId;
      this.mode = HunterEvaluationRules.Mode.fromId(mode);
      this.phase = HunterEvaluationRules.Phase.fromId(phase);
      this.classId = classId;
      this.rank = rank;
      this.previousRank = previousRank;
      this.phaseDurationTicks = Math.max(0, phaseDurationTicks);
      this.remainingTicks = Math.max(0, remainingTicks);
      this.canReroll = canReroll;
      this.fixedClass = fixedClass;
      this.stateReceivedAt = Util.getMillis();
      this.actionPending = false;
      if (this.phase != HunterEvaluationRules.Phase.CONTACT) {
         this.holdingContact = false;
      }
   }

   public static void handleServerState(
      boolean open,
      boolean forceOpen,
      UUID sessionId,
      int mode,
      int phase,
      int classId,
      int rank,
      int previousRank,
      int phaseDurationTicks,
      int remainingTicks,
      boolean canReroll,
      boolean fixedClass
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!open) {
         if (minecraft.screen instanceof HunterEvaluationScreen) {
            minecraft.setScreen(null);
         }
      } else {
         if (minecraft.screen instanceof HunterEvaluationScreen screen) {
            screen.updateState(sessionId, mode, phase, classId, rank, previousRank, phaseDurationTicks, remainingTicks, canReroll, fixedClass);
         } else if (forceOpen) {
            minecraft.setScreen(
               new HunterEvaluationScreen(sessionId, mode, phase, classId, rank, previousRank, phaseDurationTicks, remainingTicks, canReroll, fixedClass)
            );
         }
      }
   }

   private void drawClickWave(GuiGraphics graphics, int centerX, int centerY, int color) {
      if (this.clickWaveAt != 0L) {
         float age = (float)(Util.getMillis() - this.clickWaveAt) / 700.0F;
         if (age >= 1.0F) {
            this.clickWaveAt = 0L;
         } else {
            int radius = 22 + Math.round(age * 58.0F);
            int alpha = Math.max(0, Math.round((1.0F - age) * 150.0F));
            strokeCircle(graphics, centerX, centerY, radius, 1, withAlpha(color, alpha));
         }
      }
   }

   private static void fillCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
      if (radius > 0) {
         int squared = radius * radius;

         for (int dy = -radius; dy <= radius; dy++) {
            int span = (int)Math.sqrt(Math.max(0, squared - dy * dy));
            if (span > 0) {
               graphics.fill(centerX - span, centerY + dy, centerX + span + 1, centerY + dy + 1, color);
            }
         }
      }
   }

   private static void strokeCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int thickness, int color) {
      if (radius > 0) {
         int inner = Math.max(0, radius - Math.max(1, thickness));
         int outerSq = radius * radius;
         int innerSq = inner * inner;

         for (int dy = -radius; dy <= radius; dy++) {
            int ySq = dy * dy;
            if (ySq <= outerSq) {
               int outerSpan = (int)Math.sqrt(outerSq - ySq);
               int y = centerY + dy;
               if (ySq >= innerSq) {
                  graphics.fill(centerX - outerSpan, y, centerX + outerSpan + 1, y + 1, color);
               } else {
                  int innerSpan = (int)Math.sqrt(innerSq - ySq);
                  graphics.fill(centerX - outerSpan, y, centerX - innerSpan, y + 1, color);
                  graphics.fill(centerX + innerSpan + 1, y, centerX + outerSpan + 1, y + 1, color);
               }
            }
         }
      }
   }

   private static void strokeArc(GuiGraphics graphics, int centerX, int centerY, int radius, int thickness, float startDegrees, float sweepDegrees, int color) {
      if (!(sweepDegrees <= 0.0F) && radius > 0) {
         int steps = Math.max(1, Math.round(sweepDegrees * radius / 110.0F));
         int half = Math.max(1, thickness) / 2;
         int rest = Math.max(1, thickness) - half;

         for (int step = 0; step <= steps; step++) {
            double angle = Math.toRadians(startDegrees + sweepDegrees * step / steps);
            int x = centerX + (int)Math.round(Math.cos(angle) * radius);
            int y = centerY + (int)Math.round(Math.sin(angle) * radius);
            graphics.fill(x - half, y - half, x + rest, y + rest, color);
         }
      }
   }

   private static void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
      int dx = x1 - x0;
      int dy = y1 - y0;
      int steps = Math.max(Math.abs(dx), Math.abs(dy));
      if (steps <= 0) {
         graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
      } else {
         for (int step = 0; step <= steps; step++) {
            int x = x0 + Math.round(dx * ((float)step / steps));
            int y = y0 + Math.round(dy * ((float)step / steps));
            graphics.fill(x, y, x + 1, y + 1, color);
         }
      }
   }

   private static void drawDiamond(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
      for (int dy = -radius; dy <= radius; dy++) {
         int span = radius - Math.abs(dy);
         if (span >= 0) {
            graphics.fill(centerX - span, centerY + dy, centerX + span + 1, centerY + dy + 1, color);
         }
      }
   }

   private static void drawClassGlyph(GuiGraphics graphics, int centerX, int centerY, int classId, int color) {
      switch (classId) {
         case 1:
            drawLine(graphics, centerX - 7, centerY + 7, centerX + 6, centerY - 6, color);
            drawLine(graphics, centerX + 7, centerY + 7, centerX - 6, centerY - 6, color);
            graphics.fill(centerX - 8, centerY + 1, centerX - 2, centerY + 3, color);
            graphics.fill(centerX + 3, centerY + 1, centerX + 9, centerY + 3, color);
            break;
         case 2:
            fillCircle(graphics, centerX, centerY, 4, color);

            for (int ray = 0; ray < 8; ray++) {
               double angle = ray * Math.PI / 4.0;
               int x0 = centerX + (int)Math.round(Math.cos(angle) * 6.0);
               int y0 = centerY + (int)Math.round(Math.sin(angle) * 6.0);
               int x1 = centerX + (int)Math.round(Math.cos(angle) * 9.0);
               int y1 = centerY + (int)Math.round(Math.sin(angle) * 9.0);
               drawLine(graphics, x0, y0, x1, y1, color);
            }
            break;
         case 3:
            graphics.fill(centerX - 1, centerY - 9, centerX + 2, centerY + 5, color);
            graphics.fill(centerX - 6, centerY + 1, centerX + 7, centerY + 3, color);
            graphics.fill(centerX - 2, centerY + 5, centerX + 3, centerY + 9, color);
            break;
         case 4:
            for (int dy = -8; dy <= 8; dy++) {
               float t = (dy + 8) / 16.0F;
               int span = t < 0.55F ? 7 : Math.round(7.0F * (1.0F - t) / 0.45F);
               if (span > 0) {
                  boolean edge = dy == -8 || span <= 1;
                  graphics.fill(centerX - span, centerY + dy, centerX + span + 1, centerY + dy + 1, edge ? color : withAlpha(color, 89));
               }
            }

            graphics.fill(centerX - 7, centerY - 8, centerX + 8, centerY - 6, color);
            graphics.fill(centerX - 1, centerY - 6, centerX + 2, centerY + 6, color);
            break;
         case 5:
            graphics.fill(centerX - 2, centerY - 9, centerX + 3, centerY + 10, color);
            graphics.fill(centerX - 8, centerY - 3, centerX + 9, centerY + 4, color);
            break;
         default:
            for (int dy = -8; dy <= 8; dy++) {
               int span = 6 - dy * dy / 12;
               graphics.fill(centerX - 2 + span, centerY + dy, centerX - 1 + span, centerY + dy + 1, color);
            }

            drawLine(graphics, centerX - 3, centerY - 8, centerX - 3, centerY + 8, withAlpha(color, 140));
            graphics.fill(centerX - 8, centerY - 1, centerX + 5, centerY + 1, color);
            drawLine(graphics, centerX + 5, centerY, centerX + 1, centerY - 4, color);
            drawLine(graphics, centerX + 5, centerY, centerX + 1, centerY + 4, color);
      }
   }

   private void drawGlitchText(GuiGraphics graphics, String text, int centerX, int y, int seed) {
      int slot = (int)(Util.getMillis() / 60L);
      int offset = Math.round(hash01(seed, slot) * 5.0F) - 2;
      int width = this.trackedWidth(text, 1);
      int x = centerX - width / 2;
      this.drawTracked(graphics, text, x - 2 + offset, y, -54709, 1);
      this.drawTracked(graphics, text, x + 2 - offset, y, -13963009, 1);
      this.drawTracked(graphics, text, x, y, -1, 1);
   }

   private void drawGlitchTears(GuiGraphics graphics, int x, int y, int width, int height) {
      int slot = (int)(Util.getMillis() / 90L);

      for (int tear = 0; tear < 4; tear++) {
         float seed = hash01(tear * 31 + slot, slot);
         int tearY = y + (int)(seed * (height - 4));
         int tearHeight = 1 + (int)(hash01(slot, tear) * 3.0F);
         int shift = Math.round(hash01(tear, slot * 7) * 12.0F) - 6;
         graphics.fill(x + Math.max(0, shift), tearY, x + width + Math.min(0, shift), tearY + tearHeight, tear % 2 == 0 ? 1509894731 : 1294659839);
      }
   }

   private static float hash01(int x, int y) {
      int hash = x * 374761393 + y * 668265263;
      hash = (hash ^ hash >>> 13) * 1274126177;
      return ((hash ^ hash >>> 16) & 65535) / 65535.0F;
   }

   private int drawTracked(GuiGraphics graphics, String text, int x, int y, int color, int tracking) {
      int cursor = x;

      for (int index = 0; index < text.length(); index++) {
         String glyph = String.valueOf(text.charAt(index));
         graphics.drawString(this.font, glyph, cursor, y, color, false);
         cursor += this.font.width(glyph) + tracking;
      }

      return cursor - tracking;
   }

   private int trackedWidth(String text, int tracking) {
      if (text.isEmpty()) {
         return 0;
      }

      int width = -tracking;

      for (int index = 0; index < text.length(); index++) {
         width += this.font.width(String.valueOf(text.charAt(index))) + tracking;
      }

      return width;
   }

   private void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
      graphics.fill(x, y, x + width, y + 1, color);
      graphics.fill(x, y + height - 1, x + width, y + height, color);
      graphics.fill(x, y, x + 1, y + height, color);
      graphics.fill(x + width - 1, y, x + width, y + height, color);
   }

   private void drawCorners(GuiGraphics graphics, int color) {
      int length = 13;
      graphics.fill(this.panelX - 1, this.panelY - 1, this.panelX + length, this.panelY + 1, color);
      graphics.fill(this.panelX - 1, this.panelY - 1, this.panelX + 1, this.panelY + length, color);
      graphics.fill(this.panelX + this.panelW - length, this.panelY - 1, this.panelX + this.panelW + 1, this.panelY + 1, color);
      graphics.fill(this.panelX + this.panelW - 1, this.panelY - 1, this.panelX + this.panelW + 1, this.panelY + length, color);
      graphics.fill(this.panelX - 1, this.panelY + this.panelH - 1, this.panelX + length, this.panelY + this.panelH + 1, color);
      graphics.fill(this.panelX - 1, this.panelY + this.panelH - length, this.panelX + 1, this.panelY + this.panelH + 1, color);
      graphics.fill(this.panelX + this.panelW - length, this.panelY + this.panelH - 1, this.panelX + this.panelW + 1, this.panelY + this.panelH + 1, color);
      graphics.fill(this.panelX + this.panelW - 1, this.panelY + this.panelH - length, this.panelX + this.panelW + 1, this.panelY + this.panelH + 1, color);
   }

   private static float pulse01() {
      return 0.5F + 0.5F * (float)Math.sin(Util.getMillis() / 170.0);
   }

   private static int withAlpha(int color, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | color & 16777215;
   }

   private static int mix(int from, int to, float amount) {
      float bounded = clamp01(amount);
      int red = Math.round((from >> 16 & 0xFF) * (1.0F - bounded) + (to >> 16 & 0xFF) * bounded);
      int green = Math.round((from >> 8 & 0xFF) * (1.0F - bounded) + (to >> 8 & 0xFF) * bounded);
      int blue = Math.round((from & 0xFF) * (1.0F - bounded) + (to & 0xFF) * bounded);
      return 0xFF000000 | red << 16 | green << 8 | blue;
   }

   private static float frac(float value) {
      return value - (float)Math.floor(value);
   }

   private static float clamp01(float value) {
      return value < 0.0F ? 0.0F : Math.min(1.0F, value);
   }
}
