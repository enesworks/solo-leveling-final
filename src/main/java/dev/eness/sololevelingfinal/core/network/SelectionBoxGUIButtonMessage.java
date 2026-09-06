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
import dev.eness.sololevelingfinal.core.procedures.SelectionSlot1Procedure;
import dev.eness.sololevelingfinal.core.procedures.SelectionSlot2Procedure;
import dev.eness.sololevelingfinal.core.procedures.SelectionSlot3Procedure;
import dev.eness.sololevelingfinal.core.world.inventory.SelectionBoxGUIMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class SelectionBoxGUIButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public SelectionBoxGUIButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public SelectionBoxGUIButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(SelectionBoxGUIButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(SelectionBoxGUIButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = SelectionBoxGUIMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            SelectionSlot1Procedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 1) {
            SelectionSlot2Procedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 2) {
            SelectionSlot3Procedure.execute(world, x, y, z, entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         SelectionBoxGUIButtonMessage.class, SelectionBoxGUIButtonMessage::buffer, SelectionBoxGUIButtonMessage::new, SelectionBoxGUIButtonMessage::handler
      );
   }
}
