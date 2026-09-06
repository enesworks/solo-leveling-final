package dev.eness.sololevelingfinal.core.util;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class VesselManaScaling {
   private static final double REFERENCE_STRENGTH = 50.0;
   private static final double CURVE_POWER = 0.9;
   private static final double MAX_COST_BONUS = 4.0;

   private VesselManaScaling() {
   }

   public static int strengthScaledCost(Entity entity, int baseCost, double scalingWeight) {
      double strength = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .map(variables -> Math.max(0.0, variables.Strength))
         .orElse(0.0);
      double pressure = Math.pow(strength / 50.0, 0.9);
      double bonus = Math.min(4.0, pressure * Math.max(0.0, scalingWeight));
      double scaled = Math.ceil(Math.max(1, baseCost) * (1.0 + bonus));
      return (int)Math.min(2.147483647E9, scaled);
   }
}
