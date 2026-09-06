package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DKCTeamLoadProcedure {
   @SubscribeEvent
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      execute(event, event.getEntity());
   }

   public static void execute(Entity entity) {
      execute(null, entity);
   }

   private static void execute(@Nullable Event event, Entity entity) {
      if (entity != null) {
         if (entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("demons")))) {
            Entity _entityTeam = entity;
            PlayerTeam _pt = _entityTeam.level().getScoreboard().getPlayerTeam("slr_dkc_demon");
            if (_pt != null) {
               if (_entityTeam instanceof Player _player) {
                  _entityTeam.level().getScoreboard().addPlayerToTeam(_player.getGameProfile().getName(), _pt);
               } else {
                  _entityTeam.level().getScoreboard().addPlayerToTeam(_entityTeam.getStringUUID(), _pt);
               }
            }
         }
      }
   }
}
