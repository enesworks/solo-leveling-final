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

public class DungeonPlaceKamishProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      double xOff = 0.0;
      double zOff = 0.0;
      double yOff = 0.0;
      double Rand = 0.0;
      double sx = 0.0;
      double sy = 0.0;
      double sz = 0.0;
      boolean asd = false;
      boolean found = false;
      boolean spawn = false;
      asd = false;
      spawn = true;
      xOff = x - 30.0;
      yOff = y - 23.0;
      zOff = z - 25.5;

      for (int index0 = 0; index0 < 50; index0++) {
         Rand = Math.random();
         if (!spawn) {
            if (Rand < 0.33) {
               xOff += 49.0;
               asd = true;
            } else if (Rand < 0.67) {
               zOff += 49.0;
               asd = false;
            } else if (Rand < 1.0) {
               zOff -= 49.0;
               asd = false;
            }
         } else if (spawn) {
            if (world instanceof ServerLevel _serverworld) {
               StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishupdatedstart"));
               if (template != null) {
                  template.placeInWorld(
                     _serverworld,
                     BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                     BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                     new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                     _serverworld.random,
                     2
                  );
               }
            }

            xOff += 49.0;
            asd = false;
            spawn = false;
         }

         sx = -50.0;

         for (int index1 = 0; index1 < 100; index1++) {
            sy = -50.0;

            for (int index2 = 0; index2 < 100; index2++) {
               sz = -50.0;

               for (int index3 = 0; index3 < 100; index3++) {
                  if (world.getBlockState(BlockPos.containing(xOff + sx, yOff + sy, zOff + sz)).getBlock() == Blocks.OBSIDIAN) {
                     if (world.getBlockState(BlockPos.containing(xOff + sx + 27.0, yOff + sy - 5.0, zOff + sz)).getBlock() == Blocks.AIR
                        && world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishblock"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(xOff + sx + 25.0, yOff + sy + 3.0, zOff + sz - 1.0),
                              BlockPos.containing(xOff + sx + 25.0, yOff + sy + 3.0, zOff + sz - 1.0),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }

                     if (world.getBlockState(BlockPos.containing(xOff + sx - 27.0, yOff + sy - 5.0, zOff + sz)).getBlock() == Blocks.AIR
                        && world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishblock"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(xOff + sx - 26.0, yOff + sy + 3.0, zOff + sz - 1.0),
                              BlockPos.containing(xOff + sx - 26.0, yOff + sy + 3.0, zOff + sz - 1.0),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }

                     if (world.getBlockState(BlockPos.containing(xOff + sx, yOff + sy + 10.0, zOff + sz - -27.0)).getBlock() == Blocks.AIR
                        && world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishblock"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(xOff + sx - 1.0, yOff + sy + 3.0, zOff + sz - 26.0),
                              BlockPos.containing(xOff + sx - 1.0, yOff + sy + 3.0, zOff + sz - 26.0),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }

                     if (world.getBlockState(BlockPos.containing(xOff + sx, yOff + sy - 5.0, zOff + sz + 27.0)).getBlock() == Blocks.AIR
                        && world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishblock"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(xOff + sx - 1.0, yOff + sy + 3.0, zOff + sz + 25.0),
                              BlockPos.containing(xOff + sx - 1.0, yOff + sy + 3.0, zOff + sz + 25.0),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }
                  }

                  sz++;
               }

               sy++;
            }

            sx++;
         }

         if (world.getBlockState(BlockPos.containing(xOff, yOff, zOff)).getBlock() != Blocks.OBSIDIAN) {
            Rand = Math.random();
            if (!spawn) {
               if (!asd) {
                  if (Rand < 0.33) {
                     if (world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishroom1"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                              BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }
                  } else if (Rand < 0.67) {
                     if (world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishroom2"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                              BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }
                  } else if (Rand < 1.0 && world instanceof ServerLevel _serverworld) {
                     StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishroom3"));
                     if (template != null) {
                        template.placeInWorld(
                           _serverworld,
                           BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                           BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                           new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                           _serverworld.random,
                           2
                        );
                     }
                  }
               } else if (asd) {
                  Rand = Math.random();
                  if (Rand < 0.75) {
                     Rand = Math.random();
                     if (Rand < 0.33) {
                        if (world instanceof ServerLevel _serverworld) {
                           StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishroom1"));
                           if (template != null) {
                              template.placeInWorld(
                                 _serverworld,
                                 BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                                 BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                                 new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                                 _serverworld.random,
                                 2
                              );
                           }
                        }
                     } else if (Rand < 0.67) {
                        if (world instanceof ServerLevel _serverworld) {
                           StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishroom2"));
                           if (template != null) {
                              template.placeInWorld(
                                 _serverworld,
                                 BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                                 BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                                 new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                                 _serverworld.random,
                                 2
                              );
                           }
                        }
                     } else if (Rand < 1.0 && world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishroom3"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                              BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }
                  } else if (Rand < 1.0) {
                     if (world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "kamishboss"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                              BlockPos.containing(xOff - 24.0, yOff, zOff - 24.0),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }
                     break;
                  }
               }
            }
         }
      }
   }
}
