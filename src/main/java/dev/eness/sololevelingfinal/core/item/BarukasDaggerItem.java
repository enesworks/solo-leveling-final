package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class BarukasDaggerItem extends SwordItem {
   public BarukasDaggerItem() {
      super(new Tier() {
         @Override
         public int getUses() {
            return 0;
         }

         @Override
         public float getSpeed() {
            return 4.0F;
         }

         @Override
         public float getAttackDamageBonus() {
            return 7.0F;
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
      }, 3, -2.0F, new Properties());
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§6LEVEL OF DIFFICULTY: A"));
      list.add(Component.literal("§6TYPE: DAGGER"));
      list.add(Component.literal("§6ATTACK: +110"));
      list.add(Component.literal("§6AGILITY: +10"));
      list.add(Component.literal("§6THIS DAGGER WAS ONCE USED BY WARLORD \"BARUKA\" "));
      list.add(Component.literal("§6THIS WEAPON IS INFUSED WITH MAGIC THAT REDUCES THE WEIGHT OF ITS WIELDER"));
      list.add(Component.literal("§6ALLOWING FOR GREATER AGILITY"));
   }
}
