package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class SpiritBowItem extends BowItem {
   public SpiritBowItem() {
      super(new Properties().stacksTo(1).durability(1024).rarity(Rarity.RARE));
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.translatable("tooltip.sololeveling.spirit_bow.mana"));
      list.add(Component.translatable("tooltip.sololeveling.spirit_bow.lore"));
   }
}
