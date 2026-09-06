package dev.eness.sololevelingfinal.core.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.party.PartyService;

@EventBusSubscriber(bus = Bus.MOD)
public final class PartyActionMessage {
   private static final int MAX_ACTION_LENGTH = 32;
   private final String action;
   private final CompoundTag payload;

   public PartyActionMessage(String action, CompoundTag payload) {
      String safeAction = action == null ? "" : action;
      this.action = safeAction.substring(0, Math.min(32, safeAction.length()));
      this.payload = boundedPayload(payload);
   }

   public PartyActionMessage(FriendlyByteBuf buffer) {
      this.action = buffer.readUtf(32);
      CompoundTag read = new CompoundTag();
      read.putString("Name", buffer.readUtf(24));
      readUuid(buffer, read, "PartyId");
      readUuid(buffer, read, "PlayerId");
      read.putBoolean("Enabled", buffer.readBoolean());
      read.putInt("Color", buffer.readInt() & 16777215);
      this.payload = read;
   }

   public static void buffer(PartyActionMessage message, FriendlyByteBuf buffer) {
      buffer.writeUtf(message.action, 32);
      buffer.writeUtf(message.payload.getString("Name"), 24);
      writeUuid(buffer, message.payload, "PartyId");
      writeUuid(buffer, message.payload, "PlayerId");
      buffer.writeBoolean(message.payload.getBoolean("Enabled"));
      buffer.writeInt(message.payload.getInt("Color") & 16777215);
   }

   public static void handler(PartyActionMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            PartyService.handleAction(player, message.action, message.payload);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         PartyActionMessage.class, PartyActionMessage::buffer, PartyActionMessage::new, PartyActionMessage::handler, NetworkDirection.PLAY_TO_SERVER
      );
   }

   private static CompoundTag boundedPayload(CompoundTag source) {
      CompoundTag bounded = new CompoundTag();
      if (source == null) {
         return bounded;
      }

      String name = source.getString("Name");
      bounded.putString("Name", name.substring(0, Math.min(24, name.length())));
      copyUuid(source, bounded, "PartyId");
      copyUuid(source, bounded, "PlayerId");
      bounded.putBoolean("Enabled", source.getBoolean("Enabled"));
      bounded.putInt("Color", source.getInt("Color") & 16777215);
      return bounded;
   }

   private static void copyUuid(CompoundTag source, CompoundTag target, String key) {
      if (source.hasUUID(key)) {
         target.putUUID(key, source.getUUID(key));
      }
   }

   private static void writeUuid(FriendlyByteBuf buffer, CompoundTag source, String key) {
      boolean present = source.hasUUID(key);
      buffer.writeBoolean(present);
      if (present) {
         buffer.writeUUID(source.getUUID(key));
      }
   }

   private static void readUuid(FriendlyByteBuf buffer, CompoundTag target, String key) {
      if (buffer.readBoolean()) {
         UUID value = buffer.readUUID();
         target.putUUID(key, value);
      }
   }
}
