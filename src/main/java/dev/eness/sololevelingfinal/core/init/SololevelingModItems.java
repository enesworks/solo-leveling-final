package dev.eness.sololevelingfinal.core.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import dev.eness.sololevelingfinal.core.block.display.DungeonWallDisplayItem;
import dev.eness.sololevelingfinal.core.block.display.HunterRankEvaluatorDisplayItem;
import dev.eness.sololevelingfinal.core.block.display.InstanceCoverDisplayItem;
import dev.eness.sololevelingfinal.core.block.display.InstanceDungeonKeyLoggerDisplayItem;
import dev.eness.sololevelingfinal.core.dungeon.builder.DungeonBuilderTool;
import dev.eness.sololevelingfinal.core.item.ATierSwordItem;
import dev.eness.sololevelingfinal.core.item.ArcaneMageRunestoneItem;
import dev.eness.sololevelingfinal.core.item.AssasinStarterpackItem;
import dev.eness.sololevelingfinal.core.item.AssassinMasteryItemItem;
import dev.eness.sololevelingfinal.core.item.BTierSwordItem;
import dev.eness.sololevelingfinal.core.item.BarrierMageRunestoneItem;
import dev.eness.sololevelingfinal.core.item.BarukasDaggerItem;
import dev.eness.sololevelingfinal.core.item.BluekeyItem;
import dev.eness.sololevelingfinal.core.item.CTierSwordItem;
import dev.eness.sololevelingfinal.core.item.ChoiCloakItem;
import dev.eness.sololevelingfinal.core.item.ClassChooserItem;
import dev.eness.sololevelingfinal.core.item.CoinItem100Item;
import dev.eness.sololevelingfinal.core.item.CoinItemItem;
import dev.eness.sololevelingfinal.core.item.CreativetabitemItem;
import dev.eness.sololevelingfinal.core.item.DKCLevelItemItem;
import dev.eness.sololevelingfinal.core.item.DKCTravelItem;
import dev.eness.sololevelingfinal.core.item.DTierSwordItem;
import dev.eness.sololevelingfinal.core.item.DaggerChainCItem;
import dev.eness.sololevelingfinal.core.item.DaggerDuolityAItem;
import dev.eness.sololevelingfinal.core.item.DaggerGoldenBItem;
import dev.eness.sololevelingfinal.core.item.DaggerHeatAItem;
import dev.eness.sololevelingfinal.core.item.DaggerKarambitEItem;
import dev.eness.sololevelingfinal.core.item.DaggerKnightDItem;
import dev.eness.sololevelingfinal.core.item.DatapackGateSpawnEggItem;
import dev.eness.sololevelingfinal.core.item.DemonKingsDaggerItem;
import dev.eness.sololevelingfinal.core.item.DemonKingsLongSwordItem;
import dev.eness.sololevelingfinal.core.item.DragonShortswordItem;
import dev.eness.sololevelingfinal.core.item.Dun1Item;
import dev.eness.sololevelingfinal.core.item.DungeonBuilderWandItem;
import dev.eness.sololevelingfinal.core.item.DungeonNostalgiaItem;
import dev.eness.sololevelingfinal.core.item.DungeonSpawnerItem;
import dev.eness.sololevelingfinal.core.item.ETierSwordItem;
import dev.eness.sololevelingfinal.core.item.EmeraldDaggerItem;
import dev.eness.sololevelingfinal.core.item.EntryPermitItem;
import dev.eness.sololevelingfinal.core.item.FighterMasteryItemItem;
import dev.eness.sololevelingfinal.core.item.FighterStarterpackItem;
import dev.eness.sololevelingfinal.core.item.FireMageRunestoneItem;
import dev.eness.sololevelingfinal.core.item.FrostBladeItem;
import dev.eness.sololevelingfinal.core.item.GgItem;
import dev.eness.sololevelingfinal.core.item.GiveBeruItem;
import dev.eness.sololevelingfinal.core.item.GiveIgrisItem;
import dev.eness.sololevelingfinal.core.item.GiveKamishItem;
import dev.eness.sololevelingfinal.core.item.GiveTuskItem;
import dev.eness.sololevelingfinal.core.item.GoliathARMORItem;
import dev.eness.sololevelingfinal.core.item.GrandMageItem;
import dev.eness.sololevelingfinal.core.item.GravityDaggerItem;
import dev.eness.sololevelingfinal.core.item.GriamoreItem;
import dev.eness.sololevelingfinal.core.item.HammerItem;
import dev.eness.sololevelingfinal.core.item.HealerMasteryItemItem;
import dev.eness.sololevelingfinal.core.item.HealerStarterpackItem;
import dev.eness.sololevelingfinal.core.item.HolyWaterOfLifeItem;
import dev.eness.sololevelingfinal.core.item.HunterIDItem;
import dev.eness.sololevelingfinal.core.item.IceSpearItem;
import dev.eness.sololevelingfinal.core.item.IgrislongswordItem;
import dev.eness.sololevelingfinal.core.item.InstanceDungeonKeyItem;
import dev.eness.sololevelingfinal.core.item.JobChangeDebugItem;
import dev.eness.sololevelingfinal.core.item.JobKeyItem;
import dev.eness.sololevelingfinal.core.item.KamishToothItem;
import dev.eness.sololevelingfinal.core.item.KamishWrath2Item;
import dev.eness.sololevelingfinal.core.item.KamishWrathItem;
import dev.eness.sololevelingfinal.core.item.KangHairItem;
import dev.eness.sololevelingfinal.core.item.KangsDaggerItem;
import dev.eness.sololevelingfinal.core.item.KasakasAwakenedVenomFangItem;
import dev.eness.sololevelingfinal.core.item.KasakasVenomFangsItem;
import dev.eness.sololevelingfinal.core.item.KatanaSItem;
import dev.eness.sololevelingfinal.core.item.KatanaStierItem;
import dev.eness.sololevelingfinal.core.item.KnightKillerItem;
import dev.eness.sololevelingfinal.core.item.LargeFatiguePotionItem;
import dev.eness.sololevelingfinal.core.item.LargeHealthPotionItem;
import dev.eness.sololevelingfinal.core.item.LargeManaPotionItem;
import dev.eness.sololevelingfinal.core.item.LevelItemItem;
import dev.eness.sololevelingfinal.core.item.MageMasteryItemItem;
import dev.eness.sololevelingfinal.core.item.MageStarterpackItem;
import dev.eness.sololevelingfinal.core.item.MagicReaderItem;
import dev.eness.sololevelingfinal.core.item.ManaCrystalAItem;
import dev.eness.sololevelingfinal.core.item.ManaCrystalBItem;
import dev.eness.sololevelingfinal.core.item.ManaCrystalCItem;
import dev.eness.sololevelingfinal.core.item.ManaCrystalDItem;
import dev.eness.sololevelingfinal.core.item.ManaCrystalEItem;
import dev.eness.sololevelingfinal.core.item.ManaCrystalSItem;
import dev.eness.sololevelingfinal.core.item.ManaGunItem;
import dev.eness.sololevelingfinal.core.item.MediumFatiguePotionItem;
import dev.eness.sololevelingfinal.core.item.MediumHealthPotionItem;
import dev.eness.sololevelingfinal.core.item.MediumManaPotionItem;
import dev.eness.sololevelingfinal.core.item.MonarchTerritoryGateSpawnEggItem;
import dev.eness.sololevelingfinal.core.item.MurderiousIntentStoneItem;
import dev.eness.sololevelingfinal.core.item.MythicDaggerItem;
import dev.eness.sololevelingfinal.core.item.OrbOfAvariceItem;
import dev.eness.sololevelingfinal.core.item.PurifiedBloodOfTheDemonKingItem;
import dev.eness.sololevelingfinal.core.item.RandomSpecialBoxItem;
import dev.eness.sololevelingfinal.core.item.RangerMasteryItemItem;
import dev.eness.sololevelingfinal.core.item.RangerStarterpackItem;
import dev.eness.sololevelingfinal.core.item.RedkeyItem;
import dev.eness.sololevelingfinal.core.item.RetiredMageRunestoneItem;
import dev.eness.sololevelingfinal.core.item.RotationDeviceItem;
import dev.eness.sololevelingfinal.core.item.RunestoneBackstabItem;
import dev.eness.sololevelingfinal.core.item.RunestoneBackstepItem;
import dev.eness.sololevelingfinal.core.item.RunestoneBlessingMarkItem;
import dev.eness.sololevelingfinal.core.item.RunestoneColdBloodItem;
import dev.eness.sololevelingfinal.core.item.RunestoneCriticalAttackItem;
import dev.eness.sololevelingfinal.core.item.RunestoneCriticalstrikeItem;
import dev.eness.sololevelingfinal.core.item.RunestoneDaggerRushItem;
import dev.eness.sololevelingfinal.core.item.RunestoneDaggerThrowItem;
import dev.eness.sololevelingfinal.core.item.RunestoneDetectionItem;
import dev.eness.sololevelingfinal.core.item.RunestoneDualwieldItem;
import dev.eness.sololevelingfinal.core.item.RunestoneHasteItem;
import dev.eness.sololevelingfinal.core.item.RunestoneHawkeyeItem;
import dev.eness.sololevelingfinal.core.item.RunestoneHealBeamItem;
import dev.eness.sololevelingfinal.core.item.RunestoneHighValueTargetItem;
import dev.eness.sololevelingfinal.core.item.RunestoneHyperfocusItem;
import dev.eness.sololevelingfinal.core.item.RunestoneManaQuiverItem;
import dev.eness.sololevelingfinal.core.item.RunestoneMonarchsDomainItem;
import dev.eness.sololevelingfinal.core.item.RunestoneMutilationItem;
import dev.eness.sololevelingfinal.core.item.RunestoneOverhealItem;
import dev.eness.sololevelingfinal.core.item.RunestonePhysicalItem;
import dev.eness.sololevelingfinal.core.item.RunestoneProtectionMarkItem;
import dev.eness.sololevelingfinal.core.item.RunestoneProximityTrapItem;
import dev.eness.sololevelingfinal.core.item.RunestonePurificationItem;
import dev.eness.sololevelingfinal.core.item.RunestoneQuickslashesItem;
import dev.eness.sololevelingfinal.core.item.RunestoneReinforcementItem;
import dev.eness.sololevelingfinal.core.item.RunestoneShadowExchangeItem;
import dev.eness.sololevelingfinal.core.item.RunestoneShadowSpiritualBodyManifestationItem;
import dev.eness.sololevelingfinal.core.item.RunestoneShadowstepItem;
import dev.eness.sololevelingfinal.core.item.RunestoneSharpshooterItem;
import dev.eness.sololevelingfinal.core.item.RunestoneShieldBashItem;
import dev.eness.sololevelingfinal.core.item.RunestoneSlamItem;
import dev.eness.sololevelingfinal.core.item.RunestoneSlashFuryItem;
import dev.eness.sololevelingfinal.core.item.RunestoneSlashdashItem;
import dev.eness.sololevelingfinal.core.item.RunestoneSwordBeamItem;
import dev.eness.sololevelingfinal.core.item.RunestoneSwordDanceItem;
import dev.eness.sololevelingfinal.core.item.RunestoneSwordofLightItem;
import dev.eness.sololevelingfinal.core.item.RunestoneTankLeapItem;
import dev.eness.sololevelingfinal.core.item.RunestoneTauntItem;
import dev.eness.sololevelingfinal.core.item.RunestoneWillpowerItem;
import dev.eness.sololevelingfinal.core.item.STierSwordItem;
import dev.eness.sololevelingfinal.core.item.SelectionSpecialBoxItem;
import dev.eness.sololevelingfinal.core.item.ShadowARMORItem;
import dev.eness.sololevelingfinal.core.item.ShadowMonarchItem;
import dev.eness.sololevelingfinal.core.item.SmallFatiguePotionItem;
import dev.eness.sololevelingfinal.core.item.SmallHealthPotionItem;
import dev.eness.sololevelingfinal.core.item.SmallManaPotionItem;
import dev.eness.sololevelingfinal.core.item.SpiritBowItem;
import dev.eness.sololevelingfinal.core.item.SpringWaterOfTheEchoingForestItem;
import dev.eness.sololevelingfinal.core.item.StealthStoneItem;
import dev.eness.sololevelingfinal.core.item.StormGriamoreItem;
import dev.eness.sololevelingfinal.core.item.StormMageRunestoneItem;
import dev.eness.sololevelingfinal.core.item.SungJinWooDrip2Item;
import dev.eness.sololevelingfinal.core.item.SungJinWooDripItem;
import dev.eness.sololevelingfinal.core.item.SwordCurvedDItem;
import dev.eness.sololevelingfinal.core.item.SwordEnrichedBItem;
import dev.eness.sololevelingfinal.core.item.SwordNatureBItem;
import dev.eness.sololevelingfinal.core.item.SwordTwinwingCItem;
import dev.eness.sololevelingfinal.core.item.SwordWarriorDItem;
import dev.eness.sololevelingfinal.core.item.TankerMasteryItemItem;
import dev.eness.sololevelingfinal.core.item.TankerStarterpackItem;
import dev.eness.sololevelingfinal.core.item.TelekinesisStoneItem;
import dev.eness.sololevelingfinal.core.item.TeleportSaiItem;
import dev.eness.sololevelingfinal.core.item.TestItem;
import dev.eness.sololevelingfinal.core.item.TestParticlesItem;
import dev.eness.sololevelingfinal.core.item.WarAxeItem;
import dev.eness.sololevelingfinal.core.item.WorldTreesFragmentItem;
import dev.eness.sololevelingfinal.core.item.YellowkeyItem;
import dev.eness.sololevelingfinal.core.world.dimension.rift.RiftTerritory;

public class SololevelingModItems {
   public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, "sololeveling");
   public static final RegistryObject<Item> HAMMER = REGISTRY.register("hammer", () -> new HammerItem());
   public static final RegistryObject<Item> WAR_AXE = REGISTRY.register("war_axe", () -> new WarAxeItem());
   public static final RegistryObject<Item> ICE_SPEAR = REGISTRY.register("ice_spear", () -> new IceSpearItem());
   public static final RegistryObject<Item> RADIRU_BLOOD_SPEAR = REGISTRY.register(
      "radiru_blood_spear", () -> new Item(new Properties().stacksTo(1).fireResistant())
   );
   public static final RegistryObject<Item> DRAGON_SHORTSWORD = REGISTRY.register("dragon_shortsword", DragonShortswordItem::new);
   public static final RegistryObject<Item> DEMON_KINGS_LONG_SWORD = REGISTRY.register("demon_kings_long_sword", () -> new DemonKingsLongSwordItem());
   public static final RegistryObject<Item> FROST_BLADE = REGISTRY.register("frost_blade", () -> new FrostBladeItem());
   public static final RegistryObject<Item> S_TIER_SWORD = REGISTRY.register("s_tier_sword", () -> new STierSwordItem());
   public static final RegistryObject<Item> A_TIER_SWORD = REGISTRY.register("a_tier_sword", () -> new ATierSwordItem());
   public static final RegistryObject<Item> B_TIER_SWORD = REGISTRY.register("b_tier_sword", () -> new BTierSwordItem());
   public static final RegistryObject<Item> C_TIER_SWORD = REGISTRY.register("c_tier_sword", () -> new CTierSwordItem());
   public static final RegistryObject<Item> D_TIER_SWORD = REGISTRY.register("d_tier_sword", () -> new DTierSwordItem());
   public static final RegistryObject<Item> E_TIER_SWORD = REGISTRY.register("e_tier_sword", () -> new ETierSwordItem());
   public static final RegistryObject<Item> KATANA_STIER = REGISTRY.register("katana_stier", () -> new KatanaStierItem());
   public static final RegistryObject<Item> KATANA_S = REGISTRY.register("katana_s", () -> new KatanaSItem());
   public static final RegistryObject<Item> SWORD_ENRICHED_B = REGISTRY.register("sword_enriched_b", () -> new SwordEnrichedBItem());
   public static final RegistryObject<Item> SWORD_NATURE_B = REGISTRY.register("sword_nature_b", () -> new SwordNatureBItem());
   public static final RegistryObject<Item> SWORD_TWINWING_C = REGISTRY.register("sword_twinwing_c", () -> new SwordTwinwingCItem());
   public static final RegistryObject<Item> SWORD_WARRIOR_D = REGISTRY.register("sword_warrior_d", () -> new SwordWarriorDItem());
   public static final RegistryObject<Item> SWORD_CURVED_D = REGISTRY.register("sword_curved_d", () -> new SwordCurvedDItem());
   public static final RegistryObject<Item> KAMISH_WRATH_2 = REGISTRY.register("kamish_wrath_2", () -> new KamishWrath2Item());
   public static final RegistryObject<Item> KAMISH_WRATH = REGISTRY.register("kamish_wrath", () -> new KamishWrathItem());
   public static final RegistryObject<Item> DEMON_KINGS_DAGGER = REGISTRY.register("demon_kings_dagger", () -> new DemonKingsDaggerItem());
   public static final RegistryObject<Item> KASAKAS_AWAKENED_VENOM_FANG = REGISTRY.register(
      "kasakas_awakened_venom_fang", () -> new KasakasAwakenedVenomFangItem()
   );
   public static final RegistryObject<Item> BARUKAS_DAGGER = REGISTRY.register("barukas_dagger", () -> new BarukasDaggerItem());
   public static final RegistryObject<Item> KNIGHT_KILLER = REGISTRY.register("knight_killer", () -> new KnightKillerItem());
   public static final RegistryObject<Item> MYTHIC_DAGGER = REGISTRY.register("mythic_dagger", () -> new MythicDaggerItem());
   public static final RegistryObject<Item> KASAKAS_VENOM_FANGS = REGISTRY.register("kasakas_venom_fangs", () -> new KasakasVenomFangsItem());
   public static final RegistryObject<Item> GRAVITY_DAGGER = REGISTRY.register("gravity_dagger", () -> new GravityDaggerItem());
   public static final RegistryObject<Item> EMERALD_DAGGER = REGISTRY.register("emerald_dagger", () -> new EmeraldDaggerItem());
   public static final RegistryObject<Item> DAGGER_HEAT_A = REGISTRY.register("dagger_heat_a", () -> new DaggerHeatAItem());
   public static final RegistryObject<Item> DAGGER_DUOLITY_A = REGISTRY.register("dagger_duolity_a", () -> new DaggerDuolityAItem());
   public static final RegistryObject<Item> DAGGER_GOLDEN_B = REGISTRY.register("dagger_golden_b", () -> new DaggerGoldenBItem());
   public static final RegistryObject<Item> DAGGER_CHAIN_C = REGISTRY.register("dagger_chain_c", () -> new DaggerChainCItem());
   public static final RegistryObject<Item> DAGGER_KNIGHT_D = REGISTRY.register("dagger_knight_d", () -> new DaggerKnightDItem());
   public static final RegistryObject<Item> DAGGER_KARAMBIT_E = REGISTRY.register("dagger_karambit_e", () -> new DaggerKarambitEItem());
   public static final RegistryObject<Item> MANA_GUN = REGISTRY.register("mana_gun", () -> new ManaGunItem());
   public static final RegistryObject<Item> STORM_GRIAMORE = REGISTRY.register("storm_griamore", () -> new StormGriamoreItem());
   public static final RegistryObject<Item> SPIRIT_BOW = REGISTRY.register("spirit_bow", () -> new SpiritBowItem());
   public static final RegistryObject<Item> SUNG_JIN_WOO_SPAWN_EGG = REGISTRY.register(
      "sung_jin_woo_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.SUNG_JIN_WOO, -1, -1, new Properties())
   );
   public static final RegistryObject<Item> ORC_SPAWN_EGG = REGISTRY.register(
      "orc_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.GREEN_ORC, -16764109, -16751104, new Properties())
   );
   public static final RegistryObject<Item> GEM_GOLEM_SPAWN_EGG = REGISTRY.register(
      "gem_golem_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.GEM_GOLEM, -16777114, -39169, new Properties())
   );
   public static final RegistryObject<Item> BERU_BOSS_SPAWN_EGG = REGISTRY.register(
      "beru_boss_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.BERU_BOSS, -13421773, -26215, new Properties())
   );
   public static final RegistryObject<Item> CENTIPEDE_SPAWN_EGG = REGISTRY.register(
      "centipede_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.CENTIPEDE, -6737152, -26368, new Properties())
   );
   public static final RegistryObject<Item> D_KNIGHT_1_SPAWN_EGG = REGISTRY.register(
      "d_knight_1_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.D_KNIGHT_1, -16777216, -16777114, new Properties())
   );
   public static final RegistryObject<Item> D_KNIGHT_2_SPAWN_EGG = REGISTRY.register(
      "d_knight_2_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.D_KNIGHT_2, -16777216, -10079488, new Properties())
   );
   public static final RegistryObject<Item> D_KNIGHT_3_SPAWN_EGG = REGISTRY.register(
      "d_knight_3_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.D_KNIGHT_3, -10066330, -26113, new Properties())
   );
   public static final RegistryObject<Item> MINI_GEM_GOLEM_SPAWN_EGG = REGISTRY.register(
      "mini_gem_golem_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.MINI_GEM_GOLEM, -10066330, -10092442, new Properties())
   );
   public static final RegistryObject<Item> STEEL_FANG_WOLF_SPAWN_EGG = REGISTRY.register(
      "steel_fang_wolf_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.STEEL_FANG_WOLF, -13421773, -10092544, new Properties())
   );
   public static final RegistryObject<Item> ANCIENT_SAMURAI_SPAWN_EGG = REGISTRY.register(
      "ancient_samurai_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.ANCIENT_SAMURAI, -13421773, -26368, new Properties())
   );
   public static final RegistryObject<Item> STONE_GOLEM_SPAWN_EGG = REGISTRY.register(
      "stone_golem_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.STONE_GOLEM, -10066330, -3355444, new Properties())
   );
   public static final RegistryObject<Item> SPIDER_BOSS_SPAWN_EGG = REGISTRY.register(
      "spider_boss_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.SPIDER_BOSS, -16777165, -10079488, new Properties())
   );
   public static final RegistryObject<Item> FIRE_FLY_SPAWN_EGG = REGISTRY.register(
      "fire_fly_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.FIRE_FLY, -26266, -103, new Properties())
   );
   public static final RegistryObject<Item> POLAR_BEAR_SPAWN_EGG = REGISTRY.register(
      "polar_bear_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.POLAR_BEAR, -3342337, -1, new Properties())
   );
   public static final RegistryObject<Item> ICE_ELF_SPAWN_EGG = REGISTRY.register(
      "ice_elf_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.ICE_ELF, -16777063, -16777216, new Properties())
   );
   public static final RegistryObject<Item> BARUKA_SPAWN_EGG = REGISTRY.register(
      "baruka_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.BARUKA, -13434880, -3342337, new Properties())
   );
   public static final RegistryObject<Item> CHOIJONG_SPAWN_EGG = REGISTRY.register(
      "choijong_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.CHOIJONG, -6750208, -65434, new Properties())
   );
   public static final RegistryObject<Item> BAEK_YOONHO_SPAWN_EGG = REGISTRY.register(
      "baek_yoonho_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.BAEK_YOONHO, -1, -26266, new Properties())
   );
   public static final RegistryObject<Item> GOBLIN_KING_SPAWN_EGG = REGISTRY.register(
      "goblin_king_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.GOBLIN_KING, -16738048, -39322, new Properties())
   );
   public static final RegistryObject<Item> STATUE_OF_GOD_SPAWN_EGG = REGISTRY.register(
      "statue_of_god_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.STATUE_OF_GOD, -12960668, -16740609, new Properties())
   );
   public static final RegistryObject<Item> KANG_TAESHIK_SPAWN_EGG = REGISTRY.register(
      "kang_taeshik_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.KANG_TAESHIK, -16773324, -1, new Properties())
   );
   public static final RegistryObject<Item> RED_ANTS_SPAWN_EGG = REGISTRY.register(
      "red_ants_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.RED_ANTS, -10944512, -14191873, new Properties())
   );
   public static final RegistryObject<Item> THOMAS_ANDRE_SPAWN_EGG = REGISTRY.register(
      "thomas_andre_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.THOMAS_ANDRE, -26317, -10066330, new Properties())
   );
   public static final RegistryObject<Item> FANGED_KASAKA_SPAWN_EGG = REGISTRY.register(
      "fanged_kasaka_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.FANGED_KASAKA, -16777114, -13395712, new Properties())
   );
   public static final RegistryObject<Item> STATUEAXE_SPAWN_EGG = REGISTRY.register(
      "statueaxe_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.STATUEAXE, -10066330, -10066177, new Properties())
   );
   public static final RegistryObject<Item> STATUEHAMMER_SPAWN_EGG = REGISTRY.register(
      "statuehammer_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.STATUEHAMMER, -10066330, -10066177, new Properties())
   );
   public static final RegistryObject<Item> STATUESWORD_SPAWN_EGG = REGISTRY.register(
      "statuesword_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.STATUESWORD, -10066330, -10066177, new Properties())
   );
   public static final RegistryObject<Item> FUTURISTIC_GOLEM_SPAWN_EGG = REGISTRY.register(
      "futuristic_golem_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.FUTURISTIC_GOLEM, -16764007, -16751104, new Properties())
   );
   public static final RegistryObject<Item> MUTATED_SPAWN_EGG = REGISTRY.register(
      "mutated_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.MUTATED, -65281, -16751104, new Properties())
   );
   public static final RegistryObject<Item> BLOOD_RED_COM_IGRIS_SPAWN_EGG = REGISTRY.register(
      "blood_red_com_igris_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.BLOOD_RED_COM_IGRIS, -10092544, -52429, new Properties())
   );
   public static final RegistryObject<Item> ANCIENT_GOLEM_SPAWN_EGG = REGISTRY.register(
      "ancient_golem_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.ANCIENT_GOLEM, -10607104, -16727297, new Properties())
   );
   public static final RegistryObject<Item> HUNTER_SPAWN_EGG = REGISTRY.register(
      "hunter_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.HUNTER, -6684775, -256, new Properties())
   );
   public static final RegistryObject<Item> CHA_HAE_IN_SPAWN_EGG = REGISTRY.register(
      "cha_hae_in_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.CHA_HAE_IN, -13261, -65281, new Properties())
   );
   public static final RegistryObject<Item> KARGALGAN_SPAWN_EGG = REGISTRY.register(
      "kargalgan_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.KARGALGAN, -13421773, -39424, new Properties())
   );
   public static final RegistryObject<Item> SKELETON_WARRIOR_SPAWN_EGG = REGISTRY.register(
      "skeleton_warrior_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.SKELETON_WARRIOR, -6710887, -12046336, new Properties())
   );
   public static final RegistryObject<Item> SKELETON_BRUTE_SPAWN_EGG = REGISTRY.register(
      "skeleton_brute_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.SKELETON_BRUTE, -6710887, -12046336, new Properties())
   );
   public static final RegistryObject<Item> KAMISH_SPAWN_EGG = REGISTRY.register(
      "kamish_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.KAMISH, -6750208, -13312, new Properties())
   );
   public static final RegistryObject<Item> STEEL_FANGED_LYCAN_SPAWN_EGG = REGISTRY.register(
      "steel_fanged_lycan_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.STEEL_FANGED_LYCAN, -39373, -1, new Properties())
   );
   public static final RegistryObject<Item> DUMMY_PORTAL_NORMAL_SPAWN_EGG = REGISTRY.register(
      "dummy_portal_normal_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.DUMMY_PORTAL_NORMAL, -16763905, -16777216, new Properties())
   );
   public static final RegistryObject<Item> DUMMY_PORTAL_RED_SPAWN_EGG = REGISTRY.register(
      "dummy_portal_red_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.DUMMY_PORTAL_RED, -6750208, -16777216, new Properties())
   );
   public static final RegistryObject<Item> DUMMY_PORTAL_PURPLE_SPAWN_EGG = REGISTRY.register(
      "dummy_portal_purple_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.DUMMY_PORTAL_PURPLE, -6750055, -16777216, new Properties())
   );
   public static final RegistryObject<Item> HUNTER_ID = REGISTRY.register("hunter_id", () -> new HunterIDItem());
   public static final RegistryObject<Item> JOB_KEY = REGISTRY.register("job_key", () -> new JobKeyItem());
   public static final RegistryObject<Item> INSTANCE_DUNGEON_KEY = REGISTRY.register("instance_dungeon_key", () -> new InstanceDungeonKeyItem());
   public static final RegistryObject<Item> PORTAL_BERU_SPAWN_EGG = REGISTRY.register(
      "portal_beru_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_BERU, -34304, -6400, new Properties())
   );
   public static final RegistryObject<Item> MONARCH_TERRITORY_DESTRUCTION_GATE_SPAWN_EGG = REGISTRY.register(
      "monarch_territory_destruction_gate_spawn_egg", () -> new MonarchTerritoryGateSpawnEggItem(RiftTerritory.DESTRUCTION, -9699050, -331776)
   );
   public static final RegistryObject<Item> MONARCH_TERRITORY_FROST_GATE_SPAWN_EGG = REGISTRY.register(
      "monarch_territory_frost_gate_spawn_egg", () -> new MonarchTerritoryGateSpawnEggItem(RiftTerritory.FROST, -10092544, -3407770)
   );
   public static final RegistryObject<Item> MONARCH_TERRITORY_FANGS_GATE_SPAWN_EGG = REGISTRY.register(
      "monarch_territory_fangs_gate_spawn_egg", () -> new MonarchTerritoryGateSpawnEggItem(RiftTerritory.FANGS, -13421773, -43776)
   );
   public static final RegistryObject<Item> MONARCH_TERRITORY_PLAGUES_GATE_SPAWN_EGG = REGISTRY.register(
      "monarch_territory_plagues_gate_spawn_egg", () -> new MonarchTerritoryGateSpawnEggItem(RiftTerritory.PLAGUES, -10066330, -11730944)
   );
   public static final RegistryObject<Item> MONARCH_TERRITORY_IRON_BODY_GATE_SPAWN_EGG = REGISTRY.register(
      "monarch_territory_iron_body_gate_spawn_egg", () -> new MonarchTerritoryGateSpawnEggItem(RiftTerritory.IRON_BODY, -6710887, -263173)
   );
   public static final RegistryObject<Item> MONARCH_TERRITORY_WHITE_FLAMES_GATE_SPAWN_EGG = REGISTRY.register(
      "monarch_territory_white_flames_gate_spawn_egg", () -> new MonarchTerritoryGateSpawnEggItem(RiftTerritory.WHITE_FLAMES, -1, -26317)
   );
   public static final RegistryObject<Item> MONARCH_TERRITORY_TRANSFIGURATION_GATE_SPAWN_EGG = REGISTRY.register(
      "monarch_territory_transfiguration_gate_spawn_egg", () -> new MonarchTerritoryGateSpawnEggItem(RiftTerritory.TRANSFIGURATION, -13421569, -39271)
   );
   public static final RegistryObject<Item> MONARCH_TERRITORY_BEGINNING_GATE_SPAWN_EGG = REGISTRY.register(
      "monarch_territory_beginning_gate_spawn_egg", () -> new MonarchTerritoryGateSpawnEggItem(RiftTerritory.BEGINNING, -16777216, -5592406)
   );
   public static final RegistryObject<Item> PORTAL_1_SPAWN_EGG = REGISTRY.register(
      "portal_1_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_1, -35072, -16711681, new Properties())
   );
   public static final RegistryObject<Item> DATAPACK_GATE_SPAWN_EGG = REGISTRY.register(
      "datapack_gate_spawn_egg", () -> new DatapackGateSpawnEggItem(-6750055, -10027009)
   );
   public static final RegistryObject<Item> PORTAL_LUSH_SPAWN_EGG = REGISTRY.register(
      "portal_lush_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_LUSH, -35072, -65536, new Properties())
   );
   public static final RegistryObject<Item> PORTAL_KARGALGANS_THRONE_ROOM_SPAWN_EGG = REGISTRY.register(
      "portal_kargalgans_throne_room_spawn_egg",
      () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_KARGALGANS_THRONE_ROOM, -6684673, -10066330, new Properties())
   );
   public static final RegistryObject<Item> RANDOM_CAVE_LARGE_SPAWN_EGG = REGISTRY.register(
      "random_cave_large_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.RANDOM_CAVE_LARGE, -35072, -1, new Properties())
   );
   public static final RegistryObject<Item> PORTAL_ANCIENT_GOLEM_SPAWN_EGG = REGISTRY.register(
      "portal_ancient_golem_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_ANCIENT_GOLEM, -6684673, -10066330, new Properties())
   );
   public static final RegistryObject<Item> PORTAL_SPAWN_EGG = REGISTRY.register(
      "portal_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL, -35072, -10027009, new Properties())
   );
   public static final RegistryObject<Item> SPAWNER_PORTAL_SPAWN_EGG = REGISTRY.register(
      "spawner_portal_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.SPAWNER_PORTAL, -6710887, -13057, new Properties())
   );
   public static final RegistryObject<Item> PORTAL_12_SPAWN_EGG = REGISTRY.register(
      "portal_12_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_12, -16777216, -16777216, new Properties())
   );
   public static final RegistryObject<Item> PORTAL_SEWERS_SPAWN_EGG = REGISTRY.register(
      "portal_sewers_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_SEWERS, -35072, -16711681, new Properties())
   );
   public static final RegistryObject<Item> PORTAL_LAB_SPAWN_EGG = REGISTRY.register(
      "portal_lab_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_LAB, -16738048, -10092442, new Properties())
   );
   public static final RegistryObject<Item> PORTAL_CEMETERY_SPAWN_EGG = REGISTRY.register(
      "portal_cemetery_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.PORTAL_CEMETERY, -6684673, -10066330, new Properties())
   );
   public static final RegistryObject<Item> DUNGEON_BLOCK = block(SololevelingModBlocks.DUNGEON_BLOCK);
   public static final RegistryObject<Item> DUNGEON_BLOCK_2 = block(SololevelingModBlocks.DUNGEON_BLOCK_2);
   public static final RegistryObject<Item> TRAINING_BOT_SPAWN_EGG = REGISTRY.register(
      "training_bot_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.TRAINING_BOT, -1, -1, new Properties())
   );
   public static final RegistryObject<Item> AFTER_IMAGE_SPAWN_EGG = REGISTRY.register(
      "after_image_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.AFTER_IMAGE, -1, -1, new Properties())
   );
   public static final RegistryObject<Item> SECRETARY_SPAWN_EGG = REGISTRY.register(
      "secretary_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.SECRETARY, -1, -1, new Properties())
   );
   public static final RegistryObject<Item> ELDER_BEAST_SPAWN_EGG = REGISTRY.register(
      "elder_beast_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.ELDER_BEAST, -1, -1, new Properties())
   );
   public static final RegistryObject<Item> GOBLIN_CLUB_SPAWN_EGG = REGISTRY.register(
      "goblin_club_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.GOBLIN_CLUB, -16751104, -9425152, new Properties())
   );
   public static final RegistryObject<Item> GOBLIN_ARCHER_SPAWN_EGG = REGISTRY.register(
      "goblin_archer_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.GOBLIN_ARCHER, -16751104, -26317, new Properties())
   );
   public static final RegistryObject<Item> GOBLIN_MAGE_SPAWN_EGG = REGISTRY.register(
      "goblin_mage_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.GOBLIN_MAGE, -16751104, -3407668, new Properties())
   );
   public static final RegistryObject<Item> GREEN_ORC_SPAWN_EGG = REGISTRY.register(
      "green_orc_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.GREEN_ORC, -16764109, -16751104, new Properties())
   );
   public static final RegistryObject<Item> HIGH_ORC_SPAWN_EGG = REGISTRY.register(
      "high_orc_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.HIGH_ORC, -10092544, -16751104, new Properties())
   );
   public static final RegistryObject<Item> SKELETON_SUMMONER_SPAWN_EGG = REGISTRY.register(
      "skeleton_summoner_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.SKELETON_SUMMONER, -1, -1, new Properties())
   );
   public static final RegistryObject<Item> LEVEL_ITEM = REGISTRY.register("level_item", () -> new LevelItemItem());
   public static final RegistryObject<Item> COIN_ITEM = REGISTRY.register("coin_item", () -> new CoinItemItem());
   public static final RegistryObject<Item> COIN_ITEM_100 = REGISTRY.register("coin_item_100", () -> new CoinItem100Item());
   public static final RegistryObject<Item> BLUEKEY = REGISTRY.register("bluekey", () -> new BluekeyItem());
   public static final RegistryObject<Item> YELLOWKEY = REGISTRY.register("yellowkey", () -> new YellowkeyItem());
   public static final RegistryObject<Item> REDKEY = REGISTRY.register("redkey", () -> new RedkeyItem());
   public static final RegistryObject<Item> SHADOW_MONARCH = REGISTRY.register("shadow_monarch", () -> new ShadowMonarchItem());
   public static final RegistryObject<Item> TEST_PARTICLES = REGISTRY.register("test_particles", () -> new TestParticlesItem());
   public static final RegistryObject<Item> CLASS_CHOOSER = REGISTRY.register("class_chooser", () -> new ClassChooserItem());
   public static final RegistryObject<Item> PURIFIED_BLOOD_OF_THE_DEMON_KING = REGISTRY.register(
      "purified_blood_of_the_demon_king", () -> new PurifiedBloodOfTheDemonKingItem()
   );
   public static final RegistryObject<Item> WORLD_TREES_FRAGMENT = REGISTRY.register("world_trees_fragment", () -> new WorldTreesFragmentItem());
   public static final RegistryObject<Item> SPRING_WATER_OF_THE_ECHOING_FOREST = REGISTRY.register(
      "spring_water_of_the_echoing_forest", () -> new SpringWaterOfTheEchoingForestItem()
   );
   public static final RegistryObject<Item> ORB_OF_AVARICE = REGISTRY.register("orb_of_avarice", OrbOfAvariceItem::new);
   public static final RegistryObject<Item> GIVE_BERU = REGISTRY.register("give_beru", () -> new GiveBeruItem());
   public static final RegistryObject<Item> GIVE_IGRIS = REGISTRY.register("give_igris", () -> new GiveIgrisItem());
   public static final RegistryObject<Item> GRAND_MAGE = REGISTRY.register("grand_mage", () -> new GrandMageItem());
   public static final RegistryObject<Item> GG = REGISTRY.register("gg", () -> new GgItem());
   public static final RegistryObject<Item> ROTATION_DEVICE = REGISTRY.register("rotation_device", () -> new RotationDeviceItem());
   public static final RegistryObject<Item> MAGIC_READER = REGISTRY.register("magic_reader", () -> new MagicReaderItem());
   public static final RegistryObject<Item> GIVE_KAMISH = REGISTRY.register("give_kamish", () -> new GiveKamishItem());
   public static final RegistryObject<Item> JOB_CHANGE_DEBUG = REGISTRY.register("job_change_debug", () -> new JobChangeDebugItem());
   public static final RegistryObject<Item> KAMISH_TOOTH = REGISTRY.register("kamish_tooth", () -> new KamishToothItem());
   public static final RegistryObject<Item> ASSASIN_STARTERPACK = REGISTRY.register("assasin_starterpack", () -> new AssasinStarterpackItem());
   public static final RegistryObject<Item> MAGE_STARTERPACK = REGISTRY.register("mage_starterpack", () -> new MageStarterpackItem());
   public static final RegistryObject<Item> FIGHTER_STARTERPACK = REGISTRY.register("fighter_starterpack", () -> new FighterStarterpackItem());
   public static final RegistryObject<Item> TANKER_STARTERPACK = REGISTRY.register("tanker_starterpack", () -> new TankerStarterpackItem());
   public static final RegistryObject<Item> HEALER_STARTERPACK = REGISTRY.register("healer_starterpack", () -> new HealerStarterpackItem());
   public static final RegistryObject<Item> RANGER_STARTERPACK = REGISTRY.register("ranger_starterpack", () -> new RangerStarterpackItem());
   public static final RegistryObject<Item> RANDOM_SPECIAL_BOX = REGISTRY.register("random_special_box", () -> new RandomSpecialBoxItem());
   public static final RegistryObject<Item> SELECTION_SPECIAL_BOX = REGISTRY.register("selection_special_box", () -> new SelectionSpecialBoxItem());
   public static final RegistryObject<Item> ASSASSIN_MASTERY_ITEM = REGISTRY.register("assassin_mastery_item", () -> new AssassinMasteryItemItem());
   public static final RegistryObject<Item> MAGE_MASTERY_ITEM = REGISTRY.register("mage_mastery_item", () -> new MageMasteryItemItem());
   public static final RegistryObject<Item> FIGHTER_MASTERY_ITEM = REGISTRY.register("fighter_mastery_item", () -> new FighterMasteryItemItem());
   public static final RegistryObject<Item> TANKER_MASTERY_ITEM = REGISTRY.register("tanker_mastery_item", () -> new TankerMasteryItemItem());
   public static final RegistryObject<Item> HEALER_MASTERY_ITEM = REGISTRY.register("healer_mastery_item", () -> new HealerMasteryItemItem());
   public static final RegistryObject<Item> RANGER_MASTERY_ITEM = REGISTRY.register("ranger_mastery_item", () -> new RangerMasteryItemItem());
   public static final RegistryObject<Item> SMALL_HEALTH_POTION = REGISTRY.register("small_health_potion", () -> new SmallHealthPotionItem());
   public static final RegistryObject<Item> MEDIUM_HEALTH_POTION = REGISTRY.register("medium_health_potion", () -> new MediumHealthPotionItem());
   public static final RegistryObject<Item> LARGE_HEALTH_POTION = REGISTRY.register("large_health_potion", () -> new LargeHealthPotionItem());
   public static final RegistryObject<Item> SMALL_MANA_POTION = REGISTRY.register("small_mana_potion", () -> new SmallManaPotionItem());
   public static final RegistryObject<Item> MEDIUM_MANA_POTION = REGISTRY.register("medium_mana_potion", () -> new MediumManaPotionItem());
   public static final RegistryObject<Item> LARGE_MANA_POTION = REGISTRY.register("large_mana_potion", () -> new LargeManaPotionItem());
   public static final RegistryObject<Item> SMALL_FATIGUE_POTION = REGISTRY.register("small_fatigue_potion", () -> new SmallFatiguePotionItem());
   public static final RegistryObject<Item> MEDIUM_FATIGUE_POTION = REGISTRY.register("medium_fatigue_potion", () -> new MediumFatiguePotionItem());
   public static final RegistryObject<Item> LARGE_FATIGUE_POTION = REGISTRY.register("large_fatigue_potion", () -> new LargeFatiguePotionItem());
   public static final RegistryObject<Item> HOLY_WATER_OF_LIFE = REGISTRY.register("holy_water_of_life", () -> new HolyWaterOfLifeItem());
   public static final RegistryObject<Item> GIVE_TUSK = REGISTRY.register("give_tusk", () -> new GiveTuskItem());
   public static final RegistryObject<Item> RUNESTONE_SHADOW_EXCHANGE = REGISTRY.register("runestone_shadow_exchange", () -> new RunestoneShadowExchangeItem());
   public static final RegistryObject<Item> RUNESTONE_SHADOW_SPIRITUAL_BODY_MANIFESTATION = REGISTRY.register(
      "runestone_shadow_spiritual_body_manifestation", () -> new RunestoneShadowSpiritualBodyManifestationItem()
   );
   public static final RegistryObject<Item> RUNESTONE_MONARCHS_DOMAIN = REGISTRY.register("runestone_monarchs_domain", () -> new RunestoneMonarchsDomainItem());
   public static final RegistryObject<Item> MURDERIOUS_INTENT_STONE = REGISTRY.register("murderious_intent_stone", () -> new MurderiousIntentStoneItem());
   public static final RegistryObject<Item> RUNESTONE_COLD_BLOOD = REGISTRY.register("runestone_cold_blood", () -> new RunestoneColdBloodItem());
   public static final RegistryObject<Item> TELEKINESIS_STONE = REGISTRY.register("telekinesis_stone", () -> new TelekinesisStoneItem());
   public static final RegistryObject<Item> STEALTH_STONE = REGISTRY.register("stealth_stone", () -> new StealthStoneItem());
   public static final RegistryObject<Item> RUNESTONE_BACKSTAB = REGISTRY.register("runestone_backstab", () -> new RunestoneBackstabItem());
   public static final RegistryObject<Item> RUNESTONE_DUALWIELD = REGISTRY.register("runestone_dualwield", () -> new RunestoneDualwieldItem());
   public static final RegistryObject<Item> RUNESTONE_DAGGER_THROW = REGISTRY.register("runestone_dagger_throw", () -> new RunestoneDaggerThrowItem());
   public static final RegistryObject<Item> RUNESTONE_DAGGER_RUSH = REGISTRY.register("runestone_dagger_rush", () -> new RunestoneDaggerRushItem());
   public static final RegistryObject<Item> RUNESTONE_SHADOWSTEP = REGISTRY.register("runestone_shadowstep", () -> new RunestoneShadowstepItem());
   public static final RegistryObject<Item> RUNESTONE_QUICKSLASHES = REGISTRY.register("runestone_quickslashes", () -> new RunestoneQuickslashesItem());
   public static final RegistryObject<Item> RUNESTONE_CURSE_CHAINS = REGISTRY.register(
      "runestone_curse_chains", () -> new RetiredMageRunestoneItem("Curse Chains")
   );
   public static final RegistryObject<Item> RUNESTONE_CURSE_SPHERE = REGISTRY.register(
      "runestone_curse_sphere", () -> new RetiredMageRunestoneItem("Curse Sphere")
   );
   public static final RegistryObject<Item> RUNESTONE_CURSED_SMOKE = REGISTRY.register(
      "runestone_cursed_smoke", () -> new RetiredMageRunestoneItem("Curse Smoke")
   );
   public static final RegistryObject<Item> RUNESTONE_WATERSLASH = REGISTRY.register("runestone_waterslash", () -> new RetiredMageRunestoneItem("Water Slash"));
   public static final RegistryObject<Item> RUNESTONE_LIGHTBALL = REGISTRY.register("runestone_lightball", () -> new RetiredMageRunestoneItem("Lightball"));
   public static final RegistryObject<Item> RUNESTONE_FLAME_WEAVING = REGISTRY.register(
      "runestone_flame_weaving", () -> new FireMageRunestoneItem("Flame Weaving", "Weave fast, mana-free fire bolts from raw magical output.")
   );
   public static final RegistryObject<Item> RUNESTONE_IGNITION_ORB = REGISTRY.register(
      "runestone_ignition_orb", () -> new FireMageRunestoneItem("Ignition Orb", "Launch a volatile orb that expands from impact blast to miniature sun.")
   );
   public static final RegistryObject<Item> RUNESTONE_INFERNO_LANCE = REGISTRY.register(
      "runestone_inferno_lance", () -> new FireMageRunestoneItem("Inferno Lance", "Fire a piercing solar lance that detonates Scorch at higher output.")
   );
   public static final RegistryObject<Item> RUNESTONE_FLASHFIRE = REGISTRY.register(
      "runestone_flashfire", () -> new FireMageRunestoneItem("Flashfire", "Become a rushing flame and carve through enemies in your path.")
   );
   public static final RegistryObject<Item> RUNESTONE_CREMATION = REGISTRY.register(
      "runestone_cremation", () -> new FireMageRunestoneItem("Cremation", "Consume Scorch to execute marked enemies in a linked combustion chain.")
   );
   public static final RegistryObject<Item> RUNESTONE_FURNACE_DOMINION = REGISTRY.register(
      "runestone_furnace_dominion", () -> new FireMageRunestoneItem("Furnace Dominion", "Enclose an area in a furnace that pulls, burns, and erupts.")
   );
   public static final RegistryObject<Item> RUNESTONE_HEAVENFALL = REGISTRY.register(
      "runestone_heavenfall", () -> new FireMageRunestoneItem("Heavenfall", "Call descending fire that evolves into the Day of Ruin.")
   );
   public static final RegistryObject<Item> RUNESTONE_FRACTURE_BOLT = REGISTRY.register(
      "runestone_fracture_bolt", () -> new BarrierMageRunestoneItem("Fracture Bolt", "Fire a razor-fast prism shard that Fractures magical defenses.")
   );
   public static final RegistryObject<Item> RUNESTONE_PRISM_RAMPART = REGISTRY.register(
      "runestone_prism_rampart", () -> new BarrierMageRunestoneItem("Prism Rampart", "Raise a breakable wall that catches hostile force and projectiles.")
   );
   public static final RegistryObject<Item> RUNESTONE_REPULSION_FRAME = REGISTRY.register(
      "runestone_repulsion_frame",
      () -> new BarrierMageRunestoneItem("Repulsion Frame", "Drive a moving frame through enemy lines and violently reposition them.")
   );
   public static final RegistryObject<Item> RUNESTONE_SEALING_PRISM = REGISTRY.register(
      "runestone_sealing_prism", () -> new BarrierMageRunestoneItem("Sealing Prism", "Imprison Fractured targets inside a pressure-sealed prism.")
   );
   public static final RegistryObject<Item> RUNESTONE_MIRROR_WARD = REGISTRY.register(
      "runestone_mirror_ward", () -> new BarrierMageRunestoneItem("Mirror Ward", "Catch incoming pressure and return it as retaliatory shards.")
   );
   public static final RegistryObject<Item> RUNESTONE_RESONANT_COLLAPSE = REGISTRY.register(
      "runestone_resonant_collapse",
      () -> new BarrierMageRunestoneItem("Resonant Collapse", "Collapse constructs and Fracture marks into synchronized destruction.")
   );
   public static final RegistryObject<Item> RUNESTONE_ABSOLUTE_BASTION = REGISTRY.register(
      "runestone_absolute_bastion", () -> new BarrierMageRunestoneItem("Absolute Bastion", "Enclose the battlefield in an immense fortress domain.")
   );
   public static final RegistryObject<Item> RUNESTONE_AETHER_BOLT = REGISTRY.register(
      "runestone_aether_bolt", () -> new ArcaneMageRunestoneItem("Aether Bolt", "Condense raw mana into a correcting precision bolt.")
   );
   public static final RegistryObject<Item> RUNESTONE_VECTOR_STEP = REGISTRY.register(
      "runestone_vector_step", () -> new ArcaneMageRunestoneItem("Vector Step", "Rewrite movement into a collision-safe phase vector.")
   );
   public static final RegistryObject<Item> RUNESTONE_POLARITY_SPHERE = REGISTRY.register(
      "runestone_polarity_sphere", () -> new ArcaneMageRunestoneItem("Polarity Sphere", "Shape a gravity node that pulls or repels the battlefield.")
   );
   public static final RegistryObject<Item> RUNESTONE_RUNIC_RELAY = REGISTRY.register(
      "runestone_runic_relay", () -> new ArcaneMageRunestoneItem("Runic Relay", "Route Arcane attacks through a distant linked formula.")
   );
   public static final RegistryObject<Item> RUNESTONE_ASTRAL_ARSENAL = REGISTRY.register(
      "runestone_astral_arsenal", () -> new ArcaneMageRunestoneItem("Astral Arsenal", "Maintain an orbiting formation of intelligent mana blades.")
   );
   public static final RegistryObject<Item> RUNESTONE_DIMENSIONAL_REND = REGISTRY.register(
      "runestone_dimensional_rend", () -> new ArcaneMageRunestoneItem("Dimensional Rend", "Cut space itself with a persistent piercing plane.")
   );
   public static final RegistryObject<Item> RUNESTONE_GRAND_FORMULA_CONVERGENCE = REGISTRY.register(
      "runestone_grand_formula_convergence",
      () -> new ArcaneMageRunestoneItem("Grand Formula: Convergence", "Assemble a battlefield-scale formula and collapse its center.")
   );
   public static final RegistryObject<Item> RUNESTONE_STATIC_NEEDLE = REGISTRY.register(
      "runestone_static_needle", () -> new StormMageRunestoneItem("Static Needle", "Fire a precise bolt that marks enemies Conductive.")
   );
   public static final RegistryObject<Item> RUNESTONE_SLIPSTREAM = REGISTRY.register(
      "runestone_slipstream", () -> new StormMageRunestoneItem("Slipstream", "Ride a safe four-tick current in your movement direction.")
   );
   public static final RegistryObject<Item> RUNESTONE_THUNDERCLAP = REGISTRY.register(
      "runestone_thunderclap", () -> new StormMageRunestoneItem("Thunderclap", "Peel nearby enemies away with a controlled thunder cone.")
   );
   public static final RegistryObject<Item> RUNESTONE_LIGHTNING_ROD = REGISTRY.register(
      "runestone_lightning_rod", () -> new StormMageRunestoneItem("Lightning Rod", "Privately designate an aimed enemy as your routing root.")
   );
   public static final RegistryObject<Item> RUNESTONE_CHAIN_LIGHTNING = REGISTRY.register(
      "runestone_chain_lightning", () -> new StormMageRunestoneItem("Chain Lightning", "Route a bounded lightning chain through visible enemies.")
   );
   public static final RegistryObject<Item> RUNESTONE_THUNDERHEAD = REGISTRY.register(
      "runestone_thunderhead", () -> new StormMageRunestoneItem("Thunderhead", "Place a short-lived storm that delivers scheduled strikes.")
   );
   public static final RegistryObject<Item> RUNESTONE_SKYBREAKER = REGISTRY.register(
      "runestone_skybreaker", () -> new StormMageRunestoneItem("Skybreaker", "Call a telegraphed vertical finisher onto a marked target or point.")
   );
   public static final RegistryObject<Item> RUNESTONE_TEMPEST_INCARNATE = REGISTRY.register(
      "runestone_tempest_incarnate", () -> new StormMageRunestoneItem("Tempest Incarnate", "Wear the storm, accelerating Voltage and creating bounded echoes.")
   );
   public static final RegistryObject<Item> RUNESTONE_LIGHT_GOLEM = REGISTRY.register(
      "runestone_light_golem", () -> new RetiredMageRunestoneItem("Light Golem")
   );
   public static final RegistryObject<Item> RUNESTONE_DETECTION = REGISTRY.register("runestone_detection", () -> new RunestoneDetectionItem());
   public static final RegistryObject<Item> RUNESTONE_MAGIC_MISSILES = REGISTRY.register(
      "runestone_magic_missiles", () -> new RetiredMageRunestoneItem("Magic Missiles")
   );
   public static final RegistryObject<Item> RUNESTONE_SLASHDASH = REGISTRY.register("runestone_slashdash", () -> new RunestoneSlashdashItem());
   public static final RegistryObject<Item> RUNESTONE_SLASH_FURY = REGISTRY.register("runestone_slash_fury", () -> new RunestoneSlashFuryItem());
   public static final RegistryObject<Item> RUNESTONE_CRITICALSTRIKE = REGISTRY.register("runestone_criticalstrike", () -> new RunestoneCriticalstrikeItem());
   public static final RegistryObject<Item> RUNESTONE_SWORDOF_LIGHT = REGISTRY.register("runestone_swordof_light", () -> new RunestoneSwordofLightItem());
   public static final RegistryObject<Item> RUNESTONE_SLAM = REGISTRY.register("runestone_slam", () -> new RunestoneSlamItem());
   public static final RegistryObject<Item> RUNESTONE_SWORD_DANCE = REGISTRY.register("runestone_sword_dance", () -> new RunestoneSwordDanceItem());
   public static final RegistryObject<Item> RUNESTONE_HEAL_BEAM = REGISTRY.register("runestone_heal_beam", () -> new RunestoneHealBeamItem());
   public static final RegistryObject<Item> RUNESTONE_BLESSING_MARK = REGISTRY.register("runestone_blessing_mark", () -> new RunestoneBlessingMarkItem());
   public static final RegistryObject<Item> RUNESTONE_PURIFICATION = REGISTRY.register("runestone_purification", () -> new RunestonePurificationItem());
   public static final RegistryObject<Item> RUNESTONE_PHYSICAL = REGISTRY.register("runestone_physical", () -> new RunestonePhysicalItem());
   public static final RegistryObject<Item> RUNESTONE_HASTE = REGISTRY.register("runestone_haste", () -> new RunestoneHasteItem());
   public static final RegistryObject<Item> RUNESTONE_OVERHEAL = REGISTRY.register("runestone_overheal", () -> new RunestoneOverhealItem());
   public static final RegistryObject<Item> RUNESTONE_TANK_LEAP = REGISTRY.register("runestone_tank_leap", () -> new RunestoneTankLeapItem());
   public static final RegistryObject<Item> RUNESTONE_PROTECTION_MARK = REGISTRY.register("runestone_protection_mark", () -> new RunestoneProtectionMarkItem());
   public static final RegistryObject<Item> RUNESTONE_REINFORCEMENT = REGISTRY.register("runestone_reinforcement", () -> new RunestoneReinforcementItem());
   public static final RegistryObject<Item> RUNESTONE_SHIELD_BASH = REGISTRY.register("runestone_shield_bash", () -> new RunestoneShieldBashItem());
   public static final RegistryObject<Item> RUNESTONE_WILLPOWER = REGISTRY.register("runestone_willpower", () -> new RunestoneWillpowerItem());
   public static final RegistryObject<Item> RUNESTONE_TAUNT = REGISTRY.register("runestone_taunt", () -> new RunestoneTauntItem());
   public static final RegistryObject<Item> RUNESTONE_MANA_QUIVER = REGISTRY.register("runestone_mana_quiver", () -> new RunestoneManaQuiverItem());
   public static final RegistryObject<Item> RUNESTONE_SHARPSHOOTER = REGISTRY.register("runestone_sharpshooter", () -> new RunestoneSharpshooterItem());
   public static final RegistryObject<Item> RUNESTONE_PROXIMITY_TRAP = REGISTRY.register("runestone_proximity_trap", () -> new RunestoneProximityTrapItem());
   public static final RegistryObject<Item> RUNESTONE_HIGH_VALUE_TARGET = REGISTRY.register(
      "runestone_high_value_target", () -> new RunestoneHighValueTargetItem()
   );
   public static final RegistryObject<Item> RUNESTONE_BACKSTEP = REGISTRY.register("runestone_backstep", () -> new RunestoneBackstepItem());
   public static final RegistryObject<Item> RUNESTONE_HAWKEYE = REGISTRY.register("runestone_hawkeye", () -> new RunestoneHawkeyeItem());
   public static final RegistryObject<Item> RUNESTONE_HYPERFOCUS = REGISTRY.register("runestone_hyperfocus", () -> new RunestoneHyperfocusItem());
   public static final RegistryObject<Item> GOBLIN_SPAWNER = block(SololevelingModBlocks.GOBLIN_SPAWNER);
   public static final RegistryObject<Item> DISAPPEARING_BLOCK = block(SololevelingModBlocks.DISAPPEARING_BLOCK);
   public static final RegistryObject<Item> UNBREAKABLE_DEEPSLATE = block(SololevelingModBlocks.UNBREAKABLE_DEEPSLATE);
   public static final RegistryObject<Item> DEEPSLATE_KEYBLOCK = block(SololevelingModBlocks.DEEPSLATE_KEYBLOCK);
   public static final RegistryObject<Item> DEEPSLATE_KEYBLOCK_BLUE = block(SololevelingModBlocks.DEEPSLATE_KEYBLOCK_BLUE);
   public static final RegistryObject<Item> DEEPSLATE_KEYBLOCK_RED = block(SololevelingModBlocks.DEEPSLATE_KEYBLOCK_RED);
   public static final RegistryObject<Item> CRYSTAL_GOLEM_SPAWNER = block(SololevelingModBlocks.CRYSTAL_GOLEM_SPAWNER);
   public static final RegistryObject<Item> GOLEM_DROP_BLOCK_GEM = block(SololevelingModBlocks.GOLEM_DROP_BLOCK_GEM);
   public static final RegistryObject<Item> MANA_CRYSTAL_DEPOSIT = block(SololevelingModBlocks.MANA_CRYSTAL_DEPOSIT);
   public static final RegistryObject<Item> INSTANCE_DUNGEON_KEY_LOGGER = REGISTRY.register(
      SololevelingModBlocks.INSTANCE_DUNGEON_KEY_LOGGER.getId().getPath(),
      () -> new InstanceDungeonKeyLoggerDisplayItem(SololevelingModBlocks.INSTANCE_DUNGEON_KEY_LOGGER.get(), new Properties())
   );
   public static final RegistryObject<Item> INSTANCE_COVER = REGISTRY.register(
      SololevelingModBlocks.INSTANCE_COVER.getId().getPath(), () -> new InstanceCoverDisplayItem(SololevelingModBlocks.INSTANCE_COVER.get(), new Properties())
   );
   public static final RegistryObject<Item> HUNTER_RANK_EVALUATOR = REGISTRY.register(
      SololevelingModBlocks.HUNTER_RANK_EVALUATOR.getId().getPath(),
      () -> new HunterRankEvaluatorDisplayItem(SololevelingModBlocks.HUNTER_RANK_EVALUATOR.get(), new Properties())
   );
   public static final RegistryObject<Item> WOODEN_PASSAGE_OPEN = block(SololevelingModBlocks.WOODEN_PASSAGE_OPEN);
   public static final RegistryObject<Item> CELL_DOOR_CLOSED = block(SololevelingModBlocks.CELL_DOOR_CLOSED);
   public static final RegistryObject<Item> DUNGEON_FLOOR = block(SololevelingModBlocks.DUNGEON_FLOOR);
   public static final RegistryObject<Item> DUNGEON_BRICKS = block(SololevelingModBlocks.DUNGEON_BRICKS);
   public static final RegistryObject<Item> CELL_DOOR_OPEN = block(SololevelingModBlocks.CELL_DOOR_OPEN);
   public static final RegistryObject<Item> DUNGEON_WALL_SMALL = block(SololevelingModBlocks.DUNGEON_WALL_SMALL);
   public static final RegistryObject<Item> DUNGEON_BARREL = block(SololevelingModBlocks.DUNGEON_BARREL);
   public static final RegistryObject<Item> DUNGEON_TOOLS = block(SololevelingModBlocks.DUNGEON_TOOLS);
   public static final RegistryObject<Item> SKELETON = block(SololevelingModBlocks.SKELETON);
   public static final RegistryObject<Item> DUNGEON_GRAVE_1 = block(SololevelingModBlocks.DUNGEON_GRAVE_1);
   public static final RegistryObject<Item> DUNGEON_GRAVE_2 = block(SololevelingModBlocks.DUNGEON_GRAVE_2);
   public static final RegistryObject<Item> PASSAGE_WALL = block(SololevelingModBlocks.PASSAGE_WALL);
   public static final RegistryObject<Item> DUNGEON_WALL = REGISTRY.register(
      SololevelingModBlocks.DUNGEON_WALL.getId().getPath(), () -> new DungeonWallDisplayItem(SololevelingModBlocks.DUNGEON_WALL.get(), new Properties())
   );
   public static final RegistryObject<Item> EVALUATOR_TEST = block(SololevelingModBlocks.EVALUATOR_TEST);
   public static final RegistryObject<Item> MANA_CRYSTAL_E = REGISTRY.register("mana_crystal_e", () -> new ManaCrystalEItem());
   public static final RegistryObject<Item> MANA_CRYSTAL_D = REGISTRY.register("mana_crystal_d", () -> new ManaCrystalDItem());
   public static final RegistryObject<Item> MANA_CRYSTAL_C = REGISTRY.register("mana_crystal_c", () -> new ManaCrystalCItem());
   public static final RegistryObject<Item> MANA_CRYSTAL_B = REGISTRY.register("mana_crystal_b", () -> new ManaCrystalBItem());
   public static final RegistryObject<Item> MANA_CRYSTAL_A = REGISTRY.register("mana_crystal_a", () -> new ManaCrystalAItem());
   public static final RegistryObject<Item> MANA_CRYSTAL_S = REGISTRY.register("mana_crystal_s", () -> new ManaCrystalSItem());
   public static final RegistryObject<Item> CHOI_CLOAK_CHESTPLATE = REGISTRY.register("choi_cloak_chestplate", () -> new ChoiCloakItem.Chestplate());
   public static final RegistryObject<Item> SUNG_JIN_WOO_DRIP_CHESTPLATE = REGISTRY.register(
      "sung_jin_woo_drip_chestplate", () -> new SungJinWooDripItem.Chestplate()
   );
   public static final RegistryObject<Item> SUNG_JIN_WOO_DRIP_LEGGINGS = REGISTRY.register(
      "sung_jin_woo_drip_leggings", () -> new SungJinWooDripItem.Leggings()
   );
   public static final RegistryObject<Item> SUNG_JIN_WOO_DRIP_2_CHESTPLATE = REGISTRY.register(
      "sung_jin_woo_drip_2_chestplate", () -> new SungJinWooDrip2Item.Chestplate()
   );
   public static final RegistryObject<Item> SUNG_JIN_WOO_DRIP_2_LEGGINGS = REGISTRY.register(
      "sung_jin_woo_drip_2_leggings", () -> new SungJinWooDrip2Item.Leggings()
   );
   public static final RegistryObject<Item> CREATIVETABITEM = REGISTRY.register("creativetabitem", () -> new CreativetabitemItem());
   public static final RegistryObject<Item> IGRISLONGSWORD = REGISTRY.register("igrislongsword", () -> new IgrislongswordItem());
   public static final RegistryObject<Item> SHADOW_ARMOR_HELMET = REGISTRY.register("shadow_armor_helmet", () -> new ShadowARMORItem.Helmet());
   public static final RegistryObject<Item> SHADOW_ARMOR_CHESTPLATE = REGISTRY.register("shadow_armor_chestplate", () -> new ShadowARMORItem.Chestplate());
   public static final RegistryObject<Item> SHADOW_ARMOR_LEGGINGS = REGISTRY.register("shadow_armor_leggings", () -> new ShadowARMORItem.Leggings());
   public static final RegistryObject<Item> SHADOW_ARMOR_BOOTS = REGISTRY.register("shadow_armor_boots", () -> new ShadowARMORItem.Boots());
   public static final RegistryObject<Item> GOLIATH_ARMOR_HELMET = REGISTRY.register("goliath_armor_helmet", () -> new GoliathARMORItem.Helmet());
   public static final RegistryObject<Item> GOLIATH_ARMOR_CHESTPLATE = REGISTRY.register("goliath_armor_chestplate", () -> new GoliathARMORItem.Chestplate());
   public static final RegistryObject<Item> GOLIATH_ARMOR_LEGGINGS = REGISTRY.register("goliath_armor_leggings", () -> new GoliathARMORItem.Leggings());
   public static final RegistryObject<Item> GOLIATH_ARMOR_BOOTS = REGISTRY.register("goliath_armor_boots", () -> new GoliathARMORItem.Boots());
   public static final RegistryObject<Item> DUNGEON_SPAWNER = REGISTRY.register("dungeon_spawner", () -> new DungeonSpawnerItem());
   public static final RegistryObject<Item> GRIAMORE = REGISTRY.register("griamore", () -> new GriamoreItem());
   public static final RegistryObject<Item> TELEPORT_SAI = REGISTRY.register("teleport_sai", () -> new TeleportSaiItem());
   public static final RegistryObject<Item> TEST = REGISTRY.register("test", () -> new TestItem());
   public static final RegistryObject<Item> DUN_1 = REGISTRY.register("dun_1", () -> new Dun1Item());
   public static final RegistryObject<Item> DUNGEON_NOSTALGIA = REGISTRY.register("dungeon_nostalgia", () -> new DungeonNostalgiaItem());
   public static final RegistryObject<Item> KANG_HAIR_HELMET = REGISTRY.register("kang_hair_helmet", () -> new KangHairItem.Helmet());
   public static final RegistryObject<Item> KANGS_DAGGER = REGISTRY.register("kangs_dagger", () -> new KangsDaggerItem());
   public static final RegistryObject<Item> CUSTOM_PORTAL = block(SololevelingModBlocks.CUSTOM_PORTAL);
   public static final RegistryObject<Item> SHADOW = block(SololevelingModBlocks.SHADOW);
   public static final RegistryObject<Item> RUNESTONE_CRITICAL_ATTACK = REGISTRY.register("runestone_critical_attack", () -> new RunestoneCriticalAttackItem());
   public static final RegistryObject<Item> RUNESTONE_MUTILATION = REGISTRY.register("runestone_mutilation", () -> new RunestoneMutilationItem());
   public static final RegistryObject<Item> RUNESTONE_SWORD_BEAM = REGISTRY.register("runestone_sword_beam", () -> new RunestoneSwordBeamItem());
   public static final RegistryObject<Item> DKC_LEVEL_ITEM = REGISTRY.register("dkc_level_item", () -> new DKCLevelItemItem());
   public static final RegistryObject<Item> DEMON_SPAWN_EGG = REGISTRY.register(
      "demon_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.DEMON, -13434880, -39322, new Properties())
   );
   public static final RegistryObject<Item> CERBERUS_SPAWN_EGG = REGISTRY.register(
      "cerberus_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.CERBERUS, -6724096, -13434880, new Properties())
   );
   public static final RegistryObject<Item> VULCAN_SPAWN_EGG = REGISTRY.register(
      "vulcan_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.VULCAN, -16777216, -26215, new Properties())
   );
   public static final RegistryObject<Item> BARAN_SPAWN_EGG = REGISTRY.register(
      "baran_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.BARAN, -16764109, -6684673, new Properties())
   );
   public static final RegistryObject<Item> KAISELIN_SPAWN_EGG = REGISTRY.register(
      "kaiselin_spawn_egg", () -> new ForgeSpawnEggItem(SololevelingModEntities.KAISELIN, -16777114, -16711681, new Properties())
   );
   public static final RegistryObject<Item> DEEPSLATE_KEYBLOCK_DKC = block(SololevelingModBlocks.DEEPSLATE_KEYBLOCK_DKC);
   public static final RegistryObject<Item> ENTRY_PERMIT = REGISTRY.register("entry_permit", () -> new EntryPermitItem());
   public static final RegistryObject<Item> DKC_TRAVEL = REGISTRY.register("dkc_travel", () -> new DKCTravelItem());
   public static final RegistryObject<Item> DUNGEON_SURVEYOR_WAND = REGISTRY.register(
      "dungeon_surveyor_wand", () -> new DungeonBuilderWandItem(DungeonBuilderTool.SURVEYOR)
   );
   public static final RegistryObject<Item> DUNGEON_SOCKET_WAND = REGISTRY.register(
      "dungeon_socket_wand", () -> new DungeonBuilderWandItem(DungeonBuilderTool.SOCKET)
   );
   public static final RegistryObject<Item> DUNGEON_ENCOUNTER_WAND = REGISTRY.register(
      "dungeon_encounter_wand", () -> new DungeonBuilderWandItem(DungeonBuilderTool.ENCOUNTER)
   );
   public static final RegistryObject<Item> DUNGEON_FEATURE_WAND = REGISTRY.register(
      "dungeon_feature_wand", () -> new DungeonBuilderWandItem(DungeonBuilderTool.FEATURE)
   );
   public static final RegistryObject<Item> DUNGEON_BUILDER_WAND = REGISTRY.register(
      "dungeon_builder_wand", () -> new DungeonBuilderWandItem(DungeonBuilderTool.BUILDER)
   );
   public static final RegistryObject<Item> GUILD_COMPUTER = block(SololevelingModBlocks.GUILD_COMPUTER);

   private static RegistryObject<Item> block(RegistryObject<Block> block) {
      return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Properties()));
   }
}
