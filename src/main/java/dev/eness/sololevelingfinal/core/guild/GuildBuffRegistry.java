package dev.eness.sololevelingfinal.core.guild;

import java.util.List;

public final class GuildBuffRegistry {
   public static final int NONE = 0;
   public static final int PHYSICAL_CONDITIONING = 1;
   public static final int ARCANE_CIRCULATION = 2;
   public static final int DEFENSIVE_FORMATION = 3;
   public static final int ASSASSIN_FOOTWORK = 4;
   public static final int HUNTERS_PRECISION = 5;
   public static final int COMBAT_RECOVERY = 6;
   public static final int MANA_EFFICIENCY = 7;
   public static final int BATTLE_RHYTHM = 8;
   public static final int SYSTEM_SPONSORSHIP = 9;
   private static final List<GuildBuffRegistry.GuildBuff> BUFFS = List.of(
      new GuildBuffRegistry.GuildBuff(1, "Physical Conditioning", 2, "+15% physical damage."),
      new GuildBuffRegistry.GuildBuff(2, "Arcane Circulation", 3, "+10% magic damage and +10% mana recovery speed."),
      new GuildBuffRegistry.GuildBuff(3, "Defensive Formation", 4, "+15% damage reduction while blocking. +8% damage reduction briefly after being hit."),
      new GuildBuffRegistry.GuildBuff(4, "Assassin Footwork", 5, "+12% movement speed in combat and 10% chance to dodge incoming damage."),
      new GuildBuffRegistry.GuildBuff(5, "Hunter's Precision", 6, "+15% critical damage and 5% chance for a precision critical hit."),
      new GuildBuffRegistry.GuildBuff(6, "Combat Recovery", 7, "+20% healing received and restores mana on kills."),
      new GuildBuffRegistry.GuildBuff(7, "Mana Efficiency", 8, "Refunds 25% of mana spent."),
      new GuildBuffRegistry.GuildBuff(8, "Battle Rhythm", 9, "+12% damage while chaining class-skill hits."),
      new GuildBuffRegistry.GuildBuff(9, "System Sponsorship", 10, "+15% XP gained from the System.")
   );

   private GuildBuffRegistry() {
   }

   public static List<GuildBuffRegistry.GuildBuff> all() {
      return BUFFS;
   }

   public static GuildBuffRegistry.GuildBuff byId(int id) {
      for (GuildBuffRegistry.GuildBuff buff : BUFFS) {
         if (buff.id() == id) {
            return buff;
         }
      }

      return null;
   }

   public static String displayName(int id) {
      GuildBuffRegistry.GuildBuff buff = byId(id);
      return buff == null ? "None" : buff.name();
   }

   public static int unlockLevel(int id) {
      GuildBuffRegistry.GuildBuff buff = byId(id);
      return buff == null ? 1 : buff.unlockLevel();
   }

   public static boolean isUnlocked(GuildData guild, int id) {
      return id == 0 || byId(id) != null && guild.level >= unlockLevel(id);
   }

   public static boolean isSlotUnlocked(GuildData guild, int slot) {
      return slot == 1 || slot == 2 && guild.level >= 10;
   }

   public record GuildBuff(int id, String name, int unlockLevel, String description) {
   }
}
