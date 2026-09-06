package dev.eness.sololevelingfinal.core.diagnostics;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.entity.RakanEntity;
import dev.eness.sololevelingfinal.core.registry.ModEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(SoloLeveling3.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RakanRoarGameTests {
    private static RakanEntity boss(GameTestHelper test, int x) {
        var boss = test.spawn(ModEntities.RAKAN.get(), new Vec3(x, 2, 8));
        boss.setNoAi(true);
        boss.getAttribute(Attributes.ARMOR).setBaseValue(0);
        boss.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);
        return boss;
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 95)
    public static void damageBelowHalfStartsRoarImmediately(GameTestHelper test) {
        var boss = boss(test, 15);
        boss.setHealth(boss.getMaxHealth() * .5F);
        test.runAfterDelay(2, () -> {
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_NONE, "Exactly 50 percent is not below half");
            test.assertTrue(boss.hurt(test.getLevel().damageSources().generic(), 1), "Threshold damage must apply");
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_ROAR, "Crossing below half must start roar in the damage call");
            test.assertFalse(boss.hurt(test.getLevel().damageSources().generic(), 100), "A second hit in the same tick must meet roar immunity");
        });
        test.runAfterDelay(85, () -> {
            test.assertTrue(boss.isRoarUsed() && boss.isRoarEmpowered(), "Natural roar must complete and empower");
            boss.setHealth(boss.getMaxHealth());
            boss.setHealth(boss.getMaxHealth() * .49F);
        });
        test.runAfterDelay(88, () -> {
            test.assertTrue(boss.getCombatAction() != RakanEntity.ACTION_ROAR, "Healing and recrossing must not repeat a completed natural roar");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 35)
    public static void roarInterruptsComboGuardAndAirbornePounce(GameTestHelper test) {
        var combo = boss(test, 6);
        var guard = boss(test, 15);
        var pounce = boss(test, 24);
        var target = test.spawn(EntityType.IRON_GOLEM, new Vec3(24, 2, 18));
        target.setNoAi(true);
        target.setInvulnerable(true);
        for (var boss : new RakanEntity[]{combo, guard, pounce}) boss.setHealth(boss.getMaxHealth() * .51F);
        test.assertTrue(combo.forceAbility(RakanEntity.ACTION_ATTACK_COMBO, target), "Combo must start");
        test.assertTrue(guard.forceAbility(RakanEntity.ACTION_GUARD, target), "Guard must start");
        test.assertTrue(pounce.forceAbility(RakanEntity.ACTION_POUNCE, target), "Pounce must start");
        test.runAfterDelay(17, () -> {
            test.assertFalse(pounce.onGround(), "Pounce interruption must be tested in the air");
            combo.hurt(test.getLevel().damageSources().generic(), 20);
            guard.hurt(test.getLevel().damageSources().generic(), 2000); // 1 percent passes the guard.
            pounce.hurt(test.getLevel().damageSources().generic(), 20);
            for (var boss : new RakanEntity[]{combo, guard, pounce}) {
                test.assertTrue(boss.getHealth() < boss.getMaxHealth() * .5F, "Test hit must cross half health");
                test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_ROAR, "Roar must preempt every active ability");
                test.assertTrue(boss.getGuardCounterTick() == 0 && boss.getGuardHitTick() == 0, "Guard subclips must not override the roar");
            }
        });
        test.runAfterDelay(23, () -> {
            for (var boss : new RakanEntity[]{combo, guard, pounce}) {
                test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_ROAR && boss.getActionTick() > 0,
                        "Queued guards must not replace or restart the active roar");
            }
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 180)
    public static void previewDoesNotConsumeNaturalRoar(GameTestHelper test) {
        var boss = boss(test, 15);
        double attack = boss.getAttributeValue(Attributes.ATTACK_DAMAGE);
        test.assertTrue(boss.previewAbility(RakanEntity.ACTION_ROAR, null), "Full-health preview must start");
        test.runAfterDelay(83, () -> {
            test.assertFalse(boss.isRoarUsed(), "Preview must not consume the health phase");
            test.assertFalse(boss.isRoarEmpowered(), "Preview must not permanently buff the combat boss");
            boss.setHealth(boss.getMaxHealth() * .5F);
            boss.hurt(test.getLevel().damageSources().generic(), 1);
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_ROAR, "Natural roar must still trigger after a preview");
        });
        test.runAfterDelay(166, () -> {
            test.assertTrue(Math.abs(boss.getAttributeValue(Attributes.ATTACK_DAMAGE) - attack * 1.3) < 1e-6,
                    "Natural roar after preview must buff exactly once");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 100)
    public static void oldPreviewSaveCannotSuppressHealthRoar(GameTestHelper test) {
        var original = boss(test, 15);
        double attack = original.getAttributeValue(Attributes.ATTACK_DAMAGE);
        CompoundTag saved = new CompoundTag();
        original.saveWithoutId(saved);
        saved.putFloat("Health", original.getMaxHealth() * .49F);
        saved.putBoolean("RoarUsed", true);
        saved.putBoolean("RoarEmpowered", true);
        saved.remove("HealthRoarCompleted"); // Old saves could mark a full-health preview as consumed.
        original.discard();
        var restored = ModEntities.RAKAN.get().create(test.getLevel());
        restored.load(saved);
        test.getLevel().addFreshEntity(restored);
        test.runAfterDelay(3, () -> test.assertTrue(restored.getCombatAction() == RakanEntity.ACTION_ROAR,
                "Legacy preview flag must not suppress the health transition"));
        test.runAfterDelay(86, () -> {
            test.assertTrue(restored.isRoarUsed(), "Migrated health phase must complete");
            test.assertTrue(Math.abs(restored.getAttributeValue(Attributes.ATTACK_DAMAGE) - attack * 1.3) < 1e-6,
                    "Migration must not stack the old permanent buff");
            test.succeed();
        });
    }

    @GameTest(template = "rakan_test_arena", timeoutTicks = 115)
    public static void unfinishedNaturalRoarRestartsAfterLoad(GameTestHelper test) {
        var boss = boss(test, 15);
        boss.setHealth(boss.getMaxHealth() * .49F);
        RakanEntity[] loaded = {null};
        test.runAfterDelay(20, () -> {
            test.assertTrue(boss.getCombatAction() == RakanEntity.ACTION_ROAR, "Health change without hurt must also start roar");
            CompoundTag saved = new CompoundTag();
            boss.saveWithoutId(saved);
            boss.discard();
            loaded[0] = ModEntities.RAKAN.get().create(test.getLevel());
            loaded[0].load(saved);
            test.getLevel().addFreshEntity(loaded[0]);
        });
        test.runAfterDelay(24, () -> test.assertTrue(loaded[0].getCombatAction() == RakanEntity.ACTION_ROAR,
                "Saving mid-roar must not mark the unfinished phase completed"));
        test.runAfterDelay(105, () -> {
            test.assertTrue(loaded[0].isRoarUsed() && loaded[0].isRoarEmpowered(), "Reloaded roar must finish normally");
            test.succeed();
        });
    }
}
