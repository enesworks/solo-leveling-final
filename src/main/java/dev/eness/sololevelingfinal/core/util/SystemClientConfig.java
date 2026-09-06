package dev.eness.sololevelingfinal.core.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraftforge.fml.loading.FMLPaths;

public final class SystemClientConfig {
   public static final float MIN_SCALE = 0.5F;
   public static final float MAX_SCALE = 2.0F;
   public static final float DEFAULT_SCALE = 1.0F;
   public static final float MIN_NOTIFICATION_POSITION = -1.4F;
   public static final float MAX_NOTIFICATION_POSITION = 1.4F;
   public static final float DEFAULT_NOTIFICATION_POSITION = 0.0F;
   public static final float MIN_NOTIFICATION_LIFETIME = 1.0F;
   public static final float MAX_NOTIFICATION_LIFETIME = 10.0F;
   public static final float DEFAULT_NOTIFICATION_LIFETIME = 5.0F;
   public static final int OUTLINE_DENSITY_MINIMAL = 0;
   public static final int OUTLINE_DENSITY_BALANCED = 1;
   public static final int OUTLINE_DENSITY_HIGH = 2;
   private static final String KEY_NOTIF_SCALE = "notificationScale";
   private static final String KEY_NOTIF_POSITION = "notificationHorizontalOffset";
   private static final String KEY_NOTIF_LIFETIME = "notificationLifetimeSeconds";
   private static final String KEY_NOTIF_DYNAMIC = "dynamicNotificationsEnabled";
   private static final String KEY_DAMAGE_NUMBERS = "damageNumbersEnabled";
   private static final String KEY_LEGACY_OVERLAY = "legacyOverlayEnabled";
   private static final String KEY_ENTITY_OUTLINES = "entityOutlinesEnabled";
   private static final String KEY_PERCEPTION_OUTLINES = "perceptionOutlinesEnabled";
   private static final String KEY_ENCOUNTER_OUTLINES = "encounterOutlinesEnabled";
   private static final String KEY_OUTLINE_DENSITY = "outlineDensity";
   private static float notificationScale = 1.0F;
   private static float notificationHorizontalOffset = 0.0F;
   private static float notificationLifetimeSeconds = 5.0F;
   private static boolean dynamicNotificationsEnabled = false;
   private static boolean damageNumbersEnabled = true;
   private static boolean legacyOverlayEnabled = false;
   private static boolean entityOutlinesEnabled = true;
   private static boolean perceptionOutlinesEnabled = true;
   private static boolean encounterOutlinesEnabled = true;
   private static int outlineDensity = 1;
   private static boolean loaded = false;

   private SystemClientConfig() {
   }

   public static synchronized float getNotificationScale() {
      ensureLoaded();
      return notificationScale;
   }

   public static synchronized void setNotificationScale(float value) {
      ensureLoaded();
      notificationScale = clamp(value);
      save();
   }

   public static synchronized float getNotificationHorizontalOffset() {
      ensureLoaded();
      return notificationHorizontalOffset;
   }

   public static synchronized void setNotificationHorizontalOffset(float value) {
      ensureLoaded();
      notificationHorizontalOffset = clamp(value, -1.4F, 1.4F);
      save();
   }

   public static synchronized float getNotificationLifetimeSeconds() {
      ensureLoaded();
      return notificationLifetimeSeconds;
   }

   public static synchronized void setNotificationLifetimeSeconds(float value) {
      ensureLoaded();
      notificationLifetimeSeconds = clamp(value, 1.0F, 10.0F);
      save();
   }

   public static synchronized boolean isDynamicNotificationsEnabled() {
      ensureLoaded();
      return dynamicNotificationsEnabled;
   }

   public static synchronized void toggleDynamicNotifications() {
      ensureLoaded();
      dynamicNotificationsEnabled = !dynamicNotificationsEnabled;
      save();
   }

   public static synchronized boolean isDamageNumbersEnabled() {
      ensureLoaded();
      return damageNumbersEnabled;
   }

   public static synchronized void setDamageNumbersEnabled(boolean enabled) {
      ensureLoaded();
      damageNumbersEnabled = enabled;
      save();
   }

   public static synchronized void toggleDamageNumbers() {
      setDamageNumbersEnabled(!isDamageNumbersEnabled());
   }

   public static synchronized boolean isLegacyOverlayEnabled() {
      ensureLoaded();
      return legacyOverlayEnabled;
   }

   public static synchronized void setLegacyOverlayEnabled(boolean enabled) {
      ensureLoaded();
      legacyOverlayEnabled = enabled;
      save();
   }

   public static synchronized void toggleLegacyOverlay() {
      setLegacyOverlayEnabled(!isLegacyOverlayEnabled());
   }

   public static synchronized boolean isEntityOutlinesEnabled() {
      ensureLoaded();
      return entityOutlinesEnabled;
   }

   public static synchronized void toggleEntityOutlines() {
      ensureLoaded();
      entityOutlinesEnabled = !entityOutlinesEnabled;
      save();
   }

   public static synchronized boolean isPerceptionOutlinesEnabled() {
      ensureLoaded();
      return perceptionOutlinesEnabled;
   }

   public static synchronized void togglePerceptionOutlines() {
      ensureLoaded();
      perceptionOutlinesEnabled = !perceptionOutlinesEnabled;
      save();
   }

   public static synchronized boolean isEncounterOutlinesEnabled() {
      ensureLoaded();
      return encounterOutlinesEnabled;
   }

   public static synchronized void toggleEncounterOutlines() {
      ensureLoaded();
      encounterOutlinesEnabled = !encounterOutlinesEnabled;
      save();
   }

   public static synchronized int getOutlineDensity() {
      ensureLoaded();
      return outlineDensity;
   }

   public static synchronized void cycleOutlineDensity() {
      ensureLoaded();
      outlineDensity = outlineDensity >= 2 ? 0 : outlineDensity + 1;
      save();
   }

   public static synchronized String getOutlineDensityLabel() {
      return switch (getOutlineDensity()) {
         case 0 -> "Minimal";
         case 2 -> "High";
         default -> "Balanced";
      };
   }

   private static float clamp(float v) {
      return v < 0.5F ? 0.5F : (v > 2.0F ? 2.0F : v);
   }

   private static float clamp(float value, float minimum, float maximum) {
      return value < minimum ? minimum : Math.min(value, maximum);
   }

   private static Path file() {
      return FMLPaths.CONFIGDIR.get().resolve("sololeveling-client.properties");
   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;

         try {
            Path f = file();
            if (Files.exists(f)) {
               Properties p = new Properties();

               try (InputStream in = Files.newInputStream(f)) {
                  p.load(in);
               }

               String s = p.getProperty("notificationScale");
               if (s != null) {
                  notificationScale = clamp(Float.parseFloat(s));
               }

               String position = p.getProperty("notificationHorizontalOffset");
               if (position != null) {
                  notificationHorizontalOffset = clamp(Float.parseFloat(position), -1.4F, 1.4F);
               }

               String lifetime = p.getProperty("notificationLifetimeSeconds");
               if (lifetime != null) {
                  notificationLifetimeSeconds = clamp(Float.parseFloat(lifetime), 1.0F, 10.0F);
               }

               String dynamic = p.getProperty("dynamicNotificationsEnabled");
               if (dynamic != null) {
                  dynamicNotificationsEnabled = Boolean.parseBoolean(dynamic);
               }

               String damageNumbers = p.getProperty("damageNumbersEnabled");
               if (damageNumbers != null) {
                  damageNumbersEnabled = Boolean.parseBoolean(damageNumbers);
               }

               String legacyOverlay = p.getProperty("legacyOverlayEnabled");
               if (legacyOverlay != null) {
                  legacyOverlayEnabled = Boolean.parseBoolean(legacyOverlay);
               }

               String entityOutlines = p.getProperty("entityOutlinesEnabled");
               if (entityOutlines != null) {
                  entityOutlinesEnabled = Boolean.parseBoolean(entityOutlines);
               }

               String perceptionOutlines = p.getProperty("perceptionOutlinesEnabled");
               if (perceptionOutlines != null) {
                  perceptionOutlinesEnabled = Boolean.parseBoolean(perceptionOutlines);
               }

               String encounterOutlines = p.getProperty("encounterOutlinesEnabled");
               if (encounterOutlines != null) {
                  encounterOutlinesEnabled = Boolean.parseBoolean(encounterOutlines);
               }

               String density = p.getProperty("outlineDensity");
               if (density != null) {
                  outlineDensity = Math.max(0, Math.min(2, Integer.parseInt(density)));
               }
            }
         } catch (Throwable var14) {
         }
      }
   }

   private static void save() {
      try {
         Properties p = new Properties();
         p.setProperty("notificationScale", Float.toString(notificationScale));
         p.setProperty("notificationHorizontalOffset", Float.toString(notificationHorizontalOffset));
         p.setProperty("notificationLifetimeSeconds", Float.toString(notificationLifetimeSeconds));
         p.setProperty("dynamicNotificationsEnabled", Boolean.toString(dynamicNotificationsEnabled));
         p.setProperty("damageNumbersEnabled", Boolean.toString(damageNumbersEnabled));
         p.setProperty("legacyOverlayEnabled", Boolean.toString(legacyOverlayEnabled));
         p.setProperty("entityOutlinesEnabled", Boolean.toString(entityOutlinesEnabled));
         p.setProperty("perceptionOutlinesEnabled", Boolean.toString(perceptionOutlinesEnabled));
         p.setProperty("encounterOutlinesEnabled", Boolean.toString(encounterOutlinesEnabled));
         p.setProperty("outlineDensity", Integer.toString(outlineDensity));
         Path f = file();
         Files.createDirectories(f.getParent());

         try (OutputStream out = Files.newOutputStream(f)) {
            p.store(out, "Solo Leveling client settings");
         }
      } catch (Throwable var7) {
      }
   }
}
