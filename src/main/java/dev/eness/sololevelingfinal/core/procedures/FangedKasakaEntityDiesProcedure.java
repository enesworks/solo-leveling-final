package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;

public class FangedKasakaEntityDiesProcedure {
   public static void execute(Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (sourceentity instanceof TamableAnimal _tamEnt && _tamEnt.isTame()) {
            if ((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != null
               && (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) instanceof ServerPlayer _player) {
               Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:kasakas_domain"));
               AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
               if (!_ap.isDone()) {
                  for (String criteria : _ap.getRemainingCriteria()) {
                     _player.getAdvancements().award(_adv, criteria);
                  }
               }
            }
         } else if (sourceentity instanceof ServerPlayer _player) {
            Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:kasakas_domain"));
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
