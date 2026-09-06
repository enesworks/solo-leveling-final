package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.item.GriamoreItem;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class CyclePressedProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Player) {
            if (!entity.isShiftKeyDown()) {
               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Skillcycle
                  < 10.0) {
                  double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .Skillcycle
                     + 1.0;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Skillcycle = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal(" "), true);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
                        == 2.0
                     && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GRIAMORE.get()
                     && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof GriamoreItem) {
                     (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getOrCreateTag().putString("geckoAnim", "pageup");
                  }
               } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Skillcycle
                  == 10.0) {
                  double _setval = 1.0;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.Skillcycle = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  if (entity instanceof Player _player && !_player.level().isClientSide()) {
                     _player.displayClientMessage(Component.literal(" "), true);
                  }

                  if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
                        == 2.0
                     && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GRIAMORE.get()
                     && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof GriamoreItem) {
                     (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getOrCreateTag().putString("geckoAnim", "pageup");
                  }
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Skillcycle
               > 1.0) {
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Skillcycle
                  - 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.Skillcycle = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal(" "), true);
               }

               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
                     == 2.0
                  && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GRIAMORE.get()
                  && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof GriamoreItem) {
                  (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getOrCreateTag().putString("geckoAnim", "pagedown");
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Skillcycle
               == 1.0) {
               double _setval = 10.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.Skillcycle = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal(" "), true);
               }

               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
                     == 2.0
                  && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GRIAMORE.get()
                  && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof GriamoreItem) {
                  (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getOrCreateTag().putString("geckoAnim", "pagedown");
               }
            }
         }
      }
   }
}
