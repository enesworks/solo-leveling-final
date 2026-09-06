package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class ShieldBashProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double motionZ = 0.0;
         double deltaZ = 0.0;
         double deltaX = 0.0;
         double motionY = 0.0;
         double Yspeed = 0.0;
         double deltaY = 0.0;
         double motionX = 0.0;
         double speed = 0.0;
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
            >= 300.0) {
            if (!CooldownManager.isOnCooldown(entity, "Shield Bash")) {
               if ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == Items.SHIELD
                  || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY)
                     .is(ItemTags.create(new ResourceLocation("minecraft:shields")))) {
                  double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .MP
                     - 300.0;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.MP = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  CooldownManager.set(entity, "Shield Bash", 200);
                  CooldownManager.set(entity, "mana_refresh", 40);
                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:dash")),
                           SoundSource.NEUTRAL,
                           0.5F,
                           2.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:dash")), SoundSource.NEUTRAL, 0.5F, 2.0F, false
                        );
                     }
                  }

                  if (world instanceof Level _level) {
                     if (!_level.isClientSide()) {
                        _level.playSound(
                           (Player)null,
                           BlockPos.containing(x, y, z),
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                           SoundSource.NEUTRAL,
                           0.25F,
                           1.0F
                        );
                     } else {
                        _level.playLocalSound(
                           x,
                           y,
                           z,
                           ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                           SoundSource.NEUTRAL,
                           0.25F,
                           1.0F,
                           false
                        );
                     }
                  }

                  world.addParticle(ParticleTypes.EXPLOSION, x, y + 1.6, z, 0.0, 1.0, 0.0);
                  deltaX = -Math.sin(entity.getYRot() / 180.0F * (float) Math.PI);
                  deltaY = -Math.sin(entity.getXRot() / 180.0F * (float) Math.PI);
                  deltaZ = Math.cos(entity.getYRot() / 180.0F * (float) Math.PI);
                  speed = 3.0;
                  motionX = deltaX * speed;
                  motionY = 0.0;
                  motionZ = deltaZ * speed;
                  entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.SHIELD_BASH_EFFECT.get(), 60, 1, false, false));
                  }
               } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("You need to hold a shield"), true);
               }
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Not enough MP!"), true);
         }
      }
   }
}
