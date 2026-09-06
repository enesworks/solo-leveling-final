package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.party.PartyService;

public final class PartyMembersProcedure {
   private PartyMembersProcedure() {
   }

   public static void execute(LevelAccessor world, Entity entity) {
      if (entity instanceof ServerPlayer player) {
         PartyService.legacyOpen(player);
      }
   }
}
