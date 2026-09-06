package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public class HunterRightClickedOnEntityProcedure {
   public static void execute(Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (sourceentity instanceof Player player) {
            String hunterRank = entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "";
            boolean canRecruit = false;
            switch (hunterRank) {
               case "S":
                  canRecruit = hasAndConsumeItems(player, SololevelingModItems.SHADOW_ARMOR_BOOTS.get(), 2);
                  break;
               case "A":
                  canRecruit = hasAndConsumeItems(player, SololevelingModItems.SHADOW_ARMOR_BOOTS.get(), 1);
                  break;
               case "B":
                  canRecruit = hasAndConsumeItems(player, SololevelingModItems.MANA_CRYSTAL_A.get(), 5);
                  break;
               case "C":
                  canRecruit = hasAndConsumeItems(player, SololevelingModItems.MANA_CRYSTAL_A.get(), 3);
                  break;
               case "D":
                  canRecruit = hasAndConsumeItems(player, SololevelingModItems.MANA_CRYSTAL_B.get(), 10);
            }

            if (canRecruit && entity instanceof TamableAnimal _toTame) {
               _toTame.tame(player);
            } else if (!canRecruit) {
            }
         }
      }
   }

   private static boolean hasAndConsumeItems(Player player, Item item, int count) {
      Inventory inventory = player.getInventory();
      int foundCount = 0;

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         if (stack.getItem() == item) {
            foundCount += stack.getCount();
            if (foundCount >= count) {
               break;
            }
         }
      }

      if (foundCount < count) {
         return false;
      }

      int remaining = count;

      for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
         ItemStack stack = inventory.getItem(i);
         if (stack.getItem() == item) {
            int toRemove = Math.min(stack.getCount(), remaining);
            stack.shrink(toRemove);
            remaining -= toRemove;
         }
      }

      return true;
   }
}
