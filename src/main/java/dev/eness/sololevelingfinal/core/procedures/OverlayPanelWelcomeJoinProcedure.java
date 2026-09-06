package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

@EventBusSubscriber
public class OverlayPanelWelcomeJoinProcedure {
   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         boolean[] showWelcome = new boolean[]{false};
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            showWelcome[0] = capability.Player;
            capability.overlay_alpha_welcome = 0.0;
            capability.syncPlayerVariables(entity);
         });
         if (showWelcome[0] && entity instanceof ServerPlayer player) {
            SystemNotifications.showTitleUnder(
               player,
               -12597505,
               100,
               Component.literal("WELCOME PLAYER").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
               Component.literal("SYSTEM NOTICE").withStyle(ChatFormatting.GRAY)
            );
         }
      }
   }
}
