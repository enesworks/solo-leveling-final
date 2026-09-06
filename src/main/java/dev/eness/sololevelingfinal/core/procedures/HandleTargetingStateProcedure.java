package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import dev.eness.sololevelingfinal.core.entity.SkeletonSummonerEntity;

public class HandleTargetingStateProcedure {
   public static void execute(Entity entity) {
      if (entity != null) {
         List<Object> available_attacks = new ArrayList<>();
         double distance = 0.0;
         Entity target = null;
         target = entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null;
         distance = Math.sqrt(
            Math.pow(entity.getX() - target.getX(), 2.0) + Math.pow(entity.getY() - target.getY(), 2.0) + Math.pow(entity.getZ() - target.getZ(), 2.0)
         );
         if ((entity instanceof SkeletonSummonerEntity _datEntI ? _datEntI.getEntityData().get(SkeletonSummonerEntity.DATA_MeleeCooldown) : 0) <= 0
            && distance <= 5.0) {
            if (entity instanceof SkeletonSummonerEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(SkeletonSummonerEntity.DATA_State, "MELEE");
            }

            if (entity instanceof SkeletonSummonerEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(SkeletonSummonerEntity.DATA_AttackDuration, 0);
            }

            if (entity instanceof SkeletonSummonerEntity _datEntSetI) {
               _datEntSetI.getEntityData().set(SkeletonSummonerEntity.DATA_MeleeType, Mth.nextInt(RandomSource.create(), 1, 2));
            }
         } else {
            if ((entity instanceof SkeletonSummonerEntity _datEntI ? _datEntI.getEntityData().get(SkeletonSummonerEntity.DATA_GlobalAttackCooldown) : 0) <= 0) {
               available_attacks.clear();
               if (Math.random() < 0.33333334F) {
                  available_attacks.add("SUMMONING");
               }

               if (Math.random() < 0.5) {
                  available_attacks.add("PROJECTILE");
               }

               if (Math.random() < 0.5) {
                  available_attacks.add("REPULSION");
               }

               if (!available_attacks.isEmpty()) {
                  if (entity instanceof SkeletonSummonerEntity _datEntSetS) {
                     _datEntSetS.getEntityData()
                        .set(
                           SkeletonSummonerEntity.DATA_State,
                           available_attacks.get(Mth.nextInt(RandomSource.create(), 0, available_attacks.size() - 1)) instanceof String _s ? _s : ""
                        );
                  }

                  if (entity instanceof SkeletonSummonerEntity _datEntSetI) {
                     _datEntSetI.getEntityData().set(SkeletonSummonerEntity.DATA_AttackDuration, 0);
                  }

                  if (entity instanceof SkeletonSummonerEntity _datEntSetI) {
                     _datEntSetI.getEntityData().set(SkeletonSummonerEntity.DATA_GlobalAttackCooldown, 60);
                  }

                  available_attacks.clear();
               }
            }
         }
      }
   }
}
