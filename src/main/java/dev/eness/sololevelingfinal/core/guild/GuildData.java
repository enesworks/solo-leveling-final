package dev.eness.sololevelingfinal.core.guild;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public class GuildData {
   public UUID id;
   public String name;
   public UUID ownerUUID;
   public String ownerName;
   public int level = 1;
   public long xp = 0L;
   public int totalClears = 0;
   public List<GuildMemberPermissions> memberPermissions = new ArrayList<>();
   public List<GuildHunter> hunters = new ArrayList<>();
   public List<GuildHunter> recruitPool = new ArrayList<>();
   public static final int MAX_HUNTERS = 25;
   public List<GuildTeam> teams = new ArrayList<>();
   public List<GuildDeployment> deployments = new ArrayList<>();
   public NonNullList<ItemStack> storageItems = NonNullList.withSize(27, ItemStack.EMPTY);
   public int activeBuffSlot1 = 0;
   public int activeBuffSlot2 = 0;

   public int recruitPoolSize() {
      return Math.min(3 + this.level, 10);
   }

   public GuildData(UUID id, String name, UUID ownerUUID, String ownerName) {
      this.id = id;
      this.name = name;
      this.ownerUUID = ownerUUID;
      this.ownerName = ownerName;
      this.initDefaultTeams();
   }

   private void initDefaultTeams() {
      String[] defaultNames = new String[]{"Alpha", "Beta", "Gamma", "Delta", "Epsilon"};

      for (String tname : defaultNames) {
         this.teams.add(new GuildTeam(UUID.randomUUID(), tname));
      }
   }

   public void awardXp(long amount) {
      for (this.xp += amount; this.xp >= xpForLevel(this.level); this.level++) {
         this.xp = this.xp - xpForLevel(this.level);
      }
   }

   public static long xpForLevel(int level) {
      return (long)level * level * 500L;
   }

   public GuildMemberPermissions getPermissions(UUID playerUUID) {
      for (GuildMemberPermissions p : this.memberPermissions) {
         if (p.playerUUID.equals(playerUUID)) {
            return p;
         }
      }

      return null;
   }

   public boolean canAccess(UUID playerUUID) {
      if (playerUUID.equals(this.ownerUUID)) {
         return true;
      }

      GuildMemberPermissions p = this.getPermissions(playerUUID);
      return p != null && p.canOpen;
   }

   public boolean canOperate(UUID playerUUID) {
      return playerUUID.equals(this.ownerUUID);
   }

   public boolean canAccessTab(UUID playerUUID, String tab) {
      if (playerUUID.equals(this.ownerUUID)) {
         return true;
      }

      GuildMemberPermissions p = this.getPermissions(playerUUID);
      if (p == null) {
         return false;
      }

      return switch (tab) {
         case "overview" -> p.tabOverview;
         case "roster" -> p.tabRoster;
         case "teams" -> p.tabTeams;
         case "dungeons" -> p.tabDungeons;
         case "storage" -> p.tabStorage;
         case "buffs" -> p.tabBuffs;
         case "leaderboard" -> p.tabLeaderboard;
         default -> false;
      };
   }

   public GuildHunter getHunter(UUID hunterId) {
      for (GuildHunter h : this.hunters) {
         if (h.id.equals(hunterId)) {
            return h;
         }
      }

      return null;
   }

   public GuildHunter getRecruit(UUID recruitId) {
      for (GuildHunter h : this.recruitPool) {
         if (h.id.equals(recruitId)) {
            return h;
         }
      }

      return null;
   }

   public GuildTeam getTeam(UUID teamId) {
      for (GuildTeam t : this.teams) {
         if (t.id.equals(teamId)) {
            return t;
         }
      }

      return null;
   }

   public GuildTeam getTeamForHunter(UUID hunterId) {
      for (GuildTeam t : this.teams) {
         if (t.memberIds.contains(hunterId)) {
            return t;
         }
      }

      return null;
   }

   public void removeHunterFromAllTeams(UUID hunterId) {
      for (GuildTeam t : this.teams) {
         t.memberIds.removeIf(id -> id.equals(hunterId));
      }
   }

   public void pruneTeamMembers() {
      for (GuildTeam t : this.teams) {
         Set<UUID> seen = new HashSet<>();
         t.memberIds.removeIf(id -> this.getHunter(id) == null || !seen.add(id));
      }
   }

   public void reconcileHunterDeploymentStatus() {
      Set<UUID> deployedHunters = new HashSet<>();
      this.deployments.removeIf(deployment -> this.getTeam(deployment.teamId) == null);

      for (GuildDeployment deployment : this.deployments) {
         GuildTeam team = this.getTeam(deployment.teamId);
         if (team != null) {
            for (UUID hunterId : team.memberIds) {
               if (this.getHunter(hunterId) != null) {
                  deployedHunters.add(hunterId);
               }
            }
         }
      }

      for (GuildHunter hunter : this.hunters) {
         if (deployedHunters.contains(hunter.id)) {
            hunter.status = "deployed";
         } else if ("deployed".equals(hunter.status)) {
            hunter.status = "idle";
         }
      }
   }

   public GuildDeployment getDeploymentForTeam(UUID teamId) {
      for (GuildDeployment d : this.deployments) {
         if (d.teamId.equals(teamId)) {
            return d;
         }
      }

      return null;
   }

   public String getRankBadge() {
      return switch (this.level) {
         case 1 -> "§7[E]";
         case 2 -> "§7[D]";
         case 3 -> "§a[C]";
         case 4 -> "§b[B]";
         case 5 -> "§e[A]";
         default -> "§6[S]";
      };
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("id", this.id);
      tag.putString("name", this.name);
      tag.putUUID("ownerUUID", this.ownerUUID);
      tag.putString("ownerName", this.ownerName);
      tag.putInt("level", this.level);
      tag.putLong("xp", this.xp);
      tag.putInt("totalClears", this.totalClears);
      tag.putInt("activeBuffSlot1", this.activeBuffSlot1);
      tag.putInt("activeBuffSlot2", this.activeBuffSlot2);
      ListTag permList = new ListTag();

      for (GuildMemberPermissions p : this.memberPermissions) {
         permList.add(p.save());
      }

      tag.put("members", permList);
      ListTag hunterList = new ListTag();

      for (GuildHunter h : this.hunters) {
         hunterList.add(h.save());
      }

      tag.put("hunters", hunterList);
      ListTag poolList = new ListTag();

      for (GuildHunter h : this.recruitPool) {
         poolList.add(h.save());
      }

      tag.put("recruitPool", poolList);
      ListTag teamList = new ListTag();

      for (GuildTeam t : this.teams) {
         teamList.add(t.save());
      }

      tag.put("teams", teamList);
      ListTag depList = new ListTag();

      for (GuildDeployment d : this.deployments) {
         depList.add(d.save());
      }

      tag.put("deployments", depList);
      CompoundTag storageTag = new CompoundTag();
      ContainerHelper.saveAllItems(storageTag, this.storageItems);
      tag.put("storage", storageTag);
      return tag;
   }

   public static GuildData load(CompoundTag tag) {
      GuildData g = new GuildData(tag.getUUID("id"), tag.getString("name"), tag.getUUID("ownerUUID"), tag.getString("ownerName"));
      g.level = tag.getInt("level");
      g.xp = tag.getLong("xp");
      g.totalClears = tag.getInt("totalClears");
      g.activeBuffSlot1 = tag.getInt("activeBuffSlot1");
      g.activeBuffSlot2 = tag.getInt("activeBuffSlot2");
      g.teams.clear();
      ListTag permList = tag.getList("members", 10);

      for (int i = 0; i < permList.size(); i++) {
         g.memberPermissions.add(GuildMemberPermissions.load(permList.getCompound(i)));
      }

      ListTag hunterList = tag.getList("hunters", 10);

      for (int i = 0; i < hunterList.size(); i++) {
         g.hunters.add(GuildHunter.load(hunterList.getCompound(i)));
      }

      ListTag poolList = tag.getList("recruitPool", 10);

      for (int i = 0; i < poolList.size(); i++) {
         g.recruitPool.add(GuildHunter.load(poolList.getCompound(i)));
      }

      ListTag teamList = tag.getList("teams", 10);
      if (teamList.isEmpty()) {
         g.initDefaultTeams();
      } else {
         for (int i = 0; i < teamList.size(); i++) {
            g.teams.add(GuildTeam.load(teamList.getCompound(i)));
         }
      }

      ListTag depList = tag.getList("deployments", 10);

      for (int i = 0; i < depList.size(); i++) {
         g.deployments.add(GuildDeployment.load(depList.getCompound(i)));
      }

      g.reconcileHunterDeploymentStatus();
      if (tag.contains("storage", 10)) {
         ContainerHelper.loadAllItems(tag.getCompound("storage"), g.storageItems);
      }

      return g;
   }
}
