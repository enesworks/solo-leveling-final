package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;

public class KargalganOnEntityTickUpdateProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double dirX = 0.0;
         double dirZ = 0.0;
         if ((entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_CooldownCurseMagic) : 0) > 0
            && entity instanceof KargalganEntity _datEntSetI) {
            _datEntSetI.getEntityData()
               .set(
                  KargalganEntity.DATA_CooldownCurseMagic,
                  (entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_CooldownCurseMagic) : 0) - 1
               );
         }

         label158: {
            if (!world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(new Vec3(x, y, z), 7.0, 7.0, 7.0), e -> true).isEmpty()) {
               Entity var32 = world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(new Vec3(x, y, z), 7.0, 7.0, 7.0), e -> true)
                  .stream()
                  .sorted((new Object() {
                     Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                        return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                     }
                  }).compareDistOf(x, y, z))
                  .findFirst()
                  .orElse(null);
               if (!(var32 instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt))
                  && world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(new Vec3(x, y, z), 7.0, 7.0, 7.0), e -> true).stream().sorted((new Object() {
                     Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                        return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                     }
                  }).compareDistOf(x, y, z)).findFirst().orElse(null) != entity) {
                  if (entity instanceof KargalganEntity _datEntSetI) {
                     _datEntSetI.getEntityData()
                        .set(
                           KargalganEntity.DATA_PushTimer,
                           (entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_PushTimer) : 0) + 1
                        );
                  }
                  break label158;
               }
            }

            if (entity instanceof KargalganEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(KargalganEntity.DATA_PushTimer, 0);
            }
         }

         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            if (entity instanceof KargalganEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(KargalganEntity.DATA_AI, (entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_AI) : 0) + 1);
            }

            if ((entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_AI) : 0) == 20) {
               KargalganHymOfDragonProcedure.execute(world, y, entity);
            }

            if ((entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_AI) : 0) == 90
               && entity instanceof KargalganEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(KargalganEntity.DATA_AI, 0);
            }

            if ((entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_AI) : 0) == 130
               && entity instanceof KargalganEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(KargalganEntity.DATA_AI, 0);
            }

            if ((entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_CooldownCurseMagic) : 0) <= 0) {
               KargalganHymnOfAgonyProcedure.execute(world, entity);
               if (entity instanceof KargalganEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(KargalganEntity.DATA_CooldownCurseMagic, 200);
               }
            }

            if ((entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_Push) : 0) >= 3 && !world.isClientSide()) {
               if (entity instanceof KargalganEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(KargalganEntity.DATA_Push, 0);
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12.5), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator) {
                     dirX = (entityiterator.getX() - entity.getX())
                        * (
                           5.0
                              / Math.sqrt(
                                 Math.pow(entityiterator.getX() - entity.getX(), 2.0)
                                    + Math.pow(entityiterator.getY() - entity.getY(), 2.0)
                                    + Math.pow(entityiterator.getZ() - entity.getZ(), 2.0)
                              )
                        );
                     dirZ = (entityiterator.getZ() - entity.getZ())
                        * (
                           5.0
                              / Math.sqrt(
                                 Math.pow(entityiterator.getX() - entity.getX(), 2.0)
                                    + Math.pow(entityiterator.getY() - entity.getY(), 2.0)
                                    + Math.pow(entityiterator.getZ() - entity.getZ(), 2.0)
                              )
                        );
                     entityiterator.setDeltaMovement(new Vec3(dirX, 1.0, dirZ));
                  }
               }
            }

            if ((entity instanceof KargalganEntity _datEntI ? _datEntI.getEntityData().get(KargalganEntity.DATA_PushTimer) : 0) >= 60 && !world.isClientSide()) {
               if (entity instanceof KargalganEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(KargalganEntity.DATA_PushTimer, 0);
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(12.5), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator) {
                     dirX = (entityiterator.getX() - entity.getX())
                        * (
                           5.0
                              / Math.sqrt(
                                 Math.pow(entityiterator.getX() - entity.getX(), 2.0)
                                    + Math.pow(entityiterator.getY() - entity.getY(), 2.0)
                                    + Math.pow(entityiterator.getZ() - entity.getZ(), 2.0)
                              )
                        );
                     dirZ = (entityiterator.getZ() - entity.getZ())
                        * (
                           5.0
                              / Math.sqrt(
                                 Math.pow(entityiterator.getX() - entity.getX(), 2.0)
                                    + Math.pow(entityiterator.getY() - entity.getY(), 2.0)
                                    + Math.pow(entityiterator.getZ() - entity.getZ(), 2.0)
                              )
                        );
                     entityiterator.setDeltaMovement(new Vec3(dirX, 1.0, dirZ));
                  }
               }
            }
         }
      }
   }
}
