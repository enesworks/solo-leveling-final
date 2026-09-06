package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.SkillListHelper;

public class PlistReturnProcedure {
   public static String execute(Entity entity, int position) {
      return SkillListHelper.displaySkillAt(entity, position);
   }
}
