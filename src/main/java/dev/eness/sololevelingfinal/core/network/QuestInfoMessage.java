package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dkc.DkcQuestProgressTracker;
import dev.eness.sololevelingfinal.core.procedures.QuestInfoOnKeyPressedProcedure;
import dev.eness.sololevelingfinal.core.procedures.QuestInfoOnKeyReleasedProcedure;

@EventBusSubscriber(bus = Bus.MOD)
public class QuestInfoMessage {
   int type;
   int pressedms;

   public QuestInfoMessage(int type, int pressedms) {
      this.type = type;
      this.pressedms = pressedms;
   }

   public QuestInfoMessage(FriendlyByteBuf buffer) {
      this.type = buffer.readInt();
      this.pressedms = buffer.readInt();
   }

   public static void buffer(QuestInfoMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.type);
      buffer.writeInt(message.pressedms);
   }

   public static void handler(QuestInfoMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> pressAction(context.getSender(), message.type, message.pressedms));
      context.setPacketHandled(true);
   }

   public static void pressAction(Player entity, int type, int pressedms) {
      if (entity != null && (type == 0 || type == 1)) {
         if (!(type == 0 && entity instanceof ServerPlayer player) || DkcQuestProgressTracker.acceptPress(player)) {
            Level world = entity.level();
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();
            if (world.hasChunkAt(entity.blockPosition())) {
               if (type == 0) {
                  QuestInfoOnKeyPressedProcedure.execute(entity);
                  if (entity instanceof ServerPlayer player) {
                     DkcQuestProgressTracker.beginTracking(player);
                  }
               }

               if (type == 1) {
                  QuestInfoOnKeyReleasedProcedure.execute(entity);
                  if (entity instanceof ServerPlayer player) {
                     DkcQuestProgressTracker.stopTracking(player);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         QuestInfoMessage.class, QuestInfoMessage::buffer, QuestInfoMessage::new, QuestInfoMessage::handler, NetworkDirection.PLAY_TO_SERVER
      );
   }
}
