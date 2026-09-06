package dev.eness.sololevelingfinal.core.dungeon.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;

public final class DungeonDataManager {
   private static final int MAX_DUNGEON_LEVEL = 1000;
   private static final int MAX_BASE_XP = 1000000;
   private static final int MAX_PROCEDURAL_ROOMS = 64;
   private static final int MAX_MARKERS_PER_WAVE = 256;
   private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
   private static final String ROOM_DIRECTORY = "slr/rooms";
   private static final String DUNGEON_DIRECTORY = "slr/dungeons";
   private static final String POOL_DIRECTORY = "slr/mob_pools";
   private static final String MODIFIER_DIRECTORY = "slr/pool_modifiers";
   private static final String LEGACY_MODIFIER_DIRECTORY = "slr/mob_pool_modifiers";
   private static final ResourceLocation BEDROCK_ID = new ResourceLocation("minecraft", "bedrock");
   private static volatile DungeonDataSnapshot current = DungeonDataSnapshot.empty();

   private DungeonDataManager() {
   }

   public static DungeonDataSnapshot snapshot() {
      return current;
   }

   public static Optional<DungeonRoomDefinition> room(ResourceLocation id) {
      return current.room(id);
   }

   public static Optional<DungeonDefinition> dungeon(ResourceLocation id) {
      return current.dungeon(id);
   }

   public static Optional<MobPoolDefinition> mobPool(ResourceLocation id) {
      return current.mobPool(id);
   }

   public static List<ResourceLocation> roomIds() {
      return current.roomIds();
   }

   public static List<ResourceLocation> dungeonIds() {
      return current.dungeonIds();
   }

   public static List<ResourceLocation> mobPoolIds() {
      return current.mobPoolIds();
   }

   static PreparableReloadListener reloadListener(IContext conditionContext) {
      return new DungeonDataManager.Loader(conditionContext);
   }

   static void clear() {
      current = DungeonDataSnapshot.empty();
   }

   private static Map<ResourceLocation, JsonElement> scan(ResourceManager manager, String directory) {
      Map<ResourceLocation, JsonElement> values = new HashMap<>();
      SimpleJsonResourceReloadListener.scanDirectory(manager, directory, GSON, values);
      return values;
   }

   private static Map<ResourceLocation, JsonElement> modifiers(ResourceManager manager, List<DungeonDataTypes.ValidationIssue> issues) {
      Map<ResourceLocation, JsonElement> legacy = scan(manager, "slr/mob_pool_modifiers");
      Map<ResourceLocation, JsonElement> canonical = scan(manager, "slr/pool_modifiers");
      Map<ResourceLocation, JsonElement> combined = new HashMap<>(legacy);

      for (Entry<ResourceLocation, JsonElement> entry : canonical.entrySet()) {
         if (combined.put(entry.getKey(), entry.getValue()) != null) {
            issues.add(
               warning(entry.getKey(), "Modifier exists in both slr/pool_modifiers and the legacy slr/mob_pool_modifiers directory; the canonical file won.")
            );
         }
      }

      return combined;
   }

   private static <T> Map<ResourceLocation, T> parseResources(
      Map<ResourceLocation, JsonElement> resources, DungeonDataManager.ResourceParser<T> parser, List<DungeonDataTypes.ValidationIssue> issues
   ) {
      Map<ResourceLocation, T> parsed = new LinkedHashMap<>();
      resources.entrySet().stream().sorted(Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString))).forEach(entry -> {
         try {
            parser.parse(entry.getKey(), entry.getValue()).ifPresent(value -> parsed.put((ResourceLocation)entry.getKey(), value));
         } catch (RuntimeException exception) {
            issues.add(error(entry.getKey(), cleanMessage(exception)));
         }
      });
      return parsed;
   }

   private static Optional<MobPoolDefinition> parsePool(
      ResourceLocation id, JsonElement element, IContext conditions, List<DungeonDataTypes.ValidationIssue> issues
   ) {
      JsonObject json = object(element, "mob pool");
      if (!conditionsApply(json, conditions)) {
         return Optional.empty();
      }

      int format = formatVersion(json);
      JsonArray entriesJson = array(json, "entries", new JsonArray());
      List<DungeonDataTypes.MobPoolEntry> entries = new ArrayList<>();

      for (JsonElement entryElement : entriesJson) {
         JsonObject entryJson = object(entryElement, "mob pool entry");
         parsePoolEntry(id, entryJson, conditions, issues).ifPresent(entries::add);
      }

      return Optional.of(new MobPoolDefinition(id, format, entries));
   }

   private static Optional<DungeonDataTypes.MobPoolModifier> parseModifier(
      ResourceLocation id, JsonElement element, IContext conditions, List<DungeonDataTypes.ValidationIssue> issues
   ) {
      JsonObject json = object(element, "mob pool modifier");
      if (!conditionsApply(json, conditions)) {
         return Optional.empty();
      }

      formatVersion(json);
      ResourceLocation target = internalId(requiredString(json, "target"), id.getNamespace());
      String operationName = string(json, "operation", "add").toLowerCase(Locale.ROOT);

      DungeonDataTypes.ModifierOperation operation = switch (operationName) {
         case "add", "append" -> DungeonDataTypes.ModifierOperation.ADD;
         case "remove" -> DungeonDataTypes.ModifierOperation.REMOVE;
         default -> throw new JsonParseException("operation must be add or remove");
      };
      List<DungeonDataTypes.MobPoolEntry> entries = new ArrayList<>();
      List<DungeonDataTypes.EntitySelector> selectors = new ArrayList<>();
      JsonArray values = array(json, operation == DungeonDataTypes.ModifierOperation.ADD ? "entries" : "selectors", null);
      if (values == null && operation == DungeonDataTypes.ModifierOperation.REMOVE) {
         values = array(json, "remove", null);
      }

      if (values == null && operation == DungeonDataTypes.ModifierOperation.REMOVE) {
         values = array(json, "entries", new JsonArray());
      }

      if (values == null) {
         values = new JsonArray();
      }

      for (JsonElement value : values) {
         if (operation == DungeonDataTypes.ModifierOperation.ADD) {
            JsonObject entryJson = object(value, "modifier entry");
            parsePoolEntry(id, entryJson, conditions, issues).ifPresent(entries::add);
         } else {
            selectors.add(parseSelector(value, id.getNamespace()));
         }
      }

      if (operation == DungeonDataTypes.ModifierOperation.ADD && entries.isEmpty()) {
         issues.add(warning(id, "Add modifier has no active entries."));
      }

      if (operation == DungeonDataTypes.ModifierOperation.REMOVE && selectors.isEmpty()) {
         issues.add(warning(id, "Remove modifier has no selectors."));
      }

      return Optional.of(new DungeonDataTypes.MobPoolModifier(id, target, operation, entries, selectors));
   }

   private static Optional<DungeonDataTypes.MobPoolEntry> parsePoolEntry(
      ResourceLocation owner, JsonObject json, IContext conditions, List<DungeonDataTypes.ValidationIssue> issues
   ) {
      if (!conditionsApply(json, conditions)) {
         return Optional.empty();
      }

      Optional<String> requiredMod = optionalString(json, "required_mod");
      if (requiredMod.isPresent()) {
         String modId = requiredMod.get();
         if (!modId.matches("[a-z][a-z0-9_-]{1,63}")) {
            throw new JsonParseException("required_mod is not a valid mod id: " + modId);
         }

         if (!ModList.get().isLoaded(modId)) {
            return Optional.empty();
         }
      }

      DungeonDataTypes.EntitySelector selector = parseSelector(json, owner.getNamespace());
      if (selector.kind() == DungeonDataTypes.SelectorKind.ENTITY && !ForgeRegistries.ENTITY_TYPES.containsKey(selector.id())) {
         issues.add(
            warning(
               owner,
               "Skipped missing entity " + selector.id() + requiredMod.<String>map(mod -> " although required_mod " + mod + " is loaded").orElse("") + "."
            )
         );
         return Optional.empty();
      }

      if (selector.kind() == DungeonDataTypes.SelectorKind.ENTITY && ForgeRegistries.ENTITY_TYPES.getValue(selector.id()) == EntityType.PLAYER) {
         issues.add(warning(owner, "Skipped minecraft:player because it cannot be spawned as a dungeon mob."));
         return Optional.empty();
      }

      int weight = boundedInt(json, "weight", 1, 1, 1000000);
      Optional<DungeonDataTypes.IntRange> eligible = optionalRange(json, "eligible_level", 0, 1000);
      Optional<DungeonDataTypes.IntRange> spawn = optionalRange(json, "spawn_level", 1, 1000);
      if (spawn.isEmpty()) {
         spawn = optionalRange(json, "level_range", 1, 1000);
      }

      if (json.has("xp") && json.has("base_xp")) {
         throw new JsonParseException("Mob pool entries must use either xp or base_xp, not both");
      }

      Optional<Integer> baseXp = json.has("xp")
         ? Optional.of(boundedInt(json, "xp", 0, 0, 1000000))
         : (json.has("base_xp") ? Optional.of(boundedInt(json, "base_xp", 0, 0, 1000000)) : Optional.empty());
      return Optional.of(new DungeonDataTypes.MobPoolEntry(selector, weight, requiredMod, eligible, spawn, baseXp));
   }

   private static Map<ResourceLocation, MobPoolDefinition> applyModifiers(
      Map<ResourceLocation, MobPoolDefinition> original, List<DungeonDataTypes.MobPoolModifier> modifiers, List<DungeonDataTypes.ValidationIssue> issues
   ) {
      Map<ResourceLocation, MobPoolDefinition> result = new LinkedHashMap<>(original);

      for (DungeonDataTypes.MobPoolModifier modifier : modifiers) {
         MobPoolDefinition target = result.get(modifier.target());
         if (target == null) {
            issues.add(error(modifier.id(), "Target mob pool " + modifier.target() + " does not exist."));
         } else {
            List<DungeonDataTypes.MobPoolEntry> entries = new ArrayList<>(target.entries());
            if (modifier.operation() == DungeonDataTypes.ModifierOperation.ADD) {
               entries.addAll(modifier.entries());
            } else {
               Set<String> removedKeys = modifier.selectors().stream().map(DungeonDataTypes.EntitySelector::key).collect(Collectors.toSet());
               entries.removeIf(entry -> removedKeys.contains(entry.selector().key()));
            }

            result.put(target.id(), new MobPoolDefinition(target.id(), target.formatVersion(), entries));
         }
      }

      return result;
   }

   private static Optional<DungeonRoomDefinition> parseRoom(ResourceLocation id, JsonElement element, IContext conditions) {
      JsonObject json = object(element, "room");
      if (!conditionsApply(json, conditions)) {
         return Optional.empty();
      }

      int format = formatVersion(json);
      ResourceLocation structure = internalId(requiredString(json, "structure"), id.getNamespace());
      DungeonDataTypes.Int3 size = vector(required(json, "size"), "size");
      DungeonDataTypes.Int3 origin = json.has("origin") ? vector(json.get("origin"), "origin") : new DungeonDataTypes.Int3(0, 0, 0);
      Optional<ResourceLocation> defaultPool = optionalInternalId(json, "default_mob_pool", id.getNamespace());
      List<DungeonDataTypes.Region> regions = parseRegions(json);
      List<DungeonDataTypes.Socket> sockets = parseSockets(json);
      List<DungeonDataTypes.Marker> markers = parseMarkers(json);
      String roleName = string(json, "role", "");
      DungeonDataTypes.RoomRole role = roleName.isBlank() ? inferredRole(markers) : strictRoomRole(roleName, "room role");
      int weight = boundedInt(json, "weight", 1, 1, 1000000);
      Optional<DungeonDataTypes.ShellSettings> shell = json.has("shell") ? Optional.of(parseShell(json.get("shell"), id)) : Optional.empty();
      List<DungeonDataTypes.Encounter> encounters = parseEncounters(json, id, defaultPool, Optional.empty());
      return Optional.of(
         new DungeonRoomDefinition(id, format, structure, role, weight, size, origin, defaultPool, shell, regions, sockets, markers, encounters)
      );
   }

   private static Optional<DungeonDefinition> parseDungeon(ResourceLocation id, JsonElement element, IContext conditions) {
      JsonObject json = object(element, "dungeon");
      if (!conditionsApply(json, conditions)) {
         return Optional.empty();
      }

      int format = formatVersion(json);
      String type = string(json, "generation", string(json, "type", "preset"));
      DungeonDataTypes.DungeonKind kind = strictDungeonKind(type);
      DungeonDataTypes.DungeonTopology topology = parseDungeonTopology(json, kind);
      Set<ProceduralDungeonRank> allowedRanks = parseAllowedRanks(json);
      Optional<ResourceLocation> structure = optionalInternalId(json, "structure", id.getNamespace());
      Optional<DungeonDataTypes.Int3> size = json.has("size") ? Optional.of(vector(json.get("size"), "size")) : Optional.empty();
      Optional<DungeonDataTypes.Int3> origin = json.has("origin") ? Optional.of(vector(json.get("origin"), "origin")) : Optional.empty();
      Optional<ResourceLocation> defaultPool = optionalInternalId(json, "default_mob_pool", id.getNamespace());
      Optional<ResourceLocation> bossPool = optionalInternalId(json, "boss_mob_pool", id.getNamespace());
      Map<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> roomPools = parseRoomPools(json, id);
      List<DungeonDataTypes.FixedRoomPlacement> fixedPlacements = parseFixedPlacements(json, id);
      List<DungeonDataTypes.FixedRoomConnection> fixedConnections = parseFixedConnections(json);
      DungeonDataTypes.IntRange roomCount = kind == DungeonDataTypes.DungeonKind.PRESET
         ? new DungeonDataTypes.IntRange(1, 1)
         : (
            kind == DungeonDataTypes.DungeonKind.FIXED
               ? new DungeonDataTypes.IntRange(Math.max(1, fixedPlacements.size()), Math.max(1, fixedPlacements.size()))
               : parseRoomCount(json)
         );
      int maxDepth = boundedInt(json, "max_depth", roomCount.max(), 1, 64);
      DungeonDataTypes.LevelRule level = parseLevelRule(json);
      DungeonDataTypes.ShellSettings shell = json.has("shell")
         ? parseShell(json.get("shell"), id)
         : new DungeonDataTypes.ShellSettings(true, BEDROCK_ID, 1, true, true);
      List<DungeonDataTypes.Region> regions = parseRegions(json);
      List<DungeonDataTypes.Socket> sockets = parseSockets(json);
      List<DungeonDataTypes.Marker> markers = parseMarkers(json);
      List<DungeonDataTypes.Encounter> encounters = parseEncounters(json, id, defaultPool, bossPool);
      return Optional.of(
         new DungeonDefinition(
            id,
            format,
            kind,
            allowedRanks,
            structure,
            size,
            origin,
            defaultPool,
            bossPool,
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
            topology
         )
      );
   }

   private static Set<ProceduralDungeonRank> parseAllowedRanks(JsonObject json) {
      if (json.has("rank") && json.has("ranks")) {
         throw new JsonParseException("Use either rank or ranks, not both");
      }

      if (!json.has("rank") && !json.has("ranks")) {
         return EnumSet.allOf(ProceduralDungeonRank.class);
      }

      JsonElement element = json.has("ranks") ? json.get("ranks") : json.get("rank");
      JsonArray values = element.isJsonArray() ? element.getAsJsonArray() : singletonArray(element);
      if (values.isEmpty()) {
         throw new JsonParseException("ranks must contain at least one of E, D, C, B, A, or S");
      }

      EnumSet<ProceduralDungeonRank> result = EnumSet.noneOf(ProceduralDungeonRank.class);

      for (JsonElement value : values) {
         if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("Dungeon ranks must be strings such as \"A\"");
         }

         String name = value.getAsString();
         if (name.equalsIgnoreCase("all")) {
            if (values.size() != 1) {
               throw new JsonParseException("The rank value all cannot be combined with individual ranks");
            }

            return EnumSet.allOf(ProceduralDungeonRank.class);
         }

         ProceduralDungeonRank rank = ProceduralDungeonRank.tryParse(name)
            .orElseThrow(() -> new JsonParseException("Unknown dungeon rank " + name + ". Expected E, D, C, B, A, or S"));
         if (!result.add(rank)) {
            throw new JsonParseException("Duplicate dungeon rank " + rank.name());
         }
      }

      return result;
   }

   private static Map<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> parseRoomPools(JsonObject json, ResourceLocation owner) {
      EnumMap<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> result = new EnumMap<>(DungeonDataTypes.RoomRole.class);
      JsonObject pools = null;
      if (json.has("room_pools")) {
         pools = object(json.get("room_pools"), "room_pools");
      } else if (json.has("rooms") && json.get("rooms").isJsonObject()) {
         pools = json.getAsJsonObject("rooms");
      }

      if (pools != null) {
         for (Entry<String, JsonElement> entry : pools.entrySet()) {
            DungeonDataTypes.RoomRole role = strictRoomRole(entry.getKey(), "room pool role");
            result.computeIfAbsent(role, ignored -> new ArrayList<>()).addAll(parseWeightedRooms(entry.getValue(), owner));
         }
      }

      Map<DungeonDataTypes.RoomRole, List<String>> aliases = Map.of(
         DungeonDataTypes.RoomRole.START,
         List.of("start_room", "start_rooms"),
         DungeonDataTypes.RoomRole.NORMAL,
         List.of("room_pool", "normal_rooms"),
         DungeonDataTypes.RoomRole.BOSS,
         List.of("boss_room", "boss_rooms"),
         DungeonDataTypes.RoomRole.CAP,
         List.of("cap_rooms"),
         DungeonDataTypes.RoomRole.TREASURE,
         List.of("treasure_rooms"),
         DungeonDataTypes.RoomRole.JUNCTION,
         List.of("junction_rooms"),
         DungeonDataTypes.RoomRole.DEAD_END,
         List.of("dead_end_rooms"),
         DungeonDataTypes.RoomRole.CORRIDOR,
         List.of("corridor_rooms"),
         DungeonDataTypes.RoomRole.STAIR,
         List.of("stair_rooms")
      );
      aliases.forEach((rolex, keys) -> keys.forEach(key -> {
         if (json.has(key)) {
            result.computeIfAbsent(rolex, ignored -> new ArrayList<>()).addAll(parseWeightedRooms(json.get(key), owner));
         }
      }));
      return result;
   }

   private static List<DungeonDataTypes.WeightedRoom> parseWeightedRooms(JsonElement element, ResourceLocation owner) {
      JsonArray values = element.isJsonArray() ? element.getAsJsonArray() : singletonArray(element);
      List<DungeonDataTypes.WeightedRoom> result = new ArrayList<>();

      for (JsonElement value : values) {
         if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            result.add(new DungeonDataTypes.WeightedRoom(internalId(value.getAsString(), owner.getNamespace()), 1));
         } else {
            JsonObject room = object(value, "weighted room");
            String roomId = room.has("room") ? requiredString(room, "room") : requiredString(room, "id");
            result.add(new DungeonDataTypes.WeightedRoom(internalId(roomId, owner.getNamespace()), boundedInt(room, "weight", 1, 1, 1000000)));
         }
      }

      return result;
   }

   private static List<DungeonDataTypes.FixedRoomPlacement> parseFixedPlacements(JsonObject owner, ResourceLocation resource) {
      JsonArray values = array(owner, "placements", new JsonArray());
      List<DungeonDataTypes.FixedRoomPlacement> result = new ArrayList<>();

      for (JsonElement value : values) {
         JsonObject json = object(value, "fixed placement");
         String id = localId(requiredString(json, "id"), "fixed placement id");
         ResourceLocation room = internalId(requiredString(json, "room"), resource.getNamespace());
         DungeonDataTypes.Int3 position = vector(required(json, "position"), "fixed placement position");
         Rotation rotation = fixedRotation(required(json, "rotation"));
         result.add(new DungeonDataTypes.FixedRoomPlacement(id, room, position, rotation));
      }

      return result;
   }

   private static List<DungeonDataTypes.FixedRoomConnection> parseFixedConnections(JsonObject owner) {
      JsonArray values = array(owner, "connections", new JsonArray());
      List<DungeonDataTypes.FixedRoomConnection> result = new ArrayList<>();

      for (JsonElement value : values) {
         JsonObject json = object(value, "fixed connection");
         JsonObject from = object(required(json, "from"), "fixed connection from");
         JsonObject to = object(required(json, "to"), "fixed connection to");
         result.add(
            new DungeonDataTypes.FixedRoomConnection(
               localId(requiredString(from, "room"), "fixed connection room id"),
               localId(requiredString(from, "socket"), "fixed connection socket id"),
               localId(requiredString(to, "room"), "fixed connection room id"),
               localId(requiredString(to, "socket"), "fixed connection socket id")
            )
         );
      }

      return result;
   }

   private static Rotation fixedRotation(JsonElement element) {
      String value;
      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
         value = Integer.toString(element.getAsInt());
      } else {
         if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("fixed placement rotation must be 0, 90, 180, or 270");
         }

         value = element.getAsString().toLowerCase(Locale.ROOT);
      }
      return switch (value) {
         case "0", "none" -> Rotation.NONE;
         case "90", "clockwise_90" -> Rotation.CLOCKWISE_90;
         case "180", "clockwise_180" -> Rotation.CLOCKWISE_180;
         case "270", "-90", "counterclockwise_90" -> Rotation.COUNTERCLOCKWISE_90;
         default -> throw new JsonParseException("fixed placement rotation must be 0, 90, 180, or 270");
      };
   }

   private static List<DungeonDataTypes.Region> parseRegions(JsonObject owner) {
      JsonArray values = array(owner, "regions", new JsonArray());
      List<DungeonDataTypes.Region> result = new ArrayList<>();
      int index = 0;

      for (JsonElement value : values) {
         JsonObject json = object(value, "region");
         String type = string(json, "type", "region");
         String id = string(json, "id", type + "_" + ++index);
         result.add(
            new DungeonDataTypes.Region(
               localId(id, "region id"),
               type,
               new DungeonDataTypes.Bounds3(vector(required(json, "min"), "region min"), vector(required(json, "max"), "region max"))
            )
         );
      }

      return result;
   }

   private static List<DungeonDataTypes.Socket> parseSockets(JsonObject owner) {
      JsonArray values = array(owner, "sockets", new JsonArray());
      List<DungeonDataTypes.Socket> result = new ArrayList<>();
      int index = 0;

      for (JsonElement value : values) {
         JsonObject json = object(value, "socket");
         String type = string(json, "type", "corridor");
         String id = string(json, "id", type + "_socket_" + ++index);
         Direction facing = Direction.byName(requiredString(json, "facing").toLowerCase(Locale.ROOT));
         if (facing == null) {
            throw new JsonParseException("Unknown socket facing in " + id);
         }

         result.add(
            new DungeonDataTypes.Socket(
               localId(id, "socket id"),
               type,
               new DungeonDataTypes.Bounds3(vector(required(json, "min"), "socket min"), vector(required(json, "max"), "socket max")),
               facing,
               bool(json, "required", false),
               boundedInt(json, "carve_depth", 1, 1, 16)
            )
         );
      }

      return result;
   }

   private static List<DungeonDataTypes.Marker> parseMarkers(JsonObject owner) {
      JsonArray values = array(owner, "markers", new JsonArray());
      List<DungeonDataTypes.Marker> result = new ArrayList<>();
      int index = 0;

      for (JsonElement value : values) {
         JsonObject json = object(value, "marker");
         String type = string(json, "type", "marker");
         String id = string(json, "id", type + "_" + ++index);
         String group = optionalString(json, "group").orElse("");
         if (!group.isBlank()) {
            group = localId(group, "marker group");
         }

         result.add(new DungeonDataTypes.Marker(localId(id, "marker id"), type, group, vector(required(json, "position"), "marker position")));
      }

      return result;
   }

   private static List<DungeonDataTypes.Encounter> parseEncounters(
      JsonObject owner, ResourceLocation resource, Optional<ResourceLocation> defaultPool, Optional<ResourceLocation> bossPool
   ) {
      JsonArray values = array(owner, "encounters", new JsonArray());
      List<DungeonDataTypes.Encounter> result = new ArrayList<>();
      int encounterIndex = 0;

      for (JsonElement value : values) {
         JsonObject json = object(value, "encounter");
         String id = localId(string(json, "id", "encounter_" + ++encounterIndex), "encounter id");
         Optional<String> trigger = optionalString(json, "trigger_region");
         if (trigger.isEmpty()) {
            trigger = optionalString(json, "trigger");
         }

         trigger = trigger.map(valueId -> localId(valueId, "trigger region id"));
         List<String> lockSockets = stringList(json, "lock_sockets").stream().map(valueId -> localId(valueId, "locked socket id")).toList();
         List<DungeonDataTypes.EncounterWave> waves = new ArrayList<>();
         JsonArray waveValues = array(json, "waves", null);
         if (waveValues == null && (json.has("spawn_group") || json.has("marker_group"))) {
            waveValues = singletonArray(json);
         }

         if (waveValues != null) {
            int waveIndex = 0;

            for (JsonElement waveValue : waveValues) {
               JsonObject wave = object(waveValue, "encounter wave");
               String waveId = localId(string(wave, "id", id + "_wave_" + ++waveIndex), "wave id");
               boolean boss = bool(wave, "boss", false);
               String group = wave.has("marker_group") ? requiredString(wave, "marker_group") : requiredString(wave, "spawn_group");
               Optional<ResourceLocation> pool = optionalInternalId(wave, "pool", resource.getNamespace());
               if (pool.isEmpty()) {
                  pool = optionalInternalId(wave, "mob_pool", resource.getNamespace());
               }

               if (pool.isEmpty()) {
                  pool = boss && bossPool.isPresent() ? bossPool : defaultPool;
               }

               if (pool.isEmpty()) {
                  throw new JsonParseException("Encounter wave " + waveId + " has no mob pool and no default_mob_pool");
               }

               DungeonDataTypes.IntRange count = wave.has("count") ? range(wave.get("count"), "count", 0, 256) : new DungeonDataTypes.IntRange(1, 1);
               int delay = wave.has("delay_ticks") ? boundedInt(wave, "delay_ticks", 0, 0, 1000000) : boundedInt(wave, "delay", 0, 0, 1000000);
               Optional<DungeonDataTypes.IntRange> waveLevel = optionalRange(wave, "level", 1, 1000);
               if (waveLevel.isEmpty()) {
                  waveLevel = optionalRange(wave, "spawn_level", 1, 1000);
               }

               waves.add(new DungeonDataTypes.EncounterWave(waveId, localId(group, "marker group"), pool.get(), count, delay, boss, waveLevel));
            }
         }

         result.add(new DungeonDataTypes.Encounter(id, trigger, waves, lockSockets));
      }

      return result;
   }

   private static DungeonDataTypes.IntRange parseRoomCount(JsonObject json) {
      if (json.has("room_count")) {
         return range(json.get("room_count"), "room_count", 1, 64);
      } else {
         int min = boundedInt(json, "min_rooms", 5, 1, 64);
         int max = boundedInt(json, "max_rooms", Math.max(min, 12), 1, 64);
         if (min > max) {
            throw new JsonParseException("min_rooms cannot exceed max_rooms");
         } else {
            return new DungeonDataTypes.IntRange(min, max);
         }
      }
   }

   private static DungeonDataTypes.LevelRule parseLevelRule(JsonObject json) {
      if (!json.has("level")) {
         return new DungeonDataTypes.LevelRule("fixed", new DungeonDataTypes.IntRange(1, 1), 0);
      }

      JsonElement element = json.get("level");
      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
         int value = element.getAsInt();
         if (value >= 1 && value <= 1000) {
            return new DungeonDataTypes.LevelRule("fixed", new DungeonDataTypes.IntRange(value, value), 0);
         } else {
            throw new JsonParseException("level must be between 1 and 1000");
         }
      } else {
         JsonObject level = object(element, "level");
         String source = string(level, "source", "fixed");
         if (!Set.of("fixed", "owner", "player", "party_average", "average", "party_highest", "highest").contains(source.toLowerCase(Locale.ROOT))) {
            throw new JsonParseException("Unknown level source " + source + ". Expected fixed, owner, party_average, or party_highest.");
         }

         DungeonDataTypes.IntRange range;
         if (level.has("range")) {
            range = range(level.get("range"), "level range", 1, 1000);
         } else {
            int min = boundedInt(level, "min", 1, 1, 1000);
            int max = boundedInt(level, "max", min, 1, 1000);
            if (min > max) {
               throw new JsonParseException("level min cannot exceed max");
            }

            range = new DungeonDataTypes.IntRange(min, max);
         }

         return new DungeonDataTypes.LevelRule(source, range, boundedInt(level, "variance", 0, 0, 1000));
      }
   }

   private static DungeonDataTypes.ShellSettings parseShell(JsonElement element, ResourceLocation owner) {
      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
         boolean enabled = element.getAsBoolean();
         return new DungeonDataTypes.ShellSettings(enabled, BEDROCK_ID, enabled ? 1 : 0, true, true);
      } else {
         JsonObject json = object(element, "shell");
         boolean enabled = bool(json, "enabled", true);
         ResourceLocation block = minecraftId(string(json, "block", BEDROCK_ID.toString()));
         if (ForgeRegistries.BLOCKS.containsKey(block) && ForgeRegistries.BLOCKS.getValue(block) != Blocks.AIR) {
            int thickness = enabled ? boundedInt(json, "thickness", 1, 1, 4) : 0;
            return new DungeonDataTypes.ShellSettings(enabled, block, thickness, bool(json, "cover_floor", true), bool(json, "cover_ceiling", true));
         } else {
            throw new JsonParseException("Unknown or invalid shell block " + block + " in " + owner);
         }
      }
   }

   private static Map<ResourceLocation, DungeonRoomDefinition> validatedRooms(
      Map<ResourceLocation, DungeonRoomDefinition> input, Map<ResourceLocation, MobPoolDefinition> pools, List<DungeonDataTypes.ValidationIssue> issues
   ) {
      Map<ResourceLocation, DungeonRoomDefinition> valid = new LinkedHashMap<>();
      input.forEach(
         (id, room) -> {
            int before = errorCount(issues, id);
            if (!room.origin().equals(new DungeonDataTypes.Int3(0, 0, 0))) {
               issues.add(error(id, "Non-zero room origin is reserved for a future schema version. Structure coordinates must start at [0,0,0]."));
            }

            if (room.shellOverride().isPresent()) {
               issues.add(error(id, "Room-level shell overrides are reserved for a future schema version. Configure shell on the dungeon definition."));
            }

            validateGeometry(id, room.size(), room.origin(), room.regions(), room.sockets(), room.markers(), issues);
            room.defaultMobPool().ifPresent(pool -> requirePool(id, pool, pools, issues));
            validateEncounters(id, room.formatVersion(), room.regions(), room.sockets(), room.markers(), room.encounters(), pools, issues);
            long horizontalSockets = room.sockets().stream().filter(socket -> socket.facing().getAxis().isHorizontal()).count();
            if (room.role() == DungeonDataTypes.RoomRole.START && horizontalSockets < 1L) {
               issues.add(warning(id, "Start room should have at least one horizontal exit socket."));
            }

            if (room.role() == DungeonDataTypes.RoomRole.START && room.markers().stream().noneMatch(marker -> marker.type().equals("player_start"))) {
               issues.add(error(id, "Start room requires a player_start marker."));
            }

            if (room.role() == DungeonDataTypes.RoomRole.START
               && room.markers().stream().noneMatch(marker -> marker.type().equals("exit") || marker.type().equals("return_portal"))) {
               issues.add(error(id, "Start room requires an exit or return_portal marker."));
            }

            if (room.role() == DungeonDataTypes.RoomRole.BOSS && room.markers().stream().noneMatch(marker -> marker.type().equals("boss_spawn"))) {
               issues.add(error(id, "Boss room requires a boss_spawn marker."));
            }

            if (room.role() == DungeonDataTypes.RoomRole.BOSS
               && room.encounters().stream().flatMap(encounter -> encounter.waves().stream()).noneMatch(DungeonDataTypes.EncounterWave::boss)) {
               issues.add(error(id, "Boss room requires at least one encounter wave with boss=true."));
            }

            if ((
                  room.role() == DungeonDataTypes.RoomRole.BOSS
                     || room.role() == DungeonDataTypes.RoomRole.CAP
                     || room.role() == DungeonDataTypes.RoomRole.DEAD_END
               )
               && horizontalSockets != 1L) {
               issues.add(warning(id, room.role().name().toLowerCase(Locale.ROOT) + " room should normally have exactly one horizontal connection socket."));
            }

            if (errorCount(issues, id) == before) {
               valid.put(id, room);
            }
         }
      );
      return valid;
   }

   private static Map<ResourceLocation, DungeonDefinition> validatedDungeons(
      Map<ResourceLocation, DungeonDefinition> input,
      Map<ResourceLocation, DungeonRoomDefinition> rooms,
      Map<ResourceLocation, MobPoolDefinition> pools,
      List<DungeonDataTypes.ValidationIssue> issues
   ) {
      Map<ResourceLocation, DungeonDefinition> valid = new LinkedHashMap<>();
      input.forEach(
         (id, dungeon) -> {
            int before = errorCount(issues, id);
            if (!dungeon.shell().coverFloor() || !dungeon.shell().coverCeiling()) {
               issues.add(error(id, "Schema v2 protective shells must cover both floor and ceiling."));
            }

            dungeon.defaultMobPool().ifPresent(poolx -> requirePool(id, poolx, pools, issues));
            dungeon.bossMobPool().ifPresent(poolx -> requirePool(id, poolx, pools, issues));
            if (dungeon.kind() == DungeonDataTypes.DungeonKind.PRESET) {
               if (dungeon.structure().isEmpty()) {
                  issues.add(error(id, "Preset dungeon requires structure."));
               }

               if (dungeon.size().isEmpty()) {
                  issues.add(error(id, "Preset dungeon requires size."));
               }

               if (dungeon.size().isPresent()) {
                  DungeonDataTypes.Int3 dungeonOrigin = dungeon.origin().orElse(new DungeonDataTypes.Int3(0, 0, 0));
                  if (!dungeonOrigin.equals(new DungeonDataTypes.Int3(0, 0, 0))) {
                     issues.add(error(id, "Non-zero preset origin is reserved for a future schema version. Structure coordinates must start at [0,0,0]."));
                  }

                  validateGeometry(id, dungeon.size().get(), dungeonOrigin, dungeon.regions(), dungeon.sockets(), dungeon.markers(), issues);
               }

               validateEncounters(id, dungeon.formatVersion(), dungeon.regions(), dungeon.sockets(), dungeon.markers(), dungeon.encounters(), pools, issues);
               if (dungeon.markers().stream().noneMatch(marker -> marker.type().equals("player_start"))) {
                  issues.add(error(id, "Preset dungeon requires a player_start marker."));
               }

               if (dungeon.markers().stream().noneMatch(marker -> marker.type().equals("exit") || marker.type().equals("return_portal"))) {
                  issues.add(error(id, "Preset dungeon requires an exit or return_portal marker."));
               }

               if (dungeon.markers().stream().noneMatch(marker -> marker.type().equals("boss_spawn"))) {
                  issues.add(error(id, "Preset dungeon requires a boss_spawn marker so the run can complete."));
               }

               if (dungeon.encounters().stream().flatMap(encounter -> encounter.waves().stream()).noneMatch(DungeonDataTypes.EncounterWave::boss)) {
                  issues.add(error(id, "Preset dungeon requires at least one encounter wave with boss=true."));
               }
            } else if (dungeon.kind() == DungeonDataTypes.DungeonKind.PROCEDURAL) {
               if (dungeon.topology() == DungeonDataTypes.DungeonTopology.BRANCHING && dungeon.roomCount().min() < 4) {
                  issues.add(error(id, "Branching topology requires room_count minimum 4 or greater."));
               }

               if (!dungeon.rooms(DungeonDataTypes.RoomRole.CAP).isEmpty()) {
                  issues.add(error(id, "CAP room pools are reserved for a future schema version. Leave unused sockets as solid room walls."));
               }

               if (dungeon.rooms(DungeonDataTypes.RoomRole.START).isEmpty()) {
                  issues.add(error(id, "Procedural dungeon requires at least one start room."));
               }

               if (dungeon.rooms(DungeonDataTypes.RoomRole.BOSS).isEmpty()) {
                  issues.add(error(id, "Procedural dungeon requires at least one boss room."));
               }

               boolean hasMiddle = dungeon.roomPools()
                  .entrySet()
                  .stream()
                  .filter(
                     entry -> entry.getKey() != DungeonDataTypes.RoomRole.START
                        && entry.getKey() != DungeonDataTypes.RoomRole.BOSS
                        && entry.getKey() != DungeonDataTypes.RoomRole.CAP
                  )
                  .anyMatch(entry -> !entry.getValue().isEmpty());
               if (!hasMiddle) {
                  issues.add(error(id, "Procedural dungeon requires at least one normal, corridor, junction, treasure, dead-end, or stair room."));
               }

               for (Entry<DungeonDataTypes.RoomRole, List<DungeonDataTypes.WeightedRoom>> pool : dungeon.roomPools().entrySet()) {
                  for (DungeonDataTypes.WeightedRoom choice : pool.getValue()) {
                     DungeonRoomDefinition room = rooms.get(choice.room());
                     if (room == null) {
                        issues.add(error(id, "Referenced room " + choice.room() + " does not exist or failed validation."));
                     } else if (room.role() != pool.getKey()) {
                        issues.add(
                           error(
                              id,
                              "Room "
                                 + choice.room()
                                 + " has role "
                                 + room.role().name().toLowerCase(Locale.ROOT)
                                 + " but is listed in the "
                                 + pool.getKey().name().toLowerCase(Locale.ROOT)
                                 + " pool."
                           )
                        );
                     }
                  }
               }
            } else {
               validateFixedDungeon(id, dungeon, rooms, issues);
            }

            if (errorCount(issues, id) == before) {
               valid.put(id, dungeon);
            }
         }
      );
      return valid;
   }

   private static void validateFixedDungeon(
      ResourceLocation id, DungeonDefinition dungeon, Map<ResourceLocation, DungeonRoomDefinition> rooms, List<DungeonDataTypes.ValidationIssue> issues
   ) {
      if (!dungeon.roomPools().isEmpty()) {
         issues.add(error(id, "Fixed dungeon uses placements/connections, not room_pools."));
      }

      if (!dungeon.fixedPlacements().isEmpty() && dungeon.fixedPlacements().size() <= 64) {
         if (dungeon.fixedConnections().size() > 256) {
            issues.add(error(id, "Fixed dungeon supports at most 256 connections."));
         }

         validateUniqueIds(id, "fixed placement", dungeon.fixedPlacements().stream().map(DungeonDataTypes.FixedRoomPlacement::id).toList(), issues);
         Map<String, DungeonRoomDefinition> placedRooms = new LinkedHashMap<>();
         int starts = 0;
         int bosses = 0;

         for (DungeonDataTypes.FixedRoomPlacement placement : dungeon.fixedPlacements()) {
            if (Math.abs((long)placement.position().x()) > 1000000L
               || Math.abs((long)placement.position().y()) > 1000000L
               || Math.abs((long)placement.position().z()) > 1000000L) {
               issues.add(error(id, "Fixed placement " + placement.id() + " position exceeds the +/-1,000,000 safety range."));
            }

            DungeonRoomDefinition room = rooms.get(placement.room());
            if (room == null) {
               issues.add(error(id, "Fixed placement " + placement.id() + " references missing room " + placement.room() + "."));
            } else {
               placedRooms.put(placement.id(), room);
               if (room.role() == DungeonDataTypes.RoomRole.START) {
                  starts++;
               }

               if (room.role() == DungeonDataTypes.RoomRole.BOSS) {
                  bosses++;
               }
            }
         }

         if (starts != 1 || bosses != 1) {
            issues.add(error(id, "Fixed dungeon requires exactly one start placement and exactly one boss placement."));
         }

         Set<String> usedSockets = new HashSet<>();
         Map<String, Set<String>> adjacency = new HashMap<>();
         placedRooms.keySet().forEach(key -> adjacency.put(key, new HashSet<>()));

         for (DungeonDataTypes.FixedRoomConnection connection : dungeon.fixedConnections()) {
            DungeonRoomDefinition from = placedRooms.get(connection.fromRoom());
            DungeonRoomDefinition to = placedRooms.get(connection.toRoom());
            if (from != null && to != null && !connection.fromRoom().equals(connection.toRoom())) {
               DungeonDataTypes.Socket fromSocket = from.sockets()
                  .stream()
                  .filter(socketx -> socketx.id().equals(connection.fromSocket()))
                  .findFirst()
                  .orElse(null);
               DungeonDataTypes.Socket toSocket = to.sockets().stream().filter(socketx -> socketx.id().equals(connection.toSocket())).findFirst().orElse(null);
               if (fromSocket != null && toSocket != null) {
                  if (!fromSocket.type().equals(toSocket.type())) {
                     issues.add(
                        error(
                           id,
                           "Fixed connection sockets "
                              + connection.fromRoom()
                              + ":"
                              + connection.fromSocket()
                              + " and "
                              + connection.toRoom()
                              + ":"
                              + connection.toSocket()
                              + " use different types."
                        )
                     );
                  }

                  String fromKey = connection.fromRoom() + "\u0000" + connection.fromSocket();
                  String toKey = connection.toRoom() + "\u0000" + connection.toSocket();
                  if (!usedSockets.add(fromKey) || !usedSockets.add(toKey)) {
                     issues.add(error(id, "Each socket may appear in only one fixed connection."));
                  }

                  adjacency.get(connection.fromRoom()).add(connection.toRoom());
                  adjacency.get(connection.toRoom()).add(connection.fromRoom());
               } else {
                  issues.add(
                     error(
                        id,
                        "Fixed connection "
                           + connection.fromRoom()
                           + ":"
                           + connection.fromSocket()
                           + " -> "
                           + connection.toRoom()
                           + ":"
                           + connection.toSocket()
                           + " references a missing socket."
                     )
                  );
               }
            } else {
               issues.add(error(id, "Fixed connection references a missing placement or connects a room to itself."));
            }
         }

         for (Entry<String, DungeonRoomDefinition> placement : placedRooms.entrySet()) {
            for (DungeonDataTypes.Socket socket : placement.getValue().sockets()) {
               if (socket.required() && !usedSockets.contains(placement.getKey() + "\u0000" + socket.id())) {
                  issues.add(error(id, "Required socket " + placement.getKey() + ":" + socket.id() + " is not used by a fixed connection."));
               }
            }
         }

         String start = placedRooms.entrySet()
            .stream()
            .filter(entry -> entry.getValue().role() == DungeonDataTypes.RoomRole.START)
            .map(Entry::getKey)
            .findFirst()
            .orElse(null);
         if (start != null) {
            Set<String> visited = new HashSet<>();
            ArrayDeque<String> queue = new ArrayDeque<>();
            visited.add(start);
            queue.add(start);

            while (!queue.isEmpty()) {
               for (String next : adjacency.getOrDefault(queue.removeFirst(), Set.of())) {
                  if (visited.add(next)) {
                     queue.addLast(next);
                  }
               }
            }

            if (visited.size() != placedRooms.size()) {
               issues.add(error(id, "Every fixed placement must be connected to the start-room graph."));
            }
         }
      } else {
         issues.add(error(id, "Fixed dungeon requires 1-64 placements."));
      }
   }

   private static void validateGeometry(
      ResourceLocation id,
      DungeonDataTypes.Int3 size,
      DungeonDataTypes.Int3 origin,
      List<DungeonDataTypes.Region> regions,
      List<DungeonDataTypes.Socket> sockets,
      List<DungeonDataTypes.Marker> markers,
      List<DungeonDataTypes.ValidationIssue> issues
   ) {
      if (!size.isPositive() || size.x() > 48 || size.y() > 48 || size.z() > 48) {
         issues.add(error(id, "Structure size must be positive and no larger than 48 blocks on each axis."));
      }

      if (!size.contains(origin)) {
         issues.add(error(id, "Origin lies outside structure size."));
      }

      validateUniqueIds(id, "region", regions.stream().map(DungeonDataTypes.Region::id).toList(), issues);
      validateUniqueIds(id, "socket", sockets.stream().map(DungeonDataTypes.Socket::id).toList(), issues);
      validateUniqueIds(id, "marker", markers.stream().map(DungeonDataTypes.Marker::id).toList(), issues);
      if (sockets.stream().filter(DungeonDataTypes.Socket::required).count() > 2L) {
         issues.add(error(id, "Schema v2 supports at most two required sockets per room. Extra junction branches must be optional."));
      }

      for (DungeonDataTypes.Region region : regions) {
         if (!region.bounds().inside(size)) {
            issues.add(error(id, "Region " + region.id() + " lies outside structure size."));
         }
      }

      for (DungeonDataTypes.Marker marker : markers) {
         if (!size.contains(marker.position())) {
            issues.add(error(id, "Marker " + marker.id() + " lies outside structure size."));
         } else if (marker.type().equals("loot") || marker.type().equals("checkpoint")) {
            issues.add(warning(id, "Marker " + marker.id() + " is addon metadata only; schema v2 has no built-in " + marker.type() + " behavior."));
         }
      }

      for (DungeonDataTypes.Socket socket : sockets) {
         if (!socket.opening().inside(size)) {
            issues.add(error(id, "Socket " + socket.id() + " lies outside structure size."));
         } else {
            boolean flatPlane = switch (socket.facing().getAxis()) {
               case X -> socket.opening().min().x() == socket.opening().max().x();
               case Y -> socket.opening().min().y() == socket.opening().max().y();
               case Z -> socket.opening().min().z() == socket.opening().max().z();
            };
            if (!flatPlane) {
               issues.add(error(id, "Socket " + socket.id() + " must be a flat opening plane perpendicular to its facing."));
            }

            int inset = socket.insetFromBoundary(size);
            if (inset < 0 || inset > 1) {
               issues.add(
                  error(id, "Socket " + socket.id() + " must be on the wall plane or exactly one block inside the " + socket.facing().getName() + " wall.")
               );
            }

            if (socket.type().equals("corridor") && socket.facing().getAxis().isVertical()) {
               issues.add(error(id, "Corridor socket " + socket.id() + " must face north, south, east, or west."));
            }

            if (socket.type().equals("stair") && socket.facing().getAxis().isHorizontal()) {
               issues.add(error(id, "Stair socket " + socket.id() + " must face up or down."));
            }

            if (socket.carveDepth() != 1) {
               issues.add(error(id, "Socket " + socket.id() + " uses carve_depth, which is reserved for a future schema version. Omit it or use 1."));
            }
         }
      }
   }

   private static void validateEncounters(
      ResourceLocation id,
      int formatVersion,
      List<DungeonDataTypes.Region> regions,
      List<DungeonDataTypes.Socket> sockets,
      List<DungeonDataTypes.Marker> markers,
      List<DungeonDataTypes.Encounter> encounters,
      Map<ResourceLocation, MobPoolDefinition> pools,
      List<DungeonDataTypes.ValidationIssue> issues
   ) {
      Set<String> regionIds = regions.stream().map(DungeonDataTypes.Region::id).collect(Collectors.toSet());
      Set<String> socketIds = sockets.stream().map(DungeonDataTypes.Socket::id).collect(Collectors.toSet());
      validateUniqueIds(id, "encounter", encounters.stream().map(DungeonDataTypes.Encounter::id).toList(), issues);

      for (DungeonDataTypes.Encounter encounter : encounters) {
         encounter.triggerRegion().ifPresent(trigger -> {
            if (!regionIds.contains(trigger)) {
               issues.add(error(id, "Encounter " + encounter.id() + " references missing trigger region " + trigger + "."));
            }
         });

         for (String socket : encounter.lockSockets()) {
            if (!socketIds.contains(socket)) {
               issues.add(error(id, "Encounter " + encounter.id() + " references missing lock socket " + socket + "."));
            }
         }

         if (!encounter.lockSockets().isEmpty()) {
            issues.add(error(id, "Encounter " + encounter.id() + " uses lock_sockets, which is reserved for a later schema version and is not active yet."));
         }

         if (formatVersion < 3 && encounter.waves().size() != 1) {
            issues.add(error(id, "Encounter " + encounter.id() + " must contain exactly one wave before schema v3."));
         }

         if (formatVersion >= 3 && encounter.waves().isEmpty()) {
            issues.add(error(id, "Encounter " + encounter.id() + " must contain at least one wave."));
         }

         validateUniqueIds(id, "wave in encounter " + encounter.id(), encounter.waves().stream().map(DungeonDataTypes.EncounterWave::id).toList(), issues);

         for (int waveIndex = 0; waveIndex < encounter.waves().size(); waveIndex++) {
            DungeonDataTypes.EncounterWave wave = encounter.waves().get(waveIndex);
            List<DungeonDataTypes.Marker> groupedSpawnMarkers = markers.stream()
               .filter(marker -> isSpawnMarker(marker) && marker.belongsTo(wave.markerGroup()))
               .toList();
            if (groupedSpawnMarkers.isEmpty()) {
               issues.add(error(id, "Wave " + wave.id() + " references missing marker/group " + wave.markerGroup() + "."));
            }

            requirePool(id, wave.mobPool(), pools, issues);
            if (formatVersion < 3 && wave.delayTicks() != 0) {
               issues.add(error(id, "Wave " + wave.id() + " uses delay_ticks, which requires format_version 3."));
            }

            if (formatVersion >= 3 && wave.boss() && waveIndex != encounter.waves().size() - 1) {
               issues.add(error(id, "Boss wave " + wave.id() + " must be the terminal wave of its schema-v3 encounter."));
            }

            if (wave.count().min() < 1) {
               issues.add(error(id, "Wave " + wave.id() + " must spawn at least one mob."));
            }

            List<DungeonDataTypes.Marker> eligibleMarkers = groupedSpawnMarkers.stream()
               .filter(marker -> wave.boss() == marker.type().equals("boss_spawn"))
               .toList();
            if (wave.boss() && eligibleMarkers.isEmpty()) {
               issues.add(error(id, "Boss wave " + wave.id() + " must target a boss_spawn marker."));
            }

            if (!wave.boss() && groupedSpawnMarkers.stream().anyMatch(marker -> marker.type().equals("boss_spawn"))) {
               issues.add(error(id, "Non-boss wave " + wave.id() + " cannot target a boss_spawn marker."));
            }

            if (wave.count().max() > eligibleMarkers.size()) {
               issues.add(
                  error(
                     id,
                     "Wave " + wave.id() + " can spawn " + wave.count().max() + " mobs but has only " + eligibleMarkers.size() + " compatible spawn marker(s)."
                  )
               );
            }
         }
      }
   }

   private static boolean isSpawnMarker(DungeonDataTypes.Marker marker) {
      return marker.type().equals("mob_spawn") || marker.type().equals("elite_spawn") || marker.type().equals("boss_spawn");
   }

   private static void validateUniqueIds(ResourceLocation resource, String kind, List<String> ids, List<DungeonDataTypes.ValidationIssue> issues) {
      Set<String> unique = new HashSet<>();

      for (String id : ids) {
         if (!unique.add(id)) {
            issues.add(error(resource, "Duplicate " + kind + " id " + id + "."));
         }
      }
   }

   private static void requirePool(
      ResourceLocation owner, ResourceLocation pool, Map<ResourceLocation, MobPoolDefinition> pools, List<DungeonDataTypes.ValidationIssue> issues
   ) {
      MobPoolDefinition definition = pools.get(pool);
      if (definition == null) {
         issues.add(error(owner, "Referenced mob pool " + pool + " does not exist."));
      } else if (definition.entries().isEmpty()) {
         issues.add(error(owner, "Referenced mob pool " + pool + " has no active entries. Add a fallback entry when optional mods are absent."));
      }
   }

   private static int errorCount(List<DungeonDataTypes.ValidationIssue> issues, ResourceLocation resource) {
      return (int)issues.stream().filter(issue -> issue.severity() == DungeonDataTypes.Severity.ERROR && issue.resource().equals(resource)).count();
   }

   private static DungeonDataTypes.RoomRole inferredRole(List<DungeonDataTypes.Marker> markers) {
      if (markers.stream().anyMatch(marker -> marker.type().equals("boss_spawn"))) {
         return DungeonDataTypes.RoomRole.BOSS;
      } else {
         return markers.stream().anyMatch(marker -> marker.type().equals("player_start")) ? DungeonDataTypes.RoomRole.START : DungeonDataTypes.RoomRole.NORMAL;
      }
   }

   private static DungeonDataTypes.DungeonKind strictDungeonKind(String value) {
      String normalized = value == null ? "preset" : value.toLowerCase(Locale.ROOT).replace('-', '_');

      return switch (normalized) {
         case "preset", "structure", "single" -> DungeonDataTypes.DungeonKind.PRESET;
         case "procedural", "module_pool" -> DungeonDataTypes.DungeonKind.PROCEDURAL;
         case "fixed", "fixed_layout" -> DungeonDataTypes.DungeonKind.FIXED;
         default -> throw new JsonParseException("Unknown dungeon generation type " + value + ". Expected preset, procedural, or fixed.");
      };
   }

   private static DungeonDataTypes.DungeonTopology parseDungeonTopology(JsonObject json, DungeonDataTypes.DungeonKind kind) {
      if (!json.has("topology")) {
         return DungeonDataTypes.DungeonTopology.LINEAR;
      }

      if (kind != DungeonDataTypes.DungeonKind.PROCEDURAL) {
         throw new JsonParseException("topology is supported only by procedural dungeon definitions");
      }

      String normalized = string(json, "topology", "linear").toLowerCase(Locale.ROOT).replace('-', '_');

      return switch (normalized) {
         case "linear", "path" -> DungeonDataTypes.DungeonTopology.LINEAR;
         case "branching", "branched", "branches" -> DungeonDataTypes.DungeonTopology.BRANCHING;
         default -> throw new JsonParseException("Unknown dungeon topology " + normalized + ". Expected linear or branching.");
      };
   }

   private static DungeonDataTypes.RoomRole strictRoomRole(String value, String field) {
      String normalized = value == null ? "normal" : value.toLowerCase(Locale.ROOT).replace('-', '_');

      return switch (normalized) {
         case "start", "entry", "entrance" -> DungeonDataTypes.RoomRole.START;
         case "normal" -> DungeonDataTypes.RoomRole.NORMAL;
         case "junction", "branch" -> DungeonDataTypes.RoomRole.JUNCTION;
         case "dead_end", "deadend" -> DungeonDataTypes.RoomRole.DEAD_END;
         case "treasure", "reward" -> DungeonDataTypes.RoomRole.TREASURE;
         case "boss", "boss_room" -> DungeonDataTypes.RoomRole.BOSS;
         case "cap", "wall_cap" -> DungeonDataTypes.RoomRole.CAP;
         case "corridor", "hall" -> DungeonDataTypes.RoomRole.CORRIDOR;
         case "stair", "stairs" -> DungeonDataTypes.RoomRole.STAIR;
         default -> throw new JsonParseException("Unknown " + field + " " + value + ".");
      };
   }

   private static boolean conditionsApply(JsonObject json, IContext context) {
      return CraftingHelper.processConditions(json, "conditions", context);
   }

   private static int formatVersion(JsonObject json) {
      int version = boundedInt(json, "format_version", 1, 1, 3);
      if (version != 1 && version != 2 && version != 3) {
         throw new JsonParseException("Unsupported format_version " + version + "; supported versions are 1, 2, and 3");
      } else {
         return version;
      }
   }

   private static DungeonDataTypes.EntitySelector parseSelector(JsonElement element, String ownerNamespace) {
      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
         String value = element.getAsString();
         return value.startsWith("#")
            ? new DungeonDataTypes.EntitySelector(DungeonDataTypes.SelectorKind.TAG, tagId(value.substring(1), ownerNamespace))
            : new DungeonDataTypes.EntitySelector(DungeonDataTypes.SelectorKind.ENTITY, minecraftId(value));
      } else {
         return parseSelector(object(element, "entity selector"), ownerNamespace);
      }
   }

   private static DungeonDataTypes.EntitySelector parseSelector(JsonObject json, String ownerNamespace) {
      boolean entity = json.has("entity");
      boolean tag = json.has("tag");
      if (entity == tag) {
         throw new JsonParseException("Mob pool entry must contain exactly one of entity or tag");
      }

      if (tag) {
         return new DungeonDataTypes.EntitySelector(DungeonDataTypes.SelectorKind.TAG, tagId(requiredString(json, "tag"), ownerNamespace));
      }

      String value = requiredString(json, "entity");
      return value.startsWith("#")
         ? new DungeonDataTypes.EntitySelector(DungeonDataTypes.SelectorKind.TAG, tagId(value.substring(1), ownerNamespace))
         : new DungeonDataTypes.EntitySelector(DungeonDataTypes.SelectorKind.ENTITY, minecraftId(value));
   }

   private static ResourceLocation tagId(String value, String ownerNamespace) {
      return id(value, ownerNamespace, "entity tag");
   }

   private static ResourceLocation internalId(String value, String ownerNamespace) {
      return id(value, ownerNamespace, "resource id");
   }

   private static ResourceLocation minecraftId(String value) {
      return id(value, "minecraft", "registry id");
   }

   private static ResourceLocation id(String value, String defaultNamespace, String label) {
      String qualified = value.contains(":") ? value : defaultNamespace + ":" + value;
      ResourceLocation result = ResourceLocation.tryParse(qualified);
      if (result == null) {
         throw new JsonParseException("Invalid " + label + " " + value);
      } else {
         return result;
      }
   }

   private static String localId(String value, String label) {
      if (value != null && value.matches("[a-z0-9][a-z0-9_.-]{0,63}")) {
         return value;
      } else {
         throw new JsonParseException("Invalid " + label + " " + value + "; use lowercase letters, numbers, _, - or .");
      }
   }

   private static Optional<ResourceLocation> optionalInternalId(JsonObject json, String key, String ownerNamespace) {
      return optionalString(json, key).map(value -> internalId(value, ownerNamespace));
   }

   private static Optional<DungeonDataTypes.IntRange> optionalRange(JsonObject json, String key, int minimum, int maximum) {
      return json.has(key) ? Optional.of(range(json.get(key), key, minimum, maximum)) : Optional.empty();
   }

   private static DungeonDataTypes.IntRange range(JsonElement element, String label, int minimum, int maximum) {
      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
         int value = element.getAsInt();
         if (value >= minimum && value <= maximum) {
            return new DungeonDataTypes.IntRange(value, value);
         } else {
            throw new JsonParseException(label + " must be between " + minimum + " and " + maximum);
         }
      } else if (element.isJsonArray()) {
         JsonArray values = element.getAsJsonArray();
         if (values.size() != 2) {
            throw new JsonParseException(label + " array must contain [min, max]");
         } else {
            int min = values.get(0).getAsInt();
            int max = values.get(1).getAsInt();
            if (min >= minimum && max <= maximum && min <= max) {
               return new DungeonDataTypes.IntRange(min, max);
            } else {
               throw new JsonParseException(label + " must stay between " + minimum + " and " + maximum + " with min <= max");
            }
         }
      } else {
         JsonObject json = object(element, label);
         int min = boundedInt(json, "min", minimum, minimum, maximum);
         int max = boundedInt(json, "max", min, minimum, maximum);
         if (min > max) {
            throw new JsonParseException(label + " min cannot exceed max");
         } else {
            return new DungeonDataTypes.IntRange(min, max);
         }
      }
   }

   private static DungeonDataTypes.Int3 vector(JsonElement element, String label) {
      JsonArray array = element.isJsonArray() ? element.getAsJsonArray() : null;
      if (array != null && array.size() == 3) {
         try {
            return new DungeonDataTypes.Int3(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
         } catch (RuntimeException exception) {
            throw new JsonParseException(label + " must contain integers", exception);
         }
      } else {
         throw new JsonParseException(label + " must be an array of exactly three integers");
      }
   }

   private static List<String> stringList(JsonObject json, String key) {
      if (!json.has(key)) {
         return List.of();
      }

      JsonArray array = array(json, key, null);
      if (array == null) {
         return List.of();
      }

      List<String> result = new ArrayList<>();

      for (JsonElement value : array) {
         if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new JsonParseException(key + " must contain only strings");
         }

         result.add(value.getAsString());
      }

      return result;
   }

   private static JsonArray singletonArray(JsonElement element) {
      JsonArray array = new JsonArray();
      array.add(element);
      return array;
   }

   private static JsonElement required(JsonObject json, String key) {
      if (json.has(key) && !json.get(key).isJsonNull()) {
         return json.get(key);
      } else {
         throw new JsonParseException("Missing required field " + key);
      }
   }

   private static String requiredString(JsonObject json, String key) {
      JsonElement value = required(json, key);
      if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
         return value.getAsString();
      } else {
         throw new JsonParseException(key + " must be a string");
      }
   }

   private static Optional<String> optionalString(JsonObject json, String key) {
      return json.has(key) && !json.get(key).isJsonNull() ? Optional.of(requiredString(json, key)) : Optional.empty();
   }

   private static String string(JsonObject json, String key, String fallback) {
      return optionalString(json, key).orElse(fallback);
   }

   private static boolean bool(JsonObject json, String key, boolean fallback) {
      if (!json.has(key)) {
         return fallback;
      } else {
         JsonElement value = json.get(key);
         if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            return value.getAsBoolean();
         } else {
            throw new JsonParseException(key + " must be true or false");
         }
      }
   }

   private static int boundedInt(JsonObject json, String key, int fallback, int minimum, int maximum) {
      if (!json.has(key)) {
         return fallback;
      }

      JsonElement value = json.get(key);
      if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
         int result;
         try {
            result = value.getAsInt();
         } catch (RuntimeException exception) {
            throw new JsonParseException(key + " must be an integer", exception);
         }

         if (result >= minimum && result <= maximum) {
            return result;
         } else {
            throw new JsonParseException(key + " must be between " + minimum + " and " + maximum);
         }
      } else {
         throw new JsonParseException(key + " must be an integer");
      }
   }

   private static JsonObject object(JsonElement element, String label) {
      if (element != null && element.isJsonObject()) {
         return element.getAsJsonObject();
      } else {
         throw new JsonParseException(label + " must be a JSON object");
      }
   }

   private static JsonArray array(JsonObject json, String key, JsonArray fallback) {
      if (!json.has(key)) {
         return fallback;
      } else {
         JsonElement value = json.get(key);
         if (!value.isJsonArray()) {
            throw new JsonParseException(key + " must be an array");
         } else {
            return value.getAsJsonArray();
         }
      }
   }

   private static DungeonDataTypes.ValidationIssue warning(ResourceLocation id, String message) {
      return new DungeonDataTypes.ValidationIssue(DungeonDataTypes.Severity.WARNING, id, message);
   }

   private static DungeonDataTypes.ValidationIssue error(ResourceLocation id, String message) {
      return new DungeonDataTypes.ValidationIssue(DungeonDataTypes.Severity.ERROR, id, message);
   }

   private static String cleanMessage(RuntimeException exception) {
      String message = exception.getMessage();
      return message != null && !message.isBlank() ? message : exception.getClass().getSimpleName();
   }

   private static final class Loader extends SimplePreparableReloadListener<DungeonDataSnapshot> {
      private final IContext conditionContext;

      private Loader(IContext conditionContext) {
         this.conditionContext = conditionContext;
      }

      protected DungeonDataSnapshot prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
         profiler.push("sololeveling_dungeon_data");
         List<DungeonDataTypes.ValidationIssue> issues = new ArrayList<>();

         try {
            Map<ResourceLocation, JsonElement> poolJson = DungeonDataManager.scan(resourceManager, "slr/mob_pools");
            Map<ResourceLocation, JsonElement> roomJson = DungeonDataManager.scan(resourceManager, "slr/rooms");
            Map<ResourceLocation, JsonElement> dungeonJson = DungeonDataManager.scan(resourceManager, "slr/dungeons");
            Map<ResourceLocation, JsonElement> modifierJson = DungeonDataManager.modifiers(resourceManager, issues);
            Map<ResourceLocation, MobPoolDefinition> pools = DungeonDataManager.parseResources(
               poolJson, (id, element) -> DungeonDataManager.parsePool(id, element, this.conditionContext, issues), issues
            );
            List<DungeonDataTypes.MobPoolModifier> modifiers = DungeonDataManager.parseResources(
                  modifierJson, (id, element) -> DungeonDataManager.parseModifier(id, element, this.conditionContext, issues), issues
               )
               .values()
               .stream()
               .sorted(Comparator.comparing(modifier -> modifier.id().toString()))
               .toList();
            pools = DungeonDataManager.applyModifiers(pools, modifiers, issues);
            Map<ResourceLocation, DungeonRoomDefinition> rooms = DungeonDataManager.parseResources(
               roomJson, (id, element) -> DungeonDataManager.parseRoom(id, element, this.conditionContext), issues
            );
            Map<ResourceLocation, DungeonDefinition> dungeons = DungeonDataManager.parseResources(
               dungeonJson, (id, element) -> DungeonDataManager.parseDungeon(id, element, this.conditionContext), issues
            );
            pools.values().forEach(pool -> {
               if (pool.entries().isEmpty()) {
                  issues.add(DungeonDataManager.warning(pool.id(), "Mob pool is empty."));
               }
            });
            rooms = DungeonDataManager.validatedRooms(rooms, pools, issues);
            dungeons = DungeonDataManager.validatedDungeons(dungeons, rooms, pools, issues);
            return new DungeonDataSnapshot(0L, rooms, dungeons, pools, issues);
         } finally {
            profiler.pop();
         }
      }

      protected void apply(DungeonDataSnapshot prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
         DungeonDataSnapshot next = prepared.withRevision(DungeonDataManager.current.revision() + 1L);
         DungeonDataManager.current = next;
         long errors = next.issues().stream().filter(issuex -> issuex.severity() == DungeonDataTypes.Severity.ERROR).count();
         long warnings = next.issues().size() - errors;
         SololevelingMod.LOGGER
            .info(
               "Loaded SLR dungeon data revision {}: {} rooms, {} dungeons, {} mob pools ({} errors, {} warnings)",
               next.revision(),
               next.rooms().size(),
               next.dungeons().size(),
               next.mobPools().size(),
               errors,
               warnings
            );

         for (DungeonDataTypes.ValidationIssue issue : next.issues()) {
            String message = "[SLR dungeon data] " + issue.resource() + ": " + issue.message();
            if (issue.severity() == DungeonDataTypes.Severity.ERROR) {
               SololevelingMod.LOGGER.error(message);
            } else {
               SololevelingMod.LOGGER.warn(message);
            }
         }
      }
   }

   @FunctionalInterface
   private interface ResourceParser<T> {
      Optional<T> parse(ResourceLocation var1, JsonElement var2);
   }
}
