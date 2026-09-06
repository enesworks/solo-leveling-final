package dev.eness.sololevelingfinal.core.campaign;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Owner-bound blue gates reuse SLR's visual while the campaign owns routing. */
public final class CampaignGates {
    public static final String TAG = "sl3_campaign_gate";
    private static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("sololeveling", "dummy_portal_normal");
    private static final Map<UUID, Long> MISSING_SINCE = new HashMap<>();
    private static final TicketType<UUID> LOOKUP_TICKET = TicketType.create("sl3_gate_lookup", UUID::compareTo, 80);

    /** Entity chunks finish loading asynchronously; a missing UUID needs a fresh lookup grace. */
    public static boolean waitForLookup(ServerLevel level, UUID id, BlockPos position) {
        level.getChunkSource().addRegionTicket(LOOKUP_TICKET, new ChunkPos(position), 2, id);
        level.getChunkAt(position);
        Entity existing = level.getEntity(id);
        if (existing != null && !existing.isRemoved()) { MISSING_SINCE.remove(id); return true; }
        long now = level.getGameTime();
        long since = MISSING_SINCE.computeIfAbsent(id, ignored -> now);
        if (now < since) { MISSING_SINCE.put(id, now); return true; }
        return now - since < 60;
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { MISSING_SINCE.clear(); }

    public static Entity ensure(ServerPlayer player, boolean exit) {
        CompoundTag record = CampaignDirector.progress(player);
        String key = exit ? "exit_gate" : "entry_gate";
        CompoundTag gate = record.getCompound(key);
        ServerLevel level = player.serverLevel();
        if (gate.hasUUID("id") && gate.getString("dimension").equals(level.dimension().location().toString())) {
            BlockPos old = BlockPos.of(gate.getLong("position"));
            boolean waiting = waitForLookup(level, gate.getUUID("id"), old);
            Entity existing = level.getEntity(gate.getUUID("id"));
            if (existing != null && !existing.isRemoved()) return existing;
            if (waiting) return null;
            MISSING_SINCE.remove(gate.getUUID("id"));
        }
        Vec3 at = nearbyFloor(player);
        if (at == null) return null;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(TYPE);
        if (type == null) throw new IllegalStateException("Missing SLR blue gate: " + TYPE);
        Entity entity = type.create(level);
        if (entity == null) return null;
        entity.moveTo(at.x, at.y, at.z, player.getYRot() + 180, 0);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.getPersistentData().putBoolean(TAG, true);
        entity.getPersistentData().putBoolean("exit", exit);
        entity.getPersistentData().putUUID(CampaignDirector.OWNER_TAG, player.getUUID());
        entity.getPersistentData().putLong("created", level.getGameTime());
        if (entity instanceof Mob mob) { mob.setNoAi(true); mob.setPersistenceRequired(); }
        if (!level.addFreshEntity(entity)) return null;
        CompoundTag saved = new CompoundTag();
        saved.putUUID("id", entity.getUUID());
        saved.putString("dimension", level.dimension().location().toString());
        saved.putLong("position", entity.blockPosition().asLong());
        saved.putLong("last_lookup", level.getGameTime());
        record.put(key, saved);
        CampaignDirector.changed(player);
        return entity;
    }

    public static void remove(ServerPlayer player, boolean exit) {
        CompoundTag record = CampaignDirector.progress(player);
        String key = exit ? "exit_gate" : "entry_gate";
        CompoundTag tag = record.getCompound(key);
        ResourceLocation location = ResourceLocation.tryParse(tag.getString("dimension"));
        if (tag.hasUUID("id") && location != null) {
            ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, location));
            if (level != null) {
                level.getChunkAt(BlockPos.of(tag.getLong("position")));
                Entity entity = level.getEntity(tag.getUUID("id"));
                if (entity != null && entity.getPersistentData().hasUUID(CampaignDirector.OWNER_TAG)
                        && entity.getPersistentData().getUUID(CampaignDirector.OWNER_TAG).equals(player.getUUID())) entity.discard();
            }
        }
        record.remove(key);
        CampaignDirector.changed(player);
    }

    public static boolean canUse(ServerPlayer player, Entity gate) {
        CompoundTag tag = gate.getPersistentData();
        CompoundTag current = CampaignDirector.progress(player).getCompound(tag.getBoolean("exit") ? "exit_gate" : "entry_gate");
        return tag.getBoolean(TAG) && tag.hasUUID(CampaignDirector.OWNER_TAG)
                && current.hasUUID("id") && gate.getUUID().equals(current.getUUID("id"))
                && tag.getUUID(CampaignDirector.OWNER_TAG).equals(player.getUUID())
                && player.serverLevel().getGameTime() - tag.getLong("created") >= 20
                && player.isAlive() && !player.isSpectator();
    }

    /** Never clears or replaces the player's terrain to fit a gate. */
    public static Vec3 nearbyFloor(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double facing = Math.toRadians(player.getYRot());
        for (int radius : new int[]{6, 8, 4, 10}) for (int side = 0; side < 8; side++) {
            double angle = facing + side * Math.PI / 4;
            int x = (int)Math.floor(player.getX() - Math.sin(angle) * radius);
            int z = (int)Math.floor(player.getZ() + Math.cos(angle) * radius);
            for (int dy = 2; dy >= -5; dy--) {
                BlockPos feet = new BlockPos(x, player.blockPosition().getY() + dy, z);
                if (!level.getWorldBorder().isWithinBounds(feet)
                        || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                        || !level.getFluidState(feet).isEmpty()) continue;
                AABB volume = new AABB(x - .3, feet.getY(), z - .3, x + 1.3, feet.getY() + 4, z + 1.3);
                if (level.noCollision(volume)) return new Vec3(x + .5, feet.getY(), z + .5);
            }
        }
        return null;
    }

    public static void rememberReturn(ServerPlayer player) {
        CompoundTag data = CampaignDirector.progress(player);
        data.putString("return_dimension", player.level().dimension().location().toString());
        data.putDouble("return_x", player.getX()); data.putDouble("return_y", player.getY()); data.putDouble("return_z", player.getZ());
        data.putFloat("return_yaw", player.getYRot());
        CampaignDirector.changed(player);
    }

    public static boolean returnPlayer(ServerPlayer player) {
        CompoundTag data = CampaignDirector.progress(player);
        ResourceLocation id = ResourceLocation.tryParse(data.getString("return_dimension"));
        ServerLevel target = id == null ? player.server.overworld() : player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (target == null) target = player.server.overworld();
        double x = data.getDouble("return_x"), y = data.getDouble("return_y"), z = data.getDouble("return_z");
        Vec3 safe = safeReturn(target, BlockPos.containing(x, y, z), player);
        if (safe == null) safe = safeReturn(target, target.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getSharedSpawnPos()), player);
        if (safe == null) return false;
        player.stopRiding(); player.fallDistance = 0;
        player.teleportTo(target, safe.x, safe.y, safe.z, data.getFloat("return_yaw"), 0);
        player.setDeltaMovement(Vec3.ZERO);
        return player.level().dimension().equals(target.dimension());
    }

    private static Vec3 safeReturn(ServerLevel level, BlockPos origin, ServerPlayer player) {
        for (int radius = 0; radius <= 8; radius++) for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
            for (int dy = 3; dy >= -8; dy--) {
                BlockPos feet = origin.offset(dx, dy, dz);
                if (feet.getY() < level.getMinBuildHeight() + 1 || feet.getY() >= level.getMaxBuildHeight() - 2 || !level.getWorldBorder().isWithinBounds(feet)) continue;
                level.getChunkAt(feet);
                if (!level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                        || !level.getFluidState(feet).isEmpty() || !level.getFluidState(feet.above()).isEmpty()) continue;
                Vec3 at = Vec3.atBottomCenterOf(feet);
                if (level.noCollision(player.getBoundingBox().move(at.subtract(player.position())))) return at;
            }
        }
        return null;
    }

    private CampaignGates() {}
}
