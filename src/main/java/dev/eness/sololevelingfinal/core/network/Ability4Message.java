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
import dev.eness.sololevelingfinal.core.procedures.Ability4OnKeyPressedProcedure;

@EventBusSubscriber(bus = Bus.MOD)
public class Ability4Message {
   int type;
   int pressedms;

   public Ability4Message(int type, int pressedms) {
      this.type = type;
      this.pressedms = pressedms;
   }

   public Ability4Message(FriendlyByteBuf buffer) {
      this.type = buffer.readInt();
      this.pressedms = buffer.readInt();
   }

   public static void buffer(Ability4Message message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.type);
      buffer.writeInt(message.pressedms);
   }

   public static void handler(Ability4Message message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> pressAction(context.getSender(), message.type, message.pressedms));
      context.setPacketHandled(true);
   }

   public static void pressAction(Player entity, int type, int pressedms) {
      Level world = entity.level();
      double x = entity.getX();
      double y = entity.getY();
      double z = entity.getZ();
      if (world.hasChunkAt(entity.blockPosition())) {
         if (type == 0) {
            Ability4OnKeyPressedProcedure.execute(world, x, y, z, entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(Ability4Message.class, Ability4Message::buffer, Ability4Message::new, Ability4Message::handler);
   }
}
