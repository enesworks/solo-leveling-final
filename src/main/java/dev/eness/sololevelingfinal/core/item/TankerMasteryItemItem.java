package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.procedures.TankerProgressionHelper;

public class TankerMasteryItemItem extends Item {
   public TankerMasteryItemItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.translatable("tooltip.sololeveling.tanker.mastery.grant").withStyle(ChatFormatting.GOLD));
      list.add(Component.translatable("tooltip.sololeveling.tanker.mastery.order").withStyle(ChatFormatting.GRAY));
      list.add(Component.translatable("tooltip.sololeveling.tanker.mastery.preserve").withStyle(ChatFormatting.DARK_GRAY));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      ItemStack stack = entity.getItemInHand(hand);
      if (!world.isClientSide()) {
         if (!(entity instanceof ServerPlayer player) || !TankerProgressionHelper.isTanker(player)) {
            entity.displayClientMessage(Component.translatable("message.sololeveling.tanker.mastery.wrong_class"), true);
            return InteractionResultHolder.fail(stack);
         }

         String granted = TankerProgressionHelper.grantNextMasterySkill(player);
         if (granted.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.sololeveling.tanker.mastery.complete"), false);
         } else {
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            player.displayClientMessage(Component.translatable("message.sololeveling.tanker.skill_gained", granted), false);
         }
      }

      return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
   }
}
