package dev.eness.sololevelingfinal.core.combat;

import dev.eness.sololevelingfinal.core.SoloLeveling3;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Completes SLR's otherwise empty Go Gunhee vessel path. All authoritative
 * combat work runs on the logical server and uses SLR's own MP/cooldown data.
 */
public final class GoGunheeCombatManager {
    public static final String BRILLIANT_REINFORCEMENT = "Brilliant Reinforcement";
    public static final String RULERS_IMPACT = "Ruler's Impact";
    public static final String FRAGMENTS_GUARD = "Fragment's Guard";
    public static final String BRILLIANT_MANIFESTATION = "Brilliant Manifestation";

    public static final int SKILL_COLOR = 0xFFE04E;
    public static final List<String> SKILLS = List.of(
            BRILLIANT_REINFORCEMENT,
            RULERS_IMPACT,
            FRAGMENTS_GUARD,
            BRILLIANT_MANIFESTATION
    );

    private static final String MANIFESTED_TAG = "sololeveling3_go_gunhee_manifested";
    private static final String GUARD_UNTIL_TAG = "sololeveling3_go_gunhee_guard_until";
    private static final String GUARD_POWER_TAG = "sololeveling3_go_gunhee_guard_power";
    private static final int MANIFESTATION_DRAIN = 18;
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.72F, 0.12F), 1.25F);
    private static final DustParticleOptions PALE_GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.94F, 0.58F), 0.9F);
    private static final Set<UUID> INTERNAL_DAMAGE = new HashSet<>();

    private GoGunheeCombatManager() {
    }

    public static boolean isSkill(String skill) {
        return SKILLS.contains(skill);
    }

    public static boolean isGoGunheeVessel(Entity entity) {
        if (entity == null) {
            return false;
        }
        SololevelingModVariables.PlayerVariables vars = variables(entity);
        return (int) vars.JOB == 8 && "go_gunhee".equals(vars.vesselIdentity);
    }

    public static List<String> unlockedSkills(Entity entity) {
        if (!isGoGunheeVessel(entity)) {
            return List.of();
        }

        int level = Math.max(0, (int) Math.floor(variables(entity).Level));
        ArrayList<String> result = new ArrayList<>();
        result.add(BRILLIANT_REINFORCEMENT);
        if (level >= 55) {
            result.add(RULERS_IMPACT);
        }
        if (level >= 75) {
            result.add(FRAGMENTS_GUARD);
        }
        if (level >= 95) {
            result.add(BRILLIANT_MANIFESTATION);
        }
        return List.copyOf(result);
    }

    public static boolean cast(Entity entity, String skill) {
        if (!isSkill(skill)) {
            return false;
        }
        if (!(entity instanceof ServerPlayer player)) {
            return true;
        }
        if (!isGoGunheeVessel(player) || !unlockedSkills(player).contains(skill)) {
            player.displayClientMessage(Component.translatable("message.sololeveling3.go_gunhee.locked"), true);
            return true;
        }

        switch (skill) {
            case BRILLIANT_REINFORCEMENT -> castReinforcement(player);
            case RULERS_IMPACT -> castImpact(player);
            case FRAGMENTS_GUARD -> castGuard(player);
            case BRILLIANT_MANIFESTATION -> toggleManifestation(player);
            default -> {
            }
        }
        return true;
    }

    public static List<Component> tooltip(Entity entity, String skill) {
        boolean manifested = isManifested(entity);
        ArrayList<Component> lines = new ArrayList<>();
        lines.add(Component.literal(skill).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        switch (skill) {
            case BRILLIANT_REINFORCEMENT -> {
                lines.add(Component.translatable("tooltip.sololeveling3.go_gunhee.reinforcement").withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable(manifested
                        ? "tooltip.sololeveling3.go_gunhee.reinforcement.manifested"
                        : "tooltip.sololeveling3.go_gunhee.reinforcement.normal").withStyle(ChatFormatting.YELLOW));
            }
            case RULERS_IMPACT -> {
                lines.add(Component.translatable("tooltip.sololeveling3.go_gunhee.impact").withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable(manifested
                        ? "tooltip.sololeveling3.go_gunhee.impact.manifested"
                        : "tooltip.sololeveling3.go_gunhee.impact.normal").withStyle(ChatFormatting.YELLOW));
            }
            case FRAGMENTS_GUARD -> {
                lines.add(Component.translatable("tooltip.sololeveling3.go_gunhee.guard").withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable(manifested
                        ? "tooltip.sololeveling3.go_gunhee.guard.manifested"
                        : "tooltip.sololeveling3.go_gunhee.guard.normal").withStyle(ChatFormatting.YELLOW));
            }
            case BRILLIANT_MANIFESTATION -> {
                lines.add(Component.translatable("tooltip.sololeveling3.go_gunhee.manifestation").withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable(manifested
                        ? "tooltip.sololeveling3.go_gunhee.manifestation.active"
                        : "tooltip.sololeveling3.go_gunhee.manifestation.normal").withStyle(ChatFormatting.YELLOW));
            }
            default -> {
            }
        }
        return List.copyOf(lines);
    }

    public static boolean isManifested(Entity entity) {
        return entity != null && entity.getPersistentData().getBoolean(MANIFESTED_TAG);
    }

    private static void castReinforcement(ServerPlayer player) {
        if (!ready(player, BRILLIANT_REINFORCEMENT)) {
            return;
        }
        boolean manifested = isManifested(player);
        int mana = manifested ? 290 : 220;
        if (!consumeMana(player, mana)) {
            return;
        }

        int duration = manifested ? 340 : 240;
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, manifested ? 3 : 2, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, manifested ? 2 : 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, manifested ? 2 : 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, manifested ? 3 : 1, false, true, true));
        CooldownManager.set(player, BRILLIANT_REINFORCEMENT, manifested ? 280 : 360);
        brilliantBurst(player.serverLevel(), player.position().add(0.0D, 1.0D, 0.0D), manifested ? 2.8D : 2.1D, 56);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, manifested ? 0.72F : 0.9F);
    }

    private static void castImpact(ServerPlayer player) {
        if (!ready(player, RULERS_IMPACT)) {
            return;
        }
        boolean manifested = isManifested(player);
        int mana = manifested ? 390 : 320;
        if (!consumeMana(player, mana)) {
            return;
        }

        CooldownManager.set(player, RULERS_IMPACT, manifested ? 105 : 145);
        ServerLevel level = player.serverLevel();
        Vec3 forward = horizontalLook(player);
        double reach = manifested ? 9.5D : 7.0D;
        double width = manifested ? 5.2D : 3.8D;
        Vec3 center = player.position().add(forward.scale(reach * 0.5D));
        double strength = TemporaryStatBonusManager.effectiveStrength(player);
        double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damage = (float) ((16.0D + attack * 1.15D + strength / 5.5D) * (manifested ? 1.48D : 1.0D));

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(center, center).inflate(width, 3.0D, width),
                candidate -> validTarget(player, candidate)
        )) {
            Vec3 offset = target.position().subtract(player.position());
            Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
            if (horizontal.lengthSqr() > reach * reach || horizontal.lengthSqr() < 0.001D || horizontal.normalize().dot(forward) < 0.18D) {
                continue;
            }
            if (hurt(player, target, damage)) {
                double resistance = Math.max(0.2D, 1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                Vec3 push = horizontal.normalize().scale((manifested ? 2.15D : 1.55D) * resistance);
                target.push(push.x, manifested ? 0.55D : 0.34D, push.z);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, manifested ? 80 : 50, 1, false, true));
            }
        }

        for (double distance = 1.0D; distance <= reach; distance += 0.75D) {
            Vec3 point = player.position().add(forward.scale(distance)).add(0.0D, 0.18D, 0.0D);
            level.sendParticles(GOLD, point.x, point.y, point.z, manifested ? 5 : 3, width * distance / reach * 0.32D, 0.14D, width * distance / reach * 0.32D, 0.02D);
        }
        ring(level, player.position().add(forward.scale(reach * 0.58D)), manifested ? 5.0D : 3.5D, PALE_GOLD, manifested ? 54 : 38);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, manifested ? 1.35F : 1.0F, manifested ? 0.58F : 0.78F);
        level.playSound(null, player.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0F, 0.65F);
    }

    private static void castGuard(ServerPlayer player) {
        if (!ready(player, FRAGMENTS_GUARD)) {
            return;
        }
        boolean manifested = isManifested(player);
        int mana = manifested ? 450 : 380;
        if (!consumeMana(player, mana)) {
            return;
        }

        long duration = manifested ? 180L : 120L;
        player.getPersistentData().putLong(GUARD_UNTIL_TAG, player.level().getGameTime() + duration);
        player.getPersistentData().putInt(GUARD_POWER_TAG, manifested ? 2 : 1);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, (int) duration, manifested ? 4 : 2, false, true, true));
        CooldownManager.set(player, FRAGMENTS_GUARD, manifested ? 240 : 300);
        brilliantBurst(player.serverLevel(), player.position().add(0.0D, 0.9D, 0.0D), manifested ? 3.3D : 2.6D, manifested ? 72 : 52);
        player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.25F, manifested ? 0.55F : 0.75F);
    }

    private static void toggleManifestation(ServerPlayer player) {
        if (isManifested(player)) {
            disableManifestation(player, false);
            return;
        }
        if (!ready(player, BRILLIANT_MANIFESTATION) || !consumeMana(player, 600)) {
            return;
        }

        player.getPersistentData().putBoolean(MANIFESTED_TAG, true);
        applyManifestationEffects(player);
        brilliantBurst(player.serverLevel(), player.position().add(0.0D, 1.0D, 0.0D), 4.0D, 104);
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.25F, 0.72F);
        player.displayClientMessage(Component.translatable("message.sololeveling3.go_gunhee.manifestation_on").withStyle(ChatFormatting.GOLD), true);
    }

    private static void disableManifestation(ServerPlayer player, boolean exhausted) {
        if (!isManifested(player)) {
            return;
        }
        player.getPersistentData().remove(MANIFESTED_TAG);
        CooldownManager.set(player, BRILLIANT_MANIFESTATION, exhausted ? 300 : 200);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9F, 0.8F);
        if (exhausted) {
            player.displayClientMessage(Component.translatable("message.sololeveling3.go_gunhee.manifestation_empty").withStyle(ChatFormatting.RED), true);
        } else {
            player.displayClientMessage(Component.translatable("message.sololeveling3.go_gunhee.manifestation_off").withStyle(ChatFormatting.YELLOW), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong(GUARD_UNTIL_TAG) <= now) {
            player.getPersistentData().remove(GUARD_UNTIL_TAG);
            player.getPersistentData().remove(GUARD_POWER_TAG);
        }

        if (!isManifested(player)) {
            return;
        }
        if (!player.isAlive() || !isGoGunheeVessel(player)) {
            disableManifestation(player, false);
            return;
        }

        if (player.tickCount % 4 == 0) {
            aura(player.serverLevel(), player);
        }
        if (player.tickCount % 20 == 0) {
            if (!drainManifestationMana(player)) {
                disableManifestation(player, true);
                return;
            }
            applyManifestationEffects(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isGoGunheeVessel(player)) {
            return;
        }
        if (player.getPersistentData().getLong(GUARD_UNTIL_TAG) <= player.level().getGameTime()) {
            return;
        }

        Entity directSource = event.getSource().getEntity();
        if (directSource != null && INTERNAL_DAMAGE.contains(directSource.getUUID())) {
            return;
        }

        int power = Math.max(1, player.getPersistentData().getInt(GUARD_POWER_TAG));
        float incoming = event.getAmount();
        event.setAmount(incoming * (power >= 2 ? 0.25F : 0.43F));

        if (directSource instanceof LivingEntity attacker && validTarget(player, attacker)) {
            float reflected = Math.min(power >= 2 ? 24.0F : 14.0F, incoming * (power >= 2 ? 0.36F : 0.22F));
            if (reflected > 0.0F) {
                hurt(player, attacker, reflected);
                Vec3 away = attacker.position().subtract(player.position());
                if (away.horizontalDistanceSqr() > 0.001D) {
                    Vec3 push = away.normalize().scale(power >= 2 ? 1.25D : 0.8D);
                    attacker.push(push.x, power >= 2 ? 0.32D : 0.18D, push.z);
                    attacker.hurtMarked = true;
                }
            }
        }

        brilliantBurst(player.serverLevel(), player.position().add(0.0D, 1.0D, 0.0D), power >= 2 ? 2.2D : 1.6D, power >= 2 ? 30 : 20);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.45F, power >= 2 ? 1.5F : 1.8F);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().remove(MANIFESTED_TAG);
            player.getPersistentData().remove(GUARD_UNTIL_TAG);
            player.getPersistentData().remove(GUARD_POWER_TAG);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().remove(MANIFESTED_TAG);
            player.getPersistentData().remove(GUARD_UNTIL_TAG);
            player.getPersistentData().remove(GUARD_POWER_TAG);
        }
    }

    private static void applyManifestationEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 35, 2, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 35, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 35, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 35, 0, false, false, true));
    }

    private static boolean drainManifestationMana(ServerPlayer player) {
        if (player.isCreative()) {
            return true;
        }
        SololevelingModVariables.PlayerVariables vars = variables(player);
        if (vars.MP < MANIFESTATION_DRAIN) {
            return false;
        }
        player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(capability -> {
            capability.MP = Math.max(0.0D, capability.MP - MANIFESTATION_DRAIN);
            capability.syncPlayerVariables(player);
        });
        CooldownManager.set(player, "mana_refresh", 25);
        return true;
    }

    private static boolean consumeMana(ServerPlayer player, int amount) {
        if (player.isCreative()) {
            return true;
        }
        SololevelingModVariables.PlayerVariables vars = variables(player);
        if (vars.MP < amount) {
            player.displayClientMessage(Component.translatable("message.sololeveling3.go_gunhee.no_mana", amount).withStyle(ChatFormatting.RED), true);
            return false;
        }
        player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY).ifPresent(capability -> {
            capability.MP = Math.max(0.0D, capability.MP - amount);
            capability.syncPlayerVariables(player);
        });
        CooldownManager.set(player, "mana_refresh", 35);
        return true;
    }

    private static boolean ready(ServerPlayer player, String skill) {
        if (!CooldownManager.isOnCooldown(player, skill)) {
            return true;
        }
        player.displayClientMessage(Component.translatable(
                "message.sololeveling3.go_gunhee.cooldown",
                skill,
                CooldownManager.getRemainingSeconds(player, skill)
        ).withStyle(ChatFormatting.RED), true);
        return false;
    }

    private static boolean hurt(ServerPlayer player, LivingEntity target, float amount) {
        if (!validTarget(player, target)) {
            return false;
        }
        INTERNAL_DAMAGE.add(player.getUUID());
        try {
            target.invulnerableTime = 0;
            return target.hurt(player.damageSources().playerAttack(player), Math.max(0.5F, amount));
        } finally {
            INTERNAL_DAMAGE.remove(player.getUUID());
        }
    }

    private static boolean validTarget(ServerPlayer player, LivingEntity target) {
        if (target == player || !target.isAlive() || target instanceof ArmorStand || player.isAlliedTo(target)) {
            return false;
        }
        if (target instanceof Player other) {
            return !other.isCreative() && !other.isSpectator() && player.canHarmPlayer(other);
        }
        return true;
    }

    private static Vec3 horizontalLook(Entity entity) {
        Vec3 look = entity.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        return horizontal.lengthSqr() < 0.001D ? new Vec3(0.0D, 0.0D, 1.0D) : horizontal.normalize();
    }

    private static void aura(ServerLevel level, ServerPlayer player) {
        double angle = player.tickCount * 0.22D;
        for (int index = 0; index < 4; index++) {
            double spin = angle + Math.PI * 0.5D * index;
            double radius = 0.75D + 0.15D * Math.sin(angle * 0.7D + index);
            level.sendParticles(
                    index % 2 == 0 ? GOLD : PALE_GOLD,
                    player.getX() + Math.cos(spin) * radius,
                    player.getY() + 0.2D + (index % 3) * 0.7D,
                    player.getZ() + Math.sin(spin) * radius,
                    1,
                    0.02D,
                    0.08D,
                    0.02D,
                    0.01D
            );
        }
    }

    private static void brilliantBurst(ServerLevel level, Vec3 center, double radius, int points) {
        ring(level, center, radius, GOLD, points);
        level.sendParticles(PALE_GOLD, center.x, center.y, center.z, Math.max(12, points / 2), radius * 0.28D, radius * 0.35D, radius * 0.28D, 0.08D);
    }

    private static void ring(ServerLevel level, Vec3 center, double radius, DustParticleOptions particle, int points) {
        for (int index = 0; index < points; index++) {
            double angle = Math.PI * 2.0D * index / points;
            level.sendParticles(
                    particle,
                    center.x + Math.cos(angle) * radius,
                    center.y + 0.1D,
                    center.z + Math.sin(angle) * radius,
                    1,
                    0.02D,
                    0.03D,
                    0.02D,
                    0.01D
            );
        }
    }

    private static SololevelingModVariables.PlayerVariables variables(Entity entity) {
        return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY)
                .orElse(new SololevelingModVariables.PlayerVariables());
    }
}
