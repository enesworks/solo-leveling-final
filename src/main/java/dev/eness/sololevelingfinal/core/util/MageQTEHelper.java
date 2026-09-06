package dev.eness.sololevelingfinal.core.util;

import java.util.Random;
import java.util.Set;
import net.minecraft.world.entity.Entity;

public final class MageQTEHelper {
   public static final float ROTATION_SPEED = 360.0F;
   public static final float GOOD_ZONE_SIZE = 40.0F;
   public static final float PERFECT_ZONE_SIZE = 14.0F;
   public static final int TIMEOUT_MS = 2000;
   public static final Set<String> MAGE_SKILLS = Set.of(
      "Ignition Orb",
      "Inferno Lance",
      "Flashfire",
      "Cremation",
      "Furnace Dominion",
      "Heavenfall",
      "Sealing Prism",
      "Resonant Collapse",
      "Absolute Bastion",
      "Astral Arsenal",
      "Dimensional Rend",
      "Grand Formula: Convergence",
      "Thunderhead",
      "Skybreaker",
      "Tempest Incarnate"
   );

   private MageQTEHelper() {
   }

   public static float computeZoneStart(Entity entity) {
      long tick = entity.level().getGameTime() / 5L;
      long u1 = entity.getUUID().getMostSignificantBits();
      long u2 = entity.getUUID().getLeastSignificantBits();
      long seed = u1 ^ u2 * 31L ^ tick * 17L;
      return 90.0F + new Random(seed).nextFloat() * 270.0F;
   }

   public static QTEResult computeResult(float zoneStart, int pressedMs) {
      if (pressedMs > 2000) {
         return QTEResult.MISS;
      } else {
         float rotation = pressedMs / 1000.0F * 360.0F % 360.0F;
         float goodEnd = (zoneStart + 40.0F) % 360.0F;
         float perfectStart = (zoneStart + 13.0F) % 360.0F;
         float perfectEnd = (perfectStart + 14.0F) % 360.0F;
         if (isInArc(rotation, perfectStart, perfectEnd)) {
            return QTEResult.PERFECT;
         } else {
            return isInArc(rotation, zoneStart, goodEnd) ? QTEResult.GOOD : QTEResult.MISS;
         }
      }
   }

   public static double getManaCostMultiplier(QTEResult result, double intelligence) {
      return switch (result) {
         case PERFECT -> 0.7;
         case GOOD -> 0.85;
         case MISS -> 1.0;
      };
   }

   public static boolean isInArc(float angle, float start, float end) {
      return end < start ? angle >= start || angle <= end : angle >= start && angle <= end;
   }

   public static float perfectZoneStart(float goodZoneStart) {
      return (goodZoneStart + 13.0F) % 360.0F;
   }
}
