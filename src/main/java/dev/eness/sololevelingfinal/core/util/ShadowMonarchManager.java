package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcSpatialLayout;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonInstanceSavedData;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonMobLevelAdapter;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageShadowEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KamishShadowEntity;
import dev.eness.sololevelingfinal.core.entity.OrcShadowEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowGreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowHighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowKaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowPolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowSold1Entity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfShadowEntity;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.SkillSlotHelper;

public class ShadowMonarchManager {
   public static final String FORMATION_PREFIX = "Formation:";
   public static final int FORMATION_COLOR = 12150271;
   public static final String COMMAND_DEFAULT = "default";
   public static final String COMMAND_PROTECT = "protect";
   public static final String COMMAND_BERSERK = "berserk";
   public static final String COMMAND_FOLLOW = "follow";
   public static final String COMMAND_CLEAR_DUNGEON = "clear_dungeon";
   public static final int RANK_NORMAL = 0;
   public static final int RANK_ELITE = 1;
   public static final int RANK_KNIGHT = 2;
   public static final int RANK_ELITE_KNIGHT = 3;
   public static final int RANK_GENERAL = 4;
   public static final int RANK_MARSHAL = 5;
   public static final int RANK_GRAND_MARSHAL = 6;
   public static final double SHADOW_HEALTH_PER_HEALING_MANA = 4.0;
   public static final int BASE_SHADOW_LEVEL_CAP = 10;
   public static final int MAX_ADMIN_SHADOW_LEVEL = 1000000;
   private static final int PLAYER_LEVEL_CAP_START = 40;
   private static final int PLAYER_LEVELS_PER_CAP_INCREASE = 20;
   private static final int SHADOW_LEVELS_PER_CAP_INCREASE = 10;
   private static final int MAX_SAFE_SHADOW_LEVEL = 143165574;
   private static final String ROOT = "sololeveling_shadow_monarch";
   private static final String SHADOWS = "shadows";
   private static final String FORMATIONS = "formations";
   private static final String GLOW_COLORS = "glow_colors";
   private static final String IRON_MAX = "iron_max";
   private static final String IRON_SUMMONED = "iron_summoned";
   public static final int NO_GLOW = -1;
   private static final String RANK = "rank";
   private static final String STARTING_RANK = "starting_rank";
   private static final String RANK_SCHEMA = "rank_schema";
   private static final int RANK_SCHEMA_VERSION = 3;
   private static final String GRAND_MARSHAL_ID = "grand_marshal_id";
   private static final String ADMIN_LEVEL_FLOOR = "admin_level_floor";
   private static final String SHADOW_ID = "sl_shadow_id";
   private static final String SHADOW_TYPE = "sl_shadow_type";
   private static final String SHADOW_OWNER = "sl_shadow_owner";
   private static final String SHADOW_COMMAND = "sl_shadow_command";
   private static final String PLAYER_COMMAND = "sl_shadow_command_mode";
   private static final String PLAYER_RESET_GENERATION = "sl_shadow_reset_generation";
   private static final String SHADOW_GENERATION = "sl_shadow_generation";
   private static final String SHADOW_INVENTORY = "sl_shadow_inventory";
   private static final String EQUIPMENT = "equipment";
   private static final String SHADOW_EQUIPMENT = "sl_shadow_equipment";
   private static final String CACHED_LEVEL_CAP = "shadow_level_cap";
   private static final String BASE_HEALTH = "sl_shadow_base_health";
   private static final String BASE_ATTACK = "sl_shadow_base_attack";
   private static final String APPLIED_LEVEL = "sl_shadow_applied_level";
   private static final String APPLIED_RANK = "sl_shadow_applied_rank";
   private static final String INTRINSIC_MARSHAL_DOMAIN = "sl_shadow_rank_domain";
   private static final String TEMPORARY_DOMAIN_UNTIL = "sl_shadow_temporary_domain_until";
   private static final int MARSHAL_DOMAIN_AMPLIFIER = 1;
   private static final int MARSHAL_DOMAIN_DURATION_TICKS = 240;
   private static final int MARSHAL_DOMAIN_REFRESH_THRESHOLD_TICKS = 80;
   private static final String INSUFFICIENT_MANA_NOTICE = "sl_shadow_mana_notice";
   private static final String SAVED_HEALTH = "health";
   private static final String SAVED_HEALTH_AT = "health_saved_at";
   private static final String PROCEDURAL_DUNGEON_TAG = "slr_procedural_dungeon";
   private static final TagKey<EntityType<?>> SHADOW_ENTITY_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows"));
   private static final int CLEAR_SCAN_INTERVAL_TICKS = 20;
   private static final int CLEAR_REPATH_INTERVAL_TICKS = 30;
   private static final int CLEAR_STUCK_TICKS = 80;
   private static final int CLEAR_FAILED_TARGET_COOLDOWN_TICKS = 120;
   private static final int CLEAR_MAX_CANDIDATES = 128;
   private static final int CLEAR_MAX_PATH_ATTEMPTS_PER_TICK = 8;
   private static final int CLEAR_MAX_TARGET_CHOICES_PER_SHADOW = 5;
   private static final double CLEAR_ENGAGE_RADIUS_SQR = 676.0;
   private static final int CLEAR_ADVANCE_REPATH_TICKS = 20;
   private static final double CLEAR_ADVANCE_SPEED = 1.15;
   private static final float SHADOW_TRAVERSAL_STEP_HEIGHT = 1.1F;
   private static final TagKey<EntityType<?>> SOLO_BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "soloboss"));
   private static final Map<UUID, ShadowMonarchManager.ClearDungeonState> CLEAR_DUNGEON_STATES = new HashMap<>();

   private ShadowMonarchManager() {
   }

   public static boolean isFormationSkill(String skill) {
      return skill != null && skill.startsWith("Formation:");
   }

   public static String displaySkillName(Entity entity, String skill) {
      if (isFormationSkill(skill)) {
         String embeddedName = formationNameFromSkill(skill);
         if (!embeddedName.isEmpty()) {
            return embeddedName;
         }

         CompoundTag formation = getFormation(entity, formationIdFromSkill(skill));
         return formation == null ? "Formation" : formation.getString("name");
      } else {
         return "Critical Strike".equals(skill) ? "Cross Strike" : skill;
      }
   }

   public static int skillColor(Entity entity, String skill) {
      if (isFormationSkill(skill)) {
         return 12150271;
      } else {
         return JobSkillManager.isJobSkill(skill) ? JobSkillManager.skillColor(skill) : 16777215;
      }
   }

   public static boolean isShadowAvailableFor(Player player, String requestedType) {
      String type = normalizeShadowType(requestedType == null ? "" : requestedType);
      return !type.isEmpty() && (!"iron".equals(type) || DeveloperModeManager.isEnabled(player));
   }

   public static boolean summonType(LevelAccessor world, double x, double y, double z, Entity caster, String type) {
      if (caster instanceof ServerPlayer player && world instanceof ServerLevel level) {
         type = normalizeShadowType(type);
         if (!type.isEmpty() && isShadowAvailableFor(player, type)) {
            ensureRoster(player);
            absorbVisibleOwnedShadows(player);
            enforceSummonedLimit(player, type);
            CompoundTag shadow = firstSummonableShadow(player, type);
            return shadow == null ? false : summonShadow(level, player, shadow, new Vec3(x, y, z), true);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static int summonAllOfType(LevelAccessor world, double x, double y, double z, Entity caster, String type) {
      if (caster instanceof ServerPlayer player && world instanceof ServerLevel level) {
         type = normalizeShadowType(type);
         if (!type.isEmpty() && isShadowAvailableFor(player, type)) {
            ensureRoster(player);
            absorbVisibleOwnedShadows(player);
            enforceSummonedLimit(player, type);
            List<CompoundTag> matching = ownedRosterWithinLimit(player, type);
            int summoned = 0;
            Vec3 origin = new Vec3(x, y, z);

            for (CompoundTag shadow : matching) {
               Vec3 pos = spreadSummonPosition(player, origin, summoned);
               if (summonShadow(level, player, shadow, pos, true)) {
                  summoned++;
               }
            }

            return summoned;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static boolean castFormation(LevelAccessor world, Entity caster, String skill) {
      if (caster instanceof ServerPlayer player && world instanceof ServerLevel level && isFormationSkill(skill)) {
         ensureRoster(player);
         CompoundTag formation = getFormation(player, formationIdFromSkill(skill));
         if (formation == null) {
            return false;
         }

         ListTag members = formation.getList("members", 10);
         Vec3 look = player.getLookAngle();
         Vec3 forward = new Vec3(look.x, 0.0, look.z);
         if (forward.lengthSqr() < 0.001) {
            forward = new Vec3(0.0, 0.0, 1.0);
         }

         forward = forward.normalize();
         Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
         Vec3 origin = player.position().add(forward.scale(2.5));
         boolean summonedAny = false;

         for (int i = 0; i < members.size(); i++) {
            CompoundTag member = members.getCompound(i);
            CompoundTag shadow = getShadow(player, member.getString("id"));
            if (shadow != null) {
               Vec3 pos = origin.add(right.scale(member.getDouble("rx"))).add(0.0, member.getDouble("ry"), 0.0).add(forward.scale(member.getDouble("rz")));
               summonedAny |= summonShadow(level, player, shadow, pos, true);
            }
         }

         return summonedAny;
      } else {
         return false;
      }
   }

   public static String saveFormationFromSummoned(Player player, String requestedName) {
      if (!(player.level() instanceof ServerLevel)) {
         return "";
      }

      ensureRoster(player);
      absorbVisibleOwnedShadows(player);
      List<CompoundTag> shadows = summonedOwnedShadows(player);
      if (shadows.isEmpty()) {
         return "";
      }

      String id = UUID.randomUUID().toString();
      String name = cleanFormationName(requestedName, formationCount(player) + 1);
      Vec3 look = player.getLookAngle();
      Vec3 forward = new Vec3(look.x, 0.0, look.z);
      if (forward.lengthSqr() < 0.001) {
         forward = new Vec3(0.0, 0.0, 1.0);
      }

      forward = forward.normalize();
      Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
      ListTag members = new ListTag();

      for (CompoundTag shadow : shadows) {
         UUID summonedId = shadow.getUUID("summoned");
         Entity summoned = ((ServerLevel)player.level()).getEntity(summonedId);
         if (summoned != null) {
            Vec3 delta = summoned.position().subtract(player.position());
            CompoundTag member = new CompoundTag();
            member.putString("id", shadow.getString("id"));
            member.putDouble("rx", delta.dot(right));
            member.putDouble("ry", Math.max(-1.0, Math.min(3.0, delta.y)));
            member.putDouble("rz", delta.dot(forward));
            members.add(member);
         }
      }

      if (members.isEmpty()) {
         return "";
      }

      CompoundTag formation = new CompoundTag();
      formation.putString("id", id);
      formation.putString("name", name);
      formation.put("members", members);
      formations(player).add(formation);
      appendFormationSkill(player, id, name);
      player.getPersistentData().put("sololeveling_shadow_monarch", root(player));
      return name;
   }

   public static boolean removeFormationSkill(Player player, int skillIndex) {
      if (player != null && skillIndex >= 1) {
         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         List<String> skills = parseSkillList(vars.Plist);
         if (skillIndex > skills.size()) {
            return false;
         }

         String removed = skills.get(skillIndex - 1);
         if (!isFormationSkill(removed)) {
            return false;
         }

         String formationId = formationIdFromSkill(removed);
         skills.remove(skillIndex - 1);
         removeFormationData(player, formationId);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.Plist = writeSkillList(skills);

            for (int slot = 1; slot <= 16; slot++) {
               if (removed.equals(SkillSlotHelper.getSlot(capability, slot))) {
                  SkillSlotHelper.setSlot(capability, slot, "");
               }
            }

            if (removed.equals(capability.PselectedPower)) {
               capability.PselectedPower = "";
            }

            capability.syncPlayerVariables(player);
         });
         return true;
      } else {
         return false;
      }
   }

   public static List<String> formationSkills(Player player) {
      ArrayList<String> result = new ArrayList<>();
      if (player == null) {
         return result;
      }

      ListTag list = formations(player);

      for (int i = 0; i < list.size(); i++) {
         CompoundTag formation = list.getCompound(i);
         String id = formation.getString("id");
         if (!id.isEmpty()) {
            result.add("Formation:" + id + "|" + cleanFormationName(formation.getString("name"), i + 1));
         }
      }

      return result;
   }

   public static int grantCombatXp(Player owner, String shadowId, Entity shadowEntity, int targetXpPool, double shadowDamage, double countedTargetDamage) {
      if (owner != null && shadowId != null && !shadowId.isEmpty()) {
         ensureRoster(owner);
         CompoundTag shadow = getShadow(owner, shadowId);
         if (shadow == null) {
            return 0;
         }

         int level = Math.max(1, shadow.getInt("level"));
         int levelCap = shadowLevelCap(owner);
         int xp = Math.max(0, shadow.getInt("xp"));
         Entity activeShadow = shadowId.equals(getShadowRosterId(shadowEntity)) ? shadowEntity : null;
         if (activeShadow != null
            && (
               activeShadow.getPersistentData().getInt("sl_shadow_applied_level") != level
                  || activeShadow.getPersistentData().getInt("sl_shadow_applied_rank") != rankOf(shadow)
            )) {
            applyLevelStatsPreservingHealth(activeShadow, shadow);
         }

         if (level >= levelCap) {
            return 0;
         }

         int earnedXp = ShadowExperienceRules.contributionXp(targetXpPool, shadowDamage, countedTargetDamage, level);
         if (earnedXp <= 0) {
            return 0;
         }

         xp = (int)Math.min(2147483647L, (long)xp + earnedXp);
         int needed = xpNeeded(level, shadow.getString("type"));

         boolean leveled;
         for (leveled = false; level < levelCap && xp >= needed; leveled = true) {
            xp -= needed;
            if (++level % 10 == 0) {
               promoteShadow(owner, shadow, true);
            }

            needed = xpNeeded(level, shadow.getString("type"));
         }

         if (level >= levelCap) {
            xp = Math.min(xp, needed - 1);
         }

         shadow.putInt("level", level);
         shadow.putInt("xp", xp);
         owner.getPersistentData().put("sololeveling_shadow_monarch", root(owner));
         if (leveled && activeShadow != null) {
            applyLevelStats(activeShadow, shadow, false);
         }

         if (leveled && owner instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal(shadow.getString("name") + " reached Lv." + level), true);
         }

         return earnedXp;
      } else {
         return 0;
      }
   }

   public static void collectManaStoneDropsFromKill(Entity shadowEntity, Entity killed) {
      if (shadowEntity != null && killed != null && killed.level() instanceof ServerLevel level) {
         Vec3 var4 = killed.position();
         SololevelingMod.queueServerWork(1, () -> {
            if (!shadowEntity.isRemoved() && isTrackedShadowEntity(shadowEntity)) {
               AABB area = new AABB(var4, var4).inflate(3.0);

               for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area, itemEntityx -> isCollectibleManaStone(itemEntityx.getItem()))) {
                  ItemStack stack = itemEntity.getItem().copy();
                  if (!stack.isEmpty()) {
                     addStackToShadowInventory(shadowEntity, stack);
                     itemEntity.discard();
                  }
               }
            }
         });
      }
   }

   public static void dropStoredShadowInventory(Entity shadowEntity) {
      if (shadowEntity != null && !shadowEntity.level().isClientSide()) {
         saveBossHealthBeforeDespawn((Player)null, shadowEntity);
         CompoundTag data = shadowEntity.getPersistentData();
         if (data.contains("sl_shadow_inventory", 9)) {
            ListTag inventory = data.getList("sl_shadow_inventory", 10);

            for (int i = 0; i < inventory.size(); i++) {
               ItemStack stack = ItemStack.of(inventory.getCompound(i));
               if (!stack.isEmpty()) {
                  shadowEntity.spawnAtLocation(stack);
               }
            }

            data.remove("sl_shadow_inventory");
         }
      }
   }

   public static void preserveProgressAfterPlayerClone(Player original, Player replacement) {
      if (original != null && replacement != null && !replacement.level().isClientSide()) {
         CompoundTag originalData = original.getPersistentData();
         CompoundTag replacementData = replacement.getPersistentData();
         if (originalData.contains("sololeveling_shadow_monarch", 10)) {
            replacementData.put("sololeveling_shadow_monarch", originalData.getCompound("sololeveling_shadow_monarch").copy());
         } else {
            replacementData.remove("sololeveling_shadow_monarch");
         }

         if (originalData.contains("sl_shadow_command_mode", 8)) {
            replacementData.putString("sl_shadow_command_mode", originalData.getString("sl_shadow_command_mode"));
         } else {
            replacementData.remove("sl_shadow_command_mode");
         }

         if (originalData.contains("sl_shadow_reset_generation", 4)) {
            replacementData.putLong("sl_shadow_reset_generation", originalData.getLong("sl_shadow_reset_generation"));
         } else {
            replacementData.remove("sl_shadow_reset_generation");
         }
      }
   }

   public static void handleTrackedShadowDeath(Entity shadowEntity) {
      if (shadowEntity != null && !shadowEntity.level().isClientSide() && shadowEntity.level() instanceof ServerLevel level) {
         CompoundTag entityData = shadowEntity.getPersistentData();
         if (entityData.hasUUID("sl_shadow_owner")) {
            String shadowId = entityData.getString("sl_shadow_id");
            if (!shadowId.isEmpty()) {
               Player owner = findOnlineOwner(level, entityData.getUUID("sl_shadow_owner"));
               if (owner != null) {
                  CompoundTag shadow = getShadow(owner, shadowId);
                  if (shadow != null && shadow.hasUUID("summoned") && shadowEntity.getUUID().equals(shadow.getUUID("summoned"))) {
                     shadow.remove("summoned");
                     owner.getPersistentData().put("sololeveling_shadow_monarch", root(owner));
                     String type = shadow.getString("type");
                     if (!type.isEmpty()) {
                        updateLegacySpawnCounter(owner, type, -1);
                     }
                  }
               }
            }
         }
      }
   }

   public static void saveBossHealthBeforeDespawn(Entity ownerEntity, Entity shadowEntity) {
      Player owner = ownerEntity instanceof Player player ? player : null;
      saveBossHealthBeforeDespawn(owner, shadowEntity);
   }

   private static void saveBossHealthBeforeDespawn(Player owner, Entity shadowEntity) {
      if (shadowEntity instanceof LivingEntity living && !shadowEntity.level().isClientSide()) {
         if (living.isAlive() && !(living.getHealth() <= 0.0F)) {
            CompoundTag data = shadowEntity.getPersistentData();
            String type = data.getString("sl_shadow_type");
            if (type.isEmpty()) {
               type = typeFromEntity(shadowEntity);
            }

            if (isBoss(type)) {
               if (owner == null && shadowEntity.level() instanceof ServerLevel level && data.hasUUID("sl_shadow_owner")) {
                  owner = findOnlineOwner(level, data.getUUID("sl_shadow_owner"));
               }

               if (owner != null) {
                  String id = data.getString("sl_shadow_id");
                  if (!id.isEmpty()) {
                     CompoundTag shadow = getShadow(owner, id);
                     if (shadow != null) {
                        shadow.putDouble("health", Math.max(1.0, Math.min(living.getHealth(), living.getMaxHealth())));
                        shadow.putLong("health_saved_at", shadowEntity.level().getGameTime());
                        shadow.remove("summoned");
                        owner.getPersistentData().put("sololeveling_shadow_monarch", root(owner));
                     }
                  }
               }
            }
         }
      }
   }

   public static void tagExistingSummon(Player owner, Entity summoned, String type) {
      if (owner != null && summoned != null && type != null && !owner.level().isClientSide()) {
         if (!isShadowAvailableFor(owner, type)) {
            summoned.discard();
         } else {
            ensureRoster(owner);
            CompoundTag shadow = firstAvailableShadow(owner, type);
            if (shadow == null) {
               shadow = createShadow(owner, type, countOwned(owner, type) + 1);
            }

            tagSummonedEntity(owner, shadow, summoned);
         }
      }
   }

   public static boolean modifyShadowAmount(Player player, String requestedType, int amount) {
      if (player != null && requestedType != null && amount != 0) {
         String type = normalizeShadowType(requestedType);
         if (type.isEmpty()) {
            return false;
         }

         if (amount > 0 && !isShadowAvailableFor(player, type)) {
            return false;
         }

         if (amount < 0 && SilladIcePrisonManager.guardManualDismiss(player)) {
            return false;
         }

         ensureRoster(player);
         int current = legacyMax(player, type);
         int updated = Math.max(0, current + amount);
         if ("iron".equals(type)) {
            updated = Math.min(1, updated);
            if (updated == current) {
               return false;
            }
         }

         setLegacyMax(player, type, updated);
         if (updated < current) {
            trimOwnedShadows(player, type, updated);
         } else {
            ensureRoster(player);
         }

         repairGrandMarshalClaim(player, root(player));
         JobSkillManager.syncJobSkills(player);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> capability.syncPlayerVariables(player));
         return true;
      } else {
         return false;
      }
   }

   public static List<String> shadowCommandTargets() {
      ArrayList<String> targets = new ArrayList<>();
      targets.add("all");
      targets.addAll(List.of(shadowTypes()));
      return List.copyOf(targets);
   }

   public static String currentShadowCommand(Entity shadow) {
      return shadow == null ? "default" : commandOrDefault(shadow.getPersistentData().getString("sl_shadow_command"));
   }

   public static ShadowMonarchManager.ShadowLevelCommandResult modifyShadowLevels(Player player, String requestedType, int value, boolean additive) {
      if (player != null && requestedType != null) {
         String requested = requestedType.trim();
         boolean all = "all".equalsIgnoreCase(requested);
         String type = all ? "" : normalizeShadowType(requested);
         if (!all && type.isEmpty()) {
            return new ShadowMonarchManager.ShadowLevelCommandResult(false, 0, 0, 0);
         }

         ensureRoster(player);
         ArrayList<CompoundTag> targets = new ArrayList<>();

         for (String candidateType : shadowTypes()) {
            if (all || candidateType.equals(type)) {
               targets.addAll(ownedRosterWithinLimit(player, candidateType));
            }
         }

         if (targets.isEmpty()) {
            return new ShadowMonarchManager.ShadowLevelCommandResult(true, 0, 0, 0);
         }

         CompoundTag ownerRoot = root(player);
         int normalCap = shadowLevelCap(player);
         int lowest = 1000000;
         int highest = 1;

         for (CompoundTag shadow : targets) {
            int oldLevel = Math.max(1, shadow.getInt("level"));
            long requestedLevel = additive ? (long)oldLevel + Math.max(0, value) : value;
            int newLevel = (int)Math.max(1L, Math.min(1000000L, requestedLevel));
            shadow.putInt("level", newLevel);
            if (!additive) {
               shadow.putInt("xp", 0);
            }

            if (newLevel > normalCap) {
               shadow.putInt("admin_level_floor", newLevel);
            } else {
               shadow.remove("admin_level_floor");
            }

            recalculateRankAfterAdminLevel(ownerRoot, shadow);
            refreshSummonedShadowRank(player, shadow);
            lowest = Math.min(lowest, newLevel);
            highest = Math.max(highest, newLevel);
         }

         player.getPersistentData().put("sololeveling_shadow_monarch", ownerRoot);
         JobSkillManager.syncJobSkills(player);
         return new ShadowMonarchManager.ShadowLevelCommandResult(true, targets.size(), lowest, highest);
      } else {
         return new ShadowMonarchManager.ShadowLevelCommandResult(false, 0, 0, 0);
      }
   }

   public static boolean dismissShadowType(Player player, String requestedType) {
      if (player != null && requestedType != null && !player.level().isClientSide()) {
         String type = normalizeShadowType(requestedType);
         if (!isDismissibleShadowType(type)) {
            return false;
         }

         ensureRoster(player);
         if (legacyMax(player, type) <= 0) {
            return false;
         }

         if (!modifyShadowAmount(player, type, -1)) {
            return false;
         }

         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.shadowstorageusage = Math.max(0.0, capability.shadowstorageusage - 1.0);
            capability.syncPlayerVariables(player);
         });
         player.getPersistentData().put("sololeveling_shadow_monarch", root(player));
         return true;
      } else {
         return false;
      }
   }

   public static boolean isInDungeon(Player player) {
      if (player == null) {
         return false;
      }

      if (DkcFloorRegistry.isSharedDkc(player.level())) {
         return !(player instanceof ServerPlayer serverPlayer)
            ? DkcSpatialLayout.floorAt(player.blockPosition()) > 0
            : player.getPersistentData().getBoolean("dkc_inside_castle") && DkcSpatialLayout.floor(serverPlayer) > 0;
      }

      SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());
      String dimension = player.level().dimension().location().getPath();
      return vars.dungeoning || dimension.contains("dungeon") || dimension.contains("castle");
   }

   public static boolean isTrackedShadowEntity(Entity entity) {
      return entity != null && entity.getPersistentData().hasUUID("sl_shadow_owner");
   }

   public static boolean isShadowEntity(Entity entity) {
      return entity != null && entity.getType().is(SHADOW_ENTITY_TAG);
   }

   public static UUID getShadowOwnerUUID(Entity entity) {
      if (entity == null) {
         return null;
      } else if (entity instanceof TamableAnimal tame && tame.getOwnerUUID() != null) {
         return tame.getOwnerUUID();
      } else {
         CompoundTag data = entity.getPersistentData();
         return data.hasUUID("sl_shadow_owner") ? data.getUUID("sl_shadow_owner") : null;
      }
   }

   public static Player getShadowOwnerPlayer(Entity shadow) {
      if (shadow == null) {
         return null;
      } else if (shadow instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player player && player.level() == shadow.level()) {
         return player;
      } else {
         UUID ownerId = getShadowOwnerUUID(shadow);
         return ownerId == null ? null : shadow.level().getPlayerByUUID(ownerId);
      }
   }

   public static int dismissLoadedOwnedShadows(ServerPlayer owner, ResourceKey<Level> previousDimension) {
      if (owner != null && owner.server != null && previousDimension != null) {
         ServerLevel previousLevel = owner.server.getLevel(previousDimension);
         if (previousLevel == null) {
            return 0;
         }

         ArrayList<Entity> ownedShadows = new ArrayList<>();

         for (Entity candidate : previousLevel.getAllEntities()) {
            UUID ownerId = getShadowOwnerUUID(candidate);
            if (owner.getUUID().equals(ownerId) && (isShadowEntity(candidate) || isTrackedShadowEntity(candidate))) {
               ownedShadows.add(candidate);
            }
         }

         for (Entity shadow : ownedShadows) {
            dismissLoadedShadow(owner, shadow);
         }

         return ownedShadows.size();
      } else {
         return 0;
      }
   }

   public static int dismissLockedPreviewShadows(ServerPlayer owner) {
      if (owner != null && owner.server != null && !DeveloperModeManager.isEnabled(owner)) {
         ArrayList<Entity> locked = new ArrayList<>();

         for (ServerLevel level : owner.server.getAllLevels()) {
            for (Entity candidate : level.getAllEntities()) {
               if (owner.getUUID().equals(getShadowOwnerUUID(candidate))) {
                  String type = candidate.getPersistentData().getString("sl_shadow_type");
                  if (type.isEmpty()) {
                     type = typeFromEntity(candidate);
                  }

                  if ("iron".equals(type)) {
                     locked.add(candidate);
                  }
               }
            }
         }

         for (Entity shadow : locked) {
            dismissLoadedShadow(owner, shadow);
         }

         return locked.size();
      } else {
         return 0;
      }
   }

   public static boolean handleUnavailableShadowOwner(Entity shadowEntity) {
      if (shadowEntity == null) {
         return false;
      }

      UUID ownerId = getShadowOwnerUUID(shadowEntity);
      if (ownerId == null) {
         return false;
      }

      if (!shadowEntity.level().isClientSide()) {
         if (shadowEntity.level() instanceof ServerLevel level) {
            Player owner = findOnlineOwner(level, ownerId);
            if (owner == null) {
               if (shadowEntity instanceof Mob mob) {
                  mob.setTarget(null);
                  mob.getNavigation().stop();
               }

               return true;
            } else {
               if (owner.isAlive() && owner.level() == shadowEntity.level()) {
                  return false;
               }

               dismissLoadedShadow(owner, shadowEntity);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return shadowEntity instanceof TamableAnimal tame && tame.getOwner() == null;
      }
   }

   private static void dismissLoadedShadow(Player owner, Entity shadowEntity) {
      if (shadowEntity != null && !shadowEntity.level().isClientSide() && !shadowEntity.isRemoved()) {
         CompoundTag entityData = shadowEntity.getPersistentData();
         if (owner == null && shadowEntity.level() instanceof ServerLevel level) {
            UUID ownerId = getShadowOwnerUUID(shadowEntity);
            if (ownerId != null) {
               owner = findOnlineOwner(level, ownerId);
            }
         }

         String type = entityData.getString("sl_shadow_type");
         if (type.isEmpty()) {
            type = typeFromEntity(shadowEntity);
         }

         if (owner != null) {
            String shadowId = entityData.getString("sl_shadow_id");
            CompoundTag rosterShadow = shadowId.isEmpty() ? null : getShadow(owner, shadowId);
            boolean releasedRosterSlot = rosterShadow != null
               && rosterShadow.hasUUID("summoned")
               && shadowEntity.getUUID().equals(rosterShadow.getUUID("summoned"));
            saveBossHealthBeforeDespawn(owner, shadowEntity);
            if (releasedRosterSlot) {
               rosterShadow.remove("summoned");
               owner.getPersistentData().put("sololeveling_shadow_monarch", root(owner));
            }

            if ((releasedRosterSlot || shadowId.isEmpty()) && !type.isEmpty()) {
               updateLegacySpawnCounter(owner, type, -1);
            }
         }

         dropStoredShadowInventory(shadowEntity);
         shadowEntity.discard();
      }
   }

   public static String getShadowRosterId(Entity entity) {
      return entity == null ? "" : entity.getPersistentData().getString("sl_shadow_id");
   }

   public static boolean isOwnedShadow(Entity shadow, LivingEntity owner) {
      UUID ownerId = getShadowOwnerUUID(shadow);
      return isShadowEntity(shadow) && owner != null && ownerId != null && ownerId.equals(owner.getUUID());
   }

   public static boolean canShadowDamage(Entity shadow, Entity candidate) {
      if (shadow instanceof Mob mob && candidate instanceof LivingEntity target) {
         Player owner = getShadowOwnerPlayer(shadow);
         if (owner == null) {
            return false;
         } else if (target.isAlive() && target.isAttackable() && !target.isInvulnerable()) {
            Player targetPlayer = target instanceof Player player
               ? player
               : (target instanceof TamableAnimal targetTame && targetTame.getOwner() instanceof Player player ? player : null);
            return targetPlayer != null && haveSameGuild(owner, targetPlayer)
               ? false
               : MageCombatHelper.isValidTarget(shadow, target)
                  && (
                     target == findOwnerCombatPriorityTarget(mob, owner)
                        ? isValidOwnerDirectedTarget(target, mob, owner)
                        : isValidShadowTarget(target, mob, owner)
                  );
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean haveSameGuild(Player first, Player second) {
      double firstGuild = first.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(data -> data.GuildCode).orElse(0.0);
      double secondGuild = second.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(data -> data.GuildCode).orElse(0.0);
      return Double.isFinite(firstGuild) && firstGuild != 0.0 && Double.compare(firstGuild, secondGuild) == 0;
   }

   public static void resetPlayerProgress(ServerPlayer player) {
      if (player != null && player.server != null) {
         CompoundTag playerData = player.getPersistentData();
         if (playerData.contains("sololeveling_shadow_monarch", 10)) {
            ListTag roster = playerData.getCompound("sololeveling_shadow_monarch").getList("shadows", 10);

            for (int index = 0; index < roster.size(); index++) {
               returnEquipmentToPlayer(player, roster.getCompound(index));
            }
         }

         long currentGeneration = playerData.getLong("sl_shadow_reset_generation");
         long nextGeneration = currentGeneration == Long.MAX_VALUE ? 1L : Math.max(1L, currentGeneration + 1L);
         playerData.putLong("sl_shadow_reset_generation", nextGeneration);
         CLEAR_DUNGEON_STATES.remove(player.getUUID());
         ArrayList<Entity> loadedOwnedShadows = new ArrayList<>();

         for (ServerLevel level : player.server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
               UUID ownerId = getShadowOwnerUUID(entity);
               if (player.getUUID().equals(ownerId) && (isShadowEntity(entity) || isTrackedShadowEntity(entity))) {
                  loadedOwnedShadows.add(entity);
               }
            }
         }

         for (Entity shadow : loadedOwnedShadows) {
            dropStoredShadowInventory(shadow);
            shadow.discard();
         }

         playerData.remove("sololeveling_shadow_monarch");
         playerData.remove("sl_shadow_command_mode");
      }
   }

   public static boolean haveSameShadowOwner(Entity first, Entity second) {
      UUID firstOwner = getShadowOwnerUUID(first);
      UUID secondOwner = getShadowOwnerUUID(second);
      return isShadowEntity(first) && isShadowEntity(second) && firstOwner != null && firstOwner.equals(secondOwner);
   }

   public static boolean commandSummonedShadows(Player player, String requestedCommand) {
      if (player instanceof ServerPlayer serverPlayer) {
         String command = normalizeCommand(requestedCommand);
         if (command.isEmpty()) {
            return false;
         }

         if ("clear_dungeon".equals(command) && !isInDungeon(player)) {
            serverPlayer.displayClientMessage(Component.literal("Clear Dungeon can only be used inside a dungeon."), true);
            return false;
         }

         ensureRoster(player);
         absorbVisibleOwnedShadows(player);
         CLEAR_DUNGEON_STATES.remove(player.getUUID());
         player.getPersistentData().putString("sl_shadow_command_mode", command);
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.berserk = "berserk".equals(command);
            capability.syncPlayerVariables(player);
         });
         List<CompoundTag> summoned = summonedOwnedShadows(player);

         for (CompoundTag shadow : summoned) {
            Entity entity = serverPlayer.serverLevel().getEntity(shadow.getUUID("summoned"));
            if (entity != null && entity.isAlive()) {
               if (entity instanceof Mob mob) {
                  resetShadowCommandState(mob);
                  entity.getPersistentData().putString("sl_shadow_command", command);
                  applyCommandTarget(mob, player, command);
               } else {
                  entity.getPersistentData().putString("sl_shadow_command", command);
               }
            }
         }

         serverPlayer.displayClientMessage(Component.literal("Shadow Command: " + commandDisplayName(command) + " (" + summoned.size() + " shadows)"), true);
         return true;
      } else {
         return false;
      }
   }

   public static boolean hasShadowForDisplay(Player player, String type) {
      return ownedCountForDisplay(player, type) > 0;
   }

   public static String shadowCountText(Player player, String type) {
      if (player == null) {
         return "0/0";
      }

      int owned = ownedCountForDisplay(player, type);
      int summoned = Math.min(owned, summonedCountForDisplay(player, type));
      return summoned + "/" + owned;
   }

   public static void prepareRosterForDisplay(Player player) {
      if (player != null && !player.level().isClientSide()) {
         ensureRoster(player);
      }
   }

   public static int highestRankForDisplay(Player player, String requestedType) {
      return progressForDisplay(player, requestedType).rank();
   }

   public static int highestLevelForDisplay(Player player, String requestedType) {
      return progressForDisplay(player, requestedType).level();
   }

   public static ShadowMonarchManager.ShadowDisplayProgress progressForDisplay(Player player, String requestedType) {
      String type = normalizeShadowType(requestedType == null ? "" : requestedType);
      int initialRank = startingRank(type);
      if (player != null && !type.isEmpty() && isShadowAvailableFor(player, type)) {
         CompoundTag shadow = strongestOwnedShadow(player, type);
         if (shadow == null) {
            return new ShadowMonarchManager.ShadowDisplayProgress(initialRank, 1, 0, 1, initialRank, true, false, false, false);
         }

         int level = Math.max(1, shadow.getInt("level"));
         int rank = rankOf(shadow);
         boolean grandMarshalActive = isClaimedGrandMarshal(player, shadow);
         boolean grandMarshalEligible = isGrandMarshalEligible(shadow);
         int maximumRank = maximumRank(type);
         boolean maximum = rank >= maximumRank;
         int nextRank = maximum ? rank : Math.min(maximumRank, rank + 1);
         int bandStart = level < 10 ? 1 : level - Math.floorMod(level, 10);
         long nextMilestoneLong = (level / 10L + 1L) * 10L;
         int nextMilestone = (int)Math.min(143165574L, nextMilestoneLong);
         long progress = Math.max(0, shadow.getInt("xp"));

         for (int current = bandStart; current < level; current++) {
            progress = saturatingDisplayXpAdd(progress, xpNeeded(current, type));
         }

         long requirement = 0L;

         for (int current = bandStart; current < nextMilestone; current++) {
            requirement = saturatingDisplayXpAdd(requirement, xpNeeded(current, type));
         }

         int needed = (int)Math.max(1L, Math.min(2147483647L, requirement));
         int earned = grandMarshalEligible && !grandMarshalActive ? needed : (int)Math.max(0L, Math.min(needed, progress));
         return new ShadowMonarchManager.ShadowDisplayProgress(
            rank, level, earned, needed, nextRank, level >= effectiveShadowLevelCap(player, shadow), maximum, grandMarshalEligible, grandMarshalActive
         );
      } else {
         return new ShadowMonarchManager.ShadowDisplayProgress(initialRank, 1, 0, 1, initialRank, true, false, false, false);
      }
   }

   public static boolean isGrandMarshalEligibleForDisplay(Player player, String requestedType) {
      if (player == null) {
         return false;
      }

      String type = normalizeShadowType(requestedType == null ? "" : requestedType);
      return isGrandMarshalEligible(strongestOwnedShadow(player, type));
   }

   public static boolean isGrandMarshalForDisplay(Player player, String requestedType) {
      if (player == null) {
         return false;
      }

      String type = normalizeShadowType(requestedType == null ? "" : requestedType);
      return isClaimedGrandMarshal(player, strongestOwnedShadow(player, type));
   }

   public static int grandMarshalRequiredLevel(String requestedType) {
      String type = normalizeShadowType(requestedType == null ? "" : requestedType);
      return !isBoss(type) ? 1000000 : Math.max(10, (6 - startingRank(type)) * 10);
   }

   public static String grandMarshalSignatureName(String requestedType) {
      String type = normalizeShadowType(requestedType == null ? "" : requestedType);

      return switch (type) {
         case "igris" -> "Crimson Cross";
         case "beru" -> "King's Restoration";
         case "kamish" -> "Dragon's Dread";
         case "tusk" -> "Gravitational Ruin";
         case "kaisel" -> "Sky Rend";
         default -> "Grand Marshal Authority";
      };
   }

   public static boolean hasAssignedGrandMarshal(Entity entity) {
      if (!(entity instanceof Player player)) {
         return false;
      } else {
         if (!player.level().isClientSide()) {
            ensureRoster(player);
         }

         String claimedId = root(player).getString("grand_marshal_id");
         CompoundTag claimed = claimedId.isEmpty() ? null : getShadow(player, claimedId);
         return claimed != null && rankOf(claimed) == 6 && isGrandMarshalEligibleByLevel(claimed);
      }
   }

   public static ShadowMonarchManager.GrandMarshalAssignmentResult assignGrandMarshal(Player player, String requestedType) {
      if (player instanceof ServerPlayer serverPlayer) {
         if (!VesselProgressionManager.isShadowMonarch(serverPlayer)) {
            return new ShadowMonarchManager.GrandMarshalAssignmentResult(false, "Only the Shadow Monarch can appoint a Grand Marshal.");
         }

         String type = normalizeShadowType(requestedType == null ? "" : requestedType);
         if (!isBoss(type)) {
            return new ShadowMonarchManager.GrandMarshalAssignmentResult(false, "Only boss shadows can become Grand Marshal.");
         }

         ensureRoster(serverPlayer);
         CompoundTag target = strongestOwnedShadow(serverPlayer, type);
         if (target == null) {
            return new ShadowMonarchManager.GrandMarshalAssignmentResult(false, "You do not own that boss shadow.");
         }

         if (isClaimedGrandMarshal(serverPlayer, target)) {
            return new ShadowMonarchManager.GrandMarshalAssignmentResult(false, target.getString("name") + " is already your Grand Marshal.");
         }

         if (rankOf(target) != 5) {
            return new ShadowMonarchManager.GrandMarshalAssignmentResult(false, target.getString("name") + " must first reach Marshal rank.");
         }

         int requiredLevel = grandMarshalRequiredLevel(type);
         if (Math.max(1, target.getInt("level")) < requiredLevel) {
            return new ShadowMonarchManager.GrandMarshalAssignmentResult(false, "Grand Marshal promotion unlocks at shadow level " + requiredLevel + ".");
         }

         CompoundTag ownerRoot = root(serverPlayer);
         String oldId = ownerRoot.getString("grand_marshal_id");
         CompoundTag previous = oldId.isEmpty() ? null : getShadow(serverPlayer, oldId);
         if (previous != null && !previous.getString("id").equals(target.getString("id"))) {
            previous.putInt("rank", 5);
            refreshSummonedShadowRank(serverPlayer, previous);
         }

         target.putInt("starting_rank", startingRank(type));
         target.putInt("rank", 6);
         ownerRoot.putString("grand_marshal_id", target.getString("id"));
         serverPlayer.getPersistentData().put("sololeveling_shadow_monarch", ownerRoot);
         refreshSummonedShadowRank(serverPlayer, target);
         JobSkillManager.syncJobSkills(serverPlayer);
         Component title = Component.literal("GRAND MARSHAL APPOINTED").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
         Component under = Component.literal(target.getString("name") + "\n")
            .withStyle(ChatFormatting.LIGHT_PURPLE)
            .append(Component.literal(grandMarshalSignatureName(type)).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
         SystemNotifications.showTitleUnder(serverPlayer, rankColor(6), 120, title, under);
         return new ShadowMonarchManager.GrandMarshalAssignmentResult(
            true, target.getString("name") + " is now your Grand Marshal. " + grandMarshalSignatureName(type) + " is available in the skill list."
         );
      } else {
         return new ShadowMonarchManager.GrandMarshalAssignmentResult(false, "Grand Marshal assignment is server-authoritative.");
      }
   }

   public static ShadowMonarchManager.GrandMarshalCommander activeGrandMarshal(ServerPlayer player) {
      if (player == null) {
         return null;
      } else {
         ensureRoster(player);
         String claimedId = root(player).getString("grand_marshal_id");
         CompoundTag shadow = claimedId.isEmpty() ? null : getShadow(player, claimedId);
         if (shadow != null && rankOf(shadow) == 6 && shadow.hasUUID("summoned")) {
            Entity summoned = findSummonedEntity(player, shadow.getUUID("summoned"));
            return summoned instanceof LivingEntity living
                  && living.isAlive()
                  && summoned.level() == player.level()
                  && isCurrentSummonedInstance(player, summoned)
               ? new ShadowMonarchManager.GrandMarshalCommander(
                  claimedId, shadow.getString("type"), shadow.getString("name"), Math.max(1, shadow.getInt("level")), living
               )
               : null;
         } else {
            return null;
         }
      }
   }

   private static long saturatingDisplayXpAdd(long current, int amount) {
      return Math.min(2147483647L, Math.max(0L, current) + Math.max(0, amount));
   }

   public static boolean isCustomizableBoss(String requestedType) {
      String type = normalizeShadowType(requestedType == null ? "" : requestedType);
      return "igris".equals(type) || "tusk".equals(type);
   }

   public static boolean isValidBossEquipment(String requestedType, ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String type = normalizeShadowType(requestedType == null ? "" : requestedType);
         return stack.getCount() == 1
            && (
               "igris".equals(type) && stack.is(SololevelingModItems.DEMON_KINGS_LONG_SWORD.get())
                  || "tusk".equals(type) && stack.is(SololevelingModItems.ORB_OF_AVARICE.get())
            );
      } else {
         return true;
      }
   }

   public static ItemStack equipmentForDisplay(Player player, String requestedType) {
      if (player == null) {
         return ItemStack.EMPTY;
      }

      String type = normalizeShadowType(requestedType == null ? "" : requestedType);
      CompoundTag shadow = strongestOwnedShadow(player, type);
      return equipmentOf(shadow).copy();
   }

   public static boolean hasEquipmentForDisplay(Player player, String requestedType) {
      return !equipmentForDisplay(player, requestedType).isEmpty();
   }

   public static boolean setEquipmentForDisplay(Player player, String requestedType, ItemStack requestedStack) {
      if (player != null && !player.level().isClientSide()) {
         String type = normalizeShadowType(requestedType == null ? "" : requestedType);
         if (!isCustomizableBoss(type)) {
            return false;
         }

         ensureRoster(player);
         CompoundTag shadow = strongestOwnedShadow(player, type);
         ItemStack stack = requestedStack == null ? ItemStack.EMPTY : requestedStack.copy();
         if (shadow != null && isValidBossEquipment(type, stack)) {
            if (stack.isEmpty()) {
               shadow.remove("equipment");
            } else {
               stack.setCount(1);
               shadow.put("equipment", stack.save(new CompoundTag()));
            }

            if (shadow.hasUUID("summoned")) {
               Entity summoned = findSummonedEntity(player, shadow.getUUID("summoned"));
               if (summoned != null) {
                  syncEquipmentTag(summoned, shadow);
               }
            }

            player.getPersistentData().put("sololeveling_shadow_monarch", root(player));
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isEquipmentEquipped(Entity shadowEntity, Item item) {
      if (shadowEntity != null && item != null) {
         CompoundTag data = shadowEntity.getPersistentData();
         return !data.contains("sl_shadow_equipment", 8) ? false : BuiltInRegistries.ITEM.getKey(item).toString().equals(data.getString("sl_shadow_equipment"));
      } else {
         return false;
      }
   }

   public static String typeForSummonButton(int buttonId) {
      return switch (buttonId) {
         case 0 -> "goblin_club";
         case 1 -> "goblin_archer";
         case 2 -> "goblin_mage";
         case 3 -> "wolf";
         case 4 -> "knight";
         case 5 -> "polar_bear";
         case 6 -> "orc";
         case 7 -> "igris";
         case 8 -> "beru";
         case 9 -> "kamish";
         case 10 -> "high_orc";
         case 11 -> "tusk";
         case 12 -> "kaisel";
         case 13 -> "iron";
         default -> "";
      };
   }

   public static boolean isGrandMarshalType(String requestedType) {
      return requestedType != null && isBoss(normalizeShadowType(requestedType));
   }

   public static int startingRankForType(String type) {
      return startingRank(normalizeShadowType(type));
   }

   public static int appliedShadowRank(Entity entity) {
      if (entity == null) {
         return 0;
      }

      CompoundTag data = entity.getPersistentData();
      return data.contains("sl_shadow_applied_rank", 3)
         ? Math.max(0, Math.min(6, data.getInt("sl_shadow_applied_rank")))
         : Math.max(0, Math.min(6, startingRank(typeFromEntity(entity))));
   }

   public static int maximumRankForType(String type) {
      return maximumRank(normalizeShadowType(type));
   }

   public static String rankDisplayName(int rank) {
      return switch (Math.max(0, Math.min(6, rank))) {
         case 1 -> "Elite";
         case 2 -> "Knight";
         case 3 -> "Elite Knight";
         case 4 -> "General";
         case 5 -> "Marshal";
         case 6 -> "Grand Marshal";
         default -> "Normal";
      };
   }

   public static int rankColor(int rank) {
      return switch (rank) {
         case 1 -> -10299649;
         case 2 -> -8804353;
         case 3 -> -4948737;
         case 4 -> -1872641;
         case 5 -> -41816;
         case 6 -> -14262;
         default -> -4668967;
      };
   }

   public static int ownedCountForDisplay(Player player, String type) {
      if (player == null) {
         return 0;
      }

      String normalized = normalizeShadowType(type);
      return !normalized.isEmpty() && isShadowAvailableFor(player, normalized) ? Math.max(legacyMax(player, normalized), countOwned(player, normalized)) : 0;
   }

   public static int summonedCountForDisplay(Player player, String type) {
      if (player == null) {
         return 0;
      }

      String normalized = normalizeShadowType(type);
      if (!normalized.isEmpty() && isShadowAvailableFor(player, normalized)) {
         int rosterCount = 0;
         ListTag shadows = shadows(player);

         for (int i = 0; i < shadows.size(); i++) {
            CompoundTag shadow = shadows.getCompound(i);
            if (normalized.equals(shadow.getString("type")) && shadow.hasUUID("summoned")) {
               rosterCount++;
            }
         }

         return Math.max(legacySpawned(player, normalized), rosterCount);
      } else {
         return 0;
      }
   }

   public static int healingManaCost(double missingHealth) {
      return ShadowHealingRules.manaCost(missingHealth);
   }

   public static ShadowMonarchManager.ShadowHealingQuote healingQuote(Player player) {
      if (player instanceof ServerPlayer owner) {
         List targets = healingTargets(owner);
         double bossMissing = 0.0;
         double allMissing = 0.0;
         int bossTargets = 0;

         for (ShadowMonarchManager.ShadowHealingTarget target : targets) {
            allMissing += target.missingHealth();
            if (isHealingBossType(target.type())) {
               bossMissing += target.missingHealth();
               bossTargets++;
            }
         }

         return new ShadowMonarchManager.ShadowHealingQuote(healingManaCost(bossMissing), healingManaCost(allMissing), bossTargets, targets.size());
      } else {
         return new ShadowMonarchManager.ShadowHealingQuote(0, 0, 0, 0);
      }
   }

   public static ShadowMonarchManager.ShadowHealingResult healSummonedShadows(ServerPlayer owner, boolean bossesOnly) {
      if (owner != null && VesselProgressionManager.isShadowMonarch(owner)) {
         List<ShadowMonarchManager.ShadowHealingTarget> selected = new ArrayList<>();
         double missingHealth = 0.0;

         for (ShadowMonarchManager.ShadowHealingTarget target : healingTargets(owner)) {
            if (!bossesOnly || isHealingBossType(target.type())) {
               selected.add(target);
               missingHealth += target.missingHealth();
            }
         }

         int quotedCost = healingManaCost(missingHealth);
         String group = bossesOnly ? "summoned boss shadows" : "summoned shadows";
         if (selected.isEmpty() || quotedCost <= 0) {
            return new ShadowMonarchManager.ShadowHealingResult(false, 0, 0, 0, "No " + group + " currently need healing.");
         }

         if (!hasSummonMana(owner, quotedCost)) {
            int available = owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .map(data -> (int)Math.max(0.0, Math.floor(data.MP)))
               .orElse(0);
            return new ShadowMonarchManager.ShadowHealingResult(
               false, 0, 0, 0, "Not enough mana: " + quotedCost + " MP required (" + available + " available)."
            );
         }

         consumeSummonMana(owner, quotedCost);

         for (ShadowMonarchManager.ShadowHealingTarget target : selected) {
            LivingEntity living = target.entity();
            living.setHealth(living.getMaxHealth());
            if (living.level() instanceof ServerLevel level) {
               level.sendParticles(
                  SololevelingModParticleTypes.SHADOW_REVIVE.get(),
                  living.getX(),
                  living.getY() + living.getBbHeight() * 0.55,
                  living.getZ(),
                  10,
                  living.getBbWidth() * 0.3,
                  living.getBbHeight() * 0.25,
                  living.getBbWidth() * 0.3,
                  0.03
               );
               level.sendParticles(
                  ParticleTypes.SOUL_FIRE_FLAME,
                  living.getX(),
                  living.getY() + living.getBbHeight() * 0.5,
                  living.getZ(),
                  8,
                  living.getBbWidth() * 0.25,
                  living.getBbHeight() * 0.2,
                  living.getBbWidth() * 0.25,
                  0.02
               );
            }
         }

         owner.serverLevel()
            .playSound((Player)null, owner.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.9F, bossesOnly ? 1.15F : 0.95F);
         int restored = (int)Math.min(2.147483647E9, Math.ceil(missingHealth));
         int consumed = owner.getAbilities().instabuild ? 0 : quotedCost;
         return new ShadowMonarchManager.ShadowHealingResult(
            true,
            selected.size(),
            consumed,
            restored,
            "Restored "
               + restored
               + " health across "
               + selected.size()
               + (bossesOnly ? " boss shadow" : " shadow")
               + (selected.size() == 1 ? "" : "s")
               + " for "
               + consumed
               + " MP."
         );
      } else {
         return new ShadowMonarchManager.ShadowHealingResult(false, 0, 0, 0, "Only the Shadow Monarch can restore shadow soldiers.");
      }
   }

   private static List<ShadowMonarchManager.ShadowHealingTarget> healingTargets(ServerPlayer owner) {
      ArrayList<ShadowMonarchManager.ShadowHealingTarget> result = new ArrayList<>();
      if (owner != null && owner.server != null) {
         ensureRoster(owner);
         Set<UUID> seenEntities = new HashSet<>();
         ListTag roster = shadows(owner);

         for (int index = 0; index < roster.size(); index++) {
            CompoundTag shadow = roster.getCompound(index);
            if (shadow.hasUUID("summoned")) {
               UUID entityId = shadow.getUUID("summoned");
               if (seenEntities.add(entityId)
                  && findSummonedEntity(owner, entityId) instanceof LivingEntity living
                  && living.isAlive()
                  && !living.isRemoved()
                  && isOwnedShadow(living, owner)
                  && isCurrentSummonedInstance(owner, living)) {
                  double missing = Math.max(0.0, living.getMaxHealth() - living.getHealth());
                  if (!(missing <= 0.01)) {
                     result.add(new ShadowMonarchManager.ShadowHealingTarget(living, normalizeShadowType(shadow.getString("type")), missing));
                  }
               }
            }
         }

         return result;
      } else {
         return result;
      }
   }

   public static void tickCommandedShadows(ServerPlayer owner) {
      if (owner != null && owner.isAlive() && owner.level() instanceof ServerLevel level) {
         ArrayList var6 = new ArrayList();

         for (Mob mob : summonedOwnedMobs(owner)) {
            prepareShadowTraversal(mob);
            if ((level.getGameTime() + mob.getId()) % 20L < 10L) {
               synchronizeShadowLevel(owner, mob);
            }

            if (!isCurrentSummonedInstance(owner, mob)) {
               dropStoredShadowInventory(mob);
               mob.discard();
            } else {
               String command = commandOrDefault(mob.getPersistentData().getString("sl_shadow_command"));
               if (!applyOwnerCombatPriority(mob, owner)) {
                  if ("clear_dungeon".equals(command)) {
                     if (isInDungeon(owner)) {
                        var6.add(mob);
                     } else {
                        mob.setTarget(null);
                        mob.getNavigation().stop();
                     }
                  } else {
                     applyCommandTarget(mob, owner, command);
                  }
               }
            }
         }

         if (var6.isEmpty()) {
            CLEAR_DUNGEON_STATES.remove(owner.getUUID());
         } else {
            tickClearDungeonCoordinator(owner, var6);
         }

         cleanupClearDungeonStates(level.getGameTime());
      }
   }

   public static void tickCommandedShadow(Entity entity) {
      if (entity instanceof Mob mob && entity.level() instanceof ServerLevel level) {
         CompoundTag data = entity.getPersistentData();
         if (data.hasUUID("sl_shadow_owner")) {
            prepareShadowTraversal(mob);
            Player owner = findOnlineOwner(level, data.getUUID("sl_shadow_owner"));
            if (owner != null && (level.getGameTime() + entity.getId()) % 20L == 0L) {
               synchronizeShadowLevel(owner, entity);
            }

            if (owner != null && !isCurrentSummonedInstance(owner, entity)) {
               dropStoredShadowInventory(entity);
               entity.discard();
            } else {
               String command = commandOrDefault(data.getString("sl_shadow_command"));
               if (owner == null || !owner.isAlive()) {
                  mob.setTarget(null);
               } else if ("clear_dungeon".equals(command)) {
                  if (!isInDungeon(owner)) {
                     mob.setTarget(null);
                     mob.getNavigation().stop();
                  }
               } else {
                  applyCommandTarget(mob, owner, command);
               }
            }
         }
      }
   }

   public static void tickShadowTargeting(Mob shadow) {
      if (shadow != null && !shadow.level().isClientSide()) {
         Player owner = getShadowOwnerPlayer(shadow);
         if (owner == null || !owner.isAlive()) {
            shadow.setTarget(null);
         } else if (!applyOwnerCombatPriority(shadow, owner)) {
            if (Math.floorMod(shadow.tickCount + shadow.getId(), 5) == 0) {
               String command = commandOrDefault(shadow.getPersistentData().getString("sl_shadow_command"));
               if (!"clear_dungeon".equals(command)) {
                  applyCommandTarget(shadow, owner, command);
               } else {
                  LivingEntity current = shadow.getTarget();
                  if (!isInDungeon(owner) || current != null && !isValidClearDungeonTarget(current, shadow, owner)) {
                     shadow.setTarget(null);
                     shadow.getNavigation().stop();
                  }
               }
            }
         }
      }
   }

   private static boolean summonShadow(ServerLevel level, ServerPlayer owner, CompoundTag shadow, Vec3 pos, boolean allowRecall) {
      if (allowRecall && shadow.hasUUID("summoned")) {
         Entity existing = findSummonedEntity(owner, shadow.getUUID("summoned"));
         if (existing != null && existing.isAlive()) {
            if (SilladIcePrisonManager.isImprisoned(existing)) {
               SilladIcePrisonManager.guardManualDismiss(owner);
               return false;
            }

            if (existing.level() == level) {
               existing.teleportTo(pos.x, pos.y, pos.z);
               existing.setYRot(owner.getYRot());
               existing.setXRot(0.0F);
               applyLevelStats(existing, shadow, false);
               playSummonEffects(level, pos);
               return true;
            }

            return recallShadowFromOtherDimension(level, owner, shadow, existing, pos);
         }

         shadow.remove("summoned");
         owner.getPersistentData().put("sololeveling_shadow_monarch", root(owner));
      }

      EntityType<?> type = entityType(shadow.getString("type"));
      if (type == null) {
         return false;
      }

      int manaCost = summonManaCost(shadow);
      if (!hasSummonMana(owner, manaCost)) {
         notifyInsufficientSummonMana(owner, shadow, manaCost);
         return false;
      }

      Entity spawned = type.spawn(level, BlockPos.containing(pos), MobSpawnType.MOB_SUMMONED);
      if (spawned == null) {
         return false;
      }

      spawned.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), 0.0F);
      tagSummonedEntity(owner, shadow, spawned);
      consumeSummonMana(owner, manaCost);
      updateLegacySpawnCounter(owner, shadow.getString("type"), 1);
      playSummonEffects(level, pos);
      return true;
   }

   private static boolean recallShadowFromOtherDimension(ServerLevel level, ServerPlayer owner, CompoundTag shadow, Entity existing, Vec3 pos) {
      EntityType<?> type = entityType(shadow.getString("type"));
      if (type == null) {
         return false;
      }

      int manaCost = summonManaCost(shadow);
      if (!hasSummonMana(owner, manaCost)) {
         notifyInsufficientSummonMana(owner, shadow, manaCost);
         return false;
      }

      ListTag carriedInventory = copyShadowInventory(existing);
      saveBossHealthBeforeDespawn(owner, existing);
      Entity spawned = type.spawn(level, BlockPos.containing(pos), MobSpawnType.MOB_SUMMONED);
      if (spawned == null) {
         return false;
      }

      existing.discard();
      spawned.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), 0.0F);
      tagSummonedEntity(owner, shadow, spawned);
      consumeSummonMana(owner, manaCost);
      if (carriedInventory != null) {
         spawned.getPersistentData().put("sl_shadow_inventory", carriedInventory);
      }

      playSummonEffects(level, pos);
      return true;
   }

   private static ListTag copyShadowInventory(Entity entity) {
      return entity != null && entity.getPersistentData().contains("sl_shadow_inventory", 9)
         ? entity.getPersistentData().getList("sl_shadow_inventory", 10).copy()
         : null;
   }

   private static void playSummonEffects(ServerLevel level, Vec3 pos) {
      LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
      if (bolt != null) {
         bolt.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(pos.x, pos.y - 1.0, pos.z)));
         bolt.setVisualOnly(true);
         level.addFreshEntity(bolt);
      }

      level.sendParticles(SololevelingModParticleTypes.SHADOW_REVIVE.get(), pos.x, pos.y + 1.6, pos.z, 20, 0.35, 0.65, 0.35, 0.04);
      level.sendParticles(ParticleTypes.SQUID_INK, pos.x, pos.y + 1.0, pos.z, 80, 1.3, 1.2, 1.3, 0.18);
      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y + 0.8, pos.z, 24, 0.8, 0.8, 0.8, 0.04);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.2, pos.z, 35, 0.6, 0.9, 0.6, 0.15);
   }

   private static int summonManaCost(CompoundTag shadow) {
      String type = shadow.getString("type");

      int baseCost = switch (type) {
         case "goblin_club" -> 8;
         case "goblin_archer" -> 10;
         case "goblin_mage", "wolf" -> 14;
         case "knight" -> 18;
         case "polar_bear", "orc" -> 22;
         case "high_orc" -> 32;
         case "iron" -> 55;
         case "igris" -> 90;
         case "tusk" -> 120;
         case "kaisel" -> 140;
         case "beru" -> 180;
         case "kamish" -> 260;
         default -> 12;
      };

      double rankMultiplier = switch (rankOf(shadow)) {
         case 1 -> 1.35;
         case 2 -> 1.8;
         case 3 -> 2.35;
         case 4 -> 3.0;
         case 5 -> 3.8;
         case 6 -> 5.0;
         default -> 1.0;
      };
      double levelMultiplier = 1.0 + Math.max(0, shadow.getInt("level") - 1) * 0.0125;
      return Math.max(1, (int)Math.ceil(baseCost * rankMultiplier * levelMultiplier));
   }

   private static boolean hasSummonMana(Player player, int cost) {
      return player != null && player.getAbilities().instabuild
         ? true
         : player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(variables -> variables.MP >= cost).orElse(false);
   }

   private static void consumeSummonMana(Player player, int cost) {
      if (player == null || !player.getAbilities().instabuild) {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.MP = Math.max(0.0, capability.MP - cost);
            capability.syncPlayerVariables(player);
         });
      }
   }

   private static void notifyInsufficientSummonMana(ServerPlayer player, CompoundTag shadow, int cost) {
      long now = player.level().getGameTime();
      if (player.getPersistentData().getLong("sl_shadow_mana_notice") <= now) {
         player.getPersistentData().putLong("sl_shadow_mana_notice", now + 20L);
         SystemNotifications.showNegativeTitleUnder(
            player,
            -49810,
            70,
            Component.literal("NOT ENOUGH MANA").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
            Component.literal(shadow.getString("name") + " requires " + cost + " MP").withStyle(ChatFormatting.GRAY)
         );
      }
   }

   private static void tagSummonedEntity(Player owner, CompoundTag shadow, Entity spawned) {
      spawned.getPersistentData().putString("sl_shadow_id", shadow.getString("id"));
      spawned.getPersistentData().putString("sl_shadow_type", shadow.getString("type"));
      spawned.getPersistentData().putUUID("sl_shadow_owner", owner.getUUID());
      spawned.getPersistentData().putLong("sl_shadow_generation", owner.getPersistentData().getLong("sl_shadow_reset_generation"));
      String command = commandOrDefault(owner.getPersistentData().getString("sl_shadow_command_mode"));
      spawned.getPersistentData().putString("sl_shadow_command", command);
      shadow.putUUID("summoned", spawned.getUUID());
      owner.getPersistentData().put("sololeveling_shadow_monarch", root(owner));
      if (spawned instanceof TamableAnimal tame) {
         tame.tame(owner);
      }

      syncEquipmentTag(spawned, shadow);
      applyLevelStats(spawned, shadow, true);
      if (spawned instanceof Mob mob) {
         applyCommandTarget(mob, owner, command);
      }
   }

   private static Vec3 spreadSummonPosition(Player player, Vec3 origin, int index) {
      if (index <= 0) {
         return origin;
      }

      Vec3 look = player.getLookAngle();
      Vec3 forward = new Vec3(look.x, 0.0, look.z);
      if (forward.lengthSqr() < 0.001) {
         forward = new Vec3(0.0, 0.0, 1.0);
      }

      forward = forward.normalize();
      Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
      int ringIndex = index - 1;
      int slot = ringIndex % 8;
      int ring = ringIndex / 8;
      double radius = 1.6 + ring * 1.15;
      double angle = slot * (Math.PI / 4) + ring * 0.35;
      return origin.add(right.scale(Math.cos(angle) * radius)).add(forward.scale(Math.sin(angle) * radius));
   }

   private static void applyCommandTarget(Mob shadow, Player owner, String command) {
      if (!applyOwnerCombatPriority(shadow, owner)) {
         if ("default".equals(command)) {
            shadow.setTarget(findDefaultCommandTarget(shadow, owner));
         } else if ("follow".equals(command)) {
            shadow.setTarget(null);
            shadow.getNavigation().stop();
         } else if ("clear_dungeon".equals(command) && !isInDungeon(owner)) {
            shadow.setTarget(null);
            shadow.getNavigation().stop();
         } else if (!"clear_dungeon".equals(command)) {
            if ("protect".equals(command)) {
               LivingEntity threat = findOwnerThreat(shadow, owner);
               shadow.setTarget(threat);
            } else {
               LivingEntity current = shadow.getTarget();
               if (!isValidShadowTarget(current, shadow, owner)) {
                  LivingEntity target = findNearestHostile(shadow, owner, 48.0);
                  shadow.setTarget(target);
               }
            }
         }
      }
   }

   private static void resetShadowCommandState(Mob shadow) {
      shadow.setTarget(null);
      shadow.getNavigation().stop();
      CompoundTag data = shadow.getPersistentData();
      data.putDouble("MF", 0.0);
      data.putBoolean("sprint", false);
      if (shadow instanceof IgrisShadowEntity igris) {
         data.putString("state", "idle");
         igris.animationprocedure = "empty";
         igris.setAnimation("empty");
      }
   }

   public static LivingEntity findDefaultCommandTarget(Mob shadow, Player owner) {
      if (shadow != null && owner != null) {
         LivingEntity priority = findOwnerCombatPriorityTarget(shadow, owner);
         if (priority != null) {
            return priority;
         }

         LivingEntity current = shadow.getTarget();
         if (isDefaultLinkedTarget(current, shadow, owner)) {
            return current;
         }

         LivingEntity ownerTarget = owner.getLastHurtMob();
         if (isValidOwnerDirectedTarget(ownerTarget, shadow, owner)) {
            return ownerTarget;
         }

         LivingEntity ownerAttacker = owner.getLastHurtByMob();
         return isValidShadowTarget(ownerAttacker, shadow, owner)
            ? ownerAttacker
            : shadow.level()
               .getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(32.0), mob -> mob.getTarget() == owner && isValidShadowTarget(mob, shadow, owner))
               .stream()
               .min((a, b) -> Double.compare(a.distanceToSqr(shadow), b.distanceToSqr(shadow)))
               .orElse(null);
      } else {
         return null;
      }
   }

   private static boolean isDefaultLinkedTarget(LivingEntity target, Mob shadow, Player owner) {
      if (target == owner.getLastHurtMob()) {
         return isValidOwnerDirectedTarget(target, shadow, owner);
      } else {
         return !isValidShadowTarget(target, shadow, owner)
            ? false
            : target == owner.getLastHurtByMob() || target instanceof Mob mob && isProtectTarget(mob.getTarget(), owner);
      }
   }

   private static boolean isValidOwnerDirectedTarget(LivingEntity target, Mob shadow, Player owner) {
      if (target == null
         || !target.isAlive()
         || target.level() != shadow.level()
         || target == shadow
         || target == owner
         || !target.isAttackable()
         || target.isInvulnerable()) {
         return false;
      }

      if (!target.getType().is(SHADOW_ENTITY_TAG) && !owner.isAlliedTo(target) && !shadow.isAlliedTo(target)) {
         if (target instanceof TamableAnimal tame && owner.getUUID().equals(tame.getOwnerUUID())) {
            return false;
         } else {
            Player targetPlayer = target instanceof Player player
               ? player
               : (target instanceof TamableAnimal tame && tame.getOwner() instanceof Player player ? player : null);
            return targetPlayer != null && haveSameGuild(owner, targetPlayer) ? false : MageCombatHelper.isValidTarget(shadow, target);
         }
      } else {
         return false;
      }
   }

   public static LivingEntity findOwnerCombatPriorityTarget(Mob shadow) {
      return findOwnerCombatPriorityTarget(shadow, getShadowOwnerPlayer(shadow));
   }

   public static LivingEntity findOwnerCombatPriorityTarget(Mob shadow, Player owner) {
      if (shadow != null && owner != null && owner.isAlive() && owner.level() == shadow.level()) {
         LivingEntity attacked = owner.getLastHurtMob();
         LivingEntity attacker = owner.getLastHurtByMob();
         boolean attackedValid = isValidOwnerDirectedTarget(attacked, shadow, owner);
         boolean attackerValid = isValidOwnerDirectedTarget(attacker, shadow, owner);
         if (attackedValid && attackerValid) {
            return owner.getLastHurtMobTimestamp() >= owner.getLastHurtByMobTimestamp() ? attacked : attacker;
         } else if (attackedValid) {
            return attacked;
         } else {
            return attackerValid ? attacker : null;
         }
      } else {
         return null;
      }
   }

   private static boolean applyOwnerCombatPriority(Mob shadow, Player owner) {
      LivingEntity priority = findOwnerCombatPriorityTarget(shadow, owner);
      if (priority == null) {
         return false;
      }

      if (shadow.getTarget() != priority) {
         shadow.setTarget(priority);
      }

      return true;
   }

   private static LivingEntity findOwnerThreat(Mob shadow, Player owner) {
      LivingEntity current = shadow.getTarget();
      if (isProtectThreat(current, shadow, owner)) {
         return current;
      }

      LivingEntity attacker = owner.getLastHurtByMob();
      if (isValidShadowTarget(attacker, shadow, owner)) {
         return attacker;
      }

      AABB searchArea = owner.getBoundingBox().minmax(shadow.getBoundingBox()).inflate(48.0);
      return shadow.level()
         .getEntitiesOfClass(Mob.class, searchArea, mob -> isProtectThreat(mob, shadow, owner))
         .stream()
         .min((a, b) -> Double.compare(a.distanceToSqr(shadow), b.distanceToSqr(shadow)))
         .orElse(null);
   }

   private static boolean isProtectThreat(LivingEntity candidate, Mob shadow, Player owner) {
      if (!isValidShadowTarget(candidate, shadow, owner)) {
         return false;
      } else {
         return candidate == owner.getLastHurtByMob() ? true : candidate instanceof Mob mob && isProtectTarget(mob.getTarget(), owner);
      }
   }

   private static boolean isProtectTarget(LivingEntity target, Player owner) {
      return target == owner || isOwnedShadow(target, owner);
   }

   private static LivingEntity findNearestHostile(Mob shadow, Player owner, double range) {
      return shadow.level()
         .getEntitiesOfClass(LivingEntity.class, shadow.getBoundingBox().inflate(range), target -> isValidShadowTarget(target, shadow, owner))
         .stream()
         .min((a, b) -> Double.compare(a.distanceToSqr(shadow), b.distanceToSqr(shadow)))
         .orElse(null);
   }

   public static boolean shouldFollowOwner(Entity shadow) {
      if (shadow == null) {
         return false;
      }

      String command = commandOrDefault(shadow.getPersistentData().getString("sl_shadow_command"));
      return "default".equals(command) || "protect".equals(command) || "follow".equals(command);
   }

   public static boolean isValidClearDungeonTarget(LivingEntity target, Mob shadow, Player owner) {
      return owner instanceof ServerPlayer serverOwner && target != null && target.level() == owner.level() && isValidShadowTarget(target, shadow, owner)
         ? matchesDungeonContext(target, serverOwner, dungeonTargetContext(serverOwner))
         : false;
   }

   private static boolean isValidShadowTarget(LivingEntity target, Mob shadow, Player owner) {
      if (target == null
         || !target.isAlive()
         || target.level() != shadow.level()
         || target == shadow
         || target == owner
         || !target.isAttackable()
         || target.isInvulnerable()) {
         return false;
      } else if (target instanceof Player) {
         return false;
      } else if (target.getType().is(SHADOW_ENTITY_TAG)) {
         return false;
      } else if (target instanceof TamableAnimal tame && owner.getUUID().equals(tame.getOwnerUUID())) {
         return false;
      } else {
         Player targetPlayer = target instanceof TamableAnimal tame && tame.getOwner() instanceof Player player ? player : null;
         if (targetPlayer != null && haveSameGuild(owner, targetPlayer)) {
            return false;
         } else {
            return !MageCombatHelper.isValidTarget(shadow, target)
               ? false
               : target instanceof Monster || target instanceof Mob mob && isProtectTarget(mob.getTarget(), owner);
         }
      }
   }

   public static boolean canReachShadowTarget(Mob shadow, LivingEntity target) {
      if (shadow == null || target == null || !target.isAlive()) {
         return false;
      }

      if (CombatRangeHelper.withinSurfaceRange(shadow, target, 6.0) && shadow.hasLineOfSight(target)) {
         return true;
      }

      Path path = shadow.getNavigation().createPath(target.blockPosition(), 0);
      return path != null && path.canReach();
   }

   private static void tickClearDungeonCoordinator(ServerPlayer owner, List<Mob> shadows) {
      ServerLevel level = owner.serverLevel();
      long now = level.getGameTime();
      ShadowMonarchManager.DungeonTargetContext context = dungeonTargetContext(owner);
      ShadowMonarchManager.ClearDungeonState state = CLEAR_DUNGEON_STATES.computeIfAbsent(
         owner.getUUID(), ignored -> new ShadowMonarchManager.ClearDungeonState()
      );
      if (state.lastSeenTick > now || !context.key().equals(state.contextKey)) {
         state.reset(context.key(), now);

         for (Mob shadow : shadows) {
            shadow.setTarget(null);
            shadow.getNavigation().stop();
         }
      }

      state.lastSeenTick = now;
      state.failedUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
      state.progress.keySet().removeIf(shadowId -> shadows.stream().noneMatch(shadowx -> shadowx.getUUID().equals(shadowId)));
      if (now >= state.nextCandidateScanTick) {
         state.candidateIds = collectClearDungeonCandidates(owner, shadows, context).stream().map(Entity::getUUID).toList();
         state.nextCandidateScanTick = now + 20L;
      }

      Map<UUID, Integer> assignments = new HashMap<>();

      for (Mob shadow : shadows) {
         LivingEntity current = shadow.getTarget();
         if (isValidClearDungeonTarget(current, shadow, owner) && !isFailedTarget(state, shadow, current, now)) {
            assignments.merge(current.getUUID(), 1, Integer::sum);
         }
      }

      ShadowMonarchManager.PathAttemptBudget pathBudget = new ShadowMonarchManager.PathAttemptBudget();

      for (Mob shadow : shadows) {
         LivingEntity current = shadow.getTarget();
         if (current != null || !tickClearTraversal(owner, shadow, state, now, pathBudget)) {
            if (isValidClearDungeonTarget(current, shadow, owner) && !isFailedTarget(state, shadow, current, now)) {
               if (tickClearTargetProgress(state, shadow, current, now, pathBudget)) {
                  continue;
               }

               assignments.computeIfPresent(current.getUUID(), (id, count) -> count > 1 ? count - 1 : null);
            } else {
               shadow.setTarget(null);
               state.progress.remove(shadow.getUUID());
            }

            assignClearDungeonTarget(owner, shadow, state, assignments, now, pathBudget);
         }
      }
   }

   private static boolean tickClearTargetProgress(
      ShadowMonarchManager.ClearDungeonState state, Mob shadow, LivingEntity target, long now, ShadowMonarchManager.PathAttemptBudget pathBudget
   ) {
      ShadowMonarchManager.ClearShadowProgress progress = state.progress
         .computeIfAbsent(shadow.getUUID(), ignored -> new ShadowMonarchManager.ClearShadowProgress());
      double distance = shadow.distanceToSqr(target);
      if (!target.getUUID().equals(progress.targetId)) {
         progress.reset(target, shadow, distance, now);
         return true;
      }

      progress.traversing = false;
      progress.lastShadowPosition = shadow.position();
      boolean hasUsefulSight = shadow.hasLineOfSight(target);
      boolean closeEnoughToFight = hasUsefulSight && CombatRangeHelper.withinSurfaceRange(shadow, target, 7.0);
      boolean damagedTarget = target.getHealth() + 0.01F < progress.lastTargetHealth;
      if (closeEnoughToFight || damagedTarget || distance + 1.0 < progress.lastDistance) {
         progress.lastProgressTick = now;
      }

      progress.lastDistance = distance;
      progress.lastTargetHealth = target.getHealth();
      if (!hasUsefulSight && shadow.getNavigation().isDone() && now >= progress.nextRepathTick && pathBudget.tryUse()) {
         if (!startClearDungeonPath(shadow, target)) {
            advanceTowardObjective(shadow, target);
            shadow.setTarget(null);
            progress.beginTraversal(target, shadow, distance, now);
            return true;
         }

         progress.nextRepathTick = now + clearRepathDelay(shadow);
      }

      if (now - progress.lastProgressTick < 80L) {
         return true;
      } else if (isClearDungeonBoss(target)) {
         progress.lastProgressTick = now;
         return true;
      } else {
         failClearDungeonTarget(state, shadow, target, now);
         return false;
      }
   }

   private static void assignClearDungeonTarget(
      ServerPlayer owner,
      Mob shadow,
      ShadowMonarchManager.ClearDungeonState state,
      Map<UUID, Integer> assignments,
      long now,
      ShadowMonarchManager.PathAttemptBudget pathBudget
   ) {
      if (shadow.level() instanceof ServerLevel level) {
         HashSet var13 = new HashSet();
         int objective = 0;

         LivingEntity candidate;
         while (true) {
            label83: {
               if (objective < 5) {
                  candidate = selectClearDungeonCandidate(owner, shadow, state, assignments, var13, now, level, 676.0);
                  if (candidate != null) {
                     var13.add(candidate.getUUID());
                     boolean hasSight = shadow.hasLineOfSight(candidate);
                     if (shadow instanceof ShadowKaiselinEntity && !hasSight) {
                        failClearDungeonTarget(state, shadow, candidate, now);
                        break label83;
                     }

                     if (hasSight || pathBudget.tryUse()) {
                        ShadowMonarchManager.ClearShadowProgress progress = state.progress
                           .computeIfAbsent(shadow.getUUID(), ignored -> new ShadowMonarchManager.ClearShadowProgress());
                        if (!hasSight && !startClearDungeonPath(shadow, candidate)) {
                           if (advanceTowardObjective(shadow, candidate)) {
                              shadow.setTarget(null);
                              progress.beginTraversal(candidate, shadow, shadow.distanceToSqr(candidate), now);
                              break;
                           }

                           failClearDungeonTarget(state, shadow, candidate, now);
                           break label83;
                        }

                        shadow.setTarget(candidate);
                        progress.reset(candidate, shadow, shadow.distanceToSqr(candidate), now);
                        break;
                     }
                  }
               }

               LivingEntity objectivex = selectClearDungeonObjective(owner, shadow, state, level);
               if (objectivex == null) {
                  shadow.setTarget(null);
                  state.progress.remove(shadow.getUUID());
                  if (shadow.distanceToSqr(owner) > 36.0) {
                     advanceTowardObjective(shadow, owner);
                  } else {
                     shadow.getNavigation().stop();
                  }

                  return;
               }

               ShadowMonarchManager.ClearShadowProgress progress = state.progress
                  .computeIfAbsent(shadow.getUUID(), ignored -> new ShadowMonarchManager.ClearShadowProgress());
               if (!shadow.hasLineOfSight(objectivex) && !startClearDungeonPath(shadow, objectivex)) {
                  shadow.setTarget(null);
                  if (!objectivex.getUUID().equals(progress.targetId) || !progress.traversing) {
                     progress.beginTraversal(objectivex, shadow, shadow.distanceToSqr(objectivex), now);
                  }

                  if (now >= progress.nextRepathTick) {
                     advanceTowardObjective(shadow, objectivex);
                     progress.nextRepathTick = now + 20L;
                  }
               } else {
                  shadow.setTarget(objectivex);
                  progress.reset(objectivex, shadow, shadow.distanceToSqr(objectivex), now);
               }

               return;
            }

            objective++;
         }

         assignments.merge(candidate.getUUID(), 1, Integer::sum);
      }
   }

   private static LivingEntity selectClearDungeonCandidate(
      ServerPlayer owner,
      Mob shadow,
      ShadowMonarchManager.ClearDungeonState state,
      Map<UUID, Integer> assignments,
      Set<UUID> examined,
      long now,
      ServerLevel level,
      double maxDistanceSqr
   ) {
      LivingEntity best = null;
      int bestAssignments = Integer.MAX_VALUE;
      double bestDistance = Double.MAX_VALUE;

      for (UUID candidateId : state.candidateIds) {
         if (!examined.contains(candidateId)
            && level.getEntity(candidateId) instanceof LivingEntity candidate
            && isValidClearDungeonTarget(candidate, shadow, owner)
            && !isFailedTarget(state, shadow, candidate, now)) {
            int assigned = assignments.getOrDefault(candidateId, 0);
            double distance = shadow.distanceToSqr(candidate);
            if (!(distance > maxDistanceSqr) && (assigned < bestAssignments || assigned == bestAssignments && distance < bestDistance)) {
               best = candidate;
               bestAssignments = assigned;
               bestDistance = distance;
            }
         }
      }

      return best;
   }

   private static boolean isClearDungeonBoss(LivingEntity target) {
      if (target == null) {
         return false;
      } else {
         return target.getType().is(SOLO_BOSS_TAG)
            ? true
            : DungeonMobLevelAdapter.MobRole.fromString(target.getPersistentData().getString("slr_dungeon_role")) == DungeonMobLevelAdapter.MobRole.BOSS;
      }
   }

   private static boolean advanceTowardObjective(Mob shadow, LivingEntity objective) {
      if (objective == null) {
         return false;
      } else {
         double x = objective.getX();
         double y = objective.getY();
         double z = objective.getZ();
         shadow.getLookControl().setLookAt(objective, 30.0F, 30.0F);
         if (shadow instanceof ShadowKaiselinEntity) {
            shadow.getMoveControl().setWantedPosition(x, y, z, 1.15);
            return true;
         } else {
            Path path = shadow.getNavigation().createPath(objective.blockPosition(), 0);
            return path != null && shadow.getNavigation().moveTo(path, 1.15) ? true : shadow.getNavigation().moveTo(x, y, z, 1.15);
         }
      }
   }

   private static LivingEntity selectClearDungeonObjective(ServerPlayer owner, Mob shadow, ShadowMonarchManager.ClearDungeonState state, ServerLevel level) {
      LivingEntity boss = null;
      double bossDistance = Double.MAX_VALUE;
      LivingEntity nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (UUID candidateId : state.candidateIds) {
         if (level.getEntity(candidateId) instanceof LivingEntity candidate && isValidClearDungeonTarget(candidate, shadow, owner)) {
            double distance = shadow.distanceToSqr(candidate);
            if (isClearDungeonBoss(candidate)) {
               if (distance < bossDistance) {
                  boss = candidate;
                  bossDistance = distance;
               }
            } else if (distance < nearestDistance) {
               nearest = candidate;
               nearestDistance = distance;
            }
         }
      }

      return boss != null ? boss : nearest;
   }

   private static boolean startClearDungeonPath(Mob shadow, LivingEntity target) {
      if (shadow instanceof ShadowKaiselinEntity) {
         return shadow.hasLineOfSight(target);
      }

      Path path = shadow.getNavigation().createPath(target.blockPosition(), 0);
      return path != null && path.canReach() ? shadow.getNavigation().moveTo(path, 1.0) : false;
   }

   private static boolean tickClearTraversal(
      ServerPlayer owner, Mob shadow, ShadowMonarchManager.ClearDungeonState state, long now, ShadowMonarchManager.PathAttemptBudget pathBudget
   ) {
      ShadowMonarchManager.ClearShadowProgress progress = state.progress.get(shadow.getUUID());
      if (progress != null && progress.traversing && progress.targetId != null) {
         if (owner.serverLevel().getEntity(progress.targetId) instanceof LivingEntity objective
            && isValidClearDungeonTarget(objective, shadow, owner)
            && !isFailedTarget(state, shadow, objective, now)) {
            if (shadow instanceof BeruShadowEntity beru && (shadow.isInWaterOrBubble() || !shadow.onGround() && shadow.fallDistance > 1.5F)) {
               beru.beginTraversalRecoveryFlight(objective);
            }

            double distance = shadow.distanceToSqr(objective);
            double movedSqr = shadow.position().distanceToSqr(progress.lastShadowPosition);
            if (movedSqr > 0.025 || distance + 1.0 < progress.lastDistance) {
               progress.lastProgressTick = now;
            }

            progress.lastDistance = distance;
            progress.lastShadowPosition = shadow.position();
            if (!shadow.hasLineOfSight(objective) && (now < progress.nextRepathTick || !pathBudget.tryUse() || !startClearDungeonPath(shadow, objective))) {
               if (shadow.getNavigation().isDone() && now >= progress.nextRepathTick) {
                  if (pathBudget.tryUse()) {
                     advanceTowardObjective(shadow, objective);
                  }

                  progress.nextRepathTick = now + clearRepathDelay(shadow);
               }

               if (now - progress.lastProgressTick < 80L) {
                  return true;
               } else if (tryRecallShadowNearOwner(shadow, owner, objective)) {
                  progress.lastProgressTick = now;
                  progress.lastDistance = shadow.distanceToSqr(objective);
                  progress.lastShadowPosition = shadow.position();
                  progress.nextRepathTick = now + 5L;
                  return true;
               } else if (shadow instanceof BeruShadowEntity beru) {
                  beru.beginTraversalRecoveryFlight(objective);
                  progress.lastProgressTick = now;
                  return true;
               } else if (isClearDungeonBoss(objective)) {
                  progress.lastProgressTick = now;
                  progress.nextRepathTick = now + clearRepathDelay(shadow);
                  return true;
               } else {
                  failClearDungeonTarget(state, shadow, objective, now);
                  return false;
               }
            } else {
               shadow.setTarget(objective);
               progress.reset(objective, shadow, distance, now);
               return true;
            }
         } else {
            state.progress.remove(shadow.getUUID());
            shadow.getNavigation().stop();
            return false;
         }
      } else {
         return false;
      }
   }

   private static void prepareShadowTraversal(Mob shadow) {
      if (shadow != null) {
         shadow.getNavigation().setCanFloat(true);
         if (shadow.maxUpStep() < 1.1F) {
            shadow.setMaxUpStep(1.1F);
         }

         if ((shadow.isInWaterOrBubble() || shadow.horizontalCollision) && !shadow.getNavigation().isDone()) {
            shadow.getJumpControl().jump();
         }
      }
   }

   private static boolean tryRecallShadowNearOwner(Mob shadow, ServerPlayer owner, LivingEntity objective) {
      if (!(shadow.distanceToSqr(owner) < 100.0) && !(owner.distanceToSqr(objective) + 64.0 >= shadow.distanceToSqr(objective))) {
         ServerLevel level = owner.serverLevel();
         BlockPos origin = owner.blockPosition();

         for (int radius = 2; radius <= 4; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                     for (int dy = -1; dy <= 2; dy++) {
                        BlockPos position = origin.offset(dx, dy, dz);
                        if (isSafeShadowRecoveryPosition(level, shadow, position)) {
                           shadow.getNavigation().stop();
                           shadow.teleportTo(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
                           shadow.fallDistance = 0.0F;
                           return true;
                        }
                     }
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean tryRecoverStuckShadowNearOwner(Mob shadow, LivingEntity objective) {
      return getShadowOwnerPlayer(shadow) instanceof ServerPlayer serverOwner && objective != null && tryRecallShadowNearOwner(shadow, serverOwner, objective);
   }

   private static boolean isSafeShadowRecoveryPosition(ServerLevel level, Mob shadow, BlockPos position) {
      if (level.hasChunkAt(position) && level.getWorldBorder().isWithinBounds(position)) {
         BlockPos floorPos = position.below();
         BlockState floor = level.getBlockState(floorPos);
         if (floor.isFaceSturdy(level, floorPos, Direction.UP) && floor.getFluidState().isEmpty()) {
            Vec3 destination = Vec3.atBottomCenterOf(position);
            AABB moved = shadow.getBoundingBox().move(destination.subtract(shadow.position()));
            return level.noCollision(shadow, moved);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static long clearRepathDelay(Mob shadow) {
      return 30 + Math.floorMod(shadow.getId(), 10);
   }

   private static void failClearDungeonTarget(ShadowMonarchManager.ClearDungeonState state, Mob shadow, LivingEntity target, long now) {
      state.failedUntil.put(new ShadowMonarchManager.ClearTargetKey(shadow.getUUID(), target.getUUID()), now + 120L);
      state.progress.remove(shadow.getUUID());
      if (shadow.getTarget() == target) {
         shadow.setTarget(null);
      }

      shadow.getNavigation().stop();
   }

   private static boolean isFailedTarget(ShadowMonarchManager.ClearDungeonState state, Mob shadow, LivingEntity target, long now) {
      return state.failedUntil.getOrDefault(new ShadowMonarchManager.ClearTargetKey(shadow.getUUID(), target.getUUID()), 0L) > now;
   }

   private static List<LivingEntity> collectClearDungeonCandidates(ServerPlayer owner, List<Mob> shadows, ShadowMonarchManager.DungeonTargetContext context) {
      ServerLevel level = owner.serverLevel();
      Comparator<LivingEntity> nearestFirst = Comparator.comparingDouble(candidatex -> distanceToShadowGroup(candidatex, shadows));
      PriorityQueue<LivingEntity> nearest = new PriorityQueue<>(128, nearestFirst.reversed());
      Set<UUID> seen = new HashSet<>();
      if (context.instanceId() != null) {
         DungeonInstanceSavedData.get(level)
            .getInstance(context.instanceId())
            .ifPresent(
               instance -> {
                  if (instance.dimension().equals(level.dimension())
                     && (instance.participants().isEmpty() || instance.participants().contains(owner.getUUID()))) {
                     for (DungeonInstanceSavedData.EncounterState encounter : instance.encounters()) {
                        if (encounter.activated() && !encounter.completed()) {
                           for (UUID mobId : encounter.trackedMobs()) {
                              if (level.getEntity(mobId) instanceof LivingEntity candidatex
                                 && seen.add(candidatex.getUUID())
                                 && isValidClearDungeonTarget(candidatex, shadows.get(0), owner)) {
                                 offerBoundedCandidate(nearest, candidatex, nearestFirst);
                              }
                           }
                        }
                     }
                  }
               }
            );
      } else {
         for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity candidate && seen.add(candidate.getUUID()) && isValidClearDungeonTarget(candidate, shadows.get(0), owner)) {
               offerBoundedCandidate(nearest, candidate, nearestFirst);
            }
         }
      }

      ArrayList<LivingEntity> result = new ArrayList<>(nearest);
      result.sort(nearestFirst);
      return result;
   }

   private static void offerBoundedCandidate(PriorityQueue<LivingEntity> candidates, LivingEntity candidate, Comparator<LivingEntity> nearestFirst) {
      if (candidates.size() < 128) {
         candidates.offer(candidate);
      } else {
         LivingEntity farthest = candidates.peek();
         if (farthest != null && nearestFirst.compare(candidate, farthest) < 0) {
            candidates.poll();
            candidates.offer(candidate);
         }
      }
   }

   private static double distanceToShadowGroup(LivingEntity candidate, List<Mob> shadows) {
      double closest = Double.MAX_VALUE;

      for (Mob shadow : shadows) {
         closest = Math.min(closest, candidate.distanceToSqr(shadow));
      }

      return closest;
   }

   private static ShadowMonarchManager.DungeonTargetContext dungeonTargetContext(ServerPlayer owner) {
      CompoundTag data = owner.getPersistentData();
      String instanceText = data.getString("slr_dungeon_instance").trim();
      UUID instanceId = parseUuid(instanceText);
      String legacyTag = data.getString("dungeon_tag");
      boolean strictLegacyTag = data.getBoolean("slr_procedural_dungeon");
      int dkcFloor = DkcFloorRegistry.isSharedDkc(owner.level()) ? DkcSpatialLayout.floor(owner) : 0;
      String key = owner.level().dimension().location()
         + "|"
         + (instanceId == null ? "" : instanceId)
         + "|"
         + legacyTag
         + "|"
         + strictLegacyTag
         + "|"
         + dkcFloor;
      return new ShadowMonarchManager.DungeonTargetContext(key, instanceId, legacyTag, strictLegacyTag, dkcFloor);
   }

   private static boolean matchesDungeonContext(LivingEntity target, ServerPlayer owner, ShadowMonarchManager.DungeonTargetContext context) {
      if (target.level() != owner.level()) {
         return false;
      }

      if (context.instanceId() != null) {
         return context.instanceId().toString().equals(target.getPersistentData().getString("slr_dungeon_instance"));
      }

      if (context.dkcFloor() > 0) {
         return DkcSpatialLayout.isEntityInOwnedFloor(target, owner.getUUID(), context.dkcFloor());
      }

      if (context.legacyTag().isEmpty()) {
         return target.level().dimension().equals(owner.level().dimension());
      }

      String targetTag = target.getPersistentData().getString("dungeon_tag");
      return context.legacyTag().equals(targetTag) || !context.strictLegacyTag() && targetTag.isEmpty();
   }

   private static UUID parseUuid(String value) {
      if (value != null && !value.isBlank()) {
         try {
            return UUID.fromString(value);
         } catch (IllegalArgumentException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static List<Mob> summonedOwnedMobs(ServerPlayer owner) {
      ArrayList<Mob> result = new ArrayList<>();
      ListTag roster = shadows(owner);
      ServerLevel level = owner.serverLevel();

      for (int index = 0; index < roster.size(); index++) {
         CompoundTag shadow = roster.getCompound(index);
         if (shadow.hasUUID("summoned") && level.getEntity(shadow.getUUID("summoned")) instanceof Mob mob && mob.isAlive()) {
            result.add(mob);
         }
      }

      return result;
   }

   private static void cleanupClearDungeonStates(long now) {
      if (now % 1200L == 0L) {
         CLEAR_DUNGEON_STATES.entrySet().removeIf(entry -> entry.getValue().lastSeenTick > now || now - entry.getValue().lastSeenTick > 1200L);
      }
   }

   private static String normalizeCommand(String command) {
      if (command == null) {
         return "";
      }

      String value = command.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

      return switch (value) {
         case "default", "protect", "berserk", "follow", "clear_dungeon" -> value;
         default -> "";
      };
   }

   private static String commandOrDefault(String command) {
      String normalized = normalizeCommand(command);
      return normalized.isEmpty() ? "default" : normalized;
   }

   private static String commandDisplayName(String command) {
      return switch (command) {
         case "default" -> "Default";
         case "protect" -> "Protect";
         case "berserk" -> "Berserk";
         case "follow" -> "Follow";
         case "clear_dungeon" -> "Clear Dungeon";
         default -> "Unknown";
      };
   }

   private static void applyLevelStats(Entity entity, CompoundTag shadow, boolean restoreSavedBossHealth) {
      if (entity instanceof LivingEntity living) {
         int level = Math.max(1, shadow.getInt("level"));
         int rank = rankOf(shadow);
         String type = shadow.getString("type");
         if (living.getAttribute(Attributes.MAX_HEALTH) != null) {
            CompoundTag data = living.getPersistentData();
            if (!data.contains("sl_shadow_base_health")) {
               data.putDouble("sl_shadow_base_health", living.getAttribute(Attributes.MAX_HEALTH).getBaseValue());
            }

            double base = data.getDouble("sl_shadow_base_health");
            living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(base + (level - 1) * healthGain(type));
            if (isBoss(type)) {
               applyBossHealth(living, shadow, restoreSavedBossHealth);
            } else {
               living.setHealth(living.getMaxHealth());
            }
         }

         if (living.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            CompoundTag data = living.getPersistentData();
            if (!data.contains("sl_shadow_base_attack")) {
               data.putDouble("sl_shadow_base_attack", living.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
            }

            double base = data.getDouble("sl_shadow_base_attack");
            living.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(base + (level - 1) * attackGain(type));
         }

         entity.getPersistentData().putInt("sl_shadow_applied_level", level);
         entity.getPersistentData().putInt("sl_shadow_applied_rank", rank);
         entity.setCustomName(Component.literal(shadow.getString("name") + " [" + rankDisplayName(rank) + "] Lv." + level));
         maintainMarshalDomainBoost(living, rank);
      }
   }

   private static void maintainMarshalDomainBoost(LivingEntity shadow, int rank) {
      if (!shadow.level().isClientSide()) {
         CompoundTag data = shadow.getPersistentData();
         if (rank < 5) {
            if (data.getBoolean("sl_shadow_rank_domain")) {
               clearIntrinsicMarshalDomainBoost(shadow);
            }
         } else {
            maintainMarshalDomainEffect(shadow, MobEffects.DAMAGE_BOOST);
            maintainMarshalDomainEffect(shadow, MobEffects.MOVEMENT_SPEED);
            maintainMarshalDomainEffect(shadow, MobEffects.DAMAGE_RESISTANCE);
            maintainMarshalDomainEffect(shadow, SololevelingModMobEffects.DOMAIN_BOOST.get());
            data.putBoolean("sl_shadow_rank_domain", true);
         }
      }
   }

   private static void maintainMarshalDomainEffect(LivingEntity shadow, MobEffect effect) {
      MobEffectInstance active = shadow.getEffect(effect);
      boolean temporaryDomainActive = shadow.getPersistentData().getLong("sl_shadow_temporary_domain_until") > shadow.level().getGameTime();
      if (active == null || !temporaryDomainActive) {
         if (active == null || active.getAmplifier() < 1 || active.getDuration() <= 80) {
            shadow.addEffect(new MobEffectInstance(effect, 240, 1, false, false));
         }
      }
   }

   private static void clearIntrinsicMarshalDomainBoost(LivingEntity shadow) {
      CompoundTag data = shadow.getPersistentData();
      boolean temporaryDomainActive = data.getLong("sl_shadow_temporary_domain_until") > shadow.level().getGameTime();
      if (!temporaryDomainActive) {
         removeIntrinsicMarshalDomainEffect(shadow, MobEffects.DAMAGE_BOOST);
         removeIntrinsicMarshalDomainEffect(shadow, MobEffects.MOVEMENT_SPEED);
         removeIntrinsicMarshalDomainEffect(shadow, MobEffects.DAMAGE_RESISTANCE);
         removeIntrinsicMarshalDomainEffect(shadow, SololevelingModMobEffects.DOMAIN_BOOST.get());
      }

      data.remove("sl_shadow_rank_domain");
   }

   private static void removeIntrinsicMarshalDomainEffect(LivingEntity shadow, MobEffect effect) {
      MobEffectInstance active = shadow.getEffect(effect);
      if (active != null && active.getAmplifier() == 1 && active.getDuration() <= 240) {
         shadow.removeEffect(effect);
      }
   }

   public static void markTemporaryDomainBoost(Entity shadow, int durationTicks) {
      if (shadow != null && !shadow.level().isClientSide() && durationTicks > 0) {
         CompoundTag data = shadow.getPersistentData();
         long until = shadow.level().getGameTime() + durationTicks;
         data.putLong("sl_shadow_temporary_domain_until", Math.max(data.getLong("sl_shadow_temporary_domain_until"), until));
      }
   }

   private static void applyLevelStatsPreservingHealth(Entity entity, CompoundTag shadow) {
      if (entity instanceof LivingEntity living) {
         float currentHealth = living.getHealth();
         applyLevelStats(entity, shadow, false);
         living.setHealth(Math.max(1.0F, Math.min(currentHealth, living.getMaxHealth())));
      }
   }

   private static void applyBossHealth(LivingEntity living, CompoundTag shadow, boolean restoreSavedHealth) {
      float maxHealth = living.getMaxHealth();
      if (!restoreSavedHealth) {
         living.setHealth(Math.max(1.0F, Math.min(living.getHealth(), maxHealth)));
      } else if (!shadow.contains("health")) {
         living.setHealth(maxHealth);
      } else {
         double savedHealth = shadow.getDouble("health");
         long savedAt = shadow.getLong("health_saved_at");
         long now = living.level().getGameTime();
         double regenerated = savedHealth + Math.max(0L, now - savedAt) / 20.0;
         living.setHealth((float)Math.max(1.0, Math.min(maxHealth, regenerated)));
         shadow.putDouble("health", living.getHealth());
         shadow.putLong("health_saved_at", now);
      }
   }

   private static void addStackToShadowInventory(Entity shadowEntity, ItemStack stack) {
      if (isCollectibleManaStone(stack)) {
         CompoundTag data = shadowEntity.getPersistentData();
         ListTag inventory = data.getList("sl_shadow_inventory", 10);
         int remaining = stack.getCount();

         for (int i = 0; i < inventory.size() && remaining > 0; i++) {
            ItemStack stored = ItemStack.of(inventory.getCompound(i));
            if (ItemStack.isSameItemSameTags(stored, stack) && stored.getCount() < stored.getMaxStackSize()) {
               int move = Math.min(remaining, stored.getMaxStackSize() - stored.getCount());
               stored.grow(move);
               remaining -= move;
               inventory.set(i, stored.save(new CompoundTag()));
            }
         }

         while (remaining > 0) {
            ItemStack stored = stack.copy();
            stored.setCount(Math.min(remaining, stored.getMaxStackSize()));
            remaining -= stored.getCount();
            inventory.add(stored.save(new CompoundTag()));
         }

         data.put("sl_shadow_inventory", inventory);
      }
   }

   private static boolean isCollectibleManaStone(ItemStack stack) {
      return stack != null && !stack.isEmpty() && isCollectibleManaStone(stack.getItem());
   }

   private static boolean isCollectibleManaStone(Item item) {
      return item == SololevelingModItems.MANA_CRYSTAL_E.get()
         || item == SololevelingModItems.MANA_CRYSTAL_D.get()
         || item == SololevelingModItems.MANA_CRYSTAL_C.get()
         || item == SololevelingModItems.MANA_CRYSTAL_B.get()
         || item == SololevelingModItems.MANA_CRYSTAL_A.get();
   }

   private static CompoundTag firstSummonableShadow(Player player, String type) {
      CompoundTag available = firstAvailableShadow(player, type);
      if (available != null) {
         return available;
      }

      CompoundTag bestActive = null;

      for (CompoundTag shadow : ownedRosterWithinLimit(player, type)) {
         if (shadow.hasUUID("summoned")) {
            Entity existing = findSummonedEntity(player, shadow.getUUID("summoned"));
            if (existing != null && existing.isAlive() && isBetterShadow(shadow, bestActive)) {
               bestActive = shadow;
            }
         }
      }

      return bestActive;
   }

   private static CompoundTag firstAvailableShadow(Player player, String type) {
      CompoundTag best = null;

      for (CompoundTag shadow : ownedRosterWithinLimit(player, type)) {
         boolean available = !shadow.hasUUID("summoned");
         if (!available) {
            Entity existing = findSummonedEntity(player, shadow.getUUID("summoned"));
            available = existing == null || !existing.isAlive();
            if (available) {
               shadow.remove("summoned");
            }
         }

         if (available && isBetterShadow(shadow, best)) {
            best = shadow;
         }
      }

      if (best != null) {
         return best;
      }

      int max = legacyMax(player, type);
      int count = countOwned(player, type);
      return count < max ? createShadow(player, type, count + 1) : null;
   }

   private static List<CompoundTag> ownedRosterWithinLimit(Player player, String type) {
      int limit = Math.max(0, legacyMax(player, type));
      if (limit == 0) {
         return List.of();
      }

      ArrayList<CompoundTag> matching = new ArrayList<>();
      ListTag roster = shadows(player);

      for (int i = 0; i < roster.size(); i++) {
         CompoundTag shadow = roster.getCompound(i);
         if (type.equals(shadow.getString("type"))) {
            matching.add(shadow);
         }
      }

      matching.sort(ShadowMonarchManager::compareStrongestFirst);
      return matching.size() > limit ? new ArrayList<>(matching.subList(0, limit)) : matching;
   }

   private static int compareStrongestFirst(CompoundTag first, CompoundTag second) {
      int byRank = Integer.compare(rankOf(second), rankOf(first));
      if (byRank != 0) {
         return byRank;
      }

      int byLevel = Integer.compare(Math.max(1, second.getInt("level")), Math.max(1, first.getInt("level")));
      if (byLevel != 0) {
         return byLevel;
      }

      int byXp = Integer.compare(second.getInt("xp"), first.getInt("xp"));
      return byXp != 0 ? byXp : first.getString("id").compareTo(second.getString("id"));
   }

   private static void enforceSummonedLimit(ServerPlayer player, String type) {
      List<CompoundTag> allowed = ownedRosterWithinLimit(player, type);
      ArrayList<String> allowedIds = new ArrayList<>(allowed.size());

      for (CompoundTag shadow : allowed) {
         allowedIds.add(shadow.getString("id"));
      }

      int removed = 0;
      ListTag roster = shadows(player);

      for (int i = 0; i < roster.size(); i++) {
         CompoundTag shadow = roster.getCompound(i);
         if (type.equals(shadow.getString("type")) && !allowedIds.contains(shadow.getString("id")) && shadow.hasUUID("summoned")) {
            Entity existing = findSummonedEntity(player, shadow.getUUID("summoned"));
            if (existing != null) {
               if (SilladIcePrisonManager.isImprisoned(existing)) {
                  continue;
               }

               dropStoredShadowInventory(existing);
               existing.discard();
               removed++;
            }

            shadow.remove("summoned");
         }
      }

      if (removed > 0) {
         updateLegacySpawnCounter(player, type, -removed);
      }

      player.getPersistentData().put("sololeveling_shadow_monarch", root(player));
   }

   private static boolean isBetterShadow(CompoundTag candidate, CompoundTag current) {
      if (current == null) {
         return true;
      }

      int candidateRank = rankOf(candidate);
      int currentRank = rankOf(current);
      if (candidateRank != currentRank) {
         return candidateRank > currentRank;
      }

      int candidateLevel = Math.max(1, candidate.getInt("level"));
      int currentLevel = Math.max(1, current.getInt("level"));
      if (candidateLevel != currentLevel) {
         return candidateLevel > currentLevel;
      }

      int candidateXp = candidate.getInt("xp");
      int currentXp = current.getInt("xp");
      return candidateXp != currentXp ? candidateXp > currentXp : candidate.getString("id").compareTo(current.getString("id")) < 0;
   }

   private static CompoundTag createShadow(Player player, String type, int number) {
      CompoundTag shadow = new CompoundTag();
      shadow.putString("id", UUID.randomUUID().toString());
      shadow.putString("type", type);
      shadow.putString("name", defaultName(type, number));
      shadow.putInt("level", 1);
      shadow.putInt("xp", 0);
      shadow.putInt("starting_rank", startingRank(type));
      shadow.putInt("rank", startingRank(type));
      shadow.putBoolean("boss", isBoss(type));
      shadows(player).add(shadow);
      return shadow;
   }

   private static void ensureRoster(Player player) {
      if (!player.getPersistentData().contains("sololeveling_shadow_monarch", 10)) {
         player.getPersistentData().put("sololeveling_shadow_monarch", new CompoundTag());
      }

      for (String type : shadowTypes()) {
         int max = legacyMax(player, type);

         while (countOwned(player, type) < max) {
            createShadow(player, type, countOwned(player, type) + 1);
         }
      }

      CompoundTag root = root(player);
      if (root.getInt("rank_schema") != 3) {
         migrateShadowRanks(player, root);
      } else {
         repairGrandMarshalClaim(player, root);
      }

      int levelCap = shadowLevelCap(player);
      if (root.getInt("shadow_level_cap") != levelCap) {
         ListTag roster = shadows(player);

         for (int i = 0; i < roster.size(); i++) {
            normalizeShadowProgress(player, roster.getCompound(i));
         }

         root.putInt("shadow_level_cap", levelCap);
         player.getPersistentData().put("sololeveling_shadow_monarch", root);
      }
   }

   private static void migrateShadowRanks(Player player, CompoundTag root) {
      int previousSchema = root.getInt("rank_schema");
      ListTag roster = shadows(player);

      for (int i = 0; i < roster.size(); i++) {
         CompoundTag shadow = roster.getCompound(i);
         String type = shadow.getString("type");
         int starting = startingRank(type);
         shadow.putInt("starting_rank", starting);
         if (previousSchema < 2) {
            shadow.putInt("rank", automaticRankForLevel(type, Math.max(1, shadow.getInt("level"))));
         } else if (previousSchema < 3 && "iron".equals(type)) {
            int current = shadow.contains("rank", 3) ? shadow.getInt("rank") : starting;
            shadow.putInt("rank", Math.min(5, Math.max(current, automaticRankForLevel(type, Math.max(1, shadow.getInt("level"))))));
         }
      }

      if (previousSchema < 2) {
         root.remove("grand_marshal_id");
      }

      root.putInt("rank_schema", 3);
      player.getPersistentData().put("sololeveling_shadow_monarch", root);
      repairGrandMarshalClaim(player, root);
   }

   private static void repairGrandMarshalClaim(Player player, CompoundTag root) {
      ListTag roster = shadows(player);
      String claimedId = root.getString("grand_marshal_id");
      CompoundTag claimed = claimedId.isEmpty() ? null : getShadow(player, claimedId);
      if (claimed == null || !isBoss(claimed.getString("type")) || rankOf(claimed) != 6 || !isGrandMarshalEligibleByLevel(claimed)) {
         claimed = null;
         root.remove("grand_marshal_id");
      }

      for (int i = 0; i < roster.size(); i++) {
         CompoundTag shadow = roster.getCompound(i);
         if (rankOf(shadow) == 6 && (claimed == null || !claimed.getString("id").equals(shadow.getString("id")))) {
            shadow.putInt("rank", automaticRankForLevel(shadow.getString("type"), Math.max(1, shadow.getInt("level"))));
         }
      }
   }

   private static boolean isBetterGrandMarshalCandidate(CompoundTag candidate, CompoundTag current) {
      if (current == null) {
         return true;
      }

      int byPower = Integer.compare(bossPower(candidate.getString("type")), bossPower(current.getString("type")));
      if (byPower != 0) {
         return byPower > 0;
      }

      int byLevel = Integer.compare(candidate.getInt("level"), current.getInt("level"));
      return byLevel != 0 ? byLevel > 0 : candidate.getInt("xp") > current.getInt("xp");
   }

   private static int bossPower(String type) {
      return switch (type) {
         case "kamish" -> 5;
         case "beru" -> 4;
         case "tusk" -> 3;
         case "kaisel" -> 2;
         case "igris" -> 1;
         default -> 0;
      };
   }

   private static int startingRank(String type) {
      return switch (type) {
         case "iron" -> 1;
         case "igris", "kaisel" -> 2;
         case "tusk" -> 3;
         case "beru", "kamish" -> 4;
         default -> 0;
      };
   }

   private static int automaticRankForLevel(String type, int level) {
      int desired = startingRank(type) + Math.max(0, level) / 10;
      return Math.min(desired, automaticRankCap(type));
   }

   private static boolean isGrandMarshalEligibleByLevel(CompoundTag shadow) {
      return shadow != null && isBoss(shadow.getString("type"))
         ? Math.max(1, shadow.getInt("level")) >= grandMarshalRequiredLevel(shadow.getString("type"))
         : false;
   }

   private static boolean isGrandMarshalEligible(CompoundTag shadow) {
      return shadow != null && rankOf(shadow) == 5 && isGrandMarshalEligibleByLevel(shadow);
   }

   private static boolean isClaimedGrandMarshal(Player player, CompoundTag shadow) {
      return player != null && shadow != null && rankOf(shadow) == 6 ? shadow.getString("id").equals(root(player).getString("grand_marshal_id")) : false;
   }

   private static void recalculateRankAfterAdminLevel(CompoundTag ownerRoot, CompoundTag shadow) {
      String id = shadow.getString("id");
      boolean assigned = id.equals(ownerRoot.getString("grand_marshal_id"));
      int level = Math.max(1, shadow.getInt("level"));
      String type = shadow.getString("type");
      shadow.putInt("starting_rank", startingRank(type));
      if (assigned && isBoss(type) && level >= grandMarshalRequiredLevel(type)) {
         shadow.putInt("rank", 6);
      } else {
         shadow.putInt("rank", automaticRankForLevel(type, level));
         if (assigned) {
            ownerRoot.remove("grand_marshal_id");
         }
      }
   }

   private static void refreshSummonedShadowRank(Player owner, CompoundTag shadow) {
      if (owner != null && shadow != null && shadow.hasUUID("summoned")) {
         Entity summoned = findSummonedEntity(owner, shadow.getUUID("summoned"));
         if (summoned != null && summoned.isAlive()) {
            applyLevelStatsPreservingHealth(summoned, shadow);
         }
      }
   }

   private static int rankOf(CompoundTag shadow) {
      if (shadow == null) {
         return 0;
      }

      String type = shadow.getString("type");
      int starting = shadow.contains("starting_rank", 3) ? shadow.getInt("starting_rank") : startingRank(type);
      int rank = shadow.contains("rank", 3) ? shadow.getInt("rank") : starting;
      int maximum = maximumRank(type);
      return Math.max(starting, Math.min(maximum, rank));
   }

   private static boolean promoteShadow(Player owner, CompoundTag shadow, boolean showPopup) {
      if (owner != null && shadow != null) {
         String type = shadow.getString("type");
         int oldRank = rankOf(shadow);
         CompoundTag ownerRoot = root(owner);
         int newRank;
         if (!isMarshalProgressionType(type)) {
            if (oldRank >= 3) {
               return false;
            }

            newRank = oldRank + 1;
         } else {
            if (oldRank >= 5) {
               return false;
            }

            newRank = oldRank + 1;
         }

         shadow.putInt("starting_rank", startingRank(type));
         shadow.putInt("rank", newRank);
         owner.getPersistentData().put("sololeveling_shadow_monarch", ownerRoot);
         if (showPopup && owner instanceof ServerPlayer serverPlayer) {
            Component title = Component.literal("SHADOW RANK UP").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
            Component under = Component.literal(shadow.getString("name") + "\n")
               .withStyle(ChatFormatting.LIGHT_PURPLE)
               .append(Component.literal(rankDisplayName(oldRank) + " -> " + rankDisplayName(newRank)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            SystemNotifications.showTitleUnder(serverPlayer, rankColor(newRank), 100, title, under);
         }

         return true;
      } else {
         return false;
      }
   }

   public static int shadowLevelCap(Player player) {
      if (player == null) {
         return 10;
      } else {
         double playerLevel = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(variables -> variables.Level).orElse(0.0);
         if (Double.isFinite(playerLevel) && !(playerLevel <= 40.0)) {
            double rawTiers = Math.floor((playerLevel - 40.0) / 20.0);
            long maximumTiers = 14316556L;
            long tiers = (long)Math.min(maximumTiers, Math.max(0.0, rawTiers));
            return 10 + (int)tiers * 10;
         } else {
            return 10;
         }
      }
   }

   private static int effectiveShadowLevelCap(Player player, CompoundTag shadow) {
      int normalCap = shadowLevelCap(player);
      int adminFloor = shadow == null ? 0 : Math.max(0, shadow.getInt("admin_level_floor"));
      return Math.max(normalCap, Math.min(1000000, adminFloor));
   }

   private static boolean normalizeShadowProgress(Player player, CompoundTag shadow) {
      boolean overrideChanged = shadow.contains("admin_level_floor", 3) && shadow.getInt("admin_level_floor") <= shadowLevelCap(player);
      if (overrideChanged) {
         shadow.remove("admin_level_floor");
      }

      int cap = effectiveShadowLevelCap(player, shadow);
      int originalLevel = shadow.getInt("level");
      int originalXp = shadow.getInt("xp");
      int level = Math.max(1, Math.min(cap, originalLevel));
      int xp = Math.max(0, originalXp);
      String type = shadow.getString("type");

      while (level < cap && xp >= xpNeeded(level, type)) {
         xp -= xpNeeded(level, type);
         if (++level % 10 == 0) {
            promoteShadow(player, shadow, false);
         }
      }

      if (level >= cap) {
         xp = Math.min(xp, xpNeeded(level, type) - 1);
      }

      if (level == originalLevel && xp == originalXp) {
         return overrideChanged;
      }

      shadow.putInt("level", level);
      shadow.putInt("xp", xp);
      return true;
   }

   private static void synchronizeShadowLevel(Player owner, Entity shadowEntity) {
      String id = shadowEntity.getPersistentData().getString("sl_shadow_id");
      if (!id.isEmpty()) {
         CompoundTag shadow = getShadow(owner, id);
         if (shadow != null) {
            boolean changed = normalizeShadowProgress(owner, shadow);
            syncEquipmentTag(shadowEntity, shadow);
            int level = Math.max(1, shadow.getInt("level"));
            int rank = rankOf(shadow);
            if (shadowEntity.getPersistentData().getInt("sl_shadow_applied_level") != level
               || shadowEntity.getPersistentData().getInt("sl_shadow_applied_rank") != rank) {
               applyLevelStatsPreservingHealth(shadowEntity, shadow);
            } else if (shadowEntity instanceof LivingEntity living) {
               maintainMarshalDomainBoost(living, rank);
            }

            if (changed) {
               owner.getPersistentData().put("sololeveling_shadow_monarch", root(owner));
            }
         }
      }
   }

   private static List<CompoundTag> summonedOwnedShadows(Player player) {
      ArrayList<CompoundTag> result = new ArrayList<>();
      if (player.level() instanceof ServerLevel level) {
         ListTag var7 = shadows(player);

         for (int i = 0; i < var7.size(); i++) {
            CompoundTag shadow = var7.getCompound(i);
            if (shadow.hasUUID("summoned")) {
               Entity entity = level.getEntity(shadow.getUUID("summoned"));
               if (entity != null && entity.isAlive()) {
                  result.add(shadow);
               }
            }
         }

         return result;
      } else {
         return result;
      }
   }

   private static void absorbVisibleOwnedShadows(Player player) {
      if (player.level() instanceof ServerLevel level) {
         AABB var7 = player.getBoundingBox().inflate(96.0);

         for (TamableAnimal tame : level.getEntitiesOfClass(
            TamableAnimal.class, var7, e -> e.getOwnerUUID() != null && e.getOwnerUUID().equals(player.getUUID())
         )) {
            if (tame.getPersistentData().getString("sl_shadow_id").isEmpty()) {
               String type = typeFromEntity(tame);
               if (!type.isEmpty()) {
                  CompoundTag shadow = firstAvailableShadow(player, type);
                  if (shadow == null) {
                     shadow = createShadow(player, type, countOwned(player, type) + 1);
                  }

                  tagSummonedEntity(player, shadow, tame);
               }
            }
         }

         for (ShadowKaiselinEntity kaisel : level.getEntitiesOfClass(
            ShadowKaiselinEntity.class, var7, e -> e.getOwnerUUID() != null && e.getOwnerUUID().equals(player.getUUID())
         )) {
            if (kaisel.getPersistentData().getString("sl_shadow_id").isEmpty()) {
               CompoundTag shadow = firstAvailableShadow(player, "kaisel");
               if (shadow == null) {
                  shadow = createShadow(player, "kaisel", countOwned(player, "kaisel") + 1);
               }

               tagSummonedEntity(player, shadow, kaisel);
            }
         }
      }
   }

   private static Entity findSummonedEntity(Player player, UUID entityId) {
      if (player != null && entityId != null && player.level() instanceof ServerLevel level) {
         for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            Entity entity = serverLevel.getEntity(entityId);
            if (entity != null) {
               return entity;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static Player findOnlineOwner(ServerLevel level, UUID ownerId) {
      return level != null && ownerId != null ? level.getServer().getPlayerList().getPlayer(ownerId) : null;
   }

   private static boolean isCurrentSummonedInstance(Player owner, Entity entity) {
      if (owner != null && entity != null) {
         long ownerGeneration = owner.getPersistentData().getLong("sl_shadow_reset_generation");
         if (ownerGeneration != entity.getPersistentData().getLong("sl_shadow_generation")) {
            return false;
         }

         String id = entity.getPersistentData().getString("sl_shadow_id");
         if (id.isEmpty()) {
            return true;
         }

         CompoundTag shadow = getShadow(owner, id);
         return shadow == null ? true : shadow.hasUUID("summoned") && shadow.getUUID("summoned").equals(entity.getUUID());
      } else {
         return true;
      }
   }

   private static CompoundTag getShadow(Entity entity, String id) {
      if (entity != null && id != null) {
         ListTag shadows = shadows(entity);

         for (int i = 0; i < shadows.size(); i++) {
            CompoundTag shadow = shadows.getCompound(i);
            if (id.equals(shadow.getString("id"))) {
               return shadow;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static CompoundTag strongestOwnedShadow(Player player, String type) {
      if (player != null && type != null && !type.isEmpty()) {
         CompoundTag best = null;

         for (CompoundTag shadow : ownedRosterWithinLimit(player, type)) {
            if (isBetterShadow(shadow, best)) {
               best = shadow;
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private static ItemStack equipmentOf(CompoundTag shadow) {
      return shadow != null && shadow.contains("equipment", 10) ? ItemStack.of(shadow.getCompound("equipment")) : ItemStack.EMPTY;
   }

   private static void syncEquipmentTag(Entity entity, CompoundTag shadow) {
      if (entity != null) {
         ItemStack equipment = equipmentOf(shadow);
         if (equipment.isEmpty()) {
            entity.getPersistentData().remove("sl_shadow_equipment");
         } else {
            entity.getPersistentData().putString("sl_shadow_equipment", BuiltInRegistries.ITEM.getKey(equipment.getItem()).toString());
         }
      }
   }

   private static void returnEquipmentToPlayer(Player player, CompoundTag shadow) {
      ItemStack equipment = equipmentOf(shadow);
      if (player != null && !equipment.isEmpty()) {
         shadow.remove("equipment");
         if (!player.getInventory().add(equipment)) {
            player.drop(equipment, false);
         }
      }
   }

   private static CompoundTag getFormation(Entity entity, String id) {
      if (entity != null && id != null) {
         ListTag formations = formations(entity);

         for (int i = 0; i < formations.size(); i++) {
            CompoundTag formation = formations.getCompound(i);
            if (id.equals(formation.getString("id"))) {
               return formation;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static void removeFormationData(Entity entity, String id) {
      ListTag formations = formations(entity);

      for (int i = formations.size() - 1; i >= 0; i--) {
         if (id.equals(formations.getCompound(i).getString("id"))) {
            formations.remove(i);
         }
      }
   }

   private static CompoundTag root(Entity entity) {
      CompoundTag data = entity.getPersistentData();
      if (!data.contains("sololeveling_shadow_monarch", 10)) {
         data.put("sololeveling_shadow_monarch", new CompoundTag());
      }

      CompoundTag root = data.getCompound("sololeveling_shadow_monarch");
      if (!root.contains("shadows", 9)) {
         root.put("shadows", new ListTag());
      }

      if (!root.contains("formations", 9)) {
         root.put("formations", new ListTag());
      }

      return root;
   }

   private static ListTag shadows(Entity entity) {
      return root(entity).getList("shadows", 10);
   }

   public static List<String> customizableTypes() {
      return List.of(shadowTypes());
   }

   public static int glowColor(Player player, String requestedType) {
      if (player == null) {
         return -1;
      }

      String type = normalizeShadowType(requestedType == null ? "" : requestedType);
      if (type.isEmpty()) {
         return -1;
      }

      CompoundTag colors = root(player).getCompound("glow_colors");
      return colors.contains(type, 3) ? colors.getInt(type) & 16777215 : -1;
   }

   public static void setGlowColor(Player player, String requestedType, int rgb) {
      if (player != null) {
         String type = normalizeShadowType(requestedType == null ? "" : requestedType);
         if (!type.isEmpty()) {
            CompoundTag playerRoot = root(player);
            CompoundTag colors = playerRoot.getCompound("glow_colors");
            if (rgb == -1) {
               colors.remove(type);
            } else {
               colors.putInt(type, rgb & 16777215);
            }

            playerRoot.put("glow_colors", colors);
            player.getPersistentData().put("sololeveling_shadow_monarch", playerRoot);
         }
      }
   }

   public static Map<UUID, String> summonedShadowTypes(ServerPlayer owner) {
      Map<UUID, String> result = new LinkedHashMap<>();
      if (owner == null) {
         return result;
      }

      ListTag roster = shadows(owner);
      ServerLevel level = owner.serverLevel();

      for (int index = 0; index < roster.size(); index++) {
         CompoundTag shadow = roster.getCompound(index);
         if (shadow.hasUUID("summoned")) {
            UUID id = shadow.getUUID("summoned");
            Entity entity = level.getEntity(id);
            if (entity != null && entity.isAlive() && !entity.isRemoved()) {
               result.put(id, normalizeShadowType(shadow.getString("type")));
            }
         }
      }

      return result;
   }

   private static ListTag formations(Entity entity) {
      return root(entity).getList("formations", 10);
   }

   private static int formationCount(Entity entity) {
      return formations(entity).size();
   }

   private static int countOwned(Player player, String type) {
      int count = 0;
      ListTag shadows = shadows(player);

      for (int i = 0; i < shadows.size(); i++) {
         if (type.equals(shadows.getCompound(i).getString("type"))) {
            count++;
         }
      }

      return count;
   }

   private static void appendFormationSkill(Player player, String id, String name) {
      String skill = "Formation:" + id + "|" + cleanFormationName(name, 1);
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         String list = capability.Plist != null && !capability.Plist.isEmpty() ? capability.Plist : ".";
         if (!list.contains("Formation:" + id)) {
            capability.Plist = list + skill + ",";
            capability.syncPlayerVariables(player);
         }
      });
   }

   private static String cleanFormationName(String value, int number) {
      String name = value == null ? "" : value.trim();
      if (name.isEmpty()) {
         return "Formation " + number;
      }

      name = name.replace(',', ' ').replace('|', ' ');
      return name.length() > 24 ? name.substring(0, 24) : name;
   }

   private static List<String> parseSkillList(String plistOriginal) {
      ArrayList<String> result = new ArrayList<>();
      if (plistOriginal != null && !plistOriginal.isEmpty()) {
         for (String item : plistOriginal.split(",")) {
            String skill = item == null ? "" : item.trim();
            if (skill.startsWith(".")) {
               skill = skill.substring(1);
            }

            if (!skill.isEmpty()) {
               result.add(skill);
            }
         }

         return result;
      } else {
         return result;
      }
   }

   private static String writeSkillList(List<String> skills) {
      if (skills != null && !skills.isEmpty()) {
         StringBuilder builder = new StringBuilder(".");

         for (String skill : skills) {
            if (skill != null && !skill.isEmpty()) {
               builder.append(skill).append(",");
            }
         }

         return builder.toString();
      } else {
         return ".";
      }
   }

   private static String formationIdFromSkill(String skill) {
      if (!isFormationSkill(skill)) {
         return "";
      }

      String value = skill.substring("Formation:".length());
      int split = value.indexOf(124);
      return split >= 0 ? value.substring(0, split) : value;
   }

   private static String formationNameFromSkill(String skill) {
      if (!isFormationSkill(skill)) {
         return "";
      }

      int split = skill.indexOf(124);
      return split >= 0 && split + 1 < skill.length() ? skill.substring(split + 1) : "";
   }

   private static int xpNeeded(int level, String type) {
      long base = 35L + Math.max(1L, level) * 15L;

      double multiplier = switch (type) {
         case "iron" -> 1.4;
         case "igris" -> 1.75;
         case "kaisel" -> 2.0;
         case "tusk" -> 2.25;
         case "beru" -> 3.0;
         case "kamish" -> 4.0;
         case "high_orc" -> 1.2;
         default -> 1.0;
      };
      return Math.max(1, (int)Math.min(2.147483647E9, Math.ceil(base * multiplier)));
   }

   private static double healthGain(String type) {
      return switch (type) {
         case "iron" -> 6.5;
         case "igris", "beru", "kamish", "tusk", "kaisel" -> 8.0;
         case "high_orc", "polar_bear" -> 5.0;
         case "knight", "orc", "wolf" -> 3.0;
         default -> 2.0;
      };
   }

   private static double attackGain(String type) {
      return switch (type) {
         case "iron" -> 0.85;
         case "igris", "beru", "kamish", "tusk", "kaisel" -> 1.25;
         case "high_orc", "polar_bear" -> 0.85;
         case "knight", "orc", "wolf" -> 0.55;
         default -> 0.4;
      };
   }

   private static String[] shadowTypes() {
      return new String[]{
         "goblin_club", "goblin_archer", "goblin_mage", "wolf", "knight", "polar_bear", "orc", "igris", "beru", "kamish", "high_orc", "tusk", "kaisel", "iron"
      };
   }

   private static EntityType<?> entityType(String type) {
      return switch (type) {
         case "goblin_club" -> (EntityType)SololevelingModEntities.GOBLIN_CLUB_SHADOW.get();
         case "goblin_archer" -> (EntityType)SololevelingModEntities.GOBLIN_ARCHER_SHADOW.get();
         case "goblin_mage" -> (EntityType)SololevelingModEntities.GOBLIN_MAGE_SHADOW.get();
         case "wolf" -> (EntityType)SololevelingModEntities.STEEL_FANG_WOLF_SHADOW.get();
         case "knight" -> (EntityType)SololevelingModEntities.SHADOW_SOLD_1.get();
         case "polar_bear" -> (EntityType)SololevelingModEntities.SHADOW_POLAR_BEAR.get();
         case "orc" -> (EntityType)SololevelingModEntities.SHADOW_GREEN_ORC.get();
         case "igris" -> (EntityType)SololevelingModEntities.IGRIS_SHADOW.get();
         case "beru" -> (EntityType)SololevelingModEntities.BERU_SHADOW.get();
         case "kamish" -> (EntityType)SololevelingModEntities.KAMISH_SHADOW.get();
         case "high_orc" -> (EntityType)SololevelingModEntities.SHADOW_HIGH_ORC.get();
         case "tusk" -> (EntityType)SololevelingModEntities.TUSK_SHADOW.get();
         case "kaisel" -> (EntityType)SololevelingModEntities.SHADOW_KAISELIN.get();
         case "iron" -> (EntityType)SololevelingModEntities.SHADOW_IRON.get();
         default -> null;
      };
   }

   private static String typeFromEntity(Entity entity) {
      if (entity instanceof GoblinClubShadowEntity) {
         return "goblin_club";
      } else if (entity instanceof GoblinArcherShadowEntity) {
         return "goblin_archer";
      } else if (entity instanceof GoblinMageShadowEntity) {
         return "goblin_mage";
      } else if (entity instanceof SteelFangWolfShadowEntity) {
         return "wolf";
      } else if (entity instanceof ShadowSold1Entity) {
         return "knight";
      } else if (entity instanceof ShadowPolarBearEntity) {
         return "polar_bear";
      } else if (entity instanceof ShadowGreenOrcEntity) {
         return "orc";
      } else if (entity instanceof OrcShadowEntity) {
         return "orc";
      } else if (entity instanceof IgrisShadowEntity) {
         return "igris";
      } else if (entity instanceof BeruShadowEntity) {
         return "beru";
      } else if (entity instanceof KamishShadowEntity) {
         return "kamish";
      } else if (entity instanceof ShadowHighOrcEntity) {
         return "high_orc";
      } else if (entity instanceof TuskShadowEntity) {
         return "tusk";
      } else if (entity instanceof ShadowKaiselinEntity) {
         return "kaisel";
      } else {
         return entity instanceof ShadowIronEntity ? "iron" : "";
      }
   }

   private static String normalizeShadowType(String type) {
      String value = type.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

      return switch (value) {
         case "goblin", "goblin_fighter", "goblin_club" -> "goblin_club";
         case "archer", "goblin_archer" -> "goblin_archer";
         case "mage", "goblin_mage" -> "goblin_mage";
         case "lycan", "wolf" -> "wolf";
         case "soldier", "knight" -> "knight";
         case "bear", "polar", "polar_bear" -> "polar_bear";
         case "orc", "green_orc" -> "orc";
         case "highorc", "high_orc" -> "high_orc";
         case "igris" -> "igris";
         case "beru" -> "beru";
         case "kamish" -> "kamish";
         case "tusk" -> "tusk";
         case "kaisel", "kaiselin", "shadow_kaisel", "shadow_kaiselin" -> "kaisel";
         case "iron", "shadow_iron" -> "iron";
         default -> "";
      };
   }

   private static String defaultName(String type, int number) {
      return switch (type) {
         case "goblin_club" -> "Goblin Fighter " + number;
         case "goblin_archer" -> "Goblin Archer " + number;
         case "goblin_mage" -> "Goblin Mage " + number;
         case "wolf" -> "Lycan " + number;
         case "knight" -> "Knight " + number;
         case "polar_bear" -> "Polar Bear " + number;
         case "orc" -> "Orc " + number;
         case "igris" -> "Igris";
         case "beru" -> "Beru";
         case "kamish" -> "Kamish";
         case "high_orc" -> "High Orc " + number;
         case "tusk" -> "Tusk";
         case "kaisel" -> "Kaisel";
         case "iron" -> "Iron";
         default -> type.replace('_', ' ').toLowerCase(Locale.ROOT);
      };
   }

   private static boolean isBoss(String type) {
      return "igris".equals(type) || "beru".equals(type) || "kamish".equals(type) || "tusk".equals(type) || "kaisel".equals(type);
   }

   private static boolean isMarshalProgressionType(String type) {
      return isBoss(type) || "iron".equals(type);
   }

   private static boolean isHealingBossType(String type) {
      return isBoss(type) || "iron".equals(type);
   }

   private static int automaticRankCap(String type) {
      return isMarshalProgressionType(type) ? 5 : 3;
   }

   private static int maximumRank(String type) {
      if (isBoss(type)) {
         return 6;
      } else {
         return "iron".equals(type) ? 5 : 3;
      }
   }

   private static boolean isDismissibleShadowType(String type) {
      return "goblin_club".equals(type)
         || "goblin_archer".equals(type)
         || "goblin_mage".equals(type)
         || "wolf".equals(type)
         || "knight".equals(type)
         || "polar_bear".equals(type)
         || "orc".equals(type)
         || "high_orc".equals(type);
   }

   private static int legacyMax(Player player, String type) {
      if ("iron".equals(type)) {
         return Math.max(0, Math.min(1, root(player).getInt("iron_max")));
      }

      SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());

      return switch (type) {
         case "goblin_club" -> (int)vars.GobShadowMax;
         case "goblin_archer" -> (int)vars.ShadowGoblinArcherMax;
         case "goblin_mage" -> (int)vars.ShadowGoblinMageMax;
         case "wolf" -> (int)vars.WolfShadowMax;
         case "knight" -> (int)vars.ordshadowmax;
         case "polar_bear" -> (int)vars.polarbearmax;
         case "orc" -> (int)vars.orcmax;
         case "igris" -> (int)vars.igris;
         case "beru" -> (int)vars.berumax;
         case "kamish" -> (int)vars.shadowdragonmax;
         case "high_orc" -> (int)vars.highorcmax;
         case "tusk" -> (int)vars.tuskmax;
         case "kaisel" -> (int)vars.Kaisel;
         default -> 0;
      };
   }

   private static int legacySpawned(Player player, String type) {
      if ("iron".equals(type)) {
         return Math.max(0, Math.min(1, root(player).getInt("iron_summoned")));
      }

      SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());

      return switch (type) {
         case "goblin_club" -> (int)vars.GobShadow;
         case "goblin_archer" -> (int)vars.ShadowGoblinArcherAmount;
         case "goblin_mage" -> (int)vars.ShadowGoblinMageAmount;
         case "wolf" -> (int)vars.WolfShadow;
         case "knight" -> (int)vars.OrdShadow;
         case "polar_bear" -> (int)vars.polarbear;
         case "orc" -> (int)vars.orcspawned;
         case "igris" -> (int)vars.IgrisSpawned;
         case "beru" -> (int)vars.beru;
         case "kamish" -> (int)vars.shadowdragonnum;
         case "high_orc" -> (int)vars.highorcspawned;
         case "tusk" -> (int)vars.tuskspawned;
         case "kaisel" -> (int)vars.KaiselSpawned;
         default -> 0;
      };
   }

   private static void setLegacyMax(Player player, String type, int amount) {
      if ("iron".equals(type)) {
         CompoundTag playerRoot = root(player);
         playerRoot.putInt("iron_max", Math.max(0, Math.min(1, amount)));
         player.getPersistentData().put("sololeveling_shadow_monarch", playerRoot);
      } else {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            switch (type) {
               case "goblin_club":
                  capability.GobShadowMax = amount;
                  break;
               case "goblin_archer":
                  capability.ShadowGoblinArcherMax = amount;
                  break;
               case "goblin_mage":
                  capability.ShadowGoblinMageMax = amount;
                  break;
               case "wolf":
                  capability.WolfShadowMax = amount;
                  break;
               case "knight":
                  capability.ordshadowmax = amount;
                  break;
               case "polar_bear":
                  capability.polarbearmax = amount;
                  break;
               case "orc":
                  capability.orcmax = amount;
                  break;
               case "igris":
                  capability.igris = amount;
                  break;
               case "beru":
                  capability.berumax = amount;
                  break;
               case "kamish":
                  capability.shadowdragonmax = amount;
                  break;
               case "high_orc":
                  capability.highorcmax = amount;
                  break;
               case "tusk":
                  capability.tuskmax = amount;
                  break;
               case "kaisel":
                  capability.Kaisel = amount;
            }
         });
      }
   }

   private static void trimOwnedShadows(Player player, String type, int amount) {
      ListTag shadows = shadows(player);

      while (countOwned(player, type) > amount) {
         int removeIndex = weakestShadowIndex(shadows, type);
         if (removeIndex < 0) {
            return;
         }

         CompoundTag shadow = shadows.getCompound(removeIndex);
         if (shadow.hasUUID("summoned") && player.level() instanceof ServerLevel level) {
            Entity summoned = findSummonedEntity(player, shadow.getUUID("summoned"));
            if (summoned != null) {
               dropStoredShadowInventory(summoned);
               summoned.discard();
               updateLegacySpawnCounter(player, type, -1);
            }
         }

         returnEquipmentToPlayer(player, shadow);
         shadows.remove(removeIndex);
      }
   }

   private static int weakestShadowIndex(ListTag shadows, String type) {
      int weakestIndex = -1;
      CompoundTag weakest = null;

      for (int i = 0; i < shadows.size(); i++) {
         CompoundTag shadow = shadows.getCompound(i);
         if (type.equals(shadow.getString("type")) && isWeakerShadow(shadow, weakest)) {
            weakest = shadow;
            weakestIndex = i;
         }
      }

      return weakestIndex;
   }

   private static boolean isWeakerShadow(CompoundTag candidate, CompoundTag current) {
      if (current == null) {
         return true;
      }

      int candidateRank = rankOf(candidate);
      int currentRank = rankOf(current);
      if (candidateRank != currentRank) {
         return candidateRank < currentRank;
      }

      int candidateLevel = Math.max(1, candidate.getInt("level"));
      int currentLevel = Math.max(1, current.getInt("level"));
      if (candidateLevel != currentLevel) {
         return candidateLevel < currentLevel;
      }

      int candidateXp = candidate.getInt("xp");
      int currentXp = current.getInt("xp");
      return candidateXp != currentXp ? candidateXp < currentXp : candidate.getString("id").compareTo(current.getString("id")) > 0;
   }

   private static void updateLegacySpawnCounter(Player player, String type, int amount) {
      if ("iron".equals(type)) {
         CompoundTag playerRoot = root(player);
         int current = playerRoot.getInt("iron_summoned");
         playerRoot.putInt("iron_summoned", Math.max(0, Math.min(1, current + amount)));
         player.getPersistentData().put("sololeveling_shadow_monarch", playerRoot);
      } else {
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            switch (type) {
               case "goblin_club":
                  capability.GobShadow = Math.max(0.0, capability.GobShadow + amount);
                  break;
               case "goblin_archer":
                  capability.ShadowGoblinArcherAmount = Math.max(0.0, capability.ShadowGoblinArcherAmount + amount);
                  break;
               case "goblin_mage":
                  capability.ShadowGoblinMageAmount = Math.max(0.0, capability.ShadowGoblinMageAmount + amount);
                  break;
               case "wolf":
                  capability.WolfShadow = Math.max(0.0, capability.WolfShadow + amount);
                  break;
               case "knight":
                  capability.OrdShadow = Math.max(0.0, capability.OrdShadow + amount);
                  break;
               case "polar_bear":
                  capability.polarbear = Math.max(0.0, capability.polarbear + amount);
                  break;
               case "orc":
                  capability.orcspawned = Math.max(0.0, capability.orcspawned + amount);
                  break;
               case "igris":
                  capability.IgrisSpawned = Math.max(0.0, capability.IgrisSpawned + amount);
                  break;
               case "beru":
                  capability.beru = Math.max(0.0, capability.beru + amount);
                  break;
               case "kamish":
                  capability.shadowdragonnum = Math.max(0.0, capability.shadowdragonnum + amount);
                  break;
               case "high_orc":
                  capability.highorcspawned = Math.max(0.0, capability.highorcspawned + amount);
                  break;
               case "tusk":
                  capability.tuskspawned = Math.max(0.0, capability.tuskspawned + amount);
                  break;
               case "kaisel":
                  capability.KaiselSpawned = Math.max(0.0, capability.KaiselSpawned + amount);
            }

            capability.syncPlayerVariables(player);
         });
      }
   }

   private static final class ClearDungeonState {
      private String contextKey = "";
      private long nextCandidateScanTick;
      private long lastSeenTick;
      private List<UUID> candidateIds = List.of();
      private final Map<ShadowMonarchManager.ClearTargetKey, Long> failedUntil = new HashMap<>();
      private final Map<UUID, ShadowMonarchManager.ClearShadowProgress> progress = new HashMap<>();

      private void reset(String contextKey, long now) {
         this.contextKey = contextKey;
         this.nextCandidateScanTick = now;
         this.candidateIds = List.of();
         this.failedUntil.clear();
         this.progress.clear();
      }
   }

   private static final class ClearShadowProgress {
      private UUID targetId;
      private double lastDistance;
      private float lastTargetHealth;
      private long lastProgressTick;
      private long nextRepathTick;
      private boolean traversing;
      private Vec3 lastShadowPosition = Vec3.ZERO;

      private void reset(LivingEntity target, Mob shadow, double distance, long now) {
         this.targetId = target.getUUID();
         this.lastDistance = distance;
         this.lastTargetHealth = target.getHealth();
         this.lastProgressTick = now;
         this.nextRepathTick = now + ShadowMonarchManager.clearRepathDelay(shadow);
         this.traversing = false;
         this.lastShadowPosition = shadow.position();
      }

      private void beginTraversal(LivingEntity target, Mob shadow, double distance, long now) {
         boolean newObjective = !target.getUUID().equals(this.targetId) || !this.traversing;
         this.targetId = target.getUUID();
         this.lastDistance = distance;
         this.lastTargetHealth = target.getHealth();
         this.traversing = true;
         if (newObjective) {
            this.lastProgressTick = now;
            this.lastShadowPosition = shadow.position();
         }

         this.nextRepathTick = now + 20L;
      }
   }

   private record ClearTargetKey(UUID shadowId, UUID targetId) {
   }

   private record DungeonTargetContext(String key, UUID instanceId, String legacyTag, boolean strictLegacyTag, int dkcFloor) {
   }

   public record GrandMarshalAssignmentResult(boolean success, String message) {
   }

   public record GrandMarshalCommander(String shadowId, String type, String name, int level, LivingEntity entity) {
   }

   private static final class PathAttemptBudget {
      private int used;

      private boolean tryUse() {
         if (this.used >= 8) {
            return false;
         }

         this.used++;
         return true;
      }
   }

   public record ShadowDisplayProgress(
      int rank,
      int level,
      int rankXp,
      int rankXpNeeded,
      int nextRank,
      boolean levelCapped,
      boolean maxRank,
      boolean grandMarshalEligible,
      boolean grandMarshalActive
   ) {
   }

   public record ShadowHealingQuote(int bossManaCost, int allManaCost, int bossTargets, int allTargets) {
   }

   public record ShadowHealingResult(boolean success, int healedShadows, int manaConsumed, int healthRestored, String message) {
   }

   private record ShadowHealingTarget(LivingEntity entity, String type, double missingHealth) {
   }

   public record ShadowLevelCommandResult(boolean knownTarget, int changed, int lowestLevel, int highestLevel) {
   }
}
