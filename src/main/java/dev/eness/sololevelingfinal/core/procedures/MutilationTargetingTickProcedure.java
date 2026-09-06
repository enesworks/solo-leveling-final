package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MutilationTargetingTickProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player);
      }
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null) {
         double raytrace_distance = 0.0;
         boolean entity_found = false;
         if (entity.getPersistentData().getBoolean("Mutilation_Targetting")) {
            raytrace_distance = 0.0;
            entity_found = false;

            for (int index0 = 0; index0 < 30; index0++) {
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
                     .isEmpty()
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
                        .orElse(null)
                     == entity) {
                  entity_found = false;
                  raytrace_distance++;
               } else if (world.getEntitiesOfClass(
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
                  .isAlive()) {
                  entity_found = true;
               }
            }

            if (entity_found) {
               Entity var7 = world.getEntitiesOfClass(
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
               if (var7 instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 5, 1, false, false));
               }

               entity.getPersistentData()
                  .putString(
                     "MutilationTarget",
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
                        .getStringUUID()
                  );
            } else {
               entity.getPersistentData().putString("MutilationTarget", "");
            }
         }
      }
   }
}
