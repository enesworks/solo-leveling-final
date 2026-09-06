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
import dev.eness.sololevelingfinal.core.dungeon.builder.DungeonBuilderStudioService;

@EventBusSubscriber(bus = Bus.MOD)
public final class DungeonBuilderStudioRequestMessage {
   public DungeonBuilderStudioRequestMessage() {
   }

   public DungeonBuilderStudioRequestMessage(FriendlyByteBuf ignored) {
   }

   public static void buffer(DungeonBuilderStudioRequestMessage message, FriendlyByteBuf buffer) {
   }

   public static void handler(DungeonBuilderStudioRequestMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            DungeonBuilderStudioService.requestOpen(player);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         DungeonBuilderStudioRequestMessage.class,
         DungeonBuilderStudioRequestMessage::buffer,
         DungeonBuilderStudioRequestMessage::new,
         DungeonBuilderStudioRequestMessage::handler
      );
   }
}
