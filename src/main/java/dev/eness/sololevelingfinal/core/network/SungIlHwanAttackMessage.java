package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.util.SungIlHwanCombatManager;

@EventBusSubscriber(bus = Bus.MOD)
public final class SungIlHwanAttackMessage {
   private static final byte ATTACK_REQUEST = 0;
   private static final byte STANCE_SYNC = 1;
   private static volatile boolean clientStanceActive;
   private final byte mode;
   private final boolean stanceActive;

   public SungIlHwanAttackMessage() {
      this((byte)0, false);
   }

   private SungIlHwanAttackMessage(byte mode, boolean stanceActive) {
      this.mode = mode;
      this.stanceActive = stanceActive;
   }

   public SungIlHwanAttackMessage(FriendlyByteBuf buffer) {
      this.mode = buffer.readByte();
      this.stanceActive = buffer.readBoolean();
   }

   public static void buffer(SungIlHwanAttackMessage message, FriendlyByteBuf buffer) {
      buffer.writeByte(message.mode);
      buffer.writeBoolean(message.stanceActive);
   }

   public static void handler(SungIlHwanAttackMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer sender = context.getSender();
         if (sender != null) {
            if (message.mode == 0) {
               SungIlHwanCombatManager.performAssassinLineCut(sender);
            }
         } else {
            if (message.mode == 1) {
               clientStanceActive = message.stanceActive;
            }
         }
      });
      context.setPacketHandled(true);
   }

   public static boolean isClientStanceActive() {
      return clientStanceActive;
   }

   public static void syncStance(ServerPlayer player, boolean active) {
      if (player != null && player.connection != null) {
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SungIlHwanAttackMessage((byte)1, active));
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         SungIlHwanAttackMessage.class, SungIlHwanAttackMessage::buffer, SungIlHwanAttackMessage::new, SungIlHwanAttackMessage::handler
      );
   }
}
