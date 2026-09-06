package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class FireReleaseSpreadProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double delay = 0.0;
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
               > 0.0
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
               <= 20.0) {
            if (entity instanceof LivingEntity _entity) {
               _entity.swing(InteractionHand.MAIN_HAND, true);
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.firecharge.use")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     2.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.firecharge.use")), SoundSource.NEUTRAL, 1.0F, 2.0F, false
                  );
               }
            }

            entity.getPersistentData().putDouble("range", 20.0);
            entity.getPersistentData().putDouble("sx", entity.getX());
            entity.getPersistentData().putDouble("sy", entity.getY() + 1.2);
            entity.getPersistentData().putDouble("sz", entity.getZ());
            entity.getPersistentData()
               .putDouble(
                  "tx",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX()
               );
            entity.getPersistentData()
               .putDouble(
                  "ty",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getY()
               );
            entity.getPersistentData()
               .putDouble(
                  "tz",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );
            entity.getPersistentData()
               .putDouble(
                  "range",
                  Math.sqrt(
                     Math.pow(entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx"), 2.0)
                        + Math.pow(entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty"), 2.0)
                        + Math.pow(entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz"), 2.0)
                  )
               );
            entity.getPersistentData()
               .putDouble(
                  "x+",
                  (entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData()
               .putDouble(
                  "y+",
                  (entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData()
               .putDouble(
                  "z+",
                  (entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData().putDouble("size", 0.0);

            for (int index0 = 0; index0 < (int)(entity.getPersistentData().getDouble("range") * 3.0); index0++) {
               delay += 0.05;
               SololevelingMod.queueServerWork(
                  (int)delay,
                  () -> {
                     entity.getPersistentData().putDouble("size", entity.getPersistentData().getDouble("size") + 1.0);
                     entity.getPersistentData().putDouble("sx", entity.getPersistentData().getDouble("sx") + entity.getPersistentData().getDouble("x+") * -0.2);
                     entity.getPersistentData().putDouble("sy", entity.getPersistentData().getDouble("sy") + entity.getPersistentData().getDouble("y+") * -0.2);
                     entity.getPersistentData().putDouble("sz", entity.getPersistentData().getDouble("sz") + entity.getPersistentData().getDouble("z+") * -0.2);
                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(
                           ParticleTypes.FLAME,
                           entity.getPersistentData().getDouble("sx"),
                           entity.getPersistentData().getDouble("sy"),
                           entity.getPersistentData().getDouble("sz"),
                           15,
                           0.5,
                           0.5,
                           0.5,
                           0.1
                        );
                     }

                     Vec3 _center = new Vec3(
                        entity.getPersistentData().getDouble("sx"), entity.getPersistentData().getDouble("sy"), entity.getPersistentData().getDouble("sz")
                     );

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if ((
                              !(entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                       ? _teamEnt.level()
                                          .getScoreboard()
                                          .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                          .getName()
                                       : "")
                                    .equals(
                                       entityiterator instanceof LivingEntity _teamEnt
                                             && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                          ? _teamEnt.level()
                                             .getScoreboard()
                                             .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                             .getName()
                                          : ""
                                    )
                                 || (entity instanceof LivingEntity _teamEnt
                                          && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                       ? _teamEnt.level()
                                          .getScoreboard()
                                          .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                          .getName()
                                       : "")
                                    .equals("")
                           )
                           && entity != entityiterator
                           && !(entityiterator instanceof ExperienceOrb)
                           && !(entityiterator instanceof ItemEntity)) {
                           entityiterator.hurt(
                              new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity),
                              (float)(4L + Math.round(TemporaryStatBonusManager.effectiveIntelligence(entity) / 150.0))
                           );
                           entityiterator.setSecondsOnFire(5);
                        }
                     }
                  }
               );
            }
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
               > 20.0
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
               <= 40.0) {
            if (entity instanceof LivingEntity _entity) {
               _entity.swing(InteractionHand.MAIN_HAND, true);
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.firecharge.use")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     2.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.firecharge.use")), SoundSource.NEUTRAL, 1.0F, 2.0F, false
                  );
               }
            }

            entity.getPersistentData().putDouble("range", 40.0);
            entity.getPersistentData().putDouble("sx", entity.getX());
            entity.getPersistentData().putDouble("sy", entity.getY() + 1.2);
            entity.getPersistentData().putDouble("sz", entity.getZ());
            entity.getPersistentData()
               .putDouble(
                  "tx",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX()
               );
            entity.getPersistentData()
               .putDouble(
                  "ty",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getY()
               );
            entity.getPersistentData()
               .putDouble(
                  "tz",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );
            entity.getPersistentData()
               .putDouble(
                  "range",
                  Math.sqrt(
                     Math.pow(entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx"), 2.0)
                        + Math.pow(entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty"), 2.0)
                        + Math.pow(entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz"), 2.0)
                  )
               );
            entity.getPersistentData()
               .putDouble(
                  "x+",
                  (entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData()
               .putDouble(
                  "y+",
                  (entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData()
               .putDouble(
                  "z+",
                  (entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData().putDouble("size", 0.0);

            for (int index1 = 0; index1 < (int)(entity.getPersistentData().getDouble("range") * 5.0); index1++) {
               delay += 0.05;
               SololevelingMod.queueServerWork(
                  (int)delay,
                  () -> {
                     entity.getPersistentData().putDouble("size", entity.getPersistentData().getDouble("size") + 1.0);
                     entity.getPersistentData().putDouble("sx", entity.getPersistentData().getDouble("sx") + entity.getPersistentData().getDouble("x+") * -0.2);
                     entity.getPersistentData().putDouble("sy", entity.getPersistentData().getDouble("sy") + entity.getPersistentData().getDouble("y+") * -0.2);
                     entity.getPersistentData().putDouble("sz", entity.getPersistentData().getDouble("sz") + entity.getPersistentData().getDouble("z+") * -0.2);
                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(
                           ParticleTypes.FLAME,
                           entity.getPersistentData().getDouble("sx"),
                           entity.getPersistentData().getDouble("sy"),
                           entity.getPersistentData().getDouble("sz"),
                           20,
                           0.8,
                           0.8,
                           0.8,
                           0.1
                        );
                     }

                     Vec3 _center = new Vec3(
                        entity.getPersistentData().getDouble("sx"), entity.getPersistentData().getDouble("sy"), entity.getPersistentData().getDouble("sz")
                     );

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.75), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if ((
                              !(entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                       ? _teamEnt.level()
                                          .getScoreboard()
                                          .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                          .getName()
                                       : "")
                                    .equals(
                                       entityiterator instanceof LivingEntity _teamEnt
                                             && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                          ? _teamEnt.level()
                                             .getScoreboard()
                                             .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                             .getName()
                                          : ""
                                    )
                                 || (entity instanceof LivingEntity _teamEnt
                                          && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                       ? _teamEnt.level()
                                          .getScoreboard()
                                          .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                          .getName()
                                       : "")
                                    .equals("")
                           )
                           && entity != entityiterator
                           && !(entityiterator instanceof ExperienceOrb)
                           && !(entityiterator instanceof ItemEntity)) {
                           entityiterator.hurt(
                              new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity),
                              (float)(5L + Math.round(TemporaryStatBonusManager.effectiveIntelligence(entity) / 100.0))
                           );
                           entityiterator.setSecondsOnFire(8);
                        }
                     }
                  }
               );
            }
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
               > 40.0
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).firecharge
               <= 60.0) {
            if (entity instanceof LivingEntity _entity) {
               _entity.swing(InteractionHand.MAIN_HAND, true);
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.firecharge.use")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     2.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.firecharge.use")), SoundSource.NEUTRAL, 1.0F, 2.0F, false
                  );
               }
            }

            entity.getPersistentData().putDouble("range", 50.0);
            entity.getPersistentData().putDouble("sx", entity.getX());
            entity.getPersistentData().putDouble("sy", entity.getY() + 1.2);
            entity.getPersistentData().putDouble("sz", entity.getZ());
            entity.getPersistentData()
               .putDouble(
                  "tx",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getX()
               );
            entity.getPersistentData()
               .putDouble(
                  "ty",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getY()
               );
            entity.getPersistentData()
               .putDouble(
                  "tz",
                  entity.level()
                     .clip(
                        new ClipContext(
                           entity.getEyePosition(1.0F),
                           entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(30.0)),
                           Block.OUTLINE,
                           Fluid.NONE,
                           entity
                        )
                     )
                     .getBlockPos()
                     .getZ()
               );
            entity.getPersistentData()
               .putDouble(
                  "range",
                  Math.sqrt(
                     Math.pow(entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx"), 2.0)
                        + Math.pow(entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty"), 2.0)
                        + Math.pow(entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz"), 2.0)
                  )
               );
            entity.getPersistentData()
               .putDouble(
                  "x+",
                  (entity.getPersistentData().getDouble("sx") - entity.getPersistentData().getDouble("tx")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData()
               .putDouble(
                  "y+",
                  (entity.getPersistentData().getDouble("sy") - entity.getPersistentData().getDouble("ty")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData()
               .putDouble(
                  "z+",
                  (entity.getPersistentData().getDouble("sz") - entity.getPersistentData().getDouble("tz")) / entity.getPersistentData().getDouble("range")
               );
            entity.getPersistentData().putDouble("size", 0.0);

            for (int index2 = 0; index2 < (int)(entity.getPersistentData().getDouble("range") * 5.0); index2++) {
               delay += 0.05;
               SololevelingMod.queueServerWork(
                  (int)delay,
                  () -> {
                     entity.getPersistentData().putDouble("size", entity.getPersistentData().getDouble("size") + 1.0);
                     entity.getPersistentData().putDouble("sx", entity.getPersistentData().getDouble("sx") + entity.getPersistentData().getDouble("x+") * -0.2);
                     entity.getPersistentData().putDouble("sy", entity.getPersistentData().getDouble("sy") + entity.getPersistentData().getDouble("y+") * -0.2);
                     entity.getPersistentData().putDouble("sz", entity.getPersistentData().getDouble("sz") + entity.getPersistentData().getDouble("z+") * -0.2);
                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(
                           ParticleTypes.FLAME,
                           entity.getPersistentData().getDouble("sx"),
                           entity.getPersistentData().getDouble("sy"),
                           entity.getPersistentData().getDouble("sz"),
                           20,
                           1.2,
                           1.2,
                           1.2,
                           0.1
                        );
                     }

                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(
                           ParticleTypes.SOUL_FIRE_FLAME,
                           entity.getPersistentData().getDouble("sx"),
                           entity.getPersistentData().getDouble("sy"),
                           entity.getPersistentData().getDouble("sz"),
                           8,
                           1.0,
                           1.0,
                           1.0,
                           0.1
                        );
                     }

                     Vec3 _center = new Vec3(
                        entity.getPersistentData().getDouble("sx"), entity.getPersistentData().getDouble("sy"), entity.getPersistentData().getDouble("sz")
                     );

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3.5), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if ((
                              !(entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                       ? _teamEnt.level()
                                          .getScoreboard()
                                          .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                          .getName()
                                       : "")
                                    .equals(
                                       entityiterator instanceof LivingEntity _teamEnt
                                             && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                          ? _teamEnt.level()
                                             .getScoreboard()
                                             .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                             .getName()
                                          : ""
                                    )
                                 || (entity instanceof LivingEntity _teamEnt
                                          && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                       ? _teamEnt.level()
                                          .getScoreboard()
                                          .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                          .getName()
                                       : "")
                                    .equals("")
                           )
                           && entity != entityiterator
                           && !(entityiterator instanceof ExperienceOrb)
                           && !(entityiterator instanceof ItemEntity)) {
                           entityiterator.hurt(
                              new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity),
                              (float)(6L + Math.round(TemporaryStatBonusManager.effectiveIntelligence(entity) / 75.0))
                           );
                        }
                     }
                  }
               );
            }
         }
      }
   }
}
