package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.DivineArrowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class ArrowRainSprayProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double delay = 0.0;
         double a = 0.0;
         double b = 0.0;
         double raytrace_distance = 0.0;
         String found_entity_name = "";
         boolean entity_found = false;
         raytrace_distance = 0.0;
         entity_found = false;

         for (int index0 = 0; index0 < 30; index0++) {
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

         if (entity_found) {
            if (world instanceof ServerLevel _level) {
               Entity entityToSpawn = SololevelingModEntities.ARROW_SPLASH
                  .get()
                  .spawn(
                     _level,
                     BlockPos.containing(
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
                           .getX(),
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
                           .getY(),
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
                           .getZ()
                     ),
                     MobSpawnType.MOB_SUMMONED
                  );
               if (entityToSpawn != null) {
               }
            }

            if (world instanceof ServerLevel projectileLevel) {
               Projectile _entityToSpawn = (new Object() {
                  public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                     AbstractArrow entityToSpawn = new DivineArrowEntity(SololevelingModEntities.DIVINE_ARROW.get(), level);
                     entityToSpawn.setOwner(shooter);
                     entityToSpawn.setBaseDamage(damage);
                     entityToSpawn.setKnockback(knockback);
                     entityToSpawn.setSilent(true);
                     return entityToSpawn;
                  }
               }).getArrow(projectileLevel, entity, (float)(3.0 + TemporaryStatBonusManager.effectiveIntelligence(entity) / 15.0), 1);
               _entityToSpawn.setPos(
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
                     .getX(),
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
                        .getY()
                     + 5.0,
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
                     .getZ()
               );
               _entityToSpawn.shoot(0.0, -2.0, 0.0, 1.0F, 0.0F);
               projectileLevel.addFreshEntity(_entityToSpawn);
            }

            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("Targetting: " + found_entity_name), true);
            }

            if (world instanceof Level _level && _level.isClientSide()) {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")), SoundSource.NEUTRAL, 2.0F, 2.0F, false
               );
            }
         }
      }
   }
}
