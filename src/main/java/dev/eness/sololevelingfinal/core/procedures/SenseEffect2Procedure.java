package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.EntityHighlightSystem;

@EventBusSubscriber
public final class SenseEffect2Procedure {
   private static final String HIGHLIGHT_SOURCE = "perception:sense";
   private static final String NEXT_SENSE_TICK = "slr_next_perception_sense";
   private static final int HIGHLIGHT_DURATION_TICKS = 80;
   private static final int SENSE_COOLDOWN_TICKS = 240;
   private static final int MAX_SENSE_TARGETS = 24;
   private static final double MAX_PERCEPTION = 100.0;

   private SenseEffect2Procedure() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer player) {
         trySense(player);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (!world.isClientSide() && entity instanceof ServerPlayer player) {
         trySense(player);
      }
   }

   private static void trySense(ServerPlayer player) {
      SololevelingModVariables.PlayerVariables variables = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
      if (variables != null) {
         double perception = Math.max(0.0, Math.min(100.0, variables.perception));
         long gameTime = player.serverLevel().getGameTime();
         if (gameTime >= player.getPersistentData().getLong("slr_next_perception_sense")) {
            if (!(perception <= 0.0) && !(player.getRandom().nextDouble() > perception / 20000.0)) {
               player.getPersistentData().putLong("slr_next_perception_sense", gameTime + 240L);
               double radius = perception / 2.0;
               List<LivingEntity> targets = player.serverLevel()
                  .getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), targetx -> eligible(player, targetx))
                  .stream()
                  .sorted(Comparator.comparingDouble(player::distanceToSqr))
                  .limit(24L)
                  .toList();

               for (LivingEntity target : targets) {
                  EntityHighlightSystem.show(
                     player, target, "perception:sense", EntityHighlightSystem.perceptionColor(target), 80, EntityHighlightSystem.perceptionPriority(target)
                  );
               }

               if (!targets.isEmpty()) {
                  player.playNotifySound(SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 0.45F, 1.6F);
               }
            }
         }
      }
   }

   private static boolean eligible(ServerPlayer viewer, LivingEntity target) {
      if (target != viewer && EntityHighlightSystem.isPerceptionCandidate(target)) {
         return target instanceof TamableAnimal tamable && tamable.isOwnedBy(viewer) ? false : !viewer.isAlliedTo(target) && !target.isAlliedTo(viewer);
      } else {
         return false;
      }
   }
}
