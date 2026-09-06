package dev.eness.sololevelingfinal.core.dungeon.data;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;

public final class DungeonDataTypes {
   private DungeonDataTypes() {
   }

   public record Bounds3(DungeonDataTypes.Int3 min, DungeonDataTypes.Int3 max) {
      public Bounds3 {
         int minX = Math.min(min.x, max.x);
         int minY = Math.min(min.y, max.y);
         int minZ = Math.min(min.z, max.z);
         int maxX = Math.max(min.x, max.x);
         int maxY = Math.max(min.y, max.y);
         int maxZ = Math.max(min.z, max.z);
         min = new DungeonDataTypes.Int3(minX, minY, minZ);
         max = new DungeonDataTypes.Int3(maxX, maxY, maxZ);
      }

      public boolean inside(DungeonDataTypes.Int3 size) {
         return size.contains(this.min) && size.contains(this.max);
      }
   }

   public enum DungeonKind {
      PRESET,
      PROCEDURAL,
      FIXED;

      static DungeonDataTypes.DungeonKind parse(String value) {
         if (!"fixed".equalsIgnoreCase(value) && !"fixed_layout".equalsIgnoreCase(value)) {
            return !"procedural".equalsIgnoreCase(value) && !"module_pool".equalsIgnoreCase(value) ? PRESET : PROCEDURAL;
         } else {
            return FIXED;
         }
      }
   }

   public enum DungeonTopology {
      LINEAR,
      BRANCHING;
   }

   public record Encounter(String id, Optional<String> triggerRegion, List<DungeonDataTypes.EncounterWave> waves, List<String> lockSockets) {
      public Encounter {
         triggerRegion = triggerRegion == null ? Optional.empty() : triggerRegion;
         waves = List.copyOf(waves);
         lockSockets = List.copyOf(lockSockets);
      }
   }

   public record EncounterWave(
      String id,
      String markerGroup,
      ResourceLocation mobPool,
      DungeonDataTypes.IntRange count,
      int delayTicks,
      boolean boss,
      Optional<DungeonDataTypes.IntRange> levelRange
   ) {
      public EncounterWave {
         levelRange = levelRange == null ? Optional.empty() : levelRange;
      }
   }

   public record EntitySelector(DungeonDataTypes.SelectorKind kind, ResourceLocation id) {
      public String key() {
         return (this.kind == DungeonDataTypes.SelectorKind.TAG ? "#" : "") + this.id;
      }
   }

   public record FixedRoomConnection(String fromRoom, String fromSocket, String toRoom, String toSocket) {
   }

   public record FixedRoomPlacement(String id, ResourceLocation room, DungeonDataTypes.Int3 position, Rotation rotation) {
   }

   public record Int3(int x, int y, int z) {
      public boolean isPositive() {
         return this.x > 0 && this.y > 0 && this.z > 0;
      }

      public boolean contains(DungeonDataTypes.Int3 position) {
         return position.x >= 0 && position.y >= 0 && position.z >= 0 && position.x < this.x && position.y < this.y && position.z < this.z;
      }
   }

   public record IntRange(int min, int max) {
      public IntRange {
         if (min > max) {
            int swap = min;
            min = max;
            max = swap;
         }
      }

      public boolean contains(int value) {
         return value >= this.min && value <= this.max;
      }

      public int random(RandomSource random) {
         if (this.min == this.max) {
            return this.min;
         }

         long width = (long)this.max - this.min + 1L;
         return (int)(this.min + Math.floorMod(random.nextLong(), width));
      }
   }

   public record LevelRule(String source, DungeonDataTypes.IntRange range, int variance) {
   }

   public record Marker(String id, String type, String group, DungeonDataTypes.Int3 position) {
      public boolean belongsTo(String idOrGroup) {
         return this.id.equals(idOrGroup) || !this.group.isBlank() && this.group.equals(idOrGroup);
      }
   }

   public record MobPoolEntry(
      DungeonDataTypes.EntitySelector selector,
      int weight,
      Optional<String> requiredMod,
      Optional<DungeonDataTypes.IntRange> eligibleLevel,
      Optional<DungeonDataTypes.IntRange> spawnLevel,
      Optional<Integer> baseXp
   ) {
      public MobPoolEntry {
         requiredMod = requiredMod == null ? Optional.empty() : requiredMod;
         eligibleLevel = eligibleLevel == null ? Optional.empty() : eligibleLevel;
         spawnLevel = spawnLevel == null ? Optional.empty() : spawnLevel;
         baseXp = baseXp == null ? Optional.empty() : baseXp;
      }

      public boolean eligibleAt(int dungeonLevel) {
         return this.eligibleLevel.isEmpty() || this.eligibleLevel.get().contains(dungeonLevel);
      }
   }

   public record MobPoolModifier(
      ResourceLocation id,
      ResourceLocation target,
      DungeonDataTypes.ModifierOperation operation,
      List<DungeonDataTypes.MobPoolEntry> entries,
      List<DungeonDataTypes.EntitySelector> selectors
   ) {
      public MobPoolModifier {
         entries = List.copyOf(entries);
         selectors = List.copyOf(selectors);
      }
   }

   public enum ModifierOperation {
      ADD,
      REMOVE;
   }

   public record Region(String id, String type, DungeonDataTypes.Bounds3 bounds) {
   }

   public enum RoomRole {
      START,
      NORMAL,
      JUNCTION,
      DEAD_END,
      TREASURE,
      BOSS,
      CAP,
      CORRIDOR,
      STAIR;

      static DungeonDataTypes.RoomRole parse(String value) {
         String normalized = value == null ? "normal" : value.toLowerCase(Locale.ROOT).replace('-', '_');

         return switch (normalized) {
            case "start", "entry", "entrance" -> START;
            case "junction", "branch" -> JUNCTION;
            case "dead_end", "deadend" -> DEAD_END;
            case "treasure", "reward" -> TREASURE;
            case "boss", "boss_room" -> BOSS;
            case "cap", "wall_cap" -> CAP;
            case "corridor", "hall" -> CORRIDOR;
            case "stair", "stairs" -> STAIR;
            default -> NORMAL;
         };
      }
   }

   public enum SelectorKind {
      ENTITY,
      TAG;
   }

   public enum Severity {
      WARNING,
      ERROR;
   }

   public record ShellSettings(boolean enabled, ResourceLocation block, int thickness, boolean coverFloor, boolean coverCeiling) {
   }

   public record Socket(String id, String type, DungeonDataTypes.Bounds3 opening, Direction facing, boolean required, int carveDepth) {
      public int insetFromBoundary(DungeonDataTypes.Int3 size) {
         return switch (this.facing) {
            case WEST -> this.opening.min.x;
            case EAST -> size.x - 1 - this.opening.max.x;
            case DOWN -> this.opening.min.y;
            case UP -> size.y - 1 - this.opening.max.y;
            case NORTH -> this.opening.min.z;
            case SOUTH -> size.z - 1 - this.opening.max.z;
         };
      }
   }

   public record ValidationIssue(DungeonDataTypes.Severity severity, ResourceLocation resource, String message) {
   }

   public record WeightedRoom(ResourceLocation room, int weight) {
   }
}
