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
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class ShadowStepProjectileHitsBlockProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity immediatesourceentity) {
      if (entity != null && immediatesourceentity != null) {
         if (world.getBlockState(BlockPos.containing(x + 0.5, y + 1.0, z + 0.5)).canOcclude()
            && world.getBlockState(BlockPos.containing(x + 0.5, y + 2.0, z + 0.5)).canOcclude()) {
            Entity _ent = entity;
            _ent.teleportTo(x + 0.5, y + 1.0, z + 0.5);
            if (_ent instanceof ServerPlayer _serverPlayer) {
               _serverPlayer.connection.teleport(x + 0.5, y + 1.0, z + 0.5, _ent.getYRot(), _ent.getXRot());
            }
         } else {
            Entity _ent = entity;
            _ent.teleportTo(immediatesourceentity.getX(), immediatesourceentity.getY(), immediatesourceentity.getZ());
            if (_ent instanceof ServerPlayer _serverPlayer) {
               _serverPlayer.connection
                  .teleport(immediatesourceentity.getX(), immediatesourceentity.getY(), immediatesourceentity.getZ(), _ent.getYRot(), _ent.getXRot());
            }

            if (world instanceof ServerLevel _level) {
               Entity entityToSpawn = SololevelingModEntities.AFTER_IMAGE
                  .get()
                  .spawn(_level, BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()), MobSpawnType.MOB_SUMMONED);
               if (entityToSpawn != null) {
                  entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
               }
            }

            if (!immediatesourceentity.level().isClientSide()) {
               immediatesourceentity.discard();
            }

            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 1, false, false));
            }
         }
      }
   }
}
