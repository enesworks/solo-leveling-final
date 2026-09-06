package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.TuskShadowCombatManager;

public final class TuskShadowOnEntityTickUpdateProcedure {
   private TuskShadowOnEntityTickUpdateProcedure() {
   }

   public static void execute(LevelAccessor world, Entity entity) {
      if (world instanceof ServerLevel level && entity instanceof TuskShadowEntity tusk) {
         if (!ShadowMonarchManager.handleUnavailableShadowOwner(tusk)) {
            if (!tusk.isTame() && ShadowMonarchManager.isShadowEntity(tusk)) {
               ShadowMonarchManager.dropStoredShadowInventory(tusk);
               tusk.discard();
            } else {
               emitAmbientShadowParticles(level, tusk);
               TuskShadowCombatManager.tick(tusk);
            }
         }
      }
   }

   private static void emitAmbientShadowParticles(ServerLevel level, TuskShadowEntity tusk) {
      int staggeredTick = tusk.tickCount + tusk.getId();
      if (Math.floorMod(staggeredTick, 4) == 0) {
         double height = tusk.getBbHeight();
         SimpleParticleType mana = tusk.hasEffect(SololevelingModMobEffects.DOMAIN_BOOST.get())
            ? SololevelingModParticleTypes.MANA_PURPLE.get()
            : SololevelingModParticleTypes.MANA_BLUE.get();
         level.sendParticles(
            mana, tusk.getX(), tusk.getY() + height * 0.5, tusk.getZ(), 1, tusk.getBbWidth() * 0.45, height * 0.45, tusk.getBbWidth() * 0.45, 0.025
         );
         if (Math.floorMod(staggeredTick, 8) == 0) {
            level.sendParticles(
               ParticleTypes.SMOKE,
               tusk.getX(),
               tusk.getY() + height * 0.5,
               tusk.getZ(),
               1,
               tusk.getBbWidth() * 0.55,
               height * 0.4,
               tusk.getBbWidth() * 0.55,
               0.02
            );
         }
      }
   }
}
