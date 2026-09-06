package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class MeleeActDaggerTickProcedure {
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
         if (!(world instanceof Level level && level.isClientSide())) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).daggermelee
               )
             {
               entity.setDeltaMovement(new Vec3(0.0, 0.0, 0.0));
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .daggermeleetimer
                  + 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.daggermeleetimer = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).daggermeleetimer
                  == 3.0) {
                  BasicAttackSlashProcedure.execute(world, x, y, z, entity, daggerStyle(entity), 0);
               }

               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).daggermeleetimer
                  == 6.0) {
                  BasicAttackSlashProcedure.execute(world, x, y, z, entity, daggerStyle(entity), 1);
               }

               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).daggermeleetimer
                  == 9.0) {
                  boolean _setvalx = false;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.daggermelee = _setval;
                     capability.syncPlayerVariables(entity);
                  });
                  BasicAttackSlashProcedure.execute(world, x, y, z, entity, daggerStyle(entity), 2);
                  _setval = 0.0;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.daggermeleetimer = _setval;
                     capability.syncPlayerVariables(entity);
                  });
               }
            }
         }
      }
   }

   private static int daggerStyle(Entity entity) {
      ItemStack offhand = entity instanceof LivingEntity livingEntity ? livingEntity.getOffhandItem() : ItemStack.EMPTY;
      return offhand.is(ItemTags.create(new ResourceLocation("dagger"))) ? 3 : 2;
   }
}
