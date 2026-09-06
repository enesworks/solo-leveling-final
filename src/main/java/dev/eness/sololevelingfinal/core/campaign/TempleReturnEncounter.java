package dev.eness.sololevelingfinal.core.campaign;

import dev.eness.sololevelingfinal.core.mixin.CartenonTempleManagerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.StatueOfGodEntity;
import dev.eness.sololevelingfinal.core.util.CartenonProgressSavedData;
import dev.eness.sololevelingfinal.core.util.CartenonTempleGenerator;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/** Campaign-only return visit for the Cartenon Temple. Native first-entry story state is left intact. */
public final class TempleReturnEncounter {
    public static final ResourceKey<Level> TEMPLE = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation("sololeveling", "cartenon_temple"));

    public static final String OWNER_TAG = "sl3_campaign_owner";
    public static final String ROLE_TAG = "sl3_campaign_role";
    public static final String INSTANCE_TAG = "sl3_campaign_cartenon_instance";
    public static final String CENTER_TAG = "sl3_campaign_cartenon_center";
    public static final String PHASE_TAG = "sl3_campaign_cartenon_phase";
    public static final String MAIN_UUID_TAG = "sl3_campaign_cartenon_main";
    public static final String MAIN_DEAD_TAG = "sl3_campaign_cartenon_main_dead";
    public static final String WEAPON_UUIDS_TAG = "sl3_campaign_cartenon_weapons";
    public static final String WEAPON_DEAD_TAG = "sl3_campaign_cartenon_weapon_dead";
    public static final String STATUE_RECORDS_TAG = "sl3_campaign_cartenon_statues";
    public static final String ENTRY_TAG = "sl3_campaign_cartenon_entry";
    public static final String ENTERING_TAG = "sl3_campaign_cartenon_entering";
    public static final String MISSING_GRACE_TAG = "sl3_campaign_cartenon_missing_grace";
    public static final String WEAPON_WOKE_TAG = "sl3_campaign_cartenon_woke";
    public static final String ROLE_MAIN = "cartenon_main";
    public static final String ROLE_WEAPON = "cartenon_weapon";

    private static final String PHASE_MAIN = "main";
    private static final String PHASE_WEAPONS = "weapons";
    private static final String STATE_TAG = "state";
    private static final String AGGRESSIVE = "aggresive";
    private static final String NATIVE_INSTANCE_TAG = "slr_cartenon_instance";
    private static final String NATIVE_PENDING_TAG = "slr_cartenon_awakening_pending";
    private static final String NATIVE_DECLINE_TICKS = "slr_cartenon_decline_ticks";
    private static final String NATIVE_ENTRY_PROTECTION = "slr_cartenon_entry_protection";
    private static final int EXPECTED_WEAPONS = 12;
    private static final double SCAN_RANGE = 170.0D;
    private static final double WEAPON_DAMAGE = 14.0D;
    private static final double WEAPON_SPEED = 0.23D;
    private static final float MAX_DAMAGE_FRACTION = 0.35F;

    private TempleReturnEncounter() {
    }

    public static boolean enter(ServerPlayer player) {
        if (player == null || player.server == null || !CampaignDirector.isTempleReturn(player)) {
            return false;
        }
        ServerLevel temple = player.server.getLevel(TEMPLE);
        if (temple == null) {
            return false;
        }

        CompoundTag progress = CampaignDirector.progress(player);
        int instance;
        if (progress.contains(INSTANCE_TAG, Tag.TAG_INT)) {
            instance = progress.getInt(INSTANCE_TAG);
        } else {
            instance = CartenonProgressSavedData.get(temple).allocateInstance();
            progress.putInt(INSTANCE_TAG, instance);
            CampaignDirector.changed(player);
        }

        BlockPos origin = CartenonTempleManagerAccessor.sololeveling3$instanceOrigin(instance);
        progress.putLong(CENTER_TAG, origin.asLong());
        if (!progress.contains(PHASE_TAG, Tag.TAG_STRING)) {
            progress.putString(PHASE_TAG, PHASE_MAIN);
        }
        progress.putBoolean(ENTERING_TAG, true);
        CampaignDirector.changed(player);

        if (CartenonProgressSavedData.get(temple).isInstanceBuilt(instance)) {
            teleportDirect(player, temple, instance);
            return true;
        }
        if (CartenonTempleGenerator.isBuildingAt(temple, origin)) {
            return true;
        }

        UUID playerId = player.getUUID();
        boolean started = CartenonTempleGenerator.startAt(
                temple,
                origin,
                Direction.SOUTH,
                playerId,
                player.getGameProfile().getName(),
                true,
                () -> {
                    CartenonProgressSavedData.get(temple).markInstanceBuilt(instance);
                    ServerPlayer online = temple.getServer().getPlayerList().getPlayer(playerId);
                    if (online != null && online.isAlive() && CampaignDirector.isTempleReturn(online)) {
                        teleportDirect(online, temple, instance);
                    }
                });
        if (!started) {
            progress.remove(ENTERING_TAG);
            CampaignDirector.changed(player);
        }
        return started;
    }

    public static void tick(ServerPlayer player) {
        if (!isActiveTemplePlayer(player)) {
            return;
        }
        stripNativeFreezeTags(player);
        rescueFromVoid(player);

        CompoundTag progress = CampaignDirector.progress(player);
        if (!progress.contains(PHASE_TAG, Tag.TAG_STRING)) {
            progress.putString(PHASE_TAG, PHASE_MAIN);
            CampaignDirector.changed(player);
        }
        BlockPos center = progress.contains(CENTER_TAG, Tag.TAG_LONG) ? BlockPos.of(progress.getLong(CENTER_TAG)) : player.blockPosition();
        CampaignDirector.rememberInside(player, center);

        ServerLevel level = player.serverLevel();
        if (player.tickCount % 20 == 0) {
            if (weaponUuids(progress).size() < EXPECTED_WEAPONS || !progress.hasUUID(MAIN_UUID_TAG)) loadTempleChunks(level, center);
            scanAndTagStatues(player, level, center, progress);
            restoreMissingStatues(player, level, progress);
        }
        if (progress.hasUUID(MAIN_DEAD_TAG) && !PHASE_WEAPONS.equals(progress.getString(PHASE_TAG))) {
            beginWeaponPhase(player, progress);
        }

        if (PHASE_WEAPONS.equals(progress.getString(PHASE_TAG))) {
            wakeWeapons(player, level, progress);
            completeWhenAllWeaponsDead(player, progress);
            return;
        }

        Mob main = mainStatue(level, progress).orElse(null);
        if (main == null || !main.isAlive()) {
            markMissingGrace(progress, level);
            CampaignDirector.changed(player);
            return;
        }
        progress.remove(MISSING_GRACE_TAG);
        keepMainAggressive(player, main);
        keepWeaponsDormant(level, progress);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void protectCampaignPlayer(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isActiveTemplePlayer(player)) {
            event.setCanceled(true);
            player.setHealth(1.0F);
            rescueFromVoid(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void receiptCampaignMobDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof Mob mob) || !isCampaignMob(mob)
                || !(mob.level() instanceof ServerLevel level) || !level.dimension().equals(TEMPLE)) {
            return;
        }
        UUID ownerId = mob.getPersistentData().getUUID(OWNER_TAG);
        CampaignSavedData saved = CampaignSavedData.get(level.getServer());
        CompoundTag progress = saved.player(ownerId);
        if (CampaignStage.read(progress.getString("stage")) != CampaignStage.DOUBLE_ACTIVE) return;
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        String role = mob.getPersistentData().getString(ROLE_TAG);
        if (ROLE_MAIN.equals(role) && progress.hasUUID(MAIN_UUID_TAG) && mob.getUUID().equals(progress.getUUID(MAIN_UUID_TAG))) {
            progress.putUUID(MAIN_DEAD_TAG, mob.getUUID());
            progress.putString(PHASE_TAG, PHASE_WEAPONS);
            saved.setDirty();
        } else if (ROLE_WEAPON.equals(role) && weaponUuids(progress).contains(mob.getUUID())) {
            addUuid(progress, WEAPON_DEAD_TAG, mob.getUUID());
            saved.setDirty();
            if (owner != null) completeWhenAllWeaponsDead(owner, progress);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void capCampaignPlayerDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isActiveTemplePlayer(player)) {
            return;
        }
        float cap = Math.max(1.0F, player.getMaxHealth() * MAX_DAMAGE_FRACTION);
        if (event.getAmount() > cap) {
            event.setAmount(cap);
        }
        if (event.getAmount() + 0.001F >= player.getHealth()) {
            event.setCanceled(true);
            player.setHealth(1.0F);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getFrom().equals(TEMPLE) && event.getEntity() instanceof ServerPlayer player) {
            cleanup(player);
        }
    }

    public static boolean shouldBypassNativeTempleHandler(ServerPlayer player) {
        return isActiveTemplePlayer(player);
    }

    public static boolean shouldBypassGodHurt(Entity entity, DamageSource source) {
        ServerPlayer player = ownerPlayer(entity);
        return entity instanceof StatueOfGodEntity
                && entity.level().dimension().equals(TEMPLE)
                && isCampaignMob(entity)
                && ROLE_MAIN.equals(entity.getPersistentData().getString(ROLE_TAG))
                && source != null
                && player != null
                && CampaignDirector.isTempleReturn(player);
    }

    public static float campaignGodDamage(float original) {
        return original;
    }

    public static boolean isCampaignMob(Entity entity) {
        return entity != null
                && entity.getPersistentData().hasUUID(OWNER_TAG)
                && entity.getPersistentData().contains(ROLE_TAG, Tag.TAG_STRING);
    }

    public static void cleanup(ServerPlayer player) {
        if (player != null && CampaignDirector.isTempleReturn(player)) {
            stripNativeFreezeTags(player);
        }
    }

    private static void teleportDirect(ServerPlayer player, ServerLevel temple, int instance) {
        BlockPos origin = CartenonTempleManagerAccessor.sololeveling3$instanceOrigin(instance);
        BlockPos entry = origin.relative(Direction.SOUTH, 8).above();
        temple.getChunkAt(entry);
        player.stopRiding();
        player.fallDistance = 0.0F;
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(temple, entry.getX() + 0.5D, entry.getY(), entry.getZ() + 0.5D, 0.0F, 0.0F);
        player.setDeltaMovement(Vec3.ZERO);

        CartenonProgressSavedData.get(temple).associateInstance(player.getUUID(), instance);
        player.getPersistentData().putInt(NATIVE_INSTANCE_TAG, instance);
        stripNativeFreezeTags(player);

        CompoundTag progress = CampaignDirector.progress(player);
        progress.putInt(INSTANCE_TAG, instance);
        progress.putLong(CENTER_TAG, origin.asLong());
        progress.putLong(ENTRY_TAG, entry.asLong());
        if (!progress.contains(PHASE_TAG, Tag.TAG_STRING)) {
            progress.putString(PHASE_TAG, PHASE_MAIN);
        }
        progress.remove(ENTERING_TAG);
        CampaignDirector.rememberInside(player, entry);
        CampaignDirector.changed(player);
    }

    private static void scanAndTagStatues(ServerPlayer player, ServerLevel level, BlockPos center, CompoundTag progress) {
        AABB area = new AABB(center).inflate(SCAN_RANGE, 90.0D, SCAN_RANGE);
        EntityType<?> god = entityType(new ResourceLocation("sololeveling", "statue_of_god"));
        if (god != null && !progress.hasUUID(MAIN_DEAD_TAG) && !progress.hasUUID(MAIN_UUID_TAG)) {
            for (Entity entity : level.getEntities(god, area, alive())) {
                if (entity instanceof Mob mob && mayAdopt(player, mob) && !isDeadMain(progress, mob.getUUID())) {
                    tag(player, mob, ROLE_MAIN);
                    progress.putUUID(MAIN_UUID_TAG, mob.getUUID());
                    saveRecord(progress, mob, ROLE_MAIN, god);
                    progress.remove(MISSING_GRACE_TAG);
                    CampaignDirector.changed(player);
                    break;
                }
            }
        }

        for (ResourceLocation id : weaponIds()) {
            EntityType<?> type = entityType(id);
            if (type == null) {
                continue;
            }
            for (Entity entity : level.getEntities(type, area, alive())) {
                if (!(entity instanceof Mob mob) || !mayAdopt(player, mob) || isDeadReceipt(progress, WEAPON_DEAD_TAG, mob.getUUID())) {
                    continue;
                }
                Set<UUID> knownWeapons = weaponUuids(progress);
                if (!knownWeapons.contains(mob.getUUID()) && knownWeapons.size() >= EXPECTED_WEAPONS) {
                    continue;
                }
                tag(player, mob, ROLE_WEAPON);
                saveRecord(progress, mob, ROLE_WEAPON, type);
                if (addUuid(progress, WEAPON_UUIDS_TAG, mob.getUUID())) {
                    CampaignDirector.changed(player);
                }
                prepareWeaponStats(mob);
            }
        }
    }

    private static boolean mayAdopt(ServerPlayer owner, Entity entity) {
        CompoundTag data = entity.getPersistentData();
        if (data.hasUUID(OWNER_TAG) && !owner.getUUID().equals(data.getUUID(OWNER_TAG))) {
            return false;
        }
        if (data.contains(INSTANCE_TAG, Tag.TAG_INT) && data.getInt(INSTANCE_TAG) != CampaignDirector.progress(owner).getInt(INSTANCE_TAG)) {
            return false;
        }
        return true;
    }

    private static EntityType<?> entityType(ResourceLocation id) {
        return ForgeRegistries.ENTITY_TYPES.containsKey(id) ? ForgeRegistries.ENTITY_TYPES.getValue(id) : null;
    }

    private static ResourceLocation entityTypeId(EntityType<?> type) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return id == null ? new ResourceLocation("minecraft", "pig") : id;
    }

    private static Predicate<Entity> alive() {
        return entity -> entity != null && entity.isAlive() && !entity.isRemoved();
    }

    private static Set<ResourceLocation> weaponIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        ids.add(new ResourceLocation("sololeveling", "statuesword"));
        ids.add(new ResourceLocation("sololeveling", "statueaxe"));
        ids.add(new ResourceLocation("sololeveling", "statuehammer"));
        return ids;
    }

    private static void tag(ServerPlayer owner, Mob mob, String role) {
        mob.getPersistentData().putUUID(OWNER_TAG, owner.getUUID());
        mob.getPersistentData().putString(ROLE_TAG, role);
        mob.getPersistentData().putInt(INSTANCE_TAG, CampaignDirector.progress(owner).getInt(INSTANCE_TAG));
        CampaignDirector.tagMob(mob, owner, role);
    }

    private static void saveRecord(CompoundTag progress, Mob mob, String role, EntityType<?> type) {
        ListTag list = progress.getList(STATUE_RECORDS_TAG, Tag.TAG_COMPOUND);
        ListTag copy = new ListTag();
        boolean replaced = false;
        for (Tag tag : list) {
            CompoundTag old = tag instanceof CompoundTag compound ? compound.copy() : new CompoundTag();
            if (old.hasUUID("id") && old.getUUID("id").equals(mob.getUUID())) {
                copy.add(record(mob, role, type));
                replaced = true;
            } else if (!old.isEmpty()) {
                copy.add(old);
            }
        }
        if (!replaced) {
            copy.add(record(mob, role, type));
        }
        progress.put(STATUE_RECORDS_TAG, copy);
    }

    private static CompoundTag record(Mob mob, String role, EntityType<?> type) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", mob.getUUID());
        tag.putString("role", role);
        tag.putString("type", entityTypeId(type).toString());
        tag.putLong("pos", mob.blockPosition().asLong());
        CompoundTag snapshot = new CompoundTag();
        mob.saveWithoutId(snapshot);
        tag.put("snapshot", snapshot);
        return tag;
    }

    /** Keep the same UUID and last saved health if a live actor was removed, never if it died. */
    private static void restoreMissingStatues(ServerPlayer player, ServerLevel level, CompoundTag progress) {
        for (Tag value : progress.getList(STATUE_RECORDS_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag)value;
            UUID id = record.getUUID("id");
            if (isDeadMain(progress, id) || isDeadReceipt(progress, WEAPON_DEAD_TAG, id)) continue;
            BlockPos position = BlockPos.of(record.getLong("pos"));
            if (CampaignGates.waitForLookup(level, id, position)) {
                Entity entity = level.getEntity(id);
                if (entity instanceof Mob mob && mob.isAlive()) {
                    record.putLong("pos", mob.blockPosition().asLong());
                    CompoundTag snapshot = new CompoundTag();
                    mob.saveWithoutId(snapshot);
                    record.put("snapshot", snapshot);
                }
                continue;
            }
            ResourceLocation typeId = ResourceLocation.tryParse(record.getString("type"));
            EntityType<?> type = typeId == null ? null : entityType(typeId);
            if (type == null || !record.contains("snapshot", Tag.TAG_COMPOUND)) continue;
            Entity entity = type.create(level);
            if (!(entity instanceof Mob restored)) continue;
            restored.load(record.getCompound("snapshot").copy());
            restored.setUUID(id);
            tag(player, restored, record.getString("role"));
            if (restored.getHealth() <= 0) { restored.discard(); continue; }
            level.addFreshEntity(restored);
        }
        CampaignDirector.changed(player);
    }

    private static void updateRecordPosition(CompoundTag progress, Mob mob) {
        ListTag list = progress.getList(STATUE_RECORDS_TAG, Tag.TAG_COMPOUND);
        ListTag copy = new ListTag();
        for (Tag tag : list) {
            if (tag instanceof CompoundTag compound && compound.hasUUID("id") && compound.getUUID("id").equals(mob.getUUID())) {
                CompoundTag updated = compound.copy();
                updated.putLong("pos", mob.blockPosition().asLong());
                copy.add(updated);
            } else {
                copy.add(tag.copy());
            }
        }
        progress.put(STATUE_RECORDS_TAG, copy);
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

    private static Optional<Mob> mainStatue(ServerLevel level, CompoundTag progress) {
        if (!progress.hasUUID(MAIN_UUID_TAG)) {
            return Optional.empty();
        }
        UUID id = progress.getUUID(MAIN_UUID_TAG);
        BlockPos pos = recordPosition(progress, id).orElse(progress.contains(CENTER_TAG, Tag.TAG_LONG) ? BlockPos.of(progress.getLong(CENTER_TAG)) : BlockPos.ZERO);
        if (CampaignGates.waitForLookup(level, id, pos)) {
            Entity entity = level.getEntity(id);
            if (entity instanceof Mob mob) {
                return Optional.of(mob);
            }
        }
        return Optional.empty();
    }

    private static Optional<BlockPos> recordPosition(CompoundTag progress, UUID id) {
        for (Tag tag : progress.getList(STATUE_RECORDS_TAG, Tag.TAG_COMPOUND)) {
            if (tag instanceof CompoundTag compound && compound.hasUUID("id") && compound.getUUID("id").equals(id)
                    && compound.contains("pos", Tag.TAG_LONG)) {
                return Optional.of(BlockPos.of(compound.getLong("pos")));
            }
        }
        return Optional.empty();
    }

    private static void keepMainAggressive(ServerPlayer player, Mob main) {
        CompoundTag data = main.getPersistentData();
        data.remove("slr_story_intro_statue");
        data.remove("slr_story_intro_instance");
        data.remove("slr_story_intro_owner");
        data.remove("slr_story_intro_hunter");
        data.putString(STATE_TAG, AGGRESSIVE);
        main.setInvulnerable(false);
        main.setNoAi(false);
        main.setTarget(player);
        updateRecordPosition(CampaignDirector.progress(player), main);
        setAttributeAtMost(main, Attributes.ATTACK_DAMAGE, 18.0D);
        var follow = main.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null && follow.getBaseValue() < 128.0D) {
            follow.setBaseValue(128.0D);
        }
    }

    private static void keepWeaponsDormant(ServerLevel level, CompoundTag progress) {
        for (UUID id : weaponUuids(progress)) {
            Entity entity = level.getEntity(id);
            if (entity instanceof Mob mob && mob.isAlive()) {
                CampaignGates.waitForLookup(level, id, mob.blockPosition());
                mob.setTarget(null);
                mob.setNoAi(true);
                mob.setInvulnerable(true);
                mob.getNavigation().stop();
                mob.setDeltaMovement(Vec3.ZERO);
                mob.fallDistance = 0.0F;
            }
        }
    }

    private static void beginWeaponPhase(ServerPlayer player, CompoundTag progress) {
        if (PHASE_WEAPONS.equals(progress.getString(PHASE_TAG))) {
            return;
        }
        progress.putString(PHASE_TAG, PHASE_WEAPONS);
        CampaignDirector.changed(player);
    }

    private static void wakeWeapons(ServerPlayer player, ServerLevel level, CompoundTag progress) {
        for (UUID id : weaponUuids(progress)) {
            Entity entity = level.getEntity(id);
            if (entity instanceof Mob mob && mob.isAlive()) {
                CampaignGates.waitForLookup(level, id, mob.blockPosition());
                tag(player, mob, ROLE_WEAPON);
                prepareWeaponStats(mob);
                moveWeaponOffPlinthOnce(mob, level, progress);
                updateRecordPosition(progress, mob);
                mob.setInvulnerable(false);
                mob.setNoAi(false);
                mob.setTarget(player);
                mob.getNavigation().moveTo(player, 1.15D);
            } else {
                recordPosition(progress, id).ifPresent(pos -> CampaignGates.waitForLookup(level, id, pos));
            }
        }
    }

    private static void moveWeaponOffPlinthOnce(Mob mob, ServerLevel level, CompoundTag progress) {
        CompoundTag data = mob.getPersistentData();
        if (data.getBoolean(WEAPON_WOKE_TAG)) {
            return;
        }
        BlockPos center = progress.contains(CENTER_TAG, Tag.TAG_LONG) ? BlockPos.of(progress.getLong(CENTER_TAG)) : mob.blockPosition();
        Vec3 from = mob.position();
        Vec3 direction = Vec3.atBottomCenterOf(center.relative(Direction.SOUTH, 77)).subtract(from);
        Vec3 toward = new Vec3(direction.x, 0, direction.z).normalize();
        if (toward.lengthSqr() < 0.01D) {
            toward = new Vec3(0.0D, 0.0D, -1.0D);
        }
        for (int distance : new int[]{5, 7, 9, 11, 13}) {
            BlockPos base = BlockPos.containing(from.add(toward.scale(distance)));
            for (int dy = 0; dy <= 2; dy++) {
                BlockPos feet = new BlockPos(base.getX(), center.getY() + 1 + dy, base.getZ());
                if (!level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                        || !level.getFluidState(feet).isEmpty()
                        || !level.getFluidState(feet.above()).isEmpty()) {
                    continue;
                }
                Vec3 at = Vec3.atBottomCenterOf(feet);
                if (level.noCollision(mob.getBoundingBox().move(at.subtract(mob.position())))) {
                    mob.moveTo(at.x, at.y, at.z, mob.getYRot(), mob.getXRot());
                    mob.setDeltaMovement(Vec3.ZERO);
                    mob.fallDistance = 0.0F;
                    data.putBoolean(WEAPON_WOKE_TAG, true);
                    return;
                }
            }
        }
        // Retry later if another statue temporarily occupies the landing space.
    }

    private static void setAttributeAtMost(Mob mob, Attribute attribute, double value) {
        var instance = mob.getAttribute(attribute);
        if (instance != null && instance.getBaseValue() > value) {
            instance.setBaseValue(value);
        }
    }

    private static void setAttributeAtLeast(Mob mob, Attribute attribute, double value) {
        var instance = mob.getAttribute(attribute);
        if (instance != null && instance.getBaseValue() < value) {
            instance.setBaseValue(value);
        }
    }

    private static void prepareWeaponStats(Mob mob) {
        setAttributeAtLeast(mob, Attributes.ATTACK_DAMAGE, WEAPON_DAMAGE);
        setAttributeAtLeast(mob, Attributes.MOVEMENT_SPEED, WEAPON_SPEED);
        setAttributeAtLeast(mob, Attributes.FOLLOW_RANGE, 192.0D);
    }

    private static void completeWhenAllWeaponsDead(ServerPlayer player, CompoundTag progress) {
        Set<UUID> known = weaponUuids(progress);
        Set<UUID> dead = uuids(progress, WEAPON_DEAD_TAG);
        if (known.size() >= EXPECTED_WEAPONS && dead.containsAll(known)) {
            CampaignDirector.completeEncounter(player);
        }
    }

    private static Set<UUID> weaponUuids(CompoundTag progress) {
        return uuids(progress, WEAPON_UUIDS_TAG);
    }

    private static Set<UUID> uuids(CompoundTag progress, String key) {
        Set<UUID> ids = new HashSet<>();
        ListTag list = progress.getList(key, Tag.TAG_STRING);
        for (Tag tag : list) {
            try {
                ids.add(UUID.fromString(tag.getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return ids;
    }

    private static boolean isDeadReceipt(CompoundTag progress, String key, UUID id) {
        return uuids(progress, key).contains(id);
    }

    private static boolean isDeadMain(CompoundTag progress, UUID id) {
        return progress.hasUUID(MAIN_DEAD_TAG) && progress.getUUID(MAIN_DEAD_TAG).equals(id);
    }

    private static void markMissingGrace(CompoundTag progress, ServerLevel level) {
        long now = level.getGameTime();
        if (!progress.contains(MISSING_GRACE_TAG, Tag.TAG_LONG)) {
            progress.putLong(MISSING_GRACE_TAG, now + 40L);
        }
    }

    private static boolean canRestoreMissing(CompoundTag progress, ServerLevel level, UUID id) {
        if (!progress.contains(MISSING_GRACE_TAG, Tag.TAG_LONG) || level.getGameTime() < progress.getLong(MISSING_GRACE_TAG)) {
            return false;
        }
        Optional<BlockPos> position = recordPosition(progress, id);
        return position.isEmpty() || !CampaignGates.waitForLookup(level, id, position.get());
    }

    private static ServerPlayer ownerPlayer(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level) || !entity.getPersistentData().hasUUID(OWNER_TAG)) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(entity.getPersistentData().getUUID(OWNER_TAG));
    }

    private static boolean isActiveTemplePlayer(ServerPlayer player) {
        return player != null
                && !player.level().isClientSide
                && player.level().dimension().equals(TEMPLE)
                && CampaignDirector.isTempleReturn(player);
    }

    private static void stripNativeFreezeTags(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(NATIVE_PENDING_TAG);
        data.remove(NATIVE_DECLINE_TICKS);
        data.remove(NATIVE_ENTRY_PROTECTION);
    }

    private static void rescueFromVoid(ServerPlayer player) {
        CompoundTag progress = CampaignDirector.progress(player);
        BlockPos entry = progress.contains(ENTRY_TAG, Tag.TAG_LONG) ? BlockPos.of(progress.getLong(ENTRY_TAG)) : player.blockPosition();
        if (player.getY() > entry.getY() - 12.0D) {
            return;
        }
        player.teleportTo(player.serverLevel(), entry.getX() + 0.5D, entry.getY(), entry.getZ() + 0.5D, player.getYRot(), 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.setHealth(Math.max(1.0F, player.getHealth()));
    }

    private static void loadTempleChunks(ServerLevel level, BlockPos center) {
        int minX = center.getX() - 190;
        int maxX = center.getX() + 190;
        int minZ = center.getZ() - 40;
        int maxZ = center.getZ() + 170;
        for (int x = minX; x <= maxX; x += 16) {
            for (int z = minZ; z <= maxZ; z += 16) {
                level.getChunkAt(new BlockPos(x, center.getY(), z));
            }
        }
    }

    private static boolean owns(ServerPlayer player, Entity entity) {
        return player != null
                && entity != null
                && entity.getPersistentData().hasUUID(OWNER_TAG)
                && player.getUUID().equals(entity.getPersistentData().getUUID(OWNER_TAG));
    }
}
