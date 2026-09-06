package dev.eness.sololevelingfinal.core.dungeon.runtime.layout;

import net.minecraft.util.RandomSource;

public final class DungeonRandomStreams {
   private static final long LEVEL_SALT = 6717842215686948973L;
   private static final long LAYOUT_SALT = -6810481495385273371L;
   private static final long ENCOUNTER_SALT = -4022252448587906143L;

   private DungeonRandomStreams() {
   }

   public static long seed(long rootSeed, DungeonRandomStreams.Stream stream) {
      long salt = switch (stream) {
         case LEVEL -> 6717842215686948973L;
         case LAYOUT -> -6810481495385273371L;
         case ENCOUNTER -> -4022252448587906143L;
      };
      return mix(rootSeed ^ salt);
   }

   public static RandomSource random(long rootSeed, DungeonRandomStreams.Stream stream) {
      return RandomSource.create(seed(rootSeed, stream));
   }

   public static long mix(long value) {
      value = (value ^ value >>> 30) * -4658895280553007687L;
      value = (value ^ value >>> 27) * -7723592293110705685L;
      return value ^ value >>> 31;
   }

   public enum Stream {
      LEVEL,
      LAYOUT,
      ENCOUNTER;
   }
}
