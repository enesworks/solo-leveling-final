package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class AssassinStealthDodgeProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (entity != null) {
         execute(event, entity, event.getSource().getEntity());
      }
   }

   public static void execute(Entity entity, Entity sourceentity) {
      execute(null, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         double scale_opp = 0.0;
         double scale_self = 0.0;
         if (CooldownManager.isOnCooldown(entity, "Stealth") && entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(MobEffects.INVISIBILITY)) {
            if (sourceentity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("hunters")))
               || sourceentity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("dm")))) {
               scale_opp = sourceentity.getPersistentData().getDouble("Level");
            } else if (sourceentity instanceof Player) {
               scale_opp = sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Level;
            }

            if (entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("dm")))
               || entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("hunters")))) {
               scale_self = entity.getPersistentData().getDouble("Level");
            } else if (entity instanceof Player) {
               scale_self = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Level;
            }

            if (Math.random() < scale_self / ((float)scale_opp + 30.0F) && event != null && event.isCancelable()) {
               event.setCanceled(true);
            }
         }
      }
   }
}
