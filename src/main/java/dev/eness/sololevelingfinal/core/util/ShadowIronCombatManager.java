package dev.eness.sololevelingfinal.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonMobLevelAdapter;
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import dev.eness.sololevelingfinal.core.entity.ai.ShadowIronCombatPolicy;
import dev.eness.sololevelingfinal.core.party.PartyService;
import org.joml.Vector3f;

@EventBusSubscriber
public final class ShadowIronCombatManager {
   public static final String TAUNT_HIGHLIGHT_SOURCE_PREFIX = "shadow:iron_taunt:";
   public static final int TAUNT_HIGHLIGHT_PRIORITY = 325;
   private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "soloboss"));
   private static final TagKey<EntityType<?>> TAUNT_IMMUNE_TAG = TagKey.create(
      Registries.ENTITY_TYPE, new ResourceLocation("sololeveling", "iron_taunt_immune")
   );
   private static final double TAUNT_RANGE_SQR = 144.0;
   private static final Map<UUID, ShadowIronCombatManager.TauntState> TAUNTS = new HashMap<>();
   private static final Map<UUID, ShadowIronCombatManager.RescueState> RESCUES = new HashMap<>();
   private static final List<ShadowIronCombatManager.RingBurst> RINGS = new ArrayList<>();

   private ShadowIronCombatManager() {
   }

   public static void tickIron(ShadowIronEntity iron) {
      if (iron != null
         && !iron.level().isClientSide()
         && iron.isAlive()
         && iron.getCombatAction() != ShadowIronEntity.Action.BLOCK
         && iron.canInterceptNow()
         && Math.floorMod(iron.tickCount + iron.getId(), 2) == 0) {
         tryGuardianRescue(iron);
      }
   }

   public static LivingEntity findGuardianThreat(ShadowIronEntity iron) {
      if (iron != null && !iron.level().isClientSide()) {
         Player owner = ShadowMonarchManager.getShadowOwnerPlayer(iron);
         if (owner != null && owner.isAlive()) {
            String command = ShadowMonarchManager.currentShadowCommand(iron);
            if (!"default".equals(command) && !"protect".equals(command)) {
               return null;
            }

            double range = 16.0;
            return iron.level()
               .getEntitiesOfClass(
                  Mob.class,
                  owner.getBoundingBox().inflate(range, range * 0.5, range),
                  mob -> mob.isAlive()
                     && mob.getTarget() == owner
                     && ShadowMonarchManager.canShadowDamage(iron, mob)
                     && (owner.hasLineOfSight(mob) || iron.hasLineOfSight(mob))
               )
               .stream()
               .min(Comparator.comparingDouble(owner::distanceToSqr))
               .orElse(null);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean shouldRoar(ShadowIronEntity iron) {
      if (iron != null && iron.canRoarNow() && Math.floorMod(iron.tickCount + iron.getId(), 10) == 0) {
         String command = ShadowMonarchManager.currentShadowCommand(iron);
         if ("follow".equals(command)) {
            return false;
         } else {
            List<Mob> candidates = tauntCandidates(iron);
            if (candidates.isEmpty()) {
               return false;
            } else {
               Player owner = ShadowMonarchManager.getShadowOwnerPlayer(iron);
               long ownerThreats = owner == null ? 0L : candidates.stream().filter(mob -> mob.getTarget() == owner).count();
               double ownerHealth = owner == null ? 1.0 : owner.getHealth() / Math.max(1.0F, owner.getMaxHealth());
               if ("protect".equals(command)) {
                  return ownerThreats >= 1L || candidates.size() >= 3;
               } else {
                  return !"berserk".equals(command) && !"clear_dungeon".equals(command)
                     ? ownerThreats >= 2L || ownerHealth <= 0.5 && ownerThreats >= 1L || candidates.size() >= 4
                     : candidates.size() >= 3;
               }
            }
         }
      } else {
         return false;
      }
   }

   public static boolean shouldBrace(ShadowIronEntity iron, LivingEntity target) {
      if (iron == null || target == null || !iron.canBlockNow() || iron.distanceToSqr(target) > 20.25) {
         return false;
      }

      if (target.getAttackAnim(0.0F) > 0.05F) {
         return true;
      }

      int nearby = iron.level()
         .getEntitiesOfClass(Mob.class, iron.getBoundingBox().inflate(4.0, 2.5, 4.0), mob -> ShadowMonarchManager.canShadowDamage(iron, mob))
         .size();
      return nearby >= 2 && Math.floorMod(iron.tickCount + iron.getId(), 20) == 0;
   }

   public static void tryGuardianChallenge(ShadowIronEntity iron, LivingEntity target) {
      if (target instanceof Mob mob && !mob.getType().is(TAUNT_IMMUNE_TAG)) {
         Player owner = ShadowMonarchManager.getShadowOwnerPlayer(iron);
         if (owner != null && mob.getTarget() == owner) {
            String command = ShadowMonarchManager.currentShadowCommand(iron);
            if ("default".equals(command) || "protect".equals(command)) {
               claimTaunt(iron, mob, ShadowIronCombatPolicy.passiveChallengeDuration(isBoss(mob)));
            }
         }
      }
   }

   public static void performCleave(ShadowIronEntity iron, LivingEntity primary, boolean counter) {
      if (iron != null && iron.level() instanceof ServerLevel level) {
         level.playSound(
            (Player)null, iron.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.NEUTRAL, counter ? 1.0F : 0.85F, counter ? 0.68F : 0.78F
         );
         level.playSound((Player)null, iron.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 0.55F, counter ? 0.72F : 0.82F);
         if (!isMeleeTarget(iron, primary)) {
            spawnSweep(level, iron.position().add(0.0, 1.8, 0.0));
         } else {
            float primaryDamage = ShadowIronCombatPolicy.primaryDamage(iron.getAttributeValue(Attributes.ATTACK_DAMAGE), counter);
            if (hurtFromIron(iron, primary, primaryDamage)) {
               primary.knockback(counter ? 0.65 : 0.35, iron.getX() - primary.getX(), iron.getZ() - primary.getZ());
               spawnHit(level, primary.getBoundingBox().getCenter(), ironColor(iron));
            }

            Vec3 forward = Vec3.directionFromRotation(0.0F, iron.getYRot()).multiply(1.0, 0.0, 1.0).normalize();
            List<LivingEntity> secondary = level.getEntitiesOfClass(
                  LivingEntity.class,
                  iron.getBoundingBox().inflate(3.2, 2.5, 3.2),
                  candidate -> candidate != primary && ShadowMonarchManager.canShadowDamage(iron, candidate) && inForwardArc(iron, candidate, forward)
               )
               .stream()
               .sorted(Comparator.comparingDouble(iron::distanceToSqr))
               .limit(2L)
               .toList();
            float splash = ShadowIronCombatPolicy.secondaryDamage(iron.getAttributeValue(Attributes.ATTACK_DAMAGE));

            for (LivingEntity target : secondary) {
               if (hurtFromIron(iron, target, splash)) {
                  target.knockback(0.25, iron.getX() - target.getX(), iron.getZ() - target.getZ());
                  spawnHit(level, target.getBoundingBox().getCenter(), ironColor(iron));
               }
            }

            spawnSweep(level, primary.getBoundingBox().getCenter());
         }
      }
   }

   public static void performRoar(ShadowIronEntity iron) {
      if (iron != null && iron.level() instanceof ServerLevel level) {
         int var7 = 0;

         for (Mob target : tauntCandidates(iron)) {
            boolean boss = isBoss(target);
            boolean elite = isElite(target);
            if (claimTaunt(iron, target, ShadowIronCombatPolicy.tauntDuration(boss, elite))) {
               var7++;
            }
         }

         iron.fortifyFromTaunt(var7);
         int color = ironColor(iron);
         RINGS.add(new ShadowIronCombatManager.RingBurst(level, iron.position(), color, 0));
         level.playSound((Player)null, iron.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.NEUTRAL, 1.25F, 0.62F);
         level.playSound((Player)null, iron.blockPosition(), SoundEvents.WITHER_AMBIENT, SoundSource.NEUTRAL, 0.42F, 1.35F);
         spawnDust(level, iron.position().add(0.0, 2.1, 0.0), color, 16, 0.85, 0.055);
      }
   }

   public static void onShieldBlock(ShadowIronEntity iron, DamageSource source) {
      if (iron != null && iron.level() instanceof ServerLevel level) {
         Vec3 var5 = Vec3.directionFromRotation(0.0F, iron.getYRot()).multiply(1.0, 0.0, 1.0).normalize();
         Vec3 shield = iron.position().add(var5.scale(0.72)).add(0.0, 1.85, 0.0);
         level.sendParticles(ParticleTypes.ELECTRIC_SPARK, shield.x, shield.y, shield.z, 8, 0.28, 0.42, 0.28, 0.08);
         spawnDust(level, shield, ironColor(iron), 8, 0.28, 0.03);
         level.playSound((Player)null, iron.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 0.72F);
         level.playSound((Player)null, iron.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.NEUTRAL, 0.24F, 1.45F);
      }
   }

   public static boolean isBossDamageSource(DamageSource source) {
      if (source == null) {
         return false;
      } else if (source.getEntity() instanceof LivingEntity living) {
         return isBoss(living);
      } else {
         return source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner ? isBoss(owner) : false;
      }
   }

   public static boolean isBoss(LivingEntity target) {
      if (target != null && !(target instanceof Player)) {
         DungeonMobLevelAdapter.MobRole role = DungeonMobLevelAdapter.MobRole.fromString(target.getPersistentData().getString("slr_dungeon_role"));
         return role == DungeonMobLevelAdapter.MobRole.BOSS || target.getType().is(BOSS_TAG) || target.getMaxHealth() >= 250.0F;
      } else {
         return false;
      }
   }

   @SubscribeEvent
   public static void onTargetChange(LivingChangeTargetEvent event) {
      if (event.getEntity() instanceof Mob mob) {
         ShadowIronCombatManager.TauntState state = TAUNTS.get(mob.getUUID());
         if (state != null && state.hardLock && state.level.getGameTime() < state.expiresAt) {
            if (state.level.getEntity(state.ironId) instanceof ShadowIronEntity iron && iron.isAlive() && event.getNewTarget() != iron) {
               event.setNewTarget(iron);
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onOwnerHurt(LivingHurtEvent event) {
      if (event.getEntity() instanceof ServerPlayer owner && !(event.getAmount() <= 0.0F)) {
         ShadowIronCombatManager.RescueState rescue = RESCUES.get(owner.getUUID());
         if (rescue != null
            && rescue.level == owner.level()
            && rescue.level.getGameTime() < rescue.expiresAt
            && matchesThreat(event.getSource(), rescue.threatId)
            && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (rescue.level.getEntity(rescue.ironId) instanceof ShadowIronEntity iron
               && iron.isAlive()
               && !(iron.distanceToSqr(owner) > 10.5625)
               && iron.canBlockSource(event.getSource())) {
               boolean boss = isBossDamageSource(event.getSource());
               float original = event.getAmount();
               float redirected = original * ShadowIronCombatPolicy.redirectedDamageFraction(boss);
               RESCUES.remove(owner.getUUID());
               if (iron.hurt(event.getSource(), redirected)) {
                  event.setAmount(original * ShadowIronCombatPolicy.ownerDamageFraction(boss));
               }
            } else {
               RESCUES.remove(owner.getUUID());
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         tickTaunts();
         tickRings();
         RESCUES.entrySet().removeIf(entry -> {
            ShadowIronCombatManager.RescueState state = entry.getValue();
            return state.level.getGameTime() >= state.expiresAt || !(state.level.getEntity(state.ironId) instanceof ShadowIronEntity iron && iron.isAlive());
         });
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      TAUNTS.clear();
      RESCUES.clear();
      RINGS.clear();
   }

   private static List<Mob> tauntCandidates(ShadowIronEntity iron) {
      Player owner = ShadowMonarchManager.getShadowOwnerPlayer(iron);
      return iron.level()
         .getEntitiesOfClass(
            Mob.class,
            iron.getBoundingBox().inflate(12.0, 6.0, 12.0),
            mob -> mob.distanceToSqr(iron) <= 144.0
               && !mob.getType().is(TAUNT_IMMUNE_TAG)
               && ShadowMonarchManager.canShadowDamage(iron, mob)
               && iron.getSensing().hasLineOfSight(mob)
               && mob.canAttack(iron)
         )
         .stream()
         .sorted(
            Comparator.<Mob>comparingInt(mob -> mob.getTarget() == owner ? 0 : 1)
               .thenComparingDouble(iron::distanceToSqr)
               .thenComparing(mob -> mob.getUUID().toString())
         )
         .limit(8L)
         .toList();
   }

   private static boolean claimTaunt(ShadowIronEntity iron, Mob target, int durationTicks) {
      if (iron != null && target != null && durationTicks > 0 && iron.level() instanceof ServerLevel level && !TankerSkillManager.hasActiveTauntClaim(target)) {
         long now = level.getGameTime();
         ShadowIronCombatManager.TauntState existing = TAUNTS.get(target.getUUID());
         if (existing != null && now < existing.expiresAt) {
            if (!existing.ironId.equals(iron.getUUID())) {
               return false;
            }

            if (existing.expiresAt >= now + durationTicks) {
               return false;
            }
         }

         UUID previous = existing != null && existing.ironId.equals(iron.getUUID())
            ? existing.previousTargetId
            : (target.getTarget() == null ? null : target.getTarget().getUUID());
         boolean hardLock = !isBoss(target);
         int color = ironColor(iron);
         String source = "shadow:iron_taunt:" + iron.getStringUUID().replace("-", "").substring(0, 12);
         List<UUID> viewers = showHighlight(iron, target, source, color, durationTicks);
         ShadowIronCombatManager.TauntState state = new ShadowIronCombatManager.TauntState(
            level, target.getUUID(), iron.getUUID(), previous, now + durationTicks, hardLock, source, viewers, color
         );
         TAUNTS.put(target.getUUID(), state);
         target.setTarget(iron);
         spawnDust(level, target.getBoundingBox().getCenter(), color, 8, Math.max(0.28, target.getBbWidth() * 0.35), 0.025);
         level.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            target.getX(),
            target.getY() + target.getBbHeight() * 0.65,
            target.getZ(),
            3,
            target.getBbWidth() * 0.25,
            target.getBbHeight() * 0.18,
            target.getBbWidth() * 0.25,
            0.015
         );
         return true;
      } else {
         return false;
      }
   }

   private static void tickTaunts() {
      Iterator<ShadowIronCombatManager.TauntState> iterator = TAUNTS.values().iterator();

      while (iterator.hasNext()) {
         ShadowIronCombatManager.TauntState state = iterator.next();
         Entity targetEntity = state.level.getEntity(state.targetId);
         Entity ironEntity = state.level.getEntity(state.ironId);
         long now = state.level.getGameTime();
         if (targetEntity instanceof Mob target && target.isAlive() && ironEntity instanceof ShadowIronEntity iron && iron.isAlive() && now < state.expiresAt) {
            if (state.hardLock && (target.tickCount & 3) == 0) {
               target.setTarget(iron);
            } else if (!state.hardLock && (target.getTarget() == null || !target.getTarget().isAlive())) {
               target.setTarget(iron);
            }

            if (now % 10L == Math.floorMod(target.getId(), 10)) {
               spawnDust(
                  state.level, target.position().add(0.0, target.getBbHeight() * 0.72, 0.0), state.color, 2, Math.max(0.18, target.getBbWidth() * 0.28), 0.005
               );
            }
         } else {
            finishTaunt(state, targetEntity instanceof Mob mob ? mob : null, ironEntity instanceof ShadowIronEntity shadow ? shadow : null);
            iterator.remove();
         }
      }
   }

   private static void finishTaunt(ShadowIronCombatManager.TauntState state, Mob target, ShadowIronEntity iron) {
      for (UUID viewerId : state.viewerIds) {
         ServerPlayer viewer = state.level.getServer().getPlayerList().getPlayer(viewerId);
         if (viewer != null) {
            EntityHighlightSystem.hide(viewer, state.targetId, state.level.dimension(), state.highlightSource);
         }
      }

      if (target != null && iron != null && target.getTarget() == iron) {
         if ((state.previousTargetId == null ? null : state.level.getEntity(state.previousTargetId)) instanceof LivingEntity living
            && living.isAlive()
            && target.canAttack(living)) {
            target.setTarget(living);
         } else {
            target.setTarget(null);
         }
      }
   }

   private static List<UUID> showHighlight(ShadowIronEntity iron, Mob target, String source, int color, int durationTicks) {
      if (ShadowMonarchManager.getShadowOwnerPlayer(iron) instanceof ServerPlayer owner) {
         ArrayList viewers = new ArrayList();

         for (ServerPlayer viewer : PartyService.onlineMembers(owner)) {
            if (viewer.level() == target.level()) {
               EntityHighlightSystem.show(viewer, target, source, color, durationTicks, 325);
               viewers.add(viewer.getUUID());
            }
         }

         return List.copyOf(viewers);
      } else {
         return List.of();
      }
   }

   private static void tryGuardianRescue(ShadowIronEntity iron) {
      Player owner = ShadowMonarchManager.getShadowOwnerPlayer(iron);
      if (owner instanceof ServerPlayer serverOwner && owner.isAlive() && iron.canBlockNow()) {
         String command = ShadowMonarchManager.currentShadowCommand(iron);
         boolean protect = "protect".equals(command);
         if (protect || "default".equals(command)) {
            Projectile projectile = findIncomingProjectile(iron, owner);
            LivingEntity threat = projectile == null ? findGuardianThreat(iron) : null;
            Entity threatEntity = projectile != null ? projectile : threat;
            if (threatEntity != null) {
               double threatDistance = threatEntity.distanceTo(owner);
               double healthRatio = owner.getHealth() / Math.max(1.0F, owner.getMaxHealth());
               if (ShadowIronCombatPolicy.shouldEmergencyIntercept(protect, healthRatio, threatDistance, projectile != null)) {
                  Vec3 destination = safeGuardPosition(iron, owner, threatEntity.position());
                  if (destination != null) {
                     Vec3 oldPosition = iron.position();
                     float facing = (float)(Mth.atan2(threatEntity.getZ() - destination.z, threatEntity.getX() - destination.x) * 180.0F / (float)Math.PI)
                        - 90.0F;
                     iron.teleportTo(destination.x, destination.y, destination.z);
                     iron.setYRot(facing);
                     iron.setYHeadRot(facing);
                     iron.setYBodyRot(facing);
                     if (!iron.beginBlock(true)) {
                        iron.teleportTo(oldPosition.x, oldPosition.y, oldPosition.z);
                     } else {
                        long now = iron.level().getGameTime();
                        iron.setNextInterceptAt(now + (protect ? 160 : 200));
                        RESCUES.put(
                           owner.getUUID(),
                           new ShadowIronCombatManager.RescueState(
                              serverOwner.serverLevel(), owner.getUUID(), iron.getUUID(), threatEntity.getUUID(), now + 14L
                           )
                        );
                        if (threat instanceof Mob mob) {
                           claimTaunt(iron, mob, ShadowIronCombatPolicy.passiveChallengeDuration(isBoss(mob)));
                        }

                        playInterceptEffects(serverOwner.serverLevel(), oldPosition, destination, ironColor(iron));
                     }
                  }
               }
            }
         }
      }
   }

   private static Projectile findIncomingProjectile(ShadowIronEntity iron, Player owner) {
      AABB futureTarget = owner.getBoundingBox().inflate(0.45);
      return owner.level().getEntitiesOfClass(Projectile.class, owner.getBoundingBox().inflate(10.0), projectile -> {
         if (!projectile.isAlive()) {
            return false;
         } else {
            Entity projectileOwner = projectile.getOwner();
            if (projectileOwner != null && !MageCombatHelper.areAllied(iron, projectileOwner)) {
               Vec3 motion = projectile.getDeltaMovement();
               return motion.lengthSqr() > 0.0025 && futureTarget.clip(projectile.position(), projectile.position().add(motion.scale(6.0))).isPresent();
            } else {
               return false;
            }
         }
      }).stream().min(Comparator.comparingDouble(owner::distanceToSqr)).orElse(null);
   }

   private static Vec3 safeGuardPosition(ShadowIronEntity iron, Player owner, Vec3 threatPosition) {
      Vec3 towardThreat = threatPosition.subtract(owner.position()).multiply(1.0, 0.0, 1.0);
      if (towardThreat.lengthSqr() < 1.0E-5) {
         towardThreat = Vec3.directionFromRotation(0.0F, owner.getYRot()).multiply(1.0, 0.0, 1.0);
      }

      towardThreat = towardThreat.normalize();
      double[] angles = new double[]{0.0, 35.0, -35.0, 70.0, -70.0};
      double[] distances = new double[]{1.45, 1.25, 1.65};

      for (double distance : distances) {
         for (double angle : angles) {
            Vec3 direction = towardThreat.yRot((float)Math.toRadians(angle));

            for (double yOffset : new double[]{0.0, 1.0, -1.0}) {
               Vec3 candidate = owner.position().add(direction.scale(distance)).add(0.0, yOffset, 0.0);
               if (isSafeGuardPosition(iron, owner, candidate)) {
                  return candidate;
               }
            }
         }
      }

      return null;
   }

   private static boolean isSafeGuardPosition(ShadowIronEntity iron, Player owner, Vec3 candidate) {
      if (!(iron.level() instanceof ServerLevel level)) {
         return false;
      } else {
         BlockPos var8 = BlockPos.containing(candidate);
         BlockPos floorPos = BlockPos.containing(candidate.x, candidate.y - 0.05, candidate.z);
         if (level.hasChunkAt(var8) && level.getWorldBorder().isWithinBounds(var8) && level.getFluidState(var8).isEmpty()) {
            BlockState floor = level.getBlockState(floorPos);
            if (!floor.isFaceSturdy(level, floorPos, Direction.UP)) {
               return false;
            }

            AABB moved = iron.getBoundingBox().move(candidate.subtract(iron.position()));
            return !moved.intersects(owner.getBoundingBox().inflate(0.05)) && level.noCollision(iron, moved);
         } else {
            return false;
         }
      }
   }

   private static boolean matchesThreat(DamageSource source, UUID threatId) {
      if (source == null || threatId == null) {
         return false;
      } else if (source.getDirectEntity() != null && threatId.equals(source.getDirectEntity().getUUID())) {
         return true;
      } else {
         return source.getEntity() != null && threatId.equals(source.getEntity().getUUID())
            ? true
            : source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() != null && threatId.equals(projectile.getOwner().getUUID());
      }
   }

   private static boolean isMeleeTarget(ShadowIronEntity iron, LivingEntity target) {
      return target != null
         && target.isAlive()
         && ShadowMonarchManager.canShadowDamage(iron, target)
         && CombatRangeHelper.surfaceDistance(iron, target) <= 4.1
         && iron.hasLineOfSight(target);
   }

   private static boolean hurtFromIron(ShadowIronEntity iron, LivingEntity target, float damage) {
      return damage > 0.0F && ShadowMonarchManager.canShadowDamage(iron, target) && target.hurt(iron.level().damageSources().mobAttack(iron), damage);
   }

   private static boolean inForwardArc(ShadowIronEntity iron, LivingEntity target, Vec3 forward) {
      Vec3 toward = target.position().subtract(iron.position()).multiply(1.0, 0.0, 1.0);
      return toward.lengthSqr() > 1.0E-5 && forward.dot(toward.normalize()) >= 0.2;
   }

   private static boolean isElite(LivingEntity target) {
      return DungeonMobLevelAdapter.MobRole.fromString(target.getPersistentData().getString("slr_dungeon_role")) == DungeonMobLevelAdapter.MobRole.ELITE;
   }

   private static int ironColor(ShadowIronEntity iron) {
      Player owner = ShadowMonarchManager.getShadowOwnerPlayer(iron);
      int selected = ShadowMonarchManager.glowColor(owner, "iron");
      if (selected != -1) {
         return selected;
      } else {
         return iron.isDomainBoosted() ? 12016895 : 4442367;
      }
   }

   private static void playInterceptEffects(ServerLevel level, Vec3 from, Vec3 to, int color) {
      level.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1.2, from.z, 14, 0.35, 0.75, 0.35, 0.08);
      level.sendParticles(ParticleTypes.PORTAL, to.x, to.y + 1.2, to.z, 18, 0.32, 0.85, 0.32, 0.06);
      spawnDust(level, to.add(0.0, 1.75, 0.0), color, 10, 0.38, 0.025);
      level.playSound((Player)null, BlockPos.containing(from), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.65F, 0.62F);
      level.playSound((Player)null, BlockPos.containing(to), SoundEvents.ARMOR_EQUIP_IRON, SoundSource.NEUTRAL, 0.95F, 0.72F);
   }

   private static void spawnSweep(ServerLevel level, Vec3 position) {
      level.sendParticles(ParticleTypes.SWEEP_ATTACK, position.x, position.y, position.z, 2, 0.36, 0.25, 0.36, 0.0);
   }

   private static void spawnHit(ServerLevel level, Vec3 position, int color) {
      level.sendParticles(ParticleTypes.CRIT, position.x, position.y, position.z, 7, 0.3, 0.45, 0.3, 0.08);
      spawnDust(level, position, color, 5, 0.25, 0.02);
   }

   private static void spawnDust(ServerLevel level, Vec3 position, int rgb, int count, double spread, double speed) {
      Vector3f color = new Vector3f((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
      level.sendParticles(new DustParticleOptions(color, 1.05F), position.x, position.y, position.z, count, spread, spread, spread, speed);
   }

   private static void tickRings() {
      Iterator<ShadowIronCombatManager.RingBurst> iterator = RINGS.iterator();

      while (iterator.hasNext()) {
         ShadowIronCombatManager.RingBurst ring = iterator.next();
         if (ring.age >= 6) {
            iterator.remove();
         } else {
            double radius = 2.0 + ring.age * 1.9;
            Vector3f color = new Vector3f((ring.color >> 16 & 0xFF) / 255.0F, (ring.color >> 8 & 0xFF) / 255.0F, (ring.color & 0xFF) / 255.0F);
            DustParticleOptions dust = new DustParticleOptions(color, 1.15F);

            for (int point = 0; point < 12; point++) {
               double angle = point * Math.PI * 2.0 / 12.0;
               double x = ring.center.x + Math.cos(angle) * radius;
               double z = ring.center.z + Math.sin(angle) * radius;
               ring.level.sendParticles(dust, x, ring.center.y + 0.16, z, 1, 0.02, 0.02, 0.02, 0.0);
            }

            ring.age++;
         }
      }
   }

   private record RescueState(ServerLevel level, UUID ownerId, UUID ironId, UUID threatId, long expiresAt) {
   }

   private static final class RingBurst {
      private final ServerLevel level;
      private final Vec3 center;
      private final int color;
      private int age;

      private RingBurst(ServerLevel level, Vec3 center, int color, int age) {
         this.level = level;
         this.center = center;
         this.color = color;
         this.age = age;
      }
   }

   private static final class TauntState {
      private final ServerLevel level;
      private final UUID targetId;
      private final UUID ironId;
      private final UUID previousTargetId;
      private final long expiresAt;
      private final boolean hardLock;
      private final String highlightSource;
      private final List<UUID> viewerIds;
      private final int color;

      private TauntState(
         ServerLevel level,
         UUID targetId,
         UUID ironId,
         UUID previousTargetId,
         long expiresAt,
         boolean hardLock,
         String highlightSource,
         List<UUID> viewerIds,
         int color
      ) {
         this.level = level;
         this.targetId = targetId;
         this.ironId = ironId;
         this.previousTargetId = previousTargetId;
         this.expiresAt = expiresAt;
         this.hardLock = hardLock;
         this.highlightSource = highlightSource;
         this.viewerIds = viewerIds;
         this.color = color;
      }
   }
}
