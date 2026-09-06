package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

public class KnightKillerItem extends SwordItem {
   public KnightKillerItem() {
      super(new Tier() {
         @Override
         public int getUses() {
            return 100;
         }

         @Override
         public float getSpeed() {
            return 4.0F;
         }

         @Override
         public float getAttackDamageBonus() {
            return 6.0F;
         }

         @Override
         public int getLevel() {
            return 1;
         }

         @Override
         public int getEnchantmentValue() {
            return 2;
         }

         @Override
         public Ingredient getRepairIngredient() {
            return Ingredient.of();
         }
      }, 3, -2.1F, new Properties());
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("Level of Difficulty: §1B"));
      list.add(Component.literal("Type: Dagger"));
      list.add(Component.literal("Attack §9+75"));
      list.add(Component.literal("A dagger designed to fight against armored enemies. Deals an extra +25% damage against armored opponents. "));
   }

   @Override
   public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
      return super.onEntitySwing(itemstack, entity);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get());
   }
}
