package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.dungeon.runtime.SnowRedGateArenaManager;
import dev.eness.sololevelingfinal.core.entity.Portal1Entity;
import dev.eness.sololevelingfinal.core.entity.PortalAncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.PortalBeruEntity;
import dev.eness.sololevelingfinal.core.entity.PortalCemeteryEntity;
import dev.eness.sololevelingfinal.core.entity.PortalKargalgansThroneRoomEntity;
import dev.eness.sololevelingfinal.core.entity.PortalLabEntity;
import dev.eness.sololevelingfinal.core.entity.PortalLushEntity;
import dev.eness.sololevelingfinal.core.entity.PortalSewersEntity;
import dev.eness.sololevelingfinal.core.entity.RandomCaveLargeEntity;
import dev.eness.sololevelingfinal.core.entity.RedGateEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModGameRules;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public class PortalPerTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         boolean activeDungeon = hasStartedDungeon(entity);
         if (!activeDungeon) {
            entity.getPersistentData().putDouble("PortalLife", entity.getPersistentData().getDouble("PortalLife") + 1.0);
         }

         if (!activeDungeon && entity.getPersistentData().getDouble("PortalLife") >= 24000.0) {
            if (entity.getPersistentData().getBoolean("slr_is_red_gate") && !entity.level().isClientSide() && world.getServer() != null) {
               SololevelingModVariables.MapVariables variables = SololevelingModVariables.MapVariables.get(world);
               variables.RedGate = SnowRedGateArenaManager.hasActiveArena(world.getServer());
               variables.syncData(world);
            }

            if (!entity.level().isClientSide()) {
               entity.discard();
            }

            if (entity.getType() == SololevelingModEntities.DATAPACK_GATE.get()) {
               return;
            }

            if (world.getLevelData().getGameRules().getBoolean(SololevelingModGameRules.SOLO_DUNGEON_BREAK)) {
               if (!GateBreakChanceProcedure.shouldBreak(world, entity)) {
                  return;
               }

               if (entity.getType() == SololevelingModEntities.PORTAL_1.get()) {
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GREEN_ORC.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GREEN_ORC.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MINI_GEM_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GEM_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               } else if (entity instanceof PortalBeruEntity) {
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.RED_ANTS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.RED_ANTS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.RED_ANTS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.RED_ANTS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.RED_ANTS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.RED_ANTS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.RED_ANTS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.BERU_BOSS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               } else if (entity instanceof PortalLushEntity) {
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.STEEL_FANGED_LYCAN
                        .get()
                        .spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.STEEL_FANGED_LYCAN
                        .get()
                        .spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.STONE_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MINI_GEM_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MINI_GEM_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GEM_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               } else if (entity instanceof RedGateEntity) {
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.ICE_ELF.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.ICE_ELF.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.ICE_ELF.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.ICE_ELF.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.ICE_ELF.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.ICE_ELF.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.POLAR_BEAR.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.POLAR_BEAR.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.POLAR_BEAR.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.POLAR_BEAR.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.BARUKA.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               } else if (entity instanceof RandomCaveLargeEntity) {
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.SPIDER_BOSS.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.STONE_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MINI_GEM_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               } else if (entity instanceof PortalSewersEntity) {
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_KING.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_ARCHER.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_ARCHER.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_MAGE.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               } else if (entity instanceof PortalLabEntity) {
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.FUTURISTIC_GOLEM
                        .get()
                        .spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MUTATED.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MUTATED.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MUTATED.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MUTATED.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MUTATED.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.MUTATED.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               } else if (entity instanceof PortalAncientGolemEntity) {
                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.ANCIENT_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.STONE_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.STONE_GOLEM.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               } else if (entity instanceof PortalKargalgansThroneRoomEntity) {
                  for (int index0 = 0; index0 < 12; index0++) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.HIGH_ORC.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                        }
                     }
                  }
               } else if (entity instanceof PortalCemeteryEntity) {
                  for (int index1 = 0; index1 < 6; index1++) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.SKELETON_WARRIOR
                           .get()
                           .spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                        }
                     }
                  }

                  for (int index2 = 0; index2 < 3; index2++) {
                     if (world instanceof ServerLevel _level) {
                        Entity entityToSpawn = SololevelingModEntities.SKELETON_BRUTE
                           .get()
                           .spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                        if (entityToSpawn != null) {
                           entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                        }
                     }
                  }

                  if (world instanceof ServerLevel _level) {
                     Entity entityToSpawn = SololevelingModEntities.SKELETON_SUMMONER
                        .get()
                        .spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                     if (entityToSpawn != null) {
                        entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                     }
                  }
               }
            } else if (!world.isClientSide() && world.getServer() != null) {
               world.getServer().getPlayerList().broadcastSystemMessage(Component.literal("A portal was cleared by unknown party"), false);
            }
         }

         if (SololevelingModVariables.MapVariables.get(world).portalreset && !entity.level().isClientSide()) {
            entity.discard();
         }
      }
   }

   private static boolean hasStartedDungeon(Entity entity) {
      if (entity.getPersistentData().getBoolean("slr_is_red_gate")) {
         return false;
      } else if (entity instanceof Portal1Entity portal) {
         return portal.getEntityData().get(Portal1Entity.DATA_usedbefore);
      } else if (entity instanceof PortalSewersEntity portal) {
         return portal.getEntityData().get(PortalSewersEntity.DATA_usedbefore);
      } else if (entity instanceof PortalCemeteryEntity portal) {
         return portal.getEntityData().get(PortalCemeteryEntity.DATA_usedbefore);
      } else if (entity instanceof PortalAncientGolemEntity portal) {
         return portal.getEntityData().get(PortalAncientGolemEntity.DATA_usedbefore);
      } else if (entity instanceof PortalLabEntity portal) {
         return portal.getEntityData().get(PortalLabEntity.DATA_usedbefore);
      } else if (entity instanceof PortalLushEntity portal) {
         return portal.getEntityData().get(PortalLushEntity.DATA_usedbefore);
      } else if (entity instanceof PortalKargalgansThroneRoomEntity portal) {
         return portal.getEntityData().get(PortalKargalgansThroneRoomEntity.DATA_usedbefore);
      } else if (entity instanceof PortalBeruEntity portal) {
         return portal.getEntityData().get(PortalBeruEntity.DATA_usedbefore);
      } else {
         return entity instanceof RandomCaveLargeEntity portal ? portal.getEntityData().get(RandomCaveLargeEntity.DATA_usedbefore) : false;
      }
   }
}
