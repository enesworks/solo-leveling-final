package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class StealthProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!CooldownManager.isOnCooldown(entity, "Stealth")) {
            CooldownManager.set(entity, "Stealth", 400);
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 1, false, false));
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 20, 1.0, 1.0, 1.0, 1.0);
            }

            if (world instanceof ServerLevel _level) {
               Entity entityToSpawn = SololevelingModEntities.AFTER_IMAGE.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
               if (entityToSpawn != null) {
                  entityToSpawn.setYRot(entity.getYRot());
                  entityToSpawn.setYBodyRot(entity.getYRot());
                  entityToSpawn.setYHeadRot(entity.getYRot());
                  entityToSpawn.setXRot(entity.getXRot());
                  entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
               }
            }

            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entity != entityiterator && entityiterator instanceof Mob) {
                  try {
                     if (!StealthBossDetectionHelper.seesThroughStealth(entityiterator)) {
                        ((Mob)entityiterator).setTarget(null);
                     }
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               }
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("skill on cooldown"), true);
         }
      }
   }
}
