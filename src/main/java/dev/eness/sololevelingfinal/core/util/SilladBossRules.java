package dev.eness.sololevelingfinal.core.util;

public final class SilladBossRules {
   public static final int PHASE_ONE = 1;
   public static final int PHASE_TWO = 2;
   public static final int PHASE_THREE = 3;
   public static final float PHASE_TWO_HEALTH_FRACTION = 0.7F;
   public static final float PHASE_THREE_HEALTH_FRACTION = 0.4F;
   public static final int MAX_ENGAGED_PLAYERS = 4;
   public static final float ADDITIONAL_PLAYER_MITIGATION = 0.45F;
   public static final float ADDITIONAL_PLAYER_DAMAGE = 0.05F;
   public static final int MAX_FROSTBITE = 5;
   public static final int FROSTBITE_LIFETIME = 120;
   public static final int PLAYER_ROOT_TICKS = 12;
   public static final int NON_PLAYER_ROOT_TICKS = 50;
   public static final int FREEZE_IMMUNITY_TICKS = 120;
   public static final int BRITTLE_DELAY_TICKS = 20;
   public static final int BRITTLE_DURATION_TICKS = 40;
   public static final int EXECUTION_WINDOW_TICKS = 200;
   public static final int PRISON_DURATION_TICKS = 480;
   public static final float PRISON_HIT_CAP = 16.0F;
   public static final float PRISON_REGEN_FRACTION = 0.06F;
   public static final float PRISON_INTEGRITY_PER_RANK = 24.0F;
   public static final int FROZEN_DOMAIN_COLUMN_BUDGET = 48;
   public static final float MAX_SINGLE_HIT = 125.0F;

   private SilladBossRules() {
   }

   public static int phaseForHealth(float health, float maximumHealth) {
      if (Float.isFinite(maximumHealth) && !(maximumHealth <= 0.0F) && Float.isFinite(health)) {
         float fraction = Math.max(0.0F, Math.min(maximumHealth, health)) / maximumHealth;
         if (fraction <= 0.4F) {
            return 3;
         } else {
            return fraction <= 0.7F ? 2 : 1;
         }
      } else {
         return 1;
      }
   }

   public static float incomingDamageMultiplier(int engagedPlayers) {
      int additional = clampEngagedPlayers(engagedPlayers) - 1;
      return 1.0F / (1.0F + 0.45F * additional);
   }

   public static float outgoingDamageMultiplier(int engagedPlayers) {
      int additional = clampEngagedPlayers(engagedPlayers) - 1;
      return 1.0F + 0.05F * additional;
   }

   public static int clampEngagedPlayers(int engagedPlayers) {
      return Math.max(1, Math.min(4, engagedPlayers));
   }

   public static int clampFrostbite(int stacks) {
      return Math.max(0, Math.min(5, stacks));
   }

   public static int frostbiteCap(boolean freezeImmune) {
      return freezeImmune ? 4 : 5;
   }

   public static int addFrostbite(int current, int amount, boolean freezeImmune) {
      long safeGain = Math.max(0, amount);
      long total = clampFrostbite(current) + safeGain;
      return (int)Math.min(frostbiteCap(freezeImmune), total);
   }

   public static float clampIncomingHit(float amount) {
      return Float.isFinite(amount) && !(amount <= 0.0F) ? Math.min(125.0F, amount) : 0.0F;
   }

   public static float prisonIntegrity(int engagedPlayers, float targetMaximumHealth) {
      return prisonIntegrity(engagedPlayers, targetMaximumHealth, 0);
   }

   public static float prisonIntegrity(int engagedPlayers, float targetMaximumHealth, int shadowRank) {
      float health = Float.isFinite(targetMaximumHealth) ? Math.max(0.0F, targetMaximumHealth) : 0.0F;
      int rank = Math.max(0, Math.min(6, shadowRank));
      return 72.0F + 12.0F * (clampEngagedPlayers(engagedPlayers) - 1) + Math.min(30.0F, health * 0.04F) + 24.0F * rank;
   }

   public static float prisonRegeneration(float maximumIntegrity) {
      return Float.isFinite(maximumIntegrity) && !(maximumIntegrity <= 0.0F) ? maximumIntegrity * 0.06F : 0.0F;
   }

   public static float prisonDot(float targetMaximumHealth, int engagedPlayers) {
      float health = Float.isFinite(targetMaximumHealth) ? Math.max(0.0F, targetMaximumHealth) : 0.0F;
      float base = Math.max(3.0F, Math.min(8.0F, 2.0F + health * 0.015F));
      return base * outgoingDamageMultiplier(engagedPlayers);
   }

   public static double prisonManaDrain(double maximumMana, int ownedPrisons) {
      if (ownedPrisons <= 0) {
         return 0.0;
      }

      double mana = Double.isFinite(maximumMana) ? Math.max(0.0, maximumMana) : 0.0;
      double base = Math.max(12.0, Math.min(30.0, Math.ceil(mana * 0.0075)));
      return Math.min(48.0, base + 3.0 * Math.max(0, ownedPrisons - 1));
   }

   public static float prisonerAttackDamage(double attackDamage) {
      double safeAttack = Double.isFinite(attackDamage) ? Math.max(0.0, attackDamage) : 0.0;
      return (float)Math.max(4.0, Math.min(10.0, 3.5 + safeAttack * 0.15));
   }

   public static int frozenDomainRadius(int phase) {
      return switch (Math.max(1, Math.min(3, phase))) {
         case 2 -> 23;
         case 3 -> 30;
         default -> 16;
      };
   }

   public static int frozenDomainPulseInterval(int phase) {
      return switch (Math.max(1, Math.min(3, phase))) {
         case 2 -> 30;
         case 3 -> 22;
         default -> 40;
      };
   }

   public static float trueFrostExecutionDamage(float targetMaximumHealth, boolean player, int phase, int engagedPlayers) {
      if (Float.isFinite(targetMaximumHealth) && !(targetMaximumHealth <= 0.0F)) {
         float health = targetMaximumHealth;
         if (player) {
            float amount = 18.0F + 2.0F * Math.max(0, Math.min(3, phase) - 1);
            amount *= outgoingDamageMultiplier(engagedPlayers);
            return Math.min(26.0F, amount);
         } else {
            float amount = 24.0F + Math.min(48.0F, health * 0.1F);
            amount *= 1.0F + 0.1F * Math.max(0, Math.min(3, phase) - 1);
            return Math.min(86.0F, amount * outgoingDamageMultiplier(engagedPlayers));
         }
      } else {
         return 0.0F;
      }
   }

   public enum Action {
      IDLE,
      PHASE_TRANSITION,
      FROST_CLEAVE,
      ICE_SPEAR,
      FLASH_FREEZE,
      FROZEN_PATH,
      FROST_COUNTER,
      STILLNESS_DECREE,
      SPIRE_CAGE,
      WHITEOUT_PROCESSION,
      WINTER_REMEMBERS,
      CROWN_OF_WINTER,
      ABSOLUTE_ZERO,
      GLACIAL_EXECUTION,
      FROST_STEP;
   }
}
