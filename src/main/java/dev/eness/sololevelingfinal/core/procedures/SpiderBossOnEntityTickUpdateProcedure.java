package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.SpiderBossEntity;
import dev.eness.sololevelingfinal.core.entity.SpiderWebEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SpiderBossOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double rand = 0.0;
         if (world.getLevelData().getGameTime() % 20L == 0L) {
            rand = Math.random();
            if (rand > 0.9
               && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).spiderstat
               && (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) instanceof Player) {
               boolean _setval = true;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.spiderstat = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof SpiderBossEntity) {
                  ((SpiderBossEntity)entity).setAnimation("web shoot");
               }

               Entity _shootFrom = entity;
               Level projectileLevel = _shootFrom.level();
               if (!projectileLevel.isClientSide()) {
                  Projectile _entityToSpawn = (new Object() {
                     public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new SpiderWebEntity(SololevelingModEntities.SPIDER_WEB.get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        entityToSpawn.setKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                     }
                  }).getArrow(projectileLevel, entity, 5.0F, 0);
                  _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
                  _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 2.0F, 0.0F);
                  projectileLevel.addFreshEntity(_entityToSpawn);
               }
            }
         }
      }
   }
}
