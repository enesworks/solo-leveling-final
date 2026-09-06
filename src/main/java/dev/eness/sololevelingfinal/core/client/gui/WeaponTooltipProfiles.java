package dev.eness.sololevelingfinal.core.client.gui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.FORGE, value = Dist.CLIENT)
public final class WeaponTooltipProfiles {
   public static final int STEEL = 0;
   public static final int FROST = 1;
   public static final int FLAME = 2;
   public static final int VENOM = 3;
   public static final int VOID = 4;
   public static final int NATURE = 5;
   public static final int STORM = 6;
   public static final int DRAGON = 7;
   public static final int SPIRIT = 8;
   public static final int BLOOD = 9;
   public static final int ROYAL = 10;
   public static final int LUNAR = 11;
   public static final int TITAN = 12;
   public static final int AVARICE = 13;
   private static final int E = 0;
   private static final int D = 1;
   private static final int C = 2;
   private static final int B = 3;
   private static final int A = 4;
   private static final int S = 5;
   private static final int RELIC = 6;
   private static final int NATIONAL = 7;
   private static final int E_COLOR = 11120053;
   private static final int D_COLOR = 7391626;
   private static final int C_COLOR = 6674664;
   private static final int B_COLOR = 6129399;
   private static final int A_COLOR = 12283647;
   private static final int S_COLOR = 16763215;
   private static final Map<String, WeaponTooltipProfiles.Profile> PROFILES = new LinkedHashMap<>();

   private WeaponTooltipProfiles() {
   }

   private static void add(String id, WeaponTooltipProfiles.Profile profile) {
      PROFILES.put(id, profile);
   }

   private static WeaponTooltipProfiles.Profile p(
      String rank, int tier, String type, int theme, int primary, int secondary, float seed, int legacyLines, List<String> lore
   ) {
      return new WeaponTooltipProfiles.Profile(rank, tier, type, theme, primary, secondary, seed, legacyLines, lore, List.of());
   }

   private static WeaponTooltipProfiles.Profile p(
      String rank, int tier, String type, int theme, int primary, int secondary, float seed, int legacyLines, List<String> lore, List<String> traits
   ) {
      return new WeaponTooltipProfiles.Profile(rank, tier, type, theme, primary, secondary, seed, legacyLines, lore, traits);
   }

   private static List<String> l(String... lines) {
      return List.of(lines);
   }

   public static WeaponTooltipProfiles.Profile find(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      }

      ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
      return id != null && "sololeveling".equals(id.getNamespace()) ? PROFILES.get(id.getPath()) : null;
   }

   public static Map<String, WeaponTooltipProfiles.Profile> all() {
      return Map.copyOf(PROFILES);
   }

   @SubscribeEvent
   public static void rebuildTooltip(ItemTooltipEvent event) {
      WeaponTooltipProfiles.Profile profile = find(event.getItemStack());
      if (profile != null && !event.getToolTip().isEmpty()) {
         List<Component> tooltip = event.getToolTip();

         for (int i = 0; i < profile.legacyLines() && tooltip.size() > 1; i++) {
            tooltip.remove(1);
         }

         TextColor primary = TextColor.fromRgb(profile.primaryColor());
         TextColor secondary = TextColor.fromRgb(profile.secondaryColor());
         tooltip.set(0, tooltip.get(0).copy().withStyle(style -> style.withColor(primary).withBold(profile.tier() >= 5)));
         int insert = 1;
         tooltip.add(insert++, field("ITEM CLASS", profile.rank(), primary));
         tooltip.add(insert++, field("TYPE", profile.type(), primary));
         tooltip.add(insert++, Component.empty());
         tooltip.add(insert++, Component.literal("APPRAISAL").withStyle(Style.EMPTY.withColor(secondary).withBold(true)));

         for (String line : profile.lore()) {
            tooltip.add(insert++, Component.literal(line).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(13225430)).withItalic(true)));
         }

         if (!profile.traits().isEmpty()) {
            tooltip.add(insert++, Component.empty());

            for (int i = 0; i < profile.traits().size(); i += 2) {
               String name = profile.traits().get(i);
               String detail = i + 1 < profile.traits().size() ? profile.traits().get(i + 1) : "";
               tooltip.add(insert++, Component.literal("[" + name + "]").withStyle(Style.EMPTY.withColor(primary).withBold(true)));
               tooltip.add(insert++, Component.literal("  " + detail).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(11450310))));
            }
         }
      }
   }

   private static Component field(String label, String value, TextColor color) {
      MutableComponent line = Component.literal(label + "  ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(8358555)));
      line.append(Component.literal(value).withStyle(Style.EMPTY.withColor(color).withBold(true)));
      return line;
   }

   static {
      add(
         "e_tier_sword",
         p(
            "E",
            0,
            "Sword",
            0,
            11120053,
            4869720,
            1.0F,
            0,
            l("An entry-grade blade issued to newly awakened", "hunters. Dependable, inexpensive, and replaceable.")
         )
      );
      add(
         "d_tier_sword",
         p(
            "D",
            1,
            "Sword",
            0,
            7391626,
            2378549,
            1.2F,
            0,
            l("A reinforced field sword made for routine gates.", "Its edge can endure prolonged low-rank combat.")
         )
      );
      add(
         "c_tier_sword",
         p(
            "C",
            2,
            "Sword",
            0,
            6674664,
            2448744,
            1.4F,
            0,
            l("A mana-treated sword trusted by veteran raiders.", "The blade remains stable under repeated reinforcement.")
         )
      );
      add(
         "b_tier_sword",
         p(
            "B",
            3,
            "Sword",
            0,
            6129399,
            2373743,
            1.6F,
            0,
            l("A high-grade sword forged around a mana core.", "Its balance responds naturally to a hunter's strength.")
         )
      );
      add(
         "a_tier_sword",
         p(
            "A",
            4,
            "Sword",
            0,
            12283647,
            5451128,
            1.8F,
            0,
            l("A rare weapon reserved for elite strike teams.", "Dense mana sharpens the edge beyond ordinary steel.")
         )
      );
      add(
         "s_tier_sword",
         p(
            "S",
            5,
            "Sword",
            10,
            16763215,
            9063698,
            2.0F,
            0,
            l("A masterwork capable of surviving an S-rank gate.", "Only exceptional hunters can draw out its full force.")
         )
      );
      add(
         "sword_curved_d",
         p(
            "E",
            0,
            "Katana",
            0,
            11120053,
            4475218,
            2.2F,
            0,
            l("A light curved blade sold to novice hunters.", "Its forgiving balance rewards careful technique.")
         )
      );
      add(
         "sword_warrior_d",
         p(
            "D",
            1,
            "Katana",
            9,
            7391626,
            4796461,
            2.4F,
            0,
            l("A practical katana favored by close-range hunters.", "The thick spine withstands rough dungeon fighting.")
         )
      );
      add(
         "sword_twinwing_c",
         p(
            "C",
            2,
            "Katana",
            8,
            6674664,
            3239030,
            2.6F,
            0,
            l("Twin grooves guide mana along the cutting edge.", "Fast swings leave a faint wing-shaped afterimage.")
         )
      );
      add(
         "sword_nature_b",
         p(
            "B",
            3,
            "Katana",
            5,
            7262331,
            2644024,
            2.8F,
            0,
            l("A living-wood hilt steadies this enchanted blade.", "Ambient mana gathers around it like drifting leaves.")
         )
      );
      add(
         "sword_enriched_b",
         p(
            "A",
            4,
            "Katana",
            10,
            12283647,
            6702219,
            3.0F,
            0,
            l("A heavily enriched katana with exceptional density.", "Its compressed mana rewards precise, decisive cuts.")
         )
      );
      add(
         "katana_s",
         p(
            "S",
            5,
            "Katana",
            9,
            16763215,
            8068898,
            3.2F,
            0,
            l("An S-rank katana polished to a mirror finish.", "The blade carries killing intent without losing control.")
         )
      );
      add(
         "katana_stier",
         p(
            "S",
            6,
            "Katana",
            10,
            16766826,
            11025182,
            3.4F,
            4,
            l("A peerless katana forged by the Dwarf King's", "finest smiths. Its wounds refuse to close cleanly."),
            l("Bleed", "Successful strikes can inflict bleeding.")
         )
      );
      add(
         "dagger_karambit_e",
         p(
            "E",
            0,
            "Dagger",
            0,
            11120053,
            4540497,
            4.0F,
            0,
            l("A compact karambit for newly awakened assassins.", "Its hooked edge favors speed over stopping power.")
         )
      );
      add(
         "dagger_knight_d",
         p(
            "D",
            1,
            "Dagger",
            0,
            7391626,
            3164733,
            4.2F,
            0,
            l("A sturdy sidearm patterned after a knight's blade.", "Reliable when a larger weapon cannot be drawn.")
         )
      );
      add(
         "dagger_chain_c",
         p(
            "C",
            2,
            "Dagger",
            9,
            6674664,
            3362401,
            4.4F,
            0,
            l("Mana-conductive chainwork reinforces the grip.", "The weapon remains steady through rapid combinations.")
         )
      );
      add(
         "dagger_golden_b",
         p(
            "B",
            3,
            "Dagger",
            10,
            15776079,
            7688988,
            4.6F,
            0,
            l("A ceremonial dagger made combat-ready by enchantment.", "Golden channels distribute mana across the edge.")
         )
      );
      add(
         "dagger_duolity_a",
         p(
            "A",
            4,
            "Dagger",
            4,
            12283647,
            3156046,
            4.8F,
            0,
            l("A divided mana core holds two opposing currents.", "Its balance shifts between restraint and aggression.")
         )
      );
      add(
         "dagger_heat_a",
         p(
            "S",
            5,
            "Dagger",
            2,
            16751426,
            8003603,
            5.0F,
            0,
            l("An S-rank dagger with a furnace-hot mana channel.", "The edge glows brighter as combat grows more intense.")
         )
      );
      add(
         "hammer",
         p(
            "S",
            6,
            "War Hammer",
            12,
            14727018,
            5849644,
            5.5F,
            5,
            l("A giant's weapon reduced by dwarven craftsmanship.", "Its weight still rejects all but chosen wielders."),
            l("Titan Weight", "Delivers overwhelming impact through raw mass.")
         )
      );
      add(
         "war_axe",
         p(
            "S",
            6,
            "War Axe",
            12,
            15247447,
            8072996,
            5.8F,
            5,
            l("A giant-forged axe reshaped for human hands.", "The head retains the pressure of its original size."),
            l("Giant Cleaver", "Built to break guards with crushing force.")
         )
      );
      add(
         "ice_spear",
         p(
            "MONARCH",
            6,
            "Spear",
            1,
            11138303,
            3958456,
            6.1F,
            0,
            l("A spear condensed from the Frost Monarch's mana.", "Its point radiates a cold untouched by mortal flame."),
            l("Permafrost", "Carries sovereign frost through every strike.")
         )
      );
      add(
         "frost_blade",
         p(
            "S",
            6,
            "Sword",
            1,
            9300991,
            3234984,
            6.4F,
            0,
            l("A frozen blade formed around an unmelting core.", "Wounds bloom with ice before the steel leaves them."),
            l("Frostbite", "Strikes can burden targets with supernatural cold.")
         )
      );
      add(
         "demon_kings_long_sword",
         p(
            "S",
            6,
            "Longsword",
            2,
            16757051,
            7478056,
            6.7F,
            6,
            l("Baran's longsword, saturated with demonic lightning.", "Each swing calls the unrest of a burning storm."),
            l(
               "Storm of Flames",
               "Unleashes lightning and fire around its target.",
               "Demonic Attunement",
               "Draws on permanent Intelligence to strengthen the wielder while held."
            )
         )
      );
      add(
         "dragon_shortsword",
         p(
            "NATIONAL",
            7,
            "Shortsword",
            7,
            16765519,
            12988194,
            7.0F,
            6,
            l("A National Rank weapon carved from a sovereign", "dragon's fang. Its edge yields only to greater power."),
            l("Dragon Fang", "Scales with a wielder capable of mastering it.", "Twin Grip", "Can be wielded as part of a dagger pair.")
         )
      );
      add(
         "kamish_wrath",
         p(
            "UNMEASURABLE",
            6,
            "Dagger",
            7,
            16757548,
            10295066,
            7.3F,
            5,
            l("One of two daggers forged from Kamish's sharpest", "fangs. Its mana sensitivity borders on the unnatural."),
            l("Mana Sensitivity", "Each held fang combines permanent Strength and Intelligence into bonus Strength.")
         )
      );
      add(
         "kamish_wrath_2",
         p(
            "UNMEASURABLE",
            6,
            "Dagger",
            7,
            16742710,
            9245751,
            7.7F,
            5,
            l("The twin fang to Kamish's Wrath, carrying the same", "savage edge through an opposing mana current."),
            l("Mana Sensitivity", "Each held fang combines permanent Strength and Intelligence into bonus Strength.")
         )
      );
      add(
         "demon_kings_dagger",
         p(
            "S",
            6,
            "Dagger",
            2,
            16756277,
            7542568,
            8.0F,
            8,
            l("A dagger claimed from Baran, King of Demons.", "Its dormant flame answers when its twin is drawn."),
            l("Two as One", "Dual wielding draws on permanent Strength to grant a scaling Strength bonus.")
         )
      );
      add(
         "kasakas_venom_fangs",
         p(
            "C",
            2,
            "Dagger",
            3,
            5759869,
            1792831,
            8.3F,
            6,
            l("A dagger fashioned from Kasaka's venom fang.", "Residual poison still circulates through the weapon."),
            l("Paralyze", "Seizes targets up to C rank.", "Bleed", "The serrated fang leaves persistent wounds.")
         )
      );
      add(
         "kasakas_awakened_venom_fang",
         p(
            "S",
            6,
            "Dagger",
            3,
            9240418,
            5118582,
            8.6F,
            6,
            l("Kasaka's fang after its venom core awakened.", "The poison now behaves like a living predator."),
            l("Awakened Venom", "Paralysis reaches one rank higher, up to B rank.")
         )
      );
      add(
         "barukas_dagger",
         p(
            "A",
            4,
            "Dagger",
            1,
            10612735,
            4022175,
            8.9F,
            7,
            l("The ice-elf warlord Baruka carried this dagger.", "Weight-reducing magic makes the blade feel effortless."),
            l("Warlord's Agility", "The enchantment favors swift movement.")
         )
      );
      add(
         "knight_killer",
         p(
            "B",
            3,
            "Dagger",
            0,
            8038399,
            4014682,
            9.2F,
            4,
            l("A narrow dagger engineered to slip through armor.", "Its mana edge seeks seams in reinforced defenses."),
            l("Armor Breaker", "Deals 25% more damage to armored enemies.")
         )
      );
      add(
         "mythic_dagger",
         p(
            "S",
            6,
            "Dagger",
            4,
            14711039,
            4855654,
            9.5F,
            2,
            l("A spatial dagger whose edge exists a step aside", "from reality. Danger awakens its hidden movement."),
            l("Counter Shift", "Can evade a hit and move behind the attacker.")
         )
      );
      add(
         "gravity_dagger",
         p(
            "A",
            4,
            "Dagger",
            4,
            10713087,
            2693972,
            9.8F,
            0,
            l("A dagger built around a compressed gravity shard.", "Every wound briefly distorts the target's weight."),
            l("Gravity Well", "Strikes can disrupt and lift their target.")
         )
      );
      add(
         "emerald_dagger",
         p(
            "A",
            4,
            "Dagger",
            11,
            9371632,
            3561889,
            10.1F,
            4,
            l("A blade forged from crystallized moonlight.", "It bends light and strikes through false angles."),
            l("Moonlight Refraction", "Fast cuts leave deceptive afterimages.")
         )
      );
      add(
         "mana_gun",
         p(
            "A",
            4,
            "Mana Firearm",
            8,
            5693439,
            2640538,
            10.4F,
            0,
            l("A precision firearm that condenses mana into rounds.", "Output scales with the intelligence of its wielder."),
            l("Mana Chamber", "Fires without conventional ammunition.")
         )
      );
      add(
         "storm_griamore",
         p(
            "S",
            6,
            "Magic Grimoire",
            6,
            14284031,
            5460668,
            10.7F,
            6,
            l("A forbidden tome that commands wind and lightning.", "Each page turn draws the wielder into the storm."),
            l("Tempest Authority", "Draws on permanent Intelligence to strengthen the wielder while held.")
         )
      );
      add(
         "spirit_bow",
         p(
            "A",
            4,
            "Mana Bow",
            5,
            9306064,
            3235705,
            11.0F,
            2,
            l("An elven bow believed to originate beyond this realm.", "It shapes the wielder's mana into silent arrows."),
            l("Spirit Arrow", "Uses mana in place of physical ammunition.")
         )
      );
      add(
         "orb_of_avarice",
         p(
            "A",
            4,
            "Magic Focus",
            13,
            5626111,
            9312056,
            12.8F,
            8,
            l("A petrified sphere condensed from Vulcan's blood.", "It devours mana and returns it as overwhelming ruin."),
            l(
               "Desire for Destruction",
               "Doubles magic damage while held.",
               "Insatiable Price",
               "Mana costs are increased by 50%.",
               "Blue-Flame Dominion",
               "Fire magic burns blue under its influence.",
               "Avaricious Insight",
               "Draws on permanent Intelligence to strengthen the wielder while held."
            )
         )
      );
      add(
         "igrislongsword",
         p(
            "A",
            4,
            "Longsword",
            9,
            15226968,
            5511979,
            11.3F,
            0,
            l("A knight's longsword stained by unwavering loyalty.", "Its heavy edge was made for relentless duels.")
         )
      );
      add(
         "griamore",
         p(
            "S",
            6,
            "Magic Grimoire",
            2,
            16747320,
            8132388,
            11.6F,
            0,
            l("A grimoire whose pages never surrender their heat.", "Its script converts mana into consuming flame."),
            l("Inferno Script", "Amplifies fire magic cast by its holder.")
         )
      );
      add(
         "teleport_sai",
         p(
            "A",
            4,
            "Throwing Sai",
            4,
            11963647,
            2505336,
            11.9F,
            1,
            l("A demonic sai anchored to its wielder's mana.", "Throwing it opens a path through folded space."),
            l("Spatial Pursuit", "Teleports the user to the marked enemy.")
         )
      );
      add(
         "kangs_dagger",
         p(
            "B",
            3,
            "Dagger",
            9,
            15227769,
            5120309,
            12.2F,
            0,
            l("The dagger of an assassin who hunted other hunters.", "A cold residue of killing intent clings to the grip.")
         )
      );
   }

   public record Profile(
      String rank, int tier, String type, int theme, int primaryColor, int secondaryColor, float seed, int legacyLines, List<String> lore, List<String> traits
   ) {
   }
}
