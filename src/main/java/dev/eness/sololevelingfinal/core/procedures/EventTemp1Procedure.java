package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class EventTemp1Procedure {
   @SubscribeEvent
   public static void onBlockPlace(EntityPlaceEvent event) {
      execute(event, event.getLevel(), event.getEntity());
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (DkcFloorRegistry.isDkc(world) && entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            if (!(event instanceof EntityPlaceEvent placeEvent && placeEvent.getPlacedBlock().is(SololevelingModBlocks.FROST_CAUSEWAY.get()))) {
               if (event != null && event.isCancelable()) {
                  event.setCanceled(true);
               }
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
