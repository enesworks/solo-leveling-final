package dev.eness.sololevelingfinal.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class SkillCategoryRegistry {
   private static final SkillCategoryRegistry.Category ASSASSIN = new SkillCategoryRegistry.Category("A", 5627903);
   private static final SkillCategoryRegistry.Category FIGHTER = new SkillCategoryRegistry.Category("F", 16735324);
   private static final SkillCategoryRegistry.Category TANKER = new SkillCategoryRegistry.Category("T", 15397631);
   private static final SkillCategoryRegistry.Category HEALER = new SkillCategoryRegistry.Category("H", 5829258);
   private static final SkillCategoryRegistry.Category RANGER = new SkillCategoryRegistry.Category("R", 7460925);
   private static final SkillCategoryRegistry.Category ARCANE_MAGE = new SkillCategoryRegistry.Category("AM", 11955455);
   private static final SkillCategoryRegistry.Category FIRE_MAGE = new SkillCategoryRegistry.Category("FM", 16738866);
   private static final SkillCategoryRegistry.Category BARRIER_MAGE = new SkillCategoryRegistry.Category("BM", 6479871);
   private static final SkillCategoryRegistry.Category STORM_MAGE = new SkillCategoryRegistry.Category("ST", 16766042);
   private static final SkillCategoryRegistry.Category FORBIDDEN = new SkillCategoryRegistry.Category("FB", 16725044);
   private static final Map<String, SkillCategoryRegistry.Category> SKILLS = new HashMap<>();

   private SkillCategoryRegistry() {
   }

   public static Component decorate(String rawSkill, String displayName) {
      if (rawSkill != null && displayName != null && !JobSkillManager.isJobSkill(rawSkill) && !ShadowMonarchManager.isFormationSkill(rawSkill)) {
         SkillCategoryRegistry.Category category = SKILLS.get(rawSkill);
         if (category == null) {
            return Component.literal(displayName);
         }

         MutableComponent result = Component.empty();
         result.append(Component.literal("("));
         result.append(Component.literal(category.code()).withStyle(style -> style.withColor(category.color()).withBold(true)));
         result.append(Component.literal(") "));
         result.append(Component.literal(displayName));
         return result;
      } else {
         return Component.literal(displayName == null ? "" : displayName);
      }
   }

   private static void register(SkillCategoryRegistry.Category category, Set<String> skills) {
      for (String skill : skills) {
         SKILLS.put(skill, category);
      }
   }

   static {
      register(
         ASSASSIN,
         Set.of(
            "Ghost Step",
            "Night Rend",
            "Stealth",
            "Flash Cut",
            "Dualwield",
            "Critical Attack",
            "Mutilation",
            "Murderious Intent",
            "Dagger Throw",
            "Dagger Rush"
         )
      );
      register(FIGHTER, Set.of("Ground Slam", "Slash Dash", "Cross Strike", "Critical Strike", "Slash Fury", "Sword Dance", "Sword of Light", "Sword Beam"));
      register(TANKER, Set.of("Taunt", "Reinforcement", "Tank Leap", "Shield Bash", "Willpower", "Protection Mark"));
      register(HEALER, Set.of("Heal Beam", "Haste Buff", "Purification", "Physical Buff", "Overheal", "Blessing Mark"));
      register(
         RANGER,
         Set.of(
            "Mana Quiver",
            "Back Step",
            "Hawkeye",
            "Rapid Fire",
            "Hyper Focus",
            "Sharpshooter",
            "High Value Target",
            "Arrow Shower",
            "Proximity Trap",
            "Detection"
         )
      );
      register(FIRE_MAGE, FireMageSpellManager.FIRE_SKILLS);
      register(BARRIER_MAGE, BarrierMageSpellManager.BARRIER_SKILLS);
      register(ARCANE_MAGE, ArcaneMageSpellManager.ARCANE_SKILLS);
      register(STORM_MAGE, StormMageSpellManager.STORM_SKILLS);
      register(FORBIDDEN, Set.of("Cold Blood"));
   }

   private record Category(String code, int color) {
   }
}
