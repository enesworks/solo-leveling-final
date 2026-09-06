package dev.eness.sololevelingfinal.core.world.dimension.rift;

public enum RiftTerritory {
   DESTRUCTION("destruction", "Destruction", 0.0),
   FROST("frost", "Frost", 45.0),
   FANGS("fangs", "Fangs", 90.0),
   PLAGUES("plagues", "Plagues", 135.0),
   IRON_BODY("iron_body", "Iron Body", 180.0),
   WHITE_FLAMES("white_flames", "White Flames", 225.0),
   TRANSFIGURATION("transfiguration", "Transfiguration", 270.0),
   BEGINNING("beginning", "Beginning", 315.0);

   private final String id;
   private final String displayName;
   private final double centerAngleDegrees;

   RiftTerritory(String id, String displayName, double centerAngleDegrees) {
      this.id = id;
      this.displayName = displayName;
      this.centerAngleDegrees = centerAngleDegrees;
   }

   public String id() {
      return this.id;
   }

   public String displayName() {
      return this.displayName;
   }

   public double centerAngleDegrees() {
      return this.centerAngleDegrees;
   }

   public double centerAngleRadians() {
      return Math.toRadians(this.centerAngleDegrees);
   }

   public static RiftTerritory byIndex(int index) {
      return values()[Math.floorMod(index, values().length)];
   }

   public static RiftTerritory fromName(String name) {
      if (name == null) {
         return null;
      }

      String normalized = name.strip().toLowerCase().replace('-', '_').replace(' ', '_');

      for (RiftTerritory territory : values()) {
         if (territory.id.equals(normalized) || territory.name().equalsIgnoreCase(normalized)) {
            return territory;
         }
      }

      return null;
   }
}
