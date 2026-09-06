package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.PortalJobChangeEntity;

public class PortalJobChangeRightClickedOnEntityProcedure {
   public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
      if (entity instanceof PortalJobChangeEntity portal && sourceentity instanceof ServerPlayer player) {
         String owner = portal.getEntityData().get(PortalJobChangeEntity.DATA_person_to_enter);
         if (owner.isBlank() || owner.equals(player.getStringUUID())) {
            if (JobChangeQuestEntryProcedure.execute(world, player) && !portal.level().isClientSide()) {
               portal.discard();
            }
         }
      }
   }
}
