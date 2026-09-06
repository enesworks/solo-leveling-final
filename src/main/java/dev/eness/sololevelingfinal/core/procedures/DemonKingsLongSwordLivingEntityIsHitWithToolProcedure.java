package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class DemonKingsLongSwordLivingEntityIsHitWithToolProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, ItemStack itemstack) {
      if (entity != null && sourceentity != null) {
         double limit = 0.0;
         double max = 0.0;
         if (sourceentity instanceof LivingEntity _livEnt0
            && _livEnt0.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get())
            && !(sourceentity instanceof Player _plrCldCheck2 && !_plrCldCheck2.isCreative() && _plrCldCheck2.getCooldowns().isOnCooldown(itemstack.getItem()))
            )
          {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 4.0) {
               max = 6.0;
            } else {
               max = 3.0;
            }

            if (sourceentity instanceof Player _player && !_player.isCreative()) {
               _player.getCooldowns().addCooldown(itemstack.getItem(), 60);
            }

            Vec3 _center = new Vec3(x, y, z);

            for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(7.5), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
               .toList()) {
               if ((
                     !(sourceentity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                              ? _teamEnt.level()
                                 .getScoreboard()
                                 .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                 .getName()
                              : "")
                           .equals(
                              entityiterator instanceof LivingEntity _teamEnt
                                    && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                                 ? _teamEnt.level()
                                    .getScoreboard()
                                    .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                    .getName()
                                 : ""
                           )
                        || (sourceentity instanceof LivingEntity _teamEnt && _teamEnt.level().getScoreboard().getPlayersTeam(_teamEnt.getStringUUID()) != null
                              ? _teamEnt.level()
                                 .getScoreboard()
                                 .getPlayersTeam(_teamEnt instanceof Player _pl ? _pl.getGameProfile().getName() : _teamEnt.getStringUUID())
                                 .getName()
                              : "")
                           .equals("")
                  )
                  && sourceentity != entityiterator
                  && !(
                     entityiterator instanceof TamableAnimal _tamIsTamedBy && sourceentity instanceof LivingEntity _livEnt && _tamIsTamedBy.isOwnedBy(_livEnt)
                  )
                  && limit <= max) {
                  limit++;
                  if (world instanceof ServerLevel _level) {
                     LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
                     entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ())));
                     entityToSpawn.setVisualOnly(true);
                     _level.addFreshEntity(entityToSpawn);
                  }

                  entityiterator.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), sourceentity),
                     (float)(5.0 + TemporaryStatBonusManager.effectiveIntelligence(sourceentity) / 15.0)
                  );
               }
            }
         }
      }
   }
}
