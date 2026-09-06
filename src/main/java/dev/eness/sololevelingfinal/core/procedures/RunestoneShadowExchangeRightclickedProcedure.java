package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobSkillManager;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

public class RunestoneShadowExchangeRightclickedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
      if (entity != null) {
         if (!(world instanceof Level level && level.isClientSide())) {
            if (!VesselProgressionManager.isShadowMonarch(entity)) {
               if (entity instanceof Player player) {
                  player.displayClientMessage(Component.literal("Only the Shadow Monarch can use the Shadow Exchange Stone."), true);
               }
            } else if (DoesHaveExchangeProcedure.execute(entity)) {
               if (entity instanceof Player player && !player.level().isClientSide()) {
                  player.displayClientMessage(Component.literal("You already have \"Shadow Exchange\""), true);
               }
            } else if (VesselProgressionManager.canUseShadowExchangeRunestone(entity)) {
               if (entity instanceof ServerPlayer player) {
                  Advancement advancement = player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:shadow_exchange"));
                  if (advancement != null) {
                     AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);

                     for (String criteria : progress.getRemainingCriteria()) {
                        player.getAdvancements().award(advancement, criteria);
                     }
                  }
               }

               if (entity instanceof Player player) {
                  ItemStack stackToRemove = itemstack;
                  player.getInventory().clearOrCountMatchingItems(stack -> stackToRemove.getItem() == stack.getItem(), 1, player.inventoryMenu.getCraftSlots());
               }

               if (world instanceof Level level) {
                  level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.enchantment_table.use")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F
                  );
               }

               JobSkillManager.markRunestoneSkill(entity, "slr_runestone_skill_shadow_exchange");
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.ShadowExchange = true;
                  capability.syncPlayerVariables(entity);
               });
               JobSkillManager.syncJobSkills(entity);
            }
         }
      }
   }
}
