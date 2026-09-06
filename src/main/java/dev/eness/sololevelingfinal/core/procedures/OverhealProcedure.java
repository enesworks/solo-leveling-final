package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class OverhealProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         boolean entity_found = false;
         boolean CanRun = false;
         String found_entity_name = "";
         String ParticleAmount = "";
         String ParticleSpeed = "";
         String ParticleType = "";
         String ParticleMode = "";
         String dx = "";
         String dy = "";
         String dz = "";
         double raytrace_distance = 0.0;
         double DivAmountX = 0.0;
         double Spacing = 0.0;
         double AddDistanceY = 0.0;
         double DistanceX = 0.0;
         double AddDistanceX = 0.0;
         double BX = 0.0;
         double DistanceY = 0.0;
         double DivAmountY = 0.0;
         double AX = 0.0;
         double BY = 0.0;
         double DistanceZ = 0.0;
         double DivAmountZ = 0.0;
         double AddDistanceZ = 0.0;
         double AY = 0.0;
         double BZ = 0.0;
         double AZ = 0.0;
         if (world instanceof ServerLevel _level) {
            _level.sendParticles(SololevelingModParticleTypes.HEALING_PARTICLE.get(), x, y, z, 12, 8.0, 4.0, 8.0, 1.0);
         }

         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ambient.cave")),
                  SoundSource.NEUTRAL,
                  1.0F,
                  1.0F
               );
            } else {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ambient.cave")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
               );
            }
         }

         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10.0), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables())
               .party
               .equals("")) {
               if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 2, false, false));
               }
            } else if (entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .party
                  .equals(
                     entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).party
                  )
               && entityiterator instanceof Player
               && entityiterator instanceof LivingEntity _entity
               && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 2, false, false));
            }
         }
      }
   }
}
