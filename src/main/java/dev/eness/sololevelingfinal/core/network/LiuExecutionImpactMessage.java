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
import dev.eness.sololevelingfinal.core.client.screens.LiuExecutionImpactRenderer;

@EventBusSubscriber(bus = Bus.MOD)
public final class LiuExecutionImpactMessage {
   private final int durationTicks;
   private final int primaryColor;
   private final int secondaryColor;

   public LiuExecutionImpactMessage(int durationTicks, int primaryColor, int secondaryColor) {
      this.durationTicks = durationTicks;
      this.primaryColor = primaryColor;
      this.secondaryColor = secondaryColor;
   }

   public LiuExecutionImpactMessage(FriendlyByteBuf buffer) {
      this(buffer.readVarInt(), buffer.readInt(), buffer.readInt());
   }

   public static void buffer(LiuExecutionImpactMessage message, FriendlyByteBuf buffer) {
      buffer.writeVarInt(message.durationTicks);
      buffer.writeInt(message.primaryColor);
      buffer.writeInt(message.secondaryColor);
   }

   public static void handler(LiuExecutionImpactMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(
         () -> DistExecutor.unsafeRunWhenOn(
            Dist.CLIENT, () -> () -> LiuExecutionImpactRenderer.start(message.durationTicks, message.primaryColor, message.secondaryColor)
         )
      );
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         LiuExecutionImpactMessage.class, LiuExecutionImpactMessage::buffer, LiuExecutionImpactMessage::new, LiuExecutionImpactMessage::handler
      );
   }
}
