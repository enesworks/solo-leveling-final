package dev.eness.sololevelingfinal.core.client.aura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientPlayerAuraManager {
   private static final Map<Integer, ClientPlayerAuraManager.AuraInstance> CONTINUOUS = new HashMap<>();
   private static final Map<Integer, List<ClientPlayerAuraManager.AuraInstance>> BURSTS = new HashMap<>();
   private static final Map<Integer, List<ClientPlayerAuraManager.TrailPoint>> TRAILS = new HashMap<>();
   private static final int TRAIL_LIFETIME = 14;
   private static final int MAX_TRAIL_POINTS = 8;

   private ClientPlayerAuraManager() {
   }

   public static void handle(int entityId, String auraId, byte action, int duration, float intensity) {
      if (action == 1) {
         CONTINUOUS.remove(entityId);
      } else if (PlayerAuraRegistry.get(auraId) != null) {
         long now = gameTime();
         ClientPlayerAuraManager.AuraInstance instance = new ClientPlayerAuraManager.AuraInstance(
            auraId, now, action == 2 ? Math.max(1, duration) : -1, Math.max(0.05F, Math.min(3.0F, intensity)), entityId * 31 + auraId.hashCode()
         );
         if (action == 2) {
            BURSTS.computeIfAbsent(entityId, ignored -> new ArrayList<>()).add(instance);
         } else {
            CONTINUOUS.put(entityId, instance);
         }
      }
   }

   public static List<ClientPlayerAuraManager.AuraInstance> activeFor(int entityId) {
      long now = gameTime();
      List<ClientPlayerAuraManager.AuraInstance> result = new ArrayList<>(4);
      ClientPlayerAuraManager.AuraInstance continuous = CONTINUOUS.get(entityId);
      if (continuous != null) {
         result.add(continuous);
      }

      List<ClientPlayerAuraManager.AuraInstance> bursts = BURSTS.get(entityId);
      if (bursts != null) {
         Iterator<ClientPlayerAuraManager.AuraInstance> iterator = bursts.iterator();

         while (iterator.hasNext()) {
            ClientPlayerAuraManager.AuraInstance burst = iterator.next();
            if (burst.expired(now)) {
               iterator.remove();
            } else {
               result.add(burst);
            }
         }

         if (bursts.isEmpty()) {
            BURSTS.remove(entityId);
         }
      }

      return result;
   }

   public static void recordTrail(int entityId, Vec3 position) {
      long now = gameTime();
      List<ClientPlayerAuraManager.TrailPoint> trail = TRAILS.computeIfAbsent(entityId, ignored -> new ArrayList<>());
      trail.removeIf(point -> now - point.tick() > 14L);
      if (!trail.isEmpty()) {
         ClientPlayerAuraManager.TrailPoint newest = trail.get(0);
         double distance = newest.position().distanceToSqr(position);
         if (distance > 64.0) {
            trail.clear();
         } else if (distance < 0.018) {
            return;
         }
      }

      trail.add(0, new ClientPlayerAuraManager.TrailPoint(position, now));

      while (trail.size() > 8) {
         trail.remove(trail.size() - 1);
      }
   }

   public static List<ClientPlayerAuraManager.TrailPoint> trailFor(int entityId) {
      long now = gameTime();
      List<ClientPlayerAuraManager.TrailPoint> trail = TRAILS.get(entityId);
      if (trail == null) {
         return List.of();
      } else {
         trail.removeIf(point -> now - point.tick() > 14L);
         if (trail.isEmpty()) {
            TRAILS.remove(entityId);
            return List.of();
         } else {
            return List.copyOf(trail);
         }
      }
   }

   public static void clearTrail(int entityId) {
      TRAILS.remove(entityId);
   }

   public static void clearEntity(int entityId) {
      CONTINUOUS.remove(entityId);
      BURSTS.remove(entityId);
      TRAILS.remove(entityId);
   }

   public static void clear() {
      CONTINUOUS.clear();
      BURSTS.clear();
      TRAILS.clear();
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      clear();
   }

   @SubscribeEvent
   public static void onEntityLeave(EntityLeaveLevelEvent event) {
      if (event.getLevel().isClientSide()) {
         clearEntity(event.getEntity().getId());
      }
   }

   @SubscribeEvent
   public static void onLevelUnload(Unload event) {
      if (event.getLevel().isClientSide()) {
         clear();
      }
   }

   private static long gameTime() {
      return Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
   }

   public record AuraInstance(String auraId, long startTick, int duration, float intensity, int seed) {
      public boolean expired(long now) {
         return this.duration >= 0 && now - this.startTick >= this.duration;
      }

      public float envelope(float partialTick, long now) {
         if (this.duration < 0) {
            return 1.0F;
         }

         float progress = Math.max(0.0F, Math.min(1.0F, ((float)(now - this.startTick) + partialTick) / this.duration));
         float fadeIn = Math.min(1.0F, progress * 7.0F);
         float fadeOut = Math.min(1.0F, (1.0F - progress) * 4.0F);
         return fadeIn * fadeOut;
      }
   }

   public record TrailPoint(Vec3 position, long tick) {
   }
}
