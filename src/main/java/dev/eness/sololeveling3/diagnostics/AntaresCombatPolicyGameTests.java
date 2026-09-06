package dev.eness.sololeveling3.diagnostics;

import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.entity.AntaresEntity;
import dev.eness.sololeveling3.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(SoloLeveling3.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AntaresCombatPolicyGameTests {
    private static AntaresEntity boss(GameTestHelper test, double x, double z) {
        AntaresEntity boss = test.spawn(ModEntities.ANTARES.get(), new Vec3(x, 2, z));
        boss.setNoAi(true);
        boss.setYRot(0);
        boss.setYBodyRot(0);
        boss.setYHeadRot(0);
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

    @GameTest(template = "rakan_test_arena", timeoutTicks = 400)
    public static void naturalPolicyUsesThreeCombosThenOneSweepAndKeepsCountThroughRay(GameTestHelper test) {
        AntaresEntity boss = boss(test, 15, 8);
        IronGolem close = target(test, 15, 10.8);
        IronGolem far = target(test, 15, 18);
        List<Integer> choices = new ArrayList<>();

        test.runAfterDelay(22, () -> {
            choices.add(boss.chooseNextAbility(close));
            test.assertTrue(boss.forceAbility(AntaresEntity.COMBO, close), "First basic combo must start");
        });
        test.runAfterDelay(102, () -> {
            test.assertTrue(boss.getCompletedCombos() == 1, "One completed combo must be counted");
            choices.add(boss.chooseNextAbility(close));
            test.assertTrue(boss.forceAbility(AntaresEntity.COMBO, close), "Second basic combo must start");
        });
        test.runAfterDelay(164, () -> {
            test.assertTrue(boss.getCompletedCombos() == 2, "Two completed combos must be counted");
            test.assertTrue(boss.forceAbility(AntaresEntity.RAY, far), "A ray interruption must be allowed in the test");
        });
        test.runAfterDelay(256, () -> {
            test.assertTrue(boss.getCompletedCombos() == 2, "Ray must not clear the basic-combo counter");
            choices.add(boss.chooseNextAbility(close));
            test.assertTrue(boss.forceAbility(AntaresEntity.COMBO, close), "Third basic combo must start after ray");
        });
        test.runAfterDelay(318, () -> {
            test.assertTrue(boss.getCompletedCombos() == 3, "Third completed combo must arm sweep");
            choices.add(boss.chooseNextAbility(close));
            test.assertTrue(boss.forceAbility(AntaresEntity.SWEEP, far), "Every third basic combo must be followed by sweep");
        });
        test.runAfterDelay(372, () -> {
            choices.add(boss.chooseNextAbility(close));
            test.assertTrue(
                    choices.equals(List.of(
                            AntaresEntity.COMBO,
                            AntaresEntity.COMBO,
                            AntaresEntity.COMBO,
                            AntaresEntity.SWEEP,
                            AntaresEntity.COMBO)),
                    "Natural choices must repeat as 1,1,1,2; got " + choices);
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 340)
    public static void guardCoalescesTenPercentThresholdsAndSurvivesReload(GameTestHelper test) {
        AntaresEntity boss = boss(test, 15, 12);
        IronGolem close = target(test, 15, 14.8);
        boss.getAttribute(Attributes.ARMOR).setBaseValue(0);
        boss.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);

        test.assertTrue(boss.forceAbility(AntaresEntity.COMBO, close), "Active combo must not be interrupted by health guard");
        test.runAfterDelay(5, () -> {
            float max = boss.getMaxHealth();
            boss.hurt(test.getLevel().damageSources().generic(), max * 0.25F);
            test.assertTrue(boss.getCombatAction() == AntaresEntity.COMBO, "Health guard must queue during an active action");
            test.assertTrue(boss.getPendingGuards() == 1, "One heavy hit must coalesce crossed thresholds into one pending guard");
            test.assertTrue(boss.getConsumedHealthThresholds() == 2, "Crossing 90 and 80 percent must advance the consumed threshold index");
        });
        test.runAfterDelay(63, () -> {
            test.assertTrue(boss.chooseNextAbility(close) == AntaresEntity.GUARD, "Queued guard must have priority after combo");
            test.assertTrue(boss.forceAbility(AntaresEntity.GUARD, close), "Start reserved guard under NoAI test control");
            test.assertTrue(boss.getPendingGuards() == 0, "Coalesced guard must be consumed on start");
            boss.invulnerableTime = 0;
            boss.hurt(test.getLevel().damageSources().generic(), boss.getMaxHealth() * 6F);
            test.assertTrue(boss.getConsumedHealthThresholds() >= 3, "Thresholds crossed during guard must still be consumed");
            test.assertTrue(boss.getPendingGuards() == 0, "Damage during guard must not queue a follow-up guard");
        });
        test.runAfterDelay(150, () -> {
            int consumed = boss.getConsumedHealthThresholds();
            boss.setHealth(boss.getMaxHealth());
            boss.invulnerableTime = 0;
            boss.hurt(test.getLevel().damageSources().generic(), boss.getMaxHealth() * 0.15F);
            test.assertTrue(boss.getPendingGuards() == 0, "Healing cannot farm already consumed thresholds");

            CompoundTag saved = new CompoundTag();
            boss.saveWithoutId(saved);
            AntaresEntity restored = ModEntities.ANTARES.get().create(test.getLevel());
            test.assertTrue(restored != null, "Saved Antares must be constructible");
            restored.load(saved);
            test.assertTrue(restored.getConsumedHealthThresholds() == consumed, "Consumed health threshold index must survive reload");
            restored.setHealth(restored.getMaxHealth());
            restored.invulnerableTime = 0;
            restored.hurt(test.getLevel().damageSources().generic(), restored.getMaxHealth() * 0.15F);
            test.assertTrue(restored.getPendingGuards() == 0, "Reload cannot rearm consumed thresholds");
            restored.discard();
        });
        test.runAfterDelay(170, () -> {
            boss.invulnerableTime=0;
            boss.hurt(test.getLevel().damageSources().generic(), boss.getMaxHealth()*.30F);
            test.assertTrue(boss.getPendingGuards()==1,"A newly crossed threshold during cooldown reserves only one guard");
            test.assertTrue(boss.chooseNextAbility(close)!=AntaresEntity.GUARD,"Guard cannot spam during its twelve-second cooldown");
        });
        test.runAfterDelay(305, () -> {
            test.assertTrue(boss.chooseNextAbility(close)==AntaresEntity.GUARD,"Reserved guard becomes eligible after twelve seconds");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 105)
    public static void rayContinuouslyTracksOriginalTargetAndGuaranteesLockedDamage(GameTestHelper test) {
        AntaresEntity boss = boss(test, 15, 5);
        IronGolem locked = target(test, 15, 13);
        IronGolem blockedBystander = target(test, 15, 20);
        for (int x = 11; x <= 19; x++) {
            for (int y = 1; y <= 7; y++) {
                test.setBlock(new BlockPos(x, y, 16), Blocks.STONE);
            }
        }

        test.assertTrue(boss.forceAbility(AntaresEntity.RAY, locked), "Ray must acquire the original target");
        test.onEachTick(() -> locked.invulnerableTime = 20);
        test.runAfterDelay(55, () -> {
            boss.setTarget(blockedBystander);
            Vec3 moved = test.absoluteVec(new Vec3(25, 6, 29));
            locked.teleportTo(moved.x, moved.y, moved.z);
        });
        for (int tick : new int[]{66, 71, 76, 81}) {
            test.runAfterDelay(tick, () -> {
                Vec3 expected = locked.getBoundingBox().getCenter().subtract(boss.rayOrigin(boss.getActionTick())).normalize();
                test.assertTrue(boss.getAim().dot(expected) > 0.995, "Ray aim must keep tracking the moved original target");
            });
        }
        test.runAfterDelay(92, () -> {
            test.assertTrue(Math.abs(locked.getHealth() - 438) < 0.01,
                    "Locked ray target must receive four 11-damage pulses and one 18-damage burst; HP=" + locked.getHealth());
            test.assertTrue(blockedBystander.getHealth() == 500,
                    "A non-locked bystander behind a wall must still require beam geometry and line of sight");
            test.succeed();
        });
    }
}
