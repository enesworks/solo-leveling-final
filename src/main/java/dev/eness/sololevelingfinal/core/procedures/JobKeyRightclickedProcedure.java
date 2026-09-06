package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

public class JobKeyRightclickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof ServerPlayer player) {
         if (!JobChangeQuestManager.isOverworld(player)) {
            player.displayClientMessage(Component.literal("§cThe Job Change Quest can only be unlocked in the Minecraft Overworld."), true);
         } else if (JobChangeQuestManager.isFinished(player)) {
            player.displayClientMessage(Component.literal("§5Job Change Quest is already complete."), true);
         } else if (JobChangeQuestManager.unlock(player)) {
            ItemStack key = new ItemStack(SololevelingModItems.JOB_KEY.get());
            player.getInventory().clearOrCountMatchingItems(stack -> key.getItem() == stack.getItem(), 1, player.inventoryMenu.getCraftSlots());
            player.displayClientMessage(Component.literal("§5Job Change Quest unlocked in System > Quests."), true);
         }
      }
   }
}
