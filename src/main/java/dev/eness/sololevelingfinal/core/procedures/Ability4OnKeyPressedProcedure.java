package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.FrostMonarchManager;
import dev.eness.sololevelingfinal.core.util.TemporaryArmorSessionManager;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

public class Ability4OnKeyPressedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (FrostMonarchManager.isDirectAbilityMode(entity)) {
            FrostMonarchManager.castFrostCounter(entity);
         } else {
            ItemStack head = ItemStack.EMPTY;
            ItemStack chest = ItemStack.EMPTY;
            ItemStack leg = ItemStack.EMPTY;
            ItemStack feet = ItemStack.EMPTY;
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).combatmode
               )
             {
               if (!CooldownManager.isOnCooldown(entity, "aura")) {
                  CooldownManager.set(entity, "aura", 7200);
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.AURA.get(), 3600, 1, false, false));
                  }

                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           2.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x,
                           y,
                           z,
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           2.0F,
                           false
                        );
                     }
                  }

                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("§1Activated Aura"), true);
                  }
               }
            } else if (VesselProgressionManager.isShadowMonarch(entity)) {
               if (DoesHaveShadowManifestationProcedure.execute(entity)) {
                  if ((entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY).getItem()
                        != SololevelingModItems.SHADOW_ARMOR_HELMET.get()
                     && (entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.CHEST) : ItemStack.EMPTY).getItem()
                        != SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get()
                     && (entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.LEGS) : ItemStack.EMPTY).getItem()
                        != SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get()
                     && (entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.FEET) : ItemStack.EMPTY).getItem()
                        != SololevelingModItems.SHADOW_ARMOR_BOOTS.get()
                     && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
                        >= 500.0
                     && VesselProgressionManager.isShadowMonarch(entity)) {
                     ItemStack _setval;
                     ItemStack var55 = _setval = entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY;
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.overridehead = _setval;
                        capability.syncPlayerVariables(entity);
                     });
                     var55 = _setval = entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.CHEST) : ItemStack.EMPTY;
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.overridetorso = _setval;
                        capability.syncPlayerVariables(entity);
                     });
                     var55 = _setval = entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.LEGS) : ItemStack.EMPTY;
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.overridelegs = _setval;
                        capability.syncPlayerVariables(entity);
                     });
                     var55 = _setval = entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.FEET) : ItemStack.EMPTY;
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.overridefeet = _setval;
                        capability.syncPlayerVariables(entity);
                     });
                     long manifestationSession = TemporaryArmorSessionManager.begin(entity);
                     SololevelingMod.queueServerWork(5, () -> {
                        if (!TemporaryArmorSessionManager.canEquipShadow(entity, manifestationSession)) {
                           TemporaryArmorSessionManager.abandonIfCurrent(entity, manifestationSession);
                        } else {
                           Entity _entity = entity;
                           if (_entity instanceof Player _playerxxxx) {
                              _playerxxxx.getInventory().armor.set(0, new ItemStack(SololevelingModItems.SHADOW_ARMOR_BOOTS.get()));
                              _playerxxxx.getInventory().setChanged();
                           } else if (_entity instanceof LivingEntity _livingxxxx) {
                              _livingxxxx.setItemSlot(EquipmentSlot.FEET, new ItemStack(SololevelingModItems.SHADOW_ARMOR_BOOTS.get()));
                           }

                           _entity = entity;
                           if (_entity instanceof Player _playerxxx) {
                              _playerxxx.getInventory().armor.set(1, new ItemStack(SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get()));
                              _playerxxx.getInventory().setChanged();
                           } else if (_entity instanceof LivingEntity _livingxxx) {
                              _livingxxx.setItemSlot(EquipmentSlot.LEGS, new ItemStack(SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get()));
                           }

                           _entity = entity;
                           if (_entity instanceof Player _playerxx) {
                              _playerxx.getInventory().armor.set(2, new ItemStack(SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get()));
                              _playerxx.getInventory().setChanged();
                           } else if (_entity instanceof LivingEntity _livingxx) {
                              _livingxx.setItemSlot(EquipmentSlot.CHEST, new ItemStack(SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get()));
                           }

                           _entity = entity;
                           if (_entity instanceof Player _playerx) {
                              _playerx.getInventory().armor.set(3, new ItemStack(SololevelingModItems.SHADOW_ARMOR_HELMET.get()));
                              _playerx.getInventory().setChanged();
                           } else if (_entity instanceof LivingEntity _livingx) {
                              _livingx.setItemSlot(EquipmentSlot.HEAD, new ItemStack(SololevelingModItems.SHADOW_ARMOR_HELMET.get()));
                           }

                           TemporaryArmorSessionManager.markEquipped(entity, manifestationSession);
                           if (world instanceof ServerLevel _levelxx) {
                              _levelxx.sendParticles(SololevelingModParticleTypes.MANA_PURPLE.get(), x, y, z, 100, 1.0, 1.0, 1.0, 1.0);
                           }

                           if (world instanceof ServerLevel _levelx) {
                              _levelx.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 100, 1.0, 1.0, 1.0, 1.0);
                           }
                        }
                     });
                  } else if ((entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY).getItem()
                        == SololevelingModItems.SHADOW_ARMOR_HELMET.get()
                     && (entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.CHEST) : ItemStack.EMPTY).getItem()
                        == SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get()
                     && (entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.LEGS) : ItemStack.EMPTY).getItem()
                        == SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get()
                     && (entity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.FEET) : ItemStack.EMPTY).getItem()
                        == SololevelingModItems.SHADOW_ARMOR_BOOTS.get()) {
                     Entity _entity = entity;
                     if (_entity instanceof Player _player) {
                        _player.getInventory()
                           .armor
                           .set(
                              0,
                              entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .overridefeet
                           );
                        _player.getInventory().setChanged();
                     } else if (_entity instanceof LivingEntity _living) {
                        _living.setItemSlot(
                           EquipmentSlot.FEET,
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .overridefeet
                        );
                     }

                     _entity = entity;
                     if (_entity instanceof Player _player) {
                        _player.getInventory()
                           .armor
                           .set(
                              1,
                              entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .overridelegs
                           );
                        _player.getInventory().setChanged();
                     } else if (_entity instanceof LivingEntity _living) {
                        _living.setItemSlot(
                           EquipmentSlot.LEGS,
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .overridelegs
                        );
                     }

                     _entity = entity;
                     if (_entity instanceof Player _player) {
                        _player.getInventory()
                           .armor
                           .set(
                              2,
                              entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .overridetorso
                           );
                        _player.getInventory().setChanged();
                     } else if (_entity instanceof LivingEntity _living) {
                        _living.setItemSlot(
                           EquipmentSlot.CHEST,
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .overridetorso
                        );
                     }

                     _entity = entity;
                     if (_entity instanceof Player _player) {
                        _player.getInventory()
                           .armor
                           .set(
                              3,
                              entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .overridehead
                           );
                        _player.getInventory().setChanged();
                     } else if (_entity instanceof LivingEntity _living) {
                        _living.setItemSlot(
                           EquipmentSlot.HEAD,
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .overridehead
                        );
                     }

                     TemporaryArmorSessionManager.finishAfterRestore(entity);
                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(SololevelingModParticleTypes.MANA_PURPLE.get(), x, y, z, 100, 1.0, 1.0, 1.0, 1.0);
                     }

                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 100, 1.0, 1.0, 1.0, 1.0);
                     }
                  }
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 2.0) {
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("ability wip"), false);
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 4.0) {
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("ability wip"), false);
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 5.0) {
               GoliathManifestationProcedure.execute(world, x, y, z, entity);
            }
         }
      }
   }
}
