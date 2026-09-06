package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class StormBurstProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         CooldownManager.set(entity, "job_3", 200);
         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12.5), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entity != entityiterator) {
               if (entity != entityiterator && !(entityiterator instanceof ExperienceOrb) && !(entityiterator instanceof ItemEntity)) {
                  entityiterator.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC), entity),
                     (float)(10.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 5.0)
                  );
               }

               entityiterator.setDeltaMovement(
                  new Vec3(
                     (entityiterator.getX() - entity.getX())
                        * (
                           2.0
                              / Math.sqrt(
                                 Math.pow(entityiterator.getX() - entity.getX(), 2.0)
                                    + Math.pow(entityiterator.getY() - entity.getY(), 2.0)
                                    + Math.pow(entityiterator.getZ() - entity.getZ(), 2.0)
                              )
                        ),
                     0.5,
                     (entityiterator.getZ() - entity.getZ())
                        * (
                           2.0
                              / Math.sqrt(
                                 Math.pow(entityiterator.getX() - entity.getX(), 2.0)
                                    + Math.pow(entityiterator.getY() - entity.getY(), 2.0)
                                    + Math.pow(entityiterator.getZ() - entity.getZ(), 2.0)
                              )
                        )
                  )
               );
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(
                     SololevelingModParticleTypes.WHITE_FLAMES.get(), x, y, z, 250, 2.0, 2.0, 2.0, Mth.nextDouble(RandomSource.create(), 0.0, 5.0)
                  );
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 250, 1.0, 1.0, 1.0, Mth.nextDouble(RandomSource.create(), 0.0, 2.5));
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 2, 1.0, 1.0, 1.0, 0.0);
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
            }
         }
      }
   }
}
