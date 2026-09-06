package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.runtime.SnowRedGateArenaManager;
import dev.eness.sololevelingfinal.core.entity.BaekYoonhoEntity;
import dev.eness.sololevelingfinal.core.entity.BarukaEntity;
import dev.eness.sololevelingfinal.core.entity.ChoijongEntity;
import dev.eness.sololevelingfinal.core.entity.KangTaeshikEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;
import dev.eness.sololevelingfinal.core.util.VesselManager;
import dev.eness.sololevelingfinal.core.world.dimension.rift.RiftTerritory;

@EventBusSubscriber
public class UnlockSkillMurderiousIntentProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         execute(event, event.getEntity(), event.getSource().getEntity());
      }
   }

   public static void execute(Entity entity, Entity sourceentity) {
      execute(null, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (sourceentity instanceof Player) {
            if (SnowRedGateArenaManager.arenaTerritory(entity).orElse(null) == RiftTerritory.FROST
               && (
                  entity instanceof BarukaEntity
                     || sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .Level
                        >= 60.0
               )
               && sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
                  == 0.0
               && Math.random() < 0.15F
               && sourceentity instanceof ServerPlayer vesselPlayer
               && VesselManager.assignPlayer(vesselPlayer, "monarch", "sillad", true) == VesselManager.AssignmentResult.SUCCESS) {
               JobChangeQuestManager.finish(vesselPlayer);
               Advancement _adv = vesselPlayer.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:coldest_monarch"));
               AdvancementProgress _ap = vesselPlayer.getAdvancements().getOrStartProgress(_adv);
               if (!_ap.isDone()) {
                  for (String criteria : _ap.getRemainingCriteria()) {
                     vesselPlayer.getAdvancements().award(_adv, criteria);
                  }
               }

               vesselPlayer.displayClientMessage(Component.literal("You've been chosen as vessel for the \"Frost Monarch\""), false);
            }

            if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Player
               && (entity instanceof Player || entity instanceof KangTaeshikEntity || entity instanceof BaekYoonhoEntity || entity instanceof ChoijongEntity)
               && !sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Plist
                  .contains("Murderious Intent")) {
               String _setval = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .Plist
                  + "Murderious Intent,";
               sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.Plist = _setval;
                  capability.syncPlayerVariables(sourceentity);
               });
               if (sourceentity instanceof ServerPlayer _player) {
                  Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:advancement_murderious_intent"));
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
   }
}
