package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class FangedKasakaOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 99, false, false));
         }

         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            entity.getPersistentData().putDouble("IA", entity.getPersistentData().getDouble("IA") + 1.0);
            entity.lookAt(
               Anchor.EYES,
               new Vec3(
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY(),
                  (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
               )
            );
         } else {
            entity.getPersistentData().putDouble("IA", 0.0);
         }

         if (entity.getPersistentData().getDouble("IA") == 19.0) {
            if (Math.sqrt(
                  Math.pow((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX() - entity.getX(), 2.0)
                     + Math.pow((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() - entity.getY(), 2.0)
                     + Math.pow((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ() - entity.getZ(), 2.0)
               )
               <= 16.0) {
               entity.getPersistentData().putString("state", "melee");
            } else {
               entity.getPersistentData().putString("state", "ranged");
            }
         }

         if (entity.getPersistentData().getDouble("IA") >= 210.0) {
            entity.getPersistentData().putDouble("IA", 0.0);
         }

         if (entity.getPersistentData().getString("state").equals("melee")) {
            FangedKasakaCloseRangeProcedure.execute(world, entity);
         }

         if (entity.getPersistentData().getString("state").equals("ranged")) {
            FangedKasakaLongRangeProcedure.execute(world, x, y, z, entity);
         }
      }
   }
}
