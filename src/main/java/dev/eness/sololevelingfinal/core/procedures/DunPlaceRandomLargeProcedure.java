package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;

public class DunPlaceRandomLargeProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      Vec3 _center = new Vec3(x, y, z);

      for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1000.0), e -> true)
         .stream()
         .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
         .toList()) {
         if ((
               entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("dm")))
                  || entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("portals")))
                  || entityiterator instanceof ItemEntity
                  || entityiterator instanceof ExperienceOrb
            )
            && !entityiterator.level().isClientSide()) {
            entityiterator.discard();
         }
      }

      SololevelingMod.queueServerWork(20, () -> DungeonPlaceLargeProcedure.execute(world, x, y, z));
   }
}
