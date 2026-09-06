package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.util.GateSpawnerUtil;

public class GetRandomPlayerProcedure {
   public static void execute(LevelAccessor world) {
      GateSpawnerUtil.spawnNearRandomOverworldPlayer(world);
   }
}
