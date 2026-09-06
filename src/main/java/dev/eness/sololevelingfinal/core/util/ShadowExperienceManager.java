package dev.eness.sololevelingfinal.core.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;

public final class ShadowExperienceManager {
   private static final String LEDGER = "sl_shadow_xp_ledger";
   private static final String TOTAL_DAMAGE = "total_damage";
   private static final String CONTRIBUTORS = "contributors";
   private static final String OWNER = "owner";
   private static final String SHADOW_ID = "shadow_id";
   private static final String SHADOW_ENTITY = "shadow_entity";
   private static final String DAMAGE = "damage";
   private static final String AWARDED = "awarded";
   private static final double MAX_TRACKED_DAMAGE = 1.0E9;
   private static final TagKey<EntityType<?>> SOLO_BOSS_TAG = entityTypeTag("minecraft", "soloboss");
   private static final TagKey<EntityType<?>> HIGH_TIER_TAG = entityTypeTag("minecraft", "hightier");
   private static final TagKey<EntityType<?>> MID_TIER_TAG = entityTypeTag("minecraft", "midtier");

   private ShadowExperienceManager() {
   }

   public static void recordDamage(LivingDamageEvent event) {
      if (event != null
         && !event.isCanceled()
         && !event.getEntity().level().isClientSide()
         && !JobChangeQuestManager.isAttemptEntity(event.getEntity())
         && isEligibleTarget(event.getEntity())) {
         LivingEntity victim = event.getEntity();
         double damage = Math.min(Math.max(0.0, event.getAmount()), Math.max(0.0, victim.getHealth()));
         if (Double.isFinite(damage) && !(damage <= 0.0)) {
            CompoundTag ledger = victim.getPersistentData().contains("sl_shadow_xp_ledger", 10)
               ? victim.getPersistentData().getCompound("sl_shadow_xp_ledger")
               : new CompoundTag();
            ledger.putDouble("total_damage", boundedAdd(ledger.getDouble("total_damage"), damage));
            Entity shadow = resolveShadow(event.getSource().getEntity());
            if (shadow == null) {
               shadow = resolveShadow(event.getSource().getDirectEntity());
            }

            if (shadow != null) {
               recordShadowContribution(ledger, shadow, damage);
            }

            victim.getPersistentData().put("sl_shadow_xp_ledger", ledger);
         }
      }
   }

   public static int awardContributions(LivingEntity victim) {
      if (!JobChangeQuestManager.isAttemptEntity(victim) && isEligibleTarget(victim) && victim.level() instanceof ServerLevel level) {
         CompoundTag persistent = victim.getPersistentData();
         if (!persistent.contains("sl_shadow_xp_ledger", 10)) {
            return 0;
         }

         CompoundTag ledger = persistent.getCompound("sl_shadow_xp_ledger");
         if (ledger.getBoolean("awarded")) {
            return 0;
         }

         ledger.putBoolean("awarded", true);
         persistent.put("sl_shadow_xp_ledger", ledger);
         ListTag contributors = ledger.getList("contributors", 10);
         if (contributors.isEmpty()) {
            return 0;
         }

         int targetPool = targetXpPool(victim);
         double countedTargetDamage = Math.max(Math.max(1.0, victim.getMaxHealth()), ledger.getDouble("total_damage"));
         int awardedCount = 0;
         MinecraftServer server = level.getServer();

         for (int index = 0; index < contributors.size(); index++) {
            CompoundTag contribution = contributors.getCompound(index);
            if (contribution.hasUUID("owner")) {
               String shadowId = contribution.getString("shadow_id");
               double shadowDamage = contribution.getDouble("damage");
               if (!shadowId.isEmpty() && Double.isFinite(shadowDamage) && !(shadowDamage <= 0.0)) {
                  ServerPlayer owner = server.getPlayerList().getPlayer(contribution.getUUID("owner"));
                  if (owner != null) {
                     Entity shadowEntity = contribution.hasUUID("shadow_entity") ? findEntity(server, contribution.getUUID("shadow_entity")) : null;
                     int earned = ShadowMonarchManager.grantCombatXp(owner, shadowId, shadowEntity, targetPool, shadowDamage, countedTargetDamage);
                     if (earned > 0) {
                        awardedCount++;
                     }
                  }
               }
            }
         }

         return awardedCount;
      } else {
         return 0;
      }
   }

   public static int targetXpPool(LivingEntity target) {
      if (target == null) {
         return 0;
      }

      CompoundTag data = target.getPersistentData();
      int configuredBaseXp = data.contains("slr_dungeon_base_xp", 99) ? Math.max(0, data.getInt("slr_dungeon_base_xp")) : -1;
      String role = data.getString("slr_dungeon_role");
      boolean boss = "boss".equals(role)
         || target.getType().is(SOLO_BOSS_TAG)
         || target.getType().is(HIGH_TIER_TAG)
         || target.getType() == EntityType.WITHER
         || target.getType() == EntityType.ENDER_DRAGON;
      boolean elite = boss || "elite".equals(role) || target.getType().is(MID_TIER_TAG);
      return ShadowExperienceRules.targetXpPool(
         target.getMaxHealth(),
         attributeValue(target, Attributes.ATTACK_DAMAGE),
         attributeValue(target, Attributes.ARMOR),
         attributeValue(target, Attributes.ARMOR_TOUGHNESS),
         DungeonLevelHelper.levelOf(target),
         configuredBaseXp,
         elite,
         boss,
         target instanceof Animal
      );
   }

   @Nullable
   public static Entity resolveShadow(@Nullable Entity source) {
      return resolveShadow(source, new HashSet<>());
   }

   private static Entity resolveShadow(@Nullable Entity source, Set<UUID> visited) {
      if (source == null || !visited.add(source.getUUID())) {
         return null;
      } else if (ShadowMonarchManager.isTrackedShadowEntity(source) && !ShadowMonarchManager.getShadowRosterId(source).isEmpty()) {
         return source;
      } else {
         return source instanceof Projectile projectile ? resolveShadow(projectile.getOwner(), visited) : null;
      }
   }

   private static boolean isEligibleTarget(@Nullable LivingEntity target) {
      return target instanceof Mob && !ShadowMonarchManager.isShadowEntity(target) && !ShadowMonarchManager.isTrackedShadowEntity(target);
   }

   private static void recordShadowContribution(CompoundTag ledger, Entity shadow, double damage) {
      UUID ownerId = ShadowMonarchManager.getShadowOwnerUUID(shadow);
      String shadowId = ShadowMonarchManager.getShadowRosterId(shadow);
      if (ownerId != null && !shadowId.isEmpty()) {
         ListTag contributors = ledger.getList("contributors", 10);
         CompoundTag entry = null;

         for (int index = 0; index < contributors.size(); index++) {
            CompoundTag candidate = contributors.getCompound(index);
            if (candidate.hasUUID("owner") && ownerId.equals(candidate.getUUID("owner")) && shadowId.equals(candidate.getString("shadow_id"))) {
               entry = candidate;
               break;
            }
         }

         if (entry == null) {
            entry = new CompoundTag();
            entry.putUUID("owner", ownerId);
            entry.putString("shadow_id", shadowId);
            contributors.add(entry);
         }

         entry.putUUID("shadow_entity", shadow.getUUID());
         entry.putDouble("damage", boundedAdd(entry.getDouble("damage"), damage));
         ledger.put("contributors", contributors);
      }
   }

   private static double boundedAdd(double current, double addition) {
      double safeCurrent = Double.isFinite(current) ? Math.max(0.0, current) : 0.0;
      double safeAddition = Double.isFinite(addition) ? Math.max(0.0, addition) : 0.0;
      return Math.min(1.0E9, safeCurrent + safeAddition);
   }

   private static double attributeValue(LivingEntity entity, Attribute attribute) {
      AttributeInstance instance = entity.getAttribute(attribute);
      return instance == null ? 0.0 : Math.max(0.0, instance.getValue());
   }

   @Nullable
   private static Entity findEntity(MinecraftServer server, UUID entityId) {
      for (ServerLevel level : server.getAllLevels()) {
         Entity entity = level.getEntity(entityId);
         if (entity != null) {
            return entity;
         }
      }

      return null;
   }

   private static TagKey<EntityType<?>> entityTypeTag(String namespace, String path) {
      return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(namespace, path));
   }
}
