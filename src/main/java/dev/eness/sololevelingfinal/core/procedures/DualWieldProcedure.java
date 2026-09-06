package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.DualWieldFlurryEntity;
import dev.eness.sololevelingfinal.core.entity.IceChunkEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class DualWieldProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         boolean CanRun = false;
         String ParticleAmount = "";
         String ParticleSpeed = "";
         String ParticleType = "";
         String ParticleMode = "";
         String dx = "";
         String dy = "";
         String dz = "";
         double motionZ = 0.0;
         double deltaZ = 0.0;
         double deltaX = 0.0;
         double motionY = 0.0;
         double Yspeed = 0.0;
         double deltaY = 0.0;
         double motionX = 0.0;
         double speed = 0.0;
         double DivAmountX = 0.0;
         double Spacing = 0.0;
         double AddDistanceY = 0.0;
         double DistanceX = 0.0;
         double AddDistanceX = 0.0;
         double BX = 0.0;
         double DistanceY = 0.0;
         double DivAmountY = 0.0;
         double AX = 0.0;
         double BY = 0.0;
         double DistanceZ = 0.0;
         double DivAmountZ = 0.0;
         double AddDistanceZ = 0.0;
         double AY = 0.0;
         double BZ = 0.0;
         double AZ = 0.0;
         double yaw = 0.0;
         double xpos = 0.0;
         double zpos = 0.0;
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
            >= 550.0) {
            if (!CooldownManager.isOnCooldown(entity, "Dualwield")
               && (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                  .is(ItemTags.create(new ResourceLocation("minecraft:dagger")))
               && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY)
                  .is(ItemTags.create(new ResourceLocation("minecraft:dagger")))) {
               CooldownManager.set(entity, "Dualwield", 260);
               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.NO_FALL_DAMAGE.get(), 999, 1, false, false));
               }

               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 99, false, false));
               }

               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 15, 1, false, false));
               }

               xpos = entity.getX() + 4.0 * entity.getLookAngle().x;
               zpos = entity.getZ() + 4.0 * entity.getLookAngle().z;
               yaw = entity.getYRot();
               entity.getPersistentData().putDouble("SlashX1", entity.getX() + 2.0 * entity.getLookAngle().x);
               entity.getPersistentData().putDouble("SlashX2", entity.getX() + 4.0 * entity.getLookAngle().x);
               entity.getPersistentData().putDouble("SlashX3", entity.getX() + 8.0 * entity.getLookAngle().x);
               entity.getPersistentData().putDouble("SlashZ1", entity.getZ() + 2.0 * entity.getLookAngle().z);
               entity.getPersistentData().putDouble("SlashZ2", entity.getZ() + 4.0 * entity.getLookAngle().z);
               entity.getPersistentData().putDouble("SlashZ3", entity.getZ() + 8.0 * entity.getLookAngle().z);
               entity.getPersistentData().putDouble("SlashX4", entity.getX() + 9.0 * entity.getLookAngle().x);
               entity.getPersistentData().putDouble("SlashZ4", entity.getZ() + 9.0 * entity.getLookAngle().z);
               if (entity instanceof LivingEntity livingEntity) {
                  DualWieldFlurryEntity.spawn(world, livingEntity);
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:slash")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        2.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:slash")), SoundSource.NEUTRAL, 0.5F, 2.0F, false
                     );
                  }
               }

               Vec3 _center = new Vec3(entity.getPersistentData().getDouble("SlashX1"), y, entity.getPersistentData().getDouble("SlashZ1"));

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof IceChunkEntity)
                     && !(entityiterator instanceof ItemEntity)) {
                     entityiterator.hurt(
                        new DamageSource(
                           world.registryAccess()
                              .registryOrThrow(Registries.DAMAGE_TYPE)
                              .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
                           entity
                        ),
                        (float)(10.0 + TemporaryStatBonusManager.effectiveStrength(entity) / 10.0)
                     );
                  }
               }

               _center = new Vec3(entity.getPersistentData().getDouble("SlashX3"), y, entity.getPersistentData().getDouble("SlashZ3"));

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof IceChunkEntity)
                     && !(entityiterator instanceof ItemEntity)) {
                     entityiterator.hurt(
                        new DamageSource(
                           world.registryAccess()
                              .registryOrThrow(Registries.DAMAGE_TYPE)
                              .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
                           entity
                        ),
                        (float)(10.0 + TemporaryStatBonusManager.effectiveStrength(entity) / 10.0)
                     );
                  }
               }

               _center = new Vec3(entity.getPersistentData().getDouble("SlashX2"), y, entity.getPersistentData().getDouble("SlashZ2"));

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entityiterator instanceof ExperienceOrb)
                     && !(entityiterator instanceof IceChunkEntity)
                     && !(entityiterator instanceof ItemEntity)) {
                     entityiterator.hurt(
                        new DamageSource(
                           world.registryAccess()
                              .registryOrThrow(Registries.DAMAGE_TYPE)
                              .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:assassin"))),
                           entity
                        ),
                        (float)(10.0 + TemporaryStatBonusManager.effectiveStrength(entity) / 10.0)
                     );
                  }
               }

               SololevelingMod.queueServerWork(
                  12,
                  () -> {
                     if (!world.getBlockState(
                              BlockPos.containing(entity.getPersistentData().getDouble("SlashX4"), y, entity.getPersistentData().getDouble("SlashZ4"))
                           )
                           .canOcclude()
                        && !world.getBlockState(
                              BlockPos.containing(entity.getPersistentData().getDouble("SlashX4"), y + 1.0, entity.getPersistentData().getDouble("SlashZ4"))
                           )
                           .canOcclude()) {
                        Entity _ent = entity;
                        _ent.teleportTo(entity.getPersistentData().getDouble("SlashX4"), y, entity.getPersistentData().getDouble("SlashZ4"));
                        if (_ent instanceof ServerPlayer _serverPlayer) {
                           _serverPlayer.connection
                              .teleport(
                                 entity.getPersistentData().getDouble("SlashX4"),
                                 y,
                                 entity.getPersistentData().getDouble("SlashZ4"),
                                 _ent.getYRot(),
                                 _ent.getXRot()
                              );
                        }
                     }
                  }
               );
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Not enough MP!"), true);
         }
      }
   }
}
