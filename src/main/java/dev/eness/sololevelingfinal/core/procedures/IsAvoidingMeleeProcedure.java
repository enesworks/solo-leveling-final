package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

public class IsAvoidingMeleeProcedure {
   public static boolean execute(Entity entity) {
      return entity == null
         ? false
         : (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null
            && (
               (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_HunterClass) : "").equals("Ranger")
                  || (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_HunterClass) : "").equals("Mage")
                  || (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_HunterClass) : "").equals("Healer")
            );
   }
}
