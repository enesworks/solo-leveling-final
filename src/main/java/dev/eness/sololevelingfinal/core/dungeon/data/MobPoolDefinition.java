package dev.eness.sololevelingfinal.core.dungeon.data;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

public record MobPoolDefinition(ResourceLocation id, int formatVersion, List<DungeonDataTypes.MobPoolEntry> entries) {
   public MobPoolDefinition {
      entries = List.copyOf(entries);
   }
}
