package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;
import dev.eness.sololevelingfinal.core.entity.BarrierVfxEntity;
import dev.eness.sololevelingfinal.core.entity.SilladBossEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber(modid = "sololeveling")
public final class SilladIcePrisonManager {
   private static final String PRISON_MARKER = "slr_sillad_ice_prison";
   private static final String PRISON_CONSTRUCT = "slr_sillad_ice_prison_construct";
   private static final String PRISON_BOSS = "slr_sillad_ice_prison_boss";
   private static final String PRISON_OWNER = "slr_sillad_ice_prison_owner";
   private static final Map<UUID, SilladIcePrisonManager.PrisonSession> BY_TARGET = new HashMap<>();
   private static final Map<UUID, Set<UUID>> BY_BOSS = new HashMap<>();

   private SilladIcePrisonManager() {
   }

   public static boolean canCapture(SilladBossEntity sillad, LivingEntity target) {
      if (sillad != null
         && target != null
         && target != sillad
         && target.isAlive()
         && !(target instanceof Player)
         && target.level() == sillad.level()
         && !target.isPassenger()
         && !target.isVehicle()
         && !isImprisoned(target)) {
         UUID ownerId = ShadowMonarchManager.getShadowOwnerUUID(target);
         if (ownerId != null && sillad.level() instanceof ServerLevel level) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
            return owner != null && owner.isAlive() && owner.level() == level && !owner.isCreative() && !owner.isSpectator();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static int captureWave(SilladBossEntity sillad, Collection<? extends LivingEntity> requestedTargets) {
      if (sillad != null && requestedTargets != null && sillad.level() instanceof ServerLevel level) {
         int var15 = 0;

         for (LivingEntity target : requestedTargets) {
            if (canCapture(sillad, target)) {
               UUID ownerId = ShadowMonarchManager.getShadowOwnerUUID(target);
               if (ownerId != null) {
                  float radius = Mth.clamp(target.getBbWidth() * 0.72F + 0.55F, 0.9F, 3.5F);
                  float height = Mth.clamp(target.getBbHeight() + 0.45F, 1.8F, 6.5F);
                  float integrity = SilladBossRules.prisonIntegrity(
                     sillad.getEngagedPlayerCount(), target.getMaxHealth(), ShadowMonarchManager.appliedShadowRank(target)
                  );
                  BarrierVfxEntity prison = BarrierVfxEntity.spawn(
                     level, target.position().add(0.0, 0.04, 0.0), 11, 5, radius, height, 492, target.getYRot(), 0.0F, sillad, target, false, integrity, true
                  );
                  prison.setCustomNameVisible(true);
                  updatePrisonName(prison);
                  long now = level.getGameTime();
                  SilladIcePrisonManager.PrisonSession session = new SilladIcePrisonManager.PrisonSession(
                     level.dimension(),
                     sillad.getUUID(),
                     prison.getUUID(),
                     target.getUUID(),
                     ownerId,
                     target.position(),
                     now + 480L,
                     integrity,
                     target.getTicksFrozen(),
                     now
                  );
                  register(session);
                  mark(target, session);
                  if (target instanceof Mob mob) {
                     mob.setTarget(null);
                  }

                  level.sendParticles(
                     ParticleTypes.SNOWFLAKE, target.getX(), target.getEyeY(), target.getZ(), 38, radius * 0.45, height * 0.25, radius * 0.45, 0.055
                  );
                  level.playSound((Player)null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.85F, 1.62F);
                  var15++;
               }
            }
         }

         return var15;
      } else {
         return 0;
      }
   }

   public static boolean isImprisoned(Entity entity) {
      if (entity == null || !entity.getPersistentData().getBoolean("slr_sillad_ice_prison")) {
         return false;
      } else if (entity.level().isClientSide()) {
         return true;
      } else {
         SilladIcePrisonManager.PrisonSession known = BY_TARGET.get(entity.getUUID());
         if (known != null) {
            return true;
         } else if (!(
            entity.level() instanceof ServerLevel level
               && entity.getPersistentData().hasUUID("slr_sillad_ice_prison_construct")
               && entity.getPersistentData().hasUUID("slr_sillad_ice_prison_boss")
               && entity.getPersistentData().hasUUID("slr_sillad_ice_prison_owner")
         )) {
            clearMarker(entity);
            return false;
         } else if (level.getEntity(entity.getPersistentData().getUUID("slr_sillad_ice_prison_construct")) instanceof BarrierVfxEntity prison
            && prison.isActive()
            && prison.getStyle() == 11) {
            long now = level.getGameTime();
            SilladIcePrisonManager.PrisonSession recovered = new SilladIcePrisonManager.PrisonSession(
               level.dimension(),
               entity.getPersistentData().getUUID("slr_sillad_ice_prison_boss"),
               prison.getUUID(),
               entity.getUUID(),
               entity.getPersistentData().getUUID("slr_sillad_ice_prison_owner"),
               entity.position(),
               now + Math.max(1, prison.getLifetime() - prison.tickCount),
               prison.getIntegrity(),
               entity instanceof LivingEntity living ? living.getTicksFrozen() : 0,
               now
            );
            register(recovered);
            return true;
         } else {
            clearMarker(entity);
            return false;
         }
      }
   }

   public static boolean guardManualDismiss(Player player) {
      if (player != null && !player.level().isClientSide()) {
         boolean blocked = hasImprisonedOwnedCompanion(player);
         if (blocked) {
            player.displayClientMessage(Component.translatable("message.sololeveling.sillad_prison.dismiss_blocked"), true);
         }

         return blocked;
      } else {
         return false;
      }
   }

   public static boolean hasImprisonedOwnedCompanion(Player owner) {
      if (owner == null) {
         return false;
      }

      UUID ownerId = owner.getUUID();

      for (SilladIcePrisonManager.PrisonSession session : new ArrayList<>(BY_TARGET.values())) {
         if (ownerId.equals(session.ownerId)) {
            return true;
         }
      }

      if (owner instanceof ServerPlayer serverPlayer && serverPlayer.server != null) {
         for (ServerLevel level : serverPlayer.server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
               if (ownerId.equals(ShadowMonarchManager.getShadowOwnerUUID(entity)) && isImprisoned(entity)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static int activeCountForBoss(UUID bossId) {
      Set<UUID> targets = bossId == null ? null : BY_BOSS.get(bossId);
      return targets == null ? 0 : targets.size();
   }

   public static void releaseAllForBoss(SilladBossEntity sillad) {
      if (sillad != null) {
         releaseAllForBoss(sillad.getUUID());
      }
   }

   public static void releaseAllForBoss(UUID bossId) {
      if (bossId != null) {
         Set<UUID> targets = BY_BOSS.get(bossId);
         if (targets != null) {
            for (UUID targetId : new HashSet<>(targets)) {
               SilladIcePrisonManager.PrisonSession session = BY_TARGET.get(targetId);
               if (session != null) {
                  release(session, true, false);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END && !BY_TARGET.isEmpty()) {
         MinecraftServer server = event.getServer();
         Map<UUID, SilladIcePrisonManager.DrainBatch> manaDrains = new HashMap<>();

         for (SilladIcePrisonManager.PrisonSession session : new ArrayList<>(BY_TARGET.values())) {
            ServerLevel level = server.getLevel(session.dimension);
            if (level == null) {
               release(session, false, false);
            } else {
               Entity bossRaw = level.getEntity(session.bossId);
               Entity targetRaw = level.getEntity(session.targetId);
               Entity prisonRaw = level.getEntity(session.constructId);
               if (bossRaw instanceof SilladBossEntity boss
                  && boss.isAlive()
                  && targetRaw instanceof LivingEntity target
                  && target.isAlive()
                  && prisonRaw instanceof BarrierVfxEntity prison
                  && prison.isActive()
                  && prison.getStyle() == 11) {
                  long now = level.getGameTime();
                  if (now >= session.expiresAt) {
                     release(session, true, true);
                  } else {
                     ServerPlayer owner = server.getPlayerList().getPlayer(session.ownerId);
                     if (owner != null && owner.isAlive() && owner.level() == level) {
                        tether(target, session);
                        if (prison.getIntegrity() + 0.01F < session.lastIntegrity && now >= session.nextDamageFeedbackAt) {
                           session.nextDamageFeedbackAt = now + 3L;
                           level.sendParticles(
                              ParticleTypes.SNOWFLAKE,
                              prison.getX(),
                              prison.getY() + prison.getLength() * 0.5,
                              prison.getZ(),
                              5,
                              prison.getScale() * 0.35,
                              prison.getLength() * 0.18,
                              prison.getScale() * 0.35,
                              0.025
                           );
                           level.playSound((Player)null, prison.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 0.34F, 1.72F);
                        }

                        session.lastIntegrity = prison.getIntegrity();
                        if (now >= session.nextAttackAt) {
                           session.nextAttackAt = now + 10L;
                           attackPrison(target, prison);
                           if (!prison.isAlive() || !prison.isActive()) {
                              release(session, false, false);
                              continue;
                           }
                        }

                        if (now >= session.nextPulseAt) {
                           session.nextPulseAt = now + 20L;
                           regenerate(level, prison);
                           applyNonlethalDot(boss, target);
                           manaDrains.compute(
                              session.ownerId,
                              (ignored, batchx) -> batchx == null
                                 ? new SilladIcePrisonManager.DrainBatch(level, owner, target.position())
                                 : batchx.add(target.position())
                           );
                        }

                        updatePrisonName(prison);
                     } else {
                        release(session, true, false);
                     }
                  }
               } else {
                  release(session, false, false);
               }
            }
         }

         for (SilladIcePrisonManager.DrainBatch batch : manaDrains.values()) {
            drainMana(batch);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onLivingAttack(LivingAttackEvent event) {
      Entity attacker = event.getSource().getDirectEntity();
      if (attacker instanceof Projectile projectile && projectile.getOwner() != null) {
         attacker = projectile.getOwner();
      }

      if (attacker == null) {
         attacker = event.getSource().getEntity();
      }

      if (isImprisoned(attacker)) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      releaseOwnedBy(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      releaseOwnedBy(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      BY_TARGET.clear();
      BY_BOSS.clear();
   }

   private static void tether(LivingEntity target, SilladIcePrisonManager.PrisonSession session) {
      if (target instanceof Mob mob) {
         mob.setTarget(null);
         mob.getNavigation().stop();
      }

      target.setDeltaMovement(Vec3.ZERO);
      if (target.position().distanceToSqr(session.anchor) > 0.015625) {
         target.teleportTo(session.anchor.x, session.anchor.y, session.anchor.z);
      }

      target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 9, false, false, false));
      target.setTicksFrozen(Math.max(target.getTicksFrozen(), 120));
      target.hurtMarked = true;
   }

   private static void attackPrison(LivingEntity target, BarrierVfxEntity prison) {
      if (target instanceof Mob mob) {
         mob.swing(InteractionHand.MAIN_HAND);
         float damage = SilladBossRules.prisonerAttackDamage(mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
         prison.hurt(mob.damageSources().mobAttack(mob), damage);
      }
   }

   private static void regenerate(ServerLevel level, BarrierVfxEntity prison) {
      if (!(prison.getIntegrity() >= prison.getMaxIntegrity())) {
         float repaired = Math.min(prison.getMaxIntegrity(), prison.getIntegrity() + SilladBossRules.prisonRegeneration(prison.getMaxIntegrity()));
         prison.setIntegrity(repaired);
         level.sendParticles(
            ParticleTypes.END_ROD,
            prison.getX(),
            prison.getY() + prison.getLength() * 0.5,
            prison.getZ(),
            6,
            prison.getScale() * 0.22,
            prison.getLength() * 0.18,
            prison.getScale() * 0.22,
            -0.018
         );
         if (Math.floorMod(prison.getId(), 4) == Math.floorMod((int)(level.getGameTime() / 20L), 4)) {
            level.playSound((Player)null, prison.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.22F, 1.78F);
         }
      }
   }

   private static void updatePrisonName(BarrierVfxEntity prison) {
      int health = Math.max(0, Mth.ceil(prison.getIntegrity()));
      int maximum = Math.max(1, Mth.ceil(prison.getMaxIntegrity()));
      Component name = Component.translatable("entity.sololeveling.sillad_ice_prison").append(Component.literal("  " + health + "/" + maximum));
      if (!name.equals(prison.getCustomName())) {
         prison.setCustomName(name);
      }
   }

   private static void applyNonlethalDot(SilladBossEntity boss, LivingEntity target) {
      float raw = SilladBossRules.prisonDot(target.getMaxHealth(), boss.getEngagedPlayerCount());
      float allowed = Math.min(raw, Math.max(0.0F, target.getHealth() - 1.0F));
      if (!(allowed <= 0.0F)) {
         target.invulnerableTime = 0;
         target.hurt(boss.damageSources().indirectMagic(boss, boss), allowed);
      }
   }

   private static void drainMana(SilladIcePrisonManager.DrainBatch batch) {
      ServerPlayer owner = batch.owner;
      if (owner != null && !owner.isCreative()) {
         double[] drained = new double[]{0.0};
         owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            double requested = SilladBossRules.prisonManaDrain(capability.Mana, batch.count);
            drained[0] = Math.min(Math.max(0.0, capability.MP), requested);
            capability.MP = Math.max(0.0, capability.MP - drained[0]);
            capability.syncPlayerVariables(owner);
         });
         if (!(drained[0] <= 0.0)) {
            CooldownManager.set(owner, "mana_refresh", 30);
            owner.displayClientMessage(Component.translatable("message.sololeveling.sillad_prison.mana_drain", (int)Math.ceil(drained[0])), true);
            Vec3 end = owner.getBoundingBox().getCenter();
            Vec3 delta = end.subtract(batch.center);

            for (int index = 1; index <= 10; index++) {
               Vec3 point = batch.center.add(delta.scale(index / 10.0));
               batch.level.sendParticles(index % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
            }
         }
      }
   }

   private static void releaseOwnedBy(UUID ownerId) {
      for (SilladIcePrisonManager.PrisonSession session : new ArrayList<>(BY_TARGET.values())) {
         if (ownerId.equals(session.ownerId)) {
            release(session, true, false);
         }
      }
   }

   private static void release(SilladIcePrisonManager.PrisonSession session, boolean dissolve, boolean timeout) {
      BY_TARGET.remove(session.targetId);
      Set<UUID> bossTargets = BY_BOSS.get(session.bossId);
      if (bossTargets != null) {
         bossTargets.remove(session.targetId);
         if (bossTargets.isEmpty()) {
            BY_BOSS.remove(session.bossId);
         }
      }

      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      ServerLevel level = server == null ? null : server.getLevel(session.dimension);
      if (level != null) {
         Entity target = level.getEntity(session.targetId);
         if (target != null) {
            clearMarker(target);
            if (target instanceof LivingEntity living) {
               living.setTicksFrozen(Math.min(living.getTicksFrozen(), session.previousFrozenTicks));
            }
         }

         if (dissolve && level.getEntity(session.constructId) instanceof BarrierVfxEntity prison && prison.isActive()) {
            prison.dissolve();
            level.playSound(
               (Player)null,
               prison.blockPosition(),
               timeout ? SoundEvents.POWDER_SNOW_BREAK : SoundEvents.AMETHYST_BLOCK_RESONATE,
               SoundSource.HOSTILE,
               timeout ? 0.65F : 0.45F,
               timeout ? 0.72F : 1.55F
            );
         }
      }
   }

   private static void register(SilladIcePrisonManager.PrisonSession session) {
      BY_TARGET.put(session.targetId, session);
      BY_BOSS.computeIfAbsent(session.bossId, ignored -> new HashSet<>()).add(session.targetId);
   }

   private static void mark(Entity target, SilladIcePrisonManager.PrisonSession session) {
      target.getPersistentData().putBoolean("slr_sillad_ice_prison", true);
      target.getPersistentData().putUUID("slr_sillad_ice_prison_construct", session.constructId);
      target.getPersistentData().putUUID("slr_sillad_ice_prison_boss", session.bossId);
      target.getPersistentData().putUUID("slr_sillad_ice_prison_owner", session.ownerId);
   }

   private static void clearMarker(Entity target) {
      target.getPersistentData().remove("slr_sillad_ice_prison");
      target.getPersistentData().remove("slr_sillad_ice_prison_construct");
      target.getPersistentData().remove("slr_sillad_ice_prison_boss");
      target.getPersistentData().remove("slr_sillad_ice_prison_owner");
   }

   private static final class DrainBatch {
      private final ServerLevel level;
      private final ServerPlayer owner;
      private Vec3 center;
      private int count = 1;

      private DrainBatch(ServerLevel level, ServerPlayer owner, Vec3 center) {
         this.level = level;
         this.owner = owner;
         this.center = center;
      }

      private SilladIcePrisonManager.DrainBatch add(Vec3 position) {
         this.center = this.center.scale(this.count).add(position).scale(1.0 / ++this.count);
         return this;
      }
   }

   private static final class PrisonSession {
      private final ResourceKey<Level> dimension;
      private final UUID bossId;
      private final UUID constructId;
      private final UUID targetId;
      private final UUID ownerId;
      private final Vec3 anchor;
      private final long expiresAt;
      private final int previousFrozenTicks;
      private float lastIntegrity;
      private long nextAttackAt;
      private long nextPulseAt;
      private long nextDamageFeedbackAt;

      private PrisonSession(
         ResourceKey<Level> dimension,
         UUID bossId,
         UUID constructId,
         UUID targetId,
         UUID ownerId,
         Vec3 anchor,
         long expiresAt,
         float integrity,
         int previousFrozenTicks,
         long now
      ) {
         this.dimension = dimension;
         this.bossId = bossId;
         this.constructId = constructId;
         this.targetId = targetId;
         this.ownerId = ownerId;
         this.anchor = anchor;
         this.expiresAt = expiresAt;
         this.previousFrozenTicks = previousFrozenTicks;
         this.lastIntegrity = integrity;
         this.nextAttackAt = now + 8L;
         this.nextPulseAt = now + 20L;
      }
   }
}
