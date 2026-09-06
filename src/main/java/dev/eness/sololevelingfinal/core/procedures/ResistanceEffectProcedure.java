package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class ResistanceEffectProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player);
      }
   }

   public static void execute(Entity entity) {
      execute(null, entity);
   }

   private static void execute(@Nullable Event event, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).resistance
            )
          {
            if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(MobEffects.POISON)) {
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("Poison neutralized due to resistance skill"), true);
               }

               if (entity instanceof LivingEntity _entity) {
                  _entity.removeEffect(MobEffects.POISON);
               }
            } else if (entity instanceof LivingEntity _livEnt3 && _livEnt3.hasEffect(MobEffects.WITHER)) {
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("Wither neutralized due to resistance skill"), true);
               }

               if (entity instanceof LivingEntity _entity) {
                  _entity.removeEffect(MobEffects.WITHER);
               }
            } else if (entity instanceof LivingEntity _livEnt6 && _livEnt6.hasEffect(MobEffects.HUNGER)) {
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("Hunger neutralized due to resistance skill"), true);
               }

               if (entity instanceof LivingEntity _entity) {
                  _entity.removeEffect(MobEffects.HUNGER);
               }
            }
         }
      }
   }
}
