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
import dev.eness.sololevelingfinal.core.procedures.AbilitiesGUIopenProcedure;
import dev.eness.sololevelingfinal.core.procedures.CraftingGUIopenProcedure;
import dev.eness.sololevelingfinal.core.procedures.DailyQuestGUIOpenProcedure;
import dev.eness.sololevelingfinal.core.procedures.IntelligenceIncreaseProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenStoreGUIProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenTrainingGUIProcedure;
import dev.eness.sololevelingfinal.core.procedures.RewardScreenOpenProcedure;
import dev.eness.sololevelingfinal.core.procedures.SenseIncreaseProcedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedIncreaseProcedure;
import dev.eness.sololevelingfinal.core.procedures.StrengthIncreaseProcedure;
import dev.eness.sololevelingfinal.core.procedures.VitalityIncreaseProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.PanelEarlyMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class PanelEarlyButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public PanelEarlyButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public PanelEarlyButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(PanelEarlyButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(PanelEarlyButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = PanelEarlyMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            StrengthIncreaseProcedure.execute(entity);
         }

         if (buttonID == 1) {
            SpeedIncreaseProcedure.execute(entity);
         }

         if (buttonID == 2) {
            SenseIncreaseProcedure.execute(entity);
         }

         if (buttonID == 3) {
            VitalityIncreaseProcedure.execute(entity);
         }

         if (buttonID == 4) {
            IntelligenceIncreaseProcedure.execute(entity);
         }

         if (buttonID == 5) {
            RewardScreenOpenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 6) {
            OpenTrainingGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 7) {
            OpenStoreGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 8) {
            DailyQuestGUIOpenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 9) {
            AbilitiesGUIopenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 10) {
            CraftingGUIopenProcedure.execute(world, x, y, z, entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         PanelEarlyButtonMessage.class, PanelEarlyButtonMessage::buffer, PanelEarlyButtonMessage::new, PanelEarlyButtonMessage::handler
      );
   }
}
