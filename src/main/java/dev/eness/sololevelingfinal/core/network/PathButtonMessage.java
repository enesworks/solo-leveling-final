package dev.eness.sololevelingfinal.core.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.procedures.DKCPathTeleportProcedure;
import dev.eness.sololevelingfinal.core.world.inventory.PathMenu;

@EventBusSubscriber(bus = Bus.MOD)
public class PathButtonMessage {
   private final PathButtonMessage.Action action;
   private final int floor;

   public PathButtonMessage(FriendlyByteBuf buffer) {
      this.action = buffer.readEnum(PathButtonMessage.Action.class);
      this.floor = buffer.readVarInt();
   }

   public PathButtonMessage(int buttonID, int x, int y, int z) {
      this(PathButtonMessage.Action.ENTER_FLOOR, buttonID + 1);
   }

   private PathButtonMessage(PathButtonMessage.Action action, int floor) {
      this.action = action;
      this.floor = floor;
   }

   public static PathButtonMessage enterFloor(int floor) {
      return new PathButtonMessage(PathButtonMessage.Action.ENTER_FLOOR, floor);
   }

   public static PathButtonMessage enterFloor(int floor, int x, int y, int z) {
      return enterFloor(floor);
   }

   public static PathButtonMessage exitCastle() {
      return new PathButtonMessage(PathButtonMessage.Action.EXIT_CASTLE, 0);
   }

   public static PathButtonMessage exitCastle(int x, int y, int z) {
      return exitCastle();
   }

   public static void buffer(PathButtonMessage message, FriendlyByteBuf buffer) {
      buffer.writeEnum(message.action);
      buffer.writeVarInt(message.floor);
   }

   public static void handler(PathButtonMessage message, Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> handleButtonAction(context.getSender(), message));
      context.setPacketHandled(true);
   }

   public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
      handleButtonAction(entity, enterFloor(buttonID + 1));
   }

   private static void handleButtonAction(Player entity, PathButtonMessage message) {
      if (entity instanceof ServerPlayer player && player.containerMenu instanceof PathMenu) {
         if (message.action == PathButtonMessage.Action.EXIT_CASTLE) {
            if (DkcFloorRegistry.isDkc(player.level())) {
               player.closeContainer();
               DKCPathTeleportProcedure.returnToSavedOverworld(player);
            }
         } else if (message.action == PathButtonMessage.Action.ENTER_FLOOR && message.floor >= 1 && message.floor <= 20) {
            if (DKCPathTeleportProcedure.canTravelToFloor(player, message.floor, true)) {
               player.closeContainer();
               DKCPathTeleportProcedure.execute(player, message.floor);
            }
         }
      }
   }

   @SubscribeEvent
   public static void registerMessage(FMLCommonSetupEvent event) {
      SololevelingMod.addNetworkMessage(PathButtonMessage.class, PathButtonMessage::buffer, PathButtonMessage::new, PathButtonMessage::handler);
   }

   public enum Action {
      ENTER_FLOOR,
      EXIT_CASTLE;
   }
}
