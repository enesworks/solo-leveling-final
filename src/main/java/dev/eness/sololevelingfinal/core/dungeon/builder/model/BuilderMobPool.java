package dev.eness.sololevelingfinal.core.dungeon.builder.model;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record BuilderMobPool(ResourceLocation id, List<BuilderMobPool.Entry> entries) {
   public BuilderMobPool {
      if (id == null) {
         throw new IllegalArgumentException("Mob-pool id is required.");
      }

      entries = entries == null ? List.of() : List.copyOf(entries);
   }

   public record Entry(
      BuilderMobPool.SelectorKind selectorKind,
      ResourceLocation selector,
      int weight,
      Optional<String> requiredMod,
      Optional<BuilderMobPool.LevelRange> eligibleLevel,
      Optional<BuilderMobPool.LevelRange> spawnLevel,
      Optional<Integer> baseXp
   ) {
      public Entry {
         if (selectorKind == null) {
            throw new IllegalArgumentException("Selector kind is required.");
         }

         if (selector == null) {
            throw new IllegalArgumentException("Entity or tag selector is required.");
         }

         requiredMod = requiredMod == null ? Optional.empty() : requiredMod;
         eligibleLevel = eligibleLevel == null ? Optional.empty() : eligibleLevel;
         spawnLevel = spawnLevel == null ? Optional.empty() : spawnLevel;
         baseXp = baseXp == null ? Optional.empty() : baseXp;
      }
   }

   public record LevelRange(int min, int max) {
      public LevelRange {
         if (min > max) {
            int swap = min;
            min = max;
            max = swap;
         }
      }
   }

   public enum SelectorKind {
      ENTITY,
      TAG;
   }
}
