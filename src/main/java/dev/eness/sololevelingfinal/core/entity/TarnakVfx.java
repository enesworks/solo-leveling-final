package dev.eness.sololevelingfinal.core.entity;

import dev.eness.sololevelingfinal.core.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Server-authored particles keep every client looking at the same ability timing. */
final class TarnakVfx {
    private static final DustParticleOptions GREEN_EYE =
            new DustParticleOptions(new Vector3f(0.45F, 1.0F, 0.18F), 0.75F);
    private static final DustParticleOptions RED_CORE =
            new DustParticleOptions(new Vector3f(1.0F, 0.035F, 0.055F), 1.25F);
    private static final DustParticleOptions DEEP_RED =
            new DustParticleOptions(new Vector3f(0.55F, 0.01F, 0.02F), 0.9F);
    private static final DustParticleOptions STEEL_COIL =
            new DustParticleOptions(new Vector3f(0.58F, 0.62F, 0.68F), 1.0F);

    private TarnakVfx() {
    }

    static void steelGuardLock(ServerLevel level, TarnakEntity tarnak) {
        Vec3 center = tarnak.position().add(0.0D, 0.95D, 0.0D);
        for (int ring = 0; ring < 3; ring++) {
            BossCombat.ring(level, center.add(0.0D, ring * 0.36D, 0.0D), 0.72D + ring * 0.18D,
                    ModParticles.CRIMSON_AURA.get(), 22 + ring * 4);
        }
        eyeFlare(level, tarnak, 8);
    }

    static void steelGuardShell(ServerLevel level, TarnakEntity tarnak) {
        double pulse = 1.05D + Math.sin(tarnak.tickCount * 0.32D) * 0.12D;
        Vec3 center = tarnak.position().add(0.0D, 0.15D, 0.0D);
        BossCombat.ring(level, center, pulse, ModParticles.CRIMSON_DUST.get(), 24);
        level.sendParticles(ModParticles.CRIMSON_AURA.get(), tarnak.getX(), tarnak.getY() + 1.0D, tarnak.getZ(),
                5, 0.5D, 0.9D, 0.5D, 0.018D);
    }

    static void steelGuardImpact(ServerLevel level, TarnakEntity tarnak, LivingEntity attacker) {
        Vec3 direction = attacker == null
                ? flatLook(tarnak)
                : attacker.position().subtract(tarnak.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 impact = tarnak.position().add(0.0D, 1.18D, 0.0D).add(direction.scale(0.72D));
        level.sendParticles(ModParticles.CRIMSON_IMPACT.get(), impact.x, impact.y, impact.z,
                5, 0.18D, 0.22D, 0.18D, 0.02D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, impact.x, impact.y, impact.z,
                18, 0.28D, 0.38D, 0.28D, 0.16D);
        level.playSound(null, BlockPos.containing(impact), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.35F, 0.58F);
    }

    static void guardCounterImpact(ServerLevel level, TarnakEntity tarnak, LivingEntity target) {
        Vec3 impact = target.getBoundingBox().getCenter();
        level.sendParticles(ModParticles.CRIMSON_IMPACT.get(), impact.x, impact.y, impact.z,
                8, 0.35D, 0.45D, 0.35D, 0.045D);
        level.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z,
                28, 0.45D, 0.55D, 0.45D, 0.18D);
        BossCombat.ring(level, target.position().add(0.0D, 0.08D, 0.0D), 1.55D,
                ModParticles.CRIMSON_DUST.get(), 34);
        eyeFlare(level, tarnak, 12);
        level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.HOSTILE, 1.45F, 0.62F);
    }

    static void breakerWindup(ServerLevel level, TarnakEntity tarnak, LivingEntity target) {
        Vec3 forward = target == null
                ? flatLook(tarnak)
                : horizontalDirection(tarnak.position(), target.position(), flatLook(tarnak));
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 fist = tarnak.position().add(0.0D, 1.08D, 0.0D)
                .add(forward.scale(0.22D)).add(right.scale(-0.36D));
        level.sendParticles(ModParticles.CRIMSON_DUST.get(), fist.x, fist.y, fist.z,
                4, 0.14D, 0.16D, 0.14D, 0.025D);
        if (tarnak.getActionTick() == 5) {
            eyeFlare(level, tarnak, 10);
            level.playSound(null, tarnak.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 0.9F, 0.68F);
        }
    }

    static void monarchBreakerDash(ServerLevel level, TarnakEntity tarnak, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int steps = Math.max(6, (int)Math.ceil(delta.length() * 5.0D));
        for (int step = 0; step <= steps; step++) {
            double amount = (double)step / (double)steps;
            Vec3 point = start.lerp(end, amount).add(0.0D, 0.85D, 0.0D);
            level.sendParticles(ModParticles.CRIMSON_AURA.get(), point.x, point.y, point.z,
                    2, 0.24D, 0.55D, 0.24D, 0.02D);
            level.sendParticles(ParticleTypes.SMOKE, point.x, point.y - 0.55D, point.z,
                    1, 0.18D, 0.08D, 0.18D, 0.01D);
        }
        level.playSound(null, tarnak.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.1F, 0.42F);
    }

    static void monarchBreakerImpact(ServerLevel level, TarnakEntity tarnak, LivingEntity target) {
        Vec3 direction = horizontalDirection(tarnak.position(), target.position(), flatLook(tarnak));
        Vec3 right = new Vec3(-direction.z, 0.0D, direction.x);
        Vec3 origin = tarnak.position().add(0.0D, 0.72D, 0.0D);

        for (int step = 1; step <= 12; step++) {
            double distance = step * (8.0D / 12.0D);
            double width = 0.2D + distance * 0.42D;
            int slices = 3 + step / 2;
            for (int slice = -slices; slice <= slices; slice++) {
                double across = width * slice / slices;
                Vec3 point = origin.add(direction.scale(distance)).add(right.scale(across));
                level.sendParticles(ModParticles.CRIMSON_DUST.get(), point.x, point.y + distance * 0.035D, point.z,
                        1, 0.055D, 0.08D, 0.055D, 0.045D);
            }
        }

        Vec3 impact = target.getBoundingBox().getCenter();
        level.sendParticles(ModParticles.CRIMSON_IMPACT.get(), impact.x, impact.y, impact.z,
                12, 0.55D, 0.75D, 0.55D, 0.055D);
        level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y, impact.z,
                2, 0.2D, 0.25D, 0.2D, 0.0D);
        level.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z,
                42, 0.75D, 0.85D, 0.75D, 0.24D);
        for (int ring = 1; ring <= 3; ring++) {
            BossCombat.ring(level, target.position().add(0.0D, 0.08D + ring * 0.03D, 0.0D),
                    ring * 1.35D, ModParticles.CRIMSON_AURA.get(), 28 + ring * 10);
        }

        BlockState ground = level.getBlockState(target.blockPosition().below());
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    target.getX(), target.getY() + 0.15D, target.getZ(), 48,
                    1.55D, 0.28D, 1.55D, 0.22D);
        }

        eyeFlare(level, tarnak, 16);
        level.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.65F, 0.58F);
        level.playSound(null, target.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.75F, 1.35F);
    }

    static void maximumOutputCharge(ServerLevel level, TarnakEntity tarnak, int tick) {
        double progress = Math.max(0.0D, Math.min(1.0D, (tick - 3.0D) / 17.0D));
        int count = 5 + (int)Math.round(progress * 8.0D);
        level.sendParticles(ModParticles.CRIMSON_AURA.get(), tarnak.getX(), tarnak.getY() + 0.85D, tarnak.getZ(),
                count, 0.62D, 0.98D, 0.62D, 0.045D);

        double spin = tick * 0.74D;
        Vec3 base = tarnak.position();
        for (int layer = 0; layer < 7; layer++) {
            double angle = spin + layer * 1.31D;
            double radius = 0.72D + Math.sin(tick * 0.34D + layer) * 0.11D;
            Vec3 point = base.add(
                    Math.cos(angle) * radius,
                    0.22D + layer * 0.27D,
                    Math.sin(angle) * radius
            );
            level.sendParticles(layer % 2 == 0 ? ModParticles.CRIMSON_AURA.get() : ModParticles.CRIMSON_DUST.get(),
                    point.x, point.y, point.z, 1, 0.025D, 0.035D, 0.025D, 0.018D);
        }

        double inflowRadius = 2.8D - progress * 1.35D;
        for (int ray = 0; ray < 8; ray++) {
            double angle = Math.PI * 2.0D * ray / 8.0D - tick * 0.11D;
            Vec3 point = base.add(Math.sin(angle) * inflowRadius, 0.08D, Math.cos(angle) * inflowRadius);
            level.sendParticles(RED_CORE, point.x, point.y, point.z, 1,
                    0.025D, 0.015D, 0.025D, 0.01D);
        }

        risingColumns(level, base, tick, 5, 1.15D + progress * 0.55D, 2.35D);
        handEnergy(level, tarnak, 2 + (int)(progress * 3.0D));

        if (tick == 6 || tick == 10 || tick == 14 || tick == 18) {
            BossCombat.ring(level, tarnak.position().add(0.0D, 0.08D, 0.0D),
                    2.75D - progress * 1.5D, ModParticles.CRIMSON_DUST.get(), 56);
        }
        if (tick % 4 == 0) {
            eyeFlare(level, tarnak, 8);
        }
    }

    static void maximumOutputBurst(ServerLevel level, TarnakEntity tarnak) {
        Vec3 center = tarnak.position().add(0.0D, 0.9D, 0.0D);
        level.sendParticles(ModParticles.CRIMSON_IMPACT.get(), center.x, center.y, center.z,
                32, 0.85D, 1.15D, 0.85D, 0.085D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z,
                58, 0.95D, 1.25D, 0.95D, 0.19D);
        groundSigil(level, tarnak.position().add(0.0D, 0.055D, 0.0D), tarnak.tickCount * 0.08D, true);
        orbitRing(level, tarnak.position().add(0.0D, 0.62D, 0.0D), 1.12D, 0.0D, 54, ModParticles.CRIMSON_AURA.get());
        orbitRing(level, tarnak.position().add(0.0D, 1.13D, 0.0D), 0.92D, 0.19D, 46, RED_CORE);
        orbitRing(level, tarnak.position().add(0.0D, 1.62D, 0.0D), 0.72D, -0.24D, 38, ModParticles.CRIMSON_DUST.get());
        risingColumns(level, tarnak.position(), tarnak.tickCount, 14, 2.35D, 3.25D);
        eyeFlare(level, tarnak, 34);
        level.playSound(null, tarnak.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.HOSTILE, 1.35F, 0.52F);
        level.playSound(null, tarnak.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0F, 0.72F);
    }

    static void maximumOutputAwakened(ServerLevel level, TarnakEntity tarnak) {
        Vec3 base = tarnak.position().add(0.0D, 0.06D, 0.0D);
        groundSigil(level, base, tarnak.tickCount * 0.11D, false);
        level.sendParticles(ModParticles.CRIMSON_IMPACT.get(), tarnak.getX(), tarnak.getY() + 1.0D, tarnak.getZ(),
                18, 0.72D, 0.95D, 0.72D, 0.06D);
        eyeFlare(level, tarnak, 28);
        level.playSound(null, tarnak.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 0.8F, 1.45F);
    }

    static void maximumOutputAura(ServerLevel level, TarnakEntity tarnak, int phase) {
        if (phase % 2 == 0) {
            level.sendParticles(ModParticles.CRIMSON_AURA.get(), tarnak.getX(), tarnak.getY() + 0.75D, tarnak.getZ(),
                    7, 0.62D, 0.95D, 0.62D, 0.032D);
        }
        if (phase % 3 == 0) {
            risingColumns(level, tarnak.position(), phase, 3, 1.25D, 2.25D);
        }
        if (phase % 4 == 0) {
            orbitRing(level, tarnak.position().add(0.0D, 0.62D, 0.0D), 0.88D,
                    phase * 0.11D, 18, ModParticles.CRIMSON_AURA.get());
            orbitRing(level, tarnak.position().add(0.0D, 1.28D, 0.0D), 0.72D,
                    -phase * 0.14D, 16, ModParticles.CRIMSON_DUST.get());
        }
        if (phase % 8 == 0) {
            BossCombat.ring(level, tarnak.position().add(0.0D, 0.055D, 0.0D),
                    1.25D + ((phase / 8) & 1) * 0.28D, ModParticles.CRIMSON_DUST.get(), 38);
            groundSpokes(level, tarnak.position().add(0.0D, 0.06D, 0.0D), phase * 0.035D, 6, 1.45D, 4);
        }
        if (phase % 2 == 0) {
            handEnergy(level, tarnak, 2);
        }
        if (phase % 16 == 0) {
            eyeFlare(level, tarnak, 8);
        }
    }

    static void maximumOutputThirdHit(ServerLevel level, TarnakEntity tarnak, Vec3 center) {
        for (int ring = 1; ring <= 2; ring++) {
            BossCombat.ring(level, center.add(0.0D, 0.08D, 0.0D), ring * 1.55D,
                    ModParticles.CRIMSON_AURA.get(), 34 + ring * 10);
        }
        level.sendParticles(ModParticles.CRIMSON_IMPACT.get(), center.x, center.y + 0.7D, center.z,
                7, 0.42D, 0.55D, 0.42D, 0.04D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.85F, 0.82F);
    }

    static void ironCannonCharge(ServerLevel level, TarnakEntity tarnak, LivingEntity target, int tick) {
        Vec3 forward = target == null
                ? flatLook(tarnak)
                : horizontalDirection(tarnak.position(), target.position(), flatLook(tarnak));
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 shoulder = tarnak.position().add(0.0D, 1.43D, 0.0D).add(side.scale(-0.34D));
        double progress = Math.max(0.0D, Math.min(1.0D, (tick - 3.0D) / 4.0D));
        Vec3 axis = forward.scale(-0.52D - progress * 0.18D).add(0.0D, -0.22D, 0.0D);
        Vec3 hand = shoulder.add(axis);

        for (int step = 0; step <= 12; step++) {
            double along = step / 12.0D;
            Vec3 center = shoulder.lerp(hand, along);
            double angle = tick * 1.18D + step * 0.92D;
            double radius = 0.11D + progress * 0.09D;
            Vec3 coil = center
                    .add(side.scale(Math.cos(angle) * radius))
                    .add(0.0D, Math.sin(angle) * radius, 0.0D);
            level.sendParticles(STEEL_COIL, coil.x, coil.y, coil.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            if (step % 3 == 0) {
                level.sendParticles(RED_CORE, center.x, center.y, center.z, 1,
                        0.018D, 0.018D, 0.018D, 0.008D);
            }
        }

        level.sendParticles(ModParticles.CRIMSON_AURA.get(), hand.x, hand.y, hand.z,
                5 + tick, 0.14D, 0.14D, 0.14D, 0.035D);
        if (tick == 3) {
            level.playSound(null, tarnak.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 0.8F, 0.72F);
        }
    }

    static void ironCannonTargetLock(ServerLevel level, TarnakEntity tarnak, LivingEntity target) {
        eyeFlare(level, tarnak, 18);
        if (target != null) {
            Vec3 from = tarnak.position().add(0.0D, 1.46D, 0.0D);
            Vec3 to = target.getBoundingBox().getCenter();
            int points = Math.min(18, Math.max(6, (int)Math.ceil(from.distanceTo(to) * 1.3D)));
            for (int point = 1; point < points; point++) {
                Vec3 position = from.lerp(to, point / (double)points);
                level.sendParticles(DEEP_RED, position.x, position.y, position.z, 1,
                        0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        level.playSound(null, tarnak.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.65F, 1.8F);
    }

    static void ironCannonFire(ServerLevel level, TarnakEntity tarnak, Vec3 muzzle, Vec3 direction) {
        for (int step = 0; step < 10; step++) {
            Vec3 point = muzzle.add(direction.scale(step * 0.13D));
            level.sendParticles(step % 2 == 0 ? RED_CORE : STEEL_COIL,
                    point.x, point.y, point.z, 1, 0.02D, 0.02D, 0.02D, 0.015D);
        }
        level.sendParticles(ModParticles.CRIMSON_IMPACT.get(), muzzle.x, muzzle.y, muzzle.z,
                9, 0.22D, 0.22D, 0.22D, 0.045D);
        level.playSound(null, tarnak.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.25F, 0.58F);
        level.playSound(null, tarnak.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.72F, 1.62F);
    }

    static void ironCannonTrail(ServerLevel level, Vec3 start, Vec3 end, int flightTick) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < 1.0E-4D) {
            return;
        }
        Vec3 direction = delta.scale(1.0D / length);
        Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
        if (side.lengthSqr() < 1.0E-4D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            side = side.normalize();
        }
        Vec3 vertical = direction.cross(side).normalize();
        int steps = Math.min(42, Math.max(6, (int)Math.ceil(length * 5.0D)));
        for (int step = 0; step <= steps; step++) {
            double amount = step / (double)steps;
            Vec3 core = start.lerp(end, amount);
            double angle = flightTick * 1.35D + step * 0.82D;
            Vec3 offset = side.scale(Math.cos(angle) * 0.18D)
                    .add(vertical.scale(Math.sin(angle) * 0.18D));
            Vec3 coil = core.add(offset);
            level.sendParticles(RED_CORE, core.x, core.y, core.z, 1,
                    0.012D, 0.012D, 0.012D, 0.008D);
            level.sendParticles(STEEL_COIL, coil.x, coil.y, coil.z, 1,
                    0.0D, 0.0D, 0.0D, 0.0D);
            if (step % 4 == 0) {
                Vec3 opposite = core.subtract(offset);
                level.sendParticles(ModParticles.CRIMSON_DUST.get(), opposite.x, opposite.y, opposite.z,
                        1, 0.025D, 0.025D, 0.025D, 0.012D);
            }
        }
    }

    static void ironCannonImpact(ServerLevel level, TarnakEntity tarnak, Vec3 impact, BlockPos groundPos) {
        level.sendParticles(ModParticles.CRIMSON_IMPACT.get(), impact.x, impact.y, impact.z,
                18, 0.58D, 0.58D, 0.58D, 0.085D);
        level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y, impact.z,
                2, 0.18D, 0.18D, 0.18D, 0.0D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, impact.x, impact.y, impact.z,
                34, 0.58D, 0.68D, 0.58D, 0.18D);
        for (int ring = 1; ring <= 3; ring++) {
            BossCombat.ring(level, new Vec3(impact.x, groundPos.getY() + 1.04D, impact.z),
                    ring * 0.88D, ring == 2 ? STEEL_COIL : ModParticles.CRIMSON_AURA.get(), 26 + ring * 8);
        }

        BlockState ground = level.getBlockState(groundPos);
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                    impact.x, groundPos.getY() + 1.08D, impact.z, 52,
                    1.15D, 0.32D, 1.15D, 0.24D);
        }
        eyeFlare(level, tarnak, 14);
        level.playSound(null, BlockPos.containing(impact), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.35F, 0.72F);
        level.playSound(null, BlockPos.containing(impact), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.72F, 1.55F);
    }

    private static void handEnergy(ServerLevel level, TarnakEntity tarnak, int count) {
        Vec3 forward = flatLook(tarnak);
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 center = tarnak.position().add(0.0D, 0.92D, 0.0D).add(forward.scale(0.08D));
        Vec3 first = center.add(right.scale(0.43D));
        Vec3 second = center.add(right.scale(-0.43D));
        level.sendParticles(RED_CORE, first.x, first.y, first.z, count,
                0.08D, 0.16D, 0.08D, 0.018D);
        level.sendParticles(RED_CORE, second.x, second.y, second.z, count,
                0.08D, 0.16D, 0.08D, 0.018D);
    }

    private static void risingColumns(
            ServerLevel level,
            Vec3 base,
            int phase,
            int strands,
            double radius,
            double height
    ) {
        for (int strand = 0; strand < strands; strand++) {
            double angle = Math.PI * 2.0D * strand / strands + phase * 0.17D;
            double waveRadius = radius * (0.72D + 0.28D * Math.sin(phase * 0.13D + strand * 1.7D));
            double y = 0.08D + Math.floorMod(phase * 3 + strand * 7, 24) / 24.0D * height;
            Vec3 point = base.add(Math.sin(angle) * waveRadius, y, Math.cos(angle) * waveRadius);
            level.sendParticles(ModParticles.CRIMSON_AURA.get(), point.x, point.y, point.z,
                    2, 0.025D, 0.18D, 0.025D, 0.035D);
            level.sendParticles(RED_CORE, point.x, point.y + 0.13D, point.z,
                    1, 0.0D, 0.08D, 0.0D, 0.02D);
        }
    }

    private static void orbitRing(
            ServerLevel level,
            Vec3 center,
            double radius,
            double rotation,
            int points,
            net.minecraft.core.particles.ParticleOptions particle
    ) {
        for (int point = 0; point < points; point++) {
            double angle = Math.PI * 2.0D * point / points + rotation;
            double ripple = Math.sin(angle * 3.0D + rotation) * 0.035D;
            level.sendParticles(particle,
                    center.x + Math.sin(angle) * radius,
                    center.y + ripple,
                    center.z + Math.cos(angle) * radius,
                    1, 0.015D, 0.015D, 0.015D, 0.006D);
        }
    }

    private static void groundSigil(ServerLevel level, Vec3 center, double rotation, boolean grand) {
        int rings = grand ? 5 : 3;
        for (int ring = 1; ring <= rings; ring++) {
            double radius = ring * (grand ? 0.68D : 0.62D);
            orbitRing(level, center.add(0.0D, ring * 0.004D, 0.0D), radius,
                    ring % 2 == 0 ? -rotation : rotation,
                    28 + ring * 10,
                    ring % 2 == 0 ? RED_CORE : ModParticles.CRIMSON_AURA.get());
        }
        groundSpokes(level, center, rotation, grand ? 10 : 6, grand ? 3.35D : 1.95D, grand ? 9 : 6);
    }

    private static void groundSpokes(
            ServerLevel level,
            Vec3 center,
            double rotation,
            int spokes,
            double radius,
            int steps
    ) {
        for (int spoke = 0; spoke < spokes; spoke++) {
            double angle = Math.PI * 2.0D * spoke / spokes + rotation;
            for (int step = 1; step <= steps; step++) {
                double distance = radius * step / steps;
                Vec3 point = center.add(Math.sin(angle) * distance, 0.0D, Math.cos(angle) * distance);
                level.sendParticles(step % 2 == 0 ? RED_CORE : DEEP_RED,
                        point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static void eyeFlare(ServerLevel level, TarnakEntity tarnak, int count) {
        Vec3 forward = flatLook(tarnak);
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 eyes = tarnak.position().add(0.0D, 1.61D, 0.0D).add(forward.scale(0.31D));
        Vec3 leftEye = eyes.add(right.scale(0.12D));
        Vec3 rightEye = eyes.add(right.scale(-0.12D));
        int half = Math.max(1, count / 2);
        level.sendParticles(GREEN_EYE, leftEye.x, leftEye.y, leftEye.z, half,
                0.035D, 0.025D, 0.035D, 0.012D);
        level.sendParticles(GREEN_EYE, rightEye.x, rightEye.y, rightEye.z, half,
                0.035D, 0.025D, 0.035D, 0.012D);
    }

    private static Vec3 flatLook(TarnakEntity tarnak) {
        Vec3 look = tarnak.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        return look.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : look.normalize();
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to, Vec3 fallback) {
        Vec3 direction = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-4D ? fallback : direction.normalize();
    }
}
