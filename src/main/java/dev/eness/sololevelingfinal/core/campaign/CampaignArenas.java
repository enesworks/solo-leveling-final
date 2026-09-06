package dev.eness.sololevelingfinal.core.campaign;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.campaign.arena.AntaresFinalArena;
import dev.eness.sololevelingfinal.core.campaign.arena.ThreeMonarchArena;
import dev.eness.sololevelingfinal.core.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Runtime owner for authored trio/final campaign arenas. */
public final class CampaignArenas {
    public static final ResourceKey<Level> ARENA = CampaignDirector.ARENA;

    public static final String OWNER_TAG = CampaignDirector.OWNER_TAG;
    public static final String ROLE_TAG = CampaignDirector.ROLE_TAG;
    public static final String KIND_TAG = "sl3_campaign_arena_kind";
    public static final String CENTER_TAG = "sl3_campaign_arena_center";

    private static final String ARENA_KIND = "arena_kind";
    private static final String ARENA_CENTER = "arena_center";
    private static final String ARENA_BUILT = "arena_built";
    private static final String ARENA_JOB_STEP = "arena_job_step";
    private static final String ARENA_MOBS = "arena_mobs";
    private static final String ARENA_ERROR = "arena_error";
    private static final String ARENA_ENTERING = "arena_entering";
    private static final String ARENA_LAST_INSIDE = "arena_last_inside";
    private static final String UUID_TAG = "uuid";
    private static final String LAST_POS = "lastpos";
    private static final String DEAD = "dead";
    private static final String ROLE_SILLAD = "sillad";
    private static final String ROLE_TARNAK = "tarnak";
    private static final String ROLE_RAKAN = "rakan";
    private static final String ROLE_ANTARES = "antares";
    private static final long STEP_BUDGET_NANOS = 6_000_000L;
    private static final int MAX_STEPS_PER_TICK = 16;
    private static final double RESCUE_DROP = 12.0D;

    private static final Map<UUID, BuildJob> BUILD_JOBS = new LinkedHashMap<>();

    private CampaignArenas() {
    }

    public static boolean enter(ServerPlayer player) {
        if (player == null || player.server == null || !player.isAlive() || player.isSpectator()) {
            return false;
        }
        ServerLevel arena = player.server.getLevel(ARENA);
        if (arena == null) {
            fail(player, "Missing arena dimension: " + ARENA.location());
            return false;
        }

        CampaignStage current = CampaignDirector.stage(player);
        Kind kind = kindForStage(current);
        if (kind == null) {
            return false;
        }

        CompoundTag record = CampaignDirector.progress(player);
        if (!kind.saveName().equals(record.getString(ARENA_KIND))) {
            allocateFreshArena(player, record, kind);
        } else if (record.contains(ARENA_ERROR) && !record.getBoolean(ARENA_BUILT)) {
            record.remove(ARENA_ERROR);
            BUILD_JOBS.remove(player.getUUID());
            CampaignDirector.changed(player);
        }

        record.putBoolean(ARENA_ENTERING, true);
        CampaignDirector.changed(player);
        if (!record.getBoolean(ARENA_BUILT)) {
            resumeBuild(player, arena, record);
            return true;
        }

        teleportToArena(player, arena, center(record));
        spawnAndMaintain(player, arena, record);
        return true;
    }

    public static void tick(ServerPlayer player) {
        if (player == null || player.server == null || player.level().isClientSide) {
            return;
        }
        CampaignStage current = CampaignDirector.stage(player);
        if (!current.activeDungeon()) {
            return;
        }

        CompoundTag record = CampaignDirector.progress(player);
        Kind kind = kindForPlayer(player, record);
        if (kind == null || record.contains(ARENA_ERROR)) {
            return;
        }
        ServerLevel arena = player.server.getLevel(ARENA);
        if (arena == null) {
            fail(player, "Missing arena dimension: " + ARENA.location());
            return;
        }

        if (!record.getBoolean(ARENA_BUILT)) {
            runBuild(player, arena, record);
            return;
        }

        if (!player.level().dimension().equals(ARENA)) {
            return;
        }
        BlockPos arenaCenter = center(record);
        rememberInside(player, arenaCenter);
        rescueFromVoid(player, arena, arenaCenter);
        spawnAndMaintain(player, arena, record);
        completeIfAllDead(player, record);
    }

    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead instanceof Mob mob) || dead.level().isClientSide) {
            return;
        }
        CompoundTag tag = mob.getPersistentData();
        if (!tag.hasUUID(OWNER_TAG) || !tag.contains(ROLE_TAG)) {
            return;
        }
        UUID owner = tag.getUUID(OWNER_TAG);
        String role = tag.getString(ROLE_TAG);
        MinecraftServer server = mob.getServer();
        if (server == null) {
            return;
        }

        CampaignSavedData data = CampaignSavedData.get(server);
        CompoundTag record = data.player(owner);
        CompoundTag mobs = record.getCompound(ARENA_MOBS);
        CompoundTag roleRecord = mobs.getCompound(role);
        if (!roleRecord.hasUUID(UUID_TAG) || !roleRecord.getUUID(UUID_TAG).equals(mob.getUUID())) {
            return;
        }

        roleRecord.putBoolean(DEAD, true);
        roleRecord.putLong(LAST_POS, mob.blockPosition().asLong());
        mobs.put(role, roleRecord);
        record.put(ARENA_MOBS, mobs);
        data.setDirty();

        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        if (player != null) {
            completeIfAllDead(player, record);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            continueBuild(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        BUILD_JOBS.clear();
    }

    public static void tagMob(Mob mob, ServerPlayer player, String role) {
        CompoundTag record = CampaignDirector.progress(player);
        CampaignDirector.tagMob(mob, player, role);
        mob.getPersistentData().putString(KIND_TAG, record.getString(ARENA_KIND));
        if (record.contains(ARENA_CENTER)) {
            mob.getPersistentData().putLong(CENTER_TAG, record.getLong(ARENA_CENTER));
        }
    }

    public static void completeEncounter(ServerPlayer player) {
        CompoundTag record = CampaignDirector.progress(player);
        record.remove(ARENA_ENTERING);
        CampaignDirector.changed(player);
        CampaignDirector.completeEncounter(player);
    }

    public static void rememberInside(ServerPlayer player, BlockPos center) {
        CompoundTag record = CampaignDirector.progress(player);
        record.putLong(ARENA_LAST_INSIDE, center.asLong());
        CampaignDirector.rememberInside(player, center);
    }

    private static void allocateFreshArena(ServerPlayer player, CompoundTag record, Kind kind) {
        BUILD_JOBS.remove(player.getUUID());
        int slot = CampaignSavedData.get(player.server).allocateArena();
        BlockPos arenaCenter = new BlockPos(Math.floorMod(slot, 128) * 512, 80, (slot / 128) * 512);
        record.putString(ARENA_KIND, kind.saveName());
        record.putLong(ARENA_CENTER, arenaCenter.asLong());
        record.putBoolean(ARENA_BUILT, false);
        record.putInt(ARENA_JOB_STEP, 0);
        record.put(ARENA_MOBS, new CompoundTag());
        record.remove(ARENA_ERROR);
        record.remove(ARENA_ENTERING);
        record.remove(ARENA_LAST_INSIDE);
        CampaignDirector.changed(player);
    }

    private static void resumeBuild(ServerPlayer player, ServerLevel arena, CompoundTag record) {
        runBuild(player, arena, record);
    }

    private static void continueBuild(ServerPlayer player) {
        if (player == null || player.server == null || !CampaignDirector.stage(player).activeDungeon()) {
            return;
        }
        CompoundTag record = CampaignDirector.progress(player);
        if (record.getBoolean(ARENA_BUILT) || record.contains(ARENA_ERROR)
                || kindForPlayer(player, record) == null) {
            return;
        }
        ServerLevel arena = player.server.getLevel(ARENA);
        if (arena != null) {
            runBuild(player, arena, record);
        }
    }

    private static void runBuild(ServerPlayer player, ServerLevel arena, CompoundTag record) {
        BuildJob job = null;
        long started = System.nanoTime();
        int steps = 0;
        try {
            job = BUILD_JOBS.computeIfAbsent(player.getUUID(), id -> createJob(arena, player, record));
            while (job.index < job.steps.size()
                    && steps < MAX_STEPS_PER_TICK
                    && System.nanoTime() - started < STEP_BUDGET_NANOS) {
                job.steps.get(job.index).run();
                job.index++;
                steps++;
            }
        } catch (RuntimeException error) {
            record.putString(ARENA_ERROR, error.toString());
            BUILD_JOBS.remove(player.getUUID());
            CampaignDirector.changed(player);
            int index = job == null ? record.getInt(ARENA_JOB_STEP) : job.index;
            int total = job == null ? -1 : job.steps.size();
            SoloLeveling3.LOGGER.error("[Arena] Build failed for {} at step {}/{}",
                    player.getGameProfile().getName(), index, total, error);
            return;
        }

        if (steps > 0) {
            record.putInt(ARENA_JOB_STEP, job.index);
            CampaignDirector.changed(player);
        }
        if (job.index < job.steps.size()) {
            return;
        }

        record.putBoolean(ARENA_BUILT, true);
        record.putInt(ARENA_JOB_STEP, job.index);
        BUILD_JOBS.remove(player.getUUID());
        CampaignDirector.changed(player);
        if (record.getBoolean(ARENA_ENTERING) && player.isAlive()) {
            teleportToArena(player, arena, job.center);
            spawnAndMaintain(player, arena, record);
        }
    }

    private static BuildJob createJob(ServerLevel arena, ServerPlayer player, CompoundTag record) {
        Kind kind = kindForPlayer(player, record);
        if (kind == null) {
            throw new IllegalStateException("Missing arena kind");
        }
        BlockPos arenaCenter = center(record);
        List<Runnable> all = kind.steps(arena, arenaCenter);
        int start = Math.max(0, Math.min(record.getInt(ARENA_JOB_STEP), all.size()));
        return new BuildJob(arenaCenter, all, start);
    }

    private static void teleportToArena(ServerPlayer player, ServerLevel arena, BlockPos arenaCenter) {
        arena.getChunkAt(arenaCenter);
        player.stopRiding();
        player.fallDistance = 0.0F;
        player.teleportTo(arena, arenaCenter.getX() + 0.5D, arenaCenter.getY() + 1.0D,
                arenaCenter.getZ() + 0.5D, 180.0F, 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        rememberInside(player, arenaCenter);
        CompoundTag record = CampaignDirector.progress(player);
        record.remove(ARENA_ENTERING);
        CampaignDirector.changed(player);
    }

    private static void spawnAndMaintain(ServerPlayer player, ServerLevel arena,
                                         CompoundTag record) {
        Kind kind = kindForPlayer(player, record);
        if (kind == null) {
            return;
        }
        for (RoleSpec role : kind.roles()) {
            maintainRole(player, arena, record, role);
        }
    }

    private static void maintainRole(ServerPlayer player, ServerLevel arena, CompoundTag record,
                                     RoleSpec role) {
        CompoundTag mobs = record.getCompound(ARENA_MOBS);
        CompoundTag saved = mobs.getCompound(role.name());
        if (saved.getBoolean(DEAD)) {
            return;
        }

        Mob mob = null;
        if (saved.hasUUID(UUID_TAG)) {
            BlockPos lookup = saved.contains(LAST_POS) ? BlockPos.of(saved.getLong(LAST_POS)) : role.blockPos(record);
            boolean waitingForLoadedLookup = CampaignGates.waitForLookup(arena, saved.getUUID(UUID_TAG), lookup);
            Entity entity = arena.getEntity(saved.getUUID(UUID_TAG));
            if (entity instanceof Mob found && found.isAlive()) {
                if (!ownsRecord(found, player, record, role.name())) {
                    return;
                }
                mob = found;
            } else if (waitingForLoadedLookup) {
                return;
            }
        }

        if (mob == null) {
            mob = spawnRole(player, arena, record, role);
            if (mob == null) {
                return;
            }
            saved = new CompoundTag();
            saved.putUUID(UUID_TAG, mob.getUUID());
            saved.putBoolean(DEAD, false);
        }

        prepareMob(player, mob, role.name(), false);
        saved.putLong(LAST_POS, mob.blockPosition().asLong());
        mobs.put(role.name(), saved);
        record.put(ARENA_MOBS, mobs);
        CampaignDirector.changed(player);
    }

    private static Mob spawnRole(ServerPlayer player, ServerLevel arena, CompoundTag record, RoleSpec role) {
        Vec3 pos = role.spawn(record);
        BlockPos chunkPos = BlockPos.containing(pos);
        arena.getChunkAt(chunkPos);
        Mob mob = role.type().create(arena);
        if (mob == null) {
            fail(player, "Could not create arena mob: " + role.name());
            return null;
        }
        mob.moveTo(pos.x, pos.y, pos.z, yawToward(pos, player.position()), 0.0F);
        mob.finalizeSpawn(arena, arena.getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.EVENT, null, null);
        prepareMob(player, mob, role.name(), true);
        if (!arena.addFreshEntity(mob)) {
            mob.discard();
            fail(player, "Could not add arena mob: " + role.name());
            return null;
        }
        CampaignGates.waitForLookup(arena, mob.getUUID(), mob.blockPosition());
        return mob;
    }

    private static void prepareMob(ServerPlayer player, Mob mob, String role, boolean restoreFlags) {
        tagMob(mob, player, role);
        if (restoreFlags) {
            if (mob.isNoAi()) {
                mob.setNoAi(false);
            }
            if (mob.isInvulnerable()) {
                mob.setInvulnerable(false);
            }
        }
        var follow = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null && follow.getBaseValue() < 256.0D) {
            follow.setBaseValue(256.0D);
        }
        if (mob.tickCount <= 1 || mob.tickCount % 20 == 0 || mob.getTarget() == null
                || !player.getUUID().equals(mob.getTarget().getUUID())) {
            mob.setTarget(player);
        }
    }

    private static void completeIfAllDead(ServerPlayer player, CompoundTag record) {
        Kind kind = kindForPlayer(player, record);
        if (kind == null) {
            return;
        }
        CompoundTag mobs = record.getCompound(ARENA_MOBS);
        for (RoleSpec role : kind.roles()) {
            CompoundTag saved = mobs.getCompound(role.name());
            if (!saved.getBoolean(DEAD)) {
                return;
            }
        }
        completeEncounter(player);
    }

    private static boolean ownsRecord(Mob mob, ServerPlayer player, CompoundTag record, String role) {
        CompoundTag tag = mob.getPersistentData();
        return tag.hasUUID(OWNER_TAG)
                && player.getUUID().equals(tag.getUUID(OWNER_TAG))
                && role.equals(tag.getString(ROLE_TAG))
                && record.getString(ARENA_KIND).equals(tag.getString(KIND_TAG))
                && (!record.contains(ARENA_CENTER) || tag.getLong(CENTER_TAG) == record.getLong(ARENA_CENTER));
    }

    private static void rescueFromVoid(ServerPlayer player, ServerLevel arena, BlockPos arenaCenter) {
        if (player.getY() >= arenaCenter.getY() - RESCUE_DROP) {
            return;
        }
        player.teleportTo(arena, arenaCenter.getX() + 0.5D, arenaCenter.getY() + 1.0D,
                arenaCenter.getZ() + 0.5D, 180.0F, 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static void fail(ServerPlayer player, String error) {
        CompoundTag record = CampaignDirector.progress(player);
        if (!record.contains(ARENA_ERROR)) {
            record.putString(ARENA_ERROR, error);
            CampaignDirector.changed(player);
            SoloLeveling3.LOGGER.error("[Arena] {}", error);
        }
    }

    private static Kind kindForStage(CampaignStage stage) {
        return switch (stage) {
            case TRIO_READY, TRIO_ACTIVE -> Kind.TRIO;
            case FINAL_READY, FINAL_ACTIVE -> Kind.FINAL;
            default -> null;
        };
    }

    private static Kind kindForPlayer(ServerPlayer player, CompoundTag record) {
        Kind expected = kindForStage(CampaignDirector.stage(player));
        if (expected == null) {
            return null;
        }
        return expected.saveName().equals(record.getString(ARENA_KIND)) ? expected : null;
    }

    private static BlockPos center(CompoundTag record) {
        return record.contains(ARENA_CENTER) ? BlockPos.of(record.getLong(ARENA_CENTER)) : new BlockPos(0, 80, 0);
    }

    private static float yawToward(Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        if (delta.horizontalDistanceSqr() < 1.0E-6D) {
            return 0.0F;
        }
        return (float) (Math.atan2(delta.z, delta.x) * 180.0D / Math.PI) - 90.0F;
    }

    private static final class BuildJob {
        private final BlockPos center;
        private final List<Runnable> steps;
        private int index;

        private BuildJob(BlockPos center, List<Runnable> steps, int index) {
            this.center = center;
            this.steps = List.copyOf(steps);
            this.index = index;
        }
    }

    private record RoleSpec(String name, EntityType<? extends Mob> type, Vec3Offset offset) {
        private Vec3 spawn(CompoundTag record) {
            return offset.apply(center(record));
        }

        private BlockPos blockPos(CompoundTag record) {
            return BlockPos.containing(spawn(record));
        }
    }

    private interface Vec3Offset {
        Vec3 apply(BlockPos center);
    }

    private enum Kind {
        TRIO(CampaignStage.TRIO_ACTIVE) {
            @Override
            List<Runnable> steps(ServerLevel level, BlockPos center) {
                return ThreeMonarchArena.steps(level, center, 0L);
            }

            @Override
            List<RoleSpec> roles() {
                return List.of(
                        new RoleSpec(ROLE_SILLAD, ModEntities.ICE_MONARCH.get(),
                                c -> ThreeMonarchArena.silladSpawnPoint(null, c)),
                        new RoleSpec(ROLE_TARNAK, ModEntities.TARNAK.get(),
                                c -> ThreeMonarchArena.tarnakSpawnPoint(null, c)),
                        new RoleSpec(ROLE_RAKAN, ModEntities.RAKAN.get(),
                                c -> ThreeMonarchArena.rakanSpawnPoint(null, c))
                );
            }
        },
        FINAL(CampaignStage.FINAL_ACTIVE) {
            @Override
            List<Runnable> steps(ServerLevel level, BlockPos center) {
                return AntaresFinalArena.steps(level, center, 0L);
            }

            @Override
            List<RoleSpec> roles() {
                return List.of(new RoleSpec(ROLE_ANTARES, ModEntities.ANTARES.get(),
                        c -> AntaresFinalArena.spawnPoint(null, c)));
            }
        };

        private final CampaignStage activeStage;

        Kind(CampaignStage activeStage) {
            this.activeStage = activeStage;
        }

        String saveName() {
            return activeStage.name();
        }

        abstract List<Runnable> steps(ServerLevel level, BlockPos center);

        abstract List<RoleSpec> roles();
    }
}
