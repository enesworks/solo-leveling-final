package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class ShopRefreshButtonProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).daily_refreshes
            > 0.0) {
            ItemStack _setval = new ItemStack(
               ForgeRegistries.ITEMS
                  .tags()
                  .getTag(ItemTags.create(new ResourceLocation("forge:shop_items")))
                  .getRandomElement(RandomSource.create())
                  .orElseGet(() -> Items.AIR)
            );
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.shopitem1 = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = new ItemStack(
               ForgeRegistries.ITEMS
                  .tags()
                  .getTag(ItemTags.create(new ResourceLocation("forge:shop_items")))
                  .getRandomElement(RandomSource.create())
                  .orElseGet(() -> Items.AIR)
            );
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.shopitem2 = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = new ItemStack(
               ForgeRegistries.ITEMS
                  .tags()
                  .getTag(ItemTags.create(new ResourceLocation("forge:shop_items")))
                  .getRandomElement(RandomSource.create())
                  .orElseGet(() -> Items.AIR)
            );
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.shopitem3 = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = new ItemStack(
               ForgeRegistries.ITEMS
                  .tags()
                  .getTag(ItemTags.create(new ResourceLocation("forge:shop_items")))
                  .getRandomElement(RandomSource.create())
                  .orElseGet(() -> Items.AIR)
            );
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.shopitem4 = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = new ItemStack(
               ForgeRegistries.ITEMS
                  .tags()
                  .getTag(ItemTags.create(new ResourceLocation("forge:shop_items")))
                  .getRandomElement(RandomSource.create())
                  .orElseGet(() -> Items.AIR)
            );
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.shopitem5 = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = new ItemStack(
               ForgeRegistries.ITEMS
                  .tags()
                  .getTag(ItemTags.create(new ResourceLocation("forge:shop_items")))
                  .getRandomElement(RandomSource.create())
                  .orElseGet(() -> Items.AIR)
            );
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.shopitem6 = _setval;
               capability.syncPlayerVariables(entity);
            });
            double _setvalx = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .daily_refreshes
               - 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.daily_refreshes = _setval;
               capability.syncPlayerVariables(entity);
            });
         }
      }
   }
}
