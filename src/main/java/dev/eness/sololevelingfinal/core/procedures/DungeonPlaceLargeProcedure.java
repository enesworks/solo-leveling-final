package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class DungeonPlaceLargeProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      boolean asd = false;
      boolean found = false;
      boolean spawn = false;
      double xOff = 0.0;
      double zOff = 0.0;
      double yOff = 0.0;
      double Rand = 0.0;
      double sx = 0.0;
      double sy = 0.0;
      double sz = 0.0;
      double comp = 0.0;
      comp = 0.0;
      xOff = x - 30.0;
      yOff = y - 23.0;
      zOff = z - 25.5;
      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "updatedlargerandstart"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(xOff, yOff, zOff),
               BlockPos.containing(xOff, yOff, zOff),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      xOff += 49.0;

      for (int index0 = 0; index0 < 10; index0++) {
         comp++;
         Rand = Mth.nextInt(RandomSource.create(), 1, 4);
         if (!(comp < 4.0)) {
            if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroomboss"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(xOff, yOff, zOff),
                     BlockPos.containing(xOff, yOff, zOff),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }
            break;
         }

         if (Rand == 1.0) {
            if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom1"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(xOff, yOff, zOff),
                     BlockPos.containing(xOff, yOff, zOff),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }

            xOff += 49.0;
         }

         if (Rand == 2.0) {
            if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom2"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(xOff, yOff, zOff),
                     BlockPos.containing(xOff, yOff, zOff),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }

            xOff += 49.0;
         }

         if (Rand == 3.0) {
            if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom3"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(xOff, yOff, zOff),
                     BlockPos.containing(xOff, yOff, zOff),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }

            xOff += 49.0;
         }

         if (Rand == 4.0) {
            if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom4"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(xOff, yOff, zOff),
                     BlockPos.containing(xOff, yOff, zOff),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }

            xOff += 49.0;
         }

         if (Rand == 5.0) {
            if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom5"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(xOff, yOff, zOff),
                     BlockPos.containing(xOff, yOff, zOff),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }

            xOff += 49.0;
         }
      }
   }
}
