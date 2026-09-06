package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.AncientSamuraiEntity;
import dev.eness.sololevelingfinal.core.entity.BarukaEntity;
import dev.eness.sololevelingfinal.core.entity.BeruBossEntity;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.entity.FuturisticGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.KamishEntity;
import dev.eness.sololevelingfinal.core.entity.MutatedEntity;
import dev.eness.sololevelingfinal.core.entity.RedAntsEntity;
import dev.eness.sololevelingfinal.core.entity.SpiderBossEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.entity.StoneGolemEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

@EventBusSubscriber
public class DungeonMobLootDropProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         execute(event, event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof GoblinArcherEntity) {
            if (Math.random() < 0.6F && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_E.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof GoblinClubEntity) {
            if (Math.random() < 0.6F && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_E.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof GoblinMageEntity) {
            if (Math.random() < 0.6F) {
               if (world instanceof ServerLevel _level) {
                  ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_D.get()));
                  entityToSpawn.setPickUpDelay(10);
                  _level.addFreshEntity(entityToSpawn);
               }
            } else if (Math.random() < 0.5 && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_E.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof SteelFangWolfEntity || entity instanceof SteelFangedLycanEntity) {
            if (Math.random() < 0.5) {
               if (world instanceof ServerLevel _level) {
                  ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_D.get()));
                  entityToSpawn.setPickUpDelay(10);
                  _level.addFreshEntity(entityToSpawn);
               }
            } else if (Math.random() < 0.5 && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_D.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof AncientSamuraiEntity) {
            if (Math.random() < 0.6F && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_A.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof StoneGolemEntity) {
            if (Math.random() < 0.4F) {
               if (world instanceof ServerLevel _level) {
                  ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_C.get()));
                  entityToSpawn.setPickUpDelay(10);
                  _level.addFreshEntity(entityToSpawn);
               }
            } else if (Math.random() < 0.6666667F && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_D.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof MutatedEntity) {
            if (Math.random() < 0.2F) {
               if (world instanceof ServerLevel _level) {
                  ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_C.get()));
                  entityToSpawn.setPickUpDelay(10);
                  _level.addFreshEntity(entityToSpawn);
               }
            } else if (Math.random() < 0.2F && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_A.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof RedAntsEntity) {
            if (Math.random() < 0.33333334F) {
               if (world instanceof ServerLevel _level) {
                  ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_C.get()));
                  entityToSpawn.setPickUpDelay(10);
                  _level.addFreshEntity(entityToSpawn);
               }
            } else if (Math.random() < 0.5 && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_A.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof GemGolemEntity) {
            if (Math.random() < 0.5 && world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_A.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof FangedKasakaEntity) {
            if (Math.random() < 0.5) {
               if (world instanceof ServerLevel _level) {
                  ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_S.get()));
                  entityToSpawn.setPickUpDelay(10);
                  _level.addFreshEntity(entityToSpawn);
               }
            } else if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_A.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof BarukaEntity) {
            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_S.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof BeruBossEntity) {
            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_S.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof BloodRedComIgrisEntity) {
            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_S.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof FuturisticGolemEntity) {
            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_S.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof SpiderBossEntity) {
            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_B.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }

            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_B.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof GoblinKingEntity) {
            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_A.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         } else if (entity instanceof KamishEntity) {
            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_S.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }

            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_S.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }

            if (world instanceof ServerLevel _level) {
               ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(SololevelingModItems.MANA_CRYSTAL_S.get()));
               entityToSpawn.setPickUpDelay(10);
               _level.addFreshEntity(entityToSpawn);
            }
         }
      }
   }
}
