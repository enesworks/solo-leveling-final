package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.SystemNotificationManager;

@EventBusSubscriber(bus = Bus.MOD)
public class ShowNotificationMessage {
   private final int accentColor;
   private final int durationTicks;
   private final Component title;
   private final Component undertext;
   private final boolean negativeSound;

   public ShowNotificationMessage(int accentColor, int durationTicks, Component title, Component undertext) {
      this(accentColor, durationTicks, title, undertext, false);
   }

   public ShowNotificationMessage(int accentColor, int durationTicks, Component title, Component undertext, boolean negativeSound) {
      this.accentColor = accentColor;
      this.durationTicks = durationTicks;
      this.title = title;
      this.undertext = undertext;
      this.negativeSound = negativeSound;
   }

   public ShowNotificationMessage(FriendlyByteBuf buffer) {
      this.accentColor = buffer.readInt();
      this.durationTicks = buffer.readInt();
      this.title = buffer.readBoolean() ? buffer.readComponent() : null;
      this.undertext = buffer.readBoolean() ? buffer.readComponent() : null;
      this.negativeSound = buffer.readBoolean();
   }

   public static void buffer(ShowNotificationMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.accentColor);
      buffer.writeInt(message.durationTicks);
      buffer.writeBoolean(message.title != null);
      if (message.title != null) {
         buffer.writeComponent(message.title);
      }

      buffer.writeBoolean(message.undertext != null);
      if (message.undertext != null) {
         buffer.writeComponent(message.undertext);
      }

      buffer.writeBoolean(message.negativeSound);
   }

   public static void handler(ShowNotificationMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         if (!context.getDirection().getReceptionSide().isServer()) {
            SystemNotificationManager.INSTANCE.push(message.accentColor, message.durationTicks, message.title, message.undertext, message.negativeSound);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         ShowNotificationMessage.class, ShowNotificationMessage::buffer, ShowNotificationMessage::new, ShowNotificationMessage::handler
      );
   }
}
