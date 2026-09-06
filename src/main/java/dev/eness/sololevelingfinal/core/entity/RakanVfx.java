package dev.eness.sololevelingfinal.core.entity;

import dev.eness.sololevelingfinal.core.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Server-side visual markers for Rakan's boss abilities. Gameplay is handled by RakanEntity. */
final class RakanVfx {
    private static final DustParticleOptions NAVY =
            new DustParticleOptions(new Vector3f(0.02F, 0.055F, 0.16F), 0.95F);
    private static final DustParticleOptions DEEP_NAVY =
            new DustParticleOptions(new Vector3f(0.01F, 0.018F, 0.055F), 1.15F);
    private static final DustParticleOptions CLAW_GOLD =
            new DustParticleOptions(new Vector3f(1.0F, 0.72F, 0.08F), 0.78F);
    private static final DustParticleOptions EYE_GOLD =
            new DustParticleOptions(new Vector3f(1.0F, 0.86F, 0.18F), 0.72F);
    private static final DustParticleOptions ASH =
            new DustParticleOptions(new Vector3f(0.48F, 0.50F, 0.54F), 1.0F);
    private static final DustParticleOptions DARK_ASH =
            new DustParticleOptions(new Vector3f(0.25F, 0.27F, 0.30F), 1.1F);

    private RakanVfx() {
    }

    static void predatorClawHit(ServerLevel level, RakanEntity rakan, LivingEntity target, int hitIndex) {
        Vec3 forward = directionToTarget(rakan, target);
        Vec3 center = target == null || rakan.distanceToSqr(target) > 20.25D
                ? rakan.position().add(0.0D, 1.05D, 0.0D).add(forward.scale(1.05D))
                : target.getBoundingBox().getCenter();

        Vec3 sweep = rakan.position().add(0,1.16,0).add(forward.scale(.8));
        if (hitIndex < 3) {
            level.sendParticles(hitIndex == 2 ? ModParticles.RAKAN_CLAW_LEFT.get() : ModParticles.RAKAN_CLAW_RIGHT.get(),
                    sweep.x,sweep.y,sweep.z,0,forward.x,0,forward.z,1);
            level.playSound(null,rakan.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.HOSTILE,.85F,hitIndex == 2 ? .72F : .85F);
        }
        level.sendParticles(CLAW_GOLD, center.x, center.y, center.z,
                6, 0.16D, 0.20D, 0.16D, 0.02D);
        level.sendParticles(NAVY, center.x, center.y, center.z,
                8, 0.20D, 0.18D, 0.20D, 0.025D);
    }

    static void predatorClawFinish(ServerLevel level, RakanEntity rakan, Vec3 impactCenter) {
        Vec3 center = impactCenter == null || rakan.position().distanceToSqr(impactCenter) > 20.25D
                ? rakan.position().add(flatLook(rakan).scale(1.15D))
                : impactCenter;
        Vec3 raised = center.add(0.0D, 0.32D, 0.0D);
        level.sendParticles(CLAW_GOLD, raised.x, raised.y, raised.z,
                12, 0.36D, 0.34D, 0.36D, 0.045D);
        level.sendParticles(DEEP_NAVY, raised.x, raised.y, raised.z,
                14, 0.40D, 0.30D, 0.40D, 0.04D);
        dustRing(level, groundAt(rakan, center), 3.0D, 40);
        Vec3 ground = groundAt(rakan, center);
        level.sendParticles(ModParticles.RAKAN_SLAM.get(),ground.x,ground.y+.06,ground.z,0,0,.75,1,1);
        groundDebris(level, ground, .75);
        level.playSound(null,rakan.blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundSource.HOSTILE,.45F,1.15F);
    }

    static void guardBlock(ServerLevel level, RakanEntity rakan, LivingEntity attacker) {
        Vec3 forward = directionFromAttacker(rakan, attacker);
        Vec3 right = rightOf(forward);
        Vec3 chest = rakan.position().add(0.0D, 1.10D, 0.0D).add(forward.scale(0.38D));
        level.sendParticles(ModParticles.RAKAN_BLOCK.get(),chest.x,chest.y,chest.z,0,forward.x,0,forward.z,1);
        level.playSound(null,rakan.blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.HOSTILE,1F,.65F);
        forearmRing(level, chest.add(right.scale(0.19D)), right, forward);
        forearmRing(level, chest.add(right.scale(-0.19D)), right.scale(-1.0D), forward);
        dustRing(level, rakan.position().add(0.0D, 0.04D, 0.0D), 0.82D, 22);
        level.sendParticles(ParticleTypes.POOF, chest.x, chest.y, chest.z,
                5, 0.18D, 0.20D, 0.18D, 0.025D);
    }

    static void guardCounter(ServerLevel level, RakanEntity rakan, LivingEntity target) {
        Vec3 forward = directionToTarget(rakan, target);
        Vec3 right = rightOf(forward);
        Vec3 impact = target == null || rakan.distanceToSqr(target) > 25.0D
                ? rakan.position().add(0.0D, 1.12D, 0.0D).add(forward.scale(1.15D))
                : target.getBoundingBox().getCenter();
        level.sendParticles(ModParticles.RAKAN_COUNTER.get(),impact.x,impact.y,impact.z,0,forward.x,0,forward.z,1);
        level.playSound(null,rakan.blockPosition(),SoundEvents.PLAYER_ATTACK_KNOCKBACK,SoundSource.HOSTILE,.9F,.65F);
        level.sendParticles(NAVY, impact.x, impact.y, impact.z,
                16, 0.36D, 0.42D, 0.36D, 0.055D);
        level.sendParticles(CLAW_GOLD, impact.x, impact.y, impact.z,
                10, 0.25D, 0.32D, 0.25D, 0.045D);
        shortArc(level, impact, right, 0.48D, 18, ASH);
    }

    static void pounceLaunch(ServerLevel level, RakanEntity rakan) {
        Vec3 base = rakan.position().add(0.0D, 0.05D, 0.0D);
        dustRing(level, base, 0.95D, 28);
        level.sendParticles(ParticleTypes.CLOUD, rakan.getX(), rakan.getY() + 0.12D, rakan.getZ(),
                18, 0.46D, 0.08D, 0.46D, 0.055D);
        eyeGlow(level, rakan, 8);
        level.playSound(null,rakan.blockPosition(),SoundEvents.RAVAGER_STEP,SoundSource.HOSTILE,1F,.75F);
    }

    static void pounceTrail(ServerLevel level, RakanEntity rakan, Vec3 start, Vec3 end, int tick) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 1.0E-4D) {
            return;
        }
        Vec3 direction = delta.scale(1.0D / length);
        Vec3 right = rightOf(direction);
        int steps = Math.min(28, Math.max(5, (int)Math.ceil(length * 3.5D)));
        for (int step = 0; step <= steps; step++) {
            double amount = step / (double)steps;
            Vec3 core = start.lerp(end, amount).add(0.0D, 0.82D + Math.sin(amount * Math.PI) * 0.28D, 0.0D);
            double wave = Math.sin(tick * 0.75D + step * 0.9D) * 0.12D;
            Vec3 point = core.add(right.scale(wave));
            level.sendParticles(step % 2 == 0 ? NAVY : CLAW_GOLD,
                    point.x, point.y, point.z, 1, 0.01D, 0.01D, 0.01D, 0.002D);
            if (step % 5 == 0) {
                level.sendParticles(ASH, point.x, point.y - 0.52D, point.z,
                        1, 0.04D, 0.025D, 0.04D, 0.006D);
            }
        }
    }

    static void pounceImpact(ServerLevel level, RakanEntity rakan, Vec3 impactCenter) {
        Vec3 center = impactCenter == null ? rakan.position() : impactCenter;
        Vec3 ground = groundAt(rakan, center);
        level.sendParticles(ModParticles.RAKAN_SLAM.get(),ground.x,ground.y+.06,ground.z,0,0,0,1,1);
        level.playSound(null,rakan.blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundSource.HOSTILE,1.1F,.65F);
        dustRing(level, ground, 4.0D, 54);
        groundDebris(level, ground, 1);
        pressureRing(level, ground.add(0.0D, 0.18D, 0.0D), 1.55D, 36, NAVY);
        level.sendParticles(ParticleTypes.POOF, center.x, center.y + 0.18D, center.z,
                20, 0.56D, 0.18D, 0.56D, 0.08D);
        level.sendParticles(CLAW_GOLD, center.x, center.y + 0.42D, center.z,
                12, 0.34D, 0.22D, 0.34D, 0.035D);
    }

    static void roarBreathe(ServerLevel level, RakanEntity rakan, int tick) {
        Vec3 chest = rakan.position().add(0.0D, 1.25D, 0.0D).add(flatLook(rakan).scale(0.24D));
        level.sendParticles(DEEP_NAVY, chest.x, chest.y, chest.z,
                3, 0.18D, 0.25D, 0.18D, 0.015D);
        if (tick % 3 == 0) {
            eyeGlow(level, rakan, 4);
        }
    }

    static void roarRelease(ServerLevel level, RakanEntity rakan) {
        Vec3 base = rakan.position().add(0.0D, 0.08D, 0.0D);
        Vec3 forward = flatLook(rakan);
        level.sendParticles(ModParticles.RAKAN_ROAR.get(),base.x,base.y+1.1,base.z,0,forward.x,0,forward.z,1);
        level.sendParticles(DEEP_NAVY, rakan.getX(), rakan.getY() + 1.22D, rakan.getZ(),
                22, 0.55D, 0.62D, 0.55D, 0.045D);
        level.sendParticles(ParticleTypes.CLOUD, rakan.getX(), rakan.getY() + 0.18D, rakan.getZ(),
                24, 0.72D, 0.08D, 0.72D, 0.035D);
        eyeGlow(level, rakan, 18);
    }

    private static void groundDebris(ServerLevel level, Vec3 center, double strength) {
        var ground = level.getBlockState(BlockPos.containing(center.x, center.y - .15, center.z));
        if (ground.isAir() || ground.getRenderShape() == RenderShape.INVISIBLE) {
            ground = Blocks.STONE.defaultBlockState();
        }
        var chips = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        int count = (int)(32 * strength);
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2 / count;
            double x = Math.cos(angle), z = Math.sin(angle);
            double speed = (.18 + (i % 4) * .045) * strength;
            level.sendParticles(chips, center.x + x * .3, center.y + .1, center.z + z * .3,
                    0, x * speed, .18 + (i % 3) * .055, z * speed, 1);
        }
        // Low outward puffs leave the upper body and the three claw marks readable.
        int puffs = (int)(16 * strength);
        for (int i = 0; i < puffs; i++) {
            double angle = i * Math.PI * 2 / puffs;
            double x = Math.cos(angle), z = Math.sin(angle);
            level.sendParticles(ParticleTypes.CLOUD, center.x + x * .4, center.y + .12, center.z + z * .4,
                    0, x * .16 * strength, .035, z * .16 * strength, 1);
        }
        level.sendParticles(ParticleTypes.POOF, center.x, center.y + .2, center.z,
                (int)(22 * strength), .65 * strength, .14, .65 * strength, .055);
    }

    private static void forearmRing(ServerLevel level, Vec3 center, Vec3 axis, Vec3 forward) {
        Vec3 vertical = new Vec3(0.0D, 1.0D, 0.0D);
        for (int point = 0; point < 18; point++) {
            double angle = Math.PI * 2.0D * point / 18.0D;
            Vec3 offset = axis.scale(Math.cos(angle) * 0.22D)
                    .add(vertical.scale(Math.sin(angle) * 0.22D))
                    .add(forward.scale(Math.sin(angle * 2.0D) * 0.035D));
            Vec3 particle = center.add(offset);
            level.sendParticles(point % 2 == 0 ? ASH : DARK_ASH,
                    particle.x, particle.y, particle.z, 1, 0.008D, 0.008D, 0.008D, 0.002D);
        }
    }

    private static void dustRing(ServerLevel level, Vec3 center, double radius, int points) {
        for (int point = 0; point < points; point++) {
            double angle = Math.PI * 2.0D * point / points;
            double wave = 0.06D * Math.sin(angle * 4.0D);
            Vec3 particle = center.add(Math.sin(angle) * radius, wave, Math.cos(angle) * radius);
            level.sendParticles(point % 4 == 0 ? ParticleTypes.CLOUD : ASH,
                    particle.x, particle.y, particle.z, 1, 0.035D, 0.025D, 0.035D, 0.006D);
        }
    }

    private static void pressureRing(ServerLevel level, Vec3 center, double radius, int points, ParticleOptions particle) {
        for (int point = 0; point < points; point++) {
            double angle = Math.PI * 2.0D * point / points;
            double ripple = Math.sin(angle * 3.0D) * 0.045D;
            level.sendParticles(
                    particle,
                    center.x + Math.sin(angle) * radius,
                    center.y + ripple,
                    center.z + Math.cos(angle) * radius,
                    1,
                    0.02D,
                    0.02D,
                    0.02D,
                    0.004D
            );
        }
    }

    private static void shortArc(ServerLevel level, Vec3 center, Vec3 side, double radius, int points, ParticleOptions particle) {
        for (int point = 0; point < points; point++) {
            double amount = point / (double)(points - 1);
            double angle = -0.75D + amount * 1.5D;
            Vec3 position = center.add(side.scale(Math.sin(angle) * radius)).add(0.0D, Math.cos(angle) * 0.18D, 0.0D);
            level.sendParticles(particle, position.x, position.y, position.z,
                    1, 0.012D, 0.012D, 0.012D, 0.002D);
        }
    }

    private static void eyeGlow(ServerLevel level, RakanEntity rakan, int count) {
        Vec3 forward = flatLook(rakan);
        Vec3 right = rightOf(forward);
        Vec3 eyes = rakan.position().add(0.0D, 1.55D, 0.0D).add(forward.scale(0.29D));
        Vec3 leftEye = eyes.add(right.scale(0.105D));
        Vec3 rightEye = eyes.add(right.scale(-0.105D));
        int half = Math.max(1, count / 2);
        level.sendParticles(EYE_GOLD, leftEye.x, leftEye.y, leftEye.z,
                half, 0.025D, 0.018D, 0.025D, 0.006D);
        level.sendParticles(EYE_GOLD, rightEye.x, rightEye.y, rightEye.z,
                half, 0.025D, 0.018D, 0.025D, 0.006D);
    }

    private static Vec3 groundAt(RakanEntity rakan, Vec3 center) {
        return new Vec3(center.x, rakan.getY() + 0.055D, center.z);
    }

    private static Vec3 directionToTarget(RakanEntity rakan, LivingEntity target) {
        return target == null ? flatLook(rakan) : horizontalDirection(rakan.position(), target.position(), flatLook(rakan));
    }

    private static Vec3 directionFromAttacker(RakanEntity rakan, LivingEntity attacker) {
        return attacker == null ? flatLook(rakan) : horizontalDirection(attacker.position(), rakan.position(), flatLook(rakan));
    }

    private static Vec3 flatLook(RakanEntity rakan) {
        Vec3 look = rakan.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        return look.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : look.normalize();
    }

    private static Vec3 rightOf(Vec3 forward) {
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        return right.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to, Vec3 fallback) {
        Vec3 direction = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-4D ? fallback : direction.normalize();
    }
}
