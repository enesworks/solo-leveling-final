package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public class IntelligenceUpdateProcedure {
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
         if (entity instanceof Player player && player.isCreative()) {
            if (world.getLevelData().getGameTime() % 20L == 0L) {
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  if (capability.Mana != 1000000.0 || capability.MP != 1000000.0) {
                     capability.Mana = 1000000.0;
                     capability.MP = 1000000.0;
                     capability.syncPlayerVariables(entity);
                  }
               });
            }
         } else {
            if (world.getLevelData().getGameTime() % 20L == 0L) {
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  double mana = 1000.0 + 100.0 * TemporaryStatBonusManager.effectiveIntelligence(entity);
                  if (capability.Mana != mana) {
                     capability.Mana = mana;
                     capability.syncPlayerVariables(entity);
                  }
               });
            }
         }
      }
   }
}
