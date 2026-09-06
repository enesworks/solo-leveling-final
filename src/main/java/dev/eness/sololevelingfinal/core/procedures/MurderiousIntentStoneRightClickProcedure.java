package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class MurderiousIntentStoneRightClickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Player) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
               == 1.0) {
               if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .abilities
                  .contains("murderiousintent")) {
                  if (entity instanceof Player _player) {
                     ItemStack _stktoremove = new ItemStack(SololevelingModItems.MURDERIOUS_INTENT_STONE.get());
                     _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
                  }

                  String _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .abilities
                     + " murderiousintent";
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.abilities = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.enchantment_table.use")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x,
                           y,
                           z,
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.enchantment_table.use")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F,
                           false
                        );
                     }
                  }

                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal("Learned \"Murderious Intent\""), true);
                  }
               } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("You already have \"Murderious Intent\""), true);
               }
            } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("You need to be an \"Assassin\" build to consume this ability"), true);
            }
         } else {
            if (entity instanceof Player _player) {
               ItemStack _stktoremove = new ItemStack(SololevelingModItems.MURDERIOUS_INTENT_STONE.get());
               _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
            }

            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("Error?!"), true);
            }
         }
      }
   }
}
