package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class GoblinKingDiesProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         execute(
            event,
            event.getEntity().level(),
            event.getEntity().getX(),
            event.getEntity().getY(),
            event.getEntity().getZ(),
            event.getEntity(),
            event.getSource().getEntity()
         );
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      execute(null, world, x, y, z, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (!entity.getPersistentData().getBoolean("slr_dungeon_spawned")) {
            boolean found = false;
            double uplvl = 0.0;
            double sx = 0.0;
            double sy = 0.0;
            double sz = 0.0;
            if (entity instanceof GoblinKingEntity) {
               if (sourceentity instanceof TamableAnimal _tamEnt && _tamEnt.isTame()) {
                  if ((sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != null) {
                     if (IsInDungeonBiomeProcedure.execute(world, x, y, z)) {
                        Entity _ent = sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                           _ent.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                    CommandSource.NULL,
                                    _ent.position(),
                                    _ent.getRotationVector(),
                                    _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                    4,
                                    _ent.getName().getString(),
                                    _ent.getDisplayName(),
                                    _ent.level().getServer(),
                                    _ent
                                 ),
                                 "/slnotify both &6&lBoss Slain | &eDungeon Cleared!"
                              );
                        }

                        _ent = sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                           _ent.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                    CommandSource.NULL,
                                    _ent.position(),
                                    _ent.getRotationVector(),
                                    _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                    4,
                                    _ent.getName().getString(),
                                    _ent.getDisplayName(),
                                    _ent.level().getServer(),
                                    _ent
                                 ),
                                 "/title @p clear"
                              );
                        }

                        boolean _setval = true;
                        (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .ifPresent(capability -> {
                              capability.BossKilled = _setval;
                              capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                           });
                     } else {
                        Entity _ent = sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                           _ent.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                    CommandSource.NULL,
                                    _ent.position(),
                                    _ent.getRotationVector(),
                                    _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                    4,
                                    _ent.getName().getString(),
                                    _ent.getDisplayName(),
                                    _ent.level().getServer(),
                                    _ent
                                 ),
                                 "/slnotify both &6&lBoss Slain | &eBoss Defeated!"
                              );
                        }

                        _ent = sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                           _ent.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                    CommandSource.NULL,
                                    _ent.position(),
                                    _ent.getRotationVector(),
                                    _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                    4,
                                    _ent.getName().getString(),
                                    _ent.getDisplayName(),
                                    _ent.level().getServer(),
                                    _ent
                                 ),
                                 "/title @p clear"
                              );
                        }
                     }
                  }
               } else if (sourceentity instanceof Player) {
                  if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .party
                     .equals("")) {
                     if (IsInDungeonBiomeProcedure.execute(world, x, y, z)) {
                        Entity _ent = sourceentity;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                           _ent.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                    CommandSource.NULL,
                                    _ent.position(),
                                    _ent.getRotationVector(),
                                    _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                    4,
                                    _ent.getName().getString(),
                                    _ent.getDisplayName(),
                                    _ent.level().getServer(),
                                    _ent
                                 ),
                                 "/slnotify both &6&lBoss Slain | &eDungeon Cleared!"
                              );
                        }

                        _ent = sourceentity;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                           _ent.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                    CommandSource.NULL,
                                    _ent.position(),
                                    _ent.getRotationVector(),
                                    _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                    4,
                                    _ent.getName().getString(),
                                    _ent.getDisplayName(),
                                    _ent.level().getServer(),
                                    _ent
                                 ),
                                 "/title @p clear"
                              );
                        }

                        boolean _setval = true;
                        sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                           capability.BossKilled = _setval;
                           capability.syncPlayerVariables(sourceentity);
                        });
                     } else {
                        Entity _ent = sourceentity;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                           _ent.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                    CommandSource.NULL,
                                    _ent.position(),
                                    _ent.getRotationVector(),
                                    _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                    4,
                                    _ent.getName().getString(),
                                    _ent.getDisplayName(),
                                    _ent.level().getServer(),
                                    _ent
                                 ),
                                 "/slnotify both &6&lBoss Slain | &eBoss Defeated!"
                              );
                        }

                        _ent = sourceentity;
                        if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                           _ent.getServer()
                              .getCommands()
                              .performPrefixedCommand(
                                 new CommandSourceStack(
                                    CommandSource.NULL,
                                    _ent.position(),
                                    _ent.getRotationVector(),
                                    _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                    4,
                                    _ent.getName().getString(),
                                    _ent.getDisplayName(),
                                    _ent.level().getServer(),
                                    _ent
                                 ),
                                 "/title @p clear"
                              );
                        }
                     }
                  } else {
                     for (Entity entityiterator : new ArrayList<>(world.players())) {
                        if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .party
                              .equals(
                                 entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                       .orElse(new SololevelingModVariables.PlayerVariables())
                                    .party
                              )
                           && entity.level().dimension() == entityiterator.level().dimension()) {
                           if (IsInDungeonBiomeProcedure.execute(world, x, y, z)) {
                              Entity _ent = entityiterator;
                              if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                                 _ent.getServer()
                                    .getCommands()
                                    .performPrefixedCommand(
                                       new CommandSourceStack(
                                          CommandSource.NULL,
                                          _ent.position(),
                                          _ent.getRotationVector(),
                                          _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                          4,
                                          _ent.getName().getString(),
                                          _ent.getDisplayName(),
                                          _ent.level().getServer(),
                                          _ent
                                       ),
                                       "/slnotify both &6&lBoss Slain | &eDungeon Cleared!"
                                    );
                              }

                              _ent = entityiterator;
                              if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                                 _ent.getServer()
                                    .getCommands()
                                    .performPrefixedCommand(
                                       new CommandSourceStack(
                                          CommandSource.NULL,
                                          _ent.position(),
                                          _ent.getRotationVector(),
                                          _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                          4,
                                          _ent.getName().getString(),
                                          _ent.getDisplayName(),
                                          _ent.level().getServer(),
                                          _ent
                                       ),
                                       "/title @p clear"
                                    );
                              }

                              boolean _setval = true;
                              entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                 capability.BossKilled = _setval;
                                 capability.syncPlayerVariables(entityiterator);
                              });
                           } else {
                              Entity _ent = entityiterator;
                              if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                                 _ent.getServer()
                                    .getCommands()
                                    .performPrefixedCommand(
                                       new CommandSourceStack(
                                          CommandSource.NULL,
                                          _ent.position(),
                                          _ent.getRotationVector(),
                                          _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                          4,
                                          _ent.getName().getString(),
                                          _ent.getDisplayName(),
                                          _ent.level().getServer(),
                                          _ent
                                       ),
                                       "/slnotify both &6&lBoss Slain | &eBoss Defeated!"
                                    );
                              }

                              _ent = entityiterator;
                              if (!_ent.level().isClientSide() && _ent.getServer() != null) {
                                 _ent.getServer()
                                    .getCommands()
                                    .performPrefixedCommand(
                                       new CommandSourceStack(
                                          CommandSource.NULL,
                                          _ent.position(),
                                          _ent.getRotationVector(),
                                          _ent.level() instanceof ServerLevel ? (ServerLevel)_ent.level() : null,
                                          4,
                                          _ent.getName().getString(),
                                          _ent.getDisplayName(),
                                          _ent.level().getServer(),
                                          _ent
                                       ),
                                       "/title @p clear"
                                    );
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
