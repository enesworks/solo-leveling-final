package dev.eness.sololevelingfinal.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.eness.sololevelingfinal.core.SoloLeveling3;
import dev.eness.sololevelingfinal.core.diagnostics.StartupDiagnostics;
import dev.eness.sololevelingfinal.core.entity.TarnakEntity;
import dev.eness.sololevelingfinal.core.entity.RakanEntity;
import dev.eness.sololevelingfinal.core.entity.AntaresEntity;
import dev.eness.sololevelingfinal.core.registry.ModEntities;
import dev.eness.sololevelingfinal.core.registry.ModItems;
import dev.eness.sololevelingfinal.core.story.StoryProgression;
import dev.eness.sololevelingfinal.core.campaign.CampaignDirector;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonInstanceSavedData;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;
import dev.eness.sololevelingfinal.core.util.VesselManager;

import java.util.UUID;
import java.util.Comparator;

/** Operator-only helpers for the owner's final in-game acceptance test. */
public final class DevCommands {
    private DevCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sl3dev")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("verify").executes(context -> verify(context.getSource())))
                        .then(Commands.literal("status").executes(context -> status(context.getSource())))
                        .then(Commands.literal("encounters").executes(context -> encounters(context.getSource())))
                        .then(Commands.literal("legacy_stage")
                                .then(Commands.argument("value", IntegerArgumentType.integer(StoryProgression.LOCKED, StoryProgression.COMPLETE))
                                        .executes(context -> stage(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "value")
                                        ))))
                        .then(Commands.literal("give_keys").executes(context -> giveKeys(context.getSource())))
                        .then(Commands.literal("spawn")
                                .then(Commands.literal("ice_monarch")
                                        .executes(context -> spawnBoss(
                                                context.getSource(),
                                                ModEntities.ICE_MONARCH.get(),
                                                "Ice Monarch"
                                        )))
                                .then(Commands.literal("legia")
                                        .executes(context -> spawnBoss(
                                                context.getSource(),
                                                ModEntities.MONARCH_OF_GIANTS.get(),
                                                "Legia"
                                        )))
                                .then(Commands.literal("tarnak")
                                        .executes(context -> spawnBoss(
                                                context.getSource(),
                                                ModEntities.TARNAK.get(),
                                                "Tarnak"
                                        ))))
                        .then(Commands.literal("tarnak")
                                .then(Commands.literal("steel_guard")
                                        .executes(context -> forceTarnakAbility(
                                                context.getSource(),
                                                TarnakEntity.ACTION_STEEL_GUARD,
                                                "Iron Body: Steel Guard"
                                        )))
                                .then(Commands.literal("monarch_breaker")
                                        .executes(context -> forceTarnakAbility(
                                                context.getSource(),
                                                TarnakEntity.ACTION_MONARCH_BREAKER,
                                                "Monarch Breaker"
                                        )))
                                .then(Commands.literal("maximum_output")
                                        .executes(context -> forceTarnakAbility(
                                                context.getSource(),
                                                TarnakEntity.ACTION_MAXIMUM_OUTPUT,
                                                "Iron Body: Maximum Output"
                                        )))
                                .then(Commands.literal("iron_cannon")
                                        .executes(context -> forceTarnakAbility(
                                                context.getSource(),
                                                TarnakEntity.ACTION_IRON_CANNON,
                                                "Iron Cannon"
                                        ))))
                        .then(Commands.literal("spawn")
                                .then(Commands.literal("rakan")
                                        .executes(context -> spawnBoss(context.getSource(), ModEntities.RAKAN.get(), "Rakan"))))
                        .then(Commands.literal("rakan")
                                .executes(context -> rakanTestMenu(context.getSource()))
                                .then(Commands.literal("combo").executes(context -> forceRakanAbility(context.getSource(), RakanEntity.ACTION_ATTACK_COMBO)))
                                .then(Commands.literal("guard").executes(context -> forceRakanAbility(context.getSource(), RakanEntity.ACTION_GUARD)))
                                .then(Commands.literal("pounce").executes(context -> forceRakanAbility(context.getSource(), RakanEntity.ACTION_POUNCE)))
                                .then(Commands.literal("roar").executes(context -> forceRakanAbility(context.getSource(), RakanEntity.ACTION_ROAR))))
                        .then(Commands.literal("spawn").then(Commands.literal("antares")
                                .executes(context -> spawnBoss(context.getSource(), ModEntities.ANTARES.get(), "Antares"))))
                        .then(Commands.literal("antares")
                                .executes(context -> antaresMenu(context.getSource()))
                                .then(Commands.literal("combo").executes(context -> antaresAbility(context.getSource(),AntaresEntity.COMBO)))
                                .then(Commands.literal("sweep").executes(context -> antaresAbility(context.getSource(),AntaresEntity.SWEEP)))
                                .then(Commands.literal("guard").executes(context -> antaresAbility(context.getSource(),AntaresEntity.GUARD)))
                                .then(Commands.literal("ray").executes(context -> antaresAbility(context.getSource(),AntaresEntity.RAY))))
                        .then(Commands.literal("vessel_screen")
                                .executes(context -> openVesselScreen(context.getSource())))
                        .then(Commands.literal("vessel")
                                .then(Commands.literal("christopher_reed")
                                        .executes(context -> assignVessel(context.getSource(), "christopher_reed")))
                                .then(Commands.literal("sung_il_hwan")
                                        .executes(context -> assignVessel(context.getSource(), "sung_il_hwan")))
                                .then(Commands.literal("go_gunhee")
                                        .executes(context -> assignVessel(context.getSource(), "go_gunhee"))))
        );
    }

    private static int verify(CommandSourceStack source) {
        StartupDiagnostics.Result result = StartupDiagnostics.verify(source.getServer());
        if (!result.success()) {
            result.failures().forEach(failure -> source.sendFailure(
                    Component.literal("[SL3] " + failure).withStyle(ChatFormatting.RED)
            ));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("[SL3] " + result.passed().size() + " automatic checks passed.")
                        .withStyle(ChatFormatting.GREEN),
                false
        );
        return result.passed().size();
    }

    private static int status(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int stage = CampaignDirector.stage(player).ordinal();
        int keys = player.getInventory().countItem(ModItems.OLD_KEY.get());
        String instance = player.getPersistentData().getString("slr_dungeon_instance");
        String vessel = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY)
                .map(vars -> vars.vesselType + ":" + vars.vesselIdentity + " (job " + (int) vars.JOB + ")")
                .orElse("unavailable");
        source.sendSuccess(
                () -> Component.literal("[Solo Leveling Final] " + CampaignDirector.status(player)
                        + ", keys=" + keys
                        + ", vessel=" + vessel
                        + ", dungeon=" + (instance.isBlank() ? "none" : instance)),
                false
        );
        return stage;
    }

    private static int encounters(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String rawId = player.getPersistentData().getString("slr_dungeon_instance");
        if (rawId.isBlank()) {
            source.sendFailure(Component.literal("[SL3] Player is not inside an SLR dungeon instance."));
            return 0;
        }

        UUID instanceId;
        try {
            instanceId = UUID.fromString(rawId);
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("[SL3] Invalid dungeon instance id: " + rawId));
            return 0;
        }

        DungeonInstanceSavedData.Instance instance = DungeonInstanceSavedData.get(source.getServer())
                .getInstance(instanceId)
                .orElse(null);
        if (instance == null) {
            source.sendFailure(Component.literal("[SL3] Dungeon instance is no longer registered: " + rawId));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("[SL3] " + instance.dungeonId() + " encounters:"),
                false
        );
        for (DungeonInstanceSavedData.EncounterState encounter : instance.encounters()) {
            source.sendSuccess(
                    () -> Component.literal(" - " + encounter.key()
                            + " sequence=" + encounter.sequenceKey() + ":" + encounter.sequenceOrder()
                            + " active=" + encounter.activated()
                            + " complete=" + encounter.completed()
                            + " mobs=" + encounter.trackedMobs().size()),
                    false
            );
        }
        return instance.encounters().size();
    }

    private static int stage(CommandSourceStack source, int value) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        player.getPersistentData().putInt(StoryProgression.STAGE_TAG, value);
        player.getPersistentData().remove("sl3_baruka_defeated_instance");
        player.getPersistentData().remove("sl3_quest_overlay_sent");
        source.sendSuccess(
                () -> Component.literal("[SL3] Legacy stage saved: " + stageName(value) + ". Yeni ana hikâye bu değeri kullanmaz.")
                        .withStyle(ChatFormatting.YELLOW),
                false
        );
        return value;
    }

    private static int giveKeys(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int current = player.getInventory().countItem(ModItems.OLD_KEY.get());
        int missing = Math.max(0, 3 - current);
        if (missing == 0) {
            source.sendSuccess(() -> Component.literal("[SL3] Player already has all three Ancient Keys."), false);
            return 0;
        }

        ItemStack keys = new ItemStack(ModItems.OLD_KEY.get(), missing);
        if (!player.addItem(keys)) {
            player.drop(keys, false);
        }
        int total = player.getInventory().countItem(ModItems.OLD_KEY.get());
        source.sendSuccess(
                () -> Component.literal("[SL3] Ancient Keys: " + total + "/3.").withStyle(ChatFormatting.GOLD),
                false
        );
        return missing;
    }

    private static int antaresAbility(CommandSourceStack source,int action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player=source.getPlayerOrException();
        var boss=player.serverLevel().getEntitiesOfClass(AntaresEntity.class,player.getBoundingBox().inflate(64),AntaresEntity::isAlive)
                .stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
        if(boss==null){source.sendFailure(Component.literal("[Antares] Önce /sl3dev spawn antares"));return 0;}
        if(!boss.previewAbility(action,player)){source.sendFailure(Component.literal("[Antares] Mevcut hareketin bitmesini bekle."));return 0;}
        source.sendSuccess(()->Component.literal("[Antares] "+AntaresEntity.clipName(action)+" · "+AntaresEntity.duration(action)/20.+" sn"
                +(action==AntaresEntity.GUARD?" · Karşı patlamayı görmek için yakından vur.":"")).withStyle(ChatFormatting.RED),false);return 1;
    }
    private static int antaresMenu(CommandSourceStack source){
        String[][] rows={{"Antares çağır","spawn antares"},{"1 · Sağ pençe → sol pençe → avuç · 1,5 sn","antares combo"},
                {"2 · Kızıl enerji yayı · 1,5 sn","antares sweep"},{"3 · Savunma · 4 sn","antares guard"},{"4 · Yıkım ışını · 4,5 sn","antares ray"}};
        for(String[] row:rows)source.sendSuccess(()->Component.literal("[ "+row[0]+" ]").withStyle(style->style.withColor(ChatFormatting.RED)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/sl3dev "+row[1]))),false);
        return 1;
    }

    private static int spawnBoss(CommandSourceStack source, EntityType<? extends Mob> type, String label)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        Mob boss = type.create(level);
        if (boss == null) {
            source.sendFailure(Component.literal("[SL3] Could not create " + label + "."));
            return 0;
        }

        Vec3 direction = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 0.001D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            direction = direction.normalize();
        }
        Vec3 spawn = player.position().add(direction.scale(4.0D));
        boss.moveTo(spawn.x, spawn.y, spawn.z, player.getYRot() + 180.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.COMMAND, null, null);
        boss.setPersistenceRequired();
        if (!level.addFreshEntity(boss)) {
            boss.discard();
            source.sendFailure(Component.literal("[SL3] Could not add " + label + " to the world."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("[SL3] Spawned " + label + ".").withStyle(ChatFormatting.LIGHT_PURPLE),
                false
        );
        return 1;
    }

    private static int openVesselScreen(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        JobChangeQuestManager.openSelectionFromCommand(player);
        source.sendSuccess(() -> Component.literal("[SL3] Opened SLR vessel selection."), false);
        return 1;
    }

    private static int forceTarnakAbility(CommandSourceStack source, int action, String label)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TarnakEntity tarnak = player.serverLevel().getEntitiesOfClass(
                        TarnakEntity.class,
                        player.getBoundingBox().inflate(64.0D),
                        TarnakEntity::isAlive
                ).stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        if (tarnak == null) {
            source.sendFailure(Component.literal("[SL3] No living Tarnak found within 64 blocks."));
            return 0;
        }

        tarnak.setTarget(player);
        if (!tarnak.forceAbility(action, player)) {
            source.sendFailure(Component.literal("[SL3] Could not start " + label + "."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("[SL3] Tarnak: " + label + " -> " + player.getGameProfile().getName())
                        .withStyle(ChatFormatting.RED),
                false
        );
        return 1;
    }

    private static int forceRakanAbility(CommandSourceStack source, int action)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        RakanEntity rakan = player.serverLevel().getEntitiesOfClass(RakanEntity.class,
                        player.getBoundingBox().inflate(64.0D), RakanEntity::isAlive)
                .stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
        if (rakan == null) {
            source.sendFailure(Component.literal("[SL3] No living Rakan within 64 blocks. Use /sl3dev spawn rakan."));
            return 0;
        }
        if (!rakan.previewAbility(action, player)) {
            source.sendFailure(Component.literal("[Rakan] Önce mevcut animasyonun bitmesini bekle. Seyirci modunda hedef alınamazsın."));
            return 0;
        }
        String label = switch (action) {
            case RakanEntity.ACTION_ATTACK_COMBO -> "1 — Pençe kombosu (2 sn)";
            case RakanEntity.ACTION_GUARD -> "2 — Savunma, blok ve karşı vuruş (4 sn)";
            case RakanEntity.ACTION_POUNCE -> "3 — Hedefe sıçrama (1,9 sn; en fazla 12 blok)";
            case RakanEntity.ACTION_ROAR -> "4 — Kükreme (4 sn)";
            default -> "Test";
        };
        source.sendSuccess(() -> Component.literal("[Rakan] " + label).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int rakanTestMenu(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Rakan testi — yaratıcı modda güvenle izleyebilirsin; bir satıra tıkla:")
                .withStyle(ChatFormatting.GOLD), false);
        String[][] commands = {
                {"Rakan çağır", "/sl3dev spawn rakan"},
                {"1 · Pençe kombosu", "/sl3dev rakan combo"},
                {"2 · Savunma + blok + karşı vuruş", "/sl3dev rakan guard"},
                {"3 · Oyuncuya sıçrama · en fazla 12 blok", "/sl3dev rakan pounce"},
                {"4 · Kükreme · tekrar oynatılabilir", "/sl3dev rakan roar"}
        };
        for (String[] row : commands) {
            source.sendSuccess(() -> Component.literal("[ " + row[0] + " ]").withStyle(style -> style
                    .withColor(ChatFormatting.AQUA)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, row[1]))), false);
        }
        return 1;
    }

    private static int assignVessel(CommandSourceStack source, String identity)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        VesselManager.AssignmentResult result = VesselManager.assignPlayer(player, "ruler", identity, false);
        if (result != VesselManager.AssignmentResult.SUCCESS) {
            source.sendFailure(Component.literal("[SL3] Vessel assignment failed: " + result));
            return 0;
        }

        JobChangeQuestManager.finish(player);
        source.sendSuccess(
                () -> Component.literal("[SL3] Assigned vessel " + identity + ".").withStyle(ChatFormatting.AQUA),
                false
        );
        return 1;
    }

    private static String stageName(int stage) {
        return switch (stage) {
            case StoryProgression.LOCKED -> "locked";
            case StoryProgression.ICE_MONARCH -> "ice_monarch";
            case StoryProgression.ANCIENT_KEYS -> "ancient_keys";
            case StoryProgression.LEGIA_READY -> "legia_ready";
            case StoryProgression.LEGIA_DUNGEON -> "legia_dungeon";
            case StoryProgression.COMPLETE -> "complete";
            default -> "unknown";
        };
    }
}
