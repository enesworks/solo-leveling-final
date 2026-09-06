package dev.eness.sololevelingfinal.core.world.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.block.entity.GuildComputerBlockEntity;
import dev.eness.sololevelingfinal.core.guild.GuildData;
import dev.eness.sololevelingfinal.core.guild.GuildHunter;
import dev.eness.sololevelingfinal.core.guild.GuildMemberPermissions;
import dev.eness.sololevelingfinal.core.guild.GuildSavedData;
import dev.eness.sololevelingfinal.core.init.SololevelingModMenus;

public class GuildComputerMenu extends AbstractContainerMenu {
   public static final int STORAGE_X = 89;
   public static final int STORAGE_Y = 46;
   public static final int INV_X = 89;
   public static final int INV_Y = 108;
   public static final int HOTBAR_Y = 166;
   public final BlockPos computerPos;
   public boolean hasGuild = false;
   public UUID guildId;
   public String guildName;
   public UUID ownerUUID;
   public String ownerName;
   public int guildLevel;
   public long guildXp;
   public long xpToNext;
   public int totalClears;
   public int activeBuffSlot1;
   public int activeBuffSlot2;
   public UUID viewerUUID;
   public boolean viewerIsOwner;
   public final List<GuildMemberPermissions> members = new ArrayList<>();
   public final List<GuildHunter> hunters = new ArrayList<>();
   public final List<GuildHunter> recruitPool = new ArrayList<>();
   public final List<GuildComputerMenu.LeaderboardEntry> leaderboard = new ArrayList<>();
   public final List<GuildComputerMenu.TeamInfo> teams = new ArrayList<>();
   public final List<GuildComputerMenu.NearbyGate> nearbyGates = new ArrayList<>();
   public final List<GuildComputerMenu.DeploymentInfo> deployments = new ArrayList<>();
   public long serverGameTime = 0L;
   private GuildComputerBlockEntity blockEntity;
   public boolean storageTabActive = false;

   public GuildComputerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
      super(SololevelingModMenus.GUILD_COMPUTER.get(), containerId);
      this.computerPos = buf.readBlockPos();
      this.hasGuild = buf.readBoolean();
      if (this.hasGuild) {
         this.guildId = buf.readUUID();
         this.guildName = buf.readUtf();
         this.ownerUUID = buf.readUUID();
         this.ownerName = buf.readUtf();
         this.guildLevel = buf.readInt();
         this.guildXp = buf.readLong();
         this.xpToNext = buf.readLong();
         this.totalClears = buf.readInt();
         this.activeBuffSlot1 = buf.readInt();
         this.activeBuffSlot2 = buf.readInt();
         this.viewerUUID = buf.readUUID();
         this.viewerIsOwner = buf.readBoolean();
         int memberCount = buf.readInt();

         for (int i = 0; i < memberCount; i++) {
            GuildMemberPermissions p = new GuildMemberPermissions(buf.readUUID(), buf.readUtf());
            p.canOpen = buf.readBoolean();
            p.tabOverview = buf.readBoolean();
            p.tabRoster = buf.readBoolean();
            p.tabTeams = buf.readBoolean();
            p.tabDungeons = buf.readBoolean();
            p.tabStorage = buf.readBoolean();
            p.tabBuffs = buf.readBoolean();
            p.tabLeaderboard = buf.readBoolean();
            this.members.add(p);
         }

         int hunterCount = buf.readInt();

         for (int i = 0; i < hunterCount; i++) {
            GuildHunter h = new GuildHunter(buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readUtf());
            h.status = buf.readUtf();
            this.hunters.add(h);
         }

         int poolCount = buf.readInt();

         for (int i = 0; i < poolCount; i++) {
            this.recruitPool.add(new GuildHunter(buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readUtf()));
         }

         int lbSize = buf.readInt();

         for (int i = 0; i < lbSize; i++) {
            this.leaderboard.add(new GuildComputerMenu.LeaderboardEntry(buf.readUtf(), buf.readInt(), buf.readInt(), buf.readBoolean()));
         }

         int teamCount = buf.readInt();

         for (int i = 0; i < teamCount; i++) {
            UUID teamId = buf.readUUID();
            String tname = buf.readUtf();
            boolean autoRaidEnabled = buf.readBoolean();
            int autoRaidMaxRank = buf.readInt();
            int mCount = buf.readInt();
            List<UUID> mids = new ArrayList<>();

            for (int j = 0; j < mCount; j++) {
               mids.add(buf.readUUID());
            }

            this.teams.add(new GuildComputerMenu.TeamInfo(teamId, tname, mids, autoRaidEnabled, autoRaidMaxRank));
         }

         int gateCount = buf.readInt();

         for (int i = 0; i < gateCount; i++) {
            this.nearbyGates.add(new GuildComputerMenu.NearbyGate(buf.readUUID(), buf.readBlockPos(), buf.readUtf(), buf.readInt()));
         }

         int depCount = buf.readInt();

         for (int i = 0; i < depCount; i++) {
            this.deployments
               .add(
                  new GuildComputerMenu.DeploymentInfo(
                     buf.readUUID(), buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readLong(), buf.readLong()
                  )
               );
         }

         this.serverGameTime = buf.readLong();
      } else {
         this.viewerUUID = playerInventory.player.getUUID();
         this.viewerIsOwner = false;
      }

      SimpleContainer clientStorage = new SimpleContainer(27);
      this.addStorageSlots(clientStorage, playerInventory);
   }

   public GuildComputerMenu(int containerId, Inventory playerInventory, GuildComputerBlockEntity be, Player player) {
      super(SololevelingModMenus.GUILD_COMPUTER.get(), containerId);
      this.blockEntity = be;
      this.computerPos = be.getBlockPos();
      GuildData guild = null;
      GuildSavedData savedData = null;
      if (!player.level().isClientSide()) {
         savedData = GuildSavedData.get((ServerLevel)player.level());
         guild = savedData.getGuildForPlayer(player.getUUID());
      }

      Container storage = guild != null ? new GuildComputerMenu.GuildStorageContainer(guild, savedData) : new SimpleContainer(27);
      this.addStorageSlots(storage, playerInventory);
   }

   private void addStorageSlots(Container storage, Inventory player) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new GuildComputerMenu.StorageSlot(storage, col + row * 9, 89 + col * 18, 46 + row * 18));
         }
      }

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new GuildComputerMenu.StorageSlot(player, col + row * 9 + 9, 89 + col * 18, 108 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new GuildComputerMenu.StorageSlot(player, col, 89 + col * 18, 166));
      }
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         result = stack.copy();
         if (index < 27) {
            if (!this.moveItemStackTo(stack, 27, 63, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 0, 27, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
         return result;
      } else {
         return ItemStack.EMPTY;
      }
   }

   @Override
   public boolean stillValid(Player player) {
      return true;
   }

   public record DeploymentInfo(UUID id, UUID teamId, String teamName, String gateLabel, int gateRank, long completesAt, long xpReward) {
   }

   private static class GuildStorageContainer implements Container {
      private final GuildData guild;
      private final GuildSavedData savedData;

      private GuildStorageContainer(GuildData guild, GuildSavedData savedData) {
         this.guild = guild;
         this.savedData = savedData;
      }

      @Override
      public int getContainerSize() {
         return this.guild.storageItems.size();
      }

      @Override
      public boolean isEmpty() {
         return this.guild.storageItems.stream().allMatch(ItemStack::isEmpty);
      }

      @Override
      public ItemStack getItem(int slot) {
         return this.guild.storageItems.get(slot);
      }

      @Override
      public ItemStack removeItem(int slot, int count) {
         ItemStack result = ContainerHelper.removeItem(this.guild.storageItems, slot, count);
         if (!result.isEmpty()) {
            this.setChanged();
         }

         return result;
      }

      @Override
      public ItemStack removeItemNoUpdate(int slot) {
         return ContainerHelper.takeItem(this.guild.storageItems, slot);
      }

      @Override
      public void setItem(int slot, ItemStack stack) {
         this.guild.storageItems.set(slot, stack);
         if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
         }

         this.setChanged();
      }

      @Override
      public void setChanged() {
         this.savedData.markDirty();
      }

      @Override
      public boolean stillValid(Player player) {
         return true;
      }

      @Override
      public void clearContent() {
         this.guild.storageItems.clear();
         this.setChanged();
      }
   }

   public record LeaderboardEntry(String name, int level, int clears, boolean isOwnGuild) {
   }

   public record NearbyGate(UUID entityId, BlockPos pos, String label, int rank) {
   }

   private class StorageSlot extends Slot {
      StorageSlot(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      @Override
      public boolean isActive() {
         return GuildComputerMenu.this.storageTabActive;
      }
   }

   public record TeamInfo(UUID id, String name, List<UUID> memberIds, boolean autoRaidEnabled, int autoRaidMaxRank) {
   }
}
