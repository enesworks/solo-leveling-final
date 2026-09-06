package dev.eness.sololevelingfinal.core.dungeon.runtime.layout;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonRoomDefinition;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonTemplatePlacer;

public record DungeonLayoutPlan(List<DungeonLayoutPlan.PlannedRoom> rooms, List<DungeonLayoutPlan.PlannedConnection> connections) {
   public DungeonLayoutPlan {
      rooms = List.copyOf(rooms);
      connections = List.copyOf(connections);
   }

   public boolean runtimePlaceable() {
      return this.rooms.stream().allMatch(room -> room.geometry().runtimePlaceable());
   }

   public record PlannedConnection(int sourceRoom, String sourceSocket, int targetRoom, String targetSocket, DungeonTemplatePlacer.WorldBounds bounds) {
   }

   public static final class PlannedRoom {
      private final int index;
      private final int depth;
      private final String placementKey;
      @Nullable
      private final DungeonRoomDefinition definition;
      private final ResourceLocation definitionId;
      private final DungeonRoomGeometry geometry;
      private final Set<String> usedSockets;

      public PlannedRoom(int index, int depth, DungeonRoomDefinition definition, DungeonTemplatePlacer.PreparedTemplate prepared, Set<String> usedSockets) {
         this("room_" + index, index, depth, definition, prepared, usedSockets);
      }

      public PlannedRoom(int index, int depth, DungeonRoomDefinition definition, DungeonRoomGeometry geometry, Set<String> usedSockets) {
         this("room_" + index, index, depth, definition, geometry, usedSockets);
      }

      public PlannedRoom(
         String placementKey, int index, int depth, DungeonRoomDefinition definition, DungeonTemplatePlacer.PreparedTemplate prepared, Set<String> usedSockets
      ) {
         this(placementKey, index, depth, definition, DungeonRoomGeometry.loaded(prepared), usedSockets);
      }

      public PlannedRoom(String placementKey, int index, int depth, DungeonRoomDefinition definition, DungeonRoomGeometry geometry, Set<String> usedSockets) {
         this.index = index;
         this.depth = depth;
         this.placementKey = placementKey != null && !placementKey.isBlank() ? placementKey : "room_" + index;
         this.definition = definition;
         this.definitionId = definition.id();
         this.geometry = geometry;
         this.usedSockets = Set.copyOf(usedSockets);
      }

      private PlannedRoom(ResourceLocation definitionId, DungeonTemplatePlacer.PreparedTemplate prepared) {
         this.index = 0;
         this.depth = 0;
         this.placementKey = "preset";
         this.definition = null;
         this.definitionId = definitionId;
         this.geometry = DungeonRoomGeometry.loaded(prepared);
         this.usedSockets = Set.of();
      }

      public static DungeonLayoutPlan.PlannedRoom preset(ResourceLocation dungeonId, DungeonTemplatePlacer.PreparedTemplate prepared) {
         return new DungeonLayoutPlan.PlannedRoom(dungeonId, prepared);
      }

      public int index() {
         return this.index;
      }

      public int depth() {
         return this.depth;
      }

      public String placementKey() {
         return this.placementKey;
      }

      public boolean preset() {
         return this.definition == null;
      }

      public DungeonRoomDefinition definition() {
         if (this.definition == null) {
            throw new IllegalStateException("Preset room has no module definition");
         } else {
            return this.definition;
         }
      }

      @Nullable
      public DungeonRoomDefinition definitionOrNull() {
         return this.definition;
      }

      public ResourceLocation definitionId() {
         return this.definitionId;
      }

      public DungeonTemplatePlacer.PreparedTemplate prepared() {
         return this.geometry.runtimeTemplate();
      }

      public DungeonRoomGeometry geometry() {
         return this.geometry;
      }

      public Set<String> usedSockets() {
         return this.usedSockets;
      }

      public DungeonLayoutPlan.PlannedRoom withUsedSocket(String socketId) {
         if (this.usedSockets.contains(socketId)) {
            return this;
         }

         Set<String> next = new HashSet<>(this.usedSockets);
         next.add(socketId);
         return new DungeonLayoutPlan.PlannedRoom(this.placementKey, this.index, this.depth, this.definition(), this.geometry, next);
      }
   }
}
