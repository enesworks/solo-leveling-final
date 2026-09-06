package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;

@EventBusSubscriber(modid = "sololeveling")
public final class MageSpellProgression {
   public static final String FIRE = "fire";
   public static final String BARRIER = "barrier";
   public static final String ARCANE = "arcane";
   public static final String STORM = "storm";
   private static final Set<String> LEGACY_FIRE_SKILLS = Set.of("Fireball", "Fire Rain", "Heavy Flame", "Flame Tornado", "Flame Vortex");
   private static final Set<String> RETIRED_UNBOUND_SKILLS = Set.of(
      "Magic Missiles", "Lightball", "Light Ball", "Water Slash", "Light Golem", "Curse Sphere", "Curse Smoke", "Curse Chains"
   );
   private static final Set<String> RETIRED_RUNESTONE_IDS = Set.of(
      "runestone_magic_missiles",
      "runestone_lightball",
      "runestone_waterslash",
      "runestone_light_golem",
      "runestone_curse_sphere",
      "runestone_cursed_smoke",
      "runestone_curse_chains"
   );
   private static final String RUNESTONE_SKILL_PREFIX = "slr_runestone_skill_";
   private static final List<List<String>> FIRE_SPELL_TIERS = List.of(
      List.of(),
      List.of("Flame Weaving", "Ignition Orb"),
      List.of("Inferno Lance", "Flashfire"),
      List.of("Cremation"),
      List.of("Furnace Dominion"),
      List.of("Heavenfall")
   );
   private static final List<List<String>> FIRE_EVALUATION_UNLOCKS = List.of(
      List.of(), List.of("Flame Weaving", "Ignition Orb"), List.of("Inferno Lance"), List.of("Cremation"), List.of("Furnace Dominion"), List.of("Heavenfall")
   );
   private static final List<List<String>> BARRIER_SPELL_TIERS = List.of(
      List.of("Fracture Bolt"),
      List.of("Prism Rampart"),
      List.of("Repulsion Frame"),
      List.of("Sealing Prism"),
      List.of("Mirror Ward"),
      List.of("Resonant Collapse", "Absolute Bastion")
   );
   private static final List<List<String>> ARCANE_SPELL_TIERS = List.of(
      List.of("Aether Bolt"),
      List.of("Vector Step"),
      List.of("Polarity Sphere"),
      List.of("Runic Relay"),
      List.of("Astral Arsenal"),
      List.of("Dimensional Rend", "Grand Formula: Convergence")
   );
   private static final List<List<String>> STORM_SPELL_TIERS = List.of(
      List.of("Static Needle"),
      List.of("Slipstream", "Thunderclap"),
      List.of("Lightning Rod"),
      List.of("Chain Lightning"),
      List.of("Thunderhead"),
      List.of("Skybreaker", "Tempest Incarnate")
   );
   private static final List<List<String>> STORM_EVALUATION_UNLOCKS = List.of(
      List.of("Static Needle"), List.of("Slipstream"), List.of("Lightning Rod"), List.of("Chain Lightning"), List.of("Thunderhead"), List.of("Skybreaker")
   );

   private MageSpellProgression() {
   }

   @SubscribeEvent
   public static void migrateExistingMage(PlayerLoggedInEvent event) {
      reconcilePlayerSkills(event.getEntity());
   }

   public static void reconcilePlayerSkills(Entity entity) {
      if (entity != null) {
         removeInvalidMageSkills(entity);
         if (variables(entity).Classes == 2.0) {
            if (specialization(entity).isBlank()) {
               setSpecialization(entity, "fire", false);
            }

            grantEvaluationSpells(entity);
         }

         grantBaranStormInheritance(entity);
      }
   }

   public static void reconcileVesselInheritance(Entity entity) {
      if (entity != null) {
         removeInvalidMageSkills(entity);
         grantBaranStormInheritance(entity);
      }
   }

   private static void grantBaranStormInheritance(Entity entity) {
      if (WhiteFlameMonarchManager.isWhiteFlameVessel(entity)) {
         for (String skill : StormMageSpellManager.BARAN_CROSSOVER_SKILL_ORDER) {
            unlockSkill(entity, skill, false);
         }
      }
   }

   private static void removeInvalidMageSkills(Entity entity) {
      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         boolean changed = false;
         String cleanedList = cleanSkillList(entity, capability.Plist);
         if (!Objects.equals(cleanedList, capability.Plist)) {
            capability.Plist = cleanedList;
            changed = true;
         }

         for (int slot = 1; slot <= 16; slot++) {
            if (isInvalidMageSkill(entity, SkillSlotHelper.getSlot(capability, slot))) {
               SkillSlotHelper.setSlot(capability, slot, "");
               changed = true;
            }
         }

         if (isInvalidMageSkill(entity, capability.PselectedPower)) {
            capability.PselectedPower = "";
            changed = true;
         }

         if (changed) {
            capability.syncPlayerVariables(entity);
         }
      });
   }

   private static String cleanSkillList(Entity entity, String skillList) {
      if (skillList != null && !skillList.isBlank()) {
         StringBuilder cleaned = new StringBuilder(".");

         for (String raw : skillList.split(",")) {
            String skill = raw == null ? "" : raw.trim();

            while (skill.startsWith(".")) {
               skill = skill.substring(1);
            }

            if (!skill.isBlank() && !isInvalidMageSkill(entity, skill)) {
               cleaned.append(skill).append(',');
            }
         }

         return cleaned.toString();
      } else {
         return ".";
      }
   }

   private static boolean isInvalidMageSkill(Entity entity, String skill) {
      if (skill == null || skill.isBlank()) {
         return false;
      } else if (!LEGACY_FIRE_SKILLS.contains(skill) && !RETIRED_UNBOUND_SKILLS.contains(skill)) {
         String required = requiredSpecialization(skill);
         return required != null && !hasNativeAccess(entity, skill) && !wasLearnedFromRunestone(entity, skill);
      } else {
         return true;
      }
   }

   public static String specialization(Entity entity) {
      String value = variables(entity).mageSpecialization;
      return value == null ? "" : value.trim().toLowerCase();
   }

   public static boolean isBarrierMage(Entity entity) {
      return "barrier".equals(specialization(entity));
   }

   public static boolean isArcaneMage(Entity entity) {
      return "arcane".equals(specialization(entity));
   }

   public static boolean isStormMage(Entity entity) {
      return "storm".equals(specialization(entity));
   }

   public static String requiredSpecialization(String skill) {
      if (FireMageSpellManager.isFireSkill(skill)) {
         return "fire";
      } else if (BarrierMageSpellManager.isBarrierSkill(skill)) {
         return "barrier";
      } else if (ArcaneMageSpellManager.isArcaneSkill(skill)) {
         return "arcane";
      } else {
         return StormMageSpellManager.isStormSkill(skill) ? "storm" : null;
      }
   }

   private static boolean hasNativeAccess(Entity entity, String skill) {
      String required = requiredSpecialization(skill);
      if (entity == null || required == null) {
         return false;
      } else {
         return "storm".equals(required) && StormMageSpellManager.BARAN_CROSSOVER_SKILLS.contains(skill) && WhiteFlameMonarchManager.isWhiteFlameVessel(entity)
            ? true
            : variables(entity).Classes == 2.0 && required.equals(specialization(entity));
      }
   }

   public static boolean canCastLearnedSkill(Entity entity, String skill) {
      return entity != null && requiredSpecialization(skill) != null && hasSkill(entity, skill);
   }

   public static boolean isRetiredRunestoneId(String registryId) {
      if (registryId == null) {
         return false;
      }

      String path = registryId.trim().toLowerCase();
      int separator = path.indexOf(58);
      if (separator >= 0) {
         path = path.substring(separator + 1);
      }

      return RETIRED_RUNESTONE_IDS.contains(path);
   }

   public static String displayName(Entity entity) {
      return switch (specialization(entity)) {
         case "barrier" -> "Barrier Mage";
         case "arcane" -> "Arcane Mage";
         case "storm" -> "Storm Mage";
         default -> "Fire Mage";
      };
   }

   public static String assignRandomSpecialization(Entity entity) {
      String current = specialization(entity);
      if (!"fire".equals(current) && !"barrier".equals(current) && !"arcane".equals(current) && !"storm".equals(current)) {
         String selected = switch (entity.level().getRandom().nextInt(4)) {
            case 1 -> "barrier";
            case 2 -> "arcane";
            case 3 -> "storm";
            default -> "fire";
         };
         setSpecialization(entity, selected, true);
         return selected;
      } else {
         return current;
      }
   }

   public static void setSpecialization(Entity entity, String specialization, boolean notify) {
      if (entity != null
         && ("fire".equals(specialization) || "barrier".equals(specialization) || "arcane".equals(specialization) || "storm".equals(specialization))) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.mageSpecialization = specialization;
            capability.syncPlayerVariables(entity);
         });
         if (notify && entity instanceof ServerPlayer player) {
            boolean barrier = "barrier".equals(specialization);
            boolean arcane = "arcane".equals(specialization);
            boolean storm = "storm".equals(specialization);
            SystemNotifications.showTitleUnder(
               player,
               barrier ? -10688257 : (arcane ? -7709441 : (storm ? -11174 : -42454)),
               120,
               Component.literal("MAGE SPECIALIZATION").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
               Component.literal(barrier ? "Barrier Mage" : (arcane ? "Arcane Mage" : (storm ? "Storm Mage" : "Fire Mage")))
                  .withStyle(
                     barrier ? ChatFormatting.AQUA : (arcane ? ChatFormatting.LIGHT_PURPLE : (storm ? ChatFormatting.YELLOW : ChatFormatting.RED)),
                     ChatFormatting.BOLD
                  )
            );
         }
      }
   }

   public static int tierForRank(double hunterRank) {
      int rank = (int)Math.round(hunterRank);
      return Math.max(0, Math.min(5, rank - 1));
   }

   public static void grantStarterSpells(Entity entity) {
      if (entity != null) {
         String type = assignRandomSpecialization(entity);

         String starter = switch (type) {
            case "barrier" -> "Fracture Bolt";
            case "arcane" -> "Aether Bolt";
            case "storm" -> "Static Needle";
            default -> "Flame Weaving";
         };
         unlockSkill(entity, starter, false);
      }
   }

   public static void grantEvaluationSpells(Entity entity) {
      if (entity != null) {
         String type = assignRandomSpecialization(entity);
         int tier = tierForRank(variables(entity).HunterRank);
         entity.getPersistentData().putInt("sl_mage_spell_tier", tier);
         if ("barrier".equals(type)) {
            unlockSkill(entity, "Fracture Bolt", false);

            for (int currentTier = 1; currentTier <= tier; currentTier++) {
               for (String skill : BARRIER_SPELL_TIERS.get(currentTier)) {
                  unlockSkill(entity, skill, false);
               }
            }
         } else if ("arcane".equals(type)) {
            for (int currentTier = 0; currentTier <= tier; currentTier++) {
               for (String skill : ARCANE_SPELL_TIERS.get(currentTier)) {
                  unlockSkill(entity, skill, false);
               }
            }
         } else if ("storm".equals(type)) {
            for (int currentTier = 0; currentTier <= tier; currentTier++) {
               for (String skill : STORM_EVALUATION_UNLOCKS.get(currentTier)) {
                  unlockSkill(entity, skill, false);
               }
            }
         } else {
            for (int currentTier = 1; currentTier <= tier; currentTier++) {
               for (String skill : FIRE_EVALUATION_UNLOCKS.get(currentTier)) {
                  unlockSkill(entity, skill, false);
               }
            }
         }
      }
   }

   public static void grantMasterySkill(Entity entity) {
      if (entity != null && variables(entity).Classes == 2.0) {
         String type = assignRandomSpecialization(entity);
         int tier = tierForRank(variables(entity).HunterRank);
         entity.getPersistentData().putInt("sl_mage_spell_tier", tier);
         List<String> missing = new ArrayList<>();
         if ("barrier".equals(type)) {
            for (int currentTier = 0; currentTier <= tier; currentTier++) {
               for (String skill : BARRIER_SPELL_TIERS.get(currentTier)) {
                  if (!hasSkill(entity, skill)) {
                     missing.add(skill);
                  }
               }
            }
         } else if ("arcane".equals(type)) {
            for (int currentTier = 0; currentTier <= tier; currentTier++) {
               for (String skill : ARCANE_SPELL_TIERS.get(currentTier)) {
                  if (!hasSkill(entity, skill)) {
                     missing.add(skill);
                  }
               }
            }
         } else if ("storm".equals(type)) {
            for (int currentTier = 0; currentTier <= tier; currentTier++) {
               for (String skill : STORM_SPELL_TIERS.get(currentTier)) {
                  if (!hasSkill(entity, skill)) {
                     missing.add(skill);
                  }
               }
            }
         } else {
            for (int currentTier = 1; currentTier <= tier; currentTier++) {
               for (String skill : FIRE_SPELL_TIERS.get(currentTier)) {
                  if (!hasSkill(entity, skill)) {
                     missing.add(skill);
                  }
               }
            }
         }

         if (!missing.isEmpty()) {
            unlockSkill(entity, missing.get(entity.level().getRandom().nextInt(missing.size())), true);
         }
      }
   }

   public static boolean hasSkill(Entity entity, String skill) {
      String list = variables(entity).Plist;
      if (list != null && !list.isBlank()) {
         for (String entry : list.split(",")) {
            String normalized = entry.trim();
            if (normalized.startsWith(".")) {
               normalized = normalized.substring(1);
            }

            if (skill.equals(normalized)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean unlockSkill(Entity entity, String skill, boolean notify) {
      if (entity != null && skill != null && !skill.isBlank() && !hasSkill(entity, skill)) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Plist = capability.Plist + skill + ",";
            capability.syncPlayerVariables(entity);
         });
         if (notify && entity instanceof Player player && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("Gained skill: " + skill), false);
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean unlockFromRunestone(Entity entity, String skill) {
      if (!unlockSkill(entity, skill, false)) {
         return false;
      }

      runestoneData(entity).putBoolean(runestoneSkillKey(skill), true);
      return true;
   }

   private static boolean wasLearnedFromRunestone(Entity entity, String skill) {
      if (entity == null) {
         return false;
      }

      String key = runestoneSkillKey(skill);
      return runestoneData(entity).getBoolean(key) || entity.getPersistentData().getBoolean(key);
   }

   private static String runestoneSkillKey(String skill) {
      StringBuilder key = new StringBuilder("slr_runestone_skill_");

      for (int index = 0; index < skill.length(); index++) {
         char character = Character.toLowerCase(skill.charAt(index));
         key.append(Character.isLetterOrDigit(character) ? character : '_');
      }

      return key.toString();
   }

   private static CompoundTag runestoneData(Entity entity) {
      CompoundTag entityData = entity.getPersistentData();
      if (!(entity instanceof Player)) {
         return entityData;
      }

      CompoundTag persisted = entityData.getCompound("PlayerPersisted");
      if (!entityData.contains("PlayerPersisted")) {
         entityData.put("PlayerPersisted", persisted);
      }

      return persisted;
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }
}
