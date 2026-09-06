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
import dev.eness.sololevelingfinal.core.util.FrostArchitectureManager;

@EventBusSubscriber(bus = Bus.MOD)
public final class FrostArchitectureSelectionMessage {
   private final int blueprintId;

   public FrostArchitectureSelectionMessage(int blueprintId) {
      this.blueprintId = blueprintId;
   }

   public FrostArchitectureSelectionMessage(FriendlyByteBuf buffer) {
      this.blueprintId = buffer.readVarInt();
   }

   public static void buffer(FrostArchitectureSelectionMessage message, FriendlyByteBuf buffer) {
      buffer.writeVarInt(message.blueprintId);
   }

   public static void handler(FrostArchitectureSelectionMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null && message.blueprintId >= 0 && message.blueprintId < 6) {
            FrostArchitectureManager.castSelection(player, message.blueprintId);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         FrostArchitectureSelectionMessage.class,
         FrostArchitectureSelectionMessage::buffer,
         FrostArchitectureSelectionMessage::new,
         FrostArchitectureSelectionMessage::handler,
         NetworkDirection.PLAY_TO_SERVER
      );
   }
}
