package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class ShadowStepWhileProjectileFlyingTickProcedure {
   public static void execute(LevelAccessor world, Entity entity, Entity immediatesourceentity) {
      if (entity != null && immediatesourceentity != null) {
         immediatesourceentity.setNoGravity(true);
         if (Math.sqrt(
               Math.pow(immediatesourceentity.getX() - entity.getX(), 2.0)
                  + Math.pow(immediatesourceentity.getY() - entity.getY(), 2.0)
                  + Math.pow(immediatesourceentity.getZ() - entity.getZ(), 2.0)
            )
            >= 6.0) {
            Entity _ent = entity;
            _ent.teleportTo(immediatesourceentity.getX(), immediatesourceentity.getY(), immediatesourceentity.getZ());
            if (_ent instanceof ServerPlayer _serverPlayer) {
               _serverPlayer.connection
                  .teleport(immediatesourceentity.getX(), immediatesourceentity.getY(), immediatesourceentity.getZ(), _ent.getYRot(), _ent.getXRot());
            }

            if (world instanceof ServerLevel _level) {
               Entity entityToSpawn = SololevelingModEntities.AFTER_IMAGE_2
                  .get()
                  .spawn(_level, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), MobSpawnType.MOB_SUMMONED);
               if (entityToSpawn != null) {
                  entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
               }
            }

            entity.setDeltaMovement(new Vec3(entity.getDeltaMovement().x(), 0.1, entity.getDeltaMovement().z()));
            if (!immediatesourceentity.level().isClientSide()) {
               immediatesourceentity.discard();
            }

            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 1, false, false));
            }
         }
      }
   }
}
