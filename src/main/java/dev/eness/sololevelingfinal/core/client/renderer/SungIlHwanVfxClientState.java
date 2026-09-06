package dev.eness.sololevelingfinal.core.client.renderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.network.SungIlHwanVfxEventMessage;

@OnlyIn(Dist.CLIENT)
public final class SungIlHwanVfxClientState {
   public static final int MAX_EXECUTION_TARGET_MARKS = 96;
   public static final int MAX_EVENTS = 160;
   private static final List<SungIlHwanVfxClientState.ActiveEvent> EVENTS = new ArrayList<>(160);
   private static long nextSequence;

   private SungIlHwanVfxClientState() {
   }

   public static void enqueue(SungIlHwanVfxEventMessage message) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!minecraft.isSameThread()) {
         minecraft.execute(() -> enqueue(message));
      } else if (minecraft.level != null && minecraft.player != null && message != null && SungIlHwanVfxEventMessage.isKnownEventType(message.eventType)) {
         if (!message.privateToCaster() || message.casterEntityId == minecraft.player.getId()) {
            long now = minecraft.level.getGameTime();
            prune(now);
            long elapsed = now - message.serverStartTick;
            if (elapsed < message.duration && elapsed >= -100L) {
               if (!isDuplicate(message)) {
                  applyTransition(message);
                  if (message.eventType == 2) {
                     addBounded(new SungIlHwanVfxClientState.ActiveEvent(message, nextSequence++));
                  } else if (message.eventType == 10) {
                     addBounded(new SungIlHwanVfxClientState.ActiveEvent(message, nextSequence++));
                  } else {
                     addBounded(new SungIlHwanVfxClientState.ActiveEvent(message, nextSequence++));
                  }
               }
            }
         }
      }
   }

   public static List<SungIlHwanVfxClientState.ActiveEvent> snapshot(long now) {
      prune(now);
      return List.copyOf(EVENTS);
   }

   public static SungIlHwanVfxClientState.OverlayState overlay(float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level != null && minecraft.player != null) {
         long now = minecraft.level.getGameTime();
         prune(now);
         SungIlHwanVfxClientState.ActiveEvent targeting = newestExecutionSphereForLocal();
         SungIlHwanVfxClientState.ActiveEvent exhaustion = newestForLocal((byte)11);
         SungIlHwanVfxClientState.ActiveEvent risk = newestForLocal((byte)12);
         return new SungIlHwanVfxClientState.OverlayState(
            frame(targeting, now, partialTick), frame(exhaustion, now, partialTick), frame(risk, now, partialTick)
         );
      } else {
         return SungIlHwanVfxClientState.OverlayState.EMPTY;
      }
   }

   public static void clear() {
      EVENTS.clear();
      nextSequence = 0L;
   }

   public static void onResourceReload() {
      Minecraft minecraft = Minecraft.getInstance();
      if (!minecraft.isSameThread()) {
         minecraft.execute(SungIlHwanVfxClientState::onResourceReload);
      } else {
         if (minecraft.level == null) {
            clear();
         } else {
            prune(minecraft.level.getGameTime());
         }
      }
   }

   private static void applyTransition(SungIlHwanVfxEventMessage message) {
      if (message.eventType == 0 || message.eventType == 1 || message.eventType == 2) {
         removeForCaster(message.casterEntityId, 0, 1, 2);
      }

      if (message.eventType == 7) {
         if (message.variant < 2) {
            EVENTS.removeIf(event -> event.message.casterEntityId == message.casterEntityId && event.message.eventType == 7);
         } else {
            EVENTS.removeIf(
               event -> event.message.casterEntityId == message.casterEntityId
                  && event.message.eventType == 7
                  && event.message.variant >= 2
                  && event.message.targetEntityId == message.targetEntityId
            );
         }
      }

      if (message.eventType == 6) {
         removeForCaster(message.casterEntityId, 6);
      }

      if (message.eventType == 8 || message.eventType == 10) {
         removeForCaster(message.casterEntityId, 6, 7);
      }

      if (message.eventType == 11) {
         removeForCaster(message.casterEntityId, 11);
      }

      if (message.eventType == 12) {
         removeForCaster(message.casterEntityId, 12);
      }
   }

   private static void removeForCaster(int casterId, byte... types) {
      EVENTS.removeIf(event -> {
         if (event.message.casterEntityId != casterId) {
            return false;
         }

         for (byte type : types) {
            if (event.message.eventType == type) {
               return true;
            }
         }

         return false;
      });
   }

   private static boolean isDuplicate(SungIlHwanVfxEventMessage message) {
      return EVENTS.stream()
         .anyMatch(
            event -> event.message.eventType == message.eventType
               && event.message.casterEntityId == message.casterEntityId
               && event.message.targetEntityId == message.targetEntityId
               && event.message.serverStartTick == message.serverStartTick
               && event.message.seed == message.seed
               && event.message.variant == message.variant
         );
   }

   private static void addBounded(SungIlHwanVfxClientState.ActiveEvent incoming) {
      if (EVENTS.size() >= 160) {
         SungIlHwanVfxClientState.ActiveEvent candidate = EVENTS.stream()
            .filter(event -> !event.essential())
            .min(Comparator.comparingLong(SungIlHwanVfxClientState.ActiveEvent::sequence))
            .orElse(EVENTS.get(0));
         EVENTS.remove(candidate);
      }

      EVENTS.add(incoming);
   }

   private static void prune(long now) {
      Minecraft minecraft = Minecraft.getInstance();
      int localId = minecraft.player == null ? -1 : minecraft.player.getId();
      EVENTS.removeIf(event -> {
         long elapsed = now - event.message.serverStartTick;
         return elapsed < event.message.duration && elapsed >= -100L ? event.message.privateToCaster() && event.message.casterEntityId != localId : true;
      });

      while (EVENTS.size() > 160) {
         EVENTS.remove(0);
      }
   }

   private static SungIlHwanVfxClientState.ActiveEvent newestForLocal(byte type) {
      Minecraft minecraft = Minecraft.getInstance();
      int localId = minecraft.player == null ? -1 : minecraft.player.getId();
      SungIlHwanVfxClientState.ActiveEvent newest = null;

      for (SungIlHwanVfxClientState.ActiveEvent event : EVENTS) {
         if (event.message.eventType == type && event.message.casterEntityId == localId && (newest == null || event.sequence > newest.sequence)) {
            newest = event;
         }
      }

      return newest;
   }

   private static SungIlHwanVfxClientState.ActiveEvent newestExecutionSphereForLocal() {
      Minecraft minecraft = Minecraft.getInstance();
      int localId = minecraft.player == null ? -1 : minecraft.player.getId();
      SungIlHwanVfxClientState.ActiveEvent newest = null;

      for (SungIlHwanVfxClientState.ActiveEvent event : EVENTS) {
         if (event.message.eventType == 7
            && event.message.casterEntityId == localId
            && event.message.variant < 2
            && (newest == null || event.sequence > newest.sequence)) {
            newest = event;
         }
      }

      return newest;
   }

   private static SungIlHwanVfxClientState.OverlayFrame frame(SungIlHwanVfxClientState.ActiveEvent event, long now, float partialTick) {
      if (event == null) {
         return SungIlHwanVfxClientState.OverlayFrame.EMPTY;
      } else {
         float elapsed = event.elapsed(now, partialTick);
         if (!(elapsed < 0.0F) && !(elapsed >= event.message.duration)) {
            float progress = Mth.clamp(elapsed / Math.max(1.0F, event.message.duration), 0.0F, 1.0F);
            return new SungIlHwanVfxClientState.OverlayFrame(
               true, progress, event.message.intensity / 255.0F, event.message.variant, event.message.seed, event.message.focus(), event.message.radius
            );
         } else {
            return SungIlHwanVfxClientState.OverlayFrame.EMPTY;
         }
      }
   }

   public static final class ActiveEvent {
      private final SungIlHwanVfxEventMessage message;
      private final long sequence;

      private ActiveEvent(SungIlHwanVfxEventMessage message, long sequence) {
         this.message = message;
         this.sequence = sequence;
      }

      public SungIlHwanVfxEventMessage message() {
         return this.message;
      }

      public long sequence() {
         return this.sequence;
      }

      public float elapsed(long now, float partialTick) {
         return (float)(now - this.message.serverStartTick) + partialTick;
      }

      public float progress(long now, float partialTick) {
         return Mth.clamp(this.elapsed(now, partialTick) / Math.max(1.0F, this.message.duration), 0.0F, 1.0F);
      }

      public boolean essential() {
         return this.message.hasFlag(1);
      }
   }

   public record OverlayFrame(boolean active, float progress, float intensity, int variant, int seed, Vec3 focus, float radius) {
      private static final SungIlHwanVfxClientState.OverlayFrame EMPTY = new SungIlHwanVfxClientState.OverlayFrame(false, 0.0F, 0.0F, 0, 0, Vec3.ZERO, 1.0F);
   }

   public record OverlayState(
      SungIlHwanVfxClientState.OverlayFrame targeting, SungIlHwanVfxClientState.OverlayFrame exhaustion, SungIlHwanVfxClientState.OverlayFrame risk
   ) {
      private static final SungIlHwanVfxClientState.OverlayState EMPTY = new SungIlHwanVfxClientState.OverlayState(
         SungIlHwanVfxClientState.OverlayFrame.EMPTY, SungIlHwanVfxClientState.OverlayFrame.EMPTY, SungIlHwanVfxClientState.OverlayFrame.EMPTY
      );

      public boolean active() {
         return this.targeting.active || this.exhaustion.active || this.risk.active;
      }
   }
}
