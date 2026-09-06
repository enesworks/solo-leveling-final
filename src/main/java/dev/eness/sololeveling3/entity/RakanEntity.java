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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.UUID;

public final class RakanEntity extends AbstractMonarchBoss {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_ATTACK_COMBO = 1;
    public static final int ACTION_GUARD = 2;
    public static final int ACTION_GUARD_COUNTER = 3;
    public static final int ACTION_POUNCE = 4;
    public static final int ACTION_ROAR = 5;

    public static final int ATTACK_COMBO_DURATION = 40;
    public static final int GUARD_DURATION = 80;
    public static final int GUARD_START_TICKS = 6;
    public static final int GUARD_HIT_TICKS = 4;
    public static final int GUARD_COUNTER_DURATION = 8;
    public static final int POUNCE_DURATION = 38;
    public static final int ROAR_DURATION = 80;

    private static final int PERFECT_GUARD_TICKS = 5;
    private static final int GUARD_COUNTER_HIT_TICK = 5;
    private static final int ATTACK_COMBO_COOLDOWN = 44;
    private static final int POUNCE_COOLDOWN = 180;
    private static final int POUNCE_LAUNCH_TICK = 13;
    private static final int POUNCE_IMPACT_TICK = 27;
    // Keep the accepted poses; compress only the 46-tick flight to 14 ticks.
    private static final int POUNCE_CLIP_IMPACT_TICK = 59;
    private static final int POUNCE_CLIP_DURATION = 70;
    private static final int ROAR_SOUND_TICK = 47;
    private static final int ROAR_RELEASE_TICK = 49;
    private static final int ROAR_BUFF_TICK = 80;
    private static final double POUNCE_MAX_DISTANCE = 12.0D;
    private static final double POUNCE_AREA_RADIUS = 4.0D;
    private static final double ROAR_RADIUS = 8.0D;
    private static final float GUARD_DAMAGE_MULTIPLIER = 0.01F;
    private static final float POUNCE_DAMAGE = 12.0F;

    private static final UUID ROAR_ATTACK_MODIFIER = UUID.fromString("5902babc-3a6d-4596-875b-c23f9ab9e8d4");
    private static final UUID ROAR_SPEED_MODIFIER = UUID.fromString("3cd661f2-47b1-44b3-86c3-271ea1d9dc7a");
    private static final UUID ROAR_ARMOR_MODIFIER = UUID.fromString("69595ed6-0da7-4d88-986e-c5b08e7c7fb3");
    private static final UUID ROAR_TOUGHNESS_MODIFIER = UUID.fromString("eb72e18f-25e5-4df9-a3f7-e647f05b05cb");

    private static final RawAnimation ATTACK_COMBO =
            RawAnimation.begin().thenPlay("animation.rakan.predator_claw_combo");
    private static final RawAnimation GUARD_START =
            RawAnimation.begin().thenPlay("animation.rakan.guard_start");
    private static final RawAnimation GUARD_HOLD =
            RawAnimation.begin().thenLoop("animation.rakan.guard_hold");
    private static final RawAnimation GUARD_HIT =
            RawAnimation.begin().thenPlay("animation.rakan.guard_hit");
    private static final RawAnimation GUARD_COUNTER =
            RawAnimation.begin().thenPlay("animation.rakan.guard_counter");
    private static final RawAnimation POUNCE =
            RawAnimation.begin().thenPlay("animation.rakan.monarch_pounce");
    private static final RawAnimation ROAR =
            RawAnimation.begin().thenPlay("animation.rakan.beast_monarch_roar");

    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(RakanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_TICK =
            SynchedEntityData.defineId(RakanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUARD_HIT_TICK =
            SynchedEntityData.defineId(RakanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUARD_COUNTER_TICK =
            SynchedEntityData.defineId(RakanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> POUNCE_LANDING_TICK =
            SynchedEntityData.defineId(RakanEntity.class, EntityDataSerializers.INT);

    private int attackComboCooldown = 24;
    private int pounceCooldown = 90;
    private int nextGuardThresholdPercent = 90;
    private UUID lockedTargetId;
    private UUID guardAttackerId;
    private int comboHitMask;
    private boolean guardBlocked;
    private boolean guardCounterHit;
    private boolean guardCounterQueued;
    private boolean pounceLaunched;
    private boolean pounceImpacted;
    private boolean roarReleased;
    private boolean roarUsed;
    private boolean roarEmpowered;
    private UUID pendingGuardTargetId;
    private int pendingGuards;
    private boolean commandPreview;
    private RawAnimation lastClientAnimation;
    private int lastClientActionTick;
    private Vec3 pounceStart = Vec3.ZERO;
    private Vec3 pounceTarget = Vec3.ZERO;
    private Vec3 pounceDirection = Vec3.ZERO;

    public RakanEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, Component.translatable("entity.sololeveling3.rakan"), BossEvent.BossBarColor.YELLOW);
        this.xpReward = 520;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.MAX_HEALTH, 520.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 23.0D)
                .add(Attributes.FOLLOW_RANGE, 56.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.86D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACTION, ACTION_NONE);
        entityData.define(ACTION_TICK, 0);
        entityData.define(GUARD_HIT_TICK, 0);
        entityData.define(GUARD_COUNTER_TICK, 0);
        entityData.define(POUNCE_LANDING_TICK, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
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

    public int getGuardHitTick() {
        return entityData.get(GUARD_HIT_TICK);
    }

    public int getGuardCounterTick() {
        return entityData.get(GUARD_COUNTER_TICK);
    }

    public int getAttackComboCooldown() {
        return attackComboCooldown;
    }

    public int getPounceCooldown() {
        return pounceCooldown;
    }

    public int getGuardCooldown() {
        return 0;
    }

    public int getNextGuardThresholdPercent() {
        return nextGuardThresholdPercent;
    }

    public boolean isRoarUsed() {
        return roarUsed;
    }

    public boolean isRoarEmpowered() {
        return roarEmpowered;
    }

    public UUID getLockedTargetId() {
        return lockedTargetId;
    }

    public boolean forceAbility(int action, LivingEntity target) {
        if (getCombatAction() != ACTION_NONE) {
            return false;
        }
        if (!isSupportedAction(action)) {
            return false;
        }
        if (action == ACTION_ROAR && (roarUsed || getHealth() >= getMaxHealth() * 0.5F)) {
            return false;
        }
        if (requiresTarget(action) && !isUsableTarget(target)) {
            return false;
        }
        beginAction(action, target);
        return true;
    }

    /** Operator command preview: allow creative targets and repeat the roar.
     * Natural AI and forceAbility retain the actual combat restrictions. */
    public boolean previewAbility(int action, LivingEntity target) {
        if (getCombatAction() != ACTION_NONE || !isSupportedAction(action)) return false;
        commandPreview = true;
        if (requiresTarget(action) && !isUsableTarget(target)) {
            commandPreview = false;
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

        decrementCooldowns();
        tickGuardVisuals();

        if (tryStartHealthRoar(getTarget())) {
            return;
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

        LivingEntity pendingGuardTarget = entityByUuid(serverLevel, pendingGuardTargetId);
        if (pendingGuardTargetId != null && !isUsableTarget(pendingGuardTarget)) {
            pendingGuardTarget = getTarget();
        }
        if (pendingGuards > 0) {
            pendingGuards--;
            pendingGuardTargetId = null;
            beginAction(ACTION_GUARD, pendingGuardTarget);
            return;
        }

        LivingEntity target = getTarget();
        if (!isUsableTarget(target) || !hasLineOfSight(target) || distanceToSqr(target) > 900.0D) {
            return;
        }

        double distance = distanceToSqr(target);
        if (pounceCooldown <= 0 && distance >= 25.0D && distance <= 256.0D) {
            beginAction(ACTION_POUNCE, target);
        } else if (attackComboCooldown <= 0 && distance <= 16.0D) {
            beginAction(ACTION_ATTACK_COMBO, target);
        }
    }

    private void decrementCooldowns() {
        attackComboCooldown = Math.max(0, attackComboCooldown - 1);
        pounceCooldown = Math.max(0, pounceCooldown - 1);
    }

    private void tickGuardVisuals() {
        int guardHitTick = getGuardHitTick();
        if (guardHitTick > 0) {
            entityData.set(GUARD_HIT_TICK, guardHitTick - 1);
            if (guardHitTick == 1 && guardCounterQueued) {
                guardCounterQueued = false;
                entityData.set(GUARD_COUNTER_TICK, 1);
                return;
            }
        }
        int counterTick = getGuardCounterTick();
        if (counterTick > 0) {
            int nextTick = counterTick + 1;
            entityData.set(GUARD_COUNTER_TICK, nextTick > GUARD_COUNTER_DURATION ? 0 : nextTick);
            if (nextTick > GUARD_COUNTER_DURATION) {
                guardCounterHit = false;
            }
        }
    }

    private void runAction(ServerLevel level, int tick) {
        switch (getCombatAction()) {
            case ACTION_ATTACK_COMBO -> runAttackCombo(level, tick);
            case ACTION_GUARD -> runGuard(level, tick);
            case ACTION_GUARD_COUNTER -> runStandaloneGuardCounter(level, tick);
            case ACTION_POUNCE -> runPounce(level, tick);
            case ACTION_ROAR -> runRoar(level, tick);
            default -> {
            }
        }
    }

    private void runAttackCombo(ServerLevel level, int tick) {
        LivingEntity target = lockedTarget(level);
        if (isUsableTarget(target)) {
            faceTargetImmediately(target);
        }
        attackHit(level, target, tick, 14, 0, 0.92F, 0.1D, 0.04D);
        attackHit(level, target, tick, 23, 1, 1.02F, 0.16D, 0.04D);
        attackHit(level, target, tick, 32, 2, 1.28F, 1.05D, 0.2D);
    }

    private void attackHit(ServerLevel level, LivingEntity target, int tick, int marker, int maskIndex,
                           float damageMultiplier, double knockback, double lift) {
        if (tick != marker || (comboHitMask & (1 << maskIndex)) != 0) {
            return;
        }
        comboHitMask |= 1 << maskIndex;
        RakanVfx.predatorClawHit(level, this, target, maskIndex + 1);
        if (isUsableTarget(target) && hasLineOfSight(target) && distanceToSqr(target) <= 20.25D) {
            strikeTarget(target, (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier, knockback, lift);
            if (maskIndex == 2) {
                comboFinalArea(level, target.position());
            }
        }
        if (maskIndex == 2) {
            RakanVfx.predatorClawFinish(level, this, target == null ? null : target.position());
        }
    }

    private void runGuard(ServerLevel level, int tick) {
        if (commandPreview && tick == 12 && !guardBlocked) {
            // Show start, hold, contact and counter in one four-second preview.
            LivingEntity target = lockedTarget(level);
            guardBlocked = true;
            guardAttackerId = target == null ? null : target.getUUID();
            entityData.set(GUARD_HIT_TICK, GUARD_HIT_TICKS);
            guardCounterQueued = true;
            RakanVfx.guardBlock(level, this, target);
        }
        runEmbeddedGuardCounter(level);
    }

    private void comboFinalArea(ServerLevel level, Vec3 center) {
        double radius = 3.0D;
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(center, radius * 2.0D, 3.0D, radius * 2.0D),
                entity -> entity != this && !isAlliedTo(entity) && isUsableTarget(entity) && entity.distanceToSqr(center) <= radius * radius
                        && hasLineOfSight(entity)
        )) {
            if (target == lockedTarget(level)) {
                continue;
            }
            int previousInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean hurt = target.hurt(damageSources().mobAttack(this), (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.45F);
            if (!hurt) {
                target.invulnerableTime = previousInvulnerableTime;
            } else {
                Vec3 away = horizontalDirection(center, target.position(), flatLook()).scale(0.65D);
                target.push(away.x, 0.12D, away.z);
                target.hurtMarked = true;
            }
        }
    }

    private void runEmbeddedGuardCounter(ServerLevel level) {
        int counterTick = getGuardCounterTick();
        if (counterTick <= 0 || guardCounterHit || counterTick != GUARD_COUNTER_HIT_TICK) {
            return;
        }
        LivingEntity attacker = entityByUuid(level, guardAttackerId);
        if (isUsableTarget(attacker)) {
            guardCounterHit = true;
            faceTargetImmediately(attacker);
            strikeTarget(attacker, (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.35F, 1.35D, 0.22D);
            RakanVfx.guardCounter(level, this, attacker);
        }
    }

    private void runStandaloneGuardCounter(ServerLevel level, int tick) {
        LivingEntity target = lockedTarget(level);
        if (isUsableTarget(target)) {
            faceTargetImmediately(target);
        }
        if (tick == GUARD_COUNTER_HIT_TICK && isUsableTarget(target)) {
            strikeTarget(target, (float)getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.35F, 1.35D, 0.22D);
            RakanVfx.guardCounter(level, this, target);
        }
    }

    private void runPounce(ServerLevel level, int tick) {
        LivingEntity target = lockedTarget(level);
        if (isUsableTarget(target) && tick <= POUNCE_LAUNCH_TICK) {
            faceTargetImmediately(target);
        }
        if (tick == POUNCE_LAUNCH_TICK && !pounceLaunched) {
            pounceLaunched = true;
            Vec3 targetPosition = isUsableTarget(target) ? target.position() : position().add(flatLook().scale(POUNCE_MAX_DISTANCE));
            Vec3 offset = targetPosition.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            double length = offset.length();
            pounceDirection = length < 0.01D ? flatLook() : offset.normalize();
            double travel = Math.min(POUNCE_MAX_DISTANCE, length);
            pounceStart = position();
            pounceTarget = position().add(pounceDirection.scale(travel));
            BlockHitResult floor = level.clip(new ClipContext(pounceTarget.add(0, 3, 0),
                    pounceTarget.add(0, -6, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (floor.getType() == HitResult.Type.BLOCK) {
                pounceTarget = new Vec3(pounceTarget.x, floor.getLocation().y, pounceTarget.z);
            }
            setDeltaMovement(Vec3.ZERO);
            RakanVfx.pounceLaunch(level, this);
        }
        if (pounceLaunched && !pounceImpacted && tick > POUNCE_LAUNCH_TICK && tick <= POUNCE_IMPACT_TICK) {
            pounceStep(level, tick);
        }
        if (tick > POUNCE_IMPACT_TICK && !pounceImpacted && onGround()) {
            pounceImpact(level, position());
        }
    }

    @Override
    public void travel(Vec3 input) {
        // The collision-aware arc is moved once by the server below. Vanilla
        // gravity/travel must not move the same flight a second time each tick.
        if (getCombatAction() == ACTION_POUNCE && getActionTick() >= POUNCE_LAUNCH_TICK
                && getActionTick() < POUNCE_IMPACT_TICK) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.travel(input);
    }

    private void pounceStep(ServerLevel level, int tick) {
        Vec3 start = position();
        float progress = Mth.clamp((tick - POUNCE_LAUNCH_TICK) / (float)(POUNCE_IMPACT_TICK - POUNCE_LAUNCH_TICK), 0.0F, 1.0F);
        Vec3 horizontal = pounceStart.lerp(pounceTarget, progress);
        double arcHeight = Math.sin(progress * Math.PI) * 1.55D;
        Vec3 desired = new Vec3(horizontal.x, horizontal.y + arcHeight - (progress == 1 ? 0.08D : 0), horizontal.z);
        Vec3 step = desired.subtract(start);
        if (step.lengthSqr() < 1.0E-5D) {
            return;
        }
        move(MoverType.SELF, step);
        if (horizontalCollision) {
            // Stop against the full body box, then finish the descent there.
            pounceStart = new Vec3(getX(), pounceStart.y, getZ());
            pounceTarget = new Vec3(getX(), pounceStart.y, getZ());
        }
        hasImpulse = true;
        RakanVfx.pounceTrail(level, this, start, position(), tick);
        if (step.y < 0 && onGround()) {
            pounceImpact(level, position());
        }
    }

    private void pounceImpact(ServerLevel level, Vec3 center) {
        if (pounceImpacted) {
            return;
        }
        pounceImpacted = true;
        entityData.set(POUNCE_LANDING_TICK, getActionTick());
        RakanVfx.pounceImpact(level, this, center);
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                AABB.ofSize(center, POUNCE_AREA_RADIUS * 2.0D, 4.0D, POUNCE_AREA_RADIUS * 2.0D),
                entity -> entity != this && !isAlliedTo(entity) && isUsableTarget(entity) && entity.distanceToSqr(center) <= POUNCE_AREA_RADIUS * POUNCE_AREA_RADIUS
                        && hasLineOfSight(entity)
        )) {
            int previousInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean hurt = target.hurt(damageSources().mobAttack(this), POUNCE_DAMAGE);
            if (!hurt) {
                target.invulnerableTime = previousInvulnerableTime;
            } else {
                Vec3 away = horizontalDirection(center, target.position(), pounceDirection).scale(1.0D);
                target.push(away.x, 0.26D, away.z);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, true));
            }
        }
    }

    private void runRoar(ServerLevel level, int tick) {
        LivingEntity target = lockedTarget(level);
        if (isUsableTarget(target)) {
            faceTargetImmediately(target);
        }
        if (tick < ROAR_RELEASE_TICK) RakanVfx.roarBreathe(level, this, tick);
        if (tick == ROAR_SOUND_TICK) {
            level.playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.4F, 0.75F);
        }
        if (tick == ROAR_RELEASE_TICK && !roarReleased) {
            roarReleased = true;
            RakanVfx.roarRelease(level, this);
        }
        if (tick == ROAR_BUFF_TICK && !commandPreview && !roarEmpowered) {
            activateRoarEmpowerment();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isFriendlyDamage(source)) return false;
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return super.hurt(source, amount);
        }
        if (getCombatAction() == ACTION_ROAR) {
            return false;
        }
        if (getCombatAction() == ACTION_GUARD) {
            return guardHurt(source, amount);
        }

        float before = getHealth();
        boolean accepted = super.hurt(source, amount);
        if (accepted && !level().isClientSide) {
            advanceGuardThresholds(before, getHealth(), source);
            tryStartHealthRoar(source.getEntity() instanceof LivingEntity attacker ? attacker : getTarget());
        }
        return accepted;
    }

    private boolean guardHurt(DamageSource source, float amount) {
        Entity direct = source.getEntity();
        LivingEntity attacker = direct instanceof LivingEntity living ? living : null;
        if (!level().isClientSide && !guardBlocked && isUsableTarget(attacker)) {
            guardBlocked = true;
            guardAttackerId = attacker.getUUID();
            entityData.set(GUARD_HIT_TICK, GUARD_HIT_TICKS);
            if (getActionTick() <= PERFECT_GUARD_TICKS) {
                guardCounterQueued = true;
            }
            if (level() instanceof ServerLevel serverLevel) {
                RakanVfx.guardBlock(serverLevel, this, attacker);
            }
        }
        float before = getHealth();
        boolean accepted = super.hurt(source, amount * GUARD_DAMAGE_MULTIPLIER);
        if (accepted && !level().isClientSide) {
            advanceGuardThresholds(before, getHealth(), source);
            tryStartHealthRoar(attacker);
        }
        return accepted;
    }

    /** The health phase takes priority over attacks, guards and command previews.
     * Called during damage so another hit in the same tick sees roar immunity. */
    private boolean tryStartHealthRoar(LivingEntity target) {
        if (level().isClientSide || !isAlive() || roarUsed || getCombatAction() == ACTION_ROAR
                || getHealth() >= getMaxHealth() * .5F) {
            return false;
        }
        if (getCombatAction() == ACTION_GUARD && !commandPreview) {
            // Resume an interrupted health-threshold guard after the awakening.
            pendingGuards = Math.min(9, pendingGuards + 1);
        }
        commandPreview = false;
        setDeltaMovement(Vec3.ZERO);
        beginAction(ACTION_ROAR, target);
        return true;
    }

    private void advanceGuardThresholds(float before, float after, DamageSource source) {
        int crossed = 0;
        while (nextGuardThresholdPercent >= 10
                && after <= getMaxHealth() * nextGuardThresholdPercent / 100.0F) {
            if (before > getMaxHealth() * nextGuardThresholdPercent / 100.0F) {
                crossed++;
            }
            nextGuardThresholdPercent -= 10;
        }

        if (crossed == 0 || !isAlive()) {
            return;
        }

        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity)) {
            attacker = source.getDirectEntity();
        }
        LivingEntity target = attacker instanceof LivingEntity living && isUsableTarget(living) ? living : getTarget();
        pendingGuards += crossed;
        pendingGuardTargetId = isUsableTarget(target) ? target.getUUID() : null;
        if (getCombatAction() == ACTION_NONE && (roarUsed || getHealth() >= getMaxHealth() * 0.5F)) {
            pendingGuards--;
            beginAction(ACTION_GUARD, target);
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (getCombatAction() == ACTION_GUARD || getCombatAction() == ACTION_ROAR) {
            return;
        }
        super.knockback(strength, x, z);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (getCombatAction() != ACTION_NONE) {
            return false;
        }
        if (target instanceof LivingEntity living && attackComboCooldown <= 0 && isUsableTarget(living)) {
            beginAction(ACTION_ATTACK_COMBO, living);
        }
        return false;
    }

    @Override
    protected boolean isImmobile() {
        return getCombatAction() != ACTION_NONE || super.isImmobile();
    }

    private void beginAction(int action, LivingEntity target) {
        navigation.stop();
        entityData.set(ACTION, action);
        entityData.set(ACTION_TICK, 0);
        entityData.set(POUNCE_LANDING_TICK, 0);
        entityData.set(GUARD_HIT_TICK, 0);
        entityData.set(GUARD_COUNTER_TICK, 0);
        lockedTargetId = isUsableTarget(target) ? target.getUUID() : null;
        guardAttackerId = null;
        comboHitMask = 0;
        guardBlocked = false;
        guardCounterHit = false;
        guardCounterQueued = false;
        pounceLaunched = false;
        pounceImpacted = false;
        roarReleased = false;
        pounceStart = Vec3.ZERO;
        pounceTarget = Vec3.ZERO;
        pounceDirection = Vec3.ZERO;
        applyCooldown(action);
        if (action == ACTION_ROAR && !commandPreview) {
            roarUsed = true;
        }
        if (isUsableTarget(target)) {
            faceTargetImmediately(target);
        }
    }

    private void finishAction() {
        int action = getCombatAction();
        if (action == ACTION_ROAR && !commandPreview && !roarEmpowered) {
            activateRoarEmpowerment();
        }
        commandPreview = false;
        entityData.set(ACTION, ACTION_NONE);
        entityData.set(ACTION_TICK, 0);
        entityData.set(POUNCE_LANDING_TICK, 0);
        entityData.set(GUARD_HIT_TICK, 0);
        entityData.set(GUARD_COUNTER_TICK, 0);
        lockedTargetId = null;
        guardAttackerId = null;
        comboHitMask = 0;
        guardBlocked = false;
        guardCounterHit = false;
        guardCounterQueued = false;
        pounceLaunched = false;
        pounceImpacted = false;
        roarReleased = false;
        pounceStart = Vec3.ZERO;
        pounceTarget = Vec3.ZERO;
        pounceDirection = Vec3.ZERO;
    }

    private void applyCooldown(int action) {
        switch (action) {
            case ACTION_ATTACK_COMBO -> attackComboCooldown = ATTACK_COMBO_COOLDOWN;
            case ACTION_POUNCE -> pounceCooldown = POUNCE_COOLDOWN;
            default -> {
            }
        }
    }

    private int actionDuration(int action) {
        return switch (action) {
            case ACTION_ATTACK_COMBO -> ATTACK_COMBO_DURATION;
            case ACTION_GUARD -> GUARD_DURATION;
            case ACTION_GUARD_COUNTER -> GUARD_COUNTER_DURATION;
            case ACTION_POUNCE -> POUNCE_DURATION;
            case ACTION_ROAR -> ROAR_DURATION;
            default -> 0;
        };
    }

    private void activateRoarEmpowerment() {
        roarEmpowered = true;
        addPermanentMultiplier(Attributes.ATTACK_DAMAGE, ROAR_ATTACK_MODIFIER, "Rakan Beast Monarch attack");
        addPermanentMultiplier(Attributes.MOVEMENT_SPEED, ROAR_SPEED_MODIFIER, "Rakan Beast Monarch speed");
        addPermanentMultiplier(Attributes.ARMOR, ROAR_ARMOR_MODIFIER, "Rakan Beast Monarch armor");
        addPermanentMultiplier(Attributes.ARMOR_TOUGHNESS, ROAR_TOUGHNESS_MODIFIER, "Rakan Beast Monarch toughness");
    }

    private void addPermanentMultiplier(Attribute attribute, UUID id, String name) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null && instance.getModifier(id) == null) {
            instance.addPermanentModifier(new AttributeModifier(
                    id,
                    name,
                    0.30D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private static boolean isSupportedAction(int action) {
        return action == ACTION_ATTACK_COMBO
                || action == ACTION_GUARD
                || action == ACTION_GUARD_COUNTER
                || action == ACTION_POUNCE
                || action == ACTION_ROAR;
    }

    private static boolean requiresTarget(int action) {
        return action == ACTION_ATTACK_COMBO || action == ACTION_GUARD_COUNTER || action == ACTION_POUNCE;
    }

    private LivingEntity lockedTarget(ServerLevel level) {
        return entityByUuid(level, lockedTargetId);
    }

    private static LivingEntity entityByUuid(ServerLevel level, UUID uuid) {
        return uuid != null && level.getEntity(uuid) instanceof LivingEntity living ? living : null;
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

    private void strikeTarget(LivingEntity target, float damage, double knockback, double lift) {
        if (!isUsableTarget(target) || isAlliedTo(target)) {
            return;
        }
        if (!hasLineOfSight(target) || distanceToSqr(target) > 25.0D) {
            return;
        }
        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        boolean hurt = target.hurt(damageSources().mobAttack(this), damage);
        if (!hurt) {
            target.invulnerableTime = previousInvulnerableTime;
        } else {
            Vec3 away = horizontalDirection(position(), target.position(), flatLook()).scale(knockback);
            target.push(away.x, lift, away.z);
            target.hurtMarked = true;
        }
    }

    private Vec3 flatLook() {
        Vec3 look = getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        return look.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : look.normalize();
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to, Vec3 fallback) {
        Vec3 direction = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-4D ? fallback : direction.normalize();
    }

    private boolean isUsableTarget(LivingEntity target) {
        if (isFriendlyMonarch(target)) return false;
        if (target == null || !target.isAlive()) {
            return false;
        }
        return !(target instanceof Player player) || (!player.isSpectator() && (commandPreview || !player.isCreative()));
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
        tag.putInt("AttackComboCooldown", attackComboCooldown);
        tag.putInt("PounceCooldown", pounceCooldown);
        tag.putInt("NextGuardThresholdPercent", nextGuardThresholdPercent);
        tag.putBoolean("RoarUsed", roarUsed);
        // Old RoarUsed also counted operator previews. Only this versioned marker
        // proves a natural health phase finished; unfinished casts restart on load.
        tag.putBoolean("HealthRoarCompleted", roarUsed && (getCombatAction() != ACTION_ROAR || commandPreview));
        tag.putBoolean("RoarEmpowered", roarEmpowered);
        tag.putInt("PendingGuards", pendingGuards + (getCombatAction() == ACTION_GUARD ? 1 : 0));
        if (pendingGuardTargetId != null) {
            tag.putUUID("PendingGuardTarget", pendingGuardTargetId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        attackComboCooldown = Math.max(0, tag.getInt("AttackComboCooldown"));
        pounceCooldown = Math.max(0, tag.getInt("PounceCooldown"));
        nextGuardThresholdPercent = tag.contains("NextGuardThresholdPercent")
                ? Mth.clamp(tag.getInt("NextGuardThresholdPercent"), 0, 90)
                : 90;
        roarEmpowered = tag.getBoolean("RoarEmpowered");
        roarUsed = tag.getBoolean("HealthRoarCompleted");
        pendingGuardTargetId = tag.hasUUID("PendingGuardTarget") ? tag.getUUID("PendingGuardTarget") : null;
        pendingGuards = Mth.clamp(tag.getInt("PendingGuards"), 0, 9);
        if (roarEmpowered) {
            activateRoarEmpowerment();
        }
        while (nextGuardThresholdPercent >= 10
                && getHealth() <= getMaxHealth() * nextGuardThresholdPercent / 100.0F) {
            nextGuardThresholdPercent -= 10;
        }
        finishAction();
    }

    private PlayState actionAnimation(AnimationState<RakanEntity> state) {
        RawAnimation animation = switch (getCombatAction()) {
            case ACTION_ATTACK_COMBO -> ATTACK_COMBO;
            case ACTION_GUARD -> getGuardCounterTick() > 0 ? GUARD_COUNTER
                    : getGuardHitTick() > 0 ? GUARD_HIT
                    : getActionTick() < GUARD_START_TICKS ? GUARD_START : GUARD_HOLD;
            case ACTION_GUARD_COUNTER -> GUARD_COUNTER;
            case ACTION_POUNCE -> POUNCE;
            case ACTION_ROAR -> ROAR;
            default -> null;
        };
        if (animation == null) {
            lastClientAnimation = null;
            return PlayState.STOP;
        }
        if (animation != lastClientAnimation || getActionTick() < lastClientActionTick) {
            state.getController().forceAnimationReset();
        }
        lastClientAnimation = animation;
        lastClientActionTick = getActionTick();
        return state.setAndContinue(animation);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rakan_actions", 0, this::actionAnimation) {
            @Override
            protected double adjustTick(double tick) {
                boolean resetting = shouldResetTick;
                double controllerTick = super.adjustTick(tick);
                // GeckoLib polls the next clip only at transition tick zero.
                // Replacing that zero with the server action time leaves the
                // animation queued forever once the model has rendered idle.
                // Synchronize playback only after its normal startup finishes.
                if (resetting || getAnimationState() != AnimationController.State.RUNNING
                        || getCurrentAnimation() == null) {
                    return controllerTick;
                }
                double partialTick = tick - Math.floor(tick);
                double elapsed = getActionTick() + partialTick;
                int landing = entityData.get(POUNCE_LANDING_TICK);
                if (getCombatAction() == ACTION_POUNCE) {
                    if (landing > 0 && landing < POUNCE_DURATION) {
                        // The impact pose and VFX meet at the actual ground contact.
                        elapsed = POUNCE_CLIP_IMPACT_TICK + (elapsed - landing)
                                * (POUNCE_CLIP_DURATION - POUNCE_CLIP_IMPACT_TICK) / (POUNCE_DURATION - landing);
                    } else if (elapsed > POUNCE_LAUNCH_TICK) {
                        elapsed = POUNCE_LAUNCH_TICK + (elapsed - POUNCE_LAUNCH_TICK)
                                * (POUNCE_CLIP_IMPACT_TICK - POUNCE_LAUNCH_TICK) / (POUNCE_IMPACT_TICK - POUNCE_LAUNCH_TICK);
                        // Wait in the impact pose if a lower ledge delays contact.
                        elapsed = Math.min(elapsed, POUNCE_CLIP_IMPACT_TICK);
                    }
                }
                if (getCombatAction() == ACTION_GUARD) {
                    if (getGuardCounterTick() > 0) elapsed = getGuardCounterTick() + partialTick;
                    else if (getGuardHitTick() > 0) elapsed = GUARD_HIT_TICKS - getGuardHitTick() + partialTick;
                    else if (getActionTick() >= GUARD_START_TICKS) elapsed = (getActionTick() - GUARD_START_TICKS) % 24 + partialTick;
                }
                return elapsed;
            }
        });
    }
}
