package dev.eness.sololevelingfinal.core.procedures;

import java.text.DecimalFormat;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class ReturnJobCD2Procedure {
   public static String execute(Entity entity) {
      return entity == null ? "" : new DecimalFormat("##").format(CooldownManager.getRemainingSeconds(entity, "job_2"));
   }
}
