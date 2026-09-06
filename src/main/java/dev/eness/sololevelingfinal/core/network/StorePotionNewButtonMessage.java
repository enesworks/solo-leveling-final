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
import dev.eness.sololevelingfinal.core.procedures.BuyPotionFTG1Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionFTG2Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionFTG3Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionHP1Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionHP2Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionHP3Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionMP1Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionMP2Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionMP3Procedure;
import dev.eness.sololevelingfinal.core.procedures.OpenStoreGUIProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.StorePotionNewMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class StorePotionNewButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public StorePotionNewButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public StorePotionNewButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(StorePotionNewButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(StorePotionNewButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = StorePotionNewMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            OpenStoreGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 1) {
            BuyPotionHP1Procedure.execute(entity);
         }

         if (buttonID == 2) {
            BuyPotionHP2Procedure.execute(entity);
         }

         if (buttonID == 3) {
            BuyPotionHP3Procedure.execute(entity);
         }

         if (buttonID == 4) {
            BuyPotionMP1Procedure.execute(entity);
         }

         if (buttonID == 5) {
            BuyPotionMP2Procedure.execute(entity);
         }

         if (buttonID == 6) {
            BuyPotionMP3Procedure.execute(entity);
         }

         if (buttonID == 7) {
            BuyPotionFTG1Procedure.execute(entity);
         }

         if (buttonID == 8) {
            BuyPotionFTG2Procedure.execute(entity);
         }

         if (buttonID == 9) {
            BuyPotionFTG3Procedure.execute(entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         StorePotionNewButtonMessage.class, StorePotionNewButtonMessage::buffer, StorePotionNewButtonMessage::new, StorePotionNewButtonMessage::handler
      );
   }
}
