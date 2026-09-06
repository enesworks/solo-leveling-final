package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.IceChunkEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class CastIceChunkProcedure {
   public static void execute(LevelAccessor world, double y, Entity entity) {
      if (entity != null) {
         double x = 0.0;
         double z = 0.0;
         double yaw = 0.0;
         if (!world.isClientSide() && !CooldownManager.isOnCooldown(entity, "job_2")) {
            if (entity.onGround()) {
               CooldownManager.set(entity, "job_2", 140);
               x = entity.getX() + 8.0 * entity.getLookAngle().x;
               z = entity.getZ() + 8.0 * entity.getLookAngle().z;
               yaw = entity.getYRot();
               entity.getPersistentData().putDouble("IceChunkX1", entity.getX() + 4.0 * entity.getLookAngle().x);
               entity.getPersistentData().putDouble("IceChunkX2", entity.getX() + 8.0 * entity.getLookAngle().x);
               entity.getPersistentData().putDouble("IceChunkX3", entity.getX() + 12.0 * entity.getLookAngle().x);
               entity.getPersistentData().putDouble("IceChunkZ1", entity.getZ() + 4.0 * entity.getLookAngle().z);
               entity.getPersistentData().putDouble("IceChunkZ2", entity.getZ() + 8.0 * entity.getLookAngle().z);
               entity.getPersistentData().putDouble("IceChunkZ3", entity.getZ() + 12.0 * entity.getLookAngle().z);
               Entity _ent = entity;
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
                        "summon sololeveling:ice_chunk " + x + " ~ " + z + " {Rotation:[" + yaw + "f,0f]}"
                     );
               }

               SololevelingMod.queueServerWork(
                  8,
                  () -> {
                     Vec3 _center = new Vec3(entity.getPersistentData().getDouble("IceChunkX1"), y, entity.getPersistentData().getDouble("IceChunkZ1"));

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if (entity != entityiterator
                           && !(entityiterator instanceof ExperienceOrb)
                           && !(entityiterator instanceof IceChunkEntity)
                           && !(entityiterator instanceof ItemEntity)) {
                           entityiterator.hurt(
                              new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC), entity),
                              10.0F
                           );
                           if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                              _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.FREEZE.get(), 120, 1, false, false));
                           }
                        }
                     }

                     SololevelingMod.queueServerWork(
                        6,
                        () -> {
                           Vec3 _centerx = new Vec3(entity.getPersistentData().getDouble("IceChunkX2"), y, entity.getPersistentData().getDouble("IceChunkZ2"));

                           for (Entity entityiteratorx : world.getEntitiesOfClass(Entity.class, new AABB(_centerx, _centerx).inflate(3.0), e -> true)
                              .stream()
                              .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_centerx)))
                              .toList()) {
                              if (entity != entityiteratorx
                                 && !(entityiteratorx instanceof ExperienceOrb)
                                 && !(entityiteratorx instanceof IceChunkEntity)
                                 && !(entityiteratorx instanceof ItemEntity)) {
                                 entityiteratorx.hurt(
                                    new DamageSource(
                                       world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC), entity
                                    ),
                                    10.0F
                                 );
                                 if (entityiteratorx instanceof LivingEntity _entityx && !_entityx.level().isClientSide()) {
                                    _entityx.addEffect(new MobEffectInstance(SololevelingModMobEffects.FREEZE.get(), 120, 1, false, false));
                                 }
                              }
                           }

                           SololevelingMod.queueServerWork(
                              6,
                              () -> {
                                 Vec3 _centerxx = new Vec3(
                                    entity.getPersistentData().getDouble("IceChunkX3"), y, entity.getPersistentData().getDouble("IceChunkZ3")
                                 );

                                 for (Entity entityiteratorxx : world.getEntitiesOfClass(Entity.class, new AABB(_centerxx, _centerxx).inflate(4.0), e -> true)
                                    .stream()
                                    .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_centerxx)))
                                    .toList()) {
                                    if (entity != entityiteratorxx
                                       && !(entityiteratorxx instanceof ExperienceOrb)
                                       && !(entityiteratorxx instanceof IceChunkEntity)
                                       && !(entityiteratorxx instanceof ItemEntity)) {
                                       entityiteratorxx.hurt(
                                          new DamageSource(
                                             world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC), entity
                                          ),
                                          10.0F
                                       );
                                       if (entityiteratorxx instanceof LivingEntity _entityxx && !_entityxx.level().isClientSide()) {
                                          _entityxx.addEffect(new MobEffectInstance(SololevelingModMobEffects.FREEZE.get(), 120, 1, false, false));
                                       }
                                    }
                                 }
                              }
                           );
                        }
                     );
                  }
               );
            } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("You need to be grounded to use this move!"), true);
            }
         }
      }
   }
}
