package dev.eness.sololevelingfinal.core.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class TemporaryArmorSessionManager {
   public static final String GENERATION_TAG = "slr_temporary_armor_generation";
   public static final String ACTIVE_ESCROW_TAG = "slr_temporary_armor_escrow_active";
   public static final String EQUIPPED_ESCROW_TAG = "slr_temporary_armor_escrow_equipped";

   private TemporaryArmorSessionManager() {
   }

   public static long begin(Entity entity) {
      long generation = advance(entity);
      if (generation > 0L) {
         entity.getPersistentData().putBoolean("slr_temporary_armor_escrow_active", true);
         entity.getPersistentData().remove("slr_temporary_armor_escrow_equipped");
      }

      return generation;
   }

   public static void invalidatePendingEquip(Entity entity) {
      advance(entity);
   }

   public static void finishAfterRestore(Entity entity) {
      advance(entity);
      if (entity != null) {
         entity.getPersistentData().remove("slr_temporary_armor_escrow_active");
         entity.getPersistentData().remove("slr_temporary_armor_escrow_equipped");
      }
   }

   public static void abandonIfCurrent(Entity entity, long generation) {
      if (entity != null && generation > 0L && entity.getPersistentData().getLong("slr_temporary_armor_generation") == generation) {
         entity.getPersistentData().remove("slr_temporary_armor_escrow_active");
         entity.getPersistentData().remove("slr_temporary_armor_escrow_equipped");
      }
   }

   public static boolean hasActiveEscrow(Entity entity) {
      return entity != null && entity.getPersistentData().getBoolean("slr_temporary_armor_escrow_active");
   }

   public static boolean hasEquippedEscrow(Entity entity) {
      return hasActiveEscrow(entity) && entity.getPersistentData().getBoolean("slr_temporary_armor_escrow_equipped");
   }

   public static void markEquipped(Entity entity, long generation) {
      if (entity != null && generation > 0L && entity.getPersistentData().getLong("slr_temporary_armor_generation") == generation && hasActiveEscrow(entity)) {
         entity.getPersistentData().putBoolean("slr_temporary_armor_escrow_equipped", true);
      }
   }

   public static boolean canEquipShadow(Entity entity, long generation) {
      return canEquip(entity, generation, 1);
   }

   public static boolean canEquipGoliath(Entity entity, long generation) {
      return canEquip(entity, generation, 5);
   }

   public static void endForVesselChange(Entity entity) {
      if (entity instanceof LivingEntity living && !entity.level().isClientSide() && hasActiveEscrow(entity)) {
         boolean restoreArmor = hasEquippedEscrow(entity);
         invalidatePendingEquip(entity);
         if (restoreArmor) {
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
               living.setItemSlot(EquipmentSlot.HEAD, variables.overridehead.copy());
               living.setItemSlot(EquipmentSlot.CHEST, variables.overridetorso.copy());
               living.setItemSlot(EquipmentSlot.LEGS, variables.overridelegs.copy());
               living.setItemSlot(EquipmentSlot.FEET, variables.overridefeet.copy());
            });
         }

         finishAfterRestore(entity);
      }
   }

   private static boolean canEquip(Entity entity, long generation, int requiredJob) {
      if (entity instanceof LivingEntity living
         && !entity.isRemoved()
         && !entity.level().isClientSide()
         && generation > 0L
         && entity.getPersistentData().getLong("slr_temporary_armor_generation") == generation
         && hasActiveEscrow(entity)) {
         SololevelingModVariables.PlayerVariables variables = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         boolean hasRequiredJob = variables.JOB == requiredJob;
         return hasRequiredJob
            && ItemStack.matches(living.getItemBySlot(EquipmentSlot.HEAD), variables.overridehead)
            && ItemStack.matches(living.getItemBySlot(EquipmentSlot.CHEST), variables.overridetorso)
            && ItemStack.matches(living.getItemBySlot(EquipmentSlot.LEGS), variables.overridelegs)
            && ItemStack.matches(living.getItemBySlot(EquipmentSlot.FEET), variables.overridefeet);
      } else {
         return false;
      }
   }

   private static long advance(Entity entity) {
      if (entity != null && !entity.level().isClientSide()) {
         long current = entity.getPersistentData().getLong("slr_temporary_armor_generation");
         long next = current == Long.MAX_VALUE ? 1L : Math.max(1L, current + 1L);
         entity.getPersistentData().putLong("slr_temporary_armor_generation", next);
         return next;
      } else {
         return -1L;
      }
   }
}
