package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.entity.CurseMagicEntity;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class KargalganHymnOfAgonyProcedure {
   public static void execute(LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof KargalganEntity) {
            ((KargalganEntity)entity).setAnimation("cast1");
         }

         if (world instanceof ServerLevel _level) {
            Entity _entityToSpawn = SololevelingModEntities.CURSE_MAGIC.get().create(_level);
            _entityToSpawn.moveTo(
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getX(),
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getY() + 1.0,
               (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null).getZ(),
               world.getRandom().nextFloat() * 360.0F,
               0.0F
            );
            if (_entityToSpawn instanceof Mob _mobToSpawn) {
               _mobToSpawn.finalizeSpawn(_level, _level.getCurrentDifficultyAt(_entityToSpawn.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
            }

            if (_entityToSpawn instanceof CurseMagicEntity _datEntSetS) {
               _datEntSetS.getEntityData().set(CurseMagicEntity.DATA_owner, entity.getStringUUID());
            }

            _level.addFreshEntity(_entityToSpawn);
         }
      }
   }
}
