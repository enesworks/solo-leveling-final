package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;

public class IgrisScreamProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
            if (entity.getPersistentData().getString("state").equals("scream")) {
               if (entity.getPersistentData().getDouble("MF") == 1.0) {
                  if (entity instanceof BloodRedComIgrisEntity) {
                     ((BloodRedComIgrisEntity)entity).setAnimation("attack_scream");
                  }

                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 55, 5, false, false));
                  }
               }

               if (entity.getPersistentData().getDouble("MF") == 19.0) {
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ravager.roar")),
                           SoundSource.NEUTRAL,
                           2.0F,
                           0.5F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ravager.roar")), SoundSource.NEUTRAL, 2.0F, 0.5F, false
                        );
                     }
                  }

                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.warden.roar")),
                           SoundSource.NEUTRAL,
                           2.0F,
                           0.5F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.warden.roar")), SoundSource.NEUTRAL, 2.0F, 0.5F, false
                        );
                     }
                  }

                  Vec3 _center = new Vec3(x + 2.0 * entity.getLookAngle().x, entity.getY() + 1.0, z + 2.0 * entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entityiterator != entity && entityiterator instanceof LivingEntity) {
                        if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                           _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false));
                        }

                        if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                           _entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 10, 0, false, false));
                        }

                        if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                           _entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, false, false));
                        }

                        entityiterator.lookAt(Anchor.EYES, new Vec3(entity.getX(), entity.getY() + 4.0, entity.getZ()));
                     }
                  }
               }

               if (entity.getPersistentData().getDouble("MF") >= 44.0) {
                  entity.getPersistentData().putString("state", "idle");
                  entity.getPersistentData().putDouble("MF", 0.0);
               }
            }
         } else {
            entity.getPersistentData().putString("state", "idle");
            entity.getPersistentData().putDouble("MF", 0.0);
         }
      }
   }
}
