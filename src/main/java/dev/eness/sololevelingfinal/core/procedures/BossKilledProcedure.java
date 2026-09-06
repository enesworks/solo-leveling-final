package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.ProceduralDungeonCompletionHandler;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CartenonTempleManager;
import dev.eness.sololevelingfinal.core.util.InstanceDungeonKeyAccess;
import dev.eness.sololevelingfinal.core.util.KangTaeshikAmbushManager;

@EventBusSubscriber
public class BossKilledProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         Entity creditedSource = ShadowKillCreditHelper.creditedSourceForDeath(
            event.getEntity().level(), event.getEntity(), event.getSource().getEntity(), event.getSource().getDirectEntity()
         );
         execute(event, event.getEntity().level(), event.getEntity(), creditedSource);
      }
   }

   public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
      execute(null, world, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity, Entity sourceentity) {
      if (entity != null) {
         if (!entity.getPersistentData().getBoolean("slr_dungeon_spawned")) {
            if (sourceentity == null) {
               String dungeonTag = resolveDungeonTag(world, entity, null);
               boolean proceduralCompletion = ProceduralDungeonCompletionHandler.isProceduralCompletion(entity, null)
                  || hasActiveProceduralParticipant(world, entity, dungeonTag);
               if (entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss")))
                  && proceduralCompletion
                  && world instanceof ServerLevel level) {
                  if (!ProceduralDungeonCompletionHandler.isExitHandled(entity)) {
                     BlockPos returnPosition = ProceduralDungeonCompletionHandler.resolveUnscopedReturnPosition(level, dungeonTag);
                     ProceduralDungeonCompletionHandler.chooseUnscopedReturnPortal(level, dungeonTag, returnPosition);
                     if (ProceduralDungeonCompletionHandler.spawnUnscopedReturnPortal(level, returnPosition, dungeonTag)) {
                        ProceduralDungeonCompletionHandler.markExitHandled(entity);
                     }
                  }

                  creditEnvironmentalProceduralCompletion(world, entity, dungeonTag);
               }
            } else {
               sourceentity = ShadowKillCreditHelper.creditedSource(world, sourceentity);
               if (sourceentity != null) {
                  Entity creditedSourceentity = sourceentity;
                  if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                           .orElse(new SololevelingModVariables.PlayerVariables())
                        .dungeoning
                     || entity.level().dimension() != Level.OVERWORLD) {
                     if (entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss")))) {
                        if (sourceentity instanceof ServerPlayer player) {
                           KangTaeshikAmbushManager.trySchedule(player, entity);
                        }

                        String defeatedDungeonTag = resolveDungeonTag(world, entity, sourceentity);
                        boolean proceduralCompletion = ProceduralDungeonCompletionHandler.isProceduralCompletion(entity, sourceentity);
                        if (!ProceduralDungeonCompletionHandler.isExitHandled(entity)) {
                           boolean cartenonSpawned;
                           if (proceduralCompletion && world instanceof ServerLevel level) {
                              Optional<List<ServerPlayer>> exactParticipants = ProceduralDungeonCompletionHandler.activeUnscopedParticipants(
                                 level, defeatedDungeonTag
                              );
                              cartenonSpawned = exactParticipants.isPresent()
                                 && CartenonTempleManager.onDungeonBossDefeated(world, entity, sourceentity, defeatedDungeonTag, exactParticipants.get());
                           } else {
                              cartenonSpawned = CartenonTempleManager.onDungeonBossDefeated(world, entity, sourceentity, defeatedDungeonTag);
                           }

                           if (cartenonSpawned) {
                              ProceduralDungeonCompletionHandler.markExitHandled(entity);
                              if (world instanceof ServerLevel level) {
                                 ProceduralDungeonCompletionHandler.chooseCartenonExit(level, defeatedDungeonTag);
                                 ProceduralDungeonCompletionHandler.discardMatchingReturnPortals(level, null, defeatedDungeonTag);
                              }
                           } else if (proceduralCompletion && world instanceof ServerLevel level) {
                              BlockPos returnPosition = ProceduralDungeonCompletionHandler.resolveUnscopedReturnPosition(level, defeatedDungeonTag);
                              ProceduralDungeonCompletionHandler.chooseUnscopedReturnPortal(level, defeatedDungeonTag, returnPosition);
                              if (ProceduralDungeonCompletionHandler.spawnUnscopedReturnPortal(level, returnPosition, defeatedDungeonTag)) {
                                 ProceduralDungeonCompletionHandler.markExitHandled(entity);
                              }
                           }
                        }

                        if (sourceentity instanceof Player) {
                           if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                 .orElse(new SololevelingModVariables.PlayerVariables())
                              .party
                              .equals("")) {
                              boolean _setval = true;
                              sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                 capability.BossKilled = _setval;
                                 capability.syncPlayerVariables(creditedSourceentity);
                              });
                              markGateCleared(world, resolveDungeonTag(world, entity, sourceentity));
                           } else {
                              for (Entity entityiterator : new ArrayList<>(world.players())) {
                                 if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                          .orElse(new SololevelingModVariables.PlayerVariables())
                                       .party
                                       .equals(
                                          entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                                .orElse(new SololevelingModVariables.PlayerVariables())
                                             .party
                                       )
                                    && entity.level().dimension() == entityiterator.level().dimension()) {
                                    boolean _setval = true;
                                    entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                       capability.BossKilled = _setval;
                                       capability.syncPlayerVariables(entityiterator);
                                    });
                                    markGateCleared(world, resolveDungeonTag(world, entity, entityiterator));
                                 }
                              }
                           }
                        } else if (sourceentity instanceof TamableAnimal _tamEnt && _tamEnt.isTame()) {
                           if ((sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null) != null) {
                              if ((sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                                    .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .orElse(new SololevelingModVariables.PlayerVariables())
                                 .party
                                 .equals("")) {
                                 boolean _setval = true;
                                 (sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                                    .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                    .ifPresent(capability -> {
                                       capability.BossKilled = _setval;
                                       capability.syncPlayerVariables(creditedSourceentity instanceof TamableAnimal _tamEntxx ? _tamEntxx.getOwner() : null);
                                    });
                                 markGateCleared(
                                    world, resolveDungeonTag(world, entity, sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                                 );
                              } else {
                                 for (Entity entityiterator : new ArrayList<>(world.players())) {
                                    if ((sourceentity instanceof TamableAnimal _tamEntx ? _tamEntx.getOwner() : null)
                                             .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                             .orElse(new SololevelingModVariables.PlayerVariables())
                                          .party
                                          .equals(
                                             entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                                   .orElse(new SololevelingModVariables.PlayerVariables())
                                                .party
                                          )
                                       && entity.level().dimension() == entityiterator.level().dimension()) {
                                       boolean _setval = true;
                                       entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                          capability.BossKilled = _setval;
                                          capability.syncPlayerVariables(entityiterator);
                                       });
                                       markGateCleared(world, resolveDungeonTag(world, entity, entityiterator));
                                    }
                                 }
                              }
                           }
                        } else {
                           markGateCleared(world, resolveDungeonTag(world, entity, sourceentity));
                        }
                     } else if (entity instanceof FangedKasakaEntity) {
                        boolean kasakaInstance = entity.level().dimension().location().equals(new ResourceLocation("sololeveling", "dungeon_dimension_kasaka"));
                        if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                              .orElse(new SololevelingModVariables.PlayerVariables())
                           .party
                           .equals("")) {
                           boolean _setval = true;
                           sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                              capability.instancecomplete = _setval;
                              capability.syncPlayerVariables(creditedSourceentity);
                           });
                           if (kasakaInstance && sourceentity instanceof Player player) {
                              InstanceDungeonKeyAccess.markCompleted(player);
                           }
                        } else {
                           for (Entity entityiterator : new ArrayList<>(world.players())) {
                              if (sourceentity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                       .orElse(new SololevelingModVariables.PlayerVariables())
                                    .party
                                    .equals(
                                       entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                             .orElse(new SololevelingModVariables.PlayerVariables())
                                          .party
                                    )
                                 && entity.level().dimension() == entityiterator.level().dimension()) {
                                 boolean _setval = true;
                                 entityiterator.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                                    capability.instancecomplete = _setval;
                                    capability.syncPlayerVariables(entityiterator);
                                 });
                                 if (kasakaInstance && entityiterator instanceof Player player) {
                                    InstanceDungeonKeyAccess.markCompleted(player);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void markGateCleared(LevelAccessor world, String dungeonTag) {
      if (dungeonTag != null && !dungeonTag.isEmpty()) {
         String token = dungeonTag + ",";
         if (!SololevelingModVariables.MapVariables.get(world).GatesCleared.contains(token)) {
            SololevelingModVariables.MapVariables.get(world).GatesCleared = SololevelingModVariables.MapVariables.get(world).GatesCleared + token;
            SololevelingModVariables.MapVariables.get(world).syncData(world);
         }
      }
   }

   private static void creditEnvironmentalProceduralCompletion(LevelAccessor world, Entity boss, String dungeonTag) {
      markGateCleared(world, dungeonTag);

      for (Entity candidate : new ArrayList<>(world.players())) {
         if (candidate instanceof ServerPlayer player
            && player.level().dimension() == boss.level().dimension()
            && dungeonTag.equals(player.getPersistentData().getString("dungeon_tag"))) {
            player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.BossKilled = true;
               capability.syncPlayerVariables(player);
            });
         }
      }
   }

   private static boolean hasActiveProceduralParticipant(LevelAccessor world, Entity boss, String dungeonTag) {
      if (dungeonTag != null && !dungeonTag.isBlank()) {
         for (Entity candidate : new ArrayList<>(world.players())) {
            if (candidate.level().dimension() == boss.level().dimension()
               && candidate.getPersistentData().getBoolean("slr_procedural_dungeon")
               && dungeonTag.equals(candidate.getPersistentData().getString("dungeon_tag"))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static String resolveDungeonTag(LevelAccessor world, Entity boss, Entity sourceentity) {
      String tag = dungeonTag(sourceentity);
      if (!tag.isEmpty()) {
         return tag;
      }

      if (sourceentity instanceof TamableAnimal tame && tame.getOwner() != null) {
         tag = dungeonTag(tame.getOwner());
         if (!tag.isEmpty()) {
            return tag;
         }
      }

      tag = dungeonTag(boss);
      if (!tag.isEmpty()) {
         return tag;
      }

      for (Entity player : new ArrayList<>(world.players())) {
         if (boss.level().dimension() == player.level().dimension()) {
            tag = dungeonTag(player);
            if (!tag.isEmpty()) {
               return tag;
            }
         }
      }

      return "";
   }

   private static String dungeonTag(Entity entity) {
      if (entity == null) {
         return "";
      }

      String tag = entity.getPersistentData().getString("dungeon_tag");
      return tag == null ? "" : tag;
   }
}
