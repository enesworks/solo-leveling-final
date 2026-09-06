package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.Portal12Entity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class DunKasakaDetectionProcedure {
   private static final ResourceKey<Level> KASAKA_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling", "dungeon_dimension_kasaka")
   );

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player);
      }
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (!world.isClientSide()) {
            if (entity.level().dimension().equals(KASAKA_DIMENSION)) {
               SololevelingModVariables.PlayerVariables variables = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                  .orElse(null);
               if (variables != null && !variables.tpd) {
                  Entity nearestPortal = world.getEntitiesOfClass(
                        Portal12Entity.class, AABB.ofSize(entity.position(), 6.0, 6.0, 6.0), portal -> portal.distanceToSqr(entity) <= 9.0
                     )
                     .stream()
                     .min(Comparator.comparingDouble(portal -> portal.distanceToSqr(entity)))
                     .orElse(null);
                  if (nearestPortal != null && entity.distanceToSqr(nearestPortal) <= 9.0) {
                     variables.tpd = true;
                     variables.syncPlayerVariables(entity);
                     entity.setNoGravity(false);
                     Entity _ent = entity;
                     _ent.teleportTo(nearestPortal.getX() + 3.0, nearestPortal.getY(), nearestPortal.getZ());
                     if (_ent instanceof ServerPlayer _serverPlayer) {
                        _serverPlayer.connection
                           .teleport(nearestPortal.getX() + 3.0, nearestPortal.getY(), nearestPortal.getZ(), _ent.getYRot(), _ent.getXRot());
                     }
                  }
               }
            }
         }
      }
   }
}
