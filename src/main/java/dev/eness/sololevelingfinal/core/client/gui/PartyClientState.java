package dev.eness.sololevelingfinal.core.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.PartyScreen;
import dev.eness.sololevelingfinal.core.network.PartyActionMessage;
import dev.eness.sololevelingfinal.core.network.PartyRequestMessage;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;

@EventBusSubscriber(modid = "sololeveling", value = Dist.CLIENT)
public final class PartyClientState {
   private static PartyClientState.Snapshot snapshot = PartyClientState.Snapshot.empty();
   private static boolean received;

   private PartyClientState() {
   }

   public static PartyClientState.Snapshot snapshot() {
      return snapshot;
   }

   public static boolean hasSnapshot() {
      return received;
   }

   public static void requestSnapshot() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.getConnection() != null) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new PartyRequestMessage(false));
      }
   }

   public static void refresh() {
      requestSnapshot();
   }

   public static void sendAction(String action) {
      sendAction(action, new CompoundTag());
   }

   public static void sendAction(String action, CompoundTag payload) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.getConnection() != null) {
         SololevelingMod.PACKET_HANDLER.sendToServer(new PartyActionMessage(action == null ? "" : action, payload == null ? new CompoundTag() : payload));
      }
   }

   public static void applySnapshot(boolean open, CompoundTag data) {
      Minecraft minecraft = Minecraft.getInstance();
      Runnable update = () -> {
         snapshot = PartyClientState.Snapshot.from(data == null ? new CompoundTag() : data.copy());
         received = true;
         if (minecraft.screen instanceof PartyScreen partyScreen) {
            partyScreen.onPartyStateChanged(snapshot);
         } else if (open && minecraft.player != null) {
            minecraft.setScreen(new PartyScreen(!SystemPlayerAccess.hasSystem(minecraft.player)));
         }
      };
      if (minecraft.isSameThread()) {
         update.run();
      } else {
         minecraft.execute(update);
      }
   }

   public static void clear() {
      snapshot = PartyClientState.Snapshot.empty();
      received = false;
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      clear();
   }

   private static UUID readUuid(CompoundTag tag, String key) {
      return tag.hasUUID(key) ? tag.getUUID(key) : null;
   }

   private static String clean(String value, int maximumLength) {
      if (value != null && !value.isEmpty()) {
         StringBuilder cleaned = new StringBuilder(Math.min(value.length(), maximumLength));

         for (int index = 0; index < value.length() && cleaned.length() < maximumLength; index++) {
            char character = value.charAt(index);
            if (character >= ' ' && character != 127 && character != 167) {
               cleaned.append(character);
            }
         }

         return cleaned.toString();
      } else {
         return "";
      }
   }

   public record JoinRequest(UUID id, String name, boolean online) {
   }

   public record Member(UUID id, String name, boolean online, int level, String rank, boolean leader) {
   }

   public record NearbyParty(UUID id, String name, String leaderName, int members, int maxMembers, int distance, boolean available, boolean requested) {
   }

   public record Snapshot(
      boolean inParty,
      UUID partyId,
      String partyName,
      UUID leaderId,
      String leaderName,
      boolean discoverable,
      int maxMembers,
      boolean glowEnabled,
      int glowColor,
      String notice,
      List<PartyClientState.Member> members,
      List<PartyClientState.NearbyParty> nearby,
      List<PartyClientState.JoinRequest> requests
   ) {
      private static PartyClientState.Snapshot empty() {
         return new PartyClientState.Snapshot(false, null, "", null, "", true, 8, true, 5626111, "", List.of(), List.of(), List.of());
      }

      private static PartyClientState.Snapshot from(CompoundTag tag) {
         List<PartyClientState.Member> members = new ArrayList<>();
         ListTag memberTags = tag.getList("Members", 10);

         for (int index = 0; index < memberTags.size(); index++) {
            CompoundTag member = memberTags.getCompound(index);
            members.add(
               new PartyClientState.Member(
                  PartyClientState.readUuid(member, "Id"),
                  PartyClientState.clean(member.getString("Name"), 32),
                  member.getBoolean("Online"),
                  Math.max(0, member.getInt("Level")),
                  PartyClientState.clean(member.getString("Rank"), 16),
                  member.getBoolean("Leader")
               )
            );
         }

         List<PartyClientState.NearbyParty> nearby = new ArrayList<>();
         ListTag nearbyTags = tag.getList("Nearby", 10);

         for (int index = 0; index < nearbyTags.size(); index++) {
            CompoundTag party = nearbyTags.getCompound(index);
            nearby.add(
               new PartyClientState.NearbyParty(
                  PartyClientState.readUuid(party, "PartyId"),
                  PartyClientState.clean(party.getString("Name"), 32),
                  PartyClientState.clean(party.getString("LeaderName"), 32),
                  Math.max(0, party.getInt("Members")),
                  Math.max(1, party.getInt("MaxMembers")),
                  Math.max(0, party.getInt("Distance")),
                  party.getBoolean("Available"),
                  party.getBoolean("Requested")
               )
            );
         }

         List<PartyClientState.JoinRequest> requests = new ArrayList<>();
         ListTag requestTags = tag.getList("Requests", 10);

         for (int index = 0; index < requestTags.size(); index++) {
            CompoundTag request = requestTags.getCompound(index);
            requests.add(
               new PartyClientState.JoinRequest(
                  PartyClientState.readUuid(request, "Id"), PartyClientState.clean(request.getString("Name"), 32), request.getBoolean("Online")
               )
            );
         }

         return new PartyClientState.Snapshot(
            tag.getBoolean("InParty"),
            PartyClientState.readUuid(tag, "PartyId"),
            PartyClientState.clean(tag.getString("PartyName"), 40),
            PartyClientState.readUuid(tag, "LeaderId"),
            PartyClientState.clean(tag.getString("LeaderName"), 32),
            tag.getBoolean("Discoverable"),
            Math.max(1, tag.getInt("MaxMembers")),
            tag.getBoolean("GlowEnabled"),
            tag.getInt("GlowColor") & 16777215,
            PartyClientState.clean(tag.getString("Notice"), 120),
            List.copyOf(members),
            List.copyOf(nearby),
            List.copyOf(requests)
         );
      }
   }
}
