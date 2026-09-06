package dev.eness.sololevelingfinal.core.init;

import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.entity.AfterImage1Entity;
import dev.eness.sololevelingfinal.core.entity.AfterImage2Entity;
import dev.eness.sololevelingfinal.core.entity.AfterImageEntity;
import dev.eness.sololevelingfinal.core.entity.AncientGolemEntity;
import dev.eness.sololevelingfinal.core.entity.AncientSamuraiEntity;
import dev.eness.sololevelingfinal.core.entity.ArrowSplashEntity;
import dev.eness.sololevelingfinal.core.entity.AttackshardEntity;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.BarukaEntity;
import dev.eness.sololevelingfinal.core.entity.BearTrapEntity;
import dev.eness.sololevelingfinal.core.entity.BellOfHealingEntity;
import dev.eness.sololevelingfinal.core.entity.BeruBossEntity;
import dev.eness.sololevelingfinal.core.entity.BeruDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.entity.BloodRedComIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.CentipedeEntity;
import dev.eness.sololevelingfinal.core.entity.CerberusEntity;
import dev.eness.sololevelingfinal.core.entity.ChaHaeInEntity;
import dev.eness.sololevelingfinal.core.entity.CursedChainsEntity;
import dev.eness.sololevelingfinal.core.entity.DaggerSlashEntity;
import dev.eness.sololevelingfinal.core.entity.DemonEntity;
import dev.eness.sololevelingfinal.core.entity.DragonheadEntity;
import dev.eness.sololevelingfinal.core.entity.DummyPortalNormalEntity;
import dev.eness.sololevelingfinal.core.entity.DummyPortalPurpleEntity;
import dev.eness.sololevelingfinal.core.entity.DummyPortalRedEntity;
import dev.eness.sololevelingfinal.core.entity.ElderBeastEntity;
import dev.eness.sololevelingfinal.core.entity.FangedKasakaEntity;
import dev.eness.sololevelingfinal.core.entity.FireFlyEntity;
import dev.eness.sololevelingfinal.core.entity.FlagOfProtectionEntity;
import dev.eness.sololevelingfinal.core.entity.FuturisticGolemEntity;
import dev.eness.sololevelingfinal.core.entity.FxPuddleEntity;
import dev.eness.sololevelingfinal.core.entity.FxspikEntity;
import dev.eness.sololevelingfinal.core.entity.GemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.HighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.IceBallEntity;
import dev.eness.sololevelingfinal.core.entity.IceChunkEntity;
import dev.eness.sololevelingfinal.core.entity.IceElfEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.KamishEntity;
import dev.eness.sololevelingfinal.core.entity.KamishShadowEntity;
import dev.eness.sololevelingfinal.core.entity.KargalganEntity;
import dev.eness.sololevelingfinal.core.entity.KasakaEntity;
import dev.eness.sololevelingfinal.core.entity.MagicEyeEntity;
import dev.eness.sololevelingfinal.core.entity.MagicalSkullEntity;
import dev.eness.sololevelingfinal.core.entity.MiniGemGolemEntity;
import dev.eness.sololevelingfinal.core.entity.MutatedEntity;
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
import dev.eness.sololevelingfinal.core.entity.RandomCaveLargeEntity;
import dev.eness.sololevelingfinal.core.entity.RedAntsEntity;
import dev.eness.sololevelingfinal.core.entity.RedGateEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowGreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowHighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowIgrisEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowPolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonBruteEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonSummonerEntity;
import dev.eness.sololevelingfinal.core.entity.SkeletonWarriorEntity;
import dev.eness.sololevelingfinal.core.entity.SlasheffectswordEntity;
import dev.eness.sololevelingfinal.core.entity.SpawnerPortalEntity;
import dev.eness.sololevelingfinal.core.entity.SpiderBossEntity;
import dev.eness.sololevelingfinal.core.entity.StatueOfGodEntity;
import dev.eness.sololevelingfinal.core.entity.StatueaxeEntity;
import dev.eness.sololevelingfinal.core.entity.StatuehammerEntity;
import dev.eness.sololevelingfinal.core.entity.StatueswordEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfShadowEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangedLycanEntity;
import dev.eness.sololevelingfinal.core.entity.StoneGolemEntity;
import dev.eness.sololevelingfinal.core.entity.ThomasAndreEntity;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.entity.VulcanEntity;

@EventBusSubscriber
public class EntityAnimationFactory {
   @SubscribeEvent
   public static void onEntityTick(LivingTickEvent event) {
      if (event != null && event.getEntity() != null) {
         if (event.getEntity() instanceof IgrisEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof ShadowIgrisEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof OrcEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof OrcShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GemGolemEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof AttackshardEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof BeruBossEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof BeruShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof CentipedeEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof KasakaEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof MiniGemGolemEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SteelFangWolfEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SteelFangWolfShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof AncientSamuraiEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof StoneGolemEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SpiderBossEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof FireFlyEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PolarBearEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof ShadowPolarBearEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof IceElfEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof BarukaEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof MagicEyeEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GoblinKingEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof StatueOfGodEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof RedAntsEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof ThomasAndreEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof FangedKasakaEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof FxPuddleEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof FxspikEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof StatueaxeEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof StatuehammerEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof StatueswordEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof FuturisticGolemEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof MutatedEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof KamishShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof BloodRedComIgrisEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof IgrisShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof AncientGolemEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof ChaHaeInEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof KargalganEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SkeletonWarriorEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SkeletonBruteEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof KamishEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SteelFangedLycanEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof DummyPortalNormalEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof DummyPortalRedEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof DummyPortalPurpleEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalBeruEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalSEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof RedGateEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalLushEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalKargalgansThroneRoomEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof RandomCaveLargeEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalAncientGolemEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof Portal1Entity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SpawnerPortalEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof Portal12Entity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalSewersEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalLabEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalJobChangeEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof PortalCemeteryEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof FlagOfProtectionEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof BellOfHealingEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof BearTrapEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof AfterImageEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof AfterImage1Entity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof AfterImage2Entity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof ElderBeastEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof IceBallEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof IceChunkEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof DaggerSlashEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof ArrowSplashEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GoblinClubEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GoblinArcherEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GoblinMageEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GoblinClubShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GoblinArcherShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GoblinMageShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SlasheffectswordEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof IgrisDeadBodyEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof BeruDeadBodyEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof CursedChainsEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof DragonheadEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof GreenOrcEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof HighOrcEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof ShadowGreenOrcEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof ShadowHighOrcEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof TuskShadowEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof SkeletonSummonerEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof MagicalSkullEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof DemonEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof CerberusEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof VulcanEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof BaranEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }

         if (event.getEntity() instanceof KaiselinEntity syncable) {
            String animation = syncable.getSyncedAnimation();
            if (!animation.equals("undefined")) {
               syncable.setAnimation("undefined");
               syncable.animationprocedure = animation;
            }
         }
      }
   }
}
