package dev.eness.sololevelingfinal.core.util;

import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;

public final class PlayerVitalSync {
   private static final double CREATIVE_MANA = 1000000.0;
   private static final double ARMOR_PER_VITALITY = 0.06;

   private PlayerVitalSync() {
   }

   public static void applyDerivedAttributes(Player player) {
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
         AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
         if (maxHealth != null) {
            maxHealth.setBaseValue(20.0 + 0.5 * variables.Vitality);
         }

         AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
         if (armor != null) {
            armor.setBaseValue(0.06 * variables.Vitality);
         }
      });
   }

   public static void restoreAfterRespawn(ServerPlayer player) {
      applyDerivedAttributes(player);
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
         variables.Mana = player.isCreative() ? 1000000.0 : 1000.0 + 100.0 * TemporaryStatBonusManager.effectiveIntelligence(player);
         variables.MP = variables.Mana;
         variables.Fatigue = 0.0;
         variables.syncPlayerVariables(player);
      });
      player.setHealth(player.getMaxHealth());
      syncClientState(player);
   }

   public static void refreshClientState(ServerPlayer player) {
      applyDerivedAttributes(player);
      player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> variables.syncPlayerVariables(player));
      syncClientState(player);
   }

   private static void syncClientState(ServerPlayer player) {
      player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), player.getAttributes().getSyncableAttributes()));
      player.connection
         .send(new ClientboundSetHealthPacket(player.getHealth(), player.getFoodData().getFoodLevel(), player.getFoodData().getSaturationLevel()));
   }
}
