package dev.eness.sololevelingfinal.core.client.gui.dungeonbuilder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@FunctionalInterface
public interface DungeonBuilderStudioController {
   void submit(DungeonBuilderStudioController.Action var1);

   default void screenClosed() {
   }

   static DungeonBuilderStudioController noop() {
      return action -> {};
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static String safe(String value, int maximum) {
      String result = value == null ? "" : value.replace('\u0000', ' ').trim();
      return result.length() <= maximum ? result : result.substring(0, maximum);
   }

   sealed interface Action
      permits DungeonBuilderStudioController.RequestSnapshot,
      DungeonBuilderStudioController.SelectProject,
      DungeonBuilderStudioController.CreateProject,
      DungeonBuilderStudioController.DeleteProject,
      DungeonBuilderStudioController.SetRoomRole,
      DungeonBuilderStudioController.SetRoomWeight,
      DungeonBuilderStudioController.SetProjectSettings,
      DungeonBuilderStudioController.CaptureSnapshot,
      DungeonBuilderStudioController.AssignAnchor,
      DungeonBuilderStudioController.EditSocket,
      DungeonBuilderStudioController.CreatePool,
      DungeonBuilderStudioController.DeletePool,
      DungeonBuilderStudioController.BeginPoolEntryDraft,
      DungeonBuilderStudioController.UpsertPoolEntry,
      DungeonBuilderStudioController.RemovePoolEntry,
      DungeonBuilderStudioController.SavePoolDraft,
      DungeonBuilderStudioController.UpdateLayout,
      DungeonBuilderStudioController.SelectDungeon,
      DungeonBuilderStudioController.NewDungeon,
      DungeonBuilderStudioController.DeleteDungeon,
      DungeonBuilderStudioController.RunSimulation,
      DungeonBuilderStudioController.ValidateDungeon,
      DungeonBuilderStudioController.ExportDungeon {
   }

   record AssignAnchor(
      String projectId,
      String anchorId,
      DungeonBuilderStudioModel.AnchorKind kind,
      DungeonBuilderStudioModel.SpawnRole spawnRole,
      DungeonBuilderStudioModel.Bounds triggerBounds,
      String encounterId,
      String poolId,
      boolean levelOverride,
      int minLevel,
      int maxLevel,
      boolean delayed
   ) implements DungeonBuilderStudioController.Action {
      public AssignAnchor {
         projectId = DungeonBuilderStudioController.safe(projectId, 128);
         anchorId = DungeonBuilderStudioController.safe(anchorId, 128);
         kind = kind == null ? DungeonBuilderStudioModel.AnchorKind.UNASSIGNED : kind;
         spawnRole = spawnRole == null ? DungeonBuilderStudioModel.SpawnRole.NONE : spawnRole;
         encounterId = DungeonBuilderStudioController.safe(encounterId, 64);
         poolId = DungeonBuilderStudioController.safe(poolId, 192);
         minLevel = DungeonBuilderStudioController.clamp(minLevel, 1, 1000);
         maxLevel = DungeonBuilderStudioController.clamp(maxLevel, minLevel, 1000);
      }

      public AssignAnchor(
         String projectId,
         String anchorId,
         DungeonBuilderStudioModel.AnchorKind kind,
         DungeonBuilderStudioModel.SpawnRole spawnRole,
         DungeonBuilderStudioModel.Bounds triggerBounds,
         String encounterId,
         String poolId,
         int minLevel,
         int maxLevel,
         boolean delayed
      ) {
         this(projectId, anchorId, kind, spawnRole, triggerBounds, encounterId, poolId, true, minLevel, maxLevel, delayed);
      }

      public static DungeonBuilderStudioController.AssignAnchor from(String projectId, DungeonBuilderStudioModel.Anchor anchor) {
         return new DungeonBuilderStudioController.AssignAnchor(
            projectId,
            anchor.id(),
            anchor.kind(),
            anchor.spawnRole(),
            anchor.triggerBounds(),
            anchor.encounterId(),
            anchor.poolId(),
            anchor.levelOverride(),
            anchor.minLevel(),
            anchor.maxLevel(),
            anchor.delayed()
         );
      }
   }

   record BeginPoolEntryDraft(String poolId) implements DungeonBuilderStudioController.Action {
      public BeginPoolEntryDraft {
         poolId = DungeonBuilderStudioController.safe(poolId, 192);
      }
   }

   record CaptureSnapshot(String projectId, boolean updateExisting) implements DungeonBuilderStudioController.Action {
      public CaptureSnapshot {
         projectId = DungeonBuilderStudioController.safe(projectId, 128);
      }
   }

   record CreatePool(String requestedId) implements DungeonBuilderStudioController.Action {
      public CreatePool {
         requestedId = DungeonBuilderStudioController.safe(requestedId, 192);
      }
   }

   record CreateProject(String namespace, String name, DungeonBuilderStudioModel.ProjectKind kind) implements DungeonBuilderStudioController.Action {
      public CreateProject {
         namespace = DungeonBuilderStudioController.safe(namespace, 32);
         name = DungeonBuilderStudioController.safe(name, 48);
         kind = kind == null ? DungeonBuilderStudioModel.ProjectKind.MODULE : kind;
      }
   }

   record DeleteDungeon(String dungeonId) implements DungeonBuilderStudioController.Action {
      public DeleteDungeon {
         dungeonId = DungeonBuilderStudioController.safe(dungeonId, 192);
      }
   }

   record DeletePool(String poolId) implements DungeonBuilderStudioController.Action {
      public DeletePool {
         poolId = DungeonBuilderStudioController.safe(poolId, 192);
      }
   }

   record DeleteProject(String projectId) implements DungeonBuilderStudioController.Action {
      public DeleteProject {
         projectId = DungeonBuilderStudioController.safe(projectId, 128);
      }
   }

   record EditSocket(String projectId, String socketId, DungeonBuilderStudioModel.SocketType type, boolean required)
      implements DungeonBuilderStudioController.Action {
      public EditSocket {
         projectId = DungeonBuilderStudioController.safe(projectId, 128);
         socketId = DungeonBuilderStudioController.safe(socketId, 128);
         type = type == null ? DungeonBuilderStudioModel.SocketType.CORRIDOR : type;
      }
   }

   record ExportDungeon(String dungeonId) implements DungeonBuilderStudioController.Action {
      public ExportDungeon {
         dungeonId = DungeonBuilderStudioController.safe(dungeonId, 192);
      }
   }

   record NewDungeon(String dungeonId) implements DungeonBuilderStudioController.Action {
      public NewDungeon {
         dungeonId = DungeonBuilderStudioController.safe(dungeonId, 192);
      }
   }

   record RemovePoolEntry(String poolId, String entityId) implements DungeonBuilderStudioController.Action {
      public RemovePoolEntry {
         poolId = DungeonBuilderStudioController.safe(poolId, 192);
         entityId = DungeonBuilderStudioController.safe(entityId, 192);
      }
   }

   record RequestSnapshot() implements DungeonBuilderStudioController.Action {
   }

   record RunSimulation(String dungeonId, long seed) implements DungeonBuilderStudioController.Action {
      public RunSimulation {
         dungeonId = DungeonBuilderStudioController.safe(dungeonId, 192);
      }
   }

   record SavePoolDraft(String poolId, List<DungeonBuilderStudioModel.PoolEntry> entries) implements DungeonBuilderStudioController.Action {
      public SavePoolDraft {
         poolId = DungeonBuilderStudioController.safe(poolId, 192);
         entries = entries == null ? List.of() : List.copyOf(entries.stream().filter(Objects::nonNull).limit(256L).toList());
      }
   }

   record SelectDungeon(String dungeonId) implements DungeonBuilderStudioController.Action {
      public SelectDungeon {
         dungeonId = DungeonBuilderStudioController.safe(dungeonId, 192);
      }
   }

   record SelectProject(String projectId) implements DungeonBuilderStudioController.Action {
      public SelectProject {
         projectId = DungeonBuilderStudioController.safe(projectId, 128);
      }
   }

   record SetProjectSettings(String projectId, Set<String> ranks, String shellBlock, int shellThickness) implements DungeonBuilderStudioController.Action {
      public SetProjectSettings(String projectId, Set<String> ranks, String shellBlock, int shellThickness) {
         projectId = DungeonBuilderStudioController.safe(projectId, 128);
         LinkedHashSet<String> cleanRanks = new LinkedHashSet<>();
         if (ranks != null) {
            for (String rank : ranks) {
               if (cleanRanks.size() >= 8) {
                  break;
               }

               String clean = DungeonBuilderStudioController.safe(rank, 16).toUpperCase(Locale.ROOT);
               if (!clean.isBlank()) {
                  cleanRanks.add(clean);
               }
            }
         }

         ranks = Set.copyOf(cleanRanks);
         shellBlock = DungeonBuilderStudioController.safe(shellBlock, 128);
         shellThickness = DungeonBuilderStudioController.clamp(shellThickness, 0, 4);
         this.projectId = projectId;
         this.ranks = ranks;
         this.shellBlock = shellBlock;
         this.shellThickness = shellThickness;
      }
   }

   record SetRoomRole(String projectId, DungeonBuilderStudioModel.RoomRole role) implements DungeonBuilderStudioController.Action {
      public SetRoomRole {
         projectId = DungeonBuilderStudioController.safe(projectId, 128);
         role = role == null ? DungeonBuilderStudioModel.RoomRole.NORMAL : role;
      }
   }

   record SetRoomWeight(String projectId, int weight) implements DungeonBuilderStudioController.Action {
      public SetRoomWeight {
         projectId = DungeonBuilderStudioController.safe(projectId, 128);
         weight = DungeonBuilderStudioController.clamp(weight, 1, 10000);
      }
   }

   record UpdateLayout(String dungeonId, DungeonBuilderStudioModel.LayoutDraft layout) implements DungeonBuilderStudioController.Action {
      public UpdateLayout {
         dungeonId = DungeonBuilderStudioController.safe(dungeonId, 192);
         layout = layout == null ? DungeonBuilderStudioModel.LayoutDraft.empty() : layout;
      }
   }

   record UpsertPoolEntry(String poolId, DungeonBuilderStudioModel.PoolEntry entry) implements DungeonBuilderStudioController.Action {
      public UpsertPoolEntry {
         poolId = DungeonBuilderStudioController.safe(poolId, 192);
         entry = entry == null ? new DungeonBuilderStudioModel.PoolEntry("minecraft:zombie", 1, 0, 1, 1) : entry;
      }
   }

   record ValidateDungeon(String dungeonId) implements DungeonBuilderStudioController.Action {
      public ValidateDungeon {
         dungeonId = DungeonBuilderStudioController.safe(dungeonId, 192);
      }
   }
}
