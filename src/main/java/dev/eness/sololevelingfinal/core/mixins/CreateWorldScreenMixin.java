package dev.eness.sololevelingfinal.core.mixins;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import dev.eness.sololevelingfinal.core.client.gui.worldcreation.SoloLevelingWorldCreationTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = CreateWorldScreen.class, priority = 800)
public abstract class CreateWorldScreenMixin {
   @ModifyArg(
      method = "init",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/components/tabs/TabNavigationBar$Builder;addTabs([Lnet/minecraft/client/gui/components/tabs/Tab;)Lnet/minecraft/client/gui/components/tabs/TabNavigationBar$Builder;"
      ),
      index = 0,
      require = 0
   )
   private Tab[] sololeveling$appendWorldCreationTab(Tab[] existingTabs) {
      for (Tab tab : existingTabs) {
         if (tab instanceof SoloLevelingWorldCreationTab) {
            return existingTabs;
         }
      }

      Tab[] expandedTabs = Arrays.copyOf(existingTabs, existingTabs.length + 1);
      CreateWorldScreen screen = (CreateWorldScreen)this;
      expandedTabs[existingTabs.length] = new SoloLevelingWorldCreationTab(Minecraft.getInstance().font, screen.getUiState());
      return expandedTabs;
   }
}
