package dev.eness.sololevelingfinal.core.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.procedures.KamishWrathEntitySwingsItemProcedure;
import dev.eness.sololevelingfinal.core.procedures.KamishWrathHasItemGlowingEffectProcedure;

public class KamishWrath2Item extends Item {
   public KamishWrath2Item() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   @Override
   public float getDestroySpeed(ItemStack par1ItemStack, BlockState par2Block) {
      return 1.9F;
   }

   @Override
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
      if (equipmentSlot == EquipmentSlot.MAINHAND) {
         Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
         builder.putAll(super.getDefaultAttributeModifiers(equipmentSlot));
         builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Item modifier", 12.0, Operation.ADDITION));
         builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Item modifier", -2.4, Operation.ADDITION));
         return builder.build();
      } else {
         return super.getDefaultAttributeModifiers(equipmentSlot);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      Entity entity = Minecraft.getInstance().player;
      return KamishWrathHasItemGlowingEffectProcedure.execute(entity);
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§6LEVEL OF DIFFICULTY: ??"));
      list.add(Component.literal("§6TYPE: DAGGER"));
      list.add(Component.literal("§6ATTACK: UNSTABLE"));
      list.add(
         Component.literal(
            "§6THE MOST POWERFUL DAGGER FORGED BY A MASTER CRAFTSMAN USING THE SHARPEST FANG OF A DRAGON. ITS SHARPNESS IS SECOND TO NONE, AND ITS MANA SENSITIVITY IS EXCEPTIONAL."
         )
      );
      list.add(
         Component.literal(
            "§6PASSIVE \"MANA SENSITIVITY\": EACH HELD FANG COMBINES THE WIELDER'S PERMANENT STRENGTH AND INTELLIGENCE INTO A SCALING STRENGTH BONUS."
         )
      );
   }

   @Override
   public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
      boolean retval = super.onEntitySwing(itemstack, entity);
      KamishWrathEntitySwingsItemProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
      return retval;
   }
}
