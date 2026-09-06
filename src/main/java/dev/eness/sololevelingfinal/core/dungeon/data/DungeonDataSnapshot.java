package dev.eness.sololevelingfinal.core.dungeon.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;

public record DungeonDataSnapshot(
   long revision,
   Map<ResourceLocation, DungeonRoomDefinition> rooms,
   Map<ResourceLocation, DungeonDefinition> dungeons,
   Map<ResourceLocation, MobPoolDefinition> mobPools,
   List<DungeonDataTypes.ValidationIssue> issues
) {
   private static final DungeonDataSnapshot EMPTY = new DungeonDataSnapshot(0L, Map.of(), Map.of(), Map.of(), List.of());

   public DungeonDataSnapshot {
      rooms = immutableSortedMap(rooms);
      dungeons = immutableSortedMap(dungeons);
      mobPools = immutableSortedMap(mobPools);
      issues = List.copyOf(issues);
   }

   public static DungeonDataSnapshot empty() {
      return EMPTY;
   }

   DungeonDataSnapshot withRevision(long value) {
      return new DungeonDataSnapshot(value, this.rooms, this.dungeons, this.mobPools, this.issues);
   }

   public Optional<DungeonRoomDefinition> room(ResourceLocation id) {
      return Optional.ofNullable(this.rooms.get(id));
   }

   public Optional<DungeonDefinition> dungeon(ResourceLocation id) {
      return Optional.ofNullable(this.dungeons.get(id));
   }

   public Optional<MobPoolDefinition> mobPool(ResourceLocation id) {
      return Optional.ofNullable(this.mobPools.get(id));
   }

   public List<ResourceLocation> roomIds() {
      return List.copyOf(this.rooms.keySet());
   }

   public List<ResourceLocation> dungeonIds() {
      return List.copyOf(this.dungeons.keySet());
   }

   public List<ResourceLocation> mobPoolIds() {
      return List.copyOf(this.mobPools.keySet());
   }

   private static <T> Map<ResourceLocation, T> immutableSortedMap(Map<ResourceLocation, T> input) {
      List<Entry<ResourceLocation, T>> entries = new ArrayList<>(input.entrySet());
      entries.sort(Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)));
      LinkedHashMap<ResourceLocation, T> sorted = new LinkedHashMap<>();
      entries.forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
      return Collections.unmodifiableMap(sorted);
   }
}
