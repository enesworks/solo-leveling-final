package dev.eness.sololevelingfinal.core.client.gui.dungeonbuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.DungeonBuilderStudioActionMessage;
import dev.eness.sololevelingfinal.core.network.DungeonBuilderStudioRequestMessage;

public final class DungeonBuilderStudioClient {
   private static DungeonBuilderStudioModel model = DungeonBuilderStudioModel.empty();
   private static final DungeonBuilderStudioController CONTROLLER = new DungeonBuilderStudioClient.NetworkController();

   private DungeonBuilderStudioClient() {
   }

   public static void requestOpen() {
      model = DungeonBuilderStudioModel.loadingState();
      SololevelingMod.PACKET_HANDLER.sendToServer(new DungeonBuilderStudioRequestMessage());
   }

   public static void handleState(boolean open, CompoundTag state) {
      model = decode(state == null ? new CompoundTag() : state);
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.screen instanceof DungeonBuilderStudioScreen studio) {
         studio.updateModel(model);
      } else if (open) {
         minecraft.setScreen(new DungeonBuilderStudioScreen(model, CONTROLLER));
      }
   }

   public static DungeonBuilderStudioModel model() {
      return model;
   }

   private static DungeonBuilderStudioModel decode(CompoundTag root) {
      List<DungeonBuilderStudioModel.Project> projects = new ArrayList<>();
      ListTag projectTags = root.getList("Projects", 10);

      for (int index = 0; index < Math.min(128, projectTags.size()); index++) {
         CompoundTag projectTag = projectTags.getCompound(index);
         DungeonBuilderStudioModel.Project previous = model.project(projectTag.getString("Id")).orElse(null);
         projects.add(project(projectTag, previous));
      }

      List<DungeonBuilderStudioModel.MobPool> pools = new ArrayList<>();
      ListTag poolTags = root.getList("Pools", 10);

      for (int index = 0; index < Math.min(128, poolTags.size()); index++) {
         pools.add(pool(poolTags.getCompound(index)));
      }

      List<DungeonBuilderStudioModel.DraftSummary> dungeonDrafts = new ArrayList<>();
      ListTag draftTags = root.getList("DungeonDrafts", 10);

      for (int index = 0; index < Math.min(64, draftTags.size()); index++) {
         CompoundTag draft = draftTags.getCompound(index);
         dungeonDrafts.add(
            new DungeonBuilderStudioModel.DraftSummary(
               draft.getString("Id"),
               enumValue(DungeonBuilderStudioModel.LayoutMode.class, draft.getString("Mode"), DungeonBuilderStudioModel.LayoutMode.PROCEDURAL),
               enumValue(DungeonBuilderStudioModel.Topology.class, draft.getString("Topology"), DungeonBuilderStudioModel.Topology.LINEAR),
               draft.getInt("RoomCount"),
               draft.getInt("PlacementCount")
            )
         );
      }

      CompoundTag noticeTag = root.getCompound("Notice");
      DungeonBuilderStudioModel.Notice notice = new DungeonBuilderStudioModel.Notice(
         enumValue(DungeonBuilderStudioModel.Severity.class, noticeTag.getString("Severity"), DungeonBuilderStudioModel.Severity.INFO),
         noticeTag.getString("Message")
      );
      return new DungeonBuilderStudioModel(
         root.getLong("Revision"),
         root.getString("SelectedProjectId"),
         root.getString("SelectedPoolId"),
         root.getString("DungeonId"),
         false,
         projects,
         pools,
         layout(root.getCompound("Layout"), projects),
         simulation(root.getCompound("Simulation")),
         validation(root.getCompound("Validation")),
         notice,
         dungeonDrafts
      );
   }

   private static DungeonBuilderStudioModel.Project project(CompoundTag tag, DungeonBuilderStudioModel.Project previous) {
      DungeonBuilderStudioModel.Bounds bounds = tag.contains("Bounds", 10) ? bounds(tag.getCompound("Bounds")) : null;
      boolean detailed = tag.getBoolean("Detailed");
      List<DungeonBuilderStudioModel.FootprintCell> footprint = new ArrayList<>();
      int width = Math.max(0, Math.min(48, tag.getInt("FootprintWidth")));
      int depth = Math.max(0, Math.min(48, tag.getInt("FootprintDepth")));
      int[] colors = tag.getIntArray("FootprintColors");

      for (int z = 0; z < depth; z++) {
         for (int x = 0; x < width; x++) {
            int cell = z * width + x;
            if (cell < colors.length && colors[cell] != 0) {
               footprint.add(new DungeonBuilderStudioModel.FootprintCell(x, z, colors[cell]));
            }
         }
      }

      List<DungeonBuilderStudioModel.Socket> sockets = new ArrayList<>();
      ListTag socketTags = tag.getList("Sockets", 10);

      for (int index = 0; index < Math.min(64, socketTags.size()); index++) {
         CompoundTag socket = socketTags.getCompound(index);
         sockets.add(
            new DungeonBuilderStudioModel.Socket(
               socket.getString("Id"),
               point(socket.getCompound("Position")),
               enumValue(DungeonBuilderStudioModel.Facing.class, socket.getString("Facing"), DungeonBuilderStudioModel.Facing.NORTH),
               enumValue(DungeonBuilderStudioModel.SocketType.class, socket.getString("Type"), DungeonBuilderStudioModel.SocketType.CORRIDOR),
               socket.getBoolean("Required"),
               socket.getInt("OpeningWidth"),
               socket.getInt("OpeningHeight")
            )
         );
      }

      List<DungeonBuilderStudioModel.Anchor> anchors = new ArrayList<>();
      ListTag anchorTags = tag.getList("Anchors", 10);

      for (int index = 0; index < Math.min(320, anchorTags.size()); index++) {
         CompoundTag anchor = anchorTags.getCompound(index);
         DungeonBuilderStudioModel.AnchorKind kind = enumValue(
            DungeonBuilderStudioModel.AnchorKind.class, anchor.getString("Kind"), DungeonBuilderStudioModel.AnchorKind.UNASSIGNED
         );

         DungeonBuilderStudioModel.SpawnRole role = switch (kind) {
            case MOB_SPAWN -> DungeonBuilderStudioModel.SpawnRole.NORMAL;
            case ELITE_SPAWN -> DungeonBuilderStudioModel.SpawnRole.ELITE;
            case BOSS_SPAWN -> DungeonBuilderStudioModel.SpawnRole.BOSS;
            default -> DungeonBuilderStudioModel.SpawnRole.NONE;
         };
         DungeonBuilderStudioModel.Bounds triggerBounds = anchor.contains("Bounds", 10) ? bounds(anchor.getCompound("Bounds")) : null;
         anchors.add(
            new DungeonBuilderStudioModel.Anchor(
               anchor.getString("Id"),
               kind,
               role,
               point(anchor.getCompound("Position")),
               triggerBounds,
               anchor.getString("EncounterId"),
               anchor.getString("PoolId"),
               anchor.getBoolean("LevelOverride"),
               nonZero(anchor.getInt("MinLevel"), 1),
               nonZero(anchor.getInt("MaxLevel"), 1),
               anchor.getBoolean("Delayed")
            )
         );
      }

      if (!detailed && previous != null) {
         footprint = new ArrayList<>(previous.footprint());
         sockets = new ArrayList<>(previous.sockets());
         anchors = new ArrayList<>(previous.anchors());
      }

      return new DungeonBuilderStudioModel.Project(
         tag.getString("Id"),
         tag.getString("Name"),
         enumValue(DungeonBuilderStudioModel.ProjectKind.class, tag.getString("Kind"), DungeonBuilderStudioModel.ProjectKind.MODULE),
         enumValue(DungeonBuilderStudioModel.RoomRole.class, tag.getString("Role"), DungeonBuilderStudioModel.RoomRole.NORMAL),
         nonZero(tag.getInt("Weight"), 1),
         new LinkedHashSet<>(strings(tag, "Ranks", 8)),
         tag.getString("ShellBlock"),
         tag.getInt("ShellThickness"),
         bounds,
         tag.getBoolean("SnapshotCaptured"),
         tag.getBoolean("SnapshotOutdated"),
         tag.getLong("SnapshotRevision"),
         footprint,
         sockets,
         anchors,
         tag.getInt("Errors"),
         tag.getInt("Warnings")
      );
   }

   private static DungeonBuilderStudioModel.MobPool pool(CompoundTag tag) {
      List<DungeonBuilderStudioModel.PoolEntry> entries = new ArrayList<>();
      ListTag values = tag.getList("Entries", 10);

      for (int index = 0; index < Math.min(256, values.size()); index++) {
         CompoundTag value = values.getCompound(index);
         entries.add(
            new DungeonBuilderStudioModel.PoolEntry(
               enumValue(DungeonBuilderStudioModel.SelectorKind.class, value.getString("SelectorKind"), DungeonBuilderStudioModel.SelectorKind.ENTITY),
               value.getString("Selector"),
               nonZero(value.getInt("Weight"), 1),
               value.getString("RequiredMod"),
               range(value, "Eligible"),
               range(value, "Spawn"),
               new DungeonBuilderStudioModel.OptionalXp(value.contains("Xp", 3), Math.max(0, value.getInt("Xp")))
            )
         );
      }

      return new DungeonBuilderStudioModel.MobPool(tag.getString("Id"), false, entries);
   }

   private static DungeonBuilderStudioModel.LayoutDraft layout(CompoundTag tag, List<DungeonBuilderStudioModel.Project> projects) {
      List<String> enabled = new ArrayList<>();
      List<DungeonBuilderStudioModel.RoomWeight> weights = new ArrayList<>();
      ListTag roomTags = tag.getList("Rooms", 10);

      for (int index = 0; index < Math.min(128, roomTags.size()); index++) {
         CompoundTag room = roomTags.getCompound(index);
         enabled.add(room.getString("ProjectId"));
         weights.add(new DungeonBuilderStudioModel.RoomWeight(room.getString("ProjectId"), nonZero(room.getInt("Weight"), 1)));
      }

      List<DungeonBuilderStudioModel.LayoutNode> nodes = new ArrayList<>();
      ListTag nodeTags = tag.getList("Nodes", 10);

      for (int index = 0; index < Math.min(64, nodeTags.size()); index++) {
         CompoundTag node = nodeTags.getCompound(index);
         DungeonBuilderStudioModel.Project project = projects.stream().filter(value -> value.id().equals(node.getString("ProjectId"))).findFirst().orElse(null);
         int rotation = rotationDegrees(node.getString("Rotation"));
         int roomWidth = project != null && project.bounds() != null ? project.bounds().width() : 1;
         int roomDepth = project != null && project.bounds() != null ? project.bounds().depth() : 1;
         if (rotation == 90 || rotation == 270) {
            int swap = roomWidth;
            roomWidth = roomDepth;
            roomDepth = swap;
         }

         nodes.add(
            new DungeonBuilderStudioModel.LayoutNode(
               node.getString("Id"),
               node.getString("ProjectId"),
               project == null ? DungeonBuilderStudioModel.RoomRole.NORMAL : project.role(),
               node.getInt("X"),
               node.getInt("Y"),
               node.getInt("Z"),
               roomWidth,
               roomDepth,
               rotation,
               false
            )
         );
      }

      List<DungeonBuilderStudioModel.LayoutConnection> connections = new ArrayList<>();
      ListTag connectionTags = tag.getList("Connections", 10);

      for (int index = 0; index < Math.min(128, connectionTags.size()); index++) {
         CompoundTag connection = connectionTags.getCompound(index);
         connections.add(
            new DungeonBuilderStudioModel.LayoutConnection(
               connection.getString("FromNode"), connection.getString("FromSocket"), connection.getString("ToNode"), connection.getString("ToSocket")
            )
         );
      }

      return new DungeonBuilderStudioModel.LayoutDraft(
         enumValue(DungeonBuilderStudioModel.LayoutMode.class, tag.getString("Mode"), DungeonBuilderStudioModel.LayoutMode.PROCEDURAL),
         enumValue(DungeonBuilderStudioModel.Topology.class, tag.getString("Topology"), DungeonBuilderStudioModel.Topology.LINEAR),
         nonZero(tag.getInt("MinRooms"), 3),
         nonZero(tag.getInt("MaxRooms"), 8),
         nonZero(tag.getInt("MaxDepth"), 16),
         new LinkedHashSet<>(strings(tag, "Ranks", 8)),
         tag.getString("ShellBlock"),
         tag.getInt("ShellThickness"),
         enabled,
         weights,
         nodes,
         connections
      );
   }

   private static DungeonBuilderStudioModel.Simulation simulation(CompoundTag tag) {
      List<DungeonBuilderStudioModel.SimRoom> rooms = new ArrayList<>();
      ListTag roomTags = tag.getList("Rooms", 10);

      for (int index = 0; index < Math.min(64, roomTags.size()); index++) {
         CompoundTag room = roomTags.getCompound(index);
         rooms.add(
            new DungeonBuilderStudioModel.SimRoom(
               room.getString("Id"),
               room.getString("ProjectId"),
               enumValue(DungeonBuilderStudioModel.RoomRole.class, room.getString("Role"), DungeonBuilderStudioModel.RoomRole.NORMAL),
               room.getInt("X"),
               room.getInt("Z"),
               nonZero(room.getInt("Width"), 1),
               nonZero(room.getInt("Depth"), 1),
               room.getInt("Rotation")
            )
         );
      }

      List<DungeonBuilderStudioModel.SimConnection> connections = new ArrayList<>();
      ListTag connectionTags = tag.getList("Connections", 10);

      for (int index = 0; index < Math.min(128, connectionTags.size()); index++) {
         CompoundTag connection = connectionTags.getCompound(index);
         connections.add(new DungeonBuilderStudioModel.SimConnection(connection.getString("FromRoom"), connection.getString("ToRoom")));
      }

      return new DungeonBuilderStudioModel.Simulation(
         tag.getLong("Seed"),
         enumValue(DungeonBuilderStudioModel.SimulationStatus.class, tag.getString("Status"), DungeonBuilderStudioModel.SimulationStatus.IDLE),
         tag.getString("Message"),
         tag.getInt("Attempts"),
         rooms,
         connections
      );
   }

   private static DungeonBuilderStudioModel.ValidationSummary validation(CompoundTag tag) {
      List<DungeonBuilderStudioModel.ValidationIssue> issues = new ArrayList<>();
      ListTag issueTags = tag.getList("Issues", 10);

      for (int index = 0; index < Math.min(128, issueTags.size()); index++) {
         CompoundTag issue = issueTags.getCompound(index);
         issues.add(
            new DungeonBuilderStudioModel.ValidationIssue(
               enumValue(DungeonBuilderStudioModel.Severity.class, issue.getString("Severity"), DungeonBuilderStudioModel.Severity.INFO),
               issue.getString("Code"),
               issue.getString("Message"),
               issue.getString("ProjectId"),
               issue.getString("ElementId")
            )
         );
      }

      return new DungeonBuilderStudioModel.ValidationSummary(tag.getBoolean("HasRun"), tag.getInt("Errors"), tag.getInt("Warnings"), issues);
   }

   private static CompoundTag encode(DungeonBuilderStudioController.Action action) {
      CompoundTag tag = new CompoundTag();
      if (action instanceof DungeonBuilderStudioController.CreateProject value) {
         tag.putString("Namespace", value.namespace());
         tag.putString("Name", value.name());
         tag.putString("Kind", value.kind().name());
      } else if (action instanceof DungeonBuilderStudioController.DeleteProject value) {
         tag.putString("ProjectId", value.projectId());
      } else if (action instanceof DungeonBuilderStudioController.SelectDungeon value) {
         tag.putString("DungeonId", value.dungeonId());
      } else if (action instanceof DungeonBuilderStudioController.NewDungeon value) {
         tag.putString("DungeonId", value.dungeonId());
      } else if (action instanceof DungeonBuilderStudioController.DeleteDungeon value) {
         tag.putString("DungeonId", value.dungeonId());
      } else if (action instanceof DungeonBuilderStudioController.SelectProject value) {
         tag.putString("ProjectId", value.projectId());
      } else if (action instanceof DungeonBuilderStudioController.SetRoomRole value) {
         tag.putString("ProjectId", value.projectId());
         tag.putString("Role", value.role().name());
      } else if (action instanceof DungeonBuilderStudioController.SetRoomWeight value) {
         tag.putString("ProjectId", value.projectId());
         tag.putInt("Weight", value.weight());
      } else if (action instanceof DungeonBuilderStudioController.SetProjectSettings value) {
         tag.putString("ProjectId", value.projectId());
         tag.put("Ranks", stringTags(value.ranks()));
         tag.putString("ShellBlock", value.shellBlock());
         tag.putInt("ShellThickness", value.shellThickness());
      } else if (action instanceof DungeonBuilderStudioController.CaptureSnapshot value) {
         tag.putString("ProjectId", value.projectId());
         tag.putBoolean("UpdateExisting", value.updateExisting());
      } else if (action instanceof DungeonBuilderStudioController.AssignAnchor value) {
         tag.putString("ProjectId", value.projectId());
         tag.putString("AnchorId", value.anchorId());
         tag.putString("AnchorKind", value.kind().name());
         tag.putString("SpawnRole", value.spawnRole().name());
         tag.putString("EncounterId", value.encounterId());
         tag.putString("PoolId", value.poolId());
         tag.putBoolean("LevelOverride", value.levelOverride());
         tag.putInt("MinLevel", value.minLevel());
         tag.putInt("MaxLevel", value.maxLevel());
         tag.putBoolean("Delayed", value.delayed());
      } else if (action instanceof DungeonBuilderStudioController.EditSocket value) {
         tag.putString("ProjectId", value.projectId());
         tag.putString("SocketId", value.socketId());
         tag.putString("SocketType", value.type().name());
         tag.putBoolean("Required", value.required());
      } else if (action instanceof DungeonBuilderStudioController.CreatePool value) {
         tag.putString("PoolId", value.requestedId());
      } else if (action instanceof DungeonBuilderStudioController.DeletePool value) {
         tag.putString("PoolId", value.poolId());
      } else if (action instanceof DungeonBuilderStudioController.BeginPoolEntryDraft value) {
         tag.putString("PoolId", value.poolId());
      } else if (action instanceof DungeonBuilderStudioController.UpsertPoolEntry value) {
         List<DungeonBuilderStudioModel.PoolEntry> entries = new ArrayList<>(
            model.pool(value.poolId()).map(DungeonBuilderStudioModel.MobPool::entries).orElse(List.of())
         );
         entries.removeIf(entry -> entry.selectorLabel().equals(value.entry().selectorLabel()));
         entries.add(value.entry());
         putPool(tag, value.poolId(), entries);
      } else if (action instanceof DungeonBuilderStudioController.RemovePoolEntry value) {
         List<DungeonBuilderStudioModel.PoolEntry> entries = new ArrayList<>(
            model.pool(value.poolId()).map(DungeonBuilderStudioModel.MobPool::entries).orElse(List.of())
         );
         entries.removeIf(entry -> entry.selectorLabel().equals(value.entityId()));
         putPool(tag, value.poolId(), entries);
      } else if (action instanceof DungeonBuilderStudioController.SavePoolDraft value) {
         putPool(tag, value.poolId(), value.entries());
      } else if (action instanceof DungeonBuilderStudioController.UpdateLayout value) {
         putLayout(tag, value.dungeonId(), value.layout());
      } else if (action instanceof DungeonBuilderStudioController.RunSimulation value) {
         tag.putString("DungeonId", value.dungeonId());
         tag.putLong("Seed", value.seed());
      } else if (action instanceof DungeonBuilderStudioController.ValidateDungeon value) {
         tag.putString("DungeonId", value.dungeonId());
      } else if (action instanceof DungeonBuilderStudioController.ExportDungeon value) {
         tag.putString("DungeonId", value.dungeonId());
      }

      return tag;
   }

   private static void putPool(CompoundTag tag, String poolId, List<DungeonBuilderStudioModel.PoolEntry> entries) {
      tag.putString("PoolId", poolId);
      ListTag list = new ListTag();

      for (DungeonBuilderStudioModel.PoolEntry entry : entries) {
         CompoundTag value = new CompoundTag();
         value.putString("Selector", entry.selectorId());
         value.putBoolean("Tag", entry.selectorKind() == DungeonBuilderStudioModel.SelectorKind.TAG);
         value.putInt("Weight", entry.weight());
         value.putString("RequiredMod", entry.requiredMod());
         if (entry.eligibleLevel().present()) {
            value.putInt("EligibleMin", entry.eligibleLevel().min());
            value.putInt("EligibleMax", entry.eligibleLevel().max());
         }

         if (entry.spawnLevel().present()) {
            value.putInt("SpawnMin", entry.spawnLevel().min());
            value.putInt("SpawnMax", entry.spawnLevel().max());
         }

         if (entry.baseXp().present()) {
            value.putInt("Xp", entry.baseXp().value());
         }

         list.add(value);
      }

      tag.put("Entries", list);
   }

   private static void putLayout(CompoundTag tag, String dungeonId, DungeonBuilderStudioModel.LayoutDraft layout) {
      tag.putString("DungeonId", dungeonId);
      tag.putString("Mode", layout.mode().name());
      tag.putString("Topology", layout.topology().name());
      tag.putInt("MinRooms", layout.minRooms());
      tag.putInt("MaxRooms", layout.maxRooms());
      tag.putInt("MaxDepth", layout.maxDepth());
      tag.putString("ShellBlock", layout.shellBlock());
      tag.putInt("ShellThickness", layout.shellThickness());
      tag.put("Ranks", stringTags(layout.ranks()));
      ListTag rooms = new ListTag();

      for (String projectId : layout.enabledProjectIds()) {
         CompoundTag room = new CompoundTag();
         room.putString("ProjectId", projectId);
         room.putInt(
            "Weight",
            layout.roomWeights()
               .stream()
               .filter(valuex -> valuex.projectId().equals(projectId))
               .findFirst()
               .map(DungeonBuilderStudioModel.RoomWeight::weight)
               .orElse(1)
         );
         rooms.add(room);
      }

      tag.put("Rooms", rooms);
      ListTag nodes = new ListTag();

      for (DungeonBuilderStudioModel.LayoutNode node : layout.nodes()) {
         CompoundTag value = new CompoundTag();
         value.putString("Id", node.id());
         value.putString("ProjectId", node.projectId());
         value.putInt("X", node.x());
         value.putInt("Y", node.y());
         value.putInt("Z", node.z());
         value.putString("Rotation", rotationName(node.rotation()));
         nodes.add(value);
      }

      tag.put("Nodes", nodes);
      ListTag connections = new ListTag();

      for (DungeonBuilderStudioModel.LayoutConnection connection : layout.connections()) {
         CompoundTag value = new CompoundTag();
         value.putString("FromNode", connection.fromNodeId());
         value.putString("FromSocket", connection.fromSocketId());
         value.putString("ToNode", connection.toNodeId());
         value.putString("ToSocket", connection.toSocketId());
         connections.add(value);
      }

      tag.put("Connections", connections);
   }

   private static String actionName(DungeonBuilderStudioController.Action action) {
      if (action instanceof DungeonBuilderStudioController.RequestSnapshot) {
         return "request_snapshot";
      } else if (action instanceof DungeonBuilderStudioController.CreateProject) {
         return "create_project";
      } else if (action instanceof DungeonBuilderStudioController.DeleteProject) {
         return "delete_project";
      } else if (action instanceof DungeonBuilderStudioController.SelectDungeon) {
         return "select_dungeon";
      } else if (action instanceof DungeonBuilderStudioController.NewDungeon) {
         return "new_dungeon";
      } else if (action instanceof DungeonBuilderStudioController.DeleteDungeon) {
         return "delete_dungeon";
      } else if (action instanceof DungeonBuilderStudioController.SelectProject) {
         return "select_project";
      } else if (action instanceof DungeonBuilderStudioController.SetRoomRole) {
         return "set_room_role";
      } else if (action instanceof DungeonBuilderStudioController.SetRoomWeight) {
         return "set_room_weight";
      } else if (action instanceof DungeonBuilderStudioController.SetProjectSettings) {
         return "set_project_settings";
      } else if (action instanceof DungeonBuilderStudioController.CaptureSnapshot) {
         return "capture_snapshot";
      } else if (action instanceof DungeonBuilderStudioController.AssignAnchor) {
         return "assign_anchor";
      } else if (action instanceof DungeonBuilderStudioController.EditSocket) {
         return "edit_socket";
      } else if (action instanceof DungeonBuilderStudioController.CreatePool) {
         return "create_pool";
      } else if (action instanceof DungeonBuilderStudioController.DeletePool) {
         return "delete_pool";
      } else if (action instanceof DungeonBuilderStudioController.BeginPoolEntryDraft) {
         return "begin_pool_entry_draft";
      } else if (action instanceof DungeonBuilderStudioController.UpsertPoolEntry) {
         return "upsert_pool_entry";
      } else if (action instanceof DungeonBuilderStudioController.RemovePoolEntry) {
         return "remove_pool_entry";
      } else if (action instanceof DungeonBuilderStudioController.SavePoolDraft) {
         return "save_pool_draft";
      } else if (action instanceof DungeonBuilderStudioController.UpdateLayout) {
         return "update_layout";
      } else if (action instanceof DungeonBuilderStudioController.RunSimulation) {
         return "run_simulation";
      } else if (action instanceof DungeonBuilderStudioController.ValidateDungeon) {
         return "validate_dungeon";
      } else {
         return action instanceof DungeonBuilderStudioController.ExportDungeon ? "export_dungeon" : "unknown";
      }
   }

   private static DungeonBuilderStudioModel.Point point(CompoundTag tag) {
      return new DungeonBuilderStudioModel.Point(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
   }

   private static DungeonBuilderStudioModel.Bounds bounds(CompoundTag tag) {
      return new DungeonBuilderStudioModel.Bounds(point(tag.getCompound("Min")), point(tag.getCompound("Max")));
   }

   private static DungeonBuilderStudioModel.LevelRange range(CompoundTag tag, String prefix) {
      boolean present = tag.contains(prefix + "Min", 3) && tag.contains(prefix + "Max", 3);
      return new DungeonBuilderStudioModel.LevelRange(present, nonZero(tag.getInt(prefix + "Min"), 1), nonZero(tag.getInt(prefix + "Max"), 1));
   }

   private static List<String> strings(CompoundTag tag, String key, int maximum) {
      ListTag list = tag.getList(key, 8);
      List<String> result = new ArrayList<>();

      for (int index = 0; index < Math.min(maximum, list.size()); index++) {
         result.add(list.getString(index));
      }

      return result;
   }

   private static ListTag stringTags(Set<String> values) {
      ListTag tags = new ListTag();
      values.forEach(value -> tags.add(StringTag.valueOf(value)));
      return tags;
   }

   private static int rotationDegrees(String value) {
      return switch (value) {
         case "CLOCKWISE_90" -> 90;
         case "CLOCKWISE_180" -> 180;
         case "COUNTERCLOCKWISE_90" -> 270;
         default -> 0;
      };
   }

   private static String rotationName(int degrees) {
      return switch (Math.floorMod(degrees, 360)) {
         case 90 -> "CLOCKWISE_90";
         case 180 -> "CLOCKWISE_180";
         case 270 -> "COUNTERCLOCKWISE_90";
         default -> "NONE";
      };
   }

   private static int nonZero(int value, int fallback) {
      return value == 0 ? fallback : value;
   }

   private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
      try {
         return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
      } catch (RuntimeException ignored) {
         return fallback;
      }
   }

   private static final class NetworkController implements DungeonBuilderStudioController {
      @Override
      public void submit(DungeonBuilderStudioController.Action action) {
         if (action instanceof DungeonBuilderStudioController.RequestSnapshot) {
            SololevelingMod.PACKET_HANDLER.sendToServer(new DungeonBuilderStudioRequestMessage());
         } else {
            SololevelingMod.PACKET_HANDLER
               .sendToServer(
                  new DungeonBuilderStudioActionMessage(
                     DungeonBuilderStudioClient.model.revision(), DungeonBuilderStudioClient.actionName(action), DungeonBuilderStudioClient.encode(action)
                  )
               );
         }
      }
   }
}
