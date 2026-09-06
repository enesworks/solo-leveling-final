package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModBlocks;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.InstanceDungeonKeyAccess;
import dev.eness.sololevelingfinal.core.util.PlayerEntryGenerationGuard;

public class InstanceDungeonKeyRightclickedOnBlockProcedure {
   private static final double PLAYER_PORTAL_ENTRY_X_OFFSET = 3.0;

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity instanceof ServerPlayer player) {
         if (world.getBlockState(BlockPos.containing(x, y, z)).getBlock() == SololevelingModBlocks.INSTANCE_DUNGEON_KEY_LOGGER.get()) {
            if (player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Player
               && InstanceDungeonKeyAccess.canEnter(player)) {
               long entryGeneration = PlayerEntryGenerationGuard.begin(player);
               InstanceDungeonKeyAccess.markClaimed(player);
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  if (capability.QuestProgression == 1.0 && "Getting Stronger".equals(capability.MainQuest)) {
                     capability.QuestProgression = 2.0;
                     capability.syncPlayerVariables(entity);
                  }
               });
               InstanceDungeonKeyAccess.consumePhysicalKey(player);
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.DunX = x;
                  capability.DunY = entity.getY();
                  capability.DunZ = z;
                  capability.instancecomplete = false;
                  capability.tpd = false;
                  capability.syncPlayerVariables(entity);
               });
               ResourceKey<Level> dungeonDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("sololeveling:dungeon_dimension_kasaka"));
               ServerLevel targetWorld = player.server.getLevel(dungeonDimension);
               if (targetWorld != null) {
                  int randX = Mth.nextInt(RandomSource.create(), -29999999, 29999999);
                  int randY = Mth.nextInt(RandomSource.create(), 60, 120);
                  int randZ = Mth.nextInt(RandomSource.create(), -29999999, 29999999);

                  while (!targetWorld.getBlockState(new BlockPos(randX, randY, randZ)).isAir() && randY < 200) {
                     randY++;
                  }

                  int safeRandY = randY;
                  entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                     capability.randplayerx = randX;
                     capability.randplayery = safeRandY;
                     capability.randplayerz = randZ;
                     capability.syncPlayerVariables(entity);
                  });
                  SololevelingMod.queueServerWork(
                     10,
                     () -> {
                        if (PlayerEntryGenerationGuard.isCurrent(player, entryGeneration)) {
                           if (player.level().dimension() != dungeonDimension) {
                              player.teleportTo(targetWorld, randX + 3.0, safeRandY, randZ, player.getYRot(), player.getXRot());
                           }

                           SololevelingMod.queueServerWork(
                              10,
                              () -> {
                                 if (PlayerEntryGenerationGuard.isCurrent(player, entryGeneration) && player.level().dimension() == dungeonDimension) {
                                    player.teleportTo(randX + 3.0, safeRandY, randZ);
                                    SololevelingMod.queueServerWork(
                                       10,
                                       () -> {
                                          if (PlayerEntryGenerationGuard.isCurrent(player, entryGeneration) && player.level().dimension() == dungeonDimension) {
                                             if (!targetWorld.isClientSide() && player.getServer() != null) {
                                                player.getServer()
                                                   .getCommands()
                                                   .performPrefixedCommand(
                                                      new CommandSourceStack(
                                                         CommandSource.NULL,
                                                         Vec3.atCenterOf(new BlockPos(randX, safeRandY, randZ)),
                                                         player.getRotationVector(),
                                                         targetWorld,
                                                         4,
                                                         player.getName().getString(),
                                                         player.getDisplayName(),
                                                         player.getServer(),
                                                         player
                                                      ),
                                                      "execute in sololeveling:dungeon_dimension_kasaka run spawninstance"
                                                   );
                                                SololevelingMod.queueServerWork(
                                                   30,
                                                   () -> {
                                                      if (PlayerEntryGenerationGuard.isCurrent(player, entryGeneration)
                                                         && player.level().dimension() == dungeonDimension) {
                                                         DunKasakaTeleportAndSpawnProcedure.execute(player.level(), player);
                                                      }
                                                   }
                                                );
                                             }
                                          }
                                       }
                                    );
                                 }
                              }
                           );
                        }
                     }
                  );
               }
            }
         }
      }
   }
}
