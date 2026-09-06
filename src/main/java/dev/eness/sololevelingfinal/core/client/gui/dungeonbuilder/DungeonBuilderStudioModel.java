package dev.eness.sololevelingfinal.core.client.gui.dungeonbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public record DungeonBuilderStudioModel(
   long revision,
   String selectedProjectId,
   String selectedPoolId,
   String dungeonId,
   boolean loading,
   List<DungeonBuilderStudioModel.Project> projects,
   List<DungeonBuilderStudioModel.MobPool> pools,
   DungeonBuilderStudioModel.LayoutDraft layout,
   DungeonBuilderStudioModel.Simulation simulation,
   DungeonBuilderStudioModel.ValidationSummary validation,
   DungeonBuilderStudioModel.Notice notice,
   List<DungeonBuilderStudioModel.DraftSummary> dungeonDrafts
) {
   public static final int MAX_PROJECTS = 128;
   public static final int MAX_POOLS = 128;
   public static final int MAX_SOCKETS = 64;
   public static final int MAX_ANCHORS = 320;
   public static final int MAX_POOL_ENTRIES = 256;
   public static final int MAX_FOOTPRINT_CELLS = 2304;
   public static final int MAX_LAYOUT_ROOMS = 64;
   public static final int MAX_CONNECTIONS = 128;
   public static final int MAX_ISSUES = 128;
   public static final int MAX_DUNGEON_DRAFTS = 64;
   private static final int ID_LENGTH = 128;
   private static final int RESOURCE_ID_LENGTH = 192;
   private static final int LABEL_LENGTH = 96;
   private static final int MESSAGE_LENGTH = 320;

   public DungeonBuilderStudioModel {
      revision = Math.max(0L, revision);
      selectedProjectId = text(selectedProjectId, 128);
      selectedPoolId = text(selectedPoolId, 192);
      dungeonId = defaultText(dungeonId, "my_dungeon", 192);
      projects = bounded(projects, 128, Function.identity());
      pools = bounded(pools, 128, Function.identity());
      layout = layout == null ? DungeonBuilderStudioModel.LayoutDraft.empty() : layout;
      simulation = simulation == null ? DungeonBuilderStudioModel.Simulation.empty() : simulation;
      validation = validation == null ? DungeonBuilderStudioModel.ValidationSummary.notRun() : validation;
      notice = notice == null ? DungeonBuilderStudioModel.Notice.none() : notice;
      dungeonDrafts = bounded(dungeonDrafts, 64, Function.identity());
   }

   public DungeonBuilderStudioModel(
      long revision,
      String selectedProjectId,
      String selectedPoolId,
      String dungeonId,
      boolean loading,
      List<DungeonBuilderStudioModel.Project> projects,
      List<DungeonBuilderStudioModel.MobPool> pools,
      DungeonBuilderStudioModel.LayoutDraft layout,
      DungeonBuilderStudioModel.Simulation simulation,
      DungeonBuilderStudioModel.ValidationSummary validation,
      DungeonBuilderStudioModel.Notice notice
   ) {
      this(revision, selectedProjectId, selectedPoolId, dungeonId, loading, projects, pools, layout, simulation, validation, notice, List.of());
   }

   public static DungeonBuilderStudioModel empty() {
      return new DungeonBuilderStudioModel(
         0L,
         "",
         "",
         "my_dungeon",
         false,
         List.of(),
         List.of(),
         DungeonBuilderStudioModel.LayoutDraft.empty(),
         DungeonBuilderStudioModel.Simulation.empty(),
         DungeonBuilderStudioModel.ValidationSummary.notRun(),
         DungeonBuilderStudioModel.Notice.none()
      );
   }

   public static DungeonBuilderStudioModel loadingState() {
      return new DungeonBuilderStudioModel(
         0L,
         "",
         "",
         "my_dungeon",
         true,
         List.of(),
         List.of(),
         DungeonBuilderStudioModel.LayoutDraft.empty(),
         DungeonBuilderStudioModel.Simulation.empty(),
         DungeonBuilderStudioModel.ValidationSummary.notRun(),
         new DungeonBuilderStudioModel.Notice(DungeonBuilderStudioModel.Severity.INFO, "Loading builder workspace...")
      );
   }

   public Optional<DungeonBuilderStudioModel.Project> selectedProject() {
      return this.project(this.selectedProjectId);
   }

   public Optional<DungeonBuilderStudioModel.Project> project(String id) {
      String target = text(id, 128);
      return this.projects.stream().filter(project -> project.id().equals(target)).findFirst();
   }

   public Optional<DungeonBuilderStudioModel.MobPool> selectedPool() {
      return this.pool(this.selectedPoolId);
   }

   public Optional<DungeonBuilderStudioModel.MobPool> pool(String id) {
      String target = text(id, 192);
      return this.pools.stream().filter(pool -> pool.id().equals(target)).findFirst();
   }

   public Optional<DungeonBuilderStudioModel.DraftSummary> draft(String id) {
      String target = text(id, 192);
      return this.dungeonDrafts.stream().filter(draft -> draft.id().equals(target)).findFirst();
   }

   private static int saturatedSize(int min, int max) {
      long value = (long)max - min + 1L;
      return (int)Math.min(2147483647L, Math.max(1L, value));
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static String defaultText(String value, String fallback, int maximum) {
      String clean = text(value, maximum);
      return clean.isBlank() ? text(fallback, maximum) : clean;
   }

   private static String text(String value, int maximum) {
      if (value == null) {
         return "";
      }

      String clean = value.replace('\u0000', ' ').trim();
      return clean.length() <= maximum ? clean : clean.substring(0, maximum);
   }

   private static Set<String> boundedSet(Set<String> source, int maximum, int stringLength) {
      if (source != null && !source.isEmpty()) {
         LinkedHashSet<String> result = new LinkedHashSet<>();

         for (String value : source) {
            if (result.size() >= maximum) {
               break;
            }

            String clean = text(value, stringLength).toUpperCase(Locale.ROOT);
            if (!clean.isBlank()) {
               result.add(clean);
            }
         }

         return Collections.unmodifiableSet(result);
      } else {
         return Set.of();
      }
   }

   private static <T, R> List<R> bounded(List<T> source, int maximum, Function<T, R> mapper) {
      if (source != null && !source.isEmpty()) {
         int size = Math.min(maximum, source.size());
         List<R> result = new ArrayList<>(size);

         for (int index = 0; index < size; index++) {
            T value = source.get(index);
            if (value != null) {
               R mapped = mapper.apply(value);
               if (mapped != null) {
                  result.add(mapped);
               }
            }
         }

         return List.copyOf(result);
      } else {
         return List.of();
      }
   }

   public record Anchor(
      String id,
      DungeonBuilderStudioModel.AnchorKind kind,
      DungeonBuilderStudioModel.SpawnRole spawnRole,
      DungeonBuilderStudioModel.Point position,
      DungeonBuilderStudioModel.Bounds triggerBounds,
      String encounterId,
      String poolId,
      boolean levelOverride,
      int minLevel,
      int maxLevel,
      boolean delayed
   ) {
      public Anchor {
         id = DungeonBuilderStudioModel.defaultText(id, "anchor", 128);
         kind = kind == null ? DungeonBuilderStudioModel.AnchorKind.UNASSIGNED : kind;
         spawnRole = spawnRole == null ? DungeonBuilderStudioModel.SpawnRole.NONE : spawnRole;
         position = position == null ? new DungeonBuilderStudioModel.Point(0, 0, 0) : position;
         encounterId = DungeonBuilderStudioModel.defaultText(encounterId, "default", 64);
         poolId = DungeonBuilderStudioModel.text(poolId, 192);
         minLevel = DungeonBuilderStudioModel.clamp(minLevel, 1, 1000);
         maxLevel = DungeonBuilderStudioModel.clamp(maxLevel, minLevel, 1000);
      }

      public Anchor(
         String id,
         DungeonBuilderStudioModel.AnchorKind kind,
         DungeonBuilderStudioModel.SpawnRole spawnRole,
         DungeonBuilderStudioModel.Point position,
         DungeonBuilderStudioModel.Bounds triggerBounds,
         String encounterId,
         String poolId,
         int minLevel,
         int maxLevel,
         boolean delayed
      ) {
         this(id, kind, spawnRole, position, triggerBounds, encounterId, poolId, true, minLevel, maxLevel, delayed);
      }
   }

   public enum AnchorKind {
      UNASSIGNED("?"),
      SPAWN_POINT("S"),
      MOB_SPAWN("M"),
      ELITE_SPAWN("E"),
      BOSS_SPAWN("B"),
      PLAYER_START("P"),
      RETURN_PORTAL("R"),
      TRIGGER("T"),
      LOOT("L"),
      CUSTOM("?");

      private final String symbol;

      AnchorKind(String symbol) {
         this.symbol = symbol;
      }

      public String symbol() {
         return this.symbol;
      }
   }

   public record Bounds(DungeonBuilderStudioModel.Point min, DungeonBuilderStudioModel.Point max) {
      public Bounds {
         DungeonBuilderStudioModel.Point a = min == null ? new DungeonBuilderStudioModel.Point(0, 0, 0) : min;
         DungeonBuilderStudioModel.Point b = max == null ? a : max;
         min = new DungeonBuilderStudioModel.Point(Math.min(a.x(), b.x()), Math.min(a.y(), b.y()), Math.min(a.z(), b.z()));
         max = new DungeonBuilderStudioModel.Point(Math.max(a.x(), b.x()), Math.max(a.y(), b.y()), Math.max(a.z(), b.z()));
      }

      public int width() {
         return DungeonBuilderStudioModel.saturatedSize(this.min.x(), this.max.x());
      }

      public int height() {
         return DungeonBuilderStudioModel.saturatedSize(this.min.y(), this.max.y());
      }

      public int depth() {
         return DungeonBuilderStudioModel.saturatedSize(this.min.z(), this.max.z());
      }

      public boolean contains(DungeonBuilderStudioModel.Point point) {
         return point != null
            && point.x() >= this.min.x()
            && point.x() <= this.max.x()
            && point.y() >= this.min.y()
            && point.y() <= this.max.y()
            && point.z() >= this.min.z()
            && point.z() <= this.max.z();
      }
   }

   public record DraftSummary(
      String id, DungeonBuilderStudioModel.LayoutMode mode, DungeonBuilderStudioModel.Topology topology, int roomCount, int placementCount
   ) {
      public DraftSummary {
         id = DungeonBuilderStudioModel.defaultText(id, "builder:my_dungeon", 192);
         mode = mode == null ? DungeonBuilderStudioModel.LayoutMode.PROCEDURAL : mode;
         topology = topology == null ? DungeonBuilderStudioModel.Topology.LINEAR : topology;
         roomCount = DungeonBuilderStudioModel.clamp(roomCount, 0, 128);
         placementCount = DungeonBuilderStudioModel.clamp(placementCount, 0, 64);
      }
   }

   public enum Facing {
      NORTH(0, 0, -1),
      EAST(1, 0, 0),
      SOUTH(0, 0, 1),
      WEST(-1, 0, 0),
      UP(0, 1, 0),
      DOWN(0, -1, 0);

      private final int stepX;
      private final int stepY;
      private final int stepZ;

      Facing(int stepX, int stepY, int stepZ) {
         this.stepX = stepX;
         this.stepY = stepY;
         this.stepZ = stepZ;
      }

      public int stepX() {
         return this.stepX;
      }

      public int stepZ() {
         return this.stepZ;
      }

      public int stepY() {
         return this.stepY;
      }
   }

   public record FootprintCell(int x, int z, int argb) {
      public FootprintCell {
         x = DungeonBuilderStudioModel.clamp(x, 0, 47);
         z = DungeonBuilderStudioModel.clamp(z, 0, 47);
         if (argb >>> 24 == 0) {
            argb |= -16777216;
         }
      }
   }

   public record LayoutConnection(String fromNodeId, String fromSocketId, String toNodeId, String toSocketId) {
      public LayoutConnection {
         fromNodeId = DungeonBuilderStudioModel.text(fromNodeId, 128);
         fromSocketId = DungeonBuilderStudioModel.text(fromSocketId, 128);
         toNodeId = DungeonBuilderStudioModel.text(toNodeId, 128);
         toSocketId = DungeonBuilderStudioModel.text(toSocketId, 128);
      }
   }

   public record LayoutDraft(
      DungeonBuilderStudioModel.LayoutMode mode,
      DungeonBuilderStudioModel.Topology topology,
      int minRooms,
      int maxRooms,
      int maxDepth,
      Set<String> ranks,
      String shellBlock,
      int shellThickness,
      List<String> enabledProjectIds,
      List<DungeonBuilderStudioModel.RoomWeight> roomWeights,
      List<DungeonBuilderStudioModel.LayoutNode> nodes,
      List<DungeonBuilderStudioModel.LayoutConnection> connections
   ) {
      public LayoutDraft {
         mode = mode == null ? DungeonBuilderStudioModel.LayoutMode.PROCEDURAL : mode;
         topology = topology == null ? DungeonBuilderStudioModel.Topology.LINEAR : topology;
         int minimumRooms = mode == DungeonBuilderStudioModel.LayoutMode.PROCEDURAL ? (topology == DungeonBuilderStudioModel.Topology.BRANCHING ? 4 : 3) : 1;
         minRooms = DungeonBuilderStudioModel.clamp(minRooms, minimumRooms, 64);
         maxRooms = DungeonBuilderStudioModel.clamp(maxRooms, minRooms, 64);
         maxDepth = DungeonBuilderStudioModel.clamp(maxDepth, 1, 64);
         ranks = DungeonBuilderStudioModel.boundedSet(ranks, 8, 16);
         shellBlock = DungeonBuilderStudioModel.defaultText(shellBlock, "minecraft:bedrock", 128);
         shellThickness = DungeonBuilderStudioModel.clamp(shellThickness, 0, 4);
         enabledProjectIds = DungeonBuilderStudioModel.bounded(enabledProjectIds, 128, value -> DungeonBuilderStudioModel.text(value, 128));
         roomWeights = DungeonBuilderStudioModel.bounded(roomWeights, 128, Function.identity());
         nodes = DungeonBuilderStudioModel.bounded(nodes, 64, Function.identity());
         connections = DungeonBuilderStudioModel.bounded(connections, 128, Function.identity());
      }

      public static DungeonBuilderStudioModel.LayoutDraft empty() {
         return new DungeonBuilderStudioModel.LayoutDraft(
            DungeonBuilderStudioModel.LayoutMode.PROCEDURAL,
            DungeonBuilderStudioModel.Topology.LINEAR,
            3,
            8,
            16,
            Set.of(),
            "minecraft:bedrock",
            1,
            List.of(),
            List.of(),
            List.of(),
            List.of()
         );
      }
   }

   public enum LayoutMode {
      PROCEDURAL,
      FIXED;
   }

   public record LayoutNode(
      String id, String projectId, DungeonBuilderStudioModel.RoomRole role, int x, int y, int z, int width, int depth, int rotation, boolean locked
   ) {
      public LayoutNode {
         id = DungeonBuilderStudioModel.defaultText(id, "node", 128);
         projectId = DungeonBuilderStudioModel.defaultText(projectId, "unknown:room", 128);
         role = role == null ? DungeonBuilderStudioModel.RoomRole.NORMAL : role;
         width = DungeonBuilderStudioModel.clamp(width, 1, 512);
         depth = DungeonBuilderStudioModel.clamp(depth, 1, 512);
         rotation = Math.floorMod(rotation, 360);
      }
   }

   public record LevelRange(boolean present, int min, int max) {
      public LevelRange {
         min = DungeonBuilderStudioModel.clamp(min, 1, 1000);
         max = DungeonBuilderStudioModel.clamp(max, min, 1000);
      }

      public static DungeonBuilderStudioModel.LevelRange unset() {
         return new DungeonBuilderStudioModel.LevelRange(false, 1, 1);
      }

      public static DungeonBuilderStudioModel.LevelRange of(int min, int max) {
         return new DungeonBuilderStudioModel.LevelRange(true, min, max);
      }
   }

   public record MobPool(String id, boolean draft, List<DungeonBuilderStudioModel.PoolEntry> entries) {
      public MobPool {
         id = DungeonBuilderStudioModel.defaultText(id, "builder:new_pool", 192);
         entries = DungeonBuilderStudioModel.bounded(entries, 256, Function.identity());
      }

      public int totalWeight() {
         long total = 0L;

         for (DungeonBuilderStudioModel.PoolEntry entry : this.entries) {
            total += entry.weight();
         }

         return (int)Math.min(2147483647L, total);
      }
   }

   public record Notice(DungeonBuilderStudioModel.Severity severity, String message) {
      public Notice {
         severity = severity == null ? DungeonBuilderStudioModel.Severity.INFO : severity;
         message = DungeonBuilderStudioModel.text(message, 320);
      }

      public static DungeonBuilderStudioModel.Notice none() {
         return new DungeonBuilderStudioModel.Notice(DungeonBuilderStudioModel.Severity.INFO, "");
      }
   }

   public record OptionalXp(boolean present, int value) {
      public OptionalXp {
         value = DungeonBuilderStudioModel.clamp(value, 0, 1000000);
      }

      public static DungeonBuilderStudioModel.OptionalXp automatic() {
         return new DungeonBuilderStudioModel.OptionalXp(false, 0);
      }
   }

   public record Point(int x, int y, int z) {
   }

   public record PoolEntry(
      DungeonBuilderStudioModel.SelectorKind selectorKind,
      String selectorId,
      int weight,
      String requiredMod,
      DungeonBuilderStudioModel.LevelRange eligibleLevel,
      DungeonBuilderStudioModel.LevelRange spawnLevel,
      DungeonBuilderStudioModel.OptionalXp baseXp
   ) {
      public PoolEntry {
         selectorKind = selectorKind == null ? DungeonBuilderStudioModel.SelectorKind.ENTITY : selectorKind;
         selectorId = DungeonBuilderStudioModel.defaultText(selectorId, "minecraft:zombie", 192);
         weight = DungeonBuilderStudioModel.clamp(weight, 1, 1000000);
         requiredMod = DungeonBuilderStudioModel.text(requiredMod, 64);
         eligibleLevel = eligibleLevel == null ? DungeonBuilderStudioModel.LevelRange.unset() : eligibleLevel;
         spawnLevel = spawnLevel == null ? DungeonBuilderStudioModel.LevelRange.unset() : spawnLevel;
         baseXp = baseXp == null ? DungeonBuilderStudioModel.OptionalXp.automatic() : baseXp;
      }

      public PoolEntry(String entityId, int weight, int xp, int minLevel, int maxLevel) {
         this(
            DungeonBuilderStudioModel.SelectorKind.ENTITY,
            entityId,
            weight,
            "",
            DungeonBuilderStudioModel.LevelRange.unset(),
            DungeonBuilderStudioModel.LevelRange.of(minLevel, maxLevel),
            new DungeonBuilderStudioModel.OptionalXp(true, xp)
         );
      }

      public String selectorLabel() {
         return this.selectorKind == DungeonBuilderStudioModel.SelectorKind.TAG ? "#" + this.selectorId : this.selectorId;
      }
   }

   public record Project(
      String id,
      String name,
      DungeonBuilderStudioModel.ProjectKind kind,
      DungeonBuilderStudioModel.RoomRole role,
      int weight,
      Set<String> ranks,
      String shellBlock,
      int shellThickness,
      DungeonBuilderStudioModel.Bounds bounds,
      boolean snapshotCaptured,
      boolean snapshotOutdated,
      long snapshotRevision,
      List<DungeonBuilderStudioModel.FootprintCell> footprint,
      List<DungeonBuilderStudioModel.Socket> sockets,
      List<DungeonBuilderStudioModel.Anchor> anchors,
      int errors,
      int warnings
   ) {
      public Project {
         id = DungeonBuilderStudioModel.defaultText(id, "unknown:room", 128);
         name = DungeonBuilderStudioModel.defaultText(name, id, 96);
         kind = kind == null ? DungeonBuilderStudioModel.ProjectKind.MODULE : kind;
         role = role == null ? DungeonBuilderStudioModel.RoomRole.NORMAL : role;
         weight = DungeonBuilderStudioModel.clamp(weight, 1, 10000);
         Set<String> normalizedRanks = DungeonBuilderStudioModel.boundedSet(ranks, 8, 16);
         ranks = normalizedRanks.size() == 6 && normalizedRanks.containsAll(Set.of("E", "D", "C", "B", "A", "S")) ? Set.of() : normalizedRanks;
         shellBlock = DungeonBuilderStudioModel.defaultText(shellBlock, "minecraft:bedrock", 128);
         shellThickness = DungeonBuilderStudioModel.clamp(shellThickness, 0, 4);
         snapshotRevision = Math.max(0L, snapshotRevision);
         footprint = DungeonBuilderStudioModel.bounded(footprint, 2304, Function.identity());
         sockets = DungeonBuilderStudioModel.bounded(sockets, 64, Function.identity());
         anchors = DungeonBuilderStudioModel.bounded(anchors, 320, Function.identity());
         errors = DungeonBuilderStudioModel.clamp(errors, 0, 10000);
         warnings = DungeonBuilderStudioModel.clamp(warnings, 0, 10000);
      }

      public Project(
         String id,
         String name,
         DungeonBuilderStudioModel.ProjectKind kind,
         DungeonBuilderStudioModel.RoomRole role,
         int weight,
         Set<String> ranks,
         DungeonBuilderStudioModel.Bounds bounds,
         boolean snapshotCaptured,
         boolean snapshotOutdated,
         long snapshotRevision,
         List<DungeonBuilderStudioModel.FootprintCell> footprint,
         List<DungeonBuilderStudioModel.Socket> sockets,
         List<DungeonBuilderStudioModel.Anchor> anchors,
         int errors,
         int warnings
      ) {
         this(
            id,
            name,
            kind,
            role,
            weight,
            ranks,
            "minecraft:bedrock",
            1,
            bounds,
            snapshotCaptured,
            snapshotOutdated,
            snapshotRevision,
            footprint,
            sockets,
            anchors,
            errors,
            warnings
         );
      }

      public String snapshotLabel() {
         if (!this.snapshotCaptured) {
            return "NOT CAPTURED";
         } else {
            return this.snapshotOutdated ? "UPDATE NEEDED" : "CAPTURED";
         }
      }
   }

   public enum ProjectKind {
      PRESET,
      MODULE;
   }

   public enum RoomRole {
      START,
      NORMAL,
      CORRIDOR,
      JUNCTION,
      DEAD_END,
      TREASURE,
      STAIR,
      BOSS;
   }

   public record RoomWeight(String projectId, int weight) {
      public RoomWeight {
         projectId = DungeonBuilderStudioModel.text(projectId, 128);
         weight = DungeonBuilderStudioModel.clamp(weight, 1, 1000000);
      }
   }

   public enum SelectorKind {
      ENTITY,
      TAG;
   }

   public enum Severity {
      INFO,
      PASS,
      TODO,
      WARNING,
      ERROR;
   }

   public record SimConnection(String fromRoomId, String toRoomId) {
      public SimConnection {
         fromRoomId = DungeonBuilderStudioModel.text(fromRoomId, 128);
         toRoomId = DungeonBuilderStudioModel.text(toRoomId, 128);
      }
   }

   public record SimRoom(String id, String projectId, DungeonBuilderStudioModel.RoomRole role, int x, int z, int width, int depth, int rotation) {
      public SimRoom {
         id = DungeonBuilderStudioModel.defaultText(id, "room", 128);
         projectId = DungeonBuilderStudioModel.defaultText(projectId, "unknown:room", 128);
         role = role == null ? DungeonBuilderStudioModel.RoomRole.NORMAL : role;
         width = DungeonBuilderStudioModel.clamp(width, 1, 512);
         depth = DungeonBuilderStudioModel.clamp(depth, 1, 512);
         rotation = Math.floorMod(rotation, 360);
      }
   }

   public record Simulation(
      long seed,
      DungeonBuilderStudioModel.SimulationStatus status,
      String message,
      int attempts,
      List<DungeonBuilderStudioModel.SimRoom> rooms,
      List<DungeonBuilderStudioModel.SimConnection> connections
   ) {
      public Simulation {
         status = status == null ? DungeonBuilderStudioModel.SimulationStatus.IDLE : status;
         message = DungeonBuilderStudioModel.text(message, 320);
         attempts = DungeonBuilderStudioModel.clamp(attempts, 0, 1000000);
         rooms = DungeonBuilderStudioModel.bounded(rooms, 64, Function.identity());
         connections = DungeonBuilderStudioModel.bounded(connections, 128, Function.identity());
      }

      public static DungeonBuilderStudioModel.Simulation empty() {
         return new DungeonBuilderStudioModel.Simulation(12345L, DungeonBuilderStudioModel.SimulationStatus.IDLE, "", 0, List.of(), List.of());
      }
   }

   public enum SimulationStatus {
      IDLE,
      RUNNING,
      SUCCESS,
      FAILED;
   }

   public record Socket(
      String id,
      DungeonBuilderStudioModel.Point position,
      DungeonBuilderStudioModel.Facing facing,
      DungeonBuilderStudioModel.SocketType type,
      boolean required,
      int openingWidth,
      int openingHeight
   ) {
      public Socket {
         id = DungeonBuilderStudioModel.defaultText(id, "socket", 128);
         position = position == null ? new DungeonBuilderStudioModel.Point(0, 0, 0) : position;
         facing = facing == null ? DungeonBuilderStudioModel.Facing.NORTH : facing;
         type = type == null ? DungeonBuilderStudioModel.SocketType.CORRIDOR : type;
         openingWidth = DungeonBuilderStudioModel.clamp(openingWidth, 1, 32);
         openingHeight = DungeonBuilderStudioModel.clamp(openingHeight, 1, 32);
      }
   }

   public enum SocketType {
      CORRIDOR,
      STAIR;
   }

   public enum SpawnRole {
      NONE,
      NORMAL,
      ELITE,
      BOSS;
   }

   public enum Topology {
      LINEAR,
      BRANCHING;
   }

   public record ValidationIssue(DungeonBuilderStudioModel.Severity severity, String code, String message, String projectId, String elementId) {
      public ValidationIssue {
         severity = severity == null ? DungeonBuilderStudioModel.Severity.INFO : severity;
         code = DungeonBuilderStudioModel.text(code, 48);
         message = DungeonBuilderStudioModel.text(message, 320);
         projectId = DungeonBuilderStudioModel.text(projectId, 128);
         elementId = DungeonBuilderStudioModel.text(elementId, 128);
      }
   }

   public record ValidationSummary(boolean hasRun, int errors, int warnings, List<DungeonBuilderStudioModel.ValidationIssue> issues) {
      public ValidationSummary {
         errors = DungeonBuilderStudioModel.clamp(errors, 0, 10000);
         warnings = DungeonBuilderStudioModel.clamp(warnings, 0, 10000);
         issues = DungeonBuilderStudioModel.bounded(issues, 128, Function.identity());
      }

      public static DungeonBuilderStudioModel.ValidationSummary notRun() {
         return new DungeonBuilderStudioModel.ValidationSummary(false, 0, 0, List.of());
      }

      public DungeonBuilderStudioModel.Severity severity() {
         if (!this.hasRun) {
            return DungeonBuilderStudioModel.Severity.TODO;
         } else if (this.errors > 0) {
            return DungeonBuilderStudioModel.Severity.ERROR;
         } else {
            return this.warnings > 0 ? DungeonBuilderStudioModel.Severity.WARNING : DungeonBuilderStudioModel.Severity.PASS;
         }
      }
   }
}
