package dev.eness.sololevelingfinal.core.util;

public final class HunterEvaluationRules {
   public static final int CLASS_COUNT = 6;
   public static final int ALL_CLASSES_MASK = 63;
   public static final int RANK_COUNT = 6;
   public static final int D_RANK_LEVEL = 15;
   public static final int C_RANK_LEVEL = 30;
   public static final int B_RANK_LEVEL = 50;
   public static final int A_RANK_LEVEL = 75;
   public static final int S_RANK_LEVEL = 100;
   public static final int CONTACT_TICKS = 30;
   public static final int INITIAL_BOOT_TICKS = 40;
   public static final int INITIAL_SCAN_TICKS = 70;
   public static final int INITIAL_CLASS_REVEAL_TICKS = 40;
   public static final int RANK_REVEAL_TICKS = 30;
   public static final int INITIAL_SETTLE_TICKS = 10;
   public static final int REEVALUATION_SCAN_TICKS = 40;
   public static final int REROLL_TICKS = 25;
   public static final int S_RANK_ERROR_TICKS = 35;
   public static final int[] DEFAULT_RANK_ODDS = new int[]{25, 25, 25, 12, 10, 3};
   public static final int MAX_RANK_WEIGHT = 100;
   private static final String[] CLASS_NAMES = new String[]{"Unknown", "Assassin", "Mage", "Fighter", "Tanker", "Healer", "Ranger"};
   private static final int[] CLASS_COLORS = new int[]{-12597505, -16736769, -4235265, -2565928, -13606657, -16318720, -17408};
   private static final String[] RANK_NAMES = new String[]{"Unranked", "E", "D", "C", "B", "A", "S"};
   private static final float[] RANK_INTENSITIES = new float[]{0.22F, 0.3F, 0.42F, 0.55F, 0.7F, 0.85F, 1.0F};

   private HunterEvaluationRules() {
   }

   public static int weightedRank(int roll) {
      int bounded = Math.max(1, Math.min(100, roll));
      if (bounded <= 25) {
         return 1;
      } else if (bounded <= 50) {
         return 2;
      } else if (bounded <= 75) {
         return 3;
      } else if (bounded <= 87) {
         return 4;
      } else {
         return bounded <= 97 ? 5 : 6;
      }
   }

   public static int rankFloorForLevel(int level) {
      if (level >= 100) {
         return 6;
      } else if (level >= 75) {
         return 5;
      } else if (level >= 50) {
         return 4;
      } else if (level >= 30) {
         return 3;
      } else {
         return level >= 15 ? 2 : 1;
      }
   }

   public static int resolvedEvaluationRank(int earnedRank, int certifiedRank, int level, boolean hasVessel) {
      return hasVessel ? 6 : Math.max(rankFloorForLevel(level), Math.max(clamp(earnedRank, 0, 6), clamp(certifiedRank, 0, 6)));
   }

   public static int[] normalizedRankOdds(int[] weights) {
      int[] source = new int[6];
      long total = 0L;

      for (int index = 0; index < 6; index++) {
         int weight = weights != null && index < weights.length ? weights[index] : 0;
         source[index] = Math.max(0, Math.min(100, weight));
         total += source[index];
      }

      if (total <= 0L) {
         return (int[])DEFAULT_RANK_ODDS.clone();
      }

      int[] result = new int[6];
      int assigned = 0;

      for (int index = 0; index < 6; index++) {
         result[index] = (int)(source[index] * 100L / total);
         assigned += result[index];
      }

      boolean[] raised = new boolean[6];

      for (int remaining = 100 - assigned; remaining > 0; remaining--) {
         int best = -1;
         long bestRemainder = -1L;

         for (int index = 0; index < 6; index++) {
            if (!raised[index]) {
               long remainder = source[index] * 100L % total;
               if (remainder > bestRemainder) {
                  bestRemainder = remainder;
                  best = index;
               }
            }
         }

         if (best < 0) {
            break;
         }

         result[best]++;
         raised[best] = true;
      }

      return result;
   }

   public static int weightedRank(int[] normalizedOdds, int roll) {
      int bounded = Math.max(1, Math.min(100, roll));
      int cumulative = 0;

      for (int rank = 1; rank <= 6; rank++) {
         cumulative += normalizedOdds[rank - 1];
         if (bounded <= cumulative) {
            return rank;
         }
      }

      for (int rank = 6; rank >= 1; rank--) {
         if (normalizedOdds[rank - 1] > 0) {
            return rank;
         }
      }

      return 1;
   }

   public static int rankRevealDuration(int rank) {
      return rank == 6 ? 65 : 30;
   }

   public static float sRankErrorFraction() {
      return 0.53846157F;
   }

   public static HunterEvaluationRules.ClassDraw drawClass(int remainingMask, int currentClass, int randomIndex) {
      int mask = remainingMask & 63;
      int currentBit = classBit(currentClass);
      int candidates = mask & ~currentBit;
      if (candidates == 0) {
         mask = 63;
         candidates = mask & ~currentBit;
      }

      if (candidates == 0) {
         throw new IllegalStateException("Hunter Evaluation has no class candidates");
      }

      int wanted = Math.floorMod(randomIndex, Integer.bitCount(candidates));
      int chosen = 0;

      for (int classId = 1; classId <= 6; classId++) {
         int bit = classBit(classId);
         if ((candidates & bit) != 0 && wanted-- == 0) {
            chosen = classId;
            break;
         }
      }

      return new HunterEvaluationRules.ClassDraw(chosen, mask & ~classBit(chosen));
   }

   public static int phaseDuration(HunterEvaluationRules.Mode mode, HunterEvaluationRules.Phase phase) {
      return switch (phase) {
         case BOOT -> mode == HunterEvaluationRules.Mode.INITIAL ? 40 : 0;
         case CONTACT -> 30;
         case SCAN -> mode == HunterEvaluationRules.Mode.INITIAL ? 70 : 40;
         case CLASS_REVEAL -> mode == HunterEvaluationRules.Mode.INITIAL ? 40 : 0;
         case RANK_REVEAL -> 30;
         case SETTLE -> mode == HunterEvaluationRules.Mode.INITIAL ? 10 : 0;
         case REROLL -> 25;
         default -> 0;
      };
   }

   public static String className(int classId) {
      return CLASS_NAMES[clamp(classId, 0, 6)];
   }

   public static int classColor(int classId) {
      return CLASS_COLORS[clamp(classId, 0, 6)];
   }

   public static String rankName(int rank) {
      return RANK_NAMES[clamp(rank, 0, 6)];
   }

   public static float rankIntensity(int rank) {
      return RANK_INTENSITIES[clamp(rank, 0, 6)];
   }

   public static boolean revealsClass(HunterEvaluationRules.Mode mode, HunterEvaluationRules.Phase phase) {
      return mode == HunterEvaluationRules.Mode.REEVALUATION
         ? true
         : phase == HunterEvaluationRules.Phase.CLASS_REVEAL
            || phase == HunterEvaluationRules.Phase.RANK_REVEAL
            || phase == HunterEvaluationRules.Phase.SETTLE
            || phase == HunterEvaluationRules.Phase.DECISION
            || phase == HunterEvaluationRules.Phase.REROLL
            || phase == HunterEvaluationRules.Phase.COMPLETE;
   }

   public static boolean revealsRank(HunterEvaluationRules.Phase phase) {
      return phase == HunterEvaluationRules.Phase.RANK_REVEAL
         || phase == HunterEvaluationRules.Phase.SETTLE
         || phase == HunterEvaluationRules.Phase.DECISION
         || phase == HunterEvaluationRules.Phase.REROLL
         || phase == HunterEvaluationRules.Phase.COMPLETE;
   }

   private static int classBit(int classId) {
      return classId >= 1 && classId <= 6 ? 1 << classId - 1 : 0;
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   public enum Action {
      BEGIN_CONTACT,
      CANCEL_CONTACT,
      REROLL_CLASS,
      ACCEPT_RESULT,
      ACKNOWLEDGE;

      public static HunterEvaluationRules.Action fromId(int id) {
         HunterEvaluationRules.Action[] values = values();
         return id >= 0 && id < values.length ? values[id] : null;
      }
   }

   public record ClassDraw(int classId, int remainingMask) {
   }

   public enum Mode {
      INITIAL,
      REEVALUATION;

      public static HunterEvaluationRules.Mode fromId(int id) {
         return id == REEVALUATION.ordinal() ? REEVALUATION : INITIAL;
      }
   }

   public enum Phase {
      BOOT,
      CONTACT,
      SCAN,
      CLASS_REVEAL,
      RANK_REVEAL,
      SETTLE,
      DECISION,
      REROLL,
      COMPLETE;

      public static HunterEvaluationRules.Phase fromId(int id) {
         HunterEvaluationRules.Phase[] values = values();
         return id >= 0 && id < values.length ? values[id] : CONTACT;
      }
   }
}
