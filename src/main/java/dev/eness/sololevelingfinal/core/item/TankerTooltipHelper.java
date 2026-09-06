package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

final class TankerTooltipHelper {
   private TankerTooltipHelper() {
   }

   static void addRunestone(List<Component> tooltip, String skill, String descriptionKey, String rank, int flatMana, String manaPercent, int cooldownSeconds) {
      tooltip.add(Component.translatable("tooltip.sololeveling.tanker.runestone.learn", skill).withStyle(ChatFormatting.GOLD));
      tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
      tooltip.add(Component.translatable("tooltip.sololeveling.tanker.runestone.rank", rank).withStyle(ChatFormatting.BLUE));
      tooltip.add(
         Component.translatable("tooltip.sololeveling.tanker.runestone.cadence", flatMana, manaPercent, cooldownSeconds).withStyle(ChatFormatting.DARK_GRAY)
      );
   }
}
