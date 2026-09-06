package dev.eness.sololeveling3.entity;

import dev.eness.sololeveling3.registry.ModItems;
import dev.eness.sololeveling3.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public final class MonarchOfGiantsEntity extends AbstractMonarchBoss {
    private static final EntityDataAccessor<Boolean> SEALED =
            SynchedEntityData.defineId(MonarchOfGiantsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> KEYS =
            SynchedEntityData.defineId(MonarchOfGiantsEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DIALOG_SHOWN =
            SynchedEntityData.defineId(MonarchOfGiantsEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation PRISON = RawAnimation.begin().thenLoop("prison");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private int skillCooldown = 45;
    private int actionType;
    private int actionTicks;
    private int actionElapsed;

    public MonarchOfGiantsEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, Component.translatable("entity.sololeveling3.monarch_of_giants"), BossEvent.BossBarColor.PURPLE);
        this.xpReward = 450;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.MAX_HEALTH, 420.0D)
                .add(Attributes.ARMOR, 34.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SEALED, true);
        entityData.define(KEYS, 0);
        entityData.define(DIALOG_SHOWN, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isSealed() {
        return entityData.get(SEALED);
    }

    public int insertedKeys() {
        return entityData.get(KEYS);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isSealed() && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isSealed() && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!isSealed() || !held.is(ModItems.OLD_KEY.get())) {
            return super.mobInteract(player, hand);
        }

        if (!level().isClientSide) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            int next = Mth.clamp(insertedKeys() + 1, 0, 3);
            entityData.set(KEYS, next);
            level().playSound(null, blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 1.1F, 0.72F + next * 0.08F);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticles.DARK_CHAIN.get(), getX(), getY() + getBbHeight() * 0.55D, getZ(), 36, 1.1D, 1.5D, 1.1D, 0.04D);
            }

            if (next >= 3) {
                entityData.set(SEALED, false);
                skillCooldown = 65;
                setTarget(player);
                triggerAnim("actions", "chain_break");
                broadcastDialogue("dialogue.sololeveling3.legia.unsealed");
                level().playSound(null, blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.HOSTILE, 1.8F, 0.6F);
            } else {
                player.displayClientMessage(Component.translatable("message.sololeveling3.legia_key", next, 3), true);
            }
        }

        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (isSealed()) {
            navigation.stop();
            setDeltaMovement(Vec3.ZERO);
            setTarget(null);
            if (tickCount % 2 == 0) {
                BossCombat.chainPrison(serverLevel, this);
            }
            if (!entityData.get(DIALOG_SHOWN) && !serverLevel.getEntitiesOfClass(Player.class, getBoundingBox().inflate(15.0D), Player::isAlive).isEmpty()) {
                entityData.set(DIALOG_SHOWN, true);
                broadcastDialogue("dialogue.sololeveling3.legia.sealed");
            }
            return;
        }

        if (actionTicks > 0) {
            navigation.stop();
            actionTicks--;
            actionElapsed++;
            runAction(serverLevel);
            if (actionTicks == 0) {
                actionType = 0;
            }
            return;
        }

        if (skillCooldown > 0) {
            skillCooldown--;
        }

        LivingEntity target = getTarget();
        if (skillCooldown <= 0 && target != null && target.isAlive() && distanceToSqr(target) <= 144.0D) {
            lookAt(target, 30.0F, 30.0F);
            startAction(random.nextInt(3) + 1);
        }
    }

    private void startAction(int type) {
        actionType = type;
        actionElapsed = 0;
        actionTicks = switch (type) {
            case 1 -> 27;
            case 2 -> 20;
            default -> 25;
        };
        skillCooldown = Mth.nextInt(random, 35, 70);
        triggerAnim("actions", "attack" + type);
    }

    private void runAction(ServerLevel level) {
        int impact = actionType == 3 ? 13 : 15;
        if (actionElapsed != impact) {
            return;
        }

        Vec3 look = getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        float damage = (float)getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (actionType == 1) {
            Vec3 center = position().add(look.scale(5.0D));
            level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.2F, 0.65F);
            for (int ring = 1; ring <= 6; ring++) {
                BossCombat.ring(level, center.add(0.0D, 0.25D, 0.0D), ring * 1.5D, ParticleTypes.POOF, 28 + ring * 6);
            }
            BossCombat.damageArea(this, center, 9.0D, damage * 1.35F, 1.1D, 0.55D);
        } else if (actionType == 2) {
            Vec3 center = position().add(look.scale(5.5D)).add(0.0D, 1.1D, 0.0D);
            level.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.1F, 0.35F);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 12, 2.6D, 1.0D, 2.6D, 0.0D);
            BossCombat.damageArea(this, center, 5.0D, damage * 1.25F, 1.45D, 0.65D);
        } else {
            Vec3 center = position().add(look.scale(6.0D)).add(0.0D, 1.4D, 0.0D);
            level.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.HOSTILE, 1.2F, 0.55F);
            for (int step = 1; step <= 9; step++) {
                Vec3 point = position().add(look.scale(step)).add(0.0D, 1.25D, 0.0D);
                level.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 4, 0.45D, 0.55D, 0.45D, 0.04D);
            }
            BossCombat.damageArea(this, center, 4.5D, damage * 1.5F, 1.75D, 0.55D);
        }
    }

    private void broadcastDialogue(String translationKey) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Component message = Component.translatable(translationKey);
        for (Player nearby : serverLevel.getEntitiesOfClass(Player.class, getBoundingBox().inflate(24.0D), Player::isAlive)) {
            nearby.sendSystemMessage(message);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Sealed", isSealed());
        tag.putInt("InsertedKeys", insertedKeys());
        tag.putBoolean("DialogShown", entityData.get(DIALOG_SHOWN));
        tag.putInt("SkillCooldown", skillCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SEALED, !tag.contains("Sealed") || tag.getBoolean("Sealed"));
        entityData.set(KEYS, Mth.clamp(tag.getInt("InsertedKeys"), 0, 3));
        entityData.set(DIALOG_SHOWN, tag.getBoolean("DialogShown"));
        skillCooldown = Math.max(0, tag.getInt("SkillCooldown"));
    }

    private PlayState movement(AnimationState<MonarchOfGiantsEntity> state) {
        if (isSealed()) {
            return state.setAndContinue(PRISON);
        }
        return state.setAndContinue(state.isMoving() ? WALK : IDLE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::movement));
        controllers.add(new AnimationController<MonarchOfGiantsEntity>(this, "actions", 0, state -> PlayState.STOP)
                .triggerableAnim("chain_break", RawAnimation.begin().thenPlay("chain_break"))
                .triggerableAnim("attack1", RawAnimation.begin().thenPlay("attack1"))
                .triggerableAnim("attack2", RawAnimation.begin().thenPlay("attack2"))
                .triggerableAnim("attack3", RawAnimation.begin().thenPlay("attack3")));
    }
}
