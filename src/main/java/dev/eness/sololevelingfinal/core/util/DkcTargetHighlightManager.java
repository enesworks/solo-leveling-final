package dev.eness.sololevelingfinal.core.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.CerberusEntity;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.VulcanEntity;

public final class DkcTargetHighlightManager {
   private static final String SOURCE = "dkc:floor_targets";
   private static final int REFRESH_INTERVAL = 20;
   private static final int LEASE_DURATION = 50;

   private DkcTargetHighlightManager() {
   }

   public static void sync(ServerPlayer player, int floor) {
      if (player != null && floor >= 1 && floor <= 20 && player.serverLevel().getGameTime() % 20L == 0L) {
         ServerLevel level = player.serverLevel();
         AABB floorBounds = DkcFloorBuilder.combatBounds(player, floor);
         String ownerId = player.getStringUUID();

         for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, floorBounds, entity -> belongsToPlayerFloor(entity, ownerId, floor))) {
            boolean boss = isBoss(target);
            boolean elite = target instanceof DemonKnightEntity || target.getPersistentData().getBoolean("dkc_miniboss");
            int color = boss ? 16724047 : (elite ? 16757575 : 16740419);
            int priority = boss ? 300 : (elite ? 250 : 200);
            EntityHighlightSystem.show(player, target, "dkc:floor_targets", color, 50, priority);
         }
      }
   }

   private static boolean belongsToPlayerFloor(LivingEntity target, String ownerId, int floor) {
      String targetOwner = target.getPersistentData().getString("dkc_spawned_by");
      int targetFloor = (int)target.getPersistentData().getDouble("dkc_floor_number");
      return ownerId.equals(targetOwner) && targetFloor == floor;
   }

   private static boolean isBoss(LivingEntity target) {
      return target instanceof CerberusEntity || target instanceof VulcanEntity || target instanceof BaranEntity || target instanceof KaiselinEntity;
   }
}
