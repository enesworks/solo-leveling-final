package dev.eness.sololevelingfinal.core.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import dev.eness.sololevelingfinal.core.item.GriamoreItem;
import dev.eness.sololevelingfinal.core.item.KangsDaggerItem;
import dev.eness.sololevelingfinal.core.item.ManaGunItem;
import software.bernie.geckolib.animatable.GeoItem;

@EventBusSubscriber
public class ItemAnimationFactory {
   public static void disableUseAnim() {
      try {
         ItemInHandRenderer renderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
         if (renderer != null) {
            renderer.mainHandHeight = 1.0F;
            renderer.oMainHandHeight = 1.0F;
            renderer.offHandHeight = 1.0F;
            renderer.oOffHandHeight = 1.0F;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @SubscribeEvent
   public static void animatedItems(PlayerTickEvent event) {
      String animation = "";
      if (event.phase == Phase.START
         && (event.player.getMainHandItem().getItem() instanceof GeoItem || event.player.getOffhandItem().getItem() instanceof GeoItem)) {
         if (!event.player.getMainHandItem().getOrCreateTag().getString("geckoAnim").equals("")
            && !(event.player.getMainHandItem().getItem() instanceof ArmorItem)) {
            animation = event.player.getMainHandItem().getOrCreateTag().getString("geckoAnim");
            event.player.getMainHandItem().getOrCreateTag().putString("geckoAnim", "");
            if (event.player.getMainHandItem().getItem() instanceof ManaGunItem animatable && event.player.level().isClientSide()) {
               animatable.animationprocedure = animation;
               disableUseAnim();
            }

            if (event.player.getMainHandItem().getItem() instanceof GriamoreItem animatable && event.player.level().isClientSide()) {
               animatable.animationprocedure = animation;
               disableUseAnim();
            }

            if (event.player.getMainHandItem().getItem() instanceof KangsDaggerItem animatable && event.player.level().isClientSide()) {
               animatable.animationprocedure = animation;
               disableUseAnim();
            }
         }

         if (!event.player.getOffhandItem().getOrCreateTag().getString("geckoAnim").equals("")
            && !(event.player.getOffhandItem().getItem() instanceof ArmorItem)) {
            animation = event.player.getOffhandItem().getOrCreateTag().getString("geckoAnim");
            event.player.getOffhandItem().getOrCreateTag().putString("geckoAnim", "");
            if (event.player.getOffhandItem().getItem() instanceof ManaGunItem animatable && event.player.level().isClientSide()) {
               animatable.animationprocedure = animation;
               disableUseAnim();
            }

            if (event.player.getOffhandItem().getItem() instanceof GriamoreItem animatable && event.player.level().isClientSide()) {
               animatable.animationprocedure = animation;
               disableUseAnim();
            }

            if (event.player.getOffhandItem().getItem() instanceof KangsDaggerItem animatable && event.player.level().isClientSide()) {
               animatable.animationprocedure = animation;
               disableUseAnim();
            }
         }
      }
   }
}
