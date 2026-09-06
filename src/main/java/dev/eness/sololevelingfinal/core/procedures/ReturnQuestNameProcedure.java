package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

public class ReturnQuestNameProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else if (!JobChangeQuestManager.isDungeonActive(entity)
         && !JobChangeQuestManager.isAdvancementActive(entity)
         && !JobChangeQuestManager.isSelectionPending(entity)
         && !JobChangeQuestManager.isShadowPresentation(entity)) {
         return !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .MainQuest
               .equals("")
            ? entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MainQuest
            : entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MainQuest;
      } else {
         return "Job Change Quest";
      }
   }
}
