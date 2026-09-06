package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.DkcQuestProgressClientState;

@EventBusSubscriber(bus = Bus.MOD)
public final class DkcQuestProgressMessage {
   private final boolean active;
   private final int floor;
   private final int cleared;
   private final String floorName;
   private final String phase;
   private final String objective;
   private final String detail;
   private final int progress;
   private final int target;

   public DkcQuestProgressMessage(
      boolean active, int floor, int cleared, String floorName, String phase, String objective, String detail, int progress, int target
   ) {
      this.active = active;
      this.floor = floor;
      this.cleared = cleared;
      this.floorName = floorName;
      this.phase = phase;
      this.objective = objective;
      this.detail = detail;
      this.progress = progress;
      this.target = target;
   }

   public DkcQuestProgressMessage(FriendlyByteBuf buffer) {
      this.active = buffer.readBoolean();
      this.floor = buffer.readVarInt();
      this.cleared = buffer.readVarInt();
      this.floorName = buffer.readUtf(64);
      this.phase = buffer.readUtf(24);
      this.objective = buffer.readUtf(256);
      this.detail = buffer.readUtf(256);
      this.progress = buffer.readVarInt();
      this.target = buffer.readVarInt();
   }

   public static void buffer(DkcQuestProgressMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.active);
      buffer.writeVarInt(message.floor);
      buffer.writeVarInt(message.cleared);
      buffer.writeUtf(message.floorName, 64);
      buffer.writeUtf(message.phase, 24);
      buffer.writeUtf(message.objective, 256);
      buffer.writeUtf(message.detail, 256);
      buffer.writeVarInt(message.progress);
      buffer.writeVarInt(message.target);
   }

   public static void handler(DkcQuestProgressMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(
         () -> DkcQuestProgressClientState.update(
            message.active,
            message.floor,
            message.cleared,
            message.floorName,
            message.phase,
            message.objective,
            message.detail,
            message.progress,
            message.target
         )
      );
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         DkcQuestProgressMessage.class,
         DkcQuestProgressMessage::buffer,
         DkcQuestProgressMessage::new,
         DkcQuestProgressMessage::handler,
         NetworkDirection.PLAY_TO_CLIENT
      );
   }
}
