package dev.eness.sololeveling3.diagnostics;

import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.entity.AntaresEntity;
import dev.eness.sololeveling3.entity.AntaresAnimationData;
import dev.eness.sololeveling3.registry.ModEntities;
import dev.eness.sololeveling3.vfx.AntaresVfxMesh;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.*;

@GameTestHolder(SoloLeveling3.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AntaresGameTests {
    private static AntaresEntity boss(GameTestHelper t,double x,double z){var b=t.spawn(ModEntities.ANTARES.get(),new Vec3(x,2,z));b.setNoAi(true);b.setYRot(0);b.setYBodyRot(0);b.setYHeadRot(0);return b;}
    private static IronGolem target(GameTestHelper t,double x,double z){var e=t.spawn(EntityType.IRON_GOLEM,new Vec3(x,2,z));e.setNoAi(true);
        e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);e.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);e.setHealth(500);return e;}
    @GameTest(template="rakan_test_arena",timeoutTicks=45)
    public static void comboAlternatesHandsHasThreeHitsAndRecoversAtThirtyTicks(GameTestHelper t){
        var b=boss(t,15,12);var e=target(t,15,14.8);var far=target(t,15,20);List<Integer> hits=new ArrayList<>();float[] hp={500};
        t.assertTrue(b.forceAbility(AntaresEntity.COMBO,e),"Combo starts");
        t.onEachTick(()->{if(e.getHealth()<hp[0]){hits.add(b.getActionTick());hp[0]=e.getHealth();}});
        t.runAfterDelay(32,()->{t.assertTrue(hits.equals(List.of(10,20,27)),"Three animation-synced hits, got "+hits);
            t.assertTrue(b.getCombatAction()==AntaresEntity.NONE,"1.5 second recovery");t.assertTrue(far.getHealth()==500,"Combo range must be bounded");t.succeed();});
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=45)
    public static void comboKeepsMovingTargetThroughRangeWallsAndHurtCooldown(GameTestHelper t){
        var b=boss(t,15,12);var e=target(t,15,14.8);var other=target(t,26,26);
        List<Integer> hits=new ArrayList<>();float[] hp={500};
        t.assertTrue(b.forceAbility(AntaresEntity.COMBO,e),"Acquire original target");
        t.onEachTick(()->{if(e.getHealth()<hp[0]){hits.add(b.getActionTick());hp[0]=e.getHealth();}e.invulnerableTime=20;});
        t.runAfterDelay(7,()->{Vec3 p=t.absoluteVec(new Vec3(23,2,20));e.teleportTo(p.x,p.y,p.z);
            b.setTarget(other);for(int x=17;x<=21;x++)for(int y=1;y<=6;y++)t.setBlock(new BlockPos(x,y,16),Blocks.STONE);});
        t.runAfterDelay(17,()->{Vec3 p=t.absoluteVec(new Vec3(7,2,23));e.teleportTo(p.x,p.y,p.z);});
        t.runAfterDelay(25,()->{Vec3 p=t.absoluteVec(new Vec3(23,5,7));e.teleportTo(p.x,p.y,p.z);});
        for(int at:new int[]{11,21,28})t.runAfterDelay(at,()->{
            Vec3 delta=e.position().subtract(b.position()).multiply(1,0,1).normalize();
            t.assertTrue(b.forward().dot(delta)>.999,"Face original moving target");
            Vec3 aim=e.getBoundingBox().getCenter().subtract(b.position().add(b.socket("right_palm",b.getActionTick()))).normalize();
            t.assertTrue(b.getAim().dot(aim)>.999,"Palm follows target including elevation");});
        t.runAfterDelay(32,()->{t.assertTrue(hits.equals(List.of(10,20,27)),"Guaranteed locked hits despite movement/cooldown: "+hits);
            t.assertTrue(Math.abs(e.getHealth()-424.6)<.05,"Every full scheduled hit is applied once: "+e.getHealth());
            t.assertTrue(other.getHealth()==500,"AI target change cannot replace original lock");
            t.assertTrue(e.getLastDamageSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD),"Locked combo bypasses shield blocking");
            t.assertTrue(b.getCombatAction()==0&&b.comboTargetPoint(1)==null,"Recovery releases target lock");t.succeed();});
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=45)
    public static void comboDamagesLockedSurvivalPlayerThreeTimes(GameTestHelper t){
        var b=boss(t,15,12);var player=t.makeMockSurvivalPlayer();
        t.getLevel().addFreshEntity(player);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);player.setHealth(500);
        Vec3 start=t.absoluteVec(new Vec3(15,2,14.8));player.setPos(start);
        player.getAbilities().invulnerable=false;
        List<Integer> hits=new ArrayList<>();float[] hp={500};
        t.assertTrue(b.forceAbility(AntaresEntity.COMBO,player),"Acquire survival player");
        t.onEachTick(()->{if(player.getHealth()<hp[0]){hits.add(b.getActionTick());hp[0]=player.getHealth();}});
        t.runAfterDelay(15,()->{Vec3 p=t.absoluteVec(new Vec3(23,3,20));player.setPos(p);});
        t.runAfterDelay(32,()->{t.assertTrue(hits.equals(List.of(10,20,27)),"Three locked hits against survival player: "+hits);player.discard();t.succeed();});
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=25)
    public static void comboEffectsUseCorrectLiveFingertipsAndPalm(GameTestHelper t){
        for(double tick:new double[]{10.125,11,20.25,21,27,28,29,30}){
            Set<String> sampled=new HashSet<>();List<Vec3> centers=new ArrayList<>();
            final Vec3 palm=new Vec3(.3,2,.7),aim=new Vec3(.2,.3,1).normalize();
            AntaresVfxMesh.emit(AntaresEntity.COMBO,tick,0,new Vec3(0,0,1),aim,8,(bone,at)->{
                sampled.add(bone);if(bone.equals("right_palm")){t.assertTrue(at==tick,"Palm origin samples current pose");return palm;}
                if(Math.abs(at-tick)<1e-6)sampled.add("live:"+bone);return AntaresAnimationData.socket("destruction_claw_combo",bone,at/20.);
            },(a,b,c,d,r,g,blue,alpha)->centers.add(a.add(b).add(c).add(d).scale(.25)));
            if(tick<13.125||tick>=17.625&&tick<22.125){String side=tick<13.125?"right":"left";
                for(int i=1;i<=3;i++)t.assertTrue(sampled.contains("live:"+side+"_claw_"+i),"Each trail ends on live "+side+" fingertip");
                t.assertTrue(sampled.stream().allMatch(n->n.contains(side+"_claw_")),"Opposite hand cannot emit this strike");
            }else if(tick<30){t.assertTrue(sampled.equals(Set.of("right_palm")),"Palm effect uses only actual palm socket");
                if(tick<28.875){Vec3 midpoint=palm.add(aim.scale(4.0075));t.assertTrue(centers.stream().anyMatch(p->p.distanceTo(midpoint)<1e-6),"Impulse runs from palm front to exact target");}
            }else t.assertTrue(centers.isEmpty(),"No lingering effect after 1.5 seconds");
        }t.succeed();
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=45)
    public static void sweepHitsMultipleTargetsOnceAndRespectsRange(GameTestHelper t){
        var b=boss(t,15,8);var one=target(t,14,12);var two=target(t,16,13.5);var far=target(t,15,19);
        t.assertTrue(b.forceAbility(AntaresEntity.SWEEP,far),"Sweep starts");
        t.runAfterDelay(32,()->{t.assertTrue(Math.abs(one.getHealth()-470)<.01,"First sweep victim exactly once: "+one.getHealth());
            t.assertTrue(Math.abs(two.getHealth()-470)<.01,"Second sweep victim exactly once: "+two.getHealth());
            t.assertTrue(far.getHealth()==500,"Wave cannot exceed six blocks of travel");t.succeed();});
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=105)
    public static void guardExpiresAndCountersOnlyFirstMelee(GameTestHelper t){
        var b=boss(t,15,12);var e=target(t,15,14.5);b.getAttribute(Attributes.ARMOR).setBaseValue(0);b.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);
        t.assertTrue(b.forceAbility(AntaresEntity.GUARD,e),"Guard starts");
        t.runAfterDelay(20,()->{float before=b.getHealth();b.invulnerableTime=0;b.hurt(t.getLevel().damageSources().mobAttack(e),40);
            t.assertTrue(Math.abs(before-b.getHealth()-.4)<.01,"Guard takes 1% incoming damage; before="+before+" after="+b.getHealth()+" max="+b.getMaxHealth());
            t.assertTrue(e.getHealth()==480&&b.getCounterTick()>0,"First melee triggers pressure counter");
            Vec3 velocityBefore=b.getDeltaMovement();b.knockback(2,1,0);t.assertTrue(b.getDeltaMovement().equals(velocityBefore),"Guard prevents knockback");});
        t.runAfterDelay(43,()->{b.invulnerableTime=0;b.hurt(t.getLevel().damageSources().mobAttack(e),40);t.assertTrue(e.getHealth()==480,"Counter may occur only once");});
        t.runAfterDelay(82,()->{t.assertTrue(b.getCombatAction()==AntaresEntity.NONE,"Guard ends after 80 ticks");float before=b.getHealth();b.invulnerableTime=0;b.hurt(t.getLevel().damageSources().mobAttack(e),40);
            t.assertTrue(Math.abs(before-b.getHealth()-40)<.01,"Normal damage restored after guard");t.succeed();});
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=105)
    public static void rayPiercesTargetsWithFourPulses(GameTestHelper t){
        var b=boss(t,15,5);var first=target(t,15,12);var second=target(t,15,19);var side=target(t,21,15);
        t.assertTrue(b.forceAbility(AntaresEntity.RAY,second),"Ray starts");
        t.runAfterDelay(92,()->{t.assertTrue(Math.abs(first.getHealth()-456)<.01,"First target receives four pulses: "+first.getHealth());
            t.assertTrue(Math.abs(second.getHealth()-438)<.01,"Locked target receives four pulses plus final burst: "+second.getHealth());
            t.assertTrue(side.getHealth()==500,"Off-axis target is safe");t.succeed();});
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=105)
    public static void rayLocksThroughWallButCollateralPulsesRespectOcclusion(GameTestHelper t){
        var b=boss(t,15,5);var front=target(t,15,13);var behind=target(t,15,19);var near=target(t,17,14.8);var shielded=target(t,17,18.5);
        for(int x=11;x<=19;x++)for(int y=1;y<=7;y++)t.setBlock(new BlockPos(x,y,16),Blocks.STONE);
        t.assertTrue(b.forceAbility(AntaresEntity.RAY,behind),"Ray preview can be aimed at occluded target");
        t.runAfterDelay(92,()->{t.assertTrue(Math.abs(front.getHealth()-456)<.01,"Visible collateral receives four pulses");t.assertTrue(Math.abs(behind.getHealth()-438)<.01,"Original locked target gets all pulses and burst through wall");
            t.assertTrue(near.getHealth()==500,"Off-axis collateral outside target burst stays safe");
            t.assertTrue(Math.abs(shielded.getHealth()-482)<.01,"Collateral behind wall receives only nearby target burst: "+shielded.getHealth());
            t.assertTrue(t.getBlockState(new BlockPos(15,3,16)).is(Blocks.STONE),"Beam does not edit terrain");t.succeed();});
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=25)
    public static void guardDoesNotCounterProjectilesAndLoadClearsCast(GameTestHelper t){
        var b=boss(t,15,12);var e=target(t,15,14.5);var arrow=t.spawn(EntityType.ARROW,new Vec3(15,3,14));arrow.setOwner(e);
        b.forceAbility(AntaresEntity.GUARD,e);b.hurt(t.getLevel().damageSources().arrow(arrow,e),30);
        t.assertTrue(b.getCounterTick()==0&&e.getHealth()==500,"Projectile never consumes melee counter");
        CompoundTag tag=new CompoundTag();b.addAdditionalSaveData(tag);b.readAdditionalSaveData(tag);
        t.assertTrue(b.getCombatAction()==AntaresEntity.NONE&&b.getCounterTick()==0&&b.getBeamLength()==0,"Reload never leaves a stuck guard or beam");
        t.succeed();
    }
    @GameTest(template="rakan_test_arena",timeoutTicks=25)
    public static void vfxMeshesAreBoundedAndUseAnimatedSockets(GameTestHelper t){
        var all=new LinkedHashMap<String,Object>();
        for(int action=1;action<=4;action++){
            final int a=action;String clip=AntaresEntity.clipName(a);var frames=new ArrayList<Object>();
            for(int tick=0;tick<=AntaresEntity.duration(action);tick++){
                final int time=tick;var quads=new ArrayList<double[]>();
                Vec3 aim=new Vec3(0,0,1);double range=12;
                if(a==AntaresEntity.COMBO){Vec3 delta=new Vec3(0,1.8,3).subtract(AntaresAnimationData.socket(clip,"right_palm",tick/20.));aim=delta.normalize();range=delta.length();}
                AntaresVfxMesh.emit(a,tick,0,new Vec3(0,0,1),aim,range,(bone,at)->AntaresAnimationData.socket(clip,bone,at/20.),(p,q,r,s,red,g,blue,alpha)->{
                    for(Vec3 v:new Vec3[]{p,q,r,s})t.assertTrue(Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z)&&v.length()<30,"VFX vertices finite and bounded at "+time);
                    t.assertTrue(alpha>=0&&alpha<=1,"Opacity in range");quads.add(new double[]{p.x,p.y,p.z,q.x,q.y,q.z,r.x,r.y,r.z,s.x,s.y,s.z,red,g,blue,alpha});});
                t.assertTrue(quads.size()<700,"Bounded mesh budget");frames.add(Map.of("tick",tick,"quads",quads));
            }
            all.put(clip,frames);
        }
        try{java.nio.file.Files.writeString(java.nio.file.Path.of("antares-vfx-meshes.json"),new com.google.gson.Gson().toJson(all));}catch(java.io.IOException e){throw new RuntimeException(e);}
        t.succeed();
    }
}
