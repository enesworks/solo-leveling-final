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
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.MasterylvlupMageProcedure;

public class MageMasteryItemItem extends Item {
   public MageMasteryItemItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("+1 Mage mastery level"));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      ItemStack stack = entity.getItemInHand(hand);
      double playerClass = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables())
         .Classes;
      if (playerClass != 2.0) {
         if (!world.isClientSide()) {
            entity.displayClientMessage(Component.literal("Only a Mage can use Mage mastery."), true);
         }

         return InteractionResultHolder.fail(stack);
      } else {
         if (!world.isClientSide()) {
            MasterylvlupMageProcedure.execute(entity);
         }

         return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
      }
   }
}
