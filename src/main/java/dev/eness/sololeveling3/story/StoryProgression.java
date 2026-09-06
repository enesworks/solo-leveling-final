package dev.eness.sololeveling3.story;

import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.entity.IceMonarchEntity;
import dev.eness.sololeveling3.entity.MonarchOfGiantsEntity;
import dev.eness.sololeveling3.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.solocraft.SololevelingMod;
import net.solocraft.entity.BarukaEntity;
import net.solocraft.entity.DatapackGateEntity;
import net.solocraft.entity.Portal1Entity;
import net.solocraft.entity.RedGateEntity;
import net.solocraft.init.SololevelingModEntities;
import net.solocraft.network.SololevelingModVariables;
import net.solocraft.network.UrgentQuestStatusMessage;
import net.solocraft.procedures.ShadowKillCreditHelper;
import net.solocraft.util.SystemNotifications;
import net.solocraft.util.UrgentQuestManager;

import java.util.List;
import java.util.Locale;

public final class StoryProgression {
    public static final String STAGE_TAG = "sl3_story_stage";

    public static final int LOCKED = 0;
    public static final int ICE_MONARCH = 1;
    public static final int ANCIENT_KEYS = 2;
    public static final int LEGIA_READY = 3;
    public static final int LEGIA_DUNGEON = 4;
    public static final int COMPLETE = 5;

    private static final String BARUKA_INSTANCE_TAG = "sl3_baruka_defeated_instance";
    private static final String OVERLAY_SENT_TAG = "sl3_quest_overlay_sent";
    private static final ResourceLocation LEGIA_DUNGEON_ID = LegiaGateSupport.DUNGEON_ID;
    private static final ResourceLocation STORY_ROOT = ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "story/monarchs_awaken");
    private static final ResourceLocation ICE_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "story/ice_monarch_defeated");
    private static final ResourceLocation KEYS_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "story/ancient_keys");
    private static final ResourceLocation LEGIA_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "story/monarch_of_beginning_defeated");
    private static final ResourceLocation A_RANK_DIMENSION = ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_a");
    private static final ResourceLocation S_RANK_DIMENSION = ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_s");
    private static final TagKey<EntityType<?>> SLR_BOSS_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("minecraft", "soloboss")
    );

    private StoryProgression() {
    }

    public static int stage(ServerPlayer player) {
        return player.getPersistentData().getInt(STAGE_TAG);
    }

    public static boolean isAwaitingIceMonarch(ServerPlayer player) {
        return stage(player) == ICE_MONARCH;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }

        int current = stage(player);
        if (current == LOCKED && hasCompletedDemonCastle(player)) {
            setStage(player, ICE_MONARCH);
            award(player, STORY_ROOT);
            notifyQuest(player, "story.sololeveling3.ice.title", "story.sololeveling3.ice.objective", 0xFF70D7FF);
            current = ICE_MONARCH;
        }

        if (current == ICE_MONARCH) {
            markNearbyRedGatesAsFrost(player);
        } else if (current == ANCIENT_KEYS && countKeys(player) >= 3) {
            setStage(player, LEGIA_READY);
            award(player, KEYS_ADVANCEMENT);
            notifyQuest(player, "story.sololeveling3.legia_gate.title", "story.sololeveling3.legia_gate.objective", 0xFFC795FF);
        }

        syncEarnedAdvancements(player);
        syncSlrQuestOverlay(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Entity target = event.getTarget();
        if (stage(player) == ICE_MONARCH) {
            markRedGateAsFrost(target);
            return;
        }

        if (stage(player) != LEGIA_READY && stage(player) != LEGIA_DUNGEON) {
            return;
        }
        if (!isSuitableBlueGate(target)) {
            return;
        }

        if (countKeys(player) < 3) {
            player.displayClientMessage(Component.translatable("message.sololeveling3.need_three_keys").withStyle(ChatFormatting.RED), true);
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        DatapackGateEntity replacement = SololevelingModEntities.DATAPACK_GATE.get().create(level);
        if (replacement == null) {
            gateFailure(player, "message.sololeveling3.gate_create_failed");
            return;
        }

        replacement.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
        replacement.setYHeadRot(target.getYHeadRot());
        replacement.setPersistenceRequired();
        if (!LegiaGateSupport.prepare(replacement, target, player.getUUID())) {
            replacement.discard();
            gateFailure(player, "message.sololeveling3.gate_bind_failed");
            return;
        }
        if (!level.addFreshEntity(replacement)) {
            replacement.discard();
            gateFailure(player, "message.sololeveling3.gate_create_failed");
            return;
        }

        target.discard();
        setStage(player, LEGIA_DUNGEON);
        notifyQuest(player, "story.sololeveling3.legia.title", "story.sololeveling3.legia.objective", 0xFFB77AFF);
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        Entity credited = ShadowKillCreditHelper.creditedPlayerForDeath(
                event.getEntity().level(),
                event.getEntity(),
                event.getSource().getEntity(),
                event.getSource().getDirectEntity()
        );
        ServerPlayer player = credited instanceof ServerPlayer serverPlayer
                ? serverPlayer
                : fallbackStoryParticipant(event.getEntity());
        if (player == null) {
            return;
        }

        int current = stage(player);
        if (event.getEntity() instanceof BarukaEntity && current == ICE_MONARCH) {
            String instance = event.getEntity().getPersistentData().getString("slr_dungeon_instance");
            if (!instance.isBlank()) {
                player.getPersistentData().putString(BARUKA_INSTANCE_TAG, instance);
            }
            SystemNotifications.showTitleUnder(
                    player,
                    0xFF70D7FF,
                    100,
                    Component.translatable("story.sololeveling3.baruka_down.title"),
                    Component.translatable("story.sololeveling3.baruka_down.objective")
            );
            return;
        }

        if (event.getEntity() instanceof IceMonarchEntity && current == ICE_MONARCH) {
            setStage(player, ANCIENT_KEYS);
            player.getPersistentData().remove(BARUKA_INSTANCE_TAG);
            award(player, ICE_ADVANCEMENT);
            notifyQuest(player, "story.sololeveling3.keys.title", "story.sololeveling3.keys.objective", 0xFFFFC857);
            return;
        }

        if (event.getEntity() instanceof MonarchOfGiantsEntity && current == LEGIA_DUNGEON) {
            setStage(player, COMPLETE);
            award(player, LEGIA_ADVANCEMENT);
            notifyQuest(player, "story.sololeveling3.complete.title", "story.sololeveling3.complete.objective", 0xFFD46BFF);
            return;
        }

        if (current >= ANCIENT_KEYS && current <= LEGIA_DUNGEON && isHighRankDungeonBoss(event.getEntity())) {
            grantAncientKey(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag source = event.getOriginal().getPersistentData();
        CompoundTag target = event.getEntity().getPersistentData();
        if (source.contains(STAGE_TAG)) {
            target.putInt(STAGE_TAG, source.getInt(STAGE_TAG));
        }
    }

    private static boolean hasCompletedDemonCastle(ServerPlayer player) {
        if (player.getPersistentData().getBoolean("dkc_floor_20_boss_defeated")) {
            return true;
        }
        return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .map(data -> data.dkc_cleared >= 20.0D)
                .orElse(false);
    }

    private static void markNearbyRedGatesAsFrost(ServerPlayer player) {
        AABB search = player.getBoundingBox().inflate(96.0D);
        List<RedGateEntity> legacyGates = player.serverLevel().getEntitiesOfClass(
                RedGateEntity.class,
                search,
                gate -> !gate.getEntityData().get(RedGateEntity.DATA_usedbefore)
        );
        for (RedGateEntity gate : legacyGates) {
            gate.getPersistentData().putString("slr_red_gate_territory", "frost");
        }

        List<Portal1Entity> proceduralGates = player.serverLevel().getEntitiesOfClass(
                Portal1Entity.class,
                search,
                StoryProgression::isUnusedProceduralRedGate
        );
        for (Portal1Entity gate : proceduralGates) {
            gate.getPersistentData().putString("slr_red_gate_territory", "frost");
        }
    }

    private static boolean isUnusedProceduralRedGate(Portal1Entity gate) {
        if (gate.getEntityData().get(Portal1Entity.DATA_usedbefore)) {
            return false;
        }
        return gate.getPersistentData().getBoolean("slr_procedural_red_gate")
                || gate.getPersistentData().getBoolean("slr_is_red_gate");
    }

    private static boolean isSuitableBlueGate(Entity entity) {
        if (!(entity instanceof Portal1Entity gate)) {
            return false;
        }
        boolean supportedType = entity.getType() == SololevelingModEntities.PORTAL_1.get()
                || entity.getType() == SololevelingModEntities.DATAPACK_GATE.get();
        if (!supportedType || gate.getEntityData().get(Portal1Entity.DATA_usedbefore)) {
            return false;
        }
        CompoundTag data = gate.getPersistentData();
        return !data.getBoolean("slr_procedural_red_gate")
                && !data.getBoolean("slr_is_red_gate")
                && !LegiaGateSupport.isLegiaGate(entity);
    }

    private static void markRedGateAsFrost(Entity entity) {
        if (entity instanceof RedGateEntity gate && !gate.getEntityData().get(RedGateEntity.DATA_usedbefore)) {
            gate.getPersistentData().putString("slr_red_gate_territory", "frost");
        } else if (entity instanceof Portal1Entity gate && isUnusedProceduralRedGate(gate)) {
            gate.getPersistentData().putString("slr_red_gate_territory", "frost");
        }
    }

    private static boolean isHighRankDungeonBoss(Entity entity) {
        boolean recognizedBoss = "boss".equalsIgnoreCase(entity.getPersistentData().getString("slr_dungeon_role"))
                || entity.getType().is(SLR_BOSS_TAG);
        if (!recognizedBoss) {
            return false;
        }
        ResourceLocation dimension = entity.level().dimension().location();
        return A_RANK_DIMENSION.equals(dimension) || S_RANK_DIMENSION.equals(dimension);
    }

    private static void grantAncientKey(ServerPlayer player) {
        if (countKeys(player) >= 3) {
            return;
        }
        ItemStack key = new ItemStack(ModItems.OLD_KEY.get());
        if (!player.addItem(key)) {
            player.drop(key, false);
        }

        int total = countKeys(player);
        player.sendSystemMessage(Component.translatable("message.sololeveling3.key_acquired", total, 3).withStyle(ChatFormatting.GOLD));
        if (total >= 3 && stage(player) == ANCIENT_KEYS) {
            setStage(player, LEGIA_READY);
            award(player, KEYS_ADVANCEMENT);
            notifyQuest(player, "story.sololeveling3.legia_gate.title", "story.sololeveling3.legia_gate.objective", 0xFFC795FF);
        } else if (total < 3) {
            SystemNotifications.showTitleUnder(
                    player,
                    0xFFFFC857,
                    80,
                    Component.translatable("story.sololeveling3.key_found.title"),
                    Component.translatable("story.sololeveling3.key_found.objective", total, 3)
            );
        }
    }

    private static int countKeys(ServerPlayer player) {
        return player.getInventory().countItem(ModItems.OLD_KEY.get());
    }

    private static void setStage(ServerPlayer player, int stage) {
        player.getPersistentData().putInt(STAGE_TAG, stage);
    }

    private static void notifyQuest(ServerPlayer player, String titleKey, String objectiveKey, int accent) {
        Component title = Component.translatable(titleKey);
        Component objective = Component.translatable(objectiveKey);
        SystemNotifications.showTitleUnder(player, accent, 120, title, objective);
        player.sendSystemMessage(Component.literal("[SYSTEM] ").withStyle(ChatFormatting.AQUA).append(title).append(Component.literal(" — ")).append(objective));
    }

    private static void syncSlrQuestOverlay(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (UrgentQuestManager.hasActiveQuest(player)) {
            data.putBoolean(OVERLAY_SENT_TAG, false);
            return;
        }

        QuestOverlay snapshot = questOverlay(player);
        if (snapshot == null) {
            if (data.getBoolean(OVERLAY_SENT_TAG)) {
                sendQuestOverlay(player, false, "", "", "", 0, 0);
                data.putBoolean(OVERLAY_SENT_TAG, false);
            }
            return;
        }

        sendQuestOverlay(
                player,
                true,
                snapshot.title(),
                snapshot.objective(),
                snapshot.kind(),
                snapshot.progress(),
                snapshot.target()
        );
        data.putBoolean(OVERLAY_SENT_TAG, true);
    }

    private static QuestOverlay questOverlay(ServerPlayer player) {
        boolean turkish = isTurkish(player);
        return switch (stage(player)) {
            case ICE_MONARCH -> {
                String currentInstance = player.getPersistentData().getString("slr_dungeon_instance");
                boolean barukaDown = !currentInstance.isBlank()
                        && currentInstance.equals(player.getPersistentData().getString(BARUKA_INSTANCE_TAG));
                yield new QuestOverlay(
                        turkish ? "DONMUŞ TAHT" : "FROZEN THRONE",
                        barukaDown
                                ? (turkish
                                        ? "Baruka yenildi. Buz Hükümdarı Sillad'a karşı hayatta kal."
                                        : "Baruka is defeated. Survive Sillad, the Monarch of Frost.")
                                : (turkish
                                        ? "Karlı Kırmızı Kapı'yı temizle; önce Baruka'yı, ardından Sillad'ı yen."
                                        : "Clear the snowy Red Gate and defeat Baruka, then Sillad."),
                        "kill",
                        barukaDown ? 1 : 0,
                        2
                );
            }
            case ANCIENT_KEYS -> new QuestOverlay(
                    turkish ? "KADİM MÜHÜRLER" : "ANCIENT SEALS",
                    turkish
                            ? "A veya S seviye zindan bosslarını yen ve üç Kadim Anahtarı ele geçir."
                            : "Defeat A- or S-rank dungeon bosses and recover three Ancient Keys.",
                    "kill",
                    Math.min(3, countKeys(player)),
                    3
            );
            case LEGIA_READY -> countKeys(player) < 3
                    ? new QuestOverlay(
                            turkish ? "KADİM MÜHÜRLER" : "ANCIENT SEALS",
                            turkish
                                    ? "Kaybolan Kadim Anahtarları A veya S seviye zindan bosslarından tekrar topla."
                                    : "Recover missing Ancient Keys from A- or S-rank dungeon bosses.",
                            "kill",
                            Math.min(3, countKeys(player)),
                            3
                    )
                    : new QuestOverlay(
                            turkish ? "BAŞLANGICIN HÜKÜMDARI" : "MONARCH OF THE BEGINNING",
                            turkish
                                    ? "Legia'nın hapishanesini açığa çıkarmak için kullanılmamış bir mavi kapıya dokun."
                                    : "Touch an unused blue gate to reveal Legia's prison.",
                            "gate",
                            0,
                            0
                    );
            case LEGIA_DUNGEON -> legiaOverlay(player);
            default -> null;
        };
    }

    private static QuestOverlay legiaOverlay(ServerPlayer player) {
        boolean turkish = isTurkish(player);
        boolean insideLegiaDungeon = LEGIA_DUNGEON_ID.toString().equals(player.getPersistentData().getString("sl_urgent_dungeon_id"))
                && !player.getPersistentData().getString("slr_dungeon_instance").isBlank();
        if (!insideLegiaDungeon) {
            return new QuestOverlay(
                    turkish ? "BAŞLANGICIN HÜKÜMDARI" : "MONARCH OF THE BEGINNING",
                    turkish
                            ? "Dönüşen mavi kapıdan Legia'nın hapishanesine gir."
                            : "Enter Legia's prison through the transformed blue gate.",
                    "gate",
                    0,
                    0
            );
        }

        MonarchOfGiantsEntity legia = player.serverLevel()
                .getEntitiesOfClass(MonarchOfGiantsEntity.class, player.getBoundingBox().inflate(96.0D), Entity::isAlive)
                .stream()
                .findFirst()
                .orElse(null);
        if (legia != null && legia.isSealed()) {
            return new QuestOverlay(
                    turkish ? "KADİM MÜHRÜ KIR" : "BREAK THE ANCIENT SEAL",
                    turkish
                            ? "Üç Kadim Anahtarın tamamını Legia'nın zincirlerine tak."
                            : "Insert all three Ancient Keys into Legia's chains.",
                    "kill",
                    legia.insertedKeys(),
                    3
            );
        }
        return new QuestOverlay(
                turkish ? "BAŞLANGICIN HÜKÜMDARI" : "MONARCH OF THE BEGINNING",
                turkish
                        ? "Başlangıcın Hükümdarı Legia'yı yen."
                        : "Defeat Legia, Monarch of the Beginning.",
                "kill",
                0,
                1
        );
    }

    private static void sendQuestOverlay(
            ServerPlayer player,
            boolean active,
            String title,
            String objective,
            String kind,
            int progress,
            int target
    ) {
        SololevelingMod.PACKET_HANDLER.send(
                PacketDistributor.PLAYER.with(() -> player),
                new UrgentQuestStatusMessage(active, title, objective, kind, progress, target, -1)
        );
    }

    private static void gateFailure(ServerPlayer player, String translationKey) {
        player.sendSystemMessage(Component.translatable(translationKey).withStyle(ChatFormatting.RED));
        SoloLeveling3.LOGGER.error("Could not create Legia gate for {} using dungeon {}", player.getGameProfile().getName(), LEGIA_DUNGEON_ID);
    }

    private static void syncEarnedAdvancements(ServerPlayer player) {
        int current = stage(player);
        if (current >= ICE_MONARCH) {
            award(player, STORY_ROOT);
        }
        if (current >= ANCIENT_KEYS) {
            award(player, ICE_ADVANCEMENT);
        }
        if (current >= LEGIA_READY) {
            award(player, KEYS_ADVANCEMENT);
        }
        if (current >= COMPLETE) {
            award(player, LEGIA_ADVANCEMENT);
        }
    }

    private static void award(ServerPlayer player, ResourceLocation id) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(id);
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    private static ServerPlayer fallbackStoryParticipant(LivingEntity defeated) {
        int requiredStage;
        if (defeated instanceof IceMonarchEntity) {
            requiredStage = ICE_MONARCH;
        } else if (defeated instanceof MonarchOfGiantsEntity) {
            requiredStage = LEGIA_DUNGEON;
        } else {
            return null;
        }
        if (!(defeated.level() instanceof ServerLevel level)) {
            return null;
        }

        String instance = defeated.getPersistentData().getString("slr_dungeon_instance");
        return level.players().stream()
                .filter(ServerPlayer::isAlive)
                .filter(player -> stage(player) == requiredStage)
                .filter(player -> instance.isBlank()
                        ? player.distanceToSqr(defeated) <= 16384.0D
                        : instance.equals(player.getPersistentData().getString("slr_dungeon_instance")))
                .min((first, second) -> Double.compare(first.distanceToSqr(defeated), second.distanceToSqr(defeated)))
                .orElse(null);
    }

    private static boolean isTurkish(ServerPlayer player) {
        return player.getLanguage().toLowerCase(Locale.ROOT).startsWith("tr_");
    }

    private record QuestOverlay(String title, String objective, String kind, int progress, int target) {
    }
}
