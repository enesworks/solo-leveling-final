package dev.eness.sololevelingfinal.core.util;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

@EventBusSubscriber(modid = "sololeveling")
public final class ShadowEquipmentCombatHandler {
   private static final float TUSK_ORB_DAMAGE_MULTIPLIER = 2.0F;
   private static final String IGRIS_STORM_COOLDOWN = "sl_igris_equipment_storm_at";
   private static final int IGRIS_STORM_COOLDOWN_TICKS = 24;
   private static final double IGRIS_STORM_RADIUS = 7.5;
   private static final double IGRIS_STORM_RADIUS_SQR = 56.25;
   private static final int IGRIS_STORM_MAX_TARGETS = 4;
   private static final float IGRIS_STORM_DAMAGE = 8.0F;

   private ShadowEquipmentCombatHandler() {
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void amplifyEquippedTuskDamage(LivingHurtEvent event) {
      if (!event.getEntity().level().isClientSide() && !(event.getAmount() <= 0.0F) && isOrbAmplifiedTuskDamage(event.getSource())) {
         event.setAmount(event.getAmount() * 2.0F);
      }
   }

   public static boolean isOrbAmplifiedTuskDamage(DamageSource source) {
      Entity attacker = resolveAttackActor(source);
      return attacker instanceof TuskShadowEntity && ShadowMonarchManager.isEquipmentEquipped(attacker, SololevelingModItems.ORB_OF_AVARICE.get());
   }

   public static void tryIgrisImpactStorm(LevelAccessor world, Entity source, Vec3 center) {
      if (world instanceof ServerLevel level
         && source instanceof IgrisShadowEntity igris
         && center != null
         && igris.isAlive()
         && igris.level() == level
         && ShadowMonarchManager.isEquipmentEquipped(igris, SololevelingModItems.DEMON_KINGS_LONG_SWORD.get())) {
         CompoundTag data = igris.getPersistentData();
         long now = level.getGameTime();
         if (now >= data.getLong("sl_igris_equipment_storm_at")) {
            List<LivingEntity> candidates = level.getEntitiesOfClass(
                  LivingEntity.class,
                  new AABB(center, center).inflate(7.5),
                  targetx -> targetx.distanceToSqr(center) <= 56.25 && ShadowMonarchManager.canShadowDamage(igris, targetx)
               )
               .stream()
               .sorted(Comparator.comparingDouble(targetx -> targetx.distanceToSqr(center)))
               .toList();
            int struck = 0;

            for (LivingEntity target : candidates) {
               if (struck >= 4) {
                  break;
               }

               if (ShadowMonarchManager.canShadowDamage(igris, target)) {
                  if (struck == 0) {
                     data.putLong("sl_igris_equipment_storm_at", now + 24L);
                  }

                  spawnVisualLightning(level, target.position());
                  target.hurt(
                     new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), igris), 8.0F
                  );
                  struck++;
               }
            }
         }
      }
   }

   private static void spawnVisualLightning(ServerLevel level, Vec3 position) {
      LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
      if (lightning != null) {
         lightning.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(position)));
         lightning.setVisualOnly(true);
         level.addFreshEntity(lightning);
      }
   }

   private static Entity resolveAttackActor(DamageSource source) {
      if (source == null) {
         return null;
      } else {
         Entity attacker = source.getEntity();
         if (attacker instanceof Projectile projectile && projectile.getOwner() != null) {
            return projectile.getOwner();
         } else if (attacker != null) {
            return attacker;
         } else {
            return source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() != null
               ? projectile.getOwner()
               : source.getDirectEntity();
         }
      }
   }
}
