package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.util.LiuZhigangCombatManager;

@EventBusSubscriber(bus = Bus.MOD)
public final class LiuChargeMessage {
   public static final int BEGIN = 0;
   public static final int RELEASE = 1;
   public static final int CANCEL = 2;
   private final int action;

   public LiuChargeMessage(int action) {
      this.action = action;
   }

   public LiuChargeMessage(FriendlyByteBuf buffer) {
      this.action = buffer.readByte();
   }

   public static void buffer(LiuChargeMessage message, FriendlyByteBuf buffer) {
      buffer.writeByte(message.action);
   }

   public static void handler(LiuChargeMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null && player.level().hasChunkAt(player.blockPosition())) {
            switch (message.action) {
               case 0:
                  LiuZhigangCombatManager.beginBeamCharge(player);
                  break;
               case 1:
                  LiuZhigangCombatManager.releaseBeamCharge(player);
                  break;
               case 2:
                  LiuZhigangCombatManager.cancelBeamCharge(player);
            }
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(LiuChargeMessage.class, LiuChargeMessage::buffer, LiuChargeMessage::new, LiuChargeMessage::handler);
   }
}
