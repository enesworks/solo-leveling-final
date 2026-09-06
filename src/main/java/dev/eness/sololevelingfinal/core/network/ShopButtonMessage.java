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
import dev.eness.sololevelingfinal.core.procedures.BuyingProcedure;
import dev.eness.sololevelingfinal.core.procedures.BuyingS2Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyingS3Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyingS4Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyingS5Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyingS6Procedure;
import dev.eness.sololevelingfinal.core.procedures.OpenStoreGUIProcedure;
import dev.eness.sololevelingfinal.core.procedures.ShopRefreshButtonProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.ShopMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class ShopButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public ShopButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public ShopButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(ShopButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(ShopButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = ShopMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            BuyingProcedure.execute(entity);
         }

         if (buttonID == 1) {
            BuyingS2Procedure.execute(entity);
         }

         if (buttonID == 2) {
            BuyingS3Procedure.execute(entity);
         }

         if (buttonID == 3) {
            BuyingS4Procedure.execute(entity);
         }

         if (buttonID == 4) {
            BuyingS5Procedure.execute(entity);
         }

         if (buttonID == 5) {
            BuyingS6Procedure.execute(entity);
         }

         if (buttonID == 6) {
            OpenStoreGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 7) {
            ShopRefreshButtonProcedure.execute(entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(ShopButtonMessage.class, ShopButtonMessage::buffer, ShopButtonMessage::new, ShopButtonMessage::handler);
   }
}
