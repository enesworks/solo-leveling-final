package dev.eness.sololevelingfinal.core.procedures;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.RangerCombatManager;

public final class MasterylvlupRangerProcedure {
   private static final List<String> ORDER = List.of("Back Step", "Hawkeye", "Rapid Fire", "High Value Target", "Sharpshooter", "Arrow Shower", "Hyper Focus");

   private MasterylvlupRangerProcedure() {
   }

   public static void execute(Entity entity) {
      if (entity instanceof ServerPlayer player) {
         RangerCombatManager.reconcileRanger(player);

         for (String skill : ORDER) {
            if (!RangerCombatManager.hasSkill(player, skill)) {
               if (RangerCombatManager.grantSkill(player, skill)) {
                  player.displayClientMessage(Component.translatable("message.sololeveling.ranger.skill_gained", skill), false);
               }

               return;
            }
         }
      }
   }
}
