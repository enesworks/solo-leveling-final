package dev.eness.sololevelingfinal.core.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.util.LiuManifestationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class LiuManifestedSwordMenuMixin {
   @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
   private void sololeveling$lockManifestedSword(int slotId, int button, ClickType clickType, Player player, CallbackInfo callback) {
      AbstractContainerMenu menu = (AbstractContainerMenu)this;
      ItemStack clicked = ItemStack.EMPTY;
      if (slotId >= 0 && slotId < menu.slots.size()) {
         Slot slot = menu.getSlot(slotId);
         clicked = slot == null ? ItemStack.EMPTY : slot.getItem();
      }

      boolean swappingManifestedHotbar = clickType == ClickType.SWAP
         && button >= 0
         && button < player.getInventory().getContainerSize()
         && LiuManifestationManager.isManifestedSword(player.getInventory().getItem(button));
      boolean collectingWhileManifested = clickType == ClickType.PICKUP_ALL && LiuManifestationManager.isActive(player);
      if (LiuManifestationManager.isManifestedSword(menu.getCarried())
         || LiuManifestationManager.isManifestedSword(clicked)
         || swappingManifestedHotbar
         || collectingWhileManifested) {
         callback.cancel();
         LiuManifestationManager.rejectContainerMove(player);
         menu.broadcastChanges();
      }
   }
}
