package dev.eness.sololevelingfinal.core.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerEntryGenerationGuard {
   public static final String GENERATION_TAG = "slr_player_entry_generation";

   private PlayerEntryGenerationGuard() {
   }

   public static long begin(ServerPlayer player) {
      invalidate(player);
      return capture(player);
   }

   public static long capture(ServerPlayer player) {
      return player == null ? 0L : player.getPersistentData().getLong("slr_player_entry_generation");
   }

   public static boolean isCurrent(ServerPlayer player, long generation) {
      if (player != null && player.isAlive() && !player.hasDisconnected()) {
         MinecraftServer server = player.getServer();
         return server != null && server.getPlayerList().getPlayer(player.getUUID()) == player && capture(player) == generation;
      } else {
         return false;
      }
   }

   public static void invalidate(ServerPlayer player) {
      if (player != null) {
         long current = capture(player);
         long next = current == Long.MAX_VALUE ? 1L : current + 1L;
         if (next == 0L) {
            next = 1L;
         }

         player.getPersistentData().putLong("slr_player_entry_generation", next);
      }
   }
}
