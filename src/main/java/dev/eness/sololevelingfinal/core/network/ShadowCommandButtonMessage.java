package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber(bus = Bus.MOD)
public class ShadowCommandButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public ShadowCommandButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public ShadowCommandButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(ShadowCommandButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(ShadowCommandButtonMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> handleButtonAction(context.getSender(), message.buttonID, message.x, message.y, message.z));
      context.setPacketHandled(true);
   }

   public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
      if (entity != null) {
         Level world = entity.level();
         if (world.hasChunkAt(new BlockPos(x, y, z))) {
            String command = switch (buttonID) {
               case 0 -> "default";
               case 1 -> "protect";
               case 2 -> "berserk";
               case 3 -> "follow";
               case 4 -> "clear_dungeon";
               default -> "";
            };
            if (!command.isEmpty()) {
               ShadowMonarchManager.commandSummonedShadows(entity, command);
            }
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         ShadowCommandButtonMessage.class, ShadowCommandButtonMessage::buffer, ShadowCommandButtonMessage::new, ShadowCommandButtonMessage::handler
      );
   }
}
