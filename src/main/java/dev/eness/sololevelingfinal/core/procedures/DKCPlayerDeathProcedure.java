package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorBuilder;
import dev.eness.sololevelingfinal.core.dkc.DkcFloorRegistry;
import dev.eness.sololevelingfinal.core.dkc.DkcRadiruManager;
import dev.eness.sololevelingfinal.core.entity.BaranEntity;
import dev.eness.sololevelingfinal.core.entity.CerberusEntity;
import dev.eness.sololevelingfinal.core.entity.DemonEntity;
import dev.eness.sololevelingfinal.core.entity.DemonKnightEntity;
import dev.eness.sololevelingfinal.core.entity.KaiselinEntity;
import dev.eness.sololevelingfinal.core.entity.ShadowSoulEntity;
import dev.eness.sololevelingfinal.core.entity.VulcanEntity;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public class DKCPlayerDeathProcedure {
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onPlayerDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         if (DkcFloorRegistry.isDkc(player.level())) {
            int floor = DKCFloorDetectorProcedure.getCurrentFloor(player);
            if (floor > 0) {
               double cleared = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .dkc_cleared;
               if (!(cleared >= floor)) {
                  resetFloor((ServerLevel)player.level(), player, floor);
                  if (floor == 15) {
                     DkcRadiruManager.resetFailedEncounter((ServerLevel)player.level(), player);
                  }

                  player.sendSystemMessage(Component.literal("§c§lFloor " + floor + " Failed! §7Your progress on this floor has been reset."));
               }
            }
         }
      }
   }

   public static void resetFloor(ServerLevel level, Player player, int floor) {
      CompoundTag data = player.getPersistentData();
      String uuid = player.getStringUUID();
      AABB area = DkcFloorBuilder.combatBounds((ServerPlayer)player, floor);

      for (DemonEntity d : level.getEntitiesOfClass(DemonEntity.class, area)) {
         if (floor == (int)d.getPersistentData().getDouble("dkc_floor_number") && uuid.equals(d.getPersistentData().getString("dkc_spawned_by"))) {
            d.discard();
         }
      }

      for (DemonKnightEntity k : level.getEntitiesOfClass(DemonKnightEntity.class, area)) {
         if (floor == (int)k.getPersistentData().getDouble("dkc_floor_number") && uuid.equals(k.getPersistentData().getString("dkc_spawned_by"))) {
            k.discard();
         }
      }

      for (ShadowSoulEntity soul : level.getEntitiesOfClass(ShadowSoulEntity.class, area)) {
         if (floor == (int)soul.getPersistentData().getDouble("dkc_floor_number") && uuid.equals(soul.getPersistentData().getString("dkc_spawned_by"))) {
            soul.discard();
         }
      }

      switch (floor) {
         case 1:
            for (CerberusEntity boss : level.getEntitiesOfClass(CerberusEntity.class, area)) {
               if (uuid.equals(boss.getPersistentData().getString("dkc_spawned_by"))) {
                  boss.discard();
               }
            }
            break;
         case 10:
            for (VulcanEntity boss : level.getEntitiesOfClass(VulcanEntity.class, area)) {
               if (uuid.equals(boss.getPersistentData().getString("dkc_spawned_by"))) {
                  boss.discard();
               }
            }
            break;
         case 20:
            for (BaranEntity boss : level.getEntitiesOfClass(BaranEntity.class, area)) {
               if (uuid.equals(boss.getPersistentData().getString("dkc_spawned_by"))) {
                  boss.discard();
               }
            }

            for (KaiselinEntity boss : level.getEntitiesOfClass(KaiselinEntity.class, area)) {
               if (uuid.equals(boss.getPersistentData().getString("dkc_spawned_by"))) {
                  boss.discard();
               }
            }
      }

      DKCDemonSpawnerProcedure.invalidateAttempt(player, floor);
      String prefix = "dkc_floor_" + floor;
      data.remove(prefix + "_spawned");
      data.remove(prefix + "_initial_spawned");
      data.remove(prefix + "_spawning");
      data.remove(prefix + "_complete");
      data.remove(prefix + "_killed");
      data.remove(prefix + "_required");
      data.remove(prefix + "_demon_count");
      data.remove(prefix + "_knight_count");
      data.remove(prefix + "_miniboss_spawned");
      data.remove(prefix + "_spawn_retry_after");
      data.remove(prefix + "_kaiselin_defeated");
      data.remove(prefix + "_kaiselin_spawned");
      data.remove(prefix + "_baran_defeated");
      data.remove("dkc_floor_20_kaisel_soul_spawned");
      data.remove(prefix + "_enter_time");
      data.remove(prefix + "_boss_defeated");
   }
}
