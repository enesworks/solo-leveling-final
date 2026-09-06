package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.ShadowStepEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class LllRightclickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
            >= 100.0) {
            if (!CooldownManager.isOnCooldown(entity, "Shadowstep")) {
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .MP
                  - 100.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.MP = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.AFTER_IMAGE.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                  }
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.5F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")), SoundSource.NEUTRAL, 1.0F, 1.5F, false
                     );
                  }
               }

               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 1, false, false));
               }

               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.NO_FALL_DAMAGE.get(), 9999, 1, false, false));
               }

               CooldownManager.set(entity, "mana_refresh", 40);
               CooldownManager.set(entity, "Shadowstep", 40);
               Entity _shootFrom = entity;
               Level projectileLevel = _shootFrom.level();
               if (!projectileLevel.isClientSide()) {
                  Projectile _entityToSpawn = (new Object() {
                     public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                        AbstractArrow entityToSpawn = new ShadowStepEntity(SololevelingModEntities.SHADOW_STEP.get(), level);
                        entityToSpawn.setOwner(shooter);
                        entityToSpawn.setBaseDamage(damage);
                        entityToSpawn.setKnockback(knockback);
                        entityToSpawn.setSilent(true);
                        return entityToSpawn;
                     }
                  }).getArrow(projectileLevel, entity, (float)(1.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 45.0), 0);
                  _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
                  _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 3.0F, 0.0F);
                  projectileLevel.addFreshEntity(_entityToSpawn);
               }
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Not enough MP!"), true);
         }
      }
   }
}
