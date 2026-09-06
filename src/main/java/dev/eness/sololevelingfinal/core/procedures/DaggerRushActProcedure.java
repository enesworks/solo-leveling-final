package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class DaggerRushActProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!(world instanceof Level level && level.isClientSide())) {
            CooldownManager.discardIfRemainingExceeds(entity, "dagger_rush", 40);
            ItemStack mainHand = entity instanceof LivingEntity livingEntity ? livingEntity.getMainHandItem() : ItemStack.EMPTY;
            if (mainHand.is(ItemTags.create(new ResourceLocation("dagger")))) {
               runDaggerCombo(world, entity);
            } else if (mainHand.is(ItemTags.create(new ResourceLocation("nsword")))
               || mainHand.getItem() instanceof SwordItem
               || mainHand.getItem() instanceof AxeItem) {
               runSingleSlash(world, x, y, z, entity, 1, 100.0, 40, 4, 1);
            } else if (mainHand.isEmpty()) {
               runFistSlash(world, x, y, z, entity);
            } else {
               runFistSlash(world, x, y, z, entity);
            }
         }
      }
   }

   private static void runDaggerCombo(LevelAccessor world, Entity entity) {
      if (!hasMp(entity, 100.0)) {
         showStatus(entity, "Not enough MP!");
      } else if (CooldownManager.isOnCooldown(entity, "dagger_rush")) {
         showStatus(entity, "Ability on cooldown!");
      } else {
         shake(entity, 10, 1);
         spendMp(entity, 100.0);
         CooldownManager.set(entity, "dagger_rush", 40);
         entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
            capability.daggermelee = true;
            capability.daggermeleetimer = 0.0;
            capability.syncPlayerVariables(entity);
         });
      }
   }

   private static void runFistSlash(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (!hasMp(entity, 50.0)) {
         showStatus(entity, "Not enough MP!");
      } else if (CooldownManager.isOnCooldown(entity, "dagger_rush")) {
         showStatus(entity, "Ability on cooldown!");
      } else {
         shake(entity, 2, 1);
         if (entity instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide()) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 4, false, false));
         }

         spendMp(entity, 50.0);
         CooldownManager.set(entity, "dagger_rush", 20);
         int swingIndex = Mth.nextInt(RandomSource.create(), 1, 10) == 10 ? 1 : 0;
         BasicAttackSlashProcedure.execute(world, x, y, z, entity, 0, swingIndex);
      }
   }

   private static void runSingleSlash(
      LevelAccessor world, double x, double y, double z, Entity entity, int style, double mpCost, int cooldown, int shakeTicks, int shakePower
   ) {
      if (!hasMp(entity, mpCost)) {
         showStatus(entity, "Not enough MP!");
      } else if (CooldownManager.isOnCooldown(entity, "dagger_rush")) {
         showStatus(entity, "Ability on cooldown!");
      } else {
         shake(entity, shakeTicks, shakePower);
         spendMp(entity, mpCost);
         CooldownManager.set(entity, "dagger_rush", cooldown);
         BasicAttackSlashProcedure.execute(world, x, y, z, entity, style, Mth.nextInt(RandomSource.create(), 0, 2));
      }
   }

   private static boolean hasMp(Entity entity, double amount) {
      return entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
         >= amount;
   }

   private static void spendMp(Entity entity, double amount) {
      entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.MP -= amount;
         capability.syncPlayerVariables(entity);
      });
   }

   private static void shake(Entity entity, int ticks, int power) {
      if (entity instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide()) {
         livingEntity.addEffect(new MobEffectInstance(SololevelingModMobEffects.SCREEN_SHAKE.get(), ticks, power, false, false));
      }
   }

   private static void showStatus(Entity entity, String message) {
      if (entity instanceof Player player && !player.level().isClientSide()) {
         player.displayClientMessage(Component.literal(message), true);
      }
   }
}
