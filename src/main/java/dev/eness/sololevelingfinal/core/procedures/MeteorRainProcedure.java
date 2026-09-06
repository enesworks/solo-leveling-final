package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import dev.eness.sololevelingfinal.core.SololevelingMod;

public class MeteorRainProcedure {
   public static void execute(LevelAccessor world, double y, Entity entity) {
      if (entity != null) {
         double delay = 0.0;
         double chn = 0.0;
         if (entity instanceof LivingEntity _entity) {
            _entity.swing(InteractionHand.MAIN_HAND, true);
         }

         entity.getPersistentData().putDouble("range", 3.0);
         entity.getPersistentData().putDouble("sx", entity.getX());
         entity.getPersistentData().putDouble("sy", entity.getY() + 1.2);
         entity.getPersistentData().putDouble("sz", entity.getZ());
         entity.getPersistentData()
            .putDouble(
               "tx",
               entity.level()
                  .clip(
                     new ClipContext(
                        entity.getEyePosition(1.0F), entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)), Block.OUTLINE, Fluid.NONE, entity
                     )
                  )
                  .getBlockPos()
                  .getX()
            );
         entity.getPersistentData()
            .putDouble(
               "ty",
               entity.level()
                  .clip(
                     new ClipContext(
                        entity.getEyePosition(1.0F), entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)), Block.OUTLINE, Fluid.NONE, entity
                     )
                  )
                  .getBlockPos()
                  .getY()
            );
         entity.getPersistentData()
            .putDouble(
               "tz",
               entity.level()
                  .clip(
                     new ClipContext(
                        entity.getEyePosition(1.0F), entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)), Block.OUTLINE, Fluid.NONE, entity
                     )
                  )
                  .getBlockPos()
                  .getZ()
            );
         entity.getPersistentData()
            .putDouble(
               "range",
               Math.sqrt(
                  Math.pow(entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx"), 2.0)
                     + Math.pow(entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty"), 2.0)
                     + Math.pow(entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz"), 2.0)
               )
            );
         entity.getPersistentData()
            .putDouble(
               "x+", (entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx")) / entity.getPersistentData().getDouble("range")
            );
         entity.getPersistentData()
            .putDouble(
               "y+", (entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty")) / entity.getPersistentData().getDouble("range")
            );
         entity.getPersistentData()
            .putDouble(
               "z+", (entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz")) / entity.getPersistentData().getDouble("range")
            );
         entity.getPersistentData().putDouble("size", 0.0);

         for (int index0 = 0; index0 < (int)(entity.getPersistentData().getDouble("range") * 0.4); index0++) {
            delay += 2.0;
            SololevelingMod.queueServerWork((int)delay, () -> {
               entity.getPersistentData().putDouble("size", entity.getPersistentData().getDouble("size") + 1.0);
               entity.getPersistentData().putDouble("sx", entity.getPersistentData().getDouble("sx") + entity.getPersistentData().getDouble("x+") * -3.0);
               entity.getPersistentData().putDouble("sy", entity.getPersistentData().getDouble("sy") + entity.getPersistentData().getDouble("y+") * -2.0);
               entity.getPersistentData().putDouble("sz", entity.getPersistentData().getDouble("sz") + entity.getPersistentData().getDouble("z+") * -3.0);
               if (world instanceof ServerLevel projectileLevel) {
                  Projectile _entityToSpawn = (new Object() {
                     public Projectile getFireball(Level level, Entity shooter, double ax, double ay, double az) {
                        AbstractHurtingProjectile entityToSpawn = new LargeFireball(EntityType.FIREBALL, level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.xPower = ax;
                        entityToSpawn.yPower = ay;
                        entityToSpawn.zPower = az;
                        return entityToSpawn;
                     }
                  }).getFireball(projectileLevel, entity, 0.0, -0.3, 0.0);
                  _entityToSpawn.setPos(entity.getPersistentData().getDouble("sx"), y + 8.0, entity.getPersistentData().getDouble("sz"));
                  _entityToSpawn.shoot(0.0, -1.0, 0.0, 1.0F, 0.0F);
                  projectileLevel.addFreshEntity(_entityToSpawn);
               }
            });
         }
      }
   }
}
