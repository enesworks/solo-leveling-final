package dev.eness.sololevelingfinal.core.procedures;

import io.netty.buffer.Unpooled;
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
import dev.eness.sololevelingfinal.core.world.inventory.EquippedAbilitiesMenu;

public class OpenAbilitiesListProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof Player _player) {
            _player.closeContainer();
         }

         if (entity instanceof ServerPlayer _ent) {
            final BlockPos _bpos = BlockPos.containing(x, y, z);
            final int initialPage = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .map(capability -> capability.PslotSelecting >= 9.0 && capability.PslotSelecting <= 16.0 ? 2 : 1)
               .orElse(1);
            NetworkHooks.openScreen(_ent, new MenuProvider() {
               @Override
               public Component getDisplayName() {
                  return Component.literal("EquippedAbilities");
               }

               @Override
               public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                  return new EquippedAbilitiesMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos).writeVarInt(initialPage));
               }
            }, data -> data.writeBlockPos(_bpos).writeVarInt(initialPage));
         }
      }
   }
}
