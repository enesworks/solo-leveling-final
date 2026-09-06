package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class LevelBarProcedure {
   public static String execute(Entity entity) {
      return entity == null ? "" : Math.round(entity instanceof Player _plr ? _plr.experienceLevel : 0.0F) + "";
   }
}
