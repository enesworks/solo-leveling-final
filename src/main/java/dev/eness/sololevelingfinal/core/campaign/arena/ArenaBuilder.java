package dev.eness.sololevelingfinal.core.campaign.arena;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Small shared placement helpers for authored campaign arenas. */
public final class ArenaBuilder {
    private static final int WORLD_LIMIT = 29_999_984;
    private static final java.util.concurrent.atomic.AtomicLong CLIPPED =
            new java.util.concurrent.atomic.AtomicLong();
    private static long lastReported;

    private ArenaBuilder() {
    }

    public static boolean inBounds(ServerLevel level, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return y >= level.getMinBuildHeight()
                && y < level.getMaxBuildHeight()
                && Math.abs(x) <= WORLD_LIMIT
                && Math.abs(z) <= WORLD_LIMIT;
    }

    public static boolean set(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        if (!inBounds(level, pos)) {
            CLIPPED.incrementAndGet();
            return false;
        }
        return level.setBlock(pos, state, flags);
    }

    public static boolean ensureChunk(ServerLevel level, int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (level.hasChunk(chunkX, chunkZ)) {
            return false;
        }
        level.getChunk(chunkX, chunkZ);
        return true;
    }

    public static void reportClipped(String what) {
        long now = CLIPPED.get();
        if (now == lastReported) {
            return;
        }
        SoloLeveling3.LOGGER.warn(
                "[Arena] '{}': {} blocks were outside world/build height bounds and were skipped.",
                what,
                now - lastReported
        );
        lastReported = now;
    }

    public static int updateFlags() {
        return Block.UPDATE_CLIENTS;
    }
}
