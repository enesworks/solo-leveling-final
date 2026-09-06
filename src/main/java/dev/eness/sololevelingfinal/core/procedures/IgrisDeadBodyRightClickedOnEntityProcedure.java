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
import dev.eness.sololevelingfinal.core.entity.IgrisDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

public class IgrisDeadBodyRightClickedOnEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 1.0
            && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).igris
               == 0.0) {
            if ((entity instanceof IgrisDeadBodyEntity _datEntI ? _datEntI.getEntityData().get(IgrisDeadBodyEntity.DATA_arise) : 0) < 3) {
               if (entity instanceof IgrisDeadBodyEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(
                        IgrisDeadBodyEntity.DATA_arise,
                        (entity instanceof IgrisDeadBodyEntity _datEntI ? _datEntI.getEntityData().get(IgrisDeadBodyEntity.DATA_arise) : 0) + 1
                     );
               }

               if (sourceentity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(
                     Component.literal(
                        "Failed! "
                           + Math.round(4 - (entity instanceof IgrisDeadBodyEntity _datEntI ? _datEntI.getEntityData().get(IgrisDeadBodyEntity.DATA_arise) : 0))
                           + " tries remaining"
                     ),
                     false
                  );
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SMOKE, x, y, z, 6, 2.0, 2.0, 2.0, 1.0);
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(SololevelingModParticleTypes.MANA_BLUE.get(), x, y, z, 6, 2.0, 2.0, 2.0, 1.0);
               }
            } else if ((entity instanceof IgrisDeadBodyEntity _datEntI ? _datEntI.getEntityData().get(IgrisDeadBodyEntity.DATA_arise) : 0) == 3) {
               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(ParticleTypes.SMOKE, x, y, z, 12, 2.0, 2.0, 2.0, 1.0);
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(SololevelingModParticleTypes.MANA_BLUE.get(), x, y, z, 12, 2.0, 2.0, 2.0, 1.0);
               }

               if (world instanceof ServerLevel _level) {
                  LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                  entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
                  entityToSpawn.setVisualOnly(true);
                  _level.addFreshEntity(entityToSpawn);
               }

               double _setval = 1.0;
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.igris = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               _setval = 1.0;
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.IgrisSpawned = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.IGRIS_SHADOW.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null && sourceentity instanceof Player _owner) {
                     ShadowMonarchManager.tagExistingSummon(_owner, entityToSpawn, "igris");
                  }
               }

               if (!world.getEntitiesOfClass(IgrisShadowEntity.class, AABB.ofSize(new Vec3(x, y, z), 4.0, 4.0, 4.0), e -> true).isEmpty()) {
                  Entity var28 = world.getEntitiesOfClass(IgrisShadowEntity.class, AABB.ofSize(new Vec3(x, y, z), 4.0, 4.0, 4.0), e -> true)
                     .stream()
                     .sorted((new Object() {
                        Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                           return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                        }
                     }).compareDistOf(x, y, z))
                     .findFirst()
                     .orElse(null);
                  if (var28 instanceof TamableAnimal _toTame && sourceentity instanceof Player _owner) {
                     _toTame.tame(_owner);
                  }
               }

               if (!entity.level().isClientSide()) {
                  entity.discard();
               }
            }
         }
      }
   }
}
