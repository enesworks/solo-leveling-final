package dev.eness.sololeveling3.entity;

import dev.eness.sololeveling3.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.UUID;

public final class IceMonarchEntity extends AbstractMonarchBoss {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private int skillCooldown = 90;
    private int actionType;
    private int actionTicks;
    private int actionElapsed;
    private Vec3 lockedTarget = Vec3.ZERO;
    private UUID coffinTarget;

    public IceMonarchEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, Component.translatable("entity.sololeveling3.ice_monarch"), BossEvent.BossBarColor.BLUE);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.MAX_HEALTH, 360.0D)
                .add(Attributes.ARMOR, 26.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 5.0D)
                .add(Attributes.ATTACK_DAMAGE, 19.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.35D, false));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.9D));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (tickCount % 5 == 0) {
            serverLevel.sendParticles(ModParticles.FROST.get(), getX(), getY() + 1.0D, getZ(), 3, 0.45D, 0.7D, 0.45D, 0.02D);
        }

        if (actionTicks > 0) {
            navigation.stop();
            actionTicks--;
            actionElapsed++;
            runAction(serverLevel);
            if (actionTicks == 0) {
                actionType = 0;
                coffinTarget = null;
            }
            return;
        }

        if (skillCooldown > 0) {
            skillCooldown--;
        }

        LivingEntity target = getTarget();
        if (skillCooldown <= 0 && target != null && target.isAlive() && distanceToSqr(target) <= 625.0D) {
            lookAt(target, 30.0F, 30.0F);
            startAction(random.nextInt(4) + 1, target);
        }
    }

    private void startAction(int type, LivingEntity target) {
        actionType = type;
        actionElapsed = 0;
        actionTicks = type == 4 ? 60 : 42;
        lockedTarget = target.position();
        coffinTarget = type == 4 ? target.getUUID() : null;
        skillCooldown = Mth.nextInt(random, 80, 150);
        String animation = switch (type) {
            case 2 -> "attack1";
            case 3 -> "attack2";
            default -> "attack_magic";
        };
        triggerAnim("actions", animation);
    }

    private void runAction(ServerLevel level) {
        float damage = (float)getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (actionElapsed == 8) {
            level.playSound(null, blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.25F, 0.65F);
            level.sendParticles(ModParticles.FROST.get(), getX(), getY() + 1.25D, getZ(), 36, 0.7D, 1.0D, 0.7D, 0.1D);
        }

        if (actionType == 1 && actionElapsed >= 20 && actionElapsed <= 32 && actionElapsed % 3 == 2) {
            double radius = 2.0D + (actionElapsed - 20) * 0.48D;
            BossCombat.frostBurst(level, position().add(0.0D, 0.15D, 0.0D), radius, 36);
            BossCombat.freezeArea(this, position(), radius, damage * 0.34F, 70, 1);
        } else if (actionType == 2 && actionElapsed >= 20 && actionElapsed <= 34 && actionElapsed % 2 == 0) {
            Vec3 origin = position().add(0.0D, 0.2D, 0.0D);
            Vec3 delta = lockedTarget.subtract(origin).multiply(1.0D, 0.0D, 1.0D);
            Vec3 direction = delta.lengthSqr() < 0.01D ? getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize() : delta.normalize();
            double distance = Math.min(16.0D, 2.0D + (actionElapsed - 20) * 0.9D);
            Vec3 point = origin.add(direction.scale(distance));
            BossCombat.spike(level, point, 10);
            BossCombat.freezeArea(this, point, 2.2D, damage * 0.6F, 80, 2);
        } else if (actionType == 3 && actionElapsed >= 20 && actionElapsed <= 38 && actionElapsed % 3 == 2) {
            double angle = (actionElapsed - 20) * 0.9D;
            double radius = 3.0D + (actionElapsed - 20) * 0.35D;
            Vec3 first = lockedTarget.add(Math.cos(angle) * radius, 0.1D, Math.sin(angle) * radius);
            Vec3 second = lockedTarget.add(-Math.cos(angle) * radius, 0.1D, -Math.sin(angle) * radius);
            BossCombat.spike(level, first, 7);
            BossCombat.spike(level, second, 7);
            BossCombat.freezeArea(this, first, 2.0D, damage * 0.52F, 65, 1);
            BossCombat.freezeArea(this, second, 2.0D, damage * 0.52F, 65, 1);
        } else if (actionType == 4 && actionElapsed >= 20 && actionElapsed <= 48 && actionElapsed % 4 == 0) {
            LivingEntity target = coffinTarget == null ? null : findLiving(level, coffinTarget);
            if (target != null && target.isAlive()) {
                Vec3 point = target.position();
                BossCombat.spike(level, point, 14);
                BossCombat.freezeArea(this, point, 1.75D, damage * 0.38F, 18, 3);
            }
        }
    }

    private LivingEntity findLiving(ServerLevel level, UUID id) {
        if (level.getEntity(id) instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkillCooldown", skillCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        skillCooldown = Math.max(0, tag.getInt("SkillCooldown"));
    }

    private PlayState movement(AnimationState<IceMonarchEntity> state) {
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::movement));
        controllers.add(new AnimationController<IceMonarchEntity>(this, "actions", 0, state -> PlayState.STOP)
                .triggerableAnim("attack1", RawAnimation.begin().thenPlay("attack1"))
                .triggerableAnim("attack2", RawAnimation.begin().thenPlay("attack2"))
                .triggerableAnim("attack_magic", RawAnimation.begin().thenPlay("attack_magic")));
    }
}
