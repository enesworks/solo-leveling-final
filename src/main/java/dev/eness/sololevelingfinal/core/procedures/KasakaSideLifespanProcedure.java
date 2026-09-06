package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.FxPuddleEntity;

public class KasakaSideLifespanProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         entity.getPersistentData().putDouble("lifespan", entity.getPersistentData().getDouble("lifespan") + 1.0);
         if (entity.getPersistentData().getDouble("lifespan") >= 40.0 && !entity.level().isClientSide()) {
            entity.discard();
         }

         Entity _ent = entity;
         _ent.teleportTo(entity.getPersistentData().getDouble("SideX"), y, entity.getPersistentData().getDouble("SideZ"));
         if (_ent instanceof ServerPlayer _serverPlayer) {
            _serverPlayer.connection
               .teleport(entity.getPersistentData().getDouble("SideX"), y, entity.getPersistentData().getDouble("SideZ"), _ent.getYRot(), _ent.getXRot());
         }

         entity.setDeltaMovement(new Vec3(0.0, -1.0, 0.0));
         if (entity instanceof FxPuddleEntity) {
            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 2, false, false));
               }

               if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 0, false, false));
               }
            }
         }
      }
   }
}
