package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.KangTaeshikEntity;
import dev.eness.sololevelingfinal.core.guild.GuildData;
import dev.eness.sololevelingfinal.core.guild.GuildSavedData;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.network.UrgentQuestStatusMessage;

@EventBusSubscriber
public final class UrgentQuestManager {
   private static final int ACCENT = -49859;
   private static final double START_CHANCE = 0.35;
   private static final double RED_OR_PROCEDURAL_START_CHANCE = 0.55;
   private static final double REPEAT_PVP_RUNESTONE_CHANCE = 0.35;
   private static final int WEAPON_HITS_REQUIRED = 3;
   private static final long WEAPON_HIT_WINDOW_TICKS = 160L;
   private static final long PVP_RETRIGGER_COOLDOWN_TICKS = 1200L;
   private static final int MIN_KILL_TARGET = 6;
   private static final int MIN_KILL_HEADROOM = 2;
   private static final int KILL_HEADROOM_DIVISOR = 5;
   private static final double UNTAGGED_DUNGEON_RADIUS_SQR = 36864.0;
   private static final String ACTIVE = "sl_urgent_active";
   private static final String ID = "sl_urgent_id";
   private static final String KIND = "sl_urgent_kind";
   private static final String FAMILY = "sl_urgent_family";
   private static final String TITLE = "sl_urgent_title";
   private static final String OBJECTIVE = "sl_urgent_objective";
   private static final String PROGRESS = "sl_urgent_progress";
   private static final String TARGET = "sl_urgent_target";
   private static final String START_TICK = "sl_urgent_start_tick";
   private static final String TIME_LIMIT = "sl_urgent_time_limit";
   private static final String XP_REWARD = "sl_urgent_xp_reward";
   private static final String NO_SKILLS_FAILED = "sl_urgent_no_skills_failed";
   private static final String ACTIVE_TAG = "sl_urgent_active_tag";
   private static final String LAST_TAG = "sl_urgent_last_tag";
   private static final String DUNGEON_ID = "sl_urgent_dungeon_id";
   private static final String PROCEDURAL_DUNGEON = "slr_procedural_dungeon";
   private static final String PROCEDURAL_RED = "slr_procedural_red";
   private static final String PVP_TARGETS = "sl_urgent_pvp_targets";
   private static final String PVP_DEFEATED = "sl_urgent_pvp_defeated";
   private static final String PVP_ATTACKER = "sl_urgent_pvp_attacker";
   private static final String PVP_RUNESTONES = "sl_urgent_pvp_runestones";
   private static final String PVP_LAST_QUEST_TICK = "sl_urgent_pvp_last_quest_tick";
   private static final String PVP_FIRST_REWARD_CLAIMED = "sl_urgent_pvp_first_reward_claimed";
   private static final String KANG_TARGET = "sl_urgent_kang_target";
   private static final String KIND_KILL = "kill";
   private static final String KIND_CLEAR = "clear";
   private static final String KIND_NO_SKILLS = "no_skills";
   private static final String KIND_PVP = "pvp";
   private static final String KIND_KANG = "kang";
   private static final Map<UrgentQuestManager.AttackPair, Long> RECENT_CRITICALS = new HashMap<>();
   private static final Map<UrgentQuestManager.AttackPair, UrgentQuestManager.HitWindow> WEAPON_HITS = new HashMap<>();
   private static final Set<String> EXCLUSIVE_RUNESTONE_SKILLS = Set.of("Cold Blood");
   private static final Map<String, String> RUNESTONE_ALIASES = Map.ofEntries(
      Map.entry("back_step", "runestone_backstep"),
      Map.entry("cross_strike", "runestone_criticalstrike"),
      Map.entry("ground_slam", "runestone_slam"),
      Map.entry("haste_buff", "runestone_haste"),
      Map.entry("hyper_focus", "runestone_hyperfocus"),
      Map.entry("monarch_s_domain", "runestone_monarchs_domain"),
      Map.entry("murderious_intent", "murderious_intent_stone"),
      Map.entry("physical_buff", "runestone_physical"),
      Map.entry("ruler_s_hand", "telekinesis_stone"),
      Map.entry("rulers_hand", "telekinesis_stone"),
      Map.entry("slash_dash", "runestone_slashdash"),
      Map.entry("sword_of_light", "runestone_swordof_light")
   );

   private UrgentQuestManager() {
   }

   public static void markDungeonId(Entity entity, String dungeonId) {
      if (entity != null && dungeonId != null && !dungeonId.isBlank()) {
         entity.getPersistentData().putString("sl_urgent_dungeon_id", dungeonId);
      }
   }

   public static boolean hasActiveQuest(ServerPlayer player) {
      return player != null && player.getPersistentData().getBoolean("sl_urgent_active");
   }

   public static void resetForPlayerReset(ServerPlayer player) {
      if (player != null) {
         UUID playerId = player.getUUID();
         RECENT_CRITICALS.keySet().removeIf(pair -> pair.attacker.equals(playerId) || pair.victim.equals(playerId));
         WEAPON_HITS.keySet().removeIf(pair -> pair.attacker.equals(playerId) || pair.victim.equals(playerId));
         KangTaeshikAmbushManager.resetPlayerProgress(player);
         clearActive(player);
      }
   }

   public static boolean startKangAmbushQuest(ServerPlayer player, KangTaeshikEntity kang) {
      if (player != null && kang != null && !hasActiveQuest(player) && SystemPlayerAccess.hasSystem(player)) {
         player.getPersistentData().putBoolean("sl_urgent_active", true);
         player.getPersistentData().putString("sl_urgent_id", "kang_taeshik_ambush");
         player.getPersistentData().putString("sl_urgent_kind", "kang");
         player.getPersistentData().putString("sl_urgent_title", "Killing Intent");
         player.getPersistentData().putString("sl_urgent_objective", "Defeat Kang Taeshik and ensure your safety");
         player.getPersistentData().putInt("sl_urgent_progress", 0);
         player.getPersistentData().putInt("sl_urgent_target", 1);
         player.getPersistentData().putInt("sl_urgent_time_limit", 0);
         player.getPersistentData().putInt("sl_urgent_xp_reward", 0);
         player.getPersistentData().putLong("sl_urgent_start_tick", player.level().getGameTime());
         player.getPersistentData().putString("sl_urgent_kang_target", kang.getUUID().toString());
         syncQuestStatus(player);
         Component undertext = Component.literal("System detected a hunter\n")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal("with killing intent towards the player\n").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("Defeat him and ensure your safety\n").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Defeat Kang Taeshik [0/1]").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
         SystemNotifications.showNegativeTitleUnder(
            player, -49859, 180, Component.literal("Warning!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), undertext
         );
         return true;
      } else {
         return false;
      }
   }

   public static void onSkillUsed(Entity entity, String skillName) {
      if (entity instanceof ServerPlayer player && skillName != null && !skillName.isBlank()) {
         if (player.getPersistentData().getBoolean("sl_urgent_active")) {
            if ("no_skills".equals(player.getPersistentData().getString("sl_urgent_kind"))) {
               player.getPersistentData().putBoolean("sl_urgent_no_skills_failed", true);
               fail(player, "Skill used: " + skillName);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onCriticalHit(CriticalHitEvent event) {
      if (event.getEntity() instanceof ServerPlayer attacker && event.getTarget() instanceof ServerPlayer victim) {
         if (pvpUrgentQuestsEnabled(victim) && !sameParty(attacker, victim) && !sameGuild(attacker, victim)) {
            if (event.isVanillaCritical() || !(event.getDamageModifier() <= 1.0F)) {
               RECENT_CRITICALS.put(new UrgentQuestManager.AttackPair(attacker.getUUID(), victim.getUUID()), attacker.level().getGameTime());
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLivingHurt(LivingHurtEvent event) {
      if (event.getEntity() instanceof ServerPlayer victim && !victim.level().isClientSide() && !(event.getAmount() <= 0.0F)) {
         ServerPlayer attacker = attackingPlayer(event.getSource());
         if (attacker != null && attacker != victim && pvpUrgentQuestsEnabled(victim) && !sameParty(attacker, victim) && !sameGuild(attacker, victim)) {
            if (!victim.getPersistentData().getBoolean("sl_urgent_active")) {
               long now = victim.level().getGameTime();
               if (!victim.getPersistentData().contains("sl_urgent_pvp_last_quest_tick")
                  || now - victim.getPersistentData().getLong("sl_urgent_pvp_last_quest_tick") >= 1200L) {
                  UrgentQuestManager.AttackPair pair = new UrgentQuestManager.AttackPair(attacker.getUUID(), victim.getUUID());
                  pruneAggressionTracking(now);
                  long criticalTick = RECENT_CRITICALS.getOrDefault(pair, Long.MIN_VALUE);
                  boolean critical = now - criticalTick >= 0L && now - criticalTick <= 3L;
                  if (critical || isOffensiveSkillDamage(event.getSource())) {
                     startPvpQuest(victim, attacker);
                     clearAggressionTracking(victim.getUUID());
                  } else if (isWeaponPressure(event.getSource(), attacker)) {
                     UrgentQuestManager.HitWindow previous = WEAPON_HITS.get(pair);
                     int hits = previous != null && now - previous.firstTick <= 160L ? previous.hits + 1 : 1;
                     long firstTick = previous != null && now - previous.firstTick <= 160L ? previous.firstTick : now;
                     WEAPON_HITS.put(pair, new UrgentQuestManager.HitWindow(firstTick, now, hits));
                     if (hits >= 3) {
                        startPvpQuest(victim, attacker);
                        clearAggressionTracking(victim.getUUID());
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerClone(Clone event) {
      if (event.getEntity() instanceof ServerPlayer clone) {
         CompoundTag originalRoot = event.getOriginal().getPersistentData();
         if (originalRoot.contains("PlayerPersisted", 10)) {
            CompoundTag originalPersisted = originalRoot.getCompound("PlayerPersisted");
            if (originalPersisted.getBoolean("sl_urgent_pvp_first_reward_claimed")) {
               persistentPlayerData(clone).putBoolean("sl_urgent_pvp_first_reward_claimed", true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         syncQuestStatus(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         syncQuestStatus(player);
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.level().isClientSide() && event.player instanceof ServerPlayer player) {
         if (player.tickCount % 20 == 0) {
            boolean urgentActive = player.getPersistentData().getBoolean("sl_urgent_active");
            if (urgentActive) {
               syncQuestStatus(player);
            }

            String urgentKind = player.getPersistentData().getString("sl_urgent_kind");
            if (!urgentActive || !"pvp".equals(urgentKind) && !"kang".equals(urgentKind)) {
               if (!isDungeonDimension(player.level())) {
                  if (player.getPersistentData().getBoolean("sl_urgent_active")) {
                     fail(player, "Dungeon left");
                  }
               } else {
                  String tag = player.getPersistentData().getString("dungeon_tag");
                  if (!tag.isBlank()) {
                     if (player.getPersistentData().getBoolean("sl_urgent_active")) {
                        checkTimeout(player);
                     } else if (!tag.equals(player.getPersistentData().getString("sl_urgent_last_tag"))) {
                        player.getPersistentData().putString("sl_urgent_last_tag", tag);
                        double chance = !player.getPersistentData().getBoolean("slr_procedural_dungeon")
                              && !player.getPersistentData().getBoolean("slr_procedural_red")
                           ? 0.35
                           : 0.55;
                        if (!(player.getRandom().nextDouble() > chance)) {
                           startRandomQuest(player, dungeonId(player), tag);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onLivingDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null && event.getSource() != null) {
         Entity killed = event.getEntity();
         if (!(killed instanceof KangTaeshikEntity kang && completeKangAmbush(kang))) {
            ServerPlayer player = creditedPlayer(event.getEntity().level(), event.getSource().getEntity());
            if (killed instanceof ServerPlayer killedPlayer) {
               if (player != null
                  && player.getPersistentData().getBoolean("sl_urgent_active")
                  && "pvp".equals(player.getPersistentData().getString("sl_urgent_kind"))
                  && recordPvpDefeat(player, killedPlayer)) {
                  return;
               }

               if (killedPlayer.getPersistentData().getBoolean("sl_urgent_active")
                  && "pvp".equals(killedPlayer.getPersistentData().getString("sl_urgent_kind"))
                  && player != null
                  && isPvpTarget(killedPlayer, player.getUUID())) {
                  fail(killedPlayer, "You were defeated");
                  return;
               }

               if (killedPlayer.getPersistentData().getBoolean("sl_urgent_active")
                  && "kang".equals(killedPlayer.getPersistentData().getString("sl_urgent_kind"))) {
                  fail(killedPlayer, "You were defeated");
                  return;
               }
            }

            if (player != null && player.getPersistentData().getBoolean("sl_urgent_active")) {
               String kind = player.getPersistentData().getString("sl_urgent_kind");
               if ("kill".equals(kind)) {
                  String family = player.getPersistentData().getString("sl_urgent_family");
                  String activeTag = player.getPersistentData().getString("sl_urgent_active_tag");
                  if (isEligibleDungeonKillTarget(player, killed, activeTag) && matchesFamily(killed, family)) {
                     int progress = player.getPersistentData().getInt("sl_urgent_progress") + 1;
                     player.getPersistentData().putInt("sl_urgent_progress", progress);
                     int target = player.getPersistentData().getInt("sl_urgent_target");
                     if (progress >= target) {
                        complete(player);
                     } else {
                        syncQuestStatus(player);
                        if (progress == 1 || progress % 5 == 0) {
                           progressPopup(player, progress, target);
                        }
                     }
                  }
               } else {
                  if (("clear".equals(kind) || "no_skills".equals(kind)) && isBoss(killed)) {
                     if ("no_skills".equals(kind) && player.getPersistentData().getBoolean("sl_urgent_no_skills_failed")) {
                        fail(player, "Skill restriction broken");
                     } else {
                        complete(player);
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean completeKangAmbush(KangTaeshikEntity kang) {
      if (KangTaeshikAmbushManager.isAmbush(kang) && kang.level() instanceof ServerLevel level) {
         UUID ownerId = KangTaeshikAmbushManager.ownerId(kang);
         ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
         if (owner == null) {
            return true;
         }

         boolean firstCompletion = KangTaeshikAmbushManager.markAmbushCompleted(owner);
         if (!firstCompletion) {
            if (hasActiveQuest(owner) && "kang".equals(owner.getPersistentData().getString("sl_urgent_kind"))) {
               clearActive(owner);
            }

            return true;
         } else if (!SystemPlayerAccess.hasSystem(owner)) {
            if (hasActiveQuest(owner) && "kang".equals(owner.getPersistentData().getString("sl_urgent_kind"))) {
               fail(owner, "System Player status required");
            }

            return true;
         } else {
            boolean stealthReward = shouldAwardStealthRunestone(owner);
            if (stealthReward) {
               RewardManager.appendReward(owner, "ITEM:sololeveling:stealth_stone");
            }

            RewardManager.appendReward(owner, "FR");
            if (hasActiveQuest(owner) && "kang".equals(owner.getPersistentData().getString("sl_urgent_kind"))) {
               clearActive(owner);
            }

            MutableComponent rewards = Component.literal("Rewards added to System Rewards\n").withStyle(ChatFormatting.GRAY);
            if (stealthReward) {
               rewards.append(Component.literal("Stealth Runestone\n").withStyle(ChatFormatting.LIGHT_PURPLE));
            }

            rewards.append(Component.literal("Full Recovery").withStyle(ChatFormatting.AQUA));
            SystemNotifications.showTitleUnder(
               owner, -11141222, 150, Component.literal("URGENT QUEST COMPLETE").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), rewards
            );
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean shouldAwardStealthRunestone(ServerPlayer player) {
      boolean learned = SkillListHelper.skills(player).stream().anyMatch(skill -> "Stealth".equalsIgnoreCase(skill));
      String abilities = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables())
         .abilities;
      if (!learned && (abilities == null || !abilities.toLowerCase(Locale.ROOT).contains("stealth"))) {
         return RewardManager.allRewards(player).contains("ITEM:sololeveling:stealth_stone")
            ? false
            : !player.getInventory().contains(new ItemStack(SololevelingModItems.STEALTH_STONE.get()));
      } else {
         return false;
      }
   }

   private static void startPvpQuest(ServerPlayer victim, ServerPlayer attacker) {
      if (!victim.getPersistentData().getBoolean("sl_urgent_active")) {
         List<ServerPlayer> targetPlayers = attackerPartyTargets(attacker, victim);
         if (!targetPlayers.isEmpty()) {
            List<String> targetIds = targetPlayers.stream().map(player -> player.getUUID().toString()).toList();
            List<String> runestones = attackerRunestones(attacker);
            victim.getPersistentData().putBoolean("sl_urgent_active", true);
            victim.getPersistentData().putString("sl_urgent_id", "pvp_killing_intent");
            victim.getPersistentData().putString("sl_urgent_kind", "pvp");
            victim.getPersistentData().putString("sl_urgent_title", "Killing Intent");
            victim.getPersistentData().putString("sl_urgent_objective", "Defeat the hostile player group");
            victim.getPersistentData().putInt("sl_urgent_progress", 0);
            victim.getPersistentData().putInt("sl_urgent_target", targetIds.size());
            victim.getPersistentData().putInt("sl_urgent_time_limit", 0);
            victim.getPersistentData().putInt("sl_urgent_xp_reward", 0);
            victim.getPersistentData().putLong("sl_urgent_start_tick", victim.level().getGameTime());
            victim.getPersistentData().putLong("sl_urgent_pvp_last_quest_tick", victim.level().getGameTime());
            victim.getPersistentData().putString("sl_urgent_pvp_targets", String.join(",", targetIds));
            victim.getPersistentData().putString("sl_urgent_pvp_defeated", "");
            victim.getPersistentData().putString("sl_urgent_pvp_attacker", attacker.getUUID().toString());
            victim.getPersistentData().putString("sl_urgent_pvp_runestones", String.join(",", runestones));
            syncQuestStatus(victim);
            Component undertext = Component.literal("System detected someone\n")
               .withStyle(ChatFormatting.GRAY)
               .append(Component.literal("with killing intent towards the player\n").withStyle(ChatFormatting.DARK_RED))
               .append(Component.literal("Defeat them and ensure your safety\n").withStyle(ChatFormatting.GRAY))
               .append(Component.literal("Defeat Players [0/" + targetIds.size() + "]").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            SystemNotifications.showNegativeTitleUnder(
               victim, -49859, 180, Component.literal("Warning!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), undertext
            );
         }
      }
   }

   private static boolean recordPvpDefeat(ServerPlayer questOwner, ServerPlayer defeatedPlayer) {
      if (!isPvpTarget(questOwner, defeatedPlayer.getUUID())) {
         return false;
      }

      Set<UUID> defeated = parseUuids(questOwner.getPersistentData().getString("sl_urgent_pvp_defeated"));
      if (!defeated.add(defeatedPlayer.getUUID())) {
         return true;
      }

      questOwner.getPersistentData().putString("sl_urgent_pvp_defeated", joinUuids(defeated));
      int progress = defeated.size();
      int target = questOwner.getPersistentData().getInt("sl_urgent_target");
      questOwner.getPersistentData().putInt("sl_urgent_progress", progress);
      if (progress >= target) {
         completePvpQuest(questOwner);
      } else {
         syncQuestStatus(questOwner);
         pvpProgressPopup(questOwner, progress, target);
      }

      return true;
   }

   private static void completePvpQuest(ServerPlayer player) {
      List<String> candidates = splitValues(player.getPersistentData().getString("sl_urgent_pvp_runestones"))
         .stream()
         .filter(UrgentQuestManager::isAvailableRunestoneReward)
         .toList();
      boolean firstReward = !persistentPlayerData(player).getBoolean("sl_urgent_pvp_first_reward_claimed");
      boolean awardRunestone = firstReward || player.getRandom().nextDouble() < 0.35;
      String reward = fallbackPvpReward(player);
      if (awardRunestone) {
         List<String> newSkillCandidates = candidates.stream().filter(candidate -> !playerAlreadyHasRunestoneSkill(player, candidate)).toList();
         if (!newSkillCandidates.isEmpty()) {
            String selected = newSkillCandidates.get(player.getRandom().nextInt(newSkillCandidates.size()));
            reward = "ITEM:sololeveling:" + selected;
         }

         persistentPlayerData(player).putBoolean("sl_urgent_pvp_first_reward_claimed", true);
      }

      clearActive(player);
      RewardManager.appendReward(player, reward);
      Component result = Component.literal("Reward added to System Rewards\n")
         .withStyle(ChatFormatting.GRAY)
         .append(
            Component.literal(RewardManager.displayName(reward).replace("§l", ""))
               .withStyle(reward.startsWith("ITEM:") ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GOLD)
         );
      SystemNotifications.showTitleUnder(
         player, -11141222, 130, Component.literal("URGENT QUEST COMPLETE").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), result
      );
   }

   private static void pvpProgressPopup(ServerPlayer player, int progress, int target) {
      Component undertext = Component.literal("Defeat Players [" + progress + "/" + target + "]").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
      SystemNotifications.showTitleUnder(
         player, -49859, 80, Component.literal("KILLING INTENT").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), undertext
      );
   }

   private static List<ServerPlayer> attackerPartyTargets(ServerPlayer attacker, ServerPlayer victim) {
      List<ServerPlayer> targets = new ArrayList<>();
      targets.add(attacker);
      String party = partyOf(attacker);
      if (!party.isBlank() && attacker.getServer() != null) {
         for (ServerPlayer candidate : attacker.getServer().getPlayerList().getPlayers()) {
            if (candidate != attacker && candidate != victim && party.equals(partyOf(candidate)) && !sameGuild(candidate, victim)) {
               targets.add(candidate);
            }
         }

         return targets;
      } else {
         return targets;
      }
   }

   private static List<String> attackerRunestones(ServerPlayer attacker) {
      Set<String> result = new HashSet<>();

      for (String skill : SkillListHelper.skills(attacker)) {
         String registryId = runestoneForSkill(skill);
         if (registryId != null) {
            result.add(registryId);
         }
      }

      return new ArrayList<>(result);
   }

   private static boolean playerAlreadyHasRunestoneSkill(ServerPlayer player, String runestoneRegistryId) {
      for (String skill : SkillListHelper.skills(player)) {
         String ownedRunestone = runestoneForSkill(skill);
         if (runestoneRegistryId.equals(ownedRunestone)) {
            return true;
         }
      }

      return false;
   }

   private static String fallbackPvpReward(ServerPlayer player) {
      return player.getRandom().nextBoolean() ? "SP" + (5 + player.getRandom().nextInt(6)) : "GOLD" + (150 + player.getRandom().nextInt(151));
   }

   private static String runestoneForSkill(String skill) {
      if (skill == null || skill.isBlank()) {
         return null;
      }

      if (EXCLUSIVE_RUNESTONE_SKILLS.contains(skill)) {
         return null;
      }

      String normalized = skill.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
      String registryId = RUNESTONE_ALIASES.getOrDefault(normalized, "runestone_" + normalized);
      if (MageSpellProgression.isRetiredRunestoneId(registryId)) {
         return null;
      }

      Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("sololeveling", registryId));
      return item != null && item != Items.AIR ? registryId : null;
   }

   private static boolean isAvailableRunestoneReward(String registryId) {
      if (registryId != null && !registryId.isBlank() && !MageSpellProgression.isRetiredRunestoneId(registryId)) {
         ResourceLocation location = ResourceLocation.tryParse(registryId.contains(":") ? registryId : "sololeveling:" + registryId);
         if (location == null) {
            return false;
         }

         Item item = ForgeRegistries.ITEMS.getValue(location);
         return item != null && item != Items.AIR;
      } else {
         return false;
      }
   }

   private static boolean isPvpTarget(ServerPlayer questOwner, UUID targetId) {
      return parseUuids(questOwner.getPersistentData().getString("sl_urgent_pvp_targets")).contains(targetId);
   }

   private static Set<UUID> parseUuids(String encoded) {
      Set<UUID> result = new HashSet<>();

      for (String value : splitValues(encoded)) {
         try {
            result.add(UUID.fromString(value));
         } catch (IllegalArgumentException var5) {
         }
      }

      return result;
   }

   private static List<String> splitValues(String encoded) {
      if (encoded != null && !encoded.isBlank()) {
         List<String> result = new ArrayList<>();

         for (String value : encoded.split(",")) {
            if (!value.isBlank()) {
               result.add(value.trim());
            }
         }

         return result;
      } else {
         return List.of();
      }
   }

   private static String joinUuids(Set<UUID> values) {
      return values.stream().map(UUID::toString).sorted().reduce((left, right) -> left + "," + right).orElse("");
   }

   private static void startRandomQuest(ServerPlayer player, String dungeonId, String tag) {
      List<UrgentQuestManager.QuestDefinition> pool = new ArrayList<>();

      for (UrgentQuestManager.QuestDefinition candidate : questPool(dungeonId)) {
         if (!"kill".equals(candidate.kind)) {
            pool.add(candidate);
         } else {
            UrgentQuestManager.QuestDefinition feasible = feasibleKillQuest(player, candidate, tag);
            if (feasible != null) {
               pool.add(feasible);
            }
         }
      }

      if (!pool.isEmpty()) {
         UrgentQuestManager.QuestDefinition quest = pool.get(player.getRandom().nextInt(pool.size()));
         player.getPersistentData().putBoolean("sl_urgent_active", true);
         player.getPersistentData().putString("sl_urgent_id", quest.id);
         player.getPersistentData().putString("sl_urgent_kind", quest.kind);
         player.getPersistentData().putString("sl_urgent_family", quest.family);
         player.getPersistentData().putString("sl_urgent_title", quest.title);
         player.getPersistentData().putString("sl_urgent_objective", quest.objective);
         player.getPersistentData().putInt("sl_urgent_progress", 0);
         player.getPersistentData().putInt("sl_urgent_target", quest.target);
         player.getPersistentData().putInt("sl_urgent_time_limit", quest.timeLimitSeconds);
         player.getPersistentData().putInt("sl_urgent_xp_reward", quest.xpReward);
         player.getPersistentData().putLong("sl_urgent_start_tick", player.level().getGameTime());
         player.getPersistentData().putBoolean("sl_urgent_no_skills_failed", false);
         player.getPersistentData().putString("sl_urgent_active_tag", tag);
         syncQuestStatus(player);
         SystemNotifications.showTitleUnder(player, -49859, 120, Component.literal("§4§lURGENT QUEST"), Component.literal("§c" + quest.objective));
      }
   }

   private static UrgentQuestManager.QuestDefinition feasibleKillQuest(ServerPlayer player, UrgentQuestManager.QuestDefinition quest, String dungeonTag) {
      int alive = aliveMatchingDungeonMobs(player, quest.family, dungeonTag);
      int headroom = Math.max(2, (alive + 5 - 1) / 5);
      int target = Math.min(quest.target, Math.max(0, alive - headroom));
      if (target < 6) {
         return null;
      }

      String objective = "Kill " + target + " " + killFamilyLabel(quest.family) + " in " + quest.timeLimitSeconds + " seconds";
      return new UrgentQuestManager.QuestDefinition(quest.id, quest.kind, quest.family, quest.title, objective, target, quest.timeLimitSeconds, quest.xpReward);
   }

   private static int aliveMatchingDungeonMobs(ServerPlayer player, String family, String dungeonTag) {
      int alive = 0;

      for (Entity candidate : player.serverLevel().getAllEntities()) {
         if (candidate.isAlive() && isEligibleDungeonKillTarget(player, candidate, dungeonTag) && matchesFamily(candidate, family)) {
            alive++;
         }
      }

      return alive;
   }

   private static boolean isEligibleDungeonKillTarget(ServerPlayer player, Entity candidate, String dungeonTag) {
      if (candidate instanceof LivingEntity && !(candidate instanceof Player) && !isBoss(candidate) && dungeonTag != null && !dungeonTag.isBlank()) {
         String candidateTag = candidate.getPersistentData().getString("dungeon_tag");
         if (!candidateTag.isBlank() && !dungeonTag.equals(candidateTag)) {
            return false;
         } else if (candidateTag.isBlank() && candidate.distanceToSqr(player) > 36864.0) {
            return false;
         } else if (candidate instanceof TamableAnimal tame && tame.isTame()) {
            return false;
         } else {
            return !ShadowMonarchManager.isShadowEntity(candidate) && ShadowMonarchManager.getShadowOwnerUUID(candidate) == null
               ? !candidate.isAlliedTo(player) && !player.isAlliedTo(candidate)
               : false;
         }
      } else {
         return false;
      }
   }

   private static String killFamilyLabel(String family) {
      return switch (family) {
         case "goblin" -> "goblins";
         case "skeleton" -> "skeletons";
         case "lab" -> "lab monsters";
         case "beast" -> "beasts";
         case "golem" -> "golems";
         case "orc" -> "orcs";
         case "ant" -> "ants";
         default -> "dungeon monsters";
      };
   }

   private static List<UrgentQuestManager.QuestDefinition> questPool(String dungeonId) {
      return switch (dungeonId) {
         case "goblin_sewers" -> List.of(
            kill("sewer_goblin_sweep", "Goblin Extermination", "Kill 20 goblins in 100 seconds", "goblin", 20, 100, 180),
            clear("sewer_boss_rush", "Sewer Boss Rush", "Clear Goblin Sewers within 300 seconds", 300, 220),
            noSkills("sewer_no_skills", "Weapon Only", "Clear Goblin Sewers without using skills", 260)
         );
         case "cemetery" -> List.of(
            kill("cemetery_undead_sweep", "Undead Sweep", "Kill 12 skeletons in 140 seconds", "skeleton", 12, 140, 220),
            clear("cemetery_clear_rush", "Graveyard Rush", "Clear the cemetery within 300 seconds", 300, 260),
            noSkills("cemetery_no_skills", "Cold Steel", "Clear the cemetery without using skills", 300)
         );
         case "lab" -> List.of(
            kill("lab_cleanup", "Experiment Cleanup", "Kill 8 lab monsters in 160 seconds", "lab", 8, 160, 280),
            clear("lab_clear_rush", "Lab Shutdown", "Clear the lab within 300 seconds", 300, 340),
            noSkills("lab_no_skills", "Clean Hands", "Clear the lab without using skills", 380)
         );
         case "lush" -> List.of(
            kill("lush_beast_hunt", "Beast Hunt", "Kill 12 beasts in 160 seconds", "beast", 12, 160, 260),
            clear("lush_clear_rush", "Overgrown Gate", "Clear the lush gate within 300 seconds", 300, 300),
            noSkills("lush_no_skills", "Wild Hunt", "Clear the lush gate without using skills", 340)
         );
         case "golem_halls" -> List.of(
            kill("golem_hall_cleanup", "Stone Breaker", "Kill 8 golems in 180 seconds", "golem", 8, 180, 300),
            clear("golem_hall_clear_rush", "Hall Breaker", "Clear the golem halls within 300 seconds", 300, 360),
            noSkills("golem_hall_no_skills", "Pure Strength", "Clear the golem halls without using skills", 420)
         );
         case "kargalgan_throne" -> List.of(
            kill("high_orc_sweep", "High Orc Sweep", "Kill 12 orcs in 160 seconds", "orc", 12, 160, 320),
            clear("kargalgan_clear_rush", "Throne Rush", "Clear Kargalgan's throne within 300 seconds", 300, 420),
            noSkills("kargalgan_no_skills", "Silent Throne", "Clear Kargalgan's throne without using skills", 480)
         );
         case "ant_island" -> List.of(
            kill("ant_cull", "Ant Cull", "Kill 15 ants in 180 seconds", "ant", 15, 180, 360),
            clear("ant_boss_rush", "Commander Hunt", "Clear the ant gate within 300 seconds", 300, 460)
         );
         case "red_gate", "procedural" -> List.of(
            kill("gate_massacre", "Gate Massacre", "Kill 20 dungeon monsters in 180 seconds", "any", 20, 180, 300),
            clear("gate_clear_rush", "Gate Rush", "Clear the dungeon within 300 seconds", 300, 360),
            noSkills("gate_no_skills", "Weapon Trial", "Clear the dungeon without using skills", 420)
         );
         default -> List.of(
            kill("dungeon_hunt", "Dungeon Hunt", "Kill 15 dungeon monsters in 180 seconds", "any", 15, 180, 220),
            clear("dungeon_clear_rush", "Dungeon Rush", "Clear the dungeon within 300 seconds", 300, 280),
            noSkills("dungeon_no_skills", "No Skills", "Clear the dungeon without using skills", 320)
         );
      };
   }

   private static UrgentQuestManager.QuestDefinition kill(String id, String title, String objective, String family, int target, int seconds, int xpReward) {
      return new UrgentQuestManager.QuestDefinition(id, "kill", family, title, objective, target, seconds, xpReward);
   }

   private static UrgentQuestManager.QuestDefinition clear(String id, String title, String objective, int seconds, int xpReward) {
      return new UrgentQuestManager.QuestDefinition(id, "clear", "", title, objective, 1, seconds, xpReward);
   }

   private static UrgentQuestManager.QuestDefinition noSkills(String id, String title, String objective, int xpReward) {
      return new UrgentQuestManager.QuestDefinition(id, "no_skills", "", title, objective, 1, 0, xpReward);
   }

   private static void checkTimeout(ServerPlayer player) {
      int seconds = player.getPersistentData().getInt("sl_urgent_time_limit");
      if (seconds > 0) {
         long elapsed = (player.level().getGameTime() - player.getPersistentData().getLong("sl_urgent_start_tick")) / 20L;
         if (elapsed > seconds) {
            fail(player, "Time expired");
         }
      }
   }

   private static void complete(ServerPlayer player) {
      String title = player.getPersistentData().getString("sl_urgent_title");
      int xpReward = player.getPersistentData().getInt("sl_urgent_xp_reward");
      clearActive(player);
      if (xpReward > 0) {
         RewardManager.appendReward(player, "XP" + xpReward);
      }

      SystemNotifications.showTitleUnder(
         player, -11141222, 110, Component.literal("§a§lURGENT QUEST COMPLETE"), Component.literal("§7" + title + "\nReward added to System Rewards")
      );
   }

   private static void fail(ServerPlayer player, String reason) {
      String title = player.getPersistentData().getString("sl_urgent_title");
      clearActive(player);
      SystemNotifications.showNegativeTitleUnder(
         player, -49859, 90, Component.literal("§4§lURGENT QUEST FAILED"), Component.literal("§7" + title + " - " + reason)
      );
   }

   private static void progressPopup(ServerPlayer player, int progress, int target) {
      String title = player.getPersistentData().getString("sl_urgent_title");
      SystemNotifications.showTitleUnder(
         player, -49859, 60, Component.literal("§4§l" + title.toUpperCase(Locale.ROOT)), Component.literal("§c" + progress + "/" + target)
      );
   }

   private static void clearActive(ServerPlayer player) {
      player.getPersistentData().putBoolean("sl_urgent_active", false);
      player.getPersistentData().remove("sl_urgent_id");
      player.getPersistentData().remove("sl_urgent_kind");
      player.getPersistentData().remove("sl_urgent_family");
      player.getPersistentData().remove("sl_urgent_title");
      player.getPersistentData().remove("sl_urgent_objective");
      player.getPersistentData().remove("sl_urgent_progress");
      player.getPersistentData().remove("sl_urgent_target");
      player.getPersistentData().remove("sl_urgent_start_tick");
      player.getPersistentData().remove("sl_urgent_time_limit");
      player.getPersistentData().remove("sl_urgent_xp_reward");
      player.getPersistentData().remove("sl_urgent_no_skills_failed");
      player.getPersistentData().remove("sl_urgent_active_tag");
      player.getPersistentData().remove("sl_urgent_pvp_targets");
      player.getPersistentData().remove("sl_urgent_pvp_defeated");
      player.getPersistentData().remove("sl_urgent_pvp_attacker");
      player.getPersistentData().remove("sl_urgent_pvp_runestones");
      player.getPersistentData().remove("sl_urgent_kang_target");
      syncQuestStatus(player);
   }

   private static void syncQuestStatus(ServerPlayer player) {
      boolean active = player.getPersistentData().getBoolean("sl_urgent_active");
      String title = active ? player.getPersistentData().getString("sl_urgent_title") : "";
      String objective = active ? player.getPersistentData().getString("sl_urgent_objective") : "";
      String kind = active ? player.getPersistentData().getString("sl_urgent_kind") : "";
      int progress = active ? player.getPersistentData().getInt("sl_urgent_progress") : 0;
      int target = active ? player.getPersistentData().getInt("sl_urgent_target") : 0;
      int remaining = -1;
      if (active) {
         int timeLimit = player.getPersistentData().getInt("sl_urgent_time_limit");
         if (timeLimit > 0) {
            long elapsed = Math.max(0L, (player.level().getGameTime() - player.getPersistentData().getLong("sl_urgent_start_tick")) / 20L);
            remaining = Math.max(0, timeLimit - (int)elapsed);
         }
      }

      SololevelingMod.PACKET_HANDLER
         .send(PacketDistributor.PLAYER.with(() -> player), new UrgentQuestStatusMessage(active, title, objective, kind, progress, target, remaining));
   }

   private static boolean isDungeonDimension(Level level) {
      ResourceLocation id = level.dimension().location();
      return "sololeveling".equals(id.getNamespace()) && (id.getPath().startsWith("dungeon_dimension") || id.getPath().startsWith("monarch_territory_"));
   }

   private static String dungeonId(ServerPlayer player) {
      String explicit = player.getPersistentData().getString("sl_urgent_dungeon_id");
      if (!explicit.isBlank()) {
         return explicit;
      }

      if (player.getPersistentData().getBoolean("slr_procedural_dungeon")) {
         return player.getPersistentData().getBoolean("slr_procedural_red") ? "red_gate" : "procedural";
      }

      String path = player.level().dimension().location().getPath();
      if (path.startsWith("monarch_territory_")) {
         return "red_gate";
      }

      return switch (path) {
         case "dungeon_dimension_d" -> "goblin_sewers";
         case "dungeon_dimension_b" -> "cemetery";
         case "dungeon_dimension_a" -> "lab";
         case "dungeon_dimension_s" -> "ant_island";
         case "dungeon_dimension_snow" -> "red_gate";
         default -> "generic";
      };
   }

   private static ServerPlayer creditedPlayer(Level level, Entity source) {
      if (source == null) {
         return null;
      } else if (source instanceof ServerPlayer player) {
         return player;
      } else if (source instanceof Projectile projectile && projectile.getOwner() != null) {
         return creditedPlayer(level, projectile.getOwner());
      } else if (source instanceof TamableAnimal tame && tame.isTame() && tame.getOwner() instanceof ServerPlayer owner) {
         return owner;
      } else {
         UUID ownerId = ShadowMonarchManager.getShadowOwnerUUID(source);
         return ownerId != null && level instanceof ServerLevel serverLevel && serverLevel.getPlayerByUUID(ownerId) instanceof ServerPlayer owner
            ? owner
            : null;
      }
   }

   private static ServerPlayer attackingPlayer(DamageSource source) {
      if (source == null) {
         return null;
      } else {
         Entity causing = source.getEntity();
         if (causing instanceof ServerPlayer player) {
            return player;
         } else if (causing instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer owner) {
            return owner;
         } else {
            return source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer owner ? owner : null;
         }
      }
   }

   private static boolean isOffensiveSkillDamage(DamageSource source) {
      if (!source.is(DamageTypes.MAGIC) && !source.is(DamageTypes.INDIRECT_MAGIC)) {
         String id = source.getMsgId().toLowerCase(Locale.ROOT);
         return id.equals("mage")
            || id.equals("assassin")
            || id.equals("fighter")
            || id.equals("ranger")
            || id.equals("tanker")
            || id.equals("magic_beast")
            || id.equals("knight_critical");
      } else {
         return true;
      }
   }

   private static boolean isWeaponPressure(DamageSource source, ServerPlayer attacker) {
      return attacker.getMainHandItem().isEmpty() ? false : source.is(DamageTypes.PLAYER_ATTACK) || source.getDirectEntity() instanceof Projectile;
   }

   private static boolean sameParty(ServerPlayer first, ServerPlayer second) {
      String party = partyOf(first);
      return !party.isBlank() && party.equals(partyOf(second));
   }

   private static boolean sameGuild(ServerPlayer first, ServerPlayer second) {
      if (first != null && second != null) {
         GuildSavedData data = GuildSavedData.get(first.serverLevel());
         GuildData firstGuild = data.getGuildForPlayer(first.getUUID());
         GuildData secondGuild = data.getGuildForPlayer(second.getUUID());
         return firstGuild != null && secondGuild != null && firstGuild.id.equals(secondGuild.id);
      } else {
         return false;
      }
   }

   private static boolean pvpUrgentQuestsEnabled(ServerPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).pvpUrgentQuests;
   }

   private static String partyOf(ServerPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).party;
   }

   private static void pruneAggressionTracking(long now) {
      RECENT_CRITICALS.entrySet().removeIf(entry -> now - entry.getValue() > 20L);
      WEAPON_HITS.entrySet().removeIf(entry -> now - entry.getValue().lastTick > 160L);
   }

   private static void clearAggressionTracking(UUID victimId) {
      RECENT_CRITICALS.keySet().removeIf(pair -> pair.victim.equals(victimId));
      WEAPON_HITS.keySet().removeIf(pair -> pair.victim.equals(victimId));
   }

   private static CompoundTag persistentPlayerData(ServerPlayer player) {
      CompoundTag root = player.getPersistentData();
      if (!root.contains("PlayerPersisted", 10)) {
         root.put("PlayerPersisted", new CompoundTag());
      }

      return root.getCompound("PlayerPersisted");
   }

   private static boolean matchesFamily(Entity entity, String family) {
      if ("any".equals(family)) {
         return !isBoss(entity);
      }

      String name = entity.getClass().getSimpleName().toLowerCase(Locale.ROOT);

      return switch (family) {
         case "goblin" -> name.contains("goblin") && !name.contains("king");
         case "skeleton" -> name.contains("skeleton");
         case "lab" -> name.contains("mutated") || name.contains("mini") || name.contains("golem") && !isBoss(entity);
         case "beast" -> name.contains("wolf") || name.contains("lycan") || name.contains("bear");
         case "golem" -> name.contains("golem") && !isBoss(entity);
         case "orc" -> name.contains("orc") && !name.contains("shadow");
         case "ant" -> name.contains("ant") && !name.contains("beru");
         default -> false;
      };
   }

   private static boolean isBoss(Entity entity) {
      String name = entity.getClass().getSimpleName();
      return name.equals("GoblinKingEntity")
         || name.equals("SpiderBossEntity")
         || name.equals("FangedKasakaEntity")
         || name.equals("IgrisEntity")
         || name.equals("BloodRedComIgrisEntity")
         || name.equals("BarukaEntity")
         || name.equals("KargalganEntity")
         || name.equals("BeruBossEntity")
         || name.equals("AncientGolemEntity")
         || name.equals("GemGolemEntity")
         || name.equals("FuturisticGolemEntity")
         || name.equals("KamishEntity")
         || name.equals("VulcanEntity")
         || name.equals("CerberusEntity")
         || name.equals("BaranEntity")
         || name.equals("KaiselinEntity")
         || name.equals("KaiselEntity");
   }

   private record AttackPair(UUID attacker, UUID victim) {
   }

   private record HitWindow(long firstTick, long lastTick, int hits) {
   }

   private record QuestDefinition(String id, String kind, String family, String title, String objective, int target, int timeLimitSeconds, int xpReward) {
   }
}
