package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class FireChargeInitialReleaseProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FireVar
            == 0.0) {
            FireReleaseSpreadProcedure.execute(world, x, y, z, entity);
            CooldownManager.set(entity, "mana_refresh", 120);
            CooldownManager.set(entity, "job_1", 180);
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FireVar
            == 1.0) {
            double _setval = entity.getX();
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.FX = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = entity.getY();
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.FY = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = entity.getZ();
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.FZ = _setval;
               capability.syncPlayerVariables(entity);
            });
            boolean _setvalx = true;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.FRing = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
                  > 0.0
               && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
                  <= 20.0) {
               _setval = 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.firestr = _setval;
                  capability.syncPlayerVariables(entity);
               });
               CooldownManager.set(entity, "mana_refresh", 270);
               CooldownManager.set(entity, "job_1", 300);
               _setval = 120.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.FireRingTimer = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
                  > 20.0
               && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
                  <= 40.0) {
               CooldownManager.set(entity, "mana_refresh", 350);
               _setval = 2.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.firestr = _setval;
                  capability.syncPlayerVariables(entity);
               });
               CooldownManager.set(entity, "job_1", 360);
               _setval = 150.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.FireRingTimer = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
                  > 40.0
               && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
                  <= 60.0) {
               CooldownManager.set(entity, "job_1", 420);
               CooldownManager.set(entity, "mana_refresh", 400);
               _setval = 3.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.firestr = _setval;
                  capability.syncPlayerVariables(entity);
               });
               _setval = 200.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.FireRingTimer = _setval;
                  capability.syncPlayerVariables(entity);
               });
            }
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).FireVar
            == 2.0) {
            CooldownManager.set(entity, "job_1", 240);
            FireReleaseBeamProcedure.execute(world, x, y, z, entity);
            CooldownManager.set(entity, "mana_refresh", 200);
         }
      }
   }
}
