package dev.eness.sololeveling3.campaign.arena;

import dev.eness.sololeveling3.SoloLeveling3;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Authored Antares throne arena. The caller owns checkpoints and encounter state. */
public final class AntaresFinalArena {
    public static final ResourceLocation MASTER_V1 = ResourceLocation.fromNamespaceAndPath(
            SoloLeveling3.MOD_ID,
            "schematics/antares_final_master_v1.schem"
    );
    public static final AuthoredSchematic.Spec SCHEMATIC = new AuthoredSchematic.Spec(
            MASTER_V1,
            141,
            64,
            141,
            70,
            8,
            70,
            "antares_final_master_v1"
    );

    public static final int OUTER_RADIUS = 64;
    public static final int ENCOUNTER_RADIUS = 62;

    public static final int ANTARES_THRONE_X = 0;
    public static final int ANTARES_THRONE_Z = -53;
    public static final int ANTARES_THRONE_FLOOR_Y_OFFSET = 6;
    public static final int ANTARES_THRONE_FEET_Y_OFFSET = 7;

    private AntaresFinalArena() {
    }

    public static List<Runnable> steps(ServerLevel level, BlockPos center, long seed) {
        return AuthoredSchematic.steps(level, center, SCHEMATIC);
    }

    public static int feetY(ServerLevel level, BlockPos center) {
        return center.getY() + 1;
    }

    public static Vec3 spawnPoint(ServerLevel level, BlockPos center) {
        return new Vec3(
                center.getX() + ANTARES_THRONE_X + 0.5D,
                center.getY() + ANTARES_THRONE_FEET_Y_OFFSET,
                center.getZ() + ANTARES_THRONE_Z + 0.5D
        );
    }
}
