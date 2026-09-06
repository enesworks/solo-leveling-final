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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmeraldDaggerItem extends SwordItem {
   public EmeraldDaggerItem() {
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
            return 8.0F;
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
      }, 3, -2.3F, new Properties().fireResistant());
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§9Level Of Difficulty: §fA"));
      list.add(Component.literal("§9Type: §fDAGGER"));
      list.add(Component.literal("§9Attack: §f+125"));
      list.add(
         Component.literal(
            "§9\"A BLADE FORGED FROM CRYSTALLIZED MOONLIGHT, SHIMMERING WITH ETHEREAL BRILLIANCE. IN THE HANDS OF A SKILLED HUNTER, IT BENDS LIGHT ITSELF, STRIKING FROM UNSEEN ANGLES AND LEAVING ONLY THE GLOW OF ITS AFTERIMAGE"
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return true;
   }
}
