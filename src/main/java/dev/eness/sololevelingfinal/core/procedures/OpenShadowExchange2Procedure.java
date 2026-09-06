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
import dev.eness.sololevelingfinal.core.util.ShadowExchangeManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;
import dev.eness.sololevelingfinal.core.world.inventory.ShadowExchangeSETMenu;

public class OpenShadowExchange2Procedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (ShadowExchangeManager.hasAnchor(entity, 1)) {
            if (entity instanceof ServerPlayer _ent) {
               final BlockPos _bpos = BlockPos.containing(x, y, z);
               NetworkHooks.openScreen(_ent, new MenuProvider() {
                  @Override
                  public Component getDisplayName() {
                     return Component.literal("ShadowExchangeSET");
                  }

                  @Override
                  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                     return new ShadowExchangeSETMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                  }
               }, _bpos);
            }
         } else if (entity instanceof ServerPlayer player) {
            SystemNotifications.showNegativeTitleUnder(
               player,
               -49859,
               80,
               Component.literal("NO ANCHORS").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
               Component.literal("Set a Shadow Exchange anchor first.").withStyle(ChatFormatting.RED)
            );
         }
      }
   }
}
