package dev.eness.sololevelingfinal.core.dungeon;

public final class DungeonRoom {
   public final int gx;
   public final int gz;
   public final int width;
   public final int length;
   public final DungeonRoom.Type type;
   public final DungeonRoom.Shape shape;

   public DungeonRoom(int gx, int gz, int width, int length, DungeonRoom.Type type) {
      this(gx, gz, width, length, type, DungeonRoom.Shape.RECTANGLE);
   }

   public DungeonRoom(int gx, int gz, int width, int length, DungeonRoom.Type type, DungeonRoom.Shape shape) {
      this.gx = gx;
      this.gz = gz;
      this.width = width;
      this.length = length;
      this.type = type;
      this.shape = shape == null ? DungeonRoom.Shape.RECTANGLE : shape;
   }

   public DungeonRoom withType(DungeonRoom.Type newType) {
      return new DungeonRoom(this.gx, this.gz, this.width, this.length, newType, this.shape);
   }

   public int centerX() {
      return this.gx + this.width / 2;
   }

   public int centerZ() {
      return this.gz + this.length / 2;
   }

   public boolean containsGrid(int x, int z) {
      return this.containsLocal(x - this.gx, z - this.gz);
   }

   public boolean containsLocal(int x, int z) {
      if (x >= 0 && z >= 0 && x < this.width && z < this.length) {
         return switch (this.shape) {
            case RECTANGLE -> true;
            case CHAMFERED -> {
               int cut = Math.max(2, Math.min(4, Math.min(this.width, this.length) / 5));
               int edgeX = Math.min(x, this.width - 1 - x);
               int edgeZ = Math.min(z, this.length - 1 - z);
               yield edgeX + edgeZ >= cut - 1;
            }
            case ROUND -> {
               double radiusX = Math.max(1.0, (this.width - 1) * 0.5);
               double radiusZ = Math.max(1.0, (this.length - 1) * 0.5);
               double dx = (x - radiusX) / radiusX;
               double dz = (z - radiusZ) / radiusZ;
               yield dx * dx + dz * dz <= 1.04;
            }
            case CROSS -> {
               int halfX = Math.max(2, this.width / 6);
               int halfZ = Math.max(2, this.length / 6);
               yield Math.abs(x - this.width / 2) <= halfX || Math.abs(z - this.length / 2) <= halfZ;
            }
         };
      } else {
         return false;
      }
   }

   public boolean isEdgeLocal(int x, int z) {
      return !this.containsLocal(x, z)
         ? false
         : !this.containsLocal(x + 1, z) || !this.containsLocal(x - 1, z) || !this.containsLocal(x, z + 1) || !this.containsLocal(x, z - 1);
   }

   public boolean overlaps(DungeonRoom other, int margin) {
      int ax1 = this.gx - 1 - margin;
      int ax2 = this.gx + this.width + margin;
      int az1 = this.gz - 1 - margin;
      int az2 = this.gz + this.length + margin;
      int bx1 = other.gx - 1;
      int bx2 = other.gx + other.width;
      int bz1 = other.gz - 1;
      int bz2 = other.gz + other.length;
      return ax1 < bx2 && ax2 > bx1 && az1 < bz2 && az2 > bz1;
   }

   public enum Shape {
      RECTANGLE,
      CHAMFERED,
      ROUND,
      CROSS;
   }

   public enum Type {
      ENTRY,
      NORMAL,
      TREASURE,
      BOSS;
   }
}
