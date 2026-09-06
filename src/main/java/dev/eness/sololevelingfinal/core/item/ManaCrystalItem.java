package dev.eness.sololevelingfinal.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;

public abstract class ManaCrystalItem extends Item {
   private final int goldValue;

   protected ManaCrystalItem(Rarity rarity, int goldValue) {
      super(new Properties().stacksTo(64).rarity(rarity));
      this.goldValue = goldValue;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack stack) {
      return true;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide()) {
         return InteractionResultHolder.success(stack);
      }

      if (!SystemPlayerAccess.hasSystem(player)) {
         return InteractionResultHolder.fail(stack);
      }

      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.golds = capability.golds + this.goldValue;
         capability.syncPlayerVariables(player);
      });
      stack.shrink(1);
      player.displayClientMessage(Component.literal("+" + this.goldValue + " gold"), true);
      return InteractionResultHolder.consume(stack);
   }
}
