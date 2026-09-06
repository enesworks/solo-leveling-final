package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.BeruDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class BeruDeadBodyRightClickedOnEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         double rand = 0.0;
         rand = Math.random();
         if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 1.0
            && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).berumax
               == 0.0
            && (entity instanceof BeruDeadBodyEntity _datEntI ? _datEntI.getEntityData().get(BeruDeadBodyEntity.DATA_tries) : 0) > 0) {
            if (entity instanceof BeruDeadBodyEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     BeruDeadBodyEntity.DATA_tries,
                     (entity instanceof BeruDeadBodyEntity _datEntI ? _datEntI.getEntityData().get(BeruDeadBodyEntity.DATA_tries) : 0) - 1
                  );
            }

            if (rand
               <= sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
                  / 100.0) {
               double _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .berumax
                  + 1.0;
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.berumax = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .beru
                  < sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .berumax) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .beru
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.beru = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.BERU_SHADOW.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                        if (sourceentity instanceof Player _owner) {
                           ShadowMonarchManager.tagExistingSummon(_owner, entityToSpawn, "beru");
                        }
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                     entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y - 1.0, z)));
                     entityToSpawn.setVisualOnly(true);
                     _level.addFreshEntity(entityToSpawn);
                  }

                  Entity var27 = world.getEntitiesOfClass(BeruShadowEntity.class, AABB.ofSize(new Vec3(x, y, z), 5.0, 5.0, 5.0), e -> true)
                     .stream()
                     .sorted((new Object() {
                        Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                           return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                        }
                     }).compareDistOf(x, y, z))
                     .findFirst()
                     .orElse(null);
                  if (var27 instanceof TamableAnimal _toTame && sourceentity instanceof Player _owner) {
                     _toTame.tame(_owner);
                  }

                  if (!entity.level().isClientSide()) {
                     entity.discard();
                  }
               }
            } else {
               if (sourceentity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("Arise Failed!"), true);
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 10, 1.0, 1.0, 1.0, 1.0);
               }
            }

            if ((entity instanceof BeruDeadBodyEntity _datEntI ? _datEntI.getEntityData().get(BeruDeadBodyEntity.DATA_tries) : 0) == 1
               && !entity.level().isClientSide()) {
               entity.discard();
            }
         }
      }
   }
}
