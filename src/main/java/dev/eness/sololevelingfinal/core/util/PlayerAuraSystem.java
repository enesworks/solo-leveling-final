package dev.eness.sololevelingfinal.core.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerEvent.StopTracking;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.PlayerAuraMessage;

@EventBusSubscriber(modid = "sololeveling")
public final class PlayerAuraSystem {
   private static final String AURA_ID = "sololeveling_player_aura";
   private static final String AURA_INTENSITY = "sololeveling_player_aura_intensity";

   private PlayerAuraSystem() {
   }

   public static void setContinuous(ServerPlayer player, String auraId, float intensity) {
      CompoundTag data = player.getPersistentData();
      data.putString("sololeveling_player_aura", auraId);
      data.putFloat("sololeveling_player_aura_intensity", intensity);
      sendTracking(player, new PlayerAuraMessage(player.getId(), auraId, (byte)0, 0, intensity));
   }

   public static void clearContinuous(ServerPlayer player) {
      player.getPersistentData().remove("sololeveling_player_aura");
      player.getPersistentData().remove("sololeveling_player_aura_intensity");
      sendTracking(player, new PlayerAuraMessage(player.getId(), "", (byte)1, 0, 1.0F));
   }

   public static void burst(ServerPlayer player, String auraId, int durationTicks, float intensity) {
      sendTracking(player, new PlayerAuraMessage(player.getId(), auraId, (byte)2, Math.max(1, durationTicks), intensity));
   }

   private static void sendTracking(ServerPlayer player, PlayerAuraMessage message) {
      SololevelingMod.PACKET_HANDLER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), message);
   }

   private static void sendCurrent(ServerPlayer subject, ServerPlayer receiver) {
      CompoundTag data = subject.getPersistentData();
      if (data.contains("sololeveling_player_aura")) {
         SololevelingMod.PACKET_HANDLER
            .send(
               PacketDistributor.PLAYER.with(() -> receiver),
               new PlayerAuraMessage(
                  subject.getId(),
                  data.getString("sololeveling_player_aura"),
                  (byte)0,
                  0,
                  data.contains("sololeveling_player_aura_intensity") ? data.getFloat("sololeveling_player_aura_intensity") : 1.0F
               )
            );
      }
   }

   @SubscribeEvent
   public static void onStartTracking(StartTracking event) {
      if (event.getEntity() instanceof ServerPlayer receiver && event.getTarget() instanceof ServerPlayer subject) {
         sendCurrent(subject, receiver);
      }
   }

   @SubscribeEvent
   public static void onStopTracking(StopTracking event) {
      if (event.getEntity() instanceof ServerPlayer receiver && event.getTarget() instanceof ServerPlayer subject) {
         SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> receiver), new PlayerAuraMessage(subject.getId(), "", (byte)1, 0, 1.0F));
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         sendCurrent(player, player);
      }
   }

   @SubscribeEvent
   public static void onChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         sendCurrent(player, player);
      }
   }
}
