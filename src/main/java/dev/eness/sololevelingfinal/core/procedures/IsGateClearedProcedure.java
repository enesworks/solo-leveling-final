package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.runtime.SnowRedGateArenaManager;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.GateCompletionTokens;

@EventBusSubscriber
public class IsGateClearedProcedure {
   @SubscribeEvent
   public static void onEntityTick(LivingTickEvent event) {
      execute(event, event.getEntity().level(), event.getEntity());
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null && !entity.level().isClientSide() && entity.tickCount % 20 == 0) {
         if (entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("portals")))) {
            SololevelingModVariables.MapVariables variables = SololevelingModVariables.MapVariables.get(world);
            String gateId = entity.getStringUUID();
            if (GateCompletionTokens.contains(variables.GatesCleared, gateId)) {
               if (entity.getPersistentData().getBoolean("slr_is_red_gate")) {
                  variables.RedGate = world.getServer() != null && SnowRedGateArenaManager.hasActiveArena(world.getServer());
               }

               entity.discard();
               variables.GatesCleared = GateCompletionTokens.remove(variables.GatesCleared, gateId);
               variables.syncData(world);
            }
         }
      }
   }
}
