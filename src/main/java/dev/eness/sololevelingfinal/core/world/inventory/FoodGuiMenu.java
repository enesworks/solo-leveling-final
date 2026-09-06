package dev.eness.sololevelingfinal.core.world.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import dev.eness.sololevelingfinal.core.init.SololevelingModMenus;
import dev.eness.sololevelingfinal.core.procedures.FoodGuiWhileThisGUIIsOpenTickProcedure;

@EventBusSubscriber
public class FoodGuiMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
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

   public FoodGuiMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
      super(SololevelingModMenus.FOOD_GUI.get(), id);
      this.entity = inv.player;
      this.world = inv.player.level();
      this.internal = new ItemStackHandler(3);
      BlockPos pos = null;
      if (extraData != null) {
         pos = extraData.readBlockPos();
         this.x = pos.getX();
         this.y = pos.getY();
         this.z = pos.getZ();
         this.access = ContainerLevelAccess.create(this.world, pos);
      }

      if (pos != null) {
         if (extraData.readableBytes() == 1) {
            byte hand = extraData.readByte();
            ItemStack itemstack = hand == 0 ? this.entity.getMainHandItem() : this.entity.getOffhandItem();
            this.boundItemMatcher = () -> itemstack == (hand == 0 ? this.entity.getMainHandItem() : this.entity.getOffhandItem());
            itemstack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> {
               this.internal = capability;
               this.bound = true;
            });
         } else if (extraData.readableBytes() > 1) {
            extraData.readByte();
            this.boundEntity = this.world.getEntity(extraData.readVarInt());
            if (this.boundEntity != null) {
               this.boundEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> {
                  this.internal = capability;
                  this.bound = true;
               });
            }
         } else {
            this.boundBlockEntity = this.world.getBlockEntity(pos);
            if (this.boundBlockEntity != null) {
               this.boundBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> {
                  this.internal = capability;
                  this.bound = true;
               });
            }
         }
      }

      this.customSlots.put(1, this.addSlot(new SlotItemHandler(this.internal, 1, -53, -84) {
         private final int slot = 1;

         @Override
         public boolean mayPickup(Player entity) {
            return false;
         }

         @Override
         public boolean mayPlace(ItemStack itemstack) {
            return false;
         }
      }));
      this.customSlots.put(0, this.addSlot(new SlotItemHandler(this.internal, 0, -53, -57) {
         private final int slot = 0;

         @Override
         public boolean mayPickup(Player entity) {
            return false;
         }

         @Override
         public boolean mayPlace(ItemStack itemstack) {
            return false;
         }
      }));
      this.customSlots.put(2, this.addSlot(new SlotItemHandler(this.internal, 2, -53, -30) {
         private final int slot = 2;

         @Override
         public boolean mayPickup(Player entity) {
            return false;
         }

         @Override
         public boolean mayPlace(ItemStack itemstack) {
            return false;
         }
      }));

      for (int si = 0; si < 3; si++) {
         for (int sj = 0; sj < 9; sj++) {
            this.addSlot(new Slot(inv, sj + (si + 1) * 9, -79 + sj * 18, 1 + si * 18));
         }
      }

      for (int si = 0; si < 9; si++) {
         this.addSlot(new Slot(inv, si, -79 + si * 18, 59));
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
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (index < 3) {
            if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
            if (index < 30) {
               if (!this.moveItemStackTo(itemstack1, 30, this.slots.size(), true)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 3, 30, false)) {
               return ItemStack.EMPTY;
            }

            return ItemStack.EMPTY;
         }

         if (itemstack1.getCount() == 0) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(playerIn, itemstack1);
      }

      return itemstack;
   }

   @Override
   protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
      boolean flag = false;
      int i = p_38905_;
      if (p_38907_) {
         i = p_38906_ - 1;
      }

      if (p_38904_.isStackable()) {
         while (!p_38904_.isEmpty() && (p_38907_ ? i >= p_38905_ : i < p_38906_)) {
            Slot slot = this.slots.get(i);
            ItemStack itemstack = slot.getItem();
            if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameTags(p_38904_, itemstack)) {
               int j = itemstack.getCount() + p_38904_.getCount();
               int maxSize = Math.min(slot.getMaxStackSize(), p_38904_.getMaxStackSize());
               if (j <= maxSize) {
                  p_38904_.setCount(0);
                  itemstack.setCount(j);
                  slot.set(itemstack);
                  flag = true;
               } else if (itemstack.getCount() < maxSize) {
                  p_38904_.shrink(maxSize - itemstack.getCount());
                  itemstack.setCount(maxSize);
                  slot.set(itemstack);
                  flag = true;
               }
            }

            if (p_38907_) {
               i--;
            } else {
               i++;
            }
         }
      }

      if (!p_38904_.isEmpty()) {
         if (p_38907_) {
            i = p_38906_ - 1;
         } else {
            i = p_38905_;
         }

         while (p_38907_ ? i >= p_38905_ : i < p_38906_) {
            Slot slot1 = this.slots.get(i);
            ItemStack itemstack1 = slot1.getItem();
            if (itemstack1.isEmpty() && slot1.mayPlace(p_38904_)) {
               if (p_38904_.getCount() > slot1.getMaxStackSize()) {
                  slot1.setByPlayer(p_38904_.split(slot1.getMaxStackSize()));
               } else {
                  slot1.setByPlayer(p_38904_.split(p_38904_.getCount()));
               }

               slot1.setChanged();
               flag = true;
               break;
            }

            if (p_38907_) {
               i--;
            } else {
               i++;
            }
         }
      }

      return flag;
   }

   @Override
   public void removed(Player playerIn) {
      super.removed(playerIn);
      if (!this.bound && playerIn instanceof ServerPlayer serverPlayer) {
         if (serverPlayer.isAlive() && !serverPlayer.hasDisconnected()) {
            for (int i = 0; i < this.internal.getSlots(); i++) {
               if (i != 0 && i != 2) {
                  playerIn.getInventory().placeItemBackInInventory(this.internal.extractItem(i, this.internal.getStackInSlot(i).getCount(), false));
               }
            }
         } else {
            for (int j = 0; j < this.internal.getSlots(); j++) {
               if (j != 0 && j != 2) {
                  playerIn.drop(this.internal.extractItem(j, this.internal.getStackInSlot(j).getCount(), false), false);
               }
            }
         }
      }
   }

   public Map<Integer, Slot> get() {
      return this.customSlots;
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      Player entity = event.player;
      if (event.phase == Phase.END && entity.containerMenu instanceof FoodGuiMenu) {
         Level world = entity.level();
         double x = entity.getX();
         double y = entity.getY();
         double z = entity.getZ();
         FoodGuiWhileThisGUIIsOpenTickProcedure.execute(entity);
      }
   }
}
