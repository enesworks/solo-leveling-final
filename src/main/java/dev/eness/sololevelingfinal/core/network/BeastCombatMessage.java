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
import dev.eness.sololevelingfinal.core.util.BeastMonarchManager;

@EventBusSubscriber(bus = Bus.MOD)
public final class BeastCombatMessage {
   public BeastCombatMessage() {
   }

   public BeastCombatMessage(FriendlyByteBuf buffer) {
   }

   public static void buffer(BeastCombatMessage message, FriendlyByteBuf buffer) {
   }

   public static void handler(BeastCombatMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            BeastMonarchManager.enhancedClawStrike(player);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(BeastCombatMessage.class, BeastCombatMessage::buffer, BeastCombatMessage::new, BeastCombatMessage::handler);
   }
}
