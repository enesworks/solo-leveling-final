package dev.eness.sololevelingfinal.core.campaign;

import dev.eness.sololevelingfinal.core.entity.MonarchOfGiantsEntity;
import dev.eness.sololevelingfinal.core.registry.ModItems;
import dev.eness.sololevelingfinal.core.story.LegiaGateSupport;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import dev.eness.sololevelingfinal.core.dungeon.DatapackDungeonGateHandler;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonInstanceSavedData;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonEncounterRuntime;
import dev.eness.sololevelingfinal.core.entity.DatapackGateEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.procedures.ShadowKillCreditHelper;

import java.util.Optional;
import java.util.UUID;

/** Campaign-owned Legia bridge: key hunt, prison gate routing, and exact Legia completion receipt. */
public final class LegiaCampaign {
    public static final String KEY_GATE_TAG = "legia_key_gate";
    public static final String KEY_GATE_ID_TAG = "legia_key_gate_id";
    public static final String KEY_GATE_POS_TAG = "legia_key_gate_pos";
    public static final String KEY_DEATHS_TAG = "legia_key_deaths";
    public static final String LEGIA_GATE_TAG = "legia_prison_gate";
    public static final String LEGIA_INSTANCE_TAG = "legia_instance";
    public static final String LEGIA_BOSS_TAG = "legia_boss";
    public static final String LEGIA_DEAD_TAG = "legia_dead";
    public static final String LEGIA_COMPLETION_PENDING_TAG = "legia_completion_pending";
    public static final String OWNER_TAG = "sl3_campaign_owner";
    public static final String ROLE_TAG = "sl3_campaign_role";
    public static final String ROLE_LEGIA = "legia";

    private static final int REQUIRED_KEYS = 3;
    private static final ResourceLocation DUNGEON_ID = LegiaGateSupport.DUNGEON_ID;
    private static final ResourceLocation A_RANK_DIMENSION = ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_a");
    private static final ResourceLocation S_RANK_DIMENSION = ResourceLocation.fromNamespaceAndPath("sololeveling", "dungeon_dimension_s");
    private static final TagKey<EntityType<?>> SLR_BOSS_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("minecraft", "soloboss")
    );

    private LegiaCampaign() {
    }

    public static void tick(ServerPlayer player) {
        if (player == null || player.server == null || player.level().isClientSide) {
            return;
        }
        CampaignStage stage = CampaignDirector.stage(player);
        if (legiaKeyStage(stage)) deliverPendingKey(player);
        stage = CampaignDirector.stage(player);
        if (stage == CampaignStage.LEGIA_KEYS) {
            recoverKeyHuntGate(player);
            if (countKeys(player) >= REQUIRED_KEYS) {
                CampaignDirector.setStage(player, CampaignStage.LEGIA_READY);
                CampaignDirector.award(player, "ancient_keys");
                message(player, "Üç Kadim Anahtar hazır. Mavi kapı Legia'nın hapishanesine açılacak.", ChatFormatting.GOLD);
            } else if (Level.OVERWORLD.equals(player.level().dimension())) {
                ensureKeyHuntGate(player);
            }
            return;
        }
        if (stage == CampaignStage.LEGIA_READY) {
            recoverPrisonGate(player);
            if (countKeys(player) < REQUIRED_KEYS && !hasLegiaInstance(player)) {
                CampaignDirector.setStage(player, CampaignStage.LEGIA_KEYS);
                CampaignGates.remove(player, false);
                message(player, "Legia'nın hapishanesi için 3 Kadim Anahtar gerekli. Mevcut: " + countKeys(player) + "/3", ChatFormatting.RED);
            }
            return;
        }
        if (stage == CampaignStage.LEGIA_ACTIVE) {
            recoverPrisonGate(player);
            if (isInside(player)) {
                rememberInstance(player);
                tagNearbyLegia(player);
                CampaignDirector.progress(player).remove("legia_entry_deadline");
            } else if (Level.OVERWORLD.equals(player.level().dimension()) && !CampaignDirector.progress(player).hasUUID(LEGIA_DEAD_TAG)
                    && !CampaignDirector.progress(player).contains("legia_entry_deadline")
                    && countKeys(player) + CampaignDirector.progress(player).getInt("legia_inserted_keys") < REQUIRED_KEYS) {
                ensureKeyHuntGate(player);
            }
            checkPendingEntry(player);
            completePendingLegia(player);
        }
    }

    public static boolean enter(ServerPlayer player, Entity gate) {
        if (player == null || player.server == null || gate == null || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        CampaignStage stage = CampaignDirector.stage(player);
        if (stage != CampaignStage.LEGIA_READY && stage != CampaignStage.LEGIA_ACTIVE) {
            return false;
        }
        CompoundTag progress = CampaignDirector.progress(player);
        boolean firstEntry = !hasLegiaInstance(player);
        if (!hasLegiaInstance(player) && countKeys(player) < REQUIRED_KEYS) {
            message(player, "Legia'nın hapishanesi için 3 Kadim Anahtar gerekli. Mevcut: " + countKeys(player) + "/3", ChatFormatting.RED);
            CampaignDirector.setStage(player, CampaignStage.LEGIA_KEYS);
            return false;
        }
        // A death/forced return can leave our own native binding behind in Overworld.
        if (Level.OVERWORLD.equals(player.level().dimension()) && hasLegiaInstance(player)
                && progress.getString(LEGIA_INSTANCE_TAG).equals(player.getPersistentData().getString("slr_dungeon_instance"))) {
            clearPlayerBinding(player);
        }
        if (isNativeBoundElsewhere(player)) {
            message(player, "Önce mevcut zindandan çıkmalısın.", ChatFormatting.RED);
            return false;
        }
        DatapackGateEntity legiaGate = existingPrisonGate(player, progress).orElse(null);
        if (legiaGate == null) {
            CompoundTag saved = progress.getCompound(LEGIA_GATE_TAG);
            if (saved.hasUUID("id") && saved.getString("dimension").equals(level.dimension().location().toString())
                    && CampaignGates.waitForLookup(level, saved.getUUID("id"), BlockPos.of(saved.getLong("position")))) return false;
            if (!Level.OVERWORLD.equals(player.level().dimension())) {
                return false;
            }
            legiaGate = SololevelingModEntities.DATAPACK_GATE.get().create(level);
            if (legiaGate == null) {
                return false;
            }
            legiaGate.moveTo(gate.getX(), gate.getY(), gate.getZ(), gate.getYRot(), gate.getXRot());
            legiaGate.setYHeadRot(gate.getYHeadRot());
            legiaGate.setPersistenceRequired();
            if (!LegiaGateSupport.prepare(legiaGate, gate, player.getUUID())) {
                legiaGate.discard();
                return false;
            }
            restoreSavedGateBinding(progress, legiaGate);
            CampaignDirector.tagMob(legiaGate, player, "legia_gate");
            legiaGate.getPersistentData().putBoolean(LEGIA_GATE_TAG, true);
            legiaGate.getPersistentData().putUUID(OWNER_TAG, player.getUUID());
            legiaGate.getPersistentData().putBoolean(CampaignGates.TAG, true);
            legiaGate.getPersistentData().putBoolean("exit", false);
            legiaGate.getPersistentData().putLong("created", level.getGameTime());
            legiaGate.setInvulnerable(true);
            if (!level.addFreshEntity(legiaGate)) {
                legiaGate.discard();
                return false;
            }
            saveGate(progress, LEGIA_GATE_TAG, legiaGate, level);
            gate.discard();
            CampaignDirector.changed(player);
        }
        DatapackDungeonGateHandler.enter(player, legiaGate);
        saveGate(progress, LEGIA_GATE_TAG, legiaGate, level);
        if (gate != legiaGate && !gate.isRemoved()) {
            gate.discard();
        }
        CampaignDirector.changed(player);
        // Native generation is deferred five ticks; preparation is its synchronous receipt.
        boolean prepared = legiaGate.getStringUUID().equals(player.getPersistentData().getString("dungeon_tag"))
                && player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).map(vars -> vars.dungeoning).orElse(false);
        if (prepared) {
            progress.putLong("legia_entry_deadline", level.getGameTime() + 100);
            progress.putBoolean("legia_entry_fresh", firstEntry);
            CampaignDirector.changed(player);
        }
        return prepared;
    }

    /** Root should call this once from its LOWEST LivingDeathEvent handler. */
    public static void onDeath(LivingDeathEvent event) {
        if (event == null || event.isCanceled() || event.getEntity().level().isClientSide) {
            return;
        }
        LivingEntity dead = event.getEntity();
        ServerPlayer credited = creditedPlayer(event, dead);
        if (dead instanceof MonarchOfGiantsEntity legia) {
            UUID ownerId = ownerIdForLegia(legia, credited);
            if (ownerId != null && exactLegiaInstance(ownerId, legia)) {
                CompoundTag progress = CampaignSavedData.get(legia.getServer()).player(ownerId);
                if (CampaignStage.read(progress.getString("stage")) != CampaignStage.LEGIA_ACTIVE) {
                    return;
                }
                if (!progress.hasUUID(LEGIA_DEAD_TAG)) {
                    progress.putUUID(LEGIA_DEAD_TAG, legia.getUUID());
                    progress.putBoolean(LEGIA_COMPLETION_PENDING_TAG, true);
                    ServerPlayer online = legia.getServer().getPlayerList().getPlayer(ownerId);
                    if (online != null && CampaignDirector.stage(online) == CampaignStage.LEGIA_ACTIVE) {
                        completePendingLegia(online);
                    } else {
                        CampaignSavedData.get(legia.getServer()).setDirty();
                    }
                }
            }
            return;
        }
        if (credited == null || !legiaKeyStage(CampaignDirector.stage(credited)) || !isHighRankDungeonBoss(dead)) {
            return;
        }
        CompoundTag progress = CampaignDirector.progress(credited);
        if (countKeys(credited) + progress.getInt("legia_pending_keys") < REQUIRED_KEYS && addUuid(progress, KEY_DEATHS_TAG, dead.getUUID())) {
            progress.putInt("legia_pending_keys", progress.getInt("legia_pending_keys") + 1);
            CampaignDirector.changed(credited);
            deliverPendingKey(credited);
        }
    }

    public static boolean isInside(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        return nativeInstance(player, player.getPersistentData().getString("slr_dungeon_instance"))
                .filter(instance -> instance.dimension().equals(player.level().dimension())).isPresent();
    }

    public static void cleanupAfterExit(ServerPlayer player) {
        if (player == null) {
            return;
        }
        CompoundTag progress = CampaignDirector.progress(player);
        nativeInstance(player, progress.getString(LEGIA_INSTANCE_TAG)).ifPresent(instance -> {
            DungeonEncounterRuntime.clearHighlightsFor(player, instance);
            instance.removeParticipant(player.getUUID());
            DungeonInstanceSavedData.get(player.server).setDirty();
        });
        clearPlayerBinding(player);
    }

    public static Component overlayProgress(ServerPlayer player) {
        if (player == null) {
            return Component.empty();
        }
        CampaignStage stage = CampaignDirector.stage(player);
        if (stage == CampaignStage.LEGIA_KEYS) {
            return Component.literal("Kadim Anahtarlar: " + countKeys(player) + "/" + REQUIRED_KEYS);
        }
        if (stage == CampaignStage.LEGIA_READY) {
            return Component.literal("Mavi kapıdan Legia'nın hapishanesine gir.");
        }
        if (stage == CampaignStage.LEGIA_ACTIVE) {
            MonarchOfGiantsEntity legia = nearbyLegia(player).orElse(null);
            if (legia != null && legia.isSealed()) {
                return Component.literal("Zincirlere anahtar tak: " + legia.insertedKeys() + "/" + REQUIRED_KEYS);
            }
            return Component.literal("Başlangıcın Hükümdarı Legia'yı yen.");
        }
        return Component.empty();
    }

    private static boolean legiaKeyStage(CampaignStage stage) {
        return stage == CampaignStage.LEGIA_KEYS || stage == CampaignStage.LEGIA_READY || stage == CampaignStage.LEGIA_ACTIVE;
    }

    private static void ensureKeyHuntGate(ServerPlayer player) {
        CompoundTag progress = CampaignDirector.progress(player);
        CompoundTag saved = progress.getCompound(KEY_GATE_TAG);
        if (saved.hasUUID("id") && saved.getString("dimension").equals(player.level().dimension().location().toString())) {
            BlockPos pos = BlockPos.of(saved.getLong("position"));
            boolean waiting = CampaignGates.waitForLookup(player.serverLevel(), saved.getUUID("id"), pos);
            Entity old = player.serverLevel().getEntity(saved.getUUID("id"));
            if (old != null && !old.isRemoved() && CampaignSlrBridge.isUsableNormalGate(old)) {
                return;
            }
            if (old != null && !old.isRemoved()) {
                old.discard();
            }
            if (waiting) {
                return;
            }
        }
        long now = player.serverLevel().getGameTime();
        if (now < progress.getLong("legia_key_gate_retry")) {
            return;
        }
        progress.putLong("legia_key_gate_retry", now + 120L);
        Entity gate = CampaignSlrBridge.spawnKeyHuntGate(player);
        if (gate == null) {
            CampaignDirector.changed(player);
            return;
        }
        gate.getPersistentData().putUUID(OWNER_TAG, player.getUUID());
        gate.getPersistentData().putBoolean(KEY_GATE_ID_TAG, true);
        saveGate(progress, KEY_GATE_TAG, gate, player.serverLevel());
        message(player, "Kadim Anahtar için A/S rank kapısı açıldı: " + gate.blockPosition().toShortString(), ChatFormatting.AQUA);
        CampaignDirector.changed(player);
    }

    private static void recoverKeyHuntGate(ServerPlayer player) {
        CompoundTag saved = CampaignDirector.progress(player).getCompound(KEY_GATE_TAG);
        if (saved.hasUUID("id") && saved.getString("dimension").equals(player.level().dimension().location().toString())) {
            CampaignGates.waitForLookup(player.serverLevel(), saved.getUUID("id"), BlockPos.of(saved.getLong("position")));
        }
    }

    private static void recoverPrisonGate(ServerPlayer player) {
        CompoundTag progress = CampaignDirector.progress(player);
        existingPrisonGate(player, progress).ifPresent(gate -> {
            saveGate(progress, LEGIA_GATE_TAG, gate, (ServerLevel)gate.level());
            CampaignDirector.changed(player);
        });
    }

    private static Optional<DatapackGateEntity> existingPrisonGate(ServerPlayer player, CompoundTag progress) {
        CompoundTag saved = progress.getCompound(LEGIA_GATE_TAG);
        if (!saved.hasUUID("id")) {
            return Optional.empty();
        }
        ResourceLocation dimension = ResourceLocation.tryParse(saved.getString("dimension"));
        if (dimension == null || player.server == null) {
            return Optional.empty();
        }
        ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        if (level == null) {
            return Optional.empty();
        }
        BlockPos pos = BlockPos.of(saved.getLong("position"));
        boolean waiting = CampaignGates.waitForLookup(level, saved.getUUID("id"), pos);
        Entity entity = level.getEntity(saved.getUUID("id"));
        if (entity instanceof DatapackGateEntity gate && !gate.isRemoved() && LegiaGateSupport.isLegiaGate(gate) && owns(player, gate)) {
            return Optional.of(gate);
        }
        return Optional.empty();
    }

    private static void saveGate(CompoundTag progress, String key, Entity gate, ServerLevel level) {
        CompoundTag saved = new CompoundTag();
        saved.putUUID("id", gate.getUUID());
        saved.putString("dimension", level.dimension().location().toString());
        saved.putLong("position", gate.blockPosition().asLong());
        CompoundTag data = gate.getPersistentData();
        if (data.contains("tpx", Tag.TAG_ANY_NUMERIC)) saved.putDouble("tpx", data.getDouble("tpx"));
        if (data.contains("tpy", Tag.TAG_ANY_NUMERIC)) saved.putDouble("tpy", data.getDouble("tpy"));
        if (data.contains("tpz", Tag.TAG_ANY_NUMERIC)) saved.putDouble("tpz", data.getDouble("tpz"));
        if (data.contains("slr_datapack_gate_instance", Tag.TAG_STRING)) {
            saved.putString("instance", data.getString("slr_datapack_gate_instance"));
            progress.putString(LEGIA_INSTANCE_TAG, data.getString("slr_datapack_gate_instance"));
        }
        progress.put(key, saved);
        if (LEGIA_GATE_TAG.equals(key)) progress.put("entry_gate", saved.copy());
    }

    private static void rememberInstance(ServerPlayer player) {
        String id = player.getPersistentData().getString("slr_dungeon_instance");
        if (!id.isBlank() && !id.equals(CampaignDirector.progress(player).getString(LEGIA_INSTANCE_TAG))) {
            CampaignDirector.progress(player).putString(LEGIA_INSTANCE_TAG, id);
            CampaignDirector.changed(player);
        }
    }

    private static void tagNearbyLegia(ServerPlayer player) {
        nearbyLegia(player).ifPresent(legia -> {
            CompoundTag progress = CampaignDirector.progress(player);
            if (!progress.hasUUID(LEGIA_BOSS_TAG)) {
                progress.putUUID(LEGIA_BOSS_TAG, legia.getUUID());
            }
            progress.putInt("legia_inserted_keys", legia.insertedKeys());
            CampaignDirector.tagMob(legia, player, ROLE_LEGIA);
            legia.getPersistentData().putUUID(OWNER_TAG, player.getUUID());
            legia.getPersistentData().putString(ROLE_TAG, ROLE_LEGIA);
            CampaignDirector.changed(player);
        });
    }

    private static Optional<MonarchOfGiantsEntity> nearbyLegia(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        String instance = player.getPersistentData().getString("slr_dungeon_instance");
        AABB area = player.getBoundingBox().inflate(128.0D);
        return level.getEntitiesOfClass(MonarchOfGiantsEntity.class, area, legia -> legia.isAlive() && matchesInstance(instance, legia))
                .stream()
                .findFirst();
    }

    private static boolean matchesInstance(String instance, Entity entity) {
        String actual = entity.getPersistentData().getString("slr_dungeon_instance");
        return !instance.isBlank() && instance.equals(actual);
    }

    private static ServerPlayer creditedPlayer(LivingDeathEvent event, LivingEntity dead) {
        Entity credited = ShadowKillCreditHelper.creditedPlayerForDeath(
                dead.level(),
                dead,
                event.getSource().getEntity(),
                event.getSource().getDirectEntity()
        );
        return credited instanceof ServerPlayer player ? player : null;
    }

    private static UUID ownerIdForLegia(MonarchOfGiantsEntity legia, ServerPlayer credited) {
        CompoundTag data = legia.getPersistentData();
        if (data.hasUUID(OWNER_TAG)) {
            return data.getUUID(OWNER_TAG);
        }
        return credited == null ? null : credited.getUUID();
    }


    private static void completePendingLegia(ServerPlayer player) {
        CompoundTag progress = CampaignDirector.progress(player);
        if (!progress.getBoolean(LEGIA_COMPLETION_PENDING_TAG) || progress.getBoolean("encounter_complete")) {
            return;
        }
        nativeInstance(player, progress.getString(LEGIA_INSTANCE_TAG)).ifPresent(instance -> {
            instance.setCompleted(true);
            instance.setReturnPortalDeferred(true);
            instance.setReturnPortalSuppressed(true);
            for (var encounter : instance.encounters()) { encounter.clearTrackedMobs(); encounter.markCompleted(); }
            DungeonInstanceSavedData.get(player.server).setDirty();
        });
        CampaignDirector.completeEncounter(player);
        CampaignDirector.award(player, "monarch_of_beginning_defeated");
        progress.remove(LEGIA_COMPLETION_PENDING_TAG);
        CampaignDirector.changed(player);
    }

    private static boolean exactLegiaInstance(UUID ownerId, Entity entity) {
        if (ownerId == null || entity == null || entity.getServer() == null) {
            return false;
        }
        CompoundTag progress = CampaignSavedData.get(entity.getServer()).player(ownerId);
        String expected = progress.getString(LEGIA_INSTANCE_TAG);
        String actual = entity.getPersistentData().getString("slr_dungeon_instance");
        return !expected.isBlank() && expected.equals(actual)
                && (!progress.hasUUID(LEGIA_BOSS_TAG) || progress.getUUID(LEGIA_BOSS_TAG).equals(entity.getUUID()));
    }

    private static void restoreSavedGateBinding(CompoundTag progress, DatapackGateEntity gate) {
        CompoundTag saved = progress.getCompound(LEGIA_GATE_TAG);
        CompoundTag data = gate.getPersistentData();
        if (saved.contains("tpx", Tag.TAG_ANY_NUMERIC)) data.putDouble("tpx", saved.getDouble("tpx"));
        if (saved.contains("tpy", Tag.TAG_ANY_NUMERIC)) data.putDouble("tpy", saved.getDouble("tpy"));
        if (saved.contains("tpz", Tag.TAG_ANY_NUMERIC)) data.putDouble("tpz", saved.getDouble("tpz"));
        String instance = firstNonBlank(saved.getString("instance"), progress.getString(LEGIA_INSTANCE_TAG));
        if (!instance.isBlank()) {
            data.putString("slr_datapack_gate_instance", instance);
            data.putBoolean("slr_datapack_gate_generated", true);
            data.putBoolean("slr_datapack_gate_generating", false);
        }
    }

    private static boolean isNativeBoundElsewhere(ServerPlayer player) {
        String instance = player.getPersistentData().getString("slr_dungeon_instance");
        if (!instance.isBlank()) return true;
        boolean dungeoning = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .map(capability -> capability.dungeoning)
                .orElse(false);
        return dungeoning;
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

    private static void deliverPendingKey(ServerPlayer player) {
        CompoundTag progress = CampaignDirector.progress(player);
        int pending = progress.getInt("legia_pending_keys");
        if (pending <= 0) return;
        ItemStack key = new ItemStack(ModItems.OLD_KEY.get());
        player.getInventory().add(key);
        if (!key.isEmpty() && player.drop(key, false) == null) return;
        progress.putInt("legia_pending_keys", pending - 1);
        CampaignDirector.changed(player);
        message(player, "Kadim Anahtar bulundu: " + countKeys(player) + "/" + REQUIRED_KEYS, ChatFormatting.GOLD);
        if (countKeys(player) >= REQUIRED_KEYS && CampaignDirector.stage(player) == CampaignStage.LEGIA_KEYS) {
            CampaignDirector.setStage(player, CampaignStage.LEGIA_READY);
            CampaignDirector.award(player, "ancient_keys");
            message(player, "Üç Kadim Anahtar tamamlandı. Legia'nın hapishanesine giden mavi kapıyı bul.", ChatFormatting.LIGHT_PURPLE);
        }
    }

    private static int countKeys(ServerPlayer player) {
        return player.getInventory().countItem(ModItems.OLD_KEY.get());
    }

    private static boolean addUuid(CompoundTag progress, String key, UUID id) {
        ListTag list = progress.getList(key, Tag.TAG_STRING);
        String value = id.toString();
        for (Tag tag : list) {
            if (value.equals(tag.getAsString())) {
                return false;
            }
        }
        ListTag copy = new ListTag();
        for (Tag tag : list) {
            copy.add(tag.copy());
        }
        copy.add(StringTag.valueOf(value));
        progress.put(key, copy);
        return true;
    }

    private static boolean hasLegiaInstance(ServerPlayer player) {
        return nativeInstance(player, CampaignDirector.progress(player).getString(LEGIA_INSTANCE_TAG)).isPresent();
    }

    private static Optional<DungeonInstanceSavedData.Instance> nativeInstance(ServerPlayer player, String id) {
        try {
            return DungeonInstanceSavedData.get(player.server).getInstance(UUID.fromString(id))
                    .filter(instance -> DUNGEON_ID.equals(instance.dungeonId()));
        } catch (IllegalArgumentException invalid) { return Optional.empty(); }
    }

    private static void clearPlayerBinding(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        for (String key : new String[]{"sl_urgent_dungeon_id", "slr_dungeon_instance", "slr_procedural_dungeon", "slr_procedural_red_gate", "dungeon_tag"}) data.remove(key);
        player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            vars.dungeoning = false; vars.BossKilled = false; vars.syncPlayerVariables(player);
        });
    }

    /** Native generation can fail after enter() has returned; leave a usable retry gate. */
    private static void checkPendingEntry(ServerPlayer player) {
        CompoundTag progress = CampaignDirector.progress(player);
        if (!progress.contains("legia_entry_deadline") || isInside(player)
                || player.serverLevel().getGameTime() < progress.getLong("legia_entry_deadline")) return;
        progress.remove("legia_entry_deadline");
        if (Level.OVERWORLD.equals(player.level().dimension())) {
            CompoundTag gate = progress.getCompound(LEGIA_GATE_TAG);
            if (gate.hasUUID("id") && gate.getUUID("id").toString().equals(player.getPersistentData().getString("dungeon_tag"))) clearPlayerBinding(player);
            if (progress.getBoolean("legia_entry_fresh")) CampaignDirector.setStage(player, CampaignStage.LEGIA_READY);
            message(player, "Hapishaneye geçiş tamamlanamadı. Anahtarların ve kapın korundu; tekrar girebilirsin.", ChatFormatting.RED);
        }
        CampaignDirector.changed(player);
    }

    private static boolean owns(ServerPlayer player, Entity entity) {
        CompoundTag tag = entity.getPersistentData();
        return tag.hasUUID(OWNER_TAG) && player.getUUID().equals(tag.getUUID(OWNER_TAG));
    }

    private static String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? (second == null ? "" : second) : first;
    }

    private static void message(ServerPlayer player, String text, ChatFormatting color) {
        player.sendSystemMessage(Component.literal(text).withStyle(color));
    }
}
