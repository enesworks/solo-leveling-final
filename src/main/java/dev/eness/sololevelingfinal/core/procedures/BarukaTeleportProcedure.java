package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;

public class BarukaTeleportProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         boolean CanTeleport = false;
         double ZTeleport = 0.0;
         double YTeleport = 0.0;
         double XTeleport = 0.0;

         for (int index0 = 0; index0 < 5 && !CanTeleport; index0++) {
            XTeleport = Mth.nextInt(RandomSource.create(), -5, 5);
            YTeleport = Mth.nextInt(RandomSource.create(), -1, 1);
            ZTeleport = Mth.nextInt(RandomSource.create(), -5, 5);

            for (int index1 = 0; index1 < 15 && !CanTeleport; index1++) {
               if (!world.getBlockState(
                        BlockPos.containing(
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX() + XTeleport,
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + YTeleport,
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ() + ZTeleport
                        )
                     )
                     .canOcclude()
                  && !world.getBlockState(
                        BlockPos.containing(
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX() + XTeleport,
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + YTeleport + 1.0,
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ() + ZTeleport
                        )
                     )
                     .canOcclude()) {
                  CanTeleport = true;
               } else {
                  YTeleport++;
               }
            }
         }

         if (CanTeleport) {
            Entity _ent = entity;
            _ent.teleportTo(
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX() + XTeleport,
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + YTeleport,
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ() + ZTeleport
            );
            if (_ent instanceof ServerPlayer _serverPlayer) {
               _serverPlayer.connection
                  .teleport(
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX() + XTeleport,
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + YTeleport,
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ() + ZTeleport,
                     _ent.getYRot(),
                     _ent.getXRot()
                  );
            }
         }
      }
   }
}
