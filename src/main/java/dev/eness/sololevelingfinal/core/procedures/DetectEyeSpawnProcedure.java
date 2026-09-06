package dev.eness.sololevelingfinal.core.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.init.SololevelingModEntities;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.OrbOfAvariceManager;

public class DetectEyeSpawnProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         int manaCost = OrbOfAvariceManager.adjustManaCost(entity, 200.0);
         if (entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables()).MP
            >= manaCost) {
            if (!CooldownManager.isOnCooldown(entity, "Detection")) {
               double _setval = entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new SololevelingModVariables.PlayerVariables())
                     .MP
                  - manaCost;
               entity.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                  capability.MP = _setval;
                  capability.syncPlayerVariables(entity);
               });
               CooldownManager.set(entity, "mana_refresh", 50);
               CooldownManager.set(entity, "Detection", 250);
               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 170, 1, false, false));
               }

               if (world instanceof ServerLevel _level) {
                  Entity entityToSpawn = SololevelingModEntities.DETECT_EYE_INV
                     .get()
                     .spawn(_level, BlockPos.containing(x, y + 3.0, z), MobSpawnType.MOB_SUMMONED);
                  if (entityToSpawn != null) {
                     entityToSpawn.setDeltaMovement(0.0, 0.0, 0.0);
                     if (entity instanceof ServerPlayer viewer) {
                        entityToSpawn.getPersistentData().putString("slr_highlight_viewer", viewer.getStringUUID());
                     }
                  }
               }
            }
         } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(Component.literal("Not enough MP!"), true);
         }
      }
   }
}
