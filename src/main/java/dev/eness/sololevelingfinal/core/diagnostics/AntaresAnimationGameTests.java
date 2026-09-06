package dev.eness.sololevelingfinal.core.diagnostics;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.entity.AntaresEntity;
import dev.eness.sololevelingfinal.core.registry.ModEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import software.bernie.geckolib.core.animatable.model.CoreBakedGeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoModel;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.loading.object.*;
import software.bernie.geckolib.util.JsonUtil;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@GameTestHolder(SoloLeveling3.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AntaresAnimationGameTests {
    @GameTest(template="rakan_test_arena",timeoutTicks=470)
    public static void allClipsPlayAfterIdleAndRepeatWithoutFrozenWrists(GameTestHelper t){
        var boss=t.spawn(ModEntities.ANTARES.get(),new Vec3(15,2,12));boss.setNoAi(true);
        var target=t.spawn(net.minecraft.world.entity.EntityType.IRON_GOLEM,new Vec3(15,2,14.5));target.setNoAi(true);target.setInvulnerable(true);
        var model=new PlaybackModel();var manager=boss.getAnimatableInstanceCache().<AntaresEntity>getManagerForId(boss.getId());
        int[] tick={0};Map<String,float[]> first=new HashMap<>();Map<String,Double> motion=new HashMap<>();
        t.onEachTick(()->{
            for(float partial:new float[]{.25F,.75F}){
                var state=new AnimationState<>(boss,0,0,partial,false);double time=1000+tick[0]+partial;state.animationTick=time;
                model.processor.tickAnimation(boss,model,manager,time,state,true);
                var playing=manager.getAnimationControllers().get("antares_actions").getCurrentAnimation();if(playing==null)continue;
                String key=playing.animation().name()+(tick[0]>=350&&boss.getCombatAction()==AntaresEntity.RAY?":repeat":"");
                float[] pose=model.pose(),origin=first.computeIfAbsent(key,k->pose);double difference=0;
                for(int i=0;i<pose.length;i++){t.assertTrue(Float.isFinite(pose[i]),"Finite bone rotations");difference=Math.max(difference,Math.abs(pose[i]-origin[i]));}
                motion.merge(key,difference,Math::max);
                for(String hand:new String[]{"left_hand","right_hand"}){
                    var b=model.processor.getBone(hand);
                    boolean openPalm=boss.getCombatAction()==AntaresEntity.COMBO&&hand.equals("right_hand");
                    t.assertTrue(Math.abs(b.getRotX())<(openPalm?1.6:.3)&&Math.abs(b.getRotY())<.3&&Math.abs(b.getRotZ())<.3,"Wrist bounds, including open palm extension: "+hand);
                }
            }
            tick[0]++;
        });
        int[] starts={5,95,155,245,350},actions={1,2,3,4,4};
        for(int i=0;i<starts.length;i++){int action=actions[i];t.runAfterDelay(starts[i],()->t.assertTrue(boss.previewAbility(action,target),"Preview starts "+action));}
        t.runAfterDelay(182,()->{boss.invulnerableTime=0;boss.hurt(t.getLevel().damageSources().mobAttack(target),10);});
        t.runAfterDelay(460,()->{
            for(String clip:new String[]{"destruction_claw_combo","crimson_ruin_sweep","dragon_scale_guard","guard_counter","condensed_destruction_ray","condensed_destruction_ray:repeat"})
                t.assertTrue(motion.getOrDefault("animation.antares."+clip,0D)>.01,"Actual GeckoLib movement for "+clip+": "+motion);
            SoloLeveling3.LOGGER.info("Antares actual GeckoLib playback motion: {}",motion);t.succeed();
        });
    }
    /** Server-safe model adapter; uses the same baked resources and processor as GeoModel. */
    private static final class PlaybackModel implements CoreGeoModel<AntaresEntity> {
        private final CoreBakedGeoModel baked;
        private final BakedAnimations animations;
        private final AnimationProcessor<AntaresEntity> processor = new AnimationProcessor<>(this);

        private PlaybackModel() {
            try (var geo = getClass().getResourceAsStream("/assets/sololeveling3/geo/antares.geo.json");
                 var animation = getClass().getResourceAsStream("/assets/sololeveling3/animations/antares.animation.json")) {
                var raw = JsonUtil.GEO_GSON.fromJson(new InputStreamReader(geo, StandardCharsets.UTF_8),
                        software.bernie.geckolib.loading.json.raw.Model.class);
                baked = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(raw));
                var json = com.google.gson.JsonParser.parseReader(new InputStreamReader(animation, StandardCharsets.UTF_8)).getAsJsonObject();
                animations = JsonUtil.GEO_GSON.fromJson(json.getAsJsonObject("animations"), BakedAnimations.class);
                processor.setActiveModel(baked);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }

        private float[] pose() {
            float[] angles = new float[processor.getRegisteredBones().size() * 3];
            int index = 0;
            for (var bone : processor.getRegisteredBones()) {
                angles[index++] = bone.getRotX();
                angles[index++] = bone.getRotY();
                angles[index++] = bone.getRotZ();
            }
            return angles;
        }

        @Override public CoreBakedGeoModel getBakedGeoModel(String resource) { return baked; }
        @Override public AnimationProcessor<AntaresEntity> getAnimationProcessor() { return processor; }
        @Override public Animation getAnimation(AntaresEntity entity, String name) { return animations.getAnimation(name); }
        @Override public void handleAnimations(AntaresEntity entity, long id, AnimationState<AntaresEntity> state) {
            throw new UnsupportedOperationException("Test drives tickAnimation directly");
        }
    }
}
