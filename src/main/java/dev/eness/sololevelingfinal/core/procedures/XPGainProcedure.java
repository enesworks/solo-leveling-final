package dev.eness.sololevelingfinal.core.procedures;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.AncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.AncientSamuraiEntity;
import dev.eness.sololevelingfinal.core.entity.BarukaEntity;
import dev.eness.sololevelingfinal.core.entity.BeruBossEntity;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.CentipedeEntity;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.entity.FuturisticGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.GreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.HighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.IceElfEntity;
import dev.eness.sololevelingfinal.core.entity.KamishEntity;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;
import dev.eness.sololevelingfinal.core.entity.MiniGemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.MutatedEntity;
import dev.eness.sololevelingfinal.core.entity.OrcEntity;
import dev.eness.sololevelingfinal.core.entity.PolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.RedAntsEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonBruteEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonSummonerEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonWarriorEntity;
import dev.eness.sololevelingfinal.core.entity.SpiderBossEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.entity.StoneGolemEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

@EventBusSubscriber
public class XPGainProcedure {
   private static final Map<Class<? extends Entity>, Integer> XP_REWARDS = new HashMap<>();

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onEntityHurt(LivingHurtEvent event) {
      if (event != null && !event.isCanceled() && !(event.getAmount() <= 0.0F) && !event.getEntity().level().isClientSide()) {
         ShadowKillCreditHelper.rememberRecentPlayerDamage(event.getEntity(), event.getSource().getEntity(), event.getSource().getDirectEntity());
      }
   }

   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         Entity source = event.getSource().getEntity();
         Player creditedPlayer = ShadowKillCreditHelper.creditedPlayerForDeath(
            event.getEntity().level(), event.getEntity(), source, event.getSource().getDirectEntity()
         );
         execute(event, event.getEntity().level(), event.getEntity(), creditedPlayer != null ? creditedPlayer : source);
      }
   }

   public static void execute(LevelAccessor world, Entity entity, Entity sourceEntity) {
      execute(null, world, entity, sourceEntity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceEntity) {
      if (entity != null && sourceEntity != null) {
         if (!JobChangeQuestManager.isAttemptEntity(entity)) {
            Player xpReceiver = ShadowKillCreditHelper.creditedPlayer(world, sourceEntity);
            if (xpReceiver != null) {
               SololevelingModVariables.PlayerVariables playerVars = xpReceiver.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(new SololevelingModVariables.PlayerVariables());
               if (playerVars.Player) {
                  boolean isListed = XP_REWARDS.containsKey(entity.getClass());
                  boolean runtimeDungeonMob = entity.getPersistentData().getBoolean("slr_dungeon_spawned");
                  boolean soloDungeonOnly = world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.SOLO_DUNGEON_PROGRESSION_ONLY);
                  if (!soloDungeonOnly || isListed || runtimeDungeonMob) {
                     boolean configuredDungeonXp = runtimeDungeonMob && entity.getPersistentData().contains("slr_dungeon_base_xp", 99);
                     int baseXP = configuredDungeonXp
                        ? Math.max(0, entity.getPersistentData().getInt("slr_dungeon_base_xp"))
                        : (isListed ? XP_REWARDS.get(entity.getClass()) : (runtimeDungeonMob ? runtimeDungeonBaseXp(entity) : 1));
                     if (baseXP > 0) {
                        int xpMultiplier = world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER);
                        double diffMultiplier = difficultyMultiplier(world);
                        awardBaseXp(world, xpReceiver, baseXP, diffMultiplier, xpMultiplier, mobLevelXpMultiplier(entity));
                     }
                  }
               }
            }
         }
      }
   }

   public static void awardBaseXp(LevelAccessor world, Player player, int baseXP) {
      if (world != null && player != null) {
         int xpMultiplier = world.getLevelData().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_XP_MULTIPLIER);
         double diffMultiplier = difficultyMultiplier(world);
         awardBaseXp(world, player, baseXP, diffMultiplier, xpMultiplier, 1.0);
      }
   }

   public static void awardRewardXp(Player player, int rewardXP) {
      if (player != null && rewardXP > 0) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            if (capability.Player) {
               capability.Xp += rewardXP;
               capability.syncPlayerVariables(player);
               showXpGain(player, rewardXP);
            }
         });
      }
   }

   private static double difficultyMultiplier(LevelAccessor world) {
      if (world.getDifficulty() == Difficulty.NORMAL) {
         return 0.75;
      } else {
         return world.getDifficulty() == Difficulty.HARD ? 0.5 : 1.0;
      }
   }

   private static double mobLevelXpMultiplier(Entity entity) {
      double statMultiplier = entity.getPersistentData().getDouble("SLRLevelStatMultiplier");
      if (statMultiplier <= 1.0) {
         double level = Math.max(0.0, entity.getPersistentData().getDouble("Level"));
         if (level <= 0.0) {
            return 1.0;
         }

         statMultiplier = 1.0 + Math.min(200.0, level) * 0.005;
      }

      return Math.min(3.0, Math.max(1.0, Math.sqrt(statMultiplier)));
   }

   private static int runtimeDungeonBaseXp(Entity entity) {
      int level = Math.max(1, Math.min(1000, entity.getPersistentData().getInt("Level")));
      String role = entity.getPersistentData().getString("slr_dungeon_role");

      return switch (role) {
         case "boss" -> Math.max(25, level * 2);
         case "elite" -> Math.max(3, level / 3);
         default -> Math.max(1, level / 5);
      };
   }

   private static void awardBaseXp(LevelAccessor world, Player player, int baseXP, double diffMultiplier, int xpMultiplier, double mobLevelMultiplier) {
      SololevelingModVariables.PlayerVariables playerVars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      if (playerVars.Player) {
         double totalXP = diffMultiplier * playerVars.xpmultiplier * (xpMultiplier / 10.0) * baseXP * mobLevelMultiplier;
         double newXP = playerVars.Xp + totalXP;
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Xp = newXP;
            capability.syncPlayerVariables(player);
         });
         showXpGain(player, totalXP);
      }
   }

   private static void showXpGain(Player player, double totalXP) {
      if (!player.level().isClientSide()) {
         String formattedXP = String.format(Locale.FRANCE, "%,.1f", totalXP);
         player.displayClientMessage(Component.literal("§bGained §f" + formattedXP + "§b XP"), true);
      }
   }

   static {
      XP_REWARDS.put(GoblinArcherEntity.class, 3);
      XP_REWARDS.put(GoblinMageEntity.class, 3);
      XP_REWARDS.put(GoblinClubEntity.class, 3);
      XP_REWARDS.put(StoneGolemEntity.class, 15);
      XP_REWARDS.put(SteelFangWolfEntity.class, 5);
      XP_REWARDS.put(SteelFangedLycanEntity.class, 5);
      XP_REWARDS.put(OrcEntity.class, 25);
      XP_REWARDS.put(RedAntsEntity.class, 40);
      XP_REWARDS.put(PolarBearEntity.class, 16);
      XP_REWARDS.put(IceElfEntity.class, 35);
      XP_REWARDS.put(MiniGemGolemEntity.class, 22);
      XP_REWARDS.put(MutatedEntity.class, 25);
      XP_REWARDS.put(SkeletonWarriorEntity.class, 15);
      XP_REWARDS.put(SkeletonBruteEntity.class, 15);
      XP_REWARDS.put(DKnight1Entity.class, 10);
      XP_REWARDS.put(DKnight2Entity.class, 10);
      XP_REWARDS.put(DKnight3Entity.class, 10);
      XP_REWARDS.put(CentipedeEntity.class, 30);
      XP_REWARDS.put(GreenOrcEntity.class, 25);
      XP_REWARDS.put(HighOrcEntity.class, 40);
      XP_REWARDS.put(AncientSamuraiEntity.class, 600);
      XP_REWARDS.put(GoblinKingEntity.class, 900);
      XP_REWARDS.put(SpiderBossEntity.class, 1000);
      XP_REWARDS.put(GemGolemEntity.class, 2000);
      XP_REWARDS.put(AncientGolemEntity.class, 1500);
      XP_REWARDS.put(FangedKasakaEntity.class, 1100);
      XP_REWARDS.put(FuturisticGolemEntity.class, 1200);
      XP_REWARDS.put(BloodRedComIgrisEntity.class, 3000);
      XP_REWARDS.put(BarukaEntity.class, 5000);
      XP_REWARDS.put(SkeletonSummonerEntity.class, 6750);
      XP_REWARDS.put(KargalganEntity.class, 8000);
      XP_REWARDS.put(BeruBossEntity.class, 10000);
      XP_REWARDS.put(KamishEntity.class, 20000);
   }
}
