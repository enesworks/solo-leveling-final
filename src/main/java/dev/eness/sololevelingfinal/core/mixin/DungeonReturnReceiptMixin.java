package dev.eness.sololevelingfinal.core.mixin;

import dev.eness.sololevelingfinal.core.campaign.CampaignSlrBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import dev.eness.sololevelingfinal.core.procedures.DungeonDimensionPlayerLeavesDimensionProcedure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DungeonDimensionPlayerLeavesDimensionProcedure.class, remap = false)
public abstract class DungeonReturnReceiptMixin {
    @Inject(method = "execute", at = @At("HEAD"))
    private static void sololeveling3$rememberNativeReturn(LevelAccessor world, Entity entity, Entity sourceentity, CallbackInfo ci) {
        CampaignSlrBridge.beforeNativeDungeonReturn(world, entity, sourceentity);
    }

    @Inject(method = "execute", at = @At("RETURN"))
    private static void sololeveling3$captureNativeNormalReturn(LevelAccessor world, Entity entity, Entity sourceentity, CallbackInfo ci) {
        CampaignSlrBridge.onNativeDungeonReturn(world, entity, sourceentity);
    }
}
