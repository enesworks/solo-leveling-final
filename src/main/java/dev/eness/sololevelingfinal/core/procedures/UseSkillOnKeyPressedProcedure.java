package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.ShadowStepEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ArcaneMageSpellManager;
import dev.eness.sololevelingfinal.core.util.AssassinSkillManager;
import dev.eness.sololevelingfinal.core.util.BarrierMageSpellManager;
import dev.eness.sololevelingfinal.core.util.ClassPassiveManager;
import dev.eness.sololevelingfinal.core.util.ColdBloodSkillManager;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.DaggerThrowManager;
import dev.eness.sololevelingfinal.core.util.FireMageSpellManager;
import dev.eness.sololevelingfinal.core.util.JobSkillManager;
import dev.eness.sololevelingfinal.core.util.MageQTEHelper;
import dev.eness.sololevelingfinal.core.util.MageQTEState;
import dev.eness.sololevelingfinal.core.util.MageSpellProgression;
import dev.eness.sololevelingfinal.core.util.QTEResult;
import dev.eness.sololevelingfinal.core.util.RangerCombatManager;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.StormMageSpellManager;
import dev.eness.sololevelingfinal.core.util.TankerSkillManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;
import dev.eness.sololevelingfinal.core.util.UrgentQuestManager;

public class UseSkillOnKeyPressedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         String _selectedPower = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables())
            .PselectedPower;
         boolean specializationSpell = FireMageSpellManager.isFireSkill(_selectedPower)
            || BarrierMageSpellManager.isBarrierSkill(_selectedPower)
            || ArcaneMageSpellManager.isArcaneSkill(_selectedPower)
            || StormMageSpellManager.isStormSkill(_selectedPower);
         if (specializationSpell && !MageSpellProgression.canCastLearnedSkill(entity, _selectedPower)) {
            if (world instanceof Level level && !level.isClientSide() && entity instanceof Player player) {
               player.displayClientMessage(Component.literal("You have not learned this spell."), true);
            }
         } else if (MageQTEHelper.MAGE_SKILLS.contains(_selectedPower) && CooldownManager.isOnCooldown(entity, _selectedPower)) {
            if (world instanceof Level level) {
               if (level.isClientSide()) {
                  DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MageQTEState.INSTANCE.endQTE());
               } else {
                  entity.getPersistentData().putBoolean("mage_casting", false);
                  entity.getPersistentData().remove("mage_qte_zone_start");
                  if (entity instanceof Player player) {
                     player.displayClientMessage(
                        Component.literal("Ability on cooldown! " + CooldownManager.getRemainingSeconds(entity, _selectedPower) + "s"), true
                     );
                  }
               }
            }
         } else if (RangerCombatManager.isRangerSkill(_selectedPower)) {
            if (entity instanceof ServerPlayer ranger && RangerCombatManager.activateSkill(ranger, _selectedPower)) {
               UrgentQuestManager.onSkillUsed(entity, _selectedPower);
            }
         } else if (TankerSkillManager.isTankerSkill(_selectedPower)) {
            if (entity instanceof ServerPlayer tanker && TankerSkillManager.activateSkill(tanker, _selectedPower)) {
               UrgentQuestManager.onSkillUsed(entity, TankerSkillManager.canonicalName(_selectedPower));
            }
         } else {
            UrgentQuestManager.onSkillUsed(entity, _selectedPower);
            if (entity instanceof ServerPlayer daggerPlayer) {
               if ("Dagger Throw".equals(_selectedPower)) {
                  DaggerThrowManager.castThrow(daggerPlayer);
                  return;
               }

               if ("Dagger Rush".equals(_selectedPower)) {
                  DaggerThrowManager.castRush(daggerPlayer);
                  return;
               }

               if (AssassinSkillManager.isReworkedSkill(_selectedPower)) {
                  AssassinSkillManager.activateSkill(daggerPlayer, _selectedPower);
                  return;
               }
            }

            if (ShadowMonarchManager.isFormationSkill(_selectedPower)) {
               ShadowFormationCastProcedure.execute(world, x, y, z, entity, _selectedPower);
            } else if (!JobSkillManager.cast(world, x, y, z, entity, _selectedPower)) {
               if ("Cold Blood".equals(_selectedPower)) {
                  ColdBloodSkillManager.cast(entity);
               } else if ("Flame Weaving".equals(_selectedPower)) {
                  if (world instanceof Level level && !level.isClientSide()) {
                     FireMageSpellManager.cast(entity, _selectedPower, QTEResult.MISS);
                  }
               } else if (BarrierMageSpellManager.isInstantSkill(_selectedPower)) {
                  if (world instanceof Level level && !level.isClientSide()) {
                     BarrierMageSpellManager.cast(entity, _selectedPower, QTEResult.MISS);
                  }
               } else if (ArcaneMageSpellManager.isInstantSkill(_selectedPower)) {
                  if (world instanceof Level level && !level.isClientSide()) {
                     ArcaneMageSpellManager.cast(entity, _selectedPower, QTEResult.MISS);
                  }
               } else if (StormMageSpellManager.isInstantSkill(_selectedPower)) {
                  if (world instanceof Level level && !level.isClientSide()) {
                     StormMageSpellManager.cast(entity, _selectedPower, QTEResult.MISS);
                  }
               } else if (MageQTEHelper.MAGE_SKILLS.contains(_selectedPower)) {
                  float qteZoneStart = MageQTEHelper.computeZoneStart(entity);
                  if (world instanceof Level _lvl) {
                     if (_lvl.isClientSide()) {
                        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MageQTEState.INSTANCE.startQTE(qteZoneStart));
                     } else {
                        entity.getPersistentData().putBoolean("mage_casting", true);
                        entity.getPersistentData().putFloat("mage_qte_zone_start", qteZoneStart);
                     }
                  }
               } else {
                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Stealth")) {
                     if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        >= 600.0) {
                        if (!CooldownManager.isOnCooldown(entity, "Stealth")) {
                           StealthProcedure.execute(world, x, y, z, entity);
                           double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .MP
                              - 600.0;
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                              capability.MP = _setval;
                              capability.syncPlayerVariables(entity);
                           });
                           if (entity instanceof Player _player && !_player.level().isClientSide()) {
                              _player.displayClientMessage(Component.literal("§1Using Stealth"), true);
                           }
                        }
                     } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("You dont have enough MP"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Murderious Intent")) {
                     if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        >= 600.0) {
                        if (!CooldownManager.isOnCooldown(entity, "Murderious Intent")) {
                           double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .MP
                              - 600.0;
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                              capability.MP = _setval;
                              capability.syncPlayerVariables(entity);
                           });
                           BloodLustProcedure.execute(world, x, y, z, entity);
                           CooldownManager.set(entity, "mana_refresh", 60);
                           if (entity instanceof Player _player && !_player.level().isClientSide()) {
                              _player.displayClientMessage(Component.literal("§1Using Murderious Intent"), true);
                           }
                        }
                     } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("You dont have enough MP"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Backstab")) {
                     PhantomLeapAttackProcedure.execute(world, x, y, z, entity);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Shadowstep")) {
                     castShadowstep(world, x, y, z, entity);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Dualwield")) {
                     DualWieldProcedure.execute(world, x, y, z, entity);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                        .PselectedPower
                        .equals("Quickslashes")
                     && !CooldownManager.isOnCooldown(entity, "Quickslashes")
                     && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
                        > 1000.0) {
                     QuickSlashesProcedure.execute(world, x, y, z, entity);
                     double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        - 1000.0;
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.MP = _setval;
                        capability.syncPlayerVariables(entity);
                     });
                     if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("§1Using Quick Slashes"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Detection")) {
                     DetectEyeSpawnProcedure.execute(world, x, y, z, entity);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Slash Dash")) {
                     if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        >= 600.0) {
                        if (!CooldownManager.isOnCooldown(entity, "Slash Dash")) {
                           if (!(entity instanceof LivingEntity _livEnt46 && _livEnt46.hasEffect(SololevelingModMobEffects.SWORD_DANCE.get()))
                              && !(entity instanceof LivingEntity _livEnt47 && _livEnt47.hasEffect(SololevelingModMobEffects.SWORD_OF_LIGHT.get()))) {
                              double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                       .orElse(new SololevelingModVariables.PlayerVariables())
                                    .MP
                                 - 600.0;
                              entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                 capability.MP = _setval;
                                 capability.syncPlayerVariables(entity);
                              });
                              HeavyImpactProcedure.execute(world, entity);
                              CooldownManager.set(entity, "mana_refresh", 60);
                              if (entity instanceof Player _player && !_player.level().isClientSide()) {
                                 _player.displayClientMessage(Component.literal("§l§7Using Slash Dash"), true);
                              }
                           } else {
                              if (entity instanceof Player _player && !_player.level().isClientSide()) {
                                 _player.displayClientMessage(Component.literal("§6Can't Use skill while Sword Dance is active!"), true);
                              }

                              if (world instanceof Level _level) {
                                 if (!_level.isClientSide()) {
                                    _level.playSound(
                                       (Player)null,
                                       BlockPos.containing(x, y, z),
                                       ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                       SoundSource.NEUTRAL,
                                       1.0F,
                                       1.0F
                                    );
                                 } else {
                                    _level.playLocalSound(
                                       x,
                                       y,
                                       z,
                                       ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                       SoundSource.NEUTRAL,
                                       1.0F,
                                       1.0F,
                                       false
                                    );
                                 }
                              }
                           }
                        }
                     } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("You dont have enough MP"), true);
                     }
                  }

                  if (_selectedPower.equals("Cross Strike") || _selectedPower.equals("Critical Strike")) {
                     if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        >= 1000.0) {
                        if (!CooldownManager.isOnCooldown(entity, "Cross Strike") && !CooldownManager.isOnCooldown(entity, "Critical Strike")) {
                           if (!(entity instanceof LivingEntity _livEnt54 && _livEnt54.hasEffect(SololevelingModMobEffects.SWORD_DANCE.get()))
                              && !(entity instanceof LivingEntity _livEnt55 && _livEnt55.hasEffect(SololevelingModMobEffects.SWORD_OF_LIGHT.get()))) {
                              double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                       .orElse(new SololevelingModVariables.PlayerVariables())
                                    .MP
                                 - 1000.0;
                              entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                 capability.MP = _setval;
                                 capability.syncPlayerVariables(entity);
                              });
                              CrossStrikeProcedure.execute(world, x, y, z, entity);
                              if (entity instanceof Player _player && !_player.level().isClientSide()) {
                                 _player.displayClientMessage(Component.literal("§l§7Using Cross Strike"), true);
                              }
                           } else {
                              if (entity instanceof Player _player && !_player.level().isClientSide()) {
                                 _player.displayClientMessage(Component.literal("§6Can't Use skill while Sword Dance is active!"), true);
                              }

                              if (world instanceof Level _level) {
                                 if (!_level.isClientSide()) {
                                    _level.playSound(
                                       (Player)null,
                                       BlockPos.containing(x, y, z),
                                       ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                       SoundSource.NEUTRAL,
                                       1.0F,
                                       1.0F
                                    );
                                 } else {
                                    _level.playLocalSound(
                                       x,
                                       y,
                                       z,
                                       ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                       SoundSource.NEUTRAL,
                                       1.0F,
                                       1.0F,
                                       false
                                    );
                                 }
                              }
                           }
                        }
                     } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("You dont have enough MP"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Sword of Light")) {
                     SwordOfLightGiveProcedure.execute(world, entity);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Ground Slam")) {
                     if (entity instanceof LivingEntity _livEnt60 && _livEnt60.hasEffect(SololevelingModMobEffects.SWORD_DANCE.get())
                        || entity instanceof LivingEntity _livEnt61 && _livEnt61.hasEffect(SololevelingModMobEffects.SWORD_OF_LIGHT.get())) {
                        if (entity instanceof Player _player && !_player.level().isClientSide()) {
                           _player.displayClientMessage(Component.literal("§6Can't Use skill while Sword Dance is active!"), true);
                        }

                        if (world instanceof Level _level) {
                           if (!_level.isClientSide()) {
                              _level.playSound(
                                 (Player)null,
                                 BlockPos.containing(x, y, z),
                                 ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                 SoundSource.NEUTRAL,
                                 1.0F,
                                 1.0F
                              );
                           } else {
                              _level.playLocalSound(
                                 x,
                                 y,
                                 z,
                                 ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                 SoundSource.NEUTRAL,
                                 1.0F,
                                 1.0F,
                                 false
                              );
                           }
                        }
                     } else {
                        UpforceSlashProcedure.execute(world, x, y, z, entity);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Sword Dance")) {
                     SwordDanceGiveProcedure.execute(entity);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Slash Fury")) {
                     if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        >= 300.0) {
                        if (!CooldownManager.isOnCooldown(entity, "Slash Fury")) {
                           if (!(entity instanceof LivingEntity _livEnt65 && _livEnt65.hasEffect(SololevelingModMobEffects.SWORD_DANCE.get()))
                              && !(entity instanceof LivingEntity _livEnt66 && _livEnt66.hasEffect(SololevelingModMobEffects.SWORD_OF_LIGHT.get()))) {
                              SlashFurryTestRightclickedProcedure.execute(entity);
                              double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                       .orElse(new SololevelingModVariables.PlayerVariables())
                                    .MP
                                 - 500.0;
                              entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                 capability.MP = _setval;
                                 capability.syncPlayerVariables(entity);
                              });
                              CooldownManager.set(entity, "mana_refresh", 60);
                              if (entity instanceof Player _player && !_player.level().isClientSide()) {
                                 _player.displayClientMessage(Component.literal("§l§7Using Slash Fury"), true);
                              }
                           } else {
                              if (entity instanceof Player _player && !_player.level().isClientSide()) {
                                 _player.displayClientMessage(Component.literal("§6Can't Use skill while Sword Dance is active!"), true);
                              }

                              if (world instanceof Level _level) {
                                 if (!_level.isClientSide()) {
                                    _level.playSound(
                                       (Player)null,
                                       BlockPos.containing(x, y, z),
                                       ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                       SoundSource.NEUTRAL,
                                       1.0F,
                                       1.0F
                                    );
                                 } else {
                                    _level.playLocalSound(
                                       x,
                                       y,
                                       z,
                                       ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                       SoundSource.NEUTRAL,
                                       1.0F,
                                       1.0F,
                                       false
                                    );
                                 }
                              }
                           }
                        }
                     } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("You dont have enough MP"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Heal Beam")) {
                     if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        >= 600.0) {
                        if (!CooldownManager.isOnCooldown(entity, "Heal Beam")) {
                           double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .MP
                              - 600.0;
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                              capability.MP = _setval;
                              capability.syncPlayerVariables(entity);
                           });
                           HealingBeamProcedure.execute(world, entity);
                           if (world instanceof Level _lvl && !_lvl.isClientSide() && entity instanceof ServerPlayer _sp) {
                              ClassPassiveManager.onHealerCast(_sp);
                           }

                           CooldownManager.set(entity, "mana_refresh", 60);
                           if (entity instanceof Player _player && !_player.level().isClientSide()) {
                              _player.displayClientMessage(Component.literal("§a§lUsing Healing"), true);
                           }
                        }
                     } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("You dont have enough MP"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Blessing Mark")) {
                     if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        >= 1500.0) {
                        if (!CooldownManager.isOnCooldown(entity, "Blessing Mark")) {
                           BellOfHealingSummonProcedure.execute(world, x, y, z, entity);
                           if (world instanceof Level _lvl && !_lvl.isClientSide() && entity instanceof ServerPlayer _sp) {
                              ClassPassiveManager.onHealerCast(_sp);
                           }

                           double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .MP
                              - 1500.0;
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                              capability.MP = _setval;
                              capability.syncPlayerVariables(entity);
                           });
                           CooldownManager.set(entity, "mana_refresh", 60);
                           if (entity instanceof Player _player && !_player.level().isClientSide()) {
                              _player.displayClientMessage(Component.literal("§a§lUsing Bell Of Healing"), true);
                           }
                        }
                     } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("You dont have enough MP"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                        .PselectedPower
                        .equals("Purification")
                     && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
                        >= 200.0) {
                     PuricficationCastProcedure.execute(world, x, y, z, entity);
                     double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        - 200.0;
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.MP = _setval;
                        capability.syncPlayerVariables(entity);
                     });
                     if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("§1Using Purification"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Physical Buff")) {
                     PhysicalBuffCastProcedure.execute(world, x, y, z, entity);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Haste Buff")) {
                     HasteBuffCastProcedure.execute(world, x, y, z, entity);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Overheal")) {
                     if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        >= 600.0) {
                        if (!CooldownManager.isOnCooldown(entity, "Overheal")) {
                           OverhealProcedure.execute(world, x, y, z, entity);
                           double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .MP
                              - 600.0;
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                              capability.MP = _setval;
                              capability.syncPlayerVariables(entity);
                           });
                           CooldownManager.set(entity, "mana_refresh", 60);
                           CooldownManager.set(entity, "Overheal", 300);
                           if (entity instanceof Player _player && !_player.level().isClientSide()) {
                              _player.displayClientMessage(Component.literal("§a§lUsing Over Effect"), true);
                           }
                        }
                     } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                        _player.displayClientMessage(Component.literal("You dont have enough MP"), true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Critical Attack")) {
                     if (!(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                           .is(ItemTags.create(new ResourceLocation("dagger")))
                        && !(entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY)
                           .is(ItemTags.create(new ResourceLocation("dagger")))) {
                        if (entity instanceof Player _player && !_player.level().isClientSide()) {
                           _player.displayClientMessage(Component.literal("This is a \"Dagger\" specific skill."), true);
                        }

                        if (world instanceof Level _level && _level.isClientSide()) {
                           _level.playLocalSound(
                              x,
                              y,
                              z,
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              0.6F,
                              false
                           );
                        }
                     } else {
                        entity.getPersistentData().putBoolean("Critical_Attack_Targetting", true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Mutilation")) {
                     if (!(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                           .is(ItemTags.create(new ResourceLocation("dagger")))
                        && !(entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY)
                           .is(ItemTags.create(new ResourceLocation("dagger")))) {
                        if (entity instanceof Player _player && !_player.level().isClientSide()) {
                           _player.displayClientMessage(Component.literal("This is a \"Dagger\" specific skill."), true);
                        }

                        if (world instanceof Level _level && _level.isClientSide()) {
                           _level.playLocalSound(
                              x,
                              y,
                              z,
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              0.6F,
                              false
                           );
                        }
                     } else {
                        entity.getPersistentData().putBoolean("Mutilation_Targetting", true);
                     }
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                     .PselectedPower
                     .equals("Sword Beam")) {
                     SwordBeamAttackProcedure.execute(world, x, y, z, entity);
                  }
               }
            }
         }
      }
   }

   private static void castShadowstep(LevelAccessor world, double x, double y, double z, Entity entity) {
      SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      if (vars.MP < 100.0) {
         if (entity instanceof Player player && !player.level().isClientSide()) {
            player.displayClientMessage(Component.literal("Not enough MP!"), true);
         }
      } else if (!CooldownManager.isOnCooldown(entity, "Shadowstep")) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.MP -= 100.0;
            capability.syncPlayerVariables(entity);
         });
         if (world instanceof ServerLevel level) {
            Entity afterImage = SololevelingModEntities.AFTER_IMAGE.get().spawn(level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
            if (afterImage != null) {
               afterImage.setYRot(world.getRandom().nextFloat() * 360.0F);
            }
         }

         if (world instanceof Level level) {
            if (!level.isClientSide()) {
               level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")),
                  SoundSource.NEUTRAL,
                  1.0F,
                  1.5F
               );
            } else {
               level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.shoot")), SoundSource.NEUTRAL, 1.0F, 1.5F, false
               );
            }
         }

         if (entity instanceof LivingEntity living) {
            if (!living.level().isClientSide()) {
               living.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 1, false, false));
               living.addEffect(new MobEffectInstance(SololevelingModMobEffects.NO_FALL_DAMAGE.get(), 9999, 1, false, false));
            }

            if (!living.level().isClientSide()) {
               ShadowStepEntity projectile = ShadowStepEntity.shoot(
                  living.level(), living, living.getRandom(), 1.5F, 1.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 45.0, 0
               );
               projectile.setPos(living.getX(), living.getEyeY() - 0.1, living.getZ());
            }
         }

         CooldownManager.set(entity, "mana_refresh", 40);
         CooldownManager.set(entity, "Shadowstep", 40);
      }
   }
}
