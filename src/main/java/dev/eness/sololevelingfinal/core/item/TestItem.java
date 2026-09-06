package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.procedures.TestItemEntitySwingsItemProcedure;
import dev.eness.sololevelingfinal.core.util.GateSpawnerUtil;

public class TestItem extends Item {
   public TestItem() {
      super(new Properties().stacksTo(64).rarity(Rarity.COMMON));
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
      GateSpawnerUtil.spawnNearRandomOverworldPlayer(world);
      return ar;
   }

   @Override
   public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
      boolean retval = super.onEntitySwing(itemstack, entity);
      TestItemEntitySwingsItemProcedure.execute(entity);
      return retval;
   }
}
