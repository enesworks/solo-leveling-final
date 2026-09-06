package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class GgRightclickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.isShiftKeyDown()) {
            double _setval = 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.JobChange_timer = _setval;
               capability.syncPlayerVariables(entity);
            });
         } else if (world instanceof ServerLevel _serverworld) {
            StructureTemplate template = _serverworld.getStructureManager().getOrCreate(new ResourceLocation("sololeveling", "lobotomy"));
            if (template != null) {
               template.placeInWorld(
                  _serverworld,
                  BlockPos.containing(x - 4.0, y + 2.0, z - 4.0),
                  BlockPos.containing(x - 4.0, y + 2.0, z - 4.0),
                  new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false),
                  _serverworld.random,
                  2
               );
            }
         }
      }
   }
}
