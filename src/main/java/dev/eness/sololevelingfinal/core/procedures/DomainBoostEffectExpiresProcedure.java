package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KamishShadowEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowKaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfShadowEntity;

public class DomainBoostEffectExpiresProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof IgrisShadowEntity animatable) {
            animatable.setTexture("igris_shadow_marcus_zero");
         }

         if (entity instanceof SteelFangWolfShadowEntity animatable) {
            animatable.setTexture("lycanshadow");
         }

         if (entity instanceof BeruShadowEntity animatable) {
            animatable.setTexture("beru_shadow");
         }

         if (entity instanceof KamishShadowEntity animatable) {
            animatable.setTexture("dragonshadow");
         }

         if (entity instanceof ShadowKaiselinEntity animatable) {
            animatable.setTexture("shadow_kaiselin");
         }

         if (entity instanceof ShadowIronEntity iron) {
            iron.setDomainBoosted(false);
         }
      }
   }
}
