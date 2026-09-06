package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class SurviveConProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : entity.level().dimension() == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:survival_dimension"));
   }
}
