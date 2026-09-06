package dev.eness.sololevelingfinal.core.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.ShowNotificationMessage;

public final class SystemNotifications {
   public static final int ACCENT = -12597505;

   private SystemNotifications() {
   }

   public static void showTitle(ServerPlayer player, int accentColor, int durationTicks, Component title) {
      send(player, new ShowNotificationMessage(accentColor, durationTicks, title, null));
   }

   public static void showUnder(ServerPlayer player, int accentColor, int durationTicks, Component undertext) {
      send(player, new ShowNotificationMessage(accentColor, durationTicks, null, undertext));
   }

   public static void showTitleUnder(ServerPlayer player, int accentColor, int durationTicks, Component title, Component undertext) {
      send(player, new ShowNotificationMessage(accentColor, durationTicks, title, undertext));
   }

   public static void showNegativeTitleUnder(ServerPlayer player, int accentColor, int durationTicks, Component title, Component undertext) {
      send(player, new ShowNotificationMessage(accentColor, durationTicks, title, undertext, true));
   }

   private static void send(ServerPlayer player, ShowNotificationMessage message) {
      if (player != null && SystemPlayerAccess.hasSystem(player)) {
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message);
      }
   }
}
