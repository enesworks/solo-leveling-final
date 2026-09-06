package dev.eness.sololevelingfinal.core.world.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import dev.eness.sololevelingfinal.core.init.SololevelingModMenus;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class ShadowSummonGUIMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
   private static final int SHADOW_TYPE_COUNT = 14;
   private static final int FIELD_RANK = 0;
   private static final int FIELD_LEVEL_LOW = 1;
   private static final int FIELD_LEVEL_HIGH = 2;
   private static final int FIELD_RANK_XP_LOW = 3;
   private static final int FIELD_RANK_XP_HIGH = 4;
   private static final int FIELD_RANK_XP_NEEDED_LOW = 5;
   private static final int FIELD_RANK_XP_NEEDED_HIGH = 6;
   private static final int FIELD_NEXT_RANK = 7;
   private static final int FIELD_FLAGS = 8;
   private static final int FIELD_OWNED = 9;
   private static final int FIELD_SUMMONED = 10;
   private static final int FIELD_COUNT = 11;
   private static final int SHADOW_DATA_COUNT = 154;
   private static final int HEAL_BOSS_COST_LOW = 154;
   private static final int HEAL_BOSS_COST_HIGH = 155;
   private static final int HEAL_ALL_COST_LOW = 156;
   private static final int HEAL_ALL_COST_HIGH = 157;
   private static final int MENU_DATA_COUNT = 158;
   private static final int FLAG_LEVEL_CAPPED = 1;
   private static final int FLAG_MAX_RANK = 2;
   private static final int FLAG_CUSTOMIZABLE = 4;
   private static final int FLAG_EQUIPPED = 8;
   private static final int FLAG_GRAND_MARSHAL_ELIGIBLE = 16;
   private static final int FLAG_GRAND_MARSHAL_ACTIVE = 32;
   public static final HashMap<String, Object> guistate = new HashMap<>();
   public final Level world;
   public final Player entity;
   public int x;
   public int y;
   public int z;
   private ContainerLevelAccess access = ContainerLevelAccess.NULL;
   private IItemHandler internal;
   private final Map<Integer, Slot> customSlots = new HashMap<>();
   private boolean bound = false;
   private Supplier<Boolean> boundItemMatcher = null;
   private Entity boundEntity = null;
   private BlockEntity boundBlockEntity = null;
   private final ContainerData shadowData;
   private long healingQuoteTick = Long.MIN_VALUE;
   private ShadowMonarchManager.ShadowHealingQuote cachedHealingQuote = new ShadowMonarchManager.ShadowHealingQuote(0, 0, 0, 0);

   public ShadowSummonGUIMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
      super(SololevelingModMenus.SHADOW_SUMMON_GUI.get(), id);
      this.entity = inv.player;
      this.world = inv.player.level();
      this.internal = new ItemStackHandler(0);
      if (this.world.isClientSide()) {
         this.shadowData = new SimpleContainerData(158);
      } else {
         ShadowMonarchManager.prepareRosterForDisplay(this.entity);
         this.shadowData = new ContainerData() {
            @Override
            public int get(int index) {
               if (index < 0 || index >= 158) {
                  return 0;
               }

               if (index >= 154) {
                  ShadowMonarchManager.ShadowHealingQuote quote = ShadowSummonGUIMenu.this.healingQuoteForData();

                  return switch (index) {
                     case 154 -> ShadowSummonGUIMenu.lowWord(quote.bossManaCost());
                     case 155 -> ShadowSummonGUIMenu.highWord(quote.bossManaCost());
                     case 156 -> ShadowSummonGUIMenu.lowWord(quote.allManaCost());
                     case 157 -> ShadowSummonGUIMenu.highWord(quote.allManaCost());
                     default -> 0;
                  };
               } else {
                  int buttonId = index % 14;
                  int field = index / 14;
                  String type = ShadowMonarchManager.typeForSummonButton(buttonId);
                  ShadowMonarchManager.ShadowDisplayProgress progress = ShadowMonarchManager.progressForDisplay(ShadowSummonGUIMenu.this.entity, type);

                  return switch (field) {
                     case 0 -> progress.rank();
                     case 1 -> ShadowSummonGUIMenu.lowWord(progress.level());
                     case 2 -> ShadowSummonGUIMenu.highWord(progress.level());
                     case 3 -> ShadowSummonGUIMenu.lowWord(progress.rankXp());
                     case 4 -> ShadowSummonGUIMenu.highWord(progress.rankXp());
                     case 5 -> ShadowSummonGUIMenu.lowWord(progress.rankXpNeeded());
                     case 6 -> ShadowSummonGUIMenu.highWord(progress.rankXpNeeded());
                     case 7 -> progress.nextRank();
                     case 8 -> (progress.levelCapped() ? 1 : 0)
                        | (progress.maxRank() ? 2 : 0)
                        | (ShadowMonarchManager.hasShadowForDisplay(ShadowSummonGUIMenu.this.entity, type) ? 4 : 0)
                        | (ShadowMonarchManager.hasEquipmentForDisplay(ShadowSummonGUIMenu.this.entity, type) ? 8 : 0)
                        | (progress.grandMarshalEligible() ? 16 : 0)
                        | (progress.grandMarshalActive() ? 32 : 0);
                     case 9 -> ShadowMonarchManager.ownedCountForDisplay(ShadowSummonGUIMenu.this.entity, type);
                     case 10 -> ShadowMonarchManager.summonedCountForDisplay(ShadowSummonGUIMenu.this.entity, type);
                     default -> 0;
                  };
               }
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
               return 158;
            }
         };
      }

      this.addDataSlots(this.shadowData);
      BlockPos pos = null;
      if (extraData != null) {
         pos = extraData.readBlockPos();
         this.x = pos.getX();
         this.y = pos.getY();
         this.z = pos.getZ();
         this.access = ContainerLevelAccess.create(this.world, pos);
      }
   }

   @Override
   public boolean stillValid(Player player) {
      if (this.bound) {
         if (this.boundItemMatcher != null) {
            return this.boundItemMatcher.get();
         }

         if (this.boundBlockEntity != null) {
            return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
         }

         if (this.boundEntity != null) {
            return this.boundEntity.isAlive();
         }
      }

      return true;
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      return ItemStack.EMPTY;
   }

   public Map<Integer, Slot> get() {
      return this.customSlots;
   }

   public int shadowRank(int buttonId) {
      return buttonId >= 0 && buttonId < 14 ? this.data(0, buttonId) : 0;
   }

   public int shadowLevel(int buttonId) {
      return buttonId >= 0 && buttonId < 14 ? Math.max(1, joinedWords(this.data(1, buttonId), this.data(2, buttonId))) : 1;
   }

   public int rankXp(int buttonId) {
      return buttonId >= 0 && buttonId < 14 ? Math.max(0, joinedWords(this.data(3, buttonId), this.data(4, buttonId))) : 0;
   }

   public int rankXpNeeded(int buttonId) {
      return buttonId >= 0 && buttonId < 14 ? Math.max(1, joinedWords(this.data(5, buttonId), this.data(6, buttonId))) : 1;
   }

   public int nextRank(int buttonId) {
      return buttonId >= 0 && buttonId < 14 ? this.data(7, buttonId) : 0;
   }

   public boolean isAtLevelCap(int buttonId) {
      return this.hasFlag(buttonId, 1);
   }

   public boolean isMaxRank(int buttonId) {
      return this.hasFlag(buttonId, 2);
   }

   public boolean isCustomizable(int buttonId) {
      return this.hasFlag(buttonId, 4);
   }

   public boolean isEquipped(int buttonId) {
      return this.hasFlag(buttonId, 8);
   }

   public boolean isGrandMarshalEligible(int buttonId) {
      return this.hasFlag(buttonId, 16);
   }

   public boolean isGrandMarshalActive(int buttonId) {
      return this.hasFlag(buttonId, 32);
   }

   public boolean hasShadow(int buttonId) {
      return this.ownedCount(buttonId) > 0;
   }

   public int ownedCount(int buttonId) {
      return buttonId >= 0 && buttonId < 14 ? Math.max(0, this.data(9, buttonId)) : 0;
   }

   public int summonedCount(int buttonId) {
      return buttonId >= 0 && buttonId < 14 ? Math.min(this.ownedCount(buttonId), Math.max(0, this.data(10, buttonId))) : 0;
   }

   public String shadowCountText(int buttonId) {
      return this.summonedCount(buttonId) + "/" + this.ownedCount(buttonId);
   }

   public int bossHealingManaCost() {
      return Math.max(0, joinedWords(this.shadowData.get(154), this.shadowData.get(155)));
   }

   public int allHealingManaCost() {
      return Math.max(0, joinedWords(this.shadowData.get(156), this.shadowData.get(157)));
   }

   private ShadowMonarchManager.ShadowHealingQuote healingQuoteForData() {
      long now = this.world.getGameTime();
      if (this.healingQuoteTick != now) {
         this.healingQuoteTick = now;
         this.cachedHealingQuote = ShadowMonarchManager.healingQuote(this.entity);
      }

      return this.cachedHealingQuote;
   }

   private boolean hasFlag(int buttonId, int flag) {
      return buttonId >= 0 && buttonId < 14 && (this.data(8, buttonId) & flag) != 0;
   }

   private int data(int field, int buttonId) {
      return this.shadowData.get(field * 14 + buttonId);
   }

   private static int lowWord(int value) {
      return value & 65535;
   }

   private static int highWord(int value) {
      return value >>> 16 & 65535;
   }

   private static int joinedWords(int low, int high) {
      return low & 65535 | (high & 65535) << 16;
   }
}
