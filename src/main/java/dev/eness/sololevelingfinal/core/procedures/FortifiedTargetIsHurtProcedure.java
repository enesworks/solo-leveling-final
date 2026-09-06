package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class FortifiedTargetIsHurtProcedure {
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
         double scale_opp = 0.0;
         double scale_self = 0.0;
         if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(SololevelingModMobEffects.FORTIFY.get())) {
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

            if (Math.random() < scale_self / ((float)scale_opp + 20.0F)) {
               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 5, 1, false, false));
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.anvil.destroy")),
                        SoundSource.PLAYERS,
                        0.7F,
                        0.5F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.anvil.destroy")), SoundSource.PLAYERS, 0.7F, 0.5F, false
                     );
                  }
               }

               if (event != null && event.isCancelable()) {
                  event.setCanceled(true);
               }
            }
         }
      }
   }
}
