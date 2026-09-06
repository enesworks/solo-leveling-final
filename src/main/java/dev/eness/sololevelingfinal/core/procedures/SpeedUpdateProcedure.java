package dev.eness.sololevelingfinal.core.procedures;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public class SpeedUpdateProcedure {
   private static final UUID AGILITY_SWIM_SPEED_MODIFIER_UUID = UUID.fromString("23b2331e-50aa-4cd5-9d32-059f0b8f7f43");
   private static final String AGILITY_SWIM_SPEED_MODIFIER_NAME = "Solo Leveling agility swim speed";
   private static final double SPRINT_AGILITY_BASELINE = 0.13;
   private static final double AGILITY_SPEED_PER_POINT = 5.0E-4;
   private static final double MAX_SWIM_SPEED_MULTIPLIER = 4.0;

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player.level(), event.player);
      }
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity instanceof LivingEntity living) {
         double effectiveAgility = TemporaryStatBonusManager.effectiveAgility(entity);
         SololevelingModVariables.PlayerVariables variables = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         living.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(variables.dash * 0.1);
         if (entity.isSprinting()) {
            living.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(variables.dash * (0.13 + 5.0E-4 * effectiveAgility * (variables.speedpercent / 100.0)));
         }

         updateAgilitySwimSpeed(living, effectiveAgility, variables.speedpercent);
         if (effectiveAgility < 30.0) {
            living.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).setBaseValue(0.3);
         } else if (effectiveAgility < 50.0) {
            living.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).setBaseValue(0.6);
         } else if (effectiveAgility < 100.0) {
            living.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).setBaseValue(1.2);
         } else {
            living.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).setBaseValue(1.6);
         }

         if (!entity.onGround() && effectiveAgility >= 70.0) {
            entity.fallDistance = 0.0F;
         }
      }
   }

   private static void updateAgilitySwimSpeed(LivingEntity living, double effectiveAgility, double speedPercent) {
      AttributeInstance swimSpeed = living.getAttribute(ForgeMod.SWIM_SPEED.get());
      if (swimSpeed != null) {
         double modifierAmount = agilitySwimSpeedMultiplier(effectiveAgility, speedPercent) - 1.0;
         AttributeModifier current = swimSpeed.getModifier(AGILITY_SWIM_SPEED_MODIFIER_UUID);
         if (modifierAmount <= 0.0) {
            if (current != null) {
               swimSpeed.removeModifier(AGILITY_SWIM_SPEED_MODIFIER_UUID);
            }
         } else if (current == null || !(Math.abs(current.getAmount() - modifierAmount) < 1.0E-6)) {
            if (current != null) {
               swimSpeed.removeModifier(AGILITY_SWIM_SPEED_MODIFIER_UUID);
            }

            swimSpeed.addTransientModifier(
               new AttributeModifier(AGILITY_SWIM_SPEED_MODIFIER_UUID, "Solo Leveling agility swim speed", modifierAmount, Operation.MULTIPLY_TOTAL)
            );
         }
      }
   }

   static double agilitySwimSpeedMultiplier(double effectiveAgility, double speedPercent) {
      double usedAgility = Math.max(0.0, effectiveAgility);
      double usage = Math.max(0.0, Math.min(100.0, speedPercent)) / 100.0;
      double sprintRelativeBonus = 5.0E-4 * usedAgility * usage / 0.13;
      return Math.min(4.0, 1.0 + sprintRelativeBonus);
   }
}
