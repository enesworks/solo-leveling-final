package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.ManaArrowEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

@EventBusSubscriber
public class MasteryGainProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, event.getSource(), entity, event.getSource().getEntity(), event.getAmount());
      }
   }

   public static void execute(DamageSource damagesource, Entity entity, Entity sourceentity, double amount) {
      execute(null, damagesource, entity, sourceentity, amount);
   }

   private static void execute(@Nullable Event event, DamageSource damagesource, Entity entity, Entity sourceentity, double amount) {
      if (damagesource != null && entity != null && sourceentity != null) {
         if (!entity.getPersistentData().getBoolean("radiru_training_dummy")) {
            if (sourceentity instanceof Player) {
               double multiplier = 0.0;
               if (entity != sourceentity) {
                  if (amount >= 1.0) {
                     if (playerClass(sourceentity) != 1.0 || !isAssassinDamage(damagesource, sourceentity)) {
                        if (damagesource.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:fighter")))) {
                           if (!CooldownManager.isOnCooldown(sourceentity, "mastery")) {
                              double _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                       .orElse(new SololevelingModVariables.PlayerVariables())
                                    .progression_fighter
                                 + 1.0;
                              sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                 capability.progression_fighter = _setval;
                                 capability.syncPlayerVariables(sourceentity);
                              });
                              CooldownManager.set(sourceentity, "mastery", 10);
                           }
                        } else if (!damagesource.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:mage")))
                           || !(sourceentity instanceof Player)
                           || playerClass(sourceentity) != 2.0) {
                           if (damagesource.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:tanker")))) {
                              if (!CooldownManager.isOnCooldown(sourceentity, "mastery")) {
                                 double _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                          .orElse(new SololevelingModVariables.PlayerVariables())
                                       .progression_tanker
                                    + 1.0;
                                 sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                    capability.progression_tanker = _setval;
                                    capability.syncPlayerVariables(sourceentity);
                                 });
                                 CooldownManager.set(sourceentity, "mastery", 10);
                              }
                           } else if (isRangerDamage(damagesource) && !CooldownManager.isOnCooldown(sourceentity, "mastery")) {
                              double _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                       .orElse(new SololevelingModVariables.PlayerVariables())
                                    .progression_ranger
                                 + 1.0;
                              sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                 capability.progression_ranger = _setval;
                                 capability.syncPlayerVariables(sourceentity);
                              });
                              CooldownManager.set(sourceentity, "mastery", 10);
                           }
                        } else if (!CooldownManager.isOnCooldown(sourceentity, "mastery")) {
                           double _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .progression_mage
                              + 1.0;
                           sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                              capability.progression_mage = _setval;
                              capability.syncPlayerVariables(sourceentity);
                           });
                           CooldownManager.set(sourceentity, "mastery", 10);
                        }
                     } else if (!CooldownManager.isOnCooldown(sourceentity, "mastery")) {
                        double _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .progression_assassin
                           + 1.0;
                        sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                           capability.progression_assassin = _setval;
                           capability.syncPlayerVariables(sourceentity);
                        });
                        CooldownManager.set(sourceentity, "mastery", 10);
                     }
                  }

                  if (playerClass(sourceentity) == 1.0
                     && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_assassin
                        > sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .progression_multiplier_assassin
                           * 7.0) {
                     double _setval = 0.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_assassin = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_assassin
                        + 3.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_multiplier_assassin = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     MasterylvlupassassinProcedure.execute(sourceentity);
                  } else if (sourceentity instanceof Player
                     && playerClass(sourceentity) == 2.0
                     && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_mage
                        > sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .progression_multiplier_mage
                           * 7.0) {
                     double _setval = 0.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_mage = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_mage
                        + 3.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_multiplier_mage = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     MasterylvlupMageProcedure.execute(sourceentity);
                  } else if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .progression_fighter
                     > sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_fighter
                        * 7.0) {
                     double _setval = 0.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_fighter = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_fighter
                        + 3.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_multiplier_fighter = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     MasterylvlupFighterProcedure.execute(sourceentity);
                  } else if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .progression_tanker
                     > sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_tanker
                        * 7.0) {
                     double _setval = 0.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_tanker = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_tanker
                        + 1.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_multiplier_tanker = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     MasterylvlupTankerProcedure.execute(sourceentity);
                  } else if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .progression_healer
                     > sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_healer
                        * 6.0) {
                     double _setval = 0.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_healer = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_healer
                        + 1.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_multiplier_healer = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     MasterylvlupHealerProcedure.execute(sourceentity);
                  } else if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .progression_ranger
                     > sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_ranger
                        * 10.0) {
                     double _setval = 0.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_ranger = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_multiplier_ranger
                        + 1.0;
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_multiplier_ranger = _setval;
                        capability.syncPlayerVariables(sourceentity);
                     });
                     MasterylvlupRangerProcedure.execute(sourceentity);
                  }
               }
            }
         }
      }
   }

   private static boolean isAssassinDamage(DamageSource damagesource, Entity sourceentity) {
      if (damagesource.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin")))) {
         return true;
      } else {
         return damagesource.is(DamageTypes.PLAYER_ATTACK) && sourceentity instanceof LivingEntity living
            ? living.getMainHandItem().is(ItemTags.create(new ResourceLocation("dagger")))
               || living.getOffhandItem().is(ItemTags.create(new ResourceLocation("dagger")))
            : false;
      }
   }

   private static boolean isRangerDamage(DamageSource damagesource) {
      return damagesource.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:ranger")))
         || damagesource.getDirectEntity() instanceof ManaArrowEntity;
   }

   private static double playerClass(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes;
   }
}
