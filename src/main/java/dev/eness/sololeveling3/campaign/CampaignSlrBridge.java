package dev.eness.sololeveling3.campaign;

import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.entity.IceMonarchEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.solocraft.dungeon.DungeonTheme;
import net.solocraft.dungeon.ProceduralDungeonCompletionHandler;
import net.solocraft.dungeon.ProceduralDungeonRank;
import net.solocraft.dungeon.runtime.DungeonInstanceSavedData;
import net.solocraft.dungeon.runtime.SnowRedGateArenaManager;
import net.solocraft.entity.Portal1Entity;
import net.solocraft.entity.RedGateEntity;
import net.solocraft.init.SololevelingModEntities;
import net.solocraft.network.SololevelingModVariables;
import net.solocraft.util.GateCompletionTokens;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Narrow bridge to SLR state used by the final campaign director. */
public final class CampaignSlrBridge {
    public static final String OWNER_TAG = "sl3_campaign_slr_owner";

    private static final String FROST_INSTANCE_TAG = "frost_instance";
    private static final String FROST_COMPLETED_TAG = "sl3_frost_flee_completed";
    private static final String FROST_EXIT_OPENED_TAG = "sl3_frost_exit_opened";
    private static final String FROST_GATE_TAG = "sl3_frost_gate";
    private static final String NATIVE_EXIT_INSTANCE_TAG = "sl3_native_exit_instance";
    private static final String NATIVE_EXIT_POS_TAG = "sl3_native_exit_pos";

    private static final ResourceLocation CARTENON_TEMPLE = ResourceLocation.fromNamespaceAndPath("sololeveling", "cartenon_temple");
    private static final ResourceLocation LEGIA_PRISON = ResourceLocation.fromNamespaceAndPath("sololeveling3", "legia_prison");
    private static final ResourceLocation RED_GATE_ID = ResourceLocation.fromNamespaceAndPath("sololeveling", "red_gate");
    private static final Set<ResourceLocation> NORMAL_DUNGEON_DIMENSIONS = Set.of(
            ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_d"),
            ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_c"),
            ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_b"),
            ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_a"),
            ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_s")
    );

    private CampaignSlrBridge() {
    }

    /** Strict Demon King's Castle completion: Baran alone is not enough; Kaiselin/floor closure must advance dkc_cleared. */
    public static boolean hasBaranCleared(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .map(vars -> vars.dkc_cleared >= 20.0D)
                .orElse(false);
    }

    public static double level(ServerPlayer player) {
        if (player == null) {
            return 0.0D;
        }
        return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .map(vars -> vars.Level)
                .orElse(0.0D);
    }

    /** Returns the native completion token for the player's current normal SLR dungeon without mutating campaign progress. */
    public static String normalReceipt(ServerPlayer player, ServerLevel source) {
        if (player == null || source == null || player.server == null) {
            return "";
        }
        String dungeonTag = player.getPersistentData().getString("dungeon_tag").trim();
        if (dungeonTag.isEmpty()) {
            return "";
        }
        if (!isNormalNativeDungeon(player, source, dungeonTag)) {
            return "";
        }
        String receipts = SololevelingModVariables.MapVariables.get(source).GatesCleared;
        if (!GateCompletionTokens.contains(receipts, dungeonTag)) {
            return "";
        }

        return dungeonTag;
    }

    /** Spawns one targeted ordinary blue native SLR gate near the player, rejecting red/campaign/Cartenon/datapack specials. */
    public static Entity spawnNormalGate(ServerPlayer player) {
        return spawnNormalGate(player, false);
    }

    /** Key hunting uses ordinary A/S dungeons so every boss can award a prison key. */
    public static Entity spawnKeyHuntGate(ServerPlayer player) {
        return spawnNormalGate(player, true);
    }

    private static Entity spawnNormalGate(ServerPlayer player, boolean keyHunt) {
        if (player == null || player.server == null || !Level.OVERWORLD.equals(player.level().dimension())) {
            return null;
        }
        ServerLevel level = player.serverLevel();
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos candidate = candidateNear(player, random, attempt);
            if (candidate == null) {
                continue;
            }
            Portal1Entity gate = SololevelingModEntities.PORTAL_1.get().spawn(level, candidate, MobSpawnType.MOB_SUMMONED);
            if (gate == null) {
                continue;
            }
            gate.moveTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D, player.getYRot() + 180.0F, 0.0F);
            gate.setPersistenceRequired();
            ProceduralDungeonRank rank = keyHunt
                    ? (random.nextBoolean() ? ProceduralDungeonRank.A : ProceduralDungeonRank.S)
                    : randomRankAtOrBelow(random, maximumRankFor(player));
            gate.getPersistentData().putUUID(OWNER_TAG, player.getUUID());
            gate.getPersistentData().putBoolean("slr_procedural_gate", true);
            gate.getPersistentData().putBoolean("slr_procedural_red_gate", false);
            gate.getPersistentData().putBoolean("slr_is_red_gate", false);
            gate.getPersistentData().putString("slr_procedural_rank", rank.name());
            gate.getPersistentData().putString("slr_procedural_theme", randomTheme(random).name());
            gate.getPersistentData().putInt("slr_procedural_complexity", complexityFor(rank, random));
            return gate;
        }
        return null;
    }

    /** Spawns a visible native SLR red gate already locked to Frost territory. */
    public static Entity spawnFrostGate(ServerPlayer player) {
        if (player == null || player.server == null || !Level.OVERWORLD.equals(player.level().dimension())) {
            return null;
        }
        ServerLevel level = player.serverLevel();
        Vec3 at = CampaignGates.nearbyFloor(player);
        if (at == null) {
            return null;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(RED_GATE_ID);
        if (type == null) {
            type = SololevelingModEntities.RED_GATE.get();
        }
        Entity gate = type.create(level);
        if (gate == null) {
            return null;
        }
        gate.moveTo(at.x, at.y, at.z, player.getYRot() + 180.0F, 0.0F);
        gate.setInvulnerable(true);
        gate.getPersistentData().putBoolean(CampaignGates.TAG, true);
        gate.getPersistentData().putBoolean("exit", false);
        gate.getPersistentData().putUUID(CampaignDirector.OWNER_TAG, player.getUUID());
        gate.getPersistentData().putLong("created", level.getGameTime());
        gate.getPersistentData().putBoolean(FROST_GATE_TAG, true);
        gate.getPersistentData().putUUID(OWNER_TAG, player.getUUID());
        gate.getPersistentData().putString(SnowRedGateArenaManager.TERRITORY_TAG, "frost");
        gate.getPersistentData().putBoolean("slr_is_red_gate", true);
        gate.setCustomNameVisible(false);
        if (!level.addFreshEntity(gate)) {
            return null;
        }
        return gate;
    }

    /** Capture origin before SLR clears instance tags or a dimension event advances the campaign. */
    public static void beforeNativeDungeonReturn(LevelAccessor sourceWorld, Entity returnPortal, Entity sourceEntity) {
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        player.getPersistentData().remove("sl3_native_normal_return");
        if (!(sourceWorld instanceof ServerLevel source) || !source.dimension().equals(player.level().dimension())
                || !CampaignDirector.stage(player).normalGates()) return;
        String receipt = player.getPersistentData().getString("dungeon_tag").trim();
        if (receipt.isBlank() && returnPortal != null) receipt = returnPortal.getPersistentData().getString("dungeon_tag").trim();
        if (receipt.isBlank() || !isNormalNativeDungeon(player, source, receipt)) return;
        CompoundTag origin = new CompoundTag();
        origin.putString("stage", CampaignDirector.stage(player).name());
        origin.putString("receipt", receipt);
        if (returnPortal != null) origin.putUUID("portal", returnPortal.getUUID());
        player.getPersistentData().put("sl3_native_normal_return", origin);
    }

    public static void onNativeDungeonReturn(LevelAccessor sourceWorld, Entity returnPortal, Entity sourceEntity) {
        if (!(sourceWorld instanceof ServerLevel source) || !(sourceEntity instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag origin = player.getPersistentData().getCompound("sl3_native_normal_return");
        player.getPersistentData().remove("sl3_native_normal_return");
        if (!Level.OVERWORLD.equals(player.level().dimension()) || !CampaignDirector.stage(player).normalGates()
                || !CampaignDirector.stage(player).name().equals(origin.getString("stage"))
                || (origin.hasUUID("portal") && (returnPortal == null || !returnPortal.getUUID().equals(origin.getUUID("portal"))))) {
            return;
        }
        String receipt = origin.getString("receipt");
        if (receipt.isBlank() || !GateCompletionTokens.contains(SololevelingModVariables.MapVariables.get(source).GatesCleared, receipt)) {
            return;
        }
        CompoundTag progress = CampaignDirector.progress(player);
        String committed = CampaignDirector.stage(player).name() + ":" + receipt;
        ListTag receipts = progress.getList("normal_receipts", Tag.TAG_STRING);
        boolean alreadyUsed = receipts.stream().map(Tag::getAsString).anyMatch(existing -> existing.endsWith(":" + receipt));
        if (!alreadyUsed) {
            receipts.add(StringTag.valueOf(committed));
            progress.put("normal_receipts", receipts);
            progress.remove("normal_gate_serial");
            progress.putLong("normal_gate_retry", player.serverLevel().getGameTime() + 80L);
        }
        progress.remove("pending_normal_receipt");
        if (returnPortal != null) {
            returnPortal.getPersistentData().putBoolean("sl3_campaign_normal_used", true);
        }
        CampaignDirector.changed(player);
    }

    public static boolean shouldBlockNativeFrostGateEntry(Entity gate, Entity sourceEntity) {
        return gate != null && gate.getPersistentData().getBoolean(FROST_GATE_TAG)
                && gate.getPersistentData().hasUUID(OWNER_TAG);
    }

    public static boolean enterFrost(ServerPlayer player, Entity gate) {
        if (player == null || gate == null || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!owns(player, gate)) {
            return false;
        }
        gate.getPersistentData().putString(SnowRedGateArenaManager.TERRITORY_TAG, "frost");
        if (gate instanceof RedGateEntity redGate) {
            return SnowRedGateArenaManager.enterLegacy(level, redGate, player);
        }
        return SnowRedGateArenaManager.enterProcedural(level, gate, player, java.util.List.of(player));
    }

    /**
     * Completes the authored Frost monarch fight without killing SLR's arena flow, then opens one scoped blue return portal near the player.
     */
    public static boolean fleeCompletion(ServerPlayer player, IceMonarchEntity boss) {
        if (player == null || boss == null || player.server == null || !(boss.level() instanceof ServerLevel level)) {
            return false;
        }
        CompoundTag bossData = boss.getPersistentData();
        if (bossData.getBoolean(FROST_COMPLETED_TAG)) {
            return ensureFrostExit(player);
        }
        String instanceId = firstNonBlank(
                bossData.getString("slr_dungeon_instance"),
                player.getPersistentData().getString("slr_dungeon_instance")
        );
        if (instanceId.isBlank()) {
            return false;
        }
        Optional<UUID> parsed = parseUuid(instanceId);
        if (parsed.isEmpty()) {
            return false;
        }
        DungeonInstanceSavedData data = DungeonInstanceSavedData.get(player.server);
        DungeonInstanceSavedData.Instance instance = data.getInstance(parsed.get()).orElse(null);
        if (instance == null) {
            return false;
        }

        CampaignDirector.progress(player).putString(FROST_INSTANCE_TAG, instance.id().toString());
        CampaignDirector.changed(player);
        instance.encounter("ice_monarch").ifPresent(encounter -> {
            encounter.clearTrackedMobs();
            encounter.markCompleted();
        });
        for (DungeonInstanceSavedData.EncounterState encounter : instance.encounters()) {
            if ("ice_monarch".equals(encounter.key())) {
                continue;
            }
            if (encounter.boss() && encounter.activated()) {
                encounter.markCompleted();
            }
        }
        instance.setCompleted(true);
        instance.setReturnPortalDeferred(true);
        instance.setReturnPortalSuppressed(false);
        data.setDirty();
        boolean opened = openScopedExitNearPlayer(player, level, instance);
        if (opened) {
            bossData.putBoolean(FROST_COMPLETED_TAG, true);
        }
        return opened;
    }

    public static boolean ensureFrostExit(ServerPlayer player) {
        if (player == null || player.server == null) {
            return false;
        }
        String instanceId = firstNonBlank(
                CampaignDirector.progress(player).getString(FROST_INSTANCE_TAG),
                player.getPersistentData().getString("slr_dungeon_instance")
        );
        Optional<DungeonInstanceSavedData.Instance> instance = parseUuid(instanceId)
                .flatMap(id -> DungeonInstanceSavedData.get(player.server).getInstance(id));
        if (instance.isEmpty()) {
            return false;
        }
        ServerLevel level = player.server.getLevel(instance.get().dimension());
        if (level == null) {
            return false;
        }
        instance.get().setCompleted(true);
        instance.get().setReturnPortalDeferred(true);
        instance.get().setReturnPortalSuppressed(false);
        DungeonInstanceSavedData.get(player.server).setDirty();
        return openScopedExitNearPlayer(player, level, instance.get());
    }

    public static void cleanupAfterFrostExit(ServerPlayer player) {
        if (player == null || player.server == null) {
            return;
        }
        String instanceId = firstNonBlank(
                player.getPersistentData().getString("slr_dungeon_instance"),
                CampaignDirector.progress(player).getString(FROST_INSTANCE_TAG)
        );
        parseUuid(instanceId).flatMap(id -> DungeonInstanceSavedData.get(player.server).getInstance(id))
                .filter(DungeonInstanceSavedData.Instance::completed)
                .ifPresent(instance -> SnowRedGateArenaManager.onParticipantExited(player.server, instance));
    }

    public static void completeEncounter(ServerPlayer player) {
        if (player == null || player.server == null) {
            return;
        }
        parseUuid(player.getPersistentData().getString("slr_dungeon_instance"))
                .flatMap(id -> DungeonInstanceSavedData.get(player.server).getInstance(id))
                .ifPresent(instance -> {
                    instance.setCompleted(true);
                    ServerLevel level = player.server.getLevel(instance.dimension());
                    if (level != null) {
                        openScopedExitNearPlayer(player, level, instance);
                    }
                });
    }

    private static boolean isNormalNativeDungeon(ServerPlayer player, ServerLevel source, String dungeonTag) {
        if (player.getPersistentData().getBoolean("slr_procedural_red_gate")) {
            return false;
        }
        ResourceLocation currentDimension = source.dimension().location();
        if (CARTENON_TEMPLE.equals(currentDimension) || !NORMAL_DUNGEON_DIMENSIONS.contains(currentDimension)) {
            return false;
        }
        String instanceId = player.getPersistentData().getString("slr_dungeon_instance");
        if (!instanceId.isBlank()) {
            Optional<DungeonInstanceSavedData.Instance> instance = parseUuid(instanceId)
                    .flatMap(id -> DungeonInstanceSavedData.get(player.server).getInstance(id));
            if (instance.isPresent()) {
                ResourceLocation dungeonId = instance.get().dungeonId();
                if (LEGIA_PRISON.equals(dungeonId) || CARTENON_TEMPLE.equals(dungeonId)) {
                    return false;
                }
                return isSlrNamespace(dungeonId) || player.getPersistentData().getBoolean("slr_procedural_dungeon");
            }
        }
        return player.getPersistentData().getBoolean("slr_procedural_dungeon") || !dungeonTag.startsWith("sl3_");
    }

    public static boolean isUsableNormalGate(Entity entity) {
        if (!(entity instanceof Portal1Entity gate)) {
            return false;
        }
        if (gate.getEntityData().get(Portal1Entity.DATA_usedbefore)) {
            return false;
        }
        CompoundTag tag = entity.getPersistentData();
        if (tag.getBoolean("sl3_campaign_normal_used") || tag.getBoolean("slr_procedural_red_gate")
                || tag.getBoolean("slr_is_red_gate") || tag.contains(SnowRedGateArenaManager.TERRITORY_TAG)) {
            return false;
        }
        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (type == null || !"sololeveling".equals(type.getNamespace())) {
            return false;
        }
        String path = type.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("red") || path.contains("beru") || path.contains("job") || path.contains("monarch") || path.contains("dummy_portal_red")) {
            return false;
        }
        String dungeon = tag.getString("slr_datapack_gate_dungeon");
        return dungeon.isBlank() || (!dungeon.equals(CARTENON_TEMPLE.toString()) && !dungeon.equals(LEGIA_PRISON.toString()));
    }



    private static boolean openScopedExitNearPlayer(ServerPlayer player, ServerLevel level, DungeonInstanceSavedData.Instance instance) {
        CompoundTag progress = CampaignDirector.progress(player);
        String instanceText = instance.id().toString();
        BlockPos exit = null;
        if (instanceText.equals(progress.getString(NATIVE_EXIT_INSTANCE_TAG)) && progress.contains(NATIVE_EXIT_POS_TAG, Tag.TAG_LONG)) {
            exit = BlockPos.of(progress.getLong(NATIVE_EXIT_POS_TAG));
        }
        if (exit == null) {
            exit = safeExitNear(player, level);
        }
        if (exit == null) {
            return false;
        }
        level.getChunkAt(exit);
        instance.setExit(exit);
        instance.setExitFacing(Direction.fromYRot(player.getYRot() + 180.0D));
        DungeonInstanceSavedData.get(player.server).setDirty();
        String dungeonTag = player.getPersistentData().getString("dungeon_tag");
        if (dungeonTag.isBlank()) {
            dungeonTag = instance.id().toString();
        }
        boolean opened = ProceduralDungeonCompletionHandler.reconcileReturnPortal(level, instance.id(), dungeonTag, exit);
        if (!opened) {
            opened = ProceduralDungeonCompletionHandler.spawnScopedReturnPortal(level, exit, instance.exitFacing().orElse(Direction.SOUTH), instance, dungeonTag);
        }
        if (opened) {
            progress.putString(NATIVE_EXIT_INSTANCE_TAG, instanceText);
            progress.putLong(NATIVE_EXIT_POS_TAG, exit.asLong());
            progress.putBoolean(FROST_EXIT_OPENED_TAG, true);
            CampaignDirector.changed(player);
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
                vars.BossKilled = true;
                vars.syncPlayerVariables(player);
            });
        }
        return opened;
    }

    private static BlockPos safeExitNear(ServerPlayer player, ServerLevel level) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();
        Vec3[] offsets = new Vec3[]{
                forward.scale(4.0D),
                forward.scale(6.0D),
                right.scale(4.0D),
                right.scale(-4.0D),
                Vec3.ZERO
        };
        for (Vec3 offset : offsets) {
            BlockPos base = BlockPos.containing(player.position().add(offset));
            for (int dy = 2; dy >= -4; dy--) {
                BlockPos feet = base.offset(0, dy, 0);
                if (!level.getWorldBorder().isWithinBounds(feet)) {
                    continue;
                }
                if (level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                        && level.getFluidState(feet).isEmpty()
                        && level.noCollision(new AABB(feet).inflate(0.35D, 0.0D, 0.35D).expandTowards(0.0D, 2.0D, 0.0D))) {
                    return feet;
                }
            }
        }
        return null;
    }

    private static BlockPos candidateNear(ServerPlayer player, RandomSource random, int attempt) {
        ServerLevel level = player.serverLevel();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        int radius = 48 + random.nextInt(49) + attempt * 12;
        int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * radius);
        int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * radius);
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        if (y <= level.getMinBuildHeight() + 1 || !level.getWorldBorder().isWithinBounds(pos)) {
            return null;
        }
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                || !level.getFluidState(pos).isEmpty()
                || !level.noCollision(new AABB(pos).inflate(1.5D, 0.0D, 1.5D).expandTowards(0.0D, 3.0D, 0.0D))) {
            return null;
        }
        return pos;
    }

    private static ProceduralDungeonRank maximumRankFor(ServerPlayer player) {
        SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new SololevelingModVariables.PlayerVariables());
        int certifiedRank = Mth.clamp((int) Math.floor(vars.HunterRank), 1, 6);
        int levelFloor;
        int playerLevel = Math.max(0, (int) Math.floor(vars.Level));
        if (playerLevel >= 100) {
            levelFloor = 6;
        } else if (playerLevel >= 75) {
            levelFloor = 5;
        } else if (playerLevel >= 50) {
            levelFloor = 4;
        } else if (playerLevel >= 30) {
            levelFloor = 3;
        } else if (playerLevel >= 15) {
            levelFloor = 2;
        } else {
            levelFloor = 1;
        }
        return ProceduralDungeonRank.values()[Math.max(certifiedRank, levelFloor) - 1];
    }

    private static ProceduralDungeonRank randomRankAtOrBelow(RandomSource random, ProceduralDungeonRank maximum) {
        ProceduralDungeonRank[] ranks = ProceduralDungeonRank.values();
        int totalWeight = 0;
        for (ProceduralDungeonRank rank : ranks) {
            if (rank.numericRank <= maximum.numericRank) {
                totalWeight += rankWeight(maximum, rank);
            }
        }
        int roll = random.nextInt(Math.max(1, totalWeight));
        for (ProceduralDungeonRank rank : ranks) {
            if (rank.numericRank <= maximum.numericRank) {
                roll -= rankWeight(maximum, rank);
                if (roll < 0) {
                    return rank;
                }
            }
        }
        return maximum;
    }

    private static int rankWeight(ProceduralDungeonRank maximum, ProceduralDungeonRank rank) {
        return switch (maximum.numericRank - rank.numericRank) {
            case 0 -> 40;
            case 1 -> 28;
            case 2 -> 17;
            default -> 9;
        };
    }

    private static DungeonTheme randomTheme(RandomSource random) {
        DungeonTheme[] themes = DungeonTheme.values();
        return themes[random.nextInt(themes.length)];
    }

    private static int complexityFor(ProceduralDungeonRank rank, RandomSource random) {
        return switch (rank) {
            case E -> 2 + random.nextInt(5);
            case D -> 3 + random.nextInt(5);
            case C -> 3 + random.nextInt(4);
            case B -> 5 + random.nextInt(3);
            case A -> 7 + random.nextInt(3);
            case S -> 8 + random.nextInt(3);
        };
    }

    private static boolean isSlrNamespace(ResourceLocation id) {
        return id != null && "sololeveling".equals(id.getNamespace());
    }

    private static boolean owns(ServerPlayer player, Entity gate) {
        CompoundTag tag = gate.getPersistentData();
        return tag.hasUUID(OWNER_TAG) && player.getUUID().equals(tag.getUUID(OWNER_TAG));
    }

    private static Optional<UUID> parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? (second == null ? "" : second) : first;
    }
}





