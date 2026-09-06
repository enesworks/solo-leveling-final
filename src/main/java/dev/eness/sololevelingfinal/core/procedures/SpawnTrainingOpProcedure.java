package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class SpawnTrainingOpProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).istraining
            )
          {
            if (entity instanceof Player _player) {
               _player.closeContainer();
            }

            boolean _setval = true;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.istraining = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (world instanceof ServerLevel _level) {
               Entity entityToSpawn = SololevelingModEntities.TRAINING_BOT.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
               if (entityToSpawn != null) {
                  entityToSpawn.getPersistentData().putUUID("slr_training_owner", entity.getUUID());
                  entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
               }
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Defeat current training dummy first"), false);
         }
      }
   }
}
