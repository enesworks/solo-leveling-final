package dev.eness.sololevelingfinal.core.procedures;

import java.text.DecimalFormat;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.AncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.AncientSamuraiEntity;
import dev.eness.sololevelingfinal.core.entity.BarukaEntity;
import dev.eness.sololevelingfinal.core.entity.BeruBossEntity;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.CentipedeEntity;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.entity.FuturisticGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.GreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.HighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;
import dev.eness.sololevelingfinal.core.entity.IceElfEntity;
import dev.eness.sololevelingfinal.core.entity.KangTaeshikEntity;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;
import dev.eness.sololevelingfinal.core.entity.MiniGemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.MutatedEntity;
import dev.eness.sololevelingfinal.core.entity.PolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.RedAntsEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonBruteEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonSummonerEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonWarriorEntity;
import dev.eness.sololevelingfinal.core.entity.SpiderBossEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.entity.StoneGolemEntity;
import dev.eness.sololevelingfinal.core.entity.ThomasAndreEntity;
import dev.eness.sololevelingfinal.core.util.NamedHunterCombatManager;

@EventBusSubscriber
public class EntityLoadedLevelPresetProcedure {
   public static final String LEVEL_STAT_MULTIPLIER_TAG = "SLRLevelStatMultiplier";
   private static final double LUSH_CAVE_LEVEL_SCALING = 0.8;

   @SubscribeEvent
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      execute(event, event.getLevel(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), event.getEntity());
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(null, world, x, y, z, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!NamedHunterCombatManager.isNamedHunter(entity)) {
            double rand = 0.0;
            double rank_addition = 0.0;
            double baseMaxHealth = getBaseAttributeValue(entity, Attributes.MAX_HEALTH);
            double baseAttackDamage = getBaseAttributeValue(entity, Attributes.ATTACK_DAMAGE);
            if (entity.getPersistentData().getDouble("Level") == 0.0) {
               if (!(entity instanceof HunterEntity)) {
                  if (world.getBiome(BlockPos.containing(x, y, z)).is(TagKey.create(Registries.BIOME, new ResourceLocation("dund")))) {
                     rank_addition = 0.0;
                  } else if (world.getBiome(BlockPos.containing(x, y, z)).is(TagKey.create(Registries.BIOME, new ResourceLocation("dunc")))) {
                     rank_addition = 10.0;
                  } else if (world.getBiome(BlockPos.containing(x, y, z)).is(TagKey.create(Registries.BIOME, new ResourceLocation("dunb")))) {
                     rank_addition = 20.0;
                  } else if (world.getBiome(BlockPos.containing(x, y, z)).is(TagKey.create(Registries.BIOME, new ResourceLocation("duna")))) {
                     rank_addition = 30.0;
                  } else if (world.getBiome(BlockPos.containing(x, y, z)).is(TagKey.create(Registries.BIOME, new ResourceLocation("duns")))) {
                     rank_addition = 40.0;
                  } else {
                     rank_addition = 0.0;
                  }
               } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("D")) {
                  rank_addition = 0.0;
               } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("C")) {
                  rank_addition = 15.0;
               } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("B")) {
                  rank_addition = 30.0;
               } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("A")) {
                  rank_addition = 45.0;
               } else if ((entity instanceof HunterEntity _datEntS ? _datEntS.getEntityData().get(HunterEntity.DATA_Rank) : "").equals("S")) {
                  rank_addition = 65.0;
               } else {
                  rank_addition = 0.0;
               }

               if (entity instanceof GoblinClubEntity || entity instanceof GoblinArcherEntity || entity instanceof GoblinMageEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 1.0), (int)(rank_addition + 10.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + rand * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ARMOR)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ARMOR).getBaseValue() + rand * 0.1);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof SteelFangWolfEntity || entity instanceof SteelFangedLycanEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 3.0), (int)(rank_addition + 10.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + rand * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ARMOR)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ARMOR).getBaseValue() + rand * 0.1);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + rand * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof DKnight1Entity || entity instanceof DKnight2Entity || entity instanceof DKnight3Entity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 1.0), (int)(rank_addition + 26.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + rand * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 1.0) * 0.1);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof MutatedEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 1.0), (int)(rank_addition + 21.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 11.0) * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 11.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof IceElfEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 11.0), (int)(rank_addition + 21.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 11.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof CentipedeEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 40, 50);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 55.0) * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 45.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof PolarBearEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 40, 64);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 57.0) * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 57.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof RedAntsEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 11.0), (int)(rank_addition + 31.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 20.0) * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 20.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof BarukaEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 60, 75);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 70.0) * 1.0);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 70.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof BeruBossEntity) {
                  if (entity instanceof BeruBossEntity animatable) {
                     animatable.setTexture("beru_base");
                  }

                  rand = Mth.nextInt(RandomSource.create(), 90, 100);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 95.0) * 0.5);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 95.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof AncientSamuraiEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 25, 35);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 25.0) * 1.0);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 25.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof FangedKasakaEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 10, 20);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 15.0) * 1.0);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 15.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof BloodRedComIgrisEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 58, 70);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).setBaseValue(150.0);
                  ((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(14.0);
                  ((LivingEntity)entity).getAttribute(Attributes.ARMOR).setBaseValue(18.0);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof GoblinKingEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 10, 25);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 10.0) * 1.0);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 10.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof SpiderBossEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 20, 25);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 20.0) * 1.0);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 20.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof GemGolemEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 40, 45);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 45.0) * 1.0);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 45.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof StoneGolemEntity || entity instanceof MiniGemGolemEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 1.0), (int)(rank_addition + 15.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - (rank_addition + 5.0)) * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - (rank_addition + 5.0)) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof FuturisticGolemEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 50, 65);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 55.0) * 1.0);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 55.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof AncientGolemEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 55, 70);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + rand * 0.2);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 62.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof KargalganEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 70, 80);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 75.0) * 0.2);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 75.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof GreenOrcEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 1.0), (int)(rank_addition + 15.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 11.0) * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 11.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof HighOrcEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 11.0), (int)(rank_addition + 31.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 18.0) * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 18.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof KangTaeshikEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 30, 40);
                  entity.getPersistentData().putDouble("Level", rand);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof ThomasAndreEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 120, 150);
                  entity.getPersistentData().putDouble("Level", rand);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof HunterEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 1.0), (int)(rank_addition + 15.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
               }

               if (entity instanceof SkeletonBruteEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 13.0), (int)(rank_addition + 25.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + rand * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + rand * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof SkeletonWarriorEntity) {
                  rand = Mth.nextInt(RandomSource.create(), (int)(rank_addition + 13.0), (int)(rank_addition + 25.0));
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + rand * 0.4);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + rand * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               if (entity instanceof SkeletonSummonerEntity) {
                  rand = Mth.nextInt(RandomSource.create(), 50, 75);
                  entity.setCustomName(Component.literal(entity.getDisplayName().getString() + " (Level: " + new DecimalFormat("##").format(rand) + ")"));
                  entity.getPersistentData().putDouble("Level", rand);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.MAX_HEALTH)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.MAX_HEALTH).getBaseValue() + (rand - 75.0) * 0.2);
                  ((LivingEntity)entity)
                     .getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(((LivingEntity)entity).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + (rand - 75.0) * 0.2);
                  if (entity instanceof LivingEntity _entity) {
                     _entity.setHealth(entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F);
                  }
               }

               applyLevelDifficultyBoost(entity);
               recordLevelStatMultiplier(entity, baseMaxHealth, baseAttackDamage);
            }
         }
      }
   }

   private static double getBaseAttributeValue(Entity entity, Attribute attribute) {
      return entity instanceof LivingEntity living && living.getAttribute(attribute) != null ? living.getAttribute(attribute).getBaseValue() : 0.0;
   }

   private static void recordLevelStatMultiplier(Entity entity, double baseMaxHealth, double baseAttackDamage) {
      if (entity instanceof LivingEntity living && !(entity.getPersistentData().getDouble("Level") <= 0.0)) {
         double multiplier = 1.0;
         double scaledMaxHealth = getBaseAttributeValue(living, Attributes.MAX_HEALTH);
         double scaledAttackDamage = getBaseAttributeValue(living, Attributes.ATTACK_DAMAGE);
         if (baseMaxHealth > 0.0) {
            multiplier = Math.max(multiplier, scaledMaxHealth / baseMaxHealth);
         }

         if (baseAttackDamage > 0.0) {
            multiplier = Math.max(multiplier, scaledAttackDamage / baseAttackDamage);
         }

         entity.getPersistentData().putDouble("SLRLevelStatMultiplier", Math.max(1.0, multiplier));
      }
   }

   private static void applyLevelDifficultyBoost(Entity entity) {
      if (entity instanceof LivingEntity living) {
         if (!(entity instanceof BloodRedComIgrisEntity)) {
            double level = Math.max(0.0, entity.getPersistentData().getDouble("Level"));
            if (!(level <= 0.0)) {
               boolean boss = entity instanceof BloodRedComIgrisEntity
                  || entity instanceof FangedKasakaEntity
                  || entity instanceof GoblinKingEntity
                  || entity instanceof SpiderBossEntity
                  || entity instanceof GemGolemEntity
                  || entity instanceof FuturisticGolemEntity
                  || entity instanceof AncientGolemEntity
                  || entity instanceof KargalganEntity
                  || entity instanceof BarukaEntity
                  || entity instanceof BeruBossEntity
                  || entity instanceof ThomasAndreEntity;
               double dungeonScaling = DunPlaceLushProcedure.isPlacingLushDungeon() ? 0.8 : 1.0;
               if (dungeonScaling < 1.0) {
                  entity.getPersistentData().putBoolean("SLRLushDungeonMob", true);
               }

               double healthPerLevel = (boss ? 1.9 : 1.15) * dungeonScaling;
               double damagePerLevel = (boss ? 0.18 : 0.11) * dungeonScaling;
               double armorPerLevel = (boss ? 0.035 : 0.025) * dungeonScaling;
               if (living.getAttribute(Attributes.MAX_HEALTH) != null) {
                  living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(living.getAttribute(Attributes.MAX_HEALTH).getBaseValue() + level * healthPerLevel);
               }

               if (living.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                  living.getAttribute(Attributes.ATTACK_DAMAGE)
                     .setBaseValue(living.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() + level * damagePerLevel);
               }

               if (living.getAttribute(Attributes.ARMOR) != null) {
                  living.getAttribute(Attributes.ARMOR).setBaseValue(living.getAttribute(Attributes.ARMOR).getBaseValue() + level * armorPerLevel);
               }

               if (living.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
                  living.getAttribute(Attributes.KNOCKBACK_RESISTANCE)
                     .setBaseValue(Math.min(0.95, living.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue() + level * 0.0025 * dungeonScaling));
               }

               living.setHealth(living.getMaxHealth());
            }
         }
      }
   }
}
