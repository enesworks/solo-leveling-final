package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;

public class DunPlaceKargalganProcedure {
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

      SololevelingMod.queueServerWork(
         20,
         () -> {
            if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "dun_kargalgan_enterance"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(x, y, z),
                     BlockPos.containing(x, y, z),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }

            SololevelingMod.queueServerWork(
               20,
               () -> {
                  if (world instanceof ServerLevel _serverworldx) {
                     StructureTemplate templatex = _serverworldx.getStructureManager()
                        .getOrCreate(new ResourceLocation("sololeveling", "dun_kargalgan_bossroom"));
                     if (templatex != null) {
                        templatex.placeInWorld(
                           _serverworldx,
                           BlockPos.containing(x, y, z + 173.0),
                           BlockPos.containing(x, y, z + 173.0),
                           new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                           _serverworldx.random,
                           2
                        );
                     }
                  }
               }
            );
         }
      );
   }
}
