package dev.eness.sololevelingfinal.core.item;

import io.netty.buffer.Unpooled;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import dev.eness.sololevelingfinal.core.item.inventory.SelectionSpecialBoxInventoryCapability;
import dev.eness.sololevelingfinal.core.world.inventory.SelectionBoxGUIMenu;

public class SelectionSpecialBoxItem extends Item {
   public SelectionSpecialBoxItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return true;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, final Player entity, final InteractionHand hand) {
      InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
      if (entity instanceof ServerPlayer serverPlayer) {
         NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
               return Component.literal("Selection Special Box");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
               FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
               packetBuffer.writeBlockPos(entity.blockPosition());
               packetBuffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
               return new SelectionBoxGUIMenu(id, inventory, packetBuffer);
            }
         }, buf -> {
            buf.writeBlockPos(entity.blockPosition());
            buf.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
         });
      }

      return ar;
   }

   @Override
   public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag compound) {
      return new SelectionSpecialBoxInventoryCapability();
   }

   @Override
   public CompoundTag getShareTag(ItemStack stack) {
      CompoundTag nbt = stack.getOrCreateTag();
      stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(capability -> nbt.put("Inventory", ((ItemStackHandler)capability).serializeNBT()));
      return nbt;
   }

   @Override
   public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
      super.readShareTag(stack, nbt);
      if (nbt != null) {
         stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
            .ifPresent(capability -> ((ItemStackHandler)capability).deserializeNBT((CompoundTag)nbt.get("Inventory")));
      }
   }
}
