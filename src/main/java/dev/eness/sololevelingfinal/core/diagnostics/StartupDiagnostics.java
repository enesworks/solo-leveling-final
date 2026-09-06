package dev.eness.sololevelingfinal.core.diagnostics;

import com.mojang.authlib.GameProfile;
import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.combat.GoGunheeCombatManager;
import dev.eness.sololevelingfinal.core.entity.IceMonarchEntity;
import dev.eness.sololevelingfinal.core.entity.MonarchOfGiantsEntity;
import dev.eness.sololevelingfinal.core.entity.TarnakEntity;
import dev.eness.sololevelingfinal.core.entity.RakanEntity;
import dev.eness.sololevelingfinal.core.registry.ModEntities;
import dev.eness.sololevelingfinal.core.story.LegiaGateSupport;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.eness.sololevelingfinal.core.dungeon.data.DungeonDataManager;
import dev.eness.sololevelingfinal.core.dungeon.DatapackDungeonGateHandler;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonRank;
import dev.eness.sololevelingfinal.core.entity.DatapackGateEntity;
import dev.eness.sololevelingfinal.core.entity.Portal1Entity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.PortalPerTickProcedure;
import dev.eness.sololevelingfinal.core.util.JobSkillManager;
import dev.eness.sololevelingfinal.core.util.SkillListHelper;
import dev.eness.sololevelingfinal.core.util.SungIlHwanCombatManager;
import dev.eness.sololevelingfinal.core.util.VesselManager;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Fails early when a mandatory private-build resource is absent. */
public final class StartupDiagnostics {
    private static final ResourceLocation LEGIA_DUNGEON = id(SoloLeveling3.MOD_ID, "legia_prison");
    private static final ResourceLocation LEGIA_POOL = id(SoloLeveling3.MOD_ID, "legia");
    private static final ResourceLocation ICE_POOL = id(SoloLeveling3.MOD_ID, "ice_monarch");
    private static final ResourceLocation LEGIA_STRUCTURE = id("solo_leveling", "monarch_of_giants_room");
    private static final TagKey<EntityType<?>> SLR_BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, id("minecraft", "soloboss"));
    private static final List<ResourceLocation> STORY_ADVANCEMENTS = List.of(
            id(SoloLeveling3.MOD_ID, "story/monarchs_awaken"),
            id(SoloLeveling3.MOD_ID, "story/double_dungeon_return"),
            id(SoloLeveling3.MOD_ID, "story/ice_monarch_retreat"),
            id(SoloLeveling3.MOD_ID, "story/three_monarchs"),
            id(SoloLeveling3.MOD_ID, "story/antares_final"),
            id(SoloLeveling3.MOD_ID, "story/ice_monarch_defeated"),
            id(SoloLeveling3.MOD_ID, "story/ancient_keys"),
            id(SoloLeveling3.MOD_ID, "story/monarch_of_beginning_defeated")
    );

    private StartupDiagnostics() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        Result result = verify(event.getServer());
        if (!result.success()) {
            for (String failure : result.failures()) {
                SoloLeveling3.LOGGER.error("Startup self-check failed: {}", failure);
            }
            throw new IllegalStateException("Solo Leveling 3 mandatory startup checks failed: " + String.join("; ", result.failures()));
        }
        SoloLeveling3.LOGGER.info("Solo Leveling 3 startup self-check passed ({} checks)", result.passed().size());
    }

    public static Result verify(MinecraftServer server) {
        ArrayList<String> passed = new ArrayList<>();
        ArrayList<String> failures = new ArrayList<>();

        check(server.getLevel(dev.eness.sololevelingfinal.core.campaign.CampaignDirector.ARENA) != null,
                "Campaign dimension", "sololeveling3:campaign_arena", passed, failures);
        for (String arena : List.of("three_monarchs_master_v1", "antares_final_master_v1")) {
            ResourceLocation resource = id(SoloLeveling3.MOD_ID, "schematics/" + arena + ".schem");
            check(server.getResourceManager().getResource(resource).isPresent(), "Campaign schematic", resource.toString(), passed, failures);
        }

        check(DungeonDataManager.dungeon(LEGIA_DUNGEON).isPresent(), "Legia dungeon data", LEGIA_DUNGEON.toString(), passed, failures);
        check(DungeonDataManager.mobPool(LEGIA_POOL).isPresent(), "Legia mob pool", LEGIA_POOL.toString(), passed, failures);
        check(DungeonDataManager.mobPool(ICE_POOL).isPresent(), "Ice Monarch mob pool", ICE_POOL.toString(), passed, failures);

        ServerLevel level = server.overworld();
        try {
            Optional<StructureTemplate> template = level.getStructureManager().get(LEGIA_STRUCTURE);
            boolean valid = template.filter(value -> value.getSize().getX() == 20
                    && value.getSize().getY() == 20
                    && value.getSize().getZ() == 30).isPresent();
            check(valid, "Legia structure", LEGIA_STRUCTURE + " (20x20x30)", passed, failures);
        } catch (RuntimeException exception) {
            failures.add("Legia structure " + LEGIA_STRUCTURE + " could not be loaded: " + exception.getMessage());
        }

        verifyEntity(level, ModEntities.ICE_MONARCH.get().create(level), IceMonarchEntity.class, "Ice Monarch entity", passed, failures);
        verifyEntity(level, ModEntities.MONARCH_OF_GIANTS.get().create(level), MonarchOfGiantsEntity.class, "Legia entity", passed, failures);
        verifyEntity(level, ModEntities.TARNAK.get().create(level), TarnakEntity.class, "Tarnak entity", passed, failures);
        verifyEntity(level, ModEntities.RAKAN.get().create(level), RakanEntity.class, "Rakan entity", passed, failures);
        verifyMaxHealth(level, ModEntities.ICE_MONARCH.get(), 360.0F, "Ice Monarch attributes", passed, failures);
        verifyMaxHealth(level, ModEntities.MONARCH_OF_GIANTS.get(), 420.0F, "Legia attributes", passed, failures);
        verifyMaxHealth(level, ModEntities.TARNAK.get(), 480.0F, "Tarnak attributes", passed, failures);
        verifyMaxHealth(level, ModEntities.RAKAN.get(), 520.0F, "Rakan attributes", passed, failures);
        check(
                SololevelingModEntities.BERU_BOSS.get().is(SLR_BOSS_TAG),
                "Legacy SLR boss tag",
                "minecraft:soloboss includes Beru",
                passed,
                failures
        );
        verifyLegiaState(level, passed, failures);
        verifyTarnakTargetLock(level, passed, failures);
        verifyLegiaGate(level, passed, failures);

        for (ResourceLocation id : STORY_ADVANCEMENTS) {
            Advancement advancement = server.getAdvancements().getAdvancement(id);
            check(advancement != null, "Story advancement", id.toString(), passed, failures);
        }

        for (String identity : List.of("christopher_reed", "sung_il_hwan", "go_gunhee")) {
            VesselManager.VesselDefinition definition = VesselManager.definition("ruler", identity);
            check(
                    definition != null && !VesselManager.isWorkInProgress(definition),
                    "Selectable vessel",
                    identity,
                    passed,
                    failures
            );
        }

        verifyCompletedVesselHooks(level, passed, failures);

        return new Result(List.copyOf(passed), List.copyOf(failures));
    }

    private static void verifyLegiaState(ServerLevel level, List<String> passed, List<String> failures) {
        MonarchOfGiantsEntity sealed = ModEntities.MONARCH_OF_GIANTS.get().create(level);
        MonarchOfGiantsEntity restored = ModEntities.MONARCH_OF_GIANTS.get().create(level);
        if (sealed == null || restored == null) {
            failures.add("Legia state test entities could not be instantiated");
            if (sealed != null) {
                sealed.discard();
            }
            if (restored != null) {
                restored.discard();
            }
            return;
        }

        try {
            check(sealed.isSealed() && sealed.insertedKeys() == 0, "Legia default seal", "sealed with 0/3 keys", passed, failures);
            float health = sealed.getHealth();
            boolean acceptedDamage = sealed.hurt(level.damageSources().generic(), 10.0F);
            check(!acceptedDamage && sealed.getHealth() == health, "Legia sealed invulnerability", "generic damage rejected", passed, failures);

            CompoundTag state = new CompoundTag();
            state.putBoolean("Sealed", false);
            state.putInt("InsertedKeys", 3);
            state.putBoolean("DialogShown", true);
            state.putInt("SkillCooldown", 65);
            restored.readAdditionalSaveData(state);
            check(!restored.isSealed() && restored.insertedKeys() == 3, "Legia seal persistence", "unsealed with 3/3 keys", passed, failures);
        } catch (RuntimeException exception) {
            failures.add("Legia state verification threw " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
        } finally {
            sealed.discard();
            restored.discard();
        }
    }

    private static void verifyLegiaGate(ServerLevel level, List<String> passed, List<String> failures) {
        Portal1Entity source = SololevelingModEntities.PORTAL_1.get().create(level);
        DatapackGateEntity replacement = SololevelingModEntities.DATAPACK_GATE.get().create(level);
        if (source == null || replacement == null) {
            failures.add("Legia gate test entities could not be instantiated");
            if (source != null) {
                source.discard();
            }
            if (replacement != null) {
                replacement.discard();
            }
            return;
        }

        UUID owner = UUID.fromString("663c5d2c-b5f0-453e-b217-45b21f82c396");
        try {
            source.getPersistentData().putDouble("tpx", 12345.0D);
            source.getPersistentData().putDouble("tpy", 84.0D);
            source.getPersistentData().putDouble("tpz", -23456.0D);
            boolean prepared = LegiaGateSupport.prepare(replacement, source, owner);
            check(prepared, "Legia gate binding", LEGIA_DUNGEON.toString(), passed, failures);
            check(
                    DatapackDungeonGateHandler.binding(replacement).filter(LEGIA_DUNGEON::equals).isPresent()
                            && DatapackDungeonGateHandler.rank(replacement).filter(rank -> rank == ProceduralDungeonRank.S).isPresent(),
                    "Legia gate route",
                    "S-rank " + LEGIA_DUNGEON,
                    passed,
                    failures
            );
            CompoundTag data = replacement.getPersistentData();
            check(
                    data.getDouble("tpx") == 12345.0D
                            && data.getDouble("tpy") == 84.0D
                            && data.getDouble("tpz") == -23456.0D,
                    "Legia gate destination transfer",
                    data.getDouble("tpx") + "," + data.getDouble("tpy") + "," + data.getDouble("tpz"),
                    passed,
                    failures
            );
            check(
                    LegiaGateSupport.isLegiaGate(replacement) && data.hasUUID(LegiaGateSupport.OWNER_TAG) && owner.equals(data.getUUID(LegiaGateSupport.OWNER_TAG)),
                    "Legia gate ownership",
                    owner.toString(),
                    passed,
                    failures
            );
            replacement.setTexture("gate_zero_purple");
            data.putDouble("PortalLife", 23999.0D);
            PortalPerTickProcedure.execute(level, 0.0D, 80.0D, 0.0D, replacement);
            check(
                    !replacement.isRemoved() && data.getDouble("PortalLife") == 0.0D && "portalgate2".equals(replacement.getTexture()),
                    "Legia gate lifetime hook",
                    "persistent blue story gate",
                    passed,
                    failures
            );
        } catch (RuntimeException exception) {
            failures.add("Legia gate verification threw " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
        } finally {
            source.discard();
            replacement.discard();
        }
    }

    private static void verifyTarnakTargetLock(ServerLevel level, List<String> passed, List<String> failures) {
        TarnakEntity tarnak = ModEntities.TARNAK.get().create(level);
        LivingEntity target = EntityType.ZOMBIE.create(level);
        if (tarnak == null || target == null) {
            failures.add("Tarnak target-lock test entities could not be instantiated");
            if (tarnak != null) {
                tarnak.discard();
            }
            if (target != null) {
                target.discard();
            }
            return;
        }

        try {
            boolean started = tarnak.forceAbility(TarnakEntity.ACTION_MONARCH_BREAKER, target);
            check(
                    started
                            && tarnak.getCombatAction() == TarnakEntity.ACTION_MONARCH_BREAKER
                            && target.getUUID().equals(tarnak.getLockedTargetId()),
                    "Tarnak Monarch Breaker lock",
                    "target UUID retained independently of collision",
                    passed,
                    failures
            );
            boolean cannonStarted = tarnak.forceAbility(TarnakEntity.ACTION_IRON_CANNON, target);
            check(
                    cannonStarted
                            && tarnak.getCombatAction() == TarnakEntity.ACTION_IRON_CANNON
                            && target.getUUID().equals(tarnak.getLockedTargetId()),
                    "Tarnak Iron Cannon lock",
                    "target UUID retained for guaranteed projectile tracking",
                    passed,
                    failures
            );

            tarnak.setTarget(target);
            tarnak.setHealth(tarnak.getMaxHealth());
            boolean thresholdDamageAccepted = tarnak.hurt(
                    level.damageSources().magic(),
                    tarnak.getMaxHealth() * 0.20F
            );
            check(
                    thresholdDamageAccepted
                            && tarnak.getNextGuardThresholdPercent() <= 80
                            && tarnak.hasPendingSteelGuard(),
                    "Tarnak Steel Guard threshold",
                    "accepted=" + thresholdDamageAccepted
                            + ", health=" + tarnak.getHealth()
                            + ", next=" + tarnak.getNextGuardThresholdPercent()
                            + ", queued=" + tarnak.hasPendingSteelGuard(),
                    passed,
                    failures
            );
        } catch (RuntimeException exception) {
            failures.add("Tarnak target-lock verification threw " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
        } finally {
            tarnak.discard();
            target.discard();
        }
    }

    private static void verifyCompletedVesselHooks(ServerLevel level, List<String> passed, List<String> failures) {
        ServerPlayer player = new ServerPlayer(
                level.getServer(),
                level,
                new GameProfile(UUID.fromString("28c780c5-008a-4ba5-9e04-d65217482c3e"), "SL3SelfTest")
        );
        Optional<SololevelingModVariables.PlayerVariables> optional = player
                .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY)
                .resolve();
        if (optional.isEmpty()) {
            failures.add("SLR player capability is unavailable on a detached server player");
            return;
        }

        SololevelingModVariables.PlayerVariables vars = optional.get();
        double oldJob = vars.JOB;
        double oldLevel = vars.Level;
        String oldType = vars.vesselType;
        String oldIdentity = vars.vesselIdentity;
        String oldList = vars.Plist;
        try {
            for (String identity : List.of("christopher_reed", "sung_il_hwan", "go_gunhee")) {
                VesselManager.VesselDefinition definition = VesselManager.definition("ruler", identity);
                check(
                        definition != null && VesselManager.isSelectableFor(player, definition),
                        "Vessel selection hook",
                        identity,
                        passed,
                        failures
                );
            }

            vars.JOB = 2.0D;
            vars.Level = 100.0D;
            vars.vesselType = "ruler";
            vars.vesselIdentity = "christopher_reed";
            List<String> christopherSkills = VesselProgressionManager.unlockedSkills(player, 2);
            check(
                    christopherSkills.equals(List.of("Fire Charge", "Meteor Rain", "Fireflies")),
                    "Christopher Reed skill path",
                    christopherSkills.toString(),
                    passed,
                    failures
            );

            vars.JOB = 7.0D;
            vars.Level = 100.0D;
            vars.vesselType = "ruler";
            vars.vesselIdentity = "sung_il_hwan";
            check(
                    SungIlHwanCombatManager.isSungIlHwanVessel(player),
                    "Sung Il-Hwan combat hook",
                    "developer gate removed",
                    passed,
                    failures
            );
            List<String> sungSkills = VesselProgressionManager.unlockedSkills(player, 7);
            check(
                    sungSkills.equals(List.of("Spiritualization", "Predator's Presence", "Assassin Stance", "Spatial Execution")),
                    "Sung Il-Hwan skill path",
                    sungSkills.toString(),
                    passed,
                    failures
            );

            vars.JOB = 8.0D;
            vars.vesselIdentity = "go_gunhee";
            vars.Level = 1.0D;
            check(
                    GoGunheeCombatManager.unlockedSkills(player).equals(List.of(GoGunheeCombatManager.BRILLIANT_REINFORCEMENT)),
                    "Go Gunhee level 1 unlock",
                    GoGunheeCombatManager.unlockedSkills(player).toString(),
                    passed,
                    failures
            );
            vars.Level = 55.0D;
            check(
                    GoGunheeCombatManager.unlockedSkills(player).equals(GoGunheeCombatManager.SKILLS.subList(0, 2)),
                    "Go Gunhee level 55 unlock",
                    GoGunheeCombatManager.unlockedSkills(player).toString(),
                    passed,
                    failures
            );
            vars.Level = 75.0D;
            check(
                    GoGunheeCombatManager.unlockedSkills(player).equals(GoGunheeCombatManager.SKILLS.subList(0, 3)),
                    "Go Gunhee level 75 unlock",
                    GoGunheeCombatManager.unlockedSkills(player).toString(),
                    passed,
                    failures
            );
            vars.Level = 95.0D;
            List<String> unlocked = VesselProgressionManager.unlockedSkills(player, 8);
            check(
                    unlocked.equals(GoGunheeCombatManager.SKILLS),
                    "Go Gunhee progression hook",
                    unlocked.toString(),
                    passed,
                    failures
            );
            check(
                    GoGunheeCombatManager.SKILLS.stream().allMatch(JobSkillManager::isJobSkill),
                    "Go Gunhee job-skill registration",
                    GoGunheeCombatManager.SKILLS.toString(),
                    passed,
                    failures
            );

            vars.Plist = String.join(",", GoGunheeCombatManager.SKILLS);
            check(
                    SkillListHelper.skills(player).containsAll(GoGunheeCombatManager.SKILLS),
                    "Go Gunhee equipment-list hook",
                    SkillListHelper.skills(player).toString(),
                    passed,
                    failures
            );
        } catch (RuntimeException exception) {
            failures.add("Completed vessel hook verification threw " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
        } finally {
            vars.JOB = oldJob;
            vars.Level = oldLevel;
            vars.vesselType = oldType;
            vars.vesselIdentity = oldIdentity;
            vars.Plist = oldList;
        }
    }

    private static void verifyEntity(
            ServerLevel level,
            Entity entity,
            Class<? extends Entity> expected,
            String label,
            List<String> passed,
            List<String> failures
    ) {
        if (entity == null) {
            failures.add(label + " could not be instantiated");
            return;
        }
        try {
            check(expected.isInstance(entity), label, expected.getSimpleName(), passed, failures);
        } finally {
            entity.discard();
        }
    }

    private static void verifyMaxHealth(
            ServerLevel level,
            EntityType<? extends LivingEntity> type,
            float expectedHealth,
            String label,
            List<String> passed,
            List<String> failures
    ) {
        LivingEntity entity = type.create(level);
        if (entity == null) {
            failures.add(label + " test entity could not be instantiated");
            return;
        }
        try {
            check(entity.getMaxHealth() == expectedHealth, label, expectedHealth + " health", passed, failures);
        } finally {
            entity.discard();
        }
    }

    private static void check(
            boolean condition,
            String label,
            String value,
            List<String> passed,
            List<String> failures
    ) {
        if (condition) {
            passed.add(label + ": " + value);
        } else {
            failures.add(label + " is missing or invalid: " + value);
        }
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public record Result(List<String> passed, List<String> failures) {
        public boolean success() {
            return failures.isEmpty();
        }
    }
}
