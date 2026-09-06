package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.BeruBossEntity;
import dev.eness.sololevelingfinal.core.entity.GemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisEntity;
import dev.eness.sololevelingfinal.core.entity.KasakaEntity;
import dev.eness.sololevelingfinal.core.entity.SpiderBossEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ShadowNavToBossProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))) {
            if (entity.level().dimension() == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_kasaka"))) {
               if (!world.getEntitiesOfClass(KasakaEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).isEmpty()) {
                  if (entity instanceof Mob _entity) {
                     _entity.getNavigation()
                        .moveTo(
                           world.getEntitiesOfClass(KasakaEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getX(),
                           world.getEntitiesOfClass(KasakaEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getY(),
                           world.getEntitiesOfClass(KasakaEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getZ(),
                           1.0
                        );
                  }

                  boolean _setval = false;
                  (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .ifPresent(capability -> {
                        capability.berserk = _setval;
                        capability.syncPlayerVariables(entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null);
                     });
               }
            } else if (entity.level().dimension() == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:deleted_mod_element"))) {
               if (!world.getEntitiesOfClass(BeruBossEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).isEmpty()) {
                  if (entity instanceof Mob _entity) {
                     _entity.getNavigation()
                        .moveTo(
                           world.getEntitiesOfClass(BeruBossEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getX(),
                           world.getEntitiesOfClass(BeruBossEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getY(),
                           world.getEntitiesOfClass(BeruBossEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getZ(),
                           1.0
                        );
                  }

                  boolean _setval = false;
                  (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .ifPresent(capability -> {
                        capability.berserk = _setval;
                        capability.syncPlayerVariables(entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null);
                     });
               }
            } else if (entity.level().dimension()
               == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_igris"))) {
               if (!world.getEntitiesOfClass(IgrisEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).isEmpty()) {
                  if (entity instanceof Mob _entity) {
                     _entity.getNavigation()
                        .moveTo(
                           world.getEntitiesOfClass(IgrisEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getX(),
                           world.getEntitiesOfClass(IgrisEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getY(),
                           world.getEntitiesOfClass(IgrisEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getZ(),
                           1.0
                        );
                  }

                  boolean _setval = false;
                  (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .ifPresent(capability -> {
                        capability.berserk = _setval;
                        capability.syncPlayerVariables(entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null);
                     });
               }
            } else if (entity.level().dimension() == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:deleted_mod_element"))) {
               if (!world.getEntitiesOfClass(GemGolemEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).isEmpty()) {
                  if (entity instanceof Mob _entity) {
                     _entity.getNavigation()
                        .moveTo(
                           world.getEntitiesOfClass(GemGolemEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getX(),
                           world.getEntitiesOfClass(GemGolemEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getY(),
                           world.getEntitiesOfClass(GemGolemEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                              .stream()
                              .sorted((new Object() {
                                 Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                    return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                                 }
                              }).compareDistOf(x, y, z))
                              .findFirst()
                              .orElse(null)
                              .getZ(),
                           1.0
                        );
                  }

                  boolean _setval = false;
                  (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .ifPresent(capability -> {
                        capability.berserk = _setval;
                        capability.syncPlayerVariables(entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null);
                     });
               }
            } else if (entity.level().dimension() == ResourceKey.<Level>create(Registries.DIMENSION, new ResourceLocation("sololeveling:deleted_mod_element"))
               && !world.getEntitiesOfClass(SpiderBossEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).isEmpty()) {
               if (entity instanceof Mob _entity) {
                  _entity.getNavigation()
                     .moveTo(
                        world.getEntitiesOfClass(SpiderBossEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                           .stream()
                           .sorted((new Object() {
                              Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                 return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                              }
                           }).compareDistOf(x, y, z))
                           .findFirst()
                           .orElse(null)
                           .getX(),
                        world.getEntitiesOfClass(SpiderBossEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                           .stream()
                           .sorted((new Object() {
                              Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                 return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                              }
                           }).compareDistOf(x, y, z))
                           .findFirst()
                           .orElse(null)
                           .getY(),
                        world.getEntitiesOfClass(SpiderBossEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                           .stream()
                           .sorted((new Object() {
                              Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                                 return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                              }
                           }).compareDistOf(x, y, z))
                           .findFirst()
                           .orElse(null)
                           .getZ(),
                        1.0
                     );
               }

               boolean _setval = false;
               (entity instanceof TamableAnimal _tamEnt ? _tamEnt.getOwner() : null)
                  .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .ifPresent(capability -> {
                     capability.berserk = _setval;
                     capability.syncPlayerVariables(entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null);
                  });
            }
         }
      }
   }
}
