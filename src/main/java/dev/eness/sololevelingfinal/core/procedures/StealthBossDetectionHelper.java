package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.CerberusEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinKingEntity;
import dev.eness.sololevelingfinal.core.entity.VulcanEntity;

public class StealthBossDetectionHelper {
   private static final TagKey<EntityType<?>> SOLO_BOSS_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("soloboss"));

   public static boolean seesThroughStealth(Entity entity) {
      return entity != null && !(entity instanceof GoblinKingEntity)
         ? entity.getType().is(SOLO_BOSS_TAG) || entity instanceof CerberusEntity || entity instanceof VulcanEntity || entity instanceof BaranEntity
         : false;
   }
}
