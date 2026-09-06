package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.util.daily.DailyPunishmentManager;

@EventBusSubscriber
public final class SurvivalDimensionTickUpdateProcedure {
   private SurvivalDimensionTickUpdateProcedure() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         DailyPunishmentManager.tick(player);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof ServerPlayer player && !world.isClientSide()) {
         DailyPunishmentManager.tick(player);
      }
   }
}
