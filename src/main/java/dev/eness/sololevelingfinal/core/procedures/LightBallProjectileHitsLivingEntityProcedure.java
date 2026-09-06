package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.util.MageCombatHelper;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class LightBallProjectileHitsLivingEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity immediatesourceentity, Entity sourceentity) {
      if (entity != null && immediatesourceentity != null && sourceentity != null) {
         if (sourceentity != entity) {
            if (world instanceof Level _level && !_level.isClientSide()) {
               _level.explode(
                  sourceentity,
                  new DamageSource(
                     world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), immediatesourceentity, sourceentity
                  ),
                  null,
                  x,
                  y,
                  z,
                  3.0F,
                  false,
                  ExplosionInteraction.NONE
               );
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                  );
               }
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 3, 0.2, 0.2, 0.2, 1.0);
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(SololevelingModParticleTypes.FIRE_PARTICLE.get(), x, y, z, 3, 0.2, 0.2, 0.2, 1.0);
            }

            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               MageCombatHelper.hurt(world, sourceentity, entityiterator, (float)(4.0 + TemporaryStatBonusManager.effectiveIntelligence(sourceentity) / 20.0));
            }

            if (!immediatesourceentity.level().isClientSide()) {
               immediatesourceentity.discard();
            }
         }
      }
   }
}
