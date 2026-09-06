package dev.eness.sololeveling3.mixin;

import dev.eness.sololeveling3.combat.GoGunheeCombatManager;
import net.minecraft.resources.ResourceLocation;
import net.solocraft.client.screens.DisplayOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DisplayOverlay.class, remap = false)
public abstract class DisplayOverlayMixin {
    @Inject(method = "getSkillTexture", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$goGunheeIcons(
            String skillName,
            boolean avariceHeld,
            CallbackInfoReturnable<ResourceLocation> cir
    ) {
        ResourceLocation texture = switch (skillName) {
            case GoGunheeCombatManager.BRILLIANT_REINFORCEMENT ->
                    ResourceLocation.fromNamespaceAndPath("sololeveling", "textures/screens/icon_reinforcement.png");
            case GoGunheeCombatManager.RULERS_IMPACT ->
                    ResourceLocation.fromNamespaceAndPath("sololeveling", "textures/screens/icon_groundslam.png");
            case GoGunheeCombatManager.FRAGMENTS_GUARD ->
                    ResourceLocation.fromNamespaceAndPath("sololeveling", "textures/screens/icon_protectionmark.png");
            case GoGunheeCombatManager.BRILLIANT_MANIFESTATION ->
                    ResourceLocation.fromNamespaceAndPath("sololeveling", "textures/screens/icon_spiritualize_goliath.png");
            default -> null;
        };
        if (texture != null) {
            cir.setReturnValue(texture);
        }
    }
}
