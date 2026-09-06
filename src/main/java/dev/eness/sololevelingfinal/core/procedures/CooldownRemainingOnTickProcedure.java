package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.AssassinSkillManager;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.JobSkillManager;

public class CooldownRemainingOnTickProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         SololevelingModVariables.PlayerVariables cap = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         String power = normalizeCooldownKey(cap.PselectedPower);
         if (power.equals("Back Step")) {
            int charges = (int)cap.rangerleapnum;
            return charges < 3 ? "Charges: " + charges + " / CD:" + (int)(cap.rangerleaptimer / 20.0) : "Charges: " + charges;
         } else {
            int remaining = CooldownManager.getRemainingSeconds(entity, power);
            return remaining > 0 ? String.valueOf(remaining) : "";
         }
      }
   }

   public static String executeForSkill(Entity entity, String power) {
      if (entity != null && power != null && !power.isBlank()) {
         SololevelingModVariables.PlayerVariables cap = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         String cooldownKey = normalizeCooldownKey(power);
         if (!cooldownKey.equals("Back Step")) {
            int remaining = CooldownManager.getRemainingSeconds(entity, cooldownKey);
            return remaining > 0 ? String.valueOf(remaining) : "";
         }

         if (cap.rangerleapnum >= 3.0) {
            return "";
         }

         int remaining = Math.max(1, (int)Math.ceil(cap.rangerleaptimer / 20.0));
         return String.valueOf(remaining);
      } else {
         return "";
      }
   }

   private static String normalizeCooldownKey(String power) {
      String canonical = AssassinSkillManager.canonicalName(power);
      if ("Dagger Throw".equalsIgnoreCase(canonical)) {
         return "dagger_throw";
      } else if ("Dagger Rush".equalsIgnoreCase(canonical)) {
         return "dagger_rush_projectile";
      } else if (JobSkillManager.isJobSkill(canonical)) {
         return JobSkillManager.cooldownKey(canonical);
      } else {
         return canonical.equalsIgnoreCase("Critical Strike") ? "Cross Strike" : canonical;
      }
   }
}
