package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryArmorSessionManager;

public class ShadowARMORHelmetTickEventProcedure {
   public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
      if (entity != null) {
         if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLAST_PROTECTION, itemstack) == 0) {
            itemstack.enchant(Enchantments.BLAST_PROTECTION, 2);
         }

         if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, itemstack) == 0) {
            itemstack.enchant(Enchantments.BINDING_CURSE, 10);
         }

         if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, itemstack) == 0) {
            itemstack.enchant(Enchantments.VANISHING_CURSE, 10);
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.SQUID_INK, entity.getX(), entity.getY(), entity.getZ(), 7, 0.15, 0.0, 0.15, 0.0);
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.MANA_PURPLE.get(), entity.getX(), entity.getY(), entity.getZ(), 4, 0.15, 0.0, 0.15, 0.0);
         }

         if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
            _entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 2));
         }

         double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .MP
            - 3.0;
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.MP = _setval;
            capability.syncPlayerVariables(entity);
         });
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP < 20.0) {
            Entity _entity = entity;
            if (_entity instanceof Player _player) {
               _player.getInventory()
                  .armor
                  .set(
                     0,
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overridefeet
                  );
               _player.getInventory().setChanged();
            } else if (_entity instanceof LivingEntity _living) {
               _living.setItemSlot(
                  EquipmentSlot.FEET,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overridefeet
               );
            }

            Entity _entityx = entity;
            if (_entityx instanceof Player _player) {
               _player.getInventory()
                  .armor
                  .set(
                     1,
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overridelegs
                  );
               _player.getInventory().setChanged();
            } else if (_entityx instanceof LivingEntity _living) {
               _living.setItemSlot(
                  EquipmentSlot.LEGS,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overridelegs
               );
            }

            Entity _entityxx = entity;
            if (_entityxx instanceof Player _player) {
               _player.getInventory()
                  .armor
                  .set(
                     2,
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overridetorso
                  );
               _player.getInventory().setChanged();
            } else if (_entityxx instanceof LivingEntity _living) {
               _living.setItemSlot(
                  EquipmentSlot.CHEST,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overridetorso
               );
            }

            Entity _entityxxx = entity;
            if (_entityxxx instanceof Player _player) {
               _player.getInventory()
                  .armor
                  .set(
                     3,
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overridehead
                  );
               _player.getInventory().setChanged();
            } else if (_entityxxx instanceof LivingEntity _living) {
               _living.setItemSlot(
                  EquipmentSlot.HEAD,
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).overridehead
               );
            }

            TemporaryArmorSessionManager.finishAfterRestore(entity);
         }
      }
   }
}
