package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;

public class BeruUpslamProcedure {
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
            _level.sendParticles(
               SololevelingModParticleTypes.IMPACT_22.get(),
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY()
                  + (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getBbHeight() / 2.0F,
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ(),
               3,
               0.1,
               0.1,
               0.1,
               0.0
            );
         }

         (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
            .setDeltaMovement(new Vec3(0.4 * entity.getLookAngle().x, 2.0, 0.4 * entity.getLookAngle().z));
         (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
            .hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity), 8.0F);
      }
   }
}
