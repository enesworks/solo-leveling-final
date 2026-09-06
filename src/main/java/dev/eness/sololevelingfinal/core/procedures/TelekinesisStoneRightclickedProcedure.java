package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.RulersAuthorityManager;

public class TelekinesisStoneRightclickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
      if (entity instanceof Player player && !player.level().isClientSide()) {
         if (RulersAuthorityManager.hasAuthority(player)) {
            player.displayClientMessage(Component.literal("You already have \"Ruler's Authority\""), true);
         } else {
            SololevelingModVariables.PlayerVariables variables = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
            if (variables != null) {
               String abilities = variables.abilities == null ? "" : variables.abilities.replace('"', ' ').trim();
               variables.abilities = abilities.isEmpty() ? "telekinesis" : abilities + " telekinesis";
               variables.syncPlayerVariables(player);
               if (!player.isCreative()) {
                  itemstack.shrink(1);
               }

               if (world instanceof Level level) {
                  level.playSound((Player)null, BlockPos.containing(x, y, z), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
               }

               player.displayClientMessage(Component.literal("Learned \"Ruler's Authority\""), true);
            }
         }
      }
   }
}
