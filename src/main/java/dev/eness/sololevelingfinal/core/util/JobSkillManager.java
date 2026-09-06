package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.Ability1OnKeyPressedProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability1OnKeyReleasedProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability2OnKeyPressedProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability3OnKeyPressedProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability3ResetProcedure;
import dev.eness.sololevelingfinal.core.procedures.Ability4OnKeyPressedProcedure;
import dev.eness.sololevelingfinal.core.procedures.AriseSkillProcedure;
import dev.eness.sololevelingfinal.core.procedures.GoliathManifestationProcedure;
import dev.eness.sololevelingfinal.core.procedures.ShadowCommandOpenProcedure;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;

@EventBusSubscriber
public class JobSkillManager {
   public static final int RULER_SKILL_COLOR = 16765774;
   public static final String ARISE = "Arise";
   public static final String SHADOW_SUMMON = "Shadow Summon";
   public static final String DISMISS_SHADOWS = "Dismiss Shadows";
   public static final String SHADOW_COMMAND = "Shadow Command";
   public static final String SHADOW_EXCHANGE = "Shadow Exchange";
   public static final String SHADOW_MANIFESTATION = "Shadow Manifestation";
   public static final String GRAND_MARSHAL_AUTHORITY = "Grand Marshal Authority";
   public static final String RUNESTONE_SHADOW_EXCHANGE_TAG = "slr_runestone_skill_shadow_exchange";
   public static final String RUNESTONE_SHADOW_MANIFESTATION_TAG = "slr_runestone_skill_shadow_manifestation";
   public static final String FIRE_CHARGE = "Fire Charge";
   public static final String METEOR_RAIN = "Meteor Rain";
   public static final String FIREFLIES = "Fireflies";
   public static final String ICE_SPEAR = "Ice Spear";
   public static final String FLASH_FREEZE = "Flash Freeze";
   public static final String FROZEN_PATH = "Frozen Path";
   public static final String FROZEN_ARCHITECTURE = "Frozen Architecture";
   public static final String FROST_COUNTER = "Frost Counter";
   public static final String ABSOLUTE_ZERO = "Absolute Zero";
   public static final String FROST_SPIRITUALIZATION = "Frost Monarch Spiritualization";
   public static final String MONARCH_BEAM = "Monarch Beam";
   public static final String LIGHTNING_STORM = "Lightning Storm";
   public static final String STORM_BURST = "Storm Burst";
   public static final String LIGHTNING_BREATH = "Lightning Breath";
   public static final String HELLSTORM_DOMINION = "Hellstorm Dominion";
   public static final String RADIRU_BLOOD_SPEAR = "Radiru Blood Spear";
   public static final String DOPPELGANGER = "Doppelganger";
   public static final String HELLS_ARMY = "Hell's Army";
   public static final String WHITE_FLAME_SPIRITUALIZATION = "White Flame Spiritualization";
   public static final String THOMAS_MANIFESTATION = "Spiritual Body Manifestation";
   public static final String THOMAS_CAPTURE = "Capture";
   public static final String THOMAS_POWER_SMASH = "Power Smash";
   public static final String THOMAS_COLLAPSE = "Collapse";
   public static final String LIU_HEAVENLY_COUNTER = "Heavenly Counter";
   public static final String LIU_GOLDEN_DRAGON_DANCE = "Golden Dragon Dance";
   public static final String LIU_SOVEREIGN_SWORD_DOMAIN = "Sovereign Sword Domain";
   public static final String LIU_MANIFESTATION = "Dragon Sword Manifestation";
   public static final String SUNG_PREDATORS_PRESENCE = "Predator's Presence";
   public static final String SUNG_ASSASSIN_STANCE = "Assassin Stance";
   public static final String SUNG_SPATIAL_EXECUTION = "Spatial Execution";
   public static final String SUNG_SPIRITUALIZATION = "Spiritualization";
   public static final String BEAST_CLAW_RIFT = "Claw-Rift Passage";
   public static final String BEAST_RUBBLE_JAW = "Rubble Jaw";
   public static final String BEAST_KINGS_MAUL = "King's Maul";
   public static final String BEAST_RECONSTITUTION = "Feral Reconstitution";
   public static final String BEAST_WHITE_FANG = "White Fang Sovereign";
   public static final String ANTARES_DESTRUCTION_CLAW = "Destruction Claw";
   public static final String ANTARES_BREATH = "Breath of Destruction";
   public static final String ANTARES_DESCENT = "Monarch's Descent";
   public static final String ANTARES_ROAR = "Sovereign Roar";
   public static final String ANTARES_EXTINCTION = "Extinction";
   public static final String ANTARES_MANIFESTATION = "Monarch Manifestation";
   private static final String LAST_SYNCED_JOB = "sololeveling:last_synced_job_skills";
   private static final String RETIRED_KINGS_VERDICT = "King's Verdict";
   private static final List<String> WHITE_FLAME_SKILLS = List.of(
      "Lightning Breath", "Hellstorm Dominion", "Radiru Blood Spear", "Doppelganger", "Hell's Army", "White Flame Spiritualization"
   );
   private static final List<String> LIU_SKILLS = List.of("Heavenly Counter", "Golden Dragon Dance", "Sovereign Sword Domain", "Dragon Sword Manifestation");
   private static final List<String> SUNG_SKILLS = List.of("Predator's Presence", "Assassin Stance", "Spatial Execution", "Spiritualization");
   private static final List<String> FROST_SKILLS = List.of(
      "Ice Spear", "Flash Freeze", "Frozen Path", "Frozen Architecture", "Frost Counter", "Absolute Zero", "Frost Monarch Spiritualization"
   );
   private static final List<String> BEAST_SKILLS = List.of("Claw-Rift Passage", "Rubble Jaw", "King's Maul", "Feral Reconstitution", "White Fang Sovereign");
   private static final List<String> ANTARES_SKILLS = List.of(
      "Destruction Claw", "Breath of Destruction", "Monarch's Descent", "Sovereign Roar", "Extinction", "Monarch Manifestation"
   );
   private static final List<String> ALL_JOB_SKILLS = List.of(
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
      "Ice Ball",
      "Ice Chunk",
      "Snow Screen",
      "Stillness Decree",
      "Pale Causeway",
      "Winter Remembers",
      "Whiteout Procession",
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
      "Monarch Beam",
      "Lightning Storm",
      "Storm Burst",
      "Lightning Breath",
      "Hellstorm Dominion",
      "King's Verdict",
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
      "Monarch Manifestation"
   );

   private JobSkillManager() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide()) {
         if (event.player.tickCount % 40 == 0) {
            syncJobSkills(event.player);
         }
      }
   }

   public static boolean isJobSkill(String skill) {
      return ALL_JOB_SKILLS.contains(skill);
   }

   public static void markRunestoneSkill(Entity entity, String tag) {
      if (entity != null && tag != null && !tag.isBlank()) {
         runestoneData(entity).putBoolean(tag, true);
      }
   }

   public static boolean hasRunestoneSkill(Entity entity, String tag) {
      return entity != null && tag != null && !tag.isBlank() ? runestoneData(entity).getBoolean(tag) || entity.getPersistentData().getBoolean(tag) : false;
   }

   public static boolean isWhiteFlameSkill(String skill) {
      return WHITE_FLAME_SKILLS.contains(skill);
   }

   public static boolean isLiuSkill(String skill) {
      return LIU_SKILLS.contains(skill);
   }

   public static boolean isSungSkill(String skill) {
      return SUNG_SKILLS.contains(skill);
   }

   public static boolean isFrostSkill(String skill) {
      return FROST_SKILLS.contains(skill);
   }

   public static boolean isBeastSkill(String skill) {
      return BEAST_SKILLS.contains(skill);
   }

   public static boolean isAntaresSkill(String skill) {
      return ANTARES_SKILLS.contains(skill);
   }

   public static int skillColor(String skill) {
      if (List.of("Arise", "Shadow Summon", "Dismiss Shadows", "Shadow Command", "Shadow Exchange", "Shadow Manifestation", "Grand Marshal Authority")
         .contains(skill)) {
         return 12150271;
      } else if (List.of("Fire Charge", "Meteor Rain", "Fireflies").contains(skill)) {
         return 16734770;
      } else if (isFrostSkill(skill)) {
         return 7334143;
      } else if (List.of("Capture", "Power Smash", "Collapse", "Spiritual Body Manifestation").contains(skill) || isLiuSkill(skill) || isSungSkill(skill)) {
         return 16765774;
      } else if (isWhiteFlameSkill(skill)) {
         return 16777215;
      } else if (isBeastSkill(skill)) {
         return 16747044;
      } else if (isAntaresSkill(skill)) {
         return 14889278;
      } else {
         return List.of("Monarch Beam", "Lightning Storm", "Storm Burst").contains(skill) ? 16769930 : 16777215;
      }
   }

   public static List<Component> tooltip(Entity entity, String skill) {
      if ("Grand Marshal Authority".equals(skill) && !DeveloperModeManager.isEnabled(entity)) {
         return List.of(
            Component.literal("Grand Marshal Authority").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            Component.literal("WIP (Work in progress)").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
         );
      }

      if (RangerCombatManager.isRangerSkill(skill)) {
         return RangerCombatManager.tooltip(entity, skill);
      }

      if (FireMageSpellManager.isFireSkill(skill)) {
         return FireMageSpellManager.tooltip(entity, skill);
      }

      if (BarrierMageSpellManager.isBarrierSkill(skill)) {
         return BarrierMageSpellManager.tooltip(entity, skill);
      }

      if (ArcaneMageSpellManager.isArcaneSkill(skill)) {
         return ArcaneMageSpellManager.tooltip(entity, skill);
      }

      if (StormMageSpellManager.isStormSkill(skill)) {
         return StormMageSpellManager.tooltip(entity, skill);
      }

      if (isFrostSkill(skill)) {
         return frostTooltip(entity, skill);
      }

      if (isWhiteFlameSkill(skill)) {
         return whiteFlameTooltip(entity, skill);
      }

      if (isLiuSkill(skill)) {
         return liuTooltip(entity, skill);
      }

      if (isSungSkill(skill)) {
         return sungTooltip(entity, skill);
      }

      if (isBeastSkill(skill)) {
         return beastTooltip(entity, skill);
      }

      if (isAntaresSkill(skill)) {
         return antaresTooltip(entity, skill);
      }

      if ("Grand Marshal Authority".equals(skill)) {
         return List.of(
            Component.literal("Grand Marshal Authority").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            Component.literal("Borrow the signature art of your appointed Grand Marshal.").withStyle(ChatFormatting.GRAY),
            Component.literal("The commander must be summoned and alive.").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.literal("Each signature spends mana; all commanders share one recharge.").withStyle(ChatFormatting.YELLOW)
         );
      }

      if ("Arise".equals(skill)) {
         return List.of(
            Component.literal("Arise").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
            Component.literal("Extract every eligible shadow within 18 blocks.").withStyle(ChatFormatting.GRAY),
            Component.literal("Includes Igris, Beru, and Kaisel. Sneak-cast to scan.").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.literal("500 MP per successful extraction | 2.6s cooldown").withStyle(ChatFormatting.YELLOW)
         );
      }

      if (!List.of("Capture", "Power Smash", "Collapse", "Spiritual Body Manifestation").contains(skill)) {
         return List.of(Component.literal(ShadowMonarchManager.displaySkillName(entity, skill)), Component.literal(skill));
      }

      boolean manifested = GoliathCombatManager.isManifested(entity);
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
      switch (skill) {
         case "Capture":
            lines.add(Component.literal("Pull and restrain enemies with crushing authority.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     manifested ? "Sovereign Capture: recast to hurl every captured target." : "Manifested: wider pull, suspension, and a throw recast."
                  )
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Power Smash":
            lines.add(Component.literal("Drive a mana-loaded fist through a frontal formation.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(manifested ? "Goliath Breaker: extended wave and fractured guard." : "Manifested: greater reach, damage, and guard damage.")
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Collapse":
            lines.add(Component.literal("Shatter the battlefield with a radial ground impact.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     manifested
                        ? "Continental Collapse: pressure lock followed by a second rupture."
                        : "Manifested: larger impact and a delayed second shockwave."
                  )
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Spiritual Body Manifestation":
            lines.add(Component.literal("Manifest the golden Goliath armor.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Transforms all Goliath skills and combat-stance attacks.").withStyle(ChatFormatting.YELLOW));
      }

      return lines;
   }

   private static List<Component> frostTooltip(Entity entity, String skill) {
      boolean manifested = FrostMonarchManager.isSpiritualized(entity);
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
      switch (skill) {
         case "Ice Spear":
            lines.add(Component.literal("Throw the Ice Spear, then cast again to recall it.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Both passes build Frostbite and shatter frozen enemies.").withStyle(ChatFormatting.AQUA));
            lines.add(Component.literal("Sneak-cast to receive the existing Ice Spear item in your inventory.").withStyle(ChatFormatting.DARK_AQUA));
            lines.add(Component.literal((manifested ? "300" : "260") + " MP throw | 5.5s cooldown | Sneak: 100 MP, 3s").withStyle(ChatFormatting.YELLOW));
            break;
         case "Flash Freeze":
            lines.add(Component.literal("Damage enemies in a forward cone and inflict heavy Frostbite.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Already-frozen enemies shatter into damaging ice fragments.").withStyle(ChatFormatting.AQUA));
            lines.add(Component.literal((manifested ? "12 blocks | 320 MP" : "9 blocks | 280 MP") + " | 7s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "Frozen Path":
            lines.add(Component.literal("Hold to steer a piercing ice current that forms a two-block-wide road.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Sneak before casting to ride it; release to rupture the current.").withStyle(ChatFormatting.AQUA));
            lines.add(Component.literal((manifested ? "210" : "180") + " MP + upkeep | 7-8s cooldown after release").withStyle(ChatFormatting.YELLOW));
            break;
         case "Frozen Architecture":
            lines.add(Component.literal("Hold to open the sovereign construction wheel.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Move the mouse left or right; release to build the shape at the top.").withStyle(ChatFormatting.AQUA));
            lines.add(Component.literal("Temporary, protected ice | " + (manifested ? "300" : "340") + " MP | 9s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "Frost Counter":
            lines.add(Component.literal("Parry the next hit and retaliate with a freezing rupture.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("The counter deals damage, inflicts Frostbite, and can shatter.").withStyle(ChatFormatting.AQUA));
            lines.add(
               Component.literal(manifested ? "2s window | 75% reduction | 280 MP | 9s cooldown" : "1.6s window | 60% reduction | 240 MP | 9s cooldown")
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Absolute Zero":
            lines.add(Component.literal("Create a damaging freezing field that follows you.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Recast to detonate chilled and frozen enemies at once.").withStyle(ChatFormatting.AQUA));
            lines.add(
               Component.literal(manifested ? "10 blocks for 10s | 700 MP | 20s cooldown" : "8 blocks for 8s | 600 MP | 20s cooldown")
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Frost Monarch Spiritualization":
            lines.add(Component.literal("Toggle the Frost Monarch's enhanced spiritual aura.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Enhances Frost skills until disabled or MP is depleted.").withStyle(ChatFormatting.AQUA));
            lines.add(Component.literal("0 MP activation | 14 MP per second | No cooldown").withStyle(ChatFormatting.YELLOW));
      }

      return lines;
   }

   private static List<Component> liuTooltip(Entity entity, String skill) {
      boolean manifested = LiuZhigangCombatManager.isManifested(entity);
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
      switch (skill) {
         case "Heavenly Counter":
            lines.add(Component.literal("Enter a razor-thin counter window and turn force back on its source.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     manifested
                        ? "Manifested: two counters; success advances the next beam charge tier."
                        : "A successful counter empowers the next charged sword beam."
                  )
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Golden Dragon Dance":
            lines.add(Component.literal("Cross the battlefield in a chained sequence of sovereign cuts.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     manifested
                        ? "Manifested: hunts up to ten targets with stronger Dragon Sword cuts."
                        : "With no target, releases three advancing sword waves."
                  )
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Sovereign Sword Domain":
            lines.add(Component.literal("Claim the area as a field of suspended, unseen sword paths.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Attacks echo, projectiles are repelled, and every mark ruptures together.").withStyle(ChatFormatting.YELLOW));
            break;
         case "Dragon Sword Manifestation":
            lines.add(Component.literal("Manifest the twin Dragon Swords granted by a Ruler's power.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     manifested
                        ? "ACTIVE: original hand items are safely sealed until release."
                        : "Transforms Liu's skills and preserves both held items exactly."
                  )
                  .withStyle(ChatFormatting.YELLOW)
            );
      }

      return lines;
   }

   private static List<Component> sungTooltip(Entity entity, String skill) {
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
      Component description = SungIlHwanCombatManager.tooltip(entity, skill);
      if (!description.getString().isBlank()) {
         lines.add(description.copy().withStyle(ChatFormatting.GRAY));
      }

      return lines;
   }

   private static List<Component> beastTooltip(Entity entity, String skill) {
      boolean sovereign = BeastMonarchManager.isWhiteFangSovereign(entity);
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
      switch (skill) {
         case "Claw-Rift Passage":
            lines.add(Component.literal("Rip forward through space and claw every enemy along the route.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     sovereign
                        ? "Sovereign: farther dash, wider path, and stronger cuts."
                        : "The dash stops safely at terrain and damages every enemy crossed."
                  )
                  .withStyle(ChatFormatting.GOLD)
            );
            lines.add(Component.literal("220 base MP | 6s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "Rubble Jaw":
            lines.add(Component.literal("Erupt the targeted area in a violent jaw of shattered ground.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     sovereign ? "Sovereign: larger eruption, stronger launch, and more damage." : "Deals immediate area damage and launches enemies upward."
                  )
                  .withStyle(ChatFormatting.GOLD)
            );
            lines.add(Component.literal("300 base MP | 8.5s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "King's Maul":
            lines.add(Component.literal("Lunge at the enemy in front of you and deliver a crushing maul.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     sovereign
                        ? "Sovereign: longer lunge, heavier hit, and stronger movement lock."
                        : "Automatically catches a visible target in your forward cone."
                  )
                  .withStyle(ChatFormatting.GOLD)
            );
            lines.add(Component.literal("380 base MP | 10s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "Feral Reconstitution":
            lines.add(Component.literal("Instantly regrow missing health and form a temporary absorption hide.").withStyle(ChatFormatting.GRAY));
            lines.add(
               Component.literal(
                     sovereign ? "Sovereign: stronger healing and a thicker, longer-lasting hide." : "No channel, wound requirement, Quarry, or Hunt cost."
                  )
                  .withStyle(ChatFormatting.GOLD)
            );
            lines.add(Component.literal("260 base MP | 12s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "White Fang Sovereign":
            lines.add(Component.literal("Instantly enter the White Fang's close-combat manifestation.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Enhances every Beast skill, adds a fourth claw beat, and increases movement speed.").withStyle(ChatFormatting.GOLD));
            lines.add(Component.literal("600 base MP | 20s duration | Press again to end early").withStyle(ChatFormatting.YELLOW));
      }

      return lines;
   }

   private static List<Component> antaresTooltip(Entity entity, String skill) {
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
      switch (skill) {
         case "Destruction Claw":
            lines.add(Component.literal("Carve a destructive cone and gain Ruin when it hits.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Sneak-cast at 3 Ruin to spend it on a wider, crushing finisher.").withStyle(ChatFormatting.RED));
            lines.add(Component.literal("140 base MP | 3.6s cooldown | 3 Ruin max").withStyle(ChatFormatting.YELLOW));
            break;
         case "Breath of Destruction":
            lines.add(Component.literal("Channel a steerable line of destruction-fire through enemies.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Successful pulses ignite targets and grant one Ruin per cast.").withStyle(ChatFormatting.RED));
            lines.add(Component.literal("320 base MP | 8s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "Monarch's Descent":
            lines.add(Component.literal("Launch into a controllable dragon rush, then rupture on landing.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Airborne downward casts begin as dives; successful impacts grant Ruin.").withStyle(ChatFormatting.RED));
            lines.add(Component.literal("280 base MP | 9s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "Sovereign Roar":
            lines.add(Component.literal("Release a dragon pressure wave that damages and Overawes enemies.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Repels hostile projectiles; bosses receive reduced control.").withStyle(ChatFormatting.RED));
            lines.add(Component.literal("420 base MP | 14s cooldown").withStyle(ChatFormatting.YELLOW));
            break;
         case "Extinction":
            lines.add(Component.literal("Lock a long destruction lane, then fire three catastrophic pulses.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Requires and consumes all 3 Ruin. Aim locks during the wind-up.").withStyle(ChatFormatting.RED));
            lines.add(Component.literal("900 base MP | 30s cooldown | Costs 3 Ruin").withStyle(ChatFormatting.YELLOW));
            break;
         case "Monarch Manifestation":
            lines.add(Component.literal("Toggle Antares's draconic spiritual body and destruction aura.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Enhances every art, prevents Ruin decay, and resists fire and explosions.").withStyle(ChatFormatting.RED));
            lines.add(Component.literal("800 base MP to awaken | 16 MP per second | Recast to release").withStyle(ChatFormatting.YELLOW));
      }

      return lines;
   }

   private static List<Component> whiteFlameTooltip(Entity entity, String skill) {
      boolean manifested = WhiteFlameMonarchManager.isSpiritualized(entity);
      ArrayList<Component> lines = new ArrayList<>();
      lines.add(Component.literal(skill).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
      switch (skill) {
         case "Lightning Breath":
            lines.add(Component.literal("Exhale a steerable stream of white lightning-fire.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Repeated hits brand enemies for Hellstorm Dominion.").withStyle(ChatFormatting.AQUA));
            lines.add(
               Component.literal(manifested ? "Manifested: wider, longer, faster, and more efficient." : "220 MP | 3.9s cooldown")
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Hellstorm Dominion":
            lines.add(Component.literal("Claim a moving domain that hunts nearby enemies with lightning.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Branded enemies are prioritized; repeated strikes on one target lose power.").withStyle(ChatFormatting.AQUA));
            lines.add(
               Component.literal(manifested ? "Manifested: larger domain, denser storm, longer reign." : "850 MP | 19.5s cooldown")
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Radiru Blood Spear":
            lines.add(Component.literal("Hurl Radiru's royal spear through an enemy formation.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Pierces targets, ignites them, and carves in a royal brand.").withStyle(ChatFormatting.AQUA));
            lines.add(
               Component.literal(manifested ? "Manifested: seven-target pierce with greater velocity." : "300 MP | 4.8s cooldown")
                  .withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Doppelganger":
            lines.add(Component.literal("Create three false selves that intercept incoming attacks.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Each broken echo dodges, repositions, and retaliates.").withStyle(ChatFormatting.AQUA));
            lines.add(
               Component.literal(manifested ? "Manifested: four echoes with a longer lifetime." : "590 MP | 14s cooldown").withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "Hell's Army":
            lines.add(Component.literal("Open Hell's gate and call Radiru's temporary royal guard.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("The guards follow your aggression and cannot harm you.").withStyle(ChatFormatting.AQUA));
            lines.add(
               Component.literal(manifested ? "Manifested: seven stronger guards remain longer." : "1200 MP | 32.5s cooldown").withStyle(ChatFormatting.YELLOW)
            );
            break;
         case "White Flame Spiritualization":
            lines.add(Component.literal("Unseal Baran's spiritual body without mortal armor.").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Adds 25% dodge after Perception; a dodge chains for 0.5 seconds.").withStyle(ChatFormatting.AQUA));
            lines.add(
               Component.literal(manifested ? "ACTIVE: skills have entered their sovereign forms." : "800 MP to awaken | 14 MP per second")
                  .withStyle(ChatFormatting.YELLOW)
            );
      }

      return lines;
   }

   public static void syncJobSkills(Entity entity) {
      if (entity != null) {
         SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         int job = (int)vars.JOB;
         List<String> granted = skillsForEntityJob(entity, job);
         boolean keepFormations = job == 1;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            String updatedList = writeSkillList(mergedJobSkillList(entity, capability.Plist, granted, keepFormations));
            boolean changed = !updatedList.equals(capability.Plist);
            changed |= clearStaleEquippedSkills(capability, granted, keepFormations);
            if (changed) {
               capability.Plist = updatedList;
               capability.syncPlayerVariables(entity);
            }
         });
         entity.getPersistentData().putInt("sololeveling:last_synced_job_skills", job);
      }
   }

   public static boolean cast(LevelAccessor world, double x, double y, double z, Entity entity, String skill) {
      if (entity != null && skill != null && isJobSkill(skill)) {
         syncJobSkills(entity);
         SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (!skillsForEntityJob(entity, (int)vars.JOB).contains(skill)) {
            if (entity instanceof Player player && !player.level().isClientSide()) {
               player.displayClientMessage(Component.literal("This job skill does not belong to your current job."), true);
            }

            return true;
         } else {
            switch (skill) {
               case "Arise":
                  AriseSkillProcedure.execute(world, x, y, z, entity);
                  break;
               case "Shadow Summon":
                  runOldJobAbility(entity, () -> Ability1OnKeyPressedProcedure.execute(world, x, y, z, entity));
                  break;
               case "Dismiss Shadows":
                  Ability3ResetProcedure.execute(world, x, y, z, entity);
                  break;
               case "Shadow Command":
                  ShadowCommandOpenProcedure.execute(world, x, y, z, entity);
                  break;
               case "Shadow Exchange":
                  runOldJobAbility(entity, () -> Ability3OnKeyPressedProcedure.execute(world, x, y, z, entity));
                  break;
               case "Shadow Manifestation":
                  runOldJobAbility(entity, () -> Ability4OnKeyPressedProcedure.execute(world, x, y, z, entity));
                  break;
               case "Grand Marshal Authority":
                  GrandMarshalAbilityManager.cast(entity);
                  break;
               case "Capture":
                  GoliathCombatManager.castCapture(entity);
                  break;
               case "Power Smash":
                  GoliathCombatManager.castPowerSmash(entity);
                  break;
               case "Collapse":
                  GoliathCombatManager.castCollapse(entity);
                  break;
               case "Spiritual Body Manifestation":
                  GoliathManifestationProcedure.execute(world, x, y, z, entity);
                  break;
               case "Heavenly Counter":
                  LiuZhigangCombatManager.castHeavenlyCounter(entity);
                  break;
               case "Golden Dragon Dance":
                  LiuZhigangCombatManager.castGoldenDragonDance(entity);
                  break;
               case "Sovereign Sword Domain":
                  LiuZhigangCombatManager.castSovereignSwordDomain(entity);
                  break;
               case "Dragon Sword Manifestation":
                  LiuZhigangCombatManager.toggleDragonSwordManifestation(entity);
                  break;
               case "Predator's Presence":
               case "Assassin Stance":
               case "Spatial Execution":
               case "Spiritualization":
                  SungIlHwanCombatManager.press(entity, skill);
                  break;
               case "Claw-Rift Passage":
                  BeastMonarchManager.castClawRift(entity);
                  break;
               case "Rubble Jaw":
                  BeastMonarchManager.castRubbleJaw(entity);
                  break;
               case "King's Maul":
                  BeastMonarchManager.castKingsMaul(entity);
                  break;
               case "Feral Reconstitution":
                  BeastMonarchManager.castFeralReconstitution(entity);
                  break;
               case "White Fang Sovereign":
                  BeastMonarchManager.castWhiteFangSovereign(entity);
                  break;
               case "Destruction Claw":
                  AntaresCombatManager.castDestructionClaw(entity);
                  break;
               case "Breath of Destruction":
                  AntaresCombatManager.castBreathOfDestruction(entity);
                  break;
               case "Monarch's Descent":
                  AntaresCombatManager.castMonarchsDescent(entity);
                  break;
               case "Sovereign Roar":
                  AntaresCombatManager.castSovereignRoar(entity);
                  break;
               case "Extinction":
                  AntaresCombatManager.castExtinction(entity);
                  break;
               case "Monarch Manifestation":
                  AntaresCombatManager.toggleManifestation(entity);
                  break;
               case "Fire Charge":
                  castFireCharge(world, x, y, z, entity);
                  break;
               case "Meteor Rain":
                  runOldJobAbility(entity, () -> Ability2OnKeyPressedProcedure.execute(world, x, y, z, entity));
                  break;
               case "Fireflies":
                  runOldJobAbility(entity, () -> Ability3OnKeyPressedProcedure.execute(world, x, y, z, entity));
                  break;
               case "Ice Spear":
                  FrostMonarchManager.castIceSpear(entity);
                  break;
               case "Flash Freeze":
                  FrostMonarchManager.castFlashFreeze(entity);
                  break;
               case "Frozen Path":
                  FrostMonarchManager.castFrozenPath(entity);
               case "Frozen Architecture":
               default:
                  break;
               case "Frost Counter":
                  FrostMonarchManager.castFrostCounter(entity);
                  break;
               case "Absolute Zero":
                  FrostMonarchManager.castAbsoluteZero(entity);
                  break;
               case "Frost Monarch Spiritualization":
                  FrostMonarchManager.toggleSpiritualization(entity);
                  break;
               case "Lightning Breath":
                  WhiteFlameMonarchManager.castLightningBreath(entity);
                  break;
               case "Hellstorm Dominion":
                  WhiteFlameMonarchManager.castHellstormDominion(entity);
                  break;
               case "Radiru Blood Spear":
                  WhiteFlameMonarchManager.castRadiruBloodSpear(entity);
                  break;
               case "Doppelganger":
                  WhiteFlameMonarchManager.castDoppelganger(entity);
                  break;
               case "Hell's Army":
                  WhiteFlameMonarchManager.castHellsArmy(entity);
                  break;
               case "White Flame Spiritualization":
                  WhiteFlameMonarchManager.toggleSpiritualization(entity);
                  break;
               case "Monarch Beam":
                  castMonarchBeam(entity);
                  break;
               case "Lightning Storm":
                  runOldJobAbility(entity, () -> Ability2OnKeyPressedProcedure.execute(world, x, y, z, entity));
                  break;
               case "Storm Burst":
                  runOldJobAbility(entity, () -> Ability3OnKeyPressedProcedure.execute(world, x, y, z, entity));
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean release(Entity entity, String skill, int pressedMs) {
      if (entity == null || skill == null || !isJobSkill(skill)) {
         return false;
      } else if ("Frozen Path".equals(skill)) {
         FrostMonarchManager.releaseFrozenPath(entity);
         return true;
      } else if (isSungSkill(skill)) {
         SungIlHwanCombatManager.release(entity, skill, pressedMs);
         return true;
      } else {
         return false;
      }
   }

   public static String cooldownKey(String skill) {
      return switch (skill) {
         case "Arise" -> "arise";
         case "Grand Marshal Authority" -> "grand_marshal_authority";
         case "Fire Charge", "Monarch Beam" -> "job_1";
         case "Meteor Rain", "Lightning Storm" -> "job_2";
         case "Fireflies", "Storm Burst" -> "job_3";
         case "Shadow Manifestation", "Spiritual Body Manifestation" -> "job_4";
         case "Destruction Claw" -> "antares_destruction_claw";
         case "Breath of Destruction" -> "antares_breath_of_destruction";
         case "Monarch's Descent" -> "antares_monarchs_descent";
         case "Sovereign Roar" -> "antares_sovereign_roar";
         case "Extinction" -> "antares_extinction";
         case "Monarch Manifestation" -> "antares_manifestation";
         case "Capture", "Power Smash", "Collapse", "Heavenly Counter", "Golden Dragon Dance", "Sovereign Sword Domain", "Dragon Sword Manifestation", "Predator's Presence", "Assassin Stance", "Spatial Execution", "Spiritualization", "Ice Spear", "Flash Freeze", "Frozen Path", "Frozen Architecture", "Frost Counter", "Absolute Zero", "Claw-Rift Passage", "Rubble Jaw", "King's Maul", "Feral Reconstitution", "White Fang Sovereign" -> skill;
         default -> skill;
      };
   }

   private static void castFireCharge(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (CooldownManager.isOnCooldown(entity, "job_1")) {
         if (entity instanceof Player player && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("Ability on cooldown!"), true);
         }
      } else if (entity.isShiftKeyDown()) {
         runOldJobAbility(entity, () -> Ability1OnKeyPressedProcedure.execute(world, x, y, z, entity));
      } else {
         runOldJobAbility(entity, () -> {
            Ability1OnKeyPressedProcedure.execute(world, x, y, z, entity);
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.firecharge = Math.max(45.0, capability.firecharge);
               capability.syncPlayerVariables(entity);
            });
            Ability1OnKeyReleasedProcedure.execute(world, x, y, z, entity);
         });
      }
   }

   private static void castMonarchBeam(Entity entity) {
      if (CooldownManager.isOnCooldown(entity, "job_1")) {
         if (entity instanceof Player player && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("Ability on cooldown!"), true);
         }
      } else {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.monarchbeam = true;
            capability.syncPlayerVariables(entity);
         });
         CooldownManager.set(entity, "job_1", 60);
         CooldownManager.set(entity, "mana_refresh", 60);
         SololevelingMod.queueServerWork(16, () -> entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.monarchbeam = false;
            capability.syncPlayerVariables(entity);
         }));
      }
   }

   private static void runOldJobAbility(Entity entity, Runnable action) {
      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         boolean oldCombatMode = capability.combatmode;
         capability.combatmode = false;
         action.run();
         capability.combatmode = oldCombatMode;
         capability.syncPlayerVariables(entity);
      });
   }

   private static List<String> skillsForJob(int job) {
      return switch (job) {
         case 1 -> List.of("Arise", "Shadow Summon", "Dismiss Shadows", "Shadow Command", "Shadow Exchange", "Shadow Manifestation", "Grand Marshal Authority");
         case 2 -> List.of("Fire Charge", "Meteor Rain", "Fireflies");
         case 3 -> FROST_SKILLS;
         case 4 -> List.of("Lightning Breath", "Hellstorm Dominion", "Radiru Blood Spear", "Doppelganger", "Hell's Army", "White Flame Spiritualization");
         case 5 -> List.of("Capture", "Power Smash", "Collapse", "Spiritual Body Manifestation");
         case 6 -> LIU_SKILLS;
         case 7 -> SUNG_SKILLS;
         default -> List.of();
         case 9 -> BEAST_SKILLS;
         case 10 -> ANTARES_SKILLS;
      };
   }

   private static List<String> skillsForEntityJob(Entity entity, int job) {
      if (job == 7 && !DeveloperModeManager.isEnabled(entity)) {
         return List.of();
      }

      ArrayList<String> skills = new ArrayList<>(VesselProgressionManager.unlockedSkills(entity, job));
      SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      if (job == 1 && vars.ShadowExchange && hasRunestoneSkill(entity, "slr_runestone_skill_shadow_exchange") && !skills.contains("Shadow Exchange")) {
         skills.add("Shadow Exchange");
      }

      if (job == 1 && vars.ShadowBody && hasRunestoneSkill(entity, "slr_runestone_skill_shadow_manifestation") && !skills.contains("Shadow Manifestation")) {
         skills.add("Shadow Manifestation");
      }

      boolean grandMarshalGranted = entity.level().isClientSide()
         ? parseSkillList(vars.Plist).contains("Grand Marshal Authority")
         : ShadowMonarchManager.hasAssignedGrandMarshal(entity);
      if (job == 1 && grandMarshalGranted && !skills.contains("Grand Marshal Authority")) {
         skills.add("Grand Marshal Authority");
      }

      return List.copyOf(skills);
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

   private static List<String> mergedJobSkillList(Entity entity, String plist, List<String> granted, boolean keepFormations) {
      LinkedHashSet<String> result = new LinkedHashSet<>();
      result.addAll(granted);

      for (String skill : parseSkillList(plist)) {
         if (!shouldRemoveJobOwnedSkill(skill, granted, keepFormations)) {
            result.add(skill);
         }
      }

      if (keepFormations && entity instanceof Player player) {
         result.addAll(ShadowMonarchManager.formationSkills(player));
      }

      return new ArrayList<>(result);
   }

   private static boolean clearStaleEquippedSkills(SololevelingModVariables.PlayerVariables vars, List<String> granted, boolean keepFormations) {
      boolean changed = false;

      for (int slot = 1; slot <= 16; slot++) {
         String skill = SkillSlotHelper.getSlot(vars, slot);
         if (shouldRemoveJobOwnedSkill(skill, granted, keepFormations)) {
            SkillSlotHelper.setSlot(vars, slot, "");
            changed = true;
         }
      }

      if (shouldRemoveJobOwnedSkill(vars.PselectedPower, granted, keepFormations)) {
         vars.PselectedPower = "";
         changed = true;
      }

      return changed;
   }

   private static boolean shouldRemoveJobOwnedSkill(String skill, List<String> granted, boolean keepFormations) {
      String cleaned = cleanSkill(skill);
      if (cleaned.isEmpty()) {
         return false;
      } else {
         return ALL_JOB_SKILLS.contains(cleaned) ? !granted.contains(cleaned) : ShadowMonarchManager.isFormationSkill(cleaned) && !keepFormations;
      }
   }

   private static List<String> parseSkillList(String plist) {
      return plist != null && !plist.isBlank()
         ? Arrays.stream(plist.split(",")).map(JobSkillManager::cleanSkill).filter(skill -> !skill.isEmpty()).toList()
         : List.of();
   }

   private static String cleanSkill(String item) {
      String skill = item == null ? "" : item.trim();
      if (skill.startsWith(".")) {
         skill = skill.substring(1);
      }

      return skill;
   }

   private static String writeSkillList(List<String> skills) {
      StringBuilder builder = new StringBuilder();

      for (String skill : skills) {
         if (skill != null && !skill.isBlank()) {
            builder.append(skill).append(",");
         }
      }

      return builder.toString();
   }
}
