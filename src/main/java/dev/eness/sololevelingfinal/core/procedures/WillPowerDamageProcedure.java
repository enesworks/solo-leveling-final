package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class WillPowerDamageProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity.level(), entity.getX(), entity.getZ(), entity, event.getAmount());
      }
   }

   public static void execute(LevelAccessor world, double x, double z, Entity entity, double amount) {
      execute(null, world, x, z, entity, amount);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double z, Entity entity, double amount) {
      if (entity != null) {
         if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(SololevelingModMobEffects.WILL_POWER.get())) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .wp
               + amount;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.wp = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).wp
                  / 4.0
               <= (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1.0F)) {
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(
                     Component.literal(
                        "Damage absorbed: "
                           + Math.round(
                              entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .wp
                           )
                     ),
                     true
                  );
               }
            } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
               _player.displayClientMessage(
                  Component.literal(
                     "§cDamage absorbed: "
                        + Math.round(
                           entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .wp
                        )
                  ),
                  true
               );
            }

            if (event != null && event.isCancelable()) {
               event.setCanceled(true);
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.ASH, x, entity.getY() + 1.0, z, 3, 0.05, 0.05, 0.05, 1.0);
            }
         }
      }
   }
}
