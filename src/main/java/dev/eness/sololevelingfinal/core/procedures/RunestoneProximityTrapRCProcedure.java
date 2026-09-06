package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.util.RangerCombatManager;

public final class RunestoneProximityTrapRCProcedure {
   private RunestoneProximityTrapRCProcedure() {
   }

   public static void execute(Entity entity, ItemStack itemStack) {
      if (entity instanceof ServerPlayer player) {
         if (!RangerCombatManager.grantSkill(player, "Arrow Shower")) {
            player.displayClientMessage(Component.translatable("message.sololeveling.ranger.skill_known"), false);
         } else {
            if (!player.isCreative()) {
               itemStack.shrink(1);
            }

            player.displayClientMessage(Component.translatable("message.sololeveling.ranger.skill_gained", "Arrow Shower"), false);
         }
      }
   }
}
