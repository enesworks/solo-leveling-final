package dev.eness.sololevelingfinal.core.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModItems;

@EventBusSubscriber(modid = "sololeveling")
public final class OrbOfAvariceManager {
   public static final int BLUE_FIRE_PRIMARY = 1527295;
   public static final int BLUE_FIRE_SECONDARY = 6940671;
   public static final double MANA_COST_MULTIPLIER = 1.5;
   private static final ResourceKey<DamageType> MAGE_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling", "mage"));

   private OrbOfAvariceManager() {
   }

   public static boolean isHeldBy(Entity entity) {
      return MageCombatHelper.resolveCaster(entity) instanceof Player player
         && (player.getMainHandItem().is(SololevelingModItems.ORB_OF_AVARICE.get()) || player.getOffhandItem().is(SololevelingModItems.ORB_OF_AVARICE.get()));
   }

   public static double manaCostMultiplier(Entity entity) {
      return isHeldBy(entity) ? 1.5 : 1.0;
   }

   public static int adjustManaCost(Entity entity, double baseCost) {
      return Math.max(0, (int)Math.ceil(Math.max(0.0, baseCost) * manaCostMultiplier(entity)));
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void amplifyMagicDamage(LivingHurtEvent event) {
      if (!event.getEntity().level().isClientSide() && !(event.getAmount() <= 0.0F) && isMagic(event.getSource())) {
         if (!ShadowEquipmentCombatHandler.isOrbAmplifiedTuskDamage(event.getSource())) {
            Entity caster = resolveDamageCaster(event.getSource());
            if (isHeldBy(caster)) {
               event.setAmount(event.getAmount() * 2.0F);
            }
         }
      }
   }

   private static boolean isMagic(DamageSource source) {
      return source.is(MAGE_DAMAGE) || source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
   }

   private static Entity resolveDamageCaster(DamageSource source) {
      Entity caster = source.getEntity();
      if (caster instanceof Projectile projectile && projectile.getOwner() != null) {
         caster = projectile.getOwner();
      }

      if (caster == null && source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() != null) {
         caster = projectile.getOwner();
      }

      return MageCombatHelper.resolveCaster(caster);
   }
}
