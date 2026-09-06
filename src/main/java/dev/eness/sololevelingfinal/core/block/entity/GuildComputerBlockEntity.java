package dev.eness.sololevelingfinal.core.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import dev.eness.sololevelingfinal.core.guild.GuildData;
import dev.eness.sololevelingfinal.core.guild.GuildDeployment;
import dev.eness.sololevelingfinal.core.guild.GuildGateHelper;
import dev.eness.sololevelingfinal.core.guild.GuildHunter;
import dev.eness.sololevelingfinal.core.guild.GuildMemberPermissions;
import dev.eness.sololevelingfinal.core.guild.GuildSavedData;
import dev.eness.sololevelingfinal.core.guild.GuildTeam;
import dev.eness.sololevelingfinal.core.guild.GuildTickHandler;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlockEntities;
import dev.eness.sololevelingfinal.core.world.inventory.GuildComputerMenu;

public class GuildComputerBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {
   private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
   @Nullable
   private UUID boundGuildId = null;
   private static final int[] NO_SLOTS = new int[0];

   public GuildComputerBlockEntity(BlockPos pos, BlockState state) {
      super(SololevelingModBlockEntities.GUILD_COMPUTER.get(), pos, state);
   }

   @Nullable
   public UUID getBoundGuildId() {
      return this.boundGuildId;
   }

   public void setBoundGuildId(UUID id) {
      this.boundGuildId = id;
      this.setChanged();
   }

   @Nullable
   public GuildData getGuild() {
      return this.level != null && !this.level.isClientSide() && this.boundGuildId != null
         ? GuildSavedData.get((ServerLevel)this.level).getGuild(this.boundGuildId)
         : null;
   }

   @Nullable
   private GuildData getGuildFor(Player player) {
      return this.level != null && !this.level.isClientSide() ? GuildSavedData.get((ServerLevel)this.level).getGuildForPlayer(player.getUUID()) : null;
   }

   public NonNullList<ItemStack> getItems() {
      return this.items;
   }

   @Override
   public int getContainerSize() {
      return 27;
   }

   @Override
   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   @Override
   public ItemStack getItem(int slot) {
      return this.items.get(slot);
   }

   @Override
   public ItemStack removeItem(int slot, int count) {
      ItemStack result = ContainerHelper.removeItem(this.items, slot, count);
      if (!result.isEmpty()) {
         this.setChanged();
      }

      return result;
   }

   @Override
   public ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.items, slot);
   }

   @Override
   public void setItem(int slot, ItemStack stack) {
      this.items.set(slot, stack);
      if (stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   @Override
   public void setChanged() {
      super.setChanged();
   }

   @Override
   public boolean stillValid(Player player) {
      return true;
   }

   @Override
   public void clearContent() {
      this.items.clear();
      this.setChanged();
   }

   @Override
   public int[] getSlotsForFace(Direction side) {
      return NO_SLOTS;
   }

   @Override
   public boolean canPlaceItemThroughFace(int i, ItemStack s, Direction d) {
      return false;
   }

   @Override
   public boolean canTakeItemThroughFace(int i, ItemStack s, Direction d) {
      return false;
   }

   @Override
   public Component getDisplayName() {
      return Component.literal("Guild Computer");
   }

   @Override
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
      return new GuildComputerMenu(containerId, playerInventory, this, player);
   }

   public void writeScreenOpeningData(Player player, FriendlyByteBuf buf) {
      buf.writeBlockPos(this.getBlockPos());
      GuildSavedData guildData = this.level != null && !this.level.isClientSide() ? GuildSavedData.get((ServerLevel)this.level) : null;
      GuildData guild = this.getGuildFor(player);
      if (guild == null) {
         buf.writeBoolean(false);
      } else {
         if (guildData != null) {
            GuildTickHandler.resolveDueDeployments(((ServerLevel)this.level).getServer(), ((ServerLevel)this.level).getServer().overworld(), guildData);
            this.migrateLegacyStorageToGuild(guild, guildData);
            guild.pruneTeamMembers();
            guild.reconcileHunterDeploymentStatus();
            guildData.markDirty();
         }

         buf.writeBoolean(true);
         buf.writeUUID(guild.id);
         buf.writeUtf(guild.name);
         buf.writeUUID(guild.ownerUUID);
         buf.writeUtf(guild.ownerName);
         buf.writeInt(guild.level);
         buf.writeLong(guild.xp);
         buf.writeLong(GuildData.xpForLevel(guild.level));
         buf.writeInt(guild.totalClears);
         buf.writeInt(guild.activeBuffSlot1);
         buf.writeInt(guild.activeBuffSlot2);
         buf.writeUUID(player.getUUID());
         buf.writeBoolean(guild.canOperate(player.getUUID()));
         buf.writeInt(guild.memberPermissions.size());

         for (GuildMemberPermissions p : guild.memberPermissions) {
            buf.writeUUID(p.playerUUID);
            buf.writeUtf(p.playerName);
            buf.writeBoolean(p.canOpen);
            buf.writeBoolean(p.tabOverview);
            buf.writeBoolean(p.tabRoster);
            buf.writeBoolean(p.tabTeams);
            buf.writeBoolean(p.tabDungeons);
            buf.writeBoolean(p.tabStorage);
            buf.writeBoolean(p.tabBuffs);
            buf.writeBoolean(p.tabLeaderboard);
         }

         buf.writeInt(guild.hunters.size());

         for (GuildHunter h : guild.hunters) {
            buf.writeUUID(h.id);
            buf.writeUtf(h.name);
            buf.writeUtf(h.rank);
            buf.writeUtf(h.hunterClass);
            buf.writeUtf(h.status);
         }

         buf.writeInt(guild.recruitPool.size());

         for (GuildHunter h : guild.recruitPool) {
            buf.writeUUID(h.id);
            buf.writeUtf(h.name);
            buf.writeUtf(h.rank);
            buf.writeUtf(h.hunterClass);
         }

         if (this.level != null && !this.level.isClientSide()) {
            List<GuildData> lb = GuildSavedData.get((ServerLevel)this.level).getLeaderboard();
            int size = Math.min(lb.size(), 10);
            buf.writeInt(size);

            for (int i = 0; i < size; i++) {
               GuildData g = lb.get(i);
               buf.writeUtf(g.name);
               buf.writeInt(g.level);
               buf.writeInt(g.totalClears);
               buf.writeBoolean(g.id.equals(guild.id));
            }
         } else {
            buf.writeInt(0);
         }

         buf.writeInt(guild.teams.size());

         for (GuildTeam t : guild.teams) {
            buf.writeUUID(t.id);
            buf.writeUtf(t.name);
            buf.writeBoolean(t.autoRaidEnabled);
            buf.writeInt(t.autoRaidMaxRank);
            buf.writeInt(t.memberIds.size());

            for (UUID mid : t.memberIds) {
               buf.writeUUID(mid);
            }
         }

         if (this.level != null && !this.level.isClientSide()) {
            ServerLevel sl = (ServerLevel)this.level;
            List<Entity> gates = sl.getEntitiesOfClass(Entity.class, new AABB(this.getBlockPos()).inflate(256.0), GuildGateHelper::isDeployableGate);
            List<Entity> available = new ArrayList<>();

            for (Entity g : gates) {
               String gateId = g.getUUID().toString();
               if (!GuildGateHelper.isGateInteracted(g)
                  && !GuildGateHelper.isGateReserved(g)
                  && GuildGateHelper.findGuildRaidingGate(guildData, gateId) == null) {
                  available.add(g);
               }
            }

            buf.writeInt(available.size());

            for (Entity g : available) {
               buf.writeUUID(g.getUUID());
               buf.writeBlockPos(g.blockPosition());
               buf.writeUtf(GuildGateHelper.gateLabel(g));
               buf.writeInt(GuildGateHelper.gateRank(g));
            }
         } else {
            buf.writeInt(0);
         }

         buf.writeInt(guild.deployments.size());

         for (GuildDeployment d : guild.deployments) {
            buf.writeUUID(d.id);
            buf.writeUUID(d.teamId);
            buf.writeUtf(d.teamName);
            buf.writeUtf(d.gateLabel);
            buf.writeInt(d.gateRank);
            buf.writeLong(d.completesAt);
            buf.writeLong(d.xpReward);
         }

         buf.writeLong(this.level != null && !this.level.isClientSide() ? ((ServerLevel)this.level).getGameTime() : 0L);
      }
   }

   private void migrateLegacyStorageToGuild(GuildData guild, GuildSavedData guildData) {
      if (this.boundGuildId != null && this.boundGuildId.equals(guild.id) && !this.isEmpty()) {
         boolean movedAny = false;

         for (int i = 0; i < this.items.size(); i++) {
            ItemStack stack = this.items.get(i);
            if (!stack.isEmpty()) {
               ItemStack remaining = stack.copy();
               if (i < guild.storageItems.size() && guild.storageItems.get(i).isEmpty()) {
                  guild.storageItems.set(i, remaining);
                  this.items.set(i, ItemStack.EMPTY);
                  movedAny = true;
               } else {
                  remaining = this.moveIntoGuildStorage(remaining, guild);
                  if (remaining.isEmpty()) {
                     this.items.set(i, ItemStack.EMPTY);
                     movedAny = true;
                  } else if (remaining.getCount() != stack.getCount()) {
                     this.items.set(i, remaining);
                     movedAny = true;
                  }
               }
            }
         }

         if (movedAny) {
            guildData.markDirty();
            this.setChanged();
         }
      }
   }

   private ItemStack moveIntoGuildStorage(ItemStack stack, GuildData guild) {
      for (int i = 0; i < guild.storageItems.size(); i++) {
         ItemStack target = guild.storageItems.get(i);
         if (!target.isEmpty() && ItemStack.isSameItemSameTags(target, stack)) {
            int move = Math.min(stack.getCount(), target.getMaxStackSize() - target.getCount());
            if (move > 0) {
               target.grow(move);
               stack.shrink(move);
               if (stack.isEmpty()) {
                  return ItemStack.EMPTY;
               }
            }
         }
      }

      for (int i = 0; i < guild.storageItems.size(); i++) {
         if (guild.storageItems.get(i).isEmpty()) {
            guild.storageItems.set(i, stack.copy());
            return ItemStack.EMPTY;
         }
      }

      return stack;
   }

   @Override
   public void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      ContainerHelper.saveAllItems(tag, this.items);
      if (this.boundGuildId != null) {
         tag.putUUID("boundGuildId", this.boundGuildId);
      }
   }

   @Override
   public void load(CompoundTag tag) {
      super.load(tag);
      ContainerHelper.loadAllItems(tag, this.items);
      if (tag.hasUUID("boundGuildId")) {
         this.boundGuildId = tag.getUUID("boundGuildId");
      }
   }
}
