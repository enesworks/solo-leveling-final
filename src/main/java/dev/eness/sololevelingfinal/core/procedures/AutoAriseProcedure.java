package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.GreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.HighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.PolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class AutoAriseProcedure {
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
         if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).shadowstorageusage
            < sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).shadowstorage
            )
          {
            if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
                  == 1.0
               && (sourceentity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.FEET) : ItemStack.EMPTY).getItem()
                  == SololevelingModItems.SHADOW_ARMOR_BOOTS.get()
               && (sourceentity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.LEGS) : ItemStack.EMPTY).getItem()
                  == SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get()
               && (sourceentity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.CHEST) : ItemStack.EMPTY).getItem()
                  == SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get()
               && (sourceentity instanceof LivingEntity _entGetArmor ? _entGetArmor.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY).getItem()
                  == SololevelingModItems.SHADOW_ARMOR_HELMET.get()) {
               double _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .shadowstorageusage
                  + 1.0;
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.shadowstorageusage = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               if ((
                     entity instanceof Zombie
                        || entity instanceof Husk
                        || entity instanceof Villager
                        || entity instanceof ZombieVillager
                        || entity instanceof Skeleton
                        || entity instanceof Pillager
                        || entity instanceof WitherSkeleton
                        || entity instanceof DKnight1Entity
                        || entity instanceof DKnight2Entity
                        || entity instanceof DKnight3Entity
                        || entity instanceof Drowned
                  )
                  && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 1.0) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .ordshadowmax
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.ordshadowmax = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                        );
                     }
                  }
               } else if (entity instanceof GoblinClubEntity
                  && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 1.0) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .GobShadowMax
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.GobShadowMax = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                        );
                     }
                  }
               } else if (entity instanceof GoblinArcherEntity
                  && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 1.0) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .ShadowGoblinArcherMax
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.ShadowGoblinArcherMax = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                        );
                     }
                  }
               } else if (entity instanceof GoblinMageEntity
                  && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 1.0) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .ShadowGoblinMageMax
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.ShadowGoblinMageMax = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                        );
                     }
                  }
               } else if ((entity instanceof SteelFangWolfEntity || entity instanceof SteelFangedLycanEntity)
                  && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 1.0) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .WolfShadowMax
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.WolfShadowMax = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                        );
                     }
                  }
               } else if (entity instanceof GreenOrcEntity
                  && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 1.0) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .orcmax
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.orcmax = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                        );
                     }
                  }
               } else if (entity instanceof PolarBearEntity
                  && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 1.0) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .polarbearmax
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.polarbearmax = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                        );
                     }
                  }
               } else if (entity instanceof HighOrcEntity
                  && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .JOB
                     == 1.0) {
                  _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .highorcmax
                     + 1.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.highorcmax = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathX = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathY = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  _setval = -80.0;
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.DeathZ = _setval;
                     capability.syncPlayerVariables(sourceentity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                        );
                     }
                  }
               }
            }
         } else if (sourceentity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("§astorage full"), true);
         }
      }
   }
}
