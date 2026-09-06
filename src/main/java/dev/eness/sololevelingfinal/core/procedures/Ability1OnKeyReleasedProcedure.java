package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.BeastMonarchManager;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.FrostMonarchManager;
import dev.eness.sololevelingfinal.core.util.GoliathCombatManager;
import dev.eness.sololevelingfinal.core.util.LiuZhigangCombatManager;

public class Ability1OnKeyReleasedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!FrostMonarchManager.isDirectAbilityMode(entity)) {
            if (BeastMonarchManager.isFangStance(entity)) {
               BeastMonarchManager.releasePredatorsIntercept(entity, 0);
            } else if (GoliathCombatManager.isCombatStance(entity)) {
               GoliathCombatManager.releasePursuit(entity, 0);
            } else if (LiuZhigangCombatManager.isCombatStance(entity)) {
               LiuZhigangCombatManager.releaseDragonFlash(entity, 0);
            } else if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).combatmode
               )
             {
               double _setval = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.firecharge = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof LivingEntity _entity) {
                  _entity.removeEffect(SololevelingModMobEffects.USING_FIRE.get());
               }

               CooldownManager.clear(entity, "mana_refresh");
               boolean _setvalx = false;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.monarchbeam = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
                  == 2.0) {
                  CooldownManager.set(entity, "job_1", 20);
                  FireChargeInitialReleaseProcedure.execute(world, x, y, z, entity);
               }

               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
                  == 4.0) {
                  CooldownManager.set(entity, "job_1", 60);
               }
            }
         }
      }
   }
}
