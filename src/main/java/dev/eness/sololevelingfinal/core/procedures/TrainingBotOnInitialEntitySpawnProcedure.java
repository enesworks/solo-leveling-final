package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.TrainingBotEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class TrainingBotOnInitialEntitySpawnProcedure {
   @SubscribeEvent
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      execute(event, event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof TrainingBotEntity
            && !(entity instanceof TrainingBotEntity _datEntL1 && _datEntL1.getEntityData().get(TrainingBotEntity.DATA_set))
            && !world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 50.0, 50.0, 50.0), e -> true).isEmpty()) {
            if (entity instanceof TrainingBotEntity _datEntSetL) {
               _datEntSetL.getEntityData().set(TrainingBotEntity.DATA_set, true);
            }

            double _setval = world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 50.0, 50.0, 50.0), e -> true)
                     .stream()
                     .sorted((new Object() {
                        Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                           return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                        }
                     }).compareDistOf(x, y, z))
                     .findFirst()
                     .orElse(null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Vitality
               * 2.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Vitality = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 50.0, 50.0, 50.0), e -> true)
                     .stream()
                     .sorted((new Object() {
                        Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                           return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                        }
                     }).compareDistOf(x, y, z))
                     .findFirst()
                     .orElse(null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Strength
               * 2.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Strength = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 50.0, 50.0, 50.0), e -> true)
                     .stream()
                     .sorted((new Object() {
                        Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                           return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                        }
                     }).compareDistOf(x, y, z))
                     .findFirst()
                     .orElse(null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Intelligence
               + 0.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Intelligence = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 50.0, 50.0, 50.0), e -> true)
                     .stream()
                     .sorted((new Object() {
                        Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                           return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                        }
                     }).compareDistOf(x, y, z))
                     .findFirst()
                     .orElse(null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Speed
               + 10.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Speed = _setval;
               capability.syncPlayerVariables(entity);
            });
            _setval = world.getEntitiesOfClass(Player.class, AABB.ofSize(new Vec3(x, y, z), 50.0, 50.0, 50.0), e -> true)
                     .stream()
                     .sorted((new Object() {
                        Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                           return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                        }
                     }).compareDistOf(x, y, z))
                     .findFirst()
                     .orElse(null)
                     .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Durability
               + 15.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.Durability = _setval;
               capability.syncPlayerVariables(entity);
            });
            if (entity instanceof LivingEntity _entity) {
               _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
            }
         }
      }
   }
}
