package dev.eness.sololevelingfinal.core.procedures;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcRadiruManager;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.CerberusEntity;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.VulcanEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.AriseExtractionRules;
import dev.eness.sololevelingfinal.core.util.BaranVictoryRewards;
import dev.eness.sololevelingfinal.core.util.RewardManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

public class DKCBossKillRewardProcedure {
   private static final String KAISEL_TARGET_LEVEL_TAG = "slr_dkc_kaisel_target_level";

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceEntity) {
      if (world instanceof ServerLevel level && entity != null && DkcFloorRegistry.isSharedDkc(level)) {
         boolean exactKaiselin = entity instanceof KaiselinEntity && entity.getType() == SololevelingModEntities.KAISELIN.get();
         if (entity instanceof CerberusEntity || entity instanceof VulcanEntity || entity instanceof BaranEntity || exactKaiselin) {
            CompoundTag bossData = entity.getPersistentData();
            int floor = (int)bossData.getDouble("dkc_floor_number");
            if (matchesBossFloor(entity, floor, exactKaiselin)) {
               String ownerText = bossData.getString("dkc_spawned_by");
               if (!ownerText.isBlank()) {
                  UUID owner;
                  try {
                     owner = UUID.fromString(ownerText);
                  } catch (IllegalArgumentException ignored) {
                     return;
                  }

                  ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
                  if (player != null && DkcSpatialLayout.isPlayerInFloor(player, floor) && DkcSpatialLayout.isEntityInOwnedFloor(entity, owner, floor)) {
                     ServerPlayer creditedPlayer = ShadowKillCreditHelper.creditedServerPlayer(world, sourceEntity);
                     if (creditedPlayer != null && creditedPlayer.getUUID().equals(player.getUUID())) {
                        double alreadyCleared = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .dkc_cleared;
                        if (!(alreadyCleared >= floor)) {
                           CompoundTag data = player.getPersistentData();
                           if (floor == 20) {
                              if (entity instanceof BaranEntity) {
                                 BaranVictoryRewards.grantIfNeeded(player);
                                 data.putBoolean("dkc_floor_20_baran_defeated", true);
                                 BaranSummonProcedure.discardBossAdds(level, player, floor);
                              }

                              if (exactKaiselin) {
                                 data.putBoolean("dkc_floor_20_kaiselin_defeated", true);
                                 double targetLevel = DungeonLevelHelper.levelOf(entity);
                                 if (targetLevel > 0.0) {
                                    data.putDouble("slr_dkc_kaisel_target_level", targetLevel);
                                 }
                              }

                              boolean baranDown = data.getBoolean("dkc_floor_20_baran_defeated");
                              boolean kaiselinDown = data.getBoolean("dkc_floor_20_kaiselin_defeated");
                              if (!baranDown || !kaiselinDown) {
                                 return;
                              }

                              ensureKaiselSoul(level, player);
                           }

                           String defeatedKey = "dkc_floor_" + floor + "_boss_defeated";
                           if (!data.getBoolean(defeatedKey)) {
                              data.putBoolean(defeatedKey, true);
                              ServerPlayer rewardPlayer = player;
                              rewardPlayer.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                 capability.dkc_cleared = Math.max(capability.dkc_cleared, floor);
                                 capability.syncPlayerVariables(rewardPlayer);
                              });
                              DkcFloorBuilder.ensurePermitPedestal(player, floor);
                              VesselProgressionManager.reconcileEntitlements(player);
                              XPGainProcedure.awardBaseXp(world, player, floor * 100);
                              notify(player, -11665528, "BOSS SLAIN", floor == 20 ? "Demon King's Castle conquered." : "Floor " + floor + " cleared.", 100);
                              grantBossRewards(level, player, floor);
                              if (floor == 20) {
                                 DkcRadiruManager.onCastleConquered(player);
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

   private static boolean matchesBossFloor(Entity entity, int floor, boolean exactKaiselin) {
      return floor == 1 && entity instanceof CerberusEntity
         || floor == 10 && entity instanceof VulcanEntity
         || floor == 20 && (entity instanceof BaranEntity || exactKaiselin);
   }

   private static void ensureKaiselSoul(ServerLevel level, ServerPlayer player) {
      CompoundTag data = player.getPersistentData();
      if (!data.getBoolean("dkc_floor_20_kaisel_soul_spawned")) {
         BlockPos ground = DkcFloorBuilder.bossPosition(player, 20);

         while (ground.getY() > level.getMinBuildHeight() + 1 && level.isEmptyBlock(ground.below())) {
            ground = ground.below();
         }

         while (ground.getY() < level.getMaxBuildHeight() - 2 && !level.isEmptyBlock(ground)) {
            ground = ground.above();
         }

         Entity soul = SololevelingModEntities.SHADOW_SOUL.get().spawn(level, ground, MobSpawnType.MOB_SUMMONED);
         if (soul != null) {
            soul.getPersistentData().putString("soultype", "kaisel");
            soul.getPersistentData()
               .putDouble(
                  "slr_arise_target_level",
                  data.getDouble("slr_dkc_kaisel_target_level") > 0.0
                     ? data.getDouble("slr_dkc_kaisel_target_level")
                     : AriseExtractionRules.defaultTargetLevel("kaisel")
               );
            soul.getPersistentData().putDouble("dkc_floor_number", 20.0);
            soul.getPersistentData().putString("dkc_spawned_by", player.getStringUUID());
            data.putBoolean("dkc_floor_20_kaisel_soul_spawned", true);
         }
      }
   }

   private static void grantBossRewards(ServerLevel level, ServerPlayer player, int floor) {
      String name = player.getGameProfile().getName();

      try {
         if (floor == 1) {
            reward(level, name, "rewards set 1 Item sololeveling:entry_permit true");
            reward(level, name, "rewards set 2 Item sololeveling:world_trees_fragment true");
            reward(level, name, "rewards set 3 FullRecovery true");
         } else if (floor == 10) {
            reward(level, name, "rewards set 1 Item sololeveling:entry_permit true");
            reward(level, name, "rewards set 2 Item sololeveling:spring_water_of_the_echoing_forest true");
            reward(level, name, "rewards set 3 FullRecovery true");
            RewardManager.appendReward(player, "ITEM:sololeveling:orb_of_avarice");
         } else if (floor == 20) {
            reward(level, name, "rewards set 1 Item sololeveling:purified_blood_of_the_demon_king true");
            reward(level, name, "rewards set 2 Item sololeveling:demon_kings_dagger true");
            reward(level, name, "rewards set 3 Item sololeveling:demon_kings_long_sword true");
         }
      } catch (RuntimeException var5) {
      }
   }

   private static void reward(ServerLevel level, String playerName, String arguments) {
      level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(), "slr " + playerName + " " + arguments);
   }

   private static void notify(ServerPlayer player, int accent, String title, String under, int duration) {
      SystemNotifications.showTitleUnder(
         player,
         accent,
         duration,
         Component.literal(title).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
         Component.literal(under).withStyle(ChatFormatting.GRAY)
      );
   }
}
