package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.item.RedkeyItem;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.InstanceDungeonKeyAccess;

public class RewardCollectProcedure {
   public static void execute(Entity entity, String reward_name) {
      if (entity != null && reward_name != null) {
         double rand = 0.0;
         String reward = "";
         String reward_list = "";
         String item = "";
         ItemStack itemtogive = ItemStack.EMPTY;
         reward_list = "SP5, SP10, SP15, SP20, FR, ITEMBOX, GOLD";
         reward = reward_name;
         if (reward.equals("FR")) {
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }

            double _setval = 0.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Fatigue = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Mana;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.MP = _setval;
               capability.syncPlayerVariables(entity);
            });
         }

         if (reward.equals("ITEMBOX")) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
                  .MainQuest
                  .equals("Getting Stronger")
               && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).QuestProgression
                  == 0.0) {
               boolean _setval = false;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.giftstatus = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof Player _player) {
                  InstanceDungeonKeyAccess.grantInitialKey(_player);
               }

               double _setvalx = 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.QuestProgression = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof Player _player) {
                  _player.closeContainer();
               }
            } else {
               rand = Mth.nextInt(RandomSource.create(), 1, 130);
               boolean _setval = false;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.giftstatus = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (rand == 1.0) {
                  if (entity instanceof Player _player) {
                     ItemStack _setstack = new ItemStack(SololevelingModItems.KATANA_STIER.get());
                     _setstack.setCount(1);
                     ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
                  }

                  if (entity instanceof Player _player) {
                     _player.closeContainer();
                  }
               } else if (rand == 2.0) {
                  if (entity instanceof Player _player) {
                     ItemStack _setstack = new ItemStack(SololevelingModItems.HAMMER.get());
                     _setstack.setCount(1);
                     ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
                  }

                  if (entity instanceof Player _player) {
                     _player.closeContainer();
                  }
               } else if (rand == 3.0) {
                  if (entity instanceof Player _player) {
                     ItemStack _setstack = new ItemStack(SololevelingModItems.HAMMER.get());
                     _setstack.setCount(1);
                     ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
                  }

                  if (entity instanceof Player _player) {
                     _player.closeContainer();
                  }
               } else if (rand == 4.0) {
                  if (entity instanceof Player _player) {
                     ItemStack _setstack = new ItemStack(SololevelingModItems.MYTHIC_DAGGER.get());
                     _setstack.setCount(1);
                     ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
                  }

                  if (entity instanceof Player _player) {
                     _player.closeContainer();
                  }
               } else if (entity instanceof Player _player) {
                  ItemStack _setstack = new ItemStack(
                     ForgeRegistries.ITEMS
                        .tags()
                        .getTag(ItemTags.create(new ResourceLocation("loot_items")))
                        .getRandomElement(RandomSource.create())
                        .orElseGet(() -> Items.AIR)
                  );
                  _setstack.setCount(1);
                  ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
               }
            }
         }

         if (reward.startsWith("SP")) {
            try {
               int amount = Integer.parseInt(reward.substring(2));
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .SkillPoints
                  + amount;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.SkillPoints = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } catch (NumberFormatException e) {
               return;
            }
         }

         if (reward.startsWith("GOLD")) {
            try {
               int amount = Integer.parseInt(reward.substring(4));
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .golds
                  + amount;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.golds = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } catch (NumberFormatException e) {
               return;
            }
         }

         if (reward.startsWith("XP")) {
            try {
               int amount = Integer.parseInt(reward.substring(2));
               if (entity instanceof Player player) {
                  XPGainProcedure.awardRewardXp(player, amount);
               }
            } catch (NumberFormatException e) {
               return;
            }
         }

         if (reward.startsWith("ITEM:")) {
            String itemResourceLocation = reward.substring(5);

            try {
               ResourceLocation itemLocation = new ResourceLocation(itemResourceLocation);
               Item itemm = ForgeRegistries.ITEMS.getValue(itemLocation);
               if (itemm == null || itemm == Items.AIR) {
                  System.err.println("[SoloLeveling] Invalid item reward: " + itemResourceLocation);
               } else if (entity instanceof Player _player) {
                  if (itemm == SololevelingModItems.INSTANCE_DUNGEON_KEY.get()) {
                     InstanceDungeonKeyAccess.grantInitialKey(_player);
                  } else {
                     ItemStack _setstack = new ItemStack(itemm);
                     _setstack.setCount(1);
                     if (itemm == SololevelingModItems.REDKEY.get()) {
                        RedkeyItem.bindOrVerifyOwner(_setstack, _player);
                     }

                     ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
                  }
               }
            } catch (Exception e) {
               System.err.println("[SoloLeveling] Failed to parse item reward: " + reward);
               e.printStackTrace();
            }
         }
      }
   }
}
