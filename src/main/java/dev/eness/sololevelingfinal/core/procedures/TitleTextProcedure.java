package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class TitleTextProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else {
         SololevelingModVariables.PlayerVariables vars = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if ("thomas_andre".equals(vars.vesselIdentity)) {
            return "Goliath";
         } else if ("liu_zhigang".equals(vars.vesselIdentity)) {
            return "Sword Demon";
         } else if ("sung_il_hwan".equals(vars.vesselIdentity)) {
            return "Silent Authority";
         } else if ("rakan".equals(vars.vesselIdentity)) {
            return "Monarch of Fangs";
         } else if ("antares".equals(vars.vesselIdentity)) {
            return "Monarch of Destruction";
         } else if (vars.JOB == 1.0) {
            return "Shadow Monarch";
         } else if (vars.JOB == 2.0) {
            return "Grand Mage";
         } else if (vars.JOB == 3.0) {
            return "Frost Monarch";
         } else if (vars.JOB == 3.0) {
            return "Demon Monarch";
         } else {
            return vars.JOB == 9.0 ? "Monarch of Fangs" : "none";
         }
      }
   }
}
