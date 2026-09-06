package dev.eness.sololevelingfinal.core.network;

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
import dev.eness.sololevelingfinal.core.party.PartyService;

@EventBusSubscriber(bus = Bus.MOD)
public final class PartyRequestMessage {
   private final boolean open;

   public PartyRequestMessage(boolean open) {
      this.open = open;
   }

   public PartyRequestMessage(FriendlyByteBuf buffer) {
      this.open = buffer.readBoolean();
   }

   public static void buffer(PartyRequestMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.open);
   }

   public static void handler(PartyRequestMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            PartyService.requestSnapshot(player, message.open);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         PartyRequestMessage.class, PartyRequestMessage::buffer, PartyRequestMessage::new, PartyRequestMessage::handler, NetworkDirection.PLAY_TO_SERVER
      );
   }
}
