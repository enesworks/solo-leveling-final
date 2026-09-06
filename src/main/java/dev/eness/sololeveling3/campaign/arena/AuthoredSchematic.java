package dev.eness.sololeveling3.campaign.arena;

import dev.eness.sololeveling3.SoloLeveling3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/** Sponge v2 schematic reader for authored campaign arena assets. */
public final class AuthoredSchematic {
    private static final int ROWS_PER_STEP = 4;
    private static final long MAX_VOLUME = 8_000_000L;

    public record Spec(ResourceLocation resource, int width, int height, int length,
                       int anchorX, int groundLayer, int anchorZ, String label) {
        public Spec {
            if (width <= 0 || height <= 0 || length <= 0
                    || anchorX < 0 || anchorX >= width
                    || anchorZ < 0 || anchorZ >= length
                    || groundLayer < 0 || groundLayer >= height) {
                throw new IllegalArgumentException("Invalid schematic spec: " + label);
            }
        }
    }

    private AuthoredSchematic() {
    }

    public static List<Runnable> steps(ServerLevel level, BlockPos centre, Spec spec) {
        Blueprint blueprint = load(level, spec);
        int originY = centre.getY() - spec.groundLayer();
        List<Runnable> out = new ArrayList<>();

        for (long packed : chunks(centre, spec)) {
            int chunkX = (int) (packed >> 32);
            int chunkZ = (int) packed;
            out.add(() -> ArenaBuilder.ensureChunk(level, chunkX << 4, chunkZ << 4));
        }

        for (int y = 0; y < spec.height(); y++) {
            for (int z0 = 0; z0 < spec.length(); z0 += ROWS_PER_STEP) {
                int layer = y;
                int fromZ = z0;
                int toZ = Math.min(spec.length(), z0 + ROWS_PER_STEP);
                out.add(() -> placeSlice(level, centre, originY, spec, blueprint, layer, fromZ, toZ));
            }
        }

        out.add(() -> {
            ArenaBuilder.reportClipped(spec.label());
            SoloLeveling3.LOGGER.info(
                    "[Arena] Placed authored schematic {}: {}x{}x{} @ {}",
                    spec.label(),
                    spec.width(),
                    spec.height(),
                    spec.length(),
                    centre
            );
        });
        return List.copyOf(out);
    }

    public static int placedGroundY(BlockPos centre, Spec spec) {
        return centre.getY();
    }

    private static Set<Long> chunks(BlockPos centre, Spec spec) {
        Set<Long> chunks = new LinkedHashSet<>();
        int minX = centre.getX() - spec.anchorX();
        int minZ = centre.getZ() - spec.anchorZ();
        int maxX = minX + spec.width() - 1;
        int maxZ = minZ + spec.length() - 1;
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                chunks.add((((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL));
            }
        }
        return chunks;
    }

    private static void placeSlice(ServerLevel level, BlockPos centre, int originY,
                                   Spec spec, Blueprint blueprint, int y,
                                   int fromZ, int toZ) {
        for (int z = fromZ; z < toZ; z++) {
            for (int x = 0; x < spec.width(); x++) {
                BlockState wanted = blueprint.at(x, y, z, spec);
                if (wanted.isAir() && y <= spec.groundLayer()) {
                    continue;
                }
                BlockPos world = new BlockPos(
                        centre.getX() + x - spec.anchorX(),
                        originY + y,
                        centre.getZ() + z - spec.anchorZ()
                );
                BlockState current = level.getBlockState(world);
                if (current.equals(wanted) || (wanted.isAir() && current.isAir())) {
                    continue;
                }
                ArenaBuilder.set(level, world, wanted, ArenaBuilder.updateFlags());
            }
        }
    }

    private static Blueprint load(ServerLevel level, Spec spec) {
        try (InputStream packed = level.getServer().getResourceManager().open(spec.resource());
             InputStream zipped = new GZIPInputStream(packed);
             DataInputStream input = new DataInputStream(zipped)) {
            CompoundTag root = NbtIo.read(input, NbtAccounter.UNLIMITED);
            int version = root.getInt("Version");
            int width = root.getShort("Width") & 0xFFFF;
            int height = root.getShort("Height") & 0xFFFF;
            int length = root.getShort("Length") & 0xFFFF;
            if (version != 2 || width != spec.width() || height != spec.height()
                    || length != spec.length()) {
                throw new IOException("Unexpected schematic header: v" + version + " "
                        + width + "x" + height + "x" + length);
            }
            long volume = (long) width * height * length;
            if (volume > MAX_VOLUME) {
                throw new IOException("Schematic volume exceeds safety limit: " + volume);
            }

            CompoundTag paletteTag = root.getCompound("Palette");
            int paletteSize = root.getInt("PaletteMax");
            if (paletteSize <= 0 || paletteSize > 1024) {
                throw new IOException("Invalid palette size: " + paletteSize);
            }
            BlockState[] palette = new BlockState[paletteSize];
            for (String stateSpec : paletteTag.getAllKeys()) {
                int id = paletteTag.getInt(stateSpec);
                if (id < 0 || id >= paletteSize || palette[id] != null) {
                    throw new IOException("Invalid or duplicate palette id: " + id);
                }
                palette[id] = parseState(stateSpec);
            }
            for (int id = 0; id < palette.length; id++) {
                if (palette[id] == null) {
                    throw new IOException("Missing palette id: " + id);
                }
            }

            int[] ids = decodeVarInts(root.getByteArray("BlockData"), (int) volume);
            for (int id : ids) {
                if (id < 0 || id >= palette.length) {
                    throw new IOException("BlockData palette id out of range: " + id);
                }
            }
            return new Blueprint(palette, ids);
        } catch (Exception error) {
            throw new IllegalStateException("Could not load authored arena schematic "
                    + spec.resource() + " (" + spec.label() + ")", error);
        }
    }

    private static int[] decodeVarInts(byte[] input, int expected) throws IOException {
        int[] output = new int[expected];
        int out = 0;
        int value = 0;
        int shift = 0;
        for (byte rawByte : input) {
            int raw = rawByte & 0xFF;
            value |= (raw & 0x7F) << shift;
            if ((raw & 0x80) != 0) {
                shift += 7;
                if (shift > 35) {
                    throw new IOException("VarInt is too long");
                }
            } else {
                if (out >= expected) {
                    throw new IOException("BlockData has extra cells");
                }
                output[out++] = value;
                value = 0;
                shift = 0;
            }
        }
        if (shift != 0 || out != expected) {
            throw new IOException("BlockData cell count " + out + ", expected " + expected);
        }
        return output;
    }

    private static BlockState parseState(String stateSpec) throws IOException {
        int bracket = stateSpec.indexOf('[');
        String name = bracket < 0 ? stateSpec : stateSpec.substring(0, bracket);
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null || !ForgeRegistries.BLOCKS.containsKey(id)) {
            throw new IOException("Unregistered block: " + name);
        }
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        if (block == null || block == Blocks.AIR && !"minecraft:air".equals(name)) {
            throw new IOException("Null block: " + name);
        }
        BlockState state = block.defaultBlockState();
        if (bracket < 0) {
            return state;
        }
        if (!stateSpec.endsWith("]")) {
            throw new IOException("Malformed block state: " + stateSpec);
        }
        String body = stateSpec.substring(bracket + 1, stateSpec.length() - 1);
        if (body.isBlank()) {
            return state;
        }
        for (String assignment : body.split(",")) {
            String[] pair = assignment.split("=", 2);
            if (pair.length != 2) {
                throw new IOException("Malformed property: " + assignment);
            }
            Property<?> property = block.getStateDefinition().getProperty(pair[0]);
            if (property == null) {
                throw new IOException("Unknown property: " + assignment);
            }
            state = setProperty(state, property, pair[1]);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState setProperty(
            BlockState state, Property<T> property, String value) throws IOException {
        T parsed = property.getValue(value).orElseThrow(
                () -> new IOException("Invalid property value: "
                        + property.getName() + "=" + value)
        );
        return state.setValue(property, parsed);
    }

    private record Blueprint(BlockState[] palette, int[] ids) {
        private BlockState at(int x, int y, int z, Spec spec) {
            int index = x + z * spec.width() + y * spec.width() * spec.length();
            return palette[ids[index]];
        }
    }
}
