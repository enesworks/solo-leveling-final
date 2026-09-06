package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class FreezeOnEffectActiveTickProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 99, false, false));
         }

         Entity _ent = entity;
         _ent.teleportTo(
            entity.getPersistentData().getDouble("FreezeX"), entity.getPersistentData().getDouble("FreezeY"), entity.getPersistentData().getDouble("FreezeZ")
         );
         if (_ent instanceof ServerPlayer _serverPlayer) {
            _serverPlayer.connection
               .teleport(
                  entity.getPersistentData().getDouble("FreezeX"),
                  entity.getPersistentData().getDouble("FreezeY"),
                  entity.getPersistentData().getDouble("FreezeZ"),
                  _ent.getYRot(),
                  _ent.getXRot()
               );
         }
      }
   }
}
