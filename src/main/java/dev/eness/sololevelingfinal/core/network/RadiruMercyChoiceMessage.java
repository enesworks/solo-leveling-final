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
import dev.eness.sololevelingfinal.core.dkc.DkcRadiruManager;

@EventBusSubscriber(bus = Bus.MOD)
public final class RadiruMercyChoiceMessage {
   private final boolean spare;

   public RadiruMercyChoiceMessage(boolean spare) {
      this.spare = spare;
   }

   public RadiruMercyChoiceMessage(FriendlyByteBuf buffer) {
      this(buffer.readBoolean());
   }

   public static void buffer(RadiruMercyChoiceMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.spare);
   }

   public static void handler(RadiruMercyChoiceMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            DkcRadiruManager.resolveMercyChoice(player, message.spare);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         RadiruMercyChoiceMessage.class, RadiruMercyChoiceMessage::buffer, RadiruMercyChoiceMessage::new, RadiruMercyChoiceMessage::handler
      );
   }
}
