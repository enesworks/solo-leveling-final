package dev.eness.sololevelingfinal.core.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class TemporaryStatBonusManager {
   public static final ResourceLocation EFFECT_PROVIDER = new ResourceLocation("sololeveling", "active_effects");
   public static final ResourceLocation EQUIPMENT_PROVIDER = new ResourceLocation("sololeveling", "equipment_sets");
   public static final ResourceLocation HASTE_BUFF_SOURCE = new ResourceLocation("sololeveling", "haste_buff");
   public static final ResourceLocation PHYSICAL_BUFF_SOURCE = new ResourceLocation("sololeveling", "physical_buff");
   public static final ResourceLocation TWO_AS_ONE_SOURCE = new ResourceLocation("sololeveling", "two_as_one");
   public static final ResourceLocation MANA_SENSITIVITY_SOURCE = new ResourceLocation("sololeveling", "mana_sensitivity");
   public static final ResourceLocation DEMONIC_ATTUNEMENT_SOURCE = new ResourceLocation("sololeveling", "demonic_attunement");
   public static final ResourceLocation AVARICIOUS_INSIGHT_SOURCE = new ResourceLocation("sololeveling", "avaricious_insight");
   public static final ResourceLocation TEMPEST_AUTHORITY_SOURCE = new ResourceLocation("sololeveling", "tempest_authority");
   public static final double HASTE_BUFF_AGILITY_BONUS = 30.0;
   public static final double PHYSICAL_BUFF_STRENGTH_BONUS = 30.0;
   public static final double TWO_AS_ONE_FLAT_BONUS = 20.0;
   public static final double TWO_AS_ONE_PERCENT_BONUS = 0.2;
   public static final double MANA_SENSITIVITY_STRENGTH_FLAT_BONUS = 10.0;
   public static final double MANA_SENSITIVITY_STRENGTH_PERCENT_BONUS = 0.1;
   public static final double MANA_SENSITIVITY_INTELLIGENCE_FLAT_BONUS = 10.0;
   public static final double MANA_SENSITIVITY_INTELLIGENCE_PERCENT_BONUS = 0.1;
   public static final double DEMONIC_ATTUNEMENT_FLAT_BONUS = 10.0;
   public static final double DEMONIC_ATTUNEMENT_PERCENT_BONUS = 0.1;
   public static final double AVARICIOUS_INSIGHT_FLAT_BONUS = 10.0;
   public static final double AVARICIOUS_INSIGHT_PERCENT_BONUS = 0.1;
   public static final double TEMPEST_AUTHORITY_FLAT_BONUS = 10.0;
   public static final double TEMPEST_AUTHORITY_PERCENT_BONUS = 0.1;
   private static final List<TemporaryStatBonusManager.ProviderEntry> PROVIDERS = new CopyOnWriteArrayList<>();

   private TemporaryStatBonusManager() {
   }

   public static synchronized void registerProvider(ResourceLocation id, TemporaryStatBonusManager.BonusProvider provider) {
      Objects.requireNonNull(id, "id");
      Objects.requireNonNull(provider, "provider");
      if (PROVIDERS.stream().anyMatch(entry -> entry.id().equals(id))) {
         throw new IllegalStateException("Temporary stat provider is already registered: " + id);
      }

      PROVIDERS.add(new TemporaryStatBonusManager.ProviderEntry(id, provider));
   }

   public static double baseValue(Entity entity, TemporaryStatBonusManager.Stat stat) {
      return entity != null && stat != null ? entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(variables -> {
         return switch (stat) {
            case STRENGTH -> variables.Strength;
            case AGILITY -> variables.Speed;
            case PERCEPTION -> variables.perception;
            case VITALITY -> variables.Vitality;
            case INTELLIGENCE -> variables.Intelligence;
         };
      }).orElse(0.0) : 0.0;
   }

   public static List<TemporaryStatBonusManager.BonusSource> sources(Entity entity, TemporaryStatBonusManager.Stat stat) {
      if (entity != null && stat != null) {
         double baseValue = baseValue(entity, stat);
         Map<ResourceLocation, TemporaryStatBonusManager.BonusSource> combined = new LinkedHashMap<>();
         TemporaryStatBonusManager.BonusSink sink = source -> {
            if (source != null && Double.isFinite(source.amount()) && !(source.amount() <= 1.0E-6)) {
               combined.merge(
                  source.id(),
                  source,
                  (left, right) -> new TemporaryStatBonusManager.BonusSource(left.id(), left.displayName(), left.amount() + right.amount())
               );
            }
         };

         for (TemporaryStatBonusManager.ProviderEntry entry : PROVIDERS) {
            entry.provider().collect(entity, stat, baseValue, sink);
         }

         return List.copyOf(new ArrayList<>(combined.values()));
      } else {
         return List.of();
      }
   }

   public static double bonusValue(Entity entity, TemporaryStatBonusManager.Stat stat) {
      return sources(entity, stat).stream().mapToDouble(TemporaryStatBonusManager.BonusSource::amount).sum();
   }

   public static double effectiveValue(Entity entity, TemporaryStatBonusManager.Stat stat) {
      return baseValue(entity, stat) + bonusValue(entity, stat);
   }

   public static double effectiveStrength(Entity entity) {
      return effectiveValue(entity, TemporaryStatBonusManager.Stat.STRENGTH);
   }

   public static double effectiveAgility(Entity entity) {
      return effectiveValue(entity, TemporaryStatBonusManager.Stat.AGILITY);
   }

   public static double effectivePerception(Entity entity) {
      return effectiveValue(entity, TemporaryStatBonusManager.Stat.PERCEPTION);
   }

   public static double effectiveVitality(Entity entity) {
      return effectiveValue(entity, TemporaryStatBonusManager.Stat.VITALITY);
   }

   public static double effectiveIntelligence(Entity entity) {
      return effectiveValue(entity, TemporaryStatBonusManager.Stat.INTELLIGENCE);
   }

   public static boolean isTwoAsOneActive(Entity entity) {
      return entity instanceof LivingEntity living
         && living.getMainHandItem().is(SololevelingModItems.DEMON_KINGS_DAGGER.get())
         && living.getOffhandItem().is(SololevelingModItems.DEMON_KINGS_DAGGER.get());
   }

   public static String format(double value) {
      return Math.abs(value - Math.rint(value)) < 1.0E-6 ? Long.toString(Math.round(value)) : new DecimalFormat("0.##").format(value);
   }

   private static void collectEffectBonuses(Entity entity, TemporaryStatBonusManager.Stat stat, double baseValue, TemporaryStatBonusManager.BonusSink sink) {
      if (entity instanceof LivingEntity living) {
         if (stat == TemporaryStatBonusManager.Stat.AGILITY) {
            MobEffectInstance haste = living.getEffect(SololevelingModMobEffects.HASTE_BUFF.get());
            if (haste != null) {
               sink.add(HASTE_BUFF_SOURCE, Component.literal("Haste Buff effect"), 30.0);
            }
         }

         if (stat == TemporaryStatBonusManager.Stat.STRENGTH) {
            MobEffectInstance physical = living.getEffect(SololevelingModMobEffects.PHYSICAL_BUFF.get());
            if (physical != null) {
               sink.add(PHYSICAL_BUFF_SOURCE, Component.literal("Physical Buff effect"), 30.0);
            }
         }
      }
   }

   private static void collectEquipmentBonuses(Entity entity, TemporaryStatBonusManager.Stat stat, double baseValue, TemporaryStatBonusManager.BonusSink sink) {
      if (entity instanceof LivingEntity living) {
         if (stat == TemporaryStatBonusManager.Stat.STRENGTH) {
            if (isTwoAsOneActive(living)) {
               sink.add(TWO_AS_ONE_SOURCE, Component.literal("Two as One"), scaledBonus(baseValue, 20.0, 0.2));
            }

            int kamishFangs = kamishWrathCount(living);
            if (kamishFangs > 0) {
               double permanentIntelligence = baseValue(entity, TemporaryStatBonusManager.Stat.INTELLIGENCE);
               sink.add(MANA_SENSITIVITY_SOURCE, Component.literal("Mana Sensitivity"), kamishFangs * manaSensitivityBonus(baseValue, permanentIntelligence));
            }
         } else if (stat == TemporaryStatBonusManager.Stat.INTELLIGENCE) {
            if (isHeld(living, SololevelingModItems.DEMON_KINGS_LONG_SWORD.get())) {
               sink.add(DEMONIC_ATTUNEMENT_SOURCE, Component.literal("Demonic Attunement"), scaledBonus(baseValue, 10.0, 0.1));
            }

            if (isHeld(living, SololevelingModItems.ORB_OF_AVARICE.get())) {
               sink.add(AVARICIOUS_INSIGHT_SOURCE, Component.literal("Avaricious Insight"), scaledBonus(baseValue, 10.0, 0.1));
            }

            if (isHeld(living, SololevelingModItems.STORM_GRIAMORE.get())) {
               sink.add(TEMPEST_AUTHORITY_SOURCE, Component.literal("Tempest Authority"), scaledBonus(baseValue, 10.0, 0.1));
            }
         }
      }
   }

   private static int kamishWrathCount(LivingEntity living) {
      int count = isKamishWrath(living.getMainHandItem().getItem()) ? 1 : 0;
      return count + (isKamishWrath(living.getOffhandItem().getItem()) ? 1 : 0);
   }

   private static boolean isKamishWrath(Item item) {
      return item == SololevelingModItems.KAMISH_WRATH.get() || item == SololevelingModItems.KAMISH_WRATH_2.get();
   }

   private static boolean isHeld(LivingEntity living, Item item) {
      return living.getMainHandItem().is(item) || living.getOffhandItem().is(item);
   }

   private static double scaledBonus(double baseValue, double flatBonus, double percentBonus) {
      return flatBonus + Math.floor(Math.max(0.0, baseValue) * percentBonus);
   }

   private static double manaSensitivityBonus(double permanentStrength, double permanentIntelligence) {
      return scaledBonus(permanentStrength, 10.0, 0.1) + scaledBonus(permanentIntelligence, 10.0, 0.1);
   }

   static {
      registerProvider(EFFECT_PROVIDER, TemporaryStatBonusManager::collectEffectBonuses);
      registerProvider(EQUIPMENT_PROVIDER, TemporaryStatBonusManager::collectEquipmentBonuses);
   }

   @FunctionalInterface
   public interface BonusProvider {
      void collect(Entity var1, TemporaryStatBonusManager.Stat var2, double var3, TemporaryStatBonusManager.BonusSink var5);
   }

   @FunctionalInterface
   public interface BonusSink {
      void add(TemporaryStatBonusManager.BonusSource var1);

      default void add(ResourceLocation id, Component displayName, double amount) {
         this.add(new TemporaryStatBonusManager.BonusSource(id, displayName, amount));
      }
   }

   public record BonusSource(ResourceLocation id, Component displayName, double amount) {
      public BonusSource {
         Objects.requireNonNull(id, "id");
         Objects.requireNonNull(displayName, "displayName");
         if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Temporary stat bonus must be finite");
         }
      }
   }

   private record ProviderEntry(ResourceLocation id, TemporaryStatBonusManager.BonusProvider provider) {
   }

   public enum Stat {
      STRENGTH("Strength"),
      AGILITY("Agility"),
      PERCEPTION("Perception"),
      VITALITY("Vitality"),
      INTELLIGENCE("Intelligence");

      private final String displayName;

      Stat(String displayName) {
         this.displayName = displayName;
      }

      public String displayName() {
         return this.displayName;
      }
   }
}
