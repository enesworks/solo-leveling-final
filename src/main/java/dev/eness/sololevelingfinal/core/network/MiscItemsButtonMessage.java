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
import dev.eness.sololevelingfinal.core.procedures.MiscItemRandomboxProcedure;
import dev.eness.sololevelingfinal.core.procedures.MiscItemSelectionProcedure;
import dev.eness.sololevelingfinal.core.procedures.MiscItemSelectorProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.MiscItemsMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class MiscItemsButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public MiscItemsButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public MiscItemsButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(MiscItemsButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(MiscItemsButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = MiscItemsMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            MiscItemSelectorProcedure.execute(entity);
         }

         if (buttonID == 1) {
            MiscItemRandomboxProcedure.execute(entity);
         }

         if (buttonID == 2) {
            MiscItemSelectionProcedure.execute(entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         MiscItemsButtonMessage.class, MiscItemsButtonMessage::buffer, MiscItemsButtonMessage::new, MiscItemsButtonMessage::handler
      );
   }
}
