package dev.eness.sololevelingfinal.core.campaign.arena;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Authored Three Monarchs arena. The caller owns checkpoints and encounter state. */
public final class ThreeMonarchArena {
    public static final ResourceLocation MASTER_V1 = ResourceLocation.fromNamespaceAndPath(
            SoloLeveling3.MOD_ID,
            "schematics/three_monarchs_master_v1.schem"
    );
    public static final AuthoredSchematic.Spec SCHEMATIC = new AuthoredSchematic.Spec(
            MASTER_V1,
            257,
            80,
            257,
            128,
            16,
            128,
            "three_monarchs_master_v1"
    );

    public static final int HALF_SIZE = 128;
    public static final int CENTRAL_RADIUS = 64;
    public static final int ENCOUNTER_RADIUS = 124;

    public static final int SILLAD_X = -94;
    public static final int SILLAD_Z = -34;
    public static final int TARNAK_X = 94;
    public static final int TARNAK_Z = -34;
    public static final int RAKAN_X = 0;
    public static final int RAKAN_Z = 100;

    private ThreeMonarchArena() {
    }

    public static List<Runnable> steps(ServerLevel level, BlockPos center, long seed) {
        return AuthoredSchematic.steps(level, center, SCHEMATIC);
    }

    public static int feetY(ServerLevel level, BlockPos center) {
        return center.getY() + 1;
    }

    public static Vec3 silladSpawnPoint(ServerLevel level, BlockPos center) {
        return spawnPoint(center, SILLAD_X, SILLAD_Z);
    }

    public static Vec3 tarnakSpawnPoint(ServerLevel level, BlockPos center) {
        return spawnPoint(center, TARNAK_X, TARNAK_Z);
    }

    public static Vec3 rakanSpawnPoint(ServerLevel level, BlockPos center) {
        return spawnPoint(center, RAKAN_X, RAKAN_Z);
    }

    private static Vec3 spawnPoint(BlockPos center, int offsetX, int offsetZ) {
        return new Vec3(center.getX() + offsetX + 0.5D, center.getY() + 1.0D,
                center.getZ() + offsetZ + 0.5D);
    }
}
