package dev.eness.sololeveling3.entity;

import dev.eness.sololeveling3.campaign.CampaignDirector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

abstract class AbstractMonarchBoss extends Monster implements GeoEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent;

    protected AbstractMonarchBoss(
            EntityType<? extends Monster> type,
            Level level,
            Component title,
            BossEvent.BossBarColor color
    ) {
        super(type, level);
        this.bossEvent = new ServerBossEvent(title, color, BossEvent.BossBarOverlay.NOTCHED_20);
        this.setPersistenceRequired();
    }

    @Override
    protected void customServerAiStep() {
        if (isFriendlyMonarch(getTarget())) setTarget(null);
        super.customServerAiStep();
        bossEvent.setProgress(Math.max(0.0F, getHealth() / getMaxHealth()));
        bossEvent.setName(getDisplayName());
    }

    /** Only the three bosses belonging to the same campaign arena are allies. */
    protected final boolean isFriendlyMonarch(Entity other) {
        if (other == null || other == this || !isTrioMonarch(this) || !isTrioMonarch(other)
                || !level().dimension().equals(CampaignDirector.ARENA) || other.level() != level()) return false;
        String owner = CampaignDirector.OWNER_TAG;
        return getPersistentData().hasUUID(owner) && other.getPersistentData().hasUUID(owner)
                && getPersistentData().getUUID(owner).equals(other.getPersistentData().getUUID(owner));
    }

    private static boolean isTrioMonarch(Entity entity) {
        return entity instanceof IceMonarchEntity || entity instanceof RakanEntity || entity instanceof TarnakEntity;
    }

    protected final boolean isFriendlyDamage(DamageSource source) {
        return isFriendlyMonarch(source.getEntity()) || isFriendlyMonarch(source.getDirectEntity());
    }

    @Override public boolean isAlliedTo(Entity other) {
        return isFriendlyMonarch(other) || super.isAlliedTo(other);
    }

    @Override public boolean canAttack(LivingEntity target) {
        return !isFriendlyMonarch(target) && super.canAttack(target);
    }

    @Override public void setTarget(LivingEntity target) {
        super.setTarget(isFriendlyMonarch(target) ? null : target);
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        return !isFriendlyDamage(source) && super.hurt(source, amount);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
