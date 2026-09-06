package dev.eness.sololeveling3.mixin;

import dev.eness.sololeveling3.SoloLeveling3;
import net.solocraft.SololevelingMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Starts the private expansion from SLR's sole visible mod container. */
@Mixin(value = SololevelingMod.class, remap = false)
public abstract class SololevelingModBootstrapMixin {
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void sololeveling3$bootstrapPrivateExpansion(CallbackInfo ci) {
        SoloLeveling3.bootstrap();
    }
}
