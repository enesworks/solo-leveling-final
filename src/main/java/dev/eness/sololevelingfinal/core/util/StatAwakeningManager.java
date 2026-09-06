package dev.eness.sololevelingfinal.core.util;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public final class StatAwakeningManager {
   private static final int WARN_LEVEL = 18;
   private static final int AWAKEN_LEVEL = 20;
   private static final double MIN_SCORE = 5.0;
   private static final String KEY_WARNED = "sl_awaken_warned";
   private static final String KEY_AWAKENED = "sl_awakened";
   private static final String KEY_EVALUATION_REMINDER = "sl_evaluation_reminder";
   private static final String[] CLASS_NAMES = new String[]{"Assassin", "Mage", "Fighter", "Tanker", "Healer", "Ranger"};
   private static final String[] CLASS_COLORS = new String[]{"#009DFF", "#BF5FFF", "#D8D8D8", "#3060FF", "#06FF00", "#FFBC00"};
   private static final int[] CLASS_ACCENTS = new int[]{-16736769, -4235265, -2565928, -13606657, -16318720, -17408};
   private static final String[][] STARTER_SKILLS = new String[][]{
      {"Ghost Step", "Night Rend"},
      {"Flame Weaving", "Ignition Orb"},
      {"Slash Dash", "Ground Slam"},
      {"Tank Leap", "Shield Bash"},
      {"Heal Beam", "Purification"},
      {"Back Step"}
   };

   private StatAwakeningManager() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.player instanceof ServerPlayer sp) {
            if (sp.tickCount % 100 == 0) {
               CompoundTag data = sp.getPersistentData();
               if (!data.getBoolean("sl_awakened")) {
                  SololevelingModVariables.PlayerVariables cap = sp.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables());
                  if (cap.Classes != 0.0) {
                     data.putBoolean("sl_awakened", true);
                  } else {
                     int level = (int)cap.Level;
                     if (level >= 18 && !data.getBoolean("sl_awaken_warned")) {
                        data.putBoolean("sl_awaken_warned", true);
                        sp.displayClientMessage(
                           Component.literal(
                              "§6§l⚠ Hunter Evaluation Available ⚠\n§eVisit a Hunter Evaluator to discover your rank and class.\n§7Rank is measured automatically; class results may be rerolled."
                           ),
                           false
                        );
                        SystemNotifications.showTitleUnder(
                           sp,
                           -18371,
                           110,
                           Component.literal("EVALUATION AVAILABLE").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                           Component.literal("Place your hand on an Evaluator crystal.").withStyle(ChatFormatting.GRAY)
                        );
                     }

                     if (level >= 20 && !data.getBoolean("sl_evaluation_reminder")) {
                        data.putBoolean("sl_evaluation_reminder", true);
                        sp.displayClientMessage(
                           Component.literal("§bThe Hunter Association is waiting to evaluate you. Right-click an Evaluator crystal when you are ready."),
                           false
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private static int computeClass(SololevelingModVariables.PlayerVariables cap) {
      double spd = cap.Speed;
      double str = cap.Strength;
      double vit = cap.Vitality;
      double intel = cap.Intelligence;
      double per = cap.perception;
      double[] scores = new double[]{spd + per, str + intel, spd + str, str + vit, vit + intel, per + vit};
      int best = 0;
      double bestScore = 5.0;

      for (int i = 0; i < scores.length; i++) {
         if (scores[i] > bestScore) {
            bestScore = scores[i];
            best = i + 1;
         }
      }

      return best;
   }

   private static void triggerAwakening(ServerPlayer sp, int classNum) {
      String name = CLASS_NAMES[classNum - 1];
      String color = CLASS_COLORS[classNum - 1];
      int accent = CLASS_ACCENTS[classNum - 1];
      sp.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(cap -> {
         cap.Classes = classNum;
         cap.syncPlayerVariables(sp);
      });
      grantStarterSkills(sp, classNum);
      if (classNum == 2) {
         name = MageSpellProgression.displayName(sp);
         boolean barrier = MageSpellProgression.isBarrierMage(sp);
         boolean arcane = MageSpellProgression.isArcaneMage(sp);
         boolean storm = MageSpellProgression.isStormMage(sp);
         color = barrier ? "aqua" : (arcane ? "light_purple" : (storm ? "yellow" : "red"));
         accent = barrier ? -10688257 : (arcane ? -7709441 : (storm ? -11174 : -42454));
      }

      SystemNotifications.showTitleUnder(
         sp,
         accent,
         140,
         Component.literal("STAT AWAKENING").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
         Component.literal("Your dominant stats awakened " + name + ".\nStarter skills have been unlocked.").withStyle(ChatFormatting.GRAY)
      );
      if (sp.getServer() != null && sp.level() instanceof ServerLevel sl) {
         CommandSourceStack css = new CommandSourceStack(
            CommandSource.NULL, sp.position(), sp.getRotationVector(), sl, 4, sp.getName().getString(), sp.getDisplayName(), sp.getServer(), sp
         );
         sp.getServer()
            .getCommands()
            .performPrefixedCommand(
               css, "/title " + sp.getName().getString() + " title {\"text\":\"\\u2726 AWAKENING \\u2726\",\"color\":\"gold\",\"bold\":true}"
            );
         sp.getServer()
            .getCommands()
            .performPrefixedCommand(
               css, "/title " + sp.getName().getString() + " subtitle {\"text\":\"" + name + "\",\"color\":\"" + color + "\",\"bold\":true,\"italic\":true}"
            );
      }

      sp.displayClientMessage(
         Component.literal(
            "§6§l⚡ Stat Awakening ⚡\n§fYour dominant stats have awakened you as a §e§l"
               + name
               + "§f!\n§7Starter skills have been added to your Plist.\n§7Seek formal §bEvaluation§7 to unlock higher-tier skills."
         ),
         false
      );
   }

   private static void grantStarterSkills(ServerPlayer sp, int classNum) {
      if (classNum == 2) {
         MageSpellProgression.assignRandomSpecialization(sp);
         MageSpellProgression.grantStarterSpells(sp);
      } else {
         String[] skills = STARTER_SKILLS[classNum - 1];

         for (String skill : skills) {
            addSkillIfMissing(sp, skill);
         }
      }
   }

   private static void addSkillIfMissing(ServerPlayer sp, String skill) {
      SololevelingModVariables.PlayerVariables cap = sp.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
      if (cap != null) {
         if (!cap.Plist.contains(skill)) {
            sp.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(c -> {
               c.Plist = c.Plist + skill + ",";
               c.syncPlayerVariables(sp);
            });
         }
      }
   }
}
