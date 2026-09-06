package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.procedures.InstanceDungeonKeyEntitySwingsItemProcedure;
import dev.eness.sololevelingfinal.core.procedures.InstanceDungeonKeyRightclickedOnBlockProcedure;

public class InstanceDungeonKeyItem extends Item {
   public InstanceDungeonKeyItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.RARE));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return true;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("Teleports player to a dungeon."));
      list.add(Component.literal("Find the enterance of the subway station to enter the dungeon."));
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      super.useOn(context);
      InstanceDungeonKeyRightclickedOnBlockProcedure.execute(
         context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getPlayer()
      );
      return InteractionResult.SUCCESS;
   }

   @Override
   public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
      boolean retval = super.onEntitySwing(itemstack, entity);
      InstanceDungeonKeyEntitySwingsItemProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
      return retval;
   }
}
