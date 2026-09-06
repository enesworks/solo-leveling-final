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
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import dev.eness.sololevelingfinal.core.init.SololevelingModMenus;

public class PathMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
   private static final long VALID_FLOOR_MASK = 1048575L;
   public static final HashMap<String, Object> guistate = new HashMap<>();
   public final Level world;
   public final Player entity;
   public int x;
   public int y;
   public int z;
   private final PathMenu.TowerState towerState;
   private ContainerLevelAccess access = ContainerLevelAccess.NULL;
   private IItemHandler internal;
   private final Map<Integer, Slot> customSlots = new HashMap<>();
   private boolean bound = false;
   private Supplier<Boolean> boundItemMatcher = null;
   private Entity boundEntity = null;
   private BlockEntity boundBlockEntity = null;

   public PathMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
      this(id, inv, readOpeningData(extraData));
   }

   public PathMenu(int id, Inventory inv, BlockPos position, PathMenu.TowerState towerState) {
      this(id, inv, new PathMenu.OpeningData(position, towerState));
   }

   private PathMenu(int id, Inventory inv, PathMenu.OpeningData openingData) {
      super(SololevelingModMenus.PATH.get(), id);
      this.entity = inv.player;
      this.world = inv.player.level();
      this.internal = new ItemStackHandler(0);
      BlockPos pos = openingData.position();
      this.x = pos.getX();
      this.y = pos.getY();
      this.z = pos.getZ();
      this.towerState = openingData.towerState();
      this.access = ContainerLevelAccess.create(this.world, pos);
   }

   public static void writeOpeningData(FriendlyByteBuf buffer, BlockPos position, PathMenu.TowerState towerState) {
      PathMenu.TowerState state = towerState == null ? PathMenu.TowerState.EMPTY : towerState;
      buffer.writeBlockPos(position == null ? BlockPos.ZERO : position);
      buffer.writeLong(state.unlockedFloorsMask());
      buffer.writeLong(state.generatedFloorsMask());
      buffer.writeLong(state.armedTransitionsMask());
      buffer.writeVarInt(state.clearedFloors());
      buffer.writeVarInt(state.currentFloor());
      buffer.writeBoolean(state.insideDkc());
      buffer.writeBoolean(state.conquered());
      buffer.writeBoolean(state.hasRadiruCastleAccess());
      buffer.writeBoolean(state.radiruPact());
      buffer.writeBoolean(state.radiruSlaughtered());
   }

   private static PathMenu.OpeningData readOpeningData(FriendlyByteBuf buffer) {
      if (buffer == null) {
         return new PathMenu.OpeningData(BlockPos.ZERO, PathMenu.TowerState.EMPTY);
      }

      BlockPos position = buffer.readBlockPos();
      PathMenu.TowerState state = new PathMenu.TowerState(
         buffer.readLong(),
         buffer.readLong(),
         buffer.readLong(),
         buffer.readVarInt(),
         buffer.readVarInt(),
         buffer.readBoolean(),
         buffer.readBoolean(),
         buffer.readBoolean(),
         buffer.readBoolean(),
         buffer.readBoolean()
      );
      return new PathMenu.OpeningData(position, state);
   }

   public long unlockedFloorsMask() {
      return this.towerState.unlockedFloorsMask();
   }

   public long generatedFloorsMask() {
      return this.towerState.generatedFloorsMask();
   }

   public long armedTransitionsMask() {
      return this.towerState.armedTransitionsMask();
   }

   public int clearedFloors() {
      return this.towerState.clearedFloors();
   }

   public int currentFloor() {
      return this.towerState.currentFloor();
   }

   public int highestUnlockedFloor() {
      return this.towerState.highestUnlockedFloor();
   }

   public boolean insideDkc() {
      return this.towerState.insideDkc();
   }

   public boolean conquered() {
      return this.towerState.conquered();
   }

   public boolean hasRadiruCastleAccess() {
      return this.towerState.hasRadiruCastleAccess();
   }

   public boolean radiruPact() {
      return this.towerState.radiruPact();
   }

   public boolean radiruSlaughtered() {
      return this.towerState.radiruSlaughtered();
   }

   public boolean isFloorUnlocked(int floor) {
      return this.towerState.isFloorUnlocked(floor);
   }

   public boolean isFloorGenerated(int floor) {
      return this.towerState.isFloorGenerated(floor);
   }

   public boolean isTransitionArmed(int floor) {
      return this.towerState.isTransitionArmed(floor);
   }

   public boolean isFloorCleared(int floor) {
      return this.towerState.isFloorCleared(floor);
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

   private record OpeningData(BlockPos position, PathMenu.TowerState towerState) {
      private OpeningData {
         position = position == null ? BlockPos.ZERO : position;
         towerState = towerState == null ? PathMenu.TowerState.EMPTY : towerState;
      }
   }

   public record TowerState(
      long unlockedFloorsMask,
      long generatedFloorsMask,
      long armedTransitionsMask,
      int clearedFloors,
      int currentFloor,
      boolean insideDkc,
      boolean conquered,
      boolean hasRadiruCastleAccess,
      boolean radiruPact,
      boolean radiruSlaughtered
   ) {
      public static final PathMenu.TowerState EMPTY = new PathMenu.TowerState(0L, 0L, 0L, 0, 0, false, false, false, false, false);

      public TowerState {
         unlockedFloorsMask &= 1048575L;
         generatedFloorsMask &= 1048575L;
         armedTransitionsMask &= 1048575L;
         clearedFloors = Math.max(0, Math.min(20, clearedFloors));
         currentFloor = Math.max(0, Math.min(20, currentFloor));
      }

      public boolean isFloorUnlocked(int floor) {
         return hasFloor(this.unlockedFloorsMask, floor);
      }

      public boolean isFloorGenerated(int floor) {
         return hasFloor(this.generatedFloorsMask, floor);
      }

      public boolean isTransitionArmed(int floor) {
         return hasFloor(this.armedTransitionsMask, floor);
      }

      public boolean isFloorCleared(int floor) {
         return floor >= 1 && floor <= this.clearedFloors;
      }

      public int highestUnlockedFloor() {
         return this.unlockedFloorsMask == 0L ? 0 : 64 - Long.numberOfLeadingZeros(this.unlockedFloorsMask);
      }

      private static boolean hasFloor(long mask, int floor) {
         return floor >= 1 && floor <= 20 && (mask & 1L << floor - 1) != 0L;
      }
   }
}
