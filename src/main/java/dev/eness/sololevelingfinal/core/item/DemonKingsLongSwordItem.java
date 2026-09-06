package dev.eness.sololevelingfinal.core.item;

import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class DemonKingsLongSwordItem extends SwordItem {
   public DemonKingsLongSwordItem() {
      super(new Tier() {
         @Override
         public int getUses() {
            return 0;
         }

         @Override
         public float getSpeed() {
            return 4.0F;
         }

         @Override
         public float getAttackDamageBonus() {
            return 10.0F;
         }

         @Override
         public int getLevel() {
            return 1;
         }

         @Override
         public int getEnchantmentValue() {
            return 2;
         }

         @Override
         public Ingredient getRepairIngredient() {
            return Ingredient.of();
         }
      }, 3, -2.8F, new Properties().fireResistant());
   }

   @Override
   public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
      boolean retval = super.hurtEnemy(itemstack, entity, sourceentity);
      stormOfTheFlames(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity, sourceentity, itemstack);
      return retval;
   }

   private static void stormOfTheFlames(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, ItemStack itemstack) {
      if (entity != null && sourceentity != null) {
         double limit = 0.0;
         double max = 0.0;
         if (sourceentity instanceof LivingEntity living
            && living.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get())
            && !(sourceentity instanceof Player player && !player.isCreative() && player.getCooldowns().isOnCooldown(itemstack.getItem()))) {
            if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).JOB
               == 4.0) {
               max = 6.0;
            } else {
               max = 3.0;
            }

            if (sourceentity instanceof Player player && !player.isCreative()) {
               player.getCooldowns().addCooldown(itemstack.getItem(), 60);
            }

            Vec3 center = new Vec3(x, y, z);

            for (Entity nearby : world.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(7.5), e -> true)
               .stream()
               .sorted(Comparator.comparingDouble(found -> found.distanceToSqr(center)))
               .toList()) {
               if (isValidStormTarget(sourceentity, nearby) && limit <= max) {
                  limit++;
                  if (world instanceof ServerLevel level) {
                     LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                     lightning.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(nearby.getX(), nearby.getY(), nearby.getZ())));
                     lightning.setVisualOnly(true);
                     level.addFreshEntity(lightning);
                  }

                  nearby.hurt(
                     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT), sourceentity),
                     (float)(5.0 + TemporaryStatBonusManager.effectiveIntelligence(sourceentity) / 15.0)
                  );
               }
            }
         }
      }
   }

   private static boolean isValidStormTarget(Entity sourceentity, Entity target) {
      if (sourceentity == target) {
         return false;
      } else if (target instanceof TamableAnimal tamable && sourceentity instanceof LivingEntity living && tamable.isOwnedBy(living)) {
         return false;
      } else {
         String sourceTeam = sourceentity instanceof LivingEntity living ? teamName(living) : "";
         String targetTeam = target instanceof LivingEntity living ? teamName(living) : "";
         return !sourceTeam.equals(targetTeam) || sourceTeam.equals("");
      }
   }

   private static String teamName(LivingEntity entity) {
      return entity.level().getScoreboard().getPlayersTeam(entity.getStringUUID()) == null
         ? ""
         : entity.level()
            .getScoreboard()
            .getPlayersTeam(entity instanceof Player player ? player.getGameProfile().getName() : entity.getStringUUID())
            .getName();
   }

   @Override
   public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(itemstack, world, list, flag);
      list.add(Component.literal("§6LEVEL OF DIFFICULTY: S"));
      list.add(Component.literal("§6ATTACK +350"));
      list.add(Component.literal("§6TYPE: LONGSWORD"));
      list.add(
         Component.literal(
            "§6A LONGSWORD CONTAINING THE POWERS OF BARAN, THE DEMON KING. THE EFFECT \"STORM OF THE FLAMES\" WILL ACTIVATE EVERY TIME THIS SWORD IS SWUNG."
         )
      );
      list.add(Component.literal("§6EFFECT \"STORM OF THE FLAMES\" : A VIOLENT THUNDERSTORM IS SUMMONED WITHIN A SPECIFIED AREA"));
      list.add(Component.literal("§6PASSIVE \"DEMONIC ATTUNEMENT\": WHILE HELD, THE SWORD DRAWS ON PERMANENT INTELLIGENCE TO STRENGTHEN THE WIELDER."));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean isFoil(ItemStack itemstack) {
      return Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(SololevelingModMobEffects.SWORD_ENHANCE.get());
   }
}
