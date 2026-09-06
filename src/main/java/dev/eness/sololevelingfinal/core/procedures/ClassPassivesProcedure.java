package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class ClassPassivesProcedure {
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
      if (entity != null) {
         if (world.getLevelData().getGameTime() % 60L == 0L) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
               == 1.0) {
               if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(new ResourceLocation("dagger")))
                  && (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).is(ItemTags.create(new ResourceLocation("dagger")))) {
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
                  }

                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false));
                  }
               } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
                  .is(ItemTags.create(new ResourceLocation("dagger")))) {
                  if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                     _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
                  }
               } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY)
                     .is(ItemTags.create(new ResourceLocation("dagger")))
                  && entity instanceof LivingEntity _entity
                  && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
               == 2.0) {
               if ((
                     (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof PickaxeItem
                        || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof ShovelItem
                        || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof AxeItem
                        || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof HoeItem
                        || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof ShieldItem
                        || (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.SHEARS
                  )
                  && entity instanceof LivingEntity _entity
                  && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 1, false, false));
               }

               if ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SololevelingModItems.GRIAMORE.get()
                  && entity instanceof LivingEntity _entity
                  && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
               == 3.0) {
               if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(new ResourceLocation("nsword")))
                  && entity instanceof LivingEntity _entity
                  && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false));
               }

               if ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof ShieldItem
                  && entity instanceof LivingEntity _entity
                  && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, false));
               }
            } else if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Classes
                  == 5.0
               && (
                  (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof PickaxeItem
                     || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof ShovelItem
                     || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof AxeItem
                     || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof HoeItem
                     || (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() instanceof ShieldItem
                     || (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.SHEARS
               )
               && entity instanceof LivingEntity _entity
               && !_entity.level().isClientSide()) {
               _entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 1, false, false));
            }
         }
      }
   }
}
