package dev.eness.sololevelingfinal.core.procedures;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.world.inventory.AbilitiesGUIMenu;
import dev.eness.sololevelingfinal.core.world.inventory.PanelRework2Menu;

public class OpenPanelOnKeyPressedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Player) {
            if (entity instanceof ServerPlayer _ent) {
               final BlockPos _bpos = BlockPos.containing(x, y, z);
               NetworkHooks.openScreen(_ent, new MenuProvider() {
                  @Override
                  public Component getDisplayName() {
                     return Component.literal("PanelRework2");
                  }

                  @Override
                  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                     return new PanelRework2Menu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                  }
               }, _bpos);
            }

            if (world instanceof Level _level && _level.isClientSide()) {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:panelopen")), SoundSource.NEUTRAL, 0.5F, 1.0F, false
               );
            }
         } else if (entity instanceof ServerPlayer _ent) {
            final BlockPos _bpos = BlockPos.containing(x, y, z);
            NetworkHooks.openScreen(_ent, new MenuProvider() {
               @Override
               public Component getDisplayName() {
                  return Component.literal("AbilitiesGUI");
               }

               @Override
               public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                  return new AbilitiesGUIMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
               }
            }, _bpos);
         }
      }
   }
}
