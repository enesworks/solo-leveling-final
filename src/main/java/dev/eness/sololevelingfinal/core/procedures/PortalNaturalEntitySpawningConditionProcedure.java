package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.RedGateEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class PortalNaturalEntitySpawningConditionProcedure {
   public static boolean execute(LevelAccessor world, Entity entity) {
      if (entity == null) {
         return false;
      }

      double rand = 0.0;
      if (world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.SOLO_GATE_SPAWNING)
         && (world instanceof Level _lvl ? _lvl.dimension() : Level.OVERWORLD) == Level.OVERWORLD
         && SololevelingModVariables.MapVariables.get(world).gatetimer >= world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_GATE_DELAY)) {
         rand = Mth.nextInt(RandomSource.create(), 1, 12);
         if (rand == 7.0) {
            if (entity instanceof RedGateEntity) {
               if (!SololevelingModVariables.MapVariables.get(world).RedGate) {
                  return true;
               }

               return false;
            }

            return true;
         }
      }

      return false;
   }
}
