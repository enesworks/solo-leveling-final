package dev.eness.sololevelingfinal.core.dungeon.runtime.layout;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

public record DungeonLayoutPreview(
   boolean success,
   DungeonLayoutPlanner.PlanFailure failure,
   String message,
   ResourceLocation dungeonId,
   long dataRevision,
   long seed,
   DungeonLayoutTopology topology,
   int targetRoomCount,
   List<DungeonLayoutPreview.RoomView> rooms,
   List<DungeonLayoutPreview.ConnectionView> connections,
   DungeonLayoutPreview.Diagnostics diagnostics
) {
   public DungeonLayoutPreview {
      message = message == null ? "" : message;
      rooms = List.copyOf(rooms);
      connections = List.copyOf(connections);
      diagnostics = diagnostics == null ? DungeonLayoutPreview.Diagnostics.empty() : diagnostics;
   }

   public record Bounds(BlockPos min, BlockPos max) {
   }

   public record ConnectionView(int sourceRoom, String sourceSocket, int targetRoom, String targetSocket, DungeonLayoutPreview.Bounds carvedBounds) {
   }

   public record DiagnosticCount(DungeonLayoutPlanner.DiagnosticCode code, int count, String description) {
   }

   public record DiagnosticSample(
      DungeonLayoutPlanner.DiagnosticCode code,
      String detail,
      @Nullable ResourceLocation candidateRoom,
      @Nullable Rotation rotation,
      @Nullable DungeonLayoutPreview.Bounds candidateBounds,
      int conflictingRoom
   ) {
   }

   public record Diagnostics(
      int nodesVisited, boolean searchBudgetExhausted, List<DungeonLayoutPreview.DiagnosticCount> counts, List<DungeonLayoutPreview.DiagnosticSample> samples
   ) {
      public Diagnostics {
         counts = List.copyOf(counts);
         samples = List.copyOf(samples);
      }

      public static DungeonLayoutPreview.Diagnostics empty() {
         return new DungeonLayoutPreview.Diagnostics(0, false, List.of(), List.of());
      }
   }

   public record MarkerView(String id, String type, String group, BlockPos position) {
      public MarkerView {
         group = group == null ? "" : group;
      }
   }

   public record RoomView(
      int index,
      int depth,
      String placementKey,
      ResourceLocation definitionId,
      ResourceLocation structureId,
      String role,
      Rotation rotation,
      DungeonLayoutPreview.Bounds bounds,
      List<DungeonLayoutPreview.SocketView> sockets,
      List<DungeonLayoutPreview.MarkerView> markers
   ) {
      public RoomView {
         role = role == null ? "normal" : role;
         sockets = List.copyOf(sockets);
         markers = List.copyOf(markers);
      }
   }

   public record SocketView(String id, String type, boolean required, boolean connected, Direction facing, DungeonLayoutPreview.Bounds opening) {
   }
}
