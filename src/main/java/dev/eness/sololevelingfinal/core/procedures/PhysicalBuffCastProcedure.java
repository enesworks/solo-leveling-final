package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class PhysicalBuffCastProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double raytrace_distance = 0.0;
         String found_entity_name = "";
         boolean entity_found = false;
         if (!CooldownManager.isOnCooldown(entity, "Physical Buff")) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
               >= 600.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 10.0) {
               if (!entity.isShiftKeyDown()) {
                  raytrace_distance = 0.0;
                  entity_found = false;

                  for (int index0 = 0; index0 < 15; index0++) {
                     if (!world.getEntitiesOfClass(
                              LivingEntity.class,
                              AABB.ofSize(
                                 new Vec3(
                                    entity.level()
                                       .clip(
                                          new ClipContext(
                                             entity.getEyePosition(1.0F),
                                             entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                             Block.COLLIDER,
                                             Fluid.NONE,
                                             entity
                                          )
                                       )
                                       .getBlockPos()
                                       .getX(),
                                    entity.level()
                                       .clip(
                                          new ClipContext(
                                             entity.getEyePosition(1.0F),
                                             entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                             Block.COLLIDER,
                                             Fluid.NONE,
                                             entity
                                          )
                                       )
                                       .getBlockPos()
                                       .getY(),
                                    entity.level()
                                       .clip(
                                          new ClipContext(
                                             entity.getEyePosition(1.0F),
                                             entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                             Block.COLLIDER,
                                             Fluid.NONE,
                                             entity
                                          )
                                       )
                                       .getBlockPos()
                                       .getZ()
                                 ),
                                 1.0,
                                 1.0,
                                 1.0
                              ),
                              e -> true
                           )
                           .isEmpty()
                        && world.getEntitiesOfClass(
                                 LivingEntity.class,
                                 AABB.ofSize(
                                    new Vec3(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    ),
                                    1.0,
                                    1.0,
                                    1.0
                                 ),
                                 e -> true
                              )
                              .stream()
                              .sorted(
                                 (new Object() {
                                       Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                          return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                       }
                                    })
                                    .compareDistOf(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    )
                              )
                              .findFirst()
                              .orElse(null)
                           != entity) {
                        entity_found = true;
                        if (world.getEntitiesOfClass(
                                 LivingEntity.class,
                                 AABB.ofSize(
                                    new Vec3(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    ),
                                    1.0,
                                    1.0,
                                    1.0
                                 ),
                                 e -> true
                              )
                              .stream()
                              .sorted(
                                 (new Object() {
                                       Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                          return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                       }
                                    })
                                    .compareDistOf(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    )
                              )
                              .findFirst()
                              .orElse(null)
                           != null) {
                           found_entity_name = world.getEntitiesOfClass(
                                 LivingEntity.class,
                                 AABB.ofSize(
                                    new Vec3(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    ),
                                    1.0,
                                    1.0,
                                    1.0
                                 ),
                                 e -> true
                              )
                              .stream()
                              .sorted(
                                 (new Object() {
                                       Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                          return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                       }
                                    })
                                    .compareDistOf(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    )
                              )
                              .findFirst()
                              .orElse(null)
                              .getDisplayName()
                              .getString();
                        }
                     } else {
                        entity_found = false;
                        raytrace_distance++;
                     }
                  }

                  if (entity_found
                     && (
                        world.getEntitiesOfClass(
                                 LivingEntity.class,
                                 AABB.ofSize(
                                    new Vec3(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    ),
                                    1.0,
                                    1.0,
                                    1.0
                                 ),
                                 e -> true
                              )
                              .stream()
                              .sorted(
                                 (new Object() {
                                       Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                          return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                       }
                                    })
                                    .compareDistOf(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    )
                              )
                              .findFirst()
                              .orElse(null) instanceof ServerPlayer
                           || world.getEntitiesOfClass(
                                 LivingEntity.class,
                                 AABB.ofSize(
                                    new Vec3(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    ),
                                    1.0,
                                    1.0,
                                    1.0
                                 ),
                                 e -> true
                              )
                              .stream()
                              .sorted(
                                 (new Object() {
                                       Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                          return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                       }
                                    })
                                    .compareDistOf(
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getX(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getY(),
                                       entity.level()
                                          .clip(
                                             new ClipContext(
                                                entity.getEyePosition(1.0F),
                                                entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                Block.COLLIDER,
                                                Fluid.NONE,
                                                entity
                                             )
                                          )
                                          .getBlockPos()
                                          .getZ()
                                    )
                              )
                              .findFirst()
                              .orElse(null) instanceof Player
                     )
                     && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                        .party
                        .equals(
                           world.getEntitiesOfClass(
                                    LivingEntity.class,
                                    AABB.ofSize(
                                       new Vec3(
                                          entity.level()
                                             .clip(
                                                new ClipContext(
                                                   entity.getEyePosition(1.0F),
                                                   entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                   Block.COLLIDER,
                                                   Fluid.NONE,
                                                   entity
                                                )
                                             )
                                             .getBlockPos()
                                             .getX(),
                                          entity.level()
                                             .clip(
                                                new ClipContext(
                                                   entity.getEyePosition(1.0F),
                                                   entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                   Block.COLLIDER,
                                                   Fluid.NONE,
                                                   entity
                                                )
                                             )
                                             .getBlockPos()
                                             .getY(),
                                          entity.level()
                                             .clip(
                                                new ClipContext(
                                                   entity.getEyePosition(1.0F),
                                                   entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                   Block.COLLIDER,
                                                   Fluid.NONE,
                                                   entity
                                                )
                                             )
                                             .getBlockPos()
                                             .getZ()
                                       ),
                                       1.0,
                                       1.0,
                                       1.0
                                    ),
                                    e -> true
                                 )
                                 .stream()
                                 .sorted(
                                    (new Object() {
                                          Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                             return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                          }
                                       })
                                       .compareDistOf(
                                          entity.level()
                                             .clip(
                                                new ClipContext(
                                                   entity.getEyePosition(1.0F),
                                                   entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                   Block.COLLIDER,
                                                   Fluid.NONE,
                                                   entity
                                                )
                                             )
                                             .getBlockPos()
                                             .getX(),
                                          entity.level()
                                             .clip(
                                                new ClipContext(
                                                   entity.getEyePosition(1.0F),
                                                   entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                   Block.COLLIDER,
                                                   Fluid.NONE,
                                                   entity
                                                )
                                             )
                                             .getBlockPos()
                                             .getY(),
                                          entity.level()
                                             .clip(
                                                new ClipContext(
                                                   entity.getEyePosition(1.0F),
                                                   entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                                   Block.COLLIDER,
                                                   Fluid.NONE,
                                                   entity
                                                )
                                             )
                                             .getBlockPos()
                                             .getZ()
                                       )
                                 )
                                 .findFirst()
                                 .orElse(null)
                                 .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .party
                        )) {
                     Entity var13 = world.getEntitiesOfClass(
                           LivingEntity.class,
                           AABB.ofSize(
                              new Vec3(
                                 entity.level()
                                    .clip(
                                       new ClipContext(
                                          entity.getEyePosition(1.0F),
                                          entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                          Block.COLLIDER,
                                          Fluid.NONE,
                                          entity
                                       )
                                    )
                                    .getBlockPos()
                                    .getX(),
                                 entity.level()
                                    .clip(
                                       new ClipContext(
                                          entity.getEyePosition(1.0F),
                                          entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                          Block.COLLIDER,
                                          Fluid.NONE,
                                          entity
                                       )
                                    )
                                    .getBlockPos()
                                    .getY(),
                                 entity.level()
                                    .clip(
                                       new ClipContext(
                                          entity.getEyePosition(1.0F),
                                          entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                          Block.COLLIDER,
                                          Fluid.NONE,
                                          entity
                                       )
                                    )
                                    .getBlockPos()
                                    .getZ()
                              ),
                              1.0,
                              1.0,
                              1.0
                           ),
                           e -> true
                        )
                        .stream()
                        .sorted(
                           (new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              })
                              .compareDistOf(
                                 entity.level()
                                    .clip(
                                       new ClipContext(
                                          entity.getEyePosition(1.0F),
                                          entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                          Block.COLLIDER,
                                          Fluid.NONE,
                                          entity
                                       )
                                    )
                                    .getBlockPos()
                                    .getX(),
                                 entity.level()
                                    .clip(
                                       new ClipContext(
                                          entity.getEyePosition(1.0F),
                                          entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                          Block.COLLIDER,
                                          Fluid.NONE,
                                          entity
                                       )
                                    )
                                    .getBlockPos()
                                    .getY(),
                                 entity.level()
                                    .clip(
                                       new ClipContext(
                                          entity.getEyePosition(1.0F),
                                          entity.getEyePosition(1.0F).add(entity.getViewVector(1.0F).scale(raytrace_distance)),
                                          Block.COLLIDER,
                                          Fluid.NONE,
                                          entity
                                       )
                                    )
                                    .getBlockPos()
                                    .getZ()
                              )
                        )
                        .findFirst()
                        .orElse(null);
                     if (var13 instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(
                           new MobEffectInstance(
                              SololevelingModMobEffects.PHYSICAL_BUFF.get(),
                              (int)(600.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 10.0),
                              0,
                              false,
                              false
                           )
                        );
                     }

                     double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .MP
                        - (600.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 10.0);
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.MP = _setval;
                        capability.syncPlayerVariables(entity);
                     });
                     _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .progression_healer
                        + 2.0;
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.progression_healer = _setval;
                        capability.syncPlayerVariables(entity);
                     });
                     CooldownManager.set(entity, "mana_refresh", 100);
                     CooldownManager.set(entity, "Physical Buff", (int)((600.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 10.0) * 1.5));
                     if (world instanceof Level _level) {
                        if (!_level.isClientSide()) {
                           _level.playSound(
                              (Player)null,
                              BlockPos.containing(x, y, z),
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              2.0F
                           );
                        } else {
                           _level.playLocalSound(
                              x,
                              y,
                              z,
                              ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              2.0F,
                              false
                           );
                        }
                     }
                  }
               } else {
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(
                        new MobEffectInstance(
                           SololevelingModMobEffects.PHYSICAL_BUFF.get(),
                           (int)(600.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 10.0),
                           0,
                           false,
                           false
                        )
                     );
                  }

                  CooldownManager.set(entity, "mana_refresh", 100);
                  CooldownManager.set(entity, "Physical Buff", (int)((600.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 10.0) * 1.5));
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           2.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")), SoundSource.NEUTRAL, 1.0F, 2.0F, false
                        );
                     }
                  }

                  double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .MP
                     - (600.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) * 10.0);
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.MP = _setval;
                     capability.syncPlayerVariables(entity);
                  });
               }
            } else {
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("Not Enough Mana!"), true);
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
         } else {
            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("Ability on Cooldown!"), true);
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
   }
}
