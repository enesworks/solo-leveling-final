package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SkillListHelper {
   private static final List<String> JOB_SKILL_ORDER = List.of(
      "Arise",
      "Shadow Summon",
      "Dismiss Shadows",
      "Shadow Command",
      "Shadow Exchange",
      "Shadow Manifestation",
      "Grand Marshal Authority",
      "Fire Charge",
      "Meteor Rain",
      "Fireflies",
      "Ice Spear",
      "Flash Freeze",
      "Frozen Path",
      "Frozen Architecture",
      "Frost Counter",
      "Absolute Zero",
      "Frost Monarch Spiritualization",
      "Capture",
      "Power Smash",
      "Collapse",
      "Spiritual Body Manifestation",
      "Heavenly Counter",
      "Golden Dragon Dance",
      "Sovereign Sword Domain",
      "Dragon Sword Manifestation",
      "Predator's Presence",
      "Assassin Stance",
      "Spatial Execution",
      "Spiritualization",
      "Lightning Breath",
      "Hellstorm Dominion",
      "Radiru Blood Spear",
      "Doppelganger",
      "Hell's Army",
      "White Flame Spiritualization",
      "Claw-Rift Passage",
      "Rubble Jaw",
      "King's Maul",
      "Feral Reconstitution",
      "White Fang Sovereign",
      "Destruction Claw",
      "Breath of Destruction",
      "Monarch's Descent",
      "Sovereign Roar",
      "Extinction",
      "Monarch Manifestation",
      "Monarch Beam",
      "Lightning Storm",
      "Storm Burst"
   );

   private SkillListHelper() {
   }

   public static String rawSkillAt(Entity entity, int position) {
      if (entity != null && position >= 1) {
         List<String> skills = skills(entity);
         return position > skills.size() ? "empty" : skills.get(position - 1);
      } else {
         return "empty";
      }
   }

   public static String displaySkillAt(Entity entity, int position) {
      String raw = rawSkillAt(entity, position);
      return "empty".equals(raw) ? "empty" : ShadowMonarchManager.displaySkillName(entity, raw);
   }

   public static int colorAt(Entity entity, int position) {
      String raw = rawSkillAt(entity, position);
      return ShadowMonarchManager.skillColor(entity, raw);
   }

   public static int skillCount(Entity entity) {
      return skills(entity).size();
   }

   public static int pageCount(Entity entity, int perPage) {
      return Math.max(1, (int)Math.ceil((double)skillCount(entity) / perPage));
   }

   public static List<String> skills(Entity entity) {
      ArrayList<String> result = new ArrayList<>();
      if (entity == null) {
         return result;
      }

      String plistOriginal = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables())
         .Plist;
      if (plistOriginal != null && !plistOriginal.isEmpty()) {
         for (String item : plistOriginal.split(",")) {
            String skill = cleanSkill(item);
            if (!skill.isEmpty()) {
               result.add(skill);
            }
         }

         return orderedForEquipList(result);
      } else {
         return result;
      }
   }

   private static List<String> orderedForEquipList(List<String> skills) {
      ArrayList<String> result = new ArrayList<>();

      for (String jobSkill : JOB_SKILL_ORDER) {
         if (skills.contains(jobSkill)) {
            result.add(jobSkill);
         }
      }

      for (String skill : skills) {
         if (!JobSkillManager.isJobSkill(skill)) {
            result.add(skill);
         }
      }

      return result;
   }

   private static String cleanSkill(String item) {
      if (item == null) {
         return "";
      }

      String skill = item.trim();
      if (skill.startsWith(".")) {
         skill = skill.substring(1);
      }

      return skill;
   }
}
