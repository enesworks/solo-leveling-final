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
import dev.eness.sololevelingfinal.core.world.inventory.ShopMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class ShopSlotMessage {
   private final int slotID;
   private final int x;
   private final int y;
   private final int z;
   private final int changeType;
   private final int meta;

   public ShopSlotMessage(int slotID, int x, int y, int z, int changeType, int meta) {
      this.slotID = slotID;
      this.x = x;
      this.y = y;
      this.z = z;
      this.changeType = changeType;
      this.meta = meta;
   }

   public ShopSlotMessage(FriendlyByteBuf buffer) {
      this.slotID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
      this.changeType = buffer.readInt();
      this.meta = buffer.readInt();
   }

   public static void buffer(ShopSlotMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.slotID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
      buffer.writeInt(message.changeType);
      buffer.writeInt(message.meta);
   }

   public static void handler(ShopSlotMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> {
         Player entity = context.getSender();
         int slotID = message.slotID;
         int changeType = message.changeType;
         int meta = message.meta;
         int x = message.x;
         int y = message.y;
         int z = message.z;
         handleSlotAction(entity, slotID, changeType, meta, x, y, z);
      });
      context.setPacketHandled(true);
   }

   public static void handleSlotAction(Player entity, int slot, int changeType, int meta, int x, int y, int z) {
      Level world = entity.level();
      HashMap guistate = ShopMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (slot == 0 && changeType == 1) {
            BuyingProcedure.execute(entity);
         }

         if (slot == 1 && changeType == 1) {
            BuyingProcedure.execute(entity);
         }

         if (slot == 2 && changeType == 1) {
            BuyingProcedure.execute(entity);
         }

         if (slot == 3 && changeType == 1) {
            BuyingProcedure.execute(entity);
         }

         if (slot == 4 && changeType == 1) {
            BuyingProcedure.execute(entity);
         }

         if (slot == 5 && changeType == 1) {
            BuyingProcedure.execute(entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(ShopSlotMessage.class, ShopSlotMessage::buffer, ShopSlotMessage::new, ShopSlotMessage::handler);
   }
}
