package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.CartenonAwakeningScreen;

@EventBusSubscriber(bus = Bus.MOD)
public final class CartenonAwakeningStateMessage {
   private final boolean open;

   public CartenonAwakeningStateMessage(boolean open) {
      this.open = open;
   }

   public CartenonAwakeningStateMessage(FriendlyByteBuf buffer) {
      this(buffer.readBoolean());
   }

   public static void buffer(CartenonAwakeningStateMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.open);
   }

   public static void handler(CartenonAwakeningStateMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CartenonAwakeningScreen.handleServerState(message.open)));
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         CartenonAwakeningStateMessage.class, CartenonAwakeningStateMessage::buffer, CartenonAwakeningStateMessage::new, CartenonAwakeningStateMessage::handler
      );
   }
}
