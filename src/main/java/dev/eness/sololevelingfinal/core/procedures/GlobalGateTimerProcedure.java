package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.DungeonBuilderMode;
import dev.eness.sololevelingfinal.core.util.GateSpawnerUtil;

@EventBusSubscriber
public class GlobalGateTimerProcedure {
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
      if (!DungeonBuilderMode.isActive(world)) {
         if (!world.isClientSide()
            && (world instanceof Level _lvl ? _lvl.dimension() : Level.OVERWORLD) == Level.OVERWORLD
            && world.getLevelData().getGameTime() % 20L == 0L) {
            if (SololevelingModVariables.MapVariables.get(world).gatetimer
               < world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_GATE_DELAY)) {
               SololevelingModVariables.MapVariables.get(world).gatetimer++;
               SololevelingModVariables.MapVariables.get(world).syncData(world);
            } else if (SololevelingModVariables.MapVariables.get(world).gatetimer
               >= world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_GATE_DELAY)) {
               GateSpawnerUtil.spawnNearRandomOverworldPlayer(world);
            }
         }
      }
   }
}
