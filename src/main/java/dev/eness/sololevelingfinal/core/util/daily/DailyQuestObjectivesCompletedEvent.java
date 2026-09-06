package dev.eness.sololevelingfinal.core.util.daily;

import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public final class DailyQuestObjectivesCompletedEvent extends Event {
   private final ServerPlayer player;
   private final DailyQuestObjectiveManager.ProgressSnapshot progress;

   public DailyQuestObjectivesCompletedEvent(ServerPlayer player, DailyQuestObjectiveManager.ProgressSnapshot progress) {
      this.player = Objects.requireNonNull(player, "player");
      this.progress = Objects.requireNonNull(progress, "progress");
   }

   public ServerPlayer getPlayer() {
      return this.player;
   }

   public DailyQuestObjectiveManager.ProgressSnapshot getProgress() {
      return this.progress;
   }
}
