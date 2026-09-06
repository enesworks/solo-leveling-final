package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class WillPowerEffectExpiresProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         entity.hurt(
            new DamageSource(
               world.registryAccess()
                  .registryOrThrow(Registries.DAMAGE_TYPE)
                  .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:will_power_feedback")))
            ),
            (float)(
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).wp / 4.0
            )
         );
         double _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.wp = _setval;
            capability.syncPlayerVariables(entity);
         });
         _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).progression_tanker
            + Math.round(
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).wp / 8.0
            );
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.progression_tanker = _setval;
            capability.syncPlayerVariables(entity);
         });
      }
   }
}
