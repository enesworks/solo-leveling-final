package dev.eness.sololevelingfinal.core.guild;

import java.util.UUID;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class HunterRecruitManager {
   private static final String[] CLASSES = new String[]{"Assassin", "Fighter", "Tanker", "Mage", "Ranger", "Healer"};
   private static final String[] NAMES_FIRST = new String[]{
      "Jin",
      "Kai",
      "Ryu",
      "Hana",
      "Sora",
      "Yuki",
      "Toma",
      "Nara",
      "Eren",
      "Mika",
      "Leon",
      "Zara",
      "Kira",
      "Dash",
      "Nova",
      "Axel",
      "Rio",
      "Vex",
      "Colt",
      "Sera",
      "Orion",
      "Lyra",
      "Blaze",
      "Storm",
      "Kael",
      "Vera",
      "Rex",
      "Mira",
      "Zion",
      "Aria",
      "Dusk",
      "Flint"
   };
   private static final String[] NAMES_LAST = new String[]{
      "Park",
      "Yun",
      "Cho",
      "Han",
      "Kim",
      "Lee",
      "Kang",
      "Cross",
      "Vale",
      "Stone",
      "Frost",
      "Ember",
      "Ash",
      "Ward",
      "Swift",
      "Iron",
      "Black",
      "Grey",
      "Silver",
      "Steele",
      "Quinn",
      "Drake",
      "Voss",
      "Chen",
      "Moon"
   };

   public static void fillPool(GuildData guild) {
      guild.recruitPool.clear();
      int size = Math.min(3 + guild.level, 10);

      for (int i = 0; i < size; i++) {
         guild.recruitPool.add(generate(guild.level));
      }
   }

   public static GuildHunter generate(int guildLevel) {
      RandomSource rng = RandomSource.create();
      String rank = rollRank(guildLevel, rng);
      String cls = CLASSES[Mth.nextInt(rng, 0, CLASSES.length - 1)];
      String firstName = NAMES_FIRST[Mth.nextInt(rng, 0, NAMES_FIRST.length - 1)];
      String lastName = NAMES_LAST[Mth.nextInt(rng, 0, NAMES_LAST.length - 1)];
      return new GuildHunter(UUID.randomUUID(), firstName + " " + lastName, rank, cls);
   }

   private static String rollRank(int guildLevel, RandomSource rng) {
      double roll = rng.nextDouble();
      int lv = Math.max(0, Math.min(guildLevel - 1, 9));
      double sChance = 0.01 + lv * 0.003;
      double aChance = sChance + 0.03 + lv * 0.005;
      double bChance = aChance + 0.08 + lv * 0.008;
      double cChance = bChance + 0.18 + lv * 0.01;
      double dChance = cChance + 0.3 - lv * 0.01;
      if (roll < sChance) {
         return "S";
      } else if (roll < aChance) {
         return "A";
      } else if (roll < bChance) {
         return "B";
      } else if (roll < cChance) {
         return "C";
      } else {
         return roll < dChance ? "D" : "E";
      }
   }
}
