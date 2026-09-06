package dev.eness.sololevelingfinal.core.util;

import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class SoloLevelingWorldRulesManager {
   private static final int STANDARD_PERCENT = 100;
   private static final int FORGIVING_DEATH_RULE = 1;
   private static final int HARSH_DEATH_RULE = 2;
   private static final String FORGIVING_SNAPSHOT = "slr_forgiving_death_snapshot";
   private static final String SNAPSHOT_INVENTORY = "Inventory";
   private static final String SNAPSHOT_SELECTED_SLOT = "SelectedSlot";
   private static final String SNAPSHOT_XP_LEVEL = "ExperienceLevel";
   private static final String SNAPSHOT_XP_TOTAL = "ExperienceTotal";
   private static final String SNAPSHOT_XP_PROGRESS = "ExperienceProgress";
   private static final UUID ENEMY_HEALTH_ID = UUID.fromString("33d15df7-4cb8-458b-98c0-d9c3d289c157");
   private static final UUID ENEMY_DAMAGE_ID = UUID.fromString("21003733-c044-42ae-a1c3-76bb8ca105c1");
   private static final UUID BOSS_HEALTH_ID = UUID.fromString("2320716c-d01e-4cc6-b15d-b9059e8cb4dd");
   private static final UUID BOSS_DAMAGE_ID = UUID.fromString("553f2e8a-143d-4382-acbc-f5f6473c8630");
   private static final UUID BOSS_ARMOR_ID = UUID.fromString("3e28b8ac-f64a-4496-92ca-d03ac4ad4b0a");
   private static final UUID BOSS_TOUGHNESS_ID = UUID.fromString("57d07fc8-6505-4ac4-824d-0c4b66446005");
   private static final UUID BOSS_KNOCKBACK_ID = UUID.fromString("6ca2c497-5875-491d-899d-f8e89e410606");
   private static final TagKey<EntityType<?>> SOLO_BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "soloboss"));
   private static final TagKey<EntityType<?>> SOLO_ENEMY_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "dm"));
   private static final Set<String> NON_ENEMY_MONSTER_IDS = Set.of(
      "after_image",
      "after_image_1",
      "after_image_2",
      "arrow_splash",
      "attackshard",
      "detect_eye_inv",
      "fx_puddle",
      "fxspik",
      "ice_chunk",
      "icecle",
      "magic_eye",
      "secretary",
      "slasheffectsword",
      "sung_jin_woo",
      "thomas_andre",
      "training_bot"
   );

   private SoloLevelingWorldRulesManager() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob) {
         ResourceLocation var15 = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
         boolean runtimeDungeonMob = mob.getPersistentData().getBoolean("slr_dungeon_spawned")
            || !mob.getPersistentData().getString("slr_dungeon_instance").isBlank();
         boolean taggedEnemy = mob.getType().is(SOLO_ENEMY_TAG);
         boolean taggedBoss = mob.getType().is(SOLO_BOSS_TAG);
         if (var15 != null && ("sololeveling".equals(var15.getNamespace()) || runtimeDungeonMob || taggedEnemy || taggedBoss)) {
            boolean excludedAlly = isHunterOrSummon(mob, var15.getPath());
            boolean boss = !excludedAlly && isBoss(mob);
            if (boss || isEnemy(mob, var15.getPath(), excludedAlly)) {
               GameRules rules = event.getLevel().getLevelData().getGameRules();
               double enemyAmount = boss ? 0.0 : percentAmount(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_ENEMY_SCALE));
               double bossAmount = boss ? percentAmount(rules.getInt(SololevelingModGameRules.SOLO_LEVELING_BOSS_POWER)) : 0.0;
               float oldMaxHealth = mob.getMaxHealth();
               float healthRatio = oldMaxHealth > 0.0F ? Mth.clamp(mob.getHealth() / oldMaxHealth, 0.0F, 1.0F) : 1.0F;
               replaceModifier(mob.getAttribute(Attributes.MAX_HEALTH), ENEMY_HEALTH_ID, "SLR enemy health setting", enemyAmount, Operation.MULTIPLY_TOTAL);
               replaceModifier(mob.getAttribute(Attributes.ATTACK_DAMAGE), ENEMY_DAMAGE_ID, "SLR enemy damage setting", enemyAmount, Operation.MULTIPLY_TOTAL);
               replaceModifier(mob.getAttribute(Attributes.MAX_HEALTH), BOSS_HEALTH_ID, "SLR boss health setting", bossAmount, Operation.MULTIPLY_TOTAL);
               replaceModifier(mob.getAttribute(Attributes.ATTACK_DAMAGE), BOSS_DAMAGE_ID, "SLR boss damage setting", bossAmount, Operation.MULTIPLY_TOTAL);
               replaceModifier(mob.getAttribute(Attributes.ARMOR), BOSS_ARMOR_ID, "SLR boss armor setting", bossAmount * 8.0, Operation.ADDITION);
               replaceModifier(
                  mob.getAttribute(Attributes.ARMOR_TOUGHNESS), BOSS_TOUGHNESS_ID, "SLR boss armor toughness setting", bossAmount * 4.0, Operation.ADDITION
               );
               replaceModifier(
                  mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE),
                  BOSS_KNOCKBACK_ID,
                  "SLR boss knockback resistance setting",
                  bossAmount * 0.2,
                  Operation.ADDITION
               );
               if (mob.isAlive()) {
                  mob.setHealth(Math.max(1.0F, mob.getMaxHealth() * healthRatio));
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onPlayerDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && isSoloDungeon(player.level())) {
         int rule = player.level().getGameRules().getInt(SololevelingModGameRules.SOLO_LEVELING_DEATH_RULES);
         player.getPersistentData().remove("slr_forgiving_death_snapshot");
         if (rule == 1) {
            captureForgivingSnapshot(player);
         } else if (rule == 2) {
            applyHarshLoss(player);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onPlayerDrops(LivingDropsEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && player.getPersistentData().contains("slr_forgiving_death_snapshot", 10)) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onPlayerExperienceDrop(LivingExperienceDropEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && player.getPersistentData().contains("slr_forgiving_death_snapshot", 10)) {
         event.setDroppedExperience(0);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onPlayerClone(Clone event) {
      if (event.isWasDeath() && event.getOriginal().getPersistentData().contains("slr_forgiving_death_snapshot", 10)) {
         CompoundTag snapshot = event.getOriginal().getPersistentData().getCompound("slr_forgiving_death_snapshot");
         if (event.getEntity() instanceof ServerPlayer replacement) {
            replacement.getInventory().clearContent();
            replacement.getInventory().load(snapshot.getList("Inventory", 10));
            replacement.getInventory().selected = Mth.clamp(snapshot.getInt("SelectedSlot"), 0, 8);
            replacement.experienceLevel = Math.max(0, snapshot.getInt("ExperienceLevel"));
            replacement.totalExperience = Math.max(0, snapshot.getInt("ExperienceTotal"));
            replacement.experienceProgress = Mth.clamp(snapshot.getFloat("ExperienceProgress"), 0.0F, 1.0F);
            replacement.getInventory().setChanged();
            event.getOriginal().getPersistentData().remove("slr_forgiving_death_snapshot");
            replacement.getPersistentData().remove("slr_forgiving_death_snapshot");
         }
      }
   }

   private static void captureForgivingSnapshot(ServerPlayer player) {
      CompoundTag snapshot = new CompoundTag();
      snapshot.put("Inventory", player.getInventory().save(new ListTag()));
      snapshot.putInt("SelectedSlot", player.getInventory().selected);
      snapshot.putInt("ExperienceLevel", player.experienceLevel);
      snapshot.putInt("ExperienceTotal", player.totalExperience);
      snapshot.putFloat("ExperienceProgress", player.experienceProgress);
      player.getPersistentData().put("slr_forgiving_death_snapshot", snapshot);
   }

   private static void applyHarshLoss(ServerPlayer player) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
         variables.Xp = Math.max(0.0, variables.Xp * 0.75);
         variables.golds = Math.max(0.0, variables.golds * 0.9);
      });
   }

   private static boolean isSoloDungeon(Level level) {
      ResourceLocation dimension = level.dimension().location();
      if (!"sololeveling".equals(dimension.getNamespace())) {
         return false;
      }

      String path = dimension.getPath();
      return path.startsWith("dungeon_dimension") || path.equals("cartenon_temple");
   }

   private static boolean isEnemy(Mob mob, String path, boolean excludedAlly) {
      return !excludedAlly && !NON_ENEMY_MONSTER_IDS.contains(path)
         ? mob.getType().is(SOLO_ENEMY_TAG) || mob instanceof SteelFangWolfEntity || mob instanceof Monster
         : false;
   }

   private static boolean isBoss(Mob mob) {
      return mob.getType().is(SOLO_BOSS_TAG) || "boss".equalsIgnoreCase(mob.getPersistentData().getString("slr_dungeon_role"));
   }

   private static boolean isHunterOrSummon(Mob mob, String path) {
      if (mob instanceof HunterEntity) {
         return true;
      }

      if (!path.startsWith("shadow_") && !path.endsWith("_shadow") && !path.contains("_shadow_")) {
         if (mob instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            return true;
         } else if (mob instanceof TamableAnimal tame) {
            return !(mob instanceof SteelFangWolfEntity) ? true : tame.isTame() || tame.getOwnerUUID() != null;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   private static double percentAmount(int percent) {
      int safePercent = Mth.clamp(percent, 25, 400);
      return safePercent == 100 ? 0.0 : safePercent / 100.0 - 1.0;
   }

   private static void replaceModifier(@Nullable AttributeInstance instance, UUID id, String name, double amount, Operation operation) {
      if (instance != null) {
         if (instance.getModifier(id) != null) {
            instance.removeModifier(id);
         }

         if (Double.isFinite(amount) && amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(id, name, amount, operation));
         }
      }
   }
}
