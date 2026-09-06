package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.guild.GuildBuffManager;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class XpmultiplierProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player);
      }
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (world.dayTime() % 100L == 0L) {
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               double xpMultiplier = GuildBuffManager.xpMultiplier(entity);
               if (capability.GuildCode != 0.0 || capability.xpmultiplier != xpMultiplier || capability.manaregen != 0.0) {
                  capability.GuildCode = 0.0;
                  capability.xpmultiplier = xpMultiplier;
                  capability.manaregen = 0.0;
                  capability.syncPlayerVariables(entity);
               }
            });
         }
      }
   }
}
