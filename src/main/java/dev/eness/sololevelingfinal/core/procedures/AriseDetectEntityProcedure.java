package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.GreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.HighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;
import dev.eness.sololevelingfinal.core.entity.PolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;

@EventBusSubscriber
public class AriseDetectEntityProcedure {
   @SubscribeEvent
   public static void onEntityDeath(LivingDeathEvent event) {
      if (event != null && event.getEntity() != null) {
         execute(
            event,
            event.getEntity().level(),
            event.getEntity().getX(),
            event.getEntity().getY(),
            event.getEntity().getZ(),
            event.getEntity(),
            event.getSource().getEntity()
         );
      }
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      execute(null, world, x, y, z, entity, sourceentity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
      if (entity != null && sourceentity != null) {
         Entity creditedKiller = ShadowKillCreditHelper.creditedPlayer(world, sourceentity);
         Entity armorEntity = creditedKiller != null ? creditedKiller : sourceentity;
         boolean sourceIsShadow = ShadowMonarchManager.isShadowEntity(sourceentity);
         boolean canCreateSoul = sourceIsShadow || creditedKiller == sourceentity && isShadowMonarch(sourceentity);
         if (!hasShadowArmor(armorEntity) && canCreateSoul) {
            String soulType = soulTypeFor(entity);
            if (!soulType.isEmpty() && world instanceof ServerLevel level) {
               spawnSoul(level, world, x, y, z, soulType, entity);
            }
         }
      }
   }

   private static String soulTypeFor(Entity entity) {
      if (entity instanceof Zombie
         || entity instanceof Husk
         || entity instanceof Villager
         || entity instanceof ZombieVillager
         || entity instanceof Skeleton
         || entity instanceof Pillager
         || entity instanceof WitherSkeleton
         || entity instanceof DKnight3Entity
         || entity instanceof DKnight2Entity
         || entity instanceof DKnight1Entity
         || entity instanceof Drowned) {
         return "soldier";
      } else if (entity instanceof GoblinClubEntity) {
         return "goblin";
      } else if (entity instanceof GoblinArcherEntity) {
         return "goblinarc";
      } else if (entity instanceof GoblinMageEntity) {
         return "goblinmage";
      } else if (entity instanceof SteelFangWolfEntity || entity instanceof SteelFangedLycanEntity) {
         return "wolf";
      } else if (entity instanceof GreenOrcEntity) {
         return "orc";
      } else if (entity instanceof PolarBearEntity) {
         return "bear";
      } else if (entity instanceof HighOrcEntity) {
         return "highorc";
      } else {
         return entity instanceof KargalganEntity ? "tusk" : "";
      }
   }

   private static void spawnSoul(ServerLevel level, LevelAccessor world, double x, double y, double z, String soulType, Entity defeated) {
      Entity entityToSpawn = SololevelingModEntities.SHADOW_SOUL.get().create(level);
      if (entityToSpawn != null) {
         entityToSpawn.moveTo(x, y, z, world.getRandom().nextFloat() * 360.0F, 0.0F);
         if (entityToSpawn instanceof Mob mobToSpawn) {
            mobToSpawn.finalizeSpawn(level, level.getCurrentDifficultyAt(entityToSpawn.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
         }

         entityToSpawn.getPersistentData().putString("soultype", soulType);
         double targetLevel = DungeonLevelHelper.levelOf(defeated);
         if (targetLevel > 0.0) {
            entityToSpawn.getPersistentData().putDouble("slr_arise_target_level", targetLevel);
         }

         level.addFreshEntity(entityToSpawn);
      }
   }

   private static boolean isShadowMonarch(Entity entity) {
      return entity != null
         && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB == 1.0;
   }

   private static boolean hasShadowArmor(Entity entity) {
      return entity instanceof LivingEntity living
         && (
            living.getItemBySlot(EquipmentSlot.FEET).getItem() == SololevelingModItems.SHADOW_ARMOR_BOOTS.get()
               || living.getItemBySlot(EquipmentSlot.LEGS).getItem() == SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get()
               || living.getItemBySlot(EquipmentSlot.CHEST).getItem() == SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get()
               || living.getItemBySlot(EquipmentSlot.HEAD).getItem() == SololevelingModItems.SHADOW_ARMOR_HELMET.get()
         );
   }
}
