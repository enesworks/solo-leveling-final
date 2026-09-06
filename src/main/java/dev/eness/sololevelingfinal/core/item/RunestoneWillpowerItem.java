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
import dev.eness.sololevelingfinal.core.procedures.RunestoneWillpowerRCProcedure;

public class RunestoneWillpowerItem extends Item {
   public RunestoneWillpowerItem() {
      super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return true;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      TankerTooltipHelper.addRunestone(list, "Willpower", "tooltip.sololeveling.tanker.runestone.willpower.description", "A", 650, "9", 45);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      ItemStack stack = entity.getItemInHand(hand);
      if (!world.isClientSide()) {
         RunestoneWillpowerRCProcedure.execute(entity, stack);
      }

      return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
   }
}
