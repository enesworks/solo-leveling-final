package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;

@EventBusSubscriber
public class BearTrapEntityIsHurtProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingHurtEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof BearTrapEntity && entity instanceof TamableAnimal _tamEnt && _tamEnt.isTame()) {
            if (world instanceof Level _level && !_level.isClientSide()) {
               _level.explode(entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null, x, y, z, 3.0F, false, ExplosionInteraction.NONE);
            }

            if (!entity.level().isClientSide()) {
               entity.discard();
            }
         }
      }
   }
}
