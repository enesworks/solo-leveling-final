package dev.eness.sololevelingfinal.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.common.ForgeSpawnEggItem;
import dev.eness.sololevelingfinal.core.dungeon.DatapackGateSelectionService;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataManager;
import dev.eness.sololevelingfinal.core.entity.DatapackGateEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class DatapackGateSpawnEggItem extends ForgeSpawnEggItem {
   public DatapackGateSpawnEggItem(int backgroundColor, int highlightColor) {
      super(SololevelingModEntities.DATAPACK_GATE, backgroundColor, highlightColor, new Properties());
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      if (context.getLevel() instanceof ServerLevel serverLevel) {
         if (DungeonDataManager.dungeonIds().isEmpty()) {
            if (context.getPlayer() != null) {
               context.getPlayer().displayClientMessage(Component.literal("No valid datapack dungeons are loaded."), true);
            }

            return InteractionResult.FAIL;
         } else {
            ItemStack stack = context.getItemInHand();
            BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
            if (SololevelingModEntities.DATAPACK_GATE.get().spawn(serverLevel, stack, context.getPlayer(), spawnPos, MobSpawnType.SPAWN_EGG, true, false) instanceof DatapackGateEntity gate
               )
             {
               if (context.getPlayer() instanceof ServerPlayer player) {
                  DatapackGateSelectionService.requestOpen(player, gate);
               }

               if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                  stack.shrink(1);
               }

               return InteractionResult.CONSUME;
            } else {
               return InteractionResult.FAIL;
            }
         }
      } else {
         return InteractionResult.SUCCESS;
      }
   }
}
