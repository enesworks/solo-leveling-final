package dev.eness.sololevelingfinal.core.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.client.highlight.ClientEntityHighlightManager;

@EventBusSubscriber(bus = Bus.MOD)
public final class EntityHighlightMessage {
   public static final byte SET = 0;
   public static final byte REMOVE = 1;
   public static final byte CLEAR_SOURCE = 2;
   public static final byte CLEAR_ALL = 3;
   public static final int MAX_SOURCE_LENGTH = 64;
   public static final UUID NO_TARGET = new UUID(0L, 0L);
   private final byte action;
   private final UUID targetId;
   private final ResourceLocation dimension;
   private final String source;
   private final int color;
   private final int durationTicks;
   private final int priority;

   public EntityHighlightMessage(byte action, UUID targetId, ResourceLocation dimension, String source, int color, int durationTicks, int priority) {
      this.action = action;
      this.targetId = targetId == null ? NO_TARGET : targetId;
      this.dimension = dimension == null ? new ResourceLocation("minecraft", "overworld") : dimension;
      String safeSource = source == null ? "" : source;
      this.source = safeSource.length() <= 64 ? safeSource : safeSource.substring(0, 64);
      this.color = color;
      this.durationTicks = durationTicks;
      this.priority = priority;
   }

   public EntityHighlightMessage(FriendlyByteBuf buffer) {
      this(buffer.readByte(), buffer.readUUID(), buffer.readResourceLocation(), buffer.readUtf(64), buffer.readInt(), buffer.readVarInt(), buffer.readVarInt());
   }

   public static void buffer(EntityHighlightMessage message, FriendlyByteBuf buffer) {
      buffer.writeByte(message.action);
      buffer.writeUUID(message.targetId);
      buffer.writeResourceLocation(message.dimension);
      buffer.writeUtf(message.source, 64);
      buffer.writeInt(message.color);
      buffer.writeVarInt(message.durationTicks);
      buffer.writeVarInt(message.priority);
   }

   public static void handler(EntityHighlightMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      if (context.getDirection().getReceptionSide().isClient()) {
         context.enqueueWork(
            () -> DistExecutor.unsafeRunWhenOn(
               Dist.CLIENT,
               () -> () -> ClientEntityHighlightManager.handle(
                  message.action, message.targetId, message.dimension, message.source, message.color, message.durationTicks, message.priority
               )
            )
         );
      }

      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         EntityHighlightMessage.class,
         EntityHighlightMessage::buffer,
         EntityHighlightMessage::new,
         EntityHighlightMessage::handler,
         NetworkDirection.PLAY_TO_CLIENT
      );
   }
}
