package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class TankerProgressionRules {
   public static final String TAUNT = "Taunt";
   public static final String REINFORCEMENT = "Reinforcement";
   public static final String TANK_LEAP = "Tank Leap";
   public static final String SHIELD_BASH = "Shield Bash";
   public static final String WILLPOWER = "Willpower";
   public static final String PROTECTION_MARK = "Protection Mark";
   public static final List<String> MASTERY_ORDER = List.of("Shield Bash", "Taunt", "Tank Leap", "Reinforcement", "Willpower", "Protection Mark");

   private TankerProgressionRules() {
   }

   public static String canonicalName(String rawName) {
      if (rawName == null) {
         return "";
      }

      String clean = rawName.trim();
      String canonical = canonicalTankerName(clean);
      return canonical == null ? clean : canonical;
   }

   public static boolean isTankerSkill(String rawName) {
      return canonicalTankerName(rawName) != null;
   }

   public static boolean hasSkill(String rawList, String requestedSkill) {
      String requested = canonicalTankerName(requestedSkill);
      if (requested != null && rawList != null && !rawList.isBlank()) {
         for (String rawToken : rawList.split(",", -1)) {
            String token = cleanListToken(rawToken);
            String canonical = canonicalTankerName(token);
            if (requested.equals(canonical)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static String canonicalizeSkillList(String rawList) {
      List<String> output = new ArrayList<>();
      Set<String> seenTankerSkills = new HashSet<>();
      if (rawList != null) {
         for (String rawToken : rawList.split(",", -1)) {
            String token = cleanListToken(rawToken);
            if (!token.isEmpty()) {
               String canonical = canonicalTankerName(token);
               if (canonical == null) {
                  output.add(token);
               } else if (seenTankerSkills.add(canonical)) {
                  output.add(canonical);
               }
            }
         }
      }

      return output.isEmpty() ? "." : "." + String.join(",", output) + ",";
   }

   public static String ensureSkill(String rawList, String requestedSkill) {
      String canonical = canonicalTankerName(requestedSkill);
      String migrated = canonicalizeSkillList(rawList);
      if (canonical != null && !hasSkill(migrated, canonical)) {
         return ".".equals(migrated) ? "." + canonical + "," : migrated + canonical + ",";
      } else {
         return migrated;
      }
   }

   public static List<String> entitlementsForRank(int rawRank) {
      int rank = Math.max(1, Math.min(6, rawRank));
      return MASTERY_ORDER.subList(0, rank);
   }

   public static String firstMissingSkill(String rawList) {
      String migrated = canonicalizeSkillList(rawList);

      for (String skill : MASTERY_ORDER) {
         if (!hasSkill(migrated, skill)) {
            return skill;
         }
      }

      return "";
   }

   public static String canonicalizeReference(String rawValue) {
      if (rawValue == null) {
         return "";
      }

      String canonical = canonicalTankerName(rawValue);
      return canonical == null ? rawValue : canonical;
   }

   private static String canonicalTankerName(String rawName) {
      if (rawName == null) {
         return null;
      }

      String clean = rawName.trim();

      while (clean.startsWith(".")) {
         clean = clean.substring(1).trim();
      }
      return switch (clean.toLowerCase(Locale.ROOT)) {
         case "taunt" -> "Taunt";
         case "reinforcement" -> "Reinforcement";
         case "tank leap", "tankleap", "tank_leap", "leap strike", "leapstrike" -> "Tank Leap";
         case "shield bash", "shieldbash", "shield_bash" -> "Shield Bash";
         case "willpower", "will power", "will_power" -> "Willpower";
         case "protection mark", "protectionmark", "protection_mark" -> "Protection Mark";
         default -> null;
      };
   }

   private static String cleanListToken(String rawToken) {
      if (rawToken == null) {
         return "";
      }

      String clean = rawToken.trim();

      while (clean.startsWith(".")) {
         clean = clean.substring(1).trim();
      }

      return clean;
   }
}
