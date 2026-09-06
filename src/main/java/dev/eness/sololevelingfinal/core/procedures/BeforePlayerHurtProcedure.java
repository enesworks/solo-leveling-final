package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class BeforePlayerHurtProcedure {
   @SubscribeEvent
   public static void onEntityAttacked(LivingAttackEvent event) {
      Entity entity = event.getEntity();
      if (event != null && entity != null) {
         execute(event, entity.level(), event.getSource(), entity, event.getSource().getEntity(), event.getAmount());
      }
   }

   public static void execute(LevelAccessor world, DamageSource damagesource, Entity entity, Entity sourceentity, double amount) {
      execute(null, world, damagesource, entity, sourceentity, amount);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, DamageSource damagesource, Entity entity, Entity sourceentity, double amount) {
      if (damagesource != null && entity != null && sourceentity != null) {
         double dmg = 0.0;
         dmg = amount;
         if (sourceentity != null
            && !damagesource.is(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:magic_beast")))
            && entity instanceof Player
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Player) {
            boolean runtimeDungeonMob = sourceentity.getPersistentData().getBoolean("slr_dungeon_spawned");
            if ((!sourceentity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss"))) || runtimeDungeonMob)
               && (sourceentity.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("dm"))) || runtimeDungeonMob)) {
               int takerLevel = (int)entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .Level;
               int dealerLevel = (int)DungeonLevelHelper.levelOf(sourceentity);
               int levelDifference = takerLevel - dealerLevel;
               float reduction = (float)Math.max(0.0, Math.min(1.0, 0.01 * levelDifference));
               float finalDamage = (float)(dmg * (1.0F - reduction));
               if (event != null && event.isCancelable()) {
                  event.setCanceled(true);
               }

               if (finalDamage < 1.0F) {
                  entity.hurt(
                     new DamageSource(
                        world.registryAccess()
                           .registryOrThrow(Registries.DAMAGE_TYPE)
                           .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:magic_beast"))),
                        sourceentity
                     ),
                     1.0F
                  );
               } else {
                  entity.hurt(
                     new DamageSource(
                        world.registryAccess()
                           .registryOrThrow(Registries.DAMAGE_TYPE)
                           .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sololeveling:magic_beast"))),
                        sourceentity
                     ),
                     finalDamage
                  );
               }
            }
         }
      }
   }
}
