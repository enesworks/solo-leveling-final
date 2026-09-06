package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.entity.HomingFlameArrowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class HomingArrowProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         Entity _shootFrom = entity;
         Level projectileLevel = _shootFrom.level();
         if (!projectileLevel.isClientSide()) {
            Projectile _entityToSpawn = (new Object() {
               public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                  AbstractArrow entityToSpawn = new HomingFlameArrowEntity(SololevelingModEntities.HOMING_FLAME_ARROW.get(), level);
                  entityToSpawn.setOwner(shooter);
                  entityToSpawn.setBaseDamage(damage);
                  entityToSpawn.setKnockback(knockback);
                  entityToSpawn.setSilent(true);
                  entityToSpawn.setSecondsOnFire(100);
                  entityToSpawn.setCritArrow(true);
                  return entityToSpawn;
               }
            }).getArrow(projectileLevel, entity, (float)(3.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 22.0), 0);
            _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
            _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 2.0F, 0.0F);
            projectileLevel.addFreshEntity(_entityToSpawn);
         }
      }
   }
}
