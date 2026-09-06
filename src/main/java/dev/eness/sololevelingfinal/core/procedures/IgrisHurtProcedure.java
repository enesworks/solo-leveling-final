package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.util.IgrisCombatTeleportHelper;

@EventBusSubscriber
public class IgrisHurtProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity, event.getSource().getEntity());
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      execute(null, world, x, y, z, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         double invulnerable = 0.0;
         double rand = 0.0;
         double randX = 0.0;
         double randZ = 0.0;
         double tprand = 0.0;
         if (entity instanceof BloodRedComIgrisEntity || entity instanceof IgrisShadowEntity) {
            tprand = Mth.nextInt(RandomSource.create(), 1, 2);
            if (tprand == 1.0) {
               rand = Mth.nextInt(RandomSource.create(), 1, 5);
               randX = Mth.nextInt(RandomSource.create(), 1, 2);
               randZ = Mth.nextInt(RandomSource.create(), 1, 2);
               if (randX == 1.0) {
                  randX = 1.0;
               } else if (randX == 2.0) {
                  randX = -1.0;
               }

               if (randZ == 1.0) {
                  randZ = 1.0;
               } else if (randZ == 2.0) {
                  randZ = -1.0;
               }

               if (rand == 3.0) {
                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(SololevelingModParticleTypes.GLOW_AURA_RED.get(), x, y, z, 4, 0.5, 1.5, 0.5, 0.0);
                  }

                  entity.setDeltaMovement(new Vec3(randX, 0.1, randZ));
                  if (world instanceof ServerLevel _level) {
                     _level.sendParticles(SololevelingModParticleTypes.GLOW_AURA_RED.get(), x, y, z, 4, 0.5, 1.5, 0.5, 0.0);
                  }

                  if (event != null && event.isCancelable()) {
                     event.setCanceled(true);
                  }
               }
            } else if (tprand == 2.0) {
               rand = Mth.nextInt(RandomSource.create(), 1, 5);
               if (rand == 3.0) {
                  Vec3 origin = entity.position();
                  if (IgrisCombatTeleportHelper.tryDodgeAttacker(entity, sourceentity)) {
                     if (world instanceof ServerLevel _level) {
                        _level.sendParticles(SololevelingModParticleTypes.GLOW_AURA_RED.get(), origin.x, origin.y, origin.z, 4, 0.5, 1.5, 0.5, 0.0);
                        _level.sendParticles(
                           SololevelingModParticleTypes.GLOW_AURA_RED.get(), entity.getX(), entity.getY(), entity.getZ(), 4, 0.5, 1.5, 0.5, 0.0
                        );
                     }

                     if (event != null && event.isCancelable()) {
                        event.setCanceled(true);
                     }
                  }
               }
            }
         }
      }
   }
}
