package dev.eness.sololevelingfinal.core.procedures;

import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.AttackshardEntity;

@EventBusSubscriber
public class ShardSpawnAnimationProcedure {
   @SubscribeEvent
   public static void onEntitySpawned(EntityJoinLevelEvent event) {
      execute(event, event.getLevel(), event.getEntity());
   }

   public static void execute(LevelAccessor world, Entity entity) {
      execute(null, world, entity);
   }

   private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
      if (entity != null) {
         if (entity instanceof AttackshardEntity) {
            if (entity instanceof AttackshardEntity) {
               ((AttackshardEntity)entity).setAnimation("spawn");
            }

            SololevelingMod.queueServerWork(
               50,
               () -> {
                  DamageSource _damageSource = new DamageSource(
                     world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity
                  );
                  if (_damageSource != null) {
                     entity.hurt(
                        new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC), entity), 100.0F
                     );
                  }
               }
            );
         }
      }
   }
}
