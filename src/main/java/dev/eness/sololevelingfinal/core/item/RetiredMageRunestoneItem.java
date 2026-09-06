package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public final class RetiredMageRunestoneItem extends Item {
   private final String formerSkill;

   public RetiredMageRunestoneItem(String formerSkill) {
      super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
      this.formerSkill = formerSkill;
   }

   @Override
   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("Retired Mage runestone").withStyle(ChatFormatting.DARK_GRAY));
      tooltip.add(Component.literal(this.formerSkill + " was not tied to a Mage specialization.").withStyle(ChatFormatting.GRAY));
      tooltip.add(Component.literal("This item no longer grants or casts a skill.").withStyle(ChatFormatting.RED));
   }
}
