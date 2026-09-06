package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

@EventBusSubscriber
public class ManaRegenProcedure {
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         execute(event, event.player);
      }
   }

   public static void execute(Entity entity) {
      execute(null, entity);
   }

   private static void execute(@Nullable Event event, Entity entity) {
      if (entity != null) {
         SololevelingModVariables.PlayerVariables capability = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(null);
         if (capability != null) {
            double previousMp = capability.MP;
            boolean suppressRegen = false;
            if (entity instanceof LivingEntity living) {
               ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);
               ItemStack leggings = living.getItemBySlot(EquipmentSlot.LEGS);
               ItemStack chestplate = living.getItemBySlot(EquipmentSlot.CHEST);
               ItemStack helmet = living.getItemBySlot(EquipmentSlot.HEAD);
               suppressRegen = boots.is(SololevelingModItems.SHADOW_ARMOR_BOOTS.get())
                  || leggings.is(SololevelingModItems.SHADOW_ARMOR_LEGGINGS.get())
                  || chestplate.is(SololevelingModItems.SHADOW_ARMOR_CHESTPLATE.get())
                  || helmet.is(SololevelingModItems.SHADOW_ARMOR_HELMET.get())
                  || boots.is(SololevelingModItems.GOLIATH_ARMOR_BOOTS.get())
                  || leggings.is(SololevelingModItems.GOLIATH_ARMOR_LEGGINGS.get())
                  || chestplate.is(SololevelingModItems.GOLIATH_ARMOR_CHESTPLATE.get())
                  || helmet.is(SololevelingModItems.GOLIATH_ARMOR_HELMET.get());
            }

            if (capability.MP < capability.Mana && !suppressRegen && !CooldownManager.isOnCooldown(entity, "mana_refresh")) {
               capability.MP = capability.MP + (capability.manaregen + TemporaryStatBonusManager.effectiveIntelligence(entity) / 20.0 * 2.0);
            }

            if (capability.MP > capability.Mana) {
               capability.MP = capability.Mana;
            }

            if (capability.MP < 0.0) {
               capability.MP = 0.0;
            }

            if (Double.compare(capability.MP, previousMp) != 0) {
               capability.syncPlayerVariables(entity);
            }
         }
      }
   }
}
