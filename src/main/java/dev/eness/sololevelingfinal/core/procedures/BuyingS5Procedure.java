package dev.eness.sololevelingfinal.core.procedures;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class BuyingS5Procedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         double slot = 0.0;
         slot = 4.0;
         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.MYTHIC_DAGGER.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 3675.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.MYTHIC_DAGGER.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 3675.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.E_TIER_SWORD.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 200.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.E_TIER_SWORD.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 200.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.EMERALD_DAGGER.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 3400.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.EMERALD_DAGGER.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 3400.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.D_TIER_SWORD.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 250.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.D_TIER_SWORD.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 250.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.C_TIER_SWORD.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 300.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.C_TIER_SWORD.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 300.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.B_TIER_SWORD.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 400.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.B_TIER_SWORD.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 400.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.A_TIER_SWORD.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 600.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.A_TIER_SWORD.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 600.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.S_TIER_SWORD.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 800.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.S_TIER_SWORD.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 800.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.KATANA_STIER.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 2000.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.KATANA_STIER.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 2000.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.HAMMER.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 2140.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.HAMMER.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 2140.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.WAR_AXE.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 2140.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.WAR_AXE.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 2140.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.SWORD_CURVED_D.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 540.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.SWORD_CURVED_D.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 540.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.SWORD_WARRIOR_D.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 480.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.SWORD_WARRIOR_D.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 480.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.DAGGER_KNIGHT_D.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 505.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.DAGGER_KNIGHT_D.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 505.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.DAGGER_KARAMBIT_E.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 380.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.DAGGER_KARAMBIT_E.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 380.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.SWORD_CURVED_D.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 460.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.SWORD_CURVED_D.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 460.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.SWORD_TWINWING_C.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 590.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.SWORD_TWINWING_C.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 590.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.DAGGER_CHAIN_C.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 600.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.DAGGER_CHAIN_C.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 600.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.SWORD_ENRICHED_B.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 720.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.SWORD_ENRICHED_B.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 720.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.SWORD_NATURE_B.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 700.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.SWORD_NATURE_B.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 700.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.DAGGER_GOLDEN_B.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 735.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.DAGGER_GOLDEN_B.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 735.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.DAGGER_HEAT_A.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 965.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.DAGGER_HEAT_A.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 965.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.DAGGER_DUOLITY_A.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 940.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.DAGGER_DUOLITY_A.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 940.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.KNIGHT_KILLER.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 3000.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.KNIGHT_KILLER.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 3000.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if ((entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt
                     ? ((Slot)_slt.get((int)slot)).getItem()
                     : ItemStack.EMPTY)
                  .getItem()
               == SololevelingModItems.GRAVITY_DAGGER.get()
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).golds
               >= 2840.0) {
            if (entity instanceof Player _player) {
               ItemStack _setstack = new ItemStack(SololevelingModItems.GRAVITY_DAGGER.get());
               _setstack.setCount(1);
               ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }

            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .golds
               - 2840.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.golds = _setval;
               capability.syncPlayerVariables(entity);
            });
         }
      }
   }
}
