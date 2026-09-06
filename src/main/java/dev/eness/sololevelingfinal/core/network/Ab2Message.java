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
public class Ab2Message {
   int type;
   int pressedms;

   public Ab2Message(int type, int pressedms) {
      this.type = type;
      this.pressedms = pressedms;
   }

   public Ab2Message(FriendlyByteBuf buffer) {
      this.type = buffer.readInt();
      this.pressedms = buffer.readInt();
   }

   public static void buffer(Ab2Message message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.type);
      buffer.writeInt(message.pressedms);
   }

   public static void handler(Ab2Message message, Supplier<Context> contextSupplier) {
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
                  capability.PselectedPower = capability.Pslot2;
                  capability.Skillcycle = 2.0;
                  capability.syncPlayerVariables(entity);
               }
            });
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(Ab2Message.class, Ab2Message::buffer, Ab2Message::new, Ab2Message::handler);
   }
}
