package dev.eness.sololeveling3.campaign;

import dev.eness.sololeveling3.SoloLeveling3;
import dev.eness.sololeveling3.entity.IceMonarchEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.solocraft.SololevelingMod;
import net.solocraft.dungeon.runtime.SnowRedGateArenaManager;
import net.solocraft.network.UrgentQuestStatusMessage;
import net.solocraft.util.SystemNotifications;
import net.solocraft.util.UrgentQuestManager;
import java.util.UUID;

/** The server owns the full quest chain and commits progress only once. */
public final class CampaignDirector {
    public static final String OWNER_TAG = "sl3_campaign_owner";
    public static final String ROLE_TAG = "sl3_campaign_role";
    public static final ResourceKey<Level> ARENA = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "campaign_arena"));
    public static final ResourceKey<Level> TEMPLE = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("sololeveling", "cartenon_temple"));

    public static CompoundTag progress(ServerPlayer player) {
        CampaignSavedData saved = CampaignSavedData.get(player.server);
        CompoundTag data = saved.player(player.getUUID());
        if (data.getInt("campaign_revision") < 2) {
            CampaignStage previous = CampaignStage.read(data.getString("stage"));
            // Insert Legia for saves that have not yet entered the Frost chapter.
            // Active fights and later chapters retain their accepted progress.
            if (previous == CampaignStage.NORMAL_BEFORE_FROST) {
                data.putString("stage", CampaignStage.NORMAL_BEFORE_LEGIA.name());
                data.remove("normal_gate_serial");
            } else if (previous == CampaignStage.FROST_READY) {
                data.putString("stage", CampaignStage.LEGIA_KEYS.name());
                data.putBoolean("retire_legacy_entry", true);
            }
            ListTag migrated = new ListTag();
            for (Tag tag : data.getList("normal_receipts", Tag.TAG_STRING)) {
                String receipt = tag.getAsString();
                if (receipt.startsWith("NORMAL_BEFORE_FROST:")) receipt = "NORMAL_BEFORE_LEGIA:" + receipt.substring("NORMAL_BEFORE_FROST:".length());
                migrated.add(StringTag.valueOf(receipt));
            }
            data.put("normal_receipts", migrated);
            data.remove("pending_normal_receipt");
            data.putInt("campaign_revision", 2);
            saved.setDirty();
        }
        return data;
    }
    public static void changed(ServerPlayer player) { CampaignSavedData.get(player.server).setDirty(); }
    public static CampaignStage stage(ServerPlayer player) { return CampaignStage.read(progress(player).getString("stage")); }
    public static boolean isTempleReturn(ServerPlayer player) { return stage(player) == CampaignStage.DOUBLE_ACTIVE; }
    public static boolean awaitingFrost(ServerPlayer player) { return stage(player) == CampaignStage.FROST_READY || stage(player) == CampaignStage.FROST_ACTIVE; }

    public static void tagMob(Mob mob, ServerPlayer owner, String role) {
        mob.getPersistentData().putUUID(OWNER_TAG, owner.getUUID());
        mob.getPersistentData().putString(ROLE_TAG, role);
        mob.setPersistenceRequired();
    }

    public static void rememberInside(ServerPlayer player, BlockPos center) {
        CompoundTag data = progress(player);
        data.putString("inside_dimension", player.level().dimension().location().toString());
        data.putLong("inside_center", center.asLong());
        changed(player);
    }

    public static void completeEncounter(ServerPlayer player) {
        if (!stage(player).activeDungeon()) return;
        CompoundTag data = progress(player);
        if (!data.getBoolean("encounter_complete")) {
            data.putBoolean("encounter_complete", true);
            changed(player);
            message(player, "Savaş sona erdi. Yakınındaki mavi çıkış kapısından dön.", ChatFormatting.AQUA);
            award(player, switch(stage(player)) {
                case DOUBLE_ACTIVE -> "double_dungeon_return";
                case LEGIA_ACTIVE -> "monarch_of_beginning_defeated";
                case FROST_ACTIVE -> "ice_monarch_retreat";
                case TRIO_ACTIVE -> "three_monarchs";
                case FINAL_ACTIVE -> "antares_final";
                default -> "monarchs_awaken";
            });
        }
        if (stage(player) != CampaignStage.FROST_ACTIVE) CampaignGates.ensure(player, true);
    }

    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || !player.isAlive()) return;
        CompoundTag data = progress(player);
        if (data.getBoolean("retire_legacy_entry")) {
            CampaignGates.remove(player, false);
            data.remove("retire_legacy_entry"); changed(player);
        }
        CampaignStage current = stage(player);
        if (current == CampaignStage.WAIT_BARAN && player.tickCount % 20 == 0 && CampaignSlrBridge.hasBaranCleared(player)) {
            setStage(player, CampaignStage.DOUBLE_READY);
            award(player, "monarchs_awaken");
            announce(player, "ÇİFT ZİNDANA DÖNÜŞ", "Kartenon Tapınağı seni yeniden çağırıyor. Giriş için 100. seviyeye ulaş.");
            current = stage(player);
        }
        if (current == CampaignStage.DOUBLE_ACTIVE && player.level().dimension().equals(TEMPLE)) TempleReturnEncounter.tick(player);
        if ((current == CampaignStage.TRIO_ACTIVE || current == CampaignStage.FINAL_ACTIVE) && player.level().dimension().equals(ARENA)) CampaignArenas.tick(player);
        if (current == CampaignStage.FROST_ACTIVE && player.tickCount % 10 == 0 && player.level().dimension().equals(SnowRedGateArenaManager.SNOW_DIMENSION)) tickFrost(player);

        if (player.tickCount % 5 == 0 && !player.isSpectator()) {
            for (Entity gate : player.serverLevel().getEntities(player, player.getBoundingBox().inflate(1.6, 1.5, 1.6),
                    entity -> entity.getPersistentData().getBoolean(CampaignGates.TAG))) {
                if (CampaignGates.canUse(player, gate)) { useGate(player, gate); break; }
            }
        }
        if (player.tickCount % 20 != 0) return;
        LegiaCampaign.tick(player);
        CampaignRewards.tick(player);
        current = stage(player);
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            if (current.activeDungeon() && data.getBoolean("encounter_complete")) finishReturn(player);
            current = stage(player);
            if (current.normalGates()) tickNormalGates(player);
            else if (current == CampaignStage.FROST_READY) ensureFrostGate(player);
            else if (current != CampaignStage.WAIT_BARAN && current != CampaignStage.COMPLETE
                    && current != CampaignStage.FROST_ACTIVE && current != CampaignStage.LEGIA_KEYS) CampaignGates.ensure(player, false);
            else if (current == CampaignStage.FROST_ACTIVE && !data.getBoolean("encounter_complete")) ensureFrostGate(player);
        } else if (current.activeDungeon() && data.getBoolean("encounter_complete")) {
            if (current == CampaignStage.FROST_ACTIVE) CampaignSlrBridge.ensureFrostExit(player);
            else CampaignGates.ensure(player, true);
        }
        syncOverlay(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getTarget().getPersistentData().getBoolean(CampaignGates.TAG)) return;
        event.setCanceled(true); event.setCancellationResult(InteractionResult.CONSUME);
        if (CampaignGates.canUse(player, event.getTarget())) useGate(player, event.getTarget());
    }

    private static void useGate(ServerPlayer player, Entity gate) {
        CompoundTag data = progress(player);
        long now = player.serverLevel().getGameTime();
        if (now < data.getLong("gate_cooldown")) return;
        data.putLong("gate_cooldown", now + 40); changed(player);
        if (gate.getPersistentData().getBoolean("exit")) {
            if (!data.getBoolean("encounter_complete") || !stage(player).activeDungeon()) return;
            if (CampaignGates.returnPlayer(player)) finishReturn(player);
            return;
        }
        CampaignStage before = stage(player);
        if ((before == CampaignStage.DOUBLE_READY || before == CampaignStage.DOUBLE_ACTIVE) && CampaignSlrBridge.level(player) < 100) {
            message(player, "Bu kapıya girmek için 100. seviye olmalısın. Mevcut seviye: " + (int)CampaignSlrBridge.level(player), ChatFormatting.RED);
            return;
        }
        if (!player.level().dimension().equals(Level.OVERWORLD)) return;
        CampaignGates.rememberReturn(player);
        CampaignStage active = switch(before) {
            case DOUBLE_READY -> CampaignStage.DOUBLE_ACTIVE;
            case LEGIA_READY -> CampaignStage.LEGIA_ACTIVE;
            case FROST_READY -> CampaignStage.FROST_ACTIVE;
            case TRIO_READY -> CampaignStage.TRIO_ACTIVE;
            case FINAL_READY -> CampaignStage.FINAL_ACTIVE;
            default -> before;
        };
        if (!active.activeDungeon()) return;
        if (!before.activeDungeon()) { data.putBoolean("encounter_complete", false); setStage(player, active); }
        boolean entered;
        try {
            entered = switch(active) {
                case DOUBLE_ACTIVE -> TempleReturnEncounter.enter(player);
                case LEGIA_ACTIVE -> LegiaCampaign.enter(player, gate);
                case FROST_ACTIVE -> CampaignSlrBridge.enterFrost(player, gate);
                case TRIO_ACTIVE, FINAL_ACTIVE -> CampaignArenas.enter(player);
                default -> false;
            };
        } catch (RuntimeException error) {
            SoloLeveling3.LOGGER.error("Campaign entry failed for {} at {}", player.getUUID(), active, error);
            entered = false;
        }
        if (!entered) {
            if (stage(player) == active) setStage(player, before);
            message(player, "Kapı henüz açılamadı. İlerlemen korundu; tekrar deneyebilirsin.", ChatFormatting.RED);
        }
    }

    private static void ensureFrostGate(ServerPlayer player) {
        CompoundTag data = progress(player), saved = data.getCompound("entry_gate");
        if (saved.hasUUID("id") && saved.getString("dimension").equals(player.level().dimension().location().toString())) {
            boolean waiting = CampaignGates.waitForLookup(player.serverLevel(), saved.getUUID("id"), BlockPos.of(saved.getLong("position")));
            Entity existing = player.serverLevel().getEntity(saved.getUUID("id"));
            if (existing != null && !existing.isRemoved()) return;
            if (waiting) return;
        }
        if (player.serverLevel().getGameTime() < data.getLong("frost_gate_retry")) return;
        data.putLong("frost_gate_retry", player.serverLevel().getGameTime() + 100);
        Entity gate = CampaignSlrBridge.spawnFrostGate(player);
        if (gate == null) { changed(player); return; }
        gate.getPersistentData().putBoolean(CampaignGates.TAG, true);
        gate.getPersistentData().putUUID(OWNER_TAG, player.getUUID());
        gate.getPersistentData().putLong("created", player.serverLevel().getGameTime());
        CompoundTag tag = new CompoundTag(); tag.putUUID("id", gate.getUUID());
        tag.putString("dimension", player.level().dimension().location().toString()); tag.putLong("position", gate.blockPosition().asLong());
        data.put("entry_gate", tag); changed(player);
    }

    private static void tickNormalGates(ServerPlayer player) {
        CompoundTag data = progress(player);
        int count = normalCount(player);
        if (count >= 2) {
            CampaignStage next = stage(player).afterNormalGates();
            setStage(player, next);
            if (next == CampaignStage.LEGIA_KEYS) announce(player, "ÜÇ KADİM ANAHTAR", "A veya S rank zindan bosslarından 3 Kadim Anahtar topla. Legia'nın hapishanesi seni bekliyor.");
            else if (next == CampaignStage.FROST_READY) announce(player, "DONMUŞ TAHT", "Kırmızı Kapı açıldı. Baruka'yı bul; Buz Hükümdarı'nın izini sür.");
            else if (next == CampaignStage.TRIO_READY) announce(player, "ÜÇ HÜKÜMDAR", "Seni bekliyoruz, Gölge Hükümdarı.");
            else announce(player, "EJDERHALARIN İSTİLASI", "Ejderhalar şehri istila etmeye başladı!");
            return;
        }
        long now = player.serverLevel().getGameTime();
        if (now < data.getLong("normal_gate_retry")) return;
        String serial = stage(player).name() + ":" + count;
        if (serial.equals(data.getString("normal_gate_serial")) && data.hasUUID("normal_gate_id")) {
            BlockPos position = BlockPos.of(data.getLong("normal_gate_position"));
            boolean waiting = CampaignGates.waitForLookup(player.serverLevel(), data.getUUID("normal_gate_id"), position);
            Entity old = player.serverLevel().getEntity(data.getUUID("normal_gate_id"));
            if (old != null && CampaignSlrBridge.isUsableNormalGate(old)) return;
            if (old == null && waiting) return;
            if (old != null && old.getPersistentData().hasUUID(CampaignSlrBridge.OWNER_TAG)
                    && old.getPersistentData().getUUID(CampaignSlrBridge.OWNER_TAG).equals(player.getUUID())) old.discard();
        }
        data.putLong("normal_gate_retry", now + 200);
        Entity gate = CampaignSlrBridge.spawnNormalGate(player);
        if (gate != null) {
            data.putString("normal_gate_serial", serial); data.putUUID("normal_gate_id", gate.getUUID());
            data.putLong("normal_gate_position", gate.blockPosition().asLong());
            message(player, "Normal kapı " + (count + 1) + "/2 açıldı: " + gate.blockPosition().getX() + ", " + gate.blockPosition().getY() + ", " + gate.blockPosition().getZ(), ChatFormatting.AQUA);
        }
        changed(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) public static void beforeTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (stage(player).normalGates() && event.getDimension().equals(Level.OVERWORLD)) {
            progress(player).remove("pending_normal_receipt");
            String receipt = CampaignSlrBridge.normalReceipt(player, player.serverLevel());
            if (!receipt.isBlank()) {
                progress(player).putString("pending_normal_receipt", stage(player).name() + ":" + receipt);
                changed(player);
            }
        }
        if (stage(player) == CampaignStage.FROST_READY && player.level().dimension().equals(Level.OVERWORLD)) CampaignGates.rememberReturn(player);
    }

    @SubscribeEvent public static void afterTravel(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag data = progress(player);
        if (event.getTo().equals(Level.OVERWORLD)) {
            String pending = data.getString("pending_normal_receipt");
            if (stage(player).normalGates() && pending.startsWith(stage(player).name() + ":")) {
                ListTag receipts = data.getList("normal_receipts", Tag.TAG_STRING);
                String raw = pending.substring(pending.indexOf(':') + 1);
                boolean alreadyUsed = receipts.stream().map(Tag::getAsString).anyMatch(receipt -> receipt.endsWith(":" + raw));
                if (!alreadyUsed) {
                    receipts.add(StringTag.valueOf(pending));
                    data.put("normal_receipts", receipts);
                    data.remove("normal_gate_serial");
                    data.putLong("normal_gate_retry", player.serverLevel().getGameTime() + 80);
                }
            }
            data.remove("pending_normal_receipt"); changed(player);
            if (stage(player).activeDungeon() && data.getBoolean("encounter_complete")) finishReturn(player);
        } else if (event.getTo().equals(SnowRedGateArenaManager.SNOW_DIMENSION) && awaitingFrost(player)) {
            setStage(player, CampaignStage.FROST_ACTIVE);
            rememberInside(player, player.blockPosition());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) public static void frostDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof IceMonarchEntity boss) || !(boss.level() instanceof ServerLevel level)) return;
        ServerPlayer owner = frostParticipant(level, boss);
        if (owner == null || progress(owner).getBoolean("frost_fled")) return;
        if (boss.getHealth() - event.getAmount() <= boss.getMaxHealth() * .30F) {
            event.setCanceled(true);
            boss.setHealth(Math.max(1, boss.getMaxHealth() * .30F));
            retreat(owner, boss);
        }
    }

    private static void tickFrost(ServerPlayer player) {
        if (progress(player).getBoolean("frost_fled")) return;
        String instance = player.getPersistentData().getString("slr_dungeon_instance");
        for (IceMonarchEntity boss : player.serverLevel().getEntitiesOfClass(IceMonarchEntity.class, player.getBoundingBox().inflate(256), Entity::isAlive)) {
            if (instance.isBlank() || !instance.equals(boss.getPersistentData().getString("slr_dungeon_instance"))) continue;
            tagMob(boss, player, "frost_intro");
            if (boss.getHealth() <= boss.getMaxHealth() * .30F || boss.getPersistentData().getBoolean("sl3_frost_retreat_pending")) retreat(player, boss);
        }
    }

    private static ServerPlayer frostParticipant(ServerLevel level, IceMonarchEntity boss) {
        if (!level.dimension().equals(SnowRedGateArenaManager.SNOW_DIMENSION)) return null;
        String instance = boss.getPersistentData().getString("slr_dungeon_instance");
        if (instance.isBlank()) return null;
        return level.players().stream().filter(p -> stage(p) == CampaignStage.FROST_ACTIVE
                && instance.equals(p.getPersistentData().getString("slr_dungeon_instance"))).findFirst().orElse(null);
    }

    private static void retreat(ServerPlayer player, IceMonarchEntity boss) {
        CompoundTag data = progress(player);
        if (data.getBoolean("frost_fled")) return;
        boss.getPersistentData().putBoolean("sl3_frost_retreat_pending", true);
        boss.setInvulnerable(true); boss.setNoAi(true); boss.setTarget(null); boss.getNavigation().stop();
        try {
            if (!CampaignSlrBridge.fleeCompletion(player, boss)) return;
            data.putBoolean("frost_fled", true); changed(player);
            player.serverLevel().sendParticles(ParticleTypes.SNOWFLAKE, boss.getX(), boss.getY() + 1, boss.getZ(), 90, .7, 1, .7, .06);
            player.serverLevel().playSound(null, boss.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1, .7F);
            String instance = boss.getPersistentData().getString("slr_dungeon_instance");
            for (ServerPlayer participant : player.serverLevel().players()) {
                if (stage(participant) != CampaignStage.FROST_ACTIVE || !instance.equals(participant.getPersistentData().getString("slr_dungeon_instance"))) continue;
                progress(participant).putBoolean("frost_fled", true);
                progress(participant).putString("frost_instance", instance);
                changed(participant);
                message(participant, "Sonra görüşürüz, Gölge Hükümdarı. O zamana dek bekle.", ChatFormatting.AQUA);
                completeEncounter(participant);
            }
            boss.discard();
        } catch (RuntimeException error) {
            if (player.tickCount % 100 == 0) SoloLeveling3.LOGGER.error("Frost retreat is waiting for a safe exit", error);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof IceMonarchEntity boss && boss.level() instanceof ServerLevel level) {
            ServerPlayer player = frostParticipant(level, boss);
            if (player != null && !progress(player).getBoolean("frost_fled")) {
                event.setCanceled(true); boss.setHealth(Math.max(1, boss.getMaxHealth() * .30F)); retreat(player, boss); return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) public static void recordArenaDeath(LivingDeathEvent event) {
        if (!event.isCanceled()) {
            CampaignArenas.onDeath(event);
            LegiaCampaign.onDeath(event);
        }
    }

    private static void finishReturn(ServerPlayer player) {
        CampaignStage old = stage(player);
        if (!old.activeDungeon() || !progress(player).getBoolean("encounter_complete")) return;
        CampaignGates.remove(player, false); CampaignGates.remove(player, true);
        if (old == CampaignStage.FROST_ACTIVE) CampaignSlrBridge.cleanupAfterFrostExit(player);
        if (old == CampaignStage.DOUBLE_ACTIVE) TempleReturnEncounter.cleanup(player);
        if (old == CampaignStage.LEGIA_ACTIVE) LegiaCampaign.cleanupAfterExit(player);
        CampaignRewards.grantOnReturn(player, old);
        CampaignStage next = switch(old) {
            case DOUBLE_ACTIVE -> CampaignStage.NORMAL_BEFORE_LEGIA;
            case LEGIA_ACTIVE -> CampaignStage.NORMAL_BEFORE_FROST;
            case FROST_ACTIVE -> CampaignStage.NORMAL_BEFORE_TRIO;
            case TRIO_ACTIVE -> CampaignStage.NORMAL_BEFORE_FINAL;
            case FINAL_ACTIVE -> CampaignStage.COMPLETE;
            default -> old;
        };
        setStage(player, next);
        CompoundTag data = progress(player);
        data.remove("encounter_complete"); data.remove("inside_dimension"); data.remove("pending_normal_receipt");
        data.remove("normal_gate_serial"); data.putLong("normal_gate_retry", player.serverLevel().getGameTime() + 80);
        if (next == CampaignStage.COMPLETE) announce(player, "HİKÂYE TAMAMLANDI", "Antares yenildi. Ejderhaların istilası sona erdi, Gölge Hükümdarı.");
        else {
            announce(player, "YENİ KAPILAR", "Bir sonraki hikâye kapısından önce iki normal zindanı tamamla. (0/2)");
        }
        changed(player);
    }

    private static int normalCount(ServerPlayer player) {
        String prefix = stage(player).name() + ":";
        return (int)progress(player).getList("normal_receipts", Tag.TAG_STRING).stream()
                .map(Tag::getAsString).filter(s -> s.startsWith(prefix)).distinct().count();
    }

    public static void setStage(ServerPlayer player, CampaignStage stage) { progress(player).putString("stage", stage.name()); changed(player); }

    public static String status(ServerPlayer player) {
        return stage(player).name() + " | Seviye " + (int)CampaignSlrBridge.level(player)
                + (stage(player).normalGates() ? " | Normal kapı " + normalCount(player) + "/2" : "")
                + (stage(player) == CampaignStage.LEGIA_KEYS || stage(player) == CampaignStage.LEGIA_READY || stage(player) == CampaignStage.LEGIA_ACTIVE
                    ? " | " + LegiaCampaign.overlayProgress(player).getString() : "")
                + " | Tamamlandı: " + progress(player).getBoolean("encounter_complete");
    }

    private static void syncOverlay(ServerPlayer player) {
        if (UrgentQuestManager.hasActiveQuest(player)) return;
        CampaignStage current = stage(player);
        boolean active = current != CampaignStage.WAIT_BARAN && current != CampaignStage.COMPLETE;
        String title = current.normalGates() ? "İKİ NORMAL KAPI" : switch(current) {
            case DOUBLE_READY, DOUBLE_ACTIVE -> "ÇİFT ZİNDANA DÖNÜŞ";
            case LEGIA_KEYS, LEGIA_READY, LEGIA_ACTIVE -> "LEGIA'NIN HAPİSHANESİ";
            case FROST_READY, FROST_ACTIVE -> "DONMUŞ TAHT";
            case TRIO_READY, TRIO_ACTIVE -> "ÜÇ HÜKÜMDAR";
            case FINAL_READY, FINAL_ACTIVE -> "EJDERHALARIN İSTİLASI";
            default -> "";
        };
        String objective = progress(player).getBoolean("encounter_complete") ? "Yakınındaki mavi çıkış kapısından dön." : current.normalGates()
                ? "İki farklı normal SLR zindanını tamamlayıp geri dön." : switch(current) {
            case DOUBLE_READY -> CampaignSlrBridge.level(player) < 100 ? "100. seviyeye ulaş. Çift zindanın kapısı seni bekliyor." : "Mavi kapıdan Kartenon Tapınağı'na yeniden gir.";
            case DOUBLE_ACTIVE -> "Tanrı heykelini, ardından uyanan 12 heykeli yen.";
            case LEGIA_KEYS -> "A/S rank bosslarını yen. " + LegiaCampaign.overlayProgress(player).getString();
            case LEGIA_READY, LEGIA_ACTIVE -> LegiaCampaign.overlayProgress(player).getString();
            case FROST_READY -> "Kırmızı Kapı'ya gir ve Baruka'yı bul.";
            case FROST_ACTIVE -> "Baruka'yı yen; Buz Hükümdarı'nın canını %30'a indir.";
            case TRIO_READY -> "Mavi kapıdan üç hükümdarın arenasına gir.";
            case TRIO_ACTIVE -> "Sillad, Rakan ve Tarnak'ı yen.";
            case FINAL_READY -> "Mavi kapıdan Antares'in tahtına ulaş.";
            case FINAL_ACTIVE -> "Yıkım Hükümdarı Antares'i yen.";
            default -> "";
        };
        SololevelingMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player),
                new UrgentQuestStatusMessage(active, title, objective, current.normalGates() ? "gate" : "kill", current.normalGates() ? Math.min(2, normalCount(player)) : 0, current.normalGates() ? 2 : 0, -1));
    }

    private static void announce(ServerPlayer player, String title, String objective) {
        SystemNotifications.showTitleUnder(player, 0xFF70D7FF, 120, Component.literal(title), Component.literal(objective));
        message(player, title + " — " + objective, ChatFormatting.AQUA);
    }

    public static void message(ServerPlayer player, String text, ChatFormatting color) {
        player.sendSystemMessage(Component.literal("[SYSTEM] " + text).withStyle(color));
    }

    public static void award(ServerPlayer player, String path) {
        var advancement = player.server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath(SoloLeveling3.MOD_ID, "story/" + path));
        if (advancement == null) return;
        var state = player.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : state.getRemainingCriteria()) player.getAdvancements().award(advancement, criterion);
    }

    private CampaignDirector() {}
}
