package dev.eness.sololevelingfinal.core.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.world.dimension.rift.RiftTerritory;

public class MonarchTerritoryGateSpawnEggItem extends ForgeSpawnEggItem {
   private final RiftTerritory territory;

   public MonarchTerritoryGateSpawnEggItem(RiftTerritory territory, int backgroundColor, int highlightColor) {
      super(SololevelingModEntities.RED_GATE, backgroundColor, highlightColor, new Properties());
      this.territory = territory;
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      if (context.getLevel() instanceof ServerLevel serverLevel) {
         ItemStack stack = context.getItemInHand();
         BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
         Entity spawned = SololevelingModEntities.RED_GATE.get().spawn(serverLevel, stack, context.getPlayer(), spawnPos, MobSpawnType.SPAWN_EGG, true, false);
         if (spawned == null) {
            return InteractionResult.FAIL;
         }

         spawned.getPersistentData().putString("slr_red_gate_territory", this.territory.id());
         if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            stack.shrink(1);
         }

         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("Territory: " + this.territory.displayName()).withStyle(ChatFormatting.DARK_RED));
      tooltip.add(Component.literal(this.levelRangeText()).withStyle(ChatFormatting.GRAY));
      tooltip.add(Component.literal("Wave-based red gate encounter.").withStyle(ChatFormatting.DARK_GRAY));
   }

   private String levelRangeText() {
      return this.territory == RiftTerritory.FROST ? "Starter red gate: level 0-10." : "Monarch red gate: scales to the party.";
   }
}
