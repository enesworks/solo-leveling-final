package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class SenseEffectProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity.level(), entity.getX(), entity.getY(), entity.getZ(), event.getSource(), entity);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, DamageSource damagesource, Entity entity) {
      execute(null, world, x, y, z, damagesource, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, DamageSource damagesource, Entity entity) {
      if (damagesource != null && entity != null) {
         double rand = 0.0;
         rand = Math.random();
         if (entity instanceof Player
            && (
               damagesource.is(DamageTypes.PLAYER_ATTACK)
                  || damagesource.is(DamageTypes.MOB_ATTACK)
                  || damagesource.is(DamageTypes.MOB_PROJECTILE)
                  || damagesource.is(DamageTypes.MAGIC)
            )
            && rand
               <= entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).perception
                  / 2000.0) {
            rand = Math.random();
            if (event != null && event.isCancelable()) {
               event.setCanceled(true);
            }

            if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(Component.literal("§aRare Dodge!"), true);
            }

            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     2.0F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x,
                     y,
                     z,
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     2.0F,
                     1.0F,
                     false
                  );
               }
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.GLOW_SQUID_INK, x, y, z, 3, 0.1, 0.1, 0.1, 0.2);
            }

            if (rand < 0.25) {
               Entity _ent = entity;
               _ent.teleportTo(x, y, z + 1.0);
               if (_ent instanceof ServerPlayer _serverPlayer) {
                  _serverPlayer.connection.teleport(x, y, z + 1.0, _ent.getYRot(), _ent.getXRot());
               }
            } else if (rand < 0.5) {
               Entity _ent = entity;
               _ent.teleportTo(x, y, z - 1.0);
               if (_ent instanceof ServerPlayer _serverPlayer) {
                  _serverPlayer.connection.teleport(x, y, z - 1.0, _ent.getYRot(), _ent.getXRot());
               }
            } else if (rand < 0.75) {
               Entity _ent = entity;
               _ent.teleportTo(x - 1.0, y, z);
               if (_ent instanceof ServerPlayer _serverPlayer) {
                  _serverPlayer.connection.teleport(x - 1.0, y, z, _ent.getYRot(), _ent.getXRot());
               }
            } else {
               Entity _ent = entity;
               _ent.teleportTo(x + 1.0, y, z);
               if (_ent instanceof ServerPlayer _serverPlayer) {
                  _serverPlayer.connection.teleport(x + 1.0, y, z, _ent.getYRot(), _ent.getXRot());
               }
            }
         }
      }
   }
}
