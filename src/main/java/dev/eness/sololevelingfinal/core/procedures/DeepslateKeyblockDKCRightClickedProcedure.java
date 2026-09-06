package dev.eness.sololevelingfinal.core.procedures;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

public class DeepslateKeyblockDKCRightClickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof ServerPlayer player && DkcFloorRegistry.isDkc(player.level())) {
            DkcFloorBuilder.handlePermit(player, BlockPos.containing(x, y, z));
         } else if (!DkcFloorRegistry.isDkc(world)) {
            boolean found = false;
            double sx = 0.0;
            double sy = 0.0;
            double sz = 0.0;
            if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.ENTRY_PERMIT.get()) {
               if (entity instanceof Player _player) {
                  ItemStack _stktoremove = new ItemStack(SololevelingModItems.ENTRY_PERMIT.get());
                  _player.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1, _player.inventoryMenu.getCraftSlots());
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F,
                        false
                     );
                  }
               }

               sx = -3.0;
               found = false;
               world.setBlock(BlockPos.containing(x, y, z), Blocks.AIR.defaultBlockState(), 3);

               for (int index0 = 0; index0 < 6; index0++) {
                  sy = -3.0;

                  for (int index1 = 0; index1 < 6; index1++) {
                     sz = -3.0;

                     for (int index2 = 0; index2 < 6; index2++) {
                        if (world.getBlockState(BlockPos.containing(x + sx, y + sy, z + sz)).getBlock() == SololevelingModBlocks.UNBREAKABLE_DEEPSLATE.get()) {
                           BlockPos _bp = BlockPos.containing(x + sx, y + sy, z + sz);
                           BlockState _bs = Blocks.AIR.defaultBlockState();
                           BlockState _bso = world.getBlockState(_bp);
                           UnmodifiableIterator var22 = _bso.getValues().entrySet().iterator();

                           while (var22.hasNext()) {
                              Entry<Property<?>, Comparable<?>> entry = (Entry<Property<?>, Comparable<?>>)var22.next();
                              Property _property = _bs.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
                              if (_property != null && _bs.getValue(_property) != null) {
                                 try {
                                    _bs = _bs.setValue(_property, entry.getValue());
                                 } catch (Exception var26) {
                                 }
                              }
                           }

                           world.setBlock(_bp, _bs, 3);
                        }

                        sz++;
                     }

                     sy++;
                  }

                  sx++;
               }
            } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("You need to use: §4Entry Permit"), true);
            }
         }
      }
   }
}
