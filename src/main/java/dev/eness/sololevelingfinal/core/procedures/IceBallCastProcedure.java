package dev.eness.sololevelingfinal.core.procedures;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.entity.IceBallEntity;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class IceBallCastProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!world.isClientSide() && !CooldownManager.isOnCooldown(entity, "job_1")) {
            if (entity.getPersistentData().getBoolean("UsingIceBall")) {
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("ability already in use"), true);
               }
            } else {
               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.ICE_BALL
                     .get()
                     .spawn(
                        _level,
                        BlockPos.containing(
                           entity.getX() + 1.0 * entity.getLookAngle().x,
                           entity.getY() + 1.6 + 1.0 * entity.getLookAngle().y,
                           entity.getZ() + 1.0 * entity.getLookAngle().z
                        ),
                        MobSpawnType.MOB_SUMMONED
                     );
                  if (entityToSpawn != null) {
                     entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                  }
               }

               entity.getPersistentData().putBoolean("UsingIceBall", true);
               if (entity instanceof Player _player && !_player.level().isClientSide()) {
                  _player.displayClientMessage(Component.literal("§3IceBall"), true);
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.hurt_freeze")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.hurt_freeze")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F,
                        false
                     );
                  }
               }

               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        (Player)null,
                        BlockPos.containing(x, y, z),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.glass.break")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                     );
                  }
               }

               if (!world.getEntitiesOfClass(IceBallEntity.class, AABB.ofSize(new Vec3(x, y, z), 40.0, 40.0, 40.0), e -> true).isEmpty()
                  && world.getEntitiesOfClass(IceBallEntity.class, AABB.ofSize(new Vec3(x, y, z), 40.0, 40.0, 40.0), e -> true).stream().sorted((new Object() {
                     Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                        return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                     }
                  }).compareDistOf(x, y, z)).findFirst().orElse(null).getPersistentData().getString("caster").equals("")) {
                  world.getEntitiesOfClass(IceBallEntity.class, AABB.ofSize(new Vec3(x, y, z), 40.0, 40.0, 40.0), e -> true).stream().sorted((new Object() {
                     Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
                        return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
                     }
                  }).compareDistOf(x, y, z)).findFirst().orElse(null).getPersistentData().putString("caster", entity.getDisplayName().getString());
               }
            }
         }
      }
   }
}
