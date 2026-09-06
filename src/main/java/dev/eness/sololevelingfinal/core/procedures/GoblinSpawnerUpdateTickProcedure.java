package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;

public class GoblinSpawnerUpdateTickProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z) {
      if (world.getLevelData().getGameTime() % 20L == 0L) {
         Vec3 _center = new Vec3(x, y, z);

         for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10.0), e -> true)
            .stream()
            .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
            .toList()) {
            if (entityiterator instanceof Player
               && (new Object() {
                     public boolean checkGamemode(Entity _ent) {
                        if (_ent instanceof ServerPlayer _serverPlayer) {
                           return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL;
                        } else {
                           return _ent.level().isClientSide() && _ent instanceof Player _player
                              ? Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null
                                 && Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode() == GameType.SURVIVAL
                              : false;
                        }
                     }
                  })
                  .checkGamemode(entityiterator)) {
               world.setBlock(BlockPos.containing(x, y, z), Blocks.AIR.defaultBlockState(), 3);
               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.GOBLIN_CLUB.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                  }
               }

               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.GOBLIN_ARCHER.get().spawn(_level, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setYRot(world.getRandom().nextFloat() * 360.0F);
                  }
               }

               return;
            }
         }
      }
   }
}
