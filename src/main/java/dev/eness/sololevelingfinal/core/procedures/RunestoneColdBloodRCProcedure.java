package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.SkillListHelper;

public class RunestoneColdBloodRCProcedure {
   public static void execute(Entity entity, ItemStack itemstack) {
      if (entity != null) {
         if (!SkillListHelper.skills(entity).contains("Cold Blood")) {
            String updated = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
               + "Cold Blood,";
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Plist = updated;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player player) {
               player.getInventory().clearOrCountMatchingItems(p -> itemstack.getItem() == p.getItem(), 1, player.inventoryMenu.getCraftSlots());
            }

            if (entity instanceof Player player && !player.level().isClientSide()) {
               player.displayClientMessage(Component.literal("Learned \"Cold Blood\""), true);
            }
         } else if (entity instanceof Player player && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("You already have this skill!"), false);
         }
      }
   }
}
