package dev.eness.sololevelingfinal.core.network;

import java.util.HashMap;
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
import dev.eness.sololevelingfinal.core.util.ShadowExchangeManager;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeSaveMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class ShadowExchangeSaveButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public ShadowExchangeSaveButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public ShadowExchangeSaveButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(ShadowExchangeSaveButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(ShadowExchangeSaveButtonMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         Player entity = context.getSender();
         int buttonID = message.buttonID;
         int x = message.x;
         int y = message.y;
         int z = message.z;
         handleButtonAction(entity, buttonID, x, y, z);
      });
      context.setPacketHandled(true);
   }

   public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
      Level world = entity.level();
      HashMap guistate = ShadowExchangeSaveMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         String type = switch (buttonID) {
            case 0 -> "knight";
            case 1 -> "goblin_club";
            case 2 -> "goblin_archer";
            case 3 -> "goblin_mage";
            case 4 -> "wolf";
            case 5 -> "polar_bear";
            default -> "";
         };

         String name = switch (buttonID) {
            case 0 -> "Knight Anchor";
            case 1 -> "Goblin Fighter Anchor";
            case 2 -> "Goblin Archer Anchor";
            case 3 -> "Goblin Mage Anchor";
            case 4 -> "Lycan Anchor";
            case 5 -> "Polar Bear Anchor";
            default -> "Shadow Anchor";
         };
         if (!type.isEmpty()) {
            ShadowExchangeManager.saveAnchor(world, entity, type, name);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         ShadowExchangeSaveButtonMessage.class,
         ShadowExchangeSaveButtonMessage::buffer,
         ShadowExchangeSaveButtonMessage::new,
         ShadowExchangeSaveButtonMessage::handler
      );
   }
}
