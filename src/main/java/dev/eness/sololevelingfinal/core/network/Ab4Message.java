package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;

@EventBusSubscriber(bus = Bus.MOD)
public class Ab4Message {
   int type;
   int pressedms;

   public Ab4Message(int type, int pressedms) {
      this.type = type;
      this.pressedms = pressedms;
   }

   public Ab4Message(FriendlyByteBuf buffer) {
      this.type = buffer.readInt();
      this.pressedms = buffer.readInt();
   }

   public static void buffer(Ab4Message message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.type);
      buffer.writeInt(message.pressedms);
   }

   public static void handler(Ab4Message message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> pressAction(context.getSender(), message.type, message.pressedms));
      context.setPacketHandled(true);
   }

   public static void pressAction(Player entity, int type, int pressedms) {
      Level world = entity.level();
      if (world.hasChunkAt(entity.blockPosition())) {
         if (type == 0) {
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               if (capability.combatmode) {
                  capability.PselectedPower = capability.Pslot4;
                  capability.Skillcycle = 4.0;
                  capability.syncPlayerVariables(entity);
               }
            });
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(Ab4Message.class, Ab4Message::buffer, Ab4Message::new, Ab4Message::handler);
   }
}
