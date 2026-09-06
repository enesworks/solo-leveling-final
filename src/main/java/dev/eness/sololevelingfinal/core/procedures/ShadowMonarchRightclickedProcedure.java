package dev.eness.sololevelingfinal.core.procedures;

import java.util.Optional;
import net.minecraft.commands.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;
import dev.eness.sololevelingfinal.core.util.VesselManager;

public class ShadowMonarchRightclickedProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof ServerPlayer player
            && (player.getMainHandItem().is(SololevelingModItems.SHADOW_MONARCH.get()) || player.getOffhandItem().is(SololevelingModItems.SHADOW_MONARCH.get()))
            && (int)entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               != 1
            && VesselManager.assignPlayer(player, "ruler", "ashborn", true) == VesselManager.AssignmentResult.SUCCESS) {
            JobChangeQuestManager.finish(player);
            Optional<CommandFunction> function = player.getServer().getFunctions().get(new ResourceLocation("mod:purple_lightning_23"));
            function.ifPresent(value -> player.getServer().getFunctions().execute(value, player.createCommandSourceStack()));
         }

         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB == 1.0
            )
          {
            if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GIVE_BERU.get()
               || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GIVE_BERU.get()) {
               double _setval = 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.berumax = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()
                  == SololevelingModItems.GIVE_IGRIS.get()
               || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GIVE_IGRIS.get()) {
               double _setval = 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.igris = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()
                  == SololevelingModItems.GIVE_KAMISH.get()
               || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GIVE_KAMISH.get()) {
               double _setval = 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.shadowdragonmax = _setval;
                  capability.syncPlayerVariables(entity);
               });
            } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GIVE_TUSK.get()
               || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GIVE_TUSK.get()) {
               double _setval = 1.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.tuskmax = _setval;
                  capability.syncPlayerVariables(entity);
               });
            }
         }
      }
   }
}
