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
import dev.eness.sololevelingfinal.core.procedures.AbilityAppendButtonProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenAbilitylist5Procedure;
import dev.eness.sololevelingfinal.core.procedures.OpenAbilitylist7Procedure;
import dev.eness.sololevelingfinal.core.world.inventory.UnlockedSkillsTab6Menu;

@EventBusSubscriber(bus = Bus.MOD)
public class UnlockedSkillsTab6ButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public UnlockedSkillsTab6ButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public UnlockedSkillsTab6ButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(UnlockedSkillsTab6ButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(UnlockedSkillsTab6ButtonMessage message, Supplier<Context> contextSupplier) {
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
      HashMap guistate = UnlockedSkillsTab6Menu.guistate;
      if (world.hasChunkAt(new BlockPos(x, y, z))) {
         if (buttonID == 0) {
            AbilityAppendButtonProcedure.execute(world, x, y, z, entity, 41, true);
         }

         if (buttonID == 1) {
            AbilityAppendButtonProcedure.execute(world, x, y, z, entity, 42, true);
         }

         if (buttonID == 2) {
            AbilityAppendButtonProcedure.execute(world, x, y, z, entity, 43, true);
         }

         if (buttonID == 3) {
            AbilityAppendButtonProcedure.execute(world, x, y, z, entity, 44, true);
         }

         if (buttonID == 4) {
            AbilityAppendButtonProcedure.execute(world, x, y, z, entity, 45, true);
         }

         if (buttonID == 5) {
            AbilityAppendButtonProcedure.execute(world, x, y, z, entity, 46, true);
         }

         if (buttonID == 6) {
            AbilityAppendButtonProcedure.execute(world, x, y, z, entity, 47, true);
         }

         if (buttonID == 7) {
            AbilityAppendButtonProcedure.execute(world, x, y, z, entity, 48, true);
         }

         if (buttonID == 8) {
            OpenAbilitylist5Procedure.execute(world, x, y, z, entity);
         }

         if (buttonID == 9) {
            OpenAbilitylist7Procedure.execute(world, x, y, z, entity);
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         UnlockedSkillsTab6ButtonMessage.class,
         UnlockedSkillsTab6ButtonMessage::buffer,
         UnlockedSkillsTab6ButtonMessage::new,
         UnlockedSkillsTab6ButtonMessage::handler
      );
   }
}
