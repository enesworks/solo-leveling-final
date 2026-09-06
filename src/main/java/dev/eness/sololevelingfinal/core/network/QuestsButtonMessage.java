package dev.eness.sololevelingfinal.core.network;

import java.util.HashMap;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.procedures.DKCPathTeleportProcedure;
import dev.eness.sololevelingfinal.core.procedures.DailyQuestGUIOpenProcedure;
import dev.eness.sololevelingfinal.core.procedures.JobChangeQuestEntryProcedure;
import dev.eness.sololevelingfinal.core.procedures.OpenPathGuiProcedure;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;
import dev.eness.sololevelingfinal.core.world.inventory.QuestsMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class QuestsButtonMessage {
   private final int buttonID;
   private final int x;
   private final int y;
   private final int z;

   public QuestsButtonMessage(FriendlyByteBuf buffer) {
      this.buttonID = buffer.readInt();
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
   }

   public QuestsButtonMessage(int buttonID, int x, int y, int z) {
      this.buttonID = buttonID;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public static void buffer(QuestsButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeInt(message.buttonID);
      buffer.writeInt(message.x);
      buffer.writeInt(message.y);
      buffer.writeInt(message.z);
   }

   public static void handler(QuestsButtonMessage message, Supplier<Context> contextSupplier) {
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
      if (entity != null) {
         Level world = entity.level();
         HashMap guistate = QuestsMenu.guistate;
         if (world.hasChunkAt(new BlockPos(x, y, z))) {
            if (buttonID == 0) {
               DailyQuestGUIOpenProcedure.execute(world, x, y, z, entity);
            }

            if (buttonID == 1) {
               if (!(entity instanceof ServerPlayer player) || !DkcQuestManager.isVisible(player)) {
                  return;
               }

               if (DkcFloorRegistry.isDkc(player.level())) {
                  OpenPathGuiProcedure.execute(world, x, y, z, player);
                  return;
               }

               if (DkcQuestManager.hasRadiruCastleAccess(player)) {
                  DKCPathTeleportProcedure.enterRadiruCastle(player);
                  player.closeContainer();
                  return;
               }

               SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables());
               if (!vars.dkc_started && vars.dkc_cleared <= 0.0) {
                  player.displayClientMessage(Component.literal("§5Claim and use the Demon King's Castle Key to break the first seal."), true);
                  return;
               }

               OpenPathGuiProcedure.execute(world, x, y, z, player);
            }

            if (buttonID == 2 && entity instanceof ServerPlayer player) {
               JobChangeQuestEntryProcedure.execute(world, player);
               player.closeContainer();
            }
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(QuestsButtonMessage.class, QuestsButtonMessage::buffer, QuestsButtonMessage::new, QuestsButtonMessage::handler);
   }
}
