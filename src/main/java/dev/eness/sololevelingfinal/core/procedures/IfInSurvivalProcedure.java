package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class IfInSurvivalProcedure {
   public static boolean execute(Entity entity) {
      if (entity == null) {
         return false;
      }

      GameType gameType;
      if (entity instanceof ServerPlayer serverPlayer) {
         gameType = serverPlayer.gameMode.getGameModeForPlayer();
      } else {
         if (!entity.level().isClientSide() || !(entity instanceof Player player)) {
            return false;
         }

         ClientPacketListener connection = Minecraft.getInstance().getConnection();
         if (connection == null) {
            return false;
         }

         PlayerInfo playerInfo = connection.getPlayerInfo(player.getGameProfile().getId());
         if (playerInfo == null) {
            return false;
         }

         gameType = playerInfo.getGameMode();
      }

      return gameType != GameType.SURVIVAL && gameType != GameType.ADVENTURE
         ? false
         : entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElseGet(SololevelingModVariables.PlayerVariables::new).CustomHUD;
   }
}
