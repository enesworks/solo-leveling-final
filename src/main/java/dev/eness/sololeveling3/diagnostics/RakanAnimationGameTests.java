package dev.eness.sololeveling3.diagnostics;

import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.entity.RakanEntity;
import dev.eness.sololeveling3.registry.ModEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import software.bernie.geckolib.core.animatable.model.CoreBakedGeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoModel;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationProcessor;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.util.JsonUtil;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@GameTestHolder(SoloLeveling3.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RakanAnimationGameTests {
    /** Exercise the real GeckoLib controller and bone processor after idle renders,
     * including fractional frames, guard subclips and a repeated command. */
    @GameTest(template = "rakan_test_arena", timeoutTicks = 390)
    public static void allAbilitiesMoveBonesAfterIdle(GameTestHelper test) {
        var boss = test.spawn(ModEntities.RAKAN.get(), new Vec3(15, 2, 8));
        boss.setNoAi(true);
        var target = test.spawn(net.minecraft.world.entity.EntityType.IRON_GOLEM, new Vec3(15, 2, 18));
        target.setNoAi(true);
        target.setInvulnerable(true);
        var model = new PlaybackModel();
        var manager = boss.getAnimatableInstanceCache().<RakanEntity>getManagerForId(boss.getId());
        int[] frameTick = {0};
        Map<String, float[]> firstPose = new HashMap<>();
        Map<String, Double> motion = new HashMap<>();
        boolean[] impactPoseSeen = {false};
        test.onEachTick(() -> {
            for (float partial : new float[]{.25F, .75F}) {
                var state = new AnimationState<>(boss, 0, 0, partial, false);
                double time = 1000 + frameTick[0] + partial;
                state.animationTick = time;
                model.processor.tickAnimation(boss, model, manager, time, state, true);
                if (boss.getCombatAction() == RakanEntity.ACTION_POUNCE && boss.getActionTick() == 27) {
                    // The accepted impact pose bends the torso 58 degrees.
                    test.assertTrue(Math.abs(model.processor.getBone("body").getRotX() - Math.toRadians(58)) < .06,
                            "Fast landing damage/VFX must coincide with the accepted impact pose");
                    impactPoseSeen[0] = true;
                }
                if (boss.getCombatAction() == RakanEntity.ACTION_NONE) continue;
                var current = manager.getAnimationControllers().get("rakan_actions").getCurrentAnimation();
                if (current == null) continue;
                String key = current.animation().name() + (frameTick[0] >= 295 ? ":repeat" : "");
                float[] pose = model.pose();
                float[] first = firstPose.computeIfAbsent(key, ignored -> pose);
                double difference = 0;
                for (int i = 0; i < pose.length; i++) {
                    test.assertTrue(Float.isFinite(pose[i]), "Animation bone angles must be finite");
                    difference = Math.max(difference, Math.abs(pose[i] - first[i]));
                }
                motion.merge(key, difference, Math::max);
            }
            frameTick[0]++;
        });
        int[] starts = {5, 50, 135, 210, 295};
        int[] actions = {RakanEntity.ACTION_ATTACK_COMBO, RakanEntity.ACTION_GUARD,
                RakanEntity.ACTION_POUNCE, RakanEntity.ACTION_ROAR, RakanEntity.ACTION_ROAR};
        for (int i = 0; i < starts.length; i++) {
            int action = actions[i];
            test.runAfterDelay(starts[i], () -> test.assertTrue(boss.previewAbility(action, target), "Preview must start: " + action));
        }
        test.runAfterDelay(380, () -> {
            test.assertTrue(impactPoseSeen[0], "Fast pounce must render its ground impact");
            for (String clip : new String[]{"predator_claw_combo", "guard_start", "guard_hold", "guard_hit",
                    "guard_counter", "monarch_pounce", "beast_monarch_roar", "beast_monarch_roar:repeat"}) {
                String key = "animation.rakan." + clip;
                test.assertTrue(motion.getOrDefault(key, 0D) > .001,
                        "GeckoLib must actually move bones for " + clip + "; observed motion=" + motion);
            }
            SoloLeveling3.LOGGER.info("Rakan GeckoLib playback bone motion (radians): {}", motion);
            test.succeed();
        });
    }

    /** Server-safe model adapter; uses the same baked resources and processor as GeoModel. */
    private static final class PlaybackModel implements CoreGeoModel<RakanEntity> {
        private final CoreBakedGeoModel baked;
        private final BakedAnimations animations;
        private final AnimationProcessor<RakanEntity> processor = new AnimationProcessor<>(this);

        private PlaybackModel() {
            try (var geo = getClass().getResourceAsStream("/assets/sololeveling3/geo/rakan.geo.json");
                 var animation = getClass().getResourceAsStream("/assets/sololeveling3/animations/rakan.animation.json")) {
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
        @Override public AnimationProcessor<RakanEntity> getAnimationProcessor() { return processor; }
        @Override public Animation getAnimation(RakanEntity entity, String name) { return animations.getAnimation(name); }
        @Override public void handleAnimations(RakanEntity entity, long id, AnimationState<RakanEntity> state) {
            throw new UnsupportedOperationException("Test drives tickAnimation directly");
        }
    }
}
