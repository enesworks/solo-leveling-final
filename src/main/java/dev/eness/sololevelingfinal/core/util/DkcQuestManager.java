package dev.eness.sololevelingfinal.core.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class DkcQuestManager {
   private DkcQuestManager() {
   }

   public static boolean isUnlocked(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = vars(entity);
      return vars.dkc_unlocked > 0.0 || vars.dkc_started || vars.dkc_cleared > 0.0;
   }

   public static boolean isFinished(Entity entity) {
      return entity != null && vars(entity).dkc_cleared >= 20.0;
   }

   public static boolean isVisible(Entity entity) {
      return hasRadiruCastleAccess(entity) ? true : isUnlocked(entity) && (!isFinished(entity) || DkcFloorRegistry.isDkc(entity.level()));
   }

   public static boolean hasRadiruCastleAccess(Entity entity) {
      if (entity == null) {
         return false;
      }

      SololevelingModVariables.PlayerVariables vars = vars(entity);
      return vars.radiru_side_quest_unlocked || vars.dkc_cleared >= 20.0 && !vars.radiru_slaughtered;
   }

   public static Component buttonLabel(Entity entity) {
      return entity != null && DkcFloorRegistry.isDkc(entity.level())
         ? Component.literal("Castle Tower")
         : Component.literal(hasRadiruCastleAccess(entity) ? "Radiru Castle" : "Demon King's Castle");
   }

   public static void unlock(Entity entity) {
      if (entity != null) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            boolean changed = false;
            if (capability.dkc_cleared < 20.0 && capability.dkc_unlocked <= 0.0) {
               capability.dkc_unlocked = 1.0;
               changed = true;
            }

            if (capability.dailysecrettrans != 0.0) {
               capability.dailysecrettrans = 0.0;
               changed = true;
            }

            if (changed) {
               capability.syncPlayerVariables(entity);
            }
         });
      }
   }

   private static SololevelingModVariables.PlayerVariables vars(Entity entity) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }
}
