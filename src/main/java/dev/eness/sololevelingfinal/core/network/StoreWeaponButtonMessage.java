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
import dev.eness.sololevelingfinal.core.procedures.BuyWeaponAProcedure;
import dev.eness.sololevelingfinal.core.procedures.BuyWeaponBProcedure;
import dev.eness.sololevelingfinal.core.procedures.BuyWeaponCProcedure;
import dev.eness.sololevelingfinal.core.procedures.BuyWeaponDProcedure;
import dev.eness.sololevelingfinal.core.procedures.BuyWeaponEProcedure;
import dev.eness.sololevelingfinal.core.procedures.BuyWeaponSProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.StoreWeaponMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class StoreWeaponButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public StoreWeaponButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public StoreWeaponButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(StoreWeaponButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(StoreWeaponButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = StoreWeaponMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            BuyWeaponAProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 1) {
            BuyWeaponEProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 2) {
            BuyWeaponDProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 3) {
            BuyWeaponCProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 4) {
            BuyWeaponBProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 5) {
            BuyWeaponSProcedure.execute(world, x, y, z, entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         StoreWeaponButtonMessage.class, StoreWeaponButtonMessage::buffer, StoreWeaponButtonMessage::new, StoreWeaponButtonMessage::handler
      );
   }
}
