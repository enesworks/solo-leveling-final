package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RandomCemeteryProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         double Xoff = 0.0;
         double Zoff = 0.0;
         double component = 0.0;
         double Yoff = 0.0;
         double rand1 = 0.0;
         Xoff = entity.getX() + -5.0;
         Yoff = entity.getY() - 15.0;
         Zoff = entity.getZ() + -5.0;
         component = 0.0;
         if (world instanceof ServerLevel _serverworld) {
            StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_enterance"));
            if (template != null) {
               template.placeInWorld(
                  _serverworld,
                  BlockPos.containing(Xoff, Yoff, Zoff),
                  BlockPos.containing(Xoff, Yoff, Zoff),
                  new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                  _serverworld.random,
                  2
               );
            }
         }

         Xoff += 26.0;

         for (int index0 = 0; index0 < 30; index0++) {
            if (component <= 10.0 && component != 5.0) {
               rand1 = Mth.nextInt(RandomSource.create(), 1, 5);
               if (rand1 <= 3.0) {
                  rand1 = Mth.nextInt(RandomSource.create(), 1, 4);
                  if (rand1 == 1.0) {
                     if (world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager()
                           .getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_corridor1"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(Xoff, Yoff, Zoff),
                              BlockPos.containing(Xoff, Yoff, Zoff),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }
                  } else if (rand1 == 2.0) {
                     if (world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager()
                           .getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_corridor2"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(Xoff, Yoff, Zoff),
                              BlockPos.containing(Xoff, Yoff, Zoff),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }
                  } else if (rand1 == 3.0) {
                     if (world instanceof ServerLevel _serverworld) {
                        StructureTemplate template = _serverworld.getStructureManager()
                           .getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_corridor3"));
                        if (template != null) {
                           template.placeInWorld(
                              _serverworld,
                              BlockPos.containing(Xoff, Yoff, Zoff),
                              BlockPos.containing(Xoff, Yoff, Zoff),
                              new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                              _serverworld.random,
                              2
                           );
                        }
                     }
                  } else if (rand1 == 4.0 && world instanceof ServerLevel _serverworld) {
                     StructureTemplate template = _serverworld.getStructureManager()
                        .getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_corridor4"));
                     if (template != null) {
                        template.placeInWorld(
                           _serverworld,
                           BlockPos.containing(Xoff, Yoff, Zoff),
                           BlockPos.containing(Xoff, Yoff, Zoff),
                           new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                           _serverworld.random,
                           2
                        );
                     }
                  }

                  Xoff += 25.0;
                  component++;
               } else if (rand1 == 4.0) {
                  Zoff -= 25.0;
                  if (world instanceof ServerLevel _serverworld) {
                     StructureTemplate template = _serverworld.getStructureManager()
                        .getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_turn_left"));
                     if (template != null) {
                        template.placeInWorld(
                           _serverworld,
                           BlockPos.containing(Xoff, Yoff, Zoff),
                           BlockPos.containing(Xoff, Yoff, Zoff),
                           new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                           _serverworld.random,
                           2
                        );
                     }
                  }

                  component++;
                  Xoff += 25.0;
               } else if (rand1 == 5.0) {
                  if (world instanceof ServerLevel _serverworld) {
                     StructureTemplate template = _serverworld.getStructureManager()
                        .getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_turn_right"));
                     if (template != null) {
                        template.placeInWorld(
                           _serverworld,
                           BlockPos.containing(Xoff, Yoff, Zoff),
                           BlockPos.containing(Xoff, Yoff, Zoff),
                           new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                           _serverworld.random,
                           2
                        );
                     }
                  }

                  component++;
                  Xoff += 25.0;
                  Zoff += 25.0;
               }
            } else {
               if (component != 5.0) {
                  Zoff -= 8.0;
                  if (world instanceof ServerLevel _serverworld) {
                     StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_boss"));
                     if (template != null) {
                        template.placeInWorld(
                           _serverworld,
                           BlockPos.containing(Xoff, Yoff, Zoff),
                           BlockPos.containing(Xoff, Yoff, Zoff),
                           new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                           _serverworld.random,
                           2
                        );
                     }
                  }
                  break;
               }

               rand1 = Mth.nextInt(RandomSource.create(), 1, 2);
               if (rand1 == 1.0) {
                  if (world instanceof ServerLevel _serverworld) {
                     StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_mid1"));
                     if (template != null) {
                        template.placeInWorld(
                           _serverworld,
                           BlockPos.containing(Xoff, Yoff, Zoff - 8.0),
                           BlockPos.containing(Xoff, Yoff, Zoff - 8.0),
                           new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                           _serverworld.random,
                           2
                        );
                     }
                  }

                  Xoff += 29.0;
                  component++;
               } else if (rand1 == 2.0) {
                  if (world instanceof ServerLevel _serverworld) {
                     StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "b_rank_cemetery_mid2"));
                     if (template != null) {
                        template.placeInWorld(
                           _serverworld,
                           BlockPos.containing(Xoff, Yoff, Zoff - 8.0),
                           BlockPos.containing(Xoff, Yoff, Zoff - 8.0),
                           new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                           _serverworld.random,
                           2
                        );
                     }
                  }

                  Xoff += 29.0;
                  component++;
               }
            }
         }
      }
   }
}
