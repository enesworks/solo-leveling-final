package dev.eness.sololeveling3.mixin;

import dev.eness.sololeveling3.campaign.CampaignSlrBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.solocraft.procedures.RedGateRightClickedOnEntityProcedure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RedGateRightClickedOnEntityProcedure.class, remap = false)
public abstract class RedGateRightClickGuardMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void sololeveling3$blockCampaignFrostNativeEntry(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (CampaignSlrBridge.shouldBlockNativeFrostGateEntry(entity, sourceentity)) {
            ci.cancel();
        }
    }
}
