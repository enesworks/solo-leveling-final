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
import dev.eness.sololevelingfinal.core.procedures.RunestoneOverhealRCProcedure;

public class RunestoneOverhealItem extends Item {
   public RunestoneOverhealItem() {
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
      list.add(Component.literal("Right click this rune to obtain \"Overheal\""));
      list.add(Component.literal("User can cast \"Overheal\" which will add absorbtion to caster and allies"));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
      RunestoneOverhealRCProcedure.execute(entity, ar.getObject());
      return ar;
   }
}
