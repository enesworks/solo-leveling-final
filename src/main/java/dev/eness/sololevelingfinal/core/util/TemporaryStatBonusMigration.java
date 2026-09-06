package dev.eness.sololevelingfinal.core.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.init.SololevelingModMobEffects;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

@EventBusSubscriber
public final class TemporaryStatBonusMigration {
   static final String MIGRATION_RECEIPT = "slr_temporary_stat_bonus_model_v1";

   private TemporaryStatBonusMigration() {
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         migrate(player);
      }
   }

   @SubscribeEvent
   public static void onClone(Clone event) {
      if (event.getOriginal().getPersistentData().getBoolean("slr_temporary_stat_bonus_model_v1")) {
         event.getEntity().getPersistentData().putBoolean("slr_temporary_stat_bonus_model_v1", true);
      }
   }

   public static void migrate(ServerPlayer player) {
      if (player != null && !player.getPersistentData().getBoolean("slr_temporary_stat_bonus_model_v1")) {
         boolean legacyHaste = player.hasEffect(SololevelingModMobEffects.HASTE_BUFF.get());
         boolean legacyPhysical = player.hasEffect(SololevelingModMobEffects.PHYSICAL_BUFF.get());
         player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            if (legacyHaste) {
               variables.Speed = Math.max(0.0, variables.Speed - 30.0);
            }

            if (legacyPhysical) {
               variables.Strength = Math.max(0.0, variables.Strength - 30.0);
            }

            if (legacyHaste || legacyPhysical) {
               variables.syncPlayerVariables(player);
            }

            player.getPersistentData().putBoolean("slr_temporary_stat_bonus_model_v1", true);
         });
      }
   }
}
