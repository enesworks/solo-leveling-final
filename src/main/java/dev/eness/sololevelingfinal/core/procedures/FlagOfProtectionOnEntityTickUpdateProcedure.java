package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class FlagOfProtectionOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double xRadius = 0.0;
         double loop = 0.0;
         double zRadius = 0.0;
         double particleAmount = 0.0;
         if (world.getLevelData().getGameTime() % 60L == 0L) {
            loop = 0.0;
            particleAmount = 20.0;
            xRadius = 5.0;
            zRadius = 5.0;

            while (loop < particleAmount) {
               world.addParticle(
                  ParticleTypes.SMALL_FLAME,
                  x + 0.5 + Math.cos((Math.PI * 2) / particleAmount * loop) * xRadius,
                  y,
                  z + 0.5 + Math.sin((Math.PI * 2) / particleAmount * loop) * zRadius,
                  0.0,
                  0.05,
                  0.0
               );
               loop++;
            }

            if ((entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null) != null) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(5.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if ((entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .party
                     .equals("")) {
                     if ((entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null) instanceof LivingEntity _entity
                        && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1));
                     }
                  } else if (entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .party
                        .equals(
                           (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
                                 .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .party
                        )
                     && entityiterator instanceof LivingEntity _entity
                     && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1));
                  }
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.FLAME, x, y, z, 3, 5.0, 5.0, 5.0, 1.0);
               }
            }
         }
      }
   }
}
