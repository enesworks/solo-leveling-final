package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

@EventBusSubscriber
public class HunterSetTargetProcedure {
   @SubscribeEvent
   public static void onEntityTick(LivingTickEvent event) {
      execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!(entity instanceof HunterEntity hunter && hunter.isStoryTempleFollower())) {
            if (world.dayTime() % 20L == 0L && entity instanceof HunterEntity) {
               if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) == null) {
                  Vec3 _center = new Vec3(x, y, z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(32.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entity != entityiterator
                        && (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Enemies) : "")
                           .contains(entityiterator.getStringUUID())
                        && entity instanceof Mob _entity
                        && entityiterator instanceof LivingEntity _ent) {
                        _entity.setTarget(_ent);
                     }
                  }
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(32.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && entityiterator instanceof Player
                     && (
                        (new Object() {
                                 public boolean checkGamemode(Entity _ent) {
                                    if (_ent instanceof ServerPlayer _serverPlayer) {
                                       return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
                                    } else {
                                       return _ent.level().isClientSide() && _ent instanceof Player _player
                                          ? Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null
                                             && Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode()
                                                == GameType.CREATIVE
                                          : false;
                                    }
                                 }
                              })
                              .checkGamemode(entityiterator)
                           || (new Object() {
                                 public boolean checkGamemode(Entity _ent) {
                                    if (_ent instanceof ServerPlayer _serverPlayer) {
                                       return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
                                    } else {
                                       return _ent.level().isClientSide() && _ent instanceof Player _player
                                          ? Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null
                                             && Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode()
                                                == GameType.SPECTATOR
                                          : false;
                                    }
                                 }
                              })
                              .checkGamemode(entityiterator)
                     )
                     && (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Enemies) : "")
                        .contains(entityiterator.getStringUUID())
                     && entity instanceof HunterEntity _datEntSetS) {
                     _datEntSetS.getEntityData()
                        .set(
                           HunterEntity.DATA_Enemies,
                           (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Enemies) : "")
                              .replace(entityiterator.getStringUUID(), "")
                        );
                  }
               }

               if (entity instanceof HunterEntity _datEntSetS) {
                  _datEntSetS.getEntityData()
                     .set(
                        HunterEntity.DATA_Enemies,
                        (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Enemies) : "")
                           .replace(entity.getStringUUID(), "")
                     );
               }

               if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null
                  && (
                     (new Object() {
                              public boolean checkGamemode(Entity _ent) {
                                 if (_ent instanceof ServerPlayer _serverPlayer) {
                                    return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
                                 } else {
                                    return _ent.level().isClientSide() && _ent instanceof Player _player
                                       ? Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null
                                          && Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode()
                                             == GameType.CREATIVE
                                       : false;
                                 }
                              }
                           })
                           .checkGamemode(entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
                        || (new Object() {
                              public boolean checkGamemode(Entity _ent) {
                                 if (_ent instanceof ServerPlayer _serverPlayer) {
                                    return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
                                 } else {
                                    return _ent.level().isClientSide() && _ent instanceof Player _player
                                       ? Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null
                                          && Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode()
                                             == GameType.SPECTATOR
                                       : false;
                                 }
                              }
                           })
                           .checkGamemode(entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)
                  )
                  && entity instanceof Mob) {
                  try {
                     ((Mob)entity).setTarget(null);
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               }

               _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(32.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entity != entityiterator
                     && !(entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Allies) : "")
                        .contains(entityiterator.getStringUUID())
                     && (
                        entityiterator instanceof HunterEntity
                           || entity instanceof TamableAnimal _tamIsTamedBy
                              && entityiterator instanceof LivingEntity _livEnt
                              && _tamIsTamedBy.isOwnedBy(_livEnt)
                     )
                     && entity instanceof HunterEntity _datEntSetS) {
                     _datEntSetS.getEntityData()
                        .set(
                           HunterEntity.DATA_Allies,
                           (entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Allies) : "")
                              + ","
                              + entityiterator.getStringUUID()
                        );
                  }
               }
            }
         }
      }
   }
}
