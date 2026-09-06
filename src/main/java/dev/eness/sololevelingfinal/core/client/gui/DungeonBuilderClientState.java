package dev.eness.sololevelingfinal.core.client.gui;

import dev.eness.sololevelingfinal.core.network.DungeonBuilderStatusMessage;

public final class DungeonBuilderClientState {
   private static final long STALE_AFTER_MS = 3000L;
   private static DungeonBuilderStatusMessage.View view = DungeonBuilderStatusMessage.View.inactive();
   private static long receivedAt;

   private DungeonBuilderClientState() {
   }

   public static void update(DungeonBuilderStatusMessage.View next) {
      view = next == null ? DungeonBuilderStatusMessage.View.inactive() : next;
      receivedAt = System.currentTimeMillis();
   }

   public static DungeonBuilderStatusMessage.View view() {
      return System.currentTimeMillis() - receivedAt > 3000L ? DungeonBuilderStatusMessage.View.inactive() : view;
   }
}
