package dev.eness.sololevelingfinal.core.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public final class JobChangeAttemptSavedData extends SavedData {
   private static final String DATA_NAME = "sololeveling_job_change_attempts";
   private static final int MAX_ATTEMPTS = 1024;
   private static final int MAX_PARTICIPANTS = 64;
   private static final int MAX_CREDITED_KILLS = 4096;
   private final Map<UUID, JobChangeAttemptSavedData.Attempt> attempts = new LinkedHashMap<>();

   public static JobChangeAttemptSavedData get(MinecraftServer server) {
      if (server == null) {
         throw new IllegalArgumentException("A server is required.");
      } else {
         return server.overworld()
            .getDataStorage()
            .computeIfAbsent(JobChangeAttemptSavedData::load, JobChangeAttemptSavedData::new, "sololeveling_job_change_attempts");
      }
   }

   public void start(UUID attemptId, UUID owner, long gameTime) {
      if (attemptId != null && owner != null) {
         if (this.attempts.size() >= 1024) {
            this.pruneOldest();
         }

         JobChangeAttemptSavedData.Attempt attempt = new JobChangeAttemptSavedData.Attempt(owner);
         attempt.participants.add(owner);
         attempt.updatedGameTime = Math.max(0L, gameTime);
         this.attempts.put(attemptId, attempt);
         this.setDirty();
      }
   }

   public boolean isActive(UUID attemptId) {
      JobChangeAttemptSavedData.Attempt attempt = this.attempts.get(attemptId);
      return attempt != null && attempt.active;
   }

   public JobChangeAttemptSavedData.AttemptView view(UUID attemptId) {
      JobChangeAttemptSavedData.Attempt attempt = this.attempts.get(attemptId);
      return attempt == null ? null : attempt.view();
   }

   public void addParticipant(UUID attemptId, UUID playerId, long gameTime) {
      JobChangeAttemptSavedData.Attempt attempt = this.attempts.get(attemptId);
      if (attempt != null && attempt.active && playerId != null && attempt.participants.size() < 64) {
         boolean changed = attempt.participants.add(playerId);
         attempt.updatedGameTime = Math.max(attempt.updatedGameTime, Math.max(0L, gameTime));
         if (changed) {
            this.setDirty();
         }
      }
   }

   public boolean creditAdvancementKill(UUID attemptId, UUID defeatedId, long gameTime) {
      JobChangeAttemptSavedData.Attempt attempt = this.attempts.get(attemptId);
      if (attempt != null && attempt.active && defeatedId != null && attempt.creditedKills.size() < 4096 && attempt.creditedKills.add(defeatedId)) {
         attempt.updatedGameTime = Math.max(attempt.updatedGameTime, Math.max(0L, gameTime));
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public Set<UUID> invalidate(UUID attemptId, long retryAfterGameTime, long gameTime) {
      JobChangeAttemptSavedData.Attempt attempt = this.attempts.get(attemptId);
      if (attempt == null) {
         return Set.of();
      }

      attempt.active = false;
      attempt.retryAfterGameTime = Math.max(0L, retryAfterGameTime);
      attempt.updatedGameTime = Math.max(attempt.updatedGameTime, Math.max(0L, gameTime));
      this.setDirty();
      return Set.copyOf(attempt.participants);
   }

   public void complete(UUID attemptId) {
      if (attemptId != null && this.attempts.remove(attemptId) != null) {
         this.setDirty();
      }
   }

   public void acknowledgeFailure(UUID attemptId, UUID playerId) {
      JobChangeAttemptSavedData.Attempt attempt = this.attempts.get(attemptId);
      if (attempt != null && !attempt.active && playerId != null) {
         if (attempt.participants.remove(playerId)) {
            if (attempt.participants.isEmpty()) {
               this.attempts.remove(attemptId);
            }

            this.setDirty();
         }
      }
   }

   public void removeParticipant(UUID attemptId, UUID playerId) {
      JobChangeAttemptSavedData.Attempt attempt = this.attempts.get(attemptId);
      if (attempt != null && playerId != null && attempt.participants.remove(playerId)) {
         if (attempt.participants.isEmpty()) {
            this.attempts.remove(attemptId);
         }

         this.setDirty();
      }
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag root) {
      ListTag list = new ListTag();

      for (Entry<UUID, JobChangeAttemptSavedData.Attempt> entry : this.attempts.entrySet()) {
         CompoundTag tag = new CompoundTag();
         tag.putUUID("AttemptId", entry.getKey());
         tag.putUUID("Owner", entry.getValue().owner);
         tag.putBoolean("Active", entry.getValue().active);
         tag.putLong("RetryAfter", entry.getValue().retryAfterGameTime);
         tag.putLong("Updated", entry.getValue().updatedGameTime);
         ListTag participants = new ListTag();

         for (UUID playerId : entry.getValue().participants) {
            CompoundTag participant = new CompoundTag();
            participant.putUUID("Id", playerId);
            participants.add(participant);
         }

         tag.put("Participants", participants);
         ListTag creditedKills = new ListTag();

         for (UUID defeatedId : entry.getValue().creditedKills) {
            CompoundTag credited = new CompoundTag();
            credited.putUUID("Id", defeatedId);
            creditedKills.add(credited);
         }

         tag.put("CreditedKills", creditedKills);
         list.add(tag);
      }

      root.put("Attempts", list);
      return root;
   }

   private static JobChangeAttemptSavedData load(CompoundTag root) {
      JobChangeAttemptSavedData data = new JobChangeAttemptSavedData();
      ListTag list = root.getList("Attempts", 10);
      int limit = Math.min(1024, list.size());

      for (int index = 0; index < limit; index++) {
         CompoundTag tag = list.getCompound(index);
         if (tag.hasUUID("AttemptId") && tag.hasUUID("Owner")) {
            JobChangeAttemptSavedData.Attempt attempt = new JobChangeAttemptSavedData.Attempt(tag.getUUID("Owner"));
            attempt.active = tag.getBoolean("Active");
            attempt.retryAfterGameTime = Math.max(0L, tag.getLong("RetryAfter"));
            attempt.updatedGameTime = Math.max(0L, tag.getLong("Updated"));
            ListTag participants = tag.getList("Participants", 10);
            int participantLimit = Math.min(64, participants.size());

            for (int participantIndex = 0; participantIndex < participantLimit; participantIndex++) {
               CompoundTag participant = participants.getCompound(participantIndex);
               if (participant.hasUUID("Id")) {
                  attempt.participants.add(participant.getUUID("Id"));
               }
            }

            if (attempt.participants.isEmpty()) {
               attempt.participants.add(attempt.owner);
            }

            ListTag creditedKills = tag.getList("CreditedKills", 10);
            int creditedLimit = Math.min(4096, creditedKills.size());

            for (int creditedIndex = 0; creditedIndex < creditedLimit; creditedIndex++) {
               CompoundTag credited = creditedKills.getCompound(creditedIndex);
               if (credited.hasUUID("Id")) {
                  attempt.creditedKills.add(credited.getUUID("Id"));
               }
            }

            data.attempts.put(tag.getUUID("AttemptId"), attempt);
         }
      }

      return data;
   }

   private void pruneOldest() {
      UUID oldest = null;
      long oldestTime = Long.MAX_VALUE;

      for (Entry<UUID, JobChangeAttemptSavedData.Attempt> entry : this.attempts.entrySet()) {
         if (entry.getValue().updatedGameTime < oldestTime) {
            oldest = entry.getKey();
            oldestTime = entry.getValue().updatedGameTime;
         }
      }

      if (oldest != null) {
         this.attempts.remove(oldest);
      }
   }

   private static final class Attempt {
      private final UUID owner;
      private final LinkedHashSet<UUID> participants = new LinkedHashSet<>();
      private final LinkedHashSet<UUID> creditedKills = new LinkedHashSet<>();
      private boolean active = true;
      private long retryAfterGameTime;
      private long updatedGameTime;

      private Attempt(UUID owner) {
         this.owner = owner;
      }

      private JobChangeAttemptSavedData.AttemptView view() {
         return new JobChangeAttemptSavedData.AttemptView(this.owner, this.participants, this.active, this.retryAfterGameTime);
      }
   }

   public record AttemptView(UUID owner, Set<UUID> participants, boolean active, long retryAfterGameTime) {
      public AttemptView {
         participants = Set.copyOf(participants);
      }
   }
}
