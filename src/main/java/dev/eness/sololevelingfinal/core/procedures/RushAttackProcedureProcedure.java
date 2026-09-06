package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public class RushAttackProcedureProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player.getX(), event.player.getY(), event.player.getZ(), event.player);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).rushattack
            )
          {
            entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 1.2, -1.0, entity.getLookAngle().z * 1.2));
            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2.0), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if (entityiterator != entity) {
                  DamageSource _damageSource = new DamageSource(
                     world.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:fighter"))),
                     entity
                  );
                  if (_damageSource != null) {
                     entityiterator.hurt(
                        new DamageSource(
                           world.registryAccess()
                              .registryOrThrow(Registries.DAMAGE_TYPE)
                              .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:fighter"))),
                           entity
                        ),
                        (float)(4.0 + TemporaryStatBonusManager.effectiveStrength(entity) / 20.0)
                     );
                  }

                  entityiterator.setDeltaMovement(new Vec3(entity.getLookAngle().x * 2.0, -1.0, entity.getLookAngle().z * 2.0));
               }
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 3, 1.0, 1.0, 1.0, 0.0);
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(SololevelingModParticleTypes.GOODSLASH_1.get(), x, y, z, 3, 1.0, 1.0, 1.0, 0.0);
            }
         }
      }
   }
}
