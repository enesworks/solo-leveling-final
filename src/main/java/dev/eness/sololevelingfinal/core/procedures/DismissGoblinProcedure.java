package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.GoblinClubShadowEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class DismissGoblinProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).GobShadowMax
            > 0.0) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .GobShadowMax
               - 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.GobShadowMax = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).shadowstorageusage
               - 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.shadowstorageusage = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).GobShadow
               > 0.0) {
               _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .GobShadow
                  - 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.GobShadow = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (!world.getEntitiesOfClass(GoblinClubShadowEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).isEmpty()) {
                  Entity var9 = world.getEntitiesOfClass(GoblinClubShadowEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                     .stream()
                     .sorted((new Object() {
                        Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                           return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                        }
                     }).compareDistOf(x, y, z))
                     .findFirst()
                     .orElse(null);
                  if (var9 instanceof TamableAnimal _tamEnt
                     && _tamEnt.isTame()
                     && !world.getEntitiesOfClass(GoblinClubShadowEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                        .stream()
                        .sorted((new Object() {
                           Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                              return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                           }
                        }).compareDistOf(x, y, z))
                        .findFirst()
                        .orElse(null)
                        .level()
                        .isClientSide()) {
                     world.getEntitiesOfClass(GoblinClubShadowEntity.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true)
                        .stream()
                        .sorted((new Object() {
                           Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                              return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                           }
                        }).compareDistOf(x, y, z))
                        .findFirst()
                        .orElse(null)
                        .discard();
                  }
               }
            }
         }
      }
   }
}
