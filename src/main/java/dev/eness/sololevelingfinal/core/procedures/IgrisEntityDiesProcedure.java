package dev.eness.sololevelingfinal.core.procedures;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.SpawnerPortalEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

@EventBusSubscriber
public class IgrisEntityDiesProcedure {
   private static final ResourceKey<Level> IGRIS_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_igris")
   );
   private static final int PORTAL_SEARCH_RADIUS = 50;
   private static final int MAX_ADVANCEMENT_PORTALS = 24;

   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof BloodRedComIgrisEntity) {
         Entity creditedSource = ShadowKillCreditHelper.creditedSourceForDeath(
            event.getEntity().level(), event.getEntity(), event.getSource().getEntity(), event.getSource().getDirectEntity()
         );
         execute(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity(), creditedSource);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity instanceof BloodRedComIgrisEntity && world instanceof ServerLevel level) {
         ServerPlayer killer = ShadowKillCreditHelper.creditedServerPlayer(world, sourceentity);
         if (killer != null) {
            if (!level.dimension().equals(IGRIS_DIMENSION)) {
               killer.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  if (capability.Player) {
                     capability.giftstatus = true;
                  }

                  capability.syncPlayerVariables(killer);
               });
               SystemNotifications.showTitleUnder(
                  killer,
                  -2124281,
                  90,
                  Component.literal("IGRIS DEFEATED").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                  Component.literal("Leveled Up!").withStyle(ChatFormatting.YELLOW)
               );
            } else {
               List<ServerPlayer> participants = JobChangeQuestManager.beginAdvancementPhase(killer, entity);
               if (!participants.isEmpty()) {
                  for (ServerPlayer participant : participants) {
                     participant.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.giftstatus = true;
                        capability.syncPlayerVariables(participant);
                     });
                  }

                  spawnAdvancementPortals(level, BlockPos.containing(x, y, z), entity);
               }
            }
         }
      }
   }

   private static void spawnAdvancementPortals(ServerLevel level, BlockPos center, Entity defeatedBoss) {
      if (level.getEntitiesOfClass(SpawnerPortalEntity.class, new AABB(center).inflate(120.0)).isEmpty()) {
         MutableBlockPos cursor = new MutableBlockPos();
         int spawned = 0;

         for (int dx = -50; dx < 50 && spawned < 24; dx++) {
            for (int dy = -50; dy < 50 && spawned < 24; dy++) {
               for (int dz = -50; dz < 50 && spawned < 24; dz++) {
                  cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                  if (level.getBlockState(cursor).is(SololevelingModBlocks.UNBREAKABLE_DEEPSLATE.get())) {
                     Entity portal = SololevelingModEntities.SPAWNER_PORTAL
                        .get()
                        .spawn(level, BlockPos.containing(cursor.getX(), cursor.getY() + 1.2, cursor.getZ()), MobSpawnType.MOB_SUMMONED);
                     if (portal != null) {
                        portal.setYRot(level.random.nextFloat() * 360.0F);
                        portal.getPersistentData().putBoolean("slr_job_change_advancement_portal", true);
                        JobChangeQuestManager.copyAttempt(defeatedBoss, portal);
                        spawned++;
                     }
                  }
               }
            }
         }
      }
   }
}
