package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KamishShadowEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowKaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfShadowEntity;

public class DomainBoostEffectStartedappliedProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         if (entity instanceof IgrisShadowEntity animatable) {
            animatable.setTexture("igris_domain_zerotekz");
         }

         if (entity instanceof SteelFangWolfShadowEntity animatable) {
            animatable.setTexture("lycanshadow_purple");
         }

         if (entity instanceof BeruShadowEntity animatable) {
            animatable.setTexture("beru_shadow_domain");
         }

         if (entity instanceof KamishShadowEntity animatable) {
            animatable.setTexture("dragonshadow_purple");
         }

         if (entity instanceof ShadowKaiselinEntity animatable) {
            animatable.setTexture("shadow_kaiselin_domain");
         }

         if (entity instanceof ShadowIronEntity iron) {
            iron.setDomainBoosted(true);
         }
      }
   }
}
