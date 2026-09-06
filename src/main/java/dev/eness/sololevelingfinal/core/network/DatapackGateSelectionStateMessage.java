package dev.eness.sololevelingfinal.core.network;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.DatapackGateSelectionScreen;

@EventBusSubscriber(bus = Bus.MOD)
public final class DatapackGateSelectionStateMessage {
   public static final int MAX_OPTIONS = 256;
   private static final int MAX_DUNGEON_ID_LENGTH = 192;
   private static final int MAX_KIND_LENGTH = 16;
   private static final int MAX_NOTICE_LENGTH = 192;
   private static final int MAX_RANKS = 6;
   private static final int MAX_RANK_LENGTH = 8;
   private final boolean open;
   private final UUID gateId;
   private final long revision;
   private final List<DatapackGateSelectionStateMessage.Option> options;
   private final String notice;

   public DatapackGateSelectionStateMessage(boolean open, UUID gateId, long revision, List<DatapackGateSelectionStateMessage.Option> options, String notice) {
      this.open = open;
      this.gateId = gateId == null ? new UUID(0L, 0L) : gateId;
      this.revision = Math.max(0L, revision);
      this.options = options == null ? List.of() : options.stream().filter(Objects::nonNull).limit(256L).toList();
      this.notice = clean(notice, 192);
   }

   public DatapackGateSelectionStateMessage(FriendlyByteBuf buffer) {
      this.open = buffer.readBoolean();
      this.gateId = buffer.readUUID();
      this.revision = Math.max(0L, buffer.readLong());
      int size = readCount(buffer, 256, "datapack dungeon options");
      List<DatapackGateSelectionStateMessage.Option> decoded = new ArrayList<>(size);

      for (int index = 0; index < size; index++) {
         String dungeonId = buffer.readUtf(192);
         String kind = buffer.readUtf(16);
         int minRooms = buffer.readVarInt();
         int maxRooms = buffer.readVarInt();
         int rankCount = readCount(buffer, 6, "datapack dungeon ranks");
         List<String> ranks = new ArrayList<>(rankCount);

         for (int rankIndex = 0; rankIndex < rankCount; rankIndex++) {
            ranks.add(buffer.readUtf(8));
         }

         decoded.add(new DatapackGateSelectionStateMessage.Option(dungeonId, kind, minRooms, maxRooms, ranks));
      }

      this.options = List.copyOf(decoded);
      this.notice = buffer.readUtf(192);
   }

   public static void buffer(DatapackGateSelectionStateMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.open);
      buffer.writeUUID(message.gateId);
      buffer.writeLong(message.revision);
      buffer.writeVarInt(message.options.size());

      for (DatapackGateSelectionStateMessage.Option option : message.options) {
         buffer.writeUtf(option.dungeonId(), 192);
         buffer.writeUtf(option.kind(), 16);
         buffer.writeVarInt(option.minRooms());
         buffer.writeVarInt(option.maxRooms());
         buffer.writeVarInt(option.ranks().size());

         for (String rank : option.ranks()) {
            buffer.writeUtf(rank, 8);
         }
      }

      buffer.writeUtf(message.notice, 192);
   }

   public static void handler(DatapackGateSelectionStateMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(
         () -> DistExecutor.unsafeRunWhenOn(
            Dist.CLIENT,
            () -> () -> DatapackGateSelectionScreen.handleServerState(message.open, message.gateId, message.revision, message.options, message.notice)
         )
      );
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         DatapackGateSelectionStateMessage.class,
         DatapackGateSelectionStateMessage::buffer,
         DatapackGateSelectionStateMessage::new,
         DatapackGateSelectionStateMessage::handler,
         NetworkDirection.PLAY_TO_CLIENT
      );
   }

   private static int readCount(FriendlyByteBuf buffer, int maximum, String label) {
      int value = buffer.readVarInt();
      if (value >= 0 && value <= maximum) {
         return value;
      } else {
         throw new IllegalArgumentException("Invalid " + label + " count " + value);
      }
   }

   private static String clean(String value, int maximum) {
      if (value == null) {
         return "";
      }

      String clean = value.replace('\u0000', ' ').trim();
      return clean.length() <= maximum ? clean : clean.substring(0, maximum);
   }

   public record Option(String dungeonId, String kind, int minRooms, int maxRooms, List<String> ranks) {
      public Option(String dungeonId, String kind, int minRooms, int maxRooms, List<String> ranks) {
         dungeonId = DatapackGateSelectionStateMessage.clean(dungeonId, 192);
         kind = DatapackGateSelectionStateMessage.clean(kind, 16).toUpperCase(Locale.ROOT);
         minRooms = Math.max(1, Math.min(64, minRooms));
         maxRooms = Math.max(minRooms, Math.min(64, maxRooms));
         Set<String> cleanRanks = new LinkedHashSet<>();
         if (ranks != null) {
            for (String rank : ranks) {
               if (cleanRanks.size() >= 6) {
                  break;
               }

               String value = DatapackGateSelectionStateMessage.clean(rank, 8).toUpperCase(Locale.ROOT);
               if (!value.isBlank()) {
                  cleanRanks.add(value);
               }
            }
         }

         ranks = List.copyOf(cleanRanks);
         this.dungeonId = dungeonId;
         this.kind = kind;
         this.minRooms = minRooms;
         this.maxRooms = maxRooms;
         this.ranks = ranks;
      }
   }
}
