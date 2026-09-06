package dev.eness.sololevelingfinal.core.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import dev.eness.sololevelingfinal.core.world.inventory.AbilitiesGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.AhjinJoinMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ChooseClassMenu;
import dev.eness.sololevelingfinal.core.world.inventory.DailyQuestsMenu;
import dev.eness.sololevelingfinal.core.world.inventory.EquippedAbilitiesMenu;
import dev.eness.sololevelingfinal.core.world.inventory.FireGriamoreMenu;
import dev.eness.sololevelingfinal.core.world.inventory.FoodGuiMenu;
import dev.eness.sololevelingfinal.core.world.inventory.GuildComputerMenu;
import dev.eness.sololevelingfinal.core.world.inventory.HunterIDGuiMenu;
import dev.eness.sololevelingfinal.core.world.inventory.HuntersJoinMenu;
import dev.eness.sololevelingfinal.core.world.inventory.MiscItemsMenu;
import dev.eness.sololevelingfinal.core.world.inventory.PanelEarlyMenu;
import dev.eness.sololevelingfinal.core.world.inventory.PanelRework2Menu;
import dev.eness.sololevelingfinal.core.world.inventory.PanelReworkMenu;
import dev.eness.sololevelingfinal.core.world.inventory.PathMenu;
import dev.eness.sololevelingfinal.core.world.inventory.PocketDimensionGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.QuestsMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ReaderGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.RewardPanelMenu;
import dev.eness.sololevelingfinal.core.world.inventory.SelectionBoxGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowCommandMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowCustomizationMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowDismissMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeMainGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeSETMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeSaveMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowSummonGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.ShopMenu;
import dev.eness.sololevelingfinal.core.world.inventory.SpecialCraftingGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.StoreGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.StorePotionNewMenu;
import dev.eness.sololevelingfinal.core.world.inventory.StoreWeaponMenu;
import dev.eness.sololevelingfinal.core.world.inventory.StoreWeaponReworkMenu;
import dev.eness.sololevelingfinal.core.world.inventory.StorepotionMenu;
import dev.eness.sololevelingfinal.core.world.inventory.TrainingGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab1Menu;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab2Menu;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab3Menu;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab4Menu;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab5Menu;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab6Menu;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab7Menu;
import dev.eness.sololevelingfinal.core.world.inventory.WTJoinMenu;

public class SololevelingModMenus {
   public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "sololeveling");
   public static final RegistryObject<MenuType<PanelEarlyMenu>> PANEL_EARLY = REGISTRY.register("panel_early", () -> IForgeMenuType.create(PanelEarlyMenu::new));
   public static final RegistryObject<MenuType<RewardPanelMenu>> REWARD_PANEL = REGISTRY.register(
      "reward_panel", () -> IForgeMenuType.create(RewardPanelMenu::new)
   );
   public static final RegistryObject<MenuType<TrainingGUIMenu>> TRAINING_GUI = REGISTRY.register(
      "training_gui", () -> IForgeMenuType.create(TrainingGUIMenu::new)
   );
   public static final RegistryObject<MenuType<AhjinJoinMenu>> AHJIN_JOIN = REGISTRY.register("ahjin_join", () -> IForgeMenuType.create(AhjinJoinMenu::new));
   public static final RegistryObject<MenuType<StoreGUIMenu>> STORE_GUI = REGISTRY.register("store_gui", () -> IForgeMenuType.create(StoreGUIMenu::new));
   public static final RegistryObject<MenuType<StoreWeaponMenu>> STORE_WEAPON = REGISTRY.register(
      "store_weapon", () -> IForgeMenuType.create(StoreWeaponMenu::new)
   );
   public static final RegistryObject<MenuType<StorepotionMenu>> STOREPOTION = REGISTRY.register(
      "storepotion", () -> IForgeMenuType.create(StorepotionMenu::new)
   );
   public static final RegistryObject<MenuType<AbilitiesGUIMenu>> ABILITIES_GUI = REGISTRY.register(
      "abilities_gui", () -> IForgeMenuType.create(AbilitiesGUIMenu::new)
   );
   public static final RegistryObject<MenuType<PocketDimensionGUIMenu>> POCKET_DIMENSION_GUI = REGISTRY.register(
      "pocket_dimension_gui", () -> IForgeMenuType.create(PocketDimensionGUIMenu::new)
   );
   public static final RegistryObject<MenuType<ChooseClassMenu>> CHOOSE_CLASS = REGISTRY.register(
      "choose_class", () -> IForgeMenuType.create(ChooseClassMenu::new)
   );
   public static final RegistryObject<MenuType<SpecialCraftingGUIMenu>> SPECIAL_CRAFTING_GUI = REGISTRY.register(
      "special_crafting_gui", () -> IForgeMenuType.create(SpecialCraftingGUIMenu::new)
   );
   public static final RegistryObject<MenuType<FireGriamoreMenu>> FIRE_GRIAMORE = REGISTRY.register(
      "fire_griamore", () -> IForgeMenuType.create(FireGriamoreMenu::new)
   );
   public static final RegistryObject<MenuType<ShadowGUIMenu>> SHADOW_GUI = REGISTRY.register("shadow_gui", () -> IForgeMenuType.create(ShadowGUIMenu::new));
   public static final RegistryObject<MenuType<HuntersJoinMenu>> HUNTERS_JOIN = REGISTRY.register(
      "hunters_join", () -> IForgeMenuType.create(HuntersJoinMenu::new)
   );
   public static final RegistryObject<MenuType<WTJoinMenu>> WT_JOIN = REGISTRY.register("wt_join", () -> IForgeMenuType.create(WTJoinMenu::new));
   public static final RegistryObject<MenuType<DailyQuestsMenu>> DAILY_QUESTS = REGISTRY.register(
      "daily_quests", () -> IForgeMenuType.create(DailyQuestsMenu::new)
   );
   public static final RegistryObject<MenuType<StoreWeaponReworkMenu>> STORE_WEAPON_REWORK = REGISTRY.register(
      "store_weapon_rework", () -> IForgeMenuType.create(StoreWeaponReworkMenu::new)
   );
   public static final RegistryObject<MenuType<ShopMenu>> SHOP = REGISTRY.register("shop", () -> IForgeMenuType.create(ShopMenu::new));
   public static final RegistryObject<MenuType<FoodGuiMenu>> FOOD_GUI = REGISTRY.register("food_gui", () -> IForgeMenuType.create(FoodGuiMenu::new));
   public static final RegistryObject<MenuType<ShadowSummonGUIMenu>> SHADOW_SUMMON_GUI = REGISTRY.register(
      "shadow_summon_gui", () -> IForgeMenuType.create(ShadowSummonGUIMenu::new)
   );
   public static final RegistryObject<MenuType<ShadowCustomizationMenu>> SHADOW_CUSTOMIZATION = REGISTRY.register(
      "shadow_customization", () -> IForgeMenuType.create(ShadowCustomizationMenu::new)
   );
   public static final RegistryObject<MenuType<ShadowDismissMenu>> SHADOW_DISMISS = REGISTRY.register(
      "shadow_dismiss", () -> IForgeMenuType.create(ShadowDismissMenu::new)
   );
   public static final RegistryObject<MenuType<ShadowCommandMenu>> SHADOW_COMMAND = REGISTRY.register(
      "shadow_command", () -> IForgeMenuType.create(ShadowCommandMenu::new)
   );
   public static final RegistryObject<MenuType<ReaderGUIMenu>> READER_GUI = REGISTRY.register("reader_gui", () -> IForgeMenuType.create(ReaderGUIMenu::new));
   public static final RegistryObject<MenuType<HunterIDGuiMenu>> HUNTER_ID_GUI = REGISTRY.register(
      "hunter_id_gui", () -> IForgeMenuType.create(HunterIDGuiMenu::new)
   );
   public static final RegistryObject<MenuType<MiscItemsMenu>> MISC_ITEMS = REGISTRY.register("misc_items", () -> IForgeMenuType.create(MiscItemsMenu::new));
   public static final RegistryObject<MenuType<SelectionBoxGUIMenu>> SELECTION_BOX_GUI = REGISTRY.register(
      "selection_box_gui", () -> IForgeMenuType.create(SelectionBoxGUIMenu::new)
   );
   public static final RegistryObject<MenuType<PanelReworkMenu>> PANEL_REWORK = REGISTRY.register(
      "panel_rework", () -> IForgeMenuType.create(PanelReworkMenu::new)
   );
   public static final RegistryObject<MenuType<PanelRework2Menu>> PANEL_REWORK_2 = REGISTRY.register(
      "panel_rework_2", () -> IForgeMenuType.create(PanelRework2Menu::new)
   );
   public static final RegistryObject<MenuType<EquippedAbilitiesMenu>> EQUIPPED_ABILITIES = REGISTRY.register(
      "equipped_abilities", () -> IForgeMenuType.create(EquippedAbilitiesMenu::new)
   );
   public static final RegistryObject<MenuType<UnlockedSkillsTab1Menu>> UNLOCKED_SKILLS_TAB_1 = REGISTRY.register(
      "unlocked_skills_tab_1", () -> IForgeMenuType.create(UnlockedSkillsTab1Menu::new)
   );
   public static final RegistryObject<MenuType<UnlockedSkillsTab2Menu>> UNLOCKED_SKILLS_TAB_2 = REGISTRY.register(
      "unlocked_skills_tab_2", () -> IForgeMenuType.create(UnlockedSkillsTab2Menu::new)
   );
   public static final RegistryObject<MenuType<UnlockedSkillsTab3Menu>> UNLOCKED_SKILLS_TAB_3 = REGISTRY.register(
      "unlocked_skills_tab_3", () -> IForgeMenuType.create(UnlockedSkillsTab3Menu::new)
   );
   public static final RegistryObject<MenuType<UnlockedSkillsTab4Menu>> UNLOCKED_SKILLS_TAB_4 = REGISTRY.register(
      "unlocked_skills_tab_4", () -> IForgeMenuType.create(UnlockedSkillsTab4Menu::new)
   );
   public static final RegistryObject<MenuType<UnlockedSkillsTab5Menu>> UNLOCKED_SKILLS_TAB_5 = REGISTRY.register(
      "unlocked_skills_tab_5", () -> IForgeMenuType.create(UnlockedSkillsTab5Menu::new)
   );
   public static final RegistryObject<MenuType<UnlockedSkillsTab6Menu>> UNLOCKED_SKILLS_TAB_6 = REGISTRY.register(
      "unlocked_skills_tab_6", () -> IForgeMenuType.create(UnlockedSkillsTab6Menu::new)
   );
   public static final RegistryObject<MenuType<ShadowExchangeSETMenu>> SHADOW_EXCHANGE_SET = REGISTRY.register(
      "shadow_exchange_set", () -> IForgeMenuType.create(ShadowExchangeSETMenu::new)
   );
   public static final RegistryObject<MenuType<ShadowExchangeSaveMenu>> SHADOW_EXCHANGE_SAVE = REGISTRY.register(
      "shadow_exchange_save", () -> IForgeMenuType.create(ShadowExchangeSaveMenu::new)
   );
   public static final RegistryObject<MenuType<ShadowExchangeMainGUIMenu>> SHADOW_EXCHANGE_MAIN_GUI = REGISTRY.register(
      "shadow_exchange_main_gui", () -> IForgeMenuType.create(ShadowExchangeMainGUIMenu::new)
   );
   public static final RegistryObject<MenuType<StorePotionNewMenu>> STORE_POTION_NEW = REGISTRY.register(
      "store_potion_new", () -> IForgeMenuType.create(StorePotionNewMenu::new)
   );
   public static final RegistryObject<MenuType<UnlockedSkillsTab7Menu>> UNLOCKED_SKILLS_TAB_7 = REGISTRY.register(
      "unlocked_skills_tab_7", () -> IForgeMenuType.create(UnlockedSkillsTab7Menu::new)
   );
   public static final RegistryObject<MenuType<PathMenu>> PATH = REGISTRY.register("path", () -> IForgeMenuType.create(PathMenu::new));
   public static final RegistryObject<MenuType<QuestsMenu>> QUESTS = REGISTRY.register("quests", () -> IForgeMenuType.create(QuestsMenu::new));
   public static final RegistryObject<MenuType<GuildComputerMenu>> GUILD_COMPUTER = REGISTRY.register(
      "guild_computer", () -> IForgeMenuType.create(GuildComputerMenu::new)
   );
}
