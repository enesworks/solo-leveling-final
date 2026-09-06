package dev.eness.sololeveling3.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.Optional;
import java.util.UUID;

/**
 * Tarnak deliberately uses a fixed vanilla-wide humanoid rig. Normal movement,
 * looking, hurt and death therefore remain Minecraft-native, while the synced
 * action state drives only the authored combat poses.
 */
public final class TarnakEntity extends AbstractMonarchBoss {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_STEEL_GUARD = 1;
    public static final int ACTION_MONARCH_BREAKER = 2;
    public static final int ACTION_MAXIMUM_OUTPUT = 3;
    public static final int ACTION_GUARD_COUNTER = 4;
    public static final int ACTION_IRON_CANNON = 5;

    public static final int SHORT_COMBAT_CLIP_TICKS = 40;
    public static final int LONG_COMBAT_CLIP_TICKS = 80;
    private static final int STEEL_GUARD_DURATION = LONG_COMBAT_CLIP_TICKS;
    private static final int MONARCH_BREAKER_DURATION = SHORT_COMBAT_CLIP_TICKS;
    private static final int MAXIMUM_OUTPUT_CAST_DURATION = LONG_COMBAT_CLIP_TICKS;
    private static final int GUARD_COUNTER_DURATION = SHORT_COMBAT_CLIP_TICKS;
    private static final int IRON_CANNON_DURATION = SHORT_COMBAT_CLIP_TICKS;
    private static final int IRON_CANNON_COOLDOWN = 200;
    private static final int IRON_CANNON_MAX_FLIGHT_TICKS = 6;
    private static final float IRON_CANNON_DIRECT_DAMAGE = 25.0F;
    private static final float IRON_CANNON_SPLASH_DAMAGE = 13.0F;
    private static final double IRON_CANNON_SPEED = 2.0D;
    private static final double IRON_CANNON_SPLASH_RADIUS = 2.5D;
    private static final float STEEL_GUARD_DAMAGE_MULTIPLIER = 0.10F;
    private static final float MAXIMUM_OUTPUT_CAST_DAMAGE_MULTIPLIER = 0.01F;

    private static final UUID OUTPUT_ATTACK_MODIFIER = UUID.fromString("3ae106bc-f9fb-4cf7-b010-8c5af41f77d1");
    private static final UUID OUTPUT_SPEED_MODIFIER = UUID.fromString("0b922237-ecbf-4b70-b319-d9500c154d3b");
    private static final UUID OUTPUT_ARMOR_MODIFIER = UUID.fromString("84c92819-b36d-49f1-91a4-b9572fc7dad5");
    private static final UUID OUTPUT_TOUGHNESS_MODIFIER = UUID.fromString("2d0fa87f-31f2-4d27-b58a-9e7ee873ee02");

    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(TarnakEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_TICK =
            SynchedEntityData.defineId(TarnakEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAXIMUM_OUTPUT_TICKS =
            SynchedEntityData.defineId(TarnakEntity.class, EntityDataSerializers.INT);

    private int skillCooldown = 45;
    private int ironCannonCooldown = 100;
    private int nextGuardThresholdPercent = 90;
    private boolean maximumOutputUsed;
    private int empoweredHitCounter;
    private UUID lockedTargetId;
    private UUID pendingGuardAttackerId;
    private boolean impactDelivered;
    private boolean cannonProjectileActive;
    private UUID cannonProjectileTargetId;
    private Vec3 cannonProjectilePosition = Vec3.ZERO;
    private int cannonProjectileTicks;

    public TarnakEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, Component.translatable("entity.sololeveling3.tarnak"), BossEvent.BossBarColor.RED);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.MAX_HEALTH, 480.0D)
                .add(Attributes.ARMOR, 28.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 7.0D)
                .add(Attributes.ATTACK_DAMAGE, 24.0D)
                .add(Attributes.FOLLOW_RANGE, 56.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.82D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACTION, ACTION_NONE);
        entityData.define(ACTION_TICK, 0);
        entityData.define(MAXIMUM_OUTPUT_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.28D, false));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.82D));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public int getCombatAction() {
        return entityData.get(ACTION);
    }

    public int getActionTick() {
        return entityData.get(ACTION_TICK);
    }

    public int getMaximumOutputTicks() {
        return entityData.get(MAXIMUM_OUTPUT_TICKS);
    }

    public boolean isMaximumOutputActive() {
        return getMaximumOutputTicks() > 0;
    }

    public UUID getLockedTargetId() {
        return lockedTargetId;
    }

    public int getNextGuardThresholdPercent() {
        return nextGuardThresholdPercent;
    }

    public boolean hasPendingSteelGuard() {
        return pendingGuardAttackerId != null;
    }

    /** Used by the owner's deterministic in-game acceptance commands. */
    public boolean forceAbility(int action, LivingEntity target) {
        if (action != ACTION_STEEL_GUARD
                && action != ACTION_MONARCH_BREAKER
                && action != ACTION_MAXIMUM_OUTPUT
                && action != ACTION_IRON_CANNON) {
            return false;
        }
        if ((action == ACTION_MONARCH_BREAKER || action == ACTION_IRON_CANNON) && !isUsableTarget(target)) {
            return false;
        }
        beginAction(action, target);
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        tickMaximumOutput(serverLevel);
        tickIronCannonProjectile(serverLevel);
        if (ironCannonCooldown > 0) {
            ironCannonCooldown--;
        }

        if (getCombatAction() != ACTION_NONE) {
            navigation.stop();
            Vec3 movement = getDeltaMovement();
            setDeltaMovement(0.0D, movement.y, 0.0D);

            int nextTick = getActionTick() + 1;
            entityData.set(ACTION_TICK, nextTick);
            runAction(serverLevel, nextTick);
            if (nextTick >= actionDuration(getCombatAction())) {
                finishAction();
            }
            return;
        }

        LivingEntity target = getTarget();
        if (!maximumOutputUsed && getHealth() < getMaxHealth() * 0.50F) {
            beginAction(ACTION_MAXIMUM_OUTPUT, target);
            return;
        }

        LivingEntity guardAttacker = entityByUuid(serverLevel, pendingGuardAttackerId);
        if (pendingGuardAttackerId != null && !isUsableTarget(guardAttacker)) {
            guardAttacker = target;
        }
        if (pendingGuardAttackerId != null && isUsableTarget(guardAttacker)) {
            pendingGuardAttackerId = null;
            beginAction(ACTION_STEEL_GUARD, guardAttacker);
            return;
        }

        if (skillCooldown > 0) {
            skillCooldown--;
        }

        if (skillCooldown <= 0 && isUsableTarget(target) && distanceToSqr(target) <= 784.0D) {
            double distance = distanceToSqr(target);
            boolean cannonRange = distance >= 25.0D && distance <= 225.0D;
            beginAction(ironCannonCooldown <= 0 && cannonRange
                    ? ACTION_IRON_CANNON
                    : ACTION_MONARCH_BREAKER, target);
        }
    }

    private void runAction(ServerLevel level, int tick) {
        switch (getCombatAction()) {
            case ACTION_STEEL_GUARD -> runSteelGuard(level, tick);
            case ACTION_MONARCH_BREAKER -> runMonarchBreaker(level, tick);
            case ACTION_MAXIMUM_OUTPUT -> runMaximumOutputCast(level, tick);
            case ACTION_GUARD_COUNTER -> runGuardCounter(level, tick);
            case ACTION_IRON_CANNON -> runIronCannon(level, tick);
            default -> {
            }
        }
    }

    private void runSteelGuard(ServerLevel level, int tick) {
        if (crossedAuthoredTick(tick, 20, STEEL_GUARD_DURATION, 3)) {
            level.playSound(null, blockPosition(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 0.9F, 0.62F);
            TarnakVfx.steelGuardLock(level, this);
        }
        int authoredTick = authoredTick(tick, 20, STEEL_GUARD_DURATION);
        if (authoredTick >= 5 && authoredTick(tick - 1, 20, STEEL_GUARD_DURATION) != authoredTick) {
            TarnakVfx.steelGuardShell(level, this);
        }
    }

    private void runMonarchBreaker(ServerLevel level, int tick) {
        LivingEntity target = lockedTarget(level);
        if (isUsableTarget(target)) {
            faceTargetImmediately(target);
        }

        int authoredTick = authoredTick(tick, 22, MONARCH_BREAKER_DURATION);
        if (authoredTick >= 3 && authoredTick <= 7
                && authoredTick(tick - 1, 22, MONARCH_BREAKER_DURATION) != authoredTick) {
            TarnakVfx.breakerWindup(level, this, target);
        }

        if (crossedAuthoredTick(tick, 22, MONARCH_BREAKER_DURATION, 8) && isUsableTarget(target)) {
            dashToward(level, target);
        }

        if (crossedAuthoredTick(tick, 22, MONARCH_BREAKER_DURATION, 10)
                && !impactDelivered && isUsableTarget(target)) {
            impactDelivered = true;
            faceTargetImmediately(target);
            float damage = (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.65F;
            strikeLockedTarget(target, damage, 1.85D, 0.32D);
            damageBreakerCone(level, target, damage * 0.72F);
            TarnakVfx.monarchBreakerImpact(level, this, target);
        }
    }

    private void runMaximumOutputCast(ServerLevel level, int tick) {
        if (tick == 1) {
            level.playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.9F, 0.55F);
        }
        int authoredTick = authoredTick(tick, 24, MAXIMUM_OUTPUT_CAST_DURATION);
        if (authoredTick >= 3 && authoredTick < 20
                && authoredTick(tick - 1, 24, MAXIMUM_OUTPUT_CAST_DURATION) != authoredTick) {
            TarnakVfx.maximumOutputCharge(level, this, authoredTick);
        }
        if (crossedAuthoredTick(tick, 24, MAXIMUM_OUTPUT_CAST_DURATION, 20)) {
            TarnakVfx.maximumOutputBurst(level, this);
        }
        if (tick == MAXIMUM_OUTPUT_CAST_DURATION) {
            activateMaximumOutput(level);
        }
    }

    private void runIronCannon(ServerLevel level, int tick) {
        LivingEntity target = lockedTarget(level);
        if (isUsableTarget(target)) {
            faceTargetImmediately(target);
        }

        int authoredTick = authoredTick(tick, 18, IRON_CANNON_DURATION);
        if (authoredTick >= 3 && authoredTick <= 8
                && authoredTick(tick - 1, 18, IRON_CANNON_DURATION) != authoredTick) {
            TarnakVfx.ironCannonCharge(level, this, target, authoredTick);
        }
        if (crossedAuthoredTick(tick, 18, IRON_CANNON_DURATION, 7)) {
            TarnakVfx.ironCannonTargetLock(level, this, target);
        }
        if (crossedAuthoredTick(tick, 18, IRON_CANNON_DURATION, 9)
                && !impactDelivered && isUsableTarget(target)) {
            impactDelivered = true;
            launchIronCannon(level, target);
        }
    }

    private void runGuardCounter(ServerLevel level, int tick) {
        LivingEntity target = lockedTarget(level);
        if (isUsableTarget(target)) {
            faceTargetImmediately(target);
        }
        if (crossedAuthoredTick(tick, 22, GUARD_COUNTER_DURATION, 2)) {
            TarnakVfx.steelGuardImpact(level, this, target);
        }
        if (crossedAuthoredTick(tick, 22, GUARD_COUNTER_DURATION, 10)
                && !impactDelivered && isUsableTarget(target)) {
            impactDelivered = true;
            float damage = (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25F;
            strikeLockedTarget(target, damage, 1.2D, 0.22D);
            TarnakVfx.guardCounterImpact(level, this, target);
        }
    }

    private void tickMaximumOutput(ServerLevel level) {
        if (!isMaximumOutputActive()) {
            return;
        }
        TarnakVfx.maximumOutputAura(level, this, tickCount);
    }

    private void beginAction(int action, LivingEntity target) {
        navigation.stop();
        entityData.set(ACTION, action);
        entityData.set(ACTION_TICK, 0);
        lockedTargetId = isUsableTarget(target) ? target.getUUID() : null;
        impactDelivered = false;
        skillCooldown = switch (action) {
            case ACTION_STEEL_GUARD -> Mth.nextInt(random, 65, 95);
            case ACTION_MONARCH_BREAKER -> Mth.nextInt(random, 48, 72);
            case ACTION_MAXIMUM_OUTPUT -> 80;
            case ACTION_IRON_CANNON -> 52;
            default -> 55;
        };
        if (action == ACTION_IRON_CANNON) {
            ironCannonCooldown = IRON_CANNON_COOLDOWN;
        }
        if (isUsableTarget(target)) {
            faceTargetImmediately(target);
        }
    }

    private void beginGuardCounter(LivingEntity attacker) {
        entityData.set(ACTION, ACTION_GUARD_COUNTER);
        entityData.set(ACTION_TICK, 0);
        lockedTargetId = attacker.getUUID();
        impactDelivered = false;
        navigation.stop();
        faceTargetImmediately(attacker);
    }

    private void finishAction() {
        entityData.set(ACTION, ACTION_NONE);
        entityData.set(ACTION_TICK, 0);
        lockedTargetId = null;
        impactDelivered = false;
    }

    private int actionDuration(int action) {
        return switch (action) {
            case ACTION_STEEL_GUARD -> STEEL_GUARD_DURATION;
            case ACTION_MONARCH_BREAKER -> MONARCH_BREAKER_DURATION;
            case ACTION_MAXIMUM_OUTPUT -> MAXIMUM_OUTPUT_CAST_DURATION;
            case ACTION_GUARD_COUNTER -> GUARD_COUNTER_DURATION;
            case ACTION_IRON_CANNON -> IRON_CANNON_DURATION;
            default -> 0;
        };
    }

    private static int authoredTick(int actionTick, int authoredDuration, int clipDuration) {
        if (actionTick <= 0) {
            return 0;
        }
        return Mth.clamp((int)Math.floor(actionTick * authoredDuration / (double)clipDuration), 0, authoredDuration);
    }

    private static boolean crossedAuthoredTick(int actionTick, int authoredDuration, int clipDuration, int marker) {
        return authoredTick(actionTick, authoredDuration, clipDuration) >= marker
                && authoredTick(actionTick - 1, authoredDuration, clipDuration) < marker;
    }

    private LivingEntity lockedTarget(ServerLevel level) {
        return entityByUuid(level, lockedTargetId);
    }

    private static LivingEntity entityByUuid(ServerLevel level, UUID uuid) {
        return uuid != null && level.getEntity(uuid) instanceof LivingEntity living ? living : null;
    }

    private void activateMaximumOutput(ServerLevel level) {
        maximumOutputUsed = true;
        entityData.set(MAXIMUM_OUTPUT_TICKS, 1);
        empoweredHitCounter = 0;
        addPermanentMultiplier(Attributes.ATTACK_DAMAGE, OUTPUT_ATTACK_MODIFIER, "Tarnak Maximum Output attack");
        addPermanentMultiplier(Attributes.MOVEMENT_SPEED, OUTPUT_SPEED_MODIFIER, "Tarnak Maximum Output speed");
        addPermanentMultiplier(Attributes.ARMOR, OUTPUT_ARMOR_MODIFIER, "Tarnak Maximum Output armor");
        addPermanentMultiplier(Attributes.ARMOR_TOUGHNESS, OUTPUT_TOUGHNESS_MODIFIER, "Tarnak Maximum Output toughness");
        TarnakVfx.maximumOutputAwakened(level, this);
    }

    private void addPermanentMultiplier(Attribute attribute, UUID id, String name) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null && instance.getModifier(id) == null) {
            instance.addPermanentModifier(new AttributeModifier(
                    id,
                    name,
                    0.20D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private void faceTargetImmediately(LivingEntity target) {
        double x = target.getX() - getX();
        double z = target.getZ() - getZ();
        if (x * x + z * z < 1.0E-6D) {
            return;
        }
        float yaw = (float)(Mth.atan2(z, x) * Mth.RAD_TO_DEG) - 90.0F;
        setYRot(yaw);
        yRotO = yaw;
        setYBodyRot(yaw);
        yBodyRotO = yaw;
        setYHeadRot(yaw);
        yHeadRotO = yaw;
        getLookControl().setLookAt(target, 360.0F, 360.0F);
    }

    private void dashToward(ServerLevel level, LivingEntity target) {
        Vec3 start = position();
        Vec3 horizontal = target.position().subtract(start).multiply(1.0D, 0.0D, 1.0D);
        double length = horizontal.length();
        if (length > 0.01D) {
            double travel = Math.min(3.5D, Math.max(0.0D, length - 1.35D));
            if (travel > 0.0D) {
                move(MoverType.SELF, horizontal.scale(travel / length));
                hasImpulse = true;
            }
        }
        TarnakVfx.monarchBreakerDash(level, this, start, position());
    }

    private void launchIronCannon(ServerLevel level, LivingEntity target) {
        Vec3 forward = horizontalDirection(position(), target.position());
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        cannonProjectilePosition = position()
                .add(0.0D, 1.18D, 0.0D)
                .add(forward.scale(0.72D))
                .add(right.scale(-0.34D));
        cannonProjectileTargetId = target.getUUID();
        cannonProjectileTicks = 0;
        cannonProjectileActive = true;
        TarnakVfx.ironCannonFire(level, this, cannonProjectilePosition, forward);
    }

    private void tickIronCannonProjectile(ServerLevel level) {
        if (!cannonProjectileActive) {
            return;
        }

        LivingEntity target = entityByUuid(level, cannonProjectileTargetId);
        if (!isUsableTarget(target)) {
            finishIronCannonProjectile();
            return;
        }

        Vec3 start = cannonProjectilePosition;
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 offset = targetCenter.subtract(start);
        double distance = offset.length();
        if (distance < 1.0E-4D) {
            ironCannonHit(level, target, targetCenter);
            return;
        }

        cannonProjectileTicks++;
        boolean finalFlightTick = cannonProjectileTicks >= IRON_CANNON_MAX_FLIGHT_TICKS;
        Vec3 end = finalFlightTick
                ? targetCenter
                : start.add(offset.scale(Math.min(IRON_CANNON_SPEED, distance) / distance));

        BlockHitResult blockHit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));
        Optional<Vec3> targetIntersection = target.getBoundingBox().inflate(0.42D).clip(start, end);
        boolean blockedFirst = blockHit.getType() == HitResult.Type.BLOCK
                && (targetIntersection.isEmpty()
                || start.distanceToSqr(blockHit.getLocation()) < start.distanceToSqr(targetIntersection.get()));

        Vec3 visibleEnd = blockedFirst ? blockHit.getLocation() : end;
        TarnakVfx.ironCannonTrail(level, start, visibleEnd, cannonProjectileTicks);
        cannonProjectilePosition = visibleEnd;

        if (blockedFirst) {
            ironCannonSplash(level, visibleEnd, null);
            TarnakVfx.ironCannonImpact(level, this, visibleEnd, blockHit.getBlockPos());
            finishIronCannonProjectile();
            return;
        }

        if (targetIntersection.isPresent() || finalFlightTick || visibleEnd.distanceToSqr(targetCenter) <= 0.36D) {
            ironCannonHit(level, target, targetCenter);
        }
    }

    private void ironCannonHit(ServerLevel level, LivingEntity target, Vec3 impact) {
        target.invulnerableTime = 0;
        if (target.hurt(damageSources().mobAttack(this), IRON_CANNON_DIRECT_DAMAGE)) {
            Vec3 away = horizontalDirection(position(), target.position()).scale(1.35D);
            target.push(away.x, 0.24D, away.z);
            target.hurtMarked = true;
        }
        ironCannonSplash(level, impact, target);
        TarnakVfx.ironCannonImpact(level, this, impact, target.blockPosition().below());
        finishIronCannonProjectile();
    }

    private void ironCannonSplash(ServerLevel level, Vec3 center, LivingEntity directTarget) {
        AABB area = AABB.ofSize(
                center,
                IRON_CANNON_SPLASH_RADIUS * 2.0D,
                IRON_CANNON_SPLASH_RADIUS * 2.0D,
                IRON_CANNON_SPLASH_RADIUS * 2.0D
        );
        for (LivingEntity candidate : level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity != this && entity != directTarget && isUsableTarget(entity)
        )) {
            if (candidate.distanceToSqr(center) > IRON_CANNON_SPLASH_RADIUS * IRON_CANNON_SPLASH_RADIUS) {
                continue;
            }
            candidate.invulnerableTime = 0;
            if (candidate.hurt(damageSources().mobAttack(this), IRON_CANNON_SPLASH_DAMAGE)) {
                Vec3 away = horizontalDirection(center, candidate.position()).scale(0.8D);
                candidate.push(away.x, 0.16D, away.z);
                candidate.hurtMarked = true;
            }
        }
    }

    private void finishIronCannonProjectile() {
        cannonProjectileActive = false;
        cannonProjectileTargetId = null;
        cannonProjectilePosition = Vec3.ZERO;
        cannonProjectileTicks = 0;
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : direction.normalize();
    }

    private void strikeLockedTarget(LivingEntity target, float damage, double knockback, double lift) {
        if (!isUsableTarget(target)) {
            return;
        }
        target.invulnerableTime = 0;
        if (target.hurt(damageSources().mobAttack(this), damage)) {
            Vec3 away = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() < 1.0E-4D) {
                away = getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            }
            away = away.normalize().scale(knockback);
            target.push(away.x, lift, away.z);
            target.hurtMarked = true;
        }
    }

    private void damageBreakerCone(ServerLevel level, LivingEntity lockedTarget, float damage) {
        Vec3 direction = lockedTarget.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        direction = direction.normalize();
        Vec3 origin = position();

        for (LivingEntity candidate : level.getEntitiesOfClass(
                LivingEntity.class,
                getBoundingBox().inflate(9.0D, 3.0D, 9.0D),
                entity -> entity != this && entity != lockedTarget && isUsableTarget(entity)
        )) {
            Vec3 offset = candidate.position().subtract(origin);
            double vertical = Math.abs(offset.y);
            Vec3 flat = offset.multiply(1.0D, 0.0D, 1.0D);
            double forward = flat.dot(direction);
            if (forward < 0.0D || forward > 8.0D || vertical > 3.0D) {
                continue;
            }
            double sideways = flat.subtract(direction.scale(forward)).length();
            double allowedWidth = 0.85D + forward * 0.38D;
            if (sideways <= allowedWidth) {
                candidate.invulnerableTime = 0;
                candidate.hurt(damageSources().mobAttack(this), damage);
                Vec3 push = direction.scale(1.15D);
                candidate.push(push.x, 0.24D, push.z);
                candidate.hurtMarked = true;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isFriendlyDamage(source)) return false;
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return super.hurt(source, amount);
        }

        if (getCombatAction() == ACTION_MAXIMUM_OUTPUT) {
            return applyTrackedDamage(source, amount * MAXIMUM_OUTPUT_CAST_DAMAGE_MULTIPLIER, false);
        }

        boolean guarding = getCombatAction() == ACTION_STEEL_GUARD
                && authoredTick(getActionTick(), 20, STEEL_GUARD_DURATION) >= 5;
        if (!guarding) {
            return applyTrackedDamage(source, amount, true);
        }

        float reduced = amount * STEEL_GUARD_DAMAGE_MULTIPLIER;
        Entity direct = source.getDirectEntity();
        if (!level().isClientSide && direct instanceof LivingEntity attacker && isUsableTarget(attacker)) {
            beginGuardCounter(attacker);
        }
        if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
            TarnakVfx.steelGuardImpact(serverLevel, this, direct instanceof LivingEntity living ? living : null);
        }
        return applyTrackedDamage(source, reduced, false);
    }

    private boolean applyTrackedDamage(DamageSource source, float amount, boolean queueGuard) {
        float before = getHealth();
        boolean accepted = super.hurt(source, amount);
        if (accepted && !level().isClientSide) {
            advanceGuardThresholds(before, getHealth(), source, queueGuard);
        }
        return accepted;
    }

    private void advanceGuardThresholds(float before, float after, DamageSource source, boolean queueGuard) {
        boolean crossed = false;
        while (nextGuardThresholdPercent >= 10
                && after <= getMaxHealth() * nextGuardThresholdPercent / 100.0F) {
            if (before > getMaxHealth() * nextGuardThresholdPercent / 100.0F) {
                crossed = true;
            }
            nextGuardThresholdPercent -= 10;
        }

        if (!crossed || !queueGuard || !isAlive()) {
            return;
        }

        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity)) {
            attacker = source.getDirectEntity();
        }
        if (attacker instanceof LivingEntity living && isUsableTarget(living)) {
            pendingGuardAttackerId = living.getUUID();
        } else if (isUsableTarget(getTarget())) {
            pendingGuardAttackerId = getTarget().getUUID();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (getCombatAction() != ACTION_NONE) {
            return false;
        }

        boolean hit = super.doHurtTarget(target);
        if (hit && isMaximumOutputActive() && level() instanceof ServerLevel serverLevel) {
            empoweredHitCounter++;
            if (empoweredHitCounter >= 3) {
                empoweredHitCounter = 0;
                TarnakVfx.maximumOutputThirdHit(serverLevel, this, target.position());
                BossCombat.damageArea(
                        this,
                        target.position(),
                        3.5D,
                        (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.42F,
                        0.85D,
                        0.18D
                );
            }
        }
        return hit;
    }

    @Override
    protected boolean isImmobile() {
        return getCombatAction() != ACTION_NONE || super.isImmobile();
    }

    private boolean isUsableTarget(LivingEntity target) {
        if (isFriendlyMonarch(target)) return false;
        if (target == null || !target.isAlive()) {
            return false;
        }
        return !(target instanceof Player player) || (!player.isCreative() && !player.isSpectator());
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkillCooldown", skillCooldown);
        tag.putInt("IronCannonCooldown", ironCannonCooldown);
        tag.putInt("NextGuardThresholdPercent", nextGuardThresholdPercent);
        tag.putBoolean("MaximumOutputUsed", maximumOutputUsed);
        tag.putInt("MaximumOutputTicks", getMaximumOutputTicks());
        tag.putInt("EmpoweredHitCounter", empoweredHitCounter);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        skillCooldown = Math.max(0, tag.getInt("SkillCooldown"));
        ironCannonCooldown = tag.contains("IronCannonCooldown")
                ? Math.max(0, tag.getInt("IronCannonCooldown"))
                : 100;
        if (tag.contains("NextGuardThresholdPercent")) {
            nextGuardThresholdPercent = Mth.clamp(tag.getInt("NextGuardThresholdPercent"), 0, 90);
        } else {
            nextGuardThresholdPercent = 90;
            while (nextGuardThresholdPercent >= 10
                    && getHealth() <= getMaxHealth() * nextGuardThresholdPercent / 100.0F) {
                nextGuardThresholdPercent -= 10;
            }
        }
        maximumOutputUsed = tag.getBoolean("MaximumOutputUsed") || tag.getInt("MaximumOutputTicks") > 0;
        entityData.set(MAXIMUM_OUTPUT_TICKS, maximumOutputUsed ? 1 : 0);
        if (maximumOutputUsed) {
            addPermanentMultiplier(Attributes.ATTACK_DAMAGE, OUTPUT_ATTACK_MODIFIER, "Tarnak Maximum Output attack");
            addPermanentMultiplier(Attributes.MOVEMENT_SPEED, OUTPUT_SPEED_MODIFIER, "Tarnak Maximum Output speed");
            addPermanentMultiplier(Attributes.ARMOR, OUTPUT_ARMOR_MODIFIER, "Tarnak Maximum Output armor");
            addPermanentMultiplier(Attributes.ARMOR_TOUGHNESS, OUTPUT_TOUGHNESS_MODIFIER, "Tarnak Maximum Output toughness");
        }
        empoweredHitCounter = Mth.clamp(tag.getInt("EmpoweredHitCounter"), 0, 2);
        pendingGuardAttackerId = null;
        finishIronCannonProjectile();
        finishAction();
    }

    /** The custom vanilla renderer owns Tarnak's animation; Gecko has no controller work here. */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }
}
