package dev.eness.sololevelingfinal.core.procedures;

import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.NetworkHooks;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.RewardManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;
import dev.eness.sololevelingfinal.core.world.inventory.RewardPanelMenu;

public class RewardScreenOpenProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Player) {
            if (entity instanceof ServerPlayer serverPlayer) {
               RewardManager.reconcileFullRecoveryRewards(serverPlayer);
            }

            if (RewardManager.hasRewards(entity)) {
               if (entity instanceof ServerPlayer _ent) {
                  final BlockPos _bpos = BlockPos.containing(x, y, z);
                  NetworkHooks.openScreen(_ent, new MenuProvider() {
                     @Override
                     public Component getDisplayName() {
                        return Component.literal("RewardPanel");
                     }

                     @Override
                     public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                        return new RewardPanelMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                     }
                  }, _bpos);
               }
            } else {
               if (entity instanceof Player _player) {
                  _player.closeContainer();
               }

               if (entity instanceof ServerPlayer player) {
                  SystemNotifications.showTitleUnder(
                     player,
                     -18371,
                     70,
                     Component.literal("NO REWARDS").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                     Component.literal("There are no rewards to collect.").withStyle(ChatFormatting.GRAY)
                  );
               }
            }
         }
      }
   }
}
