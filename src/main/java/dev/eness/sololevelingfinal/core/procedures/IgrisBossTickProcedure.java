package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class IgrisBossTickProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         rand = Math.random();
         if (!((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) instanceof Player)
            && entity instanceof LivingEntity _entity
            && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false));
         }
      }
   }
}
