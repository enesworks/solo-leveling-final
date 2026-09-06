package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class CreativeResetProcedure {
   private static final double CREATIVE_MANA = 1000000.0;

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player);
      }
   }

   public static void execute(Entity entity) {
      execute(null, entity);
   }

   private static void execute(@Nullable Event event, Entity entity) {
      if (entity != null) {
         if (isCreative(entity)) {
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               if (capability.Fatigue != 0.0 || capability.Mana != 1000000.0 || capability.MP != 1000000.0) {
                  capability.Fatigue = 0.0;
                  capability.Mana = 1000000.0;
                  capability.MP = 1000000.0;
                  capability.syncPlayerVariables(entity);
               }
            });
         }
      }
   }

   private static boolean isCreative(Entity entity) {
      if (entity instanceof ServerPlayer serverPlayer) {
         return serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
      } else if (entity.level().isClientSide() && entity instanceof Player player) {
         ClientPacketListener connection = Minecraft.getInstance().getConnection();
         if (connection == null) {
            return false;
         }

         PlayerInfo playerInfo = connection.getPlayerInfo(player.getGameProfile().getId());
         return playerInfo != null && playerInfo.getGameMode() == GameType.CREATIVE;
      } else {
         return false;
      }
   }
}
