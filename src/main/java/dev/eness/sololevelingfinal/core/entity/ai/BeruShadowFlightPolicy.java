package dev.eness.sololevelingfinal.core.entity.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.Tags.EntityTypes;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonMobLevelAdapter;

public final class BeruShadowFlightPolicy {
   private static final TagKey<EntityType<?>> FLIGHT_TARGETS = TagKey.create(
      Registries.ENTITY_TYPE, new ResourceLocation("sololeveling", "beru_flight_targets")
   );
   private static final TagKey<EntityType<?>> SOLO_BOSSES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("minecraft", "soloboss"));

   private BeruShadowFlightPolicy() {
   }

   public static boolean requiresSustainedFlight(LivingEntity target) {
      return target == null
         ? false
         : target.getType().is(FLIGHT_TARGETS) || target instanceof FlyingMob || target.isFallFlying() || target.isNoGravity() && !target.onGround();
   }

   public static boolean isBossTarget(LivingEntity target) {
      if (target == null) {
         return false;
      } else {
         return !target.getType().is(EntityTypes.BOSSES) && !target.getType().is(SOLO_BOSSES)
            ? DungeonMobLevelAdapter.MobRole.fromString(target.getPersistentData().getString("slr_dungeon_role")) == DungeonMobLevelAdapter.MobRole.BOSS
            : true;
      }
   }
}
