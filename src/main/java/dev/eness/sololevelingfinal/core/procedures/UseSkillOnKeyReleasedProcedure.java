package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ArcaneMageSpellManager;
import dev.eness.sololevelingfinal.core.util.AssassinSkillManager;
import dev.eness.sololevelingfinal.core.util.BarrierMageSpellManager;
import dev.eness.sololevelingfinal.core.util.FireMageSpellManager;
import dev.eness.sololevelingfinal.core.util.JobSkillManager;
import dev.eness.sololevelingfinal.core.util.MageQTEHelper;
import dev.eness.sololevelingfinal.core.util.MageQTEState;
import dev.eness.sololevelingfinal.core.util.MageSpellProgression;
import dev.eness.sololevelingfinal.core.util.OrbOfAvariceManager;
import dev.eness.sololevelingfinal.core.util.QTEResult;
import dev.eness.sololevelingfinal.core.util.StormMageSpellManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class UseSkillOnKeyReleasedProcedure {
   public static void execute(LevelAccessor world, Entity entity, int pressedMs) {
      if (entity != null) {
         if (entity instanceof LivingEntity _entity) {
            _entity.removeEffect(SololevelingModMobEffects.CONSECUTIVE_SLASHES.get());
         }

         double _setval = 0.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.firecharge = _setval;
            capability.syncPlayerVariables(entity);
         });
         String power = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).PselectedPower;
         if (!JobSkillManager.release(entity, power, pressedMs)) {
            if (!AssassinSkillManager.isReworkedSkill(power)) {
               if (MageQTEHelper.MAGE_SKILLS.contains(power)) {
                  boolean authorized = MageSpellProgression.canCastLearnedSkill(entity, power);
                  if (world instanceof Level clientLevel && clientLevel.isClientSide()) {
                     DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                        MageQTEState state = MageQTEState.INSTANCE;
                        if (state.isActive()) {
                           float zoneStartx = state.getGoodZoneStart();
                           state.endQTE();
                           QTEResult clientResult = MageQTEHelper.computeResult(zoneStartx, pressedMs);
                           state.showResult(clientResult);
                        }
                     });
                  }

                  if (world instanceof Level _lvl && !_lvl.isClientSide()) {
                     boolean qteStarted = entity.getPersistentData().getBoolean("mage_casting");
                     entity.getPersistentData().putBoolean("mage_casting", false);
                     if (authorized && qteStarted) {
                        float zoneStart = entity.getPersistentData().contains("mage_qte_zone_start")
                           ? entity.getPersistentData().getFloat("mage_qte_zone_start")
                           : MageQTEHelper.computeZoneStart(entity);
                        castMageSpellWithQTE(world, entity, power, pressedMs, zoneStart);
                     }

                     entity.getPersistentData().remove("mage_qte_zone_start");
                  }
               } else {
                  if (power.equals("Critical Attack") && entity.getPersistentData().getBoolean("Critical_Attack_Targetting")) {
                     CriticalAttackUseProcedure.execute(world, entity);
                     entity.getPersistentData().putBoolean("Critical_Attack_Targetting", false);
                     entity.getPersistentData().putString("CriticalAttackTarget", "");
                  }

                  if (power.equals("Mutilation") && entity.getPersistentData().getBoolean("Mutilation_Targetting")) {
                     MutilationUseProcedure.execute(world, entity);
                     entity.getPersistentData().putBoolean("Mutilation_Targetting", false);
                     entity.getPersistentData().putString("MutilationTarget", "");
                  }

                  entity.getPersistentData().putBoolean("Critical_Attack_Targetting", false);
                  entity.getPersistentData().putBoolean("Mutilation_Targetting", false);
                  entity.getPersistentData().putString("CriticalAttackTarget", "");
               }
            }
         }
      }
   }

   private static void castMageSpellWithQTE(LevelAccessor world, Entity entity, String power, int pressedMs, float zoneStart) {
      SololevelingModVariables.PlayerVariables cap = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      QTEResult result = MageQTEHelper.computeResult(zoneStart, pressedMs);
      double mult = MageQTEHelper.getManaCostMultiplier(result, TemporaryStatBonusManager.effectiveIntelligence(entity));
      double effectiveMult = mult * OrbOfAvariceManager.manaCostMultiplier(entity);
      boolean cast = false;
      if (FireMageSpellManager.isQteSkill(power)) {
         cast = FireMageSpellManager.cast(entity, power, result);
      } else if (BarrierMageSpellManager.isQteSkill(power)) {
         cast = BarrierMageSpellManager.cast(entity, power, result);
      } else if (ArcaneMageSpellManager.isQteSkill(power)) {
         cast = ArcaneMageSpellManager.cast(entity, power, result);
      } else if (StormMageSpellManager.isQteSkill(power)) {
         cast = StormMageSpellManager.cast(entity, power, result);
      }

      if (cast && entity instanceof Player _player && !_player.level().isClientSide()) {
         String manaPercent = String.format("%.0f", effectiveMult * 100.0);

         String msg = switch (result) {
            case PERFECT -> "§bPERFECT! §7(×" + manaPercent + "% mana)";
            case GOOD -> "§eGOOD! §7(×" + manaPercent + "% mana)";
            case MISS -> "§cMISS! §7(×" + manaPercent + "% mana)";
         };
         _player.displayClientMessage(Component.literal(msg), true);
      }
   }
}
