package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

@EventBusSubscriber
public class BloodEffectProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity.level(), entity, event.getSource().getEntity(), event.getAmount());
      }
   }

   public static void execute(LevelAccessor world, Entity entity, Entity sourceentity, double amount) {
      execute(null, world, entity, sourceentity, amount);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity, double amount) {
      if (entity != null && sourceentity != null) {
         boolean can_initiate = false;
         if (!world.isClientSide()) {
            can_initiate = world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.SOLO_BLOOD_EFFECTS);
            if (can_initiate
               && !entity.isInvulnerable()
               && !entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("shadows")))
               && !(entity instanceof TamableAnimal _tamIsTamedBy && sourceentity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt))
               && amount >= 2.0
               && world instanceof ServerLevel _level) {
               _level.sendParticles(
                  SololevelingModParticleTypes.BLOOD_PARTICLE.get(),
                  entity.getX(),
                  entity.getY() + entity.getBbHeight() * 2.0F / 3.0F,
                  entity.getZ(),
                  45,
                  entity.getBbWidth() / 2.0F,
                  entity.getBbHeight() / 3.0F,
                  entity.getBbWidth() / 2.0F,
                  0.25
               );
            }
         }
      }
   }
}
