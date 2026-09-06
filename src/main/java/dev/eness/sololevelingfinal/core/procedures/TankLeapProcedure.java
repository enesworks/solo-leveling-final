package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;

public class TankLeapProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).leapjump) {
            CooldownManager.set(entity, "Tank Leap", 240);
            entity.setDeltaMovement(
               new Vec3(entity.getDeltaMovement().x() + entity.getLookAngle().x * 2.0, 1.0, entity.getDeltaMovement().z() + entity.getLookAngle().z * 2.0)
            );
            if (world instanceof Level _level) {
               if (!_level.isClientSide()) {
                  _level.playSound(
                     (Player)null,
                     BlockPos.containing(x, y, z),
                     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.anvil.place")),
                     SoundSource.NEUTRAL,
                     1.0F,
                     1.0F
                  );
               } else {
                  _level.playLocalSound(
                     x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.anvil.place")), SoundSource.NEUTRAL, 1.0F, 1.0F, false
                  );
               }
            }

            double _setval = 2.0;
            entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
               capability.tj = _setval;
               capability.syncPlayerVariables(entity);
            });
            SololevelingMod.queueServerWork(5, () -> {
               boolean _setvalx = true;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.leapjump = _setvalx;
                  capability.syncPlayerVariables(entity);
               });
            });
         }
      }
   }
}
