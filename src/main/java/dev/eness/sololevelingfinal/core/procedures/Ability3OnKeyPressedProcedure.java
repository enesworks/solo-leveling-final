package dev.eness.sololevelingfinal.core.procedures;

import io.netty.buffer.Unpooled;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import dev.eness.sololevelingfinal.core.entity.FireFlyEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.FrostMonarchManager;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeMainGUIMenu;

public class Ability3OnKeyPressedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (FrostMonarchManager.isDirectAbilityMode(entity)) {
            FrostMonarchManager.castIceSpear(entity);
         } else {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).combatmode
               )
             {
               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).dash
                  == 1.0) {
                  double _setval = 1.5;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.dash = _setval;
                     capability.syncPlayerVariables(entity);
                  });
               } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .dash
                  == 1.5) {
                  double _setval = 1.0;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.dash = _setval;
                     capability.syncPlayerVariables(entity);
                  });
               }
            } else if (VesselProgressionManager.isShadowMonarch(entity)) {
               if (DoesHaveExchangeProcedure.execute(entity) && entity instanceof ServerPlayer _ent) {
                  final BlockPos _bpos = BlockPos.containing(x, y, z);
                  NetworkHooks.openScreen(_ent, new MenuProvider() {
                     @Override
                     public Component getDisplayName() {
                        return Component.literal("ShadowExchangeMainGUI");
                     }

                     @Override
                     public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                        return new ShadowExchangeMainGUIMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                     }
                  }, _bpos);
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 2.0) {
               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.FIRE_FLY
                     .get()
                     .spawn(_level, BlockPos.containing(x + 1.0, y + 1.0, z), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                  }
               }

               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.FIRE_FLY
                     .get()
                     .spawn(_level, BlockPos.containing(x - 1.0, y + 1.0, z), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                  }
               }

               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.FIRE_FLY
                     .get()
                     .spawn(_level, BlockPos.containing(x, y + 1.0, z + 1.0), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                  }
               }

               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.FIRE_FLY
                     .get()
                     .spawn(_level, BlockPos.containing(x, y + 1.0, z - 1.0), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                  }
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator instanceof FireFlyEntity
                     && !(entityiterator instanceof TamableAnimal _tamEnt && _tamEnt.isTame())
                     && entityiterator instanceof TamableAnimal _toTame
                     && entity instanceof Player _owner) {
                     _toTame.tame(_owner);
                  }
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
                  == 4.0
               && !CooldownManager.isOnCooldown(entity, "job_3")) {
               StormBurstProcedure.execute(world, x, y, z, entity);
            }
         }
      }
   }
}
