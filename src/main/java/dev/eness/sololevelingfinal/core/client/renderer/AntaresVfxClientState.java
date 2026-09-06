package dev.eness.sololevelingfinal.core.client.renderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.network.AntaresVfxEventMessage;

@OnlyIn(Dist.CLIENT)
public final class AntaresVfxClientState {
   public static final int MAX_EVENTS = 128;
   private static final List<AntaresVfxClientState.ActiveEvent> EVENTS = new ArrayList<>(128);
   private static long nextSequence;
   private static int ruinCharges;
   private static int ruinMaximum = 3;
   private static boolean manifested;
   private static long lastRuinChangeTick = Long.MIN_VALUE;

   private AntaresVfxClientState() {
   }

   public static void enqueue(AntaresVfxEventMessage message) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!minecraft.isSameThread()) {
         minecraft.execute(() -> enqueue(message));
      } else if (minecraft.level != null && minecraft.player != null && message != null && AntaresVfxEventMessage.isKnownEventType(message.eventType)) {
         if (!message.privateToCaster() || message.casterEntityId == minecraft.player.getId()) {
            long now = minecraft.level.getGameTime();
            prune(now);
            if (message.eventType != 0) {
               long elapsed = now - message.serverStartTick;
               if (elapsed < message.duration && elapsed >= -100L && !duplicate(message)) {
                  applyTransition(message);
                  if (message.casterEntityId == minecraft.player.getId()) {
                     if (message.eventType == 13) {
                        manifested = true;
                     } else if (message.eventType == 14) {
                        manifested = false;
                     }
                  }

                  addBounded(new AntaresVfxClientState.ActiveEvent(message, nextSequence++));
               }
            } else {
               int nextMaximum = Mth.clamp(Mth.floor(message.radius + 0.5F), 1, 31);
               int nextCharges = Mth.clamp(message.variant, 0, nextMaximum);
               if (nextCharges != ruinCharges || nextMaximum != ruinMaximum) {
                  lastRuinChangeTick = now;
               }

               ruinMaximum = nextMaximum;
               ruinCharges = nextCharges;
               manifested = message.hasFlag(8);
            }
         }
      }
   }

   public static List<AntaresVfxClientState.ActiveEvent> snapshot(long now) {
      prune(now);
      return List.copyOf(EVENTS);
   }

   public static AntaresVfxClientState.HudState hudState(float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level != null && minecraft.player != null) {
         long now = minecraft.level.getGameTime();
         prune(now);
         float pulse = lastRuinChangeTick == Long.MIN_VALUE ? 0.0F : 1.0F - Mth.clamp(((float)(now - lastRuinChangeTick) + partialTick) / 12.0F, 0.0F, 1.0F);
         return new AntaresVfxClientState.HudState(ruinCharges, ruinMaximum, manifested, pulse, newestLocalProgress((byte)10, now, partialTick));
      } else {
         return AntaresVfxClientState.HudState.EMPTY;
      }
   }

   public static void clear() {
      EVENTS.clear();
      nextSequence = 0L;
      ruinCharges = 0;
      ruinMaximum = 3;
      manifested = false;
      lastRuinChangeTick = Long.MIN_VALUE;
   }

   public static void onResourceReload() {
      Minecraft minecraft = Minecraft.getInstance();
      if (!minecraft.isSameThread()) {
         minecraft.execute(AntaresVfxClientState::onResourceReload);
      } else {
         if (minecraft.level == null) {
            clear();
         } else {
            prune(minecraft.level.getGameTime());
         }
      }
   }

   private static void applyTransition(AntaresVfxEventMessage message) {
      byte type = message.eventType;
      if (type == 2) {
         removeForCaster(message.casterEntityId, 2, 3, 4);
      } else if (type == 3) {
         removeForCaster(message.casterEntityId, 2, 3);
      } else if (type == 4) {
         removeForCaster(message.casterEntityId, 2, 3, 4);
      } else if (type == 5) {
         removeForCaster(message.casterEntityId, 5);
      } else if (type == 6) {
         removeForCaster(message.casterEntityId, 5);
      } else if (type == 7) {
         removeForCaster(message.casterEntityId, 7);
      } else if (type == 8) {
         removeForCaster(message.casterEntityId, 7);
      } else if (type == 9 && message.targetEntityId >= 0) {
         EVENTS.removeIf(event -> event.message.eventType == 9 && event.message.targetEntityId == message.targetEntityId);
      } else if (type == 10) {
         removeForCaster(message.casterEntityId, 10, 11, 12);
      } else if (type == 12) {
         removeForCaster(message.casterEntityId, 10);
      } else if (type == 13 || type == 14) {
         removeForCaster(message.casterEntityId, 13, 14);
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

   private static boolean duplicate(AntaresVfxEventMessage message) {
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

   private static void addBounded(AntaresVfxClientState.ActiveEvent incoming) {
      if (EVENTS.size() >= 128) {
         AntaresVfxClientState.ActiveEvent oldest = EVENTS.stream()
            .filter(event -> !event.essential())
            .min(Comparator.comparingLong(AntaresVfxClientState.ActiveEvent::sequence))
            .orElse(EVENTS.get(0));
         EVENTS.remove(oldest);
      }

      EVENTS.add(incoming);
   }

   private static void prune(long now) {
      Minecraft minecraft = Minecraft.getInstance();
      int localId = minecraft.player == null ? -1 : minecraft.player.getId();
      EVENTS.removeIf(event -> {
         long elapsed = now - event.message.serverStartTick;
         return elapsed >= event.message.duration || elapsed < -100L || event.message.privateToCaster() && event.message.casterEntityId != localId;
      });

      while (EVENTS.size() > 128) {
         EVENTS.remove(0);
      }
   }

   private static float newestLocalProgress(byte type, long now, float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      int localId = minecraft.player == null ? -1 : minecraft.player.getId();
      AntaresVfxClientState.ActiveEvent newest = null;

      for (AntaresVfxClientState.ActiveEvent event : EVENTS) {
         if (event.message.eventType == type && event.message.casterEntityId == localId && (newest == null || event.sequence > newest.sequence)) {
            newest = event;
         }
      }

      return newest == null ? -1.0F : newest.progress(now, partialTick);
   }

   public static final class ActiveEvent {
      private final AntaresVfxEventMessage message;
      private final long sequence;

      private ActiveEvent(AntaresVfxEventMessage message, long sequence) {
         this.message = message;
         this.sequence = sequence;
      }

      public AntaresVfxEventMessage message() {
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

   public record HudState(int charges, int maximum, boolean manifested, float pulse, float extinctionChargeProgress) {
      private static final AntaresVfxClientState.HudState EMPTY = new AntaresVfxClientState.HudState(0, 3, false, 0.0F, -1.0F);
   }
}
