package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.util.ClassPassiveClientState;

@EventBusSubscriber(bus = Bus.MOD)
public class ClassPassiveMessage {
   public final int passiveType;
   public final double value;

   public ClassPassiveMessage(int passiveType, double value) {
      this.passiveType = passiveType;
      this.value = value;
   }

   public ClassPassiveMessage(FriendlyByteBuf buf) {
      this.passiveType = buf.readInt();
      this.value = buf.readDouble();
   }

   public static void buffer(ClassPassiveMessage msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.passiveType);
      buf.writeDouble(msg.value);
   }

   public static void handler(ClassPassiveMessage msg, Supplier<Context> ctx) {
      ctx.get().enqueueWork(() -> ClassPassiveClientState.update(msg.passiveType, msg.value));
      ctx.get().setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(ClassPassiveMessage.class, ClassPassiveMessage::buffer, ClassPassiveMessage::new, ClassPassiveMessage::handler);
   }
}
