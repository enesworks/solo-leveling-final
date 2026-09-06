package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

@EventBusSubscriber
public class HealerDamageRecieveProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity, event.getSource().getEntity());
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      execute(null, world, x, y, z, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         if (!(entity instanceof HunterEntity hunter && hunter.isStoryTempleFollower())) {
            double particleNum = 0.0;
            double vX = 0.0;
            double vY = 0.0;
            double vZ = 0.0;
            double i = 0.0;
            double x_pos = 0.0;
            double z_pos = 0.0;
            double hei = 0.0;
            double speed = 0.0;
            double arcAngle = 0.0;
            double rand = 0.0;
            double radAngle = 0.0;
            double radYaw = 0.0;
            double radPitch = 0.0;
            double angle = 0.0;
            double y_pos = 0.0;
            double radius = 0.0;
            double rnk = 0.0;
            if (entity instanceof HunterEntity
               && (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_HunterClass) : "").equals("Healer")) {
               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(32.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Allies) : "")
                     .contains(entityiterator.getStringUUID())) {
                     if (entity instanceof Mob) {
                        try {
                           ((Mob)entity).setTarget(null);
                        } catch (Exception e) {
                           e.printStackTrace();
                        }
                     }

                     if (entityiterator instanceof Mob _entity && sourceentity instanceof LivingEntity _ent) {
                        _entity.setTarget(_ent);
                     }
                  }
               }
            }
         }
      }
   }
}
