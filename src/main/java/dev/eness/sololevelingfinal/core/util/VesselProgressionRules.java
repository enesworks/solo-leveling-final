package dev.eness.sololevelingfinal.core.util;

final class VesselProgressionRules {
   static final int SHADOW_MONARCH_JOB = 1;
   static final double MONARCHS_DOMAIN_FLOOR = 10.0;
   static final double SHADOW_MANIFESTATION_LEVEL = 120.0;
   static final double SHADOW_MANIFESTATION_STORAGE = 60.0;

   private VesselProgressionRules() {
   }

   static boolean hasMonarchsDomain(int resolvedJob, double dkcCleared) {
      return resolvedJob == 1 && dkcCleared >= 10.0;
   }

   static boolean isShadowMonarch(int resolvedJob) {
      return resolvedJob == 1;
   }

   static boolean canUseShadowExchangeRunestone(int resolvedJob, boolean alreadyUnlocked) {
      return isShadowMonarch(resolvedJob) && !alreadyUnlocked;
   }

   static boolean canUnlockShadowManifestation(
      int resolvedJob, boolean alreadyUnlocked, double level, double shadowStorageUsage, boolean shadowExchange, double dkcCleared
   ) {
      if (resolvedJob != 1) {
         return false;
      } else {
         return alreadyUnlocked ? true : level >= 120.0 && shadowStorageUsage >= 60.0 && shadowExchange && dkcCleared >= 10.0;
      }
   }
}
