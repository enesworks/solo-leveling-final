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
import dev.eness.sololevelingfinal.core.util.ShadowGlowManager;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber(bus = Bus.MOD)
public class ShadowGlowColorMessage {
   private final String shadowType;
   private final int color;

   public ShadowGlowColorMessage(FriendlyByteBuf buffer) {
      this.shadowType = buffer.readUtf(24);
      this.color = buffer.readInt();
   }

   public ShadowGlowColorMessage(String shadowType, int color) {
      this.shadowType = shadowType == null ? "" : shadowType;
      this.color = color;
   }

   public static void buffer(ShadowGlowColorMessage message, FriendlyByteBuf buffer) {
      buffer.writeUtf(message.shadowType, 24);
      buffer.writeInt(message.color);
   }

   public static void handler(ShadowGlowColorMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            if (ShadowMonarchManager.customizableTypes().contains(message.shadowType)) {
               int color = message.color == -1 ? -1 : message.color & 16777215;
               ShadowMonarchManager.setGlowColor(player, message.shadowType, color);
               ShadowGlowManager.syncNow(player);
            }
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         ShadowGlowColorMessage.class, ShadowGlowColorMessage::buffer, ShadowGlowColorMessage::new, ShadowGlowColorMessage::handler
      );
   }
}
