package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE)
public final class VesselProgressionManager {
   private static final int SYNC_INTERVAL = 40;

   private VesselProgressionManager() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player && player.tickCount % 40 == Math.floorMod(player.getId(), 40)) {
         sync(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         reconcileEntitlements(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         reconcileEntitlements(player);
      }
   }

   public static void reconcileEntitlements(ServerPlayer player) {
      sync(player);
      JobSkillManager.syncJobSkills(player);
   }

   public static void sync(ServerPlayer player) {
      if (player != null && player.server != null) {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         VesselManager.VesselDefinition definition = VesselManager.currentDefinition(player);
         int resolvedJob = definition == null ? (int)vars.JOB : definition.jobId();
         if (vars.Player) {
            award(player, "system/root");
            if (vars.Level >= 10.0) {
               award(player, "system/level_10");
            }

            if (vars.Level >= 30.0) {
               award(player, "system/level_30");
            }

            if (vars.Level >= 50.0) {
               award(player, "system/level_50");
            }

            if (vars.Level >= 100.0) {
               award(player, "system/level_100");
            }

            if (JobChangeQuestManager.isUnlocked(player)) {
               award(player, "system/job_change");
            }
         }

         syncDemonKingsCastle(player, vars, resolvedJob);
         MageSpellProgression.reconcileVesselInheritance(player);
         if (resolvedJob > 0) {
            award(player, "system/vessel");
            if (definition != null) {
               syncVesselIdentity(player, definition);
               resolvedJob = definition.jobId();
            }

            boolean changed = false;
            if (resolvedJob == 1
               && !vars.ShadowBody
               && VesselProgressionRules.canUnlockShadowManifestation(
                  resolvedJob, vars.ShadowBody, vars.Level, vars.shadowstorageusage, vars.ShadowExchange, vars.dkc_cleared
               )) {
               vars.ShadowBody = true;
               changed = true;
            }

            if (changed) {
               player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.ShadowExchange = vars.ShadowExchange;
                  capability.ShadowBody = vars.ShadowBody;
                  capability.syncPlayerVariables(player);
               });
               JobSkillManager.syncJobSkills(player);
            }

            syncSkillAdvancements(player, resolvedJob);
         }
      }
   }

   public static List<String> unlockedSkills(Entity entity, int job) {
      SololevelingModVariables.PlayerVariables vars = variables(entity);
      int level = Math.max(0, (int)Math.floor(vars.Level));
      ArrayList<String> skills = new ArrayList<>();
      switch (job) {
         case 1:
            add(skills, "Arise", "Shadow Summon", "Dismiss Shadows", "Shadow Command");
            if (vars.ShadowExchange) {
               skills.add("Shadow Exchange");
            }

            if (vars.ShadowBody) {
               skills.add("Shadow Manifestation");
            }
            break;
         case 2:
            skills.add("Fire Charge");
            if (level >= 65) {
               skills.add("Meteor Rain");
            }

            if (level >= 85) {
               skills.add("Fireflies");
            }
            break;
         case 3:
            skills.add("Ice Spear");
            if (level >= 55) {
               skills.add("Flash Freeze");
            }

            if (level >= 65) {
               skills.add("Frozen Path");
            }

            if (level >= 75) {
               skills.add("Frozen Architecture");
            }

            if (level >= 85) {
               skills.add("Frost Counter");
            }

            if (level >= 100) {
               skills.add("Absolute Zero");
            }

            if (level >= 120) {
               skills.add("Frost Monarch Spiritualization");
            }
            break;
         case 4:
            skills.add("Lightning Breath");
            if (level >= 60) {
               skills.add("Radiru Blood Spear");
            }

            if (level >= 70) {
               skills.add("Doppelganger");
            }

            if (level >= 85) {
               skills.add("Hellstorm Dominion");
            }

            if (level >= 100) {
               skills.add("Hell's Army");
            }

            if (level >= 120) {
               skills.add("White Flame Spiritualization");
            }
            break;
         case 5:
            skills.add("Spiritual Body Manifestation");
            if (level >= 55) {
               skills.add("Capture");
            }

            if (level >= 70) {
               skills.add("Power Smash");
            }

            if (level >= 90) {
               skills.add("Collapse");
            }
            break;
         case 6:
            skills.add("Dragon Sword Manifestation");
            if (level >= 55) {
               skills.add("Heavenly Counter");
            }

            if (level >= 75) {
               skills.add("Golden Dragon Dance");
            }

            if (level >= 95) {
               skills.add("Sovereign Sword Domain");
            }
            break;
         case 7:
            skills.add("Spiritualization");
            if (level >= 55) {
               skills.add("Predator's Presence");
            }

            if (level >= 70) {
               skills.add("Assassin Stance");
            }

            if (level >= 90) {
               skills.add("Spatial Execution");
            }
         case 8:
         default:
            break;
         case 9:
            skills.add("Claw-Rift Passage");
            if (level >= 60) {
               skills.add("Rubble Jaw");
            }

            if (level >= 75) {
               skills.add("King's Maul");
            }

            if (level >= 90) {
               skills.add("Feral Reconstitution");
            }

            if (level >= 120) {
               skills.add("White Fang Sovereign");
            }
            break;
         case 10:
            skills.add("Destruction Claw");
            if (level >= 55) {
               skills.add("Breath of Destruction");
            }

            if (level >= 70) {
               skills.add("Monarch's Descent");
            }

            if (level >= 85) {
               skills.add("Sovereign Roar");
            }

            if (level >= 100) {
               skills.add("Extinction");
            }

            if (level >= 120) {
               skills.add("Monarch Manifestation");
            }
      }

      return List.copyOf(skills);
   }

   public static boolean isShadowMonarch(Entity entity) {
      SololevelingModVariables.PlayerVariables vars = variables(entity);
      return VesselProgressionRules.isShadowMonarch(resolvedJob(entity, vars));
   }

   public static boolean canUseShadowExchangeRunestone(Entity entity) {
      SololevelingModVariables.PlayerVariables vars = variables(entity);
      return VesselProgressionRules.canUseShadowExchangeRunestone(resolvedJob(entity, vars), vars.ShadowExchange);
   }

   public static boolean canUnlockShadowManifestation(Entity entity) {
      SololevelingModVariables.PlayerVariables vars = variables(entity);
      return VesselProgressionRules.canUnlockShadowManifestation(
         resolvedJob(entity, vars), vars.ShadowBody, vars.Level, vars.shadowstorageusage, vars.ShadowExchange, vars.dkc_cleared
      );
   }

   private static void syncDemonKingsCastle(ServerPlayer player, SololevelingModVariables.PlayerVariables vars, int resolvedJob) {
      if (vars.dkc_started || vars.dkc_cleared > 0.0) {
         award(player, "system/dkc_entered");
      }

      if (vars.dkc_cleared >= 1.0) {
         award(player, "system/dkc_first_floor");
      }

      if (vars.dkc_cleared >= 10.0) {
         award(player, "system/dkc_midpoint");
         if (VesselProgressionRules.hasMonarchsDomain(resolvedJob, vars.dkc_cleared)) {
            award(player, "monarchs_domain");
         }
      }

      if (vars.dkc_cleared >= 15.0) {
         award(player, "system/dkc_radiru");
         if (vars.radiru_pact && !vars.radiru_slaughtered) {
            award(player, "system/dkc_radiru_pact");
         }

         if (vars.radiru_slaughtered) {
            award(player, "system/dkc_radiru_bloodshed");
         }
      }

      if (vars.dkc_cleared >= 20.0) {
         award(player, "system/dkc_conquered");
      }

      if (vars.dkc_cleared >= 20.0) {
         BaranVictoryRewards.grantIfNeeded(player);
      }
   }

   private static void syncVesselIdentity(ServerPlayer player, VesselManager.VesselDefinition definition) {
      String identity = definition.identity();
      switch (identity) {
         case "ashborn":
            award(player, "vessels/shadow_monarch");
            break;
         case "sillad":
            award(player, "coldest_monarch");
            break;
         case "baran":
            award(player, "monarch_of_blue_flames");
            break;
         case "rakan":
            award(player, "vessels/monarch_of_fangs");
            break;
         case "antares":
            award(player, "vessels/monarch_of_destruction");
            break;
         case "christopher_reed":
            award(player, "scorching_mage");
            award(player, "vessels/rulers/flame_manifestation");
            break;
         case "thomas_andre":
            award(player, "vessels/thomas_andre");
            break;
         case "liu_zhigang":
            award(player, "vessels/liu_zhigang");
            break;
         case "sung_il_hwan":
            award(player, "vessels/sung_il_hwan");
            award(player, "vessels/rulers/silent_manifestation");
            break;
         case "go_gunhee":
            award(player, "vessels/go_gunhee");
            award(player, "vessels/rulers/brilliant_manifestation");
      }

      if ("ruler".equals(definition.type()) && !"ashborn".equals(identity)) {
         award(player, "rulers_will");
      }
   }

   private static void syncSkillAdvancements(ServerPlayer player, int job) {
      List<String> unlocked = unlockedSkills(player, job);
      switch (job) {
         case 1:
            award(player, "shadow_extraction");
            award(player, "shadow_storage");
            double level = variables(player).Level;
            if (level >= 70.0) {
               award(player, "shadow_storage_level_2");
            }

            if (level >= 90.0) {
               award(player, "shadow_storage_level_3");
            }

            if (level >= 100.0) {
               award(player, "shadow_storage_level_4");
            }

            if (level >= 120.0) {
               award(player, "shadow_storage_level_5");
            }

            if (unlocked.contains("Shadow Exchange")) {
               award(player, "shadow_exchange");
            }

            if (unlocked.contains("Shadow Manifestation")) {
               award(player, "shadow_spiritual_body_manifestation");
            }
            break;
         case 2:
            award(player, "vessels/flame/fire_charge");
            if (unlocked.contains("Meteor Rain")) {
               award(player, "vessels/flame/meteor_rain");
            }

            if (unlocked.contains("Fireflies")) {
               award(player, "vessels/flame/fireflies");
            }
            break;
         case 3:
            award(player, "vessels/frost/ice_spear");
            awardIfUnlocked(player, unlocked, "Flash Freeze", "vessels/frost/flash_freeze");
            awardIfUnlocked(player, unlocked, "Frozen Path", "vessels/frost/frozen_path");
            awardIfUnlocked(player, unlocked, "Frozen Architecture", "vessels/frost/frozen_architecture");
            awardIfUnlocked(player, unlocked, "Frost Counter", "vessels/frost/frost_counter");
            awardIfUnlocked(player, unlocked, "Absolute Zero", "vessels/frost/absolute_zero");
            awardIfUnlocked(player, unlocked, "Frost Monarch Spiritualization", "vessels/frost/spiritual_body");
            break;
         case 4:
            award(player, "vessels/white_flames/lightning_breath");
            award(player, "vessels/white_flames/storm_inheritance");
            awardIfUnlocked(player, unlocked, "Radiru Blood Spear", "vessels/white_flames/blood_spear");
            awardIfUnlocked(player, unlocked, "Doppelganger", "vessels/white_flames/doppelganger");
            awardIfUnlocked(player, unlocked, "Hellstorm Dominion", "vessels/white_flames/hellstorm");
            awardIfUnlocked(player, unlocked, "Hell's Army", "vessels/white_flames/hells_army");
            awardIfUnlocked(player, unlocked, "White Flame Spiritualization", "vessels/white_flames/spiritual_body");
            break;
         case 5:
            award(player, "vessels/rulers/goliath_manifestation");
            awardIfUnlocked(player, unlocked, "Capture", "vessels/rulers/capture");
            awardIfUnlocked(player, unlocked, "Power Smash", "vessels/rulers/power_smash");
            awardIfUnlocked(player, unlocked, "Collapse", "vessels/rulers/collapse");
            break;
         case 6:
            award(player, "vessels/rulers/dragon_sword_manifestation");
            awardIfUnlocked(player, unlocked, "Heavenly Counter", "vessels/rulers/heavenly_counter");
            awardIfUnlocked(player, unlocked, "Golden Dragon Dance", "vessels/rulers/golden_dragon_dance");
            awardIfUnlocked(player, unlocked, "Sovereign Sword Domain", "vessels/rulers/sword_domain");
            break;
         case 7:
            award(player, "vessels/rulers/silent_manifestation");
            awardIfUnlocked(player, unlocked, "Predator's Presence", "vessels/rulers/predators_presence");
            awardIfUnlocked(player, unlocked, "Assassin Stance", "vessels/rulers/assassin_stance");
            awardIfUnlocked(player, unlocked, "Spatial Execution", "vessels/rulers/spatial_execution");
         case 8:
         default:
            break;
         case 9:
            award(player, "vessels/fangs/claw_rift");
            awardIfUnlocked(player, unlocked, "Rubble Jaw", "vessels/fangs/rubble_jaw");
            awardIfUnlocked(player, unlocked, "King's Maul", "vessels/fangs/kings_maul");
            awardIfUnlocked(player, unlocked, "Feral Reconstitution", "vessels/fangs/reconstitution");
            awardIfUnlocked(player, unlocked, "White Fang Sovereign", "vessels/fangs/spiritual_body");
            break;
         case 10:
            award(player, "vessels/destruction/destruction_claw");
            awardIfUnlocked(player, unlocked, "Breath of Destruction", "vessels/destruction/breath_of_destruction");
            awardIfUnlocked(player, unlocked, "Monarch's Descent", "vessels/destruction/monarchs_descent");
            awardIfUnlocked(player, unlocked, "Sovereign Roar", "vessels/destruction/sovereign_roar");
            awardIfUnlocked(player, unlocked, "Extinction", "vessels/destruction/extinction");
            awardIfUnlocked(player, unlocked, "Monarch Manifestation", "vessels/destruction/monarch_manifestation");
      }
   }

   private static void awardIfUnlocked(ServerPlayer player, List<String> unlocked, String skill, String advancement) {
      if (unlocked.contains(skill)) {
         award(player, advancement);
      }
   }

   private static boolean award(ServerPlayer player, String path) {
      Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling", path));
      if (advancement == null) {
         return false;
      }

      AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
      if (progress.isDone()) {
         return false;
      }

      ArrayList<String> remaining = new ArrayList<>();
      progress.getRemainingCriteria().forEach(remaining::add);

      for (String criterion : remaining) {
         player.getAdvancements().award(advancement, criterion);
      }

      return true;
   }

   private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
      return entity == null
         ? new SololevelingModVariables.PlayerVariables()
         : entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static int resolvedJob(Entity entity, SololevelingModVariables.PlayerVariables vars) {
      VesselManager.VesselDefinition definition = VesselManager.currentDefinition(entity);
      return definition == null ? (int)vars.JOB : definition.jobId();
   }

   private static void add(List<String> target, String... skills) {
      target.addAll(List.of(skills));
   }
}
