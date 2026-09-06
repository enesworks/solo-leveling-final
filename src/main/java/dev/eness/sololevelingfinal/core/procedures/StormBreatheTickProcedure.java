package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.WhiteFlameEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public class StormBreatheTickProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player.getX(), event.player.getY(), event.player.getZ(), event.player);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).monarchbeam
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
               >= 10.0 + 10.0 * (TemporaryStatBonusManager.effectiveIntelligence(entity) / 25.0)) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .MP
               - (10.0 + 10.0 * (TemporaryStatBonusManager.effectiveIntelligence(entity) / 25.0));
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.SCREEN_SHAKE.get(), 5, 1, false, false));
            }

            for (int index0 = 0; index0 < Mth.nextInt(RandomSource.create(), 12, 16); index0++) {
               if (world instanceof ServerLevel projectileLevel) {
                  Projectile _entityToSpawn = (new Object() {
                     public Projectile getArrow(Level level, Entity shooter, float damage, int knockback, byte piercing) {
                        AbstractArrow entityToSpawn = new WhiteFlameEntity(SololevelingModEntities.WHITE_FLAME.get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        entityToSpawn.setKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        entityToSpawn.setPierceLevel(piercing);
                        return entityToSpawn;
                     }
                  }).getArrow(projectileLevel, entity, (float)(5.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 50.0), 0, (byte)1);
                  _entityToSpawn.setPos(
                     x + Mth.nextDouble(RandomSource.create(), -0.75, 0.75),
                     y + 1.6 + Mth.nextDouble(RandomSource.create(), -0.75, 0.75),
                     z + Mth.nextDouble(RandomSource.create(), -0.75, 0.75)
                  );
                  _entityToSpawn.shoot(entity.getLookAngle().x, entity.getLookAngle().y, entity.getLookAngle().z, 2.5F, 5.0F);
                  projectileLevel.addFreshEntity(_entityToSpawn);
               }
            }
         }
      }
   }
}
