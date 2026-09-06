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
import dev.eness.sololevelingfinal.core.util.CartenonTempleManager;

@EventBusSubscriber(bus = Bus.MOD)
public final class CartenonAwakeningChoiceMessage {
   private final boolean accept;

   public CartenonAwakeningChoiceMessage(boolean accept) {
      this.accept = accept;
   }

   public CartenonAwakeningChoiceMessage(FriendlyByteBuf buffer) {
      this(buffer.readBoolean());
   }

   public static void buffer(CartenonAwakeningChoiceMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.accept);
   }

   public static void handler(CartenonAwakeningChoiceMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            CartenonTempleManager.resolveAwakeningChoice(player, message.accept);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         CartenonAwakeningChoiceMessage.class,
         CartenonAwakeningChoiceMessage::buffer,
         CartenonAwakeningChoiceMessage::new,
         CartenonAwakeningChoiceMessage::handler
      );
   }
}
