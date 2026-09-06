package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;
import dev.eness.sololevelingfinal.core.util.VesselManager;

public class GrandMageRightclickedProcedure {
   public static void execute(Entity entity) {
      if (entity instanceof ServerPlayer player
         && VesselManager.assignPlayer(player, "ruler", "christopher_reed", true) == VesselManager.AssignmentResult.SUCCESS) {
         JobChangeQuestManager.finish(player);
      }
   }
}
