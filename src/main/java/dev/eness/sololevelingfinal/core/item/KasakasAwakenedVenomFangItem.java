package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.procedures.KasakasVenomFangsLivingEntityIsHitWithToolProcedure;

public class KasakasAwakenedVenomFangItem extends SwordItem {
   public KasakasAwakenedVenomFangItem() {
      super(new Tier() {
         @Override
         public int getUses() {
            return 0;
         }

         @Override
         public float getSpeed() {
            return 4.0F;
         }

         @Override
         public float getAttackDamageBonus() {
            return 9.0F;
         }

         @Override
         public int getLevel() {
            return 1;
         }

         @Override
         public int getEnchantmentValue() {
            return 0;
         }

         @Override
         public Ingredient getRepairIngredient() {
            return Ingredient.of();
         }
      }, 3, -1.8F, new Properties());
   }

   @Override
   public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
      boolean retval = super.hurtEnemy(itemstack, entity, sourceentity);
      KasakasVenomFangsLivingEntityIsHitWithToolProcedure.execute(entity.level(), entity, sourceentity, 4);
      return retval;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§9Level Of Difficulty: §6S"));
      list.add(Component.literal("§9Type: §6Dagger"));
      list.add(Component.literal("§9Attack: §6+200"));
      list.add(
         Component.literal(
            "§9A DAGGER MADE FROM KASAKAS VENOM FANG. KASAKAS VENOM STILL REMAINS WITHIN IT, THUS CAUSING PARALYZATION AND BLEEDING WHEN USED UPON AN OPONENT."
         )
      );
      list.add(Component.literal("§9-Effect: \"§6PARALYZE\": §9Effective against B-rank targets and below."));
      list.add(Component.literal("§9-Effect: \"§6BLEED\": §9The oponent will be bleeding for certain rate."));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get());
   }
}
