package dev.eness.sololevelingfinal.core.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

public class WarAxeItem extends Item {
   public WarAxeItem() {
      super(new Properties().durability(0));
   }

   @Override
   public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
      return 1.0F;
   }

   @Override
   public boolean mineBlock(ItemStack itemstack, Level world, BlockState blockstate, BlockPos pos, LivingEntity entity) {
      itemstack.hurtAndBreak(1, entity, i -> i.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      return true;
   }

   @Override
   public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
      itemstack.hurtAndBreak(2, entity, i -> i.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      return true;
   }

   @Override
   public int getEnchantmentValue() {
      return 2;
   }

   @Override
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
      if (equipmentSlot == EquipmentSlot.MAINHAND) {
         Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
         builder.putAll(super.getDefaultAttributeModifiers(equipmentSlot));
         builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 12.2F, Operation.ADDITION));
         builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.2, Operation.ADDITION));
         return builder.build();
      } else {
         return super.getDefaultAttributeModifiers(equipmentSlot);
      }
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§6RARITY: S"));
      list.add(Component.literal("§6ATTACK: +400"));
      list.add(Component.literal("§6TYPE: AXE"));
      list.add(
         Component.literal(
            "§6THIS HAMMER WAS PREVIOUSLY USED BY GIANTS BUT THEN IT'S SIZE REDUCED BY THE HELP OF THE DRARWES TO A LEVEL WHERE HUMANS CAN USE IT."
         )
      );
      list.add(Component.literal("§6BUT STILL THERE ARE VERY LESS CHOOSEN PEOPLE WHO CAN LIFT IT"));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get());
   }
}
