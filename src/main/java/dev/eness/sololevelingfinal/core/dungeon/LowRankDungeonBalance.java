package dev.eness.sololevelingfinal.core.dungeon;

import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public final class LowRankDungeonBalance {
   public static final String PROCEDURAL_RANK_TAG = "slr_procedural_mob_rank";
   private static final ResourceKey<Level> LOW_RANK_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling", "dungeon_dimension_d")
   );
   private static final TagKey<EntityType<?>> DUNGEON_MOBS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("dm"));
   private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("ea555382-31fc-4414-8d06-93b25e3dd0d8");
   private static final double E_HEALTH_MULTIPLIER = 0.65;
   private static final double D_HEALTH_MULTIPLIER = 0.8;
   private static final float E_DAMAGE_MULTIPLIER = 0.55F;
   private static final float D_DAMAGE_MULTIPLIER = 0.7F;

   private LowRankDungeonBalance() {
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide() && event.getLevel().dimension() == LOW_RANK_DIMENSION && isDungeonEnemy(event.getEntity())) {
         applyMobBalance(event.getEntity(), rankFromTag(event.getEntity()));
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onLivingHurt(LivingHurtEvent event) {
      if (event.getEntity() instanceof Player victim
         && !victim.level().isClientSide()
         && victim.level().dimension() == LOW_RANK_DIMENSION
         && !(event.getAmount() <= 0.0F)) {
         Entity attacker = resolveAttacker(event.getSource());
         if (isDungeonEnemy(attacker) && attacker.level().dimension() == LOW_RANK_DIMENSION) {
            ProceduralDungeonRank rank = rankFromTag(attacker);
            float multiplier = rank == ProceduralDungeonRank.E ? 0.55F : 0.7F;
            event.setAmount(event.getAmount() * multiplier);
         }
      }
   }

   public static void applyMobBalance(Entity entity, ProceduralDungeonRank rank) {
      if (entity instanceof LivingEntity living && isDungeonEnemy(entity) && (rank == ProceduralDungeonRank.E || rank == ProceduralDungeonRank.D)) {
         double multiplier = rank == ProceduralDungeonRank.E ? 0.65 : 0.8;
         AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
         if (maxHealth != null) {
            if (maxHealth.getModifier(HEALTH_MODIFIER_ID) != null) {
               maxHealth.removeModifier(HEALTH_MODIFIER_ID);
            }

            maxHealth.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, "SLR low-rank dungeon health", multiplier - 1.0, Operation.MULTIPLY_TOTAL));
            living.setHealth(Math.min(living.getHealth(), living.getMaxHealth()));
         }
      }
   }

   private static ProceduralDungeonRank rankFromTag(Entity entity) {
      return entity == null
         ? ProceduralDungeonRank.D
         : ProceduralDungeonRank.tryParse(entity.getPersistentData().getString("slr_procedural_mob_rank"))
            .filter(rank -> rank == ProceduralDungeonRank.E || rank == ProceduralDungeonRank.D)
            .orElse(ProceduralDungeonRank.D);
   }

   private static boolean isDungeonEnemy(Entity entity) {
      return entity != null && !(entity instanceof Player) && (entity instanceof Monster || entity.getType().is(DUNGEON_MOBS));
   }

   private static Entity resolveAttacker(DamageSource source) {
      if (source == null) {
         return null;
      }

      Entity attacker = source.getEntity();
      if (attacker == null) {
         attacker = source.getDirectEntity();
      }

      for (int depth = 0; depth < 4 && attacker != null; depth++) {
         Entity owner = null;
         if (attacker instanceof Projectile projectile) {
            owner = projectile.getOwner();
         }

         if (owner == null && attacker instanceof OwnableEntity ownable) {
            owner = ownable.getOwner();
         }

         if (owner == null || owner == attacker) {
            break;
         }

         attacker = owner;
      }

      return attacker;
   }
}
