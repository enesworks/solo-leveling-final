package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public final class TankerStarterpackRightclickedProcedure {
   public static final String REDEEMED_TAG = "slr_tanker_starter_redeemed_v1";

   private TankerStarterpackRightclickedProcedure() {
   }

   public static boolean execute(ServerPlayer player, ItemStack stack) {
      if (player == null || stack == null || stack.isEmpty() || stack.getItem() != SololevelingModItems.TANKER_STARTERPACK.get()) {
         return false;
      } else if (!TankerProgressionHelper.isTanker(player)) {
         player.displayClientMessage(Component.translatable("message.sololeveling.tanker.starter.wrong_class"), true);
         return false;
      } else {
         CompoundTag persisted = persistedData(player);
         if (persisted.getBoolean("slr_tanker_starter_redeemed_v1")) {
            player.displayClientMessage(Component.translatable("message.sololeveling.tanker.starter.already_redeemed"), true);
            return false;
         } else {
            TankerProgressionHelper.reconcileRankEntitlements(player);
            persisted.putBoolean("slr_tanker_starter_redeemed_v1", true);
            stack.shrink(1);
            giveOrDrop(player, new ItemStack(Items.SHIELD));
            giveOrDrop(player, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            giveOrDrop(player, new ItemStack(Items.BREAD, 16));
            player.displayClientMessage(Component.translatable("message.sololeveling.tanker.starter.redeemed"), false);
            return true;
         }
      }
   }

   private static void giveOrDrop(ServerPlayer player, ItemStack reward) {
      if (!player.addItem(reward)) {
         player.drop(reward, false);
      }
   }

   private static CompoundTag persistedData(Player player) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         root.put("PlayerPersisted", new CompoundTag());
      }

      return root.getCompound("PlayerPersisted");
   }
}
