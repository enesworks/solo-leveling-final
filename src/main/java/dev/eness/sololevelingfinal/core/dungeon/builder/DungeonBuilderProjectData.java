package dev.eness.sololevelingfinal.core.dungeon.builder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.dungeon.builder.model.BuilderMobPool;
import dev.eness.sololevelingfinal.core.dungeon.builder.model.BuilderWorkspaceSnapshot;
import dev.eness.sololevelingfinal.core.dungeon.builder.model.DungeonDraft;
import dev.eness.sololevelingfinal.core.dungeon.builder.model.RoomSnapshot;

public final class DungeonBuilderProjectData extends SavedData {
   private static final String DATA_NAME = "sololeveling_dungeon_builder_projects";
   private static final int SCHEMA_VERSION = 6;
   private static final int MAX_PROJECTS_PER_BUILDER = 128;
   public static final int MAX_MOB_POOLS_PER_BUILDER = 128;
   public static final int MAX_MOB_POOL_ENTRIES = 256;
   public static final int MAX_TOTAL_MOB_POOL_ENTRIES = 2048;
   public static final int MAX_DUNGEON_DRAFTS_PER_BUILDER = 64;
   public static final int MAX_DRAFT_ROOM_REFS = 256;
   public static final int MAX_FIXED_PLACEMENTS = 64;
   public static final int MAX_FIXED_CONNECTIONS = 128;
   public static final int MAX_FIXED_COORDINATE = 1000000;
   public static final int MAX_PROJECT_MARKERS = 256;
   public static final int MAX_PROJECT_SOCKETS = 64;
   public static final int MAX_PROJECT_REGIONS = 64;
   public static final int MAX_PROJECT_ENCOUNTERS = 128;
   private static final int MAX_NAMESPACE_LENGTH = 32;
   private static final int MAX_NAME_LENGTH = 48;
   private static final int MAX_LOCAL_ID_LENGTH = 64;
   public static final int MAX_STRUCTURE_AXIS = 48;
   public static final long MAX_STRUCTURE_VOLUME = 110592L;
   public static final int MAX_ENCOUNTER_LEVEL = 1000;
   public static final int MAX_POOL_WEIGHT = 1000000;
   public static final int MAX_BASE_XP = 1000000;
   private final Map<UUID, DungeonBuilderProjectData.Workspace> workspaces = new HashMap<>();

   public static DungeonBuilderProjectData get(ServerLevel level) {
      ServerLevel storageLevel = level.getServer().overworld();
      return storageLevel.getDataStorage()
         .computeIfAbsent(DungeonBuilderProjectData::load, DungeonBuilderProjectData::new, "sololeveling_dungeon_builder_projects");
   }

   public long revision(ServerPlayer player) {
      return this.workspace(player).revision;
   }

   public long touch(ServerPlayer player) {
      return this.touch(this.workspace(player));
   }

   public BuilderWorkspaceSnapshot workspaceSnapshot(ServerPlayer player) {
      DungeonBuilderProjectData.Workspace workspace = this.workspace(player);
      return new BuilderWorkspaceSnapshot(
         workspace.revision,
         workspace.activeId,
         workspace.projects.keySet().stream().sorted().toList(),
         workspace.mobPools.values().stream().sorted(Comparator.comparing(pool -> pool.id().toString())).toList(),
         workspace.dungeonDrafts.values().stream().sorted(Comparator.comparing(draft -> draft.id().toString())).toList()
      );
   }

   public DungeonBuilderProjectData.Project project(ServerPlayer player) {
      DungeonBuilderProjectData.Workspace workspace = this.workspace(player);
      DungeonBuilderProjectData.Project active = workspace.active();
      if (active != null) {
         return active;
      }

      DungeonBuilderProjectData.Project project = new DungeonBuilderProjectData.Project(
         defaultNamespace(player), "my_dungeon", DungeonBuilderProjectData.ProjectKind.PRESET
      );
      workspace.addAndSelect(project);
      this.bindProject(workspace, project);
      this.touch(workspace);
      return project;
   }

   public DungeonBuilderProjectData.ProjectResult createProject(ServerPlayer player, String namespace, String name, DungeonBuilderProjectData.ProjectKind kind) {
      String problem = authorIdProblem(namespace, "namespace", 32);
      if (problem != null) {
         return DungeonBuilderProjectData.ProjectResult.failure(problem);
      }

      problem = authorIdProblem(name, "name", 48);
      if (problem != null) {
         return DungeonBuilderProjectData.ProjectResult.failure(problem);
      }

      String cleanNamespace = namespace;
      String cleanName = name;
      String id = projectId(cleanNamespace, cleanName);
      DungeonBuilderProjectData.Workspace workspace = this.workspace(player);
      if (workspace.projects.containsKey(id)) {
         return DungeonBuilderProjectData.ProjectResult.failure("Project " + id + " already exists. Select it instead.");
      }

      if (workspace.projects.size() >= 128) {
         return DungeonBuilderProjectData.ProjectResult.failure("This workspace already has the maximum of 128 projects.");
      }

      DungeonBuilderProjectData.Project project = new DungeonBuilderProjectData.Project(cleanNamespace, cleanName, kind);
      if (kind == DungeonBuilderProjectData.ProjectKind.MODULE) {
         workspace.projects
            .values()
            .stream()
            .filter(existing -> existing.kind == DungeonBuilderProjectData.ProjectKind.MODULE && existing.namespace.equals(cleanNamespace))
            .findFirst()
            .ifPresent(existing -> project.allowedRanks = EnumSet.copyOf(existing.allowedRanks));
      }

      workspace.addAndSelect(project);
      this.bindProject(workspace, project);
      this.touch(workspace);
      return DungeonBuilderProjectData.ProjectResult.success(project, "Created and selected " + id + ".");
   }

   public DungeonBuilderProjectData.ProjectResult selectProject(ServerPlayer player, String namespace, String name) {
      DungeonBuilderProjectData.Workspace workspace = this.workspaces.get(player.getUUID());
      String problem = authorIdProblem(namespace, "namespace", 32);
      if (problem != null) {
         return DungeonBuilderProjectData.ProjectResult.failure(problem);
      } else {
         problem = authorIdProblem(name, "name", 48);
         if (problem != null) {
            return DungeonBuilderProjectData.ProjectResult.failure(problem);
         } else {
            String id = projectId(namespace, name);
            if (workspace != null && workspace.projects.containsKey(id)) {
               workspace.activeId = id;
               this.bindProject(workspace, workspace.projects.get(id));
               this.touch(workspace);
               return DungeonBuilderProjectData.ProjectResult.success(workspace.projects.get(id), "Selected " + id + ".");
            } else {
               return DungeonBuilderProjectData.ProjectResult.failure("No project named " + id + " exists in your workspace.");
            }
         }
      }
   }

   public DungeonBuilderProjectData.ProjectResult resetActiveProject(ServerPlayer player) {
      DungeonBuilderProjectData.Workspace workspace = this.workspaces.get(player.getUUID());
      DungeonBuilderProjectData.Project active = workspace == null ? null : workspace.active();
      if (active == null) {
         return DungeonBuilderProjectData.ProjectResult.failure("There is no active project to reset.");
      }

      DungeonBuilderProjectData.Project replacement = new DungeonBuilderProjectData.Project(active.namespace, active.name, active.kind);
      workspace.addAndSelect(replacement);
      this.bindProject(workspace, replacement);
      this.touch(workspace);
      return DungeonBuilderProjectData.ProjectResult.success(replacement, "Reset " + replacement.id() + " to an empty project.");
   }

   public DungeonBuilderProjectData.ProjectResult deleteProject(ServerPlayer player, String namespace, String name) {
      DungeonBuilderProjectData.Workspace workspace = this.workspaces.get(player.getUUID());
      String problem = authorIdProblem(namespace, "namespace", 32);
      if (problem != null) {
         return DungeonBuilderProjectData.ProjectResult.failure(problem);
      }

      problem = authorIdProblem(name, "name", 48);
      if (problem != null) {
         return DungeonBuilderProjectData.ProjectResult.failure(problem);
      }

      String id = projectId(namespace, name);
      if (workspace != null && workspace.projects.containsKey(id)) {
         ResourceLocation projectResource = ResourceLocation.tryParse(id);
         if (projectResource != null
            && workspace.dungeonDrafts
               .values()
               .stream()
               .anyMatch(
                  draft -> draft.rooms().stream().anyMatch(ref -> ref.room().equals(projectResource))
                     || draft.fixedPlacements().stream().anyMatch(placement -> placement.room().equals(projectResource))
               )) {
            return DungeonBuilderProjectData.ProjectResult.failure(
               "Project " + id + " is referenced by a dungeon draft. Remove it from that draft before deleting the room."
            );
         }

         workspace.projects.remove(id);
         if (id.equals(workspace.activeId)) {
            workspace.activeId = workspace.projects.keySet().stream().findFirst().orElse("");
         }

         this.touch(workspace);
         return DungeonBuilderProjectData.ProjectResult.success(workspace.active(), "Deleted project " + id + ". Exported datapacks were not removed.");
      } else {
         return DungeonBuilderProjectData.ProjectResult.failure("No project named " + id + " exists in your workspace.");
      }
   }

   public List<DungeonBuilderProjectData.Project> projects(ServerPlayer player) {
      this.project(player);
      DungeonBuilderProjectData.Workspace workspace = this.workspaces.get(player.getUUID());
      this.bindWorkspace(workspace);
      return workspace.projects.values().stream().sorted(Comparator.comparing(DungeonBuilderProjectData.Project::id)).toList();
   }

   public boolean isActive(ServerPlayer player, DungeonBuilderProjectData.Project project) {
      DungeonBuilderProjectData.Workspace workspace = this.workspaces.get(player.getUUID());
      return workspace != null && project.id().equals(workspace.activeId);
   }

   private DungeonBuilderProjectData.Workspace workspace(ServerPlayer player) {
      DungeonBuilderProjectData.Workspace workspace = this.workspaces.computeIfAbsent(player.getUUID(), uuid -> new DungeonBuilderProjectData.Workspace());
      this.bindWorkspace(workspace);
      return workspace;
   }

   private void bindWorkspace(@Nullable DungeonBuilderProjectData.Workspace workspace) {
      if (workspace != null) {
         workspace.projects.values().forEach(project -> this.bindProject(workspace, project));
      }
   }

   private void bindProject(DungeonBuilderProjectData.Workspace workspace, DungeonBuilderProjectData.Project project) {
      project.bindMutationHook(() -> this.touch(workspace));
   }

   private long touch(DungeonBuilderProjectData.Workspace workspace) {
      if (workspace.revision < Long.MAX_VALUE) {
         workspace.revision++;
      }

      this.setDirty();
      return workspace.revision;
   }

   public List<BuilderMobPool> mobPools(ServerPlayer player) {
      return this.workspace(player).mobPools.values().stream().sorted(Comparator.comparing(pool -> pool.id().toString())).toList();
   }

   public Optional<BuilderMobPool> mobPool(ServerPlayer player, ResourceLocation id) {
      return id == null ? Optional.empty() : Optional.ofNullable(this.workspace(player).mobPools.get(id));
   }

   public DungeonBuilderProjectData.MutationResult upsertMobPool(ServerPlayer player, BuilderMobPool pool) {
      if (pool == null) {
         return DungeonBuilderProjectData.MutationResult.failure("Mob pool is required.", this.revision(player));
      }

      DungeonBuilderProjectData.Workspace workspace = this.workspace(player);
      String problem = mobPoolProblem(pool);
      if (problem != null) {
         return DungeonBuilderProjectData.MutationResult.failure(problem, workspace.revision);
      }

      if (!workspace.mobPools.containsKey(pool.id()) && workspace.mobPools.size() >= 128) {
         return DungeonBuilderProjectData.MutationResult.failure("This workspace already has the maximum of 128 mob pools.", workspace.revision);
      }

      long otherEntries = workspace.mobPools
         .values()
         .stream()
         .filter(existing -> !existing.id().equals(pool.id()))
         .mapToLong(existing -> existing.entries().size())
         .sum();
      if (otherEntries + pool.entries().size() > 2048L) {
         return DungeonBuilderProjectData.MutationResult.failure(
            "Studio workspaces support at most 2048 total mob-pool entries so workspace snapshots remain network-safe.", workspace.revision
         );
      }

      workspace.mobPools.put(pool.id(), pool);
      long revision = this.touch(workspace);
      return DungeonBuilderProjectData.MutationResult.success("Saved mob pool " + pool.id() + ".", revision);
   }

   public DungeonBuilderProjectData.MutationResult deleteMobPool(ServerPlayer player, ResourceLocation id) {
      DungeonBuilderProjectData.Workspace workspace = this.workspace(player);
      if (id != null && workspace.mobPools.containsKey(id)) {
         String value = id.toString();
         boolean referenced = workspace.projects
            .values()
            .stream()
            .anyMatch(
               project -> project.defaultMobPool.equals(value)
                  || project.bossMobPool.equals(value)
                  || project.encounters.values().stream().anyMatch(encounter -> encounter.pool.equals(value))
            );
         if (referenced) {
            return DungeonBuilderProjectData.MutationResult.failure(
               "Mob pool " + id + " is still referenced by a room or encounter. Reassign those references first.", workspace.revision
            );
         }

         workspace.mobPools.remove(id);
         long revision = this.touch(workspace);
         return DungeonBuilderProjectData.MutationResult.success("Deleted mob pool " + id + ".", revision);
      } else {
         return DungeonBuilderProjectData.MutationResult.failure("No authored mob pool named " + id + " exists.", workspace.revision);
      }
   }

   public List<DungeonDraft> dungeonDrafts(ServerPlayer player) {
      return this.workspace(player).dungeonDrafts.values().stream().sorted(Comparator.comparing(draft -> draft.id().toString())).toList();
   }

   public Optional<DungeonDraft> dungeonDraft(ServerPlayer player, ResourceLocation id) {
      return id == null ? Optional.empty() : Optional.ofNullable(this.workspace(player).dungeonDrafts.get(id));
   }

   public DungeonBuilderProjectData.MutationResult upsertDungeonDraft(ServerPlayer player, DungeonDraft draft) {
      if (draft == null) {
         return DungeonBuilderProjectData.MutationResult.failure("Dungeon draft is required.", this.revision(player));
      }

      DungeonBuilderProjectData.Workspace workspace = this.workspace(player);
      String problem = dungeonDraftProblem(workspace, draft);
      if (problem != null) {
         return DungeonBuilderProjectData.MutationResult.failure(problem, workspace.revision);
      }

      if (!workspace.dungeonDrafts.containsKey(draft.id()) && workspace.dungeonDrafts.size() >= 64) {
         return DungeonBuilderProjectData.MutationResult.failure("This workspace already has the maximum of 64 dungeon drafts.", workspace.revision);
      }

      workspace.dungeonDrafts.put(draft.id(), draft);
      long revision = this.touch(workspace);
      return DungeonBuilderProjectData.MutationResult.success("Saved dungeon draft " + draft.id() + ".", revision);
   }

   public DungeonBuilderProjectData.MutationResult deleteDungeonDraft(ServerPlayer player, ResourceLocation id) {
      DungeonBuilderProjectData.Workspace workspace = this.workspace(player);
      if (id != null && workspace.dungeonDrafts.remove(id) != null) {
         long revision = this.touch(workspace);
         return DungeonBuilderProjectData.MutationResult.success("Deleted dungeon draft " + id + ".", revision);
      } else {
         return DungeonBuilderProjectData.MutationResult.failure("No dungeon draft named " + id + " exists.", workspace.revision);
      }
   }

   private static String defaultNamespace(ServerPlayer player) {
      return sanitizeId(player.getGameProfile().getName(), "builder", 32);
   }

   public static String sanitizeId(String value, String fallback) {
      return sanitizeId(value, fallback, 48);
   }

   private static String sanitizeId(String value, String fallback, int maxLength) {
      String sanitized = value == null
         ? ""
         : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_").replaceAll("_+", "_").replaceAll("^[._-]+|[._-]+$", "");
      if (sanitized.length() > maxLength) {
         sanitized = sanitized.substring(0, maxLength);
      }

      if (sanitized.matches("(con|prn|aux|nul|com[1-9]|lpt[1-9])(\\..*)?")) {
         sanitized = fallback;
      }

      return sanitized.isBlank() ? fallback : sanitized;
   }

   @Nullable
   private static String authorIdProblem(String value, String label, int maxLength) {
      if (value != null && !value.isBlank()) {
         if (value.length() > maxLength) {
            return "Project " + label + " is too long (maximum " + maxLength + " characters).";
         } else if (value.matches("[a-z0-9][a-z0-9_.-]*") && !value.endsWith(".")) {
            String lower = value.toLowerCase(Locale.ROOT);
            return lower.matches("(con|prn|aux|nul|com[1-9]|lpt[1-9])(\\..*)?") ? "Project " + label + " uses a reserved file name." : null;
         } else {
            return "Project " + label + " must use lowercase letters, numbers, _, - or . and start with a letter or number.";
         }
      } else {
         return "Project " + label + " cannot be empty.";
      }
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag tag) {
      tag.putInt("SchemaVersion", 6);
      ListTag workspaceList = new ListTag();
      this.workspaces.entrySet().stream().sorted(Entry.comparingByKey()).forEach(entry -> {
         CompoundTag workspaceTag = entry.getValue().save();
         workspaceTag.putUUID("Owner", entry.getKey());
         workspaceList.add(workspaceTag);
      });
      tag.put("Workspaces", workspaceList);
      return tag;
   }

   private static DungeonBuilderProjectData load(CompoundTag tag) {
      DungeonBuilderProjectData data = new DungeonBuilderProjectData();
      if (tag.contains("SchemaVersion", 3)) {
         tag.getInt("SchemaVersion");
      } else {
         int schemaVersion = 1;
      }

      if (tag.contains("Workspaces", 9)) {
         ListTag workspaceList = tag.getList("Workspaces", 10);

         for (int i = 0; i < workspaceList.size(); i++) {
            CompoundTag workspaceTag = workspaceList.getCompound(i);
            if (workspaceTag.hasUUID("Owner")) {
               data.workspaces.put(workspaceTag.getUUID("Owner"), DungeonBuilderProjectData.Workspace.load(workspaceTag));
            }
         }

         data.workspaces.values().forEach(data::bindWorkspace);
         return data;
      } else {
         ListTag projectList = tag.getList("Projects", 10);

         for (int i = 0; i < projectList.size(); i++) {
            CompoundTag projectTag = projectList.getCompound(i);
            if (projectTag.hasUUID("Owner")) {
               DungeonBuilderProjectData.Workspace workspace = data.workspaces
                  .computeIfAbsent(projectTag.getUUID("Owner"), uuid -> new DungeonBuilderProjectData.Workspace());
               workspace.addAndSelect(DungeonBuilderProjectData.Project.load(projectTag));
            }
         }

         data.workspaces.values().forEach(data::bindWorkspace);
         return data;
      }
   }

   @Nullable
   private static String mobPoolProblem(BuilderMobPool pool) {
      if (pool != null && pool.id() != null) {
         if (pool.id().toString().length() > 192) {
            return "Mob-pool resource IDs may contain at most 192 characters.";
         }

         if (safeResourceNamespace(pool.id().getNamespace()) && safeResourcePath(pool.id().getPath())) {
            if (pool.entries().size() > 256) {
               return "Mob pool " + pool.id() + " has more than 256 entries.";
            }

            int index = 0;

            while (index < pool.entries().size()) {
               BuilderMobPool.Entry entry = pool.entries().get(index);
               if (entry != null && entry.selectorKind() != null && entry.selector() != null) {
                  if (entry.selector().toString().length() > 192) {
                     return "Mob-pool entry " + (index + 1) + " has an excessively long selector.";
                  }

                  if (entry.selectorKind() == BuilderMobPool.SelectorKind.ENTITY && entry.selector().toString().equals("minecraft:player")) {
                     return "minecraft:player cannot be used as a dungeon mob.";
                  }

                  if (entry.weight() >= 1 && entry.weight() <= 1000000) {
                     if (entry.requiredMod().isPresent() && !entry.requiredMod().get().matches("[a-z][a-z0-9_-]{1,63}")) {
                        return "Mob-pool entry " + (index + 1) + " has an invalid required mod ID.";
                     }

                     if (entry.eligibleLevel().isPresent() && !rangeInside(entry.eligibleLevel().get(), 0, 1000)) {
                        return "Mob-pool entry " + (index + 1) + " eligible levels must be 0-1000.";
                     }

                     if (entry.spawnLevel().isPresent() && !rangeInside(entry.spawnLevel().get(), 1, 1000)) {
                        return "Mob-pool entry " + (index + 1) + " spawn levels must be 1-1000.";
                     }

                     if (!entry.baseXp().isPresent() || entry.baseXp().get() >= 0 && entry.baseXp().get() <= 1000000) {
                        index++;
                        continue;
                     }

                     return "Mob-pool entry " + (index + 1) + " XP must be 0-1000000.";
                  }

                  return "Mob-pool entry " + (index + 1) + " weight must be 1-1000000.";
               }

               return "Mob-pool entry " + (index + 1) + " is missing its selector.";
            }

            return null;
         } else {
            return "Mob-pool IDs must use filesystem-safe namespace and path segments.";
         }
      } else {
         return "Mob pool and its resource ID are required.";
      }
   }

   private static boolean rangeInside(BuilderMobPool.LevelRange range, int minimum, int maximum) {
      return range != null && range.min() >= minimum && range.max() >= range.min() && range.max() <= maximum;
   }

   @Nullable
   private static String dungeonDraftStructuralProblem(DungeonDraft draft) {
      if (draft != null && draft.id() != null && draft.mode() != null && draft.topology() != null) {
         if (draft.id().toString().length() > 192) {
            return "Dungeon-draft resource IDs may contain at most 192 characters.";
         }

         if (safeResourceNamespace(draft.id().getNamespace()) && safeResourcePath(draft.id().getPath())) {
            if (draft.rooms().size() > 256) {
               return "A dungeon draft may reference at most 256 room modules.";
            }

            if (draft.allowedRanks().isEmpty()) {
               return "A dungeon draft must allow at least one rank.";
            }

            if (draft.mode() != DungeonDraft.Mode.PROCEDURAL || draft.minRooms() >= 3 && draft.maxRooms() >= draft.minRooms() && draft.maxRooms() <= 64) {
               if (draft.mode() == DungeonDraft.Mode.PROCEDURAL && draft.topology() == DungeonDraft.Topology.BRANCHING && draft.minRooms() < 4) {
                  return "Branching dungeons need a minimum of at least 4 rooms so one room can form a real branch.";
               }

               if (draft.maxDepth() < 1 || draft.maxDepth() > 64) {
                  return "Dungeon max depth must be 1-64.";
               }

               if (draft.shellBlock() == null) {
                  return "A dungeon shell block is required.";
               }

               if (draft.shellThickness() >= 0 && draft.shellThickness() <= 4) {
                  if (draft.fixedPlacements().size() > 64) {
                     return "A fixed dungeon may contain at most 64 placements.";
                  }

                  if (draft.fixedConnections().size() > 128) {
                     return "A fixed dungeon may contain at most 128 connections.";
                  }

                  Set<ResourceLocation> roomIds = new HashSet<>();

                  for (DungeonDraft.RoomRef room : draft.rooms()) {
                     if (room != null && room.room() != null) {
                        if (safeResourceNamespace(room.room().getNamespace()) && safeResourcePath(room.room().getPath())) {
                           if (!roomIds.add(room.room())) {
                              return "Dungeon draft contains duplicate room reference " + room.room() + ".";
                           }

                           if (room.weight() >= 1 && room.weight() <= 1000000) {
                              continue;
                           }

                           return "Room " + room.room() + " weight must be 1-1000000.";
                        }

                        return "Room reference " + room.room() + " is not filesystem-safe.";
                     }

                     return "Every room reference needs a resource ID.";
                  }

                  Set<String> placementIds = new HashSet<>();

                  for (DungeonDraft.FixedPlacement placement : draft.fixedPlacements()) {
                     if (placement != null && placement.room() != null && placement.rotation() != null) {
                        if (safeResourceNamespace(placement.room().getNamespace()) && safeResourcePath(placement.room().getPath())) {
                           String idProblem = localIdProblem(placement.id(), "placement");
                           if (idProblem != null) {
                              return idProblem;
                           }

                           if (!placementIds.add(placement.id())) {
                              return "Duplicate fixed placement ID " + placement.id() + ".";
                           }

                           if (Math.abs((long)placement.x()) <= 1000000L
                              && Math.abs((long)placement.y()) <= 1000000L
                              && Math.abs((long)placement.z()) <= 1000000L) {
                              continue;
                           }

                           return "Fixed placement " + placement.id() + " is outside the supported coordinate range.";
                        }

                        return "Fixed placement room " + placement.room() + " is not filesystem-safe.";
                     }

                     return "Every fixed placement needs an ID, room, and rotation.";
                  }

                  for (DungeonDraft.FixedConnection connection : draft.fixedConnections()) {
                     if (connection == null) {
                        return "Fixed connections cannot be null.";
                     }

                     String problem = localIdProblem(connection.fromPlacement(), "connection placement");
                     if (problem == null) {
                        problem = localIdProblem(connection.toPlacement(), "connection placement");
                     }

                     if (problem == null) {
                        problem = localIdProblem(connection.fromSocket(), "connection socket");
                     }

                     if (problem == null) {
                        problem = localIdProblem(connection.toSocket(), "connection socket");
                     }

                     if (problem != null) {
                        return problem;
                     }

                     if (connection.fromPlacement().equals(connection.toPlacement())) {
                        return "A fixed connection cannot connect a placement to itself.";
                     }
                  }

                  return null;
               } else {
                  return "Dungeon shell thickness must be 0-4.";
               }
            } else {
               return "Dungeon room counts must be 3-64 and max must be at least min.";
            }
         } else {
            return "Dungeon-draft IDs must use filesystem-safe namespace and path segments.";
         }
      } else {
         return "Dungeon draft ID, mode, and topology are required.";
      }
   }

   @Nullable
   private static String dungeonDraftProblem(DungeonBuilderProjectData.Workspace workspace, DungeonDraft draft) {
      String structural = dungeonDraftStructuralProblem(draft);
      if (structural != null) {
         return structural;
      }

      if (ForgeRegistries.BLOCKS.containsKey(draft.shellBlock()) && ForgeRegistries.BLOCKS.getValue(draft.shellBlock()) != Blocks.AIR) {
         for (DungeonDraft.RoomRef ref : draft.rooms()) {
            DungeonBuilderProjectData.Project project = workspace.projects.get(ref.room().toString());
            if (project == null || project.kind != DungeonBuilderProjectData.ProjectKind.MODULE) {
               return "Referenced room " + ref.room() + " is not a module in this workspace.";
            }
         }

         Map<String, DungeonDraft.FixedPlacement> placements = new LinkedHashMap<>();

         for (DungeonDraft.FixedPlacement placement : draft.fixedPlacements()) {
            DungeonBuilderProjectData.Project project = workspace.projects.get(placement.room().toString());
            if (project == null || project.kind != DungeonBuilderProjectData.ProjectKind.MODULE) {
               return "Fixed placement " + placement.id() + " references missing module " + placement.room() + ".";
            }

            placements.put(placement.id(), placement);
         }

         Set<String> usedEndpoints = new HashSet<>();

         for (DungeonDraft.FixedConnection connection : draft.fixedConnections()) {
            DungeonDraft.FixedPlacement from = placements.get(connection.fromPlacement());
            DungeonDraft.FixedPlacement to = placements.get(connection.toPlacement());
            if (from != null && to != null) {
               DungeonBuilderProjectData.Project fromProject = workspace.projects.get(from.room().toString());
               DungeonBuilderProjectData.Project toProject = workspace.projects.get(to.room().toString());
               if (fromProject != null && !fromProject.sockets.stream().noneMatch(socket -> socket.id.equals(connection.fromSocket()))) {
                  if (toProject != null && !toProject.sockets.stream().noneMatch(socket -> socket.id.equals(connection.toSocket()))) {
                     String firstEndpoint = connection.fromPlacement() + "\u0000" + connection.fromSocket();
                     String secondEndpoint = connection.toPlacement() + "\u0000" + connection.toSocket();
                     if (usedEndpoints.add(firstEndpoint) && usedEndpoints.add(secondEndpoint)) {
                        continue;
                     }

                     return "A fixed-layout socket cannot be used by more than one connection.";
                  }

                  return "Fixed connection references missing socket " + connection.toSocket() + " on " + to.id() + ".";
               }

               return "Fixed connection references missing socket " + connection.fromSocket() + " on " + from.id() + ".";
            }

            return "Fixed connection references a placement that does not exist.";
         }

         return null;
      } else {
         return "Shell block " + draft.shellBlock() + " is not a loaded, solid block.";
      }
   }

   @Nullable
   private static String localIdProblem(String value, String label) {
      if (value != null && !value.isBlank()) {
         if (value.length() > 64) {
            return "Fixed " + label + " ID may contain at most 64 characters.";
         } else {
            return value.matches("[a-z0-9][a-z0-9_.-]*") && !value.endsWith(".")
               ? null
               : "Fixed " + label + " IDs must use lowercase letters, numbers, _, - or .";
         }
      } else {
         return "Fixed " + label + " ID cannot be empty.";
      }
   }

   private static boolean safeResourceNamespace(String namespace) {
      return safeFilesystemSegment(namespace);
   }

   private static boolean safeResourcePath(String path) {
      if (path != null && !path.isBlank() && !path.startsWith("/") && !path.endsWith("/")) {
         for (String segment : path.split("/", -1)) {
            if (!safeFilesystemSegment(segment)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean safeFilesystemSegment(String segment) {
      return segment != null && !segment.isBlank() && !segment.equals(".") && !segment.equals("..") && !segment.endsWith(".")
         ? !segment.toLowerCase(Locale.ROOT).matches("(con|prn|aux|nul|com[1-9]|lpt[1-9])(\\..*)?")
         : false;
   }

   @Nullable
   private static String roomSnapshotProblem(@Nullable RoomSnapshot snapshot, @Nullable DungeonBuilderProjectData.Bounds bounds) {
      if (snapshot == null) {
         return "Room snapshot metadata is required.";
      } else if (bounds == null) {
         return "Select structure bounds before saving a room snapshot.";
      } else {
         BlockPos size = snapshot.size();
         if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1 || size.getX() > 48 || size.getY() > 48 || size.getZ() > 48) {
            return "Room snapshot size must be 1-48 blocks on each axis.";
         } else if (!size.equals(bounds.size()) || !snapshot.captureMin().equals(bounds.min())) {
            return "Room snapshot capture bounds no longer match this project's structure bounds.";
         } else if (snapshot.structureKey().toString().length() > 192) {
            return "Room snapshot structure key is too long.";
         } else if (!snapshot.checksum().matches("(?i)[0-9a-f]{32,128}")) {
            return "Room snapshot checksum must be a 32-128 character hexadecimal digest.";
         } else {
            return snapshot.capturedAt() >= 0L && snapshot.metadataRevision() >= 0L ? null : "Room snapshot timestamps and revisions cannot be negative.";
         }
      }
   }

   private static CompoundTag saveRoomSnapshot(RoomSnapshot snapshot) {
      CompoundTag tag = new CompoundTag();
      tag.putString("StructureKey", snapshot.structureKey().toString());
      tag.put("Size", NbtUtils.writeBlockPos(snapshot.size()));
      tag.put("CaptureMin", NbtUtils.writeBlockPos(snapshot.captureMin()));
      tag.putString("Checksum", snapshot.checksum());
      tag.putLong("CapturedAt", snapshot.capturedAt());
      tag.putLong("MetadataRevision", snapshot.metadataRevision());
      return tag;
   }

   private static RoomSnapshot loadRoomSnapshot(CompoundTag tag) {
      ResourceLocation structureKey = ResourceLocation.tryParse(tag.getString("StructureKey"));
      if (structureKey != null && tag.contains("Size", 10) && tag.contains("CaptureMin", 10)) {
         return new RoomSnapshot(
            structureKey,
            NbtUtils.readBlockPos(tag.getCompound("Size")),
            NbtUtils.readBlockPos(tag.getCompound("CaptureMin")),
            tag.getString("Checksum"),
            tag.contains("CapturedAt", 4) ? tag.getLong("CapturedAt") : 0L,
            tag.contains("MetadataRevision", 4) ? tag.getLong("MetadataRevision") : 0L
         );
      } else {
         throw new IllegalArgumentException("Malformed room snapshot metadata");
      }
   }

   private static CompoundTag saveMobPool(BuilderMobPool pool) {
      CompoundTag tag = new CompoundTag();
      tag.putString("Id", pool.id().toString());
      ListTag entries = new ListTag();

      for (BuilderMobPool.Entry entry : pool.entries()) {
         CompoundTag entryTag = new CompoundTag();
         entryTag.putString("SelectorKind", entry.selectorKind().name());
         entryTag.putString("Selector", entry.selector().toString());
         entryTag.putInt("Weight", entry.weight());
         entry.requiredMod().ifPresent(value -> entryTag.putString("RequiredMod", value));
         entry.eligibleLevel().ifPresent(value -> entryTag.put("EligibleLevel", saveRange(value)));
         entry.spawnLevel().ifPresent(value -> entryTag.put("SpawnLevel", saveRange(value)));
         entry.baseXp().ifPresent(value -> entryTag.putInt("BaseXp", value));
         entries.add(entryTag);
      }

      tag.put("Entries", entries);
      return tag;
   }

   private static BuilderMobPool loadMobPool(CompoundTag tag) {
      ResourceLocation id = ResourceLocation.tryParse(tag.getString("Id"));
      if (id == null) {
         throw new IllegalArgumentException("Malformed mob-pool ID");
      }

      List<BuilderMobPool.Entry> entries = new ArrayList<>();
      if (tag.contains("Entries", 9)) {
         ListTag list = tag.getList("Entries", 10);

         for (int index = 0; index < list.size() && entries.size() < 256; index++) {
            try {
               CompoundTag entry = list.getCompound(index);
               BuilderMobPool.SelectorKind kind = BuilderMobPool.SelectorKind.valueOf(entry.getString("SelectorKind").toUpperCase(Locale.ROOT));
               ResourceLocation selector = ResourceLocation.tryParse(entry.getString("Selector"));
               if (selector != null) {
                  entries.add(
                     new BuilderMobPool.Entry(
                        kind,
                        selector,
                        entry.contains("Weight", 3) ? entry.getInt("Weight") : 1,
                        entry.contains("RequiredMod", 8) ? Optional.of(entry.getString("RequiredMod")) : Optional.empty(),
                        entry.contains("EligibleLevel", 10) ? Optional.of(loadRange(entry.getCompound("EligibleLevel"))) : Optional.empty(),
                        entry.contains("SpawnLevel", 10) ? Optional.of(loadRange(entry.getCompound("SpawnLevel"))) : Optional.empty(),
                        entry.contains("BaseXp", 3) ? Optional.of(entry.getInt("BaseXp")) : Optional.empty()
                     )
                  );
               }
            } catch (RuntimeException var8) {
            }
         }
      }

      return new BuilderMobPool(id, entries);
   }

   private static CompoundTag saveRange(BuilderMobPool.LevelRange range) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("Min", range.min());
      tag.putInt("Max", range.max());
      return tag;
   }

   private static BuilderMobPool.LevelRange loadRange(CompoundTag tag) {
      return new BuilderMobPool.LevelRange(tag.getInt("Min"), tag.getInt("Max"));
   }

   private static CompoundTag saveDungeonDraft(DungeonDraft draft) {
      CompoundTag tag = new CompoundTag();
      tag.putString("Id", draft.id().toString());
      tag.putString("Mode", draft.mode().name());
      tag.putString("Topology", draft.topology().name());
      tag.putString(
         "AllowedRanks", Arrays.stream(ProceduralDungeonRank.values()).filter(draft.allowedRanks()::contains).map(Enum::name).collect(Collectors.joining(","))
      );
      tag.putInt("MinRooms", draft.minRooms());
      tag.putInt("MaxRooms", draft.maxRooms());
      tag.putInt("MaxDepth", draft.maxDepth());
      tag.putString("ShellBlock", draft.shellBlock().toString());
      tag.putInt("ShellThickness", draft.shellThickness());
      ListTag rooms = new ListTag();

      for (DungeonDraft.RoomRef room : draft.rooms()) {
         CompoundTag roomTag = new CompoundTag();
         roomTag.putString("Room", room.room().toString());
         roomTag.putInt("Weight", room.weight());
         rooms.add(roomTag);
      }

      tag.put("Rooms", rooms);
      ListTag placements = new ListTag();

      for (DungeonDraft.FixedPlacement placement : draft.fixedPlacements()) {
         CompoundTag placementTag = new CompoundTag();
         placementTag.putString("Id", placement.id());
         placementTag.putString("Room", placement.room().toString());
         placementTag.putInt("X", placement.x());
         placementTag.putInt("Y", placement.y());
         placementTag.putInt("Z", placement.z());
         placementTag.putString("Rotation", placement.rotation().name());
         placements.add(placementTag);
      }

      tag.put("FixedPlacements", placements);
      ListTag connections = new ListTag();

      for (DungeonDraft.FixedConnection connection : draft.fixedConnections()) {
         CompoundTag connectionTag = new CompoundTag();
         connectionTag.putString("FromPlacement", connection.fromPlacement());
         connectionTag.putString("FromSocket", connection.fromSocket());
         connectionTag.putString("ToPlacement", connection.toPlacement());
         connectionTag.putString("ToSocket", connection.toSocket());
         connections.add(connectionTag);
      }

      tag.put("FixedConnections", connections);
      return tag;
   }

   private static DungeonDraft loadDungeonDraft(CompoundTag tag) {
      ResourceLocation id = ResourceLocation.tryParse(tag.getString("Id"));
      if (id == null) {
         throw new IllegalArgumentException("Malformed dungeon-draft ID");
      }

      DungeonDraft.Mode mode = enumOrDefault(DungeonDraft.Mode.class, tag.getString("Mode"), DungeonDraft.Mode.PROCEDURAL);
      DungeonDraft.Topology topology = enumOrDefault(DungeonDraft.Topology.class, tag.getString("Topology"), DungeonDraft.Topology.LINEAR);
      EnumSet<ProceduralDungeonRank> ranks = EnumSet.noneOf(ProceduralDungeonRank.class);
      if (tag.contains("AllowedRanks", 8)) {
         for (String value : tag.getString("AllowedRanks").split(",")) {
            ProceduralDungeonRank.tryParse(value.trim()).ifPresent(ranks::add);
         }
      }

      if (ranks.isEmpty()) {
         ranks = EnumSet.allOf(ProceduralDungeonRank.class);
      }

      ResourceLocation shell = tag.contains("ShellBlock", 8) ? ResourceLocation.tryParse(tag.getString("ShellBlock")) : null;
      if (shell == null) {
         shell = new ResourceLocation("minecraft", "bedrock");
      }

      List<DungeonDraft.RoomRef> rooms = new ArrayList<>();

      for (CompoundTag roomTag : readList(tag, "Rooms")) {
         if (rooms.size() >= 256) {
            break;
         }

         ResourceLocation room = ResourceLocation.tryParse(roomTag.getString("Room"));
         if (room != null) {
            rooms.add(new DungeonDraft.RoomRef(room, roomTag.contains("Weight", 3) ? roomTag.getInt("Weight") : 1));
         }
      }

      List<DungeonDraft.FixedPlacement> placements = new ArrayList<>();

      for (CompoundTag placementTag : readList(tag, "FixedPlacements")) {
         if (placements.size() >= 64) {
            break;
         }

         ResourceLocation room = ResourceLocation.tryParse(placementTag.getString("Room"));
         if (room != null) {
            placements.add(
               new DungeonDraft.FixedPlacement(
                  placementTag.getString("Id"),
                  room,
                  placementTag.getInt("X"),
                  placementTag.getInt("Y"),
                  placementTag.getInt("Z"),
                  enumOrDefault(DungeonDraft.PlacementRotation.class, placementTag.getString("Rotation"), DungeonDraft.PlacementRotation.NONE)
               )
            );
         }
      }

      List<DungeonDraft.FixedConnection> connections = new ArrayList<>();

      for (CompoundTag connectionTag : readList(tag, "FixedConnections")) {
         if (connections.size() >= 128) {
            break;
         }

         connections.add(
            new DungeonDraft.FixedConnection(
               connectionTag.getString("FromPlacement"),
               connectionTag.getString("FromSocket"),
               connectionTag.getString("ToPlacement"),
               connectionTag.getString("ToSocket")
            )
         );
      }

      return new DungeonDraft(
         id,
         mode,
         topology,
         rooms,
         ranks,
         tag.contains("MinRooms", 3) ? tag.getInt("MinRooms") : 3,
         tag.contains("MaxRooms", 3) ? tag.getInt("MaxRooms") : 3,
         tag.contains("MaxDepth", 3) ? tag.getInt("MaxDepth") : 3,
         shell,
         tag.contains("ShellThickness", 3) ? tag.getInt("ShellThickness") : 1,
         placements,
         connections
      );
   }

   private static <E extends Enum<E>> E enumOrDefault(Class<E> type, String value, E fallback) {
      if (value != null) {
         try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var4) {
         }
      }

      return fallback;
   }

   private static ListTag saveList(List<CompoundTag> entries) {
      ListTag list = new ListTag();
      entries.forEach(list::add);
      return list;
   }

   private static List<CompoundTag> readList(CompoundTag tag, String key) {
      ListTag list = tag.getList(key, 10);
      List<CompoundTag> result = new ArrayList<>(list.size());

      for (int i = 0; i < list.size(); i++) {
         result.add(list.getCompound(i));
      }

      return result;
   }

   private static String nextId(String base, List<String> existing) {
      String cleanBase = sanitizeId(base, "marker");

      for (int index = 1; index < Integer.MAX_VALUE; index++) {
         String candidate = cleanBase + "_" + index;
         if (!existing.contains(candidate)) {
            return candidate;
         }
      }

      return cleanBase;
   }

   private static String projectId(String namespace, String name) {
      return namespace + ":" + name;
   }

   private static boolean isResourceId(String value) {
      return value != null && ResourceLocation.tryParse(value) != null;
   }

   private static String display(String id) {
      String value = id.replace('_', ' ');
      return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
   }

   private static String rankText(Set<ProceduralDungeonRank> ranks) {
      return ranks.size() == ProceduralDungeonRank.values().length
         ? "all"
         : Arrays.stream(ProceduralDungeonRank.values()).filter(ranks::contains).map(Enum::name).collect(Collectors.joining(","));
   }

   private static String shortPos(BlockPos pos) {
      return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
   }

   public record Bounds(BlockPos min, BlockPos max) {
      public Bounds {
         BlockPos first = min;
         BlockPos second = max;
         min = new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
         max = new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
      }

      public boolean contains(BlockPos pos) {
         return pos.getX() >= this.min.getX()
            && pos.getX() <= this.max.getX()
            && pos.getY() >= this.min.getY()
            && pos.getY() <= this.max.getY()
            && pos.getZ() >= this.min.getZ()
            && pos.getZ() <= this.max.getZ();
      }

      public BlockPos size() {
         return new BlockPos(this.max.getX() - this.min.getX() + 1, this.max.getY() - this.min.getY() + 1, this.max.getZ() - this.min.getZ() + 1);
      }

      public long volume() {
         BlockPos size = this.size();
         return (long)size.getX() * size.getY() * size.getZ();
      }

      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.put("Min", NbtUtils.writeBlockPos(this.min));
         tag.put("Max", NbtUtils.writeBlockPos(this.max));
         return tag;
      }

      private static DungeonBuilderProjectData.Bounds load(CompoundTag tag) {
         return new DungeonBuilderProjectData.Bounds(NbtUtils.readBlockPos(tag.getCompound("Min")), NbtUtils.readBlockPos(tag.getCompound("Max")));
      }
   }

   public record Encounter(String id, String pool, int minLevel, int maxLevel, boolean delayed) {
      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putString("Id", this.id);
         tag.putString("Pool", this.pool);
         tag.putInt("MinLevel", this.minLevel);
         tag.putInt("MaxLevel", this.maxLevel);
         tag.putBoolean("Delayed", this.delayed);
         return tag;
      }

      private static DungeonBuilderProjectData.Encounter load(CompoundTag tag) {
         return new DungeonBuilderProjectData.Encounter(
            DungeonBuilderProjectData.sanitizeId(tag.getString("Id"), "default", 64),
            tag.getString("Pool"),
            Math.max(1, tag.getInt("MinLevel")),
            Math.max(1, tag.getInt("MaxLevel")),
            tag.contains("Delayed", 1) && tag.getBoolean("Delayed")
         );
      }
   }

   public record Issue(DungeonBuilderProjectData.Severity severity, String message) {
   }

   public record Marker(String id, String type, BlockPos position, String group) {
      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putString("Id", this.id);
         tag.putString("Type", this.type);
         tag.put("Position", NbtUtils.writeBlockPos(this.position));
         tag.putString("Group", this.group);
         return tag;
      }

      private static DungeonBuilderProjectData.Marker load(CompoundTag tag) {
         String group = tag.contains("Group", 8) ? tag.getString("Group") : "default";
         return new DungeonBuilderProjectData.Marker(tag.getString("Id"), tag.getString("Type"), NbtUtils.readBlockPos(tag.getCompound("Position")), group);
      }
   }

   public record MutationResult(boolean success, String message, long revision) {
      private static DungeonBuilderProjectData.MutationResult success(String message, long revision) {
         return new DungeonBuilderProjectData.MutationResult(true, message, revision);
      }

      private static DungeonBuilderProjectData.MutationResult failure(String message, long revision) {
         return new DungeonBuilderProjectData.MutationResult(false, message, revision);
      }
   }

   private record Pending(String kind, BlockPos first, @Nullable Direction facing) {
      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putString("Kind", this.kind);
         tag.put("First", NbtUtils.writeBlockPos(this.first));
         if (this.facing != null) {
            tag.putString("Facing", this.facing.getName());
         }

         return tag;
      }

      private static DungeonBuilderProjectData.Pending load(CompoundTag tag) {
         Direction facing = tag.contains("Facing") ? Direction.byName(tag.getString("Facing")) : null;
         return new DungeonBuilderProjectData.Pending(tag.getString("Kind"), NbtUtils.readBlockPos(tag.getCompound("First")), facing);
      }
   }

   public static final class Project {
      private static final int UNDO_LIMIT = 32;
      private String namespace;
      private String name;
      private DungeonBuilderProjectData.ProjectKind kind;
      private DungeonBuilderProjectData.RoomRole roomRole;
      private String activeEncounterGroup;
      private String defaultMobPool;
      private String bossMobPool;
      private EnumSet<ProceduralDungeonRank> allowedRanks;
      private int roomWeight;
      private String shellBlock;
      private int shellThickness;
      @Nullable
      private DungeonBuilderProjectData.Bounds structureBounds;
      @Nullable
      private BlockPos origin;
      @Nullable
      private DungeonBuilderProjectData.Pending pending;
      @Nullable
      private RoomSnapshot roomSnapshot;
      @Nullable
      private transient Runnable mutationHook;
      private final List<DungeonBuilderProjectData.Region> regions = new ArrayList<>();
      private final List<DungeonBuilderProjectData.Marker> markers = new ArrayList<>();
      private final List<DungeonBuilderProjectData.Socket> sockets = new ArrayList<>();
      private final Map<String, DungeonBuilderProjectData.Encounter> encounters = new LinkedHashMap<>();
      private final Deque<DungeonBuilderProjectData.Project.ProjectState> undoHistory = new ArrayDeque<>();

      private Project(String namespace, String name, DungeonBuilderProjectData.ProjectKind kind) {
         this.namespace = namespace;
         this.name = name;
         this.kind = kind;
         this.roomRole = DungeonBuilderProjectData.RoomRole.NORMAL;
         this.activeEncounterGroup = "default";
         this.defaultMobPool = namespace + ":" + name + "_default";
         this.bossMobPool = namespace + ":" + name + "_boss";
         this.allowedRanks = EnumSet.allOf(ProceduralDungeonRank.class);
         this.roomWeight = 1;
         this.shellBlock = "minecraft:bedrock";
         this.shellThickness = 1;
      }

      public String namespace() {
         return this.namespace;
      }

      public String id() {
         return DungeonBuilderProjectData.projectId(this.namespace, this.name);
      }

      public String name() {
         return this.name;
      }

      public DungeonBuilderProjectData.ProjectKind kind() {
         return this.kind;
      }

      public DungeonBuilderProjectData.RoomRole roomRole() {
         return this.roomRole;
      }

      public String activeEncounterGroup() {
         return this.activeEncounterGroup;
      }

      public String defaultMobPool() {
         return this.defaultMobPool;
      }

      public String bossMobPool() {
         return this.bossMobPool;
      }

      public Set<ProceduralDungeonRank> allowedRanks() {
         return Set.copyOf(this.allowedRanks);
      }

      public int roomWeight() {
         return this.roomWeight;
      }

      public String shellBlock() {
         return this.shellBlock;
      }

      public int shellThickness() {
         return this.shellThickness;
      }

      public Optional<RoomSnapshot> roomSnapshot() {
         return Optional.ofNullable(this.roomSnapshot);
      }

      @Nullable
      public DungeonBuilderProjectData.Bounds structureBounds() {
         return this.structureBounds;
      }

      @Nullable
      public BlockPos origin() {
         return this.origin;
      }

      public List<DungeonBuilderProjectData.Region> regions() {
         return List.copyOf(this.regions);
      }

      public List<DungeonBuilderProjectData.Marker> markers() {
         return List.copyOf(this.markers);
      }

      public List<DungeonBuilderProjectData.Marker> markersForExport() {
         return this.markers.stream().filter(marker -> !marker.type.equals("spawn_point")).toList();
      }

      public List<DungeonBuilderProjectData.Socket> sockets() {
         return List.copyOf(this.sockets);
      }

      public List<DungeonBuilderProjectData.Encounter> encounters() {
         return List.copyOf(this.encounters.values());
      }

      private void bindMutationHook(Runnable hook) {
         this.mutationHook = hook;
      }

      private void changed() {
         if (this.mutationHook != null) {
            this.mutationHook.run();
         }
      }

      public String setRoomRole(DungeonBuilderProjectData.RoomRole role) {
         if (role == null) {
            return "Room role is required.";
         }

         this.rememberState();
         this.roomRole = role;
         this.pending = null;
         this.changed();
         return "Room role set to " + role.name().toLowerCase(Locale.ROOT) + ".";
      }

      public String selectEncounterGroup(String group) {
         String clean = DungeonBuilderProjectData.sanitizeId(group, "default", 64);
         if (!clean.equals(group)) {
            return "Encounter group IDs must use lowercase letters, numbers, _, - or .";
         }

         this.rememberState();
         this.activeEncounterGroup = clean;
         this.pending = null;
         this.changed();
         return "Active encounter group set to " + clean + ". New spawn markers and trigger regions will use it.";
      }

      public String configureEncounter(String group, String pool, int minLevel, int maxLevel) {
         String clean = DungeonBuilderProjectData.sanitizeId(group, "default", 64);
         if (!clean.equals(group)) {
            return "Encounter group IDs must use lowercase letters, numbers, _, - or .";
         }

         if (!DungeonBuilderProjectData.isResourceId(pool)) {
            return "Mob or mob-pool ID must be a resource ID such as minecraft:zombie or mypack:goblins.";
         }

         boolean automaticLevel = maxLevel == Integer.MAX_VALUE;
         if (minLevel >= 1 && (automaticLevel || maxLevel >= minLevel && maxLevel <= 1000)) {
            if (!this.encounters.containsKey(clean) && this.encounters.size() >= 128) {
               return "This room already has the maximum of 128 encounters.";
            }

            this.rememberState();
            DungeonBuilderProjectData.Encounter previous = this.encounters.get(clean);
            this.encounters.put(clean, new DungeonBuilderProjectData.Encounter(clean, pool, minLevel, maxLevel, previous != null && previous.delayed));
            this.activeEncounterGroup = clean;
            this.pending = null;
            this.changed();
            return "Encounter "
               + clean
               + " now uses "
               + pool
               + (automaticLevel ? " with automatic dungeon/pool levels." : " at levels " + minLevel + "-" + maxLevel + ".");
         } else {
            return "Encounter levels must be 1-1000, or use the automatic dungeon/pool level setting.";
         }
      }

      public String configureEncounterActivation(String encounterId, boolean delayed) {
         DungeonBuilderProjectData.Encounter encounter = this.encounters.get(encounterId);
         if (encounter == null) {
            return "No encounter named " + encounterId + " exists.";
         }

         if (delayed && this.regions.stream().noneMatch(region -> region.type.equals("trigger_region") && region.group.equals(encounterId))) {
            return "Encounter " + encounterId + " cannot be delayed until it has a matching Trigger Region.";
         }

         if (encounter.delayed == delayed) {
            return "Encounter " + encounterId + " is already " + (delayed ? "trigger-delayed." : "automatic.");
         }

         this.rememberState();
         this.encounters
            .put(encounterId, new DungeonBuilderProjectData.Encounter(encounter.id, encounter.pool, encounter.minLevel, encounter.maxLevel, delayed));
         this.pending = null;
         this.changed();
         return "Encounter " + encounterId + " activation set to " + (delayed ? "delayed trigger." : "automatic spawn.");
      }

      public String assignTriggerRegion(String regionId, String encounterId) {
         String clean = DungeonBuilderProjectData.sanitizeId(encounterId, "default", 64);
         if (!clean.equals(encounterId)) {
            return "Encounter group IDs must use lowercase letters, numbers, _, - or .";
         }

         int regionIndex = -1;

         for (int index = 0; index < this.regions.size(); index++) {
            DungeonBuilderProjectData.Region region = this.regions.get(index);
            if (region.id.equals(regionId) && region.type.equals("trigger_region")) {
               regionIndex = index;
               break;
            }
         }

         if (regionIndex < 0) {
            return "No Trigger Region named " + regionId + " exists.";
         }

         if (this.regions.stream().anyMatch(region -> region.type.equals("trigger_region") && region.group.equals(clean) && !region.id.equals(regionId))) {
            return "Encounter " + clean + " already has a Trigger Region.";
         }

         if (!this.encounters.containsKey(clean) && this.encounters.size() >= 128) {
            return "This room already has the maximum of 128 encounters.";
         }

         this.rememberState();
         DungeonBuilderProjectData.Region previousRegion = this.regions.get(regionIndex);
         String previousGroup = previousRegion.group;
         this.regions.set(regionIndex, new DungeonBuilderProjectData.Region(previousRegion.id, previousRegion.type, previousRegion.bounds, clean));
         this.ensureEncounter(clean, false);
         DungeonBuilderProjectData.Encounter target = this.encounters.get(clean);
         this.encounters.put(clean, new DungeonBuilderProjectData.Encounter(target.id, target.pool, target.minLevel, target.maxLevel, true));
         if (!previousGroup.equals(clean)
            && this.regions.stream().noneMatch(region -> region.type.equals("trigger_region") && region.group.equals(previousGroup))) {
            DungeonBuilderProjectData.Encounter previous = this.encounters.get(previousGroup);
            if (previous != null && previous.delayed) {
               this.encounters
                  .put(previousGroup, new DungeonBuilderProjectData.Encounter(previous.id, previous.pool, previous.minLevel, previous.maxLevel, false));
            }
         }

         this.activeEncounterGroup = clean;
         this.pending = null;
         this.changed();
         return "Assigned Trigger Region " + regionId + " to encounter " + clean + ".";
      }

      public String setDefaultMobPool(String pool, boolean boss) {
         if (!DungeonBuilderProjectData.isResourceId(pool)) {
            return "Mob pool must be a resource ID such as mypack:goblins.";
         }

         this.rememberState();
         String previous = boss ? this.bossMobPool : this.defaultMobPool;
         if (boss) {
            this.bossMobPool = pool;
         } else {
            this.defaultMobPool = pool;
         }

         this.encounters
            .replaceAll(
               (id, encounter) -> {
                  boolean bossGroup = this.markers.stream().anyMatch(marker -> marker.group.equals(id) && marker.type.equals("boss_spawn"));
                  return encounter.pool.equals(previous) && bossGroup == boss
                     ? new DungeonBuilderProjectData.Encounter(encounter.id, pool, encounter.minLevel, encounter.maxLevel, encounter.delayed)
                     : encounter;
               }
            );
         this.pending = null;
         this.changed();
         return (boss ? "Boss" : "Default") + " mob pool set to " + pool + ". Existing auto-configured " + (boss ? "boss" : "normal") + " groups were updated.";
      }

      public String setRoomWeight(int weight) {
         if (weight >= 1 && weight <= 10000) {
            this.rememberState();
            this.roomWeight = weight;
            this.changed();
            return "Room generation weight set to " + weight + ".";
         } else {
            return "Room weight must be from 1 to 10000.";
         }
      }

      public String setAllowedRanks(Set<ProceduralDungeonRank> ranks) {
         if (ranks != null && !ranks.isEmpty()) {
            this.rememberState();
            this.allowedRanks = EnumSet.copyOf(ranks);
            this.pending = null;
            this.changed();
            return "Allowed dungeon ranks set to " + DungeonBuilderProjectData.rankText(this.allowedRanks) + ".";
         } else {
            return "Select at least one dungeon rank.";
         }
      }

      public String setShell(String block, int thickness) {
         if (!DungeonBuilderProjectData.isResourceId(block)) {
            return "Shell block must be a resource ID such as minecraft:bedrock.";
         } else {
            ResourceLocation blockId = ResourceLocation.tryParse(block);
            if (blockId == null || !ForgeRegistries.BLOCKS.containsKey(blockId) || ForgeRegistries.BLOCKS.getValue(blockId) == Blocks.AIR) {
               return "Shell block " + block + " is not a loaded, solid block.";
            } else if (thickness >= 0 && thickness <= 4) {
               this.rememberState();
               this.shellBlock = block;
               this.shellThickness = thickness;
               this.changed();
               return thickness == 0 ? "Protective shell disabled." : "Protective shell set to " + thickness + " layer(s) of " + block + ".";
            } else {
               return "Shell thickness must be from 0 (disabled) to 4.";
            }
         }
      }

      public String setRoomSnapshot(RoomSnapshot snapshot) {
         String problem = DungeonBuilderProjectData.roomSnapshotProblem(snapshot, this.structureBounds);
         if (problem != null) {
            return problem;
         }

         this.rememberState();
         this.roomSnapshot = snapshot;
         this.pending = null;
         this.changed();
         return "Saved room snapshot " + snapshot.structureKey() + ".";
      }

      public String clearRoomSnapshot() {
         if (this.roomSnapshot == null) {
            return "This project has no saved room snapshot.";
         }

         this.rememberState();
         this.roomSnapshot = null;
         this.pending = null;
         this.changed();
         return "Cleared the saved room snapshot metadata.";
      }

      @Nullable
      public BlockPos pendingPosition() {
         return this.pending == null ? null : this.pending.first;
      }

      public String selectStructureCorner(BlockPos pos) {
         if (!this.isPending("structure_bounds")) {
            this.pending = new DungeonBuilderProjectData.Pending("structure_bounds", pos.immutable(), null);
            this.changed();
            return "Structure corner 1 set to " + DungeonBuilderProjectData.shortPos(pos) + ". Select the opposite corner.";
         }

         this.rememberState();
         DungeonBuilderProjectData.Bounds previous = this.structureBounds;
         this.structureBounds = new DungeonBuilderProjectData.Bounds(this.pending.first, pos);
         boolean invalidatedSnapshot = this.roomSnapshot != null && !this.structureBounds.equals(previous);
         if (invalidatedSnapshot) {
            this.roomSnapshot = null;
         }

         this.pending = null;
         BlockPos size = this.structureBounds.size();
         this.changed();
         return "Structure bounds saved: "
            + size.getX()
            + " x "
            + size.getY()
            + " x "
            + size.getZ()
            + "."
            + (invalidatedSnapshot ? " The previous room snapshot was invalidated." : "");
      }

      public String selectRegionCorner(String type, BlockPos pos) {
         String pendingKind = "region:" + type;
         if (this.isPending(pendingKind)) {
            this.rememberState();
            DungeonBuilderProjectData.Bounds bounds = new DungeonBuilderProjectData.Bounds(this.pending.first, pos);
            int replacedRooms = 0;
            if (type.equals("room")) {
               replacedRooms = (int)this.regions.stream().filter(region -> region.type.equals("room")).count();
               this.regions.removeIf(region -> region.type.equals("room"));
            }

            String id = DungeonBuilderProjectData.nextId(type, this.regions.stream().map(DungeonBuilderProjectData.Region::id).toList());
            String group = type.equals("trigger_region") ? this.activeEncounterGroup : "";
            if (type.equals("trigger_region")) {
               this.regions.removeIf(region -> region.type.equals("trigger_region") && region.group.equals(group));
            }

            this.regions.add(new DungeonBuilderProjectData.Region(id, type, bounds, group));
            if (type.equals("trigger_region")) {
               this.ensureEncounter(group, false);
               DungeonBuilderProjectData.Encounter encounter = this.encounters.get(group);
               this.encounters.put(group, new DungeonBuilderProjectData.Encounter(encounter.id, encounter.pool, encounter.minLevel, encounter.maxLevel, true));
            }

            this.pending = null;
            this.changed();
            return DungeonBuilderProjectData.display(type)
               + " "
               + id
               + " saved."
               + (type.equals("trigger_region") ? " This optional encounter will wait until a participant enters this area." : "")
               + (replacedRooms > 0 ? " Replaced " + replacedRooms + " previous Room Bounds selection" + (replacedRooms == 1 ? "." : "s.") : "");
         } else {
            boolean replacesExisting = type.equals("room")
               ? this.regions.stream().anyMatch(region -> region.type.equals("room"))
               : type.equals("trigger_region")
                  && this.regions.stream().anyMatch(region -> region.type.equals("trigger_region") && region.group.equals(this.activeEncounterGroup));
            if (this.regions.size() >= 64 && !replacesExisting) {
               return "This room already has the maximum of 64 regions.";
            }

            if (type.equals("trigger_region") && !this.encounters.containsKey(this.activeEncounterGroup) && this.encounters.size() >= 128) {
               return "This room already has the maximum of 128 encounters.";
            }

            this.pending = new DungeonBuilderProjectData.Pending(pendingKind, pos.immutable(), null);
            this.changed();
            return DungeonBuilderProjectData.display(type) + " corner 1 set to " + DungeonBuilderProjectData.shortPos(pos) + ". Select the opposite corner.";
         }
      }

      public String setOrigin(BlockPos pos) {
         this.rememberState();
         this.origin = pos.immutable();
         this.pending = null;
         this.changed();
         return "Structure origin set to " + DungeonBuilderProjectData.shortPos(pos) + ".";
      }

      public String selectSocketCorner(String type, boolean required, BlockPos pos, Direction facing) {
         String pendingKind = "socket:" + type + ":" + required;
         if (!this.isPending(pendingKind)) {
            if (this.sockets.size() >= 64) {
               return "This room already has the maximum of 64 sockets.";
            }

            this.pending = new DungeonBuilderProjectData.Pending(pendingKind, pos.immutable(), facing);
            this.changed();
            return DungeonBuilderProjectData.display(type) + " socket corner 1 set. Select the opposite corner on the same plane.";
         } else {
            Direction socketFacing = this.pending.facing == null ? facing : this.pending.facing;
            if (!samePlane(this.pending.first, pos, socketFacing)) {
               return "The second socket corner must be on the same " + socketFacing.getAxis().getName() + " plane.";
            }

            this.rememberState();
            DungeonBuilderProjectData.Bounds opening = new DungeonBuilderProjectData.Bounds(this.pending.first, pos);
            String id = DungeonBuilderProjectData.nextId(type + "_socket", this.sockets.stream().map(DungeonBuilderProjectData.Socket::id).toList());
            this.sockets.add(new DungeonBuilderProjectData.Socket(id, type, opening, socketFacing, required));
            this.pending = null;
            this.changed();
            return DungeonBuilderProjectData.display(type) + " socket " + id + " saved facing " + socketFacing.getName() + ".";
         }
      }

      public String addMarker(String type, BlockPos pos) {
         boolean replacesExisting = !type.equals("exit") && !type.equals("return_portal")
            ? isUniqueMarker(type) && this.markers.stream().anyMatch(marker -> marker.type.equals(type))
            : this.markers.stream().anyMatch(marker -> marker.type.equals("exit") || marker.type.equals("return_portal"));
         if (this.markers.size() >= 256 && !replacesExisting) {
            return "This room already has the maximum of 256 markers.";
         }

         if (isEncounterMarker(type) && !this.encounters.containsKey(this.activeEncounterGroup) && this.encounters.size() >= 128) {
            return "This room already has the maximum of 128 encounters.";
         }

         this.rememberState();
         if (type.equals("exit") || type.equals("return_portal")) {
            this.markers.removeIf(marker -> marker.type.equals("exit") || marker.type.equals("return_portal"));
         } else if (isUniqueMarker(type)) {
            this.markers.removeIf(marker -> marker.type.equals(type));
         }

         String id = DungeonBuilderProjectData.nextId(type, this.markers.stream().map(DungeonBuilderProjectData.Marker::id).toList());
         String group = "";
         if (isEncounterMarker(type)) {
            if (type.equals("boss_spawn") && this.activeEncounterGroup.equals("default")) {
               boolean defaultHasSpawn = this.markers.stream().anyMatch(marker -> isEncounterMarker(marker.type) && marker.group.equals("default"));
               if (!defaultHasSpawn) {
                  for (int index = 0; index < this.regions.size(); index++) {
                     DungeonBuilderProjectData.Region region = this.regions.get(index);
                     if (region.type.equals("trigger_region") && region.group.equals("default")) {
                        this.regions.set(index, new DungeonBuilderProjectData.Region(region.id, region.type, region.bounds, "boss"));
                     }
                  }

                  this.encounters.remove("default");
               }

               this.activeEncounterGroup = "boss";
            }

            group = this.activeEncounterGroup;
         }

         this.markers.add(new DungeonBuilderProjectData.Marker(id, type, pos.immutable(), group));
         if (isEncounterMarker(type)) {
            this.ensureEncounter(group, type.equals("boss_spawn"));
         }

         this.pending = null;
         this.changed();
         return DungeonBuilderProjectData.display(type)
            + " marker "
            + id
            + " set at "
            + DungeonBuilderProjectData.shortPos(pos)
            + (group.isBlank() ? "." : " in encounter group " + group + ".");
      }

      public String configureSpawnPoint(String markerId, DungeonBuilderProjectData.Project.SpawnRole role, String encounterId) {
         if (role == null) {
            return "Spawn role is required.";
         }

         String clean = DungeonBuilderProjectData.sanitizeId(encounterId, "default", 64);
         if (!clean.equals(encounterId)) {
            return "Encounter IDs must use lowercase letters, numbers, _, - or .";
         }

         int markerIndex = this.markerIndex(markerId);
         if (markerIndex < 0) {
            return "No marker named " + markerId + " exists in this room.";
         }

         DungeonBuilderProjectData.Marker marker = this.markers.get(markerIndex);
         if (!isAssignableSpawnMarker(marker.type)) {
            return "Marker " + markerId + " is not an encounter spawn point.";
         }

         if (!this.encounters.containsKey(clean) && this.encounters.size() >= 128) {
            return "This room already has the maximum of 128 encounters.";
         }

         this.rememberState();
         this.markers.set(markerIndex, new DungeonBuilderProjectData.Marker(marker.id, role.markerType, marker.position, clean));
         this.ensureEncounter(clean, role == DungeonBuilderProjectData.Project.SpawnRole.BOSS);
         this.activeEncounterGroup = clean;
         this.pending = null;
         this.changed();
         return "Assigned " + markerId + " as a " + role.name().toLowerCase(Locale.ROOT) + " spawn in encounter " + clean + ".";
      }

      public String clearSpawnPointAssignment(String markerId) {
         int markerIndex = this.markerIndex(markerId);
         if (markerIndex < 0) {
            return "No marker named " + markerId + " exists in this room.";
         }

         DungeonBuilderProjectData.Marker marker = this.markers.get(markerIndex);
         if (!isAssignableSpawnMarker(marker.type)) {
            return "Marker " + markerId + " is not an encounter spawn point.";
         }

         if (marker.type.equals("spawn_point") && marker.group.isBlank()) {
            return "Spawn point " + markerId + " is already unassigned.";
         }

         this.rememberState();
         this.markers.set(markerIndex, new DungeonBuilderProjectData.Marker(marker.id, "spawn_point", marker.position, ""));
         this.pending = null;
         this.changed();
         return "Spawn point " + markerId + " is now unassigned.";
      }

      public String configureSocket(String socketId, String type, boolean required) {
         if (!type.equals("corridor") && !type.equals("stair")) {
            return "Socket type must be corridor or stair.";
         }

         int socketIndex = this.socketIndex(socketId);
         if (socketIndex < 0) {
            return "No socket named " + socketId + " exists in this room.";
         }

         DungeonBuilderProjectData.Socket socket = this.sockets.get(socketIndex);
         if (type.equals("corridor") && socket.facing.getAxis().isVertical()) {
            return "A vertical socket cannot be configured as a corridor.";
         }

         if (type.equals("stair") && socket.facing.getAxis().isHorizontal()) {
            return "A horizontal socket cannot be configured as a stair.";
         }

         long requiredWithoutTarget = this.sockets
            .stream()
            .filter(DungeonBuilderProjectData.Socket::required)
            .filter(existing -> !existing.id.equals(socketId))
            .count();
         if (required && requiredWithoutTarget >= 2L) {
            return "Schema v2 supports at most two required sockets per room.";
         }

         this.rememberState();
         this.sockets.set(socketIndex, new DungeonBuilderProjectData.Socket(socket.id, type, socket.opening, socket.facing, required));
         this.pending = null;
         this.changed();
         return "Socket " + socketId + " is now " + (required ? "required " : "optional ") + type + ".";
      }

      public String renameEncounter(String oldId, String newId) {
         String clean = DungeonBuilderProjectData.sanitizeId(newId, "default", 64);
         if (!clean.equals(newId)) {
            return "Encounter IDs must use lowercase letters, numbers, _, - or .";
         }

         DungeonBuilderProjectData.Encounter encounter = this.encounters.get(oldId);
         if (encounter == null) {
            return "No encounter named " + oldId + " exists.";
         }

         if (oldId.equals(newId)) {
            return "Encounter " + oldId + " already has that name.";
         }

         if (this.encounters.containsKey(newId)) {
            return "Encounter " + newId + " already exists.";
         }

         this.rememberState();
         LinkedHashMap<String, DungeonBuilderProjectData.Encounter> renamed = new LinkedHashMap<>();

         for (DungeonBuilderProjectData.Encounter value : this.encounters.values()) {
            renamed.put(
               value.id.equals(oldId) ? newId : value.id,
               value.id.equals(oldId) ? new DungeonBuilderProjectData.Encounter(newId, value.pool, value.minLevel, value.maxLevel, value.delayed) : value
            );
         }

         this.encounters.clear();
         this.encounters.putAll(renamed);

         for (int index = 0; index < this.markers.size(); index++) {
            DungeonBuilderProjectData.Marker marker = this.markers.get(index);
            if (marker.group.equals(oldId)) {
               this.markers.set(index, new DungeonBuilderProjectData.Marker(marker.id, marker.type, marker.position, newId));
            }
         }

         for (int index = 0; index < this.regions.size(); index++) {
            DungeonBuilderProjectData.Region region = this.regions.get(index);
            if (region.group.equals(oldId)) {
               this.regions.set(index, new DungeonBuilderProjectData.Region(region.id, region.type, region.bounds, newId));
            }
         }

         if (this.activeEncounterGroup.equals(oldId)) {
            this.activeEncounterGroup = newId;
         }

         this.pending = null;
         this.changed();
         return "Renamed encounter " + oldId + " to " + newId + ".";
      }

      public String deleteEncounter(String id) {
         if (!this.encounters.containsKey(id)) {
            return "No encounter named " + id + " exists.";
         }

         this.rememberState();
         this.encounters.remove(id);

         for (int index = 0; index < this.markers.size(); index++) {
            DungeonBuilderProjectData.Marker marker = this.markers.get(index);
            if (marker.group.equals(id) && isAssignableSpawnMarker(marker.type)) {
               this.markers.set(index, new DungeonBuilderProjectData.Marker(marker.id, "spawn_point", marker.position, ""));
            }
         }

         this.regions.removeIf(region -> region.type.equals("trigger_region") && region.group.equals(id));
         if (this.activeEncounterGroup.equals(id)) {
            this.activeEncounterGroup = "default";
         }

         this.pending = null;
         this.changed();
         return "Deleted encounter " + id + "; its spawn positions are now unassigned.";
      }

      public void cancelPending() {
         if (this.pending != null) {
            this.pending = null;
            this.changed();
         }
      }

      public String undoLast() {
         if (this.pending != null) {
            this.pending = null;
            this.changed();
            return "Canceled the unfinished two-point selection.";
         }

         DungeonBuilderProjectData.Project.ProjectState state = this.undoHistory.pollLast();
         if (state == null) {
            return "Nothing to undo in this play session.";
         }

         this.structureBounds = state.structureBounds;
         this.origin = state.origin;
         this.pending = state.pending;
         this.regions.clear();
         this.regions.addAll(state.regions);
         this.markers.clear();
         this.markers.addAll(state.markers);
         this.sockets.clear();
         this.sockets.addAll(state.sockets);
         this.roomRole = state.roomRole;
         this.activeEncounterGroup = state.activeEncounterGroup;
         this.defaultMobPool = state.defaultMobPool;
         this.bossMobPool = state.bossMobPool;
         this.allowedRanks = EnumSet.copyOf(state.allowedRanks);
         this.roomWeight = state.roomWeight;
         this.shellBlock = state.shellBlock;
         this.shellThickness = state.shellThickness;
         this.roomSnapshot = state.roomSnapshot;
         this.encounters.clear();
         state.encounters.forEach(encounter -> this.encounters.put(encounter.id(), encounter));
         this.changed();
         return "Undid the most recent authoring action.";
      }

      public String eraseNearest(BlockPos pos) {
         DungeonBuilderProjectData.Project.EraseTarget nearest = null;

         for (int index = 0; index < this.markers.size(); index++) {
            DungeonBuilderProjectData.Marker marker = this.markers.get(index);
            nearest = nearer(nearest, new DungeonBuilderProjectData.Project.EraseTarget("marker", index, distanceSquared(pos, marker.position), marker.id));
         }

         for (int index = 0; index < this.sockets.size(); index++) {
            DungeonBuilderProjectData.Socket socket = this.sockets.get(index);
            nearest = nearer(nearest, new DungeonBuilderProjectData.Project.EraseTarget("socket", index, distanceSquared(pos, socket.opening), socket.id));
         }

         for (int index = 0; index < this.regions.size(); index++) {
            DungeonBuilderProjectData.Region region = this.regions.get(index);
            nearest = nearer(nearest, new DungeonBuilderProjectData.Project.EraseTarget("region", index, distanceSquared(pos, region.bounds), region.id));
         }

         if (nearest != null && !(nearest.distance > 64.0)) {
            this.rememberState();
            switch (nearest.collection) {
               case "marker":
                  this.markers.remove(nearest.index);
                  break;
               case "socket":
                  this.sockets.remove(nearest.index);
                  break;
               case "region":
                  DungeonBuilderProjectData.Region removed = this.regions.remove(nearest.index);
                  if (removed.type.equals("trigger_region")
                     && this.regions.stream().noneMatch(regionx -> regionx.type.equals("trigger_region") && regionx.group.equals(removed.group))) {
                     DungeonBuilderProjectData.Encounter encounter = this.encounters.get(removed.group);
                     if (encounter != null) {
                        this.encounters
                           .put(
                              removed.group,
                              new DungeonBuilderProjectData.Encounter(encounter.id, encounter.pool, encounter.minLevel, encounter.maxLevel, false)
                           );
                     }
                  }
                  break;
               default:
                  throw new IllegalStateException("Unknown erase target " + nearest.collection);
            }

            this.pending = null;
            this.changed();
            return "Removed " + nearest.collection + " " + nearest.id + ".";
         } else {
            return "No marker, socket, or region was found within 8 blocks.";
         }
      }

      public List<DungeonBuilderProjectData.Issue> validate() {
         List<DungeonBuilderProjectData.Issue> issues = new ArrayList<>();
         if (this.structureBounds == null) {
            issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Select the structure bounds with the Surveyor Wand."));
            return issues;
         }

         BlockPos size = this.structureBounds.size();
         if (size.getX() > 48 || size.getY() > 48 || size.getZ() > 48 || this.structureBounds.volume() > 110592L) {
            issues.add(
               new DungeonBuilderProjectData.Issue(
                  DungeonBuilderProjectData.Severity.ERROR,
                  "Structure is too large for safe single-tick capture. Keep each export within 48 blocks per axis (48 x 48 x 48 maximum)."
               )
            );
         }

         if (this.origin != null && !this.structureBounds.contains(this.origin)) {
            issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "The structure origin is outside the selected bounds."));
         }

         if (this.roomSnapshot != null) {
            String snapshotProblem = DungeonBuilderProjectData.roomSnapshotProblem(this.roomSnapshot, this.structureBounds);
            if (snapshotProblem != null) {
               issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, snapshotProblem));
            }
         }

         for (DungeonBuilderProjectData.Region region : this.regions) {
            if (!this.structureBounds.contains(region.bounds.min) || !this.structureBounds.contains(region.bounds.max)) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR, "Region " + region.id + " extends outside the structure bounds."
                  )
               );
            }
         }

         for (DungeonBuilderProjectData.Marker marker : this.markers) {
            if (!this.structureBounds.contains(marker.position)) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Marker " + marker.id + " is outside the structure bounds.")
               );
            }
         }

         for (DungeonBuilderProjectData.Socket socket : this.sockets) {
            if (!this.structureBounds.contains(socket.opening.min) || !this.structureBounds.contains(socket.opening.max)) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR, "Socket " + socket.id + " extends outside the structure bounds."
                  )
               );
            } else if (!isOnBoundaryOrOneInside(socket, this.structureBounds)) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR, "Socket " + socket.id + " must be on the indicated wall or exactly one block inside it."
                  )
               );
            } else if (socket.type.equals("corridor") && socket.facing.getAxis().isVertical()) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Corridor socket " + socket.id + " must face horizontally.")
               );
            } else if (socket.type.equals("stair") && socket.facing.getAxis().isHorizontal()) {
               issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Stair socket " + socket.id + " must face up or down."));
            }
         }

         if (this.sockets.stream().filter(DungeonBuilderProjectData.Socket::required).count() > 2L) {
            issues.add(
               new DungeonBuilderProjectData.Issue(
                  DungeonBuilderProjectData.Severity.ERROR, "Schema v2 supports at most two required sockets per room. Leave extra junction branches optional."
               )
            );
         }

         if (this.kind == DungeonBuilderProjectData.ProjectKind.PRESET) {
            if (this.regions.stream().noneMatch(region -> region.type.equals("room"))) {
               issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Preset dungeons require one Room Bounds volume."));
            }

            if (!this.hasMarker("player_start")) {
               issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Preset dungeons require one Player Start marker."));
            }

            if (!this.hasMarker("exit") && !this.hasMarker("return_portal")) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Preset dungeons require an Exit or Return Portal marker.")
               );
            }

            if (!this.hasMarker("boss_spawn")) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR, "Preset dungeons require one Boss Spawn marker so the run can complete."
                  )
               );
            }
         } else {
            long roomRegionCount = this.regions.stream().filter(region -> region.type.equals("room")).count();
            if (roomRegionCount == 0L) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR, "This module needs one Room Bounds volume. One module project represents exactly one room."
                  )
               );
            } else if (roomRegionCount > 1L) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR,
                     "This module contains "
                        + roomRegionCount
                        + " Room Bounds volumes. One module project represents exactly one room; select Room Bounds once more to replace the old selections."
                  )
               );
            }

            if (this.roomRole == DungeonBuilderProjectData.RoomRole.CAP) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR,
                     "CAP modules are reserved for a future schema version. Leave unused sockets as solid authored walls instead."
                  )
               );
            }

            if (this.sockets.isEmpty()) {
               issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Procedural modules require at least one socket."));
            }

            if ((
                  this.roomRole == DungeonBuilderProjectData.RoomRole.START
                     || this.roomRole == DungeonBuilderProjectData.RoomRole.BOSS
                     || this.roomRole == DungeonBuilderProjectData.RoomRole.CAP
               )
               && this.sockets.size() != 1) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR,
                     DungeonBuilderProjectData.display(this.roomRole.name()) + " modules must have exactly one socket so they remain terminal rooms."
                  )
               );
            }

            if ((this.roomRole == DungeonBuilderProjectData.RoomRole.NORMAL || this.roomRole == DungeonBuilderProjectData.RoomRole.CORRIDOR)
               && this.sockets.size() < 2) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR,
                     DungeonBuilderProjectData.display(this.roomRole.name()) + " modules need at least two sockets: one entrance and one exit."
                  )
               );
            }

            if (this.roomRole == DungeonBuilderProjectData.RoomRole.NORMAL && this.sockets.size() > 2) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.WARNING,
                     "A normal module usually has two sockets. Use the junction role when this room is intended to branch."
                  )
               );
            }

            if (this.roomRole == DungeonBuilderProjectData.RoomRole.START && !this.hasMarker("player_start")) {
               issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Start modules require a Player Start marker."));
            }

            if (this.roomRole == DungeonBuilderProjectData.RoomRole.START && !this.hasMarker("exit") && !this.hasMarker("return_portal")) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR, "Start modules require an Exit or Return Portal marker for safe dungeon return."
                  )
               );
            }

            if (this.roomRole == DungeonBuilderProjectData.RoomRole.BOSS && !this.hasMarker("boss_spawn")) {
               issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.ERROR, "Boss modules require a Boss Spawn marker."));
            }
         }

         for (DungeonBuilderProjectData.Marker marker : this.markers) {
            if (isEncounterMarker(marker.type) && !this.encounters.containsKey(marker.group)) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR, "Marker " + marker.id + " refers to missing encounter group " + marker.group + "."
                  )
               );
            }
         }

         for (DungeonBuilderProjectData.Region region : this.regions) {
            if (region.type.equals("trigger_region") && !this.encounters.containsKey(region.group)) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR, "Trigger " + region.id + " refers to missing encounter group " + region.group + "."
                  )
               );
            }
         }

         for (DungeonBuilderProjectData.Encounter encounter : this.encounters.values()) {
            boolean hasSpawn = this.markers.stream().anyMatch(marker -> isEncounterMarker(marker.type) && marker.group.equals(encounter.id));
            if (!hasSpawn) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.WARNING, "Encounter group " + encounter.id + " has no assigned spawn points and will not be exported."
                  )
               );
            }

            if (encounter.minLevel < 1 || encounter.maxLevel < encounter.minLevel || encounter.maxLevel != Integer.MAX_VALUE && encounter.maxLevel > 1000) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR,
                     "Encounter group " + encounter.id + " must use levels 1-1000, or the automatic dungeon-level setting."
                  )
               );
            }

            if (encounter.delayed && this.regions.stream().noneMatch(region -> region.type.equals("trigger_region") && region.group.equals(encounter.id))) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR,
                     "Delayed encounter group " + encounter.id + " has no Trigger Region. Place one or erase/recreate the encounter as automatic."
                  )
               );
            }

            boolean hasBoss = this.markers.stream().anyMatch(marker -> marker.group.equals(encounter.id) && marker.type.equals("boss_spawn"));
            boolean hasNonBoss = this.markers
               .stream()
               .anyMatch(marker -> marker.group.equals(encounter.id) && (marker.type.equals("mob_spawn") || marker.type.equals("elite_spawn")));
            if (hasBoss && hasNonBoss) {
               issues.add(
                  new DungeonBuilderProjectData.Issue(
                     DungeonBuilderProjectData.Severity.ERROR,
                     "Encounter group " + encounter.id + " mixes boss and normal spawn points. Assign them to separate encounters."
                  )
               );
            }
         }

         long unassignedSpawns = this.markers.stream().filter(marker -> marker.type.equals("spawn_point")).count();
         if (unassignedSpawns > 0L) {
            issues.add(
               new DungeonBuilderProjectData.Issue(
                  DungeonBuilderProjectData.Severity.WARNING,
                  unassignedSpawns
                     + " spawn point"
                     + (unassignedSpawns == 1L ? " is" : "s are")
                     + " not configured yet. Assign an encounter in the Dungeon Builder GUI; unassigned points are not exported."
               )
            );
         }

         if (!this.hasMarker("mob_spawn") && !this.hasMarker("elite_spawn") && !this.hasMarker("boss_spawn") && unassignedSpawns == 0L) {
            issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.WARNING, "This structure has no encounter spawn markers."));
         }

         if (this.pending != null) {
            issues.add(new DungeonBuilderProjectData.Issue(DungeonBuilderProjectData.Severity.WARNING, "An unfinished two-point selection is still active."));
         }

         return issues;
      }

      public long errorCount() {
         return this.validate().stream().filter(issue -> issue.severity == DungeonBuilderProjectData.Severity.ERROR).count();
      }

      public String summary() {
         String boundsText = this.structureBounds == null
            ? "not selected"
            : DungeonBuilderProjectData.shortPos(this.structureBounds.min) + " to " + DungeonBuilderProjectData.shortPos(this.structureBounds.max);
         String roleText = this.kind == DungeonBuilderProjectData.ProjectKind.MODULE ? "/" + this.roomRole.name().toLowerCase(Locale.ROOT) : "";
         return this.namespace
            + ":"
            + this.name
            + " ["
            + this.kind.name().toLowerCase(Locale.ROOT)
            + roleText
            + "] | ranks "
            + DungeonBuilderProjectData.rankText(this.allowedRanks)
            + " | bounds "
            + boundsText
            + " | group "
            + this.activeEncounterGroup
            + " | "
            + this.regions.size()
            + " regions | "
            + this.sockets.size()
            + " sockets | "
            + this.markers.size()
            + " markers";
      }

      private boolean hasMarker(String type) {
         return this.markers.stream().anyMatch(marker -> marker.type.equals(type));
      }

      private void rememberState() {
         this.undoHistory
            .addLast(
               new DungeonBuilderProjectData.Project.ProjectState(
                  this.structureBounds,
                  this.origin,
                  this.pending,
                  List.copyOf(this.regions),
                  List.copyOf(this.markers),
                  List.copyOf(this.sockets),
                  this.roomRole,
                  this.activeEncounterGroup,
                  this.defaultMobPool,
                  this.bossMobPool,
                  Set.copyOf(this.allowedRanks),
                  this.roomWeight,
                  this.shellBlock,
                  this.shellThickness,
                  this.roomSnapshot,
                  List.copyOf(this.encounters.values())
               )
            );

         while (this.undoHistory.size() > 32) {
            this.undoHistory.removeFirst();
         }
      }

      private boolean isPending(String kind) {
         return this.pending != null && this.pending.kind.equals(kind);
      }

      private static boolean isUniqueMarker(String type) {
         return type.equals("player_start") || type.equals("return_portal") || type.equals("boss_spawn");
      }

      private static boolean isEncounterMarker(String type) {
         return type.equals("mob_spawn") || type.equals("elite_spawn") || type.equals("boss_spawn");
      }

      private static boolean isAssignableSpawnMarker(String type) {
         return type.equals("spawn_point") || isEncounterMarker(type);
      }

      private int markerIndex(String id) {
         for (int index = 0; index < this.markers.size(); index++) {
            if (this.markers.get(index).id.equals(id)) {
               return index;
            }
         }

         return -1;
      }

      private int socketIndex(String id) {
         for (int index = 0; index < this.sockets.size(); index++) {
            if (this.sockets.get(index).id.equals(id)) {
               return index;
            }
         }

         return -1;
      }

      private void ensureEncounter(String group, boolean boss) {
         this.encounters
            .computeIfAbsent(
               group, id -> new DungeonBuilderProjectData.Encounter(id, boss ? this.bossMobPool : this.defaultMobPool, 1, Integer.MAX_VALUE, false)
            );
      }

      private static boolean samePlane(BlockPos first, BlockPos second, Direction facing) {
         return switch (facing.getAxis()) {
            case X -> first.getX() == second.getX();
            case Y -> first.getY() == second.getY();
            case Z -> first.getZ() == second.getZ();
         };
      }

      private static boolean isOnBoundaryOrOneInside(DungeonBuilderProjectData.Socket socket, DungeonBuilderProjectData.Bounds structureBounds) {
         return switch (socket.facing) {
            case WEST -> socket.opening.min.getX() == structureBounds.min.getX() || socket.opening.min.getX() == structureBounds.min.getX() + 1;
            case EAST -> socket.opening.max.getX() == structureBounds.max.getX() || socket.opening.max.getX() == structureBounds.max.getX() - 1;
            case DOWN -> socket.opening.min.getY() == structureBounds.min.getY() || socket.opening.min.getY() == structureBounds.min.getY() + 1;
            case UP -> socket.opening.max.getY() == structureBounds.max.getY() || socket.opening.max.getY() == structureBounds.max.getY() - 1;
            case NORTH -> socket.opening.min.getZ() == structureBounds.min.getZ() || socket.opening.min.getZ() == structureBounds.min.getZ() + 1;
            case SOUTH -> socket.opening.max.getZ() == structureBounds.max.getZ() || socket.opening.max.getZ() == structureBounds.max.getZ() - 1;
         };
      }

      private static DungeonBuilderProjectData.Project.EraseTarget nearer(
         @Nullable DungeonBuilderProjectData.Project.EraseTarget current, DungeonBuilderProjectData.Project.EraseTarget candidate
      ) {
         return current != null && !(eraseScore(candidate) < eraseScore(current)) ? current : candidate;
      }

      private static double eraseScore(DungeonBuilderProjectData.Project.EraseTarget target) {
         int priority = switch (target.collection) {
            case "marker" -> 0;
            case "socket" -> 1;
            default -> 2;
         };
         return target.distance + priority * 16.0;
      }

      private static double distanceSquared(BlockPos first, BlockPos second) {
         long dx = (long)first.getX() - second.getX();
         long dy = (long)first.getY() - second.getY();
         long dz = (long)first.getZ() - second.getZ();
         return (double)dx * dx + (double)dy * dy + (double)dz * dz;
      }

      private static double distanceSquared(BlockPos pos, DungeonBuilderProjectData.Bounds bounds) {
         long dx = Math.max(Math.max((long)bounds.min.getX() - pos.getX(), 0L), (long)pos.getX() - bounds.max.getX());
         long dy = Math.max(Math.max((long)bounds.min.getY() - pos.getY(), 0L), (long)pos.getY() - bounds.max.getY());
         long dz = Math.max(Math.max((long)bounds.min.getZ() - pos.getZ(), 0L), (long)pos.getZ() - bounds.max.getZ());
         return (double)dx * dx + (double)dy * dy + (double)dz * dz;
      }

      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putString("Namespace", this.namespace);
         tag.putString("Name", this.name);
         tag.putString("Kind", this.kind.name());
         tag.putString("RoomRole", this.roomRole.name());
         tag.putString("ActiveEncounterGroup", this.activeEncounterGroup);
         tag.putString("DefaultMobPool", this.defaultMobPool);
         tag.putString("BossMobPool", this.bossMobPool);
         tag.putString("AllowedRanks", this.allowedRanks.stream().map(Enum::name).collect(Collectors.joining(",")));
         tag.putInt("RoomWeight", this.roomWeight);
         tag.putString("ShellBlock", this.shellBlock);
         tag.putInt("ShellThickness", this.shellThickness);
         if (this.structureBounds != null) {
            tag.put("StructureBounds", this.structureBounds.save());
         }

         if (this.origin != null) {
            tag.put("Origin", NbtUtils.writeBlockPos(this.origin));
         }

         if (this.pending != null) {
            tag.put("Pending", this.pending.save());
         }

         if (this.roomSnapshot != null) {
            tag.put("RoomSnapshot", DungeonBuilderProjectData.saveRoomSnapshot(this.roomSnapshot));
         }

         tag.put("Regions", DungeonBuilderProjectData.saveList(this.regions.stream().map(DungeonBuilderProjectData.Region::save).toList()));
         tag.put("Markers", DungeonBuilderProjectData.saveList(this.markers.stream().map(DungeonBuilderProjectData.Marker::save).toList()));
         tag.put("Sockets", DungeonBuilderProjectData.saveList(this.sockets.stream().map(DungeonBuilderProjectData.Socket::save).toList()));
         tag.put("Encounters", DungeonBuilderProjectData.saveList(this.encounters.values().stream().map(DungeonBuilderProjectData.Encounter::save).toList()));
         return tag;
      }

      private static DungeonBuilderProjectData.Project load(CompoundTag tag) {
         DungeonBuilderProjectData.Project project = new DungeonBuilderProjectData.Project(
            DungeonBuilderProjectData.sanitizeId(tag.getString("Namespace"), "builder", 32),
            DungeonBuilderProjectData.sanitizeId(tag.getString("Name"), "my_dungeon", 48),
            DungeonBuilderProjectData.ProjectKind.parse(tag.getString("Kind"))
         );
         project.roomRole = DungeonBuilderProjectData.RoomRole.parse(tag.getString("RoomRole"));
         project.activeEncounterGroup = tag.contains("ActiveEncounterGroup", 8)
            ? DungeonBuilderProjectData.sanitizeId(tag.getString("ActiveEncounterGroup"), "default", 64)
            : "default";
         if (tag.contains("DefaultMobPool", 8) && DungeonBuilderProjectData.isResourceId(tag.getString("DefaultMobPool"))) {
            project.defaultMobPool = tag.getString("DefaultMobPool");
         }

         if (tag.contains("BossMobPool", 8) && DungeonBuilderProjectData.isResourceId(tag.getString("BossMobPool"))) {
            project.bossMobPool = tag.getString("BossMobPool");
         }

         if (tag.contains("AllowedRanks", 8)) {
            EnumSet<ProceduralDungeonRank> loadedRanks = EnumSet.noneOf(ProceduralDungeonRank.class);

            for (String value : tag.getString("AllowedRanks").split(",")) {
               ProceduralDungeonRank.tryParse(value.trim()).ifPresent(loadedRanks::add);
            }

            if (!loadedRanks.isEmpty()) {
               project.allowedRanks = loadedRanks;
            }
         }

         project.roomWeight = tag.contains("RoomWeight", 3) ? Math.max(1, tag.getInt("RoomWeight")) : 1;
         if (tag.contains("ShellBlock", 8) && DungeonBuilderProjectData.isResourceId(tag.getString("ShellBlock"))) {
            project.shellBlock = tag.getString("ShellBlock");
         }

         project.shellThickness = tag.contains("ShellThickness", 3) ? Math.max(0, Math.min(4, tag.getInt("ShellThickness"))) : 1;
         if (tag.contains("StructureBounds", 10)) {
            project.structureBounds = DungeonBuilderProjectData.Bounds.load(tag.getCompound("StructureBounds"));
         }

         if (tag.contains("Origin", 10)) {
            project.origin = NbtUtils.readBlockPos(tag.getCompound("Origin"));
         }

         if (tag.contains("Pending", 10)) {
            project.pending = DungeonBuilderProjectData.Pending.load(tag.getCompound("Pending"));
         }

         DungeonBuilderProjectData.readList(tag, "Regions").forEach(element -> project.regions.add(DungeonBuilderProjectData.Region.load(element)));
         DungeonBuilderProjectData.readList(tag, "Markers").forEach(element -> {
            DungeonBuilderProjectData.Marker marker = DungeonBuilderProjectData.Marker.load(element);
            project.markers.add(marker.type.equals("spawn_point") ? new DungeonBuilderProjectData.Marker(marker.id, marker.type, marker.position, "") : marker);
         });
         DungeonBuilderProjectData.readList(tag, "Sockets").forEach(element -> project.sockets.add(DungeonBuilderProjectData.Socket.load(element)));
         DungeonBuilderProjectData.readList(tag, "Encounters").forEach(element -> {
            DungeonBuilderProjectData.Encounter encounter = DungeonBuilderProjectData.Encounter.load(element);
            project.encounters.put(encounter.id(), encounter);
         });
         if (tag.contains("RoomSnapshot", 10)) {
            try {
               RoomSnapshot snapshot = DungeonBuilderProjectData.loadRoomSnapshot(tag.getCompound("RoomSnapshot"));
               if (DungeonBuilderProjectData.roomSnapshotProblem(snapshot, project.structureBounds) == null) {
                  project.roomSnapshot = snapshot;
               }
            } catch (RuntimeException var7) {
            }
         }

         boolean legacyDefaultBoss = project.markers.stream().anyMatch(marker -> marker.type.equals("boss_spawn") && marker.group.equals("default"));
         boolean legacyDefaultNormal = project.markers
            .stream()
            .anyMatch(marker -> (marker.type.equals("mob_spawn") || marker.type.equals("elite_spawn")) && marker.group.equals("default"));
         if (legacyDefaultBoss) {
            for (int index = 0; index < project.markers.size(); index++) {
               DungeonBuilderProjectData.Marker marker = project.markers.get(index);
               if (marker.type.equals("boss_spawn") && marker.group.equals("default")) {
                  project.markers.set(index, new DungeonBuilderProjectData.Marker(marker.id, marker.type, marker.position, "boss"));
               }
            }

            if (!legacyDefaultNormal) {
               for (int index = 0; index < project.regions.size(); index++) {
                  DungeonBuilderProjectData.Region region = project.regions.get(index);
                  if (region.type.equals("trigger_region") && region.group.equals("default")) {
                     project.regions.set(index, new DungeonBuilderProjectData.Region(region.id, region.type, region.bounds, "boss"));
                  }
               }

               project.encounters.remove("default");
            }
         }

         for (DungeonBuilderProjectData.Marker marker : project.markers) {
            if (isEncounterMarker(marker.type)) {
               project.ensureEncounter(marker.group, marker.type.equals("boss_spawn"));
            }
         }

         for (DungeonBuilderProjectData.Region region : project.regions) {
            if (region.type.equals("trigger_region")) {
               project.ensureEncounter(region.group, false);
            }
         }

         return project;
      }

      private record EraseTarget(String collection, int index, double distance, String id) {
      }

      private record ProjectState(
         @Nullable DungeonBuilderProjectData.Bounds structureBounds,
         @Nullable BlockPos origin,
         @Nullable DungeonBuilderProjectData.Pending pending,
         List<DungeonBuilderProjectData.Region> regions,
         List<DungeonBuilderProjectData.Marker> markers,
         List<DungeonBuilderProjectData.Socket> sockets,
         DungeonBuilderProjectData.RoomRole roomRole,
         String activeEncounterGroup,
         String defaultMobPool,
         String bossMobPool,
         Set<ProceduralDungeonRank> allowedRanks,
         int roomWeight,
         String shellBlock,
         int shellThickness,
         @Nullable RoomSnapshot roomSnapshot,
         List<DungeonBuilderProjectData.Encounter> encounters
      ) {
      }

      public enum SpawnRole {
         NORMAL("mob_spawn"),
         ELITE("elite_spawn"),
         BOSS("boss_spawn");

         private final String markerType;

         SpawnRole(String markerType) {
            this.markerType = markerType;
         }

         public String markerType() {
            return this.markerType;
         }
      }
   }

   public enum ProjectKind {
      PRESET,
      MODULE;

      public static DungeonBuilderProjectData.ProjectKind parse(String value) {
         return "module".equalsIgnoreCase(value) ? MODULE : PRESET;
      }
   }

   public record ProjectResult(boolean success, String message, @Nullable DungeonBuilderProjectData.Project project) {
      private static DungeonBuilderProjectData.ProjectResult success(@Nullable DungeonBuilderProjectData.Project project, String message) {
         return new DungeonBuilderProjectData.ProjectResult(true, message, project);
      }

      private static DungeonBuilderProjectData.ProjectResult failure(String message) {
         return new DungeonBuilderProjectData.ProjectResult(false, message, null);
      }
   }

   public record Region(String id, String type, DungeonBuilderProjectData.Bounds bounds, String group) {
      private CompoundTag save() {
         CompoundTag tag = this.bounds.save();
         tag.putString("Id", this.id);
         tag.putString("Type", this.type);
         tag.putString("Group", this.group);
         return tag;
      }

      private static DungeonBuilderProjectData.Region load(CompoundTag tag) {
         String group = tag.contains("Group", 8) ? tag.getString("Group") : "default";
         return new DungeonBuilderProjectData.Region(tag.getString("Id"), tag.getString("Type"), DungeonBuilderProjectData.Bounds.load(tag), group);
      }
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

      public static DungeonBuilderProjectData.RoomRole parse(String value) {
         if (value != null) {
            try {
               return valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException var2) {
            }
         }

         return NORMAL;
      }
   }

   public enum Severity {
      ERROR,
      WARNING;
   }

   public record Socket(String id, String type, DungeonBuilderProjectData.Bounds opening, Direction facing, boolean required) {
      private CompoundTag save() {
         CompoundTag tag = this.opening.save();
         tag.putString("Id", this.id);
         tag.putString("Type", this.type);
         tag.putString("Facing", this.facing.getName());
         tag.putBoolean("Required", this.required);
         return tag;
      }

      private static DungeonBuilderProjectData.Socket load(CompoundTag tag) {
         Direction facing = Direction.byName(tag.getString("Facing"));
         String type = tag.getString("Type");
         if (type.equals("stair_up") || type.equals("stair_down")) {
            type = "stair";
         }

         return new DungeonBuilderProjectData.Socket(
            tag.getString("Id"), type, DungeonBuilderProjectData.Bounds.load(tag), facing == null ? Direction.NORTH : facing, tag.getBoolean("Required")
         );
      }
   }

   private static final class Workspace {
      private final Map<String, DungeonBuilderProjectData.Project> projects = new LinkedHashMap<>();
      private final Map<ResourceLocation, BuilderMobPool> mobPools = new LinkedHashMap<>();
      private final Map<ResourceLocation, DungeonDraft> dungeonDrafts = new LinkedHashMap<>();
      private String activeId = "";
      private long revision;

      @Nullable
      private DungeonBuilderProjectData.Project active() {
         return this.projects.get(this.activeId);
      }

      private void addAndSelect(DungeonBuilderProjectData.Project project) {
         this.activeId = project.id();
         this.projects.put(this.activeId, project);
      }

      private CompoundTag save() {
         CompoundTag tag = new CompoundTag();
         tag.putString("Active", this.activeId);
         tag.putLong("Revision", this.revision);
         ListTag projectList = new ListTag();
         this.projects
            .values()
            .stream()
            .sorted(Comparator.comparing(DungeonBuilderProjectData.Project::id))
            .forEach(project -> projectList.add(project.save()));
         tag.put("Projects", projectList);
         tag.put(
            "MobPools",
            DungeonBuilderProjectData.saveList(
               this.mobPools.values().stream().sorted(Comparator.comparing(pool -> pool.id().toString())).map(DungeonBuilderProjectData::saveMobPool).toList()
            )
         );
         tag.put(
            "DungeonDrafts",
            DungeonBuilderProjectData.saveList(
               this.dungeonDrafts
                  .values()
                  .stream()
                  .sorted(Comparator.comparing(draft -> draft.id().toString()))
                  .map(DungeonBuilderProjectData::saveDungeonDraft)
                  .toList()
            )
         );
         return tag;
      }

      private static DungeonBuilderProjectData.Workspace load(CompoundTag tag) {
         DungeonBuilderProjectData.Workspace workspace = new DungeonBuilderProjectData.Workspace();
         workspace.revision = tag.contains("Revision", 4) ? Math.max(0L, tag.getLong("Revision")) : 0L;
         ListTag projectList = tag.getList("Projects", 10);

         for (int i = 0; i < projectList.size() && workspace.projects.size() < 128; i++) {
            DungeonBuilderProjectData.Project project = DungeonBuilderProjectData.Project.load(projectList.getCompound(i));
            workspace.projects.put(project.id(), project);
         }

         String savedActive = tag.getString("Active");
         workspace.activeId = workspace.projects.containsKey(savedActive) ? savedActive : workspace.projects.keySet().stream().findFirst().orElse("");
         if (tag.contains("MobPools", 9)) {
            int totalPoolEntries = 0;

            for (CompoundTag poolTag : DungeonBuilderProjectData.readList(tag, "MobPools")) {
               try {
                  BuilderMobPool pool = DungeonBuilderProjectData.loadMobPool(poolTag);
                  if (workspace.mobPools.size() < 128
                     && !workspace.mobPools.containsKey(pool.id())
                     && DungeonBuilderProjectData.mobPoolProblem(pool) == null
                     && totalPoolEntries + pool.entries().size() <= 2048) {
                     workspace.mobPools.put(pool.id(), pool);
                     totalPoolEntries += pool.entries().size();
                  } else if (totalPoolEntries + pool.entries().size() > 2048) {
                     SololevelingMod.LOGGER
                        .warn("Skipped Dungeon Builder mob pool {} while loading: the workspace exceeds the {}-entry safety limit.", pool.id(), 2048);
                  }
               } catch (RuntimeException var9) {
               }
            }
         }

         if (tag.contains("DungeonDrafts", 9)) {
            for (CompoundTag draftTag : DungeonBuilderProjectData.readList(tag, "DungeonDrafts")) {
               try {
                  DungeonDraft draft = DungeonBuilderProjectData.loadDungeonDraft(draftTag);
                  if (workspace.dungeonDrafts.size() < 64 && DungeonBuilderProjectData.dungeonDraftStructuralProblem(draft) == null) {
                     workspace.dungeonDrafts.put(draft.id(), draft);
                  }
               } catch (RuntimeException var8) {
               }
            }
         }

         return workspace;
      }
   }
}
