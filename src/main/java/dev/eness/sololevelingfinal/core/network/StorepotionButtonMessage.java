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
import dev.eness.sololevelingfinal.core.procedures.BuyPotionFatigue2Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionFatigue3Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionFatigueProcedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionHealth2Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionHealth3Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionHealthProcedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionMana2Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionMana3Procedure;
import dev.eness.sololevelingfinal.core.procedures.BuyPotionManaProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.StorepotionMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class StorepotionButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public StorepotionButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public StorepotionButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(StorepotionButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(StorepotionButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = StorepotionMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            BuyPotionManaProcedure.execute(entity);
         }

         if (buttonID == 1) {
            BuyPotionHealthProcedure.execute(entity);
         }

         if (buttonID == 2) {
            BuyPotionFatigueProcedure.execute(entity);
         }

         if (buttonID == 3) {
            BuyPotionMana2Procedure.execute(entity);
         }

         if (buttonID == 4) {
            BuyPotionMana3Procedure.execute(entity);
         }

         if (buttonID == 5) {
            BuyPotionHealth2Procedure.execute(entity);
         }

         if (buttonID == 6) {
            BuyPotionFatigue2Procedure.execute(entity);
         }

         if (buttonID == 7) {
            BuyPotionHealth3Procedure.execute(entity);
         }

         if (buttonID == 8) {
            BuyPotionFatigue3Procedure.execute(entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         StorepotionButtonMessage.class, StorepotionButtonMessage::buffer, StorepotionButtonMessage::new, StorepotionButtonMessage::handler
      );
   }
}
