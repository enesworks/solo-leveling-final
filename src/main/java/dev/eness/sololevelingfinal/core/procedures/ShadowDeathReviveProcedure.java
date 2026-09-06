package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageShadowEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KamishShadowEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowGreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowHighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowPolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowSold1Entity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfShadowEntity;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

@EventBusSubscriber
public final class ShadowDeathReviveProcedure {
   private ShadowDeathReviveProcedure() {
   }

   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         tryRevive(event, event.getEntity());
      }
   }

   public static void execute(Entity entity) {
      tryRevive(null, entity);
   }

   private static void tryRevive(LivingDeathEvent event, Entity entity) {
      if (entity != null && !entity.level().isClientSide() && entity instanceof TamableAnimal tame && tame.isTame() && tame.getOwner() instanceof Player owner) {
         int manaCost = revivalManaCost(entity);
         if (manaCost > 0) {
            SololevelingModVariables.PlayerVariables variables = owner.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
            if (variables != null && !(variables.MP < manaCost)) {
               if (event != null && event.isCancelable()) {
                  event.setCanceled(true);
               }

               variables.MP = Math.max(0.0, variables.MP - manaCost);
               variables.syncPlayerVariables(owner);
               if (entity instanceof LivingEntity living) {
                  living.setHealth(living.getMaxHealth());
               }

               CooldownManager.set(entity, "mana_refresh", entity instanceof IgrisShadowEntity ? 200 : 40);
            }
         }
      }
   }

   private static int revivalManaCost(Entity entity) {
      if (entity instanceof SteelFangWolfShadowEntity) {
         return 100;
      } else if (entity instanceof ShadowSold1Entity || entity instanceof ShadowPolarBearEntity) {
         return 200;
      } else if (entity instanceof ShadowGreenOrcEntity) {
         return 300;
      } else if (entity instanceof GoblinClubShadowEntity
         || entity instanceof GoblinArcherShadowEntity
         || entity instanceof GoblinMageShadowEntity
         || entity instanceof ShadowHighOrcEntity) {
         return 500;
      } else if (entity instanceof IgrisShadowEntity) {
         return 5000;
      } else if (entity instanceof TuskShadowEntity) {
         return 6000;
      } else if (entity instanceof BeruShadowEntity) {
         return 10000;
      } else {
         return entity instanceof KamishShadowEntity ? 20000 : 0;
      }
   }
}
