package dev.eness.sololevelingfinal.core.campaign;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** World-owned progression survives player death, clone and dimension changes. */
public final class CampaignSavedData extends SavedData {
    private final Map<UUID, CompoundTag> players = new LinkedHashMap<>();
    private int nextArena;

    public static CampaignSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                CampaignSavedData::load, CampaignSavedData::new, "solo_leveling_final_campaign");
    }

    public CompoundTag player(UUID id) {
        return players.computeIfAbsent(id, key -> { setDirty(); return new CompoundTag(); });
    }

    public int allocateArena() { setDirty(); return nextArena++; }

    private static CampaignSavedData load(CompoundTag root) {
        CampaignSavedData data = new CampaignSavedData();
        data.nextArena = Math.max(0, root.getInt("nextArena"));
        for (Tag item : root.getList("players", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag)item;
            if (entry.hasUUID("id")) data.players.put(entry.getUUID("id"), entry.getCompound("progress"));
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag root) {
        root.putInt("version", 1);
        root.putInt("nextArena", nextArena);
        ListTag list = new ListTag();
        players.forEach((id, progress) -> {
            CompoundTag item = new CompoundTag();
            item.putUUID("id", id); item.put("progress", progress.copy()); list.add(item);
        });
        root.put("players", list);
        return root;
    }
}
