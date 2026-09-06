package dev.eness.sololeveling3.story;

import dev.eness.sololeveling3.SoloLeveling3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.solocraft.dungeon.DatapackDungeonGateHandler;
import net.solocraft.dungeon.ProceduralDungeonRank;
import net.solocraft.entity.DatapackGateEntity;

import java.util.UUID;

/** Creates and identifies the story-bound gate leading to Legia's prison. */
public final class LegiaGateSupport {
    public static final String GATE_TAG = "sl3_legia_gate";
    public static final String OWNER_TAG = "sl3_legia_gate_owner";
    public static final ResourceLocation DUNGEON_ID = ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "legia_prison");

    private LegiaGateSupport() {
    }

    /**
     * Initializes an SLR datapack gate without losing the destination reserved
     * by the blue gate it replaces. Calling only SLR's initializeSpawn method
     * would leave tpx/tpy/tpz at zero because this entity is not spawned via a
     * spawn egg/finalizeSpawn path.
     */
    public static boolean prepare(DatapackGateEntity replacement, Entity sourceGate, UUID owner) {
        if (replacement == null || sourceGate == null || owner == null) {
            return false;
        }

        DatapackDungeonGateHandler.initializeSpawn(replacement, MobSpawnType.MOB_SUMMONED);
        copyOrCreateDungeonTarget(sourceGate, replacement);
        if (!DatapackDungeonGateHandler.bind(replacement, DUNGEON_ID, ProceduralDungeonRank.S)) {
            return false;
        }

        replacement.setTexture("portalgate2");
        replacement.getPersistentData().putBoolean(GATE_TAG, true);
        replacement.getPersistentData().putUUID(OWNER_TAG, owner);
        replacement.getPersistentData().putDouble("PortalLife", 0.0D);
        return true;
    }

    public static boolean isLegiaGate(Entity entity) {
        return entity != null && entity.getPersistentData().getBoolean(GATE_TAG);
    }

    private static void copyOrCreateDungeonTarget(Entity source, Entity replacement) {
        CompoundTag sourceData = source.getPersistentData();
        CompoundTag replacementData = replacement.getPersistentData();
        if (hasNumericTarget(sourceData)) {
            replacementData.putDouble("tpx", sourceData.getDouble("tpx"));
            replacementData.putDouble("tpy", sourceData.getDouble("tpy"));
            replacementData.putDouble("tpz", sourceData.getDouble("tpz"));
            return;
        }

        long seed = source.getUUID().getMostSignificantBits()
                ^ source.getUUID().getLeastSignificantBits()
                ^ Double.doubleToLongBits(source.getX())
                ^ Long.rotateLeft(Double.doubleToLongBits(source.getZ()), 17);
        RandomSource random = RandomSource.create(seed);
        replacementData.putDouble("tpx", Mth.nextInt(random, -299999, 299999));
        replacementData.putDouble("tpy", Mth.nextInt(random, 60, 120));
        replacementData.putDouble("tpz", Mth.nextInt(random, -299999, 299999));
    }

    private static boolean hasNumericTarget(CompoundTag data) {
        return data.contains("tpx", Tag.TAG_ANY_NUMERIC)
                && data.contains("tpy", Tag.TAG_ANY_NUMERIC)
                && data.contains("tpz", Tag.TAG_ANY_NUMERIC)
                && data.getDouble("tpy") > 0.0D;
    }
}
