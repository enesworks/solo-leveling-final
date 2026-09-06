package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class FrostExplosionProcedure {
   public static void execute(LevelAccessor world, double x, double z, Entity entity) {
      if (entity != null) {
         double delay = 0.0;
         if (entity instanceof LivingEntity _entity) {
            _entity.swing(InteractionHand.MAIN_HAND, true);
         }

         int horizontalRadiusHemiBot = 17;
         int verticalRadiusHemiBot = 18;
         int yIterationsHemiBot = verticalRadiusHemiBot;

         for (int i = -yIterationsHemiBot; i <= 0; i++) {
            if (i != -verticalRadiusHemiBot) {
               for (int xi = -horizontalRadiusHemiBot; xi <= horizontalRadiusHemiBot; xi++) {
                  for (int zi = -horizontalRadiusHemiBot; zi <= horizontalRadiusHemiBot; zi++) {
                     double distanceSq = (double)(xi * xi) / (horizontalRadiusHemiBot * horizontalRadiusHemiBot)
                        + (double)(i * i) / (verticalRadiusHemiBot * verticalRadiusHemiBot)
                        + (double)(zi * zi) / (horizontalRadiusHemiBot * horizontalRadiusHemiBot);
                     if (distanceSq <= 1.0 && world.getBlockState(BlockPos.containing(x + xi, entity.getY() - 0.5, z + zi)).getBlock() != Blocks.AIR) {
                        world.setBlock(BlockPos.containing(x + xi, entity.getY() - 0.5, z + zi), Blocks.ICE.defaultBlockState(), 3);
                     }
                  }
               }
            }
         }

         Vec3 _center = new Vec3(
            entity.getPersistentData().getDouble("sx"), entity.getPersistentData().getDouble("sy"), entity.getPersistentData().getDouble("sz")
         );

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(18.0), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if ((
                  !(entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                           ? _teamEnt.level()
                              .getScoreboard()
                              .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                              .getName()
                           : "")
                        .equals(
                           entityiterator instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                              ? _teamEnt.level()
                                 .getScoreboard()
                                 .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                 .getName()
                              : ""
                        )
                     || (entity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                           ? _teamEnt.level()
                              .getScoreboard()
                              .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                              .getName()
                           : "")
                        .equals("")
               )
               && entity != entityiterator) {
               entityiterator.hurt(
                  new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity), 3.0F
               );
               if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 200, false, false));
               }

               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.ICECLE
                     .get()
                     .spawn(_level, BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                  }
               }
            }
         }
      }
   }
}
