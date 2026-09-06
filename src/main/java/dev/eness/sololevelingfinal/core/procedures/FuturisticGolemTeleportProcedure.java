package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.FuturisticGolemEntity;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class FuturisticGolemTeleportProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            if (!CooldownManager.isOnCooldown(entity, "golem_teleport")) {
               if (entity.getPersistentData().getDouble("MF") == 1.0) {
                  if (entity instanceof FuturisticGolemEntity) {
                     ((FuturisticGolemEntity)entity).setAnimation("tpattack");
                  }

                  entity.setNoGravity(true);
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 105, 10));
                  }
               }

               if (entity.getPersistentData().getDouble("MF") == 6.0) {
                  Entity _ent = entity;
                  _ent.teleportTo(
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 3.0,
                     (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ()
                  );
                  if (_ent instanceof ServerPlayer _serverPlayer) {
                     _serverPlayer.connection
                        .teleport(
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 3.0,
                           (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ(),
                           _ent.getYRot(),
                           _ent.getXRot()
                        );
                  }
               }

               if (entity.getPersistentData().getDouble("MF") == 15.0) {
                  entity.setNoGravity(false);
               }

               if (entity.getPersistentData().getDouble("MF") == 31.0) {
                  world.addParticle(ParticleTypes.EXPLOSION, x, y, z, 0.0, 0.0, 0.0);
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x,
                           y,
                           z,
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                           SoundSource.NEUTRAL,
                           1.0F,
                           1.0F,
                           false
                        );
                     }
                  }

                  CooldownManager.set(entity, "golem_teleport", 310);
                  Vec3 _center = new Vec3(x, y, z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entity != entityiterator) {
                        entityiterator.hurt(
                           new DamageSource(
                              ((Level)world).registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity
                           ) {
                              @Override
                              public Component getLocalizedDeathMessage(LivingEntity _livingEntity) {
                                 Component _attackerName = null;
                                 Component _entityName = _livingEntity.getDisplayName();
                                 Component _itemName = null;
                                 Entity _attacker = this.getEntity();
                                 ItemStack _itemStack = ItemStack.EMPTY;
                                 if (_attacker != null) {
                                    _attackerName = _attacker.getDisplayName();
                                 }

                                 if (_attacker instanceof LivingEntity _livingAttacker) {
                                    _itemStack = _livingAttacker.getMainHandItem();
                                 }

                                 if (!_itemStack.isEmpty() && _itemStack.hasCustomHoverName()) {
                                    _itemName = _itemStack.getDisplayName();
                                 }

                                 if (_attacker != null && _itemName != null) {
                                    return Component.translatable("death.attack.mob.item", _entityName, _attackerName, _itemName);
                                 } else {
                                    return _attacker != null
                                       ? Component.translatable("death.attack.mob", _entityName, _attackerName)
                                       : Component.translatable("death.attack.mob", _entityName);
                                 }
                              }
                           },
                           26.0F
                        );
                     }
                  }
               }

               if (entity.getPersistentData().getDouble("MF") >= 101.0) {
                  entity.getPersistentData().putString("state", "idle");
                  entity.getPersistentData().putDouble("MF", 0.0);
               }
            } else {
               entity.getPersistentData().putString("state", "idle");
               entity.getPersistentData().putDouble("MF", 0.0);
            }
         } else {
            entity.getPersistentData().putString("state", "idle");
            entity.getPersistentData().putDouble("MF", 0.0);
         }
      }
   }
}
