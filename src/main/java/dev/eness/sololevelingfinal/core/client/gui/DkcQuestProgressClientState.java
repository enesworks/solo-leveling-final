package dev.eness.sololevelingfinal.core.client.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;

@EventBusSubscriber(modid = "sololeveling", value = Dist.CLIENT)
public final class DkcQuestProgressClientState {
   private static boolean active;
   private static int floor;
   private static int cleared;
   private static String floorName = "";
   private static String phase = "";
   private static String objective = "";
   private static String detail = "";
   private static int progress;
   private static int target;

   private DkcQuestProgressClientState() {
   }

   public static void update(
      boolean isActive,
      int currentFloor,
      int clearedFloors,
      String currentFloorName,
      String currentPhase,
      String currentObjective,
      String currentDetail,
      int currentProgress,
      int currentTarget
   ) {
      active = isActive && currentFloor >= 1 && currentFloor <= 20;
      floor = active ? currentFloor : 0;
      cleared = Math.max(0, Math.min(20, clearedFloors));
      floorName = currentFloorName == null ? "" : currentFloorName;
      phase = currentPhase == null ? "" : currentPhase;
      objective = currentObjective == null ? "" : currentObjective;
      detail = currentDetail == null ? "" : currentDetail;
      progress = Math.max(0, currentProgress);
      target = Math.max(0, currentTarget);
   }

   public static void clear() {
      update(false, 0, 0, "", "", "", "", 0, 0);
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      clear();
   }

   public static boolean isActive(Player player) {
      return active && player != null && DkcFloorRegistry.isSharedDkc(player.level()) && DkcSpatialLayout.floorAt(player.blockPosition()) == floor;
   }

   public static int floor() {
      return floor;
   }

   public static int cleared() {
      return cleared;
   }

   public static String floorName() {
      return floorName;
   }

   public static String phase() {
      return phase;
   }

   public static String objective() {
      return objective;
   }

   public static String detail() {
      return detail;
   }

   public static int progress() {
      return progress;
   }

   public static int target() {
      return target;
   }
}
