package dev.eness.sololevelingfinal.core.procedures;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.entity.SpawnerPortalEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.JobChangeKnightBalance;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

public class SpawnerPortalOnEntityTickUpdateProcedure {
   private static final int LOCAL_KNIGHT_CAP = 10;
   private static final int DUNGEON_KNIGHT_CAP = 48;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (world instanceof ServerLevel level && entity instanceof SpawnerPortalEntity portal) {
         if (JobChangeQuestManager.hasAdvancementPlayerNear(portal, 192.0) && !(level.random.nextFloat() < 0.99F)) {
            UUID attemptId = JobChangeQuestManager.attemptId(portal);
            if (attemptId != null) {
               AABB localArea = portal.getBoundingBox().inflate(18.0, 8.0, 18.0);
               int nearbyKnights = level.getEntitiesOfClass(
                     Entity.class,
                     localArea,
                     target -> (target instanceof DKnight1Entity || target instanceof DKnight2Entity || target instanceof DKnight3Entity)
                        && JobChangeQuestManager.hasAttemptId(target, attemptId)
                  )
                  .size();
               if (nearbyKnights < 10) {
                  AABB dungeonArea = portal.getBoundingBox().inflate(160.0);
                  int dungeonKnights = level.getEntitiesOfClass(
                        Entity.class,
                        dungeonArea,
                        target -> (target instanceof DKnight1Entity || target instanceof DKnight2Entity || target instanceof DKnight3Entity)
                           && JobChangeQuestManager.hasAttemptId(target, attemptId)
                     )
                     .size();
                  if (dungeonKnights < 48) {
                     Entity spawned = switch (level.random.nextInt(3)) {
                        case 0 -> (DKnight1Entity)SololevelingModEntities.D_KNIGHT_1
                           .get()
                           .spawn(level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                        case 1 -> (DKnight2Entity)SololevelingModEntities.D_KNIGHT_2
                           .get()
                           .spawn(level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                        default -> (DKnight3Entity)SololevelingModEntities.D_KNIGHT_3
                           .get()
                           .spawn(level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     };
                     if (spawned != null) {
                        spawned.setYRot(level.random.nextFloat() * 360.0F);
                        JobChangeQuestManager.copyAttempt(portal, spawned);
                        JobChangeKnightBalance.markAndBalance(spawned);
                     }
                  }
               }
            }
         }
      }
   }
}
