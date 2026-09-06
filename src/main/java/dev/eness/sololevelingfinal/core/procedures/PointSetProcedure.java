package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;

public class PointSetProcedure {
   public static void execute(Entity entity) {
      if (entity instanceof ServerPlayer player && player.server != null) {
         DkcRunSavedData.get(player.server).getOrCreate(player);
      }
   }
}
