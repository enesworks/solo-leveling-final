package dev.eness.sololeveling3.campaign;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Prevents player death inside the authored three-monarch arena while keeping the player in place. */
public final class CampaignSurvival {
    private static final String ARENA_KIND = "arena_kind";
    private static final String ARENA_CENTER = "arena_center";
    private static final String SURVIVAL_UNTIL = "trio_survival_until";
    private static final String SURVIVAL_CENTER = "trio_survival_center";
    private static final int PROTECTION_TICKS = 100;
    private static final int ARENA_RADIUS = 240;

    private CampaignSurvival() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isProtected(player)) {
            event.setCanceled(true);
            floorHealth(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isProtected(player)) {
            event.setCanceled(true);
            floorHealth(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isTrioArena(player)) {
            return;
        }
        if (isProtected(player)) {
            event.setCanceled(true);
            floorHealth(player);
            return;
        }
        if (player.getHealth() - event.getAmount() <= 0.0F) {
            beginProtection(player);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isTrioArena(player)) {
            return;
        }
        event.setCanceled(true);
        if (!isProtected(player)) {
            beginProtection(player);
        } else {
            floorHealth(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag record = CampaignDirector.progress(player);
        if (record.contains(SURVIVAL_UNTIL)
                && (!isTrioArena(player)
                || player.serverLevel().getGameTime() >= record.getLong(SURVIVAL_UNTIL)
                || !survivalCenterMatches(record))) {
            record.remove(SURVIVAL_UNTIL);
            record.remove(SURVIVAL_CENTER);
            CampaignDirector.changed(player);
        }
    }

    private static void beginProtection(ServerPlayer player) {
        CompoundTag record = CampaignDirector.progress(player);
        long now = player.serverLevel().getGameTime();
        long until = record.getLong(SURVIVAL_UNTIL);
        if (now < until) {
            floorHealth(player);
            return;
        }

        player.setHealth(1.0F);
        record.putLong(SURVIVAL_UNTIL, now + PROTECTION_TICKS);
        if (record.contains(ARENA_CENTER)) {
            record.putLong(SURVIVAL_CENTER, record.getLong(ARENA_CENTER));
        }
        CampaignDirector.changed(player);
        player.displayClientMessage(Component.literal("Ölümcül vuruştan 1 canla kurtuldun. 5 saniye korunuyorsun; uzaklaş ve iyileş!")
                .withStyle(ChatFormatting.RED), true);
    }

    private static boolean isProtected(ServerPlayer player) {
        if (!isTrioArena(player)) {
            return false;
        }
        CompoundTag record = CampaignDirector.progress(player);
        if (!survivalCenterMatches(record)) {
            return false;
        }
        long until = record.getLong(SURVIVAL_UNTIL);
        return player.serverLevel().getGameTime() < until;
    }

    private static boolean isTrioArena(ServerPlayer player) {
        if (player == null || player.server == null || player.level().isClientSide) {
            return false;
        }
        if (CampaignDirector.stage(player) != CampaignStage.TRIO_ACTIVE
                || !player.level().dimension().equals(CampaignDirector.ARENA)) {
            return false;
        }

        CompoundTag record = CampaignDirector.progress(player);
        if (!CampaignStage.TRIO_ACTIVE.name().equals(record.getString(ARENA_KIND)) || !record.contains(ARENA_CENTER)) {
            return false;
        }

        BlockPos center = BlockPos.of(record.getLong(ARENA_CENTER));
        return Math.abs(player.getX() - center.getX()) <= ARENA_RADIUS
                && Math.abs(player.getZ() - center.getZ()) <= ARENA_RADIUS;
    }

    private static boolean survivalCenterMatches(CompoundTag record) {
        return !record.contains(SURVIVAL_CENTER)
                || record.getLong(SURVIVAL_CENTER) == record.getLong(ARENA_CENTER);
    }

    private static void floorHealth(ServerPlayer player) {
        if (player.getHealth() < 1.0F) {
            player.setHealth(1.0F);
        }
    }
}
