package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class DungeonNostalgiaRightclickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankroom1"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 60.0, z),
               BlockPos.containing(x, y + 60.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankroom2"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 75.0, z),
               BlockPos.containing(x, y + 75.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankroom3"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 90.0, z),
               BlockPos.containing(x, y + 90.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankroom4"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 105.0, z),
               BlockPos.containing(x, y + 105.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankstart"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 120.0, z),
               BlockPos.containing(x, y + 120.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankbig1"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 135.0, z),
               BlockPos.containing(x, y + 135.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankbig2"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 150.0, z),
               BlockPos.containing(x, y + 150.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankboss"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 165.0, z),
               BlockPos.containing(x, y + 165.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankleftrightand"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 180.0, z),
               BlockPos.containing(x, y + 180.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "erankrightleftand"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x, y + 195.0, z),
               BlockPos.containing(x, y + 195.0, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom1"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x + 100.0, y, z),
               BlockPos.containing(x + 100.0, y, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom2"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x + 150.0, y, z),
               BlockPos.containing(x + 150.0, y, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom3"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x + 200.0, y, z),
               BlockPos.containing(x + 200.0, y, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom4"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x + 250.0, y, z),
               BlockPos.containing(x + 250.0, y, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }

      if (world instanceof ServerLevel _serverworld) {
         StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "bigroom5"));
         if (template != null) {
            template.placeInWorld(
               _serverworld,
               BlockPos.containing(x + 300.0, y, z),
               BlockPos.containing(x + 300.0, y, z),
               new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
               _serverworld.random,
               2
            );
         }
      }
   }
}
