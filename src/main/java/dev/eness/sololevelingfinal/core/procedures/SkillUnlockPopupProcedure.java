package dev.eness.sololevelingfinal.core.procedures;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

@EventBusSubscriber
public class SkillUnlockPopupProcedure {
   private static final String KEY_READY = "sl_skill_unlock_popup_ready";
   private static final String KEY_PLIST = "sl_skill_unlock_popup_plist";
   private static final int ACCENT = -7713281;

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.player instanceof ServerPlayer player) {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .ifPresent(capability -> checkForNewSkills(player, capability.Plist == null ? "" : capability.Plist));
         }
      }
   }

   private static void checkForNewSkills(ServerPlayer player, String currentPlist) {
      CompoundTag data = player.getPersistentData();
      Set<String> currentSkills = parseSkills(currentPlist);
      String normalizedCurrent = normalizeSkills(currentSkills);
      if (!data.getBoolean("sl_skill_unlock_popup_ready")) {
         data.putBoolean("sl_skill_unlock_popup_ready", true);
         data.putString("sl_skill_unlock_popup_plist", normalizedCurrent);
      } else {
         String previousPlist = data.getString("sl_skill_unlock_popup_plist");
         Set<String> previousSkills = parseSkills(previousPlist);
         String normalizedPrevious = normalizeSkills(previousSkills);
         if (normalizedCurrent.equals(normalizedPrevious)) {
            if (!previousPlist.equals(normalizedCurrent)) {
               data.putString("sl_skill_unlock_popup_plist", normalizedCurrent);
            }
         } else {
            for (String skill : currentSkills) {
               if (!previousSkills.contains(skill) && !ShadowMonarchManager.isFormationSkill(skill)) {
                  showSkillPopup(player, skill);
               }
            }

            data.putString("sl_skill_unlock_popup_plist", normalizedCurrent);
         }
      }
   }

   private static Set<String> parseSkills(String plist) {
      Set<String> skills = new LinkedHashSet<>();

      for (String entry : plist.split(",")) {
         String skill = normalizeSkill(entry);
         if (!isPlaceholderSkill(skill)) {
            skills.add(skill);
         }
      }

      return skills;
   }

   private static String normalizeSkill(String entry) {
      String skill = entry == null ? "" : entry.trim();
      if (skill.startsWith(".")) {
         skill = skill.substring(1).trim();
      }

      return skill;
   }

   private static String normalizeSkills(Set<String> skills) {
      return String.join(",", skills);
   }

   private static boolean isPlaceholderSkill(String skill) {
      return skill.isEmpty() || ".".equals(skill) || "\"\"".equals(skill) || "empty".equalsIgnoreCase(skill) || "null".equalsIgnoreCase(skill);
   }

   private static void showSkillPopup(ServerPlayer player, String skill) {
      SystemNotifications.showTitleUnder(
         player,
         -7713281,
         90,
         Component.literal("NEW SKILL").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
         Component.literal(skill).withStyle(ChatFormatting.AQUA)
      );
   }
}
