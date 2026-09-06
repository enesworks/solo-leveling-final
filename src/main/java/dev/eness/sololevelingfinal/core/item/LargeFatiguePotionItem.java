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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.procedures.PotionPlayerFinishesUsingItemProcedure;

public class LargeFatiguePotionItem extends Item {
   public LargeFatiguePotionItem() {
      super(new Properties().stacksTo(4).rarity(Rarity.RARE));
   }

   @Override
   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.DRINK;
   }

   @Override
   public int getUseDuration(ItemStack itemstack) {
      return 20;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return true;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§6Type: §cFatigue Potion"));
      list.add(Component.literal("§6Difficulty Level: §bA"));
      list.add(Component.literal("§6Restores a large portion of users fatigue after consuming."));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
      PotionPlayerFinishesUsingItemProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity, ar.getObject());
      return ar;
   }
}
