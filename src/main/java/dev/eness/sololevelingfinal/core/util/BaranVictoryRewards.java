package dev.eness.sololevelingfinal.core.util;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class BaranVictoryRewards {
   public static final String SECOND_DAGGER_REWARD = "ITEM:sololeveling:demon_kings_dagger";
   public static final String SHADOW_EXCHANGE_RUNESTONE_REWARD = "ITEM:sololeveling:runestone_shadow_exchange";
   private static final String REWARDS_RESOLVED_TAG = "dkc_baran_victory_rewards_resolved_v1";

   private BaranVictoryRewards() {
   }

   public static boolean grantIfNeeded(ServerPlayer player) {
      if (player == null) {
         return false;
      }

      CompoundTag data = persistedData(player);
      if (data.getBoolean("dkc_baran_victory_rewards_resolved_v1")) {
         return false;
      }

      RewardManager.appendReward(player, "ITEM:sololeveling:demon_kings_dagger");
      boolean shadowMonarch = VesselProgressionManager.isShadowMonarch(player);
      if (shadowMonarch) {
         RewardManager.appendReward(player, "ITEM:sololeveling:runestone_shadow_exchange");
      }

      data.putBoolean("dkc_baran_victory_rewards_resolved_v1", true);
      String detail = shadowMonarch ? "Demon King's Dagger and Shadow Exchange Stone added to System Rewards." : "Demon King's Dagger added to System Rewards.";
      player.displayClientMessage(Component.literal(detail).withStyle(ChatFormatting.LIGHT_PURPLE), false);
      return true;
   }

   private static CompoundTag persistedData(Player player) {
      CompoundTag root = player.getPersistentData();
      CompoundTag persisted = root.getCompound("PlayerPersisted");
      if (!root.contains("PlayerPersisted")) {
         root.put("PlayerPersisted", persisted);
      }

      return persisted;
   }
}
