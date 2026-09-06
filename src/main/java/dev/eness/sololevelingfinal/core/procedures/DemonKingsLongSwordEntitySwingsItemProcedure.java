package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class DemonKingsLongSwordEntitySwingsItemProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double delay = 0.0;
         if (!CooldownManager.isOnCooldown(entity, "soff")
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Player) {
            if (entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get())) {
               entity.getPersistentData().putDouble("range", 60.0);
               entity.getPersistentData().putDouble("sx", entity.getX());
               CooldownManager.set(entity, "soff", 100);
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

               for (int index0 = 0; index0 < (int)(entity.getPersistentData().getDouble("range") * 5.0); index0++) {
                  delay += 0.05;
                  SololevelingMod.queueServerWork(
                     (int)delay,
                     () -> {
                        entity.getPersistentData().putDouble("size", entity.getPersistentData().getDouble("size") + 1.0);
                        entity.getPersistentData()
                           .putDouble("sx", entity.getPersistentData().getDouble("sx") + entity.getPersistentData().getDouble("x+") * -0.2);
                        entity.getPersistentData()
                           .putDouble("sy", entity.getPersistentData().getDouble("sy") + entity.getPersistentData().getDouble("y+") * -0.2);
                        entity.getPersistentData()
                           .putDouble("sz", entity.getPersistentData().getDouble("sz") + entity.getPersistentData().getDouble("z+") * -0.2);
                        if (world instanceof ServerLevel _level) {
                           LightningBolt entityToSpawnx = EntityType.LIGHTNING_BOLT.create(_level);
                           entityToSpawnx.moveTo(
                              Vec3.atBottomCenterOf(
                                 BlockPos.containing(entity.getPersistentData().getDouble("sx"), y, entity.getPersistentData().getDouble("sz"))
                              )
                           );
                           entityToSpawnx.setVisualOnly(true);
                           _level.addFreshEntity(entityToSpawnx);
                        }

                        Vec3 _center = new Vec3(
                           entity.getPersistentData().getDouble("sx"), entity.getPersistentData().getDouble("sy"), entity.getPersistentData().getDouble("sz")
                        );

                        for (Entity entityiteratorx : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                           .stream()
                           .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                           .toList()) {
                           if (entity != entityiteratorx && !(entityiteratorx instanceof ExperienceOrb) && !(entityiteratorx instanceof ItemEntity)) {
                              entityiteratorx.hurt(
                                 new DamageSource(
                                    world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), entity
                                 ),
                                 (entity instanceof LivingEntity _livEntx ? _livEntx.getMainHandItem() : ItemStack.EMPTY).getDamageValue() / 3
                              );
                           }
                        }
                     }
                  );
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
               >= 100.0) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12.5), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && (
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
                           || (entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                 ? _teamEnt.level()
                                    .getScoreboard()
                                    .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                    .getName()
                                 : "")
                              .equals("")
                     )
                     && entityiterator != (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)) {
                     if (world instanceof ServerLevel _level) {
                        LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                        entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ())));
                        entityToSpawn.setVisualOnly(true);
                        _level.addFreshEntity(entityToSpawn);
                     }

                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), entity),
                        (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getDamageValue() / 2
                     );
                  }
               }

               CooldownManager.set(entity, "soff", 20);
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .MP
                  - 100.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.MP = _setval;
                  capability.syncPlayerVariables(entity);
               });
            }
         }
      }
   }
}
