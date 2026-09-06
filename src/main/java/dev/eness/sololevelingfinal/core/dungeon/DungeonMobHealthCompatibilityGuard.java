package dev.eness.sololevelingfinal.core.dungeon;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;

@EventBusSubscriber
public final class DungeonMobHealthCompatibilityGuard {
   private static final String SANITIZED_TAG = "slr_dungeon_health_compat_sanitized";
   private static final double PATHOLOGICAL_VALUE = 1000000.0;
   private static final double GOBLIN_BASE_HEALTH = 16.0;

   private DungeonMobHealthCompatibilityGuard() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide() && isEligibleDungeonGoblin(event.getEntity())) {
         stabilize(event.getEntity());
         SololevelingMod.queueServerWork(1, () -> stabilize(event.getEntity()));
      }
   }

   public static void stabilize(Entity entity) {
      if (entity instanceof LivingEntity living && isGoblin(living) && isDungeonMob(living)) {
         AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
         if (maxHealth != null) {
            boolean changed = false;
            if (!Double.isFinite(maxHealth.getBaseValue()) || maxHealth.getBaseValue() >= 1000000.0) {
               maxHealth.setBaseValue(expectedBaseHealth(living));
               changed = true;
            }

            for (AttributeModifier modifier : List.copyOf(maxHealth.getModifiers())) {
               if (pathological(modifier)) {
                  maxHealth.removeModifier(modifier);
                  changed = true;
               }
            }

            if (!Double.isFinite(maxHealth.getValue()) || maxHealth.getValue() >= 1000000.0) {
               for (AttributeModifier modifier : List.copyOf(maxHealth.getModifiers())) {
                  String modifierName = modifier.getName();
                  if (modifierName == null || !modifierName.startsWith("SLR ")) {
                     maxHealth.removeModifier(modifier);
                     changed = true;
                     if (Double.isFinite(maxHealth.getValue()) && maxHealth.getValue() < 1000000.0) {
                        break;
                     }
                  }
               }
            }

            boolean healthOverflow = !Float.isFinite(living.getHealth()) || living.getHealth() > living.getMaxHealth();
            if (changed || healthOverflow) {
               float repairedHealth = Float.isFinite(living.getHealth()) ? Math.min(living.getHealth(), living.getMaxHealth()) : living.getMaxHealth();
               living.setHealth(repairedHealth);
               if (living.getHealth() <= 0.0F && living.isAlive()) {
                  living.setHealth(living.getMaxHealth());
               }

               living.getPersistentData().putBoolean("slr_dungeon_health_compat_sanitized", true);
               if (changed) {
                  ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType());
                  SololevelingMod.LOGGER.warn("Removed a pathological max-health value from dungeon mob {} ({})", entityId, living.getUUID());
               }
            }
         }
      }
   }

   static boolean pathological(AttributeModifier modifier) {
      if (modifier != null && Double.isFinite(modifier.getAmount())) {
         return switch (modifier.getOperation()) {
            case ADDITION -> Math.abs(modifier.getAmount()) >= 1000000.0;
            case MULTIPLY_BASE, MULTIPLY_TOTAL -> Math.abs(modifier.getAmount()) >= 10000.0;
         };
      } else {
         return true;
      }
   }

   private static double expectedBaseHealth(LivingEntity living) {
      if (living.getPersistentData().getBoolean("slr_dungeon_spawned")) {
         return 16.0;
      }

      double level = Math.max(0.0, DungeonLevelHelper.levelOf(living));
      return 16.0 + Math.min(1000.0, level) * 0.4;
   }

   private static boolean isEligibleDungeonGoblin(Entity entity) {
      return isGoblin(entity) && isDungeonMob(entity);
   }

   private static boolean isGoblin(Entity entity) {
      return entity instanceof GoblinClubEntity || entity instanceof GoblinArcherEntity || entity instanceof GoblinMageEntity;
   }

   private static boolean isDungeonMob(Entity entity) {
      return entity.getPersistentData().getBoolean("slr_dungeon_spawned") || entity.getPersistentData().getBoolean("slr_procedural_dungeon");
   }
}
