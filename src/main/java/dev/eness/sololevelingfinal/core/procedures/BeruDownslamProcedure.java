package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class BeruDownslamProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double sx = 0.0;
         double knockbackres = 0.0;
         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")),
                  SoundSource.PLAYERS,
                  1.0F,
                  1.5F
               );
            } else {
               _level.playLocalSound(
                  x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.explode")), SoundSource.PLAYERS, 1.0F, 1.5F, false
               );
            }
         }

         if (world instanceof ServerLevel _level) {
            _level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 5, 0.1, 0.1, 0.1, 1.0);
         }

         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3.5), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entityiterator != entity
               && !(entityiterator instanceof TamableAnimal _tamIsTamedBy && entity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt))
               && (entityiterator instanceof LivingEntity || entityiterator instanceof Player)) {
               entityiterator.hurt(
                  new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK), entity), 12.0F
               );
               entityiterator.setDeltaMovement(new Vec3(0.0, 0.5, 0.0));
            }
         }
      }
   }
}
