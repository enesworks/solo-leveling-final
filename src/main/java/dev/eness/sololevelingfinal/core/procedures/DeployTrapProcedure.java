package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class DeployTrapProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (world instanceof ServerLevel _level) {
            Entity entityToSpawn = SololevelingModEntities.BEAR_TRAP.get().spawn(_level, BlockPos.containing(x, y + 1.5, z), MobSpawnType.MOB_SUMMONED);
            if (entityToSpawn != null) {
               entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
            }
         }

         if (!world.getEntitiesOfClass(BearTrapEntity.class, AABB.ofSize(new Vec3(x, y, z), 5.0, 5.0, 5.0), e -> true).isEmpty()) {
            Entity var13 = world.getEntitiesOfClass(BearTrapEntity.class, AABB.ofSize(new Vec3(x, y, z), 5.0, 5.0, 5.0), e -> true)
               .stream()
               .sorted((new Object() {
                  Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                     return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                  }
               }).compareDistOf(x, y, z))
               .findFirst()
               .orElse(null);
            if (!(var13 instanceof TamableAnimal _tamEnt && _tamEnt.isTame())) {
               Entity var11 = world.getEntitiesOfClass(BearTrapEntity.class, AABB.ofSize(new Vec3(x, y, z), 5.0, 5.0, 5.0), e -> true)
                  .stream()
                  .sorted((new Object() {
                     Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                        return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                     }
                  }).compareDistOf(x, y, z))
                  .findFirst()
                  .orElse(null);
               if (var11 instanceof TamableAnimal _toTame && entity instanceof Player _owner) {
                  _toTame.tame(_owner);
               }
            }
         }
      }
   }
}
