package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class DungeonPlaceProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      double xOff = 0.0;
      double zOff = 0.0;
      double yOff = 0.0;
      double Rand = 0.0;
      xOff = x + 5.0;
      yOff = y;
      zOff = z + 5.0;

      for (int index0 = 0; index0 < 100; index0++) {
         Rand = Math.random();
         if (Rand < 0.25) {
            xOff += 11.0;
         } else if (Rand < 0.5) {
            xOff -= 11.0;
         } else if (Rand < 0.75) {
            zOff -= 11.0;
         } else {
            zOff += 11.0;
         }

         if (world.getBlockState(BlockPos.containing(xOff, yOff, zOff)).getBlock() != Blocks.REINFORCED_DEEPSLATE) {
            Rand = Math.random();
            if (Rand < 0.33) {
               if (world instanceof ServerLevel _serverworld) {
                  StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "testroom1"));
                  if (template != null) {
                     template.placeInWorld(
                        _serverworld,
                        BlockPos.containing(xOff - 5.0, yOff, zOff - 5.0),
                        BlockPos.containing(xOff - 5.0, yOff, zOff - 5.0),
                        new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                        _serverworld.random,
                        2
                     );
                  }
               }
            } else if (Rand < 0.67) {
               if (world instanceof ServerLevel _serverworld) {
                  StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "testroom2"));
                  if (template != null) {
                     template.placeInWorld(
                        _serverworld,
                        BlockPos.containing(xOff - 5.0, yOff, zOff - 5.0),
                        BlockPos.containing(xOff - 5.0, yOff, zOff - 5.0),
                        new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                        _serverworld.random,
                        2
                     );
                  }
               }
            } else if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "testroom3"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(xOff - 5.0, yOff, zOff - 5.0),
                     BlockPos.containing(xOff - 5.0, yOff, zOff - 5.0),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }
         }
      }
   }
}
