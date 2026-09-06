package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class BearTrapOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double counter = 0.0;
         if (entity instanceof TamableAnimal _tamEnt && _tamEnt.isTame() && (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != null) {
            if (entity instanceof BearTrapEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     BearTrapEntity.DATA_trigger_timer,
                     (entity instanceof BearTrapEntity _datEntI ? _datEntI.getEntityData().get(BearTrapEntity.DATA_trigger_timer) : 0) + 1
                  );
            }

            if ((entity instanceof BearTrapEntity _datEntI ? _datEntI.getEntityData().get(BearTrapEntity.DATA_trigger_timer) : 0) > 19
               && entity instanceof BearTrapEntity _datEntSetL) {
               _datEntSetL.getEntityData().set(BearTrapEntity.DATA_trigger, true);
            }

            if (entity instanceof BearTrapEntity _datEntL7 && _datEntL7.getEntityData().get(BearTrapEntity.DATA_trigger)) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if ((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .party
                     .equals("")) {
                     if ((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != entityiterator
                        && entity != entityiterator
                        && entityiterator instanceof LivingEntity) {
                        counter = 1.0;
                     }
                  } else if ((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .party
                        .equals(
                           entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .party
                        )
                     && (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != entityiterator
                     && entity != entityiterator
                     && entityiterator instanceof LivingEntity) {
                     counter = 1.0;
                  }
               }

               if (counter > 0.0) {
                  if (world instanceof Level _level && !_level.isClientSide()) {
                     _level.explode(
                        entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null,
                        new DamageSource(
                           world.registryAccess()
                              .registryOrThrow(Registries.DAMAGE_TYPE)
                              .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:ranger"))),
                           entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null
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

                  if (!entity.level().isClientSide()) {
                     entity.discard();
                  }
               }
            }
         }
      }
   }
}
