package dev.eness.sololevelingfinal.core.dungeon.runtime;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class DungeonLevelHelper {
   public static final String DUNGEON_LEVEL_TAG = "slr_dungeon_level";
   public static final String LEGACY_LEVEL_TAG = "Level";

   private DungeonLevelHelper() {
   }

   public static double playerLevel(@Nullable Player player) {
      if (player == null) {
         return 0.0;
      }

      double level = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(variables -> variables.Level).orElse(0.0);
      return finiteNonNegative(level);
   }

   public static double levelOf(@Nullable Entity entity) {
      if (entity instanceof Player player) {
         return playerLevel(player);
      } else if (!(entity instanceof LivingEntity)) {
         return 0.0;
      } else {
         CompoundTag persistent = entity.getPersistentData();
         if (persistent.contains("slr_dungeon_level", 99)) {
            return finiteNonNegative(persistent.getDouble("slr_dungeon_level"));
         } else {
            return persistent.contains("Level", 99) ? finiteNonNegative(persistent.getDouble("Level")) : 0.0;
         }
      }
   }

   public static void setEntityLevel(LivingEntity entity, int level) {
      int safeLevel = Math.max(0, level);
      entity.getPersistentData().putInt("slr_dungeon_level", safeLevel);
      entity.getPersistentData().putDouble("Level", safeLevel);
   }

   public static int clampLevel(double requestedLevel, int minimum, int maximum) {
      int low = Math.min(minimum, maximum);
      int high = Math.max(minimum, maximum);
      if (!Double.isFinite(requestedLevel)) {
         return low;
      } else {
         long rounded = Math.round(requestedLevel);
         if (rounded <= low) {
            return low;
         } else {
            return rounded >= high ? high : (int)rounded;
         }
      }
   }

   public static int resolveEffectiveLevel(
      DungeonLevelHelper.EffectiveLevelSource source,
      @Nullable Entity owner,
      Collection<? extends Entity> participants,
      int fixedLevel,
      int minimum,
      int maximum
   ) {
      DungeonLevelHelper.EffectiveLevelSource safeSource = source == null ? DungeonLevelHelper.EffectiveLevelSource.FIXED : source;
      double ownerLevel = levelOf(owner);

      double resolved = switch (safeSource) {
         case FIXED -> fixedLevel;
         case OWNER -> ownerLevel;
         case PARTY_AVERAGE -> averagePositiveLevel(participants);
         case PARTY_HIGHEST -> highestPositiveLevel(participants);
      };
      if (resolved <= 0.0) {
         resolved = ownerLevel;
      }

      if (resolved <= 0.0) {
         resolved = fixedLevel;
      }

      return clampLevel(resolved, minimum, maximum);
   }

   public static double averagePositiveLevel(Collection<? extends Entity> participants) {
      if (participants != null && !participants.isEmpty()) {
         double total = 0.0;
         int count = 0;

         for (Entity participant : participants) {
            double level = levelOf(participant);
            if (level > 0.0) {
               total += level;
               count++;
            }
         }

         return count == 0 ? 0.0 : total / count;
      } else {
         return 0.0;
      }
   }

   public static double highestPositiveLevel(Collection<? extends Entity> participants) {
      if (participants != null && !participants.isEmpty()) {
         double highest = 0.0;

         for (Entity participant : participants) {
            highest = Math.max(highest, levelOf(participant));
         }

         return highest;
      } else {
         return 0.0;
      }
   }

   private static double finiteNonNegative(double value) {
      return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
   }

   public enum EffectiveLevelSource {
      FIXED,
      OWNER,
      PARTY_AVERAGE,
      PARTY_HIGHEST;
   }
}
