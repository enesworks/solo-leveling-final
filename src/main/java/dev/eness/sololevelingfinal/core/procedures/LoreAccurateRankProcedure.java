package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class LoreAccurateRankProcedure {
   @SubscribeEvent
   public static void onWorldTick(LevelTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.level);
      }
   }

   public static void execute(LevelAccessor world) {
      execute(null, world);
   }

   private static void execute(@Nullable Event event, LevelAccessor world) {
      if (world.getLevelData().getGameTime() % 100L == 0L) {
         if (world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.SOLO_LEVELING_LORE_ACCURATE_RANKS)) {
            for (Entity entityiterator : new ArrayList<>(world.players())) {
               double _setval = 2.0;
               entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.LoreAccurateRankStart = _setval;
                  capability.syncPlayerVariables(entityiterator);
               });
            }
         } else {
            for (Entity entityiterator : new ArrayList<>(world.players())) {
               double _setval = 2.0;
               entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.LoreAccurateRankStart = _setval;
                  capability.syncPlayerVariables(entityiterator);
               });
            }
         }
      }
   }
}
