package dev.eness.sololevelingfinal.core.diagnostics;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.entity.RakanEntity;
import dev.eness.sololevelingfinal.core.registry.ModEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Behavioral tests in a generated flat arena, never the owner's saved world. */
@GameTestHolder(SoloLeveling3.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RakanGameTests {
    @GameTest(template = "rakan_test_arena", timeoutTicks = 100)
    public static void pounceUsesTheNearbyTargetDistance(GameTestHelper test) {
        RakanEntity boss = boss(test,15,8);
        IronGolem target = target(test,15,12);
        Vec3 start = boss.position();
        test.assertTrue(boss.forceAbility(RakanEntity.ACTION_POUNCE,target),"Pounce must start");
        test.runAfterDelay(76, () -> {
            double distance = boss.position().subtract(start).horizontalDistance();
            test.assertTrue(distance >= 3.8 && distance <= 4.2,"A target four blocks away must cause a four-block jump, got " + distance);
            test.assertTrue(Math.abs(target.getHealth()-488)<.001,"Nearby landing must hit once");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 100)
    public static void pounceClampsOnlyAtTwelveBlocks(GameTestHelper test) {
        RakanEntity boss = boss(test,15,6);
        IronGolem target = target(test,15,26);
        Vec3 start = boss.position();
        test.assertTrue(boss.forceAbility(RakanEntity.ACTION_POUNCE,target),"Pounce must start");
        test.runAfterDelay(76, () -> {
            double distance = boss.position().subtract(start).horizontalDistance();
            test.assertTrue(distance >= 11.8 && distance <= 12.2,"A distant target must cap the jump at twelve blocks, got " + distance);
            test.assertTrue(target.getHealth()==500,"A distant target outside the landing area must not be damaged");
            test.assertTrue(boss.onGround(),"Maximum-range jump must land");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 20)
    public static void geckoLibLoadsTheExportedRigAndClips(GameTestHelper test) {
        try (var geo = RakanGameTests.class.getResourceAsStream("/assets/sololeveling3/geo/rakan.geo.json");
             var animation = RakanGameTests.class.getResourceAsStream("/assets/sololeveling3/animations/rakan.animation.json")) {
            test.assertTrue(geo != null && animation != null,"Runtime Rakan resources must exist");
            var gson = software.bernie.geckolib.util.JsonUtil.GEO_GSON;
            var raw = gson.fromJson(new java.io.InputStreamReader(geo,java.nio.charset.StandardCharsets.UTF_8),
                    software.bernie.geckolib.loading.json.raw.Model.class);
            var baked = software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(
                    software.bernie.geckolib.loading.object.GeometryTree.fromModel(raw));
            test.assertTrue(baked.getBone("right_forearm").orElseThrow().getParent().getName().equals("right_arm"),"Elbow hierarchy must survive GeckoLib baking");
            test.assertTrue(baked.getBone("left_calf").orElseThrow().getParent().getName().equals("left_leg"),"Knee hierarchy must survive GeckoLib baking");
            test.assertTrue(Math.abs(baked.getBone("right_arm").orElseThrow().getPivotX()+5)<1e-5,"Bedrock pivot conversion must restore the accepted rig coordinates");
            var json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(animation,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var animations = gson.fromJson(json.getAsJsonObject("animations"),software.bernie.geckolib.loading.object.BakedAnimations.class);
            test.assertTrue(animations.animations().size()==7,"GeckoLib must successfully bake all seven animation clips");
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        test.succeed();
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 20)
    public static void vfxMeshesAreFiniteAndBounded(GameTestHelper test) {
        var output = new java.util.LinkedHashMap<String,Object>();
        for (var style : dev.eness.sololevelingfinal.core.vfx.RakanVfxMesh.Style.values()) {
            var frames = new java.util.ArrayList<Object>();
            int duration = dev.eness.sololevelingfinal.core.vfx.RakanVfxMesh.duration(style);
            for (int age=0; age<duration; age++) {
                var quads = new java.util.ArrayList<double[]>();
                dev.eness.sololevelingfinal.core.vfx.RakanVfxMesh.emit(style,age,Vec3.ZERO,new Vec3(0,0,1),(a,b,c,d,r,g,blue,alpha) -> {
                    for (Vec3 vertex : new Vec3[]{a,b,c,d}) {
                        test.assertTrue(Double.isFinite(vertex.x) && Double.isFinite(vertex.y) && Double.isFinite(vertex.z), "VFX vertex must be finite");
                        test.assertTrue(vertex.length() <= 9, "VFX must stay inside its bounded radius");
                    }
                    test.assertTrue(Float.isFinite(alpha) && alpha >= 0 && alpha <= 1, "VFX opacity must be valid");
                    quads.add(new double[]{a.x,a.y,a.z,b.x,b.y,b.z,c.x,c.y,c.z,d.x,d.y,d.z,r,g,blue,alpha});
                });
                test.assertTrue(quads.size() <= 192, "VFX geometry budget must be bounded");
                frames.add(java.util.Map.of("age",age,"quads",quads));
            }
            output.put(style.name(),frames);
        }
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("rakan-vfx-meshes.json"),new com.google.gson.Gson().toJson(output));
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        test.succeed();
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 100)
    public static void pounceRespectsWalls(GameTestHelper test) {
        RakanEntity boss = boss(test,15,8);
        IronGolem target = target(test,15,18);
        Vec3 start = boss.position();
        var base = boss.blockPosition().offset(0,0,5);
        for (int x=-3; x<=3; x++) for(int y=0; y<=7; y++) {
            test.getLevel().setBlockAndUpdate(base.offset(x,y,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        }
        test.assertTrue(boss.forceAbility(RakanEntity.ACTION_POUNCE,target), "Pounce must start");
        test.runAfterDelay(76, () -> {
            test.assertTrue(boss.position().subtract(start).horizontalDistance() < 5,"Pounce must not cross a full-height wall");
            test.assertTrue(target.getHealth() == 500,"Impact must not damage a target through the wall; HP=" + target.getHealth()
                    + ", boss=" + boss.position() + ", target=" + target.position() + ", source=" + target.getLastDamageSource());
            test.assertTrue(boss.onGround(),"Blocked pounce must land before recovering");
            test.succeed();
        });
    }

    private static RakanEntity boss(GameTestHelper test, double x, double z) {
        RakanEntity boss = test.spawn(ModEntities.RAKAN.get(), new Vec3(x, 2, z));
        boss.setNoAi(true);
        boss.setYRot(0);
        boss.setYHeadRot(0);
        boss.setYBodyRot(0);
        return boss;
    }

    private static IronGolem target(GameTestHelper test, double x, double z) {
        IronGolem target = test.spawn(EntityType.IRON_GOLEM, new Vec3(x, 2, z));
        target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);
        target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        target.setHealth(500);
        return target;
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 60)
    public static void comboHasThreeSeparateHits(GameTestHelper test) {
        RakanEntity boss = boss(test, 15, 14);
        IronGolem target = target(test, 15, 15.7);
        test.assertTrue(boss.forceAbility(RakanEntity.ACTION_ATTACK_COMBO, target), "Combo must start");
        test.assertFalse(boss.forceAbility(RakanEntity.ACTION_POUNCE, target), "Active combo must not be overwritten");
        float[] health = {target.getHealth()};
        int[] hits = {0};
        test.onEachTick(() -> {
            if (target.getHealth() < health[0] - 0.001F) {
                hits[0]++;
                health[0] = target.getHealth();
            }
        });
        test.runAfterDelay(43, () -> {
            test.assertTrue(hits[0] == 3, "Expected 3 damage contacts, received " + hits[0]);
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_NONE, "2 second combo must recover");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 100)
    public static void guardProtectionExpires(GameTestHelper test) {
        RakanEntity boss = boss(test, 15, 15);
        boss.getAttribute(Attributes.ARMOR).setBaseValue(0);
        boss.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);
        test.assertTrue(boss.forceAbility(RakanEntity.ACTION_GUARD, null), "Guard must start");
        float start = boss.getHealth();
        test.runAfterDelay(10, () -> {
            boss.hurt(test.getLevel().damageSources().generic(), 100);
            test.assertTrue(Math.abs(boss.getHealth() - (start - 1)) < 0.001F, "Guard must pass exactly 1 percent before armor");
        });
        test.runAfterDelay(75, () -> test.assertTrue(boss.getCombatAction() != RakanEntity.ACTION_NONE, "Guard must last 4 seconds"));
        test.runAfterDelay(83, () -> {
            float before = boss.getHealth();
            boss.hurt(test.getLevel().damageSources().generic(), 10);
            test.assertTrue(Math.abs(before - boss.getHealth() - 10) < 0.001F, "Normal damage must resume after the clip");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 100)
    public static void roarImmunityAndSingleBuff(GameTestHelper test) {
        RakanEntity boss = boss(test, 15, 15);
        boss.setHealth(boss.getMaxHealth() * 0.49F);
        double attack = boss.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double speed = boss.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double armor = boss.getAttributeValue(Attributes.ARMOR);
        double toughness = boss.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        test.assertTrue(boss.forceAbility(RakanEntity.ACTION_ROAR, null), "First roar must start below half health");
        test.runAfterDelay(20, () -> {
            float hp = boss.getHealth();
            test.assertFalse(boss.hurt(test.getLevel().damageSources().generic(), 100), "Roar must reject damage");
            test.assertTrue(boss.getHealth() == hp, "Roar immunity must preserve health");
        });
        test.runAfterDelay(83, () -> {
            test.assertTrue(Math.abs(boss.getAttributeValue(Attributes.ATTACK_DAMAGE) - attack*1.3) < 1e-6, "Attack +30 percent");
            test.assertTrue(Math.abs(boss.getAttributeValue(Attributes.MOVEMENT_SPEED) - speed*1.3) < 1e-6, "Speed +30 percent");
            test.assertTrue(Math.abs(boss.getAttributeValue(Attributes.ARMOR) - armor*1.3) < 1e-6, "Armor +30 percent");
            test.assertTrue(Math.abs(boss.getAttributeValue(Attributes.ARMOR_TOUGHNESS) - toughness*1.3) < 1e-6, "Toughness +30 percent");
            test.assertFalse(boss.forceAbility(RakanEntity.ACTION_ROAR, null), "Roar must not repeat or stack");
            CompoundTag saved = new CompoundTag();
            boss.saveWithoutId(saved);
            RakanEntity restored = ModEntities.RAKAN.get().create(test.getLevel());
            test.assertTrue(restored != null, "Saved boss must be constructible");
            restored.load(saved);
            test.assertTrue(restored.isRoarUsed() && restored.isRoarEmpowered(), "Awakening must survive reload");
            test.assertTrue(Math.abs(restored.getAttributeValue(Attributes.ATTACK_DAMAGE) - attack*1.3) < 1e-6,
                    "Reload must not stack or lose the permanent buff");
            restored.discard();
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 160)
    public static void healthThresholdGuardIsQueuedOnce(GameTestHelper test) {
        RakanEntity boss = boss(test, 15, 14);
        IronGolem target = target(test, 15, 15.7);
        boss.getAttribute(Attributes.ARMOR).setBaseValue(0);
        boss.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);
        test.assertTrue(boss.forceAbility(RakanEntity.ACTION_ATTACK_COMBO, target), "Combo must start");
        test.runAfterDelay(5, () -> boss.hurt(test.getLevel().damageSources().generic(), 54));
        test.runAfterDelay(44, () -> {
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_GUARD, "A health threshold crossed during combo must queue a guard");
            test.assertTrue(boss.getNextGuardThresholdPercent() == 80, "90 percent threshold must be consumed once");
        });
        test.runAfterDelay(125, () -> {
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_NONE, "Queued guard must expire normally");
            boss.setHealth(boss.getMaxHealth());
            boss.hurt(test.getLevel().damageSources().generic(), 54);
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_NONE, "Healing must not rearm a consumed threshold");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 100)
    public static void pounceLandsAndDamagesOnce(GameTestHelper test) {
        RakanEntity boss = boss(test, 15, 8);
        IronGolem target = target(test, 15, 18);
        Vec3 start = boss.position();
        double[] maximumHeight = {start.y};
        int[] firstImpactTick = {-1};
        test.onEachTick(() -> {
            maximumHeight[0] = Math.max(maximumHeight[0], boss.getY());
            if (firstImpactTick[0] < 0 && target.getHealth() < 500) firstImpactTick[0] = boss.getActionTick();
        });
        test.assertTrue(boss.forceAbility(RakanEntity.ACTION_POUNCE, target), "Pounce must start");
        test.runAfterDelay(41, () -> {
            double travel = boss.position().subtract(start).horizontalDistance();
            test.assertTrue(maximumHeight[0] > start.y + 1, "Pounce must jump into the air rather than slide on the ground");
            test.assertTrue(travel >= 8.5 && travel <= 10.5, "Pounce must cover approximately 10 blocks, got " + travel);
            test.assertTrue(boss.onGround(), "Pounce must land");
            test.assertTrue(Math.abs(target.getHealth() - 488) < 0.001F, "Landing must deal exactly 12 damage once, HP=" + target.getHealth());
            test.assertTrue(firstImpactTick[0] >= 26 && firstImpactTick[0] <= 28,
                    "Flight must finish in about 14 ticks, at least three times faster than the previous 46; impact=" + firstImpactTick[0]);
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_NONE, "Fast pounce must recover after 1.9 seconds");
            test.succeed();
        });
    }
}
