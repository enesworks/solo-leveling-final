package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class MutilationUseProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double raytrace_distance = 0.0;
         Entity ent = null;
         boolean entity_found = false;
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
                        2.0,
                        2.0,
                        2.0
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
                           2.0,
                           2.0,
                           2.0
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
                  == entity
               || !entity.getPersistentData()
                  .getString("MutilationTarget")
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
                              2.0,
                              2.0,
                              2.0
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
                  )) {
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
                     2.0,
                     2.0,
                     2.0
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
               ent = world.getEntitiesOfClass(
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
                        2.0,
                        2.0,
                        2.0
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
               entity_found = true;
            }
         }

         if (entity_found) {
            Entity _ent = entity;
            _ent.teleportTo(ent.getX() + -1.5 * entity.getLookAngle().x, ent.getY(), ent.getZ() + -1.5 * entity.getLookAngle().z);
            if (_ent instanceof ServerPlayer _serverPlayer) {
               _serverPlayer.connection
                  .teleport(
                     ent.getX() + -1.5 * entity.getLookAngle().x, ent.getY(), ent.getZ() + -1.5 * entity.getLookAngle().z, _ent.getYRot(), _ent.getXRot()
                  );
            }

            Vec3 _center = new Vec3(entity.getX(), entity.getY(), entity.getZ());

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(8.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entityiterator != entity
                  && !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt))
                  && entityiterator instanceof LivingEntity) {
                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(
                        SololevelingModParticleTypes.GOODSLASH_3.get(),
                        entityiterator.getX(),
                        entityiterator.getY() + 2.0F * entityiterator.getBbHeight() / 3.0F,
                        entityiterator.getZ(),
                        2,
                        0.35,
                        1.0,
                        0.35,
                        0.0
                     );
                  }

                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(
                        SololevelingModParticleTypes.GOODSLASH_1.get(),
                        entityiterator.getX(),
                        entityiterator.getY() + 2.0F * entityiterator.getBbHeight() / 3.0F,
                        entityiterator.getZ(),
                        2,
                        0.35,
                        1.0,
                        0.35,
                        0.0
                     );
                  }

                  entityiterator.hurt(
                     new DamageSource(
                        world.registryAccess()
                           .registryOrThrow(Registries.DAMAGE_TYPE)
                           .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
                        entity
                     ),
                     (float)(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getValue() * 3.0)
                  );
               }
            }

            if (entity instanceof LivingEntity _entity) {
               _entity.swing(InteractionHand.MAIN_HAND, true);
            }

            entity.getPersistentData().putBoolean("Mutilation_Targetting", false);
            entity.getPersistentData().putString(entity.getPersistentData().getString("MutilationTarget"), "");
         }
      }
   }
}
