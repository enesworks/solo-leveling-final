package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class ShadowFormationCastProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, String skill) {
      if (entity != null) {
         boolean success = ShadowMonarchManager.castFormation(world, entity, skill);
         if (!success && entity instanceof Player player && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("No shadows found for that formation."), true);
         }
      }
   }
}
