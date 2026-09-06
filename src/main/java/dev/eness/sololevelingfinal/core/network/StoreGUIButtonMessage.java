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
import dev.eness.sololevelingfinal.core.procedures.OpenFoodGuiProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenMistItemsProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenShopProcedure;
import dev.eness.sololevelingfinal.core.procedures.StopPotionGUIopenProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.StoreGUIMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class StoreGUIButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public StoreGUIButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public StoreGUIButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(StoreGUIButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(StoreGUIButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = StoreGUIMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            OpenShopProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 1) {
            OpenFoodGuiProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 2) {
            StopPotionGUIopenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 3) {
            OpenMistItemsProcedure.execute(world, x, y, z, entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(StoreGUIButtonMessage.class, StoreGUIButtonMessage::buffer, StoreGUIButtonMessage::new, StoreGUIButtonMessage::handler);
   }
}
