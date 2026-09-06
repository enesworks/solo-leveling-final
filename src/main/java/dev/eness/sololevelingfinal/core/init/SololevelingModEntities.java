package dev.eness.sololevelingfinal.core.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import dev.eness.sololevelingfinal.core.entity.AfterImage1Entity;
import dev.eness.sololevelingfinal.core.entity.AfterImage2Entity;
import dev.eness.sololevelingfinal.core.entity.AfterImageEntity;
import dev.eness.sololevelingfinal.core.entity.AncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.AncientSamuraiEntity;
import dev.eness.sololevelingfinal.core.entity.ArcaneVfxEntity;
import dev.eness.sololevelingfinal.core.entity.ArrowSplashEntity;
import dev.eness.sololevelingfinal.core.entity.AttackshardEntity;
import dev.eness.sololevelingfinal.core.entity.BaekYoonhoEntity;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.BarrierVfxEntity;
import dev.eness.sololevelingfinal.core.entity.BarukaEntity;
import dev.eness.sololevelingfinal.core.entity.BasicAttackSlashEntity;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;
import dev.eness.sololevelingfinal.core.entity.BeastVfxEntity;
import dev.eness.sololevelingfinal.core.entity.BellOfHealingEntity;
import dev.eness.sololevelingfinal.core.entity.BeruBossEntity;
import dev.eness.sololevelingfinal.core.entity.BeruDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.CartenonGateEntity;
import dev.eness.sololevelingfinal.core.entity.CentipedeEntity;
import dev.eness.sololevelingfinal.core.entity.CerberusEntity;
import dev.eness.sololevelingfinal.core.entity.ChaHaeInEntity;
import dev.eness.sololevelingfinal.core.entity.ChoijongEntity;
import dev.eness.sololevelingfinal.core.entity.CrossStrikeEntity;
import dev.eness.sololevelingfinal.core.entity.CurseMagicEntity;
import dev.eness.sololevelingfinal.core.entity.CursedChainsEntity;
import dev.eness.sololevelingfinal.core.entity.DKCTowerAuraEntity;
import dev.eness.sololevelingfinal.core.entity.DKnight1Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight2Entity;
import dev.eness.sololevelingfinal.core.entity.DKnight3Entity;
import dev.eness.sololevelingfinal.core.entity.DaggerSlashEntity;
import dev.eness.sololevelingfinal.core.entity.DatapackGateEntity;
import dev.eness.sololevelingfinal.core.entity.DemonEntity;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import dev.eness.sololevelingfinal.core.entity.DetectEyeInvEntity;
import dev.eness.sololevelingfinal.core.entity.DivineArrowEntity;
import dev.eness.sololevelingfinal.core.entity.DragonBreatheEntity;
import dev.eness.sololevelingfinal.core.entity.DragonFireballEntity;
import dev.eness.sololevelingfinal.core.entity.DragonheadEntity;
import dev.eness.sololevelingfinal.core.entity.DualWieldFlurryEntity;
import dev.eness.sololevelingfinal.core.entity.DummyPortalNormalEntity;
import dev.eness.sololevelingfinal.core.entity.DummyPortalPurpleEntity;
import dev.eness.sololevelingfinal.core.entity.DummyPortalRedEntity;
import dev.eness.sololevelingfinal.core.entity.ElderBeastEntity;
import dev.eness.sololevelingfinal.core.entity.EsilRadiruEntity;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.entity.FireFlyEntity;
import dev.eness.sololevelingfinal.core.entity.FireMageVfxEntity;
import dev.eness.sololevelingfinal.core.entity.FlagOfProtectionEntity;
import dev.eness.sololevelingfinal.core.entity.FuturisticGolemEntity;
import dev.eness.sololevelingfinal.core.entity.FxPuddleEntity;
import dev.eness.sololevelingfinal.core.entity.FxspikEntity;
import dev.eness.sololevelingfinal.core.entity.GemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GlacialPursuitEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.HighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.HomingFlameArrowEntity;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;
import dev.eness.sololevelingfinal.core.entity.IceBallEntity;
import dev.eness.sololevelingfinal.core.entity.IceChunkEntity;
import dev.eness.sololevelingfinal.core.entity.IceElfEntity;
import dev.eness.sololevelingfinal.core.entity.IcecleEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.KamishEntity;
import dev.eness.sololevelingfinal.core.entity.KamishShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KangTaeshikEntity;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;
import dev.eness.sololevelingfinal.core.entity.KasakaEntity;
import dev.eness.sololevelingfinal.core.entity.LightBallEntity;
import dev.eness.sololevelingfinal.core.entity.LiuSwordBeamEntity;
import dev.eness.sololevelingfinal.core.entity.LiuSwordVfxEntity;
import dev.eness.sololevelingfinal.core.entity.MagicEyeEntity;
import dev.eness.sololevelingfinal.core.entity.MagicMissileEntity;
import dev.eness.sololevelingfinal.core.entity.MagicalSkullEntity;
import dev.eness.sololevelingfinal.core.entity.ManaArrowEntity;
import dev.eness.sololevelingfinal.core.entity.ManaBulletEntity;
import dev.eness.sololevelingfinal.core.entity.MiniGemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.MutatedEntity;
import dev.eness.sololevelingfinal.core.entity.NecroBlastEntity;
import dev.eness.sololevelingfinal.core.entity.OrcEntity;
import dev.eness.sololevelingfinal.core.entity.OrcShadowEntity;
import dev.eness.sololevelingfinal.core.entity.PolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.Portal12Entity;
import dev.eness.sololevelingfinal.core.entity.Portal1Entity;
import dev.eness.sololevelingfinal.core.entity.PortalAncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.PortalBeruEntity;
import dev.eness.sololevelingfinal.core.entity.PortalCemeteryEntity;
import dev.eness.sololevelingfinal.core.entity.PortalEntity;
import dev.eness.sololevelingfinal.core.entity.PortalJobChangeEntity;
import dev.eness.sololevelingfinal.core.entity.PortalKargalgansThroneRoomEntity;
import dev.eness.sololevelingfinal.core.entity.PortalLabEntity;
import dev.eness.sololevelingfinal.core.entity.PortalLushEntity;
import dev.eness.sololevelingfinal.core.entity.PortalSEntity;
import dev.eness.sololevelingfinal.core.entity.PortalSewersEntity;
import dev.eness.sololevelingfinal.core.entity.QuickSlashesEntity;
import dev.eness.sololevelingfinal.core.entity.RadiruBloodSpearEntity;
import dev.eness.sololevelingfinal.core.entity.RandomCaveLargeEntity;
import dev.eness.sololevelingfinal.core.entity.RangerProjectileEntity;
import dev.eness.sololevelingfinal.core.entity.RedAntsEntity;
import dev.eness.sololevelingfinal.core.entity.RedGateEntity;
import dev.eness.sololevelingfinal.core.entity.RulersAuthorityAuraEntity;
import dev.eness.sololevelingfinal.core.entity.RulersHandEntity;
import dev.eness.sololevelingfinal.core.entity.SecretaryEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowGreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowHighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowIronEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowKaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowPolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowSold1Entity;
import dev.eness.sololevelingfinal.core.entity.ShadowSoulEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowStepEntity;
import dev.eness.sololevelingfinal.core.entity.ShamanMagicEntity;
import dev.eness.sololevelingfinal.core.entity.SilladBossEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonBruteEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonSummonerEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonWarriorEntity;
import dev.eness.sololevelingfinal.core.entity.Slash2Entity;
import dev.eness.sololevelingfinal.core.entity.Slash3Entity;
import dev.eness.sololevelingfinal.core.entity.Slash4Entity;
import dev.eness.sololevelingfinal.core.entity.Slash5Entity;
import dev.eness.sololevelingfinal.core.entity.Slash6Entity;
import dev.eness.sololevelingfinal.core.entity.SlashEffectEntity;
import dev.eness.sololevelingfinal.core.entity.SlashEntity;
import dev.eness.sololevelingfinal.core.entity.SlasheffectswordEntity;
import dev.eness.sololevelingfinal.core.entity.SpawnerPortalEntity;
import dev.eness.sololevelingfinal.core.entity.SpiderBossEntity;
import dev.eness.sololevelingfinal.core.entity.SpiderWebEntity;
import dev.eness.sololevelingfinal.core.entity.StatueOfGodEntity;
import dev.eness.sololevelingfinal.core.entity.StatueaxeEntity;
import dev.eness.sololevelingfinal.core.entity.StatuehammerEntity;
import dev.eness.sololevelingfinal.core.entity.StatueswordEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfShadowEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.entity.StoneGolemEntity;
import dev.eness.sololevelingfinal.core.entity.SungJinWooEntity;
import dev.eness.sololevelingfinal.core.entity.SwordBeamProjectileEntity;
import dev.eness.sololevelingfinal.core.entity.ThomasAndreEntity;
import dev.eness.sololevelingfinal.core.entity.ThrownDaggerEntity;
import dev.eness.sololevelingfinal.core.entity.TrainingBotEntity;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.entity.VulcanEntity;
import dev.eness.sololevelingfinal.core.entity.WhiteFlameEntity;
import dev.eness.sololevelingfinal.core.entity.WhiteFlameVfxEntity;

@EventBusSubscriber(bus = Bus.MOD)
public class SololevelingModEntities {
   public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "sololeveling");
   public static final RegistryObject<EntityType<IgrisEntity>> IGRIS = register(
      "igris",
      Builder.<IgrisEntity>of(IgrisEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(IgrisEntity::new)
         .sized(1.2F, 3.0F)
   );
   public static final RegistryObject<EntityType<ShadowIgrisEntity>> SHADOW_IGRIS = register(
      "shadow_igris",
      Builder.<ShadowIgrisEntity>of(ShadowIgrisEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(ShadowIgrisEntity::new)
         .fireImmune()
         .sized(1.2F, 2.0F)
   );
   public static final RegistryObject<EntityType<ShadowSold1Entity>> SHADOW_SOLD_1 = register(
      "shadow_sold_1",
      Builder.<ShadowSold1Entity>of(ShadowSold1Entity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ShadowSold1Entity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<SungJinWooEntity>> SUNG_JIN_WOO = register(
      "sung_jin_woo",
      Builder.<SungJinWooEntity>of(SungJinWooEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SungJinWooEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<OrcEntity>> ORC = register(
      "orc",
      Builder.<OrcEntity>of(OrcEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(OrcEntity::new)
         .fireImmune()
         .sized(0.8F, 3.0F)
   );
   public static final RegistryObject<EntityType<OrcShadowEntity>> ORC_SHADOW = register(
      "orc_shadow",
      Builder.<OrcShadowEntity>of(OrcShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(OrcShadowEntity::new)
         .fireImmune()
         .sized(0.8F, 3.0F)
   );
   public static final RegistryObject<EntityType<GemGolemEntity>> GEM_GOLEM = register(
      "gem_golem",
      Builder.<GemGolemEntity>of(GemGolemEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(GemGolemEntity::new)
         .fireImmune()
         .sized(1.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<AttackshardEntity>> ATTACKSHARD = register(
      "attackshard",
      Builder.<AttackshardEntity>of(AttackshardEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(1)
         .setUpdateInterval(3)
         .setCustomClientFactory(AttackshardEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<BeruBossEntity>> BERU_BOSS = register(
      "beru_boss",
      Builder.<BeruBossEntity>of(BeruBossEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(BeruBossEntity::new)
         .sized(1.0F, 4.0F)
   );
   public static final RegistryObject<EntityType<BeruShadowEntity>> BERU_SHADOW = register(
      "beru_shadow",
      Builder.<BeruShadowEntity>of(BeruShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(BeruShadowEntity::new)
         .sized(1.0F, 4.0F)
   );
   public static final RegistryObject<EntityType<CentipedeEntity>> CENTIPEDE = register(
      "centipede",
      Builder.<CentipedeEntity>of(CentipedeEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(256)
         .setUpdateInterval(3)
         .setCustomClientFactory(CentipedeEntity::new)
         .sized(3.0F, 3.5F)
   );
   public static final RegistryObject<EntityType<DKnight1Entity>> D_KNIGHT_1 = register(
      "d_knight_1",
      Builder.<DKnight1Entity>of(DKnight1Entity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DKnight1Entity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<DKnight2Entity>> D_KNIGHT_2 = register(
      "d_knight_2",
      Builder.<DKnight2Entity>of(DKnight2Entity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(32)
         .setUpdateInterval(3)
         .setCustomClientFactory(DKnight2Entity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<DKnight3Entity>> D_KNIGHT_3 = register(
      "d_knight_3",
      Builder.<DKnight3Entity>of(DKnight3Entity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(32)
         .setUpdateInterval(3)
         .setCustomClientFactory(DKnight3Entity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<KasakaEntity>> KASAKA = register(
      "kasaka",
      Builder.<KasakaEntity>of(KasakaEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(KasakaEntity::new)
         .sized(3.0F, 2.0F)
   );
   public static final RegistryObject<EntityType<MiniGemGolemEntity>> MINI_GEM_GOLEM = register(
      "mini_gem_golem",
      Builder.<MiniGemGolemEntity>of(MiniGemGolemEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(MiniGemGolemEntity::new)
         .sized(1.0F, 1.8F)
   );
   public static final RegistryObject<EntityType<SteelFangWolfEntity>> STEEL_FANG_WOLF = register(
      "steel_fang_wolf",
      Builder.<SteelFangWolfEntity>of(SteelFangWolfEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SteelFangWolfEntity::new)
         .sized(0.6F, 0.7F)
   );
   public static final RegistryObject<EntityType<SteelFangWolfShadowEntity>> STEEL_FANG_WOLF_SHADOW = register(
      "steel_fang_wolf_shadow",
      Builder.<SteelFangWolfShadowEntity>of(SteelFangWolfShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SteelFangWolfShadowEntity::new)
         .sized(0.6F, 0.7F)
   );
   public static final RegistryObject<EntityType<AncientSamuraiEntity>> ANCIENT_SAMURAI = register(
      "ancient_samurai",
      Builder.<AncientSamuraiEntity>of(AncientSamuraiEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(AncientSamuraiEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<StoneGolemEntity>> STONE_GOLEM = register(
      "stone_golem",
      Builder.<StoneGolemEntity>of(StoneGolemEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(20)
         .setUpdateInterval(3)
         .setCustomClientFactory(StoneGolemEntity::new)
         .sized(0.6F, 0.75F)
   );
   public static final RegistryObject<EntityType<SpiderBossEntity>> SPIDER_BOSS = register(
      "spider_boss",
      Builder.<SpiderBossEntity>of(SpiderBossEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SpiderBossEntity::new)
         .sized(1.5F, 2.5F)
   );
   public static final RegistryObject<EntityType<FireFlyEntity>> FIRE_FLY = register(
      "fire_fly",
      Builder.<FireFlyEntity>of(FireFlyEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(FireFlyEntity::new)
         .fireImmune()
         .sized(0.2F, 0.2F)
   );
   public static final RegistryObject<EntityType<PolarBearEntity>> POLAR_BEAR = register(
      "polar_bear",
      Builder.<PolarBearEntity>of(PolarBearEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PolarBearEntity::new)
         .sized(1.0F, 1.4F)
   );
   public static final RegistryObject<EntityType<ShadowPolarBearEntity>> SHADOW_POLAR_BEAR = register(
      "shadow_polar_bear",
      Builder.<ShadowPolarBearEntity>of(ShadowPolarBearEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ShadowPolarBearEntity::new)
         .sized(1.0F, 1.4F)
   );
   public static final RegistryObject<EntityType<IceElfEntity>> ICE_ELF = register(
      "ice_elf",
      Builder.<IceElfEntity>of(IceElfEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(IceElfEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<BarukaEntity>> BARUKA = register(
      "baruka",
      Builder.<BarukaEntity>of(BarukaEntity::new, MobCategory.AMBIENT)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(BarukaEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<ChoijongEntity>> CHOIJONG = register(
      "choijong",
      Builder.<ChoijongEntity>of(ChoijongEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ChoijongEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<BaekYoonhoEntity>> BAEK_YOONHO = register(
      "baek_yoonho",
      Builder.<BaekYoonhoEntity>of(BaekYoonhoEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(BaekYoonhoEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<MagicEyeEntity>> MAGIC_EYE = register(
      "magic_eye",
      Builder.<MagicEyeEntity>of(MagicEyeEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(MagicEyeEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<GoblinKingEntity>> GOBLIN_KING = register(
      "goblin_king",
      Builder.<GoblinKingEntity>of(GoblinKingEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(GoblinKingEntity::new)
         .sized(2.5F, 4.0F)
   );
   public static final RegistryObject<EntityType<StatueOfGodEntity>> STATUE_OF_GOD = register(
      "statue_of_god",
      Builder.<StatueOfGodEntity>of(StatueOfGodEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(512)
         .setUpdateInterval(3)
         .setCustomClientFactory(StatueOfGodEntity::new)
         .sized(5.25F, 23.25F)
   );
   public static final RegistryObject<EntityType<KangTaeshikEntity>> KANG_TAESHIK = register(
      "kang_taeshik",
      Builder.<KangTaeshikEntity>of(KangTaeshikEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(KangTaeshikEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<RedAntsEntity>> RED_ANTS = register(
      "red_ants",
      Builder.<RedAntsEntity>of(RedAntsEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(RedAntsEntity::new)
         .sized(0.8F, 1.3F)
   );
   public static final RegistryObject<EntityType<ThomasAndreEntity>> THOMAS_ANDRE = register(
      "thomas_andre",
      Builder.<ThomasAndreEntity>of(ThomasAndreEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ThomasAndreEntity::new)
         .sized(0.7F, 2.5F)
   );
   public static final RegistryObject<EntityType<FangedKasakaEntity>> FANGED_KASAKA = register(
      "fanged_kasaka",
      Builder.<FangedKasakaEntity>of(FangedKasakaEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(FangedKasakaEntity::new)
         .sized(5.0F, 7.0F)
   );
   public static final RegistryObject<EntityType<FxPuddleEntity>> FX_PUDDLE = register(
      "fx_puddle",
      Builder.<FxPuddleEntity>of(FxPuddleEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(FxPuddleEntity::new)
         .sized(1.7F, 0.2F)
   );
   public static final RegistryObject<EntityType<FxspikEntity>> FXSPIK = register(
      "fxspik",
      Builder.<FxspikEntity>of(FxspikEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(FxspikEntity::new)
         .sized(0.8F, 5.0F)
   );
   public static final RegistryObject<EntityType<StatueaxeEntity>> STATUEAXE = register(
      "statueaxe",
      Builder.<StatueaxeEntity>of(StatueaxeEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(StatueaxeEntity::new)
         .fireImmune()
         .sized(1.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<StatuehammerEntity>> STATUEHAMMER = register(
      "statuehammer",
      Builder.<StatuehammerEntity>of(StatuehammerEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(StatuehammerEntity::new)
         .fireImmune()
         .sized(1.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<StatueswordEntity>> STATUESWORD = register(
      "statuesword",
      Builder.<StatueswordEntity>of(StatueswordEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(StatueswordEntity::new)
         .fireImmune()
         .sized(1.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<FuturisticGolemEntity>> FUTURISTIC_GOLEM = register(
      "futuristic_golem",
      Builder.<FuturisticGolemEntity>of(FuturisticGolemEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(48)
         .setUpdateInterval(3)
         .setCustomClientFactory(FuturisticGolemEntity::new)
         .sized(1.1F, 3.1F)
   );
   public static final RegistryObject<EntityType<MutatedEntity>> MUTATED = register(
      "mutated",
      Builder.<MutatedEntity>of(MutatedEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(MutatedEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<KamishShadowEntity>> KAMISH_SHADOW = register(
      "kamish_shadow",
      Builder.<KamishShadowEntity>of(KamishShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(KamishShadowEntity::new)
         .fireImmune()
         .sized(1.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<BloodRedComIgrisEntity>> BLOOD_RED_COM_IGRIS = register(
      "blood_red_com_igris",
      Builder.<BloodRedComIgrisEntity>of(BloodRedComIgrisEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(BloodRedComIgrisEntity::new)
         .sized(0.9F, 3.5F)
   );
   public static final RegistryObject<EntityType<IgrisShadowEntity>> IGRIS_SHADOW = register(
      "igris_shadow",
      Builder.<IgrisShadowEntity>of(IgrisShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(IgrisShadowEntity::new)
         .sized(0.9F, 3.5F)
   );
   public static final RegistryObject<EntityType<AncientGolemEntity>> ANCIENT_GOLEM = register(
      "ancient_golem",
      Builder.<AncientGolemEntity>of(AncientGolemEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(AncientGolemEntity::new)
         .fireImmune()
         .sized(1.2F, 4.51F)
   );
   public static final RegistryObject<EntityType<HunterEntity>> HUNTER = register(
      "hunter",
      Builder.<HunterEntity>of(HunterEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(HunterEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<ChaHaeInEntity>> CHA_HAE_IN = register(
      "cha_hae_in",
      Builder.<ChaHaeInEntity>of(ChaHaeInEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(ChaHaeInEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<KargalganEntity>> KARGALGAN = register(
      "kargalgan",
      Builder.<KargalganEntity>of(KargalganEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(KargalganEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<SkeletonWarriorEntity>> SKELETON_WARRIOR = register(
      "skeleton_warrior",
      Builder.<SkeletonWarriorEntity>of(SkeletonWarriorEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SkeletonWarriorEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<SkeletonBruteEntity>> SKELETON_BRUTE = register(
      "skeleton_brute",
      Builder.<SkeletonBruteEntity>of(SkeletonBruteEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SkeletonBruteEntity::new)
         .sized(0.8F, 2.6F)
   );
   public static final RegistryObject<EntityType<KamishEntity>> KAMISH = register(
      "kamish",
      Builder.<KamishEntity>of(KamishEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(KamishEntity::new)
         .fireImmune()
         .sized(1.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<SteelFangedLycanEntity>> STEEL_FANGED_LYCAN = register(
      "steel_fanged_lycan",
      Builder.<SteelFangedLycanEntity>of(SteelFangedLycanEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SteelFangedLycanEntity::new)
         .sized(0.9F, 1.8F)
   );
   public static final RegistryObject<EntityType<DummyPortalNormalEntity>> DUMMY_PORTAL_NORMAL = register(
      "dummy_portal_normal",
      Builder.<DummyPortalNormalEntity>of(DummyPortalNormalEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DummyPortalNormalEntity::new)
         .fireImmune()
         .sized(3.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<DummyPortalRedEntity>> DUMMY_PORTAL_RED = register(
      "dummy_portal_red",
      Builder.<DummyPortalRedEntity>of(DummyPortalRedEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DummyPortalRedEntity::new)
         .fireImmune()
         .sized(3.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<DummyPortalPurpleEntity>> DUMMY_PORTAL_PURPLE = register(
      "dummy_portal_purple",
      Builder.<DummyPortalPurpleEntity>of(DummyPortalPurpleEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DummyPortalPurpleEntity::new)
         .fireImmune()
         .sized(3.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<PortalBeruEntity>> PORTAL_BERU = register(
      "portal_beru",
      Builder.<PortalBeruEntity>of(PortalBeruEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalBeruEntity::new)
         .fireImmune()
         .sized(3.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<PortalSEntity>> PORTAL_S = register(
      "portal_s",
      Builder.<PortalSEntity>of(PortalSEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalSEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<RedGateEntity>> RED_GATE = register(
      "red_gate",
      Builder.<RedGateEntity>of(RedGateEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(RedGateEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<PortalLushEntity>> PORTAL_LUSH = register(
      "portal_lush",
      Builder.<PortalLushEntity>of(PortalLushEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalLushEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<PortalKargalgansThroneRoomEntity>> PORTAL_KARGALGANS_THRONE_ROOM = register(
      "portal_kargalgans_throne_room",
      Builder.<PortalKargalgansThroneRoomEntity>of(PortalKargalgansThroneRoomEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalKargalgansThroneRoomEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<RandomCaveLargeEntity>> RANDOM_CAVE_LARGE = register(
      "random_cave_large",
      Builder.<RandomCaveLargeEntity>of(RandomCaveLargeEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(RandomCaveLargeEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<PortalAncientGolemEntity>> PORTAL_ANCIENT_GOLEM = register(
      "portal_ancient_golem",
      Builder.<PortalAncientGolemEntity>of(PortalAncientGolemEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalAncientGolemEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<PortalEntity>> PORTAL = register(
      "portal",
      Builder.<PortalEntity>of(PortalEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<Portal1Entity>> PORTAL_1 = register(
      "portal_1",
      Builder.<Portal1Entity>of(Portal1Entity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(Portal1Entity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<DatapackGateEntity>> DATAPACK_GATE = register(
      "datapack_gate",
      Builder.<DatapackGateEntity>of(DatapackGateEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DatapackGateEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<SpawnerPortalEntity>> SPAWNER_PORTAL = register(
      "spawner_portal",
      Builder.<SpawnerPortalEntity>of(SpawnerPortalEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SpawnerPortalEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<Portal12Entity>> PORTAL_12 = register(
      "portal_12",
      Builder.<Portal12Entity>of(Portal12Entity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(Portal12Entity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<PortalSewersEntity>> PORTAL_SEWERS = register(
      "portal_sewers",
      Builder.<PortalSewersEntity>of(PortalSewersEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalSewersEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<PortalLabEntity>> PORTAL_LAB = register(
      "portal_lab",
      Builder.<PortalLabEntity>of(PortalLabEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalLabEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<PortalJobChangeEntity>> PORTAL_JOB_CHANGE = register(
      "portal_job_change",
      Builder.<PortalJobChangeEntity>of(PortalJobChangeEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalJobChangeEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<CartenonGateEntity>> CARTENON_GATE = register(
      "cartenon_gate",
      Builder.<CartenonGateEntity>of(CartenonGateEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(2)
         .setCustomClientFactory(CartenonGateEntity::new)
         .fireImmune()
         .sized(1.2F, 2.8F)
   );
   public static final RegistryObject<EntityType<PortalCemeteryEntity>> PORTAL_CEMETERY = register(
      "portal_cemetery",
      Builder.<PortalCemeteryEntity>of(PortalCemeteryEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(PortalCemeteryEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<TrainingBotEntity>> TRAINING_BOT = register(
      "training_bot",
      Builder.<TrainingBotEntity>of(TrainingBotEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(256)
         .setUpdateInterval(3)
         .setCustomClientFactory(TrainingBotEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<ShadowSoulEntity>> SHADOW_SOUL = register(
      "shadow_soul",
      Builder.<ShadowSoulEntity>of(ShadowSoulEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ShadowSoulEntity::new)
         .fireImmune()
         .sized(0.6F, 1.0F)
   );
   public static final RegistryObject<EntityType<FlagOfProtectionEntity>> FLAG_OF_PROTECTION = register(
      "flag_of_protection",
      Builder.<FlagOfProtectionEntity>of(FlagOfProtectionEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(FlagOfProtectionEntity::new)
         .fireImmune()
         .sized(0.3F, 0.8F)
   );
   public static final RegistryObject<EntityType<BellOfHealingEntity>> BELL_OF_HEALING = register(
      "bell_of_healing",
      Builder.<BellOfHealingEntity>of(BellOfHealingEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(BellOfHealingEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<BearTrapEntity>> BEAR_TRAP = register(
      "bear_trap",
      Builder.<BearTrapEntity>of(BearTrapEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(BearTrapEntity::new)
         .fireImmune()
         .sized(0.6F, 0.5F)
   );
   public static final RegistryObject<EntityType<IcecleEntity>> ICECLE = register(
      "icecle",
      Builder.<IcecleEntity>of(IcecleEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(32)
         .setUpdateInterval(3)
         .setCustomClientFactory(IcecleEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<AfterImageEntity>> AFTER_IMAGE = register(
      "after_image",
      Builder.<AfterImageEntity>of(AfterImageEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(1)
         .setUpdateInterval(3)
         .setCustomClientFactory(AfterImageEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<AfterImage1Entity>> AFTER_IMAGE_1 = register(
      "after_image_1",
      Builder.<AfterImage1Entity>of(AfterImage1Entity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(1)
         .setUpdateInterval(3)
         .setCustomClientFactory(AfterImage1Entity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<AfterImage2Entity>> AFTER_IMAGE_2 = register(
      "after_image_2",
      Builder.<AfterImage2Entity>of(AfterImage2Entity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(1)
         .setUpdateInterval(3)
         .setCustomClientFactory(AfterImage2Entity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<SecretaryEntity>> SECRETARY = register(
      "secretary",
      Builder.<SecretaryEntity>of(SecretaryEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SecretaryEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<EsilRadiruEntity>> ESIL_RADIRU = register(
      "esil_radiru",
      Builder.<EsilRadiruEntity>of(EsilRadiruEntity::new, MobCategory.CREATURE)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(EsilRadiruEntity::new)
         .fireImmune()
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<ElderBeastEntity>> ELDER_BEAST = register(
      "elder_beast",
      Builder.<ElderBeastEntity>of(ElderBeastEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ElderBeastEntity::new)
         .sized(2.0F, 4.0F)
   );
   public static final RegistryObject<EntityType<DetectEyeInvEntity>> DETECT_EYE_INV = register(
      "detect_eye_inv",
      Builder.<DetectEyeInvEntity>of(DetectEyeInvEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DetectEyeInvEntity::new)
         .fireImmune()
         .sized(0.1F, 0.1F)
   );
   public static final RegistryObject<EntityType<IceBallEntity>> ICE_BALL = register(
      "ice_ball",
      Builder.<IceBallEntity>of(IceBallEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(IceBallEntity::new)
         .fireImmune()
         .sized(0.3F, 0.3F)
   );
   public static final RegistryObject<EntityType<IceChunkEntity>> ICE_CHUNK = register(
      "ice_chunk",
      Builder.<IceChunkEntity>of(IceChunkEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(IceChunkEntity::new)
         .sized(3.0F, 2.0F)
   );
   public static final RegistryObject<EntityType<DaggerSlashEntity>> DAGGER_SLASH = register(
      "dagger_slash",
      Builder.<DaggerSlashEntity>of(DaggerSlashEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DaggerSlashEntity::new)
         .fireImmune()
         .sized(1.0F, 1.0F)
   );
   public static final RegistryObject<EntityType<ThrownDaggerEntity>> THROWN_DAGGER = register(
      "thrown_dagger",
      Builder.<ThrownDaggerEntity>of(ThrownDaggerEntity::new, MobCategory.MISC)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(96)
         .setUpdateInterval(1)
         .setCustomClientFactory(ThrownDaggerEntity::new)
         .sized(0.45F, 0.25F)
   );
   public static final RegistryObject<EntityType<ArrowSplashEntity>> ARROW_SPLASH = register(
      "arrow_splash",
      Builder.<ArrowSplashEntity>of(ArrowSplashEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ArrowSplashEntity::new)
         .fireImmune()
         .sized(0.2F, 0.2F)
   );
   public static final RegistryObject<EntityType<GoblinClubEntity>> GOBLIN_CLUB = register(
      "goblin_club",
      Builder.<GoblinClubEntity>of(GoblinClubEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(GoblinClubEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<GoblinArcherEntity>> GOBLIN_ARCHER = register(
      "goblin_archer",
      Builder.<GoblinArcherEntity>of(GoblinArcherEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(GoblinArcherEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<GoblinMageEntity>> GOBLIN_MAGE = register(
      "goblin_mage",
      Builder.<GoblinMageEntity>of(GoblinMageEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(GoblinMageEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<GoblinClubShadowEntity>> GOBLIN_CLUB_SHADOW = register(
      "goblin_club_shadow",
      Builder.<GoblinClubShadowEntity>of(GoblinClubShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(GoblinClubShadowEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<GoblinArcherShadowEntity>> GOBLIN_ARCHER_SHADOW = register(
      "goblin_archer_shadow",
      Builder.<GoblinArcherShadowEntity>of(GoblinArcherShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(GoblinArcherShadowEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<GoblinMageShadowEntity>> GOBLIN_MAGE_SHADOW = register(
      "goblin_mage_shadow",
      Builder.<GoblinMageShadowEntity>of(GoblinMageShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(GoblinMageShadowEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<SlasheffectswordEntity>> SLASHEFFECTSWORD = register(
      "slasheffectsword",
      Builder.<SlasheffectswordEntity>of(SlasheffectswordEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SlasheffectswordEntity::new)
         .fireImmune()
         .sized(0.4F, 0.4F)
   );
   public static final RegistryObject<EntityType<IgrisDeadBodyEntity>> IGRIS_DEAD_BODY = register(
      "igris_dead_body",
      Builder.<IgrisDeadBodyEntity>of(IgrisDeadBodyEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(IgrisDeadBodyEntity::new)
         .fireImmune()
         .sized(1.2F, 2.5F)
   );
   public static final RegistryObject<EntityType<BeruDeadBodyEntity>> BERU_DEAD_BODY = register(
      "beru_dead_body",
      Builder.<BeruDeadBodyEntity>of(BeruDeadBodyEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(BeruDeadBodyEntity::new)
         .fireImmune()
         .sized(0.9F, 0.4F)
   );
   public static final RegistryObject<EntityType<CursedChainsEntity>> CURSED_CHAINS = register(
      "cursed_chains",
      Builder.<CursedChainsEntity>of(CursedChainsEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(CursedChainsEntity::new)
         .fireImmune()
         .sized(0.3F, 0.3F)
   );
   public static final RegistryObject<EntityType<DragonheadEntity>> DRAGONHEAD = register(
      "dragonhead",
      Builder.<DragonheadEntity>of(DragonheadEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DragonheadEntity::new)
         .fireImmune()
         .sized(0.6F, 0.8F)
   );
   public static final RegistryObject<EntityType<CurseMagicEntity>> CURSE_MAGIC = register(
      "curse_magic",
      Builder.<CurseMagicEntity>of(CurseMagicEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(CurseMagicEntity::new)
         .fireImmune()
         .sized(0.1F, 0.1F)
   );
   public static final RegistryObject<EntityType<GreenOrcEntity>> GREEN_ORC = register(
      "green_orc",
      Builder.<GreenOrcEntity>of(GreenOrcEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(GreenOrcEntity::new)
         .sized(0.9F, 4.0F)
   );
   public static final RegistryObject<EntityType<HighOrcEntity>> HIGH_ORC = register(
      "high_orc",
      Builder.<HighOrcEntity>of(HighOrcEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(HighOrcEntity::new)
         .sized(0.9F, 4.0F)
   );
   public static final RegistryObject<EntityType<ShadowGreenOrcEntity>> SHADOW_GREEN_ORC = register(
      "shadow_green_orc",
      Builder.<ShadowGreenOrcEntity>of(ShadowGreenOrcEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ShadowGreenOrcEntity::new)
         .sized(0.9F, 4.0F)
   );
   public static final RegistryObject<EntityType<ShadowHighOrcEntity>> SHADOW_HIGH_ORC = register(
      "shadow_high_orc",
      Builder.<ShadowHighOrcEntity>of(ShadowHighOrcEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(ShadowHighOrcEntity::new)
         .sized(0.9F, 4.0F)
   );
   public static final RegistryObject<EntityType<TuskShadowEntity>> TUSK_SHADOW = register(
      "tusk_shadow",
      Builder.<TuskShadowEntity>of(TuskShadowEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(TuskShadowEntity::new)
         .sized(0.8F, 2.8F)
   );
   public static final RegistryObject<EntityType<ShadowIronEntity>> SHADOW_IRON = register(
      "shadow_iron",
      Builder.<ShadowIronEntity>of(ShadowIronEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(ShadowIronEntity::new)
         .sized(0.9F, 3.6F)
   );
   public static final RegistryObject<EntityType<SkeletonSummonerEntity>> SKELETON_SUMMONER = register(
      "skeleton_summoner",
      Builder.<SkeletonSummonerEntity>of(SkeletonSummonerEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(SkeletonSummonerEntity::new)
         .sized(1.2F, 3.4F)
   );
   public static final RegistryObject<EntityType<MagicalSkullEntity>> MAGICAL_SKULL = register(
      "magical_skull",
      Builder.<MagicalSkullEntity>of(MagicalSkullEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(MagicalSkullEntity::new)
         .sized(0.4F, 0.4F)
   );
   public static final RegistryObject<EntityType<ManaArrowEntity>> MANA_ARROW = register(
      "projectile_mana_arrow",
      Builder.<ManaArrowEntity>of(ManaArrowEntity::new, MobCategory.MISC)
         .setCustomClientFactory(ManaArrowEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<HomingFlameArrowEntity>> HOMING_FLAME_ARROW = register(
      "projectile_homing_flame_arrow",
      Builder.<HomingFlameArrowEntity>of(HomingFlameArrowEntity::new, MobCategory.MISC)
         .setCustomClientFactory(HomingFlameArrowEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<RulersHandEntity>> RULERS_HAND = register(
      "projectile_rulers_hand",
      Builder.<RulersHandEntity>of(RulersHandEntity::new, MobCategory.MISC)
         .setCustomClientFactory(RulersHandEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<RulersAuthorityAuraEntity>> RULERS_AUTHORITY_AURA = register(
      "rulers_authority_aura",
      Builder.<RulersAuthorityAuraEntity>of(RulersAuthorityAuraEntity::new, MobCategory.MISC)
         .setCustomClientFactory(RulersAuthorityAuraEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(1)
         .sized(0.1F, 0.1F)
   );
   public static final RegistryObject<EntityType<DKCTowerAuraEntity>> DKC_TOWER_AURA = register(
      "dkc_tower_aura",
      Builder.<DKCTowerAuraEntity>of(DKCTowerAuraEntity::new, MobCategory.MISC)
         .setCustomClientFactory(DKCTowerAuraEntity::new)
         .setShouldReceiveVelocityUpdates(false)
         .setTrackingRange(32)
         .setUpdateInterval(20)
         .fireImmune()
         .sized(1.0F, 1.0F)
   );
   public static final RegistryObject<EntityType<SpiderWebEntity>> SPIDER_WEB = register(
      "projectile_spider_web",
      Builder.<SpiderWebEntity>of(SpiderWebEntity::new, MobCategory.MISC)
         .setCustomClientFactory(SpiderWebEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<ShadowStepEntity>> SHADOW_STEP = register(
      "projectile_shadow_step",
      Builder.<ShadowStepEntity>of(ShadowStepEntity::new, MobCategory.MISC)
         .setCustomClientFactory(ShadowStepEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<LightBallEntity>> LIGHT_BALL = register(
      "projectile_light_ball",
      Builder.<LightBallEntity>of(LightBallEntity::new, MobCategory.MISC)
         .setCustomClientFactory(LightBallEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<SlashEntity>> SLASH = register(
      "projectile_slash",
      Builder.<SlashEntity>of(SlashEntity::new, MobCategory.MISC)
         .setCustomClientFactory(SlashEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<Slash2Entity>> SLASH_2 = register(
      "projectile_slash_2",
      Builder.<Slash2Entity>of(Slash2Entity::new, MobCategory.MISC)
         .setCustomClientFactory(Slash2Entity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<Slash3Entity>> SLASH_3 = register(
      "projectile_slash_3",
      Builder.<Slash3Entity>of(Slash3Entity::new, MobCategory.MISC)
         .setCustomClientFactory(Slash3Entity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<Slash4Entity>> SLASH_4 = register(
      "projectile_slash_4",
      Builder.<Slash4Entity>of(Slash4Entity::new, MobCategory.MISC)
         .setCustomClientFactory(Slash4Entity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<Slash5Entity>> SLASH_5 = register(
      "projectile_slash_5",
      Builder.<Slash5Entity>of(Slash5Entity::new, MobCategory.MISC)
         .setCustomClientFactory(Slash5Entity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<Slash6Entity>> SLASH_6 = register(
      "projectile_slash_6",
      Builder.<Slash6Entity>of(Slash6Entity::new, MobCategory.MISC)
         .setCustomClientFactory(Slash6Entity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<SlashEffectEntity>> SLASH_EFFECT = register(
      "slash_effect",
      Builder.<SlashEffectEntity>of(SlashEffectEntity::new, MobCategory.MISC)
         .setCustomClientFactory(SlashEffectEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(2.8F, 1.6F)
   );
   public static final RegistryObject<EntityType<BasicAttackSlashEntity>> BASIC_ATTACK_SLASH = register(
      "basic_attack_slash",
      Builder.<BasicAttackSlashEntity>of(BasicAttackSlashEntity::new, MobCategory.MISC)
         .setCustomClientFactory(BasicAttackSlashEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(2.6F, 1.3F)
   );
   public static final RegistryObject<EntityType<GlacialPursuitEntity>> GLACIAL_PURSUIT = register(
      "glacial_pursuit",
      Builder.<GlacialPursuitEntity>of(GlacialPursuitEntity::new, MobCategory.MISC)
         .setCustomClientFactory(GlacialPursuitEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(192)
         .setUpdateInterval(1)
         .sized(1.35F, 0.35F)
   );
   public static final RegistryObject<EntityType<WhiteFlameVfxEntity>> WHITE_FLAME_VFX = register(
      "white_flame_vfx",
      Builder.<WhiteFlameVfxEntity>of(WhiteFlameVfxEntity::new, MobCategory.MISC)
         .setCustomClientFactory(WhiteFlameVfxEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(96)
         .setUpdateInterval(1)
         .sized(0.1F, 0.1F)
   );
   public static final RegistryObject<EntityType<FireMageVfxEntity>> FIRE_MAGE_VFX = register(
      "fire_mage_vfx",
      Builder.<FireMageVfxEntity>of(FireMageVfxEntity::new, MobCategory.MISC)
         .setCustomClientFactory(FireMageVfxEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(256)
         .setUpdateInterval(1)
         .sized(0.1F, 0.1F)
   );
   public static final RegistryObject<EntityType<BarrierVfxEntity>> BARRIER_VFX = register(
      "barrier_vfx",
      Builder.<BarrierVfxEntity>of(BarrierVfxEntity::new, MobCategory.MISC)
         .setCustomClientFactory(BarrierVfxEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(256)
         .setUpdateInterval(1)
         .sized(0.2F, 0.2F)
   );
   public static final RegistryObject<EntityType<ArcaneVfxEntity>> ARCANE_VFX = register(
      "arcane_vfx",
      Builder.<ArcaneVfxEntity>of(ArcaneVfxEntity::new, MobCategory.MISC)
         .setCustomClientFactory(ArcaneVfxEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(256)
         .setUpdateInterval(1)
         .sized(0.1F, 0.1F)
   );
   public static final RegistryObject<EntityType<RadiruBloodSpearEntity>> RADIRU_BLOOD_SPEAR = register(
      "radiru_blood_spear",
      Builder.<RadiruBloodSpearEntity>of(RadiruBloodSpearEntity::new, MobCategory.MISC)
         .setCustomClientFactory(RadiruBloodSpearEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(96)
         .setUpdateInterval(1)
         .sized(0.35F, 0.35F)
   );
   public static final RegistryObject<EntityType<LiuSwordVfxEntity>> LIU_SWORD_VFX = register(
      "liu_sword_vfx",
      Builder.<LiuSwordVfxEntity>of(LiuSwordVfxEntity::new, MobCategory.MISC)
         .setCustomClientFactory(LiuSwordVfxEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(1)
         .sized(0.1F, 0.1F)
   );
   public static final RegistryObject<EntityType<BeastVfxEntity>> BEAST_VFX = register(
      "beast_vfx",
      Builder.<BeastVfxEntity>of(BeastVfxEntity::new, MobCategory.MISC)
         .setCustomClientFactory(BeastVfxEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(1)
         .sized(0.1F, 0.1F)
   );
   public static final RegistryObject<EntityType<LiuSwordBeamEntity>> LIU_SWORD_BEAM = register(
      "liu_sword_beam",
      Builder.<LiuSwordBeamEntity>of(LiuSwordBeamEntity::new, MobCategory.MISC)
         .setCustomClientFactory(LiuSwordBeamEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(256)
         .setUpdateInterval(1)
         .sized(0.2F, 0.2F)
   );
   public static final RegistryObject<EntityType<DualWieldFlurryEntity>> DUAL_WIELD_FLURRY = register(
      "dual_wield_flurry",
      Builder.<DualWieldFlurryEntity>of(DualWieldFlurryEntity::new, MobCategory.MISC)
         .setCustomClientFactory(DualWieldFlurryEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(6.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<CrossStrikeEntity>> CROSS_STRIKE = register(
      "cross_strike",
      Builder.<CrossStrikeEntity>of(CrossStrikeEntity::new, MobCategory.MISC)
         .setCustomClientFactory(CrossStrikeEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(4.8F, 2.8F)
   );
   public static final RegistryObject<EntityType<QuickSlashesEntity>> QUICK_SLASHES = register(
      "quick_slashes",
      Builder.<QuickSlashesEntity>of(QuickSlashesEntity::new, MobCategory.MISC)
         .setCustomClientFactory(QuickSlashesEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(11.0F, 4.0F)
   );
   public static final RegistryObject<EntityType<SwordBeamProjectileEntity>> SWORD_BEAM_PROJECTILE = register(
      "projectile_sword_beam",
      Builder.<SwordBeamProjectileEntity>of(SwordBeamProjectileEntity::new, MobCategory.MISC)
         .setCustomClientFactory(SwordBeamProjectileEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(1.8F, 1.0F)
   );
   public static final RegistryObject<EntityType<DragonBreatheEntity>> DRAGON_BREATHE = register(
      "projectile_dragon_breathe",
      Builder.<DragonBreatheEntity>of(DragonBreatheEntity::new, MobCategory.MISC)
         .setCustomClientFactory(DragonBreatheEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<ManaBulletEntity>> MANA_BULLET = register(
      "projectile_mana_bullet",
      Builder.<ManaBulletEntity>of(ManaBulletEntity::new, MobCategory.MISC)
         .setCustomClientFactory(ManaBulletEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<DivineArrowEntity>> DIVINE_ARROW = register(
      "projectile_divine_arrow",
      Builder.<DivineArrowEntity>of(DivineArrowEntity::new, MobCategory.MISC)
         .setCustomClientFactory(DivineArrowEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<ShamanMagicEntity>> SHAMAN_MAGIC = register(
      "projectile_shaman_magic",
      Builder.<ShamanMagicEntity>of(ShamanMagicEntity::new, MobCategory.MISC)
         .setCustomClientFactory(ShamanMagicEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<WhiteFlameEntity>> WHITE_FLAME = register(
      "projectile_white_flame",
      Builder.<WhiteFlameEntity>of(WhiteFlameEntity::new, MobCategory.MISC)
         .setCustomClientFactory(WhiteFlameEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<DragonFireballEntity>> DRAGON_FIREBALL = register(
      "projectile_dragon_fireball",
      Builder.<DragonFireballEntity>of(DragonFireballEntity::new, MobCategory.MISC)
         .setCustomClientFactory(DragonFireballEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<RangerProjectileEntity>> RANGER_PROJECTILE = register(
      "projectile_ranger_projectile",
      Builder.<RangerProjectileEntity>of(RangerProjectileEntity::new, MobCategory.MISC)
         .setCustomClientFactory(RangerProjectileEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<MagicMissileEntity>> MAGIC_MISSILE = register(
      "projectile_magic_missile",
      Builder.<MagicMissileEntity>of(MagicMissileEntity::new, MobCategory.MISC)
         .setCustomClientFactory(MagicMissileEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<NecroBlastEntity>> NECRO_BLAST = register(
      "projectile_necro_blast",
      Builder.<NecroBlastEntity>of(NecroBlastEntity::new, MobCategory.MISC)
         .setCustomClientFactory(NecroBlastEntity::new)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(1)
         .sized(0.5F, 0.5F)
   );
   public static final RegistryObject<EntityType<DemonEntity>> DEMON = register(
      "demon",
      Builder.<DemonEntity>of(DemonEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DemonEntity::new)
         .fireImmune()
         .sized(0.6F, 2.5F)
   );
   public static final RegistryObject<EntityType<DemonKnightEntity>> DEMON_KNIGHT = register(
      "demon_knight",
      Builder.<DemonKnightEntity>of(DemonKnightEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(DemonKnightEntity::new)
         .fireImmune()
         .sized(0.7F, 2.6F)
   );
   public static final RegistryObject<EntityType<CerberusEntity>> CERBERUS = register(
      "cerberus",
      Builder.<CerberusEntity>of(CerberusEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(CerberusEntity::new)
         .fireImmune()
         .sized(3.0F, 3.5F)
   );
   public static final RegistryObject<EntityType<VulcanEntity>> VULCAN = register(
      "vulcan",
      Builder.<VulcanEntity>of(VulcanEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(64)
         .setUpdateInterval(3)
         .setCustomClientFactory(VulcanEntity::new)
         .fireImmune()
         .sized(2.0F, 5.0F)
   );
   public static final RegistryObject<EntityType<BaranEntity>> BARAN = register(
      "baran",
      Builder.<BaranEntity>of(BaranEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(BaranEntity::new)
         .fireImmune()
         .sized(1.0F, 3.0F)
   );
   public static final RegistryObject<EntityType<SilladBossEntity>> SILLAD_BOSS = register(
      "sillad_boss",
      Builder.<SilladBossEntity>of(SilladBossEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(SilladBossEntity::new)
         .sized(0.6F, 1.8F)
   );
   public static final RegistryObject<EntityType<KaiselinEntity>> KAISELIN = register(
      "kaiselin",
      Builder.<KaiselinEntity>of(KaiselinEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(KaiselinEntity::new)
         .fireImmune()
         .sized(3.25F, 2.6F)
   );
   public static final RegistryObject<EntityType<ShadowKaiselinEntity>> SHADOW_KAISELIN = register(
      "shadow_kaiselin",
      Builder.<ShadowKaiselinEntity>of(ShadowKaiselinEntity::new, MobCategory.MONSTER)
         .setShouldReceiveVelocityUpdates(true)
         .setTrackingRange(128)
         .setUpdateInterval(3)
         .setCustomClientFactory(ShadowKaiselinEntity::new)
         .fireImmune()
         .sized(3.25F, 2.6F)
   );

   private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, Builder<T> entityTypeBuilder) {
      return REGISTRY.register(registryname, () -> entityTypeBuilder.build(registryname));
   }

   @SubscribeEvent
   public static void init(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> {
         IgrisEntity.init();
         ShadowIgrisEntity.init();
         ShadowSold1Entity.init();
         SungJinWooEntity.init();
         OrcEntity.init();
         OrcShadowEntity.init();
         GemGolemEntity.init();
         AttackshardEntity.init();
         BeruBossEntity.init();
         BeruShadowEntity.init();
         CentipedeEntity.init();
         DKnight1Entity.init();
         DKnight2Entity.init();
         DKnight3Entity.init();
         KasakaEntity.init();
         MiniGemGolemEntity.init();
         SteelFangWolfEntity.init();
         SteelFangWolfShadowEntity.init();
         AncientSamuraiEntity.init();
         StoneGolemEntity.init();
         SpiderBossEntity.init();
         FireFlyEntity.init();
         PolarBearEntity.init();
         ShadowPolarBearEntity.init();
         IceElfEntity.init();
         BarukaEntity.init();
         ChoijongEntity.init();
         BaekYoonhoEntity.init();
         MagicEyeEntity.init();
         GoblinKingEntity.init();
         StatueOfGodEntity.init();
         KangTaeshikEntity.init();
         RedAntsEntity.init();
         ThomasAndreEntity.init();
         FangedKasakaEntity.init();
         FxPuddleEntity.init();
         FxspikEntity.init();
         StatueaxeEntity.init();
         StatuehammerEntity.init();
         StatueswordEntity.init();
         FuturisticGolemEntity.init();
         MutatedEntity.init();
         KamishShadowEntity.init();
         BloodRedComIgrisEntity.init();
         IgrisShadowEntity.init();
         AncientGolemEntity.init();
         HunterEntity.init();
         ChaHaeInEntity.init();
         KargalganEntity.init();
         SkeletonWarriorEntity.init();
         SkeletonBruteEntity.init();
         KamishEntity.init();
         SteelFangedLycanEntity.init();
         DummyPortalNormalEntity.init();
         DummyPortalRedEntity.init();
         DummyPortalPurpleEntity.init();
         PortalBeruEntity.init();
         PortalSEntity.init();
         RedGateEntity.init();
         PortalLushEntity.init();
         PortalKargalgansThroneRoomEntity.init();
         RandomCaveLargeEntity.init();
         PortalAncientGolemEntity.init();
         PortalEntity.init();
         Portal1Entity.init();
         SpawnerPortalEntity.init();
         Portal12Entity.init();
         PortalSewersEntity.init();
         PortalLabEntity.init();
         PortalJobChangeEntity.init();
         CartenonGateEntity.init();
         PortalCemeteryEntity.init();
         TrainingBotEntity.init();
         ShadowSoulEntity.init();
         FlagOfProtectionEntity.init();
         BellOfHealingEntity.init();
         BearTrapEntity.init();
         IcecleEntity.init();
         AfterImageEntity.init();
         AfterImage1Entity.init();
         AfterImage2Entity.init();
         SecretaryEntity.init();
         ElderBeastEntity.init();
         DetectEyeInvEntity.init();
         IceBallEntity.init();
         IceChunkEntity.init();
         DaggerSlashEntity.init();
         ArrowSplashEntity.init();
         GoblinClubEntity.init();
         GoblinArcherEntity.init();
         GoblinMageEntity.init();
         GoblinClubShadowEntity.init();
         GoblinArcherShadowEntity.init();
         GoblinMageShadowEntity.init();
         SlasheffectswordEntity.init();
         IgrisDeadBodyEntity.init();
         BeruDeadBodyEntity.init();
         CursedChainsEntity.init();
         DragonheadEntity.init();
         CurseMagicEntity.init();
         GreenOrcEntity.init();
         HighOrcEntity.init();
         ShadowGreenOrcEntity.init();
         ShadowHighOrcEntity.init();
         TuskShadowEntity.init();
         ShadowIronEntity.init();
         SkeletonSummonerEntity.init();
         MagicalSkullEntity.init();
         DemonEntity.init();
         DemonKnightEntity.init();
         CerberusEntity.init();
         VulcanEntity.init();
         BaranEntity.init();
         SilladBossEntity.init();
         KaiselinEntity.init();
         ShadowKaiselinEntity.init();
      });
   }

   @SubscribeEvent
   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put(IGRIS.get(), IgrisEntity.createAttributes().build());
      event.put(SHADOW_IGRIS.get(), ShadowIgrisEntity.createAttributes().build());
      event.put(SHADOW_SOLD_1.get(), ShadowSold1Entity.createAttributes().build());
      event.put(SUNG_JIN_WOO.get(), SungJinWooEntity.createAttributes().build());
      event.put(ORC.get(), OrcEntity.createAttributes().build());
      event.put(ORC_SHADOW.get(), OrcShadowEntity.createAttributes().build());
      event.put(GEM_GOLEM.get(), GemGolemEntity.createAttributes().build());
      event.put(ATTACKSHARD.get(), AttackshardEntity.createAttributes().build());
      event.put(BERU_BOSS.get(), BeruBossEntity.createAttributes().build());
      event.put(BERU_SHADOW.get(), BeruShadowEntity.createAttributes().build());
      event.put(CENTIPEDE.get(), CentipedeEntity.createAttributes().build());
      event.put(D_KNIGHT_1.get(), DKnight1Entity.createAttributes().build());
      event.put(D_KNIGHT_2.get(), DKnight2Entity.createAttributes().build());
      event.put(D_KNIGHT_3.get(), DKnight3Entity.createAttributes().build());
      event.put(KASAKA.get(), KasakaEntity.createAttributes().build());
      event.put(MINI_GEM_GOLEM.get(), MiniGemGolemEntity.createAttributes().build());
      event.put(STEEL_FANG_WOLF.get(), SteelFangWolfEntity.createAttributes().build());
      event.put(STEEL_FANG_WOLF_SHADOW.get(), SteelFangWolfShadowEntity.createAttributes().build());
      event.put(ANCIENT_SAMURAI.get(), AncientSamuraiEntity.createAttributes().build());
      event.put(STONE_GOLEM.get(), StoneGolemEntity.createAttributes().build());
      event.put(SPIDER_BOSS.get(), SpiderBossEntity.createAttributes().build());
      event.put(FIRE_FLY.get(), FireFlyEntity.createAttributes().build());
      event.put(POLAR_BEAR.get(), PolarBearEntity.createAttributes().build());
      event.put(SHADOW_POLAR_BEAR.get(), ShadowPolarBearEntity.createAttributes().build());
      event.put(ICE_ELF.get(), IceElfEntity.createAttributes().build());
      event.put(BARUKA.get(), BarukaEntity.createAttributes().build());
      event.put(CHOIJONG.get(), ChoijongEntity.createAttributes().build());
      event.put(BAEK_YOONHO.get(), BaekYoonhoEntity.createAttributes().build());
      event.put(MAGIC_EYE.get(), MagicEyeEntity.createAttributes().build());
      event.put(GOBLIN_KING.get(), GoblinKingEntity.createAttributes().build());
      event.put(STATUE_OF_GOD.get(), StatueOfGodEntity.createAttributes().build());
      event.put(KANG_TAESHIK.get(), KangTaeshikEntity.createAttributes().build());
      event.put(RED_ANTS.get(), RedAntsEntity.createAttributes().build());
      event.put(THOMAS_ANDRE.get(), ThomasAndreEntity.createAttributes().build());
      event.put(FANGED_KASAKA.get(), FangedKasakaEntity.createAttributes().build());
      event.put(FX_PUDDLE.get(), FxPuddleEntity.createAttributes().build());
      event.put(FXSPIK.get(), FxspikEntity.createAttributes().build());
      event.put(STATUEAXE.get(), StatueaxeEntity.createAttributes().build());
      event.put(STATUEHAMMER.get(), StatuehammerEntity.createAttributes().build());
      event.put(STATUESWORD.get(), StatueswordEntity.createAttributes().build());
      event.put(FUTURISTIC_GOLEM.get(), FuturisticGolemEntity.createAttributes().build());
      event.put(MUTATED.get(), MutatedEntity.createAttributes().build());
      event.put(KAMISH_SHADOW.get(), KamishShadowEntity.createAttributes().build());
      event.put(BLOOD_RED_COM_IGRIS.get(), BloodRedComIgrisEntity.createAttributes().build());
      event.put(IGRIS_SHADOW.get(), IgrisShadowEntity.createAttributes().build());
      event.put(ANCIENT_GOLEM.get(), AncientGolemEntity.createAttributes().build());
      event.put(HUNTER.get(), HunterEntity.createAttributes().build());
      event.put(CHA_HAE_IN.get(), ChaHaeInEntity.createAttributes().build());
      event.put(KARGALGAN.get(), KargalganEntity.createAttributes().build());
      event.put(SKELETON_WARRIOR.get(), SkeletonWarriorEntity.createAttributes().build());
      event.put(SKELETON_BRUTE.get(), SkeletonBruteEntity.createAttributes().build());
      event.put(KAMISH.get(), KamishEntity.createAttributes().build());
      event.put(STEEL_FANGED_LYCAN.get(), SteelFangedLycanEntity.createAttributes().build());
      event.put(DUMMY_PORTAL_NORMAL.get(), DummyPortalNormalEntity.createAttributes().build());
      event.put(DUMMY_PORTAL_RED.get(), DummyPortalRedEntity.createAttributes().build());
      event.put(DUMMY_PORTAL_PURPLE.get(), DummyPortalPurpleEntity.createAttributes().build());
      event.put(PORTAL_BERU.get(), PortalBeruEntity.createAttributes().build());
      event.put(PORTAL_S.get(), PortalSEntity.createAttributes().build());
      event.put(RED_GATE.get(), RedGateEntity.createAttributes().build());
      event.put(PORTAL_LUSH.get(), PortalLushEntity.createAttributes().build());
      event.put(PORTAL_KARGALGANS_THRONE_ROOM.get(), PortalKargalgansThroneRoomEntity.createAttributes().build());
      event.put(RANDOM_CAVE_LARGE.get(), RandomCaveLargeEntity.createAttributes().build());
      event.put(PORTAL_ANCIENT_GOLEM.get(), PortalAncientGolemEntity.createAttributes().build());
      event.put(PORTAL.get(), PortalEntity.createAttributes().build());
      event.put(PORTAL_1.get(), Portal1Entity.createAttributes().build());
      event.put(DATAPACK_GATE.get(), Portal1Entity.createAttributes().build());
      event.put(SPAWNER_PORTAL.get(), SpawnerPortalEntity.createAttributes().build());
      event.put(PORTAL_12.get(), Portal12Entity.createAttributes().build());
      event.put(PORTAL_SEWERS.get(), PortalSewersEntity.createAttributes().build());
      event.put(PORTAL_LAB.get(), PortalLabEntity.createAttributes().build());
      event.put(PORTAL_JOB_CHANGE.get(), PortalJobChangeEntity.createAttributes().build());
      event.put(CARTENON_GATE.get(), CartenonGateEntity.createAttributes().build());
      event.put(PORTAL_CEMETERY.get(), PortalCemeteryEntity.createAttributes().build());
      event.put(TRAINING_BOT.get(), TrainingBotEntity.createAttributes().build());
      event.put(SHADOW_SOUL.get(), ShadowSoulEntity.createAttributes().build());
      event.put(FLAG_OF_PROTECTION.get(), FlagOfProtectionEntity.createAttributes().build());
      event.put(BELL_OF_HEALING.get(), BellOfHealingEntity.createAttributes().build());
      event.put(BEAR_TRAP.get(), BearTrapEntity.createAttributes().build());
      event.put(ICECLE.get(), IcecleEntity.createAttributes().build());
      event.put(AFTER_IMAGE.get(), AfterImageEntity.createAttributes().build());
      event.put(AFTER_IMAGE_1.get(), AfterImage1Entity.createAttributes().build());
      event.put(AFTER_IMAGE_2.get(), AfterImage2Entity.createAttributes().build());
      event.put(SECRETARY.get(), SecretaryEntity.createAttributes().build());
      event.put(ESIL_RADIRU.get(), EsilRadiruEntity.createAttributes().build());
      event.put(ELDER_BEAST.get(), ElderBeastEntity.createAttributes().build());
      event.put(DETECT_EYE_INV.get(), DetectEyeInvEntity.createAttributes().build());
      event.put(ICE_BALL.get(), IceBallEntity.createAttributes().build());
      event.put(ICE_CHUNK.get(), IceChunkEntity.createAttributes().build());
      event.put(DAGGER_SLASH.get(), DaggerSlashEntity.createAttributes().build());
      event.put(ARROW_SPLASH.get(), ArrowSplashEntity.createAttributes().build());
      event.put(GOBLIN_CLUB.get(), GoblinClubEntity.createAttributes().build());
      event.put(GOBLIN_ARCHER.get(), GoblinArcherEntity.createAttributes().build());
      event.put(GOBLIN_MAGE.get(), GoblinMageEntity.createAttributes().build());
      event.put(GOBLIN_CLUB_SHADOW.get(), GoblinClubShadowEntity.createAttributes().build());
      event.put(GOBLIN_ARCHER_SHADOW.get(), GoblinArcherShadowEntity.createAttributes().build());
      event.put(GOBLIN_MAGE_SHADOW.get(), GoblinMageShadowEntity.createAttributes().build());
      event.put(SLASHEFFECTSWORD.get(), SlasheffectswordEntity.createAttributes().build());
      event.put(IGRIS_DEAD_BODY.get(), IgrisDeadBodyEntity.createAttributes().build());
      event.put(BERU_DEAD_BODY.get(), BeruDeadBodyEntity.createAttributes().build());
      event.put(CURSED_CHAINS.get(), CursedChainsEntity.createAttributes().build());
      event.put(DRAGONHEAD.get(), DragonheadEntity.createAttributes().build());
      event.put(CURSE_MAGIC.get(), CurseMagicEntity.createAttributes().build());
      event.put(GREEN_ORC.get(), GreenOrcEntity.createAttributes().build());
      event.put(HIGH_ORC.get(), HighOrcEntity.createAttributes().build());
      event.put(SHADOW_GREEN_ORC.get(), ShadowGreenOrcEntity.createAttributes().build());
      event.put(SHADOW_HIGH_ORC.get(), ShadowHighOrcEntity.createAttributes().build());
      event.put(TUSK_SHADOW.get(), TuskShadowEntity.createAttributes().build());
      event.put(SHADOW_IRON.get(), ShadowIronEntity.createAttributes().build());
      event.put(SKELETON_SUMMONER.get(), SkeletonSummonerEntity.createAttributes().build());
      event.put(MAGICAL_SKULL.get(), MagicalSkullEntity.createAttributes().build());
      event.put(DEMON.get(), DemonEntity.createAttributes().build());
      event.put(DEMON_KNIGHT.get(), DemonKnightEntity.createAttributes().build());
      event.put(CERBERUS.get(), CerberusEntity.createAttributes().build());
      event.put(VULCAN.get(), VulcanEntity.createAttributes().build());
      event.put(BARAN.get(), BaranEntity.createAttributes().build());
      event.put(SILLAD_BOSS.get(), SilladBossEntity.createAttributes().build());
      event.put(KAISELIN.get(), KaiselinEntity.createAttributes().build());
      event.put(SHADOW_KAISELIN.get(), ShadowKaiselinEntity.createAttributes().build());
   }
}
