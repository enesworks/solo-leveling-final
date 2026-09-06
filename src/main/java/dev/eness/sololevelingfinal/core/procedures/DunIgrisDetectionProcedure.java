package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
public class DunIgrisDetectionProcedure {
   private static final ResourceKey<Level> IGRIS_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling", "dungeon_dimension_igris")
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
         if (entity.level().dimension().equals(IGRIS_DIMENSION)) {
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               if (!capability.tpd) {
                  AABB nearby = AABB.ofSize(entity.position(), 6.0, 6.0, 6.0);
                  boolean reachedPortal = !world.getEntitiesOfClass(Portal12Entity.class, nearby, portal -> portal.distanceToSqr(entity) <= 9.0).isEmpty();
                  if (reachedPortal) {
                     capability.tpd = true;
                     capability.syncPlayerVariables(entity);
                     entity.setNoGravity(false);
                  }
               }
            });
         }
      }
   }
}
