package dev.eness.sololevelingfinal.core.client.gui.system;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import dev.eness.sololevelingfinal.core.util.SystemClientConfig;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;

public final class SystemNotificationManager {
   public static final SystemNotificationManager INSTANCE = new SystemNotificationManager();
   public static final long APPEAR_MS = 220L;
   public static final long DISAPPEAR_MS = 200L;
   private static final int MAX = 4;
   private final List<SystemNotificationManager.Notification> notifications = new ArrayList<>();

   private SystemNotificationManager() {
   }

   public void push(int accentColor, int durationTicks, Component title, Component undertext) {
      this.push(accentColor, durationTicks, title, undertext, false);
   }

   public void push(int accentColor, int durationTicks, Component title, Component undertext, boolean negativeSound) {
      if ((title != null || undertext != null) && hasLocalSystem()) {
         long holdMs;
         if (SystemClientConfig.isDynamicNotificationsEnabled()) {
            int characters = (title == null ? 0 : title.getString().length()) + (undertext == null ? 0 : undertext.getString().length());
            holdMs = Math.max(1800L, Math.min(7000L, 1500L + characters * 42L));
         } else {
            holdMs = Math.round(SystemClientConfig.getNotificationLifetimeSeconds() * 1000.0F);
         }

         this.notifications.add(new SystemNotificationManager.Notification(accentColor, title, undertext, System.currentTimeMillis(), holdMs));
         if (negativeSound) {
            SystemGuiSounds.negativeNotification();
         } else {
            SystemGuiSounds.notification();
         }

         while (this.notifications.size() > 4) {
            this.notifications.remove(0);
         }
      }
   }

   public void title(int accentColor, int durationTicks, Component title) {
      this.push(accentColor, durationTicks, title, null);
   }

   public void titleUnder(int accentColor, int durationTicks, Component title, Component undertext) {
      this.push(accentColor, durationTicks, title, undertext);
   }

   public void under(int accentColor, int durationTicks, Component undertext) {
      this.push(accentColor, durationTicks, null, undertext);
   }

   public List<SystemNotificationManager.Notification> active() {
      if (!hasLocalSystem()) {
         this.notifications.clear();
         return List.of();
      } else {
         long now = System.currentTimeMillis();
         this.notifications.removeIf(n -> n.expired(now));
         return this.notifications;
      }
   }

   private static boolean hasLocalSystem() {
      return SystemPlayerAccess.hasSystem(Minecraft.getInstance().player);
   }

   public static final class Notification {
      public final int accent;
      public final Component title;
      public final Component under;
      public final long start;
      public final long holdMs;

      Notification(int accent, Component title, Component under, long start, long holdMs) {
         this.accent = accent;
         this.title = title;
         this.under = under;
         this.start = start;
         this.holdMs = holdMs;
      }

      public long lifeMs() {
         return 220L + this.holdMs + 200L;
      }

      public boolean expired(long now) {
         return now - this.start >= this.lifeMs();
      }

      public float reveal(long now) {
         long e = now - this.start;
         if (e < 0L) {
            return 0.0F;
         } else if (e < 220L) {
            return ease((float)e / 220.0F);
         } else if (e < 220L + this.holdMs) {
            return 1.0F;
         } else {
            return e < this.lifeMs() ? ease(1.0F - (float)(e - 220L - this.holdMs) / 200.0F) : 0.0F;
         }
      }

      public boolean transitioning(long now) {
         long e = now - this.start;
         return e < 220L || e >= 220L + this.holdMs;
      }

      private static float ease(float t) {
         t = t < 0.0F ? 0.0F : (t > 1.0F ? 1.0F : t);
         return t * t * (3.0F - 2.0F * t);
      }
   }
}
