package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class BaranSummonProcedure {
   public static final String BOSS_ADD_ROLE = "boss_add";
   private static final int MAX_ACTIVE_BOSS_ADDS = 8;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null && entity instanceof BaranEntity baran) {
         if (baran.getState().equals("summon")) {
            LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
            if (target == null) {
               resetToIdle(baran);
            } else {
               double MF = baran.getPersistentData().getDouble("MF");
               boolean phase2 = baran.getPersistentData().getBoolean("baran_phase2");
               int baranFloor = (int)baran.getPersistentData().getDouble("dkc_floor_number");
               String baranOwner = baran.getPersistentData().getString("dkc_spawned_by");
               if ((baranFloor <= 0 || baranOwner.isBlank())
                  && target instanceof ServerPlayer player
                  && DkcFloorRegistry.isDkc(player.level())
                  && DKCFloorDetectorProcedure.getCurrentFloor(player) == 20) {
                  baranFloor = 20;
                  baranOwner = player.getStringUUID();
                  baran.getPersistentData().putDouble("dkc_floor_number", baranFloor);
                  baran.getPersistentData().putString("dkc_spawned_by", baranOwner);
               }

               if (MF == 1.0) {
                  baran.animationprocedure = "attack";
                  if (world instanceof ServerLevel sl) {
                     sl.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 60, 1.5, 1.5, 1.5, 0.5);
                     sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.0, z, 20, 0.5, 0.5, 0.5, 0.02);
                     sl.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.wither.spawn")),
                        SoundSource.HOSTILE,
                        1.0F,
                        1.5F
                     );
                  }
               }

               if (MF == 20.0 && world instanceof ServerLevel sl) {
                  int available = Math.max(0, 8 - activeBossAdds(sl, baran, baranFloor, baranOwner));
                  int count = Math.min(phase2 ? 3 : 2, available);

                  for (int i = 0; i < count; i++) {
                     double angle = (double)i / count * Math.PI * 2.0;
                     double sx = x + Math.cos(angle) * 3.0;
                     double sz = z + Math.sin(angle) * 3.0;
                     spawnKnight(sl, baran, target, sx, y, sz, baranFloor, baranOwner);
                  }

                  sl.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 30, 2.0, 1.0, 2.0, 0.3);
               }

               if (phase2 && MF == 35.0 && world instanceof ServerLevel sl) {
                  if (activeBossAdds(sl, baran, baranFloor, baranOwner) >= 8) {
                     return;
                  }

                  double tx = target.getX();
                  double ty = target.getY();
                  double tz = target.getZ();
                  double backX = tx - target.getLookAngle().x * 2.0;
                  double backZ = tz - target.getLookAngle().z * 2.0;
                  spawnKnight(sl, baran, target, backX, ty, backZ, baranFloor, baranOwner);
                  sl.sendParticles(ParticleTypes.PORTAL, backX, ty + 1.0, backZ, 20, 0.5, 1.0, 0.5, 0.3);
                  sl.playSound(
                     (Player)null,
                     BlockPos.containing(backX, ty, backZ),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.enderman.teleport")),
                     SoundSource.HOSTILE,
                     1.0F,
                     0.7F
                  );
               }

               if (MF >= 55.0) {
                  resetToIdle(baran);
               }
            }
         }
      }
   }

   private static void spawnKnight(ServerLevel sl, BaranEntity baran, LivingEntity target, double sx, double sy, double sz, int floor, String owner) {
      DemonKnightEntity knight = SololevelingModEntities.DEMON_KNIGHT.get().spawn(sl, BlockPos.containing(sx, sy, sz), MobSpawnType.SPAWNER);
      if (knight != null) {
         knight.randomizeVariant();
         knight.getPersistentData().putDouble("dkc_floor_number", floor);
         knight.getPersistentData().putString("dkc_spawned_by", owner);
         knight.getPersistentData().putString("dkc_encounter_role", "boss_add");
         knight.setTarget(target);
      }
   }

   private static int activeBossAdds(ServerLevel level, BaranEntity baran, int floor, String owner) {
      AABB area = baran.getBoundingBox().inflate(128.0, 96.0, 128.0);
      return level.getEntitiesOfClass(
            DemonKnightEntity.class,
            area,
            knight -> {
               CompoundTag tag = knight.getPersistentData();
               return "boss_add".equals(tag.getString("dkc_encounter_role"))
                  && floor == (int)tag.getDouble("dkc_floor_number")
                  && owner.equals(tag.getString("dkc_spawned_by"));
            }
         )
         .size();
   }

   public static void discardBossAdds(ServerLevel level, ServerPlayer player, int floor) {
      String owner = player.getStringUUID();
      level.getEntitiesOfClass(
            DemonKnightEntity.class,
            DkcFloorBuilder.combatBounds(player, floor),
            knight -> {
               CompoundTag tag = knight.getPersistentData();
               return "boss_add".equals(tag.getString("dkc_encounter_role"))
                  && floor == (int)tag.getDouble("dkc_floor_number")
                  && owner.equals(tag.getString("dkc_spawned_by"));
            }
         )
         .forEach(Entity::discard);
   }

   private static void resetToIdle(BaranEntity baran) {
      baran.setState("idle");
      baran.getPersistentData().putDouble("MF", 0.0);
   }
}
