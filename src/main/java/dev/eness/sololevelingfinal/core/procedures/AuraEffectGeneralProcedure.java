package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

@EventBusSubscriber
public class AuraEffectGeneralProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player);
      }
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof LivingEntity _livEnt0
            && _livEnt0.hasEffect(SololevelingModMobEffects.PHYSICAL_BUFF.get())
            && world instanceof ServerLevel _level) {
            _level.sendParticles(
               SololevelingModParticleTypes.PHYSICAL_BUFF_PARTICLE.get(),
               entity.getX(),
               entity.getY() + entity.getBbHeight() / 2.0F,
               entity.getZ(),
               5,
               entity.getBbWidth() * 0.75,
               entity.getBbHeight() / 2.0F,
               entity.getBbWidth() * 0.75,
               1.0
            );
         }

         if (entity instanceof LivingEntity _livEnt9 && _livEnt9.hasEffect(SololevelingModMobEffects.HASTE_BUFF.get()) && world instanceof ServerLevel _level) {
            _level.sendParticles(
               SololevelingModParticleTypes.HASTE_BUFF_PARTICLE.get(),
               entity.getX(),
               entity.getY() + entity.getBbHeight() / 2.0F,
               entity.getZ(),
               5,
               entity.getBbWidth() * 0.75,
               entity.getBbHeight() / 2.0F,
               entity.getBbWidth() * 0.75,
               1.0
            );
         }
      }
   }
}
