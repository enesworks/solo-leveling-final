package dev.eness.sololevelingfinal.core.entity;

import dev.eness.sololevelingfinal.core.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final class BossCombat {
    private BossCombat() {
    }

    static void ring(ServerLevel level, Vec3 center, double radius, ParticleOptions particle, int points) {
        for (int index = 0; index < points; index++) {
            double angle = Math.PI * 2.0D * index / points;
            level.sendParticles(
                    particle,
                    center.x + Math.sin(angle) * radius,
                    center.y,
                    center.z + Math.cos(angle) * radius,
                    1,
                    0.06D,
                    0.04D,
                    0.06D,
                    0.01D
            );
        }
    }

    static void chainPrison(ServerLevel level, LivingEntity entity) {
        double height = Math.max(2.0D, entity.getBbHeight());
        for (int strand = 0; strand < 8; strand++) {
            double angle = Math.PI * 2.0D * strand / 8.0D + entity.tickCount * 0.025D;
            double radius = 1.25D + (strand % 2) * 0.45D;
            for (int step = 0; step < 5; step++) {
                double y = entity.getY() + 0.25D + height * step / 4.0D;
                double twist = angle + step * 0.22D;
                level.sendParticles(
                        ModParticles.DARK_CHAIN.get(),
                        entity.getX() + Math.sin(twist) * radius,
                        y,
                        entity.getZ() + Math.cos(twist) * radius,
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
    }

    static void damageArea(Mob source, Vec3 center, double radius, float damage, double horizontalKnockback, double verticalKnockback) {
        if (!(source.level() instanceof ServerLevel level)) {
            return;
        }

        Set<UUID> hit = new HashSet<>();
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(center, radius * 2.0D, Math.max(3.0D, radius), radius * 2.0D),
                candidate -> candidate != source && candidate.isAlive() && !source.isAlliedTo(candidate) && validTarget(candidate)
        )) {
            if (target.distanceToSqr(center) > radius * radius || !hit.add(target.getUUID())) {
                continue;
            }

            if (target.hurt(source.damageSources().mobAttack(source), damage)) {
                Vec3 away = target.position().subtract(center);
                if (away.horizontalDistanceSqr() < 0.001D) {
                    away = source.getLookAngle();
                }
                Vec3 push = away.normalize().scale(horizontalKnockback);
                target.push(push.x, verticalKnockback, push.z);
                target.hurtMarked = true;
            }
        }
    }

    static void freezeArea(Mob source, Vec3 center, double radius, float damage, int slowTicks, int amplifier) {
        if (!(source.level() instanceof ServerLevel level)) {
            return;
        }

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(center, radius * 2.0D, Math.max(4.0D, radius), radius * 2.0D),
                candidate -> candidate != source && candidate.isAlive() && !source.isAlliedTo(candidate) && validTarget(candidate)
        )) {
            if (target.distanceToSqr(center) <= radius * radius) {
                target.hurt(source.damageSources().freeze(), damage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, amplifier, false, true));
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 80, target.getTicksFrozen() + 100));
            }
        }
    }

    static void frostBurst(ServerLevel level, Vec3 center, double radius, int points) {
        ring(level, center, radius, ModParticles.FROST.get(), points);
        level.sendParticles(ModParticles.FROST.get(), center.x, center.y + 0.8D, center.z, points / 2, radius * 0.3D, 0.6D, radius * 0.3D, 0.08D);
    }

    static void spike(ServerLevel level, Vec3 position, int count) {
        level.sendParticles(ModParticles.ICE_SPIKE.get(), position.x, position.y + 0.3D, position.z, count, 0.35D, 0.65D, 0.35D, 0.02D);
    }

    private static boolean validTarget(LivingEntity candidate) {
        return !(candidate instanceof Player player) || (!player.isCreative() && !player.isSpectator());
    }
}
