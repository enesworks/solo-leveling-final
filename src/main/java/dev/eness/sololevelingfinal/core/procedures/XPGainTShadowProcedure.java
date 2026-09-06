package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.AncientSamuraiEntity;
import dev.eness.sololevelingfinal.core.entity.CentipedeEntity;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.IceElfEntity;
import dev.eness.sololevelingfinal.core.entity.MiniGemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.MutatedEntity;
import dev.eness.sololevelingfinal.core.entity.OrcEntity;
import dev.eness.sololevelingfinal.core.entity.PolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.RedAntsEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.entity.StoneGolemEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class XPGainTShadowProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         execute(event, event.getEntity().level(), event.getEntity(), event.getSource().getEntity());
      }
   }

   public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
      execute(null, world, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (sourceentity instanceof TamableAnimal _tamEnt
            && _tamEnt.isTame()
            && (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .Player
            && !(entity instanceof Animal)) {
            if (entity instanceof AncientSamuraiEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 50.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof GoblinArcherEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 5.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof GoblinMageEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 5.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof CentipedeEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 100.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof DKnight1Entity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 20.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof DKnight2Entity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 20.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof DKnight3Entity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 20.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof GoblinClubEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 5.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof IceElfEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 40.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof PolarBearEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 32.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof MiniGemGolemEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 35.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (entity instanceof StoneGolemEntity) {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 30.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            } else if (!(entity instanceof SteelFangWolfEntity) && !(entity instanceof SteelFangedLycanEntity)) {
               if (entity instanceof OrcEntity) {
                  double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Xp
                     + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                              .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .xpmultiplier
                        * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                        * 5.0;
                  (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .ifPresent(capability -> {
                        capability.Xp = _setval;
                        capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                     });
               } else if (entity instanceof RedAntsEntity) {
                  double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Xp
                     + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                              .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .xpmultiplier
                        * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                        * 60.0;
                  (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .ifPresent(capability -> {
                        capability.Xp = _setval;
                        capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                     });
               } else if (entity instanceof MutatedEntity) {
                  double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Xp
                     + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                              .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .xpmultiplier
                        * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                        * 25.0;
                  (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .ifPresent(capability -> {
                        capability.Xp = _setval;
                        capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                     });
               } else if (!world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.SOLO_DUNGEON_PROGRESSION_ONLY)) {
                  double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Xp
                     + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                              .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .xpmultiplier
                        * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                        * 4.0;
                  (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .ifPresent(capability -> {
                        capability.Xp = _setval;
                        capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                     });
               }
            } else {
               double _setval = (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                        .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Xp
                  + (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                           .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .xpmultiplier
                     * (world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER) / 10.0)
                     * 7.0;
               (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.Xp = _setval;
                     capability.syncPlayerVariables(sourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                  });
            }
         }
      }
   }
}
