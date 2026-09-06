package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber
public class DomainChargeProcedure {
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
         if (entity instanceof ServerPlayer _plr0
            && _plr0.level() instanceof ServerLevel
            && _plr0.getAdvancements()
               .getOrStartProgress(_plr0.server.getAdvancements().getAdvancement(new ResourceLocation("sololeveling:monarchs_domain")))
               .isDone()) {
            if (entity.isShiftKeyDown()) {
               if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).domain
                  < 70.0) {
                  double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .domain
                     + 1.0;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.domain = _setval;
                     capability.syncPlayerVariables(entity);
                  });
               }
            } else {
               double _setval = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.domain = _setval;
                  capability.syncPlayerVariables(entity);
               });
            }

            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).domain
                  == 20.0
               && world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x,
                     y,
                     z,
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F,
                     false
                  );
               }
            }

            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).domain
                  == 40.0
               && world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.5F
                  );
               } else {
                  _level.playLocalSound(
                     x,
                     y,
                     z,
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.5F,
                     false
                  );
               }
            }

            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).domain
                  == 60.0
               && world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     2.0F
                  );
               } else {
                  _level.playLocalSound(
                     x,
                     y,
                     z,
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     2.0F,
                     false
                  );
               }
            }

            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).domain
               == 70.0) {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        3.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.experience_orb.pickup")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        3.0F,
                        false
                     );
                  }
               }

               double _setval = 0.0;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.domain = _setval;
                  capability.syncPlayerVariables(entity);
               });
               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0, false, false));
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(50.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity instanceof LivingEntity owner
                     && ShadowMonarchManager.isOwnedShadow(entityiterator, owner)
                     && !entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .domainef) {
                     ShadowMonarchManager.markTemporaryDomainBoost(entityiterator, 3000);
                     boolean _setvalx = true;
                     entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                        capability.domainef = _setval;
                        capability.syncPlayerVariables(entityiterator);
                     });
                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3000, 1, false, false));
                     }

                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3000, 1, false, false));
                     }

                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3000, 1, false, false));
                     }

                     if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                        _entity.addEffect(new MobEffectInstance(SololevelingModMobEffects.DOMAIN_BOOST.get(), 3000, 1, false, false));
                     }
                  }
               }
            }
         }
      }
   }
}
