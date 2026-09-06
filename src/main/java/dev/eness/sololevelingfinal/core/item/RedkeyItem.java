package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.procedures.DemonKingsCastleKeyUseProcedure;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;

public class RedkeyItem extends Item {
   private static final String OWNER_UUID_TAG = "dkc_key_owner_uuid";
   private static final String OWNER_NAME_TAG = "dkc_key_owner_name";

   public RedkeyItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return true;
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("A key carved from a dying throne's shadow.").withStyle(ChatFormatting.DARK_PURPLE));
      list.add(Component.literal("Consumed when it opens the Demon King's Castle.").withStyle(ChatFormatting.DARK_RED));
      list.add(Component.literal("Afterward, return through the System's DKC path.").withStyle(ChatFormatting.DARK_PURPLE));
      list.add(Component.literal("The castle remembers every hand that opens it.").withStyle(ChatFormatting.GRAY));
      if (itemstack.hasTag() && itemstack.getTag().hasUUID("dkc_key_owner_uuid")) {
         list.add(Component.literal("Bound to: " + itemstack.getTag().getString("dkc_key_owner_name")).withStyle(ChatFormatting.DARK_GRAY));
      }
   }

   @Override
   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.BOW;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
      ItemStack stack = entity.getItemInHand(hand);
      if (!world.isClientSide() && !bindOrVerifyOwner(stack, entity)) {
         entity.displayClientMessage(Component.literal("§4The key rejects your hand. It belongs to " + getOwnerName(stack) + "."), true);
         return InteractionResultHolder.fail(stack);
      }

      if (!world.isClientSide()) {
         DkcQuestManager.unlock(entity);
      }

      DemonKingsCastleKeyUseProcedure.execute(world, entity, stack);
      return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
   }

   @Override
   public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(itemstack, world, entity, slot, selected);
      if (!world.isClientSide() && entity instanceof Player player) {
         bindOrVerifyOwner(itemstack, player);
         DkcQuestManager.unlock(player);
      }
   }

   public static boolean bindOrVerifyOwner(ItemStack stack, Player player) {
      if (!stack.isEmpty() && stack.getItem() instanceof RedkeyItem) {
         if (!stack.getOrCreateTag().hasUUID("dkc_key_owner_uuid")) {
            stack.getOrCreateTag().putUUID("dkc_key_owner_uuid", player.getUUID());
            stack.getOrCreateTag().putString("dkc_key_owner_name", player.getName().getString());
            return true;
         } else {
            return stack.getOrCreateTag().getUUID("dkc_key_owner_uuid").equals(player.getUUID());
         }
      } else {
         return true;
      }
   }

   public static String getOwnerName(ItemStack stack) {
      return stack.hasTag() && stack.getTag().contains("dkc_key_owner_name") ? stack.getTag().getString("dkc_key_owner_name") : "another hunter";
   }
}
