package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.MageSpellProgression;

public class MasterylvlupMageProcedure {
   public static void execute(Entity entity) {
      MageSpellProgression.grantMasterySkill(entity);
   }
}
