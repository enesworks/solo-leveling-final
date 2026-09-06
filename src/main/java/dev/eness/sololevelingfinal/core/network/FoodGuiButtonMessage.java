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
import dev.eness.sololevelingfinal.core.procedures.Food1BoughtProcedure;
import dev.eness.sololevelingfinal.core.procedures.Food2BoughtProcedure;
import dev.eness.sololevelingfinal.core.procedures.Food3BoughtProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenStoreGUIProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.FoodGuiMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class FoodGuiButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public FoodGuiButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public FoodGuiButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(FoodGuiButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(FoodGuiButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = FoodGuiMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            Food1BoughtProcedure.execute(entity);
         }

         if (buttonID == 1) {
            Food2BoughtProcedure.execute(entity);
         }

         if (buttonID == 2) {
            Food3BoughtProcedure.execute(entity);
         }

         if (buttonID == 3) {
            OpenStoreGUIProcedure.execute(world, x, y, z, entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(FoodGuiButtonMessage.class, FoodGuiButtonMessage::buffer, FoodGuiButtonMessage::new, FoodGuiButtonMessage::handler);
   }
}
