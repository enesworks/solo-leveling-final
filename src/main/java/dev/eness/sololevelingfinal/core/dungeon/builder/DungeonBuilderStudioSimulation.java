package dev.eness.sololevelingfinal.core.dungeon.builder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Rotation;
import dev.eness.sololevelingfinal.core.dungeon.builder.model.DungeonDraft;
import dev.eness.sololevelingfinal.core.dungeon.builder.model.RoomSnapshot;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataTypes;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDefinition;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonRoomDefinition;
import dev.eness.sololevelingfinal.core.dungeon.runtime.layout.DungeonLayoutPlanner;
import dev.eness.sololevelingfinal.core.dungeon.runtime.layout.DungeonLayoutPreview;
import dev.eness.sololevelingfinal.core.dungeon.runtime.layout.DungeonLayoutTopology;

public final class DungeonBuilderStudioSimulation {
   private static final long[] COVERAGE_SEEDS = new long[]{0L, 5573589319906701683L, -7046029254386353131L};

   private DungeonBuilderStudioSimulation() {
   }

   public static CompoundTag simulatePreset(ServerPlayer player, DungeonBuilderProjectData.Project project, long seed) {
      Optional<RoomSnapshot> snapshot = project.roomSnapshot();
      if (snapshot.isEmpty()) {
         return failure(seed, "Capture the prebuilt dungeon before previewing it.");
      }

      DungeonBuilderRoomStore.VerificationResult verification = DungeonBuilderRoomStore.verify(player, project);
      if (!verification.valid()) {
         return failure(seed, verification.status() + ". Capture the prebuilt dungeon again.");
      }

      BlockPos size = snapshot.get().size();
      CompoundTag tag = new CompoundTag();
      tag.putLong("Seed", seed);
      tag.putString("Status", "SUCCESS");
      tag.putString("Message", "Prebuilt dungeon preview uses its exact captured structure.");
      tag.putInt("Attempts", 1);
      ListTag rooms = new ListTag();
      CompoundTag room = new CompoundTag();
      room.putString("Id", "room_0");
      room.putString("ProjectId", project.id());
      room.putString("Role", "START");
      room.putInt("X", 0);
      room.putInt("Z", 0);
      room.putInt("Width", size.getX());
      room.putInt("Depth", size.getZ());
      room.putInt("Rotation", 0);
      rooms.add(room);
      tag.put("Rooms", rooms);
      tag.put("Connections", new ListTag());
      tag.put("Diagnostics", new ListTag());
      return tag;
   }

   public static CompoundTag simulate(ServerPlayer player, DungeonBuilderProjectData data, ResourceLocation dungeonId, long seed) {
      DungeonBuilderStudioSimulation.Preparation preparation = prepare(player, data, dungeonId);
      return !preparation.success() ? failure(seed, preparation.message()) : encode(plan(player, preparation.simulation(), seed, 0).preview());
   }

   public static DungeonBuilderStudioSimulation.CoverageResult validateCoverage(ServerPlayer player, DungeonBuilderProjectData data, ResourceLocation dungeonId) {
      DungeonBuilderStudioSimulation.Preparation preparation = prepare(player, data, dungeonId);
      if (!preparation.success()) {
         return new DungeonBuilderStudioSimulation.CoverageResult(false, 0, 0L, 0, preparation.message());
      }

      DungeonBuilderStudioSimulation.PreparedSimulation simulation = preparation.simulation();
      if (simulation.draft().mode() == DungeonDraft.Mode.FIXED) {
         DungeonLayoutPlanner.PlanResult result = plan(player, simulation, 0L, 0);
         return !result.success()
            ? coverageFailure(1, 1, 0L, simulation.draft().fixedPlacements().size(), result)
            : new DungeonBuilderStudioSimulation.CoverageResult(
               true, 1, 0L, simulation.draft().fixedPlacements().size(), "Planner coverage passed 1 deterministic fixed-layout case."
            );
      }

      int minimum = simulation.draft().minRooms();
      int maximum = simulation.draft().maxRooms();
      LinkedHashSet<DungeonBuilderStudioSimulation.CoverageCase> cases = new LinkedHashSet<>();

      for (int targetRooms = minimum; targetRooms <= maximum; targetRooms++) {
         cases.add(new DungeonBuilderStudioSimulation.CoverageCase(targetRooms, COVERAGE_SEEDS[0]));
      }

      LinkedHashSet<Integer> representativeCounts = new LinkedHashSet<>();
      representativeCounts.add(minimum);
      representativeCounts.add(minimum + (maximum - minimum) / 2);
      representativeCounts.add(maximum);

      for (int targetRooms : representativeCounts) {
         for (int seedIndex = 1; seedIndex < COVERAGE_SEEDS.length; seedIndex++) {
            cases.add(new DungeonBuilderStudioSimulation.CoverageCase(targetRooms, COVERAGE_SEEDS[seedIndex]));
         }
      }

      int tested = 0;

      for (DungeonBuilderStudioSimulation.CoverageCase coverageCase : cases) {
         tested++;
         DungeonLayoutPlanner.PlanResult result = plan(player, simulation, coverageCase.seed(), coverageCase.targetRooms());
         if (!result.success()) {
            return coverageFailure(tested, cases.size(), coverageCase.seed(), coverageCase.targetRooms(), result);
         }
      }

      return new DungeonBuilderStudioSimulation.CoverageResult(
         true,
         tested,
         0L,
         0,
         "Planner coverage passed "
            + tested
            + " deterministic cases: every room count "
            + minimum
            + "-"
            + maximum
            + " at seed 0, plus "
            + representativeCounts
            + " at "
            + (COVERAGE_SEEDS.length - 1)
            + " additional seeds."
      );
   }

   private static DungeonBuilderStudioSimulation.CoverageResult coverageFailure(
      int tested, int total, long seed, int targetRooms, DungeonLayoutPlanner.PlanResult result
   ) {
      String detail = result.preview() != null && !result.preview().message().isBlank() ? result.preview().message() : result.message();
      return new DungeonBuilderStudioSimulation.CoverageResult(
         false,
         tested,
         seed,
         targetRooms,
         "Planner coverage case " + tested + "/" + total + " failed (target " + targetRooms + " rooms, seed " + seed + "): " + detail
      );
   }

   private static DungeonLayoutPlanner.PlanResult plan(
      ServerPlayer player, DungeonBuilderStudioSimulation.PreparedSimulation simulation, long seed, int targetRooms
   ) {
      return DungeonLayoutPlanner.plan(
         player.serverLevel(),
         simulation.catalog(),
         simulation.definition(),
         simulation.origin(),
         seed,
         new DungeonLayoutPlanner.PlanOptions(simulation.topology(), targetRooms, 0),
         DungeonLayoutPlanner.RoomTemplateResolver.declaredGeometry()
      );
   }

   private static DungeonBuilderStudioSimulation.Preparation prepare(
      final ServerPlayer player, final DungeonBuilderProjectData data, ResourceLocation dungeonId
   ) {
      Optional<DungeonDraft> optionalDraft = data.dungeonDraft(player, dungeonId);
      if (optionalDraft.isEmpty()) {
         return DungeonBuilderStudioSimulation.Preparation.failure("Save the Layout before simulating it.");
      }

      DungeonDraft draft = optionalDraft.get();
      Map<String, DungeonBuilderProjectData.Project> projects = new HashMap<>();

      for (DungeonBuilderProjectData.Project project : data.projects(player)) {
         projects.put(project.id(), project);
      }

      final Map<ResourceLocation, DungeonRoomDefinition> rooms = new HashMap<>();
      LinkedHashSet<ResourceLocation> referencedRooms = new LinkedHashSet<>();
      if (draft.mode() == DungeonDraft.Mode.FIXED) {
         draft.fixedPlacements().forEach(placement -> referencedRooms.add(placement.room()));
      } else {
         draft.rooms().forEach(reference -> referencedRooms.add(reference.room()));
      }

      for (ResourceLocation roomId : referencedRooms) {
         DungeonBuilderProjectData.Project project = projects.get(roomId.toString());
         if (project == null) {
            return DungeonBuilderStudioSimulation.Preparation.failure("Missing room asset " + roomId + ".");
         }

         if (project.roomSnapshot().isEmpty()) {
            return DungeonBuilderStudioSimulation.Preparation.failure("Capture room " + project.id() + " before simulating this dungeon.");
         }

         DungeonBuilderRoomStore.VerificationResult verification = DungeonBuilderRoomStore.verify(player, project);
         if (!verification.valid()) {
            return DungeonBuilderStudioSimulation.Preparation.failure(
               project.id() + ": " + verification.status() + ". Capture the room again before previewing it."
            );
         }

         rooms.put(roomId, room(project));
      }

      if (rooms.isEmpty()) {
         return DungeonBuilderStudioSimulation.Preparation.failure(
            draft.mode() == DungeonDraft.Mode.FIXED
               ? "Place at least a start and boss room in Layout."
               : "Enable at least a start, middle, and boss room in Layout."
         );
      }

      DungeonLayoutPlanner.RoomCatalog catalog = new DungeonLayoutPlanner.RoomCatalog() {
         @Override
         public DungeonRoomDefinition find(ResourceLocation id) {
            return rooms.get(id);
         }

         @Override
         public long revision() {
            return data.revision(player);
         }
      };
      BlockPos origin = new BlockPos(
         0, Math.max(player.serverLevel().getMinBuildHeight() + 8, Math.min(player.getBlockY(), player.serverLevel().getMaxBuildHeight() - 64)), 0
      );
      DungeonDefinition definition = draft.mode() == DungeonDraft.Mode.FIXED ? fixedDungeon(draft) : dungeon(draft, rooms);
      DungeonLayoutTopology topology = draft.mode() == DungeonDraft.Mode.FIXED
         ? DungeonLayoutTopology.FIXED
         : (draft.topology() == DungeonDraft.Topology.BRANCHING ? DungeonLayoutTopology.BRANCHING : DungeonLayoutTopology.LINEAR);
      return DungeonBuilderStudioSimulation.Preparation.success(
         new DungeonBuilderStudioSimulation.PreparedSimulation(draft, catalog, definition, origin, topology)
      );
   }

   private static DungeonDefinition fixedDungeon(DungeonDraft draft) {
      List<DungeonDataTypes.FixedRoomPlacement> placements = draft.fixedPlacements()
         .stream()
         .map(
            placement -> new DungeonDataTypes.FixedRoomPlacement(
               placement.id(),
               placement.room(),
               new DungeonDataTypes.Int3(placement.x(), placement.y(), placement.z()),
               Rotation.valueOf(placement.rotation().name())
            )
         )
         .toList();
      List<DungeonDataTypes.FixedRoomConnection> connections = draft.fixedConnections()
         .stream()
         .map(
            connection -> new DungeonDataTypes.FixedRoomConnection(
               connection.fromPlacement(), connection.fromSocket(), connection.toPlacement(), connection.toSocket()
            )
         )
         .toList();
      int count = Math.max(1, placements.size());
      return new DungeonDefinition(
         draft.id(),
         2,
         DungeonDataTypes.DungeonKind.FIXED,
         draft.allowedRanks(),
         Optional.empty(),
         Optional.empty(),
         Optional.empty(),
         Optional.empty(),
         Optional.empty(),
         Map.of(),
         new DungeonDataTypes.IntRange(count, count),
         count,
         new DungeonDataTypes.LevelRule("owner", new DungeonDataTypes.IntRange(1, 1000), 0),
         new DungeonDataTypes.ShellSettings(draft.shellThickness() > 0, draft.shellBlock(), draft.shellThickness(), true, true),
         List.of(),
         List.of(),
         List.of(),
         List.of(),
         placements,
         connections
      );
   }

   private static DungeonDefinition dungeon(DungeonDraft draft, Map<ResourceLocation, DungeonRoomDefinition> rooms) {
      EnumMap<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> pools = new EnumMap<>(DungeonDataTypes.RoomRole.class);

      for (DungeonDraft.RoomRef reference : draft.rooms()) {
         DungeonRoomDefinition room = rooms.get(reference.room());
         if (room != null) {
            pools.computeIfAbsent(room.role(), ignored -> new ArrayList<>()).add(new DungeonDataTypes.WeightedRoom(reference.room(), reference.weight()));
         }
      }

      return new DungeonDefinition(
         draft.id(),
         2,
         DungeonDataTypes.DungeonKind.PROCEDURAL,
         draft.allowedRanks(),
         Optional.empty(),
         Optional.empty(),
         Optional.empty(),
         Optional.empty(),
         Optional.empty(),
         pools,
         new DungeonDataTypes.IntRange(draft.minRooms(), draft.maxRooms()),
         draft.maxDepth(),
         new DungeonDataTypes.LevelRule("owner", new DungeonDataTypes.IntRange(1, 1000), 0),
         new DungeonDataTypes.ShellSettings(draft.shellThickness() > 0, draft.shellBlock(), draft.shellThickness(), true, true),
         List.of(),
         List.of(),
         List.of(),
         List.of(),
         List.of(),
         List.of(),
         draft.topology() == DungeonDraft.Topology.BRANCHING ? DungeonDataTypes.DungeonTopology.BRANCHING : DungeonDataTypes.DungeonTopology.LINEAR
      );
   }

   private static DungeonRoomDefinition room(DungeonBuilderProjectData.Project project) {
      RoomSnapshot snapshot = project.roomSnapshot().orElseThrow();
      BlockPos base = snapshot.captureMin();
      DungeonDataTypes.Int3 size = int3(snapshot.size());
      List<DungeonDataTypes.Region> regions = project.regions()
         .stream()
         .map(
            region -> new DungeonDataTypes.Region(
               region.id(), region.type(), new DungeonDataTypes.Bounds3(int3(region.bounds().min().subtract(base)), int3(region.bounds().max().subtract(base)))
            )
         )
         .toList();
      List<DungeonDataTypes.Socket> sockets = project.sockets()
         .stream()
         .map(
            socket -> new DungeonDataTypes.Socket(
               socket.id(),
               socket.type(),
               new DungeonDataTypes.Bounds3(int3(socket.opening().min().subtract(base)), int3(socket.opening().max().subtract(base))),
               socket.facing(),
               socket.required(),
               1
            )
         )
         .toList();
      List<DungeonDataTypes.Marker> markers = project.markers()
         .stream()
         .map(marker -> new DungeonDataTypes.Marker(marker.id(), marker.type(), marker.group(), int3(marker.position().subtract(base))))
         .toList();
      return new DungeonRoomDefinition(
         new ResourceLocation(project.namespace(), project.name()),
         2,
         snapshot.structureKey(),
         roomRole(project.roomRole()),
         project.roomWeight(),
         size,
         new DungeonDataTypes.Int3(0, 0, 0),
         Optional.empty(),
         Optional.empty(),
         regions,
         sockets,
         markers,
         List.of()
      );
   }

   private static DungeonDataTypes.RoomRole roomRole(DungeonBuilderProjectData.RoomRole role) {
      try {
         return DungeonDataTypes.RoomRole.valueOf(role.name());
      } catch (IllegalArgumentException ignored) {
         return DungeonDataTypes.RoomRole.NORMAL;
      }
   }

   private static DungeonDataTypes.Int3 int3(BlockPos position) {
      return new DungeonDataTypes.Int3(position.getX(), position.getY(), position.getZ());
   }

   private static CompoundTag encode(DungeonLayoutPreview preview) {
      CompoundTag tag = new CompoundTag();
      tag.putLong("Seed", preview.seed());
      tag.putString("Status", preview.success() ? "SUCCESS" : "FAILED");
      tag.putString(
         "Message",
         preview.success() ? "Planned " + preview.rooms().size() + " rooms with " + preview.connections().size() + " connections." : preview.message()
      );
      tag.putInt("Attempts", preview.diagnostics().nodesVisited());
      ListTag rooms = new ListTag();

      for (DungeonLayoutPreview.RoomView room : preview.rooms()) {
         CompoundTag value = new CompoundTag();
         value.putString("Id", "room_" + room.index());
         value.putString("ProjectId", room.definitionId().toString());
         value.putString("Role", room.role());
         value.putInt("X", room.bounds().min().getX());
         value.putInt("Z", room.bounds().min().getZ());
         value.putInt("Width", room.bounds().max().getX() - room.bounds().min().getX() + 1);
         value.putInt("Depth", room.bounds().max().getZ() - room.bounds().min().getZ() + 1);
         value.putInt("Rotation", degrees(room.rotation()));
         rooms.add(value);
      }

      tag.put("Rooms", rooms);
      ListTag connections = new ListTag();

      for (DungeonLayoutPreview.ConnectionView connection : preview.connections()) {
         CompoundTag value = new CompoundTag();
         value.putString("FromRoom", "room_" + connection.sourceRoom());
         value.putString("ToRoom", "room_" + connection.targetRoom());
         connections.add(value);
      }

      tag.put("Connections", connections);
      ListTag diagnostics = new ListTag();

      for (DungeonLayoutPreview.DiagnosticCount diagnostic : preview.diagnostics().counts()) {
         CompoundTag value = new CompoundTag();
         value.putString("Code", diagnostic.code().name());
         value.putInt("Count", diagnostic.count());
         value.putString("Message", diagnostic.description());
         diagnostics.add(value);
      }

      tag.put("Diagnostics", diagnostics);
      return tag;
   }

   private static int degrees(Rotation rotation) {
      return switch (rotation) {
         case CLOCKWISE_90 -> 90;
         case CLOCKWISE_180 -> 180;
         case COUNTERCLOCKWISE_90 -> 270;
         default -> 0;
      };
   }

   private static CompoundTag failure(long seed, String message) {
      CompoundTag tag = new CompoundTag();
      tag.putLong("Seed", seed);
      tag.putString("Status", "FAILED");
      tag.putString("Message", message);
      tag.put("Rooms", new ListTag());
      tag.put("Connections", new ListTag());
      return tag;
   }

   private record CoverageCase(int targetRooms, long seed) {
   }

   public record CoverageResult(boolean success, int casesTested, long failedSeed, int targetRooms, String message) {
      public CoverageResult {
         casesTested = Math.max(0, casesTested);
         targetRooms = Math.max(0, targetRooms);
         message = message != null && !message.isBlank() ? message : (success ? "Planner coverage passed." : "Planner coverage failed.");
      }
   }

   private record Preparation(@Nullable DungeonBuilderStudioSimulation.PreparedSimulation simulation, String message) {
      private static DungeonBuilderStudioSimulation.Preparation success(DungeonBuilderStudioSimulation.PreparedSimulation simulation) {
         return new DungeonBuilderStudioSimulation.Preparation(simulation, "");
      }

      private static DungeonBuilderStudioSimulation.Preparation failure(String message) {
         return new DungeonBuilderStudioSimulation.Preparation(null, message == null ? "Studio layout preparation failed." : message);
      }

      private boolean success() {
         return this.simulation != null;
      }
   }

   private record PreparedSimulation(
      DungeonDraft draft, DungeonLayoutPlanner.RoomCatalog catalog, DungeonDefinition definition, BlockPos origin, DungeonLayoutTopology topology
   ) {
   }
}
