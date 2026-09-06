package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class ShadowsReturningMonarchProcedure {
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
         if (entity instanceof TamableAnimal _tamEnt
            && _tamEnt.isTame()
            && !entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).berserk
            && entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
            && Math.sqrt(
                  Math.pow((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getX() - entity.getX(), 2.0)
                     + Math.pow((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getY() - entity.getY(), 2.0)
                     + Math.pow((entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getZ() - entity.getZ(), 2.0)
               )
               > 30.0) {
            Entity _ent = entity;
            _ent.teleportTo(
               (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getX(),
               (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getY(),
               (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getZ()
            );
            if (_ent instanceof ServerPlayer _serverPlayer) {
               _serverPlayer.connection
                  .teleport(
                     (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getX(),
                     (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getY(),
                     (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getZ(),
                     _ent.getYRot(),
                     _ent.getXRot()
                  );
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(
                  ParticleTypes.SQUID_INK,
                  (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getX(),
                  (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getY(),
                  (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getZ(),
                  128,
                  3.0,
                  3.0,
                  3.0,
                  1.0
               );
            }
         }
      }
   }
}
