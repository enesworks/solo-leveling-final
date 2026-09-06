package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.JobChangeQuestManager;

public class JobChangeQuestEntryProcedure {
   private static final double PLAYER_PORTAL_ENTRY_X_OFFSET = 3.0;
   private static final ResourceKey<Level> IGRIS_DIMENSION = ResourceKey.create(
      Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_igris")
   );

   public static boolean execute(LevelAccessor world, Entity entity) {
      if (!(entity instanceof ServerPlayer player && world != null)) {
         return false;
      } else {
         if (!JobChangeQuestManager.isVisible(player)) {
            player.displayClientMessage(Component.literal("§5No active Job Change Quest."), true);
            return false;
         }

         if (JobChangeQuestManager.isSelectionPending(player)) {
            JobChangeQuestManager.requestSelectionScreen(player);
            return false;
         }

         if (JobChangeQuestManager.isShadowPresentation(player)) {
            player.displayClientMessage(Component.literal("§5Your Job assignment is still in progress."), true);
            return false;
         }

         if (player.level().dimension() == IGRIS_DIMENSION) {
            player.displayClientMessage(Component.literal("§5Job Change Quest is already active."), true);
            return false;
         }

         if (!JobChangeQuestManager.isOverworld(player)) {
            player.displayClientMessage(Component.literal("§cThe Job Change Quest can only be entered from the Minecraft Overworld."), true);
            return false;
         }

         boolean resume = JobChangeQuestManager.canResumeDungeon(player);
         if (!resume) {
            int retryTicks = JobChangeQuestManager.retryDelayTicks(player);
            if (retryTicks > 0) {
               player.displayClientMessage(Component.literal("§cYou can restart the Job Change Quest in " + (retryTicks + 19) / 20 + " seconds."), true);
               return false;
            }
         }

         ResourceKey<Level> destinationType = IGRIS_DIMENSION;
         ServerLevel nextLevel = player.server.getLevel(destinationType);
         if (nextLevel == null) {
            player.displayClientMessage(Component.literal("§cThe Job Change dungeon is unavailable."), true);
            return false;
         }

         if (!resume) {
            if (!JobChangeQuestManager.startDungeonRun(player)) {
               return false;
            }

            saveEntryState(player);
         }

         player.getPersistentData().putBoolean("slr_job_change_dungeon", true);
         player.setNoGravity(true);
         player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0F));
         player.teleportTo(nextLevel, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
         player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));

         for (MobEffectInstance effect : player.getActiveEffects()) {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effect));
         }

         player.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
         SololevelingMod.queueServerWork(
            70,
            () -> {
               if (player.isAlive() && player.level().dimension() == IGRIS_DIMENSION) {
                  SololevelingModVariables.PlayerVariables vars = player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables());
                  player.connection.teleport(vars.randplayerx + 3.0, vars.randplayery, vars.randplayerz, player.getYRot(), player.getXRot());
                  protectDuringDungeonLoad(player);
                  if (!resume && player.isAlive() && player.level().dimension() == IGRIS_DIMENSION) {
                     spawnIgrisDungeon(player);
                  }

                  SololevelingMod.queueServerWork(resume ? 10 : 35, () -> {
                     if (player.isAlive() && player.level().dimension() == IGRIS_DIMENSION) {
                        protectDuringDungeonLoad(player);
                        player.setNoGravity(false);
                     } else {
                        player.setNoGravity(false);
                     }

                     SololevelingMod.queueServerWork(10, () -> {
                        if (player.isAlive()) {
                           player.fallDistance = 0.0F;
                        }
                     });
                  });
                  SololevelingMod.queueServerWork(55, () -> {
                     if (!resume && player.isAlive() && player.level().dimension() == IGRIS_DIMENSION) {
                        spawnIgrisDungeon(player);
                     }
                  });
               } else {
                  player.setNoGravity(false);
               }
            }
         );
         return true;
      }
   }

   private static void protectDuringDungeonLoad(ServerPlayer player) {
      player.fallDistance = 0.0F;
      player.setDeltaMovement(0.0, 0.0, 0.0);
      player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 1, false, false));
      player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false));
   }

   private static void saveEntryState(ServerPlayer player) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
         capability.DunX = player.getX();
         capability.DunY = player.getY();
         capability.DunZ = player.getZ();
         capability.randplayerx = Mth.nextInt(RandomSource.create(), -29999999, 29999999);
         capability.randplayery = Mth.nextInt(RandomSource.create(), 60, 120);
         capability.randplayerz = Mth.nextInt(RandomSource.create(), -29999999, 29999999);
         capability.instancecomplete = false;
         capability.BossKilled = false;
         capability.tpd = false;
         capability.syncPlayerVariables(player);
      });
   }

   private static void spawnIgrisDungeon(ServerPlayer player) {
      DunPlaceIgrisProcedure.executeForAttempt(player);
   }
}
