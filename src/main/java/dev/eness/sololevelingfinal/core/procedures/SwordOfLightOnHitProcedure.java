package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class SwordOfLightOnHitProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity, event.getSource().getEntity());
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      execute(null, world, x, y, z, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (sourceentity instanceof LivingEntity _livEnt0
            && _livEnt0.hasEffect(SololevelingModMobEffects.SWORD_OF_LIGHT.get())
            && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
               >= 100.0) {
            double launchPower = 1.2;
            double launchPower2 = 1.0;
            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:dash")),
                     SoundSource.NEUTRAL,
                     0.5F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("sololeveling:dash")), SoundSource.NEUTRAL, 0.5F, 1.0F, false
                  );
               }
            }

            sourceentity.setDeltaMovement(new Vec3(0.0, launchPower2, 0.0));
            syncVelocity(sourceentity);
            entity.setDeltaMovement(new Vec3(0.0, launchPower, 0.0));
            syncVelocity(entity);
            if (sourceentity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.NO_FALL_DAMAGE.get(), 999, 1, false, false));
               _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 5, false, false));
               _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 5, false, false));
            }

            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 5, false, false));
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(
                  SololevelingModParticleTypes.GLOW_YELLOW.get(), sourceentity.getX(), sourceentity.getY() + 0.8, sourceentity.getZ(), 5, 3.0, 3.0, 3.0, 1.0
               );
               _level.sendParticles(
                  SololevelingModParticleTypes.GLOW_AURA_YELLOW.get(),
                  sourceentity.getX(),
                  sourceentity.getY() + 0.8,
                  sourceentity.getZ(),
                  9,
                  0.25,
                  0.6,
                  0.25,
                  0.0
               );
            }
         }
      }
   }

   private static void syncVelocity(Entity entity) {
      if (entity instanceof ServerPlayer serverPlayer) {
         ServerGamePacketListenerImpl connection = serverPlayer.connection;
         if (connection != null) {
            connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
         }
      }
   }
}
