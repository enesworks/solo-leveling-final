package dev.eness.sololevelingfinal.core.world.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import dev.eness.sololevelingfinal.core.init.SololevelingModMenus;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class ShadowCustomizationMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
   public static final int EQUIPMENT_SLOT_X = 300;
   public static final int EQUIPMENT_SLOT_Y = 194;
   public static final int PLAYER_INVENTORY_X = 87;
   public static final int PLAYER_INVENTORY_Y = 216;
   public static final int PLAYER_HOTBAR_Y = 274;
   private static final int DATA_RANK = 0;
   private static final int DATA_LEVEL_LOW = 1;
   private static final int DATA_LEVEL_HIGH = 2;
   private static final int DATA_RANK_XP_LOW = 3;
   private static final int DATA_RANK_XP_HIGH = 4;
   private static final int DATA_RANK_XP_NEEDED_LOW = 5;
   private static final int DATA_RANK_XP_NEEDED_HIGH = 6;
   private static final int DATA_NEXT_RANK = 7;
   private static final int DATA_FLAGS = 8;
   private static final int DATA_COUNT = 9;
   private static final int FLAG_LEVEL_CAPPED = 1;
   private static final int FLAG_MAX_RANK = 2;
   public final Level world;
   public final Player entity;
   public final int x;
   public final int y;
   public final int z;
   private final String shadowType;
   private final ItemStackHandler equipment;
   private final ContainerData shadowData;
   private final Map<Integer, Slot> customSlots = new HashMap<>();
   private final int[] glowColors;
   private final boolean[] owned;
   private boolean loadingEquipment;

   public ShadowCustomizationMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
      super(SololevelingModMenus.SHADOW_CUSTOMIZATION.get(), id);
      this.entity = inventory.player;
      this.world = inventory.player.level();
      BlockPos position = extraData == null ? inventory.player.blockPosition() : extraData.readBlockPos();
      this.x = position.getX();
      this.y = position.getY();
      this.z = position.getZ();
      this.shadowType = extraData == null ? "" : extraData.readUtf(24);
      List<String> types = ShadowMonarchManager.customizableTypes();
      this.glowColors = new int[types.size()];
      this.owned = new boolean[types.size()];

      for (int index = 0; index < types.size(); index++) {
         this.owned[index] = extraData != null && extraData.readBoolean();
         this.glowColors[index] = extraData == null ? -1 : extraData.readInt();
      }

      this.loadingEquipment = true;
      this.equipment = new ItemStackHandler(1) {
         @Override
         public int getSlotLimit(int slot) {
            return 1;
         }

         @Override
         protected void onContentsChanged(int slot) {
            if (!ShadowCustomizationMenu.this.loadingEquipment && !ShadowCustomizationMenu.this.world.isClientSide()) {
               ShadowMonarchManager.setEquipmentForDisplay(
                  ShadowCustomizationMenu.this.entity, ShadowCustomizationMenu.this.shadowType, this.getStackInSlot(slot)
               );
            }
         }
      };
      if (!this.world.isClientSide()) {
         ShadowMonarchManager.prepareRosterForDisplay(this.entity);
         this.equipment.setStackInSlot(0, ShadowMonarchManager.equipmentForDisplay(this.entity, this.shadowType));
      }

      this.loadingEquipment = false;
      this.customSlots.put(0, this.addSlot(new SlotItemHandler(this.equipment, 0, 300, 194) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return ShadowMonarchManager.isValidBossEquipment(ShadowCustomizationMenu.this.shadowType, stack);
         }

         @Override
         public int getMaxStackSize() {
            return 1;
         }
      }));

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column + (row + 1) * 9, 87 + column * 18, 216 + row * 18));
         }
      }

      for (int column = 0; column < 9; column++) {
         this.addSlot(new Slot(inventory, column, 87 + column * 18, 274));
      }

      if (this.world.isClientSide()) {
         this.shadowData = new SimpleContainerData(9);
      } else {
         this.shadowData = new ContainerData() {
            @Override
            public int get(int index) {
               ShadowMonarchManager.ShadowDisplayProgress progress = ShadowMonarchManager.progressForDisplay(
                  ShadowCustomizationMenu.this.entity, ShadowCustomizationMenu.this.shadowType
               );

               return switch (index) {
                  case 0 -> progress.rank();
                  case 1 -> ShadowCustomizationMenu.lowWord(progress.level());
                  case 2 -> ShadowCustomizationMenu.highWord(progress.level());
                  case 3 -> ShadowCustomizationMenu.lowWord(progress.rankXp());
                  case 4 -> ShadowCustomizationMenu.highWord(progress.rankXp());
                  case 5 -> ShadowCustomizationMenu.lowWord(progress.rankXpNeeded());
                  case 6 -> ShadowCustomizationMenu.highWord(progress.rankXpNeeded());
                  case 7 -> progress.nextRank();
                  case 8 -> (progress.levelCapped() ? 1 : 0) | (progress.maxRank() ? 2 : 0);
                  default -> 0;
               };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
               return 9;
            }
         };
      }

      this.addDataSlots(this.shadowData);
   }

   @Override
   public boolean stillValid(Player player) {
      return player == this.entity
         && !this.shadowType.isEmpty()
         && (this.world.isClientSide() || ShadowMonarchManager.hasShadowForDisplay(player, this.shadowType));
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      if (index >= 0 && index < this.slots.size()) {
         Slot slot = this.slots.get(index);
         if (!slot.hasItem()) {
            return ItemStack.EMPTY;
         }

         ItemStack current = slot.getItem();
         ItemStack original = current.copy();
         if (index == 0) {
            if (!this.moveItemStackTo(current, 1, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!ShadowMonarchManager.isValidBossEquipment(this.shadowType, current) || !this.moveItemStackTo(current, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (current.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         return original;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public Map<Integer, Slot> get() {
      return this.customSlots;
   }

   public String shadowType() {
      return this.shadowType;
   }

   public int glowColor(int index) {
      return index >= 0 && index < this.glowColors.length ? this.glowColors[index] : -1;
   }

   public void setGlowColorLocal(int index, int color) {
      if (index >= 0 && index < this.glowColors.length) {
         this.glowColors[index] = color;
      }
   }

   public boolean ownsShadow(int index) {
      return index >= 0 && index < this.owned.length && this.owned[index];
   }

   public boolean supportsArtifact() {
      return ShadowMonarchManager.isCustomizableBoss(this.shadowType);
   }

   public int shadowRank() {
      return this.shadowData.get(0);
   }

   public int shadowLevel() {
      return Math.max(1, joinedWords(this.shadowData.get(1), this.shadowData.get(2)));
   }

   public int rankXp() {
      return Math.max(0, joinedWords(this.shadowData.get(3), this.shadowData.get(4)));
   }

   public int rankXpNeeded() {
      return Math.max(1, joinedWords(this.shadowData.get(5), this.shadowData.get(6)));
   }

   public int nextRank() {
      return this.shadowData.get(7);
   }

   public boolean isAtLevelCap() {
      return (this.shadowData.get(8) & 1) != 0;
   }

   public boolean isMaxRank() {
      return (this.shadowData.get(8) & 2) != 0;
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
