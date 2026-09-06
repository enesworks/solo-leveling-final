package dev.eness.sololeveling3.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import java.util.*;

/** Server-owned action clock, collision and damage. Client rendering never deals damage. */
public final class AntaresEntity extends AbstractMonarchBoss {
    public static final int NONE=0, COMBO=1, SWEEP=2, GUARD=3, RAY=4;
    public static final int COMBO_DURATION=30, SWEEP_DURATION=30, GUARD_DURATION=80, RAY_DURATION=90;
    public static final int COMBO_RIGHT_HIT=10, COMBO_LEFT_HIT=20, COMBO_PALM_HIT=27;
    public static final int SWEEP_RELEASE=16, SWEEP_TRAVEL_TICKS=7, RAY_LOCK=51, RAY_START=65, RAY_END=83;
    public static final double SWEEP_RANGE=6, RAY_RANGE=24, RAY_RADIUS=.65, BURST_RADIUS=2.5;
    public static final float GUARD_DAMAGE_MULTIPLIER=.01F;
    private static final ResourceKey<DamageType> COMBO_DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("sololeveling3","antares_locked_combo"));
    private static final ResourceKey<DamageType> RAY_DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("sololeveling3","antares_locked_ray"));
    private static final EntityDataAccessor<Integer> ACTION=dataInt(), AGE=dataInt(), COUNTER=dataInt(), LOCKED_TARGET=dataInt();
    private static final EntityDataAccessor<Float> AIM_X=dataFloat(), AIM_Y=dataFloat(), AIM_Z=dataFloat(), BEAM_LENGTH=dataFloat();
    private static EntityDataAccessor<Integer> dataInt(){return SynchedEntityData.defineId(AntaresEntity.class,EntityDataSerializers.INT);}
    private static EntityDataAccessor<Float> dataFloat(){return SynchedEntityData.defineId(AntaresEntity.class,EntityDataSerializers.FLOAT);}
    private static final RawAnimation[] CLIPS={RawAnimation.begin().thenLoop("animation.antares.idle"),
        RawAnimation.begin().thenPlay("animation.antares.destruction_claw_combo"),
        RawAnimation.begin().thenPlay("animation.antares.crimson_ruin_sweep"),
        RawAnimation.begin().thenPlay("animation.antares.dragon_scale_guard"),
        RawAnimation.begin().thenPlay("animation.antares.condensed_destruction_ray")};
    private static final RawAnimation WALK=RawAnimation.begin().thenLoop("animation.antares.walk");
    private static final RawAnimation COUNTER_CLIP=RawAnimation.begin().thenPlay("animation.antares.guard_counter");
    private int comboCooldown=20,sweepCooldown=60,guardCooldown,rayCooldown=160;
    private int completedCombos,consumedHealthThresholds,pendingGuards;
    private boolean counterUsed,preview;
    private UUID targetId;
    private final Set<UUID> sweepVictims=new HashSet<>();
    private RawAnimation lastAnimation;
    private int lastAge;

    public AntaresEntity(EntityType<? extends Monster> type,Level level){
        super(type,level,Component.translatable("entity.sololeveling3.antares"),BossEvent.BossBarColor.RED);xpReward=1000;
    }
    public static AttributeSupplier.Builder createAttributes(){return Mob.createMobAttributes()
        .add(Attributes.MAX_HEALTH,1600).add(Attributes.ATTACK_DAMAGE,26).add(Attributes.ARMOR,24)
        .add(Attributes.ARMOR_TOUGHNESS,8).add(Attributes.MOVEMENT_SPEED,.31)
        .add(Attributes.FOLLOW_RANGE,48).add(Attributes.KNOCKBACK_RESISTANCE,.75);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(ACTION,NONE);entityData.define(AGE,0);entityData.define(COUNTER,0);
        entityData.define(LOCKED_TARGET,0);
        entityData.define(AIM_X,0F);entityData.define(AIM_Y,0F);entityData.define(AIM_Z,1F);entityData.define(BEAM_LENGTH,0F);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1.15,false));
        goalSelector.addGoal(5,new RandomStrollGoal(this,.7));goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
    public int getCombatAction(){return entityData.get(ACTION);} public int getActionTick(){return entityData.get(AGE);}
    public int getCounterTick(){return entityData.get(COUNTER);} public double getBeamLength(){return entityData.get(BEAM_LENGTH);}
    public Vec3 getAim(){return new Vec3(entityData.get(AIM_X),entityData.get(AIM_Y),entityData.get(AIM_Z)).normalize();}
    public boolean isPreview(){return preview;}
    public int getCompletedCombos(){return completedCombos;}
    public int getPendingGuards(){return pendingGuards;}
    public int getConsumedHealthThresholds(){return consumedHealthThresholds;}
    public static int duration(int action){return switch(action){case COMBO->COMBO_DURATION;case SWEEP->SWEEP_DURATION;case GUARD->GUARD_DURATION;case RAY->RAY_DURATION;default->0;};}
    public static String clipName(int action){return switch(action){case COMBO->"destruction_claw_combo";case SWEEP->"crimson_ruin_sweep";case GUARD->"dragon_scale_guard";case RAY->"condensed_destruction_ray";default->"idle";};}
    public Vec3 forward(){double yaw=Math.toRadians(getYRot());return new Vec3(-Math.sin(yaw),0,Math.cos(yaw));}
    public Vec3 localToWorldVector(Vec3 v){Vec3 f=forward();return new Vec3(f.z*v.x+f.x*v.z,v.y,-f.x*v.x+f.z*v.z);}
    public Vec3 socket(String bone,double tick){String clip=getCounterTick()>0?"guard_counter":clipName(getCombatAction());
        double seconds=(getCounterTick()>0?getCounterTick()-1:tick)/20.;return localToWorldVector(AntaresAnimationData.socket(clip,bone,seconds));}
    public Vec3 rayOrigin(double tick){return position().add(socket("right_hand",tick).add(socket("left_hand",tick)).scale(.5));}
    public boolean forceAbility(int action,LivingEntity target){return start(action,target,false);}
    public boolean previewAbility(int action,LivingEntity target){return start(action,target,true);}
    private boolean start(int action,LivingEntity target,boolean command){
        if(level().isClientSide||!isAlive()||getCombatAction()!=NONE||getCounterTick()>0||action<1||action>4)return false;
        if(action!=GUARD&&(target==null||!target.isAlive()||target.isSpectator()||(!command&&target instanceof Player p&&p.isCreative())))return false;
        preview=command;targetId=target==null?null:target.getUUID();counterUsed=false;sweepVictims.clear();
        entityData.set(LOCKED_TARGET,target==null?0:target.getId());
        navigation.stop();setDeltaMovement(0,getDeltaMovement().y,0);entityData.set(ACTION,action);entityData.set(AGE,0);entityData.set(COUNTER,0);
        entityData.set(BEAM_LENGTH,0F);if(target!=null)face(target);setAim(forward());
        switch(action){case COMBO->comboCooldown=COMBO_DURATION+18;case SWEEP->{sweepCooldown=160;if(!command)completedCombos=0;}
            case GUARD->{guardCooldown=240;if(!command)pendingGuards=0;}case RAY->rayCooldown=320;}
        return true;
    }
    private boolean valid(LivingEntity e){return e!=null&&e!=this&&e.isAlive()&&!isAlliedTo(e)&&!e.isSpectator()&&(!(e instanceof Player p)||!p.isCreative());}
    private LivingEntity locked(){if(level() instanceof ServerLevel s&&targetId!=null&&s.getEntity(targetId) instanceof LivingEntity l&&l.isAlive())return l;return null;}
    /** Interpolated original target center relative to Antares, for locked VFX. */
    public Vec3 lockedTargetPoint(float partial){
        Entity target=level().getEntity(entityData.get(LOCKED_TARGET));
        return target instanceof LivingEntity living&&living.isAlive()?target.getPosition(partial).add(0,target.getBbHeight()*.5,0).subtract(getPosition(partial)):null;
    }
    public Vec3 comboTargetPoint(float partial){return lockedTargetPoint(partial);}
    private int healthThresholdsReached(){return Mth.clamp((int)Math.floor((getMaxHealth()-getHealth())*10.0/getMaxHealth()+1e-6),0,9);}
    private void updateGuardThresholds(){
        if(!isAlive())return;int reached=healthThresholdsReached();
        if(reached>consumedHealthThresholds){
            consumedHealthThresholds=reached;
            // A large hit reserves one guard, never a chain of skipped thresholds.
            // Damage during guard consumes thresholds without reserving another.
            if(getCombatAction()!=GUARD)pendingGuards=1;
        }
    }
    /** Shared by melee AI and the action clock so melee cannot skip a queued special. */
    public int chooseNextAbility(LivingEntity target){
        if(!isAlive()||getCombatAction()!=NONE||getCounterTick()>0)return NONE;
        if(pendingGuards>0&&guardCooldown==0)return GUARD;
        if(!valid(target)||!hasLineOfSight(target))return NONE;double distance=distanceToSqr(target);
        if(completedCombos>=3&&sweepCooldown==0&&distance<=64)return SWEEP;
        if(rayCooldown==0&&distance>=36&&distance<=RAY_RANGE*RAY_RANGE)return RAY;
        if(completedCombos<3&&comboCooldown==0&&distance<=16)return COMBO;
        return NONE;
    }
    @Override public void aiStep(){
        if(!level().isClientSide)updateGuardThresholds();
        super.aiStep();if(level().isClientSide||!(level() instanceof ServerLevel level))return;
        comboCooldown=Math.max(0,comboCooldown-1);sweepCooldown=Math.max(0,sweepCooldown-1);guardCooldown=Math.max(0,guardCooldown-1);rayCooldown=Math.max(0,rayCooldown-1);
        if(getCounterTick()>0)entityData.set(COUNTER,getCounterTick()>=13?0:getCounterTick()+1);
        int action=getCombatAction();if(action!=NONE){navigation.stop();setDeltaMovement(0,getDeltaMovement().y,0);
            int tick=getActionTick()+1;entityData.set(AGE,tick);LivingEntity target=locked();
            if(target!=null&&(action==COMBO||action==SWEEP&&tick<12||action==RAY&&tick<=RAY_END||action==GUARD&&getCounterTick()==0))face(target);
            if(action==COMBO&&target!=null){Vec3 delta=target.getBoundingBox().getCenter().subtract(position().add(socket("right_palm",tick)));
                setAim(delta.normalize());entityData.set(BEAM_LENGTH,(float)delta.length());}
            switch(action){case COMBO->combo(level,tick);case SWEEP->sweep(level,tick);case GUARD->{if(tick==15)sound(SoundEvents.BEACON_ACTIVATE,.5F,.6F);}case RAY->ray(level,tick,target);}
            if(tick>=duration(action)){
                if(action==COMBO&&!preview)completedCombos=Math.min(3,completedCombos+1);
                entityData.set(ACTION,NONE);entityData.set(AGE,0);entityData.set(BEAM_LENGTH,0F);entityData.set(LOCKED_TARGET,0);targetId=null;preview=false;}
            return;
        }
        if(isNoAi()||getCounterTick()>0)return;
        LivingEntity target=getTarget();int next=chooseNextAbility(target);if(next!=NONE)start(next,target,false);
    }
    private void combo(ServerLevel level,int tick){int hit=tick==COMBO_RIGHT_HIT?0:tick==COMBO_LEFT_HIT?1:tick==COMBO_PALM_HIT?2:-1;if(hit<0)return;
        sound(hit==2?SoundEvents.GENERIC_EXPLODE:SoundEvents.PLAYER_ATTACK_SWEEP,hit==2?.7F:1F,hit==2?.7F:.8F);
        LivingEntity primary=locked();
        // Once acquired, the same target receives all three scheduled strikes,
        // even after moving out of the melee fan or during ordinary hurt cooldown.
        if(valid(primary))hit(primary,(float)getAttributeValue(Attributes.ATTACK_DAMAGE)*(hit==2?1.3F:.8F),hit==2?.65:.06,true,
            new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(COMBO_DAMAGE),this));
        Vec3 center=position().add(forward().scale(1.9));
        for(LivingEntity victim:level.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(4),this::valid)){
            if(victim==primary)continue;
            Vec3 to=victim.position().subtract(position()).multiply(1,0,1);double reach=3.6+victim.getBbWidth()*.5;
            if(to.length()>reach||to.lengthSqr()>.01&&to.normalize().dot(forward())<.28||!hasLineOfSight(victim))continue;
            hit(victim,(float)getAttributeValue(Attributes.ATTACK_DAMAGE)*(hit==2?1.3F:.8F),hit==2?.65:.06,false);
        }
        if(hit==2)dust(level,center,38,1.7);
    }
    private void sweep(ServerLevel level,int tick){
        if(tick==SWEEP_RELEASE){setAim(forward());sound(SoundEvents.PLAYER_ATTACK_SWEEP,1.2F,.55F);}
        if(tick<SWEEP_RELEASE||tick>SWEEP_RELEASE+SWEEP_TRAVEL_TICKS)return;
        double distance=Math.min(SWEEP_RANGE,(tick-SWEEP_RELEASE)*(SWEEP_RANGE/SWEEP_TRAVEL_TICKS));Vec3 f=getAim();Vec3 center=position().add(0,1.8,0).add(f.scale(.8+distance));
        for(LivingEntity victim:level.getEntitiesOfClass(LivingEntity.class,AABB.ofSize(center,7,2.8,7),this::valid)){
            if(sweepVictims.contains(victim.getUUID()))continue;
            Vec3 q=victim.getBoundingBox().getCenter().subtract(position().add(0,1.8,0));double along=q.dot(f);
            double lateral=Math.abs(q.x*f.z-q.z*f.x);double front=.8+distance-.12*lateral*lateral;
            if(lateral>2.5+victim.getBbWidth()*.5||Math.abs(along-front)>.5+victim.getBbWidth()*.5||Math.abs(q.y)>1.3)continue;
            if(!clearLine(level,position().add(0,1.8,0),victim.getBoundingBox().getCenter()))continue;
            sweepVictims.add(victim.getUUID());hit(victim,30,.4,false);
        }
    }
    private void ray(ServerLevel level,int tick,LivingEntity target){
        if(tick==17)sound(SoundEvents.BEACON_POWER_SELECT,.6F,.65F);
        if(tick==33)sound(SoundEvents.RESPAWN_ANCHOR_CHARGE,.7F,.6F);
        if(tick<=RAY_END&&target!=null){Vec3 delta=target.getBoundingBox().getCenter().subtract(rayOrigin(tick));
            setAim(delta.normalize());entityData.set(BEAM_LENGTH,(float)delta.length());}
        if(tick>=RAY_START&&tick<=RAY_END){Vec3 origin=rayOrigin(tick);Vec3 far=origin.add(getAim().scale(RAY_RANGE));
            // Acquisition has a 24-block limit. An acquired target remains locked
            // through movement and walls; only collateral victims use ray collision.
            Vec3 end=target!=null?target.getBoundingBox().getCenter():level.clip(new ClipContext(origin,far,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getLocation();
            entityData.set(BEAM_LENGTH,(float)origin.distanceTo(end));
            if(tick==RAY_START)sound(SoundEvents.WARDEN_SONIC_BOOM,1F,.7F);
            if(tick==65||tick==70||tick==75||tick==80){
                if(valid(target))hit(target,11,0,true,new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RAY_DAMAGE),this));
                for(LivingEntity victim:level.getEntitiesOfClass(LivingEntity.class,new AABB(origin,end).inflate(RAY_RADIUS+1),this::valid))
                    if(victim!=target&&intersectsBeam(victim.getBoundingBox(),origin,end,RAY_RADIUS)&&clearLine(level,origin,victim.getBoundingBox().getCenter()))hit(victim,11,0,true);
            }
            if(tick==RAY_END){
                if(valid(target))hit(target,18,.65,true,new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RAY_DAMAGE),this));
                for(LivingEntity victim:level.getEntitiesOfClass(LivingEntity.class,AABB.ofSize(end,5,5,5),this::valid))
                if(victim!=target&&victim.getBoundingBox().getCenter().distanceToSqr(end)<=BURST_RADIUS*BURST_RADIUS&&clearLine(level,end.subtract(getAim().scale(.1)),victim.getBoundingBox().getCenter()))hit(victim,18,.65,true);
                dust(level,end,45,1.8);sound(SoundEvents.GENERIC_EXPLODE,.9F,.65F);}
        }
    }
    public static boolean intersectsBeam(AABB box,Vec3 start,Vec3 end,double radius){AABB expanded=box.inflate(radius);return expanded.contains(start)||expanded.clip(start,end).isPresent();}
    private boolean clearLine(Level level,Vec3 from,Vec3 to){return level.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
    private void hit(LivingEntity victim,float damage,double knockback,boolean pulse){
        hit(victim,damage,knockback,pulse,damageSources().mobAttack(this));
    }
    private void hit(LivingEntity victim,float damage,double knockback,boolean pulse,DamageSource source){
        if(!valid(victim))return;int old=victim.invulnerableTime;if(pulse)victim.invulnerableTime=0;
        boolean accepted=victim.hurt(source,damage);if(!accepted&&pulse)victim.invulnerableTime=old;
        if(accepted&&knockback>0){Vec3 away=victim.position().subtract(position());victim.knockback(knockback,-away.x,-away.z);victim.hurtMarked=true;}
    }
    @Override public boolean hurt(DamageSource source,float amount){
        if(source.is(DamageTypes.FELL_OUT_OF_WORLD))return super.hurt(source,amount);
        boolean guarded=getCombatAction()==GUARD;
        boolean accepted=super.hurt(source,guarded?amount*GUARD_DAMAGE_MULTIPLIER:amount);
        if(accepted&&!level().isClientSide&&isAlive()){
            updateGuardThresholds();
            if(guarded&&!counterUsed&&!source.is(DamageTypeTags.IS_PROJECTILE)&&source.getDirectEntity()==source.getEntity()
                &&source.getEntity() instanceof LivingEntity attacker&&(valid(attacker)||preview&&attacker instanceof Player&&!attacker.isSpectator())&&distanceToSqr(attacker)<=25&&hasLineOfSight(attacker)){
                counterUsed=true;entityData.set(COUNTER,1);face(attacker);
                sound(SoundEvents.SHIELD_BLOCK,1F,.55F);hit(attacker,20,.9,true);
                if(level() instanceof ServerLevel server)dust(server,position().add(forward().scale(1.3)).add(0,1.8,0),18,.7);
            }
        }
        return accepted;
    }
    @Override public void knockback(double power,double x,double z){if(getCombatAction()!=GUARD)super.knockback(power,x,z);}
    @Override public boolean doHurtTarget(Entity target){
        if(target instanceof LivingEntity living&&!isNoAi()){int next=chooseNextAbility(living);if(next!=NONE)start(next,living,false);}return false;
    }
    @Override protected boolean isImmobile(){return getCombatAction()!=NONE||getCounterTick()>0||super.isImmobile();}
    @Override public void die(DamageSource source){entityData.set(ACTION,NONE);entityData.set(COUNTER,0);entityData.set(BEAM_LENGTH,0F);entityData.set(LOCKED_TARGET,0);super.die(source);}
    private void setAim(Vec3 v){entityData.set(AIM_X,(float)v.x);entityData.set(AIM_Y,(float)v.y);entityData.set(AIM_Z,(float)v.z);}
    private void face(LivingEntity target){Vec3 d=target.position().subtract(position());if(d.horizontalDistanceSqr()<1e-6)return;
        float yaw=(float)(Mth.atan2(d.z,d.x)*Mth.RAD_TO_DEG)-90;setYRot(yaw);yRotO=yaw;setYBodyRot(yaw);yBodyRotO=yaw;setYHeadRot(yaw);yHeadRotO=yaw;}
    private void sound(SoundEvent event,float volume,float pitch){level().playSound(null,blockPosition(),event,SoundSource.HOSTILE,volume,pitch);}
    private void dust(ServerLevel level,Vec3 at,int count,double radius){
        level.sendParticles(new DustParticleOptions(new Vector3f(.6F,.015F,.035F),1.3F),at.x,at.y+.15,at.z,count,radius,.25,radius,.03);
        level.sendParticles(ParticleTypes.SMOKE,at.x,at.y+.15,at.z,count/2,radius,.15,radius,.045);
        level.sendParticles(ParticleTypes.CRIT,at.x,at.y+.3,at.z,count/3,radius*.6,.25,radius*.6,.15);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("AntaresComboCD",comboCooldown);tag.putInt("AntaresSweepCD",sweepCooldown);tag.putInt("AntaresGuardCD",guardCooldown);tag.putInt("AntaresRayCD",rayCooldown);
        tag.putInt("AntaresCombosCompleted",completedCombos);tag.putInt("AntaresGuardThresholds",consumedHealthThresholds);tag.putInt("AntaresPendingGuards",pendingGuards);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);
        if(tag.contains("AntaresComboCD")){comboCooldown=Math.max(0,tag.getInt("AntaresComboCD"));sweepCooldown=Math.max(0,tag.getInt("AntaresSweepCD"));guardCooldown=Math.max(0,tag.getInt("AntaresGuardCD"));rayCooldown=Math.max(0,tag.getInt("AntaresRayCD"));}
        completedCombos=Mth.clamp(tag.getInt("AntaresCombosCompleted"),0,3);
        consumedHealthThresholds=tag.contains("AntaresGuardThresholds")?Mth.clamp(tag.getInt("AntaresGuardThresholds"),0,9):healthThresholdsReached();
        pendingGuards=Mth.clamp(tag.getInt("AntaresPendingGuards"),0,Math.min(1,consumedHealthThresholds));
        entityData.set(ACTION,NONE);entityData.set(AGE,0);entityData.set(COUNTER,0);entityData.set(BEAM_LENGTH,0F);entityData.set(LOCKED_TARGET,0);preview=false;targetId=null;counterUsed=false;sweepVictims.clear();}
    private PlayState animate(AnimationState<AntaresEntity> state){RawAnimation next=getCounterTick()>0?COUNTER_CLIP:getCombatAction()==NONE&&state.isMoving()?WALK:CLIPS[getCombatAction()];
        state.getController().setTransitionLength(next==CLIPS[NONE]||next==WALK?3:0);
        if(next!=lastAnimation||getActionTick()<lastAge)state.getController().forceAnimationReset();lastAnimation=next;lastAge=getActionTick();return state.setAndContinue(next);}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){controllers.add(new AnimationController<>(this,"antares_actions",0,this::animate){
        @Override protected double adjustTick(double tick){boolean resetting=shouldResetTick;double normal=super.adjustTick(tick);
            if(resetting||getAnimationState()!=AnimationController.State.RUNNING||getCurrentAnimation()==null)return normal;
            double partial=tick-Math.floor(tick);if(getCounterTick()>0)return getCounterTick()-1+partial;
            return getCombatAction()==NONE?normal:getActionTick()+partial;
        }});}
}
