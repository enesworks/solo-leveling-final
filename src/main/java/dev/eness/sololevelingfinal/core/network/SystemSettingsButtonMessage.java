package dev.eness.sololevelingfinal.core.network;

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
import dev.eness.sololevelingfinal.core.procedures.CustomHudToggleProcedure;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;

@EventBusSubscriber(bus = Bus.MOD)
public class SystemSettingsButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public SystemSettingsButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public SystemSettingsButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(SystemSettingsButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(SystemSettingsButtonMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> handleButtonAction(context.getSender(), message.buttonID, message.x, message.y, message.z));
      context.setPacketHandled(true);
   }

   public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
      if (entity != null) {
         Level world = entity.level();
         if (world.hasChunkAt(new BlockPos(x, y, z))) {
            if (buttonID == 0) {
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.combatmode = !capability.combatmode;
                  capability.syncPlayerVariables(entity);
               });
            }

            if (buttonID == 1) {
               CustomHudToggleProcedure.execute(entity);
            }

            if (buttonID == 2 && SystemPlayerAccess.hasSystem(entity)) {
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.pvpUrgentQuests = !capability.pvpUrgentQuests;
                  capability.syncPlayerVariables(entity);
               });
            }
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(
         SystemSettingsButtonMessage.class, SystemSettingsButtonMessage::buffer, SystemSettingsButtonMessage::new, SystemSettingsButtonMessage::handler
      );
   }
}
