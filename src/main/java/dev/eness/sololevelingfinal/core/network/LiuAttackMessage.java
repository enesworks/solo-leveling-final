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
public final class LiuAttackMessage {
   private final boolean offhand;
   private final int comboIndex;

   public LiuAttackMessage(boolean offhand, int comboIndex) {
      this.offhand = offhand;
      this.comboIndex = comboIndex;
   }

   public LiuAttackMessage(FriendlyByteBuf buffer) {
      this.offhand = buffer.readBoolean();
      this.comboIndex = buffer.readVarInt();
   }

   public static void buffer(LiuAttackMessage message, FriendlyByteBuf buffer) {
      buffer.writeBoolean(message.offhand);
      buffer.writeVarInt(message.comboIndex);
   }

   public static void handler(LiuAttackMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null && player.level().hasChunkAt(player.blockPosition())) {
            LiuZhigangCombatManager.enhancedAttack(player, message.offhand, Math.max(0, Math.min(31, message.comboIndex)));
         }
      });
      context.setPacketHandled(true);
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(LiuAttackMessage.class, LiuAttackMessage::buffer, LiuAttackMessage::new, LiuAttackMessage::handler);
   }
}
