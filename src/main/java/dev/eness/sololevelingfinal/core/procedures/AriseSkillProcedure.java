package dev.eness.sololevelingfinal.core.procedures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.entity.BeruDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.BeruShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinArcherShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinClubShadowEntity;
import dev.eness.sololevelingfinal.core.entity.GoblinMageShadowEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisDeadBodyEntity;
import dev.eness.sololevelingfinal.core.entity.IgrisShadowEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowGreenOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowHighOrcEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowKaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowPolarBearEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowSold1Entity;
import dev.eness.sololevelingfinal.core.entity.ShadowSoulEntity;
import dev.eness.sololevelingfinal.core.entity.SteelFangWolfShadowEntity;
import dev.eness.sololevelingfinal.core.entity.TuskShadowEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.init.SololevelingModSounds;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.AriseExtractionRules;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.ShadowMonarchManager;
import dev.eness.sololevelingfinal.core.util.SystemNotifications;

public class AriseSkillProcedure {
   private static final double RANGE = 18.0;
   private static final int MANA_PER_SOUL = 500;
   private static final int COOLDOWN_TICKS = 40;
   private static final int ARISE_DELAY_TICKS = 12;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof Player player && world instanceof ServerLevel level) {
         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (vars.JOB == 1.0) {
            if (player.isShiftKeyDown()) {
               showExtractionScan(player, level, x, y, z, vars);
            } else if (CooldownManager.isOnCooldown(player, "arise")) {
               negativePopup(player, "ARISE UNAVAILABLE", "Skill is on cooldown.");
            } else {
               int freeStorage = Math.max(0, (int)Math.floor(vars.shadowstorage - vars.shadowstorageusage));
               if (freeStorage <= 0) {
                  negativePopup(player, "ARISE FAILED", "Shadow storage is full.");
               } else {
                  List<Entity> targets = findExtractableTargets(level, x, y, z, player);
                  if (targets.isEmpty()) {
                     negativePopup(player, "ARISE FAILED", "No extractable shadows nearby.");
                  } else {
                     targets.sort(
                        Comparator.comparingInt(AriseSkillProcedure::soulPriority).reversed().thenComparingDouble(target -> target.distanceToSqr(player))
                     );
                     int attempts = Math.min(freeStorage, targets.size());
                     int affordable = Math.min(attempts, (int)Math.floor(vars.MP / 500.0));
                     if (affordable <= 0) {
                        negativePopup(player, "ARISE FAILED", "Not enough mana.");
                     } else {
                        playAriseSound(level, player.blockPosition(), player.getX(), player.getY(), player.getZ());
                        CooldownManager.set(player, "arise", 52);
                        SololevelingMod.queueServerWork(12, () -> completeArise(player, x, y, z));
                     }
                  }
               }
            }
         }
      }
   }

   private static void showExtractionScan(Player player, ServerLevel level, double x, double y, double z, SololevelingModVariables.PlayerVariables vars) {
      if (player instanceof ServerPlayer serverPlayer) {
         boolean possible = !CooldownManager.isOnCooldown(player, "arise")
            && Math.max(0, (int)Math.floor(vars.shadowstorage - vars.shadowstorageusage)) > 0
            && vars.MP >= 500.0
            && findExtractableTargets(level, x, y, z, player).stream().anyMatch(target -> !isTargetOverwhelming(player, target));
         Component title = Component.literal("§6§lSystem");
         Component under = Component.literal(possible ? "§5[Shadow Extraction]\n §2is possible" : "§5[Shadow Extraction]\n §4is NOT possible");
         if (possible) {
            SystemNotifications.showTitleUnder(serverPlayer, -6595329, 80, title, under);
         } else {
            SystemNotifications.showNegativeTitleUnder(serverPlayer, -49859, 80, title, under);
         }
      }
   }

   private static void completeArise(Player player, double x, double y, double z) {
      if (player != null && player.isAlive() && player.level() instanceof ServerLevel level) {
         SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
            .orElse(new SololevelingModVariables.PlayerVariables());
         if (vars.JOB == 1.0) {
            int freeStorage = Math.max(0, (int)Math.floor(vars.shadowstorage - vars.shadowstorageusage));
            if (freeStorage <= 0) {
               negativePopup(player, "ARISE FAILED", "Shadow storage is full.");
            } else {
               List<Entity> targets = findExtractableTargets(level, x, y, z, player);
               if (targets.isEmpty()) {
                  negativePopup(player, "ARISE FAILED", "No extractable shadows nearby.");
               } else {
                  targets.sort(
                     Comparator.comparingInt(AriseSkillProcedure::soulPriority).reversed().thenComparingDouble(targetx -> targetx.distanceToSqr(player))
                  );
                  int attempts = Math.min(freeStorage, targets.size());
                  int affordable = Math.min(attempts, (int)Math.floor(vars.MP / 500.0));
                  if (affordable <= 0) {
                     negativePopup(player, "ARISE FAILED", "Not enough mana.");
                  } else {
                     int attempted = 0;
                     int revived = 0;
                     int overwhelming = 0;

                     for (Entity target : targets) {
                        if (attempted >= affordable) {
                           break;
                        }

                        AriseSkillProcedure.ExtractionResult result = reviveTarget(level, target, player);
                        if (result != AriseSkillProcedure.ExtractionResult.INVALID) {
                           attempted++;
                           if (result == AriseSkillProcedure.ExtractionResult.SUCCESS) {
                              revived++;
                           } else if (result == AriseSkillProcedure.ExtractionResult.TOO_STRONG) {
                              overwhelming++;
                           }
                        }
                     }

                     if (revived > 0) {
                        int count = revived;
                        player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                           capability.MP = Math.max(0.0, capability.MP - 500 * count);
                           capability.syncPlayerVariables(player);
                        });
                        player.displayClientMessage(Component.literal("§5ARISE x" + revived), true);
                     } else {
                        negativePopup(
                           player,
                           "ARISE FAILED",
                           attempted > 0 && overwhelming == attempted ? "The target is too strong to extract." : "The shadows resisted extraction."
                        );
                        CooldownManager.set(player, "arise", 10);
                     }
                  }
               }
            }
         }
      }
   }

   private static AriseSkillProcedure.ExtractionResult reviveTarget(ServerLevel level, Entity target, Player player) {
      String soulType = soulType(target);
      if (soulType != null && !soulType.isBlank()) {
         String shadowType = shadowType(soulType);
         if (shadowType.isEmpty() || alreadyOwnsUniqueBoss(player, shadowType)) {
            return AriseSkillProcedure.ExtractionResult.INVALID;
         }

         if (isTargetOverwhelming(player, target)) {
            recordFailedExtraction(target);
            return AriseSkillProcedure.ExtractionResult.TOO_STRONG;
         }

         double chance = successChance(player, target, soulType);
         if (level.random.nextDouble() >= chance) {
            recordFailedExtraction(target);
            return AriseSkillProcedure.ExtractionResult.RESISTED;
         }

         Vec3 pos = target.position();
         Entity summoned = createSummonedShadow(level, shadowType, pos);
         if (summoned == null) {
            return AriseSkillProcedure.ExtractionResult.INVALID;
         }

         incrementOwnedAndUsage(player, shadowType);
         level.sendParticles(SololevelingModParticleTypes.SHADOW_REVIVE.get(), pos.x, pos.y + 2.0, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
         spawnLightning(level, pos);
         if (summoned instanceof TamableAnimal tame) {
            tame.tame(player);
         }

         level.addFreshEntity(summoned);
         ShadowMonarchManager.tagExistingSummon(player, summoned, shadowType);
         target.discard();
         return AriseSkillProcedure.ExtractionResult.SUCCESS;
      } else {
         return AriseSkillProcedure.ExtractionResult.INVALID;
      }
   }

   private static List<Entity> findExtractableTargets(ServerLevel level, double x, double y, double z, Player player) {
      AABB area = new AABB(x - 18.0, y - 18.0, z - 18.0, x + 18.0, y + 18.0, z + 18.0);
      ArrayList<Entity> targets = new ArrayList<>();
      targets.addAll(level.getEntitiesOfClass(ShadowSoulEntity.class, area, target -> isExtractableTarget(player, target)));
      targets.addAll(level.getEntitiesOfClass(IgrisDeadBodyEntity.class, area, target -> isExtractableTarget(player, target)));
      targets.addAll(level.getEntitiesOfClass(BeruDeadBodyEntity.class, area, target -> isExtractableTarget(player, target)));
      return targets;
   }

   private static boolean isExtractableTarget(Player player, Entity target) {
      if (target != null && target.isAlive()) {
         String owner = target.getPersistentData().getString("dkc_spawned_by");
         if (!owner.isBlank() && !owner.equals(player.getStringUUID())) {
            return false;
         }

         String type = shadowType(soulType(target));
         return !type.isEmpty() && !alreadyOwnsUniqueBoss(player, type) && !AriseExtractionRules.failuresExhausted(failedExtractionCount(target));
      } else {
         return false;
      }
   }

   private static String soulType(Entity target) {
      if (target instanceof IgrisDeadBodyEntity) {
         return "igris";
      } else if (target instanceof BeruDeadBodyEntity) {
         return "beru";
      } else {
         return target instanceof ShadowSoulEntity ? target.getPersistentData().getString("soultype") : "";
      }
   }

   private static boolean alreadyOwnsUniqueBoss(Player player, String shadowType) {
      SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
         .orElse(new SololevelingModVariables.PlayerVariables());

      return switch (shadowType) {
         case "igris" -> vars.igris > 0.0;
         case "beru" -> vars.berumax > 0.0;
         case "tusk" -> vars.tuskmax > 0.0;
         case "kaisel" -> vars.Kaisel > 0.0;
         default -> false;
      };
   }

   private static void recordFailedExtraction(Entity target) {
      if (target instanceof IgrisDeadBodyEntity igris) {
         int failures = AriseExtractionRules.nextFailureCount(failedExtractionCount(igris));
         igris.getPersistentData().putInt("slr_arise_failures", failures);
         igris.getEntityData().set(IgrisDeadBodyEntity.DATA_arise, failures);
         if (AriseExtractionRules.failuresExhausted(failures)) {
            igris.discard();
         }
      } else if (target instanceof BeruDeadBodyEntity beru) {
         int remaining = Math.max(0, beru.getEntityData().get(BeruDeadBodyEntity.DATA_tries) - 1);
         beru.getEntityData().set(BeruDeadBodyEntity.DATA_tries, remaining);
         if (remaining <= 0) {
            beru.discard();
         }
      } else {
         int failures = AriseExtractionRules.nextFailureCount(failedExtractionCount(target));
         target.getPersistentData().putInt("slr_arise_failures", failures);
         target.getPersistentData().putDouble("ariset", failures);
         if (AriseExtractionRules.failuresExhausted(failures)) {
            target.discard();
         }
      }
   }

   private static int failedExtractionCount(Entity target) {
      if (target == null) {
         return 3;
      } else if (target.getPersistentData().contains("slr_arise_failures")) {
         return Math.max(0, target.getPersistentData().getInt("slr_arise_failures"));
      } else if (target instanceof IgrisDeadBodyEntity igris) {
         return Math.max(0, igris.getEntityData().get(IgrisDeadBodyEntity.DATA_arise) - 1);
      } else {
         return target instanceof BeruDeadBodyEntity beru
            ? Math.max(0, 3 - beru.getEntityData().get(BeruDeadBodyEntity.DATA_tries))
            : Math.max(0, (int)Math.floor(target.getPersistentData().getDouble("ariset")));
      }
   }

   private static Entity createSummonedShadow(ServerLevel level, String shadowType, Vec3 pos) {
      Entity entity = switch (shadowType) {
         case "knight" -> (ShadowSold1Entity)SololevelingModEntities.SHADOW_SOLD_1.get().create(level);
         case "goblin_club" -> (GoblinClubShadowEntity)SololevelingModEntities.GOBLIN_CLUB_SHADOW.get().create(level);
         case "goblin_archer" -> (GoblinArcherShadowEntity)SololevelingModEntities.GOBLIN_ARCHER_SHADOW.get().create(level);
         case "goblin_mage" -> (GoblinMageShadowEntity)SololevelingModEntities.GOBLIN_MAGE_SHADOW.get().create(level);
         case "wolf" -> (SteelFangWolfShadowEntity)SololevelingModEntities.STEEL_FANG_WOLF_SHADOW.get().create(level);
         case "orc" -> (ShadowGreenOrcEntity)SololevelingModEntities.SHADOW_GREEN_ORC.get().create(level);
         case "polar_bear" -> (ShadowPolarBearEntity)SololevelingModEntities.SHADOW_POLAR_BEAR.get().create(level);
         case "high_orc" -> (ShadowHighOrcEntity)SololevelingModEntities.SHADOW_HIGH_ORC.get().create(level);
         case "igris" -> (IgrisShadowEntity)SololevelingModEntities.IGRIS_SHADOW.get().create(level);
         case "beru" -> (BeruShadowEntity)SololevelingModEntities.BERU_SHADOW.get().create(level);
         case "tusk" -> (TuskShadowEntity)SololevelingModEntities.TUSK_SHADOW.get().create(level);
         case "kaisel" -> (ShadowKaiselinEntity)SololevelingModEntities.SHADOW_KAISELIN.get().create(level);
         default -> null;
      };
      if (entity == null) {
         return null;
      }

      entity.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360.0F, 0.0F);
      if (entity instanceof Mob mob) {
         mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
      }

      return entity;
   }

   private static void incrementOwnedAndUsage(Player player, String shadowType) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.shadowstorageusage++;
         switch (shadowType) {
            case "knight":
               capability.ordshadowmax++;
               capability.OrdShadow++;
               break;
            case "goblin_club":
               capability.GobShadowMax++;
               capability.GobShadow++;
               break;
            case "goblin_archer":
               capability.ShadowGoblinArcherMax++;
               capability.ShadowGoblinArcherAmount++;
               break;
            case "goblin_mage":
               capability.ShadowGoblinMageMax++;
               capability.ShadowGoblinMageAmount++;
               break;
            case "wolf":
               capability.WolfShadowMax++;
               capability.WolfShadow++;
               break;
            case "orc":
               capability.orcmax++;
               capability.orcspawned++;
               capability.summonlimitusage++;
               break;
            case "polar_bear":
               capability.polarbearmax++;
               capability.polarbear++;
               capability.summonlimitusage++;
               break;
            case "high_orc":
               capability.highorcmax++;
               capability.highorcspawned++;
               capability.summonlimitusage++;
               break;
            case "igris":
               capability.igris = Math.max(1.0, capability.igris);
               capability.IgrisSpawned = Math.max(1.0, capability.IgrisSpawned);
               break;
            case "beru":
               capability.berumax = Math.max(1.0, capability.berumax);
               capability.beru = Math.max(1.0, capability.beru);
               break;
            case "tusk":
               capability.tuskmax = Math.max(1.0, capability.tuskmax);
               capability.tuskspawned = Math.max(1.0, capability.tuskspawned);
               break;
            case "kaisel":
               capability.Kaisel = Math.max(1.0, capability.Kaisel);
               capability.KaiselSpawned = Math.max(1.0, capability.KaiselSpawned);
         }

         capability.syncPlayerVariables(player);
      });
   }

   private static double successChance(Player player, Entity target, String soulType) {
      double level = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level;
      return AriseExtractionRules.successChance(level, targetLevel(target, soulType), player.getAbilities().instabuild);
   }

   private static boolean isTargetOverwhelming(Player player, Entity target) {
      if (player != null && target != null) {
         double playerLevel = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
               .orElse(new SololevelingModVariables.PlayerVariables())
            .Level;
         return AriseExtractionRules.isOverwhelming(playerLevel, targetLevel(target, soulType(target)), player.getAbilities().instabuild);
      } else {
         return true;
      }
   }

   private static double targetLevel(Entity target, String soulType) {
      double stored = target == null ? 0.0 : target.getPersistentData().getDouble("slr_arise_target_level");
      return AriseExtractionRules.effectiveTargetLevel(soulType, stored);
   }

   private static String shadowType(String soulType) {
      return switch (normalizeSoulType(soulType)) {
         case "soldier" -> "knight";
         case "goblin" -> "goblin_club";
         case "goblinarc" -> "goblin_archer";
         case "goblinmage" -> "goblin_mage";
         case "wolf" -> "wolf";
         case "orc" -> "orc";
         case "bear" -> "polar_bear";
         case "highorc" -> "high_orc";
         case "igris" -> "igris";
         case "beru" -> "beru";
         case "tusk" -> "tusk";
         case "kaisel" -> "kaisel";
         default -> "";
      };
   }

   private static int soulPriority(Entity target) {
      return switch (normalizeSoulType(soulType(target))) {
         case "beru" -> 1200;
         case "kaisel" -> 1100;
         case "igris" -> 1000;
         case "tusk" -> 900;
         case "highorc" -> 700;
         case "bear" -> 550;
         case "orc" -> 500;
         case "soldier" -> 300;
         case "wolf" -> 250;
         case "goblinmage" -> 220;
         case "goblinarc" -> 210;
         case "goblin" -> 200;
         default -> 0;
      };
   }

   private static String normalizeSoulType(String soulType) {
      return soulType == null ? "" : soulType.trim().toLowerCase(Locale.ROOT).replace("_", "");
   }

   private static void spawnLightning(ServerLevel level, Vec3 pos) {
      LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
      if (lightning != null) {
         lightning.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(pos.x, pos.y - 1.0, pos.z)));
         lightning.setVisualOnly(true);
         level.addFreshEntity(lightning);
      }
   }

   private static void playAriseSound(Level level, BlockPos pos, double x, double y, double z) {
      if (!level.isClientSide()) {
         level.playSound((Player)null, pos, SololevelingModSounds.ARISE.get(), SoundSource.NEUTRAL, 1.1F, 0.85F);
      } else {
         level.playLocalSound(x, y, z, SololevelingModSounds.ARISE.get(), SoundSource.NEUTRAL, 1.1F, 0.85F, false);
      }
   }

   private static void negativePopup(Player player, String title, String undertext) {
      if (player instanceof ServerPlayer serverPlayer) {
         SystemNotifications.showNegativeTitleUnder(serverPlayer, -49859, 80, Component.literal("§4§l" + title), Component.literal("§c" + undertext));
      }
   }

   private enum ExtractionResult {
      SUCCESS,
      RESISTED,
      TOO_STRONG,
      INVALID;
   }
}
