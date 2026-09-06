package dev.eness.sololevelingfinal.core.dkc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class DkcRunSavedData extends SavedData {
   private static final String DATA_NAME = "sololeveling_dkc_runs";
   private static final int SCHEMA_VERSION = 3;
   private static final int LAYOUT_VERSION = 6;
   private static final long VALID_BITS = 1048575L;
   private final Map<UUID, DkcRunSavedData.RunState> runs = new LinkedHashMap<>();

   public static DkcRunSavedData get(MinecraftServer server) {
      if (server == null) {
         throw new IllegalArgumentException("A server is required.");
      } else {
         return server.overworld().getDataStorage().computeIfAbsent(DkcRunSavedData::load, DkcRunSavedData::new, "sololeveling_dkc_runs");
      }
   }

   public DkcRunSavedData.RunState getOrCreate(ServerPlayer player) {
      DkcRunSavedData.RunState existing = this.runs.get(player.getUUID());
      if (existing != null) {
         this.ensureSlot(existing);
         syncLegacyAnchor(player, existing);
         return existing;
      }

      SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      int storedFloor = Math.max(0, Math.min(20, (int)player.getPersistentData().getDouble("dkc_current_floor")));
      boolean legacyRun = vars.dkc_started || vars.dkc_cleared > 0.0 || vars.dungeoning && storedFloor > 0;
      DkcRunSavedData.RunState created = new DkcRunSavedData.RunState();
      created.slot = this.allocateSlot();
      created.unlockedFloors = bit(1);
      if (legacyRun) {
         int cleared = Math.max(0, Math.min(20, (int)vars.dkc_cleared));
         int highestReachable = Math.max(storedFloor, Math.min(20, cleared + 1));

         for (int floor = 1; floor <= highestReachable; floor++) {
            created.unlockedFloors = created.unlockedFloors | bit(floor);
         }

         for (int floor = 1; floor <= Math.min(19, cleared); floor++) {
            created.armedTransitions = created.armedTransitions | bit(floor);
         }
      }

      this.runs.put(player.getUUID(), created);
      this.setDirty();
      syncLegacyAnchor(player, created);
      return created;
   }

   public int slot(ServerPlayer player) {
      return this.getOrCreate(player).slot;
   }

   public int slot(UUID owner) {
      DkcRunSavedData.RunState state = owner == null ? null : this.runs.get(owner);
      return state == null ? -1 : state.slot;
   }

   public boolean isUnlocked(ServerPlayer player, int floor) {
      return validFloor(floor) && (this.getOrCreate(player).unlockedFloors & bit(floor)) != 0L;
   }

   public boolean isGenerated(ServerPlayer player, int floor) {
      return validFloor(floor) && (this.getOrCreate(player).generatedFloors & bit(floor)) != 0L;
   }

   public boolean isTransitionArmed(ServerPlayer player, int floor) {
      return validFloor(floor) && (this.getOrCreate(player).armedTransitions & bit(floor)) != 0L;
   }

   public boolean needsCleanup(ServerPlayer player, int floor) {
      return validFloor(floor) && (this.getOrCreate(player).cleanupFloors & bit(floor)) != 0L;
   }

   public boolean claimTransition(ServerPlayer player, int currentFloor) {
      if (currentFloor >= 1 && currentFloor < 20) {
         DkcRunSavedData.RunState state = this.getOrCreate(player);
         long transitionBit = bit(currentFloor);
         long nextBit = bit(currentFloor + 1);
         if ((state.armedTransitions & transitionBit) != 0L) {
            return false;
         }

         state.armedTransitions |= transitionBit;
         state.unlockedFloors |= nextBit;
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public void unlockFloor(ServerPlayer player, int floor) {
      if (validFloor(floor)) {
         DkcRunSavedData.RunState state = this.getOrCreate(player);
         long next = state.unlockedFloors | bit(floor);
         if (next != state.unlockedFloors) {
            state.unlockedFloors = next;
            this.setDirty();
         }
      }
   }

   public void markGenerated(ServerPlayer player, int floor) {
      if (validFloor(floor)) {
         DkcRunSavedData.RunState state = this.getOrCreate(player);
         long next = state.generatedFloors | bit(floor);
         long cleanup = state.cleanupFloors & ~bit(floor);
         if (next != state.generatedFloors || cleanup != state.cleanupFloors) {
            state.generatedFloors = next;
            state.cleanupFloors = cleanup;
            this.setDirty();
         }
      }
   }

   public void setDebugProgress(ServerPlayer player, int clearedFloors) {
      int cleared = Math.max(0, Math.min(20, clearedFloors));
      DkcRunSavedData.RunState state = this.getOrCreate(player);
      long unlocked = 0L;
      long armed = 0L;

      for (int floor = 1; floor <= Math.min(20, cleared + 1); floor++) {
         unlocked |= bit(floor);
      }

      for (int floor = 1; floor <= Math.min(19, cleared); floor++) {
         armed |= bit(floor);
      }

      state.unlockedFloors = unlocked;
      state.armedTransitions = armed;
      state.generatedFloors &= unlocked;
      this.setDirty();
   }

   public void resetProgress(UUID playerId) {
      DkcRunSavedData.RunState state = playerId == null ? null : this.runs.get(playerId);
      if (state != null) {
         state.unlockedFloors = bit(1);
         state.armedTransitions = 0L;
         state.anchorSynced = false;
         this.setDirty();
      }
   }

   @Nonnull
   @Override
   public CompoundTag save(@Nonnull CompoundTag root) {
      root.putInt("SchemaVersion", 3);
      root.putInt("LayoutVersion", 6);
      ListTag list = new ListTag();

      for (Entry<UUID, DkcRunSavedData.RunState> entry : this.runs.entrySet()) {
         CompoundTag tag = new CompoundTag();
         tag.putUUID("Player", entry.getKey());
         tag.putInt("Slot", entry.getValue().slot);
         tag.putLong("Unlocked", entry.getValue().unlockedFloors & 1048575L);
         tag.putLong("Generated", entry.getValue().generatedFloors & 1048575L);
         tag.putLong("Armed", entry.getValue().armedTransitions & 1048575L);
         tag.putLong("Cleanup", entry.getValue().cleanupFloors & 1048575L);
         list.add(tag);
      }

      root.put("Runs", list);
      return root;
   }

   private static DkcRunSavedData load(CompoundTag root) {
      DkcRunSavedData data = new DkcRunSavedData();
      boolean layoutMatches = root.getInt("LayoutVersion") == 6;
      boolean[] usedSlots = new boolean[4096];
      if (!root.contains("Runs", 9)) {
         return data;
      }

      ListTag list = root.getList("Runs", 10);

      for (int index = 0; index < Math.min(list.size(), 4096); index++) {
         CompoundTag tag = list.getCompound(index);
         if (tag.hasUUID("Player")) {
            UUID playerId = tag.getUUID("Player");
            if (data.runs.containsKey(playerId)) {
               data.setDirty();
            } else {
               DkcRunSavedData.RunState state = new DkcRunSavedData.RunState();
               int requestedSlot = tag.contains("Slot", 3) ? tag.getInt("Slot") : -1;
               if (DkcSpatialLayout.validSlot(requestedSlot) && !usedSlots[requestedSlot]) {
                  state.slot = requestedSlot;
                  usedSlots[requestedSlot] = true;
               } else {
                  state.slot = -1;
                  data.setDirty();
               }

               state.unlockedFloors = (tag.getLong("Unlocked") | bit(1)) & 1048575L;
               long storedGenerated = tag.getLong("Generated") & 1048575L;
               state.generatedFloors = layoutMatches && DkcSpatialLayout.validSlot(state.slot) ? storedGenerated : 0L;
               state.armedTransitions = tag.getLong("Armed") & 1048575L;
               state.cleanupFloors = tag.getLong("Cleanup") & 1048575L;
               if (!layoutMatches && DkcSpatialLayout.validSlot(state.slot)) {
                  state.cleanupFloors = state.cleanupFloors | storedGenerated & bit(15);
               }

               data.runs.put(playerId, state);
            }
         }
      }

      if (!layoutMatches) {
         data.setDirty();
      }

      return data;
   }

   private void ensureSlot(DkcRunSavedData.RunState state) {
      if (!DkcSpatialLayout.validSlot(state.slot)) {
         state.slot = this.allocateSlot();
         state.generatedFloors = 0L;
         state.cleanupFloors = 0L;
         state.anchorSynced = false;
         this.setDirty();
      }
   }

   private int allocateSlot() {
      boolean[] used = new boolean[4096];

      for (DkcRunSavedData.RunState state : this.runs.values()) {
         if (DkcSpatialLayout.validSlot(state.slot)) {
            used[state.slot] = true;
         }
      }

      for (int slot = 0; slot < used.length; slot++) {
         if (!used[slot]) {
            return slot;
         }
      }

      throw new IllegalStateException("Demon King's Castle has exhausted all spatial run slots.");
   }

   private static void syncLegacyAnchor(ServerPlayer player, DkcRunSavedData.RunState state) {
      if (!state.anchorSynced && DkcSpatialLayout.validSlot(state.slot)) {
         BlockPos anchor = DkcSpatialLayout.floorOrigin(state.slot, 1);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.dkc_x != anchor.getX() || capability.dkc_y != anchor.getY() || capability.dkc_z != anchor.getZ()) {
               capability.dkc_x = anchor.getX();
               capability.dkc_y = anchor.getY();
               capability.dkc_z = anchor.getZ();
               capability.syncPlayerVariables(player);
            }
         });
         state.anchorSynced = true;
      }
   }

   private static long bit(int floor) {
      return 1L << floor - 1;
   }

   private static boolean validFloor(int floor) {
      return floor >= 1 && floor <= 20;
   }

   public static final class RunState {
      private int slot = -1;
      private transient boolean anchorSynced;
      private long unlockedFloors;
      private long generatedFloors;
      private long armedTransitions;
      private long cleanupFloors;

      public long unlockedFloors() {
         return this.unlockedFloors;
      }

      public long generatedFloors() {
         return this.generatedFloors;
      }

      public long armedTransitions() {
         return this.armedTransitions;
      }

      public int slot() {
         return this.slot;
      }
   }
}
