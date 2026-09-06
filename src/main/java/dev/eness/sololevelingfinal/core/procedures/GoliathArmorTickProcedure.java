package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryArmorSessionManager;

public class GoliathArmorTickProcedure {
   public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
      if (entity != null) {
         if (!entity.level().isClientSide()) {
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLAST_PROTECTION, itemstack) == 0) {
               itemstack.enchant(Enchantments.BLAST_PROTECTION, 2);
            }

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, itemstack) == 0) {
               itemstack.enchant(Enchantments.BINDING_CURSE, 10);
            }

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, itemstack) == 0) {
               itemstack.enchant(Enchantments.VANISHING_CURSE, 10);
            }

            long gameTime = entity.level().getGameTime();
            if (entity.getPersistentData().getLong("goliath_last_armor_tick") != gameTime) {
               entity.getPersistentData().putLong("goliath_last_armor_tick", gameTime);
               if (entity instanceof LivingEntity living && !living.level().isClientSide()) {
                  living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 2));
               }

               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.MP -= 3.0;
                  capability.syncPlayerVariables(entity);
                  if (capability.MP < 20.0) {
                     restoreArmor(entity, capability);
                  }
               });
            }
         }
      }
   }

   private static void restoreArmor(Entity entity, SololevelingModVariables.PlayerVariables capability) {
      set(entity, EquipmentSlot.FEET, capability.overridefeet);
      set(entity, EquipmentSlot.LEGS, capability.overridelegs);
      set(entity, EquipmentSlot.CHEST, capability.overridetorso);
      set(entity, EquipmentSlot.HEAD, capability.overridehead);
      TemporaryArmorSessionManager.finishAfterRestore(entity);
   }

   private static void set(Entity entity, EquipmentSlot slot, ItemStack stack) {
      if (entity instanceof Player player) {
         player.getInventory().armor.set(slot.getIndex(), stack.copy());
         player.getInventory().setChanged();
      } else if (entity instanceof LivingEntity living) {
         living.setItemSlot(slot, stack.copy());
      }
   }
}
