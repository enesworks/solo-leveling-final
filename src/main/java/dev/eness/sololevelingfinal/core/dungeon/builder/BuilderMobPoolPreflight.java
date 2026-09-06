package dev.eness.sololevelingfinal.core.dungeon.builder;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.ModList;
import dev.eness.sololevelingfinal.core.dungeon.builder.model.BuilderMobPool;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataTypes;
import dev.eness.sololevelingfinal.core.dungeon.data.MobPoolResolver;

public final class BuilderMobPoolPreflight {
   private BuilderMobPoolPreflight() {
   }

   public static List<String> problems(ServerLevel level, BuilderMobPool pool) {
      List<String> problems = new ArrayList<>();
      if (pool.entries().isEmpty()) {
         problems.add("Referenced pool " + pool.id() + " has no entries.");
         return problems;
      }

      boolean[] unconditionalCoverage = new boolean[1001];

      for (BuilderMobPool.Entry entry : pool.entries()) {
         boolean conditionLoaded = entry.requiredMod().isEmpty() || ModList.get().isLoaded(entry.requiredMod().get());
         if (conditionLoaded) {
            boolean resolves = resolvesSpawnable(level, entry.selectorKind(), entry.selector());
            if (!resolves) {
               String selector = (entry.selectorKind() == BuilderMobPool.SelectorKind.TAG ? "#" : "") + entry.selector();
               problems.add("Pool " + pool.id() + " selector " + selector + " resolves to no loaded spawnable mob. Check the ID/tag and required_mod.");
            }

            if (isGuaranteedAvailable(entry) && resolves) {
               int minimum = entry.eligibleLevel().map(BuilderMobPool.LevelRange::min).orElse(1);
               int maximum = entry.eligibleLevel().map(BuilderMobPool.LevelRange::max).orElse(1000);
               minimum = Math.max(1, minimum);
               maximum = Math.min(1000, maximum);

               for (int dungeonLevel = minimum; dungeonLevel <= maximum; dungeonLevel++) {
                  unconditionalCoverage[dungeonLevel] = true;
               }
            }
         }
      }

      for (int dungeonLevel = 1; dungeonLevel <= 1000; dungeonLevel++) {
         if (!unconditionalCoverage[dungeonLevel]) {
            problems.add(
               "Referenced pool "
                  + pool.id()
                  + " has no guaranteed fallback mob at dungeon level "
                  + dungeonLevel
                  + ". Leave Optional Mod blank on at least one resolvable entry whose Eligible Level covers 1-1000, or remove an unnecessary Optional Mod condition."
            );
            break;
         }
      }

      return List.copyOf(problems);
   }

   private static boolean isGuaranteedAvailable(BuilderMobPool.Entry entry) {
      return entry.requiredMod().isEmpty() || entry.requiredMod().get().equals("sololeveling");
   }

   public static boolean resolvesSpawnable(ServerLevel level, BuilderMobPool.SelectorKind kind, ResourceLocation id) {
      DungeonDataTypes.SelectorKind runtimeKind = kind == BuilderMobPool.SelectorKind.TAG
         ? DungeonDataTypes.SelectorKind.TAG
         : DungeonDataTypes.SelectorKind.ENTITY;
      return !MobPoolResolver.resolve(level, new DungeonDataTypes.EntitySelector(runtimeKind, id)).isEmpty();
   }
}
