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
import dev.eness.sololevelingfinal.core.util.SystemNotifications;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeSaveMenu;

public class OpenShadowExchange1Procedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).dungeoning
            )
          {
            if (entity instanceof ServerPlayer _ent) {
               final BlockPos _bpos = BlockPos.containing(x, y, z);
               NetworkHooks.openScreen(_ent, new MenuProvider() {
                  @Override
                  public Component getDisplayName() {
                     return Component.literal("ShadowExchangeSave");
                  }

                  @Override
                  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                     return new ShadowExchangeSaveMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                  }
               }, _bpos);
            }
         } else if (entity instanceof ServerPlayer player) {
            SystemNotifications.showNegativeTitleUnder(
               player,
               -49859,
               80,
               Component.literal("EXCHANGE LOCKED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
               Component.literal("You cannot set an anchor in this dimension.").withStyle(ChatFormatting.RED)
            );
         }
      }
   }
}
