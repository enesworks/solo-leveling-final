package dev.eness.sololevelingfinal.core.mixins;

import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.util.BeastMonarchManager;
import dev.eness.sololevelingfinal.core.util.GoliathCombatManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemInHandLayer.class)
public abstract class GoliathThirdPersonHandMixin {
   @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
   private ItemStack sololeveling$hideGoliathMainHand(ItemStack stack, LivingEntity entity) {
      return !GoliathCombatManager.isCombatStance(entity) && !BeastMonarchManager.isFangStance(entity) ? stack : ItemStack.EMPTY;
   }
}
