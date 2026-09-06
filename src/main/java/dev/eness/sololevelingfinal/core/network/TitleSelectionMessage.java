package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.util.TitleManager;

@EventBusSubscriber(bus = Bus.MOD)
public class TitleSelectionMessage {
   private final int titleId;

   public TitleSelectionMessage(int titleId) {
      this.titleId = titleId;
   }

   public TitleSelectionMessage(FriendlyByteBuf buffer) {
      this.titleId = buffer.readInt();
   }

   public static void buffer(TitleSelectionMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.titleId);
   }

   public static void handler(TitleSelectionMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            TitleManager.selectTitle(player, message.titleId);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(TitleSelectionMessage.class, TitleSelectionMessage::buffer, TitleSelectionMessage::new, TitleSelectionMessage::handler);
   }
}
