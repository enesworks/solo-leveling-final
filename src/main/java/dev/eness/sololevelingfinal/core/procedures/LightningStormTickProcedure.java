package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class LightningStormTickProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player.getX(), event.player.getY(), event.player.getZ(), event.player);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (world.dayTime() % 20L == 0L
            && !world.isClientSide()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).baranlightningstrike
               > 0.0) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .baranlightningstrike
               - 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.baranlightningstrike = _setval;
               capability.syncPlayerVariables(entity);
            });

            for (int index0 = 0; index0 < Mth.nextInt(RandomSource.create(), 5, 10); index0++) {
               entity.getPersistentData().putBoolean("foundRand", false);

               while (!entity.getPersistentData().getBoolean("foundRand")) {
                  entity.getPersistentData().putDouble("rx", entity.getX() + Mth.nextInt(RandomSource.create(), -25, 25));
                  entity.getPersistentData().putDouble("ry", entity.getY() + Mth.nextInt(RandomSource.create(), -25, 25));
                  entity.getPersistentData().putDouble("rz", entity.getZ() + Mth.nextInt(RandomSource.create(), -25, 25));
                  if (world.getBlockState(
                              BlockPos.containing(
                                 entity.getPersistentData().getDouble("rx"),
                                 entity.getPersistentData().getDouble("ry"),
                                 entity.getPersistentData().getDouble("rz")
                              )
                           )
                           .getBlock()
                        == Blocks.AIR
                     && world.getBlockFloorHeight(
                           BlockPos.containing(
                              entity.getPersistentData().getDouble("rx"),
                              entity.getPersistentData().getDouble("ry") - 1.0,
                              entity.getPersistentData().getDouble("rz")
                           )
                        )
                        > 0.0) {
                     entity.getPersistentData().putBoolean("foundRand", true);
                     if (world instanceof ServerLevel _level) {
                        LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                        entityToSpawn.moveTo(
                           Vec3.atBottomCenterOf(
                              BlockPos.containing(
                                 entity.getPersistentData().getDouble("rx"),
                                 entity.getPersistentData().getDouble("ry"),
                                 entity.getPersistentData().getDouble("rz")
                              )
                           )
                        );
                        entityToSpawn.setVisualOnly(true);
                        _level.addFreshEntity(entityToSpawn);
                     }

                     Vec3 _center = new Vec3(
                        entity.getPersistentData().getDouble("rx"), entity.getPersistentData().getDouble("ry"), entity.getPersistentData().getDouble("rz")
                     );

                     for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(0.5), e -> true)
                        .stream()
                        .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                        .toList()) {
                        if (entity != entityiterator && entityiterator instanceof LivingEntity) {
                        }
                     }
                  }
               }
            }

            if (world.dayTime() % 40L == 0L) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(7.5), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator && entityiterator instanceof LivingEntity) {
                     entityiterator.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity), 5.0F
                     );
                     if (world instanceof ServerLevel _level) {
                        LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                        entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ())));
                        entityToSpawn.setVisualOnly(true);
                        _level.addFreshEntity(entityToSpawn);
                     }
                  }
               }
            }
         }
      }
   }
}
