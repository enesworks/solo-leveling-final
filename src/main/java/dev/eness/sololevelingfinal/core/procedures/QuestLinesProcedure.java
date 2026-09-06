package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

public class QuestLinesProcedure {
   public static String execute(Entity entity) {
      if (entity == null) {
         return "";
      } else if (JobChangeQuestManager.isDungeonActive(entity)) {
         return "Defeat Igris the Blood-Red.";
      } else if (JobChangeQuestManager.isAdvancementActive(entity)) {
         return "Defeat summoned knights: " + JobChangeQuestManager.advancementPoints(entity) + "/" + JobChangeQuestManager.requiredPoints(entity);
      } else if (JobChangeQuestManager.isSelectionPending(entity)) {
         return "Select a Ruler or Monarch vessel.";
      } else if (JobChangeQuestManager.isShadowPresentation(entity)) {
         return "Job assignment in progress...";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
            .MainQuest
            .equals("Getting Stronger")
         && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).QuestProgression
            == 0.0) {
         return "Obtain the instance dungeon key";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
            .MainQuest
            .equals("Getting Stronger")
         && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).QuestProgression
            == 1.0) {
         return "Enter the instance dungeon";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
            .MainQuest
            .equals("Getting Stronger")
         && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).QuestProgression
            == 2.0) {
         return "Complete the instance dungeon!";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
            .MainQuest
            .equals("Something Is Weird")
         && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).QuestProgression
            == 0.0) {
         return "Work in progress...";
      } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
            .MainQuest
            .equals("Something Is Weird")
         && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).QuestProgression
            == 1.0) {
         return "Work in progress...";
      } else {
         return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .MainQuest
                  .equals("Something Is Weird")
               && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).QuestProgression
                  == 2.0
            ? "Work in progress..."
            : "";
      }
   }
}
