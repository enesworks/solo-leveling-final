package dev.eness.sololevelingfinal.core.client.aura;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = "sololeveling", bus = Bus.MOD, value = Dist.CLIENT)
public final class FrostSpiritualizationAuraRegistration {
   public static final String ID = "frost_spiritualization";
   private static final ResourceLocation BLUE_GLOW = new ResourceLocation("sololeveling", "textures/particle/mana_blue.png");

   private FrostSpiritualizationAuraRegistration() {
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(
         () -> {
            if (PlayerAuraRegistry.get("frost_spiritualization") == null) {
               PlayerAuraRegistry.register(
                  new PlayerAuraDefinition(
                     "frost_spiritualization",
                     15924735,
                     5623807,
                     BLUE_GLOW,
                     PlayerAuraDefinition.Facing.HORIZONTAL_CAMERA,
                     0.72F,
                     1.48F,
                     1.08F,
                     0,
                     0,
                     0,
                     new PlayerAuraDefinition.FluidProfile(18, 8, 5, 0.88F, 0.68F, 1.25F, 1.15F, PlayerAuraDefinition.FluidStyle.WHITE_FLAME_HAIR),
                     false,
                     13169919
                  )
               );
            }
         }
      );
   }
}
