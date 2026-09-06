package dev.eness.sololevelingfinal.core.item.inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.ItemStackHandler;
import dev.eness.sololevelingfinal.core.client.gui.ChooseClassScreen;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

@EventBusSubscriber(Dist.CLIENT)
public class ClassChooserInventoryCapability implements ICapabilitySerializable<CompoundTag> {
   private final LazyOptional<ItemStackHandler> inventory = LazyOptional.of(this::createItemHandler);

   @SubscribeEvent
   @OnlyIn(Dist.CLIENT)
   public static void onItemDropped(ItemTossEvent event) {
      if (event.getEntity().getItem().getItem() == SololevelingModItems.CLASS_CHOOSER.get() && Minecraft.getInstance().screen instanceof ChooseClassScreen) {
         Minecraft.getInstance().player.closeContainer();
      }
   }

   @Override
   public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
      return capability == ForgeCapabilities.ITEM_HANDLER ? this.inventory.cast() : LazyOptional.empty();
   }

   public CompoundTag serializeNBT() {
      return this.getItemHandler().serializeNBT();
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.getItemHandler().deserializeNBT(nbt);
   }

   private ItemStackHandler createItemHandler() {
      return new ItemStackHandler(9) {
         @Override
         public int getSlotLimit(int slot) {
            return 64;
         }

         @Override
         public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() != SololevelingModItems.CLASS_CHOOSER.get();
         }

         @Override
         public void setSize(int size) {
         }
      };
   }

   private ItemStackHandler getItemHandler() {
      return this.inventory.orElseThrow(RuntimeException::new);
   }
}
