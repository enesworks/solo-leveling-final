package dev.eness.sololevelingfinal.core.dungeon;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public final class ProceduralGateRunSavedData extends SavedData {
   private static final String DATA_NAME = "sololeveling_procedural_gate_runs";
   private static final int MAX_RUNS = 2048;
   private static final int MAX_PARTICIPANTS = 64;
   private static final int MAX_TAG_LENGTH = 128;
   private final Map<String, ProceduralGateRunSavedData.Run> runs = new LinkedHashMap<>();

   public static ProceduralGateRunSavedData get(MinecraftServer server) {
      if (server == null) {
         throw new IllegalArgumentException("A server is required.");
      } else {
         return server.overworld()
            .getDataStorage()
            .computeIfAbsent(ProceduralGateRunSavedData::load, ProceduralGateRunSavedData::new, "sololeveling_procedural_gate_runs");
      }
   }

   public Optional<ProceduralGateRunSavedData.RunView> run(String dungeonTag) {
      ProceduralGateRunSavedData.Run run = this.runs.get(cleanTag(dungeonTag));
      return run == null ? Optional.empty() : Optional.of(run.view());
   }

   public void recordEntrant(String dungeonTag, UUID playerId, ResourceKey<Level> dimension, long gameTime) {
      String clean = cleanTag(dungeonTag);
      if (!clean.isEmpty() && playerId != null && dimension != null) {
         ProceduralGateRunSavedData.Run run = this.runs.get(clean);
         if (run == null) {
            this.makeRoom();
            run = new ProceduralGateRunSavedData.Run(clean, dimension);
            run.authoritativeRoster = true;
            this.runs.put(clean, run);
         }

         if (run.decision == ProceduralGateRunSavedData.ExitDecision.UNDECIDED && !run.participants.contains(playerId) && run.participants.size() < 64) {
            run.dimension = dimension;
            run.participants.add(playerId);
            run.updatedGameTime = Math.max(0L, gameTime);
            this.setDirty();
         }
      }
   }

   public void removeEntrant(String dungeonTag, UUID playerId) {
      String clean = cleanTag(dungeonTag);
      ProceduralGateRunSavedData.Run run = !clean.isEmpty() && playerId != null ? this.runs.get(clean) : null;
      if (run != null && run.participants.remove(playerId)) {
         if (run.participants.isEmpty() && run.decision == ProceduralGateRunSavedData.ExitDecision.UNDECIDED) {
            this.runs.remove(clean);
         }

         this.setDirty();
      }
   }

   public void recordLegacyRun(String dungeonTag, ResourceKey<Level> dimension, long gameTime) {
      String clean = cleanTag(dungeonTag);
      if (!clean.isEmpty() && dimension != null && !this.runs.containsKey(clean)) {
         this.makeRoom();
         ProceduralGateRunSavedData.Run run = new ProceduralGateRunSavedData.Run(clean, dimension);
         run.updatedGameTime = Math.max(0L, gameTime);
         this.runs.put(clean, run);
         this.setDirty();
      }
   }

   public void recordReturnAnchor(String dungeonTag, ResourceKey<Level> dimension, BlockPos returnAnchor, long gameTime) {
      ProceduralGateRunSavedData.Run run = this.getOrCreate(dungeonTag, dimension);
      if (run != null && returnAnchor != null && run.decision == ProceduralGateRunSavedData.ExitDecision.UNDECIDED) {
         BlockPos immutableAnchor = returnAnchor.immutable();
         boolean changed = !run.dimension.equals(dimension) || !immutableAnchor.equals(run.returnAnchor);
         run.dimension = dimension;
         run.returnAnchor = immutableAnchor;
         run.updatedGameTime = Math.max(0L, gameTime);
         if (changed) {
            this.setDirty();
         }
      }
   }

   public void chooseReturnPortal(String dungeonTag, ResourceKey<Level> dimension, BlockPos exit, long gameTime) {
      ProceduralGateRunSavedData.Run run = this.getOrCreate(dungeonTag, dimension);
      if (run != null && exit != null) {
         if (run.decision == ProceduralGateRunSavedData.ExitDecision.UNDECIDED) {
            run.dimension = dimension;
            run.decision = ProceduralGateRunSavedData.ExitDecision.RETURN_PORTAL;
            run.exit = exit.immutable();
            run.updatedGameTime = Math.max(0L, gameTime);
            this.setDirty();
         }
      }
   }

   public void chooseCartenon(String dungeonTag, ResourceKey<Level> dimension, long gameTime) {
      ProceduralGateRunSavedData.Run run = this.getOrCreate(dungeonTag, dimension);
      if (run != null) {
         if (run.decision == ProceduralGateRunSavedData.ExitDecision.UNDECIDED) {
            run.dimension = dimension;
            run.decision = ProceduralGateRunSavedData.ExitDecision.CARTENON;
            run.exit = null;
            run.updatedGameTime = Math.max(0L, gameTime);
            this.setDirty();
         }
      }
   }

   public List<ProceduralGateRunSavedData.ReturnRequest> returnRequests() {
      return this.runs
         .values()
         .stream()
         .filter(run -> run.decision == ProceduralGateRunSavedData.ExitDecision.RETURN_PORTAL && run.exit != null)
         .map(run -> new ProceduralGateRunSavedData.ReturnRequest(run.dungeonTag, run.dimension, run.exit))
         .toList();
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag root) {
      ListTag list = new ListTag();

      for (ProceduralGateRunSavedData.Run run : this.runs.values()) {
         CompoundTag tag = new CompoundTag();
         tag.putString("DungeonTag", run.dungeonTag);
         tag.putString("Dimension", run.dimension.location().toString());
         tag.putBoolean("AuthoritativeRoster", run.authoritativeRoster);
         tag.putString("Decision", run.decision.name());
         tag.putLong("UpdatedGameTime", run.updatedGameTime);
         if (run.returnAnchor != null) {
            tag.put("ReturnAnchor", NbtUtils.writeBlockPos(run.returnAnchor));
         }

         if (run.exit != null) {
            tag.put("Exit", NbtUtils.writeBlockPos(run.exit));
         }

         ListTag participants = new ListTag();

         for (UUID playerId : run.participants) {
            CompoundTag participant = new CompoundTag();
            participant.putUUID("Id", playerId);
            participants.add(participant);
         }

         tag.put("Participants", participants);
         list.add(tag);
      }

      root.put("Runs", list);
      return root;
   }

   private static ProceduralGateRunSavedData load(CompoundTag root) {
      ProceduralGateRunSavedData data = new ProceduralGateRunSavedData();
      ListTag list = root.getList("Runs", 10);
      int limit = Math.min(2048, list.size());

      for (int index = 0; index < limit; index++) {
         CompoundTag tag = list.getCompound(index);
         String dungeonTag = cleanTag(tag.getString("DungeonTag"));
         ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString("Dimension"));
         if (!dungeonTag.isEmpty() && dimensionId != null) {
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
            ProceduralGateRunSavedData.Run run = new ProceduralGateRunSavedData.Run(dungeonTag, dimension);
            run.authoritativeRoster = tag.getBoolean("AuthoritativeRoster");

            try {
               run.decision = ProceduralGateRunSavedData.ExitDecision.valueOf(tag.getString("Decision"));
            } catch (IllegalArgumentException ignored) {
               run.decision = ProceduralGateRunSavedData.ExitDecision.UNDECIDED;
            }

            run.updatedGameTime = Math.max(0L, tag.getLong("UpdatedGameTime"));
            if (tag.contains("ReturnAnchor", 10)) {
               run.returnAnchor = NbtUtils.readBlockPos(tag.getCompound("ReturnAnchor"));
            }

            if (tag.contains("Exit", 10)) {
               run.exit = NbtUtils.readBlockPos(tag.getCompound("Exit"));
            }

            ListTag participants = tag.getList("Participants", 10);
            int participantLimit = Math.min(64, participants.size());

            for (int participantIndex = 0; participantIndex < participantLimit; participantIndex++) {
               CompoundTag participant = participants.getCompound(participantIndex);
               if (participant.hasUUID("Id")) {
                  run.participants.add(participant.getUUID("Id"));
               }
            }

            data.runs.putIfAbsent(dungeonTag, run);
         }
      }

      if (list.size() > limit) {
         data.setDirty();
      }

      return data;
   }

   @Nullable
   private ProceduralGateRunSavedData.Run getOrCreate(String dungeonTag, ResourceKey<Level> dimension) {
      String clean = cleanTag(dungeonTag);
      if (!clean.isEmpty() && dimension != null) {
         ProceduralGateRunSavedData.Run run = this.runs.get(clean);
         if (run != null) {
            return run;
         }

         this.makeRoom();
         run = new ProceduralGateRunSavedData.Run(clean, dimension);
         this.runs.put(clean, run);
         return run;
      } else {
         return null;
      }
   }

   private void makeRoom() {
      if (this.runs.size() >= 2048) {
         this.runs.values().stream().min(Comparator.comparingLong(run -> run.updatedGameTime)).ifPresent(oldest -> this.runs.remove(oldest.dungeonTag));
      }
   }

   private static String cleanTag(String dungeonTag) {
      if (dungeonTag == null) {
         return "";
      }

      String clean = dungeonTag.trim();
      return clean.length() <= 128 ? clean : "";
   }

   public enum ExitDecision {
      UNDECIDED,
      RETURN_PORTAL,
      CARTENON;
   }

   public record ReturnRequest(String dungeonTag, ResourceKey<Level> dimension, BlockPos exit) {
   }

   private static final class Run {
      private final String dungeonTag;
      private final LinkedHashSet<UUID> participants = new LinkedHashSet<>();
      private ResourceKey<Level> dimension;
      private boolean authoritativeRoster;
      @Nullable
      private BlockPos returnAnchor;
      private ProceduralGateRunSavedData.ExitDecision decision = ProceduralGateRunSavedData.ExitDecision.UNDECIDED;
      @Nullable
      private BlockPos exit;
      private long updatedGameTime;

      private Run(String dungeonTag, ResourceKey<Level> dimension) {
         this.dungeonTag = dungeonTag;
         this.dimension = dimension;
      }

      private ProceduralGateRunSavedData.RunView view() {
         return new ProceduralGateRunSavedData.RunView(this.participants, this.dimension, this.authoritativeRoster, this.returnAnchor, this.decision, this.exit);
      }
   }

   public record RunView(
      Set<UUID> participants,
      ResourceKey<Level> dimension,
      boolean authoritativeRoster,
      @Nullable BlockPos returnAnchor,
      ProceduralGateRunSavedData.ExitDecision decision,
      @Nullable BlockPos exit
   ) {
      public RunView {
         participants = Set.copyOf(participants);
      }
   }
}
