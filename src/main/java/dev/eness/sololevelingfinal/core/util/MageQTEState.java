package dev.eness.sololevelingfinal.core.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MageQTEState {
   public static final MageQTEState INSTANCE = new MageQTEState();
   private boolean active = false;
   private long startTime = 0L;
   private float zoneStart = 0.0F;
   private QTEResult lastResult = null;
   private long resultDisplayUntil = 0L;

   private MageQTEState() {
   }

   public void startQTE(float zoneStart) {
      this.active = true;
      this.startTime = System.currentTimeMillis();
      this.zoneStart = zoneStart;
      this.lastResult = null;
   }

   public void endQTE() {
      this.active = false;
   }

   public void cancelQTE() {
      this.active = false;
      this.lastResult = null;
   }

   public boolean isActive() {
      return this.active;
   }

   public boolean hasTimedOut() {
      return this.active && System.currentTimeMillis() - this.startTime > 2000L;
   }

   public float getCurrentRotation() {
      if (!this.active) {
         return 0.0F;
      }

      long elapsed = System.currentTimeMillis() - this.startTime;
      return (float)elapsed / 1000.0F * 360.0F % 360.0F;
   }

   public float getGoodZoneStart() {
      return this.zoneStart;
   }

   public float getGoodZoneEnd() {
      return (this.zoneStart + 40.0F) % 360.0F;
   }

   public float getPerfectZoneStart() {
      return MageQTEHelper.perfectZoneStart(this.zoneStart);
   }

   public float getPerfectZoneEnd() {
      return (this.getPerfectZoneStart() + 14.0F) % 360.0F;
   }

   public void showResult(QTEResult result) {
      this.lastResult = result;
      this.resultDisplayUntil = System.currentTimeMillis() + 1500L;
   }

   public boolean isShowingResult() {
      return this.lastResult != null && System.currentTimeMillis() < this.resultDisplayUntil;
   }

   public QTEResult getLastResult() {
      return this.lastResult;
   }

   public float getResultAlpha() {
      if (!this.isShowingResult()) {
         return 0.0F;
      }

      long remaining = this.resultDisplayUntil - System.currentTimeMillis();
      return Math.min(1.0F, (float)remaining / 500.0F);
   }
}
