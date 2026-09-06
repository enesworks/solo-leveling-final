package dev.eness.sololevelingfinal.core.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.util.HunterEvaluationManager;
import dev.eness.sololevelingfinal.core.util.HunterEvaluationRules;

@EventBusSubscriber(bus = Bus.MOD)
public final class HunterEvaluationActionMessage {
   private final UUID sessionId;
   private final int actionId;

   public HunterEvaluationActionMessage(UUID sessionId, HunterEvaluationRules.Action action) {
      this.sessionId = sessionId == null ? new UUID(0L, 0L) : sessionId;
      this.actionId = action == null ? -1 : action.ordinal();
   }

   public HunterEvaluationActionMessage(FriendlyByteBuf buffer) {
      this.sessionId = buffer.readUUID();
      this.actionId = buffer.readVarInt();
   }

   public static void buffer(HunterEvaluationActionMessage message, FriendlyByteBuf buffer) {
      buffer.writeUUID(message.sessionId);
      buffer.writeVarInt(message.actionId);
   }

   public static void handler(HunterEvaluationActionMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         HunterEvaluationRules.Action action = HunterEvaluationRules.Action.fromId(message.actionId);
         if (player != null && action != null) {
            HunterEvaluationManager.handleAction(player, message.sessionId, action);
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         HunterEvaluationActionMessage.class,
         HunterEvaluationActionMessage::buffer,
         HunterEvaluationActionMessage::new,
         HunterEvaluationActionMessage::handler,
         NetworkDirection.PLAY_TO_SERVER
      );
   }
}
