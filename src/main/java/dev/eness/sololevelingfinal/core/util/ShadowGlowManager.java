package dev.eness.sololevelingfinal.core.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public final class ShadowGlowManager {
   private static final Map<UUID, ShadowGlowManager.ViewerState> VIEWERS = new HashMap<>();

   private ShadowGlowManager() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer owner && owner.tickCount % 20 == 0) {
         sync(owner);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer owner) {
         VIEWERS.remove(owner.getUUID());
         sync(owner);
      }
   }

   @SubscribeEvent
   public static void onPlayerLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer owner) {
         VIEWERS.remove(owner.getUUID());
      }
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer owner) {
         clear(owner);
         sync(owner);
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      VIEWERS.clear();
   }

   public static void syncNow(ServerPlayer owner) {
      if (owner != null && !owner.hasDisconnected()) {
         sync(owner);
      }
   }

   private static void sync(ServerPlayer owner) {
      if (owner != null && !owner.hasDisconnected()) {
         ResourceKey<Level> dimension = owner.level().dimension();
         Map<UUID, Integer> desired = new LinkedHashMap<>();

         for (Entry<UUID, String> summoned : ShadowMonarchManager.summonedShadowTypes(owner).entrySet()) {
            int color = ShadowMonarchManager.glowColor(owner, summoned.getValue());
            if (color != -1) {
               desired.put(summoned.getKey(), color & 16777215);
            }
         }

         ShadowGlowManager.ViewerState state = VIEWERS.computeIfAbsent(owner.getUUID(), ignored -> new ShadowGlowManager.ViewerState());

         for (Entry<UUID, ShadowGlowManager.Applied> previous : List.copyOf(state.applied.entrySet())) {
            Integer wanted = desired.get(previous.getKey());
            if (wanted == null || !previous.getValue().dimension.equals(dimension)) {
               EntityHighlightSystem.hide(owner, previous.getKey(), previous.getValue().dimension, "shadow:glow");
            }
         }

         for (Entry<UUID, Integer> target : desired.entrySet()) {
            ShadowGlowManager.Applied previous = state.applied.get(target.getKey());
            if (previous == null || previous.color != target.getValue() || !previous.dimension.equals(dimension)) {
               EntityHighlightSystem.show(owner, target.getKey(), dimension, "shadow:glow", target.getValue(), 0, 90);
            }
         }

         state.applied.clear();

         for (Entry<UUID, Integer> target : desired.entrySet()) {
            state.applied.put(target.getKey(), new ShadowGlowManager.Applied(target.getValue(), dimension));
         }

         if (state.applied.isEmpty()) {
            VIEWERS.remove(owner.getUUID());
         }
      }
   }

   private static void clear(ServerPlayer owner) {
      VIEWERS.remove(owner.getUUID());
      EntityHighlightSystem.clearSource(owner, "shadow:glow");
   }

   private record Applied(int color, ResourceKey<Level> dimension) {
   }

   private static final class ViewerState {
      private final Map<UUID, ShadowGlowManager.Applied> applied = new HashMap<>();
   }
}
