package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcRunSavedData;
import dev.eness.sololevelingfinal.core.item.RedkeyItem;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.DkcQuestManager;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

public class DemonKingsCastleKeyUseProcedure {
   public static void execute(LevelAccessor world, Entity entity, ItemStack stack) {
      if (entity instanceof ServerPlayer player && player.server != null) {
         if (DkcFloorRegistry.isDkc(player.level())) {
            DKCPathTeleportProcedure.returnToSavedOverworld(player);
         } else if (!player.level().dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(Component.literal("§4The Demon King's Castle Key only answers in the Overworld."), true);
         } else if (!DKCCombatTrackerProcedure.canEnterCastle(player)) {
            DKCCombatTrackerProcedure.sendCombatBlockedMessage(player);
         } else {
            PointSetProcedure.execute(player);
            DkcRunSavedData runs = DkcRunSavedData.get(player.server);
            runs.getOrCreate(player);
            SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables());
            if (vars.dkc_cleared >= 20.0) {
               player.displayClientMessage(Component.literal("§5The Demon King's Castle is already conquered. Its gates stay silent."), true);
            } else if (vars.dkc_started || !(vars.dkc_cleared <= 0.0) || stack != null && !stack.isEmpty() && stack.getItem() instanceof RedkeyItem) {
               DkcQuestManager.unlock(player);
               if (!vars.dkc_started && !(vars.dkc_cleared > 0.0)) {
                  player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.dkc_started = true;
                     capability.dkc_cleared = Math.max(0.0, capability.dkc_cleared);
                     capability.syncPlayerVariables(player);
                  });
                  VesselProgressionManager.reconcileEntitlements(player);
                  player.getPersistentData().putDouble("dkc_current_floor", 0.0);
                  player.getPersistentData().putBoolean("dkc_floor_just_changed", true);
                  if (world instanceof Level level) {
                     level.playSound((Player)null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.85F, 0.55F);
                     level.playSound((Player)null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.9F, 0.65F);
                  }

                  player.displayClientMessage(Component.literal("§4§lThe first seal cracks open. §5Floor 1 awaits."), false);
                  player.displayClientMessage(Component.literal("§8The Demon King's Castle has marked a path for you."), true);
                  player.serverLevel()
                     .sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 42, 0.75, 0.75, 0.75, 0.035);
                  player.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.8, player.getZ(), 24, 0.55, 0.45, 0.55, 0.02);
                  consumeKey(player, stack);
                  DKCPathTeleportProcedure.execute(player, 1);
               } else {
                  if (!vars.dkc_started) {
                     player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.dkc_started = true;
                        capability.syncPlayerVariables(player);
                     });
                     VesselProgressionManager.reconcileEntitlements(player);
                  }

                  player.displayClientMessage(Component.literal("§5The castle has already accepted your blood."), true);
                  consumeKey(player, stack);
                  DKCPathTeleportProcedure.execute(player, 1);
               }
            } else {
               player.displayClientMessage(Component.literal("§5The first seal requires the Demon King's Castle Key."), true);
            }
         }
      }
   }

   private static void consumeKey(ServerPlayer player, ItemStack stack) {
      if (player != null && stack != null && !stack.isEmpty() && stack.getItem() instanceof RedkeyItem && !player.getAbilities().instabuild) {
         stack.shrink(1);
      }
   }
}
