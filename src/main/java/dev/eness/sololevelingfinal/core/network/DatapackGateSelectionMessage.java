package dev.eness.sololevelingfinal.core.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.DatapackGateSelectionService;

@EventBusSubscriber(bus = Bus.MOD)
public final class DatapackGateSelectionMessage {
   private static final int MAX_DUNGEON_ID_LENGTH = 192;
   private static final int MAX_RANK_LENGTH = 8;
   private final UUID gateId;
   private final long expectedRevision;
   private final String dungeonId;
   private final String rank;

   public DatapackGateSelectionMessage(UUID gateId, long expectedRevision, String dungeonId, String rank) {
      this.gateId = gateId == null ? new UUID(0L, 0L) : gateId;
      this.expectedRevision = Math.max(0L, expectedRevision);
      this.dungeonId = clean(dungeonId, 192);
      this.rank = clean(rank, 8);
   }

   public DatapackGateSelectionMessage(FriendlyByteBuf buffer) {
      this(buffer.readUUID(), buffer.readLong(), buffer.readUtf(192), buffer.readUtf(8));
   }

   public static void buffer(DatapackGateSelectionMessage message, FriendlyByteBuf buffer) {
      buffer.writeUUID(message.gateId);
      buffer.writeLong(message.expectedRevision);
      buffer.writeUtf(message.dungeonId, 192);
      buffer.writeUtf(message.rank, 8);
   }

   public static void handler(DatapackGateSelectionMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            DatapackGateSelectionService.select(player, message.gateId, message.expectedRevision, message.dungeonId, message.rank);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         DatapackGateSelectionMessage.class,
         DatapackGateSelectionMessage::buffer,
         DatapackGateSelectionMessage::new,
         DatapackGateSelectionMessage::handler,
         NetworkDirection.PLAY_TO_SERVER
      );
   }

   private static String clean(String value, int maximum) {
      if (value == null) {
         return "";
      }

      String clean = value.replace('\u0000', ' ').trim();
      return clean.length() <= maximum ? clean : clean.substring(0, maximum);
   }
}
