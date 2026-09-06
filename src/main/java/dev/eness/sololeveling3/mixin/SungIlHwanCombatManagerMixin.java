package dev.eness.sololeveling3.mixin;

import net.minecraft.world.entity.Entity;
import net.solocraft.network.SololevelingModVariables;
import net.solocraft.util.SungIlHwanCombatManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SungIlHwanCombatManager.class, remap = false)
public abstract class SungIlHwanCombatManagerMixin {
    @Inject(method = "isSungIlHwanVessel", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sololeveling3$removeDeveloperGate(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity == null) {
            return;
        }
        SololevelingModVariables.PlayerVariables vars = entity
                .getCapability(SololevelingModVariables.PLAYER_VARIABLES_CAPABILITY)
                .orElse(new SololevelingModVariables.PlayerVariables());
        if ((int) vars.JOB == 7 && "sung_il_hwan".equals(vars.vesselIdentity)) {
            cir.setReturnValue(true);
        }
    }
}
