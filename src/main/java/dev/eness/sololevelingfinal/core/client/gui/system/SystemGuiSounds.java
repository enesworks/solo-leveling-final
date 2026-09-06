package dev.eness.sololevelingfinal.core.client.gui.system;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import dev.eness.sololevelingfinal.core.init.SololevelingModSounds;

public final class SystemGuiSounds {
   private static final float OPEN_VOLUME = 0.5F;
   private static final float CLOSE_VOLUME = 0.5F;
   private static final float NOTIFICATION_VOLUME = 0.5F;
   private static final float NEGATIVE_VOLUME = 0.62F;
   private static final float PITCH = 1.0F;
   private static final long COOLDOWN_MS = 90L;
   private static boolean active;
   private static long lastSoundMs;

   private SystemGuiSounds() {
   }

   public static void enter() {
      if (!active) {
         active = true;
         play(SololevelingModSounds.PANELOPEN.get(), 1.0F, 0.5F);
      }
   }

   public static void exit() {
      if (active) {
         active = false;
         play(SololevelingModSounds.PANELCLOSE.get(), 1.0F, 0.5F);
      }
   }

   public static void switchInsideSystem() {
      active = true;
   }

   public static void notification() {
      play(SololevelingModSounds.PANELOPEN.get(), 1.0F, 0.5F);
   }

   public static void negativeNotification() {
      play(SololevelingModSounds.SYSTEM_NEGATIVE.get(), 1.0F, 0.62F);
   }

   private static void play(SoundEvent sound, float pitch, float volume) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getSoundManager() != null) {
         long now = Util.getMillis();
         if (now - lastSoundMs >= 90L) {
            lastSoundMs = now;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
         }
      }
   }
}
