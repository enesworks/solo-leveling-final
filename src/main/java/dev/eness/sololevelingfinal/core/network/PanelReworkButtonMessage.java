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
import dev.eness.sololevelingfinal.core.world.inventory.PanelReworkMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class PanelReworkButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public PanelReworkButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public PanelReworkButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(PanelReworkButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(PanelReworkButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = PanelReworkMenu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            DailyQuestGUIOpenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 1) {
            RewardScreenOpenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 2) {
            OpenStoreGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 3) {
            AbilitiesGUIopenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 4) {
            OpenTrainingGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 5) {
            CraftingGUIopenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 6) {
            StrengthIncreaseProcedure.execute(entity);
         }

         if (buttonID == 7) {
            SpeedIncreaseProcedure.execute(entity);
         }

         if (buttonID == 8) {
            SenseIncreaseProcedure.execute(entity);
         }

         if (buttonID == 9) {
            VitalityIncreaseProcedure.execute(entity);
         }

         if (buttonID == 10) {
            IntelligenceIncreaseProcedure.execute(entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         PanelReworkButtonMessage.class, PanelReworkButtonMessage::buffer, PanelReworkButtonMessage::new, PanelReworkButtonMessage::handler
      );
   }
}
