package dev.eness.sololevelingfinal.core.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public final class ClassPassiveClientState {
   public static volatile int assassinTempo = 0;
   public static volatile double fighterPower = 0.0;
   public static volatile int tankWallStacks = 0;
   public static volatile int healerResonance = 0;
   public static volatile double rangerFocus = 0.0;

   private ClassPassiveClientState() {
   }

   public static void update(int type, double value) {
      switch (type) {
         case 0:
            assassinTempo = (int)value;
            break;
         case 1:
            fighterPower = value;
            break;
         case 2:
            tankWallStacks = (int)value;
            break;
         case 3:
            healerResonance = (int)value;
            break;
         case 4:
            rangerFocus = value;
      }
   }

   public static void clear() {
      assassinTempo = 0;
      fighterPower = 0.0;
      tankWallStacks = 0;
      healerResonance = 0;
      rangerFocus = 0.0;
   }

   @SubscribeEvent
   public static void onLogout(LoggingOut event) {
      clear();
   }
}
