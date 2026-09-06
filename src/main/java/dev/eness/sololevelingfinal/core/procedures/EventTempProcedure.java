package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class EventTempProcedure {
   @SubscribeEvent
   public static void onBlockBreak(BreakEvent event) {
      execute(event, event.getLevel(), event.getPlayer());
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (DkcFloorRegistry.isDkc(world) && entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            if (event != null && event.isCancelable()) {
               event.setCanceled(true);
            }
         } else {
            if (world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.DISABLE_BLOCK_BREAKING)
               && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).dungeoning
               && event != null
               && event.isCancelable()) {
               event.setCanceled(true);
            }
         }
      }
   }
}
