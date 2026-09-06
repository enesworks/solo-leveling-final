package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.RulersAuthorityManager;

public class MasterylvlupassassinProcedure {
   private static final List<String> MASTERY_SKILLS = List.of("Flash Cut", "Ghost Step", "Night Rend", "Dualwield", "Dagger Throw");

   public static void execute(Entity entity) {
      if (entity != null) {
         String current = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables())
            .Plist;
         if (current == null) {
            current = ".";
         }

         List<String> missing = new ArrayList<>();

         for (String skill : MASTERY_SKILLS) {
            if (!containsSkill(current, skill)) {
               missing.add(skill);
            }
         }

         if (RulersAuthorityManager.hasAuthority(entity) && containsSkill(current, "Dagger Throw") && !containsSkill(current, "Dagger Rush")) {
            missing.add("Dagger Rush");
         }

         if (!missing.isEmpty()) {
            String unlocked = missing.get(entity.level().random.nextInt(missing.size()));
            String updated = current + unlocked + ",";
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Plist = updated;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player player && !player.level().isClientSide()) {
               player.displayClientMessage(Component.literal("Gained skill: " + unlocked), false);
            }
         }
      }
   }

   private static boolean containsSkill(String encoded, String skill) {
      if (encoded != null && !encoded.isBlank()) {
         for (String entry : encoded.split(",")) {
            if (entry.replaceFirst("^\\.", "").trim().equalsIgnoreCase(skill)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
