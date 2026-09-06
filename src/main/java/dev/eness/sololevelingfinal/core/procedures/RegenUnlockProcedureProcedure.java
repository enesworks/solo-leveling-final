package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class RegenUnlockProcedureProcedure {
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
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
               < 25.0
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
               >= 10.0) {
            if (!(
                  entity instanceof ServerPlayer _plr0
                     && _plr0.level() instanceof ServerLevel
                     && _plr0.getAdvancements()
                        .getOrStartProgress(_plr0.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:regeneration_level_1")))
                        .isDone()
               )
               && entity instanceof ServerPlayer _player) {
               Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:regeneration_level_1"));
               AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
               if (!_ap.isDone()) {
                  for (String criteria : _ap.getRemainingCriteria()) {
                     _player.getAdvancements().award(_adv, criteria);
                  }
               }
            }
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
               < 50.0
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
               >= 25.0) {
            if (!(
                  entity instanceof ServerPlayer _plr2
                     && _plr2.level() instanceof ServerLevel
                     && _plr2.getAdvancements()
                        .getOrStartProgress(_plr2.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:regeneration_level_2")))
                        .isDone()
               )
               && entity instanceof ServerPlayer _player) {
               Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:regeneration_level_2"));
               AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
               if (!_ap.isDone()) {
                  for (String criteria : _ap.getRemainingCriteria()) {
                     _player.getAdvancements().award(_adv, criteria);
                  }
               }
            }
         } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
               >= 50.0
            && !(
               entity instanceof ServerPlayer _plr4
                  && _plr4.level() instanceof ServerLevel
                  && _plr4.getAdvancements()
                     .getOrStartProgress(_plr4.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:regeneration_level_3")))
                     .isDone()
            )
            && entity instanceof ServerPlayer _player) {
            Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:regeneration_level_3"));
            AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
            if (!_ap.isDone()) {
               for (String criteria : _ap.getRemainingCriteria()) {
                  _player.getAdvancements().award(_adv, criteria);
               }
            }
         }
      }
   }
}
