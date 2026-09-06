package dev.eness.sololevelingfinal.core.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.SololevelingMod;
import dev.eness.sololevelingfinal.core.init.SololevelingModKeyMappings;
import dev.eness.sololevelingfinal.core.network.FrostArchitectureSelectionMessage;
import dev.eness.sololevelingfinal.core.network.SololevelingModVariables;
import dev.eness.sololevelingfinal.core.util.CooldownManager;
import dev.eness.sololevelingfinal.core.util.FrostArchitectureBlueprint;

@EventBusSubscriber(Dist.CLIENT)
public final class FrostArchitectureClientState {
   private static final double RADIANS_PER_PIXEL = 0.0105;
   private static boolean active;
   private static double rotation;
   private static double lastMouseX;
   private static boolean mousePrimed;
   private static Key activationKey;
   private static float lockedYaw;
   private static float lockedPitch;

   private FrostArchitectureClientState() {
   }

   public static void begin(int hotbarSlot) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      if (player != null && minecraft.screen == null) {
         SololevelingModVariables.PlayerVariables vars = variables(player);
         if (vars.combatmode && "Frozen Architecture".equals(vars.PselectedPower)) {
            if (!CooldownManager.isOnCooldown(player, "Frozen Architecture")) {
               if (!active) {
                  active = true;
                  rotation = 0.0;
                  mousePrimed = false;
                  activationKey = hotbarKey(hotbarSlot).getKey();
                  lockedYaw = player.getYRot();
                  lockedPitch = player.getXRot();
                  minecraft.setScreen(new FrostArchitecturePauseScreen());
               }
            }
         }
      }
   }

   public static boolean isActive() {
      return active;
   }

   public static double rotation() {
      return rotation;
   }

   public static FrostArchitectureBlueprint selectedBlueprint() {
      FrostArchitectureBlueprint[] values = FrostArchitectureBlueprint.values();
      double step = (Math.PI * 2) / values.length;
      int selected = Math.floorMod((int)Math.round(-rotation / step), values.length);
      return values[selected];
   }

   public static boolean releaseAndSend() {
      if (!active) {
         return false;
      }

      FrostArchitectureBlueprint selected = selectedBlueprint();
      SololevelingMod.PACKET_HANDLER.sendToServer(new FrostArchitectureSelectionMessage(selected.id()));
      clear();
      return true;
   }

   public static void clear() {
      active = false;
      rotation = 0.0;
      mousePrimed = false;
      activationKey = null;
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.screen instanceof FrostArchitecturePauseScreen) {
         minecraft.setScreen(null);
      }
   }

   static void onPauseScreenRemoved(FrostArchitecturePauseScreen screen) {
      if (active) {
         active = false;
         rotation = 0.0;
         mousePrimed = false;
         activationKey = null;
      }
   }

   static boolean isActivationKey(int keyCode, int scanCode) {
      return active && activationKey != null && activationKey.equals(InputConstants.getKey(keyCode, scanCode));
   }

   public static void updateMouseFromFrame() {
      if (active) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (player != null && minecraft.level != null && player.isAlive()) {
            double mouseX = minecraft.mouseHandler.xpos();
            if (!mousePrimed) {
               lastMouseX = mouseX;
               mousePrimed = true;
            } else {
               double delta = Mth.clamp(mouseX - lastMouseX, -80.0, 80.0);
               lastMouseX = mouseX;
               rotation = Mth.wrapDegrees(Math.toDegrees(rotation + delta * 0.0105));
               rotation = Math.toRadians(rotation);
               player.setYRot(lockedYaw);
               player.setXRot(lockedPitch);
               player.yHeadRot = lockedYaw;
               player.yHeadRotO = lockedYaw;
            }
         } else {
            clear();
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.END && active) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (player != null
            && minecraft.level != null
            && (minecraft.screen == null || minecraft.screen instanceof FrostArchitecturePauseScreen)
            && player.isAlive()) {
            SololevelingModVariables.PlayerVariables vars = variables(player);
            if (vars.combatmode && "Frozen Architecture".equals(vars.PselectedPower)) {
               player.setYRot(lockedYaw);
               player.setXRot(lockedPitch);
               player.yHeadRot = lockedYaw;
               player.yHeadRotO = lockedYaw;
            } else {
               clear();
            }
         } else {
            clear();
         }
      }
   }

   private static SololevelingModVariables.PlayerVariables variables(LocalPlayer player) {
      return player.getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new SololevelingModVariables.PlayerVariables());
   }

   private static KeyMapping hotbarKey(int slot) {
      return switch (slot) {
         case 1 -> SololevelingModKeyMappings.AB_1;
         case 2 -> SololevelingModKeyMappings.AB_2;
         case 3 -> SololevelingModKeyMappings.AB_3;
         case 4 -> SololevelingModKeyMappings.AB_4;
         case 5 -> SololevelingModKeyMappings.AB_5;
         case 6 -> SololevelingModKeyMappings.AB_6;
         case 7 -> SololevelingModKeyMappings.AB_7;
         default -> SololevelingModKeyMappings.AB_8;
      };
   }
}
