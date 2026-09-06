package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;

public class StormGriamoreItem extends Item {
   public StormGriamoreItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get());
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§6LEVEL OF DIFFICULTY: S"));
      list.add(Component.literal("§6TYPE: MAGIC BOOK"));
      list.add(Component.literal("§6ATTACK: +225"));
      list.add(
         Component.literal(
            "§6A MYSTICAL TOME THAT COMMANDS STORMS AND LIGHTNING, UNLEASHING THE FURY OF THE SKIES WITH EVERY PAGE TURNED. WIELD ITS POWER, BUT BEWARE—IT MAY DRAW YOU INTO THE STORM'S CHAOTIC EMBRACE"
         )
      );
      list.add(Component.literal("§6PASSIVE \"TEMPEST AUTHORITY\": WHILE HELD, THE TOME DRAWS ON PERMANENT INTELLIGENCE TO STRENGTHEN THE WIELDER."));
      list.add(Component.literal(""));
   }
}
