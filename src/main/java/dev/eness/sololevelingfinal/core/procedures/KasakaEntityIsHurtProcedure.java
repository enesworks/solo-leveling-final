package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KasakaEntity;

public class KasakaEntityIsHurtProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         double rand = 0.0;
         rand = Mth.nextInt(RandomSource.create(), 1, 5);
         if (rand == 3.0) {
            if (entity instanceof KasakaEntity) {
               ((KasakaEntity)entity).setAnimation("scream");
            }

            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (sourceentity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
                  && !(sourceentity instanceof IgrisShadowEntity)
                  && !(sourceentity instanceof BeruShadowEntity)) {
               }
            }
         }
      }
   }
}
