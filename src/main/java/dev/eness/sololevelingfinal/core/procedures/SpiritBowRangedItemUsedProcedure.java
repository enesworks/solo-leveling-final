package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import dev.eness.sololevelingfinal.core.entity.ManaArrowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class SpiritBowRangedItemUsedProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _entity) {
            _entity.swing(InteractionHand.MAIN_HAND, true);
         }

         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
               == 6.0
            && !CooldownManager.isOnCooldown(entity, "mana_refresh")
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
               >= 100.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 0.5) {
            CooldownManager.set(entity, "mana_refresh", 20);
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .MP
               - (100.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 0.5);
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
            Entity _shootFrom = entity;
            Level projectileLevel = _shootFrom.level();
            if (!projectileLevel.isClientSide()) {
               Projectile _entityToSpawn = (new Object() {
                  public Projectile getArrow(Level level, Entity shooter, float damage, int knockback, byte piercing) {
                     AbstractArrow entityToSpawn = new ManaArrowEntity(SololevelingModEntities.MANA_ARROW.get(), level);
                     entityToSpawn.setOwner(shooter);
                     entityToSpawn.setBaseDamage(damage);
                     entityToSpawn.setKnockback(knockback);
                     entityToSpawn.setSilent(true);
                     entityToSpawn.setPierceLevel(piercing);
                     return entityToSpawn;
                  }
               }).getArrow(projectileLevel, entity, (float)(3.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 45.0), 0, (byte)1);
               _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
               _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 2.0F, 0.0F);
               projectileLevel.addFreshEntity(_entityToSpawn);
            }
         }
      }
   }
}
