package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.procedures.PotionPlayerFinishesUsingItemProcedure;

public class HolyWaterOfLifeItem extends Item {
   public HolyWaterOfLifeItem() {
      super(new Properties().stacksTo(6).rarity(Rarity.EPIC));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return true;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§6ITEM CLASS: S"));
      list.add(Component.literal("§6TYPE: CONSUMABLE"));
      list.add(
         Component.literal(
            "§6A MYSTERIOUS POTION THAT CAN CURE ANY DISEASE WITH POWERFUL MAGIC. THE EFFECT WILL ONLY TAKE PLACE WHEN THE ENTIRE BOTTLE IS BEEN CONSUMED"
         )
      );
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
      PotionPlayerFinishesUsingItemProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity, ar.getObject());
      return ar;
   }
}
