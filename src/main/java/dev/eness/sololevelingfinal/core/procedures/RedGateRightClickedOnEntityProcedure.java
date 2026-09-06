package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import java.util.Comparator;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.runtime.SnowRedGateArenaManager;
import dev.eness.sololevelingfinal.core.entity.RedGateEntity;
import dev.eness.sololevelingfinal.core.guild.GuildGateHelper;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.MagicReadingHelper;

public class RedGateRightClickedOnEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (!MagicReadingHelper.isHoldingMagicReader(sourceentity)) {
            if (entity instanceof RedGateEntity usedRedGate && usedRedGate.getEntityData().get(RedGateEntity.DATA_usedbefore)) {
               return;
            }

            if (GuildGateHelper.prepareGateEntry(world, entity, sourceentity)) {
               return;
            }

            if (entity instanceof RedGateEntity redGate && sourceentity instanceof Player) {
               if (!world.isClientSide() && sourceentity instanceof ServerPlayer serverPlayer) {
                  SnowRedGateArenaManager.enterLegacy(world, redGate, serverPlayer);
               }

               return;
            }

            if (!(entity instanceof RedGateEntity _datEntL2 && _datEntL2.getEntityData().get(RedGateEntity.DATA_usedbefore))) {
               if (entity instanceof RedGateEntity _datEntSetL) {
                  _datEntSetL.getEntityData().set(RedGateEntity.DATA_usedbefore, true);
               }

               if (entity instanceof RedGateEntity animatable) {
                  animatable.setTexture("21");
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(250.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
                     && entityiterator instanceof TamableAnimal _tamIsTamedBy
                     && sourceentity instanceof LivingEntity _livEnt
                     && _tamIsTamedBy.isOwnedBy(_livEnt)
                     && !entityiterator.level().isClientSide()) {
                     entityiterator.discard();
                  }
               }

               if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .party
                  .equals("")) {
                  double _setval = sourceentity.getX();
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DunX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  double _setvalx = sourceentity.getY();
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DunY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  double _setvalxx = sourceentity.getZ();
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DunZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  SololevelingMod.queueServerWork(
                     10,
                     () -> {
                        if (sourceentity instanceof ServerPlayer _player && !_player.level().isClientSide()) {
                           ResourceKey<Level> destinationType = ResourceKey.create(
                              Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_snow")
                           );
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
                     }
                  );
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
                        && Math.sqrt(
                              Math.pow(entity.getX() - entityiterator.getX(), 2.0)
                                 + Math.pow(entity.getY() - entityiterator.getY(), 2.0)
                                 + Math.pow(entity.getZ() - entityiterator.getZ(), 2.0)
                           )
                           <= 10.0) {
                        double _setval = sourceentity.getX();
                        entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                           capability.DunX = _setval;
                           capability.syncPlayerVariables(entityiterator);
                        });
                        _setval = sourceentity.getY();
                        entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                           capability.DunY = _setval;
                           capability.syncPlayerVariables(entityiterator);
                        });
                        _setval = sourceentity.getZ();
                        entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                           capability.DunZ = _setval;
                           capability.syncPlayerVariables(entityiterator);
                        });
                        SololevelingMod.queueServerWork(
                           10,
                           () -> {
                              if (entityiterator instanceof ServerPlayer _player && !_player.level().isClientSide()) {
                                 ResourceKey<Level> destinationType = ResourceKey.create(
                                    Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_snow")
                                 );
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

                              entityiterator.getPersistentData().putString("dungeon_tag", entity.getStringUUID());
                           }
                        );
                     }
                  }
               }
            }
         } else {
            MagicReadingHelper.showUnreadableReading(sourceentity);
         }
      }
   }
}
