package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.HunterEvaluationRules;
import dev.eness.sololevelingfinal.core.util.LevelRewardRules;
import dev.eness.sololevelingfinal.core.util.RangerCombatManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;
import dev.eness.sololevelingfinal.core.util.SystemPlayerAccess;

@EventBusSubscriber
public class LevelUpProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player.getX(), event.player.getY(), event.player.getZ(), event.player);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null && SystemPlayerAccess.hasSystem(entity)) {
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .ifPresent(
               playerVars -> {
                  double initialXp = playerVars.Xp;
                  int initialLevel = (int)playerVars.Level;
                  double initialRank = playerVars.HunterRank;
                  int newLevel = initialLevel;
                  double remainingXp = initialXp;

                  while (true) {
                     double requiredXP = newLevel * 16 + 8;
                     if (remainingXp < requiredXP || newLevel >= 2147483646) {
                        int levelsGained = newLevel - initialLevel;
                        if (levelsGained > 0) {
                           playerVars.Level = newLevel;
                           playerVars.Xp = remainingXp;
                           playerVars.Fatigue = 0.0;
                           if (initialRank > 0.0) {
                              int levelFloor = HunterEvaluationRules.rankFloorForLevel(newLevel);
                              playerVars.HunterRank = Math.min(6.0, Math.max(initialRank, levelFloor));
                           }

                           int rankPromotions = Math.max(0, (int)Math.round(playerVars.HunterRank - initialRank));
                           playerVars.Vitality += levelsGained;
                           playerVars.Strength += levelsGained;
                           playerVars.Intelligence += levelsGained;
                           playerVars.perception += levelsGained;
                           playerVars.Speed += levelsGained;
                           playerVars.Durability += levelsGained;
                           playerVars.SkillPoints = playerVars.SkillPoints + LevelRewardRules.skillPointsForLevels(levelsGained);
                           playerVars.syncPlayerVariables(entity);
                           if (!world.isClientSide()) {
                              if (rankPromotions > 0) {
                                 grantRankSkills(entity, (int)Math.round(playerVars.Classes), rankPromotions);
                              }

                              if (entity instanceof ServerPlayer player) {
                                 SystemNotifications.showTitleUnder(
                                    player,
                                    -12597505,
                                    80,
                                    Component.literal("LEVEL UP").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                                    Component.literal("Lv " + initialLevel + " -> " + newLevel).withStyle(ChatFormatting.YELLOW)
                                 );
                              }

                              ((Level)world)
                                 .playSound(
                                    (Player)null,
                                    BlockPos.containing(x, y, z),
                                    ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                                    SoundSource.NEUTRAL,
                                    2.0F,
                                    1.0F
                                 );
                              if (playerVars.HunterRank > initialRank && entity instanceof ServerPlayer player) {
                                 SystemNotifications.showTitleUnder(
                                    player,
                                    -18371,
                                    80,
                                    Component.literal("RANK UP").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                                    Component.literal("Hunter Rank " + Math.round(initialRank) + " -> " + Math.round(playerVars.HunterRank))
                                       .withStyle(ChatFormatting.GOLD)
                                 );
                              }
                           }

                           if (entity instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide()) {
                              livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 255, false, false));
                              livingEntity.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 10, false, false));
                              livingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 3, false, false));
                           }
                        }

                        return;
                     }

                     remainingXp -= requiredXP;
                     newLevel++;
                  }
               }
            );
      }
   }

   private static void grantRankSkills(Entity entity, int playerClass, int promotions) {
      if (entity != null && promotions > 0) {
         if (playerClass == 6) {
            if (entity instanceof ServerPlayer player) {
               RangerCombatManager.reconcileRanger(player);
            }
         } else {
            for (int promotion = 0; promotion < promotions; promotion++) {
               switch (playerClass) {
                  case 1:
                     MasterylvlupassassinProcedure.execute(entity);
                     break;
                  case 2:
                     MasterylvlupMageProcedure.execute(entity);
                     break;
                  case 3:
                     MasterylvlupFighterProcedure.execute(entity);
                     break;
                  case 4:
                     MasterylvlupTankerProcedure.execute(entity);
                     break;
                  case 5:
                     MasterylvlupHealerProcedure.execute(entity);
                     break;
                  default:
                     return;
               }
            }
         }
      }
   }
}
