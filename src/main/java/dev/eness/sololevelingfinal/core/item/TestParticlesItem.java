package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.procedures.TestParticlesEntitySwingsItemProcedure;
import dev.eness.sololevelingfinal.core.procedures.TestParticlesItemInHandTickProcedure;

public class TestParticlesItem extends Item {
   public TestParticlesItem() {
      super(new Properties().stacksTo(64).rarity(Rarity.EPIC));
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
   public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
      boolean retval = super.onEntitySwing(itemstack, entity);
      TestParticlesEntitySwingsItemProcedure.execute(entity.level(), entity);
      return retval;
   }

   @Override
   public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(itemstack, world, entity, slot, selected);
      if (selected) {
         TestParticlesItemInHandTickProcedure.execute();
      }
   }
}
