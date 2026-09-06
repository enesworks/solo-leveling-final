package dev.eness.sololevelingfinal.core.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.gui.system.HunterEvaluationScreen;

@EventBusSubscriber(bus = Bus.MOD)
public final class HunterEvaluationStateMessage {
   private final boolean open;
   private final boolean forceOpen;
   private final UUID sessionId;
   private final int mode;
   private final int phase;
   private final int classId;
   private final int rank;
   private final int previousRank;
   private final int phaseDurationTicks;
   private final int remainingTicks;
   private final boolean canReroll;
   private final boolean fixedClass;

   public HunterEvaluationStateMessage(
      boolean open,
      boolean forceOpen,
      UUID sessionId,
      int mode,
      int phase,
      int classId,
      int rank,
      int previousRank,
      int phaseDurationTicks,
      int remainingTicks,
      boolean canReroll,
      boolean fixedClass
   ) {
      this.open = open;
      this.forceOpen = forceOpen;
      this.sessionId = sessionId == null ? new UUID(0L, 0L) : sessionId;
      this.mode = mode;
      this.phase = phase;
      this.classId = classId;
      this.rank = rank;
      this.previousRank = previousRank;
      this.phaseDurationTicks = Math.max(0, phaseDurationTicks);
      this.remainingTicks = Math.max(0, remainingTicks);
      this.canReroll = canReroll;
      this.fixedClass = fixedClass;
   }

   public HunterEvaluationStateMessage(FriendlyByteBuf buffer) {
      this(
         buffer.readBoolean(),
         buffer.readBoolean(),
         buffer.readUUID(),
         buffer.readVarInt(),
         buffer.readVarInt(),
         buffer.readVarInt(),
         buffer.readVarInt(),
         buffer.readVarInt(),
         buffer.readVarInt(),
         buffer.readVarInt(),
         buffer.readBoolean(),
         buffer.readBoolean()
      );
   }

   public static void buffer(HunterEvaluationStateMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.open);
      buffer.writeBoolean(message.forceOpen);
      buffer.writeUUID(message.sessionId);
      buffer.writeVarInt(message.mode);
      buffer.writeVarInt(message.phase);
      buffer.writeVarInt(message.classId);
      buffer.writeVarInt(message.rank);
      buffer.writeVarInt(message.previousRank);
      buffer.writeVarInt(message.phaseDurationTicks);
      buffer.writeVarInt(message.remainingTicks);
      buffer.writeBoolean(message.canReroll);
      buffer.writeBoolean(message.fixedClass);
   }

   public static void handler(HunterEvaluationStateMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(
         () -> DistExecutor.unsafeRunWhenOn(
            Dist.CLIENT,
            () -> () -> HunterEvaluationScreen.handleServerState(
               message.open,
               message.forceOpen,
               message.sessionId,
               message.mode,
               message.phase,
               message.classId,
               message.rank,
               message.previousRank,
               message.phaseDurationTicks,
               message.remainingTicks,
               message.canReroll,
               message.fixedClass
            )
         )
      );
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         HunterEvaluationStateMessage.class,
         HunterEvaluationStateMessage::buffer,
         HunterEvaluationStateMessage::new,
         HunterEvaluationStateMessage::handler,
         NetworkDirection.PLAY_TO_CLIENT
      );
   }
}
