package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class AttackshardPlayerCollidesWithThisEntityProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         DamageSource _damageSource = new DamageSource(
            world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity
         );
         if (_damageSource != null) {
            sourceentity.hurt(
               new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity), 7.0F
            );
         }

         SololevelingMod.queueServerWork(
            20,
            () -> {
               DamageSource _damageSourcex = new DamageSource(
                  world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity
               );
               if (_damageSourcex != null) {
                  entity.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity), 9999.0F
                  );
               }

               if (world instanceof ServerLevel _level) {
                  _level.sendParticles(SololevelingModParticleTypes.SHARD_PARTICLE.get(), x, y, z, 5, 0.0, 0.0, 0.0, 1.0);
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 2.0F, 1.0F, false
                     );
                  }
               }
            }
         );
      }
   }
}
