package dev.eness.sololevelingfinal.core.dungeon.builder.model;

import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;

public record DungeonDraft(
   ResourceLocation id,
   DungeonDraft.Mode mode,
   DungeonDraft.Topology topology,
   List<DungeonDraft.RoomRef> rooms,
   Set<ProceduralDungeonRank> allowedRanks,
   int minRooms,
   int maxRooms,
   int maxDepth,
   ResourceLocation shellBlock,
   int shellThickness,
   List<DungeonDraft.FixedPlacement> fixedPlacements,
   List<DungeonDraft.FixedConnection> fixedConnections
) {
   public DungeonDraft {
      if (id == null) {
         throw new IllegalArgumentException("Dungeon draft id is required.");
      }

      if (mode == null || topology == null) {
         throw new IllegalArgumentException("Dungeon mode and topology are required.");
      }

      if (shellBlock == null) {
         throw new IllegalArgumentException("Shell block is required.");
      }

      rooms = rooms == null ? List.of() : List.copyOf(rooms);
      allowedRanks = allowedRanks == null ? Set.of() : Set.copyOf(allowedRanks);
      fixedPlacements = fixedPlacements == null ? List.of() : List.copyOf(fixedPlacements);
      fixedConnections = fixedConnections == null ? List.of() : List.copyOf(fixedConnections);
   }

   public record FixedConnection(String fromPlacement, String fromSocket, String toPlacement, String toSocket) {
      public FixedConnection(String fromPlacement, String fromSocket, String toPlacement, String toSocket) {
         if (fromPlacement != null && fromSocket != null && toPlacement != null && toSocket != null) {
            this.fromPlacement = fromPlacement;
            this.fromSocket = fromSocket;
            this.toPlacement = toPlacement;
            this.toSocket = toSocket;
         } else {
            throw new IllegalArgumentException("Both fixed-connection endpoints are required.");
         }
      }
   }

   public record FixedPlacement(String id, ResourceLocation room, int x, int y, int z, DungeonDraft.PlacementRotation rotation) {
      public FixedPlacement(String id, ResourceLocation room, int x, int y, int z, DungeonDraft.PlacementRotation rotation) {
         if (id != null && room != null && rotation != null) {
            this.id = id;
            this.room = room;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotation = rotation;
         } else {
            throw new IllegalArgumentException("Placement id, room, and rotation are required.");
         }
      }
   }

   public enum Mode {
      PROCEDURAL,
      FIXED;
   }

   public enum PlacementRotation {
      NONE,
      CLOCKWISE_90,
      CLOCKWISE_180,
      COUNTERCLOCKWISE_90;
   }

   public record RoomRef(ResourceLocation room, int weight) {
      public RoomRef {
         if (room == null) {
            throw new IllegalArgumentException("Room id is required.");
         }
      }
   }

   public enum Topology {
      LINEAR,
      BRANCHING;
   }
}
