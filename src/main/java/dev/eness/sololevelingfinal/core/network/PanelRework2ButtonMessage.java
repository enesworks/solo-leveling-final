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
import dev.eness.sololevelingfinal.core.procedures.IntelligenceIncreaseProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenMainQuestsGUIProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenStoreGUIProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenTrainingGUIProcedure;
import dev.eness.sololevelingfinal.core.procedures.RewardScreenOpenProcedure;
import dev.eness.sololevelingfinal.core.procedures.SenseIncreaseProcedure;
import dev.eness.sololevelingfinal.core.procedures.SpeedIncreaseProcedure;
import dev.eness.sololevelingfinal.core.procedures.StrengthIncreaseProcedure;
import dev.eness.sololevelingfinal.core.procedures.VitalityIncreaseProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.PanelRework2Menu;

@EventBusSubscriber(bus = Bus.MOD)
public class PanelRework2ButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public PanelRework2ButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public PanelRework2ButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(PanelRework2ButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(PanelRework2ButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = PanelRework2Menu.guistate;
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
            OpenStoreGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 6) {
            OpenMainQuestsGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 7) {
            RewardScreenOpenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 8) {
            CraftingGUIopenProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 9) {
            OpenTrainingGUIProcedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 10) {
            AbilitiesGUIopenProcedure.execute(world, x, y, z, entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         PanelRework2ButtonMessage.class, PanelRework2ButtonMessage::buffer, PanelRework2ButtonMessage::new, PanelRework2ButtonMessage::handler
      );
   }
}
