package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.procedures.KatanaStierRightclickedProcedure;

public class KatanaStierItem extends SwordItem {
   public KatanaStierItem() {
      super(new Tier() {
         @Override
         public int getUses() {
            return 1600;
         }

         @Override
         public float getSpeed() {
            return 4.0F;
         }

         @Override
         public float getAttackDamageBonus() {
            return 10.0F;
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
            return Ingredient.of(new ItemStack(Items.NETHERITE_INGOT));
         }
      }, 3, -2.6F, new Properties());
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
      KatanaStierRightclickedProcedure.execute(world, entity, ar.getObject());
      return ar;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§6LEVEL OF DIFFICULTY: S"));
      list.add(Component.literal("§6TYPE: SWORD"));
      list.add(Component.literal("§6ATTACK +800"));
      list.add(Component.literal("§6A KATANA FORGED BY MOST SKILLED DWARVES. THIS SWORD CAN INFLICT \"BLEED\" UPON DAMAGE"));
   }
}
