package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;

@EventBusSubscriber(bus = Bus.MOD)
public final class DeveloperModeStateMessage {
   private static volatile boolean clientEnabled;
   private final boolean enabled;

   public DeveloperModeStateMessage(boolean enabled) {
      this.enabled = enabled;
   }

   public DeveloperModeStateMessage(FriendlyByteBuf buffer) {
      this.enabled = buffer.readBoolean();
   }

   public static void buffer(DeveloperModeStateMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.enabled);
   }

   public static void handler(DeveloperModeStateMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> clientEnabled = message.enabled);
      context.setPacketHandled(true);
   }

   public static boolean isClientEnabled() {
      return clientEnabled;
   }

   public static void sync(ServerPlayer player, boolean enabled) {
      if (player != null && player.connection != null) {
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new DeveloperModeStateMessage(enabled));
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         DeveloperModeStateMessage.class,
         DeveloperModeStateMessage::buffer,
         DeveloperModeStateMessage::new,
         DeveloperModeStateMessage::handler,
         NetworkDirection.PLAY_TO_CLIENT
      );
   }
}
