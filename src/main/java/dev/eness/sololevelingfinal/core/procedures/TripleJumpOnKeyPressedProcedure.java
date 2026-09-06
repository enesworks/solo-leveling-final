package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.init.SololevelingModParticleTypes;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.TemporaryStatBonusManager;

public class TripleJumpOnKeyPressedProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      execute(world, x, y, z, entity, 0.0, 0.0);
   }

   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, double clientMotionX, double clientMotionZ) {
      if (entity != null) {
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).tjonoff
            && TemporaryStatBonusManager.effectiveAgility(entity) >= 31.0
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).Level
               >= 25.0
            && (new Object() {
               public boolean checkGamemode(Entity _ent) {
                  return _ent instanceof ServerPlayer _serverPlayer ? _serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL : false;
               }
            }).checkGamemode(entity)
            && entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).tj < 2.0
            && !entity.onGround()) {
            double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                     .orElse(new SololevelingModVariables.PlayerVariables())
                  .tj
               + 1.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.tj = _setval;
               capability.syncPlayerVariables(entity);
            });
            Vec3 serverMotion = entity.getDeltaMovement();
            Vec3 horizontal = new Vec3(serverMotion.x(), 0.0, serverMotion.z());
            Vec3 clientHorizontal = new Vec3(clientMotionX, 0.0, clientMotionZ);
            if (clientHorizontal.lengthSqr() > horizontal.lengthSqr() && clientHorizontal.lengthSqr() < 16.0) {
               horizontal = clientHorizontal;
            }

            double oldSpeedSqr = horizontal.lengthSqr();
            Vec3 look = new Vec3(entity.getLookAngle().x, 0.0, entity.getLookAngle().z);
            if (look.lengthSqr() > 1.0E-4) {
               Vec3 boosted = horizontal.add(look.normalize().scale(0.25));
               if (boosted.lengthSqr() >= oldSpeedSqr) {
                  horizontal = boosted;
               }
            }

            Vec3 jumpMotion = new Vec3(horizontal.x(), 0.5, horizontal.z());
            entity.setDeltaMovement(jumpMotion);
            entity.hasImpulse = true;
            if (entity instanceof ServerPlayer _serverPlayer) {
               _serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(_serverPlayer));
            }

            entity.fallDistance = 0.0F;
            if (world instanceof Level _level && !_level.isClientSide()) {
               _level.playSound(
                  (Player)null,
                  BlockPos.containing(x, y, z),
                  ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.end_portal_frame.fill")),
                  SoundSource.NEUTRAL,
                  0.5F,
                  1.0F
               );
            }

            if (world instanceof ServerLevel _level) {
               _level.sendParticles(SololevelingModParticleTypes.MANA_BLUE.get(), x, y, z, 25, 0.5, 0.0, 0.5, 1.0);
            }
         }
      }
   }
}
