package dev.eness.sololevelingfinal.core.procedures;

import java.text.DecimalFormat;
import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ReturnClosestGateZProcedure {
   public static String execute(LevelAccessor world, double x, double y, double z) {
      return !world.getEntitiesOfClass(PathfinderMob.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).isEmpty()
            && world.getEntitiesOfClass(PathfinderMob.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).stream().sorted((new Object() {
               Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                  return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
               }
            }).compareDistOf(x, y, z)).findFirst().orElse(null).getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("portals")))
         ? new DecimalFormat("##.##")
            .format(
               world.getEntitiesOfClass(PathfinderMob.class, AABB.ofSize(new Vec3(x, y, z), 200.0, 200.0, 200.0), e -> true).stream().sorted((new Object() {
                  Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                     return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                  }
               }).compareDistOf(x, y, z)).findFirst().orElse(null).getZ()
            )
         : "No gate nearby";
   }
}
