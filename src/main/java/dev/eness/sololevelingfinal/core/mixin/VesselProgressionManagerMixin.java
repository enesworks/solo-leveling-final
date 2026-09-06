package dev.eness.sololevelingfinal.core.mixin;

import dev.eness.sololevelingfinal.core.combat.GoGunheeCombatManager;
import net.minecraft.world.entity.Entity;
import dev.eness.sololevelingfinal.core.util.VesselProgressionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = VesselProgressionManager.class, remap = false)
public abstract class VesselProgressionManagerMixin {
    @Inject(method = "unlockedSkills", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$unlockGoGunheeSkills(
            Entity entity,
            int job,
            CallbackInfoReturnable<List<String>> cir
    ) {
        if (job == 8) {
            cir.setReturnValue(GoGunheeCombatManager.unlockedSkills(entity));
        }
    }
}
