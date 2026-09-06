package dev.eness.sololevelingfinal.core.mixin;

import java.util.Set;
import dev.eness.sololevelingfinal.core.util.VesselManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Exposes the three vessel paths that SLR 1.2.0 already implements but marks as
 * work in progress. The injection is deliberately limited to the exact three
 * identities agreed for this private build.
 */
@Mixin(value = VesselManager.class, remap = false)
public abstract class VesselManagerMixin {
    private static final Set<String> SOLOLEVELING3_COMPLETED_VESSELS = Set.of(
        "christopher_reed",
        "sung_il_hwan",
        "go_gunhee"
    );

    @Inject(method = "isWorkInProgress", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$exposeCompletedVessels(
        VesselManager.VesselDefinition definition,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (definition != null && SOLOLEVELING3_COMPLETED_VESSELS.contains(definition.identity())) {
            cir.setReturnValue(false);
        }
    }
}
