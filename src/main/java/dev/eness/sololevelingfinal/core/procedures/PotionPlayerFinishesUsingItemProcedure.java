package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class PotionPlayerFinishesUsingItemProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
      if (entity != null) {
         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.drink")),
                  SoundSource.NEUTRAL,
                  1.0F,
                  1.0F
               );
            } else {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.drink")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
               );
            }
         }

         if (itemstack.getItem() == SololevelingModItems.LARGE_FATIGUE_POTION.get()) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Fatigue
               - 1000.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Fatigue = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.MEDIUM_FATIGUE_POTION.get()) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Fatigue
               - 500.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Fatigue = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.SMALL_FATIGUE_POTION.get()) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Fatigue
               - 100.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Fatigue = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.LARGE_MANA_POTION.get()) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .MP
               + 10000.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.MEDIUM_MANA_POTION.get()) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .MP
               + 5000.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.SMALL_MANA_POTION.get()) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .MP
               + 1000.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.LARGE_HEALTH_POTION.get()) {
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) + 50.0F);
            }

            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.MEDIUM_HEALTH_POTION.get()) {
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) + 20.0F);
            }

            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.SMALL_HEALTH_POTION.get()) {
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F) + 5.0F);
            }

            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }

         if (itemstack.getItem() == SololevelingModItems.HOLY_WATER_OF_LIFE.get()) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables())
               .Mana;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = 0.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Fatigue = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }

            if (entity instanceof Player _player) {
               ItemStack _stktoremove = itemstack;
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }
         }
      }
   }
}
