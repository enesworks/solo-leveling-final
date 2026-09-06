package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class PartyMemberDisableDamageProcedure {
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
         if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
            .party
            .equals("")) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .party
               .equals(
                  sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).party
               )) {
               if (sourceentity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("§e" + entity.getDisplayName().getString() + " §fIs your party member!"), true);
               }

               if (world instanceof Level _level && _level.isClientSide()) {
                  _level.playLocalSound(
                     x,
                     y,
                     z,
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     0.6F,
                     1.0F,
                     false
                  );
               }

               if (event != null && event.isCancelable()) {
                  event.setCanceled(true);
               }
            }

            if (entity instanceof TamableAnimal _tamEnt
               && _tamEnt.isTame()
               && (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != null
               && (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .party
                  .equals(
                     sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .party
                  )) {
               if (sourceentity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(
                     Component.literal(
                        "§fThis pet belongs to your party member §e"
                           + (entity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null).getDisplayName().getString()
                     ),
                     true
                  );
               }

               if (world instanceof Level _level && _level.isClientSide()) {
                  _level.playLocalSound(
                     x,
                     y,
                     z,
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     0.6F,
                     1.0F,
                     false
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
