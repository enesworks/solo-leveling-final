package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class BossStealthTargetingProcedure {
   @SubscribeEvent
   public static void onEntityTick(LivingTickEvent event) {
      if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof Mob boss && StealthBossDetectionHelper.seesThroughStealth(boss)) {
         if (boss.tickCount % 10 == 0 && !boss.isNoAi() && boss.isAlive()) {
            LivingEntity currentTarget = boss.getTarget();
            if (!isValidStealthedTarget(boss, currentTarget)) {
               Player target = boss.level()
                  .getNearestPlayer(
                     boss.getX(),
                     boss.getY(),
                     boss.getZ(),
                     followRange(boss),
                     entity -> entity instanceof Player player && isValidStealthedTarget(boss, player)
                  );
               if (target != null) {
                  boss.setTarget(target);
               }
            }
         }
      }
   }

   private static boolean isValidStealthedTarget(Mob boss, LivingEntity target) {
      return target instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()
         ? player.hasEffect(MobEffects.INVISIBILITY) && boss.hasLineOfSight(player)
         : false;
   }

   private static double followRange(Mob boss) {
      return boss.getAttribute(Attributes.FOLLOW_RANGE) != null ? Math.max(16.0, boss.getAttributeValue(Attributes.FOLLOW_RANGE)) : 48.0;
   }
}
