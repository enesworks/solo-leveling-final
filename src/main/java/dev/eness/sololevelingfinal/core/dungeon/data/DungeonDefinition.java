package dev.eness.sololevelingfinal.core.dungeon.data;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;

public record DungeonDefinition(
   ResourceLocation id,
   int formatVersion,
   DungeonDataTypes.DungeonKind kind,
   Set<ProceduralDungeonRank> allowedRanks,
   Optional<ResourceLocation> structure,
   Optional<DungeonDataTypes.Int3> size,
   Optional<DungeonDataTypes.Int3> origin,
   Optional<ResourceLocation> defaultMobPool,
   Optional<ResourceLocation> bossMobPool,
   Map<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> roomPools,
   DungeonDataTypes.IntRange roomCount,
   int maxDepth,
   DungeonDataTypes.LevelRule level,
   DungeonDataTypes.ShellSettings shell,
   List<DungeonDataTypes.Region> regions,
   List<DungeonDataTypes.Socket> sockets,
   List<DungeonDataTypes.Marker> markers,
   List<DungeonDataTypes.Encounter> encounters,
   List<DungeonDataTypes.FixedRoomPlacement> fixedPlacements,
   List<DungeonDataTypes.FixedRoomConnection> fixedConnections,
   DungeonDataTypes.DungeonTopology topology
) {
   public DungeonDefinition {
      allowedRanks = Set.copyOf(allowedRanks);
      if (allowedRanks.isEmpty()) {
         throw new IllegalArgumentException("A dungeon must allow at least one gate rank.");
      }

      structure = structure == null ? Optional.empty() : structure;
      size = size == null ? Optional.empty() : size;
      origin = origin == null ? Optional.empty() : origin;
      defaultMobPool = defaultMobPool == null ? Optional.empty() : defaultMobPool;
      bossMobPool = bossMobPool == null ? Optional.empty() : bossMobPool;
      EnumMap<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> copiedPools = new EnumMap<>(DungeonDataTypes.RoomRole.class);
      roomPools.forEach((role, rooms) -> copiedPools.put(role, List.copyOf((Collection<? extends DungeonDataTypes.WeightedRoom>)rooms)));
      roomPools = Collections.unmodifiableMap(copiedPools);
      regions = List.copyOf(regions);
      sockets = List.copyOf(sockets);
      markers = List.copyOf(markers);
      encounters = List.copyOf(encounters);
      fixedPlacements = fixedPlacements == null ? List.of() : List.copyOf(fixedPlacements);
      fixedConnections = fixedConnections == null ? List.of() : List.copyOf(fixedConnections);
      topology = topology == null ? DungeonDataTypes.DungeonTopology.LINEAR : topology;
   }

   public DungeonDefinition(
      ResourceLocation id,
      int formatVersion,
      DungeonDataTypes.DungeonKind kind,
      Set<ProceduralDungeonRank> allowedRanks,
      Optional<ResourceLocation> structure,
      Optional<DungeonDataTypes.Int3> size,
      Optional<DungeonDataTypes.Int3> origin,
      Optional<ResourceLocation> defaultMobPool,
      Optional<ResourceLocation> bossMobPool,
      Map<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> roomPools,
      DungeonDataTypes.IntRange roomCount,
      int maxDepth,
      DungeonDataTypes.LevelRule level,
      DungeonDataTypes.ShellSettings shell,
      List<DungeonDataTypes.Region> regions,
      List<DungeonDataTypes.Socket> sockets,
      List<DungeonDataTypes.Marker> markers,
      List<DungeonDataTypes.Encounter> encounters,
      List<DungeonDataTypes.FixedRoomPlacement> fixedPlacements,
      List<DungeonDataTypes.FixedRoomConnection> fixedConnections
   ) {
      this(
         id,
         formatVersion,
         kind,
         allowedRanks,
         structure,
         size,
         origin,
         defaultMobPool,
         bossMobPool,
         roomPools,
         roomCount,
         maxDepth,
         level,
         shell,
         regions,
         sockets,
         markers,
         encounters,
         fixedPlacements,
         fixedConnections,
         DungeonDataTypes.DungeonTopology.LINEAR
      );
   }

   public DungeonDefinition(
      ResourceLocation id,
      int formatVersion,
      DungeonDataTypes.DungeonKind kind,
      Set<ProceduralDungeonRank> allowedRanks,
      Optional<ResourceLocation> structure,
      Optional<DungeonDataTypes.Int3> size,
      Optional<DungeonDataTypes.Int3> origin,
      Optional<ResourceLocation> defaultMobPool,
      Optional<ResourceLocation> bossMobPool,
      Map<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> roomPools,
      DungeonDataTypes.IntRange roomCount,
      int maxDepth,
      DungeonDataTypes.LevelRule level,
      DungeonDataTypes.ShellSettings shell,
      List<DungeonDataTypes.Region> regions,
      List<DungeonDataTypes.Socket> sockets,
      List<DungeonDataTypes.Marker> markers,
      List<DungeonDataTypes.Encounter> encounters
   ) {
      this(
         id,
         formatVersion,
         kind,
         allowedRanks,
         structure,
         size,
         origin,
         defaultMobPool,
         bossMobPool,
         roomPools,
         roomCount,
         maxDepth,
         level,
         shell,
         regions,
         sockets,
         markers,
         encounters,
         List.of(),
         List.of(),
         DungeonDataTypes.DungeonTopology.LINEAR
      );
   }

   public List<DungeonDataTypes.WeightedRoom> rooms(DungeonDataTypes.RoomRole role) {
      return this.roomPools.getOrDefault(role, List.of());
   }

   public boolean supportsRank(ProceduralDungeonRank rank) {
      return rank != null && this.allowedRanks.contains(rank);
   }
}
