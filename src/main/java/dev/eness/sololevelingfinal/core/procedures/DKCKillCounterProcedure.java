package dev.eness.sololevelingfinal.core.procedures;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcRadiruManager;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import dev.eness.sololevelingfinal.core.entity.DemonEntity;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

public class DKCKillCounterProcedure {
   public static void execute(LevelAccessor world, Entity entity, Entity sourceEntity) {
      if (world instanceof ServerLevel level && (entity instanceof DemonEntity || entity instanceof DemonKnightEntity)) {
         CompoundTag enemyData = entity.getPersistentData();
         if ("floor_wave".equals(enemyData.getString("dkc_encounter_role"))) {
            int floor = (int)enemyData.getDouble("dkc_floor_number");
            if (floor >= 2 && floor <= 19) {
               String ownerText = enemyData.getString("dkc_spawned_by");

               ServerPlayer player;
               UUID owner;
               try {
                  owner = UUID.fromString(ownerText);
                  player = level.getServer().getPlayerList().getPlayer(owner);
               } catch (IllegalArgumentException exception) {
                  return;
               }

               if (player != null && DkcSpatialLayout.isPlayerInFloor(player, floor) && DkcSpatialLayout.isEntityInOwnedFloor(entity, owner, floor)) {
                  ServerPlayer creditedPlayer = ShadowKillCreditHelper.creditedServerPlayer(world, sourceEntity);
                  if (creditedPlayer != null && creditedPlayer.getUUID().equals(owner)) {
                     double alreadyCleared = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .dkc_cleared;
                     if (!(alreadyCleared >= floor)) {
                        CompoundTag data = player.getPersistentData();
                        String prefix = "dkc_floor_" + floor;
                        if (enemyData.getInt("dkc_wave_attempt") == DKCDemonSpawnerProcedure.currentAttempt(player, floor)) {
                           int required = DkcFloorRegistry.requiredKills(floor);
                           if (required > 0 && !data.getBoolean(prefix + "_spawning") && !data.getBoolean(prefix + "_complete")) {
                              int kills = Math.min(required, (int)data.getDouble(prefix + "_killed") + 1);
                              data.putDouble(prefix + "_killed", kills);
                              if (kills >= required) {
                                 data.putBoolean(prefix + "_complete", true);
                                 DKCDemonSpawnerProcedure.discardOwnedWave(level, player, floor);
                                 DKCDemonSpawnerProcedure.invalidateAttempt(player, floor);
                                 if (floor == 10) {
                                    DkcFloorBuilder.ensureBosses(player, floor);
                                 } else if (floor == 15) {
                                    DkcRadiruManager.onDefendersOverpowered(level, player);
                                 } else {
                                    player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                       capability.dkc_cleared = Math.max(capability.dkc_cleared, floor);
                                       capability.syncPlayerVariables(player);
                                    });
                                    VesselProgressionManager.reconcileEntitlements(player);
                                    XPGainProcedure.awardBaseXp(world, player, floor * 100);
                                    givePermit(level, player);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void givePermit(ServerLevel level, ServerPlayer player) {
      ItemStack permit = new ItemStack(SololevelingModItems.ENTRY_PERMIT.get());
      if (!player.getInventory().add(permit)) {
         level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), permit));
      }
   }
}
