package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.runtime.SnowRedGateArenaManager;
import dev.eness.sololevelingfinal.core.entity.AncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.BarukaEntity;
import dev.eness.sololevelingfinal.core.entity.BeruBossEntity;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.entity.FuturisticGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.entity.KamishEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.RewardManager;
import dev.eness.sololevelingfinal.core.util.StoryModeIntroManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

@EventBusSubscriber
public class RewardGainAdvProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         execute(event, event.getEntity(), event.getSource().getEntity());
      }
   }

   public static void execute(Entity entity, Entity sourceentity) {
      execute(null, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (!StoryModeIntroManager.isIntroActive(entity)) {
            boolean runtimeSpawn = entity.getPersistentData().getBoolean("slr_dungeon_spawned");
            boolean redGateBaruka = entity instanceof BarukaEntity && SnowRedGateArenaManager.isArenaMob(entity);
            if (!runtimeSpawn || redGateBaruka) {
               String command = "";
               String r1 = "";
               String r2 = "";
               String r3 = "";
               r1 = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .reward_1;
               r2 = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .reward_2;
               r3 = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .reward_3;
               Entity ent;
               if (sourceentity instanceof TamableAnimal _tamEnt && _tamEnt.isTame()) {
                  if ((sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) == null) {
                     return;
                  }

                  ent = sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null;
               } else {
                  ent = sourceentity;
               }

               if (entity instanceof FangedKasakaEntity
                  && !(
                     ent instanceof ServerPlayer _plr5
                        && _plr5.level() instanceof ServerLevel
                        && _plr5.getAdvancements()
                           .getOrStartProgress(_plr5.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:kasakas_domain")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "SP15";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "ITEM:sololeveling:kasakas_venom_fangs";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:kasakas_domain"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }

               if (entity instanceof BloodRedComIgrisEntity
                  && !(
                     ent instanceof ServerPlayer _plr12
                        && _plr12.level() instanceof ServerLevel
                        && _plr12.getAdvancements()
                           .getOrStartProgress(_plr12.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:blood_red_commander_igris")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "ITEM:sololeveling:telekinesis_stone";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "GOLD5000";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:blood_red_commander_igris"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }

               if (entity instanceof BeruBossEntity
                  && !(
                     ent instanceof ServerPlayer _plr19
                        && _plr19.level() instanceof ServerLevel
                        && _plr19.getAdvancements()
                           .getOrStartProgress(_plr19.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:ant_king")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "SP40";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "GOLD10000";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:ant_king"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }

               if (entity instanceof GoblinKingEntity
                  && !(
                     ent instanceof ServerPlayer _plr26
                        && _plr26.level() instanceof ServerLevel
                        && _plr26.getAdvancements()
                           .getOrStartProgress(_plr26.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:goblin_king_adv")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "ITEMBOX";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "ITEM:sololeveling:medium_health_potion";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:goblin_king_adv"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }

               if (entity instanceof GemGolemEntity
                  && !(
                     ent instanceof ServerPlayer _plr33
                        && _plr33.level() instanceof ServerLevel
                        && _plr33.getAdvancements()
                           .getOrStartProgress(_plr33.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:ancient_golem_adv")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "SP20";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "ITEMBOX";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:ancient_golem_adv"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }

               if (entity instanceof AncientGolemEntity
                  && !(
                     ent instanceof ServerPlayer _plr40
                        && _plr40.level() instanceof ServerLevel
                        && _plr40.getAdvancements()
                           .getOrStartProgress(_plr40.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:gem_golem_adv")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "SP20";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "ITEMBOX";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:gem_golem_adv"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }

               if (entity instanceof FuturisticGolemEntity
                  && !(
                     ent instanceof ServerPlayer _plr47
                        && _plr47.level() instanceof ServerLevel
                        && _plr47.getAdvancements()
                           .getOrStartProgress(_plr47.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:futuristic_golem_adv")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "SP10";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "GOLD2000";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:futuristic_golem_adv"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }

               if (entity instanceof BarukaEntity
                  && !(
                     ent instanceof ServerPlayer _plr54
                        && _plr54.level() instanceof ServerLevel
                        && _plr54.getAdvancements()
                           .getOrStartProgress(_plr54.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:baruka_adv")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "SP10";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "ITEM:sololeveling:barukas_dagger";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:baruka_adv"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }

               if (entity instanceof KamishEntity
                  && !(
                     ent instanceof ServerPlayer _plr61
                        && _plr61.level() instanceof ServerLevel
                        && _plr61.getAdvancements()
                           .getOrStartProgress(_plr61.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:kamish_adv")))
                           .isDone()
                  )) {
                  RewardCollectProcedure.execute(ent, r1);
                  RewardCollectProcedure.execute(ent, r2);
                  RewardCollectProcedure.execute(ent, r3);
                  String _setval = "FR";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_1 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "SP50";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_2 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  _setval = "GOLD15000";
                  ent.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.reward_3 = _setval;
                     capability.syncPlayerVariables(ent);
                  });
                  notifyRewardsAvailable(ent);
                  if (ent instanceof ServerPlayer _player) {
                     Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:kamish_adv"));
                     AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
                     if (!_ap.isDone()) {
                        for (String criteria : _ap.getRemainingCriteria()) {
                           _player.getAdvancements().award(_adv, criteria);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void notifyRewardsAvailable(Entity entity) {
      RewardManager.reconcileFullRecoveryRewards(entity);
      if (entity instanceof ServerPlayer player) {
         SystemNotifications.showTitleUnder(
            player,
            -18371,
            90,
            Component.literal("REWARDS AVAILABLE").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            Component.literal("Open Rewards to collect them.").withStyle(ChatFormatting.GRAY)
         );
      }
   }
}
