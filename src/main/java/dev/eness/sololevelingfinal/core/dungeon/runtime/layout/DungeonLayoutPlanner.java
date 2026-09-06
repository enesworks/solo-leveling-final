package dev.eness.sololevelingfinal.core.dungeon.runtime.layout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataSnapshot;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataTypes;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDefinition;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonRoomDefinition;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonTemplatePlacer;

public final class DungeonLayoutPlanner {
   private static final List<Rotation> HORIZONTAL_ROTATIONS = List.of(
      Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90
   );
   private static final long MAX_TOTAL_TEMPLATE_VOLUME = 500000L;
   private static final int MAX_SEARCH_NODES = 20000;
   private static final int MAX_COMBINATIONS_PER_STATE = 2048;
   private static final int MAX_ATTACHMENTS_PER_STATE = 256;
   private static final int MAX_DIAGNOSTIC_SAMPLES = 16;
   private static final long TARGET_ROOMS_SALT = 5165956214555911695L;
   private static final long START_ORDER_SALT = 8783151889579007295L;
   private static final long PATH_ORDER_SALT = -5436650660319618331L;
   private static final long BOSS_ORDER_SALT = -3310010102799468436L;
   private static final long BRANCH_ORDER_SALT = 2263841685330096990L;

   private DungeonLayoutPlanner() {
   }

   public static DungeonLayoutPlanner.PlanResult plan(
      ServerLevel level, DungeonDataSnapshot snapshot, DungeonDefinition dungeon, BlockPos minimum, long seed, DungeonLayoutPlanner.PlanOptions options
   ) {
      return snapshot == null
         ? invalidResult(
            dungeon == null ? new ResourceLocation("minecraft", "empty") : dungeon.id(),
            0L,
            seed,
            options == null ? DungeonLayoutTopology.LINEAR : options.topology(),
            DungeonLayoutPlanner.PlanFailure.INVALID_ARGUMENT,
            "Dungeon data snapshot is required."
         )
         : plan(
            level,
            DungeonLayoutPlanner.RoomCatalog.snapshot(snapshot),
            dungeon,
            minimum,
            seed,
            options,
            DungeonLayoutPlanner.RoomTemplateResolver.loadedTemplates()
         );
   }

   public static DungeonLayoutPlanner.PlanResult plan(
      ServerLevel level,
      DungeonLayoutPlanner.RoomCatalog catalog,
      DungeonDefinition dungeon,
      BlockPos minimum,
      long seed,
      DungeonLayoutPlanner.PlanOptions options,
      DungeonLayoutPlanner.RoomTemplateResolver resolver
   ) {
      if (level != null && catalog != null && dungeon != null && minimum != null && resolver != null) {
         DungeonLayoutPlanner.PlanOptions safeOptions = options == null ? DungeonLayoutPlanner.PlanOptions.linear() : options;
         if (dungeon.kind() == DungeonDataTypes.DungeonKind.FIXED) {
            return planFixedDefinition(level, catalog, resolver, dungeon, minimum, seed);
         } else if (safeOptions.topology() == DungeonLayoutTopology.FIXED) {
            return invalidResult(
               dungeon.id(),
               catalog.revision(),
               seed,
               safeOptions.topology(),
               DungeonLayoutPlanner.PlanFailure.INVALID_ARGUMENT,
               "FIXED topology requires planFixed and an explicit placement graph."
            );
         } else if (safeOptions.targetRoomCount() >= 0 && safeOptions.criticalPathRooms() >= 0) {
            return dungeon.kind() == DungeonDataTypes.DungeonKind.PRESET
               ? planPreset(level, catalog.revision(), dungeon, minimum, seed, safeOptions)
               : planProcedural(level, catalog, resolver, dungeon, minimum, seed, safeOptions);
         } else {
            return invalidResult(
               dungeon.id(),
               catalog.revision(),
               seed,
               safeOptions.topology(),
               DungeonLayoutPlanner.PlanFailure.INVALID_ARGUMENT,
               "Room-count overrides cannot be negative."
            );
         }
      } else {
         return invalidResult(
            dungeon == null ? new ResourceLocation("minecraft", "empty") : dungeon.id(),
            catalog == null ? 0L : catalog.revision(),
            seed,
            options == null ? DungeonLayoutTopology.LINEAR : options.topology(),
            DungeonLayoutPlanner.PlanFailure.INVALID_ARGUMENT,
            "Level, room catalog, dungeon, origin, and template resolver are required."
         );
      }
   }

   private static DungeonLayoutPlanner.PlanResult planFixedDefinition(
      ServerLevel level,
      DungeonLayoutPlanner.RoomCatalog catalog,
      DungeonLayoutPlanner.RoomTemplateResolver resolver,
      DungeonDefinition dungeon,
      BlockPos minimum,
      long seed
   ) {
      List<DungeonLayoutPlanner.FixedRoomSpec> rooms = new ArrayList<>(dungeon.fixedPlacements().size());

      for (DungeonDataTypes.FixedRoomPlacement placement : dungeon.fixedPlacements()) {
         DungeonRoomDefinition room = catalog.find(placement.room());
         if (room == null) {
            return invalidResult(
               dungeon.id(),
               catalog.revision(),
               seed,
               DungeonLayoutTopology.FIXED,
               DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
               "Fixed placement " + placement.id() + " references unknown room " + placement.room() + "."
            );
         }

         DungeonDataTypes.Int3 position = placement.position();
         rooms.add(new DungeonLayoutPlanner.FixedRoomSpec(placement.id(), room, new BlockPos(position.x(), position.y(), position.z()), placement.rotation()));
      }

      List<DungeonLayoutPlanner.FixedConnectionSpec> connections = dungeon.fixedConnections()
         .stream()
         .map(
            connection -> new DungeonLayoutPlanner.FixedConnectionSpec(
               connection.fromRoom(), connection.fromSocket(), connection.toRoom(), connection.toSocket()
            )
         )
         .toList();
      return planFixed(level, catalog.revision(), minimum, seed, new DungeonLayoutPlanner.FixedLayoutSpec(dungeon.id(), rooms, connections), resolver);
   }

   public static DungeonLayoutPlanner.PlanResult planFixed(
      ServerLevel level, DungeonDataSnapshot snapshot, BlockPos minimum, long seed, DungeonLayoutPlanner.FixedLayoutSpec spec
   ) {
      if (snapshot != null) {
         return planFixed(level, snapshot.revision(), minimum, seed, spec, DungeonLayoutPlanner.RoomTemplateResolver.loadedTemplates());
      }

      ResourceLocation layoutId = spec != null && spec.layoutId() != null ? spec.layoutId() : new ResourceLocation("minecraft", "empty");
      return invalidResult(
         layoutId, 0L, seed, DungeonLayoutTopology.FIXED, DungeonLayoutPlanner.PlanFailure.INVALID_ARGUMENT, "Dungeon data snapshot is required."
      );
   }

   public static DungeonLayoutPlanner.PlanResult planFixed(
      ServerLevel level,
      long revision,
      BlockPos minimum,
      long seed,
      DungeonLayoutPlanner.FixedLayoutSpec spec,
      DungeonLayoutPlanner.RoomTemplateResolver resolver
   ) {
      ResourceLocation layoutId = spec != null && spec.layoutId() != null ? spec.layoutId() : new ResourceLocation("minecraft", "empty");
      if (level != null && minimum != null && spec != null && spec.layoutId() != null && resolver != null) {
         if (!spec.rooms().isEmpty() && spec.rooms().size() <= 64) {
            DungeonLayoutPlanner.FixedLog log = new DungeonLayoutPlanner.FixedLog();
            List<DungeonLayoutPlanner.FixedPreparedRoom> preparedRooms = new ArrayList<>();
            Map<String, DungeonLayoutPlanner.FixedPreparedRoom> byKey = new LinkedHashMap<>();
            long totalVolume = 0L;
            int index = 0;

            while (index < spec.rooms().size()) {
               DungeonLayoutPlanner.FixedRoomSpec roomSpec = spec.rooms().get(index);
               if (roomSpec != null
                  && roomSpec.room() != null
                  && roomSpec.relativeMinimum() != null
                  && !roomSpec.placementKey().isBlank()
                  && roomSpec.placementKey().length() <= 96) {
                  if (byKey.containsKey(roomSpec.placementKey())) {
                     log.reject(
                        DungeonLayoutPlanner.DiagnosticCode.FIXED_DUPLICATE_PLACEMENT,
                        "Duplicate fixed placement key " + roomSpec.placementKey() + ".",
                        roomSpec.room().id(),
                        roomSpec.rotation(),
                        null,
                        index
                     );
                     return fixedFailure(
                        layoutId,
                        revision,
                        seed,
                        minimum,
                        spec.rooms().size(),
                        preparedRooms,
                        List.of(),
                        log,
                        DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
                        "Fixed placement keys must be unique."
                     );
                  }

                  BlockPos worldMinimum = minimum.offset(roomSpec.relativeMinimum());
                  DungeonLayoutPlanner.GeometryPreparation preparation = resolver.prepare(level, roomSpec.room(), worldMinimum, roomSpec.rotation());
                  if (preparation.success() && preparation.geometry() != null) {
                     DungeonRoomGeometry prepared = preparation.geometry();
                     if (!matchesSize(prepared, roomSpec.room().size())) {
                        log.reject(
                           DungeonLayoutPlanner.DiagnosticCode.SIZE_MISMATCH,
                           "Declared and template sizes differ for " + roomSpec.room().id() + ".",
                           roomSpec.room().id(),
                           roomSpec.rotation(),
                           prepared.worldBounds(),
                           index
                        );
                        return fixedFailure(
                           layoutId,
                           revision,
                           seed,
                           minimum,
                           spec.rooms().size(),
                           preparedRooms,
                           List.of(),
                           log,
                           DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
                           "Fixed room " + roomSpec.placementKey() + " has a template-size mismatch."
                        );
                     }

                     if (!insideBuildHeight(level, prepared.worldBounds())) {
                        log.reject(
                           DungeonLayoutPlanner.DiagnosticCode.OUT_OF_BUILD_HEIGHT,
                           "Fixed room extends outside build height.",
                           roomSpec.room().id(),
                           roomSpec.rotation(),
                           prepared.worldBounds(),
                           index
                        );
                        return fixedFailure(
                           layoutId,
                           revision,
                           seed,
                           minimum,
                           spec.rooms().size(),
                           preparedRooms,
                           List.of(),
                           log,
                           DungeonLayoutPlanner.PlanFailure.OUT_OF_WORLD,
                           "Fixed room " + roomSpec.placementKey() + " extends outside build height."
                        );
                     }

                     DungeonLayoutPlanner.FixedPreparedRoom collision = preparedRooms.stream()
                        .filter(existing -> existing.geometry().worldBounds().intersects(prepared.worldBounds()))
                        .findFirst()
                        .orElse(null);
                     if (collision != null) {
                        log.reject(
                           DungeonLayoutPlanner.DiagnosticCode.ROOM_COLLISION,
                           "Fixed room " + roomSpec.placementKey() + " overlaps " + collision.spec().placementKey() + ".",
                           roomSpec.room().id(),
                           roomSpec.rotation(),
                           prepared.worldBounds(),
                           collision.index()
                        );
                        return fixedFailure(
                           layoutId,
                           revision,
                           seed,
                           minimum,
                           spec.rooms().size(),
                           preparedRooms,
                           List.of(),
                           log,
                           DungeonLayoutPlanner.PlanFailure.NO_LAYOUT,
                           "Fixed room placements overlap."
                        );
                     }

                     totalVolume += volume(prepared.worldBounds());
                     if (totalVolume > 500000L) {
                        log.reject(
                           DungeonLayoutPlanner.DiagnosticCode.TOTAL_VOLUME_LIMIT,
                           "Fixed layout exceeds the total template-volume limit.",
                           roomSpec.room().id(),
                           roomSpec.rotation(),
                           prepared.worldBounds(),
                           index
                        );
                        return fixedFailure(
                           layoutId,
                           revision,
                           seed,
                           minimum,
                           spec.rooms().size(),
                           preparedRooms,
                           List.of(),
                           log,
                           DungeonLayoutPlanner.PlanFailure.SAFETY_LIMIT,
                           "Fixed layout exceeds the safe total template volume of 500000 blocks."
                        );
                     }

                     DungeonLayoutPlanner.FixedPreparedRoom fixed = new DungeonLayoutPlanner.FixedPreparedRoom(index, roomSpec, prepared, new HashSet<>());
                     preparedRooms.add(fixed);
                     byKey.put(roomSpec.placementKey(), fixed);
                     index++;
                     continue;
                  }

                  log.reject(
                     DungeonLayoutPlanner.DiagnosticCode.MISSING_TEMPLATE, preparation.message(), roomSpec.room().id(), roomSpec.rotation(), null, index
                  );
                  return fixedFailure(
                     layoutId,
                     revision,
                     seed,
                     minimum,
                     spec.rooms().size(),
                     preparedRooms,
                     List.of(),
                     log,
                     DungeonLayoutPlanner.PlanFailure.MISSING_TEMPLATE,
                     preparation.message()
                  );
               }

               log.reject(
                  DungeonLayoutPlanner.DiagnosticCode.FIXED_DUPLICATE_PLACEMENT,
                  "Every fixed room needs a unique 1-96 character placement key, definition, and relative minimum.",
                  roomSpec != null && roomSpec.room() != null ? roomSpec.room().id() : null,
                  roomSpec == null ? null : roomSpec.rotation(),
                  null,
                  -1
               );
               return fixedFailure(
                  layoutId,
                  revision,
                  seed,
                  minimum,
                  spec.rooms().size(),
                  preparedRooms,
                  List.of(),
                  log,
                  DungeonLayoutPlanner.PlanFailure.INVALID_ARGUMENT,
                  "Invalid fixed room placement."
               );
            }

            List<DungeonLayoutPlanner.FixedPreparedRoom> starts = preparedRooms.stream()
               .filter(room -> room.spec().room().role() == DungeonDataTypes.RoomRole.START)
               .toList();
            long bosses = preparedRooms.stream().filter(room -> room.spec().room().role() == DungeonDataTypes.RoomRole.BOSS).count();
            if (starts.size() == 1 && bosses == 1L) {
               List<DungeonLayoutPlan.PlannedConnection> connections = new ArrayList<>();
               Set<String> usedSocketKeys = new HashSet<>();
               List<List<Integer>> adjacency = new ArrayList<>(preparedRooms.size());

               for (int indexx = 0; indexx < preparedRooms.size(); indexx++) {
                  adjacency.add(new ArrayList<>());
               }

               for (DungeonLayoutPlanner.FixedConnectionSpec edge : spec.connections()) {
                  if (edge == null) {
                     log.reject(DungeonLayoutPlanner.DiagnosticCode.FIXED_INVALID_CONNECTION, "Null fixed connection.", null, null, null, -1);
                     return fixedFailure(
                        layoutId,
                        revision,
                        seed,
                        minimum,
                        spec.rooms().size(),
                        preparedRooms,
                        connections,
                        log,
                        DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
                        "Fixed connection is invalid."
                     );
                  }

                  DungeonLayoutPlanner.FixedPreparedRoom source = byKey.get(edge.sourcePlacement());
                  DungeonLayoutPlanner.FixedPreparedRoom target = byKey.get(edge.targetPlacement());
                  if (source != null && target != null && source != target) {
                     DungeonDataTypes.Socket sourceSocket = socket(source.spec().room(), edge.sourceSocket());
                     DungeonDataTypes.Socket targetSocket = socket(target.spec().room(), edge.targetSocket());
                     if (sourceSocket != null && targetSocket != null) {
                        String sourceUse = source.spec().placementKey() + "\u0000" + sourceSocket.id();
                        String targetUse = target.spec().placementKey() + "\u0000" + targetSocket.id();
                        if (usedSocketKeys.add(sourceUse) && usedSocketKeys.add(targetUse)) {
                           DungeonTemplatePlacer.TransformedSocket sourceWorld = source.geometry()
                              .transformSocket(relative(sourceSocket.opening()), sourceSocket.facing());
                           DungeonTemplatePlacer.TransformedSocket targetWorld = target.geometry()
                              .transformSocket(relative(targetSocket.opening()), targetSocket.facing());
                           if (sourceSocket.type().equals(targetSocket.type())
                              && sourceWorld.facing() == targetWorld.facing().getOpposite()
                              && sameOpeningShape(sourceWorld.opening(), targetWorld.opening(), sourceWorld.facing())) {
                              DungeonTemplatePlacer.WorldBounds carve = union(sourceWorld.opening(), targetWorld.opening());
                              if (fixedPassageAligned(sourceWorld, targetWorld) && axisLength(carve, sourceWorld.facing()) <= 64) {
                                 DungeonLayoutPlanner.FixedPreparedRoom intersectedRoom = preparedRooms.stream()
                                    .filter(room -> room != source && room != target)
                                    .filter(room -> room.geometry().worldBounds().intersects(carve))
                                    .findFirst()
                                    .orElse(null);
                                 if (intersectedRoom != null) {
                                    log.reject(
                                       DungeonLayoutPlanner.DiagnosticCode.FIXED_PASSAGE_COLLISION,
                                       "Passage intersects unrelated room " + intersectedRoom.spec().placementKey() + ".",
                                       target.spec().room().id(),
                                       target.spec().rotation(),
                                       carve,
                                       intersectedRoom.index()
                                    );
                                    return fixedFailure(
                                       layoutId,
                                       revision,
                                       seed,
                                       minimum,
                                       spec.rooms().size(),
                                       preparedRooms,
                                       connections,
                                       log,
                                       DungeonLayoutPlanner.PlanFailure.NO_LAYOUT,
                                       "A fixed passage intersects an unrelated room."
                                    );
                                 }

                                 DungeonLayoutPlan.PlannedConnection intersectedPassage = connections.stream()
                                    .filter(connection -> connection.bounds().intersects(carve))
                                    .findFirst()
                                    .orElse(null);
                                 if (intersectedPassage != null) {
                                    log.reject(
                                       DungeonLayoutPlanner.DiagnosticCode.FIXED_PASSAGE_COLLISION,
                                       "Passage intersects an existing passage.",
                                       target.spec().room().id(),
                                       target.spec().rotation(),
                                       carve,
                                       intersectedPassage.targetRoom()
                                    );
                                    return fixedFailure(
                                       layoutId,
                                       revision,
                                       seed,
                                       minimum,
                                       spec.rooms().size(),
                                       preparedRooms,
                                       connections,
                                       log,
                                       DungeonLayoutPlanner.PlanFailure.NO_LAYOUT,
                                       "Fixed passages may not cross or overlap."
                                    );
                                 }

                                 source.usedSockets().add(sourceSocket.id());
                                 target.usedSockets().add(targetSocket.id());
                                 connections.add(
                                    new DungeonLayoutPlan.PlannedConnection(source.index(), sourceSocket.id(), target.index(), targetSocket.id(), carve)
                                 );
                                 adjacency.get(source.index()).add(target.index());
                                 adjacency.get(target.index()).add(source.index());
                                 continue;
                              }

                              log.reject(
                                 DungeonLayoutPlanner.DiagnosticCode.FIXED_PASSAGE_MISALIGNED,
                                 "Connected openings are not aligned outward or their passage is longer than 64 blocks.",
                                 target.spec().room().id(),
                                 target.spec().rotation(),
                                 carve,
                                 target.index()
                              );
                              return fixedFailure(
                                 layoutId,
                                 revision,
                                 seed,
                                 minimum,
                                 spec.rooms().size(),
                                 preparedRooms,
                                 connections,
                                 log,
                                 DungeonLayoutPlanner.PlanFailure.NO_LAYOUT,
                                 "Fixed connection openings must align on one axis and stay within 64 blocks."
                              );
                           }

                           log.reject(
                              DungeonLayoutPlanner.DiagnosticCode.FIXED_INVALID_CONNECTION,
                              "Fixed sockets have incompatible type, facing, width, or height.",
                              target.spec().room().id(),
                              target.spec().rotation(),
                              target.geometry().worldBounds(),
                              target.index()
                           );
                           return fixedFailure(
                              layoutId,
                              revision,
                              seed,
                              minimum,
                              spec.rooms().size(),
                              preparedRooms,
                              connections,
                              log,
                              DungeonLayoutPlanner.PlanFailure.NO_LAYOUT,
                              "Fixed connection sockets are incompatible."
                           );
                        }

                        log.reject(
                           DungeonLayoutPlanner.DiagnosticCode.FIXED_SOCKET_REUSED,
                           "A fixed socket may participate in only one connection.",
                           null,
                           null,
                           null,
                           -1
                        );
                        return fixedFailure(
                           layoutId,
                           revision,
                           seed,
                           minimum,
                           spec.rooms().size(),
                           preparedRooms,
                           connections,
                           log,
                           DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
                           "A fixed socket is connected more than once."
                        );
                     }

                     log.reject(DungeonLayoutPlanner.DiagnosticCode.FIXED_INVALID_CONNECTION, "Connection references a missing socket.", null, null, null, -1);
                     return fixedFailure(
                        layoutId,
                        revision,
                        seed,
                        minimum,
                        spec.rooms().size(),
                        preparedRooms,
                        connections,
                        log,
                        DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
                        "Fixed connection references a missing socket."
                     );
                  }

                  log.reject(
                     DungeonLayoutPlanner.DiagnosticCode.FIXED_INVALID_CONNECTION,
                     "Connection references a missing placement or connects a room to itself.",
                     null,
                     null,
                     null,
                     -1
                  );
                  return fixedFailure(
                     layoutId,
                     revision,
                     seed,
                     minimum,
                     spec.rooms().size(),
                     preparedRooms,
                     connections,
                     log,
                     DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
                     "Fixed connection references a missing placement or the same room twice."
                  );
               }

               int[] depths = new int[preparedRooms.size()];
               Arrays.fill(depths, -1);
               ArrayDeque<Integer> queue = new ArrayDeque<>();
               depths[starts.get(0).index()] = 0;
               queue.add(starts.get(0).index());

               while (!queue.isEmpty()) {
                  int current = queue.removeFirst();

                  for (int neighbor : adjacency.get(current)) {
                     if (depths[neighbor] < 0) {
                        depths[neighbor] = depths[current] + 1;
                        queue.addLast(neighbor);
                     }
                  }
               }

               if (Arrays.stream(depths).anyMatch(depth -> depth < 0)) {
                  log.reject(
                     DungeonLayoutPlanner.DiagnosticCode.FIXED_GRAPH_DISCONNECTED,
                     "Every fixed room must be reachable from the start room.",
                     null,
                     null,
                     null,
                     -1
                  );
                  return fixedFailure(
                     layoutId,
                     revision,
                     seed,
                     minimum,
                     spec.rooms().size(),
                     preparedRooms,
                     connections,
                     log,
                     DungeonLayoutPlanner.PlanFailure.NO_LAYOUT,
                     "Fixed room graph is disconnected."
                  );
               }

               for (DungeonLayoutPlanner.FixedPreparedRoom room : preparedRooms) {
                  for (DungeonDataTypes.Socket socket : room.spec().room().sockets()) {
                     if (socket.required() && !room.usedSockets().contains(socket.id())) {
                        log.reject(
                           DungeonLayoutPlanner.DiagnosticCode.REQUIRED_SOCKET_UNUSED,
                           "Required socket " + socket.id() + " in " + room.spec().placementKey() + " is unused.",
                           room.spec().room().id(),
                           room.spec().rotation(),
                           room.geometry().worldBounds(),
                           room.index()
                        );
                        return fixedFailure(
                           layoutId,
                           revision,
                           seed,
                           minimum,
                           spec.rooms().size(),
                           preparedRooms,
                           connections,
                           log,
                           DungeonLayoutPlanner.PlanFailure.REQUIRED_SOCKET_UNUSED,
                           "Every required socket in a fixed layout must be connected."
                        );
                     }
                  }
               }

               List<DungeonLayoutPlan.PlannedRoom> rooms = fixedPlannedRooms(preparedRooms, depths);
               int capped = 0;

               for (DungeonLayoutPlanner.FixedPreparedRoom room : preparedRooms) {
                  for (DungeonDataTypes.Socket socket : room.spec().room().sockets()) {
                     if (!socket.required() && !room.usedSockets().contains(socket.id())) {
                        capped++;
                     }
                  }
               }

               if (capped > 0) {
                  log.add(DungeonLayoutPlanner.DiagnosticCode.OPTIONAL_SOCKET_CAPPED, capped);
               }

               DungeonLayoutPlan layout = new DungeonLayoutPlan(rooms, connections);
               DungeonLayoutPreview preview = preview(
                  true,
                  DungeonLayoutPlanner.PlanFailure.NONE,
                  "",
                  layoutId,
                  null,
                  revision,
                  seed,
                  DungeonLayoutTopology.FIXED,
                  rooms.size(),
                  rooms,
                  connections,
                  log.diagnostics(minimum),
                  minimum
               );
               return new DungeonLayoutPlanner.PlanResult(true, DungeonLayoutPlanner.PlanFailure.NONE, "", layout, preview);
            } else {
               return fixedFailure(
                  layoutId,
                  revision,
                  seed,
                  minimum,
                  spec.rooms().size(),
                  preparedRooms,
                  List.of(),
                  log,
                  DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
                  "A fixed dungeon layout requires exactly one start room and exactly one boss room."
               );
            }
         } else {
            return invalidResult(
               layoutId,
               revision,
               seed,
               DungeonLayoutTopology.FIXED,
               DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
               "A fixed layout requires 1-64 room placements."
            );
         }
      } else {
         return invalidResult(
            layoutId,
            revision,
            seed,
            DungeonLayoutTopology.FIXED,
            DungeonLayoutPlanner.PlanFailure.INVALID_ARGUMENT,
            "Level, origin, fixed layout spec, and template resolver are required."
         );
      }
   }

   private static DungeonLayoutPlanner.PlanResult planPreset(
      ServerLevel level, long revision, DungeonDefinition dungeon, BlockPos minimum, long seed, DungeonLayoutPlanner.PlanOptions options
   ) {
      if (dungeon.structure().isEmpty()) {
         return invalidResult(
            dungeon.id(), revision, seed, options.topology(), DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION, "Preset dungeon has no structure."
         );
      }

      DungeonTemplatePlacer.PreparationResult prepared = DungeonTemplatePlacer.prepare(level, dungeon.structure().get(), minimum, Rotation.NONE);
      if (prepared.success() && prepared.template() != null) {
         if (dungeon.size().isPresent() && !matchesSize(prepared.template(), dungeon.size().get())) {
            return invalidResult(
               dungeon.id(),
               revision,
               seed,
               options.topology(),
               DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
               "Definition size "
                  + displaySize(dungeon.size().get())
                  + " does not match structure template size "
                  + displaySize(prepared.template())
                  + ". Re-export the dungeon or correct its JSON size."
            );
         }

         DungeonLayoutPlan.PlannedRoom room = DungeonLayoutPlan.PlannedRoom.preset(dungeon.id(), prepared.template());
         if (!insideBuildHeight(level, room.prepared().worldBounds())) {
            return invalidResult(
               dungeon.id(),
               revision,
               seed,
               options.topology(),
               DungeonLayoutPlanner.PlanFailure.OUT_OF_WORLD,
               "Preset structure would extend outside build height."
            );
         }

         DungeonLayoutPlan layout = new DungeonLayoutPlan(List.of(room), List.of());
         DungeonLayoutPreview preview = preview(
            true,
            DungeonLayoutPlanner.PlanFailure.NONE,
            "",
            dungeon.id(),
            dungeon,
            revision,
            seed,
            options.topology(),
            1,
            layout.rooms(),
            layout.connections(),
            DungeonLayoutPreview.Diagnostics.empty(),
            minimum
         );
         return new DungeonLayoutPlanner.PlanResult(true, DungeonLayoutPlanner.PlanFailure.NONE, "", layout, preview);
      } else {
         return invalidResult(dungeon.id(), revision, seed, options.topology(), DungeonLayoutPlanner.PlanFailure.MISSING_TEMPLATE, prepared.message());
      }
   }

   private static DungeonLayoutPlanner.PlanResult planProcedural(
      ServerLevel level,
      DungeonLayoutPlanner.RoomCatalog catalog,
      DungeonLayoutPlanner.RoomTemplateResolver resolver,
      DungeonDefinition dungeon,
      BlockPos minimum,
      long seed,
      DungeonLayoutPlanner.PlanOptions options
   ) {
      long layoutSeed = DungeonRandomStreams.seed(seed, DungeonRandomStreams.Stream.LAYOUT);
      int targetRooms = options.targetRoomCount() > 0
         ? options.targetRoomCount()
         : dungeon.roomCount().random(RandomSource.create(DungeonRandomStreams.mix(layoutSeed ^ 5165956214555911695L)));
      if (targetRooms >= 3 && dungeon.roomCount().contains(targetRooms)) {
         List<DungeonLayoutPlanner.RoomChoice> starts = resolveChoices(dungeon.rooms(DungeonDataTypes.RoomRole.START), catalog);
         List<DungeonLayoutPlanner.RoomChoice> bosses = resolveChoices(dungeon.rooms(DungeonDataTypes.RoomRole.BOSS), catalog);
         List<DungeonDataTypes.WeightedRoom> middlePool = new ArrayList<>();

         for (DungeonDataTypes.RoomRole role : DungeonDataTypes.RoomRole.values()) {
            if (role != DungeonDataTypes.RoomRole.START && role != DungeonDataTypes.RoomRole.BOSS && role != DungeonDataTypes.RoomRole.CAP) {
               middlePool.addAll(dungeon.rooms(role));
            }
         }

         List<DungeonLayoutPlanner.RoomChoice> middle = resolveChoices(middlePool, catalog);
         if (!starts.isEmpty() && !bosses.isEmpty() && !middle.isEmpty()) {
            int totalMiddle = targetRooms - 2;
            int branchRooms = 0;
            int criticalMiddle = totalMiddle;
            if (options.topology() == DungeonLayoutTopology.BRANCHING) {
               int requestedCritical = options.criticalPathRooms();
               if (requestedCritical > 0) {
                  if (requestedCritical < 3 || requestedCritical >= targetRooms) {
                     return invalidResult(
                        dungeon.id(),
                        catalog.revision(),
                        seed,
                        options.topology(),
                        DungeonLayoutPlanner.PlanFailure.INVALID_ARGUMENT,
                        "Branching criticalPathRooms must be at least 3 and smaller than the total room count."
                     );
                  }

                  criticalMiddle = requestedCritical - 2;
                  branchRooms = targetRooms - requestedCritical;
               } else {
                  branchRooms = Math.max(1, totalMiddle / 3);
                  criticalMiddle = totalMiddle - branchRooms;
               }

               if (criticalMiddle < 1 || branchRooms < 1) {
                  return invalidResult(
                     dungeon.id(),
                     catalog.revision(),
                     seed,
                     options.topology(),
                     DungeonLayoutPlanner.PlanFailure.NO_LAYOUT,
                     "Branching topology needs room budget for at least one critical-path middle room and one side room."
                  );
               }
            }

            DungeonLayoutPlanner.SearchContext context = new DungeonLayoutPlanner.SearchContext(
               level, catalog, resolver, dungeon, minimum, seed, layoutSeed, options.topology(), targetRooms
            );
            DungeonLayoutPlanner.SearchState solved = null;
            List<DungeonLayoutPlanner.RoomChoice> orderedStarts = weightedOrder(starts, DungeonRandomStreams.mix(layoutSeed ^ 8783151889579007295L));
            List<Rotation> startRotations = shuffled(HORIZONTAL_ROTATIONS, DungeonRandomStreams.mix(layoutSeed ^ 8783151889579007295L ^ 826366246L));

            for (DungeonLayoutPlanner.RoomChoice choice : orderedStarts) {
               for (Rotation rotation : startRotations) {
                  if (context.budgetExhausted()) {
                     break;
                  }

                  DungeonLayoutPlanner.GeometryPreparation prepared = context.prepare(choice.definition(), minimum, rotation);
                  if (prepared.success() && prepared.geometry() != null) {
                     DungeonRoomGeometry geometry = prepared.geometry();
                     if (!matchesSize(geometry, choice.definition().size())) {
                        context.reject(
                           DungeonLayoutPlanner.DiagnosticCode.SIZE_MISMATCH,
                           "Declared and template sizes differ for start room " + choice.definition().id() + ".",
                           choice.definition().id(),
                           rotation,
                           geometry.worldBounds(),
                           -1
                        );
                     } else if (!insideBuildHeight(level, geometry.worldBounds())) {
                        context.reject(
                           DungeonLayoutPlanner.DiagnosticCode.OUT_OF_BUILD_HEIGHT,
                           "Start room extends outside build height.",
                           choice.definition().id(),
                           rotation,
                           geometry.worldBounds(),
                           -1
                        );
                     } else {
                        DungeonLayoutPlan.PlannedRoom start = new DungeonLayoutPlan.PlannedRoom(0, 0, choice.definition(), geometry, Set.of());
                        DungeonLayoutPlanner.SearchState initial = new DungeonLayoutPlanner.SearchState(
                           List.of(start), List.of(), volume(geometry.worldBounds())
                        );
                        context.observe(initial);
                        solved = searchCriticalPath(context, initial, 0, criticalMiddle, branchRooms, middle, bosses);
                        if (solved != null) {
                           break;
                        }
                     }
                  } else {
                     context.reject(DungeonLayoutPlanner.DiagnosticCode.MISSING_TEMPLATE, prepared.message(), choice.definition().id(), rotation, null, -1);
                  }
               }

               if (solved != null || context.budgetExhausted()) {
                  break;
               }
            }

            if (solved != null) {
               context.countCappedOptionalSockets(solved);
               DungeonLayoutPlan layout = solved.toPlan();
               DungeonLayoutPreview preview = preview(
                  true,
                  DungeonLayoutPlanner.PlanFailure.NONE,
                  "",
                  dungeon.id(),
                  dungeon,
                  catalog.revision(),
                  seed,
                  options.topology(),
                  targetRooms,
                  layout.rooms(),
                  layout.connections(),
                  context.diagnostics(minimum),
                  minimum
               );
               return new DungeonLayoutPlanner.PlanResult(true, DungeonLayoutPlanner.PlanFailure.NONE, "", layout, preview);
            } else {
               DungeonLayoutPlanner.SearchState partial = context.bestState();
               List<DungeonLayoutPlan.PlannedRoom> partialRooms = partial == null ? List.of() : partial.rooms();
               List<DungeonLayoutPlan.PlannedConnection> partialConnections = partial == null ? List.of() : partial.connections();
               DungeonLayoutPlanner.PlanFailure failure = context.failure();
               String message = context.failureMessage(options.topology());
               DungeonLayoutPreview preview = preview(
                  false,
                  failure,
                  message,
                  dungeon.id(),
                  dungeon,
                  catalog.revision(),
                  seed,
                  options.topology(),
                  targetRooms,
                  partialRooms,
                  partialConnections,
                  context.diagnostics(minimum),
                  minimum
               );
               return new DungeonLayoutPlanner.PlanResult(false, failure, message, null, preview);
            }
         } else {
            return invalidResult(
               dungeon.id(),
               catalog.revision(),
               seed,
               options.topology(),
               DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
               "Procedural dungeon requires resolvable start, middle, and boss room pools."
            );
         }
      } else {
         return invalidResult(
            dungeon.id(),
            catalog.revision(),
            seed,
            options.topology(),
            DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION,
            "Procedural room count " + targetRooms + " must be inside the definition range and leave room for start, middle, and boss rooms."
         );
      }
   }

   @Nullable
   private static DungeonLayoutPlanner.SearchState searchCriticalPath(
      DungeonLayoutPlanner.SearchContext context,
      DungeonLayoutPlanner.SearchState state,
      int endpointRoom,
      int middleRemaining,
      int branchRooms,
      List<DungeonLayoutPlanner.RoomChoice> middleChoices,
      List<DungeonLayoutPlanner.RoomChoice> bossChoices
   ) {
      if (!context.enterNode()) {
         return null;
      }

      context.observe(state);
      if (middleRemaining == 0) {
         List<DungeonLayoutPlanner.Attachment> bosses = attachments(context, state, List.of(endpointRoom), bossChoices, -3310010102799468436L, true, false);
         if (bosses.isEmpty()) {
            context.reject(DungeonLayoutPlanner.DiagnosticCode.DEAD_END_BEFORE_TARGET, "The critical path cannot attach a boss room.", null, null, null, -1);
         }

         for (DungeonLayoutPlanner.Attachment boss : bosses) {
            DungeonLayoutPlanner.SearchState withBoss = state.apply(boss);
            context.observe(withBoss);
            if (branchRooms == 0) {
               if (requiredSocketsUsed(context, withBoss)) {
                  return withBoss;
               }
            } else if (requiredSocketsUsed(context, withBoss)) {
               DungeonLayoutPlanner.SearchState branched = searchBranches(context, withBoss, branchRooms, middleChoices);
               if (branched != null) {
                  return branched;
               }
            }
         }

         return null;
      } else {
         List<DungeonLayoutPlanner.Attachment> attachments = attachments(
            context, state, List.of(endpointRoom), middleChoices, -5436650660319618331L ^ middleRemaining, false, false
         );
         if (attachments.isEmpty()) {
            context.reject(
               DungeonLayoutPlanner.DiagnosticCode.DEAD_END_BEFORE_TARGET, "A critical-path room has no compatible onward attachment.", null, null, null, -1
            );
         }

         for (DungeonLayoutPlanner.Attachment attachment : attachments) {
            DungeonLayoutPlanner.SearchState next = state.apply(attachment);
            DungeonLayoutPlanner.SearchState solved = searchCriticalPath(
               context, next, attachment.room().index(), middleRemaining - 1, branchRooms, middleChoices, bossChoices
            );
            if (solved != null) {
               return solved;
            }

            if (context.budgetExhausted()) {
               return null;
            }
         }

         return null;
      }
   }

   @Nullable
   private static DungeonLayoutPlanner.SearchState searchBranches(
      DungeonLayoutPlanner.SearchContext context,
      DungeonLayoutPlanner.SearchState state,
      int roomsRemaining,
      List<DungeonLayoutPlanner.RoomChoice> middleChoices
   ) {
      if (!context.enterNode()) {
         return null;
      }

      context.observe(state);
      if (roomsRemaining == 0) {
         return requiredSocketsUsed(context, state) ? state : null;
      }

      List<Integer> sourceRooms = new ArrayList<>();

      for (DungeonLayoutPlan.PlannedRoom room : state.rooms()) {
         if (!room.preset() && room.definition().role() != DungeonDataTypes.RoomRole.BOSS) {
            sourceRooms.add(room.index());
         }
      }

      List<DungeonLayoutPlanner.Attachment> attachments = attachments(
         context, state, sourceRooms, middleChoices, 2263841685330096990L ^ roomsRemaining, false, true
      );
      if (attachments.isEmpty()) {
         context.reject(
            DungeonLayoutPlanner.DiagnosticCode.BRANCH_SOCKET_UNAVAILABLE,
            "No compatible optional branch socket can place the remaining " + roomsRemaining + " room(s).",
            null,
            null,
            null,
            -1
         );
      }

      for (DungeonLayoutPlanner.Attachment attachment : attachments) {
         DungeonLayoutPlanner.SearchState next = state.apply(attachment);
         DungeonLayoutPlanner.SearchState solved = searchBranches(context, next, roomsRemaining - 1, middleChoices);
         if (solved != null) {
            return solved;
         }

         if (context.budgetExhausted()) {
            return null;
         }
      }

      return null;
   }

   private static List<DungeonLayoutPlanner.Attachment> attachments(
      DungeonLayoutPlanner.SearchContext context,
      DungeonLayoutPlanner.SearchState state,
      List<Integer> sourceRoomIndexes,
      List<DungeonLayoutPlanner.RoomChoice> choices,
      long orderSalt,
      boolean boss,
      boolean branchPhase
   ) {
      long stateSeed = DungeonRandomStreams.mix(context.layoutSeed() ^ orderSalt ^ fingerprint(state));
      List<DungeonLayoutPlanner.SourceSocket> sources = sourceSockets(state, sourceRoomIndexes, branchPhase);
      if (sources.isEmpty()) {
         context.reject(
            DungeonLayoutPlanner.DiagnosticCode.NO_SOURCE_SOCKET,
            branchPhase ? "No unused branch socket is available." : "No unused path socket is available.",
            null,
            null,
            null,
            -1
         );
         return List.of();
      }

      sources = shuffled(sources, DungeonRandomStreams.mix(stateSeed ^ 1360960803L));
      List<DungeonLayoutPlanner.RoomChoice> orderedChoices = weightedOrder(choices, DungeonRandomStreams.mix(stateSeed ^ 2759596079L));
      List<DungeonLayoutPlanner.Attachment> result = new ArrayList<>();
      int combinations = 0;

      for (DungeonLayoutPlanner.SourceSocket source : sources) {
         if (source.room().depth() >= context.dungeon().maxDepth()) {
            context.reject(
               DungeonLayoutPlanner.DiagnosticCode.DEPTH_LIMIT,
               "Socket " + source.socket().id() + " is at max depth.",
               source.room().definitionId(),
               source.room().geometry().rotation(),
               source.room().geometry().worldBounds(),
               source.room().index()
            );
         } else {
            for (DungeonLayoutPlanner.RoomChoice choice : orderedChoices) {
               DungeonRoomDefinition candidate = choice.definition();
               List<DungeonDataTypes.Socket> candidateSockets = candidate.sockets().stream().filter(DungeonDataTypes.Socket::required).toList();
               if (candidateSockets.isEmpty()) {
                  candidateSockets = candidate.sockets();
               }

               candidateSockets = shuffled(candidateSockets, DungeonRandomStreams.mix(stateSeed ^ candidate.id().hashCode() ^ source.socket().id().hashCode()));
               List<Rotation> rotations = shuffled(HORIZONTAL_ROTATIONS, DungeonRandomStreams.mix(stateSeed ^ candidate.structure().hashCode() ^ 2032902941L));

               for (DungeonDataTypes.Socket candidateSocket : candidateSockets) {
                  if (!source.socket().type().equals(candidateSocket.type())) {
                     context.increment(DungeonLayoutPlanner.DiagnosticCode.SOCKET_TYPE_MISMATCH);
                  } else {
                     for (Rotation rotation : rotations) {
                        if (++combinations > 2048) {
                           context.reject(
                              DungeonLayoutPlanner.DiagnosticCode.CANDIDATE_LIMIT_REACHED,
                              "Candidate enumeration reached the per-state safety limit.",
                              candidate.id(),
                              rotation,
                              null,
                              -1
                           );
                           return result;
                        }

                        DungeonLayoutPlanner.GeometryPreparation zeroResult = context.zeroVariant(candidate, rotation);
                        if (zeroResult.success() && zeroResult.geometry() != null) {
                           DungeonRoomGeometry zero = zeroResult.geometry();
                           if (!matchesSize(zero, candidate.size())) {
                              context.reject(
                                 DungeonLayoutPlanner.DiagnosticCode.SIZE_MISMATCH,
                                 "Declared and template sizes differ for " + candidate.id() + ".",
                                 candidate.id(),
                                 rotation,
                                 zero.worldBounds(),
                                 -1
                              );
                           } else {
                              DungeonTemplatePlacer.TransformedSocket zeroSocket = zero.transformSocket(
                                 relative(candidateSocket.opening()), candidateSocket.facing()
                              );
                              if (zeroSocket.facing() != source.world().facing().getOpposite()) {
                                 context.increment(DungeonLayoutPlanner.DiagnosticCode.SOCKET_FACING_MISMATCH);
                              } else if (!sameOpeningShape(source.world().opening(), zeroSocket.opening(), source.world().facing())) {
                                 context.increment(DungeonLayoutPlanner.DiagnosticCode.OPENING_SHAPE_MISMATCH);
                              } else {
                                 BlockPos candidateMinimum = adjacentMinimum(source, zeroSocket, zero.rotatedSize());
                                 DungeonLayoutPlanner.GeometryPreparation placedResult = context.prepare(candidate, candidateMinimum, rotation);
                                 if (placedResult.success() && placedResult.geometry() != null) {
                                    DungeonRoomGeometry prepared = placedResult.geometry();
                                    if (!insideBuildHeight(context.level(), prepared.worldBounds())) {
                                       context.reject(
                                          DungeonLayoutPlanner.DiagnosticCode.OUT_OF_BUILD_HEIGHT,
                                          "Candidate room extends outside build height.",
                                          candidate.id(),
                                          rotation,
                                          prepared.worldBounds(),
                                          -1
                                       );
                                    } else {
                                       DungeonLayoutPlan.PlannedRoom collision = state.rooms()
                                          .stream()
                                          .filter(existing -> existing.geometry().worldBounds().intersects(prepared.worldBounds()))
                                          .findFirst()
                                          .orElse(null);
                                       if (collision != null) {
                                          context.reject(
                                             DungeonLayoutPlanner.DiagnosticCode.ROOM_COLLISION,
                                             "Candidate " + candidate.id() + " overlaps room " + collision.index() + ".",
                                             candidate.id(),
                                             rotation,
                                             prepared.worldBounds(),
                                             collision.index()
                                          );
                                       } else {
                                          long nextVolume = state.totalVolume() + volume(prepared.worldBounds());
                                          if (nextVolume > 500000L) {
                                             context.reject(
                                                DungeonLayoutPlanner.DiagnosticCode.TOTAL_VOLUME_LIMIT,
                                                "Candidate would exceed the total template-volume limit.",
                                                candidate.id(),
                                                rotation,
                                                prepared.worldBounds(),
                                                -1
                                             );
                                          } else {
                                             int roomIndex = state.rooms().size();
                                             DungeonLayoutPlan.PlannedRoom room = new DungeonLayoutPlan.PlannedRoom(
                                                roomIndex, source.room().depth() + 1, candidate, prepared, Set.of(candidateSocket.id())
                                             );
                                             DungeonTemplatePlacer.TransformedSocket targetWorld = prepared.transformSocket(
                                                relative(candidateSocket.opening()), candidateSocket.facing()
                                             );
                                             DungeonTemplatePlacer.WorldBounds carve = union(source.world().opening(), targetWorld.opening());
                                             result.add(
                                                new DungeonLayoutPlanner.Attachment(
                                                   source,
                                                   room,
                                                   new DungeonLayoutPlan.PlannedConnection(
                                                      source.room().index(), source.socket().id(), room.index(), candidateSocket.id(), carve
                                                   ),
                                                   nextVolume
                                                )
                                             );
                                             if (result.size() >= 256) {
                                                context.reject(
                                                   DungeonLayoutPlanner.DiagnosticCode.CANDIDATE_LIMIT_REACHED,
                                                   "Feasible attachment list reached the per-state safety limit.",
                                                   candidate.id(),
                                                   rotation,
                                                   prepared.worldBounds(),
                                                   -1
                                                );
                                                return result;
                                             }
                                          }
                                       }
                                    }
                                 } else {
                                    context.reject(
                                       DungeonLayoutPlanner.DiagnosticCode.MISSING_TEMPLATE, placedResult.message(), candidate.id(), rotation, null, -1
                                    );
                                 }
                              }
                           }
                        } else {
                           context.reject(DungeonLayoutPlanner.DiagnosticCode.MISSING_TEMPLATE, zeroResult.message(), candidate.id(), rotation, null, -1);
                        }
                     }
                  }
               }
            }
         }
      }

      return result;
   }

   private static List<DungeonLayoutPlanner.SourceSocket> sourceSockets(DungeonLayoutPlanner.SearchState state, List<Integer> roomIndexes, boolean branchPhase) {
      List<DungeonLayoutPlanner.SourceSocket> result = new ArrayList<>();

      for (int roomIndex : roomIndexes) {
         if (roomIndex >= 0 && roomIndex < state.rooms().size()) {
            DungeonLayoutPlan.PlannedRoom room = state.rooms().get(roomIndex);
            if (!room.preset()) {
               for (DungeonDataTypes.Socket socket : room.definition().sockets()) {
                  if (!room.usedSockets().contains(socket.id())) {
                     result.add(
                        new DungeonLayoutPlanner.SourceSocket(room, socket, room.geometry().transformSocket(relative(socket.opening()), socket.facing()))
                     );
                  }
               }
            }
         }
      }

      List<DungeonLayoutPlanner.SourceSocket> required = result.stream().filter(source -> source.socket().required()).toList();
      if (!required.isEmpty()) {
         return required;
      } else {
         return branchPhase ? result.stream().filter(source -> !source.socket().required()).toList() : result;
      }
   }

   private static boolean requiredSocketsUsed(DungeonLayoutPlanner.SearchContext context, DungeonLayoutPlanner.SearchState state) {
      boolean valid = true;

      for (DungeonLayoutPlan.PlannedRoom room : state.rooms()) {
         if (!room.preset()) {
            for (DungeonDataTypes.Socket socket : room.definition().sockets()) {
               if (socket.required() && !room.usedSockets().contains(socket.id())) {
                  context.reject(
                     DungeonLayoutPlanner.DiagnosticCode.REQUIRED_SOCKET_UNUSED,
                     "Required socket " + socket.id() + " in " + room.definitionId() + " is unused.",
                     room.definitionId(),
                     room.geometry().rotation(),
                     room.geometry().worldBounds(),
                     room.index()
                  );
                  valid = false;
               }
            }
         }
      }

      return valid;
   }

   private static List<DungeonLayoutPlanner.RoomChoice> resolveChoices(List<DungeonDataTypes.WeightedRoom> choices, DungeonLayoutPlanner.RoomCatalog catalog) {
      Map<ResourceLocation, Integer> combined = new LinkedHashMap<>();

      for (DungeonDataTypes.WeightedRoom choice : choices) {
         if (catalog.find(choice.room()) != null) {
            combined.merge(choice.room(), Math.max(1, choice.weight()), (left, right) -> (int)Math.min(2147483647L, (long)left.intValue() + right.intValue()));
         }
      }

      List<DungeonLayoutPlanner.RoomChoice> result = new ArrayList<>();
      combined.forEach((id, weight) -> {
         DungeonRoomDefinition definition = catalog.find(id);
         if (definition != null) {
            result.add(new DungeonLayoutPlanner.RoomChoice(definition, weight));
         }
      });
      return result;
   }

   private static List<DungeonLayoutPlanner.RoomChoice> weightedOrder(List<DungeonLayoutPlanner.RoomChoice> source, long seed) {
      List<DungeonLayoutPlanner.RoomChoice> remaining = new ArrayList<>(source);
      List<DungeonLayoutPlanner.RoomChoice> result = new ArrayList<>(source.size());
      RandomSource random = RandomSource.create(seed);

      while (!remaining.isEmpty()) {
         long total = remaining.stream().mapToLong(choice -> Math.max(1, choice.weight())).sum();
         long roll = Math.floorMod(random.nextLong(), Math.max(1L, total));
         int selected = remaining.size() - 1;

         for (int index = 0; index < remaining.size(); index++) {
            roll -= Math.max(1, remaining.get(index).weight());
            if (roll < 0L) {
               selected = index;
               break;
            }
         }

         result.add(remaining.remove(selected));
      }

      return result;
   }

   private static <T> List<T> shuffled(Collection<T> source, long seed) {
      List<T> result = new ArrayList<>(source);
      RandomSource random = RandomSource.create(seed);

      for (int index = result.size() - 1; index > 0; index--) {
         int other = random.nextInt(index + 1);
         T value = result.get(index);
         result.set(index, result.get(other));
         result.set(other, value);
      }

      return result;
   }

   private static long fingerprint(DungeonLayoutPlanner.SearchState state) {
      long result = 7640891576956012809L;
      BlockPos origin = state.rooms().isEmpty() ? BlockPos.ZERO : state.rooms().get(0).geometry().worldBounds().min();

      for (DungeonLayoutPlan.PlannedRoom room : state.rooms()) {
         DungeonTemplatePlacer.WorldBounds bounds = room.geometry().worldBounds();
         result = DungeonRandomStreams.mix(result ^ room.definitionId().hashCode());
         result = DungeonRandomStreams.mix(result ^ room.geometry().rotation().ordinal());
         result = DungeonRandomStreams.mix(result ^ (long)bounds.min().getX() - origin.getX());
         result = DungeonRandomStreams.mix(result ^ (long)bounds.min().getY() - origin.getY());
         result = DungeonRandomStreams.mix(result ^ (long)bounds.min().getZ() - origin.getZ());
         result = DungeonRandomStreams.mix(result ^ (long)bounds.max().getX() - origin.getX());
         result = DungeonRandomStreams.mix(result ^ (long)bounds.max().getY() - origin.getY());
         result = DungeonRandomStreams.mix(result ^ (long)bounds.max().getZ() - origin.getZ());

         for (String socket : room.usedSockets().stream().sorted().toList()) {
            result = DungeonRandomStreams.mix(result ^ socket.hashCode());
         }
      }

      return result;
   }

   private static BlockPos adjacentMinimum(DungeonLayoutPlanner.SourceSocket source, DungeonTemplatePlacer.TransformedSocket candidateAtZero, Vec3i rotatedSize) {
      DungeonTemplatePlacer.WorldBounds sourceRoom = source.room().geometry().worldBounds();
      DungeonTemplatePlacer.WorldBounds sourceOpening = source.world().opening();
      DungeonTemplatePlacer.WorldBounds candidateOpening = candidateAtZero.opening();
      Direction direction = source.world().facing();
      int x = sourceOpening.min().getX() - candidateOpening.min().getX();
      int y = sourceOpening.min().getY() - candidateOpening.min().getY();
      int z = sourceOpening.min().getZ() - candidateOpening.min().getZ();
      switch (direction) {
         case EAST:
            x = sourceRoom.max().getX() + 1;
            break;
         case WEST:
            x = sourceRoom.min().getX() - rotatedSize.getX();
            break;
         case UP:
            y = sourceRoom.max().getY() + 1;
            break;
         case DOWN:
            y = sourceRoom.min().getY() - rotatedSize.getY();
            break;
         case SOUTH:
            z = sourceRoom.max().getZ() + 1;
            break;
         case NORTH:
            z = sourceRoom.min().getZ() - rotatedSize.getZ();
      }

      return new BlockPos(x, y, z);
   }

   private static boolean sameOpeningShape(DungeonTemplatePlacer.WorldBounds first, DungeonTemplatePlacer.WorldBounds second, Direction direction) {
      return switch (direction.getAxis()) {
         case X -> first.sizeY() == second.sizeY() && first.sizeZ() == second.sizeZ();
         case Y -> first.sizeX() == second.sizeX() && first.sizeZ() == second.sizeZ();
         case Z -> first.sizeX() == second.sizeX() && first.sizeY() == second.sizeY();
      };
   }

   private static boolean matchesSize(DungeonTemplatePlacer.PreparedTemplate prepared, DungeonDataTypes.Int3 declared) {
      return prepared.sourceSize().getX() == declared.x() && prepared.sourceSize().getY() == declared.y() && prepared.sourceSize().getZ() == declared.z();
   }

   private static boolean matchesSize(DungeonRoomGeometry prepared, DungeonDataTypes.Int3 declared) {
      return prepared.sourceSize().getX() == declared.x() && prepared.sourceSize().getY() == declared.y() && prepared.sourceSize().getZ() == declared.z();
   }

   private static String displaySize(DungeonTemplatePlacer.PreparedTemplate prepared) {
      return prepared.sourceSize().getX() + "x" + prepared.sourceSize().getY() + "x" + prepared.sourceSize().getZ();
   }

   private static String displaySize(DungeonDataTypes.Int3 size) {
      return size.x() + "x" + size.y() + "x" + size.z();
   }

   private static boolean insideBuildHeight(ServerLevel level, DungeonTemplatePlacer.WorldBounds bounds) {
      return bounds.min().getY() >= level.getMinBuildHeight() && bounds.max().getY() < level.getMaxBuildHeight();
   }

   private static long volume(DungeonTemplatePlacer.WorldBounds bounds) {
      return (long)bounds.sizeX() * bounds.sizeY() * bounds.sizeZ();
   }

   private static BlockPos block(DungeonDataTypes.Int3 vector) {
      return new BlockPos(vector.x(), vector.y(), vector.z());
   }

   private static DungeonTemplatePlacer.RelativeBounds relative(DungeonDataTypes.Bounds3 bounds) {
      return new DungeonTemplatePlacer.RelativeBounds(block(bounds.min()), block(bounds.max()));
   }

   private static DungeonTemplatePlacer.WorldBounds union(DungeonTemplatePlacer.WorldBounds first, DungeonTemplatePlacer.WorldBounds second) {
      return new DungeonTemplatePlacer.WorldBounds(
         new BlockPos(
            Math.min(first.min().getX(), second.min().getX()),
            Math.min(first.min().getY(), second.min().getY()),
            Math.min(first.min().getZ(), second.min().getZ())
         ),
         new BlockPos(
            Math.max(first.max().getX(), second.max().getX()),
            Math.max(first.max().getY(), second.max().getY()),
            Math.max(first.max().getZ(), second.max().getZ())
         )
      );
   }

   private static DungeonLayoutPlanner.PlanResult invalidResult(
      ResourceLocation dungeonId, long revision, long seed, DungeonLayoutTopology topology, DungeonLayoutPlanner.PlanFailure failure, String message
   ) {
      DungeonLayoutPreview preview = new DungeonLayoutPreview(
         false, failure, message, dungeonId, revision, seed, topology, 0, List.of(), List.of(), DungeonLayoutPreview.Diagnostics.empty()
      );
      return new DungeonLayoutPlanner.PlanResult(false, failure, message, null, preview);
   }

   private static DungeonLayoutPreview preview(
      boolean success,
      DungeonLayoutPlanner.PlanFailure failure,
      String message,
      ResourceLocation dungeonId,
      @Nullable DungeonDefinition dungeon,
      long revision,
      long seed,
      DungeonLayoutTopology topology,
      int targetRooms,
      List<DungeonLayoutPlan.PlannedRoom> rooms,
      List<DungeonLayoutPlan.PlannedConnection> connections,
      DungeonLayoutPreview.Diagnostics diagnostics,
      BlockPos origin
   ) {
      List<DungeonLayoutPreview.RoomView> roomViews = new ArrayList<>();

      for (DungeonLayoutPlan.PlannedRoom room : rooms) {
         List<DungeonDataTypes.Socket> sockets = room.preset() && dungeon != null
            ? dungeon.sockets()
            : (room.preset() ? List.of() : room.definition().sockets());
         List<DungeonDataTypes.Marker> markers = room.preset() && dungeon != null
            ? dungeon.markers()
            : (room.preset() ? List.of() : room.definition().markers());
         List<DungeonLayoutPreview.SocketView> socketViews = new ArrayList<>();

         for (DungeonDataTypes.Socket socket : sockets) {
            DungeonTemplatePlacer.TransformedSocket transformed = room.geometry().transformSocket(relative(socket.opening()), socket.facing());
            socketViews.add(
               new DungeonLayoutPreview.SocketView(
                  socket.id(),
                  socket.type(),
                  socket.required(),
                  room.usedSockets().contains(socket.id()),
                  transformed.facing(),
                  relativeBounds(transformed.opening(), origin)
               )
            );
         }

         List<DungeonLayoutPreview.MarkerView> markerViews = new ArrayList<>();

         for (DungeonDataTypes.Marker marker : markers) {
            markerViews.add(
               new DungeonLayoutPreview.MarkerView(
                  marker.id(), marker.type(), marker.group(), relativePosition(room.geometry().transformRelative(block(marker.position())), origin)
               )
            );
         }

         String role = room.preset() ? "preset" : room.definition().role().name().toLowerCase(Locale.ROOT);
         roomViews.add(
            new DungeonLayoutPreview.RoomView(
               room.index(),
               room.depth(),
               room.placementKey(),
               room.definitionId(),
               room.geometry().templateId(),
               role,
               room.geometry().rotation(),
               relativeBounds(room.geometry().worldBounds(), origin),
               socketViews,
               markerViews
            )
         );
      }

      List<DungeonLayoutPreview.ConnectionView> connectionViews = connections.stream()
         .map(
            connection -> new DungeonLayoutPreview.ConnectionView(
               connection.sourceRoom(),
               connection.sourceSocket(),
               connection.targetRoom(),
               connection.targetSocket(),
               relativeBounds(connection.bounds(), origin)
            )
         )
         .toList();
      return new DungeonLayoutPreview(success, failure, message, dungeonId, revision, seed, topology, targetRooms, roomViews, connectionViews, diagnostics);
   }

   private static DungeonLayoutPlanner.PlanResult fixedFailure(
      ResourceLocation layoutId,
      long revision,
      long seed,
      BlockPos origin,
      int targetRooms,
      List<DungeonLayoutPlanner.FixedPreparedRoom> preparedRooms,
      List<DungeonLayoutPlan.PlannedConnection> connections,
      DungeonLayoutPlanner.FixedLog log,
      DungeonLayoutPlanner.PlanFailure failure,
      String message
   ) {
      int[] depths = new int[preparedRooms.size()];
      List<DungeonLayoutPlan.PlannedRoom> rooms = fixedPlannedRooms(preparedRooms, depths);
      DungeonLayoutPreview preview = preview(
         false, failure, message, layoutId, null, revision, seed, DungeonLayoutTopology.FIXED, targetRooms, rooms, connections, log.diagnostics(origin), origin
      );
      return new DungeonLayoutPlanner.PlanResult(false, failure, message, null, preview);
   }

   private static List<DungeonLayoutPlan.PlannedRoom> fixedPlannedRooms(List<DungeonLayoutPlanner.FixedPreparedRoom> source, int[] depths) {
      List<DungeonLayoutPlan.PlannedRoom> result = new ArrayList<>(source.size());

      for (DungeonLayoutPlanner.FixedPreparedRoom room : source) {
         int depth = room.index() < depths.length ? Math.max(0, depths[room.index()]) : 0;
         result.add(new DungeonLayoutPlan.PlannedRoom(room.spec().placementKey(), room.index(), depth, room.spec().room(), room.geometry(), room.usedSockets()));
      }

      return result;
   }

   @Nullable
   private static DungeonDataTypes.Socket socket(DungeonRoomDefinition room, String id) {
      return id == null ? null : room.sockets().stream().filter(socket -> socket.id().equals(id)).findFirst().orElse(null);
   }

   private static boolean fixedPassageAligned(DungeonTemplatePlacer.TransformedSocket source, DungeonTemplatePlacer.TransformedSocket target) {
      DungeonTemplatePlacer.WorldBounds first = source.opening();
      DungeonTemplatePlacer.WorldBounds second = target.opening();

      return switch (source.facing()) {
         case EAST -> first.min().getY() == second.min().getY()
            && first.max().getY() == second.max().getY()
            && first.min().getZ() == second.min().getZ()
            && first.max().getZ() == second.max().getZ()
            && second.min().getX() > first.max().getX();
         case WEST -> first.min().getY() == second.min().getY()
            && first.max().getY() == second.max().getY()
            && first.min().getZ() == second.min().getZ()
            && first.max().getZ() == second.max().getZ()
            && second.max().getX() < first.min().getX();
         case UP -> first.min().getX() == second.min().getX()
            && first.max().getX() == second.max().getX()
            && first.min().getZ() == second.min().getZ()
            && first.max().getZ() == second.max().getZ()
            && second.min().getY() > first.max().getY();
         case DOWN -> first.min().getX() == second.min().getX()
            && first.max().getX() == second.max().getX()
            && first.min().getZ() == second.min().getZ()
            && first.max().getZ() == second.max().getZ()
            && second.max().getY() < first.min().getY();
         case SOUTH -> first.min().getX() == second.min().getX()
            && first.max().getX() == second.max().getX()
            && first.min().getY() == second.min().getY()
            && first.max().getY() == second.max().getY()
            && second.min().getZ() > first.max().getZ();
         case NORTH -> first.min().getX() == second.min().getX()
            && first.max().getX() == second.max().getX()
            && first.min().getY() == second.min().getY()
            && first.max().getY() == second.max().getY()
            && second.max().getZ() < first.min().getZ();
      };
   }

   private static int axisLength(DungeonTemplatePlacer.WorldBounds bounds, Direction direction) {
      return switch (direction.getAxis()) {
         case X -> bounds.sizeX();
         case Y -> bounds.sizeY();
         case Z -> bounds.sizeZ();
      };
   }

   private static DungeonLayoutPreview.Bounds relativeBounds(DungeonTemplatePlacer.WorldBounds bounds, BlockPos origin) {
      return new DungeonLayoutPreview.Bounds(relativePosition(bounds.min(), origin), relativePosition(bounds.max(), origin));
   }

   private static BlockPos relativePosition(BlockPos position, BlockPos origin) {
      return new BlockPos(position.getX() - origin.getX(), position.getY() - origin.getY(), position.getZ() - origin.getZ());
   }

   private static String description(DungeonLayoutPlanner.DiagnosticCode code) {
      return switch (code) {
         case MISSING_TEMPLATE -> "Structure template could not be loaded.";
         case SIZE_MISMATCH -> "Room JSON size differs from its structure template.";
         case NO_SOURCE_SOCKET -> "No unused compatible source socket was available.";
         case DEPTH_LIMIT -> "A source socket had reached max_depth.";
         case SOCKET_TYPE_MISMATCH -> "Socket types differed.";
         case SOCKET_FACING_MISMATCH -> "Rotated sockets did not face each other.";
         case OPENING_SHAPE_MISMATCH -> "Socket opening width or height differed.";
         case OUT_OF_BUILD_HEIGHT -> "A room would extend outside build height.";
         case ROOM_COLLISION -> "A candidate room intersected an already planned room.";
         case DEAD_END_BEFORE_TARGET -> "The critical path ended before all requested rooms or boss were placed.";
         case REQUIRED_SOCKET_UNUSED -> "A required socket remained unconnected.";
         case TOTAL_VOLUME_LIMIT -> "A candidate exceeded the safe total template volume.";
         case SEARCH_BUDGET_EXHAUSTED -> "Deterministic backtracking reached its node budget.";
         case CANDIDATE_LIMIT_REACHED -> "Per-state candidate enumeration was truncated for safety.";
         case BRANCH_SOCKET_UNAVAILABLE -> "The requested branch could not be attached or capped.";
         case OPTIONAL_SOCKET_CAPPED -> "Unused optional sockets remain solid and uncarved.";
         case FIXED_DUPLICATE_PLACEMENT -> "A fixed placement key was missing or duplicated.";
         case FIXED_INVALID_CONNECTION -> "A fixed connection referenced incompatible rooms or sockets.";
         case FIXED_SOCKET_REUSED -> "A fixed socket was connected more than once.";
         case FIXED_PASSAGE_MISALIGNED -> "Fixed connection openings were not aligned along one axis.";
         case FIXED_PASSAGE_COLLISION -> "A fixed passage crossed an unrelated room or another passage.";
         case FIXED_GRAPH_DISCONNECTED -> "The fixed room graph was not fully reachable from start.";
      };
   }

   private record Attachment(
      DungeonLayoutPlanner.SourceSocket source, DungeonLayoutPlan.PlannedRoom room, DungeonLayoutPlan.PlannedConnection connection, long totalVolume
   ) {
   }

   public enum DiagnosticCode {
      MISSING_TEMPLATE,
      SIZE_MISMATCH,
      NO_SOURCE_SOCKET,
      DEPTH_LIMIT,
      SOCKET_TYPE_MISMATCH,
      SOCKET_FACING_MISMATCH,
      OPENING_SHAPE_MISMATCH,
      OUT_OF_BUILD_HEIGHT,
      ROOM_COLLISION,
      DEAD_END_BEFORE_TARGET,
      REQUIRED_SOCKET_UNUSED,
      TOTAL_VOLUME_LIMIT,
      SEARCH_BUDGET_EXHAUSTED,
      CANDIDATE_LIMIT_REACHED,
      BRANCH_SOCKET_UNAVAILABLE,
      OPTIONAL_SOCKET_CAPPED,
      FIXED_DUPLICATE_PLACEMENT,
      FIXED_INVALID_CONNECTION,
      FIXED_SOCKET_REUSED,
      FIXED_PASSAGE_MISALIGNED,
      FIXED_PASSAGE_COLLISION,
      FIXED_GRAPH_DISCONNECTED;
   }

   public record FixedConnectionSpec(String sourcePlacement, String sourceSocket, String targetPlacement, String targetSocket) {
   }

   public record FixedLayoutSpec(
      ResourceLocation layoutId, List<DungeonLayoutPlanner.FixedRoomSpec> rooms, List<DungeonLayoutPlanner.FixedConnectionSpec> connections
   ) {
      public FixedLayoutSpec {
         rooms = rooms == null ? List.of() : List.copyOf(rooms);
         connections = connections == null ? List.of() : List.copyOf(connections);
      }
   }

   private static final class FixedLog {
      private final EnumMap<DungeonLayoutPlanner.DiagnosticCode, Integer> counts = new EnumMap<>(DungeonLayoutPlanner.DiagnosticCode.class);
      private final List<DungeonLayoutPlanner.RawSample> samples = new ArrayList<>();

      private void add(DungeonLayoutPlanner.DiagnosticCode code, int amount) {
         if (amount > 0) {
            this.counts.merge(code, amount, (left, right) -> (int)Math.min(2147483647L, (long)left.intValue() + right.intValue()));
         }
      }

      private void reject(
         DungeonLayoutPlanner.DiagnosticCode code,
         String detail,
         @Nullable ResourceLocation candidateRoom,
         @Nullable Rotation rotation,
         @Nullable DungeonTemplatePlacer.WorldBounds bounds,
         int conflictingRoom
      ) {
         this.add(code, 1);
         if (this.samples.size() < 16) {
            this.samples.add(new DungeonLayoutPlanner.RawSample(code, detail == null ? "" : detail, candidateRoom, rotation, bounds, conflictingRoom));
         }
      }

      private DungeonLayoutPreview.Diagnostics diagnostics(BlockPos origin) {
         List<DungeonLayoutPreview.DiagnosticCount> countViews = this.counts
            .entrySet()
            .stream()
            .sorted(Entry.comparingByKey())
            .map(entry -> new DungeonLayoutPreview.DiagnosticCount(entry.getKey(), entry.getValue(), DungeonLayoutPlanner.description(entry.getKey())))
            .toList();
         List<DungeonLayoutPreview.DiagnosticSample> sampleViews = this.samples
            .stream()
            .map(
               sample -> new DungeonLayoutPreview.DiagnosticSample(
                  sample.code(),
                  sample.detail(),
                  sample.candidateRoom(),
                  sample.rotation(),
                  sample.bounds() == null ? null : DungeonLayoutPlanner.relativeBounds(sample.bounds(), origin),
                  sample.conflictingRoom()
               )
            )
            .toList();
         return new DungeonLayoutPreview.Diagnostics(0, false, countViews, sampleViews);
      }
   }

   private record FixedPreparedRoom(int index, DungeonLayoutPlanner.FixedRoomSpec spec, DungeonRoomGeometry geometry, Set<String> usedSockets) {
   }

   public record FixedRoomSpec(String placementKey, DungeonRoomDefinition room, BlockPos relativeMinimum, Rotation rotation) {
      public FixedRoomSpec {
         placementKey = placementKey == null ? "" : placementKey;
         rotation = rotation == null ? Rotation.NONE : rotation;
      }
   }

   public record GeometryPreparation(boolean success, String message, @Nullable DungeonRoomGeometry geometry) {
      private static DungeonLayoutPlanner.GeometryPreparation success(DungeonRoomGeometry geometry) {
         return new DungeonLayoutPlanner.GeometryPreparation(true, "", geometry);
      }

      private static DungeonLayoutPlanner.GeometryPreparation failure(String message) {
         return new DungeonLayoutPlanner.GeometryPreparation(false, message == null ? "Room geometry could not be prepared." : message, null);
      }
   }

   public enum PlanFailure {
      NONE,
      INVALID_ARGUMENT,
      INVALID_DEFINITION,
      MISSING_TEMPLATE,
      NO_LAYOUT,
      REQUIRED_SOCKET_UNUSED,
      OUT_OF_WORLD,
      SAFETY_LIMIT,
      SEARCH_BUDGET_EXHAUSTED;
   }

   public record PlanOptions(DungeonLayoutTopology topology, int targetRoomCount, int criticalPathRooms) {
      public PlanOptions {
         topology = topology == null ? DungeonLayoutTopology.LINEAR : topology;
      }

      public static DungeonLayoutPlanner.PlanOptions linear() {
         return new DungeonLayoutPlanner.PlanOptions(DungeonLayoutTopology.LINEAR, 0, 0);
      }

      public static DungeonLayoutPlanner.PlanOptions branching() {
         return new DungeonLayoutPlanner.PlanOptions(DungeonLayoutTopology.BRANCHING, 0, 0);
      }
   }

   public record PlanResult(
      boolean success, DungeonLayoutPlanner.PlanFailure failure, String message, @Nullable DungeonLayoutPlan layout, DungeonLayoutPreview preview
   ) {
   }

   private record RawSample(
      DungeonLayoutPlanner.DiagnosticCode code,
      String detail,
      @Nullable ResourceLocation candidateRoom,
      @Nullable Rotation rotation,
      @Nullable DungeonTemplatePlacer.WorldBounds bounds,
      int conflictingRoom
   ) {
   }

   public interface RoomCatalog {
      @Nullable
      DungeonRoomDefinition find(ResourceLocation var1);

      long revision();

      static DungeonLayoutPlanner.RoomCatalog snapshot(final DungeonDataSnapshot snapshot) {
         return new DungeonLayoutPlanner.RoomCatalog() {
            @Override
            public DungeonRoomDefinition find(ResourceLocation id) {
               return snapshot.room(id).orElse(null);
            }

            @Override
            public long revision() {
               return snapshot.revision();
            }
         };
      }
   }

   private record RoomChoice(DungeonRoomDefinition definition, int weight) {
   }

   @FunctionalInterface
   public interface RoomTemplateResolver {
      DungeonLayoutPlanner.GeometryPreparation prepare(ServerLevel var1, DungeonRoomDefinition var2, BlockPos var3, Rotation var4);

      static DungeonLayoutPlanner.RoomTemplateResolver loadedTemplates() {
         return (level, room, minimum, rotation) -> {
            DungeonTemplatePlacer.PreparationResult prepared = DungeonTemplatePlacer.prepare(level, room.structure(), minimum, rotation);
            return prepared.success() && prepared.template() != null
               ? DungeonLayoutPlanner.GeometryPreparation.success(DungeonRoomGeometry.loaded(prepared.template()))
               : DungeonLayoutPlanner.GeometryPreparation.failure(prepared.message());
         };
      }

      static DungeonLayoutPlanner.RoomTemplateResolver declaredGeometry() {
         return (level, room, minimum, rotation) -> {
            try {
               return DungeonLayoutPlanner.GeometryPreparation.success(DungeonRoomGeometry.declared(room.structure(), room.size(), minimum, rotation));
            } catch (RuntimeException exception) {
               String message = exception.getMessage();
               return DungeonLayoutPlanner.GeometryPreparation.failure(message == null ? exception.getClass().getSimpleName() : message);
            }
         };
      }
   }

   private static final class SearchContext {
      private final ServerLevel level;
      private final DungeonLayoutPlanner.RoomTemplateResolver resolver;
      private final DungeonDefinition dungeon;
      private final long layoutSeed;
      private final Map<DungeonLayoutPlanner.VariantKey, DungeonLayoutPlanner.GeometryPreparation> zeroVariants = new LinkedHashMap<>();
      private final EnumMap<DungeonLayoutPlanner.DiagnosticCode, Integer> counts = new EnumMap<>(DungeonLayoutPlanner.DiagnosticCode.class);
      private final List<DungeonLayoutPlanner.RawSample> samples = new ArrayList<>();
      @Nullable
      private DungeonLayoutPlanner.SearchState bestState;
      private int nodesVisited;
      private boolean budgetExhausted;

      private SearchContext(
         ServerLevel level,
         DungeonLayoutPlanner.RoomCatalog catalog,
         DungeonLayoutPlanner.RoomTemplateResolver resolver,
         DungeonDefinition dungeon,
         BlockPos origin,
         long rootSeed,
         long layoutSeed,
         DungeonLayoutTopology topology,
         int targetRooms
      ) {
         this.level = level;
         this.resolver = resolver;
         this.dungeon = dungeon;
         this.layoutSeed = layoutSeed;
      }

      private ServerLevel level() {
         return this.level;
      }

      private DungeonDefinition dungeon() {
         return this.dungeon;
      }

      private long layoutSeed() {
         return this.layoutSeed;
      }

      private boolean enterNode() {
         if (this.budgetExhausted) {
            return false;
         }

         this.nodesVisited++;
         if (this.nodesVisited <= 20000) {
            return true;
         }

         this.budgetExhausted = true;
         this.increment(DungeonLayoutPlanner.DiagnosticCode.SEARCH_BUDGET_EXHAUSTED);
         return false;
      }

      private boolean budgetExhausted() {
         return this.budgetExhausted;
      }

      private void observe(DungeonLayoutPlanner.SearchState state) {
         if (this.bestState == null
            || state.rooms().size() > this.bestState.rooms().size()
            || state.rooms().size() == this.bestState.rooms().size() && state.connections().size() > this.bestState.connections().size()) {
            this.bestState = state;
         }
      }

      @Nullable
      private DungeonLayoutPlanner.SearchState bestState() {
         return this.bestState;
      }

      private DungeonLayoutPlanner.GeometryPreparation prepare(DungeonRoomDefinition room, BlockPos minimum, Rotation rotation) {
         return this.resolver.prepare(this.level, room, minimum, rotation);
      }

      private DungeonLayoutPlanner.GeometryPreparation zeroVariant(DungeonRoomDefinition room, Rotation rotation) {
         return this.zeroVariants
            .computeIfAbsent(new DungeonLayoutPlanner.VariantKey(room.id(), rotation), key -> this.prepare(room, BlockPos.ZERO, key.rotation()));
      }

      private void increment(DungeonLayoutPlanner.DiagnosticCode code) {
         this.counts.merge(code, 1, (left, right) -> left == Integer.MAX_VALUE ? left : left + right);
      }

      private void reject(
         DungeonLayoutPlanner.DiagnosticCode code,
         String detail,
         @Nullable ResourceLocation candidateRoom,
         @Nullable Rotation rotation,
         @Nullable DungeonTemplatePlacer.WorldBounds candidateBounds,
         int conflictingRoom
      ) {
         this.increment(code);
         if (this.samples.size() < 16) {
            this.samples.add(new DungeonLayoutPlanner.RawSample(code, detail == null ? "" : detail, candidateRoom, rotation, candidateBounds, conflictingRoom));
         }
      }

      private void countCappedOptionalSockets(DungeonLayoutPlanner.SearchState state) {
         int capped = 0;

         for (DungeonLayoutPlan.PlannedRoom room : state.rooms()) {
            if (!room.preset()) {
               for (DungeonDataTypes.Socket socket : room.definition().sockets()) {
                  if (!socket.required() && !room.usedSockets().contains(socket.id())) {
                     capped++;
                  }
               }
            }
         }

         if (capped > 0) {
            this.counts.merge(DungeonLayoutPlanner.DiagnosticCode.OPTIONAL_SOCKET_CAPPED, capped, Integer::sum);
         }
      }

      private DungeonLayoutPlanner.PlanFailure failure() {
         if (this.budgetExhausted) {
            return DungeonLayoutPlanner.PlanFailure.SEARCH_BUDGET_EXHAUSTED;
         } else if (this.counts.containsKey(DungeonLayoutPlanner.DiagnosticCode.REQUIRED_SOCKET_UNUSED)) {
            return DungeonLayoutPlanner.PlanFailure.REQUIRED_SOCKET_UNUSED;
         } else if (this.bestState == null && this.counts.containsKey(DungeonLayoutPlanner.DiagnosticCode.MISSING_TEMPLATE)) {
            return DungeonLayoutPlanner.PlanFailure.MISSING_TEMPLATE;
         } else if (this.bestState == null && this.counts.containsKey(DungeonLayoutPlanner.DiagnosticCode.SIZE_MISMATCH)) {
            return DungeonLayoutPlanner.PlanFailure.INVALID_DEFINITION;
         } else if (this.bestState == null && this.counts.containsKey(DungeonLayoutPlanner.DiagnosticCode.OUT_OF_BUILD_HEIGHT)) {
            return DungeonLayoutPlanner.PlanFailure.OUT_OF_WORLD;
         } else {
            return this.counts.containsKey(DungeonLayoutPlanner.DiagnosticCode.TOTAL_VOLUME_LIMIT)
               ? DungeonLayoutPlanner.PlanFailure.SAFETY_LIMIT
               : DungeonLayoutPlanner.PlanFailure.NO_LAYOUT;
         }
      }

      private String failureMessage(DungeonLayoutTopology requestedTopology) {
         if (this.budgetExhausted) {
            return "Layout search exhausted its deterministic 20000-node safety budget. Reduce room count or room/socket variety.";
         } else if (requestedTopology == DungeonLayoutTopology.BRANCHING
            && this.counts.containsKey(DungeonLayoutPlanner.DiagnosticCode.BRANCH_SOCKET_UNAVAILABLE)) {
            return "Could not build the requested branching layout. Add optional side sockets and one-socket dead-end rooms that can cap branches.";
         } else if (this.counts.containsKey(DungeonLayoutPlanner.DiagnosticCode.REQUIRED_SOCKET_UNUSED)) {
            return "No complete layout connected every required socket.";
         } else {
            return this.bestState != null && !this.bestState.rooms().isEmpty()
               ? "Could not complete a non-overlapping start-to-boss layout. Inspect simulation diagnostics for socket, collision, and depth rejections."
               : "No valid start-room template and rotation could be prepared.";
         }
      }

      private DungeonLayoutPreview.Diagnostics diagnostics(BlockPos previewOrigin) {
         List<DungeonLayoutPreview.DiagnosticCount> countViews = this.counts
            .entrySet()
            .stream()
            .sorted(Entry.comparingByKey())
            .map(entry -> new DungeonLayoutPreview.DiagnosticCount(entry.getKey(), entry.getValue(), DungeonLayoutPlanner.description(entry.getKey())))
            .toList();
         List<DungeonLayoutPreview.DiagnosticSample> sampleViews = this.samples
            .stream()
            .map(
               sample -> new DungeonLayoutPreview.DiagnosticSample(
                  sample.code(),
                  sample.detail(),
                  sample.candidateRoom(),
                  sample.rotation(),
                  sample.bounds() == null ? null : DungeonLayoutPlanner.relativeBounds(sample.bounds(), previewOrigin),
                  sample.conflictingRoom()
               )
            )
            .toList();
         return new DungeonLayoutPreview.Diagnostics(this.nodesVisited, this.budgetExhausted, countViews, sampleViews);
      }
   }

   private static final class SearchState {
      private final List<DungeonLayoutPlan.PlannedRoom> rooms;
      private final List<DungeonLayoutPlan.PlannedConnection> connections;
      private final long totalVolume;

      private SearchState(List<DungeonLayoutPlan.PlannedRoom> rooms, List<DungeonLayoutPlan.PlannedConnection> connections, long totalVolume) {
         this.rooms = List.copyOf(rooms);
         this.connections = List.copyOf(connections);
         this.totalVolume = totalVolume;
      }

      private List<DungeonLayoutPlan.PlannedRoom> rooms() {
         return this.rooms;
      }

      private List<DungeonLayoutPlan.PlannedConnection> connections() {
         return this.connections;
      }

      private long totalVolume() {
         return this.totalVolume;
      }

      private DungeonLayoutPlanner.SearchState apply(DungeonLayoutPlanner.Attachment attachment) {
         List<DungeonLayoutPlan.PlannedRoom> nextRooms = new ArrayList<>(this.rooms);
         int sourceIndex = attachment.source().room().index();
         nextRooms.set(sourceIndex, nextRooms.get(sourceIndex).withUsedSocket(attachment.source().socket().id()));
         nextRooms.add(attachment.room());
         List<DungeonLayoutPlan.PlannedConnection> nextConnections = new ArrayList<>(this.connections);
         nextConnections.add(attachment.connection());
         return new DungeonLayoutPlanner.SearchState(nextRooms, nextConnections, attachment.totalVolume());
      }

      private DungeonLayoutPlan toPlan() {
         return new DungeonLayoutPlan(this.rooms, this.connections);
      }
   }

   private record SourceSocket(DungeonLayoutPlan.PlannedRoom room, DungeonDataTypes.Socket socket, DungeonTemplatePlacer.TransformedSocket world) {
   }

   private record VariantKey(ResourceLocation room, Rotation rotation) {
   }
}
