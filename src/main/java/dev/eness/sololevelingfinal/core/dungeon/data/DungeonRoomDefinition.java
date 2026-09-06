package dev.eness.sololevelingfinal.core.dungeon.data;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record DungeonRoomDefinition(
   ResourceLocation id,
   int formatVersion,
   ResourceLocation structure,
   DungeonDataTypes.RoomRole role,
   int weight,
   DungeonDataTypes.Int3 size,
   DungeonDataTypes.Int3 origin,
   Optional<ResourceLocation> defaultMobPool,
   Optional<DungeonDataTypes.ShellSettings> shellOverride,
   List<DungeonDataTypes.Region> regions,
   List<DungeonDataTypes.Socket> sockets,
   List<DungeonDataTypes.Marker> markers,
   List<DungeonDataTypes.Encounter> encounters
) {
   public DungeonRoomDefinition {
      defaultMobPool = defaultMobPool == null ? Optional.empty() : defaultMobPool;
      shellOverride = shellOverride == null ? Optional.empty() : shellOverride;
      regions = List.copyOf(regions);
      sockets = List.copyOf(sockets);
      markers = List.copyOf(markers);
      encounters = List.copyOf(encounters);
   }
}
