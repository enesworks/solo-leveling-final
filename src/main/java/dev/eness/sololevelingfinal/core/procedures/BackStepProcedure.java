package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class BackStepProcedure {
   public static boolean execute(Entity entity) {
      if (entity == null) {
         return false;
      }

      if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).rangerleapnum
         > 0.0) {
         double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .rangerleapnum
            - 1.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.rangerleapnum = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, false, false));
         }

         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.NO_FALL_DAMAGE.get(), 80, 0, false, false));
         }

         Vec3 look = entity.getLookAngle();
         Vec3 retreat = new Vec3(-look.x, 0.0, -look.z);
         if (retreat.lengthSqr() > 1.0E-4) {
            retreat = retreat.normalize().scale(1.25);
         }

         entity.setDeltaMovement(retreat.x, entity.onGround() ? 0.28 : 0.08, retreat.z);
         entity.hurtMarked = true;
         return true;
      } else {
         if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Back Step has no charges"), true);
         }

         return false;
      }
   }
}
