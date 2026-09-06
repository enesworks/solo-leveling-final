package dev.eness.sololevelingfinal.core.client.model;

import dev.eness.sololevelingfinal.core.entity.TarnakEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Vanilla-wide Steve model with storyboard poses layered only over Tarnak's
 * authored abilities. All values below are degrees copied from the 15-frame
 * reference and converted to Minecraft radians only when applied.
 */
public final class TarnakModel extends PlayerModel<TarnakEntity> {
    private static final Rotation ZERO = rotation(0.0F, 0.0F, 0.0F);

    private static final Pose GUARD_READY = pose(
            rotation(-3.0F, 0.0F, 0.0F), rotation(-2.0F, 0.0F, 0.0F),
            rotation(-15.0F, 0.0F, 4.0F), rotation(-15.0F, 0.0F, -4.0F),
            rotation(5.0F, 0.0F, 0.0F), rotation(-5.0F, 0.0F, 0.0F)
    );
    private static final Pose GUARD_ANTICIPATION = pose(
            rotation(-5.0F, 0.0F, 0.0F), rotation(8.0F, 0.0F, 0.0F),
            rotation(-55.0F, -20.0F, -18.0F), rotation(-55.0F, 20.0F, 18.0F),
            rotation(8.0F, 0.0F, 0.0F), rotation(0.0F, 0.0F, -8.0F)
    );
    private static final Pose GUARD_LOCK = pose(
            rotation(5.0F, 0.0F, 0.0F), rotation(6.0F, 0.0F, 0.0F),
            rotation(-78.0F, -35.0F, -28.0F), rotation(-78.0F, 35.0F, 28.0F),
            rotation(8.0F, 0.0F, 0.0F), rotation(0.0F, 0.0F, -8.0F)
    );
    private static final Pose GUARD_IMPACT = pose(
            rotation(-6.0F, 0.0F, 0.0F), rotation(12.0F, 0.0F, 0.0F),
            rotation(-78.0F, -35.0F, -28.0F), rotation(-78.0F, 35.0F, 28.0F),
            rotation(8.0F, 0.0F, 0.0F), rotation(0.0F, 0.0F, -8.0F)
    );
    private static final Pose GUARD_COUNTER = pose(
            rotation(-4.0F, 0.0F, 0.0F), rotation(-8.0F, -25.0F, 0.0F),
            rotation(-92.0F, -15.0F, -5.0F), rotation(-72.0F, 35.0F, 28.0F),
            rotation(-18.0F, 0.0F, 0.0F), rotation(12.0F, 0.0F, 0.0F)
    );

    private static final Pose BREAKER_READY = pose(
            rotation(3.0F, 0.0F, 0.0F), rotation(-2.0F, 0.0F, 0.0F),
            rotation(-20.0F, 0.0F, 0.0F), rotation(-18.0F, 0.0F, 0.0F),
            rotation(8.0F, 0.0F, 0.0F), rotation(-8.0F, 0.0F, 0.0F)
    );
    private static final Pose BREAKER_WINDUP = pose(
            rotation(5.0F, -25.0F, 0.0F), rotation(5.0F, -25.0F, 0.0F),
            rotation(25.0F, -10.0F, -8.0F), rotation(-45.0F, 8.0F, 12.0F),
            rotation(18.0F, 0.0F, 0.0F), rotation(-12.0F, 0.0F, 0.0F)
    );
    private static final Pose BREAKER_DASH = pose(
            rotation(5.0F, -10.0F, 0.0F), rotation(-12.0F, 15.0F, 0.0F),
            rotation(-35.0F, -5.0F, 0.0F), rotation(15.0F, 10.0F, 0.0F),
            rotation(-40.0F, 0.0F, 0.0F), rotation(35.0F, 0.0F, 0.0F)
    );
    private static final Pose BREAKER_IMPACT = pose(
            rotation(-4.0F, -5.0F, 0.0F), rotation(-10.0F, 30.0F, 0.0F),
            rotation(-90.0F, -20.0F, -5.0F), rotation(25.0F, 20.0F, 12.0F),
            rotation(-20.0F, 0.0F, 0.0F), rotation(15.0F, 0.0F, 0.0F)
    );
    private static final Pose BREAKER_RECOVERY = pose(
            ZERO, rotation(5.0F, 10.0F, 0.0F),
            rotation(-35.0F, 0.0F, 0.0F), rotation(-10.0F, 0.0F, 0.0F),
            rotation(8.0F, 0.0F, 0.0F), rotation(-6.0F, 0.0F, 0.0F)
    );

    private static final Pose OUTPUT_READY = pose(
            rotation(-2.0F, 0.0F, 0.0F), rotation(-2.0F, 0.0F, 0.0F),
            rotation(-10.0F, 0.0F, 0.0F), rotation(-10.0F, 0.0F, 0.0F),
            rotation(4.0F, 0.0F, 0.0F), rotation(-4.0F, 0.0F, 0.0F)
    );
    private static final Pose OUTPUT_CLENCH = pose(
            rotation(-6.0F, 0.0F, 0.0F), rotation(8.0F, 0.0F, 0.0F),
            rotation(10.0F, 0.0F, 8.0F), rotation(10.0F, 0.0F, -8.0F),
            rotation(0.0F, 0.0F, 10.0F), rotation(0.0F, 0.0F, -10.0F)
    );
    private static final Pose OUTPUT_CHARGE = pose(
            rotation(-15.0F, 0.0F, 0.0F), rotation(15.0F, 0.0F, 0.0F),
            rotation(18.0F, 0.0F, 12.0F), rotation(18.0F, 0.0F, -12.0F),
            rotation(0.0F, 0.0F, 14.0F), rotation(0.0F, 0.0F, -14.0F)
    );
    private static final Pose OUTPUT_BURST = pose(
            rotation(-8.0F, 0.0F, 0.0F), rotation(-4.0F, 0.0F, 0.0F),
            rotation(-8.0F, 0.0F, 6.0F), rotation(-8.0F, 0.0F, -6.0F),
            ZERO, ZERO
    );

    private static final Pose CANNON_READY = pose(
            ZERO, ZERO, ZERO, ZERO, ZERO, ZERO
    );
    private static final Pose CANNON_ANCHOR = pose(
            rotation(-3.0F, 6.0F, 0.0F), rotation(4.0F, -8.0F, 0.0F),
            rotation(10.0F, -10.0F, -8.0F), rotation(-28.0F, 8.0F, 10.0F),
            rotation(8.0F, 0.0F, 8.0F), rotation(-10.0F, 0.0F, -8.0F)
    );
    private static final Pose CANNON_CHARGE = pose(
            rotation(-4.0F, 12.0F, 0.0F), rotation(6.0F, -18.0F, 0.0F),
            rotation(58.0F, -15.0F, -12.0F), rotation(-32.0F, 10.0F, 14.0F),
            rotation(14.0F, 0.0F, 10.0F), rotation(-8.0F, 0.0F, -10.0F)
    );
    private static final Pose CANNON_RELEASE = pose(
            rotation(-5.0F, -8.0F, 0.0F), rotation(-6.0F, 18.0F, 0.0F),
            rotation(-90.0F, 8.0F, -6.0F), rotation(18.0F, 12.0F, 12.0F),
            rotation(-12.0F, 0.0F, 6.0F), rotation(10.0F, 0.0F, -6.0F)
    );
    private static final Pose CANNON_RECOIL = pose(
            rotation(-4.0F, -2.0F, 0.0F), rotation(5.0F, 10.0F, 0.0F),
            rotation(-70.0F, 5.0F, -4.0F), rotation(10.0F, 8.0F, 8.0F),
            rotation(-6.0F, 0.0F, 4.0F), rotation(6.0F, 0.0F, -4.0F)
    );

    public TarnakModel(ModelPart root) {
        super(root, false);
    }

    @Override
    public void setupAnim(
            TarnakEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        float actionTick = entity.getActionTick() + partialTick;
        Pose actionPose = switch (entity.getCombatAction()) {
            case TarnakEntity.ACTION_STEEL_GUARD -> steelGuardPose(scaleToAuthoredTimeline(
                    actionTick, 20.0F, TarnakEntity.LONG_COMBAT_CLIP_TICKS));
            case TarnakEntity.ACTION_MONARCH_BREAKER -> monarchBreakerPose(scaleToAuthoredTimeline(
                    actionTick, 22.0F, TarnakEntity.SHORT_COMBAT_CLIP_TICKS));
            case TarnakEntity.ACTION_MAXIMUM_OUTPUT -> maximumOutputPose(scaleToAuthoredTimeline(
                    actionTick, 24.0F, TarnakEntity.LONG_COMBAT_CLIP_TICKS));
            case TarnakEntity.ACTION_GUARD_COUNTER -> guardCounterPose(scaleToAuthoredTimeline(
                    actionTick, 22.0F, TarnakEntity.SHORT_COMBAT_CLIP_TICKS));
            case TarnakEntity.ACTION_IRON_CANNON -> ironCannonPose(scaleToAuthoredTimeline(
                    actionTick, 18.0F, TarnakEntity.SHORT_COMBAT_CLIP_TICKS));
            default -> null;
        };

        if (actionPose != null) {
            apply(actionPose);
        } else if (entity.isMaximumOutputActive()) {
            applyMaximumOutputLoop(ageInTicks);
        }

        syncOuterLayers();
    }

    private static float scaleToAuthoredTimeline(float actionTick, float authoredDuration, float clipDuration) {
        return Mth.clamp(actionTick * authoredDuration / clipDuration, 0.0F, authoredDuration);
    }

    private static Pose steelGuardPose(float tick) {
        if (tick <= 3.0F) {
            return catmull(GUARD_READY, GUARD_READY, GUARD_ANTICIPATION, GUARD_LOCK, tick / 3.0F);
        }
        if (tick <= 5.0F) {
            return catmull(GUARD_READY, GUARD_ANTICIPATION, GUARD_LOCK, GUARD_IMPACT, (tick - 3.0F) / 2.0F);
        }
        if (tick <= 10.0F) {
            return lerp(GUARD_LOCK, GUARD_IMPACT, (tick - 5.0F) / 5.0F);
        }
        return GUARD_LOCK;
    }

    private static Pose guardCounterPose(float tick) {
        if (tick <= 2.0F) {
            return lerp(GUARD_LOCK, GUARD_IMPACT, tick / 2.0F);
        }
        if (tick <= 10.0F) {
            return catmull(GUARD_LOCK, GUARD_IMPACT, GUARD_COUNTER, GUARD_COUNTER, (tick - 2.0F) / 8.0F);
        }
        if (tick <= 14.0F) {
            return GUARD_COUNTER;
        }
        return smoothLerp(GUARD_COUNTER, GUARD_READY, (tick - 14.0F) / 8.0F);
    }

    private static Pose monarchBreakerPose(float tick) {
        if (tick <= 5.0F) {
            return catmull(BREAKER_READY, BREAKER_READY, BREAKER_WINDUP, BREAKER_DASH, tick / 5.0F);
        }
        if (tick <= 8.0F) {
            return catmull(BREAKER_READY, BREAKER_WINDUP, BREAKER_DASH, BREAKER_IMPACT, (tick - 5.0F) / 3.0F);
        }
        if (tick <= 10.0F) {
            return lerp(BREAKER_DASH, BREAKER_IMPACT, (tick - 8.0F) / 2.0F);
        }
        if (tick <= 18.0F) {
            return catmull(BREAKER_DASH, BREAKER_IMPACT, BREAKER_RECOVERY, BREAKER_READY, (tick - 10.0F) / 8.0F);
        }
        return smoothLerp(BREAKER_RECOVERY, BREAKER_READY, (tick - 18.0F) / 4.0F);
    }

    private static Pose maximumOutputPose(float tick) {
        if (tick <= 6.0F) {
            return catmull(OUTPUT_READY, OUTPUT_READY, OUTPUT_CLENCH, OUTPUT_CHARGE, tick / 6.0F);
        }
        if (tick <= 12.0F) {
            return catmull(OUTPUT_READY, OUTPUT_CLENCH, OUTPUT_CHARGE, OUTPUT_BURST, (tick - 6.0F) / 6.0F);
        }
        if (tick <= 20.0F) {
            return catmull(OUTPUT_CLENCH, OUTPUT_CHARGE, OUTPUT_BURST, OUTPUT_BURST, (tick - 12.0F) / 8.0F);
        }
        return OUTPUT_BURST;
    }

    private static Pose ironCannonPose(float tick) {
        if (tick <= 3.0F) {
            return catmull(CANNON_READY, CANNON_READY, CANNON_ANCHOR, CANNON_CHARGE, tick / 3.0F);
        }
        if (tick <= 7.0F) {
            return catmull(CANNON_READY, CANNON_ANCHOR, CANNON_CHARGE, CANNON_RELEASE, (tick - 3.0F) / 4.0F);
        }
        if (tick <= 9.0F) {
            return lerp(CANNON_CHARGE, CANNON_RELEASE, (tick - 7.0F) / 2.0F);
        }
        if (tick <= 11.0F) {
            return lerp(CANNON_RELEASE, CANNON_RECOIL, (tick - 9.0F) / 2.0F);
        }
        return catmull(CANNON_RELEASE, CANNON_RECOIL, CANNON_READY, CANNON_READY, (tick - 11.0F) / 7.0F);
    }

    private void applyMaximumOutputLoop(float ageInTicks) {
        float pulse = Mth.sin(ageInTicks * 0.23F) * degrees(1.5F);
        body.xRot += degrees(-4.0F);
        head.xRot += degrees(-6.0F);
        rightArm.xRot += degrees(-5.0F) - pulse;
        rightArm.zRot += degrees(6.0F);
        leftArm.xRot += degrees(-5.0F) + pulse;
        leftArm.zRot += degrees(-6.0F);
    }

    private void apply(Pose pose) {
        apply(head, pose.head());
        apply(body, pose.body());
        apply(rightArm, pose.rightArm());
        apply(leftArm, pose.leftArm());
        apply(rightLeg, pose.rightLeg());
        apply(leftLeg, pose.leftLeg());
    }

    private static void apply(ModelPart part, Rotation rotation) {
        part.xRot = degrees(rotation.x());
        part.yRot = degrees(rotation.y());
        part.zRot = degrees(rotation.z());
    }

    private void syncOuterLayers() {
        hat.copyFrom(head);
        jacket.copyFrom(body);
        rightSleeve.copyFrom(rightArm);
        leftSleeve.copyFrom(leftArm);
        rightPants.copyFrom(rightLeg);
        leftPants.copyFrom(leftLeg);
    }

    private static Pose smoothLerp(Pose from, Pose to, float amount) {
        float clamped = Mth.clamp(amount, 0.0F, 1.0F);
        float smooth = clamped * clamped * (3.0F - 2.0F * clamped);
        return lerp(from, to, smooth);
    }

    private static Pose lerp(Pose from, Pose to, float amount) {
        float value = Mth.clamp(amount, 0.0F, 1.0F);
        return new Pose(
                lerp(from.head(), to.head(), value),
                lerp(from.body(), to.body(), value),
                lerp(from.rightArm(), to.rightArm(), value),
                lerp(from.leftArm(), to.leftArm(), value),
                lerp(from.rightLeg(), to.rightLeg(), value),
                lerp(from.leftLeg(), to.leftLeg(), value)
        );
    }

    private static Rotation lerp(Rotation from, Rotation to, float amount) {
        return rotation(
                Mth.lerp(amount, from.x(), to.x()),
                Mth.lerp(amount, from.y(), to.y()),
                Mth.lerp(amount, from.z(), to.z())
        );
    }

    private static Pose catmull(Pose p0, Pose p1, Pose p2, Pose p3, float amount) {
        float value = Mth.clamp(amount, 0.0F, 1.0F);
        return new Pose(
                catmull(p0.head(), p1.head(), p2.head(), p3.head(), value),
                catmull(p0.body(), p1.body(), p2.body(), p3.body(), value),
                catmull(p0.rightArm(), p1.rightArm(), p2.rightArm(), p3.rightArm(), value),
                catmull(p0.leftArm(), p1.leftArm(), p2.leftArm(), p3.leftArm(), value),
                catmull(p0.rightLeg(), p1.rightLeg(), p2.rightLeg(), p3.rightLeg(), value),
                catmull(p0.leftLeg(), p1.leftLeg(), p2.leftLeg(), p3.leftLeg(), value)
        );
    }

    private static Rotation catmull(Rotation p0, Rotation p1, Rotation p2, Rotation p3, float amount) {
        return rotation(
                Mth.catmullrom(amount, p0.x(), p1.x(), p2.x(), p3.x()),
                Mth.catmullrom(amount, p0.y(), p1.y(), p2.y(), p3.y()),
                Mth.catmullrom(amount, p0.z(), p1.z(), p2.z(), p3.z())
        );
    }

    private static float degrees(float value) {
        return value * Mth.DEG_TO_RAD;
    }

    private static Rotation rotation(float x, float y, float z) {
        return new Rotation(x, y, z);
    }

    private static Pose pose(
            Rotation head,
            Rotation body,
            Rotation rightArm,
            Rotation leftArm,
            Rotation rightLeg,
            Rotation leftLeg
    ) {
        return new Pose(head, body, rightArm, leftArm, rightLeg, leftLeg);
    }

    private record Rotation(float x, float y, float z) {
    }

    private record Pose(
            Rotation head,
            Rotation body,
            Rotation rightArm,
            Rotation leftArm,
            Rotation rightLeg,
            Rotation leftLeg
    ) {
    }
}
