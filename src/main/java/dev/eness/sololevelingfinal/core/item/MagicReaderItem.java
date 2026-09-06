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
import dev.eness.sololevelingfinal.core.procedures.MagicReaderRightclickedProcedure;

public class MagicReaderItem extends Item {
   public MagicReaderItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("Right Click to gates to determine their Rank!"));
      list.add(Component.literal("Right Click to see the gate Locations!"));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
      MagicReaderRightclickedProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity);
      return ar;
   }
}
