package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.entity.PortalLushEntity;
import dev.eness.sololevelingfinal.core.guild.GuildGateHelper;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.MagicReadingHelper;
import dev.eness.sololevelingfinal.core.util.PlayerEntryGenerationGuard;
import dev.eness.sololevelingfinal.core.util.UrgentQuestManager;

public class PortalLushRightClickedOnEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (!MagicReadingHelper.isHoldingMagicReader(sourceentity)) {
            if (GuildGateHelper.prepareGateEntry(world, entity, sourceentity)) {
               return;
            }

            if (!(sourceentity instanceof ServerPlayer entryPlayer)) {
               return;
            }

            long entryGeneration = PlayerEntryGenerationGuard.begin(entryPlayer);
            ResourceKey dungeonDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_b"));
            Vec3 _setval = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_setval, _setval).inflate(250.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_setval)))
               .toList()) {
               if (entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
                  && entityiterator instanceof TamableAnimal _tamIsTamedBy
                  && sourceentity instanceof LivingEntity _livEnt
                  && _tamIsTamedBy.isOwnedBy(_livEnt)
                  && !entityiterator.level().isClientSide()) {
                  entityiterator.discard();
               }
            }

            double _setvalx = sourceentity.getX();
            sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.DunX = _setval;
               capability.syncPlayerVariables(sourceentity);
            });
            double _setvalxx = sourceentity.getY();
            sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.DunY = _setval;
               capability.syncPlayerVariables(sourceentity);
            });
            double _setvalxxx = sourceentity.getZ();
            sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.DunZ = _setval;
               capability.syncPlayerVariables(sourceentity);
            });
            if (!world.isClientSide()) {
               sourceentity.getPersistentData().putDouble("tpx", entity.getPersistentData().getDouble("tpx"));
               sourceentity.getPersistentData().putDouble("tpx", entity.getPersistentData().getDouble("tpx"));
               sourceentity.getPersistentData().putDouble("tpx", entity.getPersistentData().getDouble("tpx"));
            }

            sourceentity.setNoGravity(true);
            SololevelingMod.queueServerWork(
               10,
               () -> {
                  if (PlayerEntryGenerationGuard.isCurrent(entryPlayer, entryGeneration)) {
                     if (sourceentity instanceof ServerPlayer _player && !_player.level().isClientSide()) {
                        ResourceKey<Level> destinationType = dungeonDimension;
                        if (_player.level().dimension() == destinationType) {
                           return;
                        }

                        ServerLevel nextLevel = _player.server.getLevel(destinationType);
                        if (nextLevel != null) {
                           _player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0F));
                           _player.teleportTo(nextLevel, _player.getX(), _player.getY(), _player.getZ(), _player.getYRot(), _player.getXRot());
                           _player.connection.send(new ClientboundPlayerAbilitiesPacket(_player.getAbilities()));

                           for (MobEffectInstance _effectinstance : _player.getActiveEffects()) {
                              _player.connection.send(new ClientboundUpdateMobEffectPacket(_player.getId(), _effectinstance));
                           }

                           _player.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
                        }
                     }

                     sourceentity.getPersistentData().putString("dungeon_tag", entity.getStringUUID());
                     UrgentQuestManager.markDungeonId(sourceentity, "lush");
                     SololevelingMod.queueServerWork(
                        5,
                        () -> {
                           if (PlayerEntryGenerationGuard.isCurrent(entryPlayer, entryGeneration) && entryPlayer.level().dimension() == dungeonDimension) {
                              Entity _ent = sourceentity;
                              _ent.teleportTo(
                                 entity.getPersistentData().getDouble("tpx"),
                                 entity.getPersistentData().getDouble("tpy"),
                                 entity.getPersistentData().getDouble("tpz")
                              );
                              if (_ent instanceof ServerPlayer _serverPlayer) {
                                 _serverPlayer.connection
                                    .teleport(
                                       entity.getPersistentData().getDouble("tpx"),
                                       entity.getPersistentData().getDouble("tpy"),
                                       entity.getPersistentData().getDouble("tpz"),
                                       _ent.getYRot(),
                                       _ent.getXRot()
                                    );
                              }

                              sourceentity.getPersistentData().putString("dungeon_tag", entity.getStringUUID());
                              SololevelingMod.queueServerWork(
                                 10,
                                 () -> {
                                    if (PlayerEntryGenerationGuard.isCurrent(entryPlayer, entryGeneration)
                                       && entryPlayer.level().dimension() == dungeonDimension) {
                                       if (!(entity instanceof PortalLushEntity _datEntL26 && _datEntL26.getEntityData().get(PortalLushEntity.DATA_usedbefore))
                                          )
                                        {
                                          if (entity instanceof PortalLushEntity _datEntSetL) {
                                             _datEntSetL.getEntityData().set(PortalLushEntity.DATA_usedbefore, true);
                                          }

                                          Entity _entx = sourceentity;
                                          if (!_entx.level().isClientSide() && _entx.getServer() != null) {
                                             _entx.getServer()
                                                .getCommands()
                                                .performPrefixedCommand(
                                                   new CommandSourceStack(
                                                      CommandSource.NULL,
                                                      _entx.position(),
                                                      _entx.getRotationVector(),
                                                      _entx.level() instanceof ServerLevel ? (ServerLevel)_entx.level() : null,
                                                      4,
                                                      _entx.getName().getString(),
                                                      _entx.getDisplayName(),
                                                      _entx.level().getServer(),
                                                      _entx
                                                   ),
                                                   "execute in sololeveling:dungeon_dimension_b as @s at @s unless entity @e[type=sololeveling:portal_12,distance=..100] run spawnlush"
                                                );
                                          }

                                          sourceentity.getPersistentData().putString("dungeon_tag", entity.getStringUUID());
                                       }
                                    }
                                 }
                              );
                           }
                        }
                     );
                  }
               }
            );
         } else {
            MagicReadingHelper.showRankReading(sourceentity, ProceduralDungeonRank.B);
         }
      }
   }
}
