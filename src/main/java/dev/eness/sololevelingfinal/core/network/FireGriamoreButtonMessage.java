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
import dev.eness.sololevelingfinal.core.procedures.FireVar1Procedure;
import dev.eness.sololevelingfinal.core.procedures.FireVar2Procedure;
import dev.eness.sololevelingfinal.core.procedures.FireVar3Procedure;
import dev.eness.sololevelingfinal.core.world.inventory.FireGriamoreMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class FireGriamoreButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public FireGriamoreButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public FireGriamoreButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(FireGriamoreButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(FireGriamoreButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = FireGriamoreMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            FireVar1Procedure.execute(entity);
         }

         if (buttonID == 1) {
            FireVar2Procedure.execute(entity);
         }

         if (buttonID == 2) {
            FireVar3Procedure.execute(entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         FireGriamoreButtonMessage.class, FireGriamoreButtonMessage::buffer, FireGriamoreButtonMessage::new, FireGriamoreButtonMessage::handler
      );
   }
}
