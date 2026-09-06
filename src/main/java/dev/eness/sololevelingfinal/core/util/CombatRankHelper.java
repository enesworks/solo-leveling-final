package dev.eness.sololevelingfinal.core.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import dev.eness.sololevelingfinal.core.entity.HunterEntity;

public final class CombatRankHelper {
   private static final TagKey<Biome> E_RANK_BIOME = biomeTag("dune");
   private static final TagKey<Biome> D_RANK_BIOME = biomeTag("dund");
   private static final TagKey<Biome> C_RANK_BIOME = biomeTag("dunc");
   private static final TagKey<Biome> B_RANK_BIOME = biomeTag("dunb");
   private static final TagKey<Biome> A_RANK_BIOME = biomeTag("duna");
   private static final TagKey<Biome> S_RANK_BIOME = biomeTag("duns");
   private static final TagKey<EntityType<?>> HIGH_TIER = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("hightier"));

   private CombatRankHelper() {
   }

   public static boolean isAtMost(Entity entity, int maximumRank) {
      return rankOf(entity) <= maximumRank;
   }

   public static int rankOf(Entity entity) {
      if (entity == null) {
         return 1;
      }

      int explicitRank = explicitRank(entity);
      if (explicitRank > 0) {
         return explicitRank;
      }

      if (entity instanceof HunterEntity hunter) {
         int hunterRank = parseRank(hunter.getEntityData().get(HunterEntity.DATA_Rank));
         if (hunterRank > 0) {
            return hunterRank;
         }
      }

      Holder<Biome> biome = entity.level().getBiome(entity.blockPosition());
      if (biome.is(E_RANK_BIOME)) {
         return 1;
      } else if (biome.is(D_RANK_BIOME)) {
         return 2;
      } else if (biome.is(C_RANK_BIOME)) {
         return 3;
      } else if (biome.is(B_RANK_BIOME)) {
         return 4;
      } else if (biome.is(A_RANK_BIOME)) {
         return 5;
      } else if (biome.is(S_RANK_BIOME)) {
         return 6;
      } else {
         int dimensionRank = dimensionRank(entity.level().dimension().location().getPath());
         if (dimensionRank > 0) {
            return dimensionRank;
         } else {
            ResourceLocation entityId = EntityType.getKey(entity.getType());
            int knownRank = entityId == null ? 0 : knownEntityRank(entityId.getPath());
            if (knownRank > 0) {
               return knownRank;
            } else if (entity.getType().is(HIGH_TIER)) {
               return 5;
            } else {
               double level = entity.getPersistentData().getDouble("Level");
               if (level <= 0.0) {
                  return 2;
               } else if (level <= 30.0) {
                  return 3;
               } else if (level <= 55.0) {
                  return 4;
               } else {
                  return level <= 80.0 ? 5 : 6;
               }
            }
         }
      }
   }

   private static int explicitRank(Entity entity) {
      int numeric = entity.getPersistentData().getInt("slr_entity_rank");
      if (numeric >= 1 && numeric <= 6) {
         return numeric;
      }

      int named = parseRank(entity.getPersistentData().getString("slr_entity_rank"));
      return named > 0 ? named : parseRank(entity.getPersistentData().getString("slr_procedural_rank"));
   }

   private static int dimensionRank(String path) {
      if (path != null && path.startsWith("monarch_territory_")) {
         return 4;
      }

      if (path != null && path.startsWith("dungeon_dimension_dkc")) {
         return 6;
      }

      return switch (path) {
         case "dungeon_dimension_d" -> 2;
         case "dungeon_dimension_c", "dungeon_dimension_kasaka" -> 3;
         case "dungeon_dimension_b", "dungeon_dimension_snow", "dungeon_dimension_igris" -> 4;
         case "dungeon_dimension_a" -> 5;
         case "dungeon_dimension_s", "dungeon_dimension_dkc", "cartenon_temple", "survival_dimension" -> 6;
         default -> 0;
      };
   }

   private static int knownEntityRank(String path) {
      return switch (path) {
         case "kasaka", "fanged_kasaka", "goblin_king", "spider_boss", "ancient_golem" -> 3;
         case "igris", "blood_red_com_igris", "baruka", "skeleton_summoner" -> 4;
         case "gem_golem", "futuristic_golem", "kargalgan" -> 5;
         case "beru_boss", "kamish", "cerberus", "vulcan", "baran", "kaiselin", "statue_of_god", "sillad_boss" -> 6;
         default -> 0;
      };
   }

   private static int parseRank(String rank) {
      if (rank == null) {
         return 0;
      }

      return switch (rank.trim().toUpperCase()) {
         case "E" -> 1;
         case "D" -> 2;
         case "C" -> 3;
         case "B" -> 4;
         case "A" -> 5;
         case "S" -> 6;
         default -> 0;
      };
   }

   private static TagKey<Biome> biomeTag(String id) {
      return TagKey.create(Registries.BIOME, new ResourceLocation(id));
   }
}
