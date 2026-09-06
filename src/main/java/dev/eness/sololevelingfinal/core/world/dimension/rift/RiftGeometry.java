package dev.eness.sololevelingfinal.core.world.dimension.rift;

public final class RiftGeometry {
   public static final double TWO_PI = Math.PI * 2;
   public static final double SLICE_ANGLE = Math.PI / 4;
   public static final double HALF_SLICE_ANGLE = Math.PI / 8;
   public static final double DEFAULT_SAFE_CENTER_RADIUS = 256.0;
   public static final double DEFAULT_STAR_CORE_RADIUS = 600.0;
   public static final double DEFAULT_STAR_TIP_RADIUS = 1200.0;
   public static final double DEFAULT_STAR_EXPONENT = 1.7;
   public static final double DEFAULT_SCAR_HALF_WIDTH = 56.0;
   public static final double DEFAULT_TERRAIN_FADE_START = 3180.0;
   public static final double DEFAULT_PLAYABLE_RADIUS = 3500.0;

   private RiftGeometry() {
   }

   public static double distance(double x, double z) {
      return Math.hypot(x, z);
   }

   public static double angle(double x, double z) {
      double angle = Math.atan2(z, x);
      return angle < 0.0 ? angle + (Math.PI * 2) : angle;
   }

   public static int territoryIndex(double x, double z) {
      double shifted = positiveModulo(angle(x, z) + (Math.PI / 8), Math.PI * 2);
      return Math.floorMod((int)Math.floor(shifted / (Math.PI / 4)), RiftTerritory.values().length);
   }

   public static RiftTerritory territory(double x, double z) {
      return RiftTerritory.byIndex(territoryIndex(x, z));
   }

   public static double starRadius(double angle, double coreRadius, double tipRadius, double exponent) {
      double lobe = 0.5 + 0.5 * Math.cos(8.0 * (angle - (Math.PI / 8)));
      double shaped = Math.pow(clamp(lobe, 0.0, 1.0), Math.max(0.1, exponent));
      return lerp(shaped, Math.min(coreRadius, tipRadius), Math.max(coreRadius, tipRadius));
   }

   public static boolean insideStar(double x, double z, double coreRadius, double tipRadius, double exponent) {
      return distance(x, z) <= starRadius(angle(x, z), coreRadius, tipRadius, exponent);
   }

   public static double distanceToNearestBoundary(double x, double z) {
      double radius = distance(x, z);
      if (radius == 0.0) {
         return 0.0;
      }

      int territory = territoryIndex(x, z);
      double center = territory * (Math.PI / 4);
      double fromCenter = Math.abs(wrapRadians(angle(x, z) - center));
      double angularDistance = Math.max(0.0, (Math.PI / 8) - fromCenter);
      return radius * Math.sin(angularDistance);
   }

   public static RiftGeometry.Region resolveDefault(double x, double z) {
      return resolve(x, z, 256.0, 600.0, 1200.0, 1.7, 56.0, 3500.0);
   }

   public static RiftGeometry.Region resolve(
      double x,
      double z,
      double safeCenterRadius,
      double starCoreRadius,
      double starTipRadius,
      double starExponent,
      double scarHalfWidth,
      double playableRadius
   ) {
      double radius = distance(x, z);
      if (radius > playableRadius) {
         return RiftGeometry.Region.voidRegion(radius);
      } else if (radius <= safeCenterRadius) {
         return RiftGeometry.Region.center(radius);
      } else if (insideStar(x, z, starCoreRadius, starTipRadius, starExponent)) {
         return RiftGeometry.Region.wasteland(radius);
      } else {
         return distanceToNearestBoundary(x, z) <= scarHalfWidth ? RiftGeometry.Region.scar(radius) : RiftGeometry.Region.territory(radius, territory(x, z));
      }
   }

   public static int levelForDistance(double distance) {
      double start = 256.0;
      double end = 3180.0;
      double progress = clamp((distance - start) / (end - start), 0.0, 1.0);
      progress = Math.pow(progress, 1.08);
      return clamp((int)Math.round(lerp(progress, 5.0, 100.0)), 5, 100);
   }

   public static double smoothStep(double value) {
      double clamped = clamp(value, 0.0, 1.0);
      return clamped * clamped * (3.0 - 2.0 * clamped);
   }

   private static double lerp(double amount, double start, double end) {
      return start + amount * (end - start);
   }

   private static double clamp(double value, double minimum, double maximum) {
      return Math.max(minimum, Math.min(maximum, value));
   }

   private static int clamp(int value, int minimum, int maximum) {
      return Math.max(minimum, Math.min(maximum, value));
   }

   private static double positiveModulo(double value, double modulus) {
      double result = value % modulus;
      return result < 0.0 ? result + modulus : result;
   }

   private static double wrapRadians(double value) {
      return positiveModulo(value + Math.PI, Math.PI * 2) - Math.PI;
   }

   public record Region(RiftGeometry.RegionType type, double distance, RiftTerritory territory) {
      private static RiftGeometry.Region center(double distance) {
         return new RiftGeometry.Region(RiftGeometry.RegionType.CENTER, distance, null);
      }

      private static RiftGeometry.Region wasteland(double distance) {
         return new RiftGeometry.Region(RiftGeometry.RegionType.WASTELAND, distance, null);
      }

      private static RiftGeometry.Region scar(double distance) {
         return new RiftGeometry.Region(RiftGeometry.RegionType.RIFT_SCAR, distance, null);
      }

      private static RiftGeometry.Region territory(double distance, RiftTerritory territory) {
         return new RiftGeometry.Region(RiftGeometry.RegionType.TERRITORY, distance, territory);
      }

      private static RiftGeometry.Region voidRegion(double distance) {
         return new RiftGeometry.Region(RiftGeometry.RegionType.VOID, distance, null);
      }
   }

   public enum RegionType {
      CENTER,
      WASTELAND,
      RIFT_SCAR,
      TERRITORY,
      VOID;
   }
}
