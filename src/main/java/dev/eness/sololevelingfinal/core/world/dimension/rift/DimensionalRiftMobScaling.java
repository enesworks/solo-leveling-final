package dev.eness.sololevelingfinal.core.world.dimension.rift;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonLevelHelper;
import dev.eness.sololevelingfinal.core.dungeon.runtime.DungeonMobLevelAdapter;

@EventBusSubscriber(modid = "sololeveling")
public final class DimensionalRiftMobScaling {
   public static final String SCALED_TAG = "slr_rift_radial_scaled";
   public static final String SPAWN_DISTANCE_TAG = "slr_rift_spawn_distance";
   public static final String PROGRESSION_TIER_TAG = "slr_rift_progression_tier";
   private static final TagKey<EntityType<?>> SCALABLE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("sololeveling", "rift_scalable"));

   private DimensionalRiftMobScaling() {
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()
         && !event.loadedFromDisk()
         && DimensionalRiftDimension.LEVEL_KEY.equals(event.getLevel().dimension())
         && event.getEntity() instanceof Mob mob
         && mob.getType().is(SCALABLE)) {
         CompoundTag data = mob.getPersistentData();
         if (!data.getBoolean("slr_rift_radial_scaled") && !data.getBoolean("slr_dungeon_spawned") && !(DungeonLevelHelper.levelOf(mob) > 0.0)) {
            double distance = RiftGeometry.distance(mob.getX(), mob.getZ());
            int level = RiftGeometry.levelForDistance(distance);
            level = Math.max(5, Math.min(100, level + mob.getRandom().nextInt(7) - 3));
            DungeonMobLevelAdapter.ScalingResult result = DungeonMobLevelAdapter.applyGenericScaling(mob, level, DungeonMobLevelAdapter.MobRole.NORMAL);
            if (result.succeeded()) {
               data.putBoolean("slr_rift_radial_scaled", true);
               data.putDouble("slr_rift_spawn_distance", distance);
               data.putInt("slr_rift_progression_tier", Math.min(5, Math.max(1, 1 + (level - 1) / 20)));
            }
         }
      }
   }
}
