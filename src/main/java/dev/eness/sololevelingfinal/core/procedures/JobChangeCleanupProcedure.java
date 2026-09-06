package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.entity.IgrisDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.Portal12Entity;
import dev.eness.sololevelingfinal.core.entity.SpawnerPortalEntity;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

public class JobChangeCleanupProcedure {
   private static final ResourceKey<Level> IGRIS_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling", "dungeon_dimension_igris")
   );

   public static void execute(LevelAccessor world, double x, double y, double z) {
      Vec3 center = new Vec3(x, y, z);
      List<Entity> entities = world.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(160.0), e -> true)
         .stream()
         .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
         .toList();
      TagKey<EntityType<?>> dungeonMobTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("dm"));

      for (Entity target : entities) {
         if ((
               target.getType().is(dungeonMobTag)
                  || target instanceof DKnight1Entity
                  || target instanceof DKnight2Entity
                  || target instanceof DKnight3Entity
                  || target instanceof SpawnerPortalEntity
            )
            && !target.level().isClientSide()) {
            target.discard();
         }
      }
   }

   public static void executeAttempt(MinecraftServer server, UUID attemptId) {
      cleanupAttempt(server, attemptId, false);
   }

   public static void completeAttempt(MinecraftServer server, UUID attemptId) {
      cleanupAttempt(server, attemptId, true);
   }

   private static void cleanupAttempt(MinecraftServer server, UUID attemptId, boolean preserveCompletionEntities) {
      if (server != null && attemptId != null) {
         ServerLevel level = server.getLevel(IGRIS_DIMENSION);
         if (level != null) {
            List<Entity> removals = new ArrayList<>();

            for (Entity entity : level.getAllEntities()) {
               if (JobChangeQuestManager.hasAttemptId(entity, attemptId)) {
                  if (!preserveCompletionEntities || !(entity instanceof Portal12Entity) && !(entity instanceof IgrisDeadBodyEntity)) {
                     removals.add(entity);
                  } else {
                     entity.getPersistentData().remove("slr_job_change_attempt_id");
                     entity.getPersistentData().remove("slr_job_change_attempt_owner");
                  }
               }
            }

            for (Entity entity : removals) {
               entity.discard();
            }
         }
      }
   }
}
